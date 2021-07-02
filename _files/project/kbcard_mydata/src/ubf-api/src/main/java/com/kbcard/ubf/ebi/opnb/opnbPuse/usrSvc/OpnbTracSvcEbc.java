package com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.exception.LNotFoundException;
import devonenterprise.service.nestedtx.NestedTxDao;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbTracSvcEbc
 * 
 * @logicalname  : 오픈뱅킹이체서비스Ebc
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

public class OpnbTracSvcEbc {

    /**
     * - 이용기관이 출금이체 혹은 입금이체 후 이체결과를 다시 확인
     * - 이체 시 비정상적인 응답코드를 받았을 경우나 응답을 받지 못했을 경우 등 이체결과 확인이 필요한 경우 사용하는 용도
     * 
     * 1. 오픈뱅킹이체내역 정보 확인
     *     - NOT FOUND일경우 오류
     * 2. 이체결과조회 API 호출
     * 3. 오픈뱅킹이체결과조회요청내역에 등록처리.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹이체내역
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 입출금구분코드, 요청일시
     * LIST
     * - 원거래고유번호, 원거래거래일시, 원거래금액, 
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * LIST
     * - 거래고유번호(참가기관), 거래일자, 참가기관응답코드, 참가기관응답메시지, 출금기관코드, 출금기관점별코드, 출금기관명, 개별저축은행명,출금계좌핀테크번호, 출금계좌번호(출력용), 출금계좌인자내역, 송금인성명, 입금기관코드, 입금기관점별코드, 입금기관명, 입금개별저축은행명, 입금계좌핀테크번호, 입금계좌번호(출력용), 입금계좌인자내역, 수취인성명, 거래금액 
     * 
     * @method selectTracRst
     * @method(한글명) 이체결과조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectTracRst(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/selectTracRst", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 거래가 완료된 입금이체에 대해서 사용자의 착오송금, 이용기관의 장애 등으로 자금반환이 필요한 경우 이용기관은 자금반환을 청구
     * 
     * 1. 입금이체 원장 정보 확인.
     *     - NOT FOUND일경우 오류
     * 2. 자금반환 요청 원장에 등록처리.
     * 3. 자금반환청구 API 호출
     * 4. 거래고유번호등 정상처리 결과 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹이체내역
     * - UBF오픈뱅킹자금청구접수내역
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 거래고유번호(참가기관), 전액반환여부, 반환금액, 자금반환청구내용, 자금반환청구사유구분코드, 청구사유, 반환금액입금계좌, 이용기관담당자연락처, 이용기관담당자이메일주소
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 응답코드,응답메시지, 정상등록여부
     * 
     * @method insertFndsRtunBil
     * @method(한글명) 자금반환청구등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertFndsRtunBil(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/insertFndsRtunBil", input);
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
     * - 이용기관이 자금반환 청구 요청 후 자금반환 결과를 확인
     * - 자금반환 청구 요청결과 확인이 필요한 
     * 경우나 자금반환 청구가 정상적으로 요청된 후 반환 결과를 확인하는 용도
     * 
     * 1. 조회조건(청구요청거래일자/청구요청거래고유번호)에 따른 자금반환요청원장 목록 조회
     *     - not found일 경우 오류 리턴
     * 2. 요청원장의 정보로 자금반환결과 조회 API 호출하여 조회목록에 결과값 셋팅
     * 3. 신청결과 목록조회 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹자금청구접수내역
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI값, 거래기간
     * 
     * <OUTPUT>
     * LIST
     * - 거래고유번호(참가기관), 금융기관코드, 계좌번호, 전액반환여부, 자금반환결과구분코드, 자금반환청구내용, 자금반환불가사유
     * 
     * @method selectFndsRtunRst
     * @method(한글명) 자금반환결과조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectFndsRtunRst(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/selectFndsRtunRst", input);
        return dao.executeQuery(); 
    }

    /**
     * - 이용자의 최근 입금이체이력을 조회
     * 최근입금이체 이력을 바탕으로 간편 송금이체 처리하기 위함.
     * 
     * 1. 최근 입금이체이력 n건 조회처리.
     * 2. 이체이력 원장 조회.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI값.
     * 
     * <OUTPUT>
     * LIST
     * - 거래고유번호(참가기관), 금융기관코드, 계좌번호, 입금금액, 입금일자
     * 
     * @method selectLaMrvTracPhs
     * @method(한글명) 최근입금이체이력조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData selectLaMrvTracPhs(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/selectLaMrvTracPhs", input);
        return dao.executeQuery(); 
    }

    /**
     * - 입금계좌의 유효성 검증
     * 이체처리 전 사용자가 입력한 계좌의 입출금 거래 가능여부를 체크하기 위함.
     * 
     * 1. 기관코드와 은행계좌번호로 계좌실명조회 API 호출.
     * 2. 입출력계좌 정상 여부를 확인하여 리턴한다.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 개설기관코드, 계좌번호, 예금주실명번호구분코드, 실명번호
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 계좌종류, 개별저축은행명,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지, 입금가능여부
     * 
     * @method selectMrvAccVld
     * @method(한글명) 입금계좌유효성조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectMrvAccVld(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/selectMrvAccVld", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 출금계좌의 유효성 검증.
     * 송금처리전 출금계좌의 유효성을 검증한다.
     * 
     * 1. 출금계좌 등록여부 확인.
     * 2. 잔액조회 API를 호출하여, 출금한도 조회.
     * 3. 일누적출금금액을 체크하여 일 이체한도 조회 체크.
     * 4. 출금 가능여부 리턴.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹계좌기본
     * - UBF오픈뱅킹계좌상세
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 계좌잔액, 출금계좌핀테크이용번호,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지, 출금가능여부
     * 
     * @method selectOdwAccVld
     * @method(한글명) 출금계좌유효성조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOdwAccVld(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/selectOdwAccVld", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹이체내역등록 NestedDAO처리
     * 
     * 1. 입출금요청 필수입력값 체크
     *     - 입금계좌번호 및 입금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 출금계좌인자내역, 입금이체용 암호문구, 수취인성명검증여부
     * - 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도, 입금계좌인자내역
     * 
     * @method regOpnbTracHis
     * @method(한글명) 이체내역등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int regOpnbTracHis(LData input) throws LException {
        int iRtn = 0;

        try { 
            NestedTxDao dao = new NestedTxDao();
            iRtn = dao.executeUpdate("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/regOpnbTracHis", input);
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 오픈뱅킹이체내역조회
     * 
     * 1. 이체내역 필수입력값 체크
     *     - 거래고유번호/거래년월일
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 거래고유번호/거래년월일
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOpnbTracHis
     * @method(한글명) 이체내역조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOpnbTracHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvOpnbTracHis", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹이체거래고유번호조회
     * 
     * 1. 이체내역 필수입력값 체크
     *     - 오픈뱅킹이체거래고유번호
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 오픈뱅킹이체거래고유번호
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOpnbTracHis
     * @method(한글명) 오픈뱅킹이체거래고유번호조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvTracTrUno(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvTracTrUno", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹이체내역목록조회
     * 
     * 1. 이체내역 필수입력값 체크
     *     - 이용자고유번호/조회기간
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 이용자고유번호/조회기간
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOpnbTracHis
     * @method(한글명) 이체내역조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LMultiData retvLstOpnbTracHis(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvLstOpnbTracHis", input);
        return dao.executeQuery(); 
    }

    /**
     * - 오픈뱅킹이체내역수정
     * 
     * 1. 입출금요청 필수입력값 체크
     *     - 입금계좌번호 및 입금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 출금계좌인자내역, 입금이체용 암호문구, 수취인성명검증여부
     * - 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도, 입금계좌인자내역
     * 
     * @method uptOpnbTracHis
     * @method(한글명) 이체내역수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbTracHis(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/uptOpnbTracHis", input);
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
     * - 오픈뱅킹이체내역결과수정
     * 
     * 1. 이체내역결과수정 필수입력값 체크
     *     - 거래일자/거래고유번호
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 거래일자/거래고유번호/API응답코드/API응답내용/참가기관응답코드/참가기관응답내용
     * 
     * @method uptOpnbTracHis
     * @method(한글명) 이체내역결과수정
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbTracHisRst(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/uptOpnbTracHisRst", input);
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
     * - 오픈뱅킹이체결과조회요청회차조회
     * 
     * 1. 이체결과조회요청내역 필수입력값 체크
     *     - 거래고유번호/거래년월일
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 거래고유번호/거래년월일
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOpnbTracHis
     * @method(한글명) 이체결과조회요청회차조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOpnbTracRstNth(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvOpnbTracRstNth", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹이체결과조회요청내역등록
     * 
     * 1. 이체결과조회요청내역 필수입력값 체크
     *     -  등
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체결과조회요청내역
     * 
     * <INPUT>
     * - 
     * 
     * @method regOpnbTracHis
     * @method(한글명) 이체결과조회요청내역등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int regOpnbTracRst(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao();
            iRtn = dao.executeUpdate("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/regOpnbTracRst", input);
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 오픈뱅킹자금반환요청내역등록
     * 
     * 1. 자금반환요청내역 필수입력값 체크
     *     -  등
     * 
     * <관련테이블>
     * - UBF오픈뱅킹자금청구요청내역
     * 
     * <INPUT>
     * - 
     * 
     * @method regOpnbTracHis
     * @method(한글명) 자금청구요청내역등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int regOpnbFndsRtun(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao();
            iRtn = dao.executeUpdate("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/regOpnbFndsRtun", input);
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 오픈뱅킹자금반환요청내역조회
     * 
     * 1. 자금청구요청내역조회 필수입력값 체크
     *     - 거래고유번호/거래년월일
     * 
     * <관련테이블>
     * - UBF오픈뱅킹자금청구요청내역
     * 
     * <INPUT>
     * - 거래고유번호/거래년월일
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOpnbTracHis
     * @method(한글명) 오픈뱅킹자금반환요청내역조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOpnbFndsRtun(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvOpnbFndsRtun", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 정상자금반환요청내역존재여부조회
     * 
     * 1. 자금청구요청내역조회 필수입력값 체크
     *     - 원거래고유번호/원거래년월일
     * 
     * <관련테이블>
     * - UBF오픈뱅킹자금청구요청내역
     * 
     * <INPUT>
     * - 원거래고유번호/원거래년월일
     * 
     * <OUTPUT>
     * - 
     * 
     * @method retvOgtnRcpYn
     * @method(한글명) 오픈뱅킹자금반환요청내역조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOgtnRcpYn(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/retvOgtnRcpYn", input);
        return dao.executeQueryOnlySingle(); 
    }

    /**
     * - 오픈뱅킹자금반환결과수정
     * 
     * 1. 자금반환결과수정 필수입력값 체크
     *     -  등
     * 
     * <관련테이블>
     * - UBF오픈뱅킹자금청구요청내역
     * 
     * <INPUT>
     * - 
     * 
     * @method regOpnbTracHis
     * @method(한글명) 자금청구요청내역등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbFndsRtun(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao();
            iRtn = dao.executeUpdate("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/uptOpnbFndsRtun", input);
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        if(iRtn == 0){
            throw new LNotFoundException("ERR_MSG_CD", "오류메세지코드 코드");
        }
        
        return iRtn;
    }

    /**
     * - 입금계좌정보와 당사출금정보를 입력받아 출금처리.
     * 
     * 1. 입금요청 필수입력값 체크
     *     - 입금계좌번호 및 입금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 2. 계좌실명조회 API 호출
     *     - 입금계좌의 입금가능여부 및 계좌주실명 사전 조회
     * 3. 입금이체 API 호출
     * 4. 오픈뱅킹이체내역 LIST 생성
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 출금계좌인자내역, 입금이체용 암호문구, 수취인성명검증여부
     * LIST
     * - 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도, 입금계좌인자내역
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * LIST
     * - 거래고유번호(참가기관),핀테크이용번호, 참가기관응답코드, 참가기관응답메시지
     * 
     * @method insertMrvDmd
     * @method(한글명) 입금요청등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertMrvDmd(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/insertMrvDmd", input);
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
     * - 출금계좌정보와 당사입금정보를 입력받아 출금처리.
     * 
     * 1. 출금요청 필수입력값 체크
     *     - 출금계좌번호 및 출금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 2. 출금계좌 등록여부 확인 및 수취조회 API 호출
     *     - 수취계좌의 입금가능여부 및 수취인성명 사전 조회
     * 3. 출금이체 API 호출
     * 4. 오픈뱅킹이체내역 생성
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역
     * - UBF오픈뱅킹고객정보기본
     * - UBF오픈뱅킹계좌기본
     * - UBF오픈뱅킹계좌상세
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 입금계좌인자내역, 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관),출금한도잔여금액,출금계좌핀테크이용번호,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지
     * 
     * @method insertOdwDmd
     * @method(한글명) 출금요청등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int insertOdwDmd(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbPuse/usrSvc/opnbTracSvcEbc/insertOdwDmd", input);
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

