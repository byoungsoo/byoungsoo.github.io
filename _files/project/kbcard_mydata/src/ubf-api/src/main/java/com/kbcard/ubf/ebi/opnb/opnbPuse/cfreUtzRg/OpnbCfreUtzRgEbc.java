package com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCfreUtzRgEbc
 * 
 * @logicalname  : 오픈뱅킹무료이용등록관리Ebc
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

public class OpnbCfreUtzRgEbc {

    /**
     * - 오픈뱅킹 무료거래내역등록
     * 
     * 1. 송금거래시 오픈뱅킹사용자고유번호와 거래정보로 거래내역 등록을 요청
     * 2. 고객별무료거래정보를 조회하여 무료이용잔여 건수가 0보다 크면 무료정책
     *     적용여부를 Y 를 세팅하고 무료정책식별번호를 세팅 하여 등록
     * 3. 고객별무료거래정보에 무료건수 차감
     * 4. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * 
     * <INPUT>
     * 오픈뱅킹전문거래년월일,참가기관출금거래고유번호,참가기관입금거래고유번호,채널세부업무구분코드,오픈뱅킹사용자고유번호,오픈뱅킹거래금액,오픈뱅킹무료정책일련번호,무료정책적용여부
     * 
     * @method insertTrHis
     * @method(한글명) 거래 내역 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertTrHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/cfreUtzRg/opnbCfreUtzRgEbc/insertOpnbCstPrCfreTnhs", input);        
        return dao.executeUpdate();
    }
  
    /**
     * - 오픈뱅킹무료거래내역 목록조회
     * 
     * 1. 오픈뱅킹사용자고유번호로 무료거래내역 목록조회 요청
     * 2. 무료거래내역 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * UBF오픈뱅킹고객별무료거래내역
     * UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * 거래시작일자 , 거래종료일자 , 오픈뱅킹사용자고유번호,채널세부구분코드
     * <OUTPUT>
     * 오픈뱅킹거래년월,출금기관명,입금기관명,오픈뱅킹거래금액,무료정책일련번호
     * 
     * @method selectCfreTrHisCtg
     * @method(한글명) 무료거래 내역 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectCfreTrHisCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/cfreUtzRg/opnbCfreUtzRgEbc/selectCfreTrHisCtg", input);
        return dao.executeQuery(); 
    }

    /**
     * - 오픈뱅킹무료거래내역 상세조회
     * 
     * 1. 오픈뱅킹사용자고유번호와 거래고유번호로 무료거래내역 상세조회 요청
     * 2. 상세정보 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * UBF오픈뱅킹고객별무료거래내역
     * UBF오픈뱅킹무료이용정책기본
     * UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,채널세부구분코드,무료정책일련번호,거래년월일
     * <OUTPUT>
     * 오픈뱅킹거래년월일,출금기관명,입금기관명,오픈뱅킹거래금액,오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용
     * 
     * @method selectCfreTrHisDtl
     * @method(한글명) 무료거래 내역 상세 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCfreTrHisDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/cfreUtzRg/opnbCfreUtzRgEbc/selectCfreTrHisDtl", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹을 무료로 거래할 수 있는 잔여건수를 조회
     * 
     * 1. 오픈뱅킹사용자고유번호로 무료거래 잔여건수를 조회요청
     * 2. 잔여건수 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객별무료거래내역
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,채널세부구분코드
     * <OUTPUT>
     * 무료거래잔여건수
     * 
     * @method selectCfreTrRmgNcn
     * @method(한글명) 무료거래 잔여 건수 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCfreTrRmgNcn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/cfreUtzRg/opnbCfreUtzRgEbc/selectCfreTrRmgNcn", input);
        return dao.executeQueryOnlySingle(); 
    }
   

    /**
     * - 오픈뱅킹무료거래내역 존재여부를 조회한다.
     * 
     * 1. 오픈뱅킹무료거래내역 존재여부를 조회요청
     * 2. 잔여건수 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객별무료거래내역
     * 
     * <INPUT>
     * 오픈뱅킹무료정책일련번호
     * <OUTPUT>
     * 존재여부
     * 
     * @method selectCfreTnhsExstYn
     * @method(한글명) 무료거래내역존재여부조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCfreTnhsExstYn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/cfreUtzRg/opnbCfreUtzRgEbc/selectCfreTnhsExstYn", input);
        return dao.executeQueryOnlySingle(); 
    }
    
}

