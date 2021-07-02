package com.kbcard.ubf.ebi.opnb.opnbMg.utzInsCtfMg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.core.exception.LNotAffectedException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCfreUtzRgEbc
 * 
 * @logicalname  : 오픈뱅킹이용기관인증Ebc
 * @author       : 김정화
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       김정화       최초 생성
 *
 */

public class OpnbUtzInsCtfEbc {

   
    /**
     * - Access Token의 유효기간 만료되기 전에 폐기 처리
     * 
     * 1. [오픈뱅킹접근토큰내용] 항목으로 UBF오픈뱅킹토큰기본 테이블의 [오픈뱅킹토큰최종여부]를 N으로 Update
     * 2.  수정결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹토큰기본
     * <INPUT>
     * 오픈뱅킹접근토큰내용, 오픈뱅킹토큰최종여부
     * <OUTPUT>
     * 수정결과(T/F)
     * 
     * @method updateTken
     * @method(한글명) 토큰 폐기 수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateTken(LData input) throws LException {

    	int iRtn = 0;
		
		LCommonDao dao = new LCommonDao("opnb/opnbMg/utzInsCtfMg/opnbUtzInsCtfEbc/updateTken", input);
		iRtn = dao.executeUpdate();
	
        //처리 건수가 0이면 강제로 LNotAffectedException 을 발생시킴
        if (iRtn == 0) {
        	  throw new LNotAffectedException();
        }
  
		return iRtn;
    }
    /**
     * - 현재 보유한 Access Token 정보 조회
     * 
     * 1. [오픈뱅킹접근토큰내용] 항목으로 UBF오픈뱅킹토큰기본 테이블 조회
     * 2. Access Token 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹토큰기본
     * <INPUT>
     * 오픈뱅킹접근토큰내용
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹접근토큰내용, 오픈뱅킹토큰발급일시, 오픈뱅킹토큰만료일시, 오픈뱅킹토큰범위구분코드, 오픈뱅킹토큰최종여부
     * 
     * 
     * @method selectTkenInfCtg
     * @method(한글명) 토큰 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectTkenInfCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/utzInsCtfMg/opnbUtzInsCtfEbc/selectTkenInfCtg", input);
        return dao.executeQuery(); 
    }

  


}
