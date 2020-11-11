package pl.marcinchwedczuk.shuntingyard;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class Ast {
    public final AstType operator;
    public final String functionName;
    public final Ast[] args;
    public final int value;

    public Ast(int value) {
        this.operator = AstType.NUMBER;
        this.value = value;
        this.functionName = null;
        this.args = null;
    }

    public Ast(AstType unaryOp, Ast child) {
        this.operator = unaryOp;
        this.args = new Ast[] { child };
        this.functionName = null;
        this.value = Integer.MIN_VALUE;
    }

    public Ast(AstType binaryOp, Ast left, Ast right) {
        this.operator = binaryOp;
        this.args = new Ast[] { left, right };
        this.functionName = null;
        this.value = Integer.MIN_VALUE;
    }

    public Ast(String functionName, Ast... args) {
        this.operator = AstType.CALL_FUNCTION;
        this.args = args;
        this.functionName = functionName;
        this.value = Integer.MIN_VALUE;
    }

    public String toRPN() {
        return switch (operator) {
            case NUMBER -> Integer.toString(value);
            case UNARY_MINUS, UNARY_PLUS ->
                    args[0].toRPN() + " " + operatorString(operator, true);
            case PLUS, MINUS, MULTIPLY, DIVIDE, POWER ->
                    args[0].toRPN() + " " + args[1].toRPN() + " " + operatorString(operator, true);
            case CALL_FUNCTION ->
                    Arrays.stream(args)
                            .map(Ast::toRPN)
                            .collect(joining(" ")) + " " + functionName;
        };
    }

    public String toReadableExpr(int outsidePriority) {
        return switch (operator) {
            case NUMBER ->
                    Integer.toString(value);

            case UNARY_MINUS, UNARY_PLUS ->
                    parensIf(operator.priority() <= outsidePriority,
                            String.format("%s%s", operatorString(operator, false), args[0].toReadableExpr(operator.priority())));

            case PLUS, MINUS, MULTIPLY, DIVIDE, POWER ->
                    parensIf(operator.priority() <= outsidePriority,
                        String.format("%s %s %s",
                                args[0].toReadableExpr(operator.priority()),
                                operatorString(operator, false),
                                args[1].toReadableExpr(operator.priority())));

            case CALL_FUNCTION ->
                    String.format("%s(%s)", functionName,
                            Arrays.stream(args)
                                    .map(a -> a.toReadableExpr(0))
                                    .collect(joining(", ")));
        };
    }

    private String parensIf(boolean cond, String s) {
        return cond ? "(" + s + ")" : s;
    }

    private static String operatorString(AstType type, boolean specialUnary) {
        return switch (type) {
            case UNARY_MINUS -> specialUnary ? "un-" : "-";
            case UNARY_PLUS -> specialUnary ? "un+" : "+";
            case PLUS -> "+";
            case MINUS -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            case POWER -> "^";
            default -> null;
        };
    }
}
