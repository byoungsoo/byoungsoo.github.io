package com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.core.exception.LBizNotAffectedException;
import devonenterprise.ext.core.exception.LNotAffectedException;
import devonenterprise.ext.util.LDataUtil;
import devonframework.persistent.autodao.LCommonDao;

/**
 * opnbApiEbc
 * 
 * @logicalname : 오픈뱅킹APIEbc
 * @author : 박건우
 * @since : 2021-04-30
 * @version : 1.0
 * @see :
 * 
 *      << 개정이력(Modification Information) >>
 *
 *      수정일 수정자 수정내용 --------------- --------- ---------------------------
 *      2021-04-30 박건우 최초 생성
 *
 */

public class OpnbApiEbc {

	/**
	 * - 금융결제원 API를 호출 이력을 UBF오픈뱅킹기관거래내역 테이블에 등록한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청/응답메시지
	 * <OUTPUT> 등록결과
	 * 
	 * @method insertAPICallPhs
	 * @method(한글명) API호출이력등록
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public int insertAPICallPhs(LData input) throws LException {
		int iRtn = 0;
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/insertAPICallPhs", input);
		iRtn = dao.executeUpdate();
		
		if (iRtn == 0) {
			throw new LBizNotAffectedException("ERR_MSG_CD");
		}

		return iRtn;
	} 

	/**
	 * - AccessToken 정보를 테이블에 등록한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹토큰기본
	 * <INPUT> AccessToken 정보
	 * <OUTPUT> 등록결과
	 * 
	 * @method insertAccessToken
	 * @method(한글명) AccessToken 등록
	 * @param LData
	 * @return int
	 * @throws LException
	 */
	public int insertAccessToken(LData input) throws LException {
		int iRtn = 0;
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/insertAccessToken", input);
		iRtn = dao.executeUpdate();
		
		//처리 건수가 0이면 강제로 LNotAffectedException 을 발생시킴
		if (iRtn == 0) {
			throw new LNotAffectedException();
		}
        
		return iRtn;
	}
	
	/**
	 * - AccessToken 정보를 변경한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹토큰기본
	 * <INPUT> AccessToken 정보
	 * <OUTPUT> 변경결과
	 * 
	 * @method updateAccessToken
	 * @method(한글명) AccessToken 등록
	 * @param LData
	 * @return int
	 * @throws LException
	 */
	public int updateAccessToken(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/updateAccessToken", input);        
		return dao.executeUpdate();
	}
	
	/**
	 * - AccessToken 정보를 테이블에서 조회한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹토큰기본
	 * <INPUT> AccessToken 정보
	 * <OUTPUT> 등록결과
	 * 
	 * @method insertAccessToken
	 * @method(한글명) AccessToken 조회
	 * @param LData 토큰조회조건
	 * @return LData 토큰정보
	 * @throws LException
	 */
	public LData selectAccessToken(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/selectAccessToken", input);
		return dao.executeQueryForSingle();
	}

	/**
	 * - 금결원 API 정보를 테이블에서 조회한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본, UBF오픈뱅킹API수수료기본
	 * <INPUT> API정보 정보
	 * <OUTPUT> 등록결과
	 * 
	 * @method selectAPIInf
	 * @method(한글명) API정보 조회
	 * @param LData API정보조회조건
	 * @return LData API정보
	 * @throws LException
	 */
	public LData selectAPIInf(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/selectAPIInf", input);
		return dao.executeQueryOnlySingle();
	}
	
	public LMultiData selectAPIInfList(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/selectAPIInfList", input);
		return dao.executeQuery();
	}
	
	public LData selectCiCtt(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/selectCiCtt", input);
		return dao.executeQueryForSingle();
	}
	
	public LData selectAPICallPhsSeq(LData input) throws LException {
		LCommonDao dao = new LCommonDao("opnb/apiCllg/kftcApi/OpnbApiEbc/selectAPICallPhsSeq", input);
		LDataUtil.putAllLDataProtocol(input, dao.executeQueryForSingle());		
		return input;
	}	
}
