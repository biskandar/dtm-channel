package com.beepcast.channel.common;

import com.beepcast.channel.ChannelSessionBean;
import com.beepcast.channel.view.ChannelSessionInfoBean;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionLogSupport {

  static final DLogContext lctx = new SimpleContext( "ChannelSessionLogSupport" );

  public static String headerLog( ChannelSessionBean csBean ) {
    String headerLog = "";
    if ( csBean != null ) {
      headerLog = headerLog( csBean.getId() );
    }
    return headerLog;
  }

  public static String headerLog( ChannelSessionInfoBean csiBean ) {
    String headerLog = "";
    if ( csiBean != null ) {
      headerLog = headerLog( csiBean.getChannelSessionId() );
    }
    return headerLog;
  }

  public static String headerLog( int channelSessionId ) {
    String headerLog = "";
    if ( channelSessionId > 0 ) {
      headerLog = "[ChannelSession-" + channelSessionId + "] ";
    }
    return headerLog;
  }

}
