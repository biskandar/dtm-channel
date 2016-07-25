package com.beepcast.channel.view;

import java.util.List;

public class ListBroadcastHistoryBean {

  private int clientId;
  private long totalRecords;
  private List channelSessionInfoBeans;

  public ListBroadcastHistoryBean() {

  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId( int clientId ) {
    this.clientId = clientId;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords( long totalRecords ) {
    this.totalRecords = totalRecords;
  }

  public List getChannelSessionInfoBeans() {
    return channelSessionInfoBeans;
  }

  public void setChannelSessionInfoBeans( List channelSessionInfoBeans ) {
    this.channelSessionInfoBeans = channelSessionInfoBeans;
  }

}
