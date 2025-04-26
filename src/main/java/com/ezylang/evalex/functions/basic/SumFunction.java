/*
  Copyright 2012-2022 Udo Klimaschewski

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.ezylang.evalex.functions.basic;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractNumericFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;
import java.math.BigDecimal;
import java.util.stream.Stream;

/** Returns the sum value of all parameters. */
@FunctionParameter(name = "value", isVarArg = true)
public class SumFunction extends AbstractNumericFunction {
  @Override
  public EvaluationValue evaluate(
      Expression expression, Token functionToken, EvaluationValue... parameterValues) {
    switch (expression.getConfiguration().getInternalNumberRepresentation()) {
      case BIG_DECIMAL:
        return sumBigDecimal(expression, parameterValues);
      case DOUBLE:
        return sumDouble(expression, parameterValues);
    }
    throw new UnsupportedOperationException("Unsupported number representation: "
            + expression.getConfiguration().getInternalNumberRepresentation());
  }

  private EvaluationValue sumBigDecimal(Expression expression, EvaluationValue[] parameterValues) {
    BigDecimal sum = BigDecimal.ZERO;
    for (EvaluationValue parameter : parameterValues) {
      sum =
              sum.add(
                      recursiveSum(parameter, expression), expression.getConfiguration().getMathContext());
    }
    return expression.convertValue(sum);
  }

  private BigDecimal recursiveSum(EvaluationValue parameter, Expression expression) {
    BigDecimal sum = BigDecimal.ZERO;
    if (parameter.isArrayValue()) {
      for (EvaluationValue element : parameter.getArrayValue()) {
        sum =
            sum.add(
                recursiveSum(element, expression), expression.getConfiguration().getMathContext());
      }
    } else {
      sum = sum.add(parameter.getNumberValue(), expression.getConfiguration().getMathContext());
    }
    return sum;
  }

  private EvaluationValue sumDouble(Expression expression, EvaluationValue[] parameterValues) {
    return expression.convertValue(recursiveDoubles(Stream.of(parameterValues), expression.getConfiguration()).sum());
  }
}
