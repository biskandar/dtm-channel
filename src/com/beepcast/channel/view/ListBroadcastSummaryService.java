package com.beepcast.channel.view;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastSummaryService {

  static final DLogContext lctx = new SimpleContext(
      "ListBroadcastSummaryService" );

  private ListBroadcastSummaryDAO dao;

  public ListBroadcastSummaryService() {
    dao = new ListBroadcastSummaryDAO();
  }

  public ListBroadcastSummaryBean listBroadcastSummaryBean( int clientId ,
      String strEventIds , Date dateStarted , Date dateFinished , int top ,
      int limit ) {
    return listBroadcastSummaryBean( Integer.toString( clientId ) ,
        strEventIds , dateStarted , dateFinished , top , limit );
  }

  public ListBroadcastSummaryBean listBroadcastSummaryBean(
      String strClientIds , String strEventIds , Date dateStarted ,
      Date dateFinished , int top , int limit ) {
    ListBroadcastSummaryBean bean = ListBroadcastSummaryFactory
        .createListBroadcastSummaryBean( strClientIds , strEventIds ,
            dateStarted , dateFinished );
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to create list broadcast summary bean" );
      return bean;
    }
    totalRecords( bean , top , limit );
    if ( top < 0 ) {
      top = 0;
    }
    if ( limit > 100 ) {
      limit = 100;
    }
    selectRecords( bean , top , limit );
    return bean;
  }

  public Iterator iterBeans( ListBroadcastSummaryBean bean ) {
    Iterator iter = null;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to generate iterator "
          + ", found null list broadcast summary bean" );
      return iter;
    }
    List channelSessionReportInfoBeans = bean
        .getChannelSessionReportInfoBeans();
    if ( channelSessionReportInfoBeans == null ) {
      DLog.warning( lctx , "Failed to generate iterator "
          + ", found null channel session report info beans" );
      return iter;
    }
    iter = channelSessionReportInfoBeans.iterator();
    return iter;
  }

  private void totalRecords( ListBroadcastSummaryBean bean , int top , int limit ) {
    bean.setTotalRecords( dao.totalBeans( bean.getStrClientIds() ,
        bean.getStrEventIds() , bean.getDateStarted() , bean.getDateFinished() ,
        top , limit ) );
  }

  private void selectRecords( ListBroadcastSummaryBean bean , int top ,
      int limit ) {
    bean.setChannelSessionReportInfoBeans( dao.selectBeans(
        bean.getStrClientIds() , bean.getStrEventIds() , bean.getDateStarted() ,
        bean.getDateFinished() , top , limit ) );
  }

}
