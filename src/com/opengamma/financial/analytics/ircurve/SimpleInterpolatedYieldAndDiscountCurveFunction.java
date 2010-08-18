/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class SimpleInterpolatedYieldAndDiscountCurveFunction extends AbstractFunction implements FunctionInvoker {

  @SuppressWarnings("unchecked")
  private Interpolator1D _interpolator;
  private InterpolatedYieldAndDiscountCurveDefinition _definition;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private final Currency _curveCurrency;
  private final String _curveName;
  private final boolean _isYieldCurve;

  public SimpleInterpolatedYieldAndDiscountCurveFunction(final Currency currency, final String name, final boolean isYieldCurve) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    _definition = null;
    _curveCurrency = currency;
    _curveName = name;
    _isYieldCurve = isYieldCurve;
    _interpolator = null;
    _requirements = null;
    _result = null;
    _results = null;
  }

  private void initImpl() {
    _interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    _result = new ValueSpecification(new ValueRequirement(_isYieldCurve ? ValueRequirementNames.YIELD_CURVE : ValueRequirementNames.DISCOUNT_CURVE, _definition.getCurrency()));
    _results = Collections.singleton(_result);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final InterpolatedYieldAndDiscountCurveSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
    _definition = curveSource.getDefinition(_curveCurrency, _curveName);
    initImpl();
  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldAndDiscountCurveDefinition definition) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStrip strip : definition.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getMarketDataSpecification());
      result.add(requirement);
    }
    return result;
  }

  /**
   * @return the definition
   */
  public InterpolatedYieldAndDiscountCurveDefinition getDefinition() {
    return _definition;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueIdentifier(), getDefinition().getCurrency().getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _results;
    }
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public String getShortName() {
    return _curveCurrency + "-" + _curveName + (_isYieldCurve ? " Yield Curve" : " Discount Curve");
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime today = snapshotClock.zonedDateTime(); // TODO: change to times
    final Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    boolean isFirst = true;
    for (final FixedIncomeStrip strip : getDefinition().getStrips()) {
      final ValueRequirement stripRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getMarketDataSpecification());
      Double price = (Double) inputs.getValue(stripRequirement);
      if (strip.getInstrumentType() == StripInstrument.FUTURE) {
        price = (100d - price);
      }
      price /= 100d;
      if (_isYieldCurve) {
        if (isFirst) {
          // TODO This is here to avoid problems with instruments with expiry < 1 day
          // At the moment, interpolators don't extrapolate, and so asking for the rate
          // if t < 1 throws an exception. It doesn't actually matter in the case of discount curves,
          // because df at t = 0 is 1 by definition, but for yield curves this should change when
          // extrapolation is allowed
          timeInYearsToRates.put(0., 0.);
          isFirst = false;
        }
        // final double numYears = (strip.getEndDate().toEpochDays() - today.toEpochDays()) / DateUtil.DAYS_PER_YEAR;
        final double numYears = DateUtil.getDifferenceInYears(today, strip.getEndDate().atStartOfDayInZone(TimeZone.UTC));
        timeInYearsToRates.put(numYears, price);
      } else {
        if (isFirst) {
          timeInYearsToRates.put(0., 1.);
          isFirst = false;
        }
        final double numYears = DateUtil.getDifferenceInYears(today, strip.getEndDate().atStartOfDayInZone(TimeZone.UTC));
        timeInYearsToRates.put(numYears, Math.exp(-price * numYears));
      }
    }
    // System.err.println("Time in years to rates: " + timeInYearsToRates);
    // Bootstrap the yield curve
    final YieldAndDiscountCurve curve = _isYieldCurve ? new InterpolatedYieldCurve(timeInYearsToRates, _interpolator) : new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    final ComputedValue resultValue = new ComputedValue(_result, curve);
    return Collections.singleton(resultValue);
  }

  private static final String DEFINITION_KEY = "definition";
  private static final String CURVE_CURRENCY_KEY = "curveCurrency";
  private static final String CURVE_NAME_KEY = "curveName";
  private static final String IS_YIELD_CURVE_KEY = "yieldCurve";

  @Override
  public void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsgWithClassHeaders(message, DEFINITION_KEY, null, _definition, InterpolatedYieldAndDiscountCurveDefinition.class);
    context.objectToFudgeMsgWithClassHeaders(message, CURVE_CURRENCY_KEY, null, _curveCurrency, Currency.class);
    message.add(CURVE_NAME_KEY, _curveName);
    message.add(IS_YIELD_CURVE_KEY, _isYieldCurve);
  }

  public static SimpleInterpolatedYieldAndDiscountCurveFunction fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final SimpleInterpolatedYieldAndDiscountCurveFunction object = new SimpleInterpolatedYieldAndDiscountCurveFunction(context
        .fieldValueToObject(Currency.class, message.getByName(CURVE_CURRENCY_KEY)), message.getString(CURVE_NAME_KEY), message.getBoolean(IS_YIELD_CURVE_KEY));
    final FudgeField field = message.getByName(DEFINITION_KEY);
    if (field != null) {
      object._definition = context.fieldValueToObject(InterpolatedYieldAndDiscountCurveDefinition.class, field);
      object.initImpl();
    }
    return fromFudgeMsg(object, message);
  }

}
