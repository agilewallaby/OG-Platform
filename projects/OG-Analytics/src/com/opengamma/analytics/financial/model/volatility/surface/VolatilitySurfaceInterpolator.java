/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static com.opengamma.analytics.math.FunctionUtils.square;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VolatilitySurfaceInterpolator {
  private static final Logger LOGGER = LoggerFactory.getLogger(VolatilitySurfaceInterpolator.class);
  //TODO Set this back to SmileInterpolatorSABR() once a full fudge build is in place
  private static final GeneralSmileInterpolator DEFAULT_SMILE_INTERPOLATOR = new SmileInterpolatorSABR();
  private static final Interpolator1D DEFAULT_TIME_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final boolean USE_LOG_TIME = true;
  private static final boolean USE_INTEGRATED_VARIANCE = true;
  private static final boolean USE_LOG_VALUE = true;

  private final GeneralSmileInterpolator _smileInterpolator;
  private final Interpolator1D _timeInterpolator;
  private final boolean _useLogTime;
  private final boolean _useLogVar;
  private final boolean _useIntegratedVariance;

  public VolatilitySurfaceInterpolator() {
    this(DEFAULT_SMILE_INTERPOLATOR, DEFAULT_TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  public VolatilitySurfaceInterpolator(final GeneralSmileInterpolator smileInterpolator) {
    this(smileInterpolator, DEFAULT_TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  public VolatilitySurfaceInterpolator(final Interpolator1D timeInterpolator) {
    this(DEFAULT_SMILE_INTERPOLATOR, timeInterpolator, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  public VolatilitySurfaceInterpolator(final GeneralSmileInterpolator smileInterpolator, final Interpolator1D timeInterpolator) {
    this(smileInterpolator, timeInterpolator, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  /**
   * <b>Note</b> The combination of useIntegratedVariance = true, useLogTime != useLogValue  can produce very bad results, including considerable dips/humps between
   * points at the same level (all other combinations give a flat line), and thus should be avoided.
   * @param useIntegratedVariance if true integrated variance ($\sigma^2t$) is used in the interpolation, otherwise variance is used
   * @param useLogTime if true the natural-log of the time values are used in interpolation, if false the time values are used directly. This can be useful if the
   * expiries vary greatly in magnitude
   * @param useLogVariance If true the log of variance (actually either variance or integrated variance) is used in the interpolation
   */
  public VolatilitySurfaceInterpolator(final boolean useIntegratedVariance, final boolean useLogTime, final boolean useLogVariance) {
    _smileInterpolator = DEFAULT_SMILE_INTERPOLATOR;
    _timeInterpolator = DEFAULT_TIME_INTERPOLATOR;
    _useLogTime = useLogTime;
    _useIntegratedVariance = useIntegratedVariance;
    _useLogVar = useLogVariance;
    if (_useIntegratedVariance && _useLogVar != _useLogTime) {
      LOGGER.warn("The combination of useIntegratedVariance = true, useLogTime != useLogValue  can produce very bad results, including considerable dips between " +
          "points at the same level (all other combinations give a flat line), and thus should be avoided.");
    }
  }

  public VolatilitySurfaceInterpolator(final GeneralSmileInterpolator smileInterpolator, final boolean useLogTime, final boolean useIntegratedVariance, final boolean useLogValue) {
    this(smileInterpolator, DEFAULT_TIME_INTERPOLATOR, useLogTime, useIntegratedVariance, useLogValue);
  }

  public VolatilitySurfaceInterpolator(final Interpolator1D timeInterpolator, final boolean useLogTime, final boolean useIntegratedVariance, final boolean useLogValue) {
    this(DEFAULT_SMILE_INTERPOLATOR, timeInterpolator, useLogTime, useIntegratedVariance, useLogValue);
  }

  public VolatilitySurfaceInterpolator(final GeneralSmileInterpolator smileInterpolator, final Interpolator1D timeInterpolator, final boolean useLogTime, final boolean useIntegratedVariance,
      final boolean useLogValue) {
    ArgumentChecker.notNull(smileInterpolator, "null smile interpolator");
    ArgumentChecker.notNull(timeInterpolator, "null time interpolator");
    _smileInterpolator = smileInterpolator;
    _timeInterpolator = timeInterpolator;
    _useLogTime = useLogTime;
    _useIntegratedVariance = useIntegratedVariance;
    _useLogVar = useLogValue;
    if (_useIntegratedVariance && _useLogVar != _useLogTime) {
      LOGGER.warn("The combination of useIntegratedVariance = true, useLogTime != useLogValue  can produce very bad results, including considerable dips between " +
          "points at the same level (all other combinations give a flat line), and thus should be avoided.");
    }
  }

  //TODO add constructors to set options

  public Function1D<Double, Double>[] getIndependentSmileFits(final SmileSurfaceDataBundle marketData) {
    ArgumentChecker.notNull(marketData, "market data");
    final int n = marketData.getNumExpiries();
    final double[] forwards = marketData.getForwards();
    final double[][] strikes = marketData.getStrikes();
    final double[] expiries = marketData.getExpiries();
    final double[][] vols = marketData.getVolatilities();

    //fit each smile independently
    @SuppressWarnings("unchecked")
    final Function1D<Double, Double>[] smileFunctions = new Function1D[n];
    for (int i = 0; i < n; i++) {
      smileFunctions[i] = _smileInterpolator.getVolatilityFunction(forwards[i], strikes[i], expiries[i], vols[i]);
    }
    return smileFunctions;
  }

  /**
   * For a given expiry and strike, perform an interpolation between either the variance (square of volatility) or integrated variances
   *  of points with the same proxy delta (defined as d = Math.log(forward / k) / Math.sqrt(t)) on the fitted smiles.<p>
   *  Each smile is fitted independently using the supplied GeneralSmileInterpolator (the default is SmileInterpolatorSABR), which produces a curve (the smile) that
   *  fits all the market implied volatilities and has sensible extrapolation behaviour. <p>
   *  The interpolation in the time direction uses the supplied interpolator (default is  natural cubic spline) using the four nearest points. There is no guarantees of a
   *  monotonically increasing integrated variance  (hence calendar arbitrage or negative local volatility are possible), but using log time to better space out the x-points helps.
   * @param marketData The mark data - contains the forwards, expiries, and strikes and (market) implied volatilities at each expiry, not null
   * @return Implied volatility surface parameterised by time and moneyness
   */
  public BlackVolatilitySurfaceMoneyness getVolatilitySurface(final SmileSurfaceDataBundle marketData) {
    ArgumentChecker.notNull(marketData, "market data");
    final int n = marketData.getNumExpiries();
    final double[] forwards = marketData.getForwards();
    final double[] expiries = marketData.getExpiries();
    final Function1D<Double, Double>[] smileFunctions = getIndependentSmileFits(marketData);

    double[] temp = null;
    if (_useLogTime) {
      temp = new double[n];
      for (int i = 0; i < n; i++) {
        temp[i] = Math.log(expiries[i]);
      }
    } else {
      temp = expiries;
    }
    final double[] xValues = temp;

    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tm) {
        final double t = tm[0];
        final double m = tm[1];

        //For time less than the first expiry, linearly extrapolate the variance
        if (t <= expiries[0]) {
          final double k1 = forwards[0] * m;
          final double k2 = forwards[1] * m;
          final double var1 = square(smileFunctions[0].evaluate(k1));
          final double var2 = square(smileFunctions[1].evaluate(k2));
          final double dt = expiries[1] - expiries[0];
          final double var = ((expiries[1] - t) * var1 + (t - expiries[0]) * var2) / dt;
          if (var >= 0.0) {
            return Math.sqrt(var);
          }
          return Math.sqrt(var1);
        }

        final int index = SurfaceArrayUtils.getLowerBoundIndex(expiries, t);
        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index == n - 2) {
          lower = index - 2;
        } else if (index == n - 1) {
          lower = index - 3;
        } else {
          lower = index - 1;
        }

        final double[] xData = Arrays.copyOfRange(xValues, lower, lower + 4);
        final double x = _useLogTime ? Math.log(t) : t;

        final double[] yData = new double[4];

        for (int i = 0; i < 4; i++) {
          final double time = expiries[lower + i];
          final double k = forwards[lower + i] * Math.pow(m, Math.sqrt(time / t));
          double y = square(smileFunctions[lower + i].evaluate(k));
          if (_useIntegratedVariance) {
            y *= time;
          }
          yData[i] = _useLogVar ? Math.log(y) : y;
        }

        final Interpolator1DDataBundle db = _timeInterpolator.getDataBundle(xData, yData);
        final double tRes = _timeInterpolator.interpolate(db, x);
        final double yValue = _useLogVar ? Math.exp(tRes) : tRes;
        final double res = _useIntegratedVariance ? Math.sqrt(yValue / t) : Math.sqrt(yValue);

        return res;
      }

      public Object writeReplace() {
        return new InvokedSerializedForm(VolatilitySurfaceInterpolator.this, "getVolatilitySurface", marketData);
      }
    };

    return new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(surFunc), marketData.getForwardCurve());
  }

  //TODO find a way of bumping a single point without recalibrating all unaffected smiles
  public BlackVolatilitySurfaceMoneyness getBumpedVolatilitySurface(final SmileSurfaceDataBundle marketData, final int expiryIndex, final int strikeIndex, final double amount) {
    ArgumentChecker.notNull(marketData, "marketData");
    final SmileSurfaceDataBundle bumpedData = marketData.withBumpedPoint(expiryIndex, strikeIndex, amount);
    return getVolatilitySurface(bumpedData);
  }

  public boolean useLogTime() {
    return _useLogTime;
  }

  public boolean useIntegratedVariance() {
    return _useIntegratedVariance;
  }

  public boolean useLogValue() {
    return _useLogVar;
  }

  public Interpolator1D getTimeInterpolator() {
    return _timeInterpolator;
  }

  public GeneralSmileInterpolator getSmileInterpolator() {
    return _smileInterpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _smileInterpolator.hashCode();
    result = prime * result + _timeInterpolator.hashCode();
    result = prime * result + (_useIntegratedVariance ? 1231 : 1237);
    result = prime * result + (_useLogTime ? 1231 : 1237);
    result = prime * result + (_useLogVar ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VolatilitySurfaceInterpolator other = (VolatilitySurfaceInterpolator) obj;
    if (!ObjectUtils.equals(_smileInterpolator, other._smileInterpolator)) {
      return false;
    }
    if (!ObjectUtils.equals(_timeInterpolator, other._timeInterpolator)) {
      return false;
    }
    if (_useIntegratedVariance != other._useIntegratedVariance) {
      return false;
    }
    if (_useLogTime != other._useLogTime) {
      return false;
    }
    if (_useLogVar != other._useLogVar) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("VolatilitySurfaceInterpolator[time interpolator=");
    sb.append(_timeInterpolator.toString());
    sb.append(", smile interpolator=");
    sb.append(_smileInterpolator.toString());
    sb.append(" using ");
    sb.append(_useIntegratedVariance ? "integrated variance, " : " variance, ");
    sb.append(_useLogTime ? " log time and " : " linear time and ");
    sb.append(_useLogVar ? " log y" : " linear y");
    sb.append("]");
    return sb.toString();
  }
}
