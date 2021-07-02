package com.kbcard.ubf.pbi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.exception.LException;

/** 
 * opnbUtzFeePrcPbc
 * 
 * @logicalname  : 오픈뱅킹이용수수료처리Pbc
 * @author       : 임헌택
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       임헌택       최초 생성
 *
 */

public class OpnbUtzFeePrcPbc {

    /**
     * - 전일자 기준 이용수수료 집계금액과 처리대행 비용 회계처리한다.
     * 
     * 1. 거래코드 GBG03J1401(통합회계처리전문등록(무자원입금 불가)), GBG03J1402(개별회계처리전문등록(무자원입금 가능))
     * 2. 인터페이스ID : UBF_2_GBGS00001(UBB_2_GBGS00001 참조)
     * 
     * <관련테이블>
     * - UBF오픈뱅킹채널별수수료집계내역
     * - UBF오픈뱅킹금결원수수료집계내역
     * 
     * <INPUT>
     * - 회계일자
     * 
     * <OUTPUT>
     * - 회계전표번호
     * 
     * @method prcUtzFeeActg
     * @method(한글명) 이용수수료 회계 처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcUtzFeeActg(LData input) throws LException {
        LData result = new LData();

        return result;
    }

}

