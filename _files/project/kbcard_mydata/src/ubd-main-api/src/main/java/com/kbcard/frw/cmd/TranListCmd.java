package com.kbcard.frw.cmd;

import com.kbcard.ubd.common.util.CryptoDataUtil;

import devon.core.collection.LData;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.persistent.page.PageConstants;
import devonenterprise.ext.persistent.page.ScrollPageData;
import devonframework.front.channel.context.LActionContext;
import devonframework.front.command.LAbstractCommand;
import devonframework.front.taglib.saymessage.LSayMessage;

public class TranListCmd extends LAbstractCommand {
	
	@Override
	public void execute() throws Exception {
		//		EmployeeBiz biz = new EmployeeBiz();
		//		LData result = biz.retrieveEmployee(data);
		
		LLog.debug.println( "TranListCmd !!!!!!!!!!!!!!!" );
		
		String rawdata = "가나다";
		LLog.debug.println("암호화 테스트 : raw [" + rawdata + "]");
		String encdata = CryptoDataUtil.encrypt(rawdata, CryptoDataUtil.KB_UBE_SERVER_KEY);
		LLog.debug.println("암호화 테스트 : enc [" + encdata + "]");
		String decdata = CryptoDataUtil.decrypt(rawdata, CryptoDataUtil.KB_UBE_SERVER_KEY);
		LLog.debug.println("암호화 테스트 : dec [" + decdata + "]");
		
		LData result = new LData();
		result.setString("raw", rawdata);
		result.setString("enc", encdata);
		result.setString("dec", decdata);
		
		
		result = (LData) BizCommand.execute("com.kbcard.frw.pbi.TranListPbc", "retrieveTranList", data);
		
		LData pagingData = new LData(data);
		pagingData.setString(PageConstants.NEXT_INQ_KY,data.getString("nextKey"));
		pagingData.setString(PageConstants.PGE_SIZE,data.getString("pageSize"));
		
		LData paging = (LData) BizCommand.execute("com.kbcard.frw.pbi.TranListPbc", "retrieveTranListForPaging", pagingData);
		
		String _next_page_exis_yn = ScrollPageData.getNextYn();
		String _next_key = ScrollPageData.getNextKey();
		
		paging.setString(PageConstants.NEXT_EXST_YN, _next_page_exis_yn);
		paging.setString(PageConstants.NEXT_INQ_KY, _next_key);
		// LData result = (LData) BizCommand.execute( "com.kbcard.pbi.EmployeePbi", "retrieveEmployee", data );
		
		//		LDataProtocol output = (LDataProtocol) ServiceManager.execute(data);
		
		LActionContext.setAttribute( "query", data );
		LActionContext.setAttribute( "result", result );
		LActionContext.setAttribute( "paging", paging );
		
		//LHttpHeaderParameter headerParameter = new LHttpHeaderParameter();
		//headerParameter.setHeaderParameter("404","aaa");
		//{header:{code:404,message:aaa}} 
		
		if ( result.isEmpty() && !"create".equals( data.getString( "mode" ) ) ) {
			//			LSayMessage.setMessageCode(Constants.MSG_INFO_NODATA);
			LSayMessage.setMessageCode( "dev.inf.com.nodata" );
		}
	}	
}
