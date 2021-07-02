package com.kbcard.ubd.cmd.ctf;

//import com.kbcard.ubd.common.pbi.token.TokenCommonPbc;

import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.channel.telegram.TelegramConstants;
import devonframework.front.channel.context.LActionContext;
import devon.core.collection.LData;

import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.log.LLog;
import devonenterprise.ext.front.command.ExtAbstractCommand;

public class IdvCtfCmd extends ExtAbstractCommand {

	@Override
	public void execute() throws Exception {

		LLog.debug.println("IdvCtfCmd !!!!!!!!!!!!!!!");

		LLog.debug.println("GUID : [" + ContextHandler.getContextObject(ContextKey.GUID) + "]");

		//최종갱신식별자 setting
		setAuditInfo("IDV001");
		
		LData result = (LData) BizCommand.execute("com.kbcard.ubd.pbi.ctf.idv.IdvCtfPbc", "idvCtfCnfCdDmd", data);
    	
    	LActionContext.setAttribute(TelegramConstants.BODY_FIELD_NAME,result);
    	

		
//		String iss         = "D1AAAF0000"; //접근토큰발급자기관코드
//		String aud         = "E1AAAF0000"; //접근토큰수신자기관코드
//		String jti         = "jjwt123"; //접근토큰식별자
//		Long expiredTime   = 1000 * 60L * 60L * 24L * 90L; //접근토큰유효시간
//		String scope       = "card.list"; //scope
//		
//		data.put("iss",iss);
//		data.put("aud",aud);
//		data.put("jti",jti);
//		data.put("expiredTime",expiredTime);
//		data.put("scope",scope);
//		
//		String token = (String)BizCommand.execute("com.kbcard.ubd.pbi.token.TokenCommonPbc", "makeToken", data);
//		
//		
//		LData result = BizCommand.execute("com.kbcard.ubd.pbi.token.TokenCommonPbc", "verifyToken", token);
//		System.out.println(result);
//		TokenCommonPbc tokenPbc = new TokenCommonPbc();
//		//tokenPbc.verifyToken(토큰, 요청업권, 기관코드(or 클라이언트ID), APIID, 자산ID ) 
//		tokenPbc.verifyToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOm51bGwsInNjb3BlIjoiY2FyZC5saXN0IGNhcmQuY2FyZCIsImlzcyI6IkQxQUFBRjAwMDAiLCJleHAiOjE2MzIwNDU3ODQsImp0aSI6IklfOTk3MTExMTExMVNQMjAyMTA2MTcwMDExMF8yMDIxMDYyMSJ9.vjQY9ZbxXQNZ8QUENspAapyB5tMAAfb2Mb6o1YHF-dQ", "card", "D1AAAF0000", "CD02", " ");
	}
}
