/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.lang.Validate;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Probability;

import com.opengamma.analytics.math.statistics.distribution.fnlib.DERFC;

/**
 * The normal distribution is a continuous probability distribution with probability density function
 * $$
 * \begin{align*}
 * f(x) = \frac{1}{\sqrt{2\pi}\sigma} e^{-\frac{(x - \mu)^2}{2\sigma^2}}
 * \end{align*}
 * $$
 * where $\mu$ is the mean and $\sigma$ the standard deviation of 
 * the distribution.
 * <p>
 * For values of the cumulative distribution function $|x| > 7.6$ this class calculates the cdf 
 * directly. For all other methods and values of $x$, this class is a wrapper for the
 * <a href="http://acs.lbl.gov/software/colt/api/cern/jet/random/Normal.html">Colt</a> implementation of the normal distribution.
 */
public class NormalDistribution implements ProbabilityDistribution<Double> {
  private static final double ROOT2 = Math.sqrt(2);
  private static final double XMIN = -7.6; //-7.6
  private static final double DELTA = 0.6; //0.05

  // TODO need a better seed
  private final double _mean;
  private final double _standardDeviation;
  private final Normal _normal;

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution, not negative or zero
   */
  public NormalDistribution(final double mean, final double standardDeviation) {
    this(mean, standardDeviation, new MersenneTwister64(new Date()));
  }

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution, not negative or zero
   * @param randomEngine A generator of uniform random numbers, not null
   */
  public NormalDistribution(final double mean, final double standardDeviation, final RandomEngine randomEngine) {
    Validate.isTrue(standardDeviation > 0, "standard deviation");
    Validate.notNull(randomEngine);
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  /**
   * {@inheritDoc}
   * The cern.jet.random library gives poor results for $x < -8$, and returns zero for $x < -8.37$, so beyond $x < -7.6$ 
   * we use the approximation 
   * $$
   * \begin{align*}
   * N(x) \approx \frac{N'(x)}{\sqrt{1 + x^2}}
   * \end{align*}
   * $$
   * and use the symmetry of the distribution for $x > 7.6$  
   */
  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x);

    return DERFC.getErfc(-x / ROOT2) / 2;

    //    if (x <= XMIN) {
    //      return tailApprox(x);
    //    }
    //    if (x < XMIN + DELTA) {
    //      // smooth the two approximations together
    //      final double a = tailApprox(x);
    //      final double b = _normal.cdf(x);
    //      final double w = weight(x);
    //      final double temp = w * a + (1 - w) * b;
    //      return temp;
    //    }
    //    if (x > -(XMIN + DELTA)) {
    //      return 1.0 - getCDF(-x);
    //    }
    //    return _normal.cdf(x);
  }

  private double tailApprox(final double x) {
    return _normal.pdf(x) / Math.sqrt(1 + x * x);
  }

  private double weight(final double x) {
    if (x < XMIN) {
      return 1.0;
    } else if (x > XMIN + DELTA) {
      return 0.0;
    } else {
      return (XMIN + DELTA - x) / DELTA;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    Validate.notNull(x);
    return _normal.pdf(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    Validate.notNull(p);
    Validate.isTrue(p >= 0 && p <= 1, "Probability must be >= 0 and <= 1");
    return Probability.normalInverse(p);
  }

  /**
   * @return The mean
   */
  public double getMean() {
    return _mean;
  }

  /**
   * @return The standard deviation
   */
  public double getStandardDeviation() {
    return _standardDeviation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mean);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_standardDeviation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final NormalDistribution other = (NormalDistribution) obj;
    if (Double.doubleToLongBits(_mean) != Double.doubleToLongBits(other._mean)) {
      return false;
    }
    return Double.doubleToLongBits(_standardDeviation) == Double.doubleToLongBits(other._standardDeviation);
  }
}
