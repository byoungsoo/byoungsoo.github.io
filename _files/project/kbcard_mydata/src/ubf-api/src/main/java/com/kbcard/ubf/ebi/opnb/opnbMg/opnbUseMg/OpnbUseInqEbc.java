package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbUseInqEbc
 * 
 * @logicalname  : 오픈뱅킹사용조회Ebc
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

public class OpnbUseInqEbc {

    /**
     * - 이용기관이 사용한 API에 대한 이용 수수료를 조회한다.
     * 
     * 1. 조회일자로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹수수료조회내역
     * <INPUT>
     * 오픈뱅킹전문거래년월일
     * <OUTPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * 
     * @method selectFee
     * @method(한글명) 수수료 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectFee(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUseInqEbc/selectFee", input);
        return dao.executeQuery(); 
    }

    /**
     * - 이용기관이 사용한 API에 대한 집계정보를 조회한다.
     * 
     * 1. 금융기관코드, 조회기간으로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹집계조회내역
     * <INPUT>
     * 오픈뱅킹금융기관코드, 조회시작일자, 조회종료일자
     * <OUTPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * 
     * @method selectSmm
     * @method(한글명) 집계 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectSmm(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUseInqEbc/selectSmm", input);
        return dao.executeQuery(); 
    }

}

