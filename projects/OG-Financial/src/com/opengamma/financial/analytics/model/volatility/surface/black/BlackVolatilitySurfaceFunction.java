/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.SurfacePropertyNames;

/**
 *
 */
public abstract class BlackVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(SURFACE);
    final String curveCalculationMethodName = desiredValue.getConstraint(CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final Object interpolatorObject = inputs.getValue(getInterpolatorRequirement(target, desiredValue));
    if (interpolatorObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface interpolator");
    }
    final VolatilitySurfaceInterpolator surfaceInterpolator = (VolatilitySurfaceInterpolator) interpolatorObject;
    final SmileSurfaceDataBundle data = getData(inputs, getVolatilityDataRequirement(target, surfaceName, getInstrumentType(), getSurfaceQuoteType(), getSurfaceQuoteUnits()),
        getForwardCurveRequirement(target, curveCalculationMethodName, forwardCurveName));
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = surfaceInterpolator.getVolatilitySurface(data);
    final ValueProperties properties = getResultProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PRIMITIVE && isCorrectIdType(target);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String forwardCurveCalculationMethod = forwardCurveCalculationMethods.iterator().next();
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(target, forwardCurveCalculationMethod, forwardCurveName);
    final ValueRequirement volatilitySurfaceRequirement = getVolatilityDataRequirement(target, surfaceName, getInstrumentType(), getSurfaceQuoteType(), getSurfaceQuoteUnits());
    final ValueRequirement interpolatorRequirement = getInterpolatorRequirement(target, desiredValue);
    return Sets.newHashSet(forwardCurveRequirement, volatilitySurfaceRequirement, interpolatorRequirement);
  }

  protected abstract boolean isCorrectIdType(final ComputationTarget target);

  protected abstract SmileSurfaceDataBundle getData(final FunctionInputs inputs, final ValueRequirement volatilityDataRequirement, final ValueRequirement forwardCurveRequirement);

  protected abstract String getInstrumentType();

  protected abstract String getSurfaceQuoteUnits();

  protected abstract String getSurfaceQuoteType();

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

  private ValueRequirement getInterpolatorRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, target.toSpecification(),
        BlackVolatilitySurfaceUtils.addVolatilityInterpolatorProperties(ValueProperties.builder().get(), desiredValue).get());
  }

  private ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName, final String instrumentType,
      final String surfaceQuoteType, final String surfaceQuoteUnits) {
    final ValueRequirement volDataRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get());
    return volDataRequirement;
  }

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String calculationMethod, final String forwardCurveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_CALCULATION_METHOD, calculationMethod)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

}
