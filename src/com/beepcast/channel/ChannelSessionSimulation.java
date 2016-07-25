package com.beepcast.channel;

import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSimulation {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSimulationService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support API Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static boolean startSimulation( int channelSessionId ,
      String simulationType , long simulationNumbers ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to start simulation "
          + ", found zero channel session id" );
      return result;
    }

    // header log
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // prepare the services
    ChannelSessionService csService = new ChannelSessionService();
    ChannelSessionSimulationService cssService = new ChannelSessionSimulationService();

    // verify the channel session profile
    ChannelSessionBean csBean = csService.getChannelSession( channelSessionId );
    if ( csBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to start simulation "
          + ", found invalid channel session id" );
      return result;
    }

    // select the last channel session simulation profile
    ChannelSessionSimulationBean cssBean = cssService
        .selectByChannelSessionId( channelSessionId );
    if ( cssBean != null ) {
      // validate is still running
      if ( ( cssBean.isStarted() ) && ( !cssBean.isFinished() ) ) {
        DLog.warning( lctx , headerLog + "Failed to start simulation "
            + ", found it was started already and haven't yet finished" );
        return result;
      }
      if ( !cssService.deleteById( cssBean.getId() ) ) {
        DLog.warning( lctx , headerLog + "Failed to start simulation "
            + ", found failed to dispose the last running broadcast" );
        return result;
      }
    }

    // insert the new channel session simulation profile
    if ( !cssService.insert( ChannelSessionSimulationBeanFactory
        .createChannelSessionSimulationBean( simulationType ,
            simulationNumbers , channelSessionId , simulationNumbers ,
            simulationNumbers , 0 , csBean.getPriority() , false , false ,
            false , ChannelSessionNewStatus.ST_EMPTY , null , null , null ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to start simulation "
          + ", found failed to insert the new channel session "
          + "simulation record" );
      return result;
    }

    // re select the channel session simulation record
    cssBean = cssService.selectByChannelSessionId( channelSessionId );
    if ( cssBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to start simulation "
          + ", found failed to find the channel session simulation record" );
    }

    // update status start
    result = cssService.updateStatusStartChannelSessionSimulation( cssBean
        .getId() );
    return result;
  }

  public static boolean stopSimulation( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to stop simulation "
          + ", found zero channel session id" );
      return result;
    }

    // header log
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // prepare the services
    ChannelSessionService csService = new ChannelSessionService();
    ChannelSessionSimulationService cssService = new ChannelSessionSimulationService();

    // verify the channel session profile
    ChannelSessionBean csBean = csService.getChannelSession( channelSessionId );
    if ( csBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to stop simulation "
          + ", found invalid channel session id" );
      return result;
    }

    // verify the channel session simulation profile
    ChannelSessionSimulationBean cssBean = cssService
        .selectByChannelSessionId( channelSessionId );
    if ( cssBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to stop simulation "
          + ", found missing channel session simulation record" );
      return result;
    }

    // validate is the simulation is started
    if ( !cssBean.isStarted() ) {
      DLog.warning( lctx , headerLog + "Failed to stop simulation "
          + ", found simulation is not started yet" );
      return result;
    }

    // update status stop
    result = cssService.updateStatusFinishChannelSessionSimulation( cssBean
        .getId() );
    return result;
  }

}
