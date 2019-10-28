package kilanny.muslimalarm.util;

import com.udojava.evalex.Expression;

import java.util.Random;

public final class MathExpressionManager {

    private MathExpressionManager() { }

    /**
     * Generates an expression whose evaluation result is an integer greater than zero
     *
     * @param level
     * @return
     */
    public static String generateExpression(int level) {
        if (level < 1 || level > 7)
            throw new IllegalArgumentException();
        Random random = new Random();
        switch (level) {
            case 1:
                return (1 + random.nextInt(9)) + "+" + random.nextInt(10);
            case 2:
                return Math.max(10, random.nextInt(100))
                        + "+" + Math.max(10, random.nextInt(100));
            case 3:
                return Math.max(10, random.nextInt(100))
                        + "+" + Math.max(10, random.nextInt(100))
                        + "+" + Math.max(10, random.nextInt(100));
            case 4:
                return "(" + Math.max(10, random.nextInt(100))
                        + "x" + random.nextInt(10)
                        + ")+" + Math.max(10, random.nextInt(100));
            case 5:
                return "(" + Math.max(10, random.nextInt(100))
                        + "x" + Math.max(10, random.nextInt(100))
                        + ")+" + Math.max(100, random.nextInt(1000));
            case 6:
                return "(" + Math.max(100, random.nextInt(1000))
                        + "x" + Math.max(10, random.nextInt(100))
                        + ")+" + Math.max(1000, random.nextInt(10000));
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int solveExpression(String expression) {
        return new Expression(expression.replace("x", "*"))
                .eval().intValue();
    }
}
