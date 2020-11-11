package pl.marcinchwedczuk.shuntingyard;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * RPN does not support unary operators out of the box.
 * Consider this example:
 *      2 + (-5)
 * How should we write it in RPN:
 *      2 5 - +
 * can represent both
 *      2 + (-5)
 *      +(2 - 5)
 * The result will be the same but the AST and used operators
 * will not be unique.
 *
 * To solve this problem we need to introduce a new operators
 * unary+ (un+) and unary- (un-) and change our tokenizer to
 * produce these new operators.
 *
 * un+ and un- can only occur:
 * - at the beginning of the expression e.g. -5
 * - right after left parentheses e.g. (-5)
 * - after some other operator e.g. 3 * -5
 * - after , in a function call e.g. pow(2, -5)
 *
 */
public class ShuntingYardAlgorithm {
    @VisibleForTesting
    public static List<String> tokenize(String input) {
        var tokenizer = new StringTokenizer(input, "+-*/^(),", true);

        var tokens = Streams.stream(tokenizer.asIterator())
                .map(String.class::cast)
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(toList());

        var transformedTokens = new ArrayList<String>(tokens.size());

        boolean allowUnary = true;
        for (var token: tokens) {
            if (allowUnary && ("-".equals(token) || "+".equals(token))) {
                token = "un" + token;
            }
            else {
                allowUnary = "(".equals(token) || ",".equals(token) || isOperator(token);
            }
            transformedTokens.add(token);
        }

        return transformedTokens;
    }

    public static String toRPN(String input) {
        var tokens = tokenize(input);
        var algorithm = new ShuntingYardAlgorithm(new ArrayDeque<>(tokens));
        return algorithm.toRPN();
    }

    private final static Pattern numberRegex = Pattern.compile("^\\d+$");
    private final static Pattern identifierRegex = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]*$");

    private final Stack<String> operatorStack = new Stack<>();
    private final Queue<String> outputQueue = new ArrayDeque<>();
    private final Queue<String> inputQueue;

    public ShuntingYardAlgorithm(Queue<String> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String toRPN() {
        while (!inputQueue.isEmpty()) {
            var elem = inputQueue.poll();

            if (elem.equals("(")) {
                operatorStack.push(elem);
            }
            else if (elem.equals(")")) {
                while (true) {
                    String op = operatorStack.pop();
                    if (op.equals("(")) break;
                    outputQueue.offer(op);
                }

                if (!operatorStack.isEmpty() && isFunctionName(operatorStack.peek())) {
                    outputQueue.offer(operatorStack.pop());
                }
            }
            else if (elem.equals(",")) {
                while (!operatorStack.peek().equals("(")) {
                    outputQueue.offer(operatorStack.pop());
                }
                // TODO: Handle error
            }
            else if (isOperator(elem)) {
                while (!operatorStack.isEmpty() &&
                        ( (precedence(operatorStack.peek()) > precedence(elem)) ||
                          (precedence(operatorStack.peek()) == precedence(elem) && associativity(operatorStack.peek()).equals("left"))
                        ))
                {
                    outputQueue.offer(operatorStack.pop());
                }
                operatorStack.push(elem);
            }
            else if (isNumber(elem)) {
                outputQueue.offer(elem);
            }
            else if (isFunctionName(elem)) {
                operatorStack.push(elem);
            }
            else {
                throw new IllegalArgumentException("Unknown token: '" + elem + "'");
            }
        }

        while (!operatorStack.isEmpty()) {
            outputQueue.offer(operatorStack.pop());
        }

        return String.join(" ", outputQueue);
    }

    private boolean isNumber(String s) {
        return numberRegex.asMatchPredicate().test(s);
    }

    private boolean isFunctionName(String s) {
        return identifierRegex.asMatchPredicate().test(s);
    }

    private static boolean isOperator(String s) {
        return switch (s) {
            case "+","-","*","/","^" -> true;
            case "un-","un+" -> true;
            default -> false;
        };
    }

    // the higher number, the most strongly operator binds its arguments
    private int precedence(String operator) {
        if (isFunctionName(operator)) {
            return 100;
        }

        return switch (operator) {
            // parentheses are special case - we shouldn't drop them from stack
            case "(", ")" -> 0;
            case "+","-" -> 5;
            case "*","/" -> 10;
            // Be careful with negation and raising to power:
            // -2^2 == -(2^2)
            // -2^-2^-2 = -(2^-(2^-2))
            case "un+","un-" -> 15;
            case "^" -> 20;
            default -> throw new IllegalArgumentException("not an operator: '" + operator + "'");
        };
    }

    private String associativity(String operator) {
        return switch (operator) {
            case "+","-","*","/" -> "left";
            // We use "right" for unary operators. From math POV it really doesn't matter, but
            // we need to use "right" to prevent pop'ing them from the stack when next unary operator
            // is coming.
            case "un+", "un-" -> "right";
            case "^" -> "right";
            default -> throw new IllegalArgumentException("not an operator: '" + operator + "'");
        };
    }
}
