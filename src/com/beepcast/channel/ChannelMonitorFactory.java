package com.beepcast.channel;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelMonitorFactory {

  static final DLogContext lctx = new SimpleContext( "ChannelMonitorFactory" );

  public static ChannelMonitorBean createChannelMonitorBean(
      int channelSessionId , int eventId , String eventName , int listId ,
      String listName , long totalAllNumber , int initLoad , boolean debug ) {
    ChannelMonitorBean bean = null;

    // validate must be params

    if ( channelSessionId < 1 ) {
      return bean;
    }

    // validate clean params

    initLoad = ( initLoad < 1 ) ? 0 : initLoad;

    // create bean

    bean = new ChannelMonitorBean();
    bean.setChannelSessionId( channelSessionId );
    bean.setEventId( eventId );
    bean.setEventName( eventName );
    bean.setListId( listId );
    bean.setListName( listName );
    bean.setTotalAllNumber( totalAllNumber );
    bean.setInitLoad( initLoad );

    // reset bean

    bean.reset();

    // log it

    if ( debug ) {
      DLog.debug( lctx , ChannelMonitorSupport.headerLog( bean )
          + "Successfully created a new channel monitor with total numbers = "
          + bean.getTotalAllNumber() );
    }

    return bean;
  }

}
