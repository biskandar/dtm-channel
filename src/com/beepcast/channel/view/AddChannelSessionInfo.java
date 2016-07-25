package com.beepcast.channel.view;

import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class AddChannelSessionInfo {

  static final DLogContext lctx = new SimpleContext( "AddChannelSessionInfo" );

  private AddChannelSessionInfoDAO dao;

  public AddChannelSessionInfo() {
    dao = new AddChannelSessionInfoDAO();
  }

  public List getAvailableChannel( int clientId ) {
    List list = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get available channel "
          + ", found zero clientId" );
      return list;
    }
    list = dao.getAvailableChannel( clientId );
    return list;
  }

  public List getAvailableGroupSubscriber( int clientId ) {
    List list = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to get available group subscriber "
          + ", found zero clientId" );
      return list;
    }
    list = dao.getAvailableGroupSubscriber( clientId );
    return list;
  }

}
