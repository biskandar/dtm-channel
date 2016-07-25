package com.beepcast.channel;

import java.util.Date;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionScheduleService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionScheduleService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelSessionScheduleDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionScheduleService() {
    dao = new ChannelSessionScheduleDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionScheduleBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert , found null bean" );
      return result;
    }
    result = dao.insert( bean );
    return result;
  }

  public boolean updateInactive( int id ) {
    boolean result = false;
    if ( id < 1 ) {
      DLog.warning( lctx , "Failed to delete , found zero id" );
      return result;
    }
    result = dao.updateInactive( id );
    return result;
  }

  public ChannelSessionScheduleBean selectNext( int channelSessionId ,
      Date dateAfter ) {
    ChannelSessionScheduleBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select latest "
          + ", found zero channel session id" );
      return bean;
    }
    bean = dao.selectNext( channelSessionId , dateAfter );
    return bean;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ...

}
