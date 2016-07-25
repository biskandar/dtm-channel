package com.beepcast.channel.view;

import java.util.Date;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastSummaryFactory {

  static final DLogContext lctx = new SimpleContext(
      "ListBroadcastSummaryFactory" );

  public static ListBroadcastSummaryBean createListBroadcastSummaryBean(
      String strClientIds , String strEventIds , Date dateStarted ,
      Date dateFinished ) {
    ListBroadcastSummaryBean bean = new ListBroadcastSummaryBean();
    bean.setStrClientIds( strClientIds );
    bean.setStrEventIds( strEventIds );
    bean.setDateStarted( dateStarted );
    bean.setDateFinished( dateFinished );
    bean.setTotalRecords( 0 );
    bean.setChannelSessionReportInfoBeans( null );
    return bean;
  }

}
