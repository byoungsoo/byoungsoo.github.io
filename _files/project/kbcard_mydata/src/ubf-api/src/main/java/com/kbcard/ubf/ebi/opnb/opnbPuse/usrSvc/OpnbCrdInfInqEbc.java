package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCrdInfInqEbc
 * 
 * @logicalname  : 오픈뱅킹카드정보조회Ebc
 * @author       : 박건우
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       박건우       최초 생성
 *
 */

public class OpnbCrdInfInqEbc {

    /**
     * - 요청자의 카드사별 오픈뱅킹이용동의(제3자정보제공동의) 등록이 되있는 카드사의 보유카드 정보 조회
     * 
     * 1. 오픈뱅킹사용자고유번호를 이용하여 오픈뱅킹보유카드정보기본 원장에 있는 요청자의 보유카드 정보 조회
     * 2. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드발급기관기본
     *  UBF오픈뱅킹보유카드정보기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드
     * <OUTPUT>
     *  LIST
     *  - 오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드, 오픈뱅킹카드일련번호, 오픈뱅킹개설기관코드, 오픈뱅킹카드식별자, 오픈뱅킹마스킹카드번호, 카드상품명, 가족카드여부, 오픈뱅킹카드구분코드, 결제은행코드, 오픈뱅킹마스킹계좌번호, 카드발급년월일, 오픈뱅킹카드결제일
     * 
     * @method selectCrdCtg
     * @method(한글명) 카드 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectCrdCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/selectCrdCtg", input);
        return dao.executeQuery(); 
    }
    
    /**
     * - 요청자의 카드사별 오픈뱅킹이용동의(제3자정보제공동의) 등록이 되있는 카드사의 보유카드 정보 원장 적재
     * 
     * 1. 오픈뱅킹사용자고유번호를 이용하여 오픈뱅킹보유카드정보기본 원장에 있는 요청자의 보유카드 정보 조회
     * 2. 조회된 응답값 UBF오픈뱅킹보유카드정보기본 원장 적재
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드발급기관기본
     *  UBF오픈뱅킹보유카드정보기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드
     * <OUTPUT>
     *  
     * 
     * @method insertCrdCtg
     * @method(한글명) 카드 목록 등록
     * @param LData
     * @return int
     * @throws LException 
     */ 
    public int insertCrdCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/insertCrdCtg", input);
        return dao.executeUpdate(); 
    }

    
    
    /**
     * - 카드 식별자를 이용하여 해당 카드의 (신용/체크)카드 구분, 결제계좌 등의 카드 기본정보 조회
     * 
     * 1. 카드기본정보조회 API 호출
     * 2. 오픈뱅킹카드정보기본 원장 내 응답값과 일치하는 카드 식별자를 가지고 있는 동일 정보의 상세 속성 업데이트
     * 3. 조회된 응답값 리턴
     * 
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드정보기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 카드식별자
     * <OUTPUT>
     *  오픈뱅킹카드구분코드, 결제은행코드, 결제 계좌번호, 오픈뱅킹마스킹계좌번호, 카드발급년월일
     * 
     * @method updateCrdBasInf
     * @method(한글명) 카드기본정보 수정
     * @param int
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdBasInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/updateCrdBasInf", input);
        return dao.executeUpdate(); 
    }

    /**
     * - 오픈뱅킹센터에 등록된 사용자의 월별 대금 청구 목록을 카드사별로 조회
     * - 조회 당월을 포함하여 최대 13개월의 청구내역을 조회
     * - 결제년월일 기준 내림차순으로 조회
     * 
     * 1. 카드청구기본정보조회 API 호출
     * 2. 오픈뱅킹으로부터 조회된 카드 청구 기본 정보 오픈뱅킹카드청구기본 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드청구기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 조회시작월, 조회종료월
     * <OUTPUT>
     *  LIST
     *   - 청구년월, 카드결제일련번호(결제순번), 오픈뱅킹카드식별자, 청구금액, 오픈뱅킹카드결제일, 실제결제년월일, 오픈뱅킹카드구분코드
     * 
     * 
     * 
     * @method selectCrdBilBasInf
     * @method(한글명) 카드청구기본정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCrdBilBasInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/selectCrdBilBasInf", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹센터에 등록된 사용자의 월별 대금 청구 목록을 카드사별로 조회
     * - 조회 된 청구기본정보를 [오픈뱅킹카드청구기본] 원장에 적재
     * 
     * 1. 카드청구기본정보조회 API 호출
     * 2. 오픈뱅킹으로부터 조회된 카드 청구 기본 정보 오픈뱅킹카드청구기본 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드청구기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 조회시작월, 조회종료월
     * <OUTPUT>
     *  LIST
     *   - 청구년월, 카드결제일련번호(결제순번), 오픈뱅킹카드식별자, 청구금액, 오픈뱅킹카드결제일, 실제결제년월일, 오픈뱅킹카드구분코드
     * 
     * 
     * 
     * @method insertCrdBilBasInf
     * @method(한글명) 카드청구기본정보 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertCrdBilBasInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/insertCrdBilBasInf", input);
        return dao.executeUpdate();
    }

    /**
     * - 오픈뱅킹센터에 등록된 사용자의 카드 청구 세부항목을 조회
     * - (신용/체크)카드 청구기본정보보회 API 응답전문의 청구년월(charge_month)과 결제순번(settlement_seq_no)을 기준으로 조회
     * - 조회당월을 포함하여 최대 13개월의 청구 거래내역을 청구년월 기준으로 조회
     * - 사용일시 기준 내림차순으로 조회
     * 
     * 1. 카드청구상세정보조회 요청
     * 2. 오픈뱅킹으로부터 조회된 카드 청구 상세 정보 오픈뱅킹카드청구상세 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드청구상세
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 청구년월, 카드결제일련번호(결제순번)
     * <OUTPUT>
     *  LIST
     * - 오픈뱅킹카드식별번호(카드식별값), 카드사용년월일, 오픈뱅킹카드사용시각, 카드이용금액, 오픈뱅킹마스킹가맹점명, 신용판매거래수수료, 오픈뱅킹카드상품구분코드
     * 
     * @method selectCrdBilDtlInf
     * @method(한글명) 카드청구상세정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCrdBilDtlInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/selectCrdBilDtlInf", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹카드청구상세일련번호채번조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹카드청구상세
     * 
     * <INPUT>
     * 
     * <OUTPUT>
     * - 오픈뱅킹카드청구상세일련번호
     * 
     * @method selectOpnbCrdSnoBbr
     * @method(한글명) 오픈뱅킹카드청구상세일련번호채번조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdBilDtlSnoBbr(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/selectOpnbCrdBilDtlSnoBbr", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹센터에 등록된 사용자의 카드 청구 세부항목을 조회
     * - 조회된 카드청구상세 정보를 [오픈뱅킹카드청구상세] 원장에 적재
     * 
     * 1. 카드청구상세정보조회 요청
     * 2. 오픈뱅킹으로부터 조회된 카드 청구 상세 정보 오픈뱅킹카드청구상세 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드청구상세
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 청구년월, 카드결제일련번호(결제순번)
     * <OUTPUT>
     *  LIST
     * - 오픈뱅킹카드식별번호(카드식별값), 카드사용년월일, 오픈뱅킹카드사용시각, 카드이용금액, 오픈뱅킹마스킹가맹점명, 신용판매거래수수료, 오픈뱅킹카드상품구분코드
     * 
     * @method insertCrdBilDtlInf
     * @method(한글명) 카드청구상세정보 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertCrdBilDtlInf(LData input) throws LException {
    	LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdInfInqEbc/insertCrdBilDtlInf", input);
        return dao.executeUpdate();
    }

}

