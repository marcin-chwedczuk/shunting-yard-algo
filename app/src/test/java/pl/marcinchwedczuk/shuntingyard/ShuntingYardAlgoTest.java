package pl.marcinchwedczuk.shuntingyard;

import com.google.common.collect.Streams;
import org.junit.Test;

import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ShuntingYardAlgoTest {
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

    private void checkCase(String input, String expected) {
        var result = ShuntingYardAlgo.toRPN(input);
        assertEquals(expected, result);
    }
}