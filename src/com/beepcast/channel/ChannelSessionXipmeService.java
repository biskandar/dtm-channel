package com.beepcast.channel;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionXipmeService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionXipmeService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelSessionXipmeDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionXipmeService() {
    dao = new ChannelSessionXipmeDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean persistBean( ChannelSessionXipmeBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to persist bean , found null bean" );
      return result;
    }
    ChannelSessionXipmeBean selectBean = selectBean(
        bean.getChannelSessionId() , bean.getXipmeCode() );
    if ( selectBean != null ) {
      bean.setId( selectBean.getId() );
      result = updateTotals( bean );
      return result;
    }
    result = insertBean( bean );
    return result;
  }

  public boolean insertBean( ChannelSessionXipmeBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to insert bean , found null bean" );
      return result;
    }
    if ( bean.getChannelSessionId() < 1 ) {
      DLog.warning( lctx , "Failed to insert bean "
          + ", found zero channel session id" );
      return result;
    }
    if ( StringUtils.isBlank( bean.getXipmeCode() ) ) {
      DLog.warning( lctx , "Failed to insert bean "
          + ", found blank xipme code" );
      return result;
    }
    result = dao.insertBean( bean );
    return result;
  }

  public boolean updateTotals( ChannelSessionXipmeBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to update totals , found null bean" );
      return result;
    }
    if ( bean.getId() < 1 ) {
      DLog.warning( lctx , "Failed to update totals , found zero id" );
      return result;
    }
    result = dao.updateTotals( bean );
    return result;
  }

  public ChannelSessionXipmeBean selectBean( int channelSessionId ,
      String xipmeCode ) {
    ChannelSessionXipmeBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select bean "
          + ", found zero channel session id" );
      return bean;
    }
    if ( StringUtils.isBlank( xipmeCode ) ) {
      DLog.warning( lctx , "Failed to select bean "
          + ", found blank xipme code" );
      return bean;
    }
    bean = dao.selectBean( channelSessionId , xipmeCode );
    return bean;
  }

  public List selectBeans( int channelSessionId ) {
    List listBeans = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select beans "
          + ", found zero channel session id" );
      return listBeans;
    }
    listBeans = dao.selectBeans( channelSessionId );
    return listBeans;
  }

  public ChannelSessionXipmeBean selectFirstBean( int channelSessionId ) {
    ChannelSessionXipmeBean bean = null;
    List listBeans = selectBeans( channelSessionId );
    if ( ( listBeans == null ) || ( listBeans.size() < 1 ) ) {
      return bean;
    }
    bean = (ChannelSessionXipmeBean) listBeans.get( 0 );
    return bean;
  }

}
