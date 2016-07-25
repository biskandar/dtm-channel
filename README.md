# dtm-channel
Directtomobile Channel Module

v1.0.69

- Make sure alert email message can be composed outside , otherwise no hard code text .

- Add new library :
  . router2beepcast-v1.0.43.jar
  . transaction2beepcast-v1.1.38.jar
  . provider2beepcast-v2.2.31.jar
  . client2beepcast-v2.3.08.jar
  . subscriber2beepcast-v1.2.40.jar
  . beepcast_loadmanagement-v1.2.05.jar
  . beepcast_session-v1.0.03.jar
  . beepcast_database-v1.2.02.jar
  . beepcast_properties-v2.0.01.jar
  . http-v1.0.02.jar
  . 

v1.0.68

- Add channel management task to clean channel log simulation as well

- Add field message id at channel log table

    ALTER TABLE `channel_log` 
      ADD COLUMN `message_id` VARCHAR(45) AFTER `marked` ;

    ALTER TABLE `channel_log_simulation` 
      ADD COLUMN `message_id` VARCHAR(45) AFTER `marked` ;

- Add new library :
  . 

v1.0.67

- Found bug that the broadcast calculation for the debit is still wrong for qr image producer

    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport Defined event : id = 186 , name = Test Outgoing Customized QR Event 05
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [ChannelSession-189] [List-147] [Event-186] 
      Resolved first message content : http:\/\/xip.me:8883\/35Ie7
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [ChannelSession-189] [List-147] [Event-186] 
      Resolved first message type : SMS_TEXT
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport Defined total sms = 1 message(s)
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [List-147] Calculating total debit amount from subscribed numbers , with : clientId = 1 , groupSubscriberId = 147 , allowDuplicated = false , useDateSend = false
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [List-147] Getting map country code total subscribed unique numbers
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [List-147] Read map of countryCode of totalNumbers ( 1 ) : {SG=99938}
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [ChannelSession-189] [List-147] [Event-186] Calculated debitAmountPerMessage = 99938.0
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ChannelSessionReportSupport [ChannelSession-189] [List-147] [Event-186] Calculated total debit amount units : 99938.0 + 9993.800000000001
    DEBUG   {Http[Rmt-192.168.0.111][Usr-benny]}ListChannelSessionInfoService [ChannelSession-189] Generating status , found clientId = 1 , paymentType = 1 , totalDebitAmount = 109931.8 unit(s)

- Found bug that the broadcast is never stop after all number processed ( with no response message )

    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Processed channel log , for phone = +6500000349 , channelCode = ADADADIS , dateSend = null , resultCode = PROCESS_SUCCEED , take = 67 ms
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Finished process all subscriber(s) data in the list , creating the next list of subscriber number(s)
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Found all subscriber(s) are processed already , updated : csBean.totalRemains : 1000 -> 0 , csBean.debitUnits : 910.0 -> 0.0
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Found no more available records to be process , started the finisher agent , takes ( 5 ms )
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Successfully run the broadcast
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Xipme buffer stopped
    DEBUG   {Thread-6}XipmeBuffer [ChannelSession-3] All workers are stopping
    DEBUG   {Thread-6}XipmeBufferWriter [ChannelSession-3] Thread stopping
    DEBUG   {Thread-6}XipmeBufferWorker [ChannelSession-3] Thread stopping
    DEBUG   {Thread-6}XipmeBufferWorker [ChannelSession-3] Thread stopping
    DEBUG   {Thread-6}XipmeBufferManager [ChannelSession-3] Thread stopping
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-3] Thread finished ( takes 229306 ms )
    DEBUG   {XipmeBufferManager}XipmeBufferManager [ChannelSession-3] Thread stopped.
    DEBUG   {XipmeBufferWorker.0}XipmeBufferWorker [ChannelSession-3] Thread stopped.
    DEBUG   {XipmeBufferWorker.1}XipmeBufferWorker [ChannelSession-3] Thread stopped.

- Found bug no need to do the finisher ( drop message checker ) if there is no response out 

    DEBUG   {Thread-8}ChannelAgentFinisher [ChannelSession-2] Read 0 record(s) still remains in the send buffer table
    DEBUG   {Thread-8}ChannelAgentFinisher [ChannelSession-2] Found empty send messages stopped the waiting process here
    DEBUG   {Thread-8}ChannelAgentFinisher [ChannelSession-2] Found total 0 send message(s) remains in the send buffer table
    DEBUG   {Thread-8}ChannelAgentFinisher [ChannelSession-2] Observing drop messages with timeout : 15 minutes , 3 times

- Found bug when found empty message for the broadcast , it will suspend the broadcast .

    DEBUG   {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSession [ChannelSession-2] Validating schedule date time : 2015-10-07 10:20:00 to null
    DEBUG   {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSession [ChannelSession-2] Reserving client prepaid balance , trying to update status schedule channel session
    DEBUG   {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSessionReportSupport Defined event : id = 165 , name = Test Outgoing Customized QR Event 01
    WARNING {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSessionReportSupport [ChannelSession-2] [List-152] [Event-165] Failed to calculate total debit amount , found zero message count
    DEBUG   {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSession No need to reserve balance , found zero total debit units
    DEBUG   {Http[Rmt-127.0.0.1][Usr-benny]}ChannelSession [ChannelSession-2] Updating status schedule channel session

    DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Running the broadcast with burstSize = 2 pipe(s) , maxLoad = 35 msg/sec
    DEBUG   {Thread-6}ChannelSessionDAO Perform UPDATE channel_session SET total_remains = 1000 WHERE ( id = 2 ) 
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Updated total number remains = 1000 number(s)
    DEBUG   {Thread-6}XipmeBufferWriter [ChannelSession-2] Trying to update record offset if possible , with : recordOffset = 1000 , channelAgentTotalRemains = 1000 , xipmeBufferTotalRemains = 0 , sizeQueueChannelLogBeans = 0 , threadActiveWriter = false
    DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Found a new list of subscriber(s) ready to process , reset the counter to begin
    WARNING {Thread-6}BasicBillingEngine Failed to get account in the list
    DEBUG   {Thread-6}PersistenceBillingEngine [Account-EPE-165] Failed to found client account entity from table , trying to create new one with : paymentType = PREPAID , billingCmd = GET_BALANCE , unit = null
    DEBUG   {Thread-6}PersistenceBillingEngine [Account-EPE-165] Found null balance , set into default value = 0.0
    WARNING {Thread-6}PersistenceBillingEngine [Account-EPE-165] Failed to create first account , found invalid billing command = GET_BALANCE
    WARNING {Thread-6}PersistenceBillingEngine [Account-EPE-165] Failed to persist account , failed to generate new account
    DEBUG   {Thread-6}ChannelSessionDAO Perform UPDATE channel_session SET suspended = 1 , date_suspended = NOW() WHERE ( id = 2 ) 
    WARNING {Thread-6}ChannelAgent [ChannelSession-2] Suspend the running broadcast , failed to get event prepaid balance

- Resolve xipme link into buffer for the next step response message as well

- Add feature to include attach files when generate broadcast report summary

- Use database server mysql 5.6 port 3307

- Migrate all the existing projects to use IDE MyEclipse Pro 2014
  . Build as java application ( not as web project )

- Use jdk 1.7
  
- Add new library :

  . stax-api-1.0.1.jar
  . stax-1.2.0.jar
  . jettison-1.2.jar
  . xpp3_min-1.1.4c.jar
  . jdom-1.1.3.jar
  . dom4j-1.6.1.jar
  . xom-1.1.jar
  . cglib-nodep-2.2.jar
  . xmlpull-1.1.3.1.jar
  . xstream-1.4.8.jar

  . commons-logging-1.2.jar
  . commons-pool2-2.4.2.jar
  . commons-dbcp2-2.1.1.jar
  . mysql-connector-java-5.1.35-bin.jar
  . beepcast_database-v1.2.00.jar
  
  . beepcast_clientapi-v1.0.04.jar
  . xipme_api-v1.0.29.jar
  . model2beepcast-v1.0.82.jar
  . router2beepcast-v1.0.40.jar
  . lookup2beepcast-v1.0.00.jar
  . provider2beepcast-v2.2.28.jar
  . transaction2beepcast-v1.1.34.jar

v1.0.66

- Add parameter to know the report file is generated or not,
    at class ChannelSessionReportInfoService

- Add new library :
  . xipme_api-v1.0.27.jar
  . router2beepcast-v1.0.37.jar
  . transaction2beepcast-v1.1.31.jar
  . provider2beepcast-v2.2.25.jar
  . client_request2beepcast-v1.0.05.jar
  . model2beepcast-v1.0.80.jar
  . subscriber2beepcast-v1.2.36.jar
  . dwh_model-v1.0.33.jar
  . beepcast_keyword-v1.0.07.jar
  . beepcast_dbmanager-v1.1.36.jar
  . beepcast_onm-v1.2.09.jar
  . C:/benny.iskandar/program/rhino1_7R4/js.jar
  . C:/benny.iskandar/project/beepcast_commonlib/http/jar/http-v1.0.00.jar

v1.0.65

- Detail broadcast report total submitted shall include the refresh ,
  there is possibility retry message send later ( after the broadcast )

- Add new library :
  . provider2beepcast-v2.2.24.jar
  . router2beepcast-v1.0.36.jar
  . model2beepcast-v1.0.76.jar
  . transaction2beepcast-v1.1.28.jar

v1.0.64

- There are change inside file ./conf/oproperties.xml

      <property field="Channel.DBReader.RestartRunningBroadcastsAfterBoot" value="true"
        description="Restart all the running broadcasts after boot" />
      <property field="Channel.DBReader.SleepTime" value="10000"
        description="Sleep interval between the call process to execute" />
      <property field="Channel.DBReader.EnableBroadcastStatusProcessor" value="true"
        description="Enable to process broadcast with new status" />
      <property field="Channel.DBReader.EnableBroadcastSimulationStatusProcessor" value="true"
        description="Enable to process broadcast simulation with new status" />

- Send notification email for scheduled broadcast time that has been reached,
  but no activation.
  
- Add new library :
  . provider2beepcast-v2.2.23.jar
  . model2beepcast-v1.0.75.jar
  . dwh_model-v1.0.32.jar
  . 

v1.0.63

- Do not show all the running broadcast in the traffic broadcast summary
  report 
  
- Add new library :
  . router2beepcast-v1.0.35.jar
  . transaction2beepcast-v1.1.27.jar
  . provider2beepcast-v2.2.22.jar
  . subscriber2beepcast-v1.2.35.jar
  . dwh_model-v1.0.31.jar
  . beepcast_dbmanager-v1.1.34.jar
  . 

v1.0.62

- Found bug display total clicks was zero in 
    the broadcast report but when download the details found many .

- Found bug suspend broadcast when found 
    un-reserved client balance becoz finished and 
    found drop messages need to retry

- Found bug that master / super can not view the member running broadcast

- Add new library :
  . xipme_api-v1.0.26.jar
  . transaction2beepcast-v1.1.26.jar
  . provider2beepcast-v2.2.21.jar
  . model2beepcast-v1.0.74.jar

v1.0.61

- Additional "xipmeCloneParam" param added if found any

- Add new library :
  . transaction2beepcast-v1.1.25.jar
  . router2beepcast-v1.0.34.jar
  . provider2beepcast-v2.2.20.jar
  . client_request2beepcast-v1.0.04.jar
  . client2beepcast-v2.3.06.jar
  . model2beepcast-v1.0.73.jar
  . beepcast_dbmanager-v1.1.33.jar
  . beepcast_encrypt-v1.0.04.jar

v1.0.60

- Feature to calculate channel_session_xipme 

- Feature to calculate the total xipme visitor in the broadcast detail report file

- Add new library :
  . xipme_api-v1.0.25.jar

v1.0.59

- There is a change in file ./conf/channel.xml

    <property name="workerSize" value="5" />

- There is a bug of the log shown below :

    beepadmin.log	1902	04.07.2014 15.42.35:487 1893 WARNING {ChannelManagementReportUpdaterThread}FetchBase 
      Found delay ( take 1060 ms ) to perform query object with the cache , sql = SELECT id , field_name , cur_value , min_value , max_value , description , active FROM global_properties WHERE ( field_name = 'Channel.UpdateBroadcastReport.IntervalMillisReports' ) AND ( active = 1 )ORDER BY id DESC LIMIT 1
    beepadmin.log	1908	04.07.2014 15.42.54:411 1899 WARNING {ChannelManagementReportUpdaterThread}FetchBase 
      Found delay ( take 1017 ms ) to perform query object with the cache , sql = SELECT id , field_name , cur_value , min_value , max_value , description , active FROM global_properties WHERE ( field_name = 'Channel.UpdateBroadcastReport.IntervalMillisReports' ) AND ( active = 1 )ORDER BY id DESC LIMIT 1
    beepadmin.log	1920	04.07.2014 15.43.44:914 1911 WARNING {ChannelManagementReportUpdaterThread}FetchBase 
      Found delay ( take 1265 ms ) to perform query object with the cache , sql = SELECT id , field_name , cur_value , min_value , max_value , description , active FROM global_properties WHERE ( field_name = 'Channel.UpdateBroadcastReport.IntervalMillisReports' ) AND ( active = 1 )ORDER BY id DESC LIMIT 1

- Add feature to refresh broadcast when found the total current sent
  is zero

- Change the queue in the channelManagementReportUpdater to use
  linkedQueue so that it can track the message that has already 
  has the same channelSessionId 

- There is a change in file ./conf/oproperties.xml

      <property field="Channel.Management.SleepInMillis" value="15000"
        description="Channel management interval in n millis" />

      <property field="Channel.UpdateBroadcastReport.AfterInMinutes" value="15"
        description="Refresh broadcast report after generated in n minutes" />

- Put guardian to not refresh broadcast report when found 
  empty record in the gateway log and/or channel log
  
- Execute sql below :

    ALTER TABLE `transaction` 
      ADD INDEX `channel_session_id_encrypt_destination_address`(`channel_session_id`, `encrypt_destination_address`) ;

- Put a guardian on the external request to refresh broadcast report 
  it will not refresh the report when if found as an old broadcast 
    and the records is already move to dwh transaction

- Add new library :
  . xipme_api-v1.0.24.jar
  . transaction2beepcast-v1.1.22.jar
  . subscriber2beepcast-v1.2.34.jar
  . dwh_model-v1.0.30.jar
  . beepcast_online_properties-v1.0.05.jar
  . beepcast_dbmanager-v1.1.32.jar
  . 

v1.0.58

- Remove wild cards character for file name report

    // clean characters
    sgName = sgName.trim();
    sgName = sgName.replaceAll( "[^a-zA-Z0-9]" , "_" );
    
- Add new library :
  . 

v1.0.57

- change the report download file name , like below :

    Broadcast Summary Report : broadcast-summary-report-YYYMMDD.HHMMSS-ZXCVB.zip
    Broadcast Details Report : broadcast-details-report-event_name-YYYMMDD.HHMMSS-ZXCVB.zip

- add feature inside the finisher to track drop messages on the send buffer instead

- add new library :
  . router2beepcast-v1.0.33.jar
  . transaction2beepcast-v1.1.21.jar
  . provider2beepcast-v2.2.17.jar
  . client_request2beepcast-v1.0.03.jar
  . client2beepcast-v2.3.05.jar
  . model2beepcast-v1.0.70.jar
  . subscriber2beepcast-v1.2.33.jar
  . beepcast_loadmanagement-v1.2.04.jar
  . beepcast_session-v1.0.02.jar
  . beepcast_encrypt-v1.0.03.jar
  . beepcast_onm-v1.2.08.jar
  

v1.0.56

- support duplicated numbers to send during the simulation broadcast

- send alert with new onm app library

- Add new library :
  . transaction2beepcast-v1.1.20.jar
  . provider2beepcast-v2.2.16.jar
  . client2beepcast-v2.3.04.jar
  . model2beepcast-v1.0.68.jar
  . beepcast_encrypt-v1.0.02.jar
  . beepcast_onm-v1.2.07.jar

v1.0.55

- Where report summary is so slow to generate :

    ALTER TABLE `channel_session_xipme` 
      ADD INDEX `channel_session_id_xipme_code`(`channel_session_id`, `xipme_code`) ;

- Build report broadcast details summary with xipme link information , like sql below :

    SELECT gl.log_id , gl.message_id , gl.gateway_xipme_id , gx.xipme_code , gxh.date_hit
    FROM gateway_log gl
      LEFT OUTER JOIN gateway_xipme gx ON gx.gateway_xipme_id = gl.gateway_xipme_id
      LEFT OUTER JOIN gateway_xipme_hit gxh ON ( gxh.gateway_xipme_id = gx.gateway_xipme_id ) 
        AND ( gxh.xipme_code = gx.xipme_code )
    WHERE ( gl.channel_session_id = 67 )
    ORDER BY gl.log_id ASC
    LIMIT 50 ;

- Execute sql below :

    ALTER TABLE `gateway_xipme` 
      DROP INDEX `gateway_xipme_id` ,
      ADD  INDEX `gateway_xipme_id_xipme_code`( `gateway_xipme_id` , `xipme_code` ) ;
    ALTER TABLE `gateway_xipme_hit` 
      DROP INDEX `gateway_xipme_id` ,
      ADD  INDEX `gateway_xipme_id_xipme_code`( `gateway_xipme_id` , `xipme_code` ) ;
 
- Put "None" for the delivery report date in the broadcast summary file

- Take out validation to update total remains periodically

- Put in the global properties to setup the running broadcast like the delay

- Add trap load management during the preparation preparation messages from channel log
  and xipme buffer 

- Add feature to add tracking gateway_log_xipme_id in the xipme buffer

- Add new library :
  . xipme_api-v1.0.23.jar
  . transaction2beepcast-v1.1.16.jar
  . beepcast_xipme-v1.0.00.jar
  . router2beepcast-v1.0.31.jar
  . provider2beepcast-v2.2.15.jar
  . billing2beepcast-v1.1.07.jar
  . model2beepcast-v1.0.66.jar
  . dwh_model-v1.0.29.jar
  . beepcast_keyword-v1.0.06.jar
  . beepcast_online_properties-v1.0.04.jar
  . 

v1.0.54

- Put dnc filter before the broadcast start ( during the store subscriber into channel log )

- Add new library :
  . dwh_model-v1.0.28.jar
  . model2beepcast-v1.0.65.jar
  . subscriber2beepcast-v1.2.27.jar

v1.0.53

- Validate the batch insert and selection channel_log must use encrypt_phone instead of phone

- Execute sql below :

    ALTER TABLE `channel_log` 
      DROP INDEX `event_id` , 
      DROP INDEX `event_id_phone` ;
    
    ALTER TABLE `channel_log` 
      ADD INDEX `event_id_encrypt_phone`( `event_id` , `encrypt_phone` ) ;
    
    ALTER TABLE `gateway_log` 
      ADD INDEX `channel_session_id_encrypt_phone`( `channel_session_id` , `encrypt_phone` ) ;
    
- Create optimized query to select drop messages based on channel_log and gateway_log table .

  SELECT cl.id , gl.log_id
  FROM channel_log cl
    LEFT OUTER JOIN gateway_log gl
      ON ( gl.channel_session_id = cl.channel_session_id ) AND ( gl.encrypt_phone = cl.encrypt_phone )
  WHERE ( cl.event_id = 41 )
  ORDER BY cl.id ASC
      
- Put notification if found drop messages after the broadcast finished 
  to support email .

- Add feature to "re-sent" the drop messages automatically after the broadcast finished .
  Put like an agent worker just to analysis the drop messages and resent .
  
  The logic is use send buffer as loop condition , it will do the wait until there is no records
  left in the send buffer , and done the analysis of the drop and resent the ones ( if found any )

- Add new library :
  . 

v1.0.52

- Put pagination under broadcast summary 

- Found bug wrong to calculate the retry numbers under broadcast summary

- Add new library :
  . router2beepcast-v1.0.30.jar
  . transaction2beepcast-v1.1.14.jar
  . provider2beepcast-v2.2.14.jar
  . model2beepcast-v1.0.63.jar
  . dwh_model-v1.0.27.jar
  . beepcast_onm-v1.2.06.jar

v1.0.51

- Create new table to store all the missing ( drop ) numbers

    ALTER TABLE `channel_log` 
      MODIFY COLUMN `date_tm` DATETIME,
      MODIFY COLUMN `date_subscribed` DATETIME,
      ADD COLUMN `encrypt_phone` varbinary(512) AFTER `phone`,
      ADD COLUMN `channel_session_id` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `encrypt_phone` ;

    ALTER TABLE `channel_log_simulation` 
      MODIFY COLUMN `date_tm` DATETIME,
      MODIFY COLUMN `date_subscribed` DATETIME,
      ADD COLUMN `encrypt_phone` varbinary(512) AFTER `phone`,
      ADD COLUMN `channel_session_id` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `encrypt_phone` ;

    CREATE TABLE `channel_session_subscriber` (
      `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
      `channel_session_id` INTEGER UNSIGNED NOT NULL DEFAULT 0,
      `type` VARCHAR(45) NOT NULL,
      `phone` VARCHAR(256) NOT NULL,
      `encrypt_phone` varbinary(512) default NULL,
      `active` BOOLEAN NOT NULL DEFAULT 0,
      `date_inserted` DATETIME NOT NULL,
      `date_updated` DATETIME NOT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE = InnoDB ;
    
    ALTER TABLE `channel_session_subscriber` ADD INDEX `channel_session_id`(`channel_session_id`) ;

- Add new field "ReportCreated" in the list broadcast summary download report file

- Add new field "ListSize" in the list broadcast summary download report file

- Add new library :
  . router2beepcast-v1.0.28.jar
  . transaction2beepcast-v1.1.12.jar
  . provider2beepcast-v2.2.12.jar
  . client2beepcast-v2.3.02.jar
  . model2beepcast-v1.0.62.jar
  . subscriber2beepcast-v1.2.25.jar
  . beepcast_keyword-v1.0.04.jar
  . beepcast_dbmanager-v1.1.31.jar
  . beepcast_onm-v1.2.04.jar

v1.0.50

- Add new table to capture broadcast xipme history

  CREATE TABLE `channel_session_xipme` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `channel_session_id` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `xipme_code` VARCHAR(20) NOT NULL,
    `total_members` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_members_members` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_customers` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_visitors` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_members_visitors` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_bot_visitors` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_members_bot_visitors` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_hits` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_members_hits` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_bot_hits` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `total_members_bot_hits` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `date_inserted` DATETIME NOT NULL,
    `date_updated` DATETIME NOT NULL,
    PRIMARY KEY (`id`)
  ) ENGINE = InnoDB ;

- Add new library :
  . xipme_api-v1.0.22.jar
  . router2beepcast-v1.0.27.jar
  . transaction2beepcast-v1.1.08.jar
  . provider2beepcast-v2.2.10.jar
  . client_request2beepcast-v1.0.02.jar
  . client2beepcast-v2.3.00.jar
  . model2beepcast-v1.0.60.jar
  . subscriber2beepcast-v1.2.21.jar
  . dwh_model-v1.0.25.jar
  . beepcast_online_properties-v1.0.03.jar
  . beepcast_dbmanager-v1.1.30.jar
  . beepcast_onm-v1.2.02.jar
  

v1.0.49

- Take out the suspended event on the availabe list event for setup broadcast

- Put bypass suspended event when initiate the broadcast , 
  suspend broadcast will handled in the menu suspend link .

- Add new library :
  . transaction2beepcast-v1.1.05.jar

v1.0.48

- Put list name in alert email broadcase status message

- Found bug on alert email message date finished : null

- add new library :
  . router2beepcast-v1.0.26.jar
  . transaction2beepcast-v1.1.04.jar
  . provider2beepcast-v2.2.07.jar
  . client2beepcast-v2.2.15.jar
  . model2beepcast-v1.0.59.jar
  . dwh_model-v1.0.24.jar
  . beepcast_online_properties-v1.0.02.jar
  . beepcast_dbmanager-v1.1.27.jar
  . beepcast_onm-v1.2.01.jar

v1.0.47

- Need to compare and log the codes request and response

- After stop the thread , xipme buffer show log the rest of the codes that been not used

- Found bugs :

    WARNING {XipmeBufferManager}XipmeBufferManager Failed to take out all the rest msg(s) from buffer , java.util.ConcurrentModificationException
    DEBUG   {XipmeBufferManager}XipmeBufferManager Take out all the rest 1 msg(s) from buffer : 370561={BHd1=sGHc},

- XipmeBuffer worker always run ahead Broadcast worker for duplicated xipme code issue

  . There is some issue on the worker side , some of the codes don't fetch successfully

- There is a changed inside file ./conf/oproperties.xml

      <property field="Channel.XipmeBuffer.managerLatency" value="60000" 
        description="Xipme buffer manager latency in millis" />
      <property field="Channel.XipmeBuffer.writerLoopAfterFinish" value="false" 
        description="Xipme buffer writer do the restart after finish" />
      <property field="Channel.XipmeBuffer.synchRemainsThreshold" value="2000" 
        description="Xipme buffer synchronize offset with worker remains reached threshold" />
      <property field="Channel.XipmeBuffer.synchRemainsLimit" value="20000" 
        description="Xipme buffer synchronize offset with worker remains with limit" />
      
- add new library :
  . xipme_api-v1.0.21.jar
  . provider2beepcast-v2.2.06.jar
  . subscriber2beepcast-v1.2.20.jar
  . dwh_model-v1.0.22.jar
  . beepcast_onm-v1.1.10.jar
  . transaction2beepcast-v1.1.03.jar
  . model2beepcast-v1.0.58.jar

v1.0.46

- change the word inactivated to deactivated on broadcast status alert

- add new library :
  . 

v1.0.45

- put notification after the running broadcast is finished

- put event name instead of event id in notification email

- add alert module for active and inactive scheduled broadcast

- strong validation for scheduled broadcast date time before and after

- add new library :
  . 

v1.0.44

- Add feature to download list broadcast summary

- Adapted the api of transaction process

- add new library :
  . router2beepcast-v1.0.25.jar
  . transaction2beepcast-v1.1.00.jar

v1.0.43

- add api to generate broadcast report file name

- change the table inside channel session report 

    ALTER TABLE `channel_session_report` 
      MODIFY COLUMN `key` VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL ;

- add api to download the simulation report 

- there are changes inside channelLogDAO and channelLogSimulationDAO, make simplified for them.

- add simulation table

    ALTER TABLE `channel_log_simulation`
      ADD COLUMN `sender_id` VARCHAR(20) NOT NULL AFTER `country_credit_cost` ,
      ADD COLUMN `message_count` INTEGER UNSIGNED NOT NULL AFTER `sender_id` ;

    ALTER TABLE `channel_session_simulation` 
      ADD COLUMN `simulation_type` VARCHAR(45) NOT NULL AFTER `id`,
      ADD COLUMN `simulation_numbers` BIGINT(10) UNSIGNED NOT NULL DEFAULT '0' AFTER `simulation_type` ;

    CREATE TABLE `channel_session_simulation` (
      `id` int(10) unsigned NOT NULL auto_increment,
      `channel_session_id` int(10) unsigned NOT NULL default '0',
      `total_numbers` bigint(10) unsigned NOT NULL default '0',
      `total_remains` bigint(10) unsigned NOT NULL default '0',
      `debit_units` double NOT NULL default '0',
      `priority` decimal(2,0) NOT NULL default '0',
      `suspended` decimal(1,0) NOT NULL default '0',
      `started` decimal(1,0) NOT NULL default '0',
      `finished` decimal(1,0) NOT NULL default '0',
      `new_status` int(10) unsigned NOT NULL default '0',
      `date_started` datetime default NULL,
      `date_suspended` datetime default NULL,
      `date_finished` datetime default NULL,
      `active` decimal(1,0) NOT NULL default '0',
      `date_inserted` datetime default NULL,
      `date_updated` datetime default NULL,
      PRIMARY KEY  (`id`)
    ) ENGINE=InnoDB ;

    CREATE TABLE `channel_log_simulation` (
      `id` int(11) NOT NULL auto_increment,
      `marked` decimal(1,0) NOT NULL default '0',
      `phone` varchar(20) NOT NULL,
      `event_id` bigint(10) NOT NULL default '0',
      `subscribed` decimal(1,0) NOT NULL default '0',
      `client_subscriber_id` int(10) unsigned default '0',
      `client_subscriber_custom_id` int(10) unsigned default '0',
      `cust_ref_id` varchar(45) default NULL,
      `cust_ref_code` varchar(45) default NULL,
      `description` varchar(100) default NULL,
      `country_code` varchar(45) default NULL,
      `country_credit_cost` double default '0',
      `message_text` text default NULL,
      `date_tm` datetime default NULL,
      `date_send` datetime default NULL,
      `date_subscribed` datetime default NULL,
      `date_marked` datetime default NULL,
      `date_unmarked` datetime default NULL,
      PRIMARY KEY USING BTREE (`id`),
      KEY `event_id` (`event_id`),
      KEY `event_id_phone` (`event_id`,`phone`)
    ) ENGINE=InnoDB ;

- add new library :
  . transaction2beepcast-v1.0.80.jar
  . beepcast_unique_code-v1.0.00.jar
  . beepcast_session-v1.0.01.jar
  . beepcast_keyword-v1.0.03.jar

v1.0.42

- why became slow for the broadcast now , found only one thread .

  {Thread-6}ChannelAgent [ChannelSession-26] Add message param channelLogBean : clId = 1703456 , csId = 440956 , cscId = 440945 , evId = 1903 , phone = +6500019642 , dateSend = null
  {Thread-6}ChannelAgent [ChannelSession-26] Processed channel log , for phone = +6500019642 , channelCode = ADADADGN , dateSend = null , resultCode = PROCESS_SUCCEED , take = 404 ms
  {Thread-6}ChannelAgent [ChannelSession-26] Add message param channelLogBean : clId = 1703457 , csId = 440957 , cscId = 440946 , evId = 1903 , phone = +6500019643 , dateSend = null
  {Thread-6}ChannelAgent [ChannelSession-26] Processed channel log , for phone = +6500019643 , channelCode = ADADADGN , dateSend = null , resultCode = PROCESS_SUCCEED , take = 104 ms
  {Thread-6}ChannelAgent [ChannelSession-26] Add message param channelLogBean : clId = 1703458 , csId = 440958 , cscId = 440947 , evId = 1903 , phone = +6500019644 , dateSend = null
  {Thread-6}ChannelAgent [ChannelSession-26] Processed channel log , for phone = +6500019644 , channelCode = ADADADGN , dateSend = null , resultCode = PROCESS_SUCCEED , take = 96 ms

- need to open monitor for queue size inside transaction processor
  . change name transaction processor to channel processor

- monitor the load traffic inside the channel processor
  
- provide list of events that had performed the broadcast ( history like )

- add list broadcast summary model

- Add new library :
  . transaction2beepcast-v1.0.76.jar
  . provider2beepcast-v2.2.04.jar
  . subscriber2beepcast-v1.2.19.jar
  . client2beepcast-v2.2.13.jar
  . beepcast_database-v1.1.05.jar

v1.0.41

- Put configurable the interval delay between broadcast event report generator

      <property field="Channel.UpdateBroadcastReport.IntervalMillisReports" value="5000"
        description="Interval latency between broadcast report generated in millis" />

- Add message count and message content field under broadcast report

- Add new library :
  . transaction2beepcast-v1.0.75.jar
  . model2beepcast-v1.0.53.jar
  . dwh_model-v1.0.19.jar
  . beepcast_encrypt-v1.0.01.jar

v1.0.40

- Provide an api to query channel log bean based on event id and phone number

- Add new library :
  . router2beepcast-v1.0.23.jar
  . provider2beepcast-v2.2.03.jar
  . model2beepcast-v1.0.52.jar
  . subscriber2beepcast-v1.2.18.jar
  . dwh_model-v1.0.17.jar

v1.0.39

- Restructure inside broadcast table to enable window scheduling

    CREATE TABLE `channel_session_schedule` (
      `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT ,
      `channel_session_id` INTEGER UNSIGNED NOT NULL DEFAULT 0 ,
      `date_started` DATETIME ,
      `date_stopped` DATETIME ,
      `active` BOOLEAN NOT NULL DEFAULT 0 ,
      `date_inserted` DATETIME NOT NULL ,
      `date_updated` DATETIME NOT NULL ,
      PRIMARY KEY (`id`)
    ) ENGINE = InnoDB ;
    
    ALTER TABLE `channel_session_schedule` 
      ADD COLUMN `marked` VARCHAR(45) AFTER `date_stopped` ;

- Add new library :
  . 

v1.0.38

- Put sleep inside the channel agent for awhile when found the changes 
  of existing mapCodes to nothing

- There is a change inside oproperties.xml file

      <property field="Channel.XipmeBuffer.limitRecords" value="1000" 
        description="Xipme buffer limit records per query" />
      <property field="Channel.XipmeBuffer.writerLatency" value="3000" 
        description="Xipme buffer writer latency in millis" />
      <property field="Channel.XipmeBuffer.queueSize" value="5000" 
        description="Xipme buffer queue data capacity" />
      <property field="Channel.XipmeBuffer.queueTimeout" value="5000" 
        description="Xipme buffer queue timeout to read in millis" />
      <property field="Channel.XipmeBuffer.queueThreshold" value="10" 
        description="Xipme buffer how many record per batch" />
      <property field="Channel.XipmeBuffer.workerSize" value="10" 
        description="Xipme buffer how many worker concurrent" />
      <property field="Channel.XipmeBuffer.startTimeout" value="60000" 
        description="Xipme buffer start timeout before the broadcast run" />
      <property field="Channel.XipmeBuffer.mapSize" value="50000" 
        description="Xipme buffer maximum size for the map codes" />

- Change the channel agent queueing system, to use queue instead

- Add new library :
  . xipme_api-v1.0.19.jar
  . transaction2beepcast-v1.0.69.jar

v1.0.37

- Found bugs :

    beepadmin.log	1536	15.05.2012 16.45.30:156 1527 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Channel Agent Thread started
    beepadmin.log	1541	15.05.2012 16.45.30:156 1532 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Running the broadcast with burstSize = 2 pipe(s) , maxLoad = 40 msg/sec
    beepadmin.log	1542	15.05.2012 16.45.30:156 1533 DEBUG   {Thread-6}ChannelLogDAO Perform SELECT id , marked , phone , event_id , subscribed , client_subscriber_id , client_subscriber_custom_id , cust_ref_id , cust_ref_code , description , country_code , country_credit_cost , date_tm , date_subscribed , date_marked , date_unmarked FROM channel_log WHERE ( marked = 1 ) AND ( event_id = 1714 ) AND ( date_send < NOW() ) LIMIT 250
    beepadmin.log	1546	15.05.2012 16.45.30:156 1537 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Found empty scheduled records , pending the broadcast for a while.
    beepadmin.log	1659	15.05.2012 16.46.30:156 1650 WARNING {Thread-6}Throttle Failed to execute the process function , java.lang.NullPointerException
    beepadmin.log	1660	15.05.2012 16.46.30:156 1651 WARNING {Thread-6}ChannelAgent [ChannelSession-40] Failed to run the broadcast , found throttle problem , com.beepcast.util.throttle.ThrottleException: Failed to execute the process function , java.lang.NullPointerException
    beepadmin.log	1661	15.05.2012 16.46.30:156 1652 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Successfully run the broadcast
    beepadmin.log	1662	15.05.2012 16.46.30:156 1653 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Trying to perform unreserve client prepaid balance
    beepadmin.log	1665	15.05.2012 16.46.30:156 1656 DEBUG   {Thread-6}ClientEventReserveBalance [Client-1] Successfully perform unreserve balance from event_prepaid account to client_prepaid account , added = 4.5 unit(s)
    beepadmin.log	1666	15.05.2012 16.46.30:156 1657 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Successfully perform unreserve client prepaid balance
    beepadmin.log	1667	15.05.2012 16.46.30:156 1658 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Cleaning monitor
    beepadmin.log	1668	15.05.2012 16.46.30:156 1659 DEBUG   {Thread-6}ChannelMonitorService [ChannelMonitor-40] Successfully perform cleaned from the map
    beepadmin.log	1669	15.05.2012 16.46.30:156 1660 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Successfully clean monitor
    beepadmin.log	1670	15.05.2012 16.46.30:156 1661 DEBUG   {Thread-6}ChannelAgent [ChannelSession-40] Channel Agent Thread finished

- Start broadcast with date scheduled list

    ALTER TABLE `channel_log` 
      ADD COLUMN `date_send` DATETIME AFTER `date_tm` ; 

    ALTER TABLE `channel_session` 
      ADD COLUMN `use_date_send` BOOLEAN NOT NULL DEFAULT 0 AFTER `allow_duplicated` ; 
      
- Add new library :
  . subscriber2beepcast-v1.2.17.jar
  . throttle-v1.0.01.jar

v1.0.36

- Support broadcast with duplicated numbers

- Add new library :
  . subscriber2beepcast-v1.2.16.jar
  . transaction2beepcast-v1.0.67.jar

v1.0.35

- Optimized query with index 

    ALTER TABLE `client_subscriber` 
      ADD INDEX `subscriber_group_id_encrypt_phone`(`subscriber_group_id`, `encrypt_phone`) ;

- Fix the bug of report broadcast with duplicated numbers

- Add feature to enable broadcast with duplicated numbers

    ALTER TABLE `channel_session` 
      ADD COLUMN `allow_duplicated` BOOLEAN NOT NULL DEFAULT 0 AFTER `group_subscriber_id` ;

- Add new library :
  . subscriber2beepcast-v1.2.15.jar
  . provider2beepcast-v2.2.02.jar

v1.0.34

- Support for multiple work inside transaction worker

- There is a change inside oproperties.xml

      <property field="Channel.Worker.BurstSize" value="2"
        description="Channel worker burst size" />
      <property field="Channel.Worker.MaxLoad" value="20"
        description="Channel worker maximum load" />
      <property field="Channel.Worker.MaxSendBufferRecords" value="1000"
        description="Maximum send buffer records stored" />
      <property field="Channel.Worker.SleepTime" value="15000"
        description="Sleep interval when reached max send buffer records in millis" />
      <property field="Channel.Processor.Concurrent" value="false"
        description="Channel processor enable to perform concurrent" />
      <property field="Channel.Processor.WorkerSize" value="2"
        description="Channel processor total workers" />
      <property field="Channel.Processor.QueueSize" value="10"
        description="Channel processor queue message to feed the workers" />

- There is a changed inside channel.xml

      <agent name="processor" concurrent="false" workerSize="2" queueSize="10" />
      
- Add new library :
  . transaction2beepcast-v1.0.65.jar
  . provider2beepcast-v2.2.01.jar
  . client2beepcast-v2.2.12.jar
  . subscriber2beepcast-v1.2.13.jar
  . beepcast_loadmanagement-v1.2.03.jar
  . beepcast_online_properties-v1.0.01.jar
  . beepcast_onm-v1.1.09.jar
  . dwh_model-v1.0.15.jar

v1.0.33

- Found bug 

    16.02.2012 13.21.27:984 1072 DEBUG   {ChannelManagementThread}ChannelSubscriberDAO Perform SELECT COUNT(id) AS total FROM client_subscriber WHERE ( active = 1 ) AND ( subscribed = 0 ) AND ( subscriber_event_id = 1712 ) 
    16.02.2012 13.21.27:984 1073 ERROR   {ChannelManagementThread}DatabaseLibrary [profiledb] Database query failed , com.mysql.jdbc.exceptions.MySQLSyntaxErrorException: Unknown column 'subscriber_event_id' in 'where clause'
    16.02.2012 13.21.27:984 1074 WARNING {ChannelManagementThread}ChannelManagement Failed to do channel management thread , java.lang.NullPointerException

- calculate summary total unsubs based on related event

- add new library :
  . 

v1.0.32

- ...

- add new library :
  . router2beepcast-v1.0.21.jar
  . transaction2beepcast-v1.0.63.jar
  . client2beepcast-v2.2.11.jar
  . billing2beepcast-v1.1.05.jar
  . model2beepcast-v1.0.51.jar
  . subscriber2beepcast-v1.2.11.jar
  . 

v1.0.31

- add function to list all running and scheduled broadcast across all clients

- add function to display datetime with gmt information

- add new library :
  . router2beepcast-v1.0.20.jar
  . transaction2beepcast-v1.0.56.jar
  . provider2beepcast-v2.2.00.jar
  . model2beepcast-v1.0.46.jar
  . subscriber2beepcast-v1.2.07.jar
  . beepcast_onm-v1.1.08.jar
  . beepcast_dbmanager-v1.1.26.jar

v1.0.30

- change broadcast percentage info display into double and string format

- add new library :
  . router2beepcast-v1.0.19.jar
  . provider2beepcast-v2.1.25.jar

v1.0.29

- found bug at broadcast report , retry has negatif value 

- add new library :
  . transaction2beepcast-v1.0.55.jar
  . provider2beepcast-v2.1.24.jar
  . model2beepcast-v1.0.45.jar
  . dwh_model-v1.0.12.jar
  . beepcast_dbmanager-v1.1.25.jar

v1.0.28

- change the report calculation :
  . total submitted is the total unique number in the broadcast
  . total retry is total retry transaction only ( not include the first attempt )
    total retry is total submitted minus total numbers

- add new library :
  . transaction2beepcast-v1.0.54.jar
  . model2beepcast-v1.0.44.jar
  . beepcast_dbmanager-v1.1.24.jar

v1.0.27

- restructure channel log bean , model , and service

- store client_subscriber_id and client_subscriber_custom_id on the channel log
  before do the broadcast

- Add new library :
  . router2beepcast-v1.0.18.jar
  . transaction2beepcast-v1.0.53.jar
  . provider2beepcast-v2.1.23.jar
  . client2beepcast-v2.2.09.jar
  . billing2beepcast-v1.1.04.jar
  . model2beepcast-v1.0.43.jar
  . subscriber2beepcast-v1.2.05.jar
  . dwh_model-v1.0.11.jar
  . beepcast_dbmanager-v1.1.23.jar
  . beepcast_onm-v1.1.06.jar

v1.0.26

- add validation before client perform to start the broadcast :
  . check the campaign concurrent restriction
  . apply maximum load traffic for campaign 
  . put validation during the running broadcast also , to protect from scheduled one ....  
  
- Add new library :
  . 

v1.0.25

- Takeout prefix '+' of phone number inside broadcast report 

- Will used client_subscriber's encrypt_phone to generate broadcast report
- Will store number from client_subscriber's encrypt_phone into channel_log table before starting the broadcast
- Cleaned SendBufferDAO.java
- Fixed ChannelLogDAO.java
- Fixed ChannelManagementReportDAO.java

- Add new library :
  . transaction2beepcast-v1.0.46.jar
  . provider2beepcast-v2.1.22.jar
  . client2beepcast-v2.2.04.jar
  . model2beepcast-v1.0.40.jar
  . subscriber2beepcast-v1.2.02.jar
  . beepcast_dbmanager-v1.1.20.jar
  . beepcast_encrypt-v1.0.00.jar
  . bcprov-jdk14-145.jar

v1.0.24

- Digout the event to calculate how many messages will sent ,
  from this will decided for how many reserved credit units .
  
- Add new library :
  . router2beepcast-v1.0.15.jar
  . transaction2beepcast-v1.0.45.jar
  . provider2beepcast-v2.1.21.jar
  . client_request2beepcast-v1.0.01.jar
  . client2beepcast-v2.2.03.jar
  . model2beepcast-v1.0.37.jar
  . beepcast_dbmanager-v1.1.19.jar
  . model2beepcast-v1.0.38.jar

v1.0.23

- Use the new structure for sms process execution .

- Add new library :
  . transaction2beepcast-v1.0.43.jar
  . router2beepcast-v1.0.14.jar

v1.0.22

- Add channel api :
  . feature to query channel session bean based on id

- Add new library :
  . router2beepcast-v1.0.12.jar
  . transaction2beepcast-v1.0.37.jar
  . provider2beepcast-v2.1.20.jar
  . client2beepcast-v2.2.02.jar
  . model2beepcast-v1.0.36.jar
  . subscriber2beepcast-v1.1.04.jar
  . beepcast_dbmanager-v1.1.16.jar

v1.0.21

- Add integration with event bean , service , and dao

- Add new library :
  . model2beepcast-v1.0.31.jar
  . transaction2beepcast-v1.0.33.jar
  . provider2beepcast-v2.1.19.jar
  . client2beepcast-v2.2.01.jar
  . subscriber2beepcast-v1.1.02.jar
  . beepcast_dbmanager-v1.1.15.jar
  . dwh_model-v1.0.09.jar

v1.0.20

- Fix bugs : total reserved unit is still used old calculation 

  20.05.2010 10.53.06:960 917 DEBUG   {HttpProcessor[8080][4]}ChannelSessionCommon [ChannelSession-9] Calculate total debit unit , with totalNumbers = 131 , totalRemains = 131 , percentageExpectedResponse = 10.0
  20.05.2010 10.53.06:960 918 DEBUG   {HttpProcessor[8080][4]}ChannelSessionService Trying to reserve client prepaid balance , total 144 unit(s)

- bypass client band discount parameter for calculating total debit amount

- Store total debit unit inside channel session table , and restructure the model , bean and service .

  This will help to query the latest total debit unit and because there is a background process , 
  it will query faster .

    ALTER TABLE `channel_session` 
      ADD COLUMN `debit_units` DOUBLE NOT NULL DEFAULT 0 AFTER `total_remains` ;
  
- Fix bugs : the balance is not enough to do the broadcast but it still allow to perform broadcast ....

- Fix bugs , this error will perform after finishing the broadcast session .

  18.05.2010 16.51.51:953 37959 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Processed channel log , for phone = +6200000100 , channelCode = ADADADXU , channelSessionId = 2 , resultCode = PROCESS_SUCCEED , take = 0 ms
  18.05.2010 16.51.51:953 37960 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Finished process all subscriber(s) data in the list, creating the next list of subscriber number(s)
  18.05.2010 16.51.51:953 37961 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Calculated last total remains = 0 number(s)
  18.05.2010 16.51.51:953 37962 DEBUG   {Thread-6}ChannelLogDAO Perform SELECT id , marked , phone , event_id , subscribed , cust_ref_id , cust_ref_code , description , date_tm , date_subscribed , date_marked , date_unmarked FROM channel_log WHERE ( marked = 1 ) AND ( event_id = 1841 ) LIMIT 250 
  18.05.2010 16.51.51:968 37963 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Found all subscriber(s) sent already , trying to finish the channel agent process
  18.05.2010 16.51.51:968 37964 DEBUG   {Thread-6}ChannelSessionDAO Perform UPDATE channel_session SET suspended = 0 , finished = 1 , date_finished = 2010-05-18 16:51:51 , total_remains = 0 WHERE ( id = 2 ) 
  18.05.2010 16.51.51:968 37967 ERROR   {Thread-6}DatabaseLibrary [profiledb] Database execute query failed , com.mysql.jdbc.exceptions.MySQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '16:51:51 , total_remains = 0 WHERE ( id = 2 )' at line 1
  18.05.2010 16.51.51:968 37968 WARNING {Thread-6}ChannelAgent [ChannelSession-2] Failed to update flag finished in channel session table
  18.05.2010 16.51.51:968 37969 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Re update date inserted in the broadcast report table
  18.05.2010 16.51.51:968 37970 DEBUG   {Thread-6}ChannelSessionReportDAO Perform UPDATE channel_session_report SET date_inserted = '2010-05-18 16:51:51' WHERE channel_session_id = 2 
  18.05.2010 16.51.51:968 37971 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Successfully run the broadcast
  18.05.2010 16.51.51:968 37972 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Trying to perform unreserve client prepaid balance
  18.05.2010 16.51.51:968 37973 DEBUG   {Thread-6}ClientEventReserveBalance [Client-1] Successfully perform unreserve balance from event_prepaid account to client_prepaid account , added = 79.00000000000051 unit(s)
  18.05.2010 16.51.51:968 37974 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Successfully perform unreserve client prepaid balance
  18.05.2010 16.51.51:968 37975 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Cleaning monitor
  18.05.2010 16.51.51:968 37976 DEBUG   {Thread-6}ChannelMonitorService [ChannelMonitor-2] Successfully perform cleaned from the map
  18.05.2010 16.51.51:968 37977 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Successfully clean monitor
  18.05.2010 16.51.51:968 37978 DEBUG   {Thread-6}ChannelAgent [ChannelSession-2] Channel Agent Thread finished

- put new field inside channel_session table , to inform how many credit will be deduct ( to be continued ... )

- change the mechanism for calculating total reserved units for broadcast .
  . apply country credit cost depend on which number belong to ( to be continued ... ).

- change the channel_log table structure :

  . add new field credit_cost , this field will be used to calculate total debit amount .

    ALTER TABLE `channel_log`
      ADD COLUMN `country_code` VARCHAR(45) AFTER `description` ;
    
    ALTER TABLE `channel_log`
      ADD COLUMN `country_credit_cost` DOUBLE DEFAULT 0.0 AFTER `country_code` ;
      
- after store channel_log table , add country profile parameter inside

  . use default credit cost from online global properties : CountryProfile.CreditCost
  . re-update credit cost based on client support countries list
  
- add new package com.beepcast.channel.view.*

  this package will handle all broadcast ui : bean and control  
  
- add new library :
  . beepcast_onm-v1.1.03.jar
  . beepcast_dbmanager-v1.1.11.jar
  . model2beepcast-v1.0.28.jar
  . client2beepcast-v2.1.08.jar
  . client_request2beepcast-v1.0.00.jar
  . provider2beepcast-v2.1.18.jar
  . router2beepcast-v1.0.10.jar  
  . subscriber2beepcast-v1.1.01.jar

v1.0.19

- change the email alert title , with below content :
  . the broadcast was started
  . the broadcast has finished
  . the broadcast was suspended

- add new library : 
  . transaction2beepcast-v1.0.24.jar
  . model2beepcast-v1.0.26.jar

v1.0.18

- Change the way to create a broadcast report , not to update anythings after n days limitation .

- Add online configurable for update broadcast report parameters

- Change inside channel.xml file configuration , to setup the limit records for clean channel log task

v1.0.17

- Found a bugs , because the traffic inside gateway log can be retry able 
  so the way of create broadcast must be changed :

- Change the broadcast report format :
  . Change value on/off became true/false
  . Change field SubscribedDate became UnsubscribedDate
  . Change field GlobalSubscribedDate became GlobalUnsubscribedDate

- add new library :
  . transaction2beepcast-v1.0.23.jar
  . billing2beepcast-v1.1.03.jar
  . model2beepcast-v1.0.24.jar
  . subscriber2beepcast-v1.0.02.jar
  
v1.0.16

- Before do the broadcast start , add feature to exclude the numbers from invalid list .

- add new library :
  . router2beepcast-v1.0.07.jar
  . transaction2beepcast-v1.0.22.jar
  . model2beepcast-v1.0.23.jar
  . subscriber2beepcast-v1.0.01.jar
  . dwh_model-v1.0.08.jar

v1.0.15

- Add total retry in the broadcast history :

  . execute sql below :
  
    ALTER TABLE `channel_session_report`
      ADD COLUMN `total_retry` BIGINT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `total_sent` ;

v1.0.14

- use and integrate with the new subscriber module

- add new library :
  . client2beepcast-v2.1.06.jar
  . router2beepcast-v1.0.05.jar
  . transaction2beepcast-v1.0.20.jar
  . subscriber2beepcast-v1.0.00.jar
  . model2beepcast-v1.0.19.jar

v1.0.13

- Add new field in the list group subscriber named : totalNumber , totalSubscribed

  . execute sql below :
  
    ALTER TABLE `subscriber_group` 
      DROP COLUMN `total_invalid_numbers` ,
      DROP COLUMN `file_invalid_numbers` ;

    CREATE TABLE `subscriber_group_report` (
      `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
      `subscriber_group_id` INTEGER UNSIGNED NOT NULL DEFAULT 0,
      `total_uploaded` BIGINT UNSIGNED NOT NULL DEFAULT 0,
      `total_subscribed` BIGINT UNSIGNED NOT NULL DEFAULT 0,
      `total_invalid_number` BIGINT UNSIGNED NOT NULL DEFAULT 0,
      `active` BOOLEAN NOT NULL DEFAULT 0,
      `date_inserted` DATETIME NOT NULL,
      `date_updated` DATETIME NOT NULL,
      PRIMARY KEY (`id`)
    )
    ENGINE = InnoDB ;

v1.0.12

- Add field to store global unsubscribed information :

    ALTER TABLE `channel_session_report` 
      ADD COLUMN `total_global_unsubs` BIGINT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `total_unsubs` ;

  Update the model for generating report    
      
- fixed the unsubscribed field in the broadcast management report

- add new library :
  . model2beepcast-v1.0.17.jar

v1.0.11

- Fixed channel monitor module to init with the load value

- Detail the info value until minutes , when the remains use hour or days

- Found bugs that the time remains is not accurate , 
  find out the solutions , so far may be because of the remain threshold is 100 record(s) ,
  we need to increase to 1000 record(s) or use percentaged .... ( to be continued ) 

v1.0.10

- There is a changed inside the channel.xml file :

    <task name="updateChannelSessionReport" enabled="true">
    
      <property name="lastDays" value="3" />
      
      <property name="expiryDays" value="60" />
      
      <property name="downloadPath" value="${tomcat.webapps}/report/file/" />
      
    </task>
    
    <task name="cleanChanneLog" enabled="true">
    
      <property name="expiryDays" value="180" />
    
    </task>

- Add management task to perform clean expiry record in the channel log table ,
  the system shall provide the configuration of expiry value ( channel.xml )

- the clean channel log task will be done when there is no broadcast running ,
  so not to harm the channel log table .  
  
- There is expiry days for update the channel session report  
    
- add new library :
  . model2beepcast-v1.0.15.jar
  . transaction2beepcast-v1.0.16.jar

v1.0.09

- add estimation time during the broadcast running
  . create module channelmonitorxxxx
  . the module will be handle the monitoring during the broadcast
  . the module can produce the total percentage processed number and second remains

v1.0.08

- when all broadcast message has a failed message is suppose also create a report

- fixed the load channel hit per event based 

- add channel debug in the configuration

- add new library :
  . model2beepcast-v1.0.14.jar
  . transaction2beepcast-v1.0.15.jar
  . beepcast_loadmanagement-v1.2.00.jar

v1.0.07

- create a report still , when the broadcast is in suspended mode . ( to be continued )

- add new field , named date_suspended in the channel_session table

  ALTER TABLE `channel_session` 
    ADD COLUMN `date_suspended` DATETIME AFTER `date_started` ;

- add new class named ChannelSessionReportDAO ,

  there is a model to update dateInserted .
  
- everytime the agent do finished the pool , it will check 
  if from suspended / finished , it will reupdate again date_inserted 
  in the channel session report table .
    
v1.0.06

- put a message param channeLogBean before do the transaction run .

- change the channel log model and bean

- add cust ref field in the channel log , by executing sql below :

  ALTER TABLE `channel_log` 
    ADD COLUMN `cust_ref_id`   VARCHAR(45)  AFTER `subscribed`    ,
    ADD COLUMN `cust_ref_code` VARCHAR(45)  AFTER `cust_ref_id`   ,
    ADD COLUMN `description`   VARCHAR(100) AFTER `cust_ref_code` ;

- add new library :
  . model2beepcast-v1.0.11.jar

v1.0.05

- add new library :
  . model2beepcast-v1.0.10.jar
  . provider2beepcast-v2.1.14.jar
  . transaction2beepcast-v1.0.12.jar
  . router2beepcast-v1.0.04.jar

- Every time do the broadcast , when found the result code is failed / error
  it will stop and validate the balance .
  
- Every time the broadcast is stopped by the system , the system shall send
  alert message thru email .
  
v1.0.04

- Recontinue run the broadcast after the machine startup .

- Add management email alert when some one start / stop / suspended / or finished the broadcast 

- Add new library :
  . onm library v1.1.00
  . provider2beepcast v2.1.12
  . loadmanagement v1.1.04

v1.0.03

- Add feature to support restriction on broadcast :
  . load speed for every event
  
- Add default load speed in the configuration  

- Change the way of the reserve client event balance api

- add new library :
  . quartz and commons-collection
  . dbmanager library v1.1.06
  . billing libraray v1.1.01
  . client library v2.1.05
  . transaction library v1.0.03
  . router library v1.0.02

v1.0.02

- created a function to generate report of broadcast :
  . before n days the system will automatically
  . after n days the user will demand to create report

v1.0.01

- use the new java library 
  . transaction2beepcast v1.0.01
  . router2beepcast v1.0.01

v1.0.00

- added a new java library named model2beepcast , transaction2beepcast , and router2beepcast v1.0.00.jar

- added a new java library named provider2beepcast-v2.1.10.jar

- added a new java library named util2beepcast-v1.0.00.jar

- Just copy com.beepcast.channel.* classes from beepadmin
