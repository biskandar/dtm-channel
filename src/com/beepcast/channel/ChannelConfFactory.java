package com.beepcast.channel;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.firsthop.common.util.xml.TreeUtil;

public class ChannelConfFactory {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelConfFactory" );

  static final GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static ChannelConf generateChannelConf( String propertyFile ) {
    ChannelConf channelConf = generateChannelConf();

    if ( StringUtils.isBlank( propertyFile ) ) {
      return channelConf;
    }

    DLog.debug( lctx , "Loading from property = " + propertyFile );

    Element element = globalEnv.getElement( ChannelConf.class.getName() ,
        propertyFile );
    if ( element != null ) {
      boolean result = validateTag( element );
      if ( result ) {
        extractElement( element , channelConf );
      }
    }

    return channelConf;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static ChannelConf generateChannelConf() {
    ChannelConf channelConf = new ChannelConf();

    channelConf.setDebug( false );

    channelConf.setMsgPriority( 0 );
    channelConf.setDbreaderSleepTime( 10000 );

    channelConf.setProcessorConcurrent( false );
    channelConf.setProcessorWorkerSize( 1 );
    channelConf.setProcessorQueueSize( 10 );

    channelConf.setWorkerPoolExecutor( 5 );
    channelConf.setWorkerBurstSize( 2 );
    channelConf.setWorkerMaxLoad( 10 );
    channelConf.setWorkerMaxSendBufferRecords( 1000 );
    channelConf.setWorkerMaxBatchRecords( 1000 );
    channelConf.setWorkerSleepTime( 15000 );

    channelConf.setManagementSleepTime( 5000 );

    channelConf.setManagementUpdateChannelSessionReport( false );
    channelConf.setManagementUpdateChannelSessionReport_workerSize( 1 );
    channelConf.setManagementUpdateChannelSessionReport_lastDays( 1 );
    channelConf.setManagementUpdateChannelSessionReport_expiryDays( 30 );
    channelConf
        .setManagementUpdateChannelSessionReport_downloadPath( "./download/report" );

    channelConf.setManagementCleanChannelLog( false );
    channelConf.setManagementCleanChannelLog_expiryDays( 120 );
    channelConf.setManagementCleanChannelLog_limitRecords( 100 );

    return channelConf;
  }

  private static boolean validateTag( Element element ) {
    boolean result = false;

    if ( element == null ) {
      DLog.warning( lctx , "Found empty in element xml" );
      return result;
    }

    Node node = TreeUtil.first( element , "channel" );
    if ( node == null ) {
      DLog.warning( lctx , "Can not find root tag <channel>" );
      return result;
    }

    result = true;
    return result;
  }

  private static boolean extractElement( Element element ,
      ChannelConf channelConf ) {
    boolean result = false;

    String stemp;

    Node nodeChannel = TreeUtil.first( element , "channel" );
    if ( nodeChannel == null ) {
      DLog.warning( lctx , "Can not find tag of channel , using default ." );
      return result;
    }

    stemp = TreeUtil.getAttribute( nodeChannel , "debug" );
    if ( ( stemp != null ) && ( stemp.equalsIgnoreCase( "true" ) ) ) {
      channelConf.setDebug( true );
    }

    Node nodeMessage = TreeUtil.first( nodeChannel , "message" );
    extractNodeMessage( nodeMessage , channelConf );

    Node nodeAgents = TreeUtil.first( nodeChannel , "agents" );
    extractNodeAgents( nodeAgents , channelConf );

    result = true;
    return result;
  }

  private static boolean extractNodeMessage( Node nodeMessage ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeMessage == null ) {
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeMessage , "priority" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setMsgPriority( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgents( Node nodeAgents ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgents == null ) {
      return result;
    }

    Node nodeAgent = TreeUtil.first( nodeAgents , "agent" );
    while ( nodeAgent != null ) {
      extractNodeAgent( nodeAgent , channelConf );
      nodeAgent = TreeUtil.next( nodeAgent , "agent" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgent( Node nodeAgent ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgent == null ) {
      return result;
    }

    String agentName = TreeUtil.getAttribute( nodeAgent , "name" );
    if ( ( agentName == null ) || ( agentName.equals( "" ) ) ) {
      return result;
    }

    // process per agent name

    if ( agentName.equals( "dbreader" ) ) {
      extractNodeAgentDbReader( nodeAgent , channelConf );
    }
    if ( agentName.equals( "processor" ) ) {
      extractNodeAgentProcessor( nodeAgent , channelConf );
    }
    if ( agentName.equals( "worker" ) ) {
      extractNodeAgentWorker( nodeAgent , channelConf );
    }
    if ( agentName.equals( "management" ) ) {
      extractNodeAgentManagement( nodeAgent , channelConf );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgentDbReader( Node nodeAgent ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgent == null ) {
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeAgent , "sleepTime" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setDbreaderSleepTime( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgentProcessor( Node nodeAgent ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgent == null ) {
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeAgent , "concurrent" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      channelConf.setProcessorConcurrent( stemp.equalsIgnoreCase( "true" ) );
    }

    stemp = TreeUtil.getAttribute( nodeAgent , "workerSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setProcessorWorkerSize( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = TreeUtil.getAttribute( nodeAgent , "queueSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setProcessorQueueSize( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgentWorker( Node nodeAgent ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgent == null ) {
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeAgent , "poolExecutor" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerPoolExecutor( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = TreeUtil.getAttribute( nodeAgent , "burstSize" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerBurstSize( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = TreeUtil.getAttribute( nodeAgent , "maxLoad" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerMaxLoad( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = TreeUtil.getAttribute( nodeAgent , "maxSendBufferRecords" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerMaxSendBufferRecords( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = TreeUtil.getAttribute( nodeAgent , "maxBatchRecords" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerMaxBatchRecords( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = TreeUtil.getAttribute( nodeAgent , "sleepTime" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setWorkerSleepTime( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    result = true;
    return result;
  }

  private static boolean extractNodeAgentManagement( Node nodeAgent ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeAgent == null ) {
      return result;
    }

    String stemp;

    stemp = TreeUtil.getAttribute( nodeAgent , "sleepTime" );
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        channelConf.setManagementSleepTime( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    Node nodeTask = TreeUtil.first( nodeAgent , "task" );
    while ( nodeTask != null ) {
      extractNodeTask( nodeTask , channelConf );
      nodeTask = TreeUtil.next( nodeTask , "task" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeTask( Node nodeTask ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeTask == null ) {
      return result;
    }

    String taskName = TreeUtil.getAttribute( nodeTask , "name" );
    String taskEnabled = TreeUtil.getAttribute( nodeTask , "enabled" );
    if ( ( taskName == null ) || ( taskEnabled == null ) ) {
      return result;
    }

    if ( taskName.equals( "updateChannelSessionReport" )
        && taskEnabled.equals( "true" ) ) {
      extractNodeTaskUpdateChannelSessionReport( nodeTask , channelConf );
    }
    if ( taskName.equals( "cleanChanneLog" ) && taskEnabled.equals( "true" ) ) {
      extractNodeTaskCleanChannelLog( nodeTask , channelConf );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeTaskUpdateChannelSessionReport(
      Node nodeTask , ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeTask == null ) {
      return result;
    }

    channelConf.setManagementUpdateChannelSessionReport( true );

    String propertyName , propertyValue;
    Node nodeProperty = TreeUtil.first( nodeTask , "property" );
    while ( nodeProperty != null ) {
      propertyName = TreeUtil.getAttribute( nodeProperty , "name" );
      propertyValue = TreeUtil.getAttribute( nodeProperty , "value" );
      if ( ( propertyName != null ) && ( propertyValue != null ) ) {
        if ( propertyName.equals( "workerSize" ) ) {
          try {
            channelConf
                .setManagementUpdateChannelSessionReport_workerSize( Integer
                    .parseInt( propertyValue ) );
          } catch ( NumberFormatException e ) {
          }
        }
        if ( propertyName.equals( "lastDays" ) ) {
          try {
            channelConf
                .setManagementUpdateChannelSessionReport_lastDays( Integer
                    .parseInt( propertyValue ) );
          } catch ( NumberFormatException e ) {
          }
        }
        if ( propertyName.equals( "expiryDays" ) ) {
          try {
            channelConf
                .setManagementUpdateChannelSessionReport_expiryDays( Integer
                    .parseInt( propertyValue ) );
          } catch ( NumberFormatException e ) {
          }
        }
        if ( propertyName.equals( "downloadPath" ) ) {
          if ( ( propertyValue != null ) && ( !propertyValue.equals( "" ) ) ) {
            channelConf
                .setManagementUpdateChannelSessionReport_downloadPath( propertyValue );
          }
        }
      }
      nodeProperty = TreeUtil.next( nodeProperty , "property" );
    }

    result = true;
    return result;
  }

  private static boolean extractNodeTaskCleanChannelLog( Node nodeTask ,
      ChannelConf channelConf ) {
    boolean result = false;
    if ( nodeTask == null ) {
      return result;
    }

    channelConf.setManagementCleanChannelLog( true );

    String propertyName , propertyValue;
    Node nodeProperty = TreeUtil.first( nodeTask , "property" );
    while ( nodeProperty != null ) {
      propertyName = TreeUtil.getAttribute( nodeProperty , "name" );
      propertyValue = TreeUtil.getAttribute( nodeProperty , "value" );
      if ( ( propertyName != null ) && ( propertyValue != null ) ) {
        if ( propertyName.equals( "expiryDays" ) ) {
          try {
            channelConf.setManagementCleanChannelLog_expiryDays( Integer
                .parseInt( propertyValue ) );
          } catch ( NumberFormatException e ) {
          }
        }
        if ( propertyName.equals( "limitRecords" ) ) {
          try {
            channelConf.setManagementCleanChannelLog_limitRecords( Integer
                .parseInt( propertyValue ) );
          } catch ( NumberFormatException e ) {
          }
        }
      }
      nodeProperty = TreeUtil.next( nodeProperty , "property" );
    }

    result = true;
    return result;
  }

}
