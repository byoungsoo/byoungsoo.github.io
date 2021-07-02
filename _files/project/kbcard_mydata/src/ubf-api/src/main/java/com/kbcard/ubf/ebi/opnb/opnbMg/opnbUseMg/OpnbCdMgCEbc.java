package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCdMgCEbc
 * 
 * @logicalname  : 오픈뱅킹코드관리Ebc
 * @author       : 김상현
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       김상현       최초 생성
 *
 */

public class OpnbCdMgCEbc {

	/**
     * - 금융기관, 상호금융기관, 금융투자회사, 카드사 등의 코드정보 조회
     * - OAuth 응답코드정보 조회
     * - 세부 응답코드정보 조회
     * - API 응답코드정보 조회
     * - 참가기관 응답코드정보 조회
     * - 이체 용도 구분코드정보 조회
     * - 거래유형 코드정보 조회
     * 
     * 1. 요청된 조회유형에 따라 응답값 리턴
     * 
     * <관련 테이블>
     *  UBD코드그룹기본
     *  UBD코드상세기본
     * <INPUT>
     *  오픈API포탈코드그룹한글명
     * <OUTPUT>
     * LIST
     *  -   오픈API포탈코드그룹한글명
     *      오픈API포탈코드그룹영문명
     *      오픈API포탈코드식별번호
     *      오픈API포탈코드명
     * 
     * @serviceID UBF1010301
     * @method retvCdInf
     * @method(한글명) 코드정보 조회
     * @param String
     * @return LData
     * @throws LException 
     */ 
    public LData selectCdInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCdMgCEbc/selectCdInf", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 거래고유번호생성을 위한 일련번호(seq) 조회
     * 
     * @method selectTrUnoSeq
     * @method(한글명) 거래고유번호일련번호 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectTrUnoSeq(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCdMgCEbc/selectTrUnoSeq", input);
        return dao.executeQueryOnlySingle();
    }

}

