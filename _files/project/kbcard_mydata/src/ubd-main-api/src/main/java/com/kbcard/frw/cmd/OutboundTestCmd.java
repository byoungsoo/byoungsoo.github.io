package com.kbcard.frw.cmd;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonframework.front.channel.context.LActionContext;
import devonframework.front.command.LAbstractCommand;
import devonframework.front.taglib.saymessage.LSayMessage;

public class OutboundTestCmd extends LAbstractCommand {

	@Override
	public void execute() throws Exception {
		// EmployeeBiz biz = new EmployeeBiz();
		// LData result = biz.retrieveEmployee(data);
		// 우리 헤더가 필요한가..?

		LLog.debug.println("OutboundTestCmd !!!!!!!!!!!!!!!");

		LLog.debug.println("GUID : [" + ContextHandler.getContextObject(ContextKey.GUID) + "]");
		LData result = (LData) BizCommand.execute("com.kbcard.frw.pbi.OutboundTestPbc", "sendOutboundRequest", data);
		LActionContext.setAttribute("result", result);

		// LHttpHeaderParameter headerParameter = new LHttpHeaderParameter();
		// headerParameter.setHeaderParameter("404","aaa");
		// {header:{code:404,message:aaa}}

		if (result.isEmpty() && !"create".equals(data.getString("mode"))) {
			// LSayMessage.setMessageCode(Constants.MSG_INFO_NODATA);
			LSayMessage.setMessageCode("dev.inf.com.nodata");
		}
	}
}
