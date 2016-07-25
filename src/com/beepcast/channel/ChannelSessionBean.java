package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TEvent;

public class ChannelSessionBean {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int id;
  private int eventId;
  private int groupSubscriberId;
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
  private Date dateSuspended;
  private Date dateFinished;
  private Date dateSchedulledStart;
  private Date dateSchedulledFinish;
  private boolean active;

  private TEvent eventOut;
  private TClient clientOut;
  private TClientLevel clientLevelOut;
  private List listChannelSessionSchedules;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionBean() {
    newStatus = ChannelSessionNewStatus.ST_EMPTY;
    active = true;
    listChannelSessionSchedules = new ArrayList();
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

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public int getGroupSubscriberId() {
    return groupSubscriberId;
  }

  public void setGroupSubscriberId( int groupSubscriberId ) {
    this.groupSubscriberId = groupSubscriberId;
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

  public Date getDateSuspended() {
    return dateSuspended;
  }

  public void setDateSuspended( Date dateSuspended ) {
    this.dateSuspended = dateSuspended;
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

  public boolean isActive() {
    return active;
  }

  public void setActive( boolean active ) {
    this.active = active;
  }

  public TEvent getEventOut() {
    return eventOut;
  }

  public void setEventOut( TEvent eventOut ) {
    this.eventOut = eventOut;
  }

  public TClient getClientOut() {
    return clientOut;
  }

  public void setClientOut( TClient clientOut ) {
    this.clientOut = clientOut;
  }

  public TClientLevel getClientLevelOut() {
    return clientLevelOut;
  }

  public void setClientLevelOut( TClientLevel clientLevelOut ) {
    this.clientLevelOut = clientLevelOut;
  }

  public List getListChannelSessionSchedules() {
    return listChannelSessionSchedules;
  }

}
