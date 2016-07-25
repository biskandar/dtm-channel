package com.beepcast.channel.view;

import java.util.Date;
import java.util.List;

public class ListBroadcastSummaryBean {

  private String strClientIds;
  private String strEventIds;
  private Date dateStarted;
  private Date dateFinished;
  private long totalRecords;
  private List channelSessionReportInfoBeans;

  public ListBroadcastSummaryBean() {

  }

  public String getStrClientIds() {
    return strClientIds;
  }

  public void setStrClientIds( String strClientIds ) {
    this.strClientIds = strClientIds;
  }

  public String getStrEventIds() {
    return strEventIds;
  }

  public void setStrEventIds( String strEventIds ) {
    this.strEventIds = strEventIds;
  }

  public Date getDateStarted() {
    return dateStarted;
  }

  public void setDateStarted( Date dateStarted ) {
    this.dateStarted = dateStarted;
  }

  public Date getDateFinished() {
    return dateFinished;
  }

  public void setDateFinished( Date dateFinished ) {
    this.dateFinished = dateFinished;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords( long totalRecords ) {
    this.totalRecords = totalRecords;
  }

  public List getChannelSessionReportInfoBeans() {
    return channelSessionReportInfoBeans;
  }

  public void setChannelSessionReportInfoBeans(
      List channelSessionReportInfoBeans ) {
    this.channelSessionReportInfoBeans = channelSessionReportInfoBeans;
  }

}
