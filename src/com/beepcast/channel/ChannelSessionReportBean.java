package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionReportBean {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int id;
  private String key;
  private int channelSessionId;
  private long totalSent;
  private long totalRetry;
  private long totalDnDelivered;
  private long totalDnFailed;
  private long totalDnInvalidNumber;
  private long totalDnEmpty;
  private long totalUnsubs;
  private long totalGlobalUnsubs;
  private boolean active;
  private Date dateInserted;
  private Date dateUpdated;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionReportBean() {
    active = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int getId() {
    return id;
  }

  public void setId( int id ) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
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

  public long getTotalDnFailed() {
    return totalDnFailed;
  }

  public void setTotalDnFailed( long totalDnFailed ) {
    this.totalDnFailed = totalDnFailed;
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

  public long getTotalGlobalUnsubs() {
    return totalGlobalUnsubs;
  }

  public void setTotalGlobalUnsubs( long totalGlobalUnsubs ) {
    this.totalGlobalUnsubs = totalGlobalUnsubs;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive( boolean active ) {
    this.active = active;
  }

  public Date getDateInserted() {
    return dateInserted;
  }

  public void setDateInserted( Date dateInserted ) {
    this.dateInserted = dateInserted;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated( Date dateUpdated ) {
    this.dateUpdated = dateUpdated;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Helper
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ChannelSessionReportBean ( " + "id = " + this.id + TAB
        + "key = " + this.key + TAB + "channelSessionId = "
        + this.channelSessionId + TAB + "totalSent = " + this.totalSent + TAB
        + "totalRetry = " + this.totalRetry + TAB + "totalDnDelivered = "
        + this.totalDnDelivered + TAB + "totalDnFailed = " + this.totalDnFailed
        + TAB + "totalDnInvalidNumber = " + this.totalDnInvalidNumber + TAB
        + "totalDnEmpty = " + this.totalDnEmpty + TAB + "totalUnsubs = "
        + this.totalUnsubs + TAB + "totalGlobalUnsubs = "
        + this.totalGlobalUnsubs + TAB + "active = " + this.active + TAB + " )";
    return retValue;
  }

}
