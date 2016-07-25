package com.beepcast.channel;

public class ChannelMonitorSupport {

  public static String getKey( int channelSessionId ) {
    String key = null;
    if ( channelSessionId < 1 ) {
      return key;
    }
    key = Integer.toString( channelSessionId );
    return key;
  }

  public static String getKey( ChannelMonitorBean bean ) {
    String key = null;
    if ( bean == null ) {
      return key;
    }
    return getKey( bean.getChannelSessionId() );
  }

  public static String headerLog( int channelSessionId ) {
    StringBuffer sb = new StringBuffer();
    if ( channelSessionId > 0 ) {
      sb.append( "[ChannelMonitor-" );
      sb.append( channelSessionId );
      sb.append( "] " );
    }
    return sb.toString();
  }

  public static String headerLog( ChannelMonitorBean bean ) {
    String headerLog = "";
    if ( bean != null ) {
      headerLog = headerLog( bean.getChannelSessionId() );
    }
    return headerLog;
  }

  public static String readableShowTime( int sec ) {
    StringBuffer sb = new StringBuffer();
    if ( sec < 60 ) {
      sb.append( sec );
      sb.append( " sec(s)" );
      return sb.toString();
    }
    if ( sec < 3600 ) {
      int mm = sec / 60;
      int ss = sec % 60;
      if ( mm < 10 ) {
        sb.append( "0" );
      }
      sb.append( mm );
      sb.append( ":" );
      if ( ss < 10 ) {
        sb.append( "0" );
      }
      sb.append( ss );
      sb.append( " min(s)" );
      return sb.toString();
    }
    if ( sec < 86400 ) {
      int hh = sec / 3600;
      int mm = ( sec % 3600 ) / 60;
      if ( hh < 10 ) {
        sb.append( "0" );
      }
      sb.append( hh );
      sb.append( ":" );
      if ( mm < 10 ) {
        sb.append( "0" );
      }
      sb.append( mm );
      sb.append( " hour(s)" );
      return sb.toString();
    }
    int day = (int) Math.round( sec / 86400.0 );
    if ( day < 10 ) {
      sb.append( "0" );
    }
    sb.append( day );
    sb.append( " day(s)" );
    return sb.toString();
  }

}
