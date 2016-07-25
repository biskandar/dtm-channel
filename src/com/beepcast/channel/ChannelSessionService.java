package com.beepcast.channel;

import java.util.Date;
import java.util.List;

import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.channel.common.ChannelSessionReportSupport;
import com.beepcast.channel.view.ListChannelSessionInfoService;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.subscriber.SubscriberApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSessionService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelApp channelApp;
  private SubscriberApp subscriberApp;
  private ChannelSessionDAO dao;
  private ChannelSessionSchedulesService csssService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionService() {
    channelApp = ChannelApp.getInstance();
    subscriberApp = SubscriberApp.getInstance();
    dao = new ChannelSessionDAO();
    csssService = new ChannelSessionSchedulesService();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insertChannelSession( int eventId , int groupSubscriberId ,
      boolean allowDuplicated , boolean useDateSend , double expectResponse ,
      List listChannelSessionSchedules ) {
    boolean result = false;

    // validate must be params
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to insert channel session "
          + ", found zero eventId" );
      return result;
    }
    if ( groupSubscriberId < 1 ) {
      DLog.warning( lctx , "Failed to insert channel session "
          + ", found zero groupSubscriberId" );
      return result;
    }

    // get total subscriber(s)
    long totalSubscribers = subscriberApp.getTotalSubscriber(
        groupSubscriberId , allowDuplicated , useDateSend );

    // set default status
    int newStatus = ChannelSessionNewStatus.ST_EMPTY;

    // calculate total debit unit
    double totalDebitUnits = ChannelSessionReportSupport
        .calculateTotalDebitAmount( 0 , allowDuplicated , useDateSend ,
            groupSubscriberId , eventId , totalSubscribers , totalSubscribers ,
            0 , expectResponse );

    // get first record from listChannelSessionSchedules
    ChannelSessionScheduleBean cssBean = null;
    if ( ( listChannelSessionSchedules != null )
        && ( listChannelSessionSchedules.size() > 0 ) ) {
      cssBean = (ChannelSessionScheduleBean) listChannelSessionSchedules
          .get( 0 );
    }
    Date dateScheduledStart = null;
    Date dateScheduledFinish = null;
    if ( cssBean != null ) {
      dateScheduledStart = cssBean.getDateStarted();
      dateScheduledFinish = cssBean.getDateStopped();
    }

    // create channel session bean
    ChannelSessionBean bean = ChannelSessionBeanFactory
        .createChannelSessionBean( eventId , groupSubscriberId ,
            allowDuplicated , useDateSend , expectResponse , totalSubscribers ,
            totalSubscribers , totalDebitUnits , 0 , false , false , false ,
            newStatus , null , null , null , dateScheduledStart ,
            dateScheduledFinish );

    // log it
    DLog.debug(
        lctx ,
        "Insert new channel session : eventId = " + bean.getEventId()
            + " , groupSubscriberId = " + bean.getGroupSubscriberId()
            + " , allowDuplicated = " + bean.isAllowDuplicated()
            + " , useDateSend = " + useDateSend + " , expectResponse = "
            + bean.getExpectResponse() + " , totalNumbers = "
            + bean.getTotalNumbers() + " , debitUnits = "
            + bean.getDebitUnits() + " , newStatus = " + bean.getNewStatus()
            + " , dateScheduledStart = "
            + DateTimeFormat.convertToString( bean.getDateSchedulledStart() )
            + " , dateScheduledFinish = "
            + DateTimeFormat.convertToString( bean.getDateSchedulledFinish() ) );

    // insert channel session bean into table
    result = dao.insertChannelSession( bean );
    if ( result ) {

      // re select the insert record
      bean = dao.getLastChannelSession( channelApp.isDebug() , eventId );

    }
    if ( ( bean != null ) && ( bean.getId() > 0 ) ) {

      // update channel session schedule service
      csssService.delete( bean.getId() );
      int insertedRecords = csssService.insert( listChannelSessionSchedules ,
          bean.getId() );
      DLog.debug( lctx , "Inserted channel session schedule beans "
          + ", with : total = " + insertedRecords + " record(s)" );

    }

    return result;
  }

  public boolean deleteChannelSession( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to delete channel session "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.deleteChannelSession( channelSessionId );
    if ( result ) {
      csssService.delete( channelSessionId );
    }
    return result;
  }

  public boolean updateChannelSession( int channelSessionId ,
      double expectResponse , int newStatus , Date dateSchedulledStart ,
      Date dateSchedulledFinish ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to update channel session "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.updateChannelSession2( channelSessionId , expectResponse ,
        newStatus , dateSchedulledStart , dateSchedulledFinish );
    return result;
  }

  public boolean updateChannelSession( int channelSessionId ,
      int groupSubscriberId , boolean allowDuplicated , boolean useDateSend ,
      double expectResponse , List listChannelSessionSchedules ) {
    boolean result = false;

    // header log
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // verify group subscriber id
    if ( groupSubscriberId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to update channel session "
          + ", found zero groupSubscriberId" );
      return result;
    }

    // get first record from listChannelSessionSchedules
    ChannelSessionScheduleBean cssBean = null;
    if ( ( listChannelSessionSchedules != null )
        && ( listChannelSessionSchedules.size() > 0 ) ) {
      cssBean = (ChannelSessionScheduleBean) listChannelSessionSchedules
          .get( 0 );
    }
    Date dateScheduledStart = null;
    Date dateScheduledFinish = null;
    if ( cssBean != null ) {
      dateScheduledStart = cssBean.getDateStarted();
      dateScheduledFinish = cssBean.getDateStopped();
    }

    // read and validate channel session bean
    ChannelSessionBean bean = dao.getChannelSession( channelApp.isDebug() ,
        channelSessionId );
    if ( bean == null ) {
      DLog.warning( lctx , headerLog + "Failed to update channel session "
          + ", can not found channel session with id = " + channelSessionId );
      return result;
    }

    // set default status
    int newStatus = ChannelSessionNewStatus.ST_EMPTY;

    // update channel session record based on current status broadcast started
    if ( bean.isStarted() ) {

      // if is started , just update expectResponse , newStatus ,
      // dateSchedulledStart

      // log it
      DLog.debug(
          lctx ,
          headerLog + "Update channel session : dateScheduledStart = "
              + DateTimeFormat.convertToString( dateScheduledStart )
              + " , dateScheduledFinish = "
              + DateTimeFormat.convertToString( dateScheduledFinish )
              + " , expectResponse = " + expectResponse );

      // update record into table
      result = dao.updateChannelSession2( channelSessionId , expectResponse ,
          newStatus , dateScheduledStart , dateScheduledFinish );

    } else {

      // if is not yet start , capable to update :
      // groupSubscriberId , allowDuplicated , useDateSend ,
      // expectResponse , totalSubscriber , totalDebitUnitsFinal ,
      // newStatus ,dateSchedulledStart

      // get total subscriber(s)
      long totalSubscriber = subscriberApp.getTotalSubscriber(
          groupSubscriberId , allowDuplicated , useDateSend );

      // calculate total debit unit
      double totalDebitUnits = ChannelSessionReportSupport
          .calculateTotalDebitAmount( channelSessionId , allowDuplicated ,
              useDateSend , groupSubscriberId , bean.getEventId() ,
              totalSubscriber , totalSubscriber , 0 , expectResponse );

      // log it
      DLog.debug(
          lctx ,
          headerLog + "Update channel session : groupSubscriberId = "
              + groupSubscriberId + " , allowDuplicated = " + allowDuplicated
              + " useDateSend = " + useDateSend + " , expectResponse = "
              + expectResponse + " , totalSubscriber = " + totalSubscriber
              + " , totalDebitUnits = " + totalDebitUnits + " , newStatus = "
              + newStatus + " , dateScheduledStart  = "
              + DateTimeFormat.convertToString( dateScheduledStart )
              + " , dateScheduledFinish = "
              + DateTimeFormat.convertToString( dateScheduledFinish ) );

      // update record into table
      result = dao.updateChannelSession1( channelSessionId , groupSubscriberId ,
          allowDuplicated , useDateSend , expectResponse , totalSubscriber ,
          totalSubscriber , totalDebitUnits , newStatus , dateScheduledStart ,
          dateScheduledFinish );

    }

    if ( result ) {
      // update channel session schedule service
      csssService.delete( bean.getId() );
      int insertedRecords = csssService.insert( listChannelSessionSchedules );
      DLog.debug( lctx , "Inserted channel session schedule beans "
          + ", with : total = " + insertedRecords + " record(s)" );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int restartChannelSessions() {
    return dao.restartChannelSessions();
  }

  public boolean updateStatusStartChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_STARTED );
  }

  public boolean updateStatusScheduleChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_SCHEDULLED );
  }

  public boolean updateStatusFinishChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_FINISHED );
  }

  public boolean updateStatusUnscheduleChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_EMPTY );
  }

  public boolean updateStatusSuspendChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_SUSPENDED );
  }

  public boolean updateStatusEmptyChannelSession( int channelSessionId ) {
    return dao.updateNewStatus( channelSessionId ,
        ChannelSessionNewStatus.ST_EMPTY );
  }

  public boolean validChannelSessionConcurrent( ChannelSessionBean csBean ) {
    boolean result = false;

    // header log

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    // get client profile

    TClient clientOut = csBean.getClientOut();
    if ( clientOut == null ) {
      DLog.warning( lctx , headerLog + "Found invalid channel session "
          + "concurrent , found blank client profile" );
      return result;
    }

    // get maximum broadcast concurrent from client level profile

    TClientLevel clientLevelOut = csBean.getClientLevelOut();
    if ( clientLevelOut == null ) {
      DLog.debug( lctx , headerLog + "Found valid channel session concurrent "
          + ", no restriction client level profile found" );
      result = true;
      return result;
    }
    int maxConcurrent = clientLevelOut.getMaxCampaignConcurrent();
    if ( maxConcurrent < 1 ) {
      DLog.debug( lctx , headerLog + "Found valid channel session concurrent "
          + ", unlimit restriction client level profile found" );
      result = true;
      return result;
    }
    DLog.debug( lctx , headerLog + "Define restriction max total "
        + maxConcurrent + " broadcast(s) concurrent" );

    // get total current client's broadcast

    ListChannelSessionInfoService listService = new ListChannelSessionInfoService();
    int curConcurrent = listService.getTotalRunningBroadcast( clientOut
        .getClientId() );
    DLog.debug( lctx , headerLog + "Found current total " + curConcurrent
        + " broadcast(s) concurrent" );
    if ( curConcurrent < 1 ) {
      DLog.debug( lctx , headerLog + "Found valid channel session concurrent "
          + ", total zero current broadcasts running found" );
      result = true;
      return result;
    }

    // verify it

    if ( curConcurrent < maxConcurrent ) {
      DLog.debug( lctx , headerLog + "Found valid channel session concurrent "
          + ", total current broadcasts running isn't reached the limit" );
      result = true;
      return result;
    }

    DLog.warning( lctx , headerLog
        + "Found invalid channel session concurrent : total " + curConcurrent
        + " current broadcast(s) running is reached the limit " + maxConcurrent
        + " broadcast(s)" );
    return result;
  }

  public boolean updateStartedChannelSession( int channelSessionId ,
      long totalSubscribers , double debitUnits , Date dateStarted ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to update started channel session "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.updateStartedChannelSession( channelSessionId ,
        totalSubscribers , debitUnits , dateStarted );
    return result;
  }

  public boolean updateStartedChannelSession( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to update started channel session "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.updateStartedChannelSession( channelSessionId );
    return result;
  }

  public boolean updateSuspendedChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;
    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to update suspended channel session "
          + ", found null channel session bean" );
      return result;
    }
    result = dao.updateSuspendedChannelSession( csBean );
    return result;
  }

  public boolean updateFinishedChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;
    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to update finished channel session "
          + ", found null channel session bean" );
      return result;
    }
    result = dao.updateFinishedChannelSession( csBean );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateTotalRemains( int channelSessionId , long totalRemains ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to update total remains "
          + ", found zero channel session id" );
      return result;
    }

    result = dao.updateTotalRemains( channelSessionId , totalRemains );

    return result;
  }

  public boolean updateTotalRemainsAndDebitUnits( int channelSessionId ,
      long totalRemains , double debitUnits ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to update total remains and debit units "
          + ", found zero channel session id" );
      return result;
    }

    result = dao.updateTotalRemainsAndDebitUnits( channelSessionId ,
        totalRemains , debitUnits );

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionBean getChannelSession( int channelSessionId ) {
    ChannelSessionBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to get channel session "
          + ", found zero channelSessionId" );
      return bean;
    }
    bean = dao.getChannelSession( channelApp.isDebug() , channelSessionId );
    if ( bean == null ) {
      return bean;
    }
    // apply channel session schedules if found any
    List listChannelSessionSchedules = csssService.select( bean.getId() );
    if ( listChannelSessionSchedules == null ) {
      return bean;
    }
    bean.getListChannelSessionSchedules().clear();
    bean.getListChannelSessionSchedules().addAll( listChannelSessionSchedules );
    return bean;
  }

  public ChannelSessionBean getChannelSessionFromEventId( int eventId ) {
    return getLastChannelSession( eventId );
  }

  public ChannelSessionBean getLastChannelSession( int eventId ) {
    ChannelSessionBean bean = null;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get last channel session "
          + ", found zero eventId" );
      return bean;
    }
    bean = dao.getLastChannelSession( channelApp.isDebug() , eventId );
    if ( bean != null ) {
      List listChannelSessionSchedules = csssService.select( bean.getId() );
      if ( listChannelSessionSchedules != null ) {
        bean.getListChannelSessionSchedules().clear();
        bean.getListChannelSessionSchedules().addAll(
            listChannelSessionSchedules );
      }
    }
    return bean;
  }

  public List getAvailableChannelSessions( int limit ) {
    return dao.getAvailableChannelSessions( channelApp.isDebug() , limit );
  }

  public List getIdleScheduledChannelSessions( int limit ) {
    return dao.getIdleScheduledChannelSessions( channelApp.isDebug() , limit );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isSuspended( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to verify suspended status "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.isSuspended( channelApp.isDebug() , channelSessionId );
    return result;
  }

  public boolean isFinished( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to verify finished status "
          + ", found zero channel session id" );
      return result;
    }
    result = dao.isFinished( channelApp.isDebug() , channelSessionId );
    return result;
  }

}
