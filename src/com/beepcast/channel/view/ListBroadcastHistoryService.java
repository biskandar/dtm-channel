package com.beepcast.channel.view;

import java.util.Iterator;
import java.util.List;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastHistoryService {

  static final DLogContext lctx = new SimpleContext(
      "ListBroadcastHistoryService" );

  private ListBroadcastHistoryDAO dao;

  public ListBroadcastHistoryService() {
    dao = new ListBroadcastHistoryDAO();
  }

  public ListBroadcastHistoryBean listBroadcastHistoryBean( int clientId ,
      int top , int limit ) {
    ListBroadcastHistoryBean bean = ListBroadcastHistoryFactory
        .createListBroadcastHistoryBean( clientId );
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to create list broadcast history bean" );
      return bean;
    }
    loadTotalRecords( bean , clientId , top , limit );
    if ( top < 0 ) {
      top = 0;
    }
    if ( limit > 100 ) {
      limit = 100;
    }
    loadBeans( bean , clientId , top , limit );
    return bean;
  }

  public Iterator iterBeans( ListBroadcastHistoryBean bean ) {
    Iterator iter = null;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to generate iterator "
          + ", found null list broadcast history bean" );
      return iter;
    }
    List channelSessionInfoBeans = bean.getChannelSessionInfoBeans();
    if ( channelSessionInfoBeans == null ) {
      DLog.warning( lctx , "Failed to generate iterator "
          + ", found null channel session info beans" );
      return iter;
    }
    iter = channelSessionInfoBeans.iterator();
    return iter;
  }

  private void loadTotalRecords( ListBroadcastHistoryBean bean , int clientId ,
      int top , int limit ) {
    bean.setTotalRecords( dao.totalBeans( bean.getClientId() , top , limit ) );
  }

  private void loadBeans( ListBroadcastHistoryBean bean , int clientId ,
      int top , int limit ) {
    bean.setChannelSessionInfoBeans( dao.selectBeans( bean.getClientId() , top ,
        limit ) );
  }

}
