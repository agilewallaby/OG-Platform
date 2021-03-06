/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class BlackVolatilitySurfaceInterpolatorDefaults extends DefaultPropertyFunction {
  private final String _timeAxis;
  private final String _yAxis;
  private final String _volatilityTransform;
  private final String _timeInterpolator;
  private final String _timeLeftExtrapolator;
  private final String _timeRightExtrapolator;

  public BlackVolatilitySurfaceInterpolatorDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(timeAxis, "time axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(volatilityTransform, "volatility transform");
    ArgumentChecker.notNull(timeInterpolator, "time interpolator");
    ArgumentChecker.notNull(timeLeftExtrapolator, "time left extrapolator");
    ArgumentChecker.notNull(timeRightExtrapolator, "time right extrapolator");
    _timeAxis = timeAxis;
    _yAxis = yAxis;
    _volatilityTransform = volatilityTransform;
    _timeInterpolator = timeInterpolator;
    _timeLeftExtrapolator = timeLeftExtrapolator;
    _timeRightExtrapolator = timeRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS.equals(propertyName)) {
      return Collections.singleton(_timeAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM.equals(propertyName)) {
      return Collections.singleton(_volatilityTransform);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeRightExtrapolator);
    }
    return null;
  }

}
