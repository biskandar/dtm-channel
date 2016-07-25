package com.beepcast.channel;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementReport {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelManagementReport" );

  static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp oprops;

  private ChannelApp app;
  private ChannelConf conf;

  private int lastDays;
  private int expiryDays;

  private ChannelSessionService channelSessionService;
  private ChannelSubscriberDAO channelSubscriberDAO;
  private ChannelManagementReportDAO channelManagementReportDAO;
  private ChannelSessionReportService channelSessionReportService;

  private boolean initialized;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReport( ChannelApp app ) {
    initialized = false;

    oprops = OnlinePropertiesApp.getInstance();

    if ( app != null ) {
      this.app = app;
    } else {
      // if call from third party it will get from global resource
      this.app = ChannelApp.getInstance();
      if ( this.app == null ) {
        DLog.error( lctx , "Failed to initialized channel management report "
            + ", found empty channelApp" );
        return;
      }
      DLog.debug( lctx , "Created a new management report from third party" );
    }

    conf = this.app.getChannelConf();
    if ( conf == null ) {
      DLog.error( lctx , "Failed to initialized channel management report "
          + ", found empty channelConf" );
      return;
    }

    lastDays = conf.getManagementUpdateChannelSessionReport_lastDays();
    if ( lastDays < 1 ) {
      lastDays = 1;
      DLog.warning( lctx , "Failed to get property lastDays "
          + ", using default" );
    }
    DLog.debug(
        lctx ,
        "Defined param duration last = "
            + (int) oprops.getLong( "Channel.UpdateBroadcastReport.LastDays" ,
                lastDays ) + " day(s)" );

    expiryDays = conf.getManagementUpdateChannelSessionReport_expiryDays();
    if ( expiryDays < 1 ) {
      expiryDays = 30;
      DLog.warning( lctx , "Failed to get property expiryDays "
          + ", using default" );
    }
    DLog.debug(
        lctx ,
        "Defined param duration expiry = "
            + (int) oprops.getLong( "Channel.UpdateBroadcastReport.ExpiryDays" ,
                expiryDays ) + " day(s)" );

    // create support object
    channelSessionService = new ChannelSessionService();
    channelSubscriberDAO = new ChannelSubscriberDAO();
    channelManagementReportDAO = new ChannelManagementReportDAO();
    channelSessionReportService = new ChannelSessionReportService();

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List listAvailChannelSessionIdsReadyToReport() {
    List listChannelSessionIds = null;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to list avail channel session ids "
          + ", found not yet initialized" );
      return listChannelSessionIds;
    }
    listChannelSessionIds = channelManagementReportDAO
        .getCurrentStoppedChannelSessionId( (int) oprops.getLong(
            "Channel.UpdateBroadcastReport.LastDays" , lastDays ) , conf
            .isDebug() );
    return listChannelSessionIds;
  }

  public boolean updateChannelSessionReport( int channelSessionId ,
      boolean reqFromExternal ) {
    boolean updated = false;
    // validate channel session id
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to generate report "
          + ", found empty channelSessionId" );
      return updated;
    }
    // update channel session report based on channel session bean
    return updateChannelSessionReport(
        channelSessionService.getChannelSession( channelSessionId ) ,
        reqFromExternal );
  }

  public boolean updateChannelSessionReport( ChannelSessionBean csBean ,
      boolean reqFromExternal ) {
    boolean updated = false;

    // get current long date time
    long dateNow = System.currentTimeMillis();

    // create header log
    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    // validate channel session bean
    if ( csBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to generate report "
          + ", found empty channel session bean id" );
      return updated;
    }

    // validate to make sure the channel session is suspended / finished already
    if ( !( csBean.isFinished() || csBean.isSuspended() ) ) {
      DLog.warning( lctx , headerLog + "Failed to generate report "
          + ", found channel session is not yet suspended or finished" );
      return updated;
    }

    // validate eventId and groupSubscriberId
    if ( ( csBean.getEventId() < 1 ) || ( csBean.getGroupSubscriberId() < 1 ) ) {
      DLog.warning( lctx , headerLog + "Failed to generate report "
          + ", found empty and/or invalid eventId and/or groupSubscriberId" );
      return updated;
    }

    // prepare temporary variables
    ChannelSessionReportBean newCsrBean = null , oldCsrBean = null;
    String fileReportNameWithoutExt = null;

    // log first
    DLog.debug( lctx , headerLog + "Updating channel session report "
        + ": requestFromExternal = " + reqFromExternal );

    // query the latest generated report
    oldCsrBean = channelSessionReportService.selectByChannelSessionId( csBean
        .getId() );
    if ( oldCsrBean != null ) {
      // log it
      DLog.debug(
          lctx ,
          headerLog + "Read previous channel session report " + ": id = "
              + oldCsrBean.getId() + " , totalSent = "
              + oldCsrBean.getTotalSent() + " number(s) , totalDelivered = "
              + oldCsrBean.getTotalDnDelivered() + " number(s)" );
    }

    // validate for third party request
    if ( reqFromExternal ) {

      if ( oldCsrBean == null ) {
        // External requestor doesn't allow to generate the first report
        // let the internal system do the one
        DLog.debug( lctx , headerLog + "External requestor doesn't not allow "
            + "to generate a new report , stop process here" );
        return updated;
      }

      // calculated date time period of generated last report
      long dateInserted = oldCsrBean.getDateInserted().getTime();
      long dateUpdated = oldCsrBean.getDateUpdated().getTime();

      // suppose not to conflict with auto generated report
      int ld = (int) oprops.getLong( "Channel.UpdateBroadcastReport.LastDays" ,
          lastDays );
      long dateLastDays = ld * 24 * 3600 * 1000L;
      if ( ( dateNow - dateInserted ) <= dateLastDays ) {
        // force to use the auto generated report
        DLog.debug(
            lctx ,
            headerLog + "Can not update the new report "
                + ", still in the auto generated report period , lastDays = "
                + ld + " , lastCreated was "
                + DateTimeFormat.convertToString( oldCsrBean.getDateInserted() ) );
        return updated;
      }

      // restrict for ( the expiry date ) and ( total send is calculated )
      int ed = (int) oprops.getLong(
          "Channel.UpdateBroadcastReport.ExpiryDays" , expiryDays );
      long dateExpiryDays = ed * 24 * 3600 * 1000L;
      if ( ( ( dateNow - dateInserted ) >= dateExpiryDays )
          && ( oldCsrBean.getTotalSent() > 0 ) ) {
        DLog.debug(
            lctx ,
            headerLog + "Can not update the new report "
                + ", found in the expiry report period , expiryDays = " + ed
                + " , lastCreated was "
                + DateTimeFormat.convertToString( oldCsrBean.getDateInserted() ) );
        return updated;
      }

      // log if succeed
      DLog.debug( lctx , headerLog + "Valid to pass all the external "
          + "request validations , continue to updating the report" );

    } // if ( reqFromExternal )

    if ( oldCsrBean != null ) {

      // calculated date time period of generated last report
      long dateInserted = oldCsrBean.getDateInserted().getTime();
      long dateUpdated = oldCsrBean.getDateUpdated().getTime();

      // bypass to refresh report when found report created within n minutes
      int pe = (int) oprops.getLong(
          "Channel.UpdateBroadcastReport.AfterInMinutes" , 15 );
      long dateLastMinutes = pe * 60 * 1000L;
      if ( ( dateNow - dateUpdated ) <= dateLastMinutes ) {
        if ( reqFromExternal || conf.isDebug() ) {
          DLog.debug(
              lctx ,
              headerLog
                  + "Can not update the new report "
                  + ", still in the last generated report period , lastMinutes = "
                  + pe
                  + " , lastUpdated was "
                  + DateTimeFormat.convertToString( oldCsrBean.getDateUpdated() )
                  + " ( cache in " + pe + " minutes ) " );
        }
        return updated;
      }

    } // if ( oldCsrBean != null )

    // create new channel session report bean with defined channelSessionId
    newCsrBean = new ChannelSessionReportBean();
    newCsrBean.setChannelSessionId( csBean.getId() );

    { // update totalSent
      long totalSentFromGatewayLog = channelManagementReportDAO
          .getTotalInsertedInGatewayLog( newCsrBean.getChannelSessionId() ,
              conf.isDebug() );
      long totalSentFromTransaction = channelManagementReportDAO
          .getTotalInsertedInTransaction( newCsrBean.getChannelSessionId() ,
              conf.isDebug() );
      newCsrBean.setTotalSent( totalSentFromGatewayLog
          + totalSentFromTransaction );
      if ( reqFromExternal || conf.isDebug() ) {
        DLog.debug( lctx ,
            headerLog + "Read totalSent = " + newCsrBean.getTotalSent()
                + " record(s) : fromGatewayLog = " + totalSentFromGatewayLog
                + " + fromTransaction = " + totalSentFromTransaction );
      }
    }

    // make sure there is no empty sent transactions
    if ( newCsrBean.getTotalSent() < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to update new report "
          + ", found empty sent record(s) from the gatewayLog and "
          + "transaction table , previous was "
          + ( ( oldCsrBean != null ) ? oldCsrBean.getTotalSent() : 0 )
          + " record(s)" );
      return updated;
    }

    { // update totalDnDelivered, totalDnInvalidNumber, and totalDnEmpty
      if ( channelManagementReportDAO.updateChannelSessionReportFromGatewayLog(
          newCsrBean , conf.isDebug() ) ) {
        if ( reqFromExternal || conf.isDebug() ) {
          DLog.debug(
              lctx ,
              headerLog + "Updated channel session report bean "
                  + "from gateway log : totalDnDelivered = "
                  + newCsrBean.getTotalDnDelivered()
                  + " , totalDnInvalidNumber = "
                  + newCsrBean.getTotalDnInvalidNumber() + " , totalDnEmpty = "
                  + newCsrBean.getTotalDnEmpty() );
        }
      }
      if ( channelManagementReportDAO
          .updateChannelSessionReportFromTransaction( newCsrBean ,
              conf.isDebug() ) ) {
        if ( reqFromExternal || conf.isDebug() ) {
          DLog.debug(
              lctx ,
              headerLog + "Updated channel session report bean "
                  + "from transaction : totalDnDelivered = "
                  + newCsrBean.getTotalDnDelivered()
                  + " , totalDnInvalidNumber = "
                  + newCsrBean.getTotalDnInvalidNumber() + " , totalDnEmpty = "
                  + newCsrBean.getTotalDnEmpty() );
        }
      }
    }

    { // update totalRetry
      long totalRetryFromGatewayLog = channelManagementReportDAO
          .getTotalRetryInGatewayLog( newCsrBean.getChannelSessionId() ,
              conf.isDebug() );
      long totalRetryFromTransaction = channelManagementReportDAO
          .getTotalRetryInTransaction( newCsrBean.getChannelSessionId() ,
              conf.isDebug() );
      newCsrBean.setTotalRetry( totalRetryFromGatewayLog
          + totalRetryFromTransaction );
      if ( reqFromExternal || conf.isDebug() ) {
        DLog.debug( lctx ,
            headerLog + "Read totalRetry = " + newCsrBean.getTotalRetry()
                + " record(s) : fromGatewayLog = " + totalRetryFromGatewayLog
                + " + fromTransaction = " + totalRetryFromTransaction );
      }
    }

    { // update and calculate total unsubs subscriber
      newCsrBean.setTotalUnsubs( channelSubscriberDAO
          .getTotalUnsubscribedFromEventId( csBean.getEventId() ,
              conf.isDebug() ) );
      if ( reqFromExternal || conf.isDebug() ) {
        DLog.debug( lctx , headerLog + "Read total unsubscribed from "
            + "the list based on event id = " + newCsrBean.getTotalUnsubs()
            + " number(s)" );
      }
      if ( newCsrBean.getTotalUnsubs() < 1 ) {
        // this code shall be gone after certain of months ...
        newCsrBean.setTotalUnsubs( channelSubscriberDAO
            .getTotalUnsubscribedFromSubscriberGroupId(
                csBean.getGroupSubscriberId() , conf.isDebug() ) );
        if ( reqFromExternal || conf.isDebug() ) {
          DLog.debug(
              lctx ,
              headerLog + "Read total unsubscribed from "
                  + "the list based on subscriber group id = "
                  + newCsrBean.getTotalUnsubs() + " number(s)" );
        }
      }
    }

    { // update and calculate total global unsubs subscriber
      newCsrBean.setTotalGlobalUnsubs( channelSubscriberDAO
          .getTotalGlobalUnsubscribed( csBean.getGroupSubscriberId() ,
              conf.isDebug() ) );
    }

    // is this the first time report generated ?
    if ( oldCsrBean == null ) {

      // generated missing identifier
      newCsrBean.setKey( app.createBroadcastFileName(
          newCsrBean.getChannelSessionId() , false ) );
      if ( StringUtils.isBlank( newCsrBean.getKey() ) ) {
        newCsrBean.setKey( Integer.toString( newCsrBean.getChannelSessionId() )
            .concat( RandomStringUtils.randomAlphabetic( 5 ) ) );
      }
      if ( StringUtils.isBlank( newCsrBean.getKey() ) ) {
        DLog.warning( lctx , headerLog + "Failed to insert channel session "
            + "report , found failed to create broadcast file name" );
        return updated;
      }

      // log it
      DLog.debug( lctx , headerLog + "Inserting a new report " + newCsrBean );

      // insert the new info records
      if ( !channelSessionReportService.insert( newCsrBean ) ) {
        DLog.warning( lctx , headerLog + "Failed to insert channel session "
            + "report" );
        return updated;
      }

      // log it
      updated = true;
      DLog.debug( lctx , headerLog + "Successfully perform inserted for "
          + "channel session report summary " );

    } else {

      // make sure to efficient the update mechanism
      if ( ( newCsrBean.getTotalSent() == oldCsrBean.getTotalSent() )
          && ( newCsrBean.getTotalRetry() == oldCsrBean.getTotalRetry() )
          && ( newCsrBean.getTotalDnDelivered() == oldCsrBean
              .getTotalDnDelivered() )
          && ( newCsrBean.getTotalDnEmpty() == oldCsrBean.getTotalDnEmpty() )
          && ( newCsrBean.getTotalDnFailed() == oldCsrBean.getTotalDnFailed() )
          && ( newCsrBean.getTotalDnInvalidNumber() == oldCsrBean
              .getTotalDnInvalidNumber() )
          && ( newCsrBean.getTotalUnsubs() == oldCsrBean.getTotalUnsubs() )
          && ( newCsrBean.getTotalGlobalUnsubs() == oldCsrBean
              .getTotalGlobalUnsubs() ) ) {
        DLog.debug( lctx , headerLog + "Bypass updating the new report "
            + ", found same all property number with previous report"
            + ", stop process here." );
        return updated;
      }

      // clone missing identifier
      newCsrBean.setId( oldCsrBean.getId() );
      newCsrBean.setKey( oldCsrBean.getKey() );

      // log it
      DLog.debug( lctx , headerLog + "Updating a new report " + newCsrBean );

      // update the new info records
      if ( !channelSessionReportService.update( newCsrBean ) ) {
        DLog.warning( lctx , headerLog + "Failed to update channel session "
            + "report , found database failed to updated" );
        return updated;
      }

      // log it
      updated = true;
      DLog.debug( lctx , headerLog + "Successfully perform updated for "
          + "channel session report summary " );

    }

    return updated;
  }
  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
