package com.kbcard.ubd.cmd.ctf;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.channel.telegram.TelegramConstants;
import devonenterprise.ext.front.command.ExtAbstractCommand;
import devonframework.front.channel.context.LActionContext;

public class ApiTranIdVlnCmd extends ExtAbstractCommand {

	@Override
	public void execute() throws Exception {

		LLog.debug.println("ApiTranIdVlnCmd !!!!!!!!!!!!!!!");

		LLog.debug.println("GUID : [" + ContextHandler.getContextObject(ContextKey.GUID) + "]");

		//최종갱신식별자 setting
		setAuditInfo("IDV001");
		
		LData result = (LData) BizCommand.execute("com.kbcard.ubd.pbi.ctf.idv.IdvCtfPbc", "ApiTranIdVln", data);
    	
    	LActionContext.setAttribute(TelegramConstants.BODY_FIELD_NAME,result);
    	
	}
}
