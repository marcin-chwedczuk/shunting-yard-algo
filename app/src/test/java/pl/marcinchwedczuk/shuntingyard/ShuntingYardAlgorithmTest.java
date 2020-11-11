package pl.marcinchwedczuk.shuntingyard;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShuntingYardAlgorithmTest {
    @Test
    public void single_number_works() {
        checkCase("1", "1");
    }

    @Test
    public void addition_works() {
        checkCase("1 + 2", "1 2 +");
        checkCase("1 + 2 + 3", "1 2 + 3 +");
        checkCase("1 + 2 + 3 + 4", "1 2 + 3 + 4 +");
    }

    @Test
    public void subtraction_works() {
        checkCase("1 - 2", "1 2 -");
        checkCase("1 - 2 - 3", "1 2 - 3 -");
        checkCase("1 - 2 - 3 - 4", "1 2 - 3 - 4 -");
    }

    @Test
    public void multiplication_works() {
        checkCase("1 * 2", "1 2 *");
        checkCase("1 * 2 * 3", "1 2 * 3 *");
        checkCase("1 * 2 * 3 * 4", "1 2 * 3 * 4 *");
    }

    @Test
    public void division_works() {
        checkCase("1 / 2", "1 2 /");
        checkCase("1 / 2 / 3", "1 2 / 3 /");
        checkCase("1 / 2 / 3 / 4", "1 2 / 3 / 4 /");
    }

    @Test
    public void pow_works() {
        checkCase("2^3", "2 3 ^");
        checkCase("2^3^4", "2 3 4 ^ ^");
        checkCase("2^3^4^5", "2 3 4 5 ^ ^ ^");
    }

    @Test
    public void parentheses_work() {
        checkCase("(1 + 2) * 3", "1 2 + 3 *");
        checkCase("3 * (1 + 2)", "3 1 2 + *");
        checkCase("(1 + 2) * (3 + 4)", "1 2 + 3 4 + *");
    }

    @Test
    public void mixing_operators_works() {
        checkCase("1 * 2 / 3 + 7 / 8", "1 2 * 3 / 7 8 / +");
        checkCase("1 + 2 / 3 - 4", "1 2 3 / + 4 -");
    }

    @Test
    public void functions_work() {
        checkCase("sin(3)", "3 sin");
        checkCase("cos(sin(3))", "3 sin cos");
        checkCase("sin(3 + 7*cos(8))", "3 7 8 cos * + sin");

        checkCase("pow(2,7)", "2 7 pow");
        checkCase("exp(2, exp(3,8))", "2 3 8 exp exp");
        checkCase("iff(1, iff(1, 2, 3), 4)", "1 1 2 3 iff 4 iff");
    }

    @Test
    public void functions_complex_arguments() {
        checkCase("sin(3+7*8/cos(2))", "3 7 8 * 2 cos / + sin");
        checkCase("pow(2+3, 4+5)", "2 3 + 4 5 + pow");
    }

    @Test
    public void unary_operators() {
        checkCase("-5", "5 un-");
        checkCase("+5", "5 un+");
        checkCase("---5", "5 un- un- un-");

        checkCase("3 * -7", "3 7 un- *");

        checkCase("(-5)", "5 un-");
        checkCase("sin(-5)", "5 un- sin");
        checkCase("pow(2,-5)", "2 5 un- pow");
    }

    private void checkCase(String input, String expected) {
        var result = ShuntingYardAlgorithm.toRPN(input);
        assertEquals(expected, result);
    }

    @Test
    public void tokenize_handle_unary_operators() {
        checkTokenizer("-5", "un- 5");
        checkTokenizer("-+-+5", "un- un+ un- un+ 5");
        checkTokenizer("(-5)", "( un- 5 )");
        checkTokenizer("(3 + -4)", "( 3 + un- 4 )");
        checkTokenizer("sin(-5, 2 + -5)", "sin ( un- 5 , 2 + un- 5 )");
        checkTokenizer("exp(2, -5)", "exp ( 2 , un- 5 )");
    }

    private void checkTokenizer(String input, String expectedTokens) {
        var tokens = ShuntingYardAlgorithm.tokenize(input);
        assertEquals(expectedTokens, String.join(" ", tokens));
    }
}