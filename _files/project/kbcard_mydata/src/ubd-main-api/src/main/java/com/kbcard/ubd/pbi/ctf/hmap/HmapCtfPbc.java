package com.kbcard.ubd.pbi.ctf.hmap;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devonenterprise.ext.core.exception.LInterfaceException;

public class HmapCtfPbc {
	
	LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
	
	// 1. 마이데이터 표준API제공 개별인증 홈앱 토큰발급
	public LData retvEaiTkenIsuInf(LData input) throws LException {
		return httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_CXWS00001", input);
	}
	
	// 2. 마이데이터 표준API제공 개별인증 홈앱 토큰검증
	public LData retvEaiTkenVlnInf(LData input) throws LException {
		return httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_CXWS00002", input);
	}

	// 3. 마이데이터 표준API제공 ID/PW 방식 개별인증
	public LData retvEaiIdvCtfInf(LData input) throws LException {
		return httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_CXWS00003", input);
	}

	// 4. 마이데이터 표준API제공 카드번호 방식 개별인증
	public LData retvEaiCardIdvCtfInf(LData input) throws LException {
		return httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_CXWS00004", input);
	}	

}
