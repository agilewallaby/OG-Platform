/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.analytics.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.Swap;
import com.opengamma.analytics.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface InstrumentDerivativeVisitor<S, T> {

  // Two arguments

  T visit(InstrumentDerivative derivative, S data);

  T[] visit(InstrumentDerivative[] derivative, S data);

  T visitBondFixedSecurity(BondFixedSecurity bond, S data);

  T visitBondFixedTransaction(BondFixedTransaction bond, S data);

  T visitBondIborSecurity(BondIborSecurity bond, S data);

  T visitBondIborTransaction(BondIborTransaction bond, S data);

  T visitBillSecurity(BillSecurity bill, S data);

  T visitBillTransaction(BillTransaction bill, S data);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity, S data);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, S data);

  T visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity, S data);

  T visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, S data);

  T visitSwap(Swap<?, ?> swap, S data);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap, S data);

  T visitFixedFloatSwap(FixedFloatSwap swap, S data);

  T visitOISSwap(OISSwap swap, S data);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, S data);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, S data);

  T visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, S data);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap, S data);

  T visitFloatingRateNote(FloatingRateNote frn, S data);

  T visitCrossCurrencySwap(CrossCurrencySwap ccs, S data);

  T visitForexForward(ForexForward fx, S data);

  T visitCash(Cash cash, S data);

  T visitBondFuture(BondFuture bondFuture, S data);

  T visitInterestRateFuture(InterestRateFuture future, S data);

  T visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future, S data);

  T visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future, S data);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, S data);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, S data);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, S data);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, S data);

  T visitFixedPayment(PaymentFixed payment, S data);

  T visitFixedCouponPayment(CouponFixed payment, S data);

  T visitCouponIbor(CouponIbor payment, S data);

  T visitCouponIborFixed(CouponIborFixed payment, S data);

  T visitCouponIborGearing(CouponIborGearing payment, S data);

  T visitCouponOIS(CouponOIS payment, S data);

  T visitCouponCMS(CouponCMS payment, S data);

  T visitCapFloorIbor(CapFloorIbor payment, S data);

  T visitCapFloorCMS(CapFloorCMS payment, S data);

  T visitCapFloorCMSSpread(CapFloorCMSSpread payment, S data);

  T visitForwardRateAgreement(ForwardRateAgreement fra, S data);

  T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, S data);

  T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, S data);

  T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, S data);

  T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, S data);

  T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, S data);

  T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, S data);

  // One argument

  T visit(InstrumentDerivative derivative);

  T[] visit(InstrumentDerivative[] derivative);

  T visitBondFixedSecurity(BondFixedSecurity bond);

  T visitBondFixedTransaction(BondFixedTransaction bond);

  T visitBondIborSecurity(BondIborSecurity bond);

  T visitBondIborTransaction(BondIborTransaction bond);

  T visitBillSecurity(BillSecurity bill);

  T visitBillTransaction(BillTransaction bill);

  T visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity);

  T visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity);

  T visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity);

  T visitSwap(Swap<?, ?> swap);

  T visitFixedCouponSwap(FixedCouponSwap<?> swap);

  T visitFixedFloatSwap(FixedFloatSwap swap);

  T visitOISSwap(OISSwap swap);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption);

  T visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption);

  T visitFloatingRateNote(FloatingRateNote frn);

  T visitCrossCurrencySwap(CrossCurrencySwap ccs);

  T visitForexForward(ForexForward fx);

  T visitTenorSwap(TenorSwap<? extends Payment> tenorSwap);

  T visitCash(Cash cash);

  T visitBondFuture(BondFuture future);

  T visitInterestRateFutureSecurity(InterestRateFuture future);

  T visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future);

  T visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option);

  T visitFixedPayment(PaymentFixed payment);

  T visitFixedCouponPayment(CouponFixed payment);

  T visitCouponIborFixed(CouponIborFixed payment);

  T visitCouponIbor(CouponIbor payment);

  T visitCouponIborGearing(CouponIborGearing payment);

  T visitCouponOIS(CouponOIS payment);

  T visitCouponCMS(CouponCMS payment);

  T visitCapFloorIbor(CapFloorIbor payment);

  T visitCapFloorCMS(CapFloorCMS payment);

  T visitCapFloorCMSSpread(CapFloorCMSSpread payment);

  T visitForwardRateAgreement(ForwardRateAgreement fra);

  T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon);

  T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon);

  T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon);

  T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon);

  T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond);

  T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond);

  // -----     Deposit     -----

  T visitDepositIbor(DepositIbor deposit, S data);

  T visitDepositIbor(DepositIbor deposit);

  T visitDepositCounterpart(DepositCounterpart deposit, S data);

  T visitDepositCounterpart(DepositCounterpart deposit);

  T visitDepositZero(DepositZero deposit, S data);

  T visitDepositZero(DepositZero deposit);

  // -----     Forex     -----

  T visitForex(Forex derivative, S data);

  T visitForex(Forex derivative);

  T visitForexSwap(ForexSwap derivative, S data);

  T visitForexSwap(ForexSwap derivative);

  T visitForexOptionVanilla(ForexOptionVanilla derivative, S data);

  T visitForexOptionVanilla(ForexOptionVanilla derivative);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, S data);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, S data);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, S data);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative);

  T visitForexOptionDigital(ForexOptionDigital derivative, S data);

  T visitForexOptionDigital(ForexOptionDigital derivative);

}
