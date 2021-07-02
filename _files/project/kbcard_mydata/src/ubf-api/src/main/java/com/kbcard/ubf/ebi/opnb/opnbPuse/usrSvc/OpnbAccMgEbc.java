package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbAccMgEbc
 * 
 * @logicalname  : 오픈뱅킹계좌관리Ebc
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

public class OpnbAccMgEbc {

    /**
     * - UBF오픈뱅킹계좌기본,상세 테이블에 등록된 계좌를 채널별로 목록조회한다.
     * 
     * 1.오픈뱅킹계좌기본,상세 조회시 등록된 계좌 채널별로 목록조회
     * 2.응답값 리턴
     * * 금결원에 등록된 계좌목록은 등록계좌조회API를 이용하여 조회 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 채널세부업무구분코드,오픈뱅킹계좌일련번호,오픈뱅킹계좌상세일련번호,고객계좌번호,핀테크이용번호,납부자번호,오픈뱅킹계좌종류구분코드,오픈뱅킹명의구분코드,오픈뱅킹계좌상품명,오픈뱅킹이메일주소,계좌조회동의여부,출금동의여부,대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,계좌숨김일시,즐겨찾기계좌여부
     * 
     * @method selectChnPrRgAccCtg
     * @method(한글명) 채널별 등록계좌 목록조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectChnPrRgAccCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectChnPrRgAccCtg", input);
        return dao.executeQuery(); 
    }

//    /**
//     * - 오픈뱅킹 이체잔여 한도를 조회(1일 10,000,000 원 제한)
//     * 
//     * 1. 오픈뱅킹사용자고유번호로 이체잔여한도를 조회요청
//     * 2. 이체잔여한도 리턴
//     * 
//     * <관련 테이블>
//     * UBF오픈뱅킹무료거래내역
//     * 
//     * <INPUT>
//     * 오픈뱅킹사용자고유번호,거래년월일
//     * <OUTPUT>
//     * 이체잔여한도
//     * 
//     * @method selectTracRmgClamt
//     * @method(한글명) 이체잔여한도조회
//     * @param LData
//     * @return LData
//     * @throws LException 
//     */ 
//    public LData selectTracRmgClamt(LData input) throws LException {
//        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectTracRmgClamt", input);
//        return dao.executeQuery(); 
//    }

    /**
     * - 등록된 계좌의 정보를 변경
     * 
     * -이메일변경시 오픈뱅킹계좌기본수정 및 계좌변경 API호출
     * 1. 거래고유번호, 오픈뱅킹사용자일련번호, 개설기관표준코드, 계좌번호, 회차번호, 서비스구분, 변경할 이메일주소로  API 호출
     *  - 오픈뱅킹사용자일련번호, 개설기관표준코드, 계좌번호, 회차번호, 서비스구분이 일치하는 계좌에 대해 이메일주소를 변경함
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 오픈뱅킹센터로부터 응답을 받지 못했을 경우 계좌정보조회 API를 통해 처리 결과 확인 -> 정보변경 실패시 계좌정보변경 API 재호출
     * 3. 오픈뱅킹고객정보, 오픈뱅킹계좌기본 테이블의 이메일주소 변경
     * 4. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
    
     * <INPUT>
     * 거래고유번호,계좌개설은행코드,계좌납입회자,
     * 오픈뱅킹사용자고유번호,채널세부업무구분코드,고객계좌번호, 이메일주소, 대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,즐겨찾기계좌여부
     * 
     * 
     * @method updateAccInf
     * @method(한글명) 계좌정보 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/updateAccInf", input);
        return dao.executeUpdate();
    }
    

    /**
     * - 사용자가 등록한 계좌의 정보를 조회
     * 
     * 1. 오픈뱅킹사용자고유번호, 고객계좌번호, 계좌개설은행코드 입력
     * 2-1. 성공 응답
     *  - 오픈뱅킹계좌종류구분코드, 오픈뱅킹계좌상품명 리턴
     * 2-2. 오류 응답
     *  - 사용자탈퇴 API 요청 후 계좌 해지 처리전 계좌정보조회 요청하게 되면, '사용자탈퇴 처리중인 계좌(A0019)'로 조회 거부 응답
     *  - 응답코드(참가기관) 000 이면 동의, 551,555,556이면 동의거부, 이외코드 조회 불능
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 고객계좌번호, 계좌개설은행코드
     * <OUTPUT>
     * LIST
     *   - 오픈뱅킹계좌일련번호, 핀테크이용번호, 납부자번호, 오픈뱅킹계좌종류구분코드, 오픈뱅킹계좌명의구분코드, 오픈뱅킹계좌상품명, 계좌조회동의여부, 계좌조회동의등록일시, 조회등록채널세부업무구분코드, 계좌조회동의갱신일시, 조회갱신채널세부업무구분코드, 계좌조회동의해제일시, 조회해제채널세부업무구분코드, 출금동의여부, 출금등록채널세부업무구분코드, 출금동의갱신일시, 출금갱신채널세부업무구분코드, 출금동의해제일시, 출금해제채널세부업무구분코드, 계좌등록일시, 계좌사용여부, 권유자부점코드, 권유자부점명, 권유직원번호
     * 
     * @method selectAccInf
     * @method(한글명) 계좌정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectAccInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectAccInf", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 해당 채널에 등록돼있는 오픈뱅킹 계좌 목록 상세조회
     * 
     * 1. 채널세부업무구분코드, 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호로 조회 요청
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * <INPUT>
     * 채널세부업무구분코드, 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호
     * <OUTPUT>
     * LIST
     *   - 오픈뱅킹계좌상세일련번호, 오픈뱅킹플랫폼식별번호, 준회원식별자, 고객식별자, CI내용, 계좌납입회차, 개별저축은행명, 출금동의여부, 출금동의갱신일시, 출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부
     * 
     * @method selectChnPrAccInfCtg
     * @method(한글명) 채널별 계좌정보 목록조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectChnPrAccInfCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectChnPrAccInfCtg", input);
        return dao.executeQuery(); 
    }

    /**
     * - UBF오픈뱅킹계좌상세 수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 고객계좌번호,계좌개설은행코드,채널세부업무구분코드 ,오픈뱅킹이용기관코드, CI내용,대표계좌여부 ,계좌별명 ,계좌표시순서  ,계좌숨김여부 ,계좌숨김일시 ,즐겨찾기계좌여부 ,화면노출버튼구분코드 
     * <OUTPUT>
     * 
     * @method updateAccDtl
     * @method(한글명)계좌상세수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccDtl(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/updateAccDtl", input);
        return dao.executeUpdate();
    }
        
    /**
     * - 오픈뱅킹계좌상세테이블의 모든채널에 계좌가 사용하지 않을경우 센터에 등록 된 사용자의 계좌를 해지(등록 삭제 및 동의해지)
     * 
     * 1. 오픈뱅킹계좌상세 테이블의 계좌사용여부를 Y로 변경
     * 2. 오픈뱅킹계좌상세원장에 계좌사용여부 Y인 건수가 0 인지 체크 0일경우 계좌 API호출  
     * 2-1. 계좌정보로  API 호출 
     *  - 계좌개설기관코드, 계좌번호, 회차번호, 오픈뱅킹사용자일련번호
     * 2-2. 핀테크이용번호로 API 호출
     * 3. 응답정보 확인
     *  - 기 해지 사용자(551), 해당 사용자 없음(555), 사용자 미등록(556)인 경우 해지 완료로 처리
     * 4. 오픈뱅킹 고객정보 테이블, 오픈뱅킹계좌상세 정보 업데이트(해지)
     * 5. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본,UBF오픈뱅킹계좌상세
     * <INPUT>
     * 채널세부업무구분코드,고객계좌번호,오픈뱅킹계좌일련번호,오픈뱅킹플랫폼식별번호,오픈뱅킹사용자고유번호,거래고유번호,핀테크이용번호,계좌개설은행코드,계좌납입회차
     * 
     * 
     * @method deleteAcc
     * @method(한글명) 계좌 해지
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteAcc(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/deleteAcc", input);
        return dao.executeUpdate();
    }
    
    /**
     * -UBF 오픈뱅킹계좌상세고객 삭제
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * CI내용
     * 
     * 
     * @method deleteOpnbAccDtlCst
     * @method(한글명) 오픈뱅킹계좌상세고객삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteOpnbAccDtlCst(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/deleteOpnbAccDtlCst", input);  
        return dao.executeUpdate();
    }

    /**
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌상세 테이블에 대상 데이터 존재하지 않는 경우
     * 1. 거래고유번호, 등록계좌개설기관표준코드, 등록계좌번호, 회차번호, 생년월일, 사용자명, CI, 이메일주소, 서비스구분, 동의여부 및 동의 자료구분으로 API 호출
     * 2-1. 성공 응답
     *  - 출금서비스 사용자 등록시에만 출금등록거래고유번호 및 출금등록거래일자 세팅
     * 2-2. 오류 응답
     *  - 응답을 받지 못한 경우 API를 재호출하여 사용자계좌등록 상태 확인 -> 기등록된 경우 '기등록된 조회서비스용 사용자 계좌(A0324)' 또는 '기등록된 출금서비스용 사용자 계좌(A0325)'로 응답
     *  - 사용자 탈퇴 API 호출하여 계좌 해지 시 계좌 해지처리전에 동일한 계좌에 대해서 계좌 등록 요청하면 '사용자탈퇴 처리중인 서비스(A0019)'로 거부 응답. 계좌 해지 처리 후 정상 계좌 등록 가능
     * 3. 계좌정보조회 API호출
     * 4. 오픈뱅킹고객정보, 오픈뱅킹계좌기본, 오픈뱅킹 계좌 상세 테이블 적재
     * 5. 응답값 리턴
     * 
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌상세 테이블에 대상 데이터 존재하는 경우
     * - UBF오픈뱅킹계좌상세 테이블에 채널변경하여 재적재
     * - 오픈뱅킹 가져오기 기능에서 타 채널에 등록돼있는 사용자의 오픈뱅킹 계좌 정보를 현재 이용채널에 가져오기 위함
     * (ex.리브메이트 오픈뱅킹 등록계좌 없을시 KB PAY 계좌출력 , 반대의경우(리브메이터오픈뱅킹신규) 마스터원장에 Y일경우 약관동의 계좌등록절차없이 마스터원장 내용가져와서 계좌상세 반영 )
     * - 리브메이트 등록계좌 있는상태 가져오기 모두삭제하고 KBpay 기준의 등록계좌로 등록하고 출력 
     * 
     * 1. 채널세부업무구분코드, 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호 입력
     * 2. 이용중인 채널의 채널구분코드 입력
     * 3. UBF오픈뱅킹계좌상세 테이블에서 입력값에 해당하는 데이터의 채널세부업무구분코드를 이용중인 채널구분코드로 변경하여 원장에 적재
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * UBF오픈뱅킹계좌기본
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 거래래고유번호,계좌개설은행코드,고객계좌번호,계좌납인회차,생년월일,사용자명,이메일주소, 제3자제공 동의여부, 출금동의여부, 동의자료구분
     * <OUTPUT>
     * 개설기관명,개별저축은행명, 사용자고유번호,핀테크이용번호,계좌종류,출금등록
     * 
     * @method insertOpnbAccBas
     * @method(한글명) 사용자계좌 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbAccBas(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/insertOpnbAccBas", input);
        return dao.executeUpdate();
    }
    
    /**
     * - UBF오픈뱅킹계좌기본 계좌기본사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * 계좌사용여부,계좌조회동의갱신일시,조회갱신채널세부업무구분코드,계좌조회동의갱신일시,고객계좌번호,계좌개설은행코드,오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호
     * <OUTPUT>
     * 
     * @method updateAccBasUseYn
     * @method(한글명)계좌기본사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccBasUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/updateAccBasUseYn", input);
        return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹 계좌를 채널별 상세내역을 조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * <INPUT>
     * 고객계좌번호, 계좌개설은행코드, 오픈뱅킹이용기관코드, 채널세부업무구분코드,CI내용
     * <OUTPUT>
     * 오픈뱅킹계좌상세일련번호, 준회원식별자,고객식별자,오픈뱅킹플랫폼식별번호,계좌납입회차번호,개별저축은행명,출금동의여부,출금동의갱신일시 
     * 출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부, 개별저축은행코드
     * 
     * @method selectOpnbAccDtl
     * @method(한글명) 오픈뱅킹계좌상세조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectOpnbAccDtl", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * UBF오픈뱅킹계좌상세를 등록한다.
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 고객계좌번호, 계좌개설은행코드, 채널세부업무구분코드, 오픈뱅킹이용기관코드, CI내용, 오픈뱅킹계좌상세일련번호, 준회원식별자, 고객식별자, 오픈뱅킹플랫폼식별번호, 계좌납입회차번호,
     * 개별저축은행명, 출금동의여부, 출금동의갱신일시, 출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부, 개별저축은행코드 
     * <OUTPUT>
     * 
     * @method insertOpnbAccDtl
     * @method(한글명) 오픈뱅킹계좌상세등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbAccDtl(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/insertOpnbAccDtl", input);
        return dao.executeUpdate();
    }
    
    /**
     * UBF오픈뱅킹계좌상세를 채널별 등록 한다.
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 고객계좌번호, 계좌개설은행코드, 채널세부업무구분코드, 오픈뱅킹이용기관코드, CI내용, 오픈뱅킹계좌상세일련번호, 준회원식별자, 고객식별자, 오픈뱅킹플랫폼식별번호, 계좌납입회차번호,
     * 개별저축은행명, 출금동의여부, 출금동의갱신일시, 출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부, 개별저축은행코드 
     * <OUTPUT>
     * 
     * @method insertOpnbAccDtl
     * @method(한글명) 오픈뱅킹계좌상세등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbAccDtlChnPr(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/insertOpnbAccDtlChnPr", input);
        return dao.executeUpdate();
    }

    /**
     * - UBF오픈뱅킹계좌상세 계좌상세사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 계좌사용여부,계좌조회동의갱신일시,조회갱신채널세부업무구분코드,계좌조회동의갱신일시,고객계좌번호,계좌개설은행코드,오픈뱅킹이용기관코드, 오픈뱅킹계좌일련번호
     * <OUTPUT>
     * 
     * @method updateAccDtlUseYn
     * @method(한글명)계좌상세사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateAccDtlUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/updateAccDtlUseYn", input);
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
     * @method selectOpnbAccDtlRgAccnt
     * @method(한글명) 오픈뱅킹계좌상세등록계좌수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbAccDtlRgAccnt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectOpnbAccDtlRgAccnt", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 채널별등록계좌상세조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본 , UBF오픈뱅킹계좌상세
     * <INPUT>
     * 고객계좌번호, 고객계좌번호, 계좌개설은행코드,오픈뱅킹이용기관코드,CI내용,계좌상세사용여부
     * <OUTPUT>
     * 등록계좌수
     * 
     * @method selectChnPrRgAccDtl
     * @method(한글명) 채널별등록계좌상세조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectChnPrRgAccDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectChnPrRgAccDtl", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹채널별약관동의여부조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객동의이력
     * <INPUT>
     * 오픈뱅킹사용자고유번호,채널세부업무구분코드,오픈뱅킹약관동의구분코드,오픈뱅킹동의연장구분코드
     * <OUTPUT>
     * 존재여부
     * 
     * @method selectOpnbChnPrStplCnsYn
     * @method(한글명) 오픈뱅킹채널별약관동의여부조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbChnPrStplCnsYn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbAccMgEbc/selectOpnbChnPrStplCnsYn", input);
        return dao.executeQueryOnlySingle(); 
    }

}

