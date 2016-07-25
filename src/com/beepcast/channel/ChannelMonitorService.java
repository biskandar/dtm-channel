package com.beepcast.channel;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelMonitorService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelMonitorService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelMonitorHolder holder;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelMonitorService( ChannelMonitorHolder holder ) {
    this.holder = holder;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean executeMonitor( int channelSessionId , int eventId ,
      String eventName , int listId , String listName , long totalAllNumber ,
      int initLoad , long totalProcessedNumber , long dateProcessedNumber ,
      boolean debug ) {
    boolean result = false;

    if ( holder == null ) {
      DLog.warning( lctx , "Failed to execute monitor , found null holder" );
      return result;
    }

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to execute monitor "
          + ", found zero channel session id" );
      return result;
    }

    String headerLog = ChannelMonitorSupport.headerLog( channelSessionId );

    if ( ( totalAllNumber < 0 ) || ( totalProcessedNumber < 0 ) ) {
      DLog.warning( lctx , headerLog + "Failed to execute monitor "
          + ", found negatif total all number and/or processed number" );
      return result;
    }

    // force to get current date time if found date processed empty
    if ( dateProcessedNumber < 1 ) {
      dateProcessedNumber = System.currentTimeMillis();
      if ( debug ) {
        DLog.debug( lctx , headerLog + "Found empty date processed "
            + ", force to set with current date time" );
      }
    }

    ChannelMonitorBean bean = holder.queryChannelMonitorBean( channelSessionId );
    if ( bean == null ) {
      if ( debug ) {
        DLog.debug( lctx , headerLog + "Failed to find bean in the map "
            + ", trying to create the new one" );
      }
      holder.updateChannelMonitorBean( ChannelMonitorFactory
          .createChannelMonitorBean( channelSessionId , eventId , eventName ,
              listId , listName , totalAllNumber , initLoad , true ) );
      bean = holder.queryChannelMonitorBean( channelSessionId );
      if ( bean == null ) {
        DLog.warning( lctx , headerLog + "Failed to execute monitor "
            + ", found null channel monitor bean" );
        return result;
      }
    } else {
      if ( totalAllNumber != bean.getTotalAllNumber() ) {
        bean.reset();
        DLog.warning( lctx , headerLog
            + "Found total all number is different from previous "
            + ", do reset the channel monitor bean" );
      }
    }

    boolean updated = bean.updateNumber( totalProcessedNumber ,
        dateProcessedNumber , debug );
    if ( !updated ) {
      DLog.warning( lctx , headerLog + "Failed to update the latest "
          + "total and date processed number" );
      return result;
    }

    // calculated the percentage of processed number
    bean.calculatePercentageProcessed();

    // calculated the second of remains
    bean.calculateSecondRemains();

    // debug
    if ( debug ) {
      DLog.debug(
          lctx ,
          headerLog + "Sucessfully executed monitor , processed = "
              + bean.getPercentageProcessedStr() + " , remains = "
              + bean.getSecondRemainsStr() );
    }

    result = true;
    return result;
  }

  public boolean clean( int channelSessionId , boolean debug ) {
    boolean result = false;

    if ( holder == null ) {
      DLog.warning( lctx , "Failed to perform clean , found null holder" );
      return result;
    }

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to perform clean "
          + ", found zero channel session id" );
      return result;
    }

    String headerLog = ChannelMonitorSupport.headerLog( channelSessionId );

    result = holder.deleteChannelMonitorBean( channelSessionId );
    if ( result ) {
      DLog.debug( lctx , headerLog + "Successfully perform cleaned "
          + "from the map" );
    } else {
      DLog.warning( lctx , headerLog + "Failed to perform clean "
          + "from the map" );
    }

    return result;
  }

  public String progressToString( int channelSessionId ) {
    String result = "";
    if ( holder == null ) {
      return result;
    }
    if ( channelSessionId < 1 ) {
      return result;
    }
    result = holder.progressToString( channelSessionId );
    return result;
  }

  public String info() {
    StringBuffer sb = new StringBuffer();

    if ( holder == null ) {
      return sb.toString();
    }

    List listChannelSessionIds = holder.generateListChannelSessionIds();
    if ( listChannelSessionIds == null ) {
      return sb.toString();
    }

    String key;
    int channelSessionId;
    ChannelMonitorBean bean;
    Iterator iterChannelSessionIds = listChannelSessionIds.iterator();
    while ( iterChannelSessionIds.hasNext() ) {
      key = (String) iterChannelSessionIds.next();
      if ( StringUtils.isBlank( key ) ) {
        continue;
      }
      try {
        channelSessionId = Integer.parseInt( key );
      } catch ( NumberFormatException e ) {
        channelSessionId = 0;
      }
      if ( channelSessionId < 1 ) {
        continue;
      }
      bean = holder.queryChannelMonitorBean( channelSessionId );
      if ( bean == null ) {
        continue;
      }
      synchronized ( holder.getLockHolder() ) {
        sb.append( "[CS-" );
        sb.append( bean.getChannelSessionId() );
        sb.append( ": " );
        sb.append( bean.getPercentageProcessedStr() );
        sb.append( " - " );
        sb.append( bean.getSecondRemainsStr() );
        sb.append( "] " );
      }
    }

    return sb.toString();
  }

}
