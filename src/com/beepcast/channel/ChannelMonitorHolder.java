package com.beepcast.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelMonitorHolder {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelMonitorHolder" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private Object lockHolder;
  private Map mapHolder;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelMonitorHolder() {
    lockHolder = new Object();
    mapHolder = new HashMap();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Object getLockHolder() {
    return lockHolder;
  }

  public List generateListChannelSessionIds() {
    List listChannelSessionIds = null;
    synchronized ( lockHolder ) {
      listChannelSessionIds = new ArrayList( mapHolder.keySet() );
    }
    return listChannelSessionIds;
  }

  public ChannelMonitorBean queryChannelMonitorBean( int channelSessionId ) {
    ChannelMonitorBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to query channel monitor bean "
          + ", found zero id" );
      return bean;
    }
    synchronized ( lockHolder ) {
      String key = ChannelMonitorSupport.getKey( channelSessionId );
      Object object = mapHolder.get( key );
      if ( object instanceof ChannelMonitorBean ) {
        bean = (ChannelMonitorBean) object;
      }
    }
    return bean;
  }

  public boolean updateChannelMonitorBean( ChannelMonitorBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to update channel monitor bean "
          + ", found null bean" );
      return result;
    }
    synchronized ( lockHolder ) {
      String key = ChannelMonitorSupport.getKey( bean );
      mapHolder.put( key , bean );
      result = true;
    }
    return result;
  }

  public boolean deleteChannelMonitorBean( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to delete channel monitor bean "
          + ", found zero id" );
      return result;
    }
    synchronized ( lockHolder ) {
      String key = ChannelMonitorSupport.getKey( channelSessionId );
      mapHolder.remove( key );
      result = true;
    }
    return result;
  }

  public String progressToString( int channelSessionId ) {
    String result = "";
    if ( channelSessionId < 1 ) {
      return result;
    }
    synchronized ( lockHolder ) {
      ChannelMonitorBean bean = null;
      String key = ChannelMonitorSupport.getKey( channelSessionId );
      Object object = mapHolder.get( key );
      if ( ( object != null ) && ( object instanceof ChannelMonitorBean ) ) {
        bean = (ChannelMonitorBean) object;
      }
      if ( bean != null ) {
        result = bean.progressToString();
      }
    }
    return result;
  }

}
