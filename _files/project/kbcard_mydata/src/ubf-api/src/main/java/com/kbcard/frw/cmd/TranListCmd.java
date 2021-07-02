package com.kbcard.frw.cmd;

import com.kbcard.frw.sample.util.HttpAsyncRunner;

import devon.core.collection.LData;
import devon.core.log.LLog;
import devonenterprise.ext.persistent.page.PageConstants;
import devonframework.front.channel.context.LActionContext;
import devonframework.front.command.LAbstractCommand;
import devonframework.front.taglib.saymessage.LSayMessage;

public class TranListCmd extends LAbstractCommand {
	
	@Override
	public void execute() throws Exception {
		LLog.debug.println( "TranListCmd !!!!!!!!!!!!!!!" );
		
		
		LData result = new LData();
		

		LData pagingData = new LData(data);
		pagingData.setString(PageConstants.NEXT_INQ_KY,data.getString("nextKey"));
		pagingData.setString(PageConstants.PGE_SIZE,data.getString("pageSize"));
		
		String targetUrl1 = "http://internal-k8s-mydatadev-9d38a41273-207173145.ap-northeast-2.elb.amazonaws.com:80/ubd-main-api/rest/test/tranList/BWC";
		LData result1 = HttpAsyncRunner.execute(targetUrl1, data);
		
		String targetUrl2 = "http://internal-k8s-mydatadev-9d38a41273-207173145.ap-northeast-2.elb.amazonaws.com:80/ubd-main-api/rest/test/tranList/BWC";
		LData result2 = HttpAsyncRunner.execute(targetUrl2, data);
		
		String targetUrl3 = "http://internal-k8s-mydatadev-9d38a41273-207173145.ap-northeast-2.elb.amazonaws.com:80/ubd-main-api/rest/test/tranList/BWC";
		LData result3 = HttpAsyncRunner.execute(targetUrl3, data);
		
		String targetUrl4 = "http://internal-k8s-mydatadev-9d38a41273-207173145.ap-northeast-2.elb.amazonaws.com:80/ubd-main-api/rest/test/tranList/BWC";
		LData result4 = HttpAsyncRunner.execute(targetUrl4, data);
		
		String targetUrl5 = "http://internal-k8s-mydatadev-9d38a41273-207173145.ap-northeast-2.elb.amazonaws.com:80/ubd-main-api/rest/test/tranList/BWC";
		LData result5 = HttpAsyncRunner.execute(targetUrl5, data);
		
		result.set("result1", result1);
		result.set("result2", result2);
		result.set("result3", result3);
		result.set("result4", result4);
		result.set("result5", result5);
		
		LLog.debug.println("result : " + result);
		
		LActionContext.setAttribute( "query", data );
		LActionContext.setAttribute( "result", result );
		
		if ( result.isEmpty() && !"create".equals( data.getString( "mode" ) ) ) {
			//			LSayMessage.setMessageCode(Constants.MSG_INFO_NODATA);
			LSayMessage.setMessageCode( "dev.inf.com.nodata" );
		}
	}	
}
