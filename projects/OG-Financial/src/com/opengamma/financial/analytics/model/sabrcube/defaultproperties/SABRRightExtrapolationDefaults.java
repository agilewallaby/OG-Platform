/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationFunction;
import com.opengamma.financial.analytics.model.volatility.CubeAndSurfaceFittingMethodDefaultNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SABRRightExtrapolationDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
  };
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String _cubeName;
  private final String _fittingMethod;
  private final String _curveCalculationMethod;
  private final String _cutoff;
  private final String _mu;
  private final String[] _applicableCurrencies;

  public SABRRightExtrapolationDefaults(final String forwardCurveName, final String fundingCurveName, final String cubeName, final String fittingMethod,
      final String curveCalculationMethod, final String cutoff, final String mu, final String... applicableCurrencies) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(fundingCurveName, "funding curve name");
    ArgumentChecker.notNull(cubeName, "cube name");
    ArgumentChecker.notNull(fittingMethod, "fitting method");
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(cutoff, "cutoff");
    ArgumentChecker.notNull(mu, "mu");
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
    _cubeName = cubeName;
    _fittingMethod = fittingMethod;
    _curveCalculationMethod = curveCalculationMethod;
    _cutoff = cutoff;
    _mu = mu;
    _applicableCurrencies = applicableCurrencies;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity
        || security instanceof CapFloorCMSSpreadSecurity)) {
      return false;
    }
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    for (final String ccy : _applicableCurrencies) {
      if (ccy.equals(currency)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueRequirement, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CUBE);
      defaults.addValuePropertyName(valueRequirement, CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
      defaults.addValuePropertyName(valueRequirement, SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    }
    if (ValuePropertyNames.CUBE.equals(propertyName)) {
      return Collections.singleton(_cubeName);
    }
    if (CubeAndSurfaceFittingMethodDefaultNamesAndValues.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethod);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
    }
    if (SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE.equals(propertyName)) {
      return Collections.singleton(_cutoff);
    }
    if (SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER.equals(propertyName)) {
      return Collections.singleton(_mu);
    }
    return null;
  }

}
