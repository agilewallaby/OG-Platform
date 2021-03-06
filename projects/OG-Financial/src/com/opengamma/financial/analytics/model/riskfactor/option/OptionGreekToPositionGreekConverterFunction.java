/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.riskfactor.GreekDataBundle;
import com.opengamma.analytics.financial.riskfactor.GreekToPositionGreekConverter;
import com.opengamma.analytics.financial.sensitivity.PositionGreek;
import com.opengamma.analytics.financial.trade.OptionTradeData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.greeks.AvailablePositionGreeks;

/**
 * 
 */
public class OptionGreekToPositionGreekConverterFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(OptionGreekToPositionGreekConverterFunction.class);
  private final Function1D<GreekDataBundle, Map<PositionGreek, Double>> _converter = new GreekToPositionGreekConverter();
  //TODO pass in required greek rather than using the entire set
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final GreekResultCollection greekResultCollection = new GreekResultCollection();
    Object greekResult;    
    Greek greek;
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      greekResult = inputs.getValue(new ValueRequirement(valueName, target.getSecurity()));
      if (greekResult == null) {
        s_logger.warn("Could not get value for " + valueName);
      }
      if (!(greekResult instanceof Double)) {
        throw new IllegalArgumentException("Can only handle Double greeks.");
      }
      greek = AvailableGreeks.getGreekForValueRequirementName(valueName);
      greekResultCollection.put(greek, (Double) greekResult);
    }
    final GreekDataBundle dataBundle = new GreekDataBundle(greekResultCollection, null, new OptionTradeData(target.getPosition().getQuantity().doubleValue(), 25));
    final Map<PositionGreek, Double> positionGreeks = _converter.evaluate(dataBundle);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    PositionGreek positionGreek;
    Double positionGreekResult;
    ValueSpecification resultSpecification;
    ComputedValue resultValue;
    for (final ValueRequirement dV : desiredValues) {
      positionGreek = AvailablePositionGreeks.getPositionGreekForValueRequirementName(dV.getValueName());
      positionGreekResult = positionGreeks.get(positionGreek);
      resultSpecification = new ValueSpecification(new ValueRequirement(dV.getValueName(), target.getSecurity()), getUniqueId());
      resultValue = new ComputedValue(resultSpecification, positionGreekResult);
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (final String valueName : AvailableGreeks.getAllGreekNames()) {
        requirements.add(new ValueRequirement(valueName, target.getSecurity()));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final String valueName : AvailablePositionGreeks.getAllPositionGreekNames()) {
        results.add(new ValueSpecification(
            new ValueRequirement(valueName, target.getSecurity()),
            getUniqueId()));
      }
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GreekToPositionGreekConverter";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
