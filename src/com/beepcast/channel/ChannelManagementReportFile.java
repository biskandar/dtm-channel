package com.beepcast.channel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.channel.ChannelManagementReportFileClass.RecordBase;
import com.beepcast.channel.ChannelManagementReportFileClass.RecordChannelSessionSubscriber;
import com.beepcast.channel.ChannelManagementReportFileClass.RecordGateway;
import com.beepcast.channel.ChannelManagementReportFileClass.RecordSubscriber;
import com.beepcast.channel.ChannelManagementReportFileClass.XipmeLink;
import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.database.ConnectionWrapper;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementReportFile {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelManagementReportFile" );

  static final String STATUS_NUMBER_SUBSCRIBED = "true";
  static final String STATUS_NUMBER_UNSUBSCRIBED = "false";

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelApp channelApp;
  private DatabaseLibrary dbLib;

  private ChannelSessionService csService;
  private ChannelSessionReportService csrService;

  private ChannelManagementReportFileClass fileClass;
  private ChannelManagementReportFileQuery fileQuery;
  private ChannelManagementReportFileCsv fileCsv;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportFile() {

    channelApp = ChannelApp.getInstance();
    dbLib = DatabaseLibrary.getInstance();

    csService = new ChannelSessionService();
    csrService = new ChannelSessionReportService();

    fileClass = new ChannelManagementReportFileClass();
    fileQuery = new ChannelManagementReportFileQuery();
    fileCsv = new ChannelManagementReportFileCsv();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String filePathWithoutExtension( String eventName ) {
    String strFileName = null;

    // verify group event name
    if ( StringUtils.isBlank( eventName ) ) {
      DLog.warning( lctx , "Failed to generate file path "
          + ", found blank event name" );
      return strFileName;
    }

    // clean characters
    eventName = eventName.trim();
    eventName = eventName.replaceAll( "[^a-zA-Z0-9]" , "_" );

    // add date created time
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
    String dateTimeNowStr = sdf.format( now );

    // random alphanumeric 5 digit
    String randAlphanum5digits = RandomStringUtils.randomAlphanumeric( 5 );

    // compose str file name
    strFileName = "broadcast-details-report-".concat( eventName ).concat( "-" )
        .concat( dateTimeNowStr ).concat( "-" ).concat( randAlphanum5digits );

    return strFileName;
  }

  public boolean generateFileReport( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      return result;
    }
    return generateFileReport( csrService
        .selectByChannelSessionId( channelSessionId ) );
  }

  public boolean generateFileReport( ChannelSessionReportBean csrBean ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }

    // prepare the channel session params
    int channelSessionId = csrBean.getChannelSessionId();
    String channelSessionKey = csrBean.getKey();

    // compose header log
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // validate must be param
    if ( ( channelSessionKey == null ) || ( channelSessionKey.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to generate file report "
          + ", found blank channel session key" );
      return result;
    }

    // resolve download base path
    String downloadBasePath = null;
    if ( channelApp != null ) {
      ChannelConf channelConf = channelApp.getChannelConf();
      if ( channelConf != null ) {
        downloadBasePath = channelConf
            .getManagementUpdateChannelSessionReport_downloadPath();
      }
    }

    // validate download base path
    if ( ( downloadBasePath == null ) || ( downloadBasePath.equals( "" ) ) ) {
      DLog.warning( lctx , "Failed to generate file report "
          + ", found null and/or empty download base path" );
      return result;
    }

    // clean download base path
    if ( !downloadBasePath.endsWith( "/" ) ) {
      downloadBasePath = downloadBasePath.concat( "/" );
    }

    // resolved download path
    String downloadPath = downloadBasePath.concat( channelSessionKey );

    // generate file based on key and channel session id
    result = generateFileReport( headerLog , downloadPath , channelSessionId );
    return result;
  }

  public boolean generateFileReport( String headerLog ,
      String fileReportNameWithoutExt , int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found zero channelSessionId" );
      return result;
    }
    result = generateFileReport( headerLog , fileReportNameWithoutExt ,
        csService.getChannelSession( channelSessionId ) );
    return result;
  }

  public boolean generateFileReport( String headerLog ,
      String fileReportNameWithoutExt , ChannelSessionBean csBean ) {
    boolean result = false;

    if ( ( fileReportNameWithoutExt == null )
        || ( fileReportNameWithoutExt.equals( "" ) ) ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found empty fileReportNameWithoutExt" );
      return result;
    }

    if ( ( csBean == null ) || ( !csBean.isActive() ) ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found invalid channel session profile" );
      return result;
    }

    try {

      // log first
      DLog.debug( lctx , headerLog + "Start to generate file report "
          + "from : groupSubscriberId = " + csBean.getGroupSubscriberId()
          + " , fileReportNameWithoutExt = " + fileReportNameWithoutExt );

      // create text file object
      File frpt = ChannelManagementReportFileCommon
          .generateFileTextObject( fileReportNameWithoutExt );
      if ( frpt == null ) {
        DLog.warning( lctx , headerLog + "Failed generate file report object "
            + ", fileReportNameWithoutExt = " + fileReportNameWithoutExt );
        return result;
      }

      // open text file stream
      DataOutputStream dos = ChannelManagementReportFileCommon
          .openFileTextStream( frpt );
      if ( dos == null ) {
        DLog.warning( lctx , headerLog + "Failed generate file report stream " );
        return result;
      }

      // extract records and write into file dos
      int totalRecords = extractRecordsStoreToFile( channelApp.isDebug() ,
          headerLog , dos , csBean.getId() , csBean.getGroupSubscriberId() );
      DLog.debug( lctx , headerLog + "Stored data " + totalRecords
          + " record(s) into report file stream" );

      // extract remain records and write into file dos
      int totalRemainRecords = extractRemainRecordsStoreToFile(
          channelApp.isDebug() , headerLog , dos , csBean.getId() );
      DLog.debug( lctx , headerLog + "Stored data " + totalRemainRecords
          + " remain record(s) into report file stream" );

      // close text file stream
      ChannelManagementReportFileCommon.closeFileTextStream( dos );
      DLog.debug( lctx , headerLog + "Saved data report as text file" );

      // get file attachment based on event id
      File fileAttachment = ChannelManagementReportFileCommon
          .getFileAttachment( headerLog , csBean.getEventId() );

      // convert to zip file
      if ( !ChannelManagementReportFileCommon.convertFileTextToZip( headerLog ,
          frpt , fileAttachment ) ) {
        DLog.warning( lctx , headerLog + "Failed to convert text file "
            + "report into zip format" );
      }

      // delete text file
      if ( !frpt.delete() ) {
        DLog.warning( lctx , headerLog + "Failed to delete unused "
            + "report text file" );
      }

      // log cleaning
      DLog.debug( lctx , headerLog + "Cleaned unused report text file" );

      // return result as true
      result = true;
    } catch ( FileNotFoundException fnfe ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", can not find file report , " + fnfe );
    } catch ( IOException ioe ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", can not close file report , " + ioe );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Extract Records Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int extractRecordsStoreToFile( boolean debug , String headerLog ,
      DataOutputStream dos , int channelSessionId , int groupSubscriberId ) {
    int totalRecords = 0;

    if ( dos == null ) {
      return totalRecords;
    }

    // prepare database query params
    long deltaTime = 0;
    ConnectionWrapper cwGateway = null , cwTransaction = null , cwSubscriber = null;
    Statement stGateway = null , stTransaction = null , stSubscriber = null;
    ResultSet rsGateway = null , rsTransaction = null , rsSubscriber = null;

    try {

      cwGateway = dbLib.getReaderConnection( "transactiondb" );
      cwTransaction = dbLib.getReaderConnection( "reportdb" );
      cwSubscriber = dbLib.getReaderConnection( "transactiondb" );
      if ( ( cwGateway == null ) || ( cwTransaction == null )
          || ( cwSubscriber == null ) ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db connection pool" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully created reader connection "
          + "for gateway , transaction , and subscriber" );

      stGateway = cwGateway.createStatement();
      stTransaction = cwTransaction.createStatement();
      stSubscriber = cwSubscriber.createStatement();
      if ( ( stGateway == null ) || ( stTransaction == null )
          || ( stSubscriber == null ) ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db statement" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully created statement "
          + "for gateway , transaction , and subscriber" );

      deltaTime = System.currentTimeMillis();
      String sqlGateway = fileQuery.generateSqlGateway( channelSessionId );
      rsGateway = stGateway.executeQuery( sqlGateway );
      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( debug ) {
        DLog.debug( lctx , headerLog + "Performed sql from gateway table "
            + "( take " + deltaTime + " ms ) : " + sqlGateway );
      }

      deltaTime = System.currentTimeMillis();
      String sqlTransaction = fileQuery
          .generateSqlTransaction( channelSessionId );
      rsTransaction = stTransaction.executeQuery( sqlTransaction );
      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( debug ) {
        DLog.debug( lctx , headerLog + "Performed sql from transaction table "
            + "( take " + deltaTime + " ms ) : " + sqlTransaction );
      }

      deltaTime = System.currentTimeMillis();
      String sqlSubscriber = fileQuery
          .generateSqlSubscriber( groupSubscriberId );
      rsSubscriber = stSubscriber.executeQuery( sqlSubscriber );
      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( debug ) {
        DLog.debug( lctx , headerLog + "Performed sql from subscriber table "
            + "( take " + deltaTime + " ms ) : " + sqlSubscriber );
      }

      if ( ( rsGateway == null ) || ( rsTransaction == null )
          || ( rsSubscriber == null ) ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db record set" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully created result set "
          + "for gateway , transaction , and subscriber" );

      // first time for headers
      boolean writeHeader = true;

      // process gateway log and subscriber record(s)
      // and store particular record(s) to file
      DLog.debug( lctx , headerLog + "Extracting records "
          + "and save into the csv file" );
      RecordGateway recordGatewayOld = null , recordGatewayNew = null;
      RecordSubscriber recordSubscriberOld = null , recordSubscriberNew = null;

      // prepare record subscriber first
      if ( rsSubscriber.next() ) {
        recordSubscriberNew = extractRsSubscriber( headerLog , rsSubscriber );
      }
      if ( recordSubscriberNew != null ) {
        recordSubscriberOld = fileClass
            .cloneRecordSubscriber( recordSubscriberNew );
      }

      // loop result set current
      while ( true ) {

        int tblCurrent = 0; // 0 - gateway , 1 - transaction
        ResultSet rsCurrent = rsGateway;
        if ( !rsCurrent.next() ) {
          tblCurrent = 1;
          rsCurrent = rsTransaction;
          if ( !rsCurrent.next() ) {
            break;
          }
        }

        if ( tblCurrent == 0 ) {
          recordGatewayNew = extractRsGateway( headerLog , rsCurrent );
        }
        if ( tblCurrent == 1 ) {
          recordGatewayNew = extractRsTransaction( headerLog , rsCurrent );
        }
        if ( recordGatewayNew == null ) {
          continue;
        }

        // merging xipme links : ignore all record(s) with the same message id
        while ( rsCurrent.next() ) {
          RecordGateway recordGatewayTmp = null;
          if ( tblCurrent == 0 ) {
            recordGatewayTmp = extractRsGateway( headerLog , rsCurrent );
          }
          if ( tblCurrent == 1 ) {
            recordGatewayTmp = extractRsTransaction( headerLog , rsCurrent );
          }
          if ( recordGatewayTmp == null ) {
            break;
          }
          if ( !StringUtils.equals( recordGatewayTmp.getMessageId() ,
              recordGatewayNew.getMessageId() ) ) {
            rsCurrent.previous();
            break;
          }
          mergeXipmeLinks( recordGatewayNew , recordGatewayTmp );
        }

        if ( writeHeader ) {
          // extract header and write into file dos
          if ( !fileCsv.writeHeaders( channelApp.isDebug() , headerLog , dos ,
              recordGatewayNew.mapXipmeLinks().size() ) ) {
            DLog.warning( lctx , headerLog + "Failed to write header line "
                + "into file report stream " );
          }
          writeHeader = false;
        }

        if ( StringUtils.isBlank( recordGatewayNew.getPhone() ) ) {
          DLog.warning( lctx , headerLog + "Found empty phone "
              + "inside gateway record , bypass it" );
          continue;
        }

        if ( comparePhone( headerLog , recordGatewayOld , recordGatewayNew ) == 0 ) {
          // write to file
          if ( writeRecordToFile( debug , headerLog , recordGatewayNew ,
              recordSubscriberOld , dos ) ) {
            totalRecords = totalRecords + 1;
          }
          // saved old gateway record
          if ( recordGatewayNew != null ) {
            recordGatewayOld = fileClass.cloneRecordGateway( recordGatewayNew );
          }
          continue;
        }

        switch ( comparePhone( headerLog , recordGatewayNew ,
            recordSubscriberNew ) ) {

        case -2 : // --- phone subscriber empty
          // write to file
          if ( writeRecordToFile( debug , headerLog , recordGatewayNew , null ,
              dos ) ) {
            totalRecords = totalRecords + 1;
          }
          break;

        case 2 : // --- phone gateway empty
          // bypass it , the gateway record must exist
          break;

        case -1 : // --- phone gateway > phone subscriber
          while ( rsSubscriber.next() ) {
            recordSubscriberNew = extractRsSubscriber( headerLog , rsSubscriber );
            boolean isBreak = false;
            switch ( comparePhone( headerLog , recordGatewayNew ,
                recordSubscriberNew ) ) {
            case 0 :
              // write to file
              if ( writeRecordToFile( debug , headerLog , recordGatewayNew ,
                  recordSubscriberNew , dos ) ) {
                totalRecords = totalRecords + 1;
              }
              // saved old subscriber record
              if ( recordSubscriberNew != null ) {
                recordSubscriberOld = fileClass
                    .cloneRecordSubscriber( recordSubscriberNew );
              }
              // extract next subscriber record
              if ( rsSubscriber.next() ) {
                recordSubscriberNew = extractRsSubscriber( headerLog ,
                    rsSubscriber );
              }
              isBreak = true;
              break;
            case 1 :
              isBreak = true;
              break;
            }
            if ( isBreak ) {
              break;
            }
          } // end while rsSubscriber.next()
          break;

        case 0 : // ---- phone gateway == phone subscriber
          // write to file
          if ( writeRecordToFile( debug , headerLog , recordGatewayNew ,
              recordSubscriberNew , dos ) ) {
            totalRecords = totalRecords + 1;
          }
          // saved old subscriber record
          if ( recordSubscriberNew != null ) {
            recordSubscriberOld = fileClass
                .cloneRecordSubscriber( recordSubscriberNew );
          }
          // extract next subscriber record
          if ( rsSubscriber.next() ) {
            recordSubscriberNew = extractRsSubscriber( headerLog , rsSubscriber );
          }
          break;

        case +1 : // --- phone gateway < phone subscriber
          // write to file
          if ( writeRecordToFile( debug , headerLog , recordGatewayNew ,
              recordSubscriberOld , dos ) ) {
            totalRecords = totalRecords + 1;
          }
          break;

        } // end switch

        // saved old gateway record
        if ( recordGatewayNew != null ) {
          recordGatewayOld = fileClass.cloneRecordGateway( recordGatewayNew );
        }

      } // end while

    } catch ( SQLException sqle ) {

      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found failed to query the table , " + sqle );

    } finally {

      // close result set

      if ( rsGateway != null ) {
        try {
          rsGateway.close();
          DLog.debug( lctx , headerLog + "Closed gateway result set object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "result set gateway , " + e );
        }
      }
      if ( rsTransaction != null ) {
        try {
          rsTransaction.close();
          DLog.debug( lctx , headerLog + "Closed transaction result set object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "result set transaction , " + e );
        }
      }
      if ( rsSubscriber != null ) {
        try {
          rsSubscriber.close();
          DLog.debug( lctx , headerLog + "Closed subscriber result set object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "result set subscriber , " + e );
        }
      }

      // close statement

      if ( stGateway != null ) {
        try {
          stGateway.close();
          DLog.debug( lctx , headerLog + "Closed gateway statement object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "statement gateway , " + e );
        }
      }
      if ( stTransaction != null ) {
        try {
          stTransaction.close();
          DLog.debug( lctx , headerLog + "Closed transaction statement object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "statement transaction , " + e );
        }
      }
      if ( stSubscriber != null ) {
        try {
          stSubscriber.close();
          DLog.debug( lctx , headerLog + "Closed subscriber statement object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close "
              + "statement subscriber , " + e );
        }
      }

      // close connection wrapper

      if ( cwGateway != null ) {
        try {
          cwGateway.disconnect( true );
          DLog.debug( lctx , headerLog + "Disconnected gateway reader "
              + "connection object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close connection "
              + "reader gateway , " + e );
        }
      }
      if ( cwTransaction != null ) {
        try {
          cwTransaction.disconnect( true );
          DLog.debug( lctx , headerLog + "Disconnected transaction reader "
              + "connection object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close connection "
              + "reader transacation , " + e );
        }
      }
      if ( cwSubscriber != null ) {
        try {
          cwSubscriber.disconnect( true );
          DLog.debug( lctx , headerLog + "Disconnected subscriber reader "
              + "connection object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close connection "
              + "reader subscriber , " + e );
        }
      }

    } // end finally

    return totalRecords;
  }

  private int extractRemainRecordsStoreToFile( boolean debug ,
      String headerLog , DataOutputStream dos , int channelSessionId ) {
    int totalRecords = 0;

    if ( dos == null ) {
      return totalRecords;
    }

    // prepare database query params
    ConnectionWrapper cwChannelSessionSubscriber = null;
    Statement stChannelSessionSubscriber = null;
    ResultSet rsChannelSessionSubscriber = null;

    try {

      cwChannelSessionSubscriber = dbLib.getReaderConnection( "transactiondb" );
      if ( cwChannelSessionSubscriber == null ) {
        return totalRecords;
      }

      stChannelSessionSubscriber = cwChannelSessionSubscriber.createStatement();
      if ( stChannelSessionSubscriber == null ) {
        return totalRecords;
      }

      String sqlChannelSessionSubscriber = fileQuery
          .generateSqlChannelSessionSubscriber( channelSessionId );
      if ( StringUtils.isBlank( sqlChannelSessionSubscriber ) ) {
        return totalRecords;
      }

      rsChannelSessionSubscriber = stChannelSessionSubscriber
          .executeQuery( sqlChannelSessionSubscriber );
      if ( rsChannelSessionSubscriber == null ) {
        return totalRecords;
      }

      RecordChannelSessionSubscriber recordChannelSessionSubscriber = null;
      while ( rsChannelSessionSubscriber.next() ) {

        recordChannelSessionSubscriber = extractRsChannelSessionSubscriber(
            headerLog , rsChannelSessionSubscriber );
        if ( recordChannelSessionSubscriber == null ) {
          continue;
        }

        String phoneNumber = cleanPhoneNumber( recordChannelSessionSubscriber
            .getPhone() );
        if ( StringUtils.isBlank( phoneNumber ) ) {
          continue;
        }

        if ( writeRemainRecordToFile( debug , headerLog ,
            recordChannelSessionSubscriber , dos ) ) {
          totalRecords = totalRecords + 1;
        }

      } // while ( rsChannelSessionSubscriber.next() )

    } catch ( SQLException sqle ) {

      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found failed to query the table , " + sqle );

    } finally {

      if ( rsChannelSessionSubscriber != null ) {
        try {
          rsChannelSessionSubscriber.close();
          DLog.debug( lctx , headerLog + "Closed channel session subscriber "
              + "result set object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close result set "
              + "channel session subscriber , " + e );
        }
      }

      if ( stChannelSessionSubscriber != null ) {
        try {
          stChannelSessionSubscriber.close();
          DLog.debug( lctx , headerLog + "Closed channel session subscriber "
              + "statement object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close statement "
              + "channel session subscriber , " + e );
        }
      }

      if ( cwChannelSessionSubscriber != null ) {
        try {
          cwChannelSessionSubscriber.disconnect( true );
          DLog.debug( lctx , headerLog
              + "Disconnected channel session subscriber "
              + "reader connection object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close connection reader "
              + "channel session subscriber , " + e );
        }
      }

    } // end finally

    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Write Record Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean writeRecordToFile( boolean debug , String headerLog ,
      RecordGateway recordGateway , RecordSubscriber recordSubscriber ,
      DataOutputStream dos ) {
    boolean result = false;

    if ( recordGateway == null ) {
      DLog.warning( lctx , headerLog + "Failed to extract record "
          + ", found null record gateway" );
      return result;
    }
    if ( dos == null ) {
      DLog.warning( lctx , headerLog + "Failed to extract record "
          + ", found null dos" );
      return result;
    }

    // prepare for the csv file

    String messageId = recordGateway.getMessageId();
    String phoneNumber = cleanPhoneNumber( recordGateway.getPhone() );
    int retryStep = recordGateway.getRetry();
    Date dateSubmitMessage = recordGateway.getSubmitDate();
    String deliveryStatus = recordGateway.getStatus();
    Date dateReceivedStatus = recordGateway.getStatusDate();
    String subscribedStatus = null;
    Date unsubscribedDate = null;
    String globalSubscribedStatus = null;
    Date globalUnsubscribedDate = null;
    int messageCount = recordGateway.getMessageCount();
    String messageContent = recordGateway.getMessageContent();
    List listXipmeLinks = null;

    // prepare for un/subscribed params

    if ( recordSubscriber != null ) {
      if ( recordSubscriber.isSubscribed() ) {
        subscribedStatus = STATUS_NUMBER_SUBSCRIBED;
      } else {
        subscribedStatus = STATUS_NUMBER_UNSUBSCRIBED;
        unsubscribedDate = recordSubscriber.getSubscribedDate();
      }
      if ( recordSubscriber.isGlobalSubscribed() ) {
        globalSubscribedStatus = STATUS_NUMBER_SUBSCRIBED;
      } else {
        globalSubscribedStatus = STATUS_NUMBER_UNSUBSCRIBED;
        globalUnsubscribedDate = recordSubscriber.getGlobalSubscribedDate();
      }
    }

    // prepare for xipme links

    Map mapXipmeLinks = recordGateway.mapXipmeLinks();
    if ( mapXipmeLinks != null ) {
      listXipmeLinks = new ArrayList( mapXipmeLinks.values() );
    }

    // write to csv file

    result = fileCsv.writeRecords( debug , headerLog , dos , messageId ,
        phoneNumber , retryStep , dateSubmitMessage , deliveryStatus ,
        dateReceivedStatus , subscribedStatus , unsubscribedDate ,
        globalSubscribedStatus , globalUnsubscribedDate , messageCount ,
        messageContent , listXipmeLinks );

    return result;
  }

  private boolean writeRemainRecordToFile( boolean debug , String headerLog ,
      RecordChannelSessionSubscriber recordChannelSessionSubscriber ,
      DataOutputStream dos ) {
    boolean result = false;

    if ( recordChannelSessionSubscriber == null ) {
      DLog.warning( lctx , headerLog + "Failed to extract record "
          + ", found null record channel session subscriber" );
      return result;
    }
    if ( dos == null ) {
      DLog.warning( lctx , headerLog + "Failed to extract record "
          + ", found null dos" );
      return result;
    }

    // prepare for the csv file

    String messageId = null;
    String phoneNumber = cleanPhoneNumber( recordChannelSessionSubscriber
        .getPhone() );
    int retryStep = 0;
    Date dateSubmitMessage = null;
    String deliveryStatus = "UNSENT";
    Date dateReceivedStatus = null;
    String subscribedStatus = null;
    Date unsubscribedDate = null;
    String globalSubscribedStatus = null;
    Date globalUnsubscribedDate = null;
    int messageCount = 0;
    String messageContent = null;
    List listXipmeLinks = null;

    // write to csv file

    result = fileCsv.writeRecords( debug , headerLog , dos , messageId ,
        phoneNumber , retryStep , dateSubmitMessage , deliveryStatus ,
        dateReceivedStatus , subscribedStatus , unsubscribedDate ,
        globalSubscribedStatus , globalUnsubscribedDate , messageCount ,
        messageContent , listXipmeLinks );

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Extract Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private RecordGateway extractRsGateway( String headerLog , ResultSet rsGateway ) {
    RecordGateway recordGateway = null;
    if ( rsGateway == null ) {
      return recordGateway;
    }
    try {
      recordGateway = fileClass.newRecordGateway();
      recordGateway.setPhone( rsGateway.getString( "phone" ) );
      recordGateway.setStatus( rsGateway.getString( "status" ) );
      recordGateway.setMessageId( rsGateway.getString( "message_id" ) );
      recordGateway.setRetry( rsGateway.getInt( "retry" ) );
      recordGateway.setSubmitDate( DateTimeFormat.convertToDate( rsGateway
          .getString( "date_tm" ) ) );
      recordGateway.setStatusDate( DateTimeFormat.convertToDate( rsGateway
          .getString( "status_date_tm" ) ) );
      recordGateway.setDebitAmount( rsGateway.getDouble( "debit_amount" ) );
      recordGateway.setMessageCount( rsGateway.getInt( "message_count" ) );
      recordGateway.setMessageContent( rsGateway.getString( "message" ) );

      String xipmeCode = rsGateway.getString( "xipme_code" );
      if ( ( xipmeCode != null ) && ( !xipmeCode.equals( "" ) ) ) {
        XipmeLink xipmeLink = fileClass.newXipmeLink( xipmeCode );
        xipmeLink.setHit( rsGateway.getString( "date_hit" ) != null ? 1 : 0 );
        recordGateway.mapXipmeLinks().put( xipmeLink.getCode() , xipmeLink );
      }

    } catch ( SQLException e ) {
      DLog.warning( lctx , headerLog + "Failed to extract "
          + "result set gateway , " + e );
    }
    return recordGateway;
  }

  private RecordGateway extractRsTransaction( String headerLog ,
      ResultSet rsTransaction ) {
    RecordGateway recordTransaction = null;
    if ( rsTransaction == null ) {
      return recordTransaction;
    }
    try {
      recordTransaction = fileClass.newRecordGateway();
      recordTransaction.setPhone( rsTransaction
          .getString( "destination_address" ) );
      recordTransaction.setStatus( rsTransaction
          .getString( "internal_status_code" ) );
      recordTransaction.setMessageId( rsTransaction
          .getString( "internal_message_id" ) );
      recordTransaction.setRetry( rsTransaction.getInt( "retry_count" ) );
      recordTransaction.setSubmitDate( DateTimeFormat
          .convertToDate( rsTransaction.getString( "date_submitted" ) ) );
      recordTransaction.setStatusDate( DateTimeFormat
          .convertToDate( rsTransaction.getString( "date_received_status" ) ) );
      recordTransaction.setDebitAmount( rsTransaction
          .getDouble( "debit_amount" ) );
      recordTransaction
          .setMessageCount( rsTransaction.getInt( "message_count" ) );
      recordTransaction.setMessageContent( rsTransaction
          .getString( "message_content" ) );
    } catch ( SQLException e ) {
      DLog.warning( lctx , headerLog + "Failed to extract "
          + "result set transaction , " + e );
    }
    return recordTransaction;
  }

  private RecordSubscriber extractRsSubscriber( String headerLog ,
      ResultSet rsSubscriber ) {
    RecordSubscriber recordSubscriber = null;
    if ( rsSubscriber == null ) {
      return recordSubscriber;
    }
    try {
      recordSubscriber = fileClass.newRecordSubscriber();
      recordSubscriber.setPhone( rsSubscriber.getString( "phone" ) );
      recordSubscriber.setSubscribed( rsSubscriber.getInt( "subscribed" ) > 0 );
      recordSubscriber.setSubscribedEventId( rsSubscriber
          .getInt( "subscribed_event_id" ) );
      recordSubscriber.setSubscribedDate( DateTimeFormat
          .convertToDate( rsSubscriber.getString( "date_subscribed" ) ) );
      recordSubscriber.setGlobalSubscribed( rsSubscriber
          .getInt( "global_subscribed" ) > 0 );
      recordSubscriber.setGlobalSubscribedDate( DateTimeFormat
          .convertToDate( rsSubscriber.getString( "date_global_subscribed" ) ) );
      recordSubscriber.setCustRefId( rsSubscriber.getString( "cust_ref_id" ) );
      recordSubscriber
          .setCustRefCode( rsSubscriber.getString( "cust_ref_code" ) );
      recordSubscriber.setDescription( rsSubscriber.getString( "description" ) );
    } catch ( SQLException e ) {
      DLog.warning( lctx , headerLog + "Failed to extract "
          + "result set subscriber , " + e );
    }
    return recordSubscriber;
  }

  private RecordChannelSessionSubscriber extractRsChannelSessionSubscriber(
      String headerLog , ResultSet rsChannelSessionSubscriber ) {
    RecordChannelSessionSubscriber recordChannelSessionSubscriber = null;
    if ( rsChannelSessionSubscriber == null ) {
      return recordChannelSessionSubscriber;
    }
    try {
      recordChannelSessionSubscriber = fileClass
          .newRecordChannelSessionSubscriber();
      recordChannelSessionSubscriber.setPhone( rsChannelSessionSubscriber
          .getString( "phone" ) );
    } catch ( SQLException e ) {
      DLog.warning( lctx , headerLog + "Failed to extract "
          + "result set channel session subscriber , " + e );
    }
    return recordChannelSessionSubscriber;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Utils Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String cleanPhoneNumber( String phoneNumber ) {
    try {
      if ( phoneNumber.startsWith( "+" ) ) {
        phoneNumber = phoneNumber.substring( 1 );
      }
    } catch ( Exception e ) {
    }
    return phoneNumber;
  }

  private long getPhoneNumber( String headerLog , RecordBase recordBase ) {
    long phoneNumber = 0;
    try {
      phoneNumber = Long.parseLong( cleanPhoneNumber( recordBase.getPhone() ) );
    } catch ( Exception e ) {
      phoneNumber = -1;
    }
    return phoneNumber;
  }

  private int comparePhone( String headerLog , RecordBase recordBaseLeft ,
      RecordBase recordBaseRight ) {
    long phoneNumberLeft = getPhoneNumber( headerLog , recordBaseLeft );
    long phoneNumberRight = getPhoneNumber( headerLog , recordBaseRight );
    if ( phoneNumberLeft == phoneNumberRight ) {
      return 0;
    }
    if ( phoneNumberLeft < 0 ) {
      return 2;
    }
    if ( phoneNumberRight < 0 ) {
      return -2;
    }
    if ( phoneNumberLeft < phoneNumberRight ) {
      return 1;
    }
    return -1;
  }

  private boolean mergeXipmeLinks( RecordGateway recordGatewayA ,
      RecordGateway recordGatewayB ) {
    boolean result = false;
    if ( recordGatewayA == null ) {
      return result;
    }
    if ( recordGatewayB == null ) {
      return result;
    }
    try {
      Map mapXipmeLinksA = recordGatewayA.mapXipmeLinks();
      Map mapXipmeLinksB = recordGatewayB.mapXipmeLinks();
      List listXipmeLinksB = new ArrayList( mapXipmeLinksB.values() );
      Iterator iterXipmeLinksB = listXipmeLinksB.iterator();
      while ( iterXipmeLinksB.hasNext() ) {
        XipmeLink xipmeLinkB = (XipmeLink) iterXipmeLinksB.next();
        if ( xipmeLinkB == null ) {
          continue;
        }
        XipmeLink xipmeLinkA = (XipmeLink) mapXipmeLinksA.get( xipmeLinkB
            .getCode() );
        if ( xipmeLinkA == null ) {
          mapXipmeLinksA.put( xipmeLinkB.getCode() , xipmeLinkB );
          continue;
        }
        xipmeLinkA.setHit( xipmeLinkA.getHit() + xipmeLinkB.getHit() );
      }
      result = true;
    } catch ( Exception e ) {
    }
    return result;
  }

}
