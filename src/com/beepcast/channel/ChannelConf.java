package com.beepcast.channel;

public class ChannelConf {

  private boolean debug;
  private int msgPriority;
  private int dbreaderSleepTime;
  private boolean processorConcurrent;
  private int processorWorkerSize;
  private int processorQueueSize;
  private int workerPoolExecutor;
  private int workerBurstSize;
  private int workerMaxLoad;
  private int workerMaxSendBufferRecords;
  private int workerMaxBatchRecords;
  private int workerSleepTime;
  private int managementSleepTime;

  private boolean managementUpdateChannelSessionReport;
  private int managementUpdateChannelSessionReport_workerSize;
  private int managementUpdateChannelSessionReport_lastDays;
  private int managementUpdateChannelSessionReport_expiryDays;
  private String managementUpdateChannelSessionReport_downloadPath;

  private boolean managementCleanChannelLog;
  private int managementCleanChannelLog_expiryDays;
  private int managementCleanChannelLog_limitRecords;

  public ChannelConf() {
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug( boolean debug ) {
    this.debug = debug;
  }

  public int getMsgPriority() {
    return msgPriority;
  }

  public void setMsgPriority( int msgPriority ) {
    this.msgPriority = msgPriority;
  }

  public int getDbreaderSleepTime() {
    return dbreaderSleepTime;
  }

  public void setDbreaderSleepTime( int dbreaderSleepTime ) {
    this.dbreaderSleepTime = dbreaderSleepTime;
  }

  public boolean isProcessorConcurrent() {
    return processorConcurrent;
  }

  public void setProcessorConcurrent( boolean processorConcurrent ) {
    this.processorConcurrent = processorConcurrent;
  }

  public int getProcessorWorkerSize() {
    return processorWorkerSize;
  }

  public void setProcessorWorkerSize( int processorWorkerSize ) {
    this.processorWorkerSize = processorWorkerSize;
  }

  public int getProcessorQueueSize() {
    return processorQueueSize;
  }

  public void setProcessorQueueSize( int processorQueueSize ) {
    this.processorQueueSize = processorQueueSize;
  }

  public int getWorkerPoolExecutor() {
    return workerPoolExecutor;
  }

  public void setWorkerPoolExecutor( int workerPoolExecutor ) {
    this.workerPoolExecutor = workerPoolExecutor;
  }

  public int getWorkerBurstSize() {
    return workerBurstSize;
  }

  public void setWorkerBurstSize( int workerBurstSize ) {
    this.workerBurstSize = workerBurstSize;
  }

  public int getWorkerMaxLoad() {
    return workerMaxLoad;
  }

  public void setWorkerMaxLoad( int workerMaxLoad ) {
    this.workerMaxLoad = workerMaxLoad;
  }

  public int getWorkerMaxSendBufferRecords() {
    return workerMaxSendBufferRecords;
  }

  public void setWorkerMaxSendBufferRecords( int workerMaxSendBufferRecords ) {
    this.workerMaxSendBufferRecords = workerMaxSendBufferRecords;
  }

  public int getWorkerMaxBatchRecords() {
    return workerMaxBatchRecords;
  }

  public void setWorkerMaxBatchRecords( int workerMaxBatchRecords ) {
    this.workerMaxBatchRecords = workerMaxBatchRecords;
  }

  public int getWorkerSleepTime() {
    return workerSleepTime;
  }

  public void setWorkerSleepTime( int workerSleepTime ) {
    this.workerSleepTime = workerSleepTime;
  }

  public int getManagementSleepTime() {
    return managementSleepTime;
  }

  public void setManagementSleepTime( int managementSleepTime ) {
    this.managementSleepTime = managementSleepTime;
  }

  public boolean isManagementUpdateChannelSessionReport() {
    return managementUpdateChannelSessionReport;
  }

  public void setManagementUpdateChannelSessionReport(
      boolean managementUpdateChannelSessionReport ) {
    this.managementUpdateChannelSessionReport = managementUpdateChannelSessionReport;
  }

  public int getManagementUpdateChannelSessionReport_workerSize() {
    return managementUpdateChannelSessionReport_workerSize;
  }

  public void setManagementUpdateChannelSessionReport_workerSize(
      int managementUpdateChannelSessionReport_workerSize ) {
    this.managementUpdateChannelSessionReport_workerSize = managementUpdateChannelSessionReport_workerSize;
  }

  public int getManagementUpdateChannelSessionReport_lastDays() {
    return managementUpdateChannelSessionReport_lastDays;
  }

  public void setManagementUpdateChannelSessionReport_lastDays(
      int managementUpdateChannelSessionReport_lastDays ) {
    this.managementUpdateChannelSessionReport_lastDays = managementUpdateChannelSessionReport_lastDays;
  }

  public int getManagementUpdateChannelSessionReport_expiryDays() {
    return managementUpdateChannelSessionReport_expiryDays;
  }

  public void setManagementUpdateChannelSessionReport_expiryDays(
      int managementUpdateChannelSessionReport_expiryDays ) {
    this.managementUpdateChannelSessionReport_expiryDays = managementUpdateChannelSessionReport_expiryDays;
  }

  public String getManagementUpdateChannelSessionReport_downloadPath() {
    return managementUpdateChannelSessionReport_downloadPath;
  }

  public void setManagementUpdateChannelSessionReport_downloadPath(
      String managementUpdateChannelSessionReport_downloadPath ) {
    this.managementUpdateChannelSessionReport_downloadPath = managementUpdateChannelSessionReport_downloadPath;
  }

  public boolean isManagementCleanChannelLog() {
    return managementCleanChannelLog;
  }

  public void setManagementCleanChannelLog( boolean managementCleanChannelLog ) {
    this.managementCleanChannelLog = managementCleanChannelLog;
  }

  public int getManagementCleanChannelLog_expiryDays() {
    return managementCleanChannelLog_expiryDays;
  }

  public void setManagementCleanChannelLog_expiryDays(
      int managementCleanChannelLog_expiryDays ) {
    this.managementCleanChannelLog_expiryDays = managementCleanChannelLog_expiryDays;
  }

  public int getManagementCleanChannelLog_limitRecords() {
    return managementCleanChannelLog_limitRecords;
  }

  public void setManagementCleanChannelLog_limitRecords(
      int managementCleanChannelLog_limitRecords ) {
    this.managementCleanChannelLog_limitRecords = managementCleanChannelLog_limitRecords;
  }

}
