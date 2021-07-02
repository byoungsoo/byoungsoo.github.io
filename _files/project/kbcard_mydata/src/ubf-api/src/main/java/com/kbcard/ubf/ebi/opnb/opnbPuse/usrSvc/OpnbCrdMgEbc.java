package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbCrdMgEbc
 * 
 * @logicalname  : 오픈뱅킹카드관리Ebc
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

public class OpnbCrdMgEbc {

    /**
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹발급기관기본, UBF오픈뱅킹카드발급기관상세 테이블에 대상 데이터 존재하지 않는 경우
     * - 카드정보조회(제3자정보제공동의)를  요청한 사용자를 등록
     * 
     * 1. 카드사용자등록 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 응답받지 못한 경우 API재호출하여 사용자 등록상태 확인 -> 기등록된 경우, '기등록된 조회서비스용 사용자 서비스(A0324)'로 응답
     *  - 사용자 탈퇴 API 호출하여 카드사용자등록 해지 시 카드 사용자 등록 해지처리전에 동일한 계좌에 대해서 계좌 등록 요청하면 '사용자탈퇴 처리중인 서비스(A0019)'로 거부 응답. 사용자 등록 해지 처리 후 정상 계좌 등록 가능
     * 3. 오픈뱅킹고객정보기본 원장에 응답받은 사용자일련번호와 동일한 오픈뱅킹사용자고유번호가 존재하는지 식별 후 존재하지 않는다면 응답받은 사용자일련번호를 이용하여 오픈뱅킹 고객정보 적재
     * 4. 오픈뱅킹카드발급기관기본, 오픈뱅킹카드발급기관 원장에 동의정보 적재
     * 5. 응답값 리턴
     * 
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹발급기관기본, UBF오픈뱅킹카드발급기관상세 테이블에 대상 데이터 존재하는 경우
     * - 오픈뱅킹 가져오기 기능에서 타 채널에 등록돼있는 사용자의 오픈뱅킹 카드 조회 동의 정보를 현재 이용채널에 가져오기 위함
     * 
     * 1. 이용중인 채널의 채널구분코드 입력
     * 2. 오픈뱅킹발급기관상세 원장에서 입력값에 해당하는 데이터의 채널세부업무구분코드를 이용중인 채널구분코드로 변경하여 원장에 적재
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본
     *  UBF오픈뱅킹카드발급기관기본
     *  UBF오픈뱅킹카드발급기관상세
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 회원금융회사코드, 사용자명, CI, 이메일주소, 서비스구분(cardinfo), 제3자정보제공동의여부, 채널구분코드, 오픈뱅킹플랫폼식별번호
     * <OUTPUT>
     * 
     * @method insertCrdUsr
     * @method(한글명) 카드사용자 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertCrdUsr(LData input) throws LException {
    	LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/insertCrdUsr", input);
    	return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹카드사용여부조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹카드고객기본
     * 
     * <INPUT>
     * - 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 카드정보조회동의여부 
     * 
     * <OUTPUT>
     * - 오픈뱅킹사용자고유번호 
     * 
     * @method selectOpnbCrdUseYnInq
     * @method(한글명) 오픈뱅킹카드사용여부조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdUseYnInq(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdUseYnInq", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹카드사용여부수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객기본
     * 
     * <INPUT>
     * 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 카드정보조회동의여부
     * <OUTPUT>
     * 
     * @method updateCrdBasUseYn
     * @method(한글명) 오픈뱅킹카드사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdBasUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/updateCrdBasUseYn", input);
        return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹카드고객기본조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드, 오픈뱅킹개설기관코드, 카드정보조회동의여부
     * <OUTPUT>
     * 오픈뱅킹사용자고유번호
     * 
     * @method selectOpnbCrdBas
     * @method(한글명) 오픈뱅킹카드고객기본조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdBas(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdBas", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 해당 요청자의 카드정보조회동의여부가 Y인 카드사 전체조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 카드정보조회동의여부
     * <OUTPUT>
     * 오픈뱅킹회원금융회사코드
     * 
     * @method selectOpnbCrdCstBasWhlInq
     * @method(한글명) 오픈뱅킹카드고객기본전체조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectOpnbCrdCstBasWhlInq(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdCstBasWhlInq", input);
        return dao.executeQuery();
    }
    
    /**
     * - 오픈뱅킹카드고객상세조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹회원금융회사코드, 채널세부업무구분코드, CI내용
     * <OUTPUT>
     * 채널세부업무구분코드, 오픈뱅킹회원금융회사코드, CI내용, 오픈뱅킹플랫폼식별번호, 준회원식별자, 고객식별자,
     * 카드정보조회동의여부, 카드정보조회동의갱신일시
     * 
     * @method selectOpnbCrdDtl
     * @method(한글명) 오픈뱅킹카드고객상세조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdDtl", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹계좌상세등록카드사수조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹회원금융회사코드, CI내용, 카드정보조회동의여부
     * <OUTPUT>
     * 등록카드사수
     * 
     * @method selectOpnbCrdDtlRgAccnt
     * @method(한글명) 오픈뱅킹계좌상세등록카드사수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdDtlRgAccnt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdDtlRgAccnt", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹계좌상세채널별등록카드사수조회
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * CI내용, 카드정보조회동의여부, 채널세부업무구분코드
     * <OUTPUT>
     * 등록카드사수
     * 
     * @method selectOpnbCrdDtlChnRgAccnt
     * @method(한글명) 오픈뱅킹계좌상세등록카드사수조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdDtlChnRgAccnt(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdDtlChnRgAccnt", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹카드고객상세등록
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹회원금융회사코드, 채널세부업무구분코드, CI내용
     * <OUTPUT>
     * 채널세부업무구분코드, 오픈뱅킹회원금융회사코드, CI내용, 오픈뱅킹플랫폼식별번호, 준회원식별자, 고객식별자,
     * 카드정보조회동의여부, 카드정보조회동의갱신일시
     * 
     * @method insertOpnbCrdDtl
     * @method(한글명) 카드고객상세등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbCrdDtl(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/insertOpnbCrdDtl", input);
        return dao.executeUpdate();
    }
    
    /**
     * - UBF오픈뱅킹카드고객상세 채널별 등록(타 채널 정보 SELECT INSERT)
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * 
     * <INPUT>
     * 고객계좌번호, 계좌개설은행코드, 채널세부업무구분코드, 오픈뱅킹이용기관코드, CI내용, 오픈뱅킹계좌상세일련번호, 준회원식별자, 고객식별자, 오픈뱅킹플랫폼식별번호, 계좌납입회차번호,
     * 개별저축은행명, 출금동의여부, 출금동의갱신일시, 출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부, 개별저축은행코드 
     * <OUTPUT>
     * 
     * @method insertOpnbCrdDtlChnPr
     * @method(한글명) 카드고객상세 채널별 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOpnbCrdDtlChnPr(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/insertOpnbCrdDtlChnPr", input);
        return dao.executeUpdate();
    }
    
    /**
     * - 오픈뱅킹카드고객상세 사용여부 수정
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 오픈뱅킹회원금융회사코드, 채널세부업무구분코드, CI내용, 카드정보조회동의여부, 카드정보조회동의갱신일시
     * <OUTPUT>
     * 
     * @method updateCrdDtlUseYn
     * @method(한글명) 카드고객상세사용여부수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdDtlUseYn(LData input) throws LException {       
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/updateCrdDtlUseYn", input);
        return dao.executeUpdate();
    }
    
    
    /**
     * - 오픈뱅킹카드고객일련번호채번조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * 
     * <OUTPUT>
     * - 오픈뱅킹카드고객일련번호 
     * 
     * @method selectOpnbCrdSnoBbr
     * @method(한글명) 오픈뱅킹카드고객일련번호채번조회 (미사용)
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbCrdSnoBbr(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectOpnbCrdSnoBbr", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 등록된 사용자의 카드정보(이메일주소)를 변경
     * 
     * 1. 카드정보변경 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 오픈뱅킹센터로부터 응답을 받지 못했을 경우 카드정보조회 API를 통해 처리 결과 확인 -> 카드정보변경 API 재호출
     * 3. 오픈뱅킹고객정보기본, 오픈뱅킹발급기관기본 원장의 오픈뱅킹이메일주소  변경
     * 4. 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본
     *  UBF오픈뱅킹카드발급기관기본
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 오픈뱅킹회원금융회사코드, 오픈뱅킹사용자고유번호, 서비스구분(cardinfo), 변경할 이메일 주소
     * <OUTPUT>
     * 
     * @method updateCrdInf
     * @method(한글명) 카드정보 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int updateCrdInf(LData input) throws LException {
    	LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/updateCrdInf", input);
        return dao.executeUpdate();
    }

    /**
     * - 등록된 사용자의 카드정보를 조회
     * 
     * 1. 카드정보조회 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 사용자탈퇴 API 요청 후 카드사용자 해지 처리전 카드정보조회 요청하게 되면, '사용자탈퇴 처리중인 계좌(A0019)'로 조회 거부 응답
     *  - 응답코드 000 이면 동의, 551,555,556이면 동의거부, 이외코드 조회 불능
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     * <INPUT>
     *  거래고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 서비스구분(cardinfo)
     * <OUTPUT>
     *  카드개설기관명, 사용자일련번호, 변경된 이메일주소
     * 
     * @method selectCrdInf
     * @method(한글명) 카드정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCrdInf(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectCrdInf", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * -  요청자의 카드사별 카드 목록 정보 등록(이용동의 상태의 카드사)
     * 
     * 1. 오픈뱅킹카드사별이용동의 조회
     * 2. 카드목록조회(금결원) API 호출하여 요청받은 카드사의 카드 목록 정보를 오픈뱅킹보유카드정보기본 원장, 오픈뱅킹카드정보기본 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본
     *  UBF오픈뱅킹카드발급기관기본
     *  UBF오픈뱅킹카드발급기관상세
     *  UBF오픈뱅킹카드정보기본
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 회원금융회사코드, 사용자명, CI, 이메일주소, 서비스구분(cardinfo), 제3자정보제공동의여부, 채널구분코드, 오픈뱅킹플랫폼식별번호
     * <OUTPUT>
     * 
     * @method insertChnPrCrdCtg
     * @method(한글명) 채널별 카드 목록 등록
     * @param LMultiData
     * @return LData
     * @throws LException 
     */ 
    public int insertChnPrCrdCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/insertChnPrCrdCtg", input);
        return dao.executeUpdate();
    }

    /**
     * - 해당 채널에 등록돼있는 오픈뱅킹 카드 목록 상세조회
     * 
     * 1. 채널세부업무구분코드, 오픈뱅킹카드식별자, 오픈뱅킹이용기관코드, 오픈뱅킹카드일련번호로 조회 요청
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드발급기관상세, UBF오픈뱅킹보유카드정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 채널세부업무구분코드
     * <OUTPUT>
     * 오픈뱅킹카드일련번호, 오픈뱅킹개설기관코드, 오픈뱅킹카드식별자, 오픈뱅킹마스킹카드번호, 카드상품명, 가족카드여부, 오픈뱅킹카드구분코드, 결제은행코드, 오픈뱅킹마스킹계좌번호, 카드발급년월일, 오픈뱅킹카드결제일
     * 
     * 
     * @method selectChnPrCrdCtg
     * @method(한글명) 채널별 카드 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectChnPrCrdCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectChnPrCrdCtg", input);
        return dao.executeQuery(); 
    }
    
    /**
    * - 해당 채널에 등록돼있는 오픈뱅킹 카드고객 목록조회
    * 
    * <관련 테이블>
    * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드고객상세
    * <INPUT>
    * 오픈뱅킹사용자고유번호, 채널세부업무구분코드
    * <OUTPUT>
    * LIST
    * - 오픈뱅킹회원금융회사코드
    * 
    * @method selectCrdCstCtg
    * @method(한글명) 카드고객목록조회
    * @param LData
    * @return LMultiData
    * @throws LException 
    */ 
    public LMultiData selectCrdCstCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectCrdCstCtg", input);
        return dao.executeQuery(); 
    }
    
    /**
     * - 요청된 카드사 정보 조회 동의 여부(제3자정보제공동의)를 해지하며, 모든 채널의 카드 정보 조회 동의 여부가 해지되었을 경우 해당 카드사에 대해서 오픈뱅킹센터에 등록했던 사용자등록(제3자정보제공동의)을 해지.
     * 
     * 1. 오픈뱅킹카드발급기관상세 원장의 채널별 동의 여부 정보 수정
     * 1-1. 특정 채널만 해지되었을 경우
     *  - 오픈뱅킹카드발급기관상세 원장 동의 정보 변경
     * 1-2. 전체 채널이 해지되었을 경우
     *  - 카드조회해지API 호출 후 오픈뱅킹카드발급기관상세, 오픈뱅킹카드발급기관기본 원장 동의 정보 변경(해지)
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드발급기관기본
     *  UBF오픈뱅킹카드발급기관상세
     * <INPUT>
     *  채널세부업무구분코드, 오픈뱅킹사용자고유번호, 제3자정보제공동의여부(해지), 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 거래고유번호
     * <OUTPUT>
     *  
     * 
     * @method deleteTmnCrdInq
     * @method(한글명) 카드조회 해지처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteTmnCrdInq(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/deleteTmnCrdInq", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }

    /**
     * - 이용기관에 등록된 모든 서비스를 해지하고 오픈뱅킹에 등록된 사용자를 탈퇴. 이용기관은 장기 미사용 등을 이유로 고객정보 파기 시에는 사용자 탈퇴를 해야함. 
     * - 채널 별로 UBF테이블 상태 업데이트
     * - 모든 채널 탈퇴 시 UBF오픈뱅킹고객정보기본 업데이트 및 금결원API 호출 처리
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용, 채널세부업무구분코드] 항목으로 요청
     * 2. UBF오픈뱅킹계좌상세 테이블에 존재하는지 확인
     * 3. UBF오픈뱅킹계좌상세 테이블에 Update
     * 4. UBF오픈뱅킹계좌상세 테이블에 모든 채널에서 탈퇴했으면 사용자탈퇴처리 금결원API 호출
     * 5. UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본 Update
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용, 채널세부업무구분코드
     * <OUTPUT>
     * 결과(T/F)
     * 
     * @method deleteUsrScsn
     * @method(한글명) 사용자탈퇴 처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteUsrScsn(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/deleteUsrScsn", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }
    
    /**
     * - 오픈뱅킹카드개설기관명조회
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관기본
     * 
     * <INPUT>
     * - 오픈뱅킹금융기관코드(오픈뱅킹회원금융회사코드)
     * 
     * <OUTPUT>
     * - 오픈뱅킹기관명 
     * 
     * @method selectCrdOpeInsNm
     * @method(한글명) 오픈뱅킹카드개설기관명조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCrdOpeInsNm(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/selectCrdOpeInsNm", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - UBF오픈뱅킹카드고객기본삭제
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드고객기본
     *
     * <INPUT>
     *  오픈뱅킹사용자고유번호
     * <OUTPUT>
     *  
     * 
     * @method deleteOpnbCrdCstBas
     * @method(한글명) 오픈뱅킹카드고객기본삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteOpnbCrdCstBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/deleteOpnbCrdCstBas", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }
    
    /**
     * UBF오픈뱅킹카드고객상세삭제
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드고객상세
     *  
     * <INPUT>
     *  CI내용
     * <OUTPUT>
     *  
     * 
     * @method deleteOpnbCrdCstDtl
     * @method(한글명) 오픈뱅킹카드고객상세삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteOpnbCrdCstDtl(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/deleteOpnbCrdCstDtl", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }
    
    /**
     * UBF오픈뱅킹카드정보기본삭제
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드정보기본
     *  
     * <INPUT>
     *  CI내용
     * <OUTPUT>
     * 
     * @method deleteOpnbCrdInfBasDtl
     * @method(한글명) 오픈뱅킹카드정보기본삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int deleteOpnbCrdInfBasDtl(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbCrdMgEbc/deleteOpnbCrdInfBasDtl", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }

}

