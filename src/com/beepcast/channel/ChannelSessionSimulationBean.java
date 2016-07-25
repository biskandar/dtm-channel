package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionSimulationBean {

  private int id;
  private String simulationType;
  private long simulationNumbers;
  private int channelSessionId;
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
  private boolean active;

  private ChannelSessionBean channelSessionBean;

  public ChannelSessionSimulationBean() {
    newStatus = ChannelSessionNewStatus.ST_EMPTY;
    active = true;
  }

  public int getId() {
    return id;
  }

  public void setId( int id ) {
    this.id = id;
  }

  public String getSimulationType() {
    return simulationType;
  }

  public void setSimulationType( String simulationType ) {
    this.simulationType = simulationType;
  }

  public long getSimulationNumbers() {
    return simulationNumbers;
  }

  public void setSimulationNumbers( long simulationNumbers ) {
    this.simulationNumbers = simulationNumbers;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
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

  public boolean isActive() {
    return active;
  }

  public void setActive( boolean active ) {
    this.active = active;
  }

  public ChannelSessionBean getChannelSessionBean() {
    return channelSessionBean;
  }

  public void setChannelSessionBean( ChannelSessionBean channelSessionBean ) {
    this.channelSessionBean = channelSessionBean;
  }

}
