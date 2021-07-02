package com.kbcard.ubd.ebi.ctf.itv;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

public class ItvCtfEbc {
	
	/*
	 * 고객CI 조회
	 */
	public LMultiData selectCnfCustCi(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("itv/ItvCtfEbc/selectCnfCustCi", input);
		return dao.executeQuery();
	}
	
	/*
	 * 전송요구사항 전자서명 저장
	 */
	public int insertSignedConst(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("itv/ItvCtfEbc/insertSignedConst", input);
		return dao.executeUpdate();
	}
	
	/*
	 * UBD 인증/지원API 거래내역 저장
	 */
	public int insertCtfAPITran(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("itv/ItvCtfEbc/updateCtfAPITran", input);
		return dao.executeUpdate();
	}
	
	/*
	 * UBD 접근토큰요청내역 저장
	 */
	public int insertScope(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("itv/ItvCtfEbc/insertScope", input);
		return dao.executeUpdate();
	}
	
	/*
	 * UBD 접근토큰요청내역 저장
	 */
	public int insertAcsTkReq(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("itv/ItvCtfEbc/insertAcsTkReq", input);
		return dao.executeUpdate();
	}
	
	

}