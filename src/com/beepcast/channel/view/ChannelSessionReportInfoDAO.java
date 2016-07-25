package com.beepcast.channel.view;

import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionReportInfoDAO {

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionReportInfoDAO" );

  private DatabaseLibrary dbLib;

  public ChannelSessionReportInfoDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public ChannelSessionReportInfoBean getChannelSessionReportInfo(
      int channelSessionId ) {
    ChannelSessionReportInfoBean bean = null;

    String sqlSelectFrom = ChannelSessionReportInfoQuery.sqlSelectFrom();
    String sqlWhere = "WHERE ( cs.id = " + channelSessionId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlLimit;

    // DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }
    QueryItem qi = null;
    Iterator it = qr.iterator();
    if ( it.hasNext() ) {
      qi = (QueryItem) it.next();
    }
    if ( qi == null ) {
      return bean;
    }

    bean = ChannelSessionReportInfoQuery.populateRecord( qi );

    return bean;
  }

}
