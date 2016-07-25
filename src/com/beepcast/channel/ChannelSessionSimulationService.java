package com.beepcast.channel;

import java.util.Date;
import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSimulationService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSimulationService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelApp channelApp;
  private ChannelSessionSimulationDAO dao;
  private ChannelSessionService csService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionSimulationService() {
    channelApp = ChannelApp.getInstance();
    dao = new ChannelSessionSimulationDAO();
    csService = new ChannelSessionService();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List getAvailableChannelSessionSimulations( int limit ) {
    return dao.getAvailableChannelSessionSimulations( channelApp.isDebug() ,
        limit );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateStatusStartChannelSessionSimulation(
      int channelSessionSimulationId ) {
    return dao.updateNewStatus( channelSessionSimulationId ,
        ChannelSessionNewStatus.ST_STARTED );
  }

  public boolean updateStatusFinishChannelSessionSimulation(
      int channelSessionSimulationId ) {
    return dao.updateNewStatus( channelSessionSimulationId ,
        ChannelSessionNewStatus.ST_FINISHED );
  }

  public boolean updateStatusEmptyChannelSessionSimulation(
      int channelSessionSimulationId ) {
    return dao.updateNewStatus( channelSessionSimulationId ,
        ChannelSessionNewStatus.ST_EMPTY );
  }

  public boolean updateStartedChannelSessionSimulation(
      int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to update started channel simulation "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao
        .updateStartedChannelSessionSimulation( channelSessionSimulationId );
    return result;
  }

  public boolean updateStartedChannelSessionSimulation(
      int channelSessionSimulationId , long totalSubscribers ,
      double debitUnits , Date dateStarted ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to update started channel simulation "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao.updateStartedChannelSessionSimulation(
        channelSessionSimulationId , totalSubscribers , debitUnits ,
        dateStarted );
    return result;
  }

  public boolean updateFinishedChannelSessionSimulation(
      ChannelSessionSimulationBean cssBean ) {
    boolean result = false;
    if ( cssBean == null ) {
      DLog.warning( lctx ,
          "Failed to update finished channel session simulation "
              + ", found null channel session simulation bean" );
      return result;
    }
    result = dao.updateFinishedChannelSessionSimulation( cssBean );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateTotalRemains( int channelSessionSimulationId ,
      long totalRemains ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to update total remains "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao.updateTotalRemains( channelSessionSimulationId , totalRemains );
    return result;
  }

  public boolean updateTotalRemainsAndDebitUnits(
      int channelSessionSimulationId , long totalRemains , double debitUnits ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to update total remains and debit units "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao.updateTotalRemainsAndDebitUnits( channelSessionSimulationId ,
        totalRemains , debitUnits );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Select Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isSuspended( int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to verify suspended status "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao
        .isSuspended( channelApp.isDebug() , channelSessionSimulationId );
    return result;
  }

  public boolean isFinished( int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Failed to verify finished status "
          + ", found zero channel session simulation id" );
      return result;
    }
    result = dao.isFinished( channelApp.isDebug() , channelSessionSimulationId );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Backend Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionSimulationBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert , found null bean" );
      return result;
    }
    if ( bean.getChannelSessionId() < 1 ) {
      DLog.warning( lctx , "Failed to insert , found zero channel session id" );
      return result;
    }
    result = dao.insert( bean );
    return result;
  }

  public ChannelSessionSimulationBean selectByChannelSessionId(
      int channelSessionId ) {
    ChannelSessionSimulationBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select by channel session id "
          + ", found zero channel session id" );
      return bean;
    }
    bean = dao.selectByChannelSessionId( channelApp.isDebug() ,
        channelSessionId );
    return bean;
  }

  public boolean deleteById( int id ) {
    boolean result = false;
    if ( id < 1 ) {
      DLog.warning( lctx , "Failed to delete by id , found zero id" );
      return result;
    }
    result = dao.deleteById( id );
    return result;
  }

}
