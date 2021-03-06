/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.financial.analytics.model.volatility.CubeAndSurfaceFittingMethodDefaultNamesAndValues;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRNoExtrapolationVegaFunction extends SABRVegaFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  protected ValueProperties getSensitivityProperties(final ComputationTarget target, final String currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    return ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_VOLATILITY_MODEL, CubeAndSurfaceFittingMethodDefaultNamesAndValues.SABR_FITTING)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION).get();
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final ValueRequirement desiredValue) {
    final YieldCurveBundle yieldCurves = getYieldCurves(inputs, currency, desiredValue);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD);
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, VolatilityFunctionFactory.HAGAN_FORMULA);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CUBE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.Y_INTERPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_Y_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_Y_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION)
        .withAny(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD)
        .with(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_VOLATILITY_MODEL, CubeAndSurfaceFittingMethodDefaultNamesAndValues.SABR_FITTING)
        .with(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_CUBE).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String xInterpolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME);
    final String xLeftExtrapolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String xRightExtrapolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String yInterpolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.Y_INTERPOLATOR_NAME);
    final String yLeftExtrapolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    final String yRightExtrapolator = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    return properties.copy()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME, xInterpolator)
        .with(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME, xLeftExtrapolator)
        .with(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME, xRightExtrapolator)
        .with(InterpolatedCurveAndSurfaceProperties.Y_INTERPOLATOR_NAME, yInterpolator)
        .with(InterpolatedCurveAndSurfaceProperties.LEFT_Y_EXTRAPOLATOR_NAME, yLeftExtrapolator)
        .with(InterpolatedCurveAndSurfaceProperties.RIGHT_Y_EXTRAPOLATOR_NAME, yRightExtrapolator)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_NO_EXTRAPOLATION)
        .with(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_VOLATILITY_MODEL, CubeAndSurfaceFittingMethodDefaultNamesAndValues.SABR_FITTING)
        .with(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_CUBE).get();
  }
}
