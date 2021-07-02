package com.kbcard.ubd.cmd.ctf;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.channel.telegram.TelegramConstants;
import devonenterprise.ext.front.command.ExtAbstractCommand;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.util.StringUtil;
import devonframework.front.channel.context.LActionContext;

public class CtfTokenCmd extends ExtAbstractCommand {

	@Override
	public void execute() throws Exception {

		LLog.debug.println("CtfTokenCmd !!!!!!!!!!!!!!!");

		LLog.debug.println("GUID : [" + ContextHandler.getContextObject(ContextKey.GUID) + "]");
		
		String grant_type = data.getString("grant_type");
		String x_api_tran_id = data.getString("x-api-tran-id");

		System.out.println(data);
		System.out.println("grant_type ========== ");
		System.out.println(grant_type);
		
		LData result = new LData();
				
		// grant_type null 체크
		if( StringUtil.trimNisEmpty(grant_type)) {
			//HTTP 응답코드 셋팅
			ContextUtil.setHttpResponseHeaderParam("status", "400"); 
	    	
			result.set("error","unsupported_grant_type");
			result.set("error_description","grant_type 값이 잘못되었습니다.");
		}else if(grant_type.equals("authorization_code")) { // 개별인증002
			//최종갱신식별자 setting
			setAuditInfo("IDV002");
			result = (LData) BizCommand.execute("com.kbcard.ubd.pbi.ctf.idv.IdvCtfAcPbc", "idvCtfAcDmd", data);
		}else if(grant_type.equals("refresh_token")) { // 개별인증003
			//최종갱신식별자 setting
			setAuditInfo("IDV003");
			result = (LData) BizCommand.execute("com.kbcard.ubd.pbi.ctf.idv.IdvCtfAcPbc", "idvCtfRtAcDmd", data);
		}else if(grant_type.equals("password")) { // 통합인증002
			//최종갱신식별자 setting
			setAuditInfo("ITV002");
			result = (LData) BizCommand.execute("com.kbcard.ubd.pbi.ctf.itv.ItvCtfPbc", "getWhAuthReqData", data);
		}else {
			//HTTP 응답코드 셋팅
			ContextUtil.setHttpResponseHeaderParam("status", "400"); 
	    	
			result.set("error","unsupported_grant_type");
			result.set("error_description","grant_type 값이 잘못되었습니다.");
		}
		
    	
    	LActionContext.setAttribute(TelegramConstants.BODY_FIELD_NAME,result);
	}
}
