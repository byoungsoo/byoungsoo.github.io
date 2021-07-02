package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.exception.LNotFoundException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbUtzFeePrcEbc
 * 
 * @logicalname  : 오픈뱅킹이용수수료처리Ebc
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

public class OpnbUtzFeePrcEbc {

    /**
     * - 채널별 이용수수료 금액을 등록한다
     * 
     * @method insertChnPrUtzFee
     * @method(한글명) 채널별 이용수수료 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertChnPrUtzFee(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/insertChnPrUtzFee", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 채널별 이용수수료 금액을 조회한다
     * 
     * @method selectChnPrUtzFee
     * @method(한글명) 채널별 이용수수료 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectChnPrUtzFee(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/selectChnPrUtzFee", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 이용기관이 사용한 API에 대한 이용 수수료를 UBF오픈뱅킹수수료조회내역 테이블에 등록한다.
     * 
     * 1. 금융기관코드, 조회기간으로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹수수료조회내역
     * <INPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * <OUTPUT>
     * 등록결과
     * 
     * @method insertFeeInqHis
     * @method(한글명) 수수료조회내역 등록
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int insertFeeInqHis(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/insertFeeInqHis", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 이용기관이 사용한 API에 대한 집계정보를 UBF오픈뱅킹집계조회내역 테이블에 등록한다.
     * 
     * 1. 금융기관코드, 조회기간으로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹집계조회내역
     * <INPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * <OUTPUT>
     * 등록결과
     * 
     * @method insertSmmInqHis
     * @method(한글명) 집계조회내역 등록
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int insertSmmInqHis(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/insertSmmInqHis", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - UBF오픈뱅킹수수료조회내역 테이블을 조회한다
     * 
     * 1. 조회기간, API관리구분코드로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹수수료조회내역
     * <INPUT>
     * 조회기간, API관리구분코드
     * <OUTPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * 
     * @method selectFeeInqHis
     * @method(한글명) 수수료조회내역 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectFeeInqHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/selectFeeInqHis", input);
        return dao.executeQuery(); 
    }

    /**
     * - UBF오픈뱅킹집계조회내역 테이블을 조회한다.
     * 
     * 1. 조회기간, API관리구분코드로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹집계조회내역
     * <INPUT>
     * 조회기간, API관리구분코드
     * <OUTPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * 
     * @method selectSmmInqHis
     * @method(한글명) 집계조회내역 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectSmmInqHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/selectSmmInqHis", input);
        return dao.executeQuery(); 
    }

    /**
     * - 이용기관이 사용한 API에 대한 집계정보를 UBF오픈뱅킹집계조회내역 테이블에 등록한다.
     * 
     * 1. 금융기관코드, 조회기간으로 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * UBF오픈뱅킹집계조회내역
     * <INPUT>
     * 오픈뱅킹API관리구분코드, 채널세부업무구분코드, 오픈뱅킹API응답구분코드, 전문응답일시, 오픈뱅킹이용기관수수료
     * <OUTPUT>
     * 등록결과
     * 
     * @method insertFeeHis
     * @method(한글명) 수수료내역 등록
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int insertFeeHis(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbUtzFeePrcEbc/insertFeeHis", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

}

