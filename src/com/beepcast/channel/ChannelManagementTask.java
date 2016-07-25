package com.beepcast.channel;

import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementTask {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelManagementTask" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp oprops;

  private ChannelApp app;
  private ChannelConf conf;

  private int cleanChannelLogExpiryDays;
  private int cleanChannelLogLimitRecords;

  private ChannelManagementTaskDAO dao;

  private boolean initialized;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementTask( ChannelApp app ) {
    initialized = false;

    oprops = OnlinePropertiesApp.getInstance();

    if ( app == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel app" );
      return;
    }
    this.app = app;

    if ( app.getChannelConf() == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel conf" );
      return;
    }
    this.conf = app.getChannelConf();

    cleanChannelLogExpiryDays = conf.getManagementCleanChannelLog_expiryDays();
    cleanChannelLogLimitRecords = conf
        .getManagementCleanChannelLog_limitRecords();

    int expiryDays = (int) oprops.getLong(
        "Channel.CleanTempReport.ExpiryDays" , cleanChannelLogExpiryDays );
    int limitRecords = (int) oprops.getLong(
        "Channel.CleanTempReport.LimitRecords" , cleanChannelLogLimitRecords );

    DLog.debug( lctx , "Defined param clean channel log duration expiry = "
        + expiryDays + " day(s)" );
    DLog.debug( lctx , "Defined param clean channel log maximum limit = "
        + limitRecords + " records(s)" );

    dao = new ChannelManagementTaskDAO();

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean doCleanChannelLog() {
    boolean result = false;

    int totalRecords = 0;

    int expiryDays = (int) oprops.getLong(
        "Channel.CleanTempReport.ExpiryDays" , cleanChannelLogExpiryDays );
    int limitRecords = (int) oprops.getLong(
        "Channel.CleanTempReport.LimitRecords" , cleanChannelLogLimitRecords );

    totalRecords = dao.deleteExpiredRecords( false , expiryDays , limitRecords ,
        conf.isDebug() );
    if ( totalRecords > 0 ) {
      DLog.debug( lctx , "Cleaned expired records in the channel log "
          + "table , found total = " + totalRecords + " record(s) are deleted" );
    }

    try {
      Thread.sleep( 1000 );
    } catch ( InterruptedException e ) {
    }

    totalRecords = dao.deleteExpiredRecords( true , expiryDays , limitRecords ,
        conf.isDebug() );
    if ( totalRecords > 0 ) {
      DLog.debug( lctx , "Cleaned expired records in the channel log "
          + "simulation table , found total = " + totalRecords
          + " record(s) ared deleted" );
    }

    result = true;
    return result;
  }

}
