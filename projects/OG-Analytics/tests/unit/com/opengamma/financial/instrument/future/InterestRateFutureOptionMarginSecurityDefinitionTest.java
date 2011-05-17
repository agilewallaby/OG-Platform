/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests the interest rate future option with margin security description.
 */
public class InterestRateFutureOptionMarginSecurityDefinitionTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtil.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtil.getUTCDate(2011, 9, 16);
  private static final double STRIKE = 0.9895;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2 = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionMarginSecurityDefinition(null, EXPIRATION_DATE, STRIKE, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiration() {
    new InterestRateFutureOptionMarginSecurityDefinition(ERU2, null, STRIKE, IS_CALL);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(ERU2, OPTION_ERU2.getUnderlyingFuture());
    assertEquals(EXPIRATION_DATE, OPTION_ERU2.getExpirationDate());
    assertEquals(STRIKE, OPTION_ERU2.getStrike());
    assertEquals(IS_CALL, OPTION_ERU2.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    InterestRateFutureOptionMarginSecurityDefinition other = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);
    assertTrue(OPTION_ERU2.equals(other));
    assertTrue(OPTION_ERU2.hashCode() == other.hashCode());
    InterestRateFutureOptionMarginSecurityDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, LAST_TRADING_DATE, STRIKE, IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE + 0.001, IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, !IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
  }
}