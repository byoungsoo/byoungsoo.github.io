package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCfreUtzPlcyMgEbc
 * 
 * @logicalname  : 오픈뱅킹무료이용정책관리Ebc
 * @author       : 정영훈
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       정영훈       최초 생성
 *
 */

public class OpnbCfreUtzPlcyMgEbc {

    /**
     *  - 오픈뱅킹무료이용 정책 등록
     * 
     * 1. 무료정책정보를 등록 요청
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  채널세부업무구분코드, 오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹 무료정책시작일시,오픈뱅킹무료정책종료일시,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용,오픈뱅킹무제한여부,오픈뱅킹기간구분코드,오픈뱅킹무료제공건수,오픈뱅킹타스템연계여부
     * 
     * @method insertCfreUtzPlcy
     * @method(한글명) 무료이용정책 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertCfreUtzPlcy(LData input) throws LException {
        //LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/insertCfreUtzPlcy", input);        	
       	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/insertOpnbCfreUtzPlcyBas", input);
        return dao.executeUpdate();            
    }
    
    /**
     *  - 오픈뱅킹무료이용 정책 일련번호 채번조회
     * 
     * 1. 오픈뱅킹무료정책일련번호 를 채번하여 조회한다.
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  
     * <OUTPUT>
     * 오픈뱅킹무료정책일련번호
     * @method selectOpnbCfrePlcySnoNbr
     * @method(한글명) 오픈뱅킹무료정책일련번호채번조회
     * @param 
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCfrePlcySnoNbr(LData input) throws LException {      	
       	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectOpnbCfrePlcySnoNbr", input);
        return dao.executeQueryOnlySingle();            
    }
    
    /**
     *  - 오픈뱅킹무료이용정책일련번호조회
     * 
     * 1. 오픈뱅킹무료정책일련번호을 조회한다.
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  
     * <OUTPUT>
     * 오픈뱅킹무료정책일련번호
     * @method selectOpnbChnPrCfrePlcySno
     * @method(한글명) 오픈뱅킹무료정책일련번호조회
     * @param 
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbChnPrCfrePlcySno(LData input) throws LException {      	
       	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectOpnbChnPrCfrePlcySno", input);
        return dao.executeQueryOnlySingle();            
    }

    

    /**
     *  - 오픈뱅킹무료이용 정책 목록조회
     * 
     * 1. 무료정책 목록조회를 요청
     * 2. 정책정보 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  오픈뱅킹무료정책시작일시 ,오픈뱅킹무료정책종료일시
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹무료정책일련번호,채널세부업무구분코드,오픈뱅킹무료정책명,오픈뱅킹무료제공건수,무료정책시작일시,무료정책종료일시
     * 
     * @method selectCfreUtzPlcyCtg
     * @method(한글명) 무료이용정책 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectCfreUtzPlcyCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectCfreUtzPlcyCtg", input);
        return dao.executeQuery(); 
    }

    /**
     * - 무료이용정책 상세조회
     * 
     * 1. 오픈뱅킹무료정책일련번호로 상세조회 요청
     * 2. 무료정책 상세정보 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     * 오픈뱅킹무료정책일련번호
     * <OUTPUT>
     * 오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹 무료정책시작일시,오픈뱅킹무료정책종료일시,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용,오픈뱅킹무제한여부,오픈뱅킹기간구분코드,오픈뱅킹무료제공건수,오픈뱅킹타스템연계여부
     * 
     * @method selectCfreUtzPlcyDtl
     * @method(한글명) 무료이용정책 상세 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCfreUtzPlcyDtl(LData input) throws LException {
    	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectCfreUtzPlcyDtl", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 현재시점에 유효한 오픈뱅킹을 무료로 정책 건수를 조회
     * 
     * 1. 오픈뱅킹사용자고유번호로 무료거래 잔여건수를 조회요청
     * 2. 잔여건수 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객별무료거래내역
     * 
     * <INPUT>
     * 거래일시,채널세부구분코드
     * <OUTPUT>
     * 무료거래잔여건수
     * 
     * @method selectOpnbVldCfrePlcyCtg
     * @method(한글명) 오픈뱅킹유효무료정책목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LMultiData selectOpnbVldCfrePlcyCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectOpnbVldCfrePlcyCtg", input);
        return dao.executeQuery(); 
    }
    
    /**
     *  - 오픈뱅킹무료이용 정책 변경
     * 
     * 1. 무료정책정보를 변경 요청
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  채널세부업무구분코드, 오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹 무료정책시작일시,오픈뱅킹무료정책종료일시,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용,오픈뱅킹무제한여부,오픈뱅킹기간구분코드,오픈뱅킹무료제공건수,오픈뱅킹타스템연계여부
     * 
     * @method updateCfreUtzPlcy
     * @method(한글명) 무료이용정책 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCfreUtzPlcy(LData input) throws LException {
       	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/updateCfreUtzPlcy", input);
        return dao.executeUpdate();            
    }

    /**
     *  - 오픈뱅킹 무제한거래 대상에 해당하는 정책일련번호를  조회한다.
     * 
     * 1. 무제한여부가 Y인 정책을 조회한다.
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  
     * <OUTPUT>
     * 오픈뱅킹무제한거래대상정책조회
     * @method selectOpnbNbRccTrTgPlcy
     * @method(한글명) 오픈뱅킹무제한거래대상정책조회
     * @param 
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbNbRccTrTgPlcy(LData input) throws LException {      	
       	LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbCfreUtzPlcyMgEbc/selectOpnbNbRccTrTgPlcy", input);
        return dao.executeQueryOnlySingle();            
    }
    
}

