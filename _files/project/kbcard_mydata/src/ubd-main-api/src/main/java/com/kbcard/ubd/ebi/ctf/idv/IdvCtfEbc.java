package com.kbcard.ubd.ebi.ctf.idv;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

public class IdvCtfEbc {
	/*
	 * 거래로그저장
	 */
	public int insertTranLog(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertTranLog", input);
		return dao.executeUpdate();
	}
	/*
	 * 인가코드요청저장
	 */
	public int insertCnfCdDmd(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertCnfCdDmd", input);
		return dao.executeUpdate();
	}
	/*
	 * 인가코드요청업데이트
	 */
	public int updateCnfCdDmd(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/updateCnfCdDmd", input);
		return dao.executeUpdate();
	}

	/*
	 * 거래고유번호 검증  
	 */
	public LMultiData isTranId(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/isTranId", input);
		return dao.executeQuery();
	}
	/*
	 * 요청데이터검증 (client_id, redirect_url, app_scheme)  
	 */
	public LData isClientId(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/isClientId", input);
		return dao.executeQueryForSingle();
	}
	/*
	 * 인가코드발급요청 조회 ( 본인인증 완료 후 )
	 */
	public LMultiData selectCnfCdDmdHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/selectCnfCdDmdHis", input);
		return dao.executeQueryForPage();
	}
	/*
	 * SCOPE 목록 조회
	 */
	public LMultiData selectAtrScopCtt(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/selectAtrScopCtt", input);
		return dao.executeQuery();
	}
	/*
	 * 요청 SCOPE 목록 조회
	 */
	public int deleteScope(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/deleteScope", input);
		return dao.executeUpdate();
	}
	/*
	 * 요청 SCOPE 목록 조회
	 */
	public LMultiData selectUsrScopCtt(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/selectUsrScopCtt", input);
		return dao.executeQuery();
	}
	/*
	 * 전송요구저장
	 */
	public int insertTrsRqst(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertTrsRqst", input);
		return dao.executeUpdate();
	}
	/*
	 * 권한영역저장
	 */
	public int insertScope(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertScope", input);
		return dao.executeUpdate();
	}
	/*
	 * 자산내역저장
	 */
	public int insertAstCtt(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertAstCtt", input);
		return dao.executeUpdate();
	}
	/*
	 * 전송요구이력저장
	 */
	public int insertTrsRqstHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertTrsRqstHis", input);
		return dao.executeUpdate();
	}
	/*
	 * 권한영역이력저장
	 */
	public int insertScopeHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertScopeHis", input);
		return dao.executeUpdate();
	}
	/*
	 * 자산내역이력저장
	 */
	public int insertAstCttHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfEbc/insertAstCttHis", input);
		return dao.executeUpdate();
	}
}
