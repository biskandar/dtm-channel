package com.beepcast.channel;

import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSchedulesService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSchedulesService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelSessionSchedulesDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionSchedulesService() {
    dao = new ChannelSessionSchedulesDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List select( int channelSessionId ) {
    List list = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select , found zero channel session id" );
      return list;
    }
    list = dao.select( channelSessionId );
    return list;
  }

  public int insert( List listBeans ) {
    int totalRecords = 0;
    if ( listBeans == null ) {
      DLog.warning( lctx , "Failed to insert , found null list beans" );
      return totalRecords;
    }
    totalRecords = dao.insert( listBeans , 0 );
    return totalRecords;
  }

  public int insert( List listBeans , int channelSessionId ) {
    int totalRecords = 0;
    if ( listBeans == null ) {
      DLog.warning( lctx , "Failed to insert , found null list beans" );
      return totalRecords;
    }
    totalRecords = dao.insert( listBeans , channelSessionId );
    return totalRecords;
  }

  public int delete( int channelSessionId ) {
    int totalRecords = 0;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to delete , found zero channel session id" );
      return totalRecords;
    }
    totalRecords = dao.delete( channelSessionId );
    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
