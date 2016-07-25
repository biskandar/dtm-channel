package com.beepcast.channel;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelInitializer implements ServletContextListener {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static final String PROPERTY_FILE_CHANNEL = "channel.config.file";

  static final DLogContext lctx = new SimpleContext( "ChannelInitializer" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void contextInitialized( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    ChannelConf channelConf = ChannelConfFactory
        .generateChannelConf( PROPERTY_FILE_CHANNEL );
    logStr = this.getClass() + " : initialized " + channelConf;
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

    ChannelApp channelApp = ChannelApp.getInstance();
    channelApp.init( channelConf );
    channelApp.start();
    logStr = this.getClass() + " : initialized " + channelApp;
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

  }

  public void contextDestroyed( ServletContextEvent sce ) {

    ServletContext context = sce.getServletContext();
    String logStr = "";

    GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

    ChannelApp channelApp = ChannelApp.getInstance();
    channelApp.stop();
    logStr = this.getClass() + " : destroyed ";
    context.log( logStr );
    System.out.println( logStr );
    DLog.debug( lctx , logStr );

  }

}
