package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbUsrMgEbc
 * 
 * @logicalname  : 오픈뱅킹사용자관리Ebc
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

public class OpnbUsrMgEbc {

    /**
     * - 오픈뱅킹등록여부조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용] 항목으로 요청
     * 2. UBF오픈뱅킹고객정보기본 테이블에 존재하는지 확인
     * 3-1. 존재시 
     *  - 등록여부 TRUE 리턴
     * 3-2. 미존재시
     *  - 등록여부 FALSE 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 등록여부(T/F)
     * 
     * @method selectRgYn
     * @method(한글명) 등록여부 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectRgYn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectRgYn", input);
        return dao.executeQueryOnlySingle(); 
    } 

    /**
     * 1. 고객식별자/준회원식별자/개인고객번호/고객명/CI내용 등으로 API 호출
     * 2. 오픈뱅킹사용자고유번호 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 오픈뱅킹사용자고유번호
     * 
     * @method selectUsrUno
     * @method(한글명) 사용자고유번호조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectUsrUno(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectUsrUno", input);
        return dao.executeQuery();
    }

    /**
     * - 사용자의 오픈뱅킹등록여부(등록여부조회 API)가 Y일 때 등록한 채널명, 채널구분코드 리턴
     * - 오픈뱅킹을 타 채널에서 등록한 사용자에게 채널명을 화면에 표시하기 위함
     * ex) 고객님은 현재 'KBPay'에서 오픈뱅킹을 사용 중입니다. 사용중이던 오픈뱅킹 정보를 가져올까요?
     * 
     * 1. 오픈뱅킹사용자고유번호로 API 호출
     * 2. 채널명, 채널구분코드 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * LIST
     *   - 채널명, 채널구분코드
     * 
     * @method selectRgChnCtg
     * @method(한글명) 등록채널목록조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectRgChnCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectRgChnCtg", input);
        return dao.executeQuery(); 
    }

    /**
     * - UBF오픈뱅킹고객정보기본 원장에 적재
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 고객식별자 , 준회원식별자, 고객명, CI내용, 오픈뱅킹주민등록번호, 오픈뱅킹사용자휴대폰번호, 성별구분코드, 고객출생년월일
     * 오픈뱅킹이메일주소, 오픈뱅킹계좌등록수, 오픈뱅킹카드등록수, 제3자정보제공동의여부, 오픈뱅킹출금동의여부, 오픈뱅킹통합계좌조회동의여부, 제3자정보제공동의만기일시
     * 오픈뱅킹출금동의만기일시, 오픈뱅킹계좌조회동의만기일시, 오픈뱅킹서비스해지여부
     * 
     * <OUTPUT>
     * 개설기관명,개별저축은행명, 사용자고유번호,핀테크이용번호,계좌종류,출금등록
     * 
     * @method insertOpnbCstInfBas
     * @method(한글명) 오픈뱅킹고객정보기본등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbCstInfBas(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/insertOpnbCstInfBas", input);
        return dao.executeUpdate();
    }
    
    /**
     * - UBF오픈뱅킹고객정보기본삭제
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * 
     * <OUTPUT>
     * 
     * @method deleteOpnbCstBas
     * @method(한글명) 오픈뱅킹고객정보기본삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteOpnbCstBas(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/deleteOpnbCstBas", input);
        return dao.executeUpdate();
    }
    
    /**
     * - UBF오픈뱅킹고객정보기본
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,오픈뱅킹통합계좌조회동의여부, 계좌조회동의여부, 출금동의여부
     * 카드정보조회동의여부, 통합계좌조회동의해제일시, 계좌조회동의해제일시, 출금동의해제일시, 카드정보조회동의해제일시, 오픈뱅킹서비스해지여부 
     * 
     * <OUTPUT>
     * 
     * @method uptOpnbCstCnsYn
     * @method(한글명) 오픈뱅킹고객동의여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbCstCnsYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/uptOpnbCstCnsYn", input);
        return dao.executeUpdate();
    }

    /**
     * - UBF오픈뱅킹계좌상세 계좌상세사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 계좌사용여부, 채널세부업무구분코드, 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 
     * @method updateAccDtlUseYn
     * @method(한글명)계좌상세사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccDtlUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/updateAccDtlUseYn", input);
        return dao.executeUpdate();
    }
    
    /**
     * - UBF오픈뱅킹카드고객상세 카드고객상세사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * 
     * <INPUT>
     * 카드정보조회동의여부, 채널세부업무구분코드, CI내용
     * <OUTPUT>
     * 
     * @method updateAccDtlUseYn
     * @method(한글명)카드상세사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdDtlUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/updateCrdDtlUseYn", input);
        return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹계좌상세등록계좌수조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * <INPUT>
     * CI내용,계좌상세사용여부
     * <OUTPUT>
     * 등록계좌수
     * 
     * @method selectOpnbAccDtlRgAccnt
     * @method(한글명) 오픈뱅킹계좌상세등록계좌수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccDtlRgAccnt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectOpnbAccDtlRgAccnt", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹카드고객상세등록카드고객수조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * CI내용,카드정보조회동의여부
     * <OUTPUT>
     * 등록카드고객수
     * 
     * @method selectOpnbCrdCstDtlRgCrdCstCnt
     * @method(한글명) 오픈뱅킹카드고객상세등록카드고객수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdCstDtlRgCrdCstCnt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectOpnbCrdCstDtlRgCrdCstCnt", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - UBF오픈뱅킹계좌기본 계좌기본사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 
     * @method updateAccBasUseYn
     * @method(한글명)계좌기본사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccBasUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/updateAccBasUseYn", input);
        return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹계좌상세등록계좌수조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 고객계좌번호, 고객계좌번호, 계좌개설은행코드,오픈뱅킹이용기관코드,CI내용,계좌상세사용여부
     * <OUTPUT>
     * 등록계좌수
     * 
     * @method selectOpnbAccCrdUseAblNcn
     * @method(한글명) 오픈뱅킹계좌기본등록계좌수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccCrdUseAblNcn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectOpnbAccCrdUseAblNcn", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * 오픈뱅킹사용자고유번호로 CI내용조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * CI내용
     * 
     * @method selectCiCtt
     * @method(한글명) CI내용조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCiCtt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/selectCiCtt", input);
        return dao.executeQueryOnlySingle();
    }

    /**
     * - UBF오픈뱅킹카드고객기본 카드고객기본사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 
     * @method updateCrdCstBasUseYn
     * @method(한글명)카드고객기본사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdCstBasUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbUsrMgEbc/updateCrdCstBasUseYn", input);
        return dao.executeUpdate();
    }
}

