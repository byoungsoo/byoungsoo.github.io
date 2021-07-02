package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbKftcApiMgEbc
 * 
 * @logicalname  : 오픈뱅킹금결원API관리Ebc
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

public class OpnbKftcApiMgEbc {

    /**
     * - 금결원에 등록된 API와 수수료 금액을 조회. 
     * 
     * 1. [오픈뱅킹URL일련번호] 항목으로 UBF오픈뱅킹금융결제원API기본 테이블 조회
     * 2. 오픈뱅킹API관리내용 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹금융결제원API기본
     * <INPUT>
     *  오픈뱅킹API관리구분코드
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹API관리구분코드, 오픈뱅킹이용기관수수료, 오픈뱅킹수수료대상여부, 오픈뱅킹API명, 오픈뱅킹URL내용
     * 
     * @method selectOpnbKftcApiInf
     * @method(한글명) 오픈뱅킹금결원API정보조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectOpnbKftcApiInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbKftcApiMgEbc/selectOpnbKftcApiInf", input);
        return dao.executeQuery(); 
    }

   
}

