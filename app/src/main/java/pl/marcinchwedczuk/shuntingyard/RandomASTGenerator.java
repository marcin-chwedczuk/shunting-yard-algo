package pl.marcinchwedczuk.shuntingyard;

import com.google.common.collect.Iterators;

import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class RandomASTGenerator {
    private final Random random = new Random();

    private final int maxDepth = 10;

    private final Map<AstType, Integer> operatorProbability = Map.of(
            AstType.UNARY_MINUS, 1,
            AstType.UNARY_PLUS, 1,
            AstType.PLUS, 2,
            AstType.MINUS, 2,
            AstType.DIVIDE, 2,
            AstType.MULTIPLY, 2,
            AstType.POWER, 2,
            AstType.NUMBER, 7,
            AstType.CALL_FUNCTION, 1
    );

    private final Integer operatorTotal = operatorProbability.values().stream()
            .mapToInt(Integer::intValue)
            .sum();

    private final Map<String, Integer> availableFunctions = Map.of(
            // name, arity
            "SIN", 1,
            "COS", 1,
            "EXP", 1,
            "LN", 1,
            "POW", 2,
            "IFF", 3
    );

    public Ast generateTree() {
        return generateTree(0);
    }

    private Ast generateTree(int level) {
        if (level >= maxDepth) {
            return generateNumber();
        }

        AstType type = drawAstType();
        return switch (type) {
            case UNARY_MINUS, UNARY_PLUS -> generateUnary(type, level);
            case PLUS, MINUS, MULTIPLY, DIVIDE, POWER -> generateBinary(type, level);
            case CALL_FUNCTION -> generateFunctionCall(level);
            case NUMBER -> generateNumber();
        };
    }

    private Ast generateNumber() {
        return new Ast(random.nextInt(10));
    }

    private Ast generateFunctionCall(int level) {
        int index = random.nextInt(availableFunctions.size());
        var entry = Iterators.get(availableFunctions.entrySet().iterator(), index);

        var args = Stream.generate(() -> generateTree(level + 1))
                .limit(entry.getValue())
                .toArray(Ast[]::new);

        return new Ast(entry.getKey(), args);
    }

    private Ast generateBinary(AstType type, int level) {
        return new Ast(type, generateTree(level+1), generateTree(level+1));
    }

    private Ast generateUnary(AstType type, int level) {
        return new Ast(type, generateTree(level+1));
    }

    private AstType drawAstType() {
        double r = random.nextDouble()*operatorTotal;

        for (var entry: operatorProbability.entrySet()) {
            if (entry.getValue() >= r) {
                return entry.getKey();
            }
            r -= entry.getValue();
        }

        return drawAstType(); // numerical error, repeat draw
    }
}
