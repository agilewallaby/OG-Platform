/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class FXForwardCurveFromYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
        .withAny(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
        .withAny(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR)
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> fxForwardCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    //    final Set<String> forwardCurveInterpolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    //    if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
    //      return null;
    //    }
    //    final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    //    if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
    //      return null;
    //    }
    //    final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    //    if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
    //      return null;
    //    }
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final ValueProperties payCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, payCurveNames.iterator().next())
        .withOptional(ValuePropertyNames.PAY_CURVE)
        .get();
    final ValueProperties receiveCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, receiveCurveNames.iterator().next())
        .withOptional(ValuePropertyNames.RECEIVE_CURVE)
        .get();
    final UnorderedCurrencyPair ccyPair = UnorderedCurrencyPair.of(target.getUniqueId());
    Currency payCurrency;
    Currency receiveCurrency;
    if (FXUtils.isInBaseQuoteOrder(ccyPair.getFirstCurrency(), ccyPair.getSecondCurrency())) {
      payCurrency = ccyPair.getFirstCurrency();
      receiveCurrency = ccyPair.getSecondCurrency();
    } else {
      payCurrency = ccyPair.getSecondCurrency();
      receiveCurrency = ccyPair.getFirstCurrency();
    }
    //    final ValueProperties spotProperties = ValueProperties.builder()
    //        .with(PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolatorNames.iterator().next())
    //        .with(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolatorNames.iterator().next())
    //        .with(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolatorNames.iterator().next()).get();
    final ExternalId spotIdentifier = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    result.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, payCurrency.getUniqueId(), payCurveProperties));
    result.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, receiveCurrency.getUniqueId(), receiveCurveProperties));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier));
    return result;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    Object payCurveObject = null;
    Object receiveCurveObject = null;
    Set<String> payCurveNames = null;
    Set<String> receiveCurveNames = null;
    final UnorderedCurrencyPair ccyPair = UnorderedCurrencyPair.of(target.getUniqueId());
    Currency payCurrency;
    Currency receiveCurrency;
    if (FXUtils.isInBaseQuoteOrder(ccyPair.getFirstCurrency(), ccyPair.getSecondCurrency())) {
      payCurrency = ccyPair.getFirstCurrency();
      receiveCurrency = ccyPair.getSecondCurrency();
    } else {
      payCurrency = ccyPair.getSecondCurrency();
      receiveCurrency = ccyPair.getFirstCurrency();
    }
    for (final ComputedValue input : inputs.getAllValues()) {
      final ValueSpecification spec = input.getSpecification();
      final ValueProperties properties = spec.getProperties();
      if (spec.getTargetSpecification().getUniqueId().equals(payCurrency.getUniqueId())) {
        payCurveObject = input.getValue();
        payCurveNames = properties.getValues(ValuePropertyNames.CURVE);
      } else if (spec.getTargetSpecification().getUniqueId().equals(receiveCurrency.getUniqueId())) {
        receiveCurveObject = input.getValue();
        receiveCurveNames = properties.getValues(ValuePropertyNames.CURVE);
      }
    }
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveInterpolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_INTERPOLATOR).iterator().next();
    final String forwardCurveLeftExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR).iterator().next();
    final String forwardCurveRightExtrapolatorName = constraints.getValues(PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR).iterator().next();
    if (payCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get pay curve");
    }
    if (receiveCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get receive curve");
    }
    if (payCurveNames == null || payCurveNames.size() != 1) {
      throw new OpenGammaRuntimeException("Null or non-unique curve name: " + payCurveNames);
    }
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      throw new OpenGammaRuntimeException("Null or non-unique curve name: " + receiveCurveNames);
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> fxForwardCurveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
      throw new OpenGammaRuntimeException("Null or non-unique FX forward curve names: " + fxForwardCurveNames);
    }
    final ExternalId spotIdentifier = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    final Object spotObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot");
    }
    final double spot = (Double) spotObject;
    final YieldCurve payCurve = (YieldCurve) payCurveObject;
    final YieldCurve receiveCurve = (YieldCurve) receiveCurveObject;
    final String fxForwardCurveName = fxForwardCurveNames.iterator().next();
    final ForwardCurve fxForwardCurve = new ForwardCurveYieldImplied(spot, payCurve, receiveCurve);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, fxForwardCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FXForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)
        .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
        .with(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, forwardCurveLeftExtrapolatorName)
        .with(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, forwardCurveRightExtrapolatorName)
        .with(FXForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR, forwardCurveInterpolatorName)
        .get();
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(resultSpec, fxForwardCurve));
  }

}