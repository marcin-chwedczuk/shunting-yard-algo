package pl.marcinchwedczuk.shuntingyard;

import com.google.common.collect.Streams;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class ShuntingYardAlgo {
    public static String toRPN(String input) {
        var tokenizer = new StringTokenizer(input, "+-*/^(),", true);
        var tokens = Streams.stream(tokenizer.asIterator())
                .map(String.class::cast)
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(toList());

        var algo = new ShuntingYardAlgo(new ArrayDeque<>(tokens));
        return algo.toRPN();
    }

    private final static Pattern numberRegex = Pattern.compile("^\\d+$");
    private final static Pattern identifierRegex = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]*$");

    private final Stack<String> operatorStack = new Stack<>();
    private final Queue<String> outputQueue = new ArrayDeque<>();
    private final Queue<String> inputQueue;

    public ShuntingYardAlgo(Queue<String> inputQueue) {
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

    private boolean isOperator(String s) {
        return switch (s) {
            case "+","-","*","/","^" -> true;
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
            case "^" -> 20;
            default -> throw new IllegalArgumentException("not an operator: '" + operator + "'");
        };
    }

    private String associativity(String operator) {
        return switch (operator) {
            case "+","-","*","/" -> "left";
            case "^" -> "right";
            default -> throw new IllegalArgumentException("not an operator: '" + operator + "'");
        };
    }
}
