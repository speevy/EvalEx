package com.ezylang.evalex.functions;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public abstract class AbstractNumericFunction extends AbstractFunction {

    protected DoubleStream recursiveDoubles(Stream<EvaluationValue> values, ExpressionConfiguration configuration) {
        return values.flatMapToDouble(parameter -> {
            if (parameter.isArrayValue()) {
                return recursiveDoubles(parameter.getArrayValue().stream(), configuration);
            } else {
                return DoubleStream.of(parameter.getDoubleValue());
            }
        });
    }
}
