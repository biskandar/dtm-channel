package com.beepcast.channel.view;

import java.util.Date;

public class ChannelSessionReportInfoBean {

  private int channelSessionId;
  private String key;
  private int clientId;
  private String clientName;
  private int eventId;
  private String eventName;
  private int subscriberGroupId;
  private String subscriberGroupName;
  private long totalNumbers;
  private long totalRemains;
  private long totalSent;
  private long totalRetry;
  private long totalDnDelivered;
  private long totalDnInvalidNumber;
  private long totalDnEmpty;
  private long totalUnsubs;
  private Date dateReportCreated;
  private Date dateBroadcastStarted;
  private Date dateBroadcastFinished;

  private boolean fileReportCreated;
  private long totalVisitors;
  private long totalHits;

  public ChannelSessionReportInfoBean() {
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId( int clientId ) {
    this.clientId = clientId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName( String clientName ) {
    this.clientName = clientName;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName( String eventName ) {
    this.eventName = eventName;
  }

  public int getSubscriberGroupId() {
    return subscriberGroupId;
  }

  public void setSubscriberGroupId( int subscriberGroupId ) {
    this.subscriberGroupId = subscriberGroupId;
  }

  public String getSubscriberGroupName() {
    return subscriberGroupName;
  }

  public void setSubscriberGroupName( String subscriberGroupName ) {
    this.subscriberGroupName = subscriberGroupName;
  }

  public long getTotalNumbers() {
    return totalNumbers;
  }

  public void setTotalNumbers( long totalNumbers ) {
    this.totalNumbers = totalNumbers;
  }

  public long getTotalRemains() {
    return totalRemains;
  }

  public void setTotalRemains( long totalRemains ) {
    this.totalRemains = totalRemains;
  }

  public long getTotalSent() {
    return totalSent;
  }

  public void setTotalSent( long totalSent ) {
    this.totalSent = totalSent;
  }

  public long getTotalRetry() {
    return totalRetry;
  }

  public void setTotalRetry( long totalRetry ) {
    this.totalRetry = totalRetry;
  }

  public long getTotalDnDelivered() {
    return totalDnDelivered;
  }

  public void setTotalDnDelivered( long totalDnDelivered ) {
    this.totalDnDelivered = totalDnDelivered;
  }

  public long getTotalDnInvalidNumber() {
    return totalDnInvalidNumber;
  }

  public void setTotalDnInvalidNumber( long totalDnInvalidNumber ) {
    this.totalDnInvalidNumber = totalDnInvalidNumber;
  }

  public long getTotalDnEmpty() {
    return totalDnEmpty;
  }

  public void setTotalDnEmpty( long totalDnEmpty ) {
    this.totalDnEmpty = totalDnEmpty;
  }

  public long getTotalUnsubs() {
    return totalUnsubs;
  }

  public void setTotalUnsubs( long totalUnsubs ) {
    this.totalUnsubs = totalUnsubs;
  }

  public Date getDateReportCreated() {
    return dateReportCreated;
  }

  public void setDateReportCreated( Date dateReportCreated ) {
    this.dateReportCreated = dateReportCreated;
  }

  public Date getDateBroadcastStarted() {
    return dateBroadcastStarted;
  }

  public void setDateBroadcastStarted( Date dateBroadcastStarted ) {
    this.dateBroadcastStarted = dateBroadcastStarted;
  }

  public Date getDateBroadcastFinished() {
    return dateBroadcastFinished;
  }

  public void setDateBroadcastFinished( Date dateBroadcastFinished ) {
    this.dateBroadcastFinished = dateBroadcastFinished;
  }

  public boolean isFileReportCreated() {
    return fileReportCreated;
  }

  public void setFileReportCreated( boolean fileReportCreated ) {
    this.fileReportCreated = fileReportCreated;
  }

  public long getTotalVisitors() {
    return totalVisitors;
  }

  public void setTotalVisitors( long totalVisitors ) {
    this.totalVisitors = totalVisitors;
  }

  public long getTotalHits() {
    return totalHits;
  }

  public void setTotalHits( long totalHits ) {
    this.totalHits = totalHits;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ChannelSessionReportInfoBean ( " + "channelSessionId = "
        + this.channelSessionId + TAB + "clientId = " + this.clientId + TAB
        + "clientName = " + this.clientName + TAB + "eventId = " + this.eventId
        + TAB + "eventName = " + this.eventName + TAB + "subscriberGroupId = "
        + this.subscriberGroupId + TAB + "subscriberGroupName = "
        + this.subscriberGroupName + TAB + "totalNumbers = "
        + this.totalNumbers + TAB + "totalRemains = " + this.totalRemains + TAB
        + "totalSent = " + this.totalSent + TAB + "totalRetry = "
        + this.totalRetry + TAB + "totalDnDelivered = " + this.totalDnDelivered
        + TAB + "totalDnInvalidNumber = " + this.totalDnInvalidNumber + TAB
        + "totalDnEmpty = " + this.totalDnEmpty + TAB + "totalUnsubs = "
        + this.totalUnsubs + TAB + "totalVisitors = " + this.totalVisitors
        + TAB + "totalHits = " + this.totalHits + TAB + " )";
    return retValue;
  }

}
