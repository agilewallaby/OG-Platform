/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.NotImplementedException;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * General utility method applying to Financial Securities
 */
public class FinancialSecurityUtils {

  /**
   * 
   * @param target the computation target being examined.
   * @return ValueProperties containing a constraint of the CurrencyUnit or empty if not possible
   */
  public static ValueProperties getCurrencyConstraint(final ComputationTarget target) {
    switch (target.getType()) {
      case PORTFOLIO_NODE:
        break;
      case POSITION: {
        final Security security = target.getPosition().getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
      break;
      case PRIMITIVE: {
        final UniqueId uid = target.getUniqueId();
        if (uid.getScheme().equals(Currency.OBJECT_SCHEME)) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, uid.getValue()).get();
        }
      }
      break;
      case SECURITY: {
        final Security security = target.getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
      break;
      case TRADE: {
        final Security security = target.getTrade().getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
      break;
    }
    return ValueProperties.none();
  }

  /**
   * @param security the security to be examined.
   * @return an ExternalId for a Region, where it is possible to determine, null otherwise.
   */
  public static ExternalId getRegion(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final ExternalId regionId = finSec.accept(new FinancialSecurityVisitor<ExternalId>() {
        @Override
        public ExternalId visitBondSecurity(final BondSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
        }

        @Override
        public ExternalId visitCashSecurity(final CashSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitEquitySecurity(final EquitySecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFRASecurity(final FRASecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitFutureSecurity(final FutureSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitSwapSecurity(final SwapSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFXOptionSecurity(final FXOptionSecurity security) {
          throw null;
        }

        @Override
        public ExternalId visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          throw null;
        }

        @Override
        public ExternalId visitSwaptionSecurity(final SwaptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return null;
        }

        @Override
        public ExternalId visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitCapFloorSecurity(final CapFloorSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return security.getRegion();
        }

        @Override
        public ExternalId visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return security.getRegion();
        }

        @Override
        public ExternalId visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return security.getRegion();
        }

      });
      return regionId;
    }
    return null;
  }

  /**
   * @param security the security to be examined.
   * @return an ExternalId for an Exchange, where it is possible to determine, null otherwise.
   */
  public static ExternalId getExchange(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final ExternalId regionId = finSec.accept(new FinancialSecurityVisitor<ExternalId>() {
        @Override
        public ExternalId visitBondSecurity(final BondSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitCashSecurity(final CashSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquitySecurity(final EquitySecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchangeCode());
        }

        @Override
        public ExternalId visitFRASecurity(final FRASecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFutureSecurity(final FutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitSwapSecurity(final SwapSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitFXOptionSecurity(final FXOptionSecurity security) {
          throw null;
        }

        @Override
        public ExternalId visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          throw null;
        }

        @Override
        public ExternalId visitSwaptionSecurity(final SwaptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return null;
        }

        @Override
        public ExternalId visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitCapFloorSecurity(final CapFloorSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return null;
        }

        @Override
        public ExternalId visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return null;
        }

      });
      return regionId;
    }
    return null;
  }

  /**
   * @param security the security to be examined.
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Currency getCurrency(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final Currency ccy = finSec.accept(new FinancialSecurityVisitor<Currency>() {
        @Override
        public Currency visitBondSecurity(final BondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCashSecurity(final CashSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquitySecurity(final EquitySecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFRASecurity(final FRASecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFutureSecurity(final FutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitSwapSecurity(final SwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

        @Override
        public Currency visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXOptionSecurity(final FXOptionSecurity security) {
          throw new UnsupportedOperationException("FX securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          throw new UnsupportedOperationException("FX securities do not have a currency");
        }

        @Override
        public Currency visitSwaptionSecurity(final SwaptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return equityIndexDividendFutureOptionSecurity.getCurrency();
        }

        @Override
        public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          throw new UnsupportedOperationException("FX Barrier Options do not have a currency");
        }

        @Override
        public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
          throw new UnsupportedOperationException("FX forward securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          throw new UnsupportedOperationException("Non-deliverable FX forward securities do not have a currency");
        }

        @Override
        public Currency visitCapFloorSecurity(final CapFloorSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          throw new UnsupportedOperationException("FX digital option securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          throw new UnsupportedOperationException("NDF FX digital option securities do not have a currency");
        }

        @Override
        public Currency visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return security.getCurrency();
        }
      });
      return ccy;
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
        return securityEntryData.getCurrency();
      }
    }

    return null;
  }

  /**
   * @param security the security to be examined.
   * @param securitySource a security source
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Collection<Currency> getCurrencies(final Security security, final SecuritySource securitySource) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final Collection<Currency> ccy = finSec.accept(new FinancialSecurityVisitor<Collection<Currency>>() {
        @Override
        public Collection<Currency> visitBondSecurity(final BondSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCashSecurity(final CashSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquitySecurity(final EquitySecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFRASecurity(final FRASecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFutureSecurity(final FutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitSwapSecurity(final SwapSecurity security) {

          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return Collections.singletonList(payLeg.getCurrency());
            } else {
              final Collection<Currency> collection = new ArrayList<Currency>();
              collection.add(payLeg.getCurrency());
              collection.add(receiveLeg.getCurrency());
              return collection;
            }
          }
          return null;
        }

        @Override
        public Collection<Currency> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXOptionSecurity(final FXOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          //deliveryCurrency is always already covered
          return currencies;
        }

        @Override
        public Collection<Currency> visitSwaptionSecurity(final SwaptionSecurity security) {
          // REVIEW: jim 1-Aug-2011 -- should we include the currencies of the underlying?
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitFXForwardSecurity(final FXForwardSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getPayCurrency());
          currencies.add(security.getReceiveCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getPayCurrency());
          currencies.add(security.getReceiveCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitCapFloorSecurity(final CapFloorSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

      });
      return ccy;
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
        return Collections.singleton(securityEntryData.getCurrency());
      }
    }
    return null;
  }

  /**
   * Check if a security is exchange traded
   * 
   * @param security the security to be examined.
   * @return true if exchange traded or false otherwise.
   */
  public static boolean isExchangedTraded(final Security security) {
    boolean result = false;
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final Boolean isExchangeTraded = finSec.accept(new FinancialSecurityVisitor<Boolean>() {

        @Override
        public Boolean visitBondSecurity(final BondSecurity security) {
          return null;
        }

        @Override
        public Boolean visitCashSecurity(final CashSecurity security) {
          return null;
        }

        @Override
        public Boolean visitEquitySecurity(final EquitySecurity security) {
          return true;
        }

        @Override
        public Boolean visitFRASecurity(final FRASecurity security) {
          return null;
        }

        @Override
        public Boolean visitFutureSecurity(final FutureSecurity security) {
          return true;
        }

        @Override
        public Boolean visitSwapSecurity(final SwapSecurity security) {
          return null;
        }

        @Override
        public Boolean visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return true;
        }

        @Override
        public Boolean visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return true;
        }

        @Override
        public Boolean visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          throw new NotImplementedException();
        }

        @Override
        public Boolean visitFXOptionSecurity(final FXOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitSwaptionSecurity(final SwaptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          throw new NotImplementedException();
        }

        @Override
        public Boolean visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitFXForwardSecurity(final FXForwardSecurity security) {
          return true;
        }

        @Override
        public Boolean visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return true;
        }

        @Override
        public Boolean visitCapFloorSecurity(final CapFloorSecurity security) {
          return null;
        }

        @Override
        public Boolean visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return null;
        }

        @Override
        public Boolean visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return null;
        }

        @Override
        public Boolean visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return null;
        }

        @Override
        public Boolean visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return null;
        }

        @Override
        public Boolean visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return null;
        }

        @Override
        public Boolean visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return null;
        }

      });

      result = isExchangeTraded == null ? false : isExchangeTraded;
    }

    return result;
  }

}
