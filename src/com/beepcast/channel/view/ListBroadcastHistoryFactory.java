package com.beepcast.channel.view;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastHistoryFactory {

  static final DLogContext lctx = new SimpleContext(
      "ListBroadcastHistoryFactory" );

  public static ListBroadcastHistoryBean createListBroadcastHistoryBean(
      int clientId ) {
    ListBroadcastHistoryBean bean = null;
    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to create list broadcast history bean "
          + ", found zero client id" );
      return bean;
    }

    bean = new ListBroadcastHistoryBean();
    bean.setClientId( clientId );
    bean.setTotalRecords( 0 );
    bean.setChannelSessionInfoBeans( null );
    return bean;
  }

}
