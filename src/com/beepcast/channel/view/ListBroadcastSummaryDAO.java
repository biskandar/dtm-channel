package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastSummaryDAO {

  static final DLogContext lctx = new SimpleContext( "ListBroadcastSummaryDAO" );

  private DatabaseLibrary dbLib;

  public ListBroadcastSummaryDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public long totalBeans( String strClientIds , String strEventIds ,
      Date dateStarted , Date dateFinished , int top , int limit ) {
    long totalRecords = 0;

    // compose sql
    String sqlInnerSelectFrom = ChannelSessionReportInfoQuery.sqlSelectFrom();
    String sqlInnerWhere = ListBroadcastSummaryQuery.sqlWhere( strClientIds ,
        strEventIds , dateStarted , dateFinished );
    String sqlInnerGroupBy = "GROUP BY cs.id ";
    String sqlInner = sqlInnerSelectFrom + sqlInnerWhere + sqlInnerGroupBy;
    String sqlSelect = "SELECT COUNT(t.id) AS total ";
    String sqlFrom = "FROM ( " + sqlInner + " ) t ";
    String sql = sqlSelect + sqlFrom;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return totalRecords;
    }

    // fetch record
    QueryItem qi = null;
    Iterator it = qr.iterator();
    if ( it.hasNext() ) {
      qi = (QueryItem) it.next();
    }
    if ( qi == null ) {
      return totalRecords;
    }
    try {
      totalRecords = Long.parseLong( qi.getFirstValue() );
    } catch ( NumberFormatException e ) {
    }

    return totalRecords;
  }

  public List selectBeans( String strClientIds , String strEventIds ,
      Date dateStarted , Date dateFinished , int top , int limit ) {
    List listBeans = new ArrayList();

    // compose sql
    String sqlSelectFrom = ChannelSessionReportInfoQuery.sqlSelectFrom();
    String sqlWhere = ListBroadcastSummaryQuery.sqlWhere( strClientIds ,
        strEventIds , dateStarted , dateFinished );
    String sqlGroupBy = "GROUP BY cs.id ";
    String sqlOrderBy = "ORDER BY cs.id DESC ";
    String sqlLimit = "LIMIT " + top + " , " + limit + " ";
    String sql = sqlSelectFrom + sqlWhere + sqlGroupBy + sqlOrderBy + sqlLimit;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listBeans;
    }

    // fetch records
    ChannelSessionReportInfoBean bean = null;
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      bean = ChannelSessionReportInfoQuery.populateRecord( qi );
      if ( bean == null ) {
        continue;
      }
      listBeans.add( bean );
    }

    return listBeans;
  }

}
