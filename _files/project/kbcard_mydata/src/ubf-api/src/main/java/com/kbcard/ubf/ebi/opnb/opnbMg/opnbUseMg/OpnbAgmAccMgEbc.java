package com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.exception.LNotFoundException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbAgmAccMgEbc
 * 
 * @logicalname  : 오픈뱅킹약정계좌관리Ebc
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

public class OpnbAgmAccMgEbc {

    /**
     * - 오픈뱅킹 모계좌 정보 등록
     * 
     * 1. 오픈뱅킹약정계좌기본 원장에 약정계좌 정보 등록
     * 2. 오픈뱅킹업무구분기본 원장에 해당 약정계좌에 대한 업무구분 정보 등록
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     * <INPUT>
     *  오픈뱅킹약정계좌기관코드<필수>
     *  오픈뱅킹계좌계정구분코드<필수> 
     *  오픈뱅킹약정계정계좌번호 <필수>
     *  예금주명<필수>
     *  오픈뱅킹모계좌용도구분코드<필수> 
     *  오픈뱅킹업무구분명<필수>
     *  오픈뱅킹약정계좌설명
     * <OUTPUT>
     * 
     * @method regOpnbAgmAccBas
     * @method(한글명) 약정계좌 등록(UBF오픈뱅킹약정계좌기본)
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int regOpnbAgmAccBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/regOpnbAgmAccBas", input);
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
     * - 오픈뱅킹 모계좌 정보 등록
     * 
     * 1. 오픈뱅킹약정계좌기본 원장에 약정계좌 정보 등록
     * 2. 오픈뱅킹업무구분기본 원장에 해당 약정계좌에 대한 업무구분 정보 등록
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹약정계좌기관코드<필수>
     *  오픈뱅킹계좌계정구분코드<필수> 
     *  오픈뱅킹약정계정계좌번호 <필수>
     *  예금주명<필수>
     *  오픈뱅킹모계좌용도구분코드<필수> 
     *  오픈뱅킹업무구분명<필수>
     *  오픈뱅킹약정계좌설명
     * <OUTPUT>
     * 
     * @method regOpnbBwkDcBas
     * @method(한글명) 약정계좌 등록(UBF오픈뱅킹업무구분기본)
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int regOpnbBwkDcBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/regOpnbBwkDcBas", input);
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
     * - 오픈뱅킹 모계좌 정보 조회
     * 
     * 1. 오픈뱅킹약정계좌기본, 오픈뱅킹업무구분기본 원장에서 약정계좌 정보 조회
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹모계좌용도구분코드<필수>
     * <OUTPUT>
     *  오픈뱅킹약정계좌일련번호
     *  오픈뱅킹약정계좌기관코드
     *  오픈뱅킹계좌계정구분코드
     *  오픈뱅킹약정계정계좌번호
     *  예금주명
     *  오픈뱅킹모계좌용도구분코드
     *  오픈뱅킹업무구분명
     *  오픈뱅킹약정계좌설명
     * 
     * @method retvLstOpnbAgmAccInq
     * @method(한글명) 약정계좌 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvLstOpnbAgmAccInq(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/retvLstOpnbAgmAccInq", input);
        return dao.executeQueryOnlySingle(); 
    }

    
    /**
     * - 오픈뱅킹 약정계좌일련번호 채번
     * 
     * 1. UBF오픈뱅킹약정계좌기본 약정계좌일련번호 채번
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     * <INPUT>
     * <OUTPUT>
     *  오픈뱅킹약정계좌일련번호
     * 
     * @method retvLstOpnbAgmAccInq
     * @method(한글명) 약정계좌 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    
    public LData selectAgmAccSnoSeq() throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/selectAgmAccSnoSeq", null);
        return dao.executeQueryOnlySingle(); 
    }
    
    /**
     * - 오픈뱅킹 모계좌 정보 변경
     * 
     * 1. 오픈뱅킹약정계좌기본 원장에 약정계좌 정보 변경
     * 
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     * <INPUT>
     *  오픈뱅킹약정계좌설명, 오픈뱅킹약정계좌기관코드, 오픈뱅킹계좌계정구분코드, 오픈뱅킹약정계정계좌번호, 예금주명, 오픈뱅킹모계좌용도구분코드, 오픈뱅킹업무구분명
     * <OUTPUT>
     * 
     * @method uptOpnbAgmAccBas
     * @method(한글명) 약정계좌 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbAgmAccBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/uptOpnbAgmAccBas", input);
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
     * - 오픈뱅킹 모계좌 업무 구분 정보 변경
     * 
     * 1. 오픈뱅킹업무구분기본 원장에 약정계좌 업무 구분 정보 변경
     * 
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹약정계좌설명, 오픈뱅킹약정계좌기관코드, 오픈뱅킹계좌계정구분코드, 오픈뱅킹약정계정계좌번호, 예금주명, 오픈뱅킹모계좌용도구분코드, 오픈뱅킹업무구분명
     * <OUTPUT>
     * 
     * @method uptOpnbBwkDcBas
     * @method(한글명) 약정계좌 업무정보 변경(UBF오픈뱅킹업무구분기본)
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbBwkDcBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/uptOpnbBwkDcBas", input);
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
     * - 오픈뱅킹 모계좌 정보를 삭제한다.
     * 
     * 1. 오픈뱅킹약정계좌기본 원장에서 약정계좌 정보 삭제
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     * <INPUT>
     *  오픈뱅킹약정계좌일련번호, 오픈뱅킹모계좌용도구분코드
     * <OUTPUT>
     * 삭제결과
     * 
     * @method delOpnbAgmAccBas
     * @method(한글명) 약정계좌 삭제
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int delOpnbAgmAccBas(LData input) throws LException {
        int iRtn = 0;
        
        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/delOpnbAgmAccBas", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }
    
    /**
     * - 오픈뱅킹 모계좌 업무 구분 정보를 삭제한다.
     * 
     * 1. 오픈뱅킹업무구분기본 원장에서 모계좌 업무 구분 정보 삭제
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹약정계좌일련번호, 오픈뱅킹모계좌용도구분코드
     * <OUTPUT>
     * 삭제결과
     * 
     * @method delOpnbBwkDcBas
     * @method(한글명) 약정계좌 삭제(UBF오픈뱅킹업무구분기본)
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public int delOpnbBwkDcBas(LData input) throws LException {
        int iRtn = 0;

        try { 
            LCommonDao dao = new LCommonDao("opnb/opnbMg/opnbUseMg/opnbAgmAccMgEbc/delOpnbBwkDcBas", input);
            iRtn = dao.executeUpdate();
        } catch (LException e) {
             throw new LException("메세지 코드", e);
        }
        
        return iRtn;
    }

}

