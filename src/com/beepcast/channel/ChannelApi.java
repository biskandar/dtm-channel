package com.beepcast.channel;

import java.util.List;

import com.beepcast.channel.view.ChannelSessionReportInfoBean;
import com.beepcast.model.transaction.TransactionInputMessage;

public interface ChannelApi {

  public boolean insertChannelSession( int eventId , int groupSubscriberId ,
      boolean allowDuplicated , boolean useDateSend , double expectResponse ,
      List listChannelSessionSchedules );

  public boolean updateChannelSession( int channelSessionId ,
      int groupSubscriberId , boolean allowDuplicated , boolean useDateSend ,
      double expectResponse , List listChannelSessionSchedules );

  public ChannelSessionBean selectChannelSession( int channelSessionId );

  public ChannelSessionSimulationBean selectChannelSessionSimulation(
      int channelSessionId );

  public ChannelSessionBean getLastChannelSession( int eventId );

  public boolean executeChannelAgent( ChannelSessionBean channelSessionBean );

  public boolean executeChannelAgentSimulation(
      ChannelSessionSimulationBean channelSessionSimulationBean );

  public int getChannelProcessorQueueSize();

  public int refreshChannelProcessorWorkers();

  public int executeChannelProcessor( TransactionInputMessage imsg );

  public String progressToString( int channelSessionId );

  public List listHistoryBroadcasts( int clientId );

  public List listScheduledBroadcasts( int clientId );

  public List listRunningBroadcasts( int clientId );

  public List listHistoryBroadcastsFromMaster( int clientType ,
      int masterClientId );

  public List listScheduledBroadcastsFromMaster( int clientType ,
      int masterClientId );

  public List listRunningBroadcastsFromMaster( int clientType ,
      int masterClientId );

  public ChannelLogBean getChannelLogBean( int eventId , String phoneNumber );

  public String createBroadcastFileName( int channelSessionId , boolean fullPath );

  public ChannelSessionReportInfoBean getReportInfo( int channelSessionId );

  public boolean requestToUpdateReport( int channelSessionId );

  public boolean requestToUpdateReports( List listChannelSessionIds );

}
