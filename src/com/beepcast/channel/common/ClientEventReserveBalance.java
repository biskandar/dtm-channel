package com.beepcast.channel.common;

import com.beepcast.billing.BillingApp;
import com.beepcast.billing.BillingResult;
import com.beepcast.billing.BillingStatus;
import com.beepcast.model.transaction.billing.AccountProfile;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ClientEventReserveBalance {

  static final DLogContext lctx = new SimpleContext(
      "ClientEventReserveBalance" );

  public static boolean reserveBalance( int clientId , int eventId , double unit ) {
    return reserveBalance( null , clientId , eventId , unit );
  }

  public static boolean reserveBalance( String headerLog , int clientId ,
      int eventId , double unit ) {
    boolean result = false;

    BillingApp billingApp = BillingApp.getInstance();
    if ( billingApp == null ) {
      return result;
    }

    if ( ( clientId < 1 ) || ( eventId < 1 ) || ( unit < 0.0 ) ) {
      return result;
    }

    headerLog = ( headerLog == null ) ? "" : headerLog;
    headerLog = headerLog + "[Client-" + clientId + "] ";
    headerLog = headerLog + "[Event-" + eventId + "] ";

    BillingResult billingResultClient = billingApp.doDebit(
        AccountProfile.CLIENT_PREPAID , new Integer( clientId ) , new Double(
            unit ) );
    if ( billingResultClient.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
      DLog.warning( lctx , headerLog + "Failed to perform reserve balance "
          + ", found failed to perform debit from "
          + AccountProfile.CLIENT_PREPAID + " account" );
      return result;
    }

    BillingResult billingResultEvent = billingApp.doCredit(
        AccountProfile.EVENT_PREPAID , new Integer( eventId ) , new Double(
            unit ) );
    if ( billingResultEvent.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
      DLog.warning( lctx , headerLog + "Failed to perform reserve balance "
          + ", found failed to perform credit from "
          + AccountProfile.EVENT_PREPAID
          + " account , trying to add credit to "
          + AccountProfile.CLIENT_PREPAID + " account" );
      billingResultClient = billingApp.doCredit( AccountProfile.CLIENT_PREPAID ,
          new Integer( clientId ) , new Double( unit ) );
      if ( billingResultClient.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
        DLog.warning( lctx , headerLog + "Failed to perform add balance to "
            + AccountProfile.CLIENT_PREPAID + " account" );
      }
      return result;
    }

    DLog.debug(
        lctx ,
        headerLog + "Successfully performed reserve balance " + "from "
            + AccountProfile.CLIENT_PREPAID + " account to "
            + AccountProfile.EVENT_PREPAID + " account , added = " + unit
            + " unit(s) , event balance : "
            + billingResultEvent.getBalanceBefore() + " -> "
            + billingResultEvent.getBalanceAfter()
            + " unit(s) , client balance : "
            + billingResultClient.getBalanceBefore() + " -> "
            + billingResultClient.getBalanceAfter() + " unit(s) " );

    result = true;
    return result;
  }

  public static Double unreserveBalance( int clientId , int eventId ) {
    return unreserveBalance( null , clientId , eventId );
  }

  public static Double unreserveBalance( String headerLog , int clientId ,
      int eventId ) {
    Double unreserveBalance = null;

    BillingApp billingApp = BillingApp.getInstance();
    if ( billingApp == null ) {
      return unreserveBalance;
    }

    if ( ( clientId < 1 ) || ( eventId < 1 ) ) {
      return unreserveBalance;
    }

    headerLog = ( headerLog == null ) ? "" : headerLog;
    headerLog = headerLog + "[Client-" + clientId + "] ";
    headerLog = headerLog + "[Event-" + eventId + "] ";

    BillingResult billingResultEvent = billingApp.getBalance(
        AccountProfile.EVENT_PREPAID , new Integer( eventId ) );
    if ( billingResultEvent.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
      DLog.warning( lctx , headerLog + "Failed to perform unreserve balance "
          + ", found failed to perform get balance from "
          + AccountProfile.EVENT_PREPAID + " account" );
      return unreserveBalance;
    }

    Double currentBalance = billingResultEvent.getBalanceAfter();
    if ( ( currentBalance == null ) || ( currentBalance.doubleValue() <= 0 ) ) {
      DLog.debug( lctx , headerLog + "No need to perform unreserve balance "
          + "from " + AccountProfile.EVENT_PREPAID
          + " account , found zero value" );
      unreserveBalance = new Double( 0 );
      return unreserveBalance;
    }

    billingResultEvent = billingApp.doDebit( AccountProfile.EVENT_PREPAID ,
        new Integer( eventId ) , currentBalance );
    if ( billingResultEvent.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
      DLog.warning( lctx , headerLog + "Failed to perform unreserve balance "
          + currentBalance + " unit(s) , found failed to perform debit to "
          + AccountProfile.EVENT_PREPAID + " account" );
      return unreserveBalance;
    }

    BillingResult billingResultClient = billingApp.doCredit(
        AccountProfile.CLIENT_PREPAID , new Integer( clientId ) ,
        currentBalance );
    if ( billingResultClient.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) {
      DLog.warning( lctx , headerLog + "Failed to perform unreserve balance "
          + currentBalance + " unit(s) , found failed to perform credit to "
          + AccountProfile.CLIENT_PREPAID + " account" );
      return unreserveBalance;
    }

    DLog.debug(
        lctx ,
        headerLog + "Successfully performed unreserve balance " + "from "
            + AccountProfile.EVENT_PREPAID + " account to "
            + AccountProfile.CLIENT_PREPAID + " account , added = "
            + currentBalance + " unit(s) , event balance : "
            + billingResultEvent.getBalanceBefore() + " -> "
            + billingResultEvent.getBalanceAfter()
            + " unit(s) , client balance : "
            + billingResultClient.getBalanceBefore() + " -> "
            + billingResultClient.getBalanceAfter() + " unit(s) " );

    unreserveBalance = currentBalance;
    return unreserveBalance;
  }

}
