/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Swap generator for the USD semi-annual 30/360 vs Libor 3M.
 */
public class USD6MLIBOR3M extends GeneratorSwap {

  /**
   * Constructor.
   * @param calendar A USD calendar.
   */
  public USD6MLIBOR3M(Calendar calendar) {
    super(Period.ofMonths(6), DayCountFactory.INSTANCE.getDayCount("30/360"), IndexIborTestsMaster.getInstance().getIndex("USDLIBOR3M", calendar));
  }

}
