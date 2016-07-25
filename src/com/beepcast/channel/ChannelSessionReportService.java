package com.beepcast.channel;

import java.util.Date;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionReportService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionReportService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelSessionReportDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionReportService() {
    dao = new ChannelSessionReportDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionReportBean csrBean ) {
    boolean result = false;
    if ( csrBean == null ) {
      DLog.warning( lctx , "Failed to insert channel session report "
          + ", null bean" );
      return result;
    }
    result = dao.insert( csrBean );
    return result;
  }

  public ChannelSessionReportBean selectByChannelSessionId( int channelSessionId ) {
    ChannelSessionReportBean csrBean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select by "
          + "channel session id , found zero" );
      return csrBean;
    }
    csrBean = dao.selectByChannelSessionId( channelSessionId );
    return csrBean;
  }

  public boolean update( ChannelSessionReportBean csrBean ) {
    boolean result = false;
    if ( csrBean == null ) {
      DLog.warning( lctx , "Failed to update channel session report "
          + ", null bean" );
      return result;
    }
    if ( csrBean.getId() < 1 ) {
      DLog.warning( lctx , "Failed to update channel session report "
          + ", found zero id" );
      return result;
    }
    result = dao.update( csrBean );
    return result;
  }

  public boolean updateDateInserted( int channelSessionId , Date dateInserted ) {
    return dao.updateDateInserted( channelSessionId , dateInserted );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
