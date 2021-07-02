package com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.core.exception.LNotAffectedException;
import devonframework.persistent.autodao.LCommonDao;

/** 
 * opnbUtzCnsMgEbc
 * 
 * @logicalname  : 오픈뱅킹이용동의관리Ebc
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

public class OpnbUtzCnsMgEbc {

	/**
     * - 오픈뱅킹계좌기본동의갱신
     *  계좌조회동의갱신일시, 조회갱신채널세부업무구분코드, 출금동의갱신일시, 출금갱신채널세부업무구분코드 업데이트
     *  
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 채널세부업무구분코드, 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @method uptOpnbAccBasCns
     * @method(한글명) 오픈뱅킹계좌기본동의갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbAccBasCns(LData input) throws LException {
    	  int iRtn = 0;

       	  LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/uptOpnbAccBasCns", input);
          iRtn = dao.executeUpdate();
          if(iRtn == 0){
              throw new LNotAffectedException();
          }
          
          return iRtn;
    }
    
	/**
     * - 오픈뱅킹계좌상세동의갱신
     *  출금동의갱신일시 업데이트
     *  
     * <관련 테이블>
     * UBF오픈뱅킹계좌상세
     * <INPUT>
     * 채널세부업무구분코드, 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @method uptOpnbAccDtlCns
     * @method(한글명) 오픈뱅킹계좌상세동의갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbAccDtlCns(LData input) throws LException {
        int iRtn = 0;	
      
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/uptOpnbAccDtlCns", input);
        iRtn = dao.executeUpdate();
        if(iRtn == 0){
            throw new LNotAffectedException();
        }
       
        return iRtn;
    }
    
    /**
     * - 오픈뱅킹카드고객기본동의갱신
     *  카드정보조회동의갱신일시, 조회갱신채널세부업무구분코드
     *  
     * <관련 테이블>
     * UBF오픈뱅킹카드고객기본
     * <INPUT>
     * 채널세부업무구분코드, 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @method uptOpnbCrdCstBasCns
     * @method(한글명) 오픈뱅킹카드고객기본동의갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbCrdCstBasCns(LData input) throws LException {
    	  int iRtn = 0;

       	  LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/uptOpnbCrdCstBasCns", input);
          iRtn = dao.executeUpdate();
          if(iRtn == 0){
              throw new LNotAffectedException();
          }
          
          return iRtn;
       
    }
    
	/**
     * - 오픈뱅킹카드고객상세동의갱신
     *  카드정보조회동의갱신일시 업데이트
     *  
     * <관련 테이블>
     * UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 채널세부업무구분코드, 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @method uptOpnbCrdCstDtlCns
     * @method(한글명) 오픈뱅킹카드고객상세동의갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int uptOpnbCrdCstDtlCns(LData input) throws LException {
        int iRtn = 0;	
      
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/uptOpnbCrdCstDtlCns", input);
        iRtn = dao.executeUpdate();
        if(iRtn == 0){
            throw new LNotAffectedException();
        }
       
        return iRtn;
        
    }
    

	/**
     * - 오픈뱅킹고객동의이력등록
     *  
     * <관련 테이블>
     * UBF오픈뱅킹고객동의이력
     * <INPUT>
     * [오픈뱅킹고객동의이력]정보
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @method regOpnbCstCnsPhs
     * @method(한글명) 오픈뱅킹고객동의이력등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public int regOpnbCstCnsPhs(LData input) throws LException {
        int iRtn = 0;

        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/regOpnbCstCnsPhs", input);
        iRtn = dao.executeUpdate();
        if(iRtn == 0){
            throw new LNotAffectedException();
        }
        
        return iRtn;
    }

    /**
     * - 오픈뱅킹이용동의 조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용]를 이용하여 조회 요청
     * 2. 사용자의 이용동의 상태 리턴
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * LIST
     *  - 채널세부업무구분코드,  개인정보제3자정보제공동의여부, 출금동의여부, 통합계좌조회동의여부, 계좌조회동의여부
     * 
     * @method selectUtzCns
     * @method(한글명) 이용동의 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    @Deprecated
    public LMultiData selectUtzCns(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectUtzCns", input);
        return dao.executeQuery(); 
    }

    /**
     * -계좌 별 오픈뱅킹이용동의 조회
     * 
     * 1. 오픈뱅킹사용자고유번호/계좌번호/개설은행코드/이용기관코드/계좌일련번호를 이용하여 조회 요청
     * 2. 사용자의 이용동의 상태 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,계좌번호,개설은행코드,이용기관코드,계좌일련번호
     * <OUTPUT>
     * 계좌조회동의여부,출금동의여부
     * 
     * 
     * @method selectAccPrUtzCns
     * @method(한글명) 계좌별이용동의 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectAccPrUtzCns(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectAccPrUtzCns", input);
        return dao.executeQueryOnlySingle(); 
    }


    /**
     * -카드사 별 오픈뱅킹이용동의 조회
     * 
     * 1. 해당 카드사의 카드조회동의여부(제3자정보제공동의여부) 조회
     * 2. 사용자의 카드조회동의여부(제3자정보제공동의여부) 상태 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드고객기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호<필수> 
     *  오픈뱅킹회원금융회사코드<필수>
     * <OUTPUT>
     *  오픈뱅킹사용자고유번호 
     *  오픈뱅킹회원금융회사코드 
     *  오픈뱅킹개설기관코드
     *  제3자정보조회동의여부
     *  제3자정보조회동의등록일시
     *  제3자정보조회동의갱신일시
     *  제3자정보조회동의해제일시
     * 
     * @ServiceID UBF2010106
     * @method retvCrdPrUtzCns
     * @method(한글명) 카드사별이용동의 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectCrdPrUtzCns(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectCrdPrUtzCns", input);
        return dao.executeQueryOnlySingle(); 
    }
    
    
    /**
     * - 고객동의이력 목록 조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용]를 이용하여 조회 요청
     * 2. 사용자의 동의이력 목록 리턴
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객동의이력
     * <INPUT>
     * 채널세부업무구분코드, 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * LIST
     *  - 채널세부업무구분코드, 오픈뱅킹약관동의구분코드, 오픈뱅킹동의연장구분코드, 오픈뱅킹동의자료구분코드, 오픈뱅킹약관동의일시
     * 
     * @method selectCstCnsHisCtg
     * @method(한글명) 고객동의이력 목록 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LMultiData selectCstCnsHisCtg(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectCstCnsHisCtg", input);
        return dao.executeQuery(); 
    }

    
    /**
     * - 오픈뱅킹최초등록일시 조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용]를 이용하여 조회 요청
     * 2. 오픈뱅킹최초등록일시
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객동의이력
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 오픈뱅킹최초등록일시
     * 
     * @method selectOpnbFstRgYms
     * @method(한글명) 오픈뱅킹최초등록일시 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbFstRgYms(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectOpnbFstRgYms", input);
        return dao.executeQueryForSingle(); 
    }
    
    /**
     * - 오픈뱅킹최종사용일시 조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용]를 이용하여 조회 요청
     * 2. 오픈뱅킹최종사용일시
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹기관거래내역
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 오픈뱅킹최종사용일시
     * 
     * @method selectOpnbLstUseYms
     * @method(한글명) 오픈뱅킹최종사용일시 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbLstUseYms(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectOpnbLstUseYms", input);
        return dao.executeQueryForSingle();  
    }
    
    
    /**
     * - 오픈뱅킹이용동의최종갱신일시 조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용]를 이용하여 조회 요청
     * 2. 오픈뱅킹이용동의최종갱신일시
     * 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객동의이력
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * 오픈뱅킹이용동의최종갱신일시
     * 
     * @method selectOpnbUtzCnsLstUpdYms
     * @method(한글명) 오픈뱅킹이용동의최종갱신일시 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData selectOpnbUtzCnsLstUpdYms(LData input) throws LException {
        LCommonDao dao = new LCommonDao("opnb/opnbPuse/utzRg/opnbUtzCnsMgEbc/selectOpnbUtzCnsLstUpdYms", input);
        return dao.executeQueryForSingle();  
    }
}

