/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

/**
 * 
 */
public class InstrumentTypeProperties {
  /**
   * Name of the surface type property. This allows surfaces to be distinguished by instrument type (e.g. an FX volatility
   * surface, swaption ATM volatility surface).
   */
  public static final String PROPERTY_SURFACE_INSTRUMENT_TYPE = "InstrumentType";
  /** Property representing a surface for FX vanilla options */
  public static final String FOREX = "FX_VANILLA_OPTION";
  /** Property representing a surface for IR future options */
  public static final String IR_FUTURE_OPTION = "IR_FUTURE_OPTION";

}