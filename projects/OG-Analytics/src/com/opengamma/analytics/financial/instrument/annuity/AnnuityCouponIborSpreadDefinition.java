/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for an AnnuityDefinition containing CouponIborSpreadDefinition.
 */
public class AnnuityCouponIborSpreadDefinition extends AnnuityDefinition<CouponIborSpreadDefinition> {
  private final IborIndex _iborIndex;

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   * @param iborIndex The underlying ibor index
   */
  public AnnuityCouponIborSpreadDefinition(final CouponIborSpreadDefinition[] payments, final IborIndex iborIndex) {
    super(payments);
    ArgumentChecker.notNull(iborIndex, "ibor index");
    _iborIndex = iborIndex;
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index, final double spread,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(tenor, "tenor");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, tenor, notional, index, isPayer);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, index);
  }

  /**
   * Annuity builder from the conventions and common characteristics. 
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param spread The spread rate.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final IborIndex index,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount, final double spread) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.notNull(paymentPeriod, "payment period");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(dayCount, "Day count convention");
    ArgumentChecker.isTrue(notional > 0, "notional <= 0");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false, businessDayConvention, index.getCalendar(), endOfMonth);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), index.getCalendar());
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0]), sign * notional,
        fixingDate, index, spread);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), index.getCalendar());
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn], dayCount.getDayCountFraction(
          paymentDates[loopcpn - 1], paymentDates[loopcpn]), sign * notional, fixingDate, index, spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, index);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param spread The common spread.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final double spread,
      final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(maturityDate, "maturity date");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, maturityDate, notional, index, isPayer);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, index);
  }

  /**
   * Returns the underlying ibor index
   * @return The underlying ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _iborIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AnnuityCouponIborSpreadDefinition other = (AnnuityCouponIborSpreadDefinition) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }
}
