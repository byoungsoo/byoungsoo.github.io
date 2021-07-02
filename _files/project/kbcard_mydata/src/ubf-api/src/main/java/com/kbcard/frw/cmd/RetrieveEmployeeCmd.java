package com.kbcard.frw.cmd;

import devon.core.collection.LData;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonframework.front.annotation.Action;
import devonframework.front.channel.context.LActionContext;
import devonframework.front.command.LAbstractCommand;
import devonframework.front.taglib.saymessage.LSayMessage;

public class RetrieveEmployeeCmd extends LAbstractCommand {
	
	@Override
	public void execute() throws Exception {
		//		EmployeeBiz biz = new EmployeeBiz();
		//		LData result = biz.retrieveEmployee(data);
		
		LLog.debug.println( "RetrieveEmployeeCmd !!!!!!!!!!!!!!!" );
		
		// LData result = (LData) BizCommand.execute( "com.kbcard.pbi.EmployeePbi", "retrieveEmployee", data );
		
		//		LDataProtocol output = (LDataProtocol) ServiceManager.execute(data);
		
		LData result = data;
		
		LActionContext.setAttribute( "data", result );
		LActionContext.setAttribute( "query", data );
		
		//LHttpHeaderParameter headerParameter = new LHttpHeaderParameter();
		//headerParameter.setHeaderParameter("404","aaa");
		//{header:{code:404,message:aaa}} 
		
		if ( result.isEmpty() && !"create".equals( data.getString( "mode" ) ) ) {
			//			LSayMessage.setMessageCode(Constants.MSG_INFO_NODATA);
			LSayMessage.setMessageCode( "dev.inf.com.nodata" );
		}
	}
	
}
