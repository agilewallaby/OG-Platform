/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.instrument.index.GeneratorOIS;
import com.opengamma.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing CouponOISDefinition.
 */
public class AnnuityCouponOISDefinition extends AnnuityCouponDefinition<CouponOISDefinition> {

  /** Empty array for array conversion of list */
  protected static final Coupon[] EMPTY_ARRAY_COUPON = new Coupon[0];

  /**
   * Constructor from a list of OIS coupons.
   * @param payments The coupons.
   */
  public AnnuityCouponOISDefinition(final CouponOISDefinition[] payments) {
    super(payments);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The OIS generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorOIS generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param maturityDate The maturity date of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The OIS generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final GeneratorOIS generator, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  private static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final GeneratorOIS generator,
      final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponOISDefinition[] coupons = new CouponOISDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponOISDefinition.from(generator.getIndex(), settlementDate, endFixingPeriodDate[0], notionalSigned, generator.getSpotLag());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponOISDefinition.from(generator.getIndex(), endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, generator.getSpotLag());
    }
    return new AnnuityCouponOISDefinition(coupons);
  }

  @Override
  public GenericAnnuity<? extends Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    final CouponOISDefinition[] payments = getPayments();
    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!date.isAfter(payments[loopcoupon].getPaymentDate())) {
        resultList.add(payments[loopcoupon].toDerivative(date, indexFixingTS, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

}