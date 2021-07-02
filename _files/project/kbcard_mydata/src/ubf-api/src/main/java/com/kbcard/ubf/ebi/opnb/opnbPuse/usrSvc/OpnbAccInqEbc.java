package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbAccInqEbc
 * 
 * @logicalname  : 오픈뱅킹계좌조회Ebc
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

public class OpnbAccInqEbc {

    /**
     * - 사용자 계좌의 잔액 정보 조회
     * 
     * 1. 실 계좌번호로 계좌의 잔액 정보 조회 요청(자체인증 이용기관만 이용가능)
     * 2. 오픈뱅킹으로부터 전달받은 응답 정보 오픈뱅킹기관거래내역 원장에 적재
     * 3. 오픈뱅킹으로부터 전달받은 잔액 정보 및 계좌 종류, 상품명 함께 전달
     * 4. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 개설기관코드, 계좌번호
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지, 기관응답코드, 기관응답메시지, 개설기관명, 개설저축은행명, 계좌잔액, 출금가능금액, 계좌종류구분코드, 계좌상품명, 계좌개설일, 만기일, 최종거래일자
     * 
     * @method selectAcnoPrBl
     * @method(한글명) 계좌번호 별 잔액 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectAcnoPrBl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/selectAcnoPrBl", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 사용자 계좌의 잔액 정보 조회
     * 
     * 1. 요청고객의 등록계좌의 모든 잔액을 조회.
     * 2. 등록 계좌번호로 계좌의 잔액 정보 조회 요청(자체인증 이용기관만 이용가능)
     * 3. 오픈뱅킹으로부터 전달받은 응답 정보 이용기관거래내역 원장에 적재
     * 4. 오픈뱅킹으로부터 전달받은 잔액 정보 및 계좌 종류, 상품명 함께 전달
     * 5. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI번호
     * 
     * <OUTPUT>
     * LIST
     * - 개설기관명, 개설저축은행명, 계좌잔액, 출금가능금액, 계좌종류구분코드, 계좌상품명, 계좌개설일, 만기일, 최종거래일자
     * 
     * @method retvLstAcnoPrWhlBl
     * @method(한글명) 계좌번호 별 전체잔액 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LMultiData retvLstAcnoPrWhlBl(LData input) throws LException {
        //LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/retvLstAcnoPrWhlBl", input);
        //return dao.executeQuery(); 
		LPagingCommonDao dao = new LPagingCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/retvLstAcnoPrWhlBl", input);
		return dao.executeQueryForScrollPage();
    }

    /**
     * - 사용자보유 계좌건수 조회
     * 
     * 1. 요청고객의 등록계좌의 모든 잔액을 조회.
     * 2. 등록 계좌번호로 계좌의 잔액 정보 조회 요청(자체인증 이용기관만 이용가능)
     * 3. 오픈뱅킹으로부터 전달받은 응답 정보 이용기관거래내역 원장에 적재
     * 4. 오픈뱅킹으로부터 전달받은 잔액 정보 및 계좌 종류, 상품명 함께 전달
     * 5. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호
     * 
     * <OUTPUT>
     * - 계좌건수
     * 
     * @method retvAcnoCntPrWhlBl
     * @method(한글명) 보유계좌건수 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAcnoCntPrWhlBl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/retvAcnoCntPrWhlBl", input);
        return dao.executeQueryForSingle(); 
    }

    /**
     * - 사용자가 등록한 계좌의 거래내역 조회
     * 
     * 1. 오픈뱅킹사용자고유번호, 입출금금융기관코드드, 고객계좌번호, 조회기간 입력
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹이체내역
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 입출금금융기관코드, 고객계좌번호, 조회시작일자, 조회종료일자
     * <OUTPUT>
     * LIST
     *   - 입출금구분코드, 입출금금액, 입출금계좌인자내용, 입출금요청고객명, 입출금거래일시
     * 
     * @method selectTrHis
     * @method(한글명) 거래 내역 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectTrHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbi/opnbAccBas/retvLstAcnoPrWhlBl", input);
        return dao.executeQuery(); 
    }
    
    /**
     * - 오픈뱅킹계좌기본상세조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * - 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호, 오픈뱅킹사용자고유번호 
     * 
     * <OUTPUT>
     * - 핀테크이용번호, 납부자번호, 오픈뱅킹계좌종류구분코드, 오픈뱅킹계좌명의구분코드, 오픈뱅킹계좌상품명, 오픈뱅킹이메일주소, 계좌조회동의여부, 계좌조회동의등록일시, 조회등록채널세부업무구분코드
     *   계좌조회동의갱신일시, 조회갱신채널세부업무구분코드, 계좌조회동의해제일시, 조회해제채널세부업무구분코드, 출금동의여부, 출금등록채널세부업무구분코드, 출금동의갱신일시, 출금갱신채널세부업무구분코드
     *   출금동의해제일시, 출금해제채널세부업무구분코드, 계좌등록일시, 계좌사용여부, 권유자부점코드, 권유자부점명, 권유직원번호 
     * 
     * @method selectOpnbAccBasDtl
     * @method(한글명) 오픈뱅킹계좌기본상세조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccBasDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/selectOpnbAccBasDtl", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹계좌사용여부조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * - 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 계좌사용여부 
     * 
     * <OUTPUT>
     * - 오픈뱅킹계좌일련번호 
     * 
     * @method selectOpnbAccUseYnInq
     * @method(한글명) 오픈뱅킹계좌사용여부조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccUseYnInq(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/selectOpnbAccUseYnInq", input);
        return dao.executeQueryOnlySingle(); 
    }

    
    /**
     * - 오픈뱅킹계좌일련번호채번조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * 
     * <OUTPUT>
     * - 오픈뱅킹계좌일련번호 
     * 
     * @method selectOpnbAccSnoBbr
     * @method(한글명) 오픈뱅킹계좌일련번호채번조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccSnoBbr(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/selectOpnbAccSnoBbr", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹사용여부가 Y인 계좌를 조회한다.
     * 
     * 1. 오픈뱅킹사용자고유번호
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 오픈뱅킹사용자고우번호
     * <OUTPUT>
     * LIST
     *   - 고객계좌번호 , 계좌개설은행코드 , 오픈뱅킹이용기관코드
     * 
     * @method selectOpnbUseAblAcc
     * @method(한글명) 오픈뱅킹사용가능계좌조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectOpnbUseAblAcc(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccInqEbc/selectOpnbUseAblAcc", input);
        return dao.executeQuery(); 
    }

}

