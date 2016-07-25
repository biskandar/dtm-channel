package com.beepcast.channel.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.beepcast.billing.BillingApp;
import com.beepcast.billing.BillingResult;
import com.beepcast.billing.BillingStatus;
import com.beepcast.billing.profile.PaymentType;
import com.beepcast.channel.ChannelApp;
import com.beepcast.channel.ChannelConf;
import com.beepcast.channel.ChannelSessionNewStatus;
import com.beepcast.channel.common.ChannelSessionReportSupport;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.model.client.ClientType;
import com.beepcast.model.transaction.billing.AccountProfile;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListChannelSessionInfoService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ListChannelSessionInfoService" );

  private static String CanonicalDateTimeFormat = "dd MMM yyyy HH:mm 'GMT'Z";

  public static final int STATUS_SCHEDULLED_UNKNOWN = 0;
  public static final int STATUS_SCHEDULLED_EMPTYBEAN = 1;
  public static final int STATUS_SCHEDULLED_NOCLIENT = 2;
  public static final int STATUS_SCHEDULLED_NOBALANCE = 3;
  public static final int STATUS_SCHEDULLED_READY = 4;
  public static final int STATUS_SCHEDULLED_AUTO = 5;
  public static final int STATUS_SCHEDULLED_INPROGRESS = 6;
  public static final int STATUS_SCHEDULLED_THROTTLE = 7;

  private static BillingApp billingApp = BillingApp.getInstance();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelApp channelApp;
  private ChannelConf channelConf;
  private boolean debug;
  private ListChannelSessionInfoDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ListChannelSessionInfoService( ChannelApp channelApp ,
      ChannelConf channelConf ) {
    this.channelApp = channelApp;
    this.channelConf = channelConf;
    if ( this.channelConf != null ) {
      debug = channelConf.isDebug();
    }
    dao = new ListChannelSessionInfoDAO();
  }

  public ListChannelSessionInfoService() {
    channelApp = ChannelApp.getInstance();
    if ( channelApp != null ) {
      channelConf = channelApp.getChannelConf();
    }
    if ( channelConf != null ) {
      debug = channelConf.isDebug();
    }
    dao = new ListChannelSessionInfoDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List getListHistoryBroadcast( int clientId ) {
    List list = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get the list of History Broadcast "
          + ", found zero clientId" );
      return list;
    }
    list = dao.getListHistoryBroadcast( clientId , debug );
    return list;
  }

  public List getListSchedulledBroadcast( int clientId ) {
    List list = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get the list of Schedulled Broadcast "
          + ", found zero clientId" );
      return list;
    }
    list = dao.getListSchedulledBroadcast( clientId , debug );
    return list;
  }

  public List getListRunningBroadcast( int clientId ) {
    List list = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get the list of Running Broadcast "
          + ", found zero clientId" );
      return list;
    }
    list = dao.getListRunningBroadcast( clientId , debug );
    return list;
  }

  public int getTotalRunningBroadcast( int clientId ) {
    int totalBroadcasts = 0;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get the total of Running Broadcast "
          + ", found zero clientId" );
      return totalBroadcasts;
    }
    totalBroadcasts = dao.getTotalRunningBroadcast( clientId , debug );
    return totalBroadcasts;
  }

  public List getListHistoryBroadcastFromMaster( int clientType ,
      int masterClientId ) {
    List list = new ArrayList();
    if ( clientType == ClientType.SUPER ) {
      masterClientId = 0;
    }
    if ( clientType == ClientType.MASTER ) {
      if ( masterClientId < 1 ) {
        DLog.warning( lctx , "Failed to list history broadcast from master "
            + ", found zero masterClientId" );
        return list;
      }
    }
    list = dao.getListHistoryBroadcastFromMaster( masterClientId , debug );
    return list;
  }

  public List getListSchedulledBroadcastFromMaster( int clientType ,
      int masterClientId ) {
    List list = new ArrayList();
    if ( clientType == ClientType.SUPER ) {
      masterClientId = 0;
    }
    if ( clientType == ClientType.MASTER ) {
      if ( masterClientId < 1 ) {
        DLog.warning( lctx , "Failed to list scheduled broadcast from master "
            + ", found zero masterClientId" );
        return list;
      }
    }
    list = dao.getListSchedulledBroadcastFromMaster( masterClientId , debug );
    return list;
  }

  public List getListRunningBroadcastFromMaster( int clientType ,
      int masterClientId ) {
    List list = new ArrayList();
    if ( clientType == ClientType.SUPER ) {
      masterClientId = 0;
    }
    if ( clientType == ClientType.MASTER ) {
      if ( masterClientId < 1 ) {
        DLog.warning( lctx , "Failed to list running broadcast from master "
            + ", found zero masterClientId" );
        return list;
      }
    }
    list = dao.getListRunningBroadcastFromMaster( masterClientId , debug );
    return list;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Publish Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static int getStatusSchedulledBroadcast( ChannelSessionInfoBean bean ) {
    int status = STATUS_SCHEDULLED_UNKNOWN;

    if ( bean == null ) {
      status = STATUS_SCHEDULLED_EMPTYBEAN;
      return status;
    }

    String headerLog = "[ChannelSession-" + bean.getChannelSessionId() + "] ";

    int clientId = bean.getClientId();
    if ( clientId < 1 ) {
      status = STATUS_SCHEDULLED_NOCLIENT;
      return status;
    }

    // get client level bean
    TClientLevel clientLevelBean = ChannelSessionReportSupport
        .getClientLevelBean( bean );
    if ( clientLevelBean == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to get status schedulled broadcast "
          + ", found null client level" );
      status = STATUS_SCHEDULLED_NOCLIENT;
      return status;
    }

    // get the new status info
    int newStatus = bean.getNewStatus();

    // decided to which mode ?
    if ( newStatus == ChannelSessionNewStatus.ST_EMPTY ) {
      // capable to override
      status = STATUS_SCHEDULLED_READY;
    } else if ( newStatus == ChannelSessionNewStatus.ST_SCHEDULLED ) {
      // capable to override
      status = STATUS_SCHEDULLED_AUTO;
      return status;
    } else {
      status = STATUS_SCHEDULLED_INPROGRESS;
      return status;
    }

    // get payment type information
    int paymentType = bean.getPaymentType();

    // validate balance if found prepaid client
    if ( paymentType == PaymentType.PREPAID ) {

      double totalDebitAmount = ChannelSessionReportSupport
          .calculateTotalDebitAmount( bean );

      // log it
      DLog.debug( lctx , headerLog + "Generating status , found clientId = "
          + clientId + " , paymentType = " + paymentType
          + " , totalDebitAmount = " + totalDebitAmount + " unit(s)" );

      // get daily client track
      if ( clientLevelBean.getDailyTrafficQuota() > 0 ) {
        Double dailyClientTrack = getDailyClientTrack( clientId );
        if ( dailyClientTrack == null ) {
          DLog.warning( lctx , headerLog
              + "Set status schedulled broadcast : NOCLIENT "
              + ", found null daily client track account" );
          status = STATUS_SCHEDULLED_NOCLIENT;
          return status;
        }
        if ( dailyClientTrack.doubleValue() < totalDebitAmount ) {
          DLog.warning( lctx , headerLog
              + "Set status schedulled broadcast : THROTTLE "
              + ", found unsufficient daily client track" );
          status = STATUS_SCHEDULLED_THROTTLE;
          return status;
        }
      }

      // get monthly client track
      if ( clientLevelBean.getMonthlyTrafficQuota() > 0 ) {
        Double monthlyClientTrack = getMonthlyClientTrack( clientId );
        if ( monthlyClientTrack == null ) {
          DLog.warning( lctx , headerLog
              + "Set status schedulled broadcast : NOCLIENT "
              + ", found null monthly client track account" );
          status = STATUS_SCHEDULLED_NOCLIENT;
          return status;
        }
        if ( monthlyClientTrack.doubleValue() < totalDebitAmount ) {
          DLog.warning( lctx , headerLog
              + "Set status schedulled broadcast : THROTTLE "
              + ", found unsufficient monthly client track" );
          status = STATUS_SCHEDULLED_THROTTLE;
          return status;
        }
      }

      // get client prepaid balance
      Double prepaidClientBalance = getPrepaidClientBalance( clientId );
      if ( prepaidClientBalance == null ) {
        DLog.warning( lctx , headerLog
            + "Set status schedulled broadcast : NOCLIENT "
            + ", found null prepaid client balance account" );
        status = STATUS_SCHEDULLED_NOCLIENT;
        return status;
      }
      if ( prepaidClientBalance.doubleValue() < totalDebitAmount ) {
        DLog.warning( lctx , headerLog
            + "Set status schedulled broadcast : NOBALANCE "
            + ", found unsufficient prepaid client balance" );
        status = STATUS_SCHEDULLED_NOBALANCE;
        return status;
      }

    }

    return status;
  }

  public static String getStatusSchedulledBroadcast( int statusSchedulled ) {
    String statusString = "";
    switch ( statusSchedulled ) {
    case STATUS_SCHEDULLED_UNKNOWN :
      statusString = "UNKNOWN";
      break;
    case STATUS_SCHEDULLED_EMPTYBEAN :
      statusString = "EMPTY";
      break;
    case STATUS_SCHEDULLED_NOCLIENT :
      statusString = "NO CLIENT";
      break;
    case STATUS_SCHEDULLED_NOBALANCE :
      statusString = "LOW BALANCE";
      break;
    case STATUS_SCHEDULLED_READY :
      statusString = "READY";
      break;
    case STATUS_SCHEDULLED_AUTO :
      statusString = "SCHEDULED";
      break;
    case STATUS_SCHEDULLED_INPROGRESS :
      statusString = "PROCESSING";
      break;
    case STATUS_SCHEDULLED_THROTTLE :
      statusString = "THROTTLE";
      break;
    }
    return statusString;
  }

  public static String convertDateTime( Date date ) {
    String result = "";
    if ( date != null ) {
      SimpleDateFormat sdf = new SimpleDateFormat( CanonicalDateTimeFormat );
      try {
        result = sdf.format( date );
      } catch ( Exception e ) {
        result = "";
      }
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static Double getDailyClientTrack( int clientId ) {
    Double balance = null;
    if ( billingApp == null ) {
      return balance;
    }
    BillingResult billingResult = billingApp.getBalance(
        AccountProfile.DAILY_CLIENT_TRACK , new Integer( clientId ) );
    if ( ( billingResult == null )
        || ( billingResult.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) ) {
      return balance;
    }
    balance = billingResult.getBalanceAfter();
    return balance;
  }

  private static Double getMonthlyClientTrack( int clientId ) {
    Double balance = null;
    if ( billingApp == null ) {
      return balance;
    }
    BillingResult billingResult = billingApp.getBalance(
        AccountProfile.MONTHLY_CLIENT_TRACK , new Integer( clientId ) );
    if ( ( billingResult == null )
        || ( billingResult.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) ) {
      return balance;
    }
    balance = billingResult.getBalanceAfter();
    return balance;
  }

  private static Double getPrepaidClientBalance( int clientId ) {
    Double balance = null;
    if ( billingApp == null ) {
      return balance;
    }
    BillingResult billingResult = billingApp.getBalance(
        AccountProfile.CLIENT_PREPAID , new Integer( clientId ) );
    if ( ( billingResult == null )
        || ( billingResult.getPaymentResult() != BillingStatus.PAYMENT_RESULT_SUCCEED ) ) {
      return balance;
    }
    balance = billingResult.getBalanceAfter();
    return balance;
  }

}
