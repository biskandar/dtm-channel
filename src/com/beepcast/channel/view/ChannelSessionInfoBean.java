package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.beepcast.model.util.DateTimeFormat;

public class ChannelSessionInfoBean {

  private int channelSessionId;
  private int clientId;
  private String clientName;
  private int paymentType;
  private int eventId;
  private String eventName;
  private int groupSubscriberId;
  private String groupSubscriberName;
  private boolean allowDuplicated;
  private boolean useDateSend;
  private double expectResponse;
  private long totalNumbers;
  private long totalRemains;
  private double debitUnits;
  private int priority;
  private boolean suspended;
  private boolean started;
  private boolean finished;
  private int newStatus;
  private Date dateStarted;
  private Date dateFinished;
  private Date dateSchedulledStart;
  private Date dateSchedulledFinish;

  private List listChannelSessionSchedules;

  public ChannelSessionInfoBean() {
    listChannelSessionSchedules = new ArrayList();
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
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

  public int getPaymentType() {
    return paymentType;
  }

  public void setPaymentType( int paymentType ) {
    this.paymentType = paymentType;
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

  public int getGroupSubscriberId() {
    return groupSubscriberId;
  }

  public void setGroupSubscriberId( int groupSubscriberId ) {
    this.groupSubscriberId = groupSubscriberId;
  }

  public String getGroupSubscriberName() {
    return groupSubscriberName;
  }

  public void setGroupSubscriberName( String groupSubscriberName ) {
    this.groupSubscriberName = groupSubscriberName;
  }

  public boolean isAllowDuplicated() {
    return allowDuplicated;
  }

  public void setAllowDuplicated( boolean allowDuplicated ) {
    this.allowDuplicated = allowDuplicated;
  }

  public boolean isUseDateSend() {
    return useDateSend;
  }

  public void setUseDateSend( boolean useDateSend ) {
    this.useDateSend = useDateSend;
  }

  public double getExpectResponse() {
    return expectResponse;
  }

  public void setExpectResponse( double expectResponse ) {
    this.expectResponse = expectResponse;
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

  public double getDebitUnits() {
    return debitUnits;
  }

  public void setDebitUnits( double debitUnits ) {
    this.debitUnits = debitUnits;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public void setSuspended( boolean suspended ) {
    this.suspended = suspended;
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted( boolean started ) {
    this.started = started;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished( boolean finished ) {
    this.finished = finished;
  }

  public int getNewStatus() {
    return newStatus;
  }

  public void setNewStatus( int newStatus ) {
    this.newStatus = newStatus;
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

  public Date getDateSchedulledStart() {
    return dateSchedulledStart;
  }

  public void setDateSchedulledStart( Date dateSchedulledStart ) {
    this.dateSchedulledStart = dateSchedulledStart;
  }

  public Date getDateSchedulledFinish() {
    return dateSchedulledFinish;
  }

  public void setDateSchedulledFinish( Date dateSchedulledFinish ) {
    this.dateSchedulledFinish = dateSchedulledFinish;
  }

  public List getListChannelSessionSchedules() {
    return listChannelSessionSchedules;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ChannelSessionInfoBean ( " + "channelSessionId = "
        + this.channelSessionId + TAB + "clientId = " + this.clientId + TAB
        + "clientName = " + this.clientName + TAB + "paymentType = "
        + this.paymentType + TAB + "eventId = " + this.eventId + TAB
        + "eventName = " + this.eventName + TAB + "groupSubscriberId = "
        + this.groupSubscriberId + TAB + "groupSubscriberName = "
        + this.groupSubscriberName + TAB + "allowDuplicated = "
        + this.allowDuplicated + TAB + "useDateSend = " + this.useDateSend
        + TAB + "expectResponse = " + this.expectResponse + TAB
        + "totalNumbers = " + this.totalNumbers + TAB + "totalRemains = "
        + this.totalRemains + TAB + "debitUnits = " + this.debitUnits + TAB
        + "priority = " + this.priority + TAB + "suspended = " + this.suspended
        + TAB + "started = " + this.started + TAB + "finished = "
        + this.finished + TAB + "newStatus = " + this.newStatus + TAB
        + "dateStarted = " + DateTimeFormat.convertToString( this.dateStarted )
        + TAB + "dateFinished = "
        + DateTimeFormat.convertToString( this.dateFinished ) + TAB
        + "dateSchedulledStart = "
        + DateTimeFormat.convertToString( this.dateSchedulledStart ) + TAB
        + "dateSchedulledFinish = "
        + DateTimeFormat.convertToString( this.dateSchedulledFinish ) + TAB
        + "listChannelSessionSchedules = " + this.listChannelSessionSchedules
        + TAB + " )";
    return retValue;
  }

}
