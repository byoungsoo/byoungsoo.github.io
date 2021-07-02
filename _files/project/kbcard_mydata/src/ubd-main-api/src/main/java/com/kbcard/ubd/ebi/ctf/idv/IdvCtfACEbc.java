package com.kbcard.ubd.ebi.ctf.idv;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

public class IdvCtfACEbc {
	/*
	 * 요청 SCOPE 목록 조회 
	 */
	public LMultiData selectUsrScopCtt(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfACEbc/selectUsrScopCtt", input);
		return dao.executeQuery();
	}
	/*
	 * 기발급 접근토큰 조회
	 */
	public LMultiData selectUsrAtHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfACEbc/selectUsrAtHis", input);
		return dao.executeQuery();
	}
	/*
	 * 기발급 접근토큰 조회(리프레시토큰)
	 */
	public LMultiData selectUsrAtRefresh(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfACEbc/selectUsrAtRefresh", input);
		return dao.executeQuery();
	}
	/*
	 * 기발행접근토큰이력삭제
	 */
	public int deleteUsrAcHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/deleteUsrAcHis", input);
		return dao.executeUpdate();
	}
	/*
	 * 기발행접근토큰삭제
	 */
	public int deleteUsrAc(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/deleteUsrAc", input);
		return dao.executeUpdate();
	}
	/*
	 * 기발행접근토큰이력 폐기처리
	 */
	public int updateUsrAcHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/updateUsrAcHis", input);
		return dao.executeUpdate();
	}
	/*
	 * 접근토큰발행내역 저장
	 */
	public int insertUsrAc(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/insertUsrAc", input);
		return dao.executeUpdate();
	}
	/*
	 * 접근토큰발행이력 저장 ( 접근토큰만 )
	 */
	public int insertUsrAcHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/insertUsrAcHis", input);
		return dao.executeUpdate();
	}
	/*
	 * 접근토큰발행이력 저장
	 */
	public int insertUsrAcHisAll(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("idv/IdvCtfAcEbc/insertUsrAcHisAll", input);
		return dao.executeUpdate();
	}
}
