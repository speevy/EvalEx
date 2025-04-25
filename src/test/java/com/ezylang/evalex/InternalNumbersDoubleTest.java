package com.ezylang.evalex;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ezylang.evalex.config.ExpressionConfiguration.InternalNumberRepresentation.DOUBLE;
import static org.assertj.core.api.Assertions.assertThat;


class InternalNumbersDoubleTest {

    @Test
    void testSumWithDouble() throws EvaluationException, ParseException {
        // Given
        Random random = new Random();
        String expressionString = "sum(" + IntStream.range(0, 10_000)
                .mapToObj(i -> "v" + i)
                .collect(Collectors.joining(",")) + ")";

        var values = IntStream.range(0, 10_000).boxed()
                .collect(Collectors.toMap(i -> "v" + i, i-> random.nextDouble() * 100.0));

        var configuration = ExpressionConfiguration.builder().internalNumberRepresentation(DOUBLE).build();
        var configuration2 = ExpressionConfiguration.builder().mathContext(new MathContext(10)).build();

        // Warm up
        var warmUpValues = Map.of("a", 1.0, "b", 2.0);
        var warmUpExpression = "sum(a,b)";
        new Expression(warmUpExpression, configuration).withValues(warmUpValues).evaluate().getDoubleValue();
        new Expression(warmUpExpression, configuration2).withValues(warmUpValues).evaluate().getNumberValue();


        // When
        long bigdecimalTime = 0L;
        long doubleTime = 0L;
        for (int i = 0; i < 50; i++) {
            long t0 = System.nanoTime();
            Expression expressionDouble = new Expression(expressionString, configuration);

            double resultDouble = expressionDouble.withValues(values).evaluate().getDoubleValue();

            long t1 = System.nanoTime();

            Expression expressionBigDecimal = new Expression(expressionString, configuration2);
            BigDecimal resultBigDecimal = expressionBigDecimal.withValues(values).evaluate().getNumberValue();

            long t2 = System.nanoTime();

            // Then
            assertThat(resultDouble).isCloseTo(resultBigDecimal.doubleValue(), Offset.offset(1.0));
            bigdecimalTime += t2 - t1;
            doubleTime += t1 - t0;
        }

        System.out.println("SpeedUp: " + calculateSpeedUp(bigdecimalTime, doubleTime) + "%");
        assertThat(doubleTime).isLessThan(bigdecimalTime);
    }

    private static double calculateSpeedUp(long bigdecimalTime, long doubleTime) {
        return (((double) bigdecimalTime) / ((double) doubleTime) * 100.0) - 100.0;
    }

}
