package com.beepcast.channel;

import java.util.Date;

import com.beepcast.model.util.DateTimeFormat;

public class ChannelSessionScheduleBean {

  private int id;
  private int channelSessionId;
  private Date dateStarted;
  private Date dateStopped;
  private String marked;
  private boolean active;

  public ChannelSessionScheduleBean() {
  }

  public int getId() {
    return id;
  }

  public void setId( int id ) {
    this.id = id;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public Date getDateStarted() {
    return dateStarted;
  }

  public void setDateStarted( Date dateStarted ) {
    this.dateStarted = dateStarted;
  }

  public Date getDateStopped() {
    return dateStopped;
  }

  public void setDateStopped( Date dateStopped ) {
    this.dateStopped = dateStopped;
  }

  public String getMarked() {
    return marked;
  }

  public void setMarked( String marked ) {
    this.marked = marked;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive( boolean active ) {
    this.active = active;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ChannelSessionScheduleBean ( " + "id = " + this.id + TAB
        + "channelSessionId = " + this.channelSessionId + TAB
        + "dateStarted = " + DateTimeFormat.convertToString( this.dateStarted )
        + TAB + "dateStopped = "
        + DateTimeFormat.convertToString( this.dateStopped ) + TAB
        + "marked = " + this.marked + TAB + "active = " + this.active + TAB
        + " )";
    return retValue;
  }

}
