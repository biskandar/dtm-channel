package com.beepcast.channel;

import java.util.Date;

import com.beepcast.billing.profile.PaymentType;
import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.channel.common.ChannelSessionReportSupport;
import com.beepcast.channel.common.ClientEventReserveBalance;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSession {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSession" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support API Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static boolean start( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;
    int csId = ( csBean == null ) ? 0 : csBean.getId();
    if ( csId < 1 ) {
      DLog.warning( lctx , "Failed to start channel session "
          + ", found zero csId" );
      return result;
    }
    ChannelSessionService csService = new ChannelSessionService();
    String headerLog = ChannelSessionLogSupport.headerLog( csId );
    if ( reserveBalance( clientOut , eventOut , csBean ) ) {
      DLog.debug( lctx , headerLog + "Successfully reserved "
          + "client prepaid balance , trying to update "
          + "status start channel session" );
      result = csService.updateStatusStartChannelSession( csId );
      if ( result ) {
        DLog.debug( lctx , headerLog + "Updated status start channel session" );
      } else {
        DLog.warning( lctx , headerLog + "Failed to update "
            + "status start channel session" );
      }
    } else {
      DLog.warning( lctx , headerLog + "Failed to reserve "
          + "client prepaid balance" );
    }
    return result;
  }

  public static boolean schedule( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;

    int csId = ( csBean == null ) ? 0 : csBean.getId();
    if ( csId < 1 ) {
      DLog.warning( lctx , "Failed to schedule channel session "
          + ", found zero csId" );
      return result;
    }

    ChannelSessionService csService = new ChannelSessionService();
    String headerLog = ChannelSessionLogSupport.headerLog( csId );

    DLog.debug(
        lctx ,
        headerLog + "Validating schedule date time : "
            + DateTimeFormat.convertToString( csBean.getDateSchedulledStart() )
            + " to "
            + DateTimeFormat.convertToString( csBean.getDateSchedulledFinish() ) );
    if ( !validSchedule( csBean ) ) {
      DLog.warning( lctx , headerLog + "Failed to schedule channel session "
          + ", found invalid schedule date time" );
      return result;
    }

    DLog.debug( lctx , headerLog + "Reserving client prepaid balance "
        + ", trying to update status schedule channel session" );
    if ( !reserveBalance( clientOut , eventOut , csBean ) ) {
      DLog.warning( lctx , headerLog + "Failed to schedule channel session "
          + ", found failed to reserve client prepaid balance" );
      return result;
    }

    DLog.debug( lctx , headerLog + "Updating status schedule channel session" );
    if ( !csService.updateStatusScheduleChannelSession( csId ) ) {
      DLog.warning( lctx , headerLog + "Failed to schedule channel session "
          + ", found failed to update status schedule channel session" );
      return result;
    }

    result = true;
    return result;
  }

  public static boolean finish( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;
    int csId = ( csBean == null ) ? 0 : csBean.getId();
    if ( csId < 1 ) {
      DLog.warning( lctx , "Failed to finish channel session "
          + ", found zero csId" );
      return result;
    }
    ChannelSessionService csService = new ChannelSessionService();
    String headerLog = ChannelSessionLogSupport.headerLog( csId );
    boolean unreserved = unreserveBalance( clientOut , eventOut , csBean );
    if ( unreserved ) {
      DLog.debug( lctx , headerLog + "Successfully unreserved "
          + "client prepaid balance" );
    }
    result = csService.updateStatusFinishChannelSession( csId );
    return result;
  }

  public static boolean unschedule( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;
    int csId = ( csBean == null ) ? 0 : csBean.getId();
    if ( csId < 1 ) {
      DLog.warning( lctx , "Failed to unschedule channel session "
          + ", found zero csId" );
      return result;
    }
    ChannelSessionService csService = new ChannelSessionService();
    String headerLog = ChannelSessionLogSupport.headerLog( csId );
    boolean unreserved = unreserveBalance( clientOut , eventOut , csBean );
    if ( unreserved ) {
      DLog.debug( lctx , headerLog + "Successfully unreserved "
          + "client prepaid balance" );
    }
    result = csService.updateStatusUnscheduleChannelSession( csId );
    return result;
  }

  public static boolean nextSchedule( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;

    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to setup next schedule channel session "
          + ", found null channel session bean" );
      return result;
    }

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    ChannelSessionService csService = new ChannelSessionService();
    ChannelSessionScheduleService cssService = new ChannelSessionScheduleService();
    ChannelSessionScheduleBean cssBean = cssService.selectNext( csBean.getId() ,
        csBean.getDateSchedulledFinish() );
    if ( cssBean == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to setup next schedule channel session "
          + ", found empty channel session schedule bean" );
      return result;
    }

    if ( !reserveBalance( clientOut , eventOut , csBean ) ) {
      DLog.warning( lctx , headerLog
          + "Failed to setup next schedule channel session "
          + ", found failed to reserve balance" );
      return result;
    }

    DLog.debug( lctx , headerLog
        + "Successfully reserved client prepaid balance "
        + ", trying to update status schedule channel session" );

    if ( !csService.updateChannelSession( csBean.getId() ,
        csBean.getExpectResponse() , ChannelSessionNewStatus.ST_SCHEDULLED ,
        cssBean.getDateStarted() , cssBean.getDateStopped() ) ) {
      DLog.warning( lctx , headerLog
          + "Failed to setup next schedule channel session "
          + ", failed to update channel session record" );
      return result;
    }

    DLog.debug(
        lctx ,
        headerLog + "Setup next schedule broadcast : dateStarted = "
            + DateTimeFormat.convertToString( cssBean.getDateStarted() )
            + " , dateStopped = "
            + DateTimeFormat.convertToString( cssBean.getDateStopped() ) );

    result = true;
    return result;
  }

  public static boolean suspend( int csId ) {
    boolean result = false;
    if ( csId < 1 ) {
      DLog.warning( lctx , "Failed to suspend channel session "
          + ", found zero csId" );
      return result;
    }
    ChannelSessionService csService = new ChannelSessionService();
    String headerLog = ChannelSessionLogSupport.headerLog( csId );
    result = csService.updateStatusSuspendChannelSession( csId );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Schedule Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static boolean validSchedule( ChannelSessionBean csBean ) {
    boolean result = false;

    if ( csBean == null ) {
      return result;
    }

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    Date dateNow = new Date();

    Date dateScheduleStart = csBean.getDateSchedulledStart();
    if ( dateScheduleStart == null ) {
      DLog.warning( lctx , headerLog + "Found invalid schedule "
          + ", found null date schedule start" );
      return result;
    }

    Date dateScheduleFinish = csBean.getDateSchedulledFinish();
    if ( dateScheduleFinish != null ) {
      if ( dateScheduleFinish.getTime() <= dateScheduleStart.getTime() ) {
        DLog.warning( lctx , headerLog + "Found invalid schedule "
            + ", found date schedule finish less than date schedule start" );
        return result;
      }
      if ( dateScheduleFinish.getTime() <= dateNow.getTime() ) {
        DLog.warning( lctx , headerLog + "Found invalid schedule "
            + ", found date schedule finish less than date now" );
        return result;
      }
    }

    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Billing Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static boolean reserveBalance( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    // do reserved balance only for prepaid client
    if ( clientOut.getPaymentType() != PaymentType.PREPAID ) {
      DLog.debug( lctx , headerLog + "No need to do reserve balance "
          + ", found postpaid payment type" );
      result = true;
      return result;
    }

    // calculate total candidate debit
    double totalDebitUnits = ChannelSessionReportSupport
        .calculateTotalDebitAmount( csBean );

    // validate total candidate debit
    if ( totalDebitUnits < 0 ) {
      DLog.debug( lctx , "No need to reserve balance "
          + ", found negatif total debit : " + totalDebitUnits + " unit(s)" );
      result = true;
      return result;
    }

    // do the reserve balance
    if ( !ClientEventReserveBalance.reserveBalance( headerLog ,
        clientOut.getClientId() , eventOut.getEventId() , totalDebitUnits ) ) {
      DLog.warning( lctx , headerLog + "Failed to reserve "
          + "client prepaid balance , total " + totalDebitUnits + " unit(s)" );
      return result;
    }

    // log it
    DLog.debug( lctx , headerLog + "Successfully reserved client prepaid "
        + "balance , total " + totalDebitUnits + " unit(s)" );
    result = true;
    return result;
  }

  public static boolean unreserveBalance( TClient clientOut , TEvent eventOut ,
      ChannelSessionBean csBean ) {
    boolean result = false;

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    // do reserved balance only for prepaid client
    if ( clientOut.getPaymentType() != PaymentType.PREPAID ) {
      DLog.debug( lctx , headerLog + "No need to do reserve balance "
          + ", found postpaid payment type" );
      result = true;
      return result;
    }

    // do the unreserve balance
    Double unreserveUnit = ClientEventReserveBalance.unreserveBalance(
        headerLog , clientOut.getClientId() , eventOut.getEventId() );
    if ( unreserveUnit == null ) {
      DLog.warning( lctx , headerLog + "Failed to perform unreserve "
          + "client prepaid balance" );
      return result;
    }

    // log it
    DLog.debug( lctx , headerLog + "Successfully unreserved client prepaid "
        + "balance , total " + unreserveUnit + " unit(s)" );
    result = true;
    return result;
  }

}
