package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionSimulationBeanFactory {

  public static ChannelSessionSimulationBean createChannelSessionSimulationBean(
      String simulationType , long simulationNumbers , int channelSessionId ,
      long totalNumbers , long totalRemains , double debitUnits , int priority ,
      boolean suspended , boolean started , boolean finished , int newStatus ,
      Date dateStarted , Date dateSuspended , Date dateFinished ) {
    ChannelSessionSimulationBean bean = new ChannelSessionSimulationBean();
    bean.setSimulationType( simulationType );
    bean.setSimulationNumbers( simulationNumbers );
    bean.setChannelSessionId( channelSessionId );
    bean.setTotalNumbers( totalNumbers );
    bean.setTotalRemains( totalRemains );
    bean.setDebitUnits( debitUnits );
    bean.setPriority( priority );
    bean.setSuspended( suspended );
    bean.setStarted( started );
    bean.setFinished( finished );
    bean.setNewStatus( newStatus );
    bean.setDateStarted( dateStarted );
    bean.setDateSuspended( dateSuspended );
    bean.setDateFinished( dateFinished );
    bean.setActive( true );
    return bean;
  }

}
