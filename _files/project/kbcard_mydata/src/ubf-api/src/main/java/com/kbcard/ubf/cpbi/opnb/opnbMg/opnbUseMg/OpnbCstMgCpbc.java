package com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.util.StringUtil;

/** 
 * OpnbCstMgCpbc
 * 
 * @logicalname  : 오픈뱅킹고객관리Pbc
 * @author       : 정영훈
 * @since        : 2021-05-31
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-05-31       박건우       최초 생성
 *
 */

public class OpnbCstMgCpbc {
	
	
    /**
     * - 오픈뱅킹 이용영역에 관리중인 ‘사용자 정보 관리 원장(공통)’은 앱단위로 ‘회원ID와 CI번호, 등록계좌번호
     * (사용자일련번호)’를 매핑하여 오픈뱅킹 이용여부를 관리함 (관리포탈에서 앱회원ID로 오픈뱅킹서비스 조회)
     *  - 앱회원 탈회시(해당앱에서 오픈뱅킹 서비스 해지 포함), ‘사용자 정보 관리 원장(공통)’에서 해당앱과   매핑된 사용자 정보 즉시 삭제
     *  ▶ ‘사용자 정보 관리 원장(공통)’에서 동일 CI번호에 연계된 마지막 사용자 정보에 대한 삭제시 금결원 앞  오픈뱅킹 사용자 정보 해지 API송부
     *  ▶ ‘사용자 정보 관리 원장(공통)’에서 등록계좌번호 최종삭제시 관계된 대상거래 응답전문 삭제(3영업일내)
     * 
     * <관련 테이블>
     * TBUBFS001(UBF오픈뱅킹고객정보기본) , TBUBFS002(UBF오픈뱅킹계좌기본) , TBUBFS003(UBF오픈뱅킹계좌상세) , TBUBFS005(UBF오픈뱅킹카드고객기본), TBUBFS006(UBF오픈뱅킹고객기본)  
     *  
     * <INPUT>
     * 오픈뱅킹사용자고유번호 , CI내용
     * 
     * <OUTPUT>
     * 
     * @method(한글명) 오픈뱅킹고객개인정보삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData delOpnbCstPsnInf(LData input) throws LException {
    	
    	LLog.debug.println("오픈뱅킹고객개인정보삭제 START ☆★☆☆★☆☆★☆");
    	
    	if(StringUtils.isEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹사용자고유번호");
		}
    	
    	if(StringUtils.isEmpty(input.getString("CI내용"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "CI내용");
		}
    	
    	LData iDelcstInfo = LDataUtil.deepCopyLData(input);
    	LData rDelcstInfo = new LData();
      	
    	try{
    		//TBUBFS003 삭제 CI내용으로 삭제
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "deleteOpnbAccDtlCst" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹상세계좌 원장 삭제처리중 오류가 발생했습니다.");
    	}
	    
    	try{
    		//TBUBFS002 삭제 오픈뱅킹사용자고유번호로 삭제
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "deleteAcc" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹계좌기본 원장 삭제처리중 오류가 발생했습니다.");
    	}
	  
    	try{
    		//TBUBFS006 삭제 CI내용으로 삭제 
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "deleteOpnbCrdCstDtl" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹카드고객상세 원장 삭제처리중 오류가 발생했습니다.");
    	}
	   
    	try{
    		//TBUBFS005 삭제 오픈뱅킹사용자고유번호로 삭제
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "deleteOpnbCrdCstBas" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹카드고객기본 원장 삭제처리중 오류가 발생했습니다.");
    	}

    	try{
    		//TBUBFS020 삭제 오픈뱅킹사용자고유번호로 삭제
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "deleteOpnbCrdInfBasDtl" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹카드정보기본 원장 삭제처리중 오류가 발생했습니다.");
    	}
	    
    	try{
    		//TBUBFS001 삭제 오픈뱅킹사용자고유번호로 삭제
    		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "deleteOpnbCstBas" , iDelcstInfo);
	    
    	}catch(LException e){        		
    		throw new LException("오픈뱅킹고객기본 원장 삭제처리중 오류가 발생했습니다.");
    	}
    	
        return rDelcstInfo;
        
    }
    
    /**
     * - [오픈뱅킹고객동의이력] 원장에 채널별 동의/해지 이력 적재
     * 
     * 
     * <관련 테이블>
     * TBUBFS004(UBF오픈뱅킹고객동의이력)  
     *  
     * <INPUT>
     * 오픈뱅킹사용자고유번호 , 채널세부업무구분코드, 오픈뱅킹약관동의구분코드, 오픈뱅킹동의연장구분코드, 오픈뱅킹동의자료구분코드
     * 
     * <OUTPUT>
     * 
     * @method(한글명) 오픈뱅킹고객개인정보삭제
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public void opnbCstCnsPhsRg(LData input) throws LException {

    	boolean regFlag = false; // 오픈뱅킹고객동의이력 원장 적재 가능 여부
    	
	    try { //오픈뱅킹채널별약관동의여부조회
	        
	    	LData iOpnbChnPrStplCnsYn = new LData();
	    	LData rOpnbChnPrStplCnsYn = new LData();
	    	
	    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"     , input.getString("오픈뱅킹사용자고유번호"));
	    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
	    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , input.getString("오픈뱅킹약관동의구분코드"));
	    	
	    	rOpnbChnPrStplCnsYn = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbChnPrStplCnsYn" , iOpnbChnPrStplCnsYn);
	    	LLog.debug.println("오픈뱅킹동의연장구분코드: " + rOpnbChnPrStplCnsYn.getString("오픈뱅킹동의연장구분코드"));
	    	
	    	if("1".equals(input.getString("오픈뱅킹동의연장구분코드"))) { // 등록
	    		if("3".equals(rOpnbChnPrStplCnsYn.getString("오픈뱅킹동의연장구분코드"))) { // 가장 최근에 등록된 이력 정보가 3(해지) 일 경우
	    			regFlag = true;
	    		}
	    	} else if("3".equals(input.getString("오픈뱅킹동의연장구분코드"))) { // 해지
	    		if("1".equals(rOpnbChnPrStplCnsYn.getString("오픈뱅킹동의연장구분코드")) || "2".equals(rOpnbChnPrStplCnsYn.getString("오픈뱅킹동의연장구분코드"))) { // 가장 최근에 등록된 이력 정보가 1(등록) 또는 2(갱신) 일 경우 
	    			regFlag = true;
	    		}
	    	}
	    	
        } catch (LNotFoundException nfe) {
        	
        	regFlag = true; // 등록된 동의 이력 정보가 없을 경우
            
        }
	    
	    if(regFlag) { //채널별동의이력등록
	    	
		    LData iRegOpnbCstCnsPhsIn = new LData(); // 오픈뱅킹고객동의이력등록입력
		    
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , input.getString("오픈뱅킹사용자고유번호"));
		    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , input.getString("오픈뱅킹약관동의구분코드")); // 3: 계좌사용동의, 4:카드조회동의
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
			iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , input.getString("오픈뱅킹동의연장구분코드")); //1.약관동의, 3: 약관해지
		  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
			
	  	  	BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "regOpnbCstCnsPhs", iRegOpnbCstCnsPhsIn);
	  	  	
    	}
        
    }
    

    /**
     * 1. 고객식별자/준회원식별자/CI내용 등으로 API 호출
     * 2. 오픈뱅킹사용자고유번호 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 오픈뱅킹사용자고유번호
     * 
     * @serviceId UBF2030102
     * @method retvUsrUno
     * @method(한글명) 사용자고유번호조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvUsrUno(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("사용자고유번호조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
    	LData iRetvUsrUnoP = input; // i사용자고유번호조회입력
        LData rRetvUsrUnoP = new LData(); // r사용자고유번호조회출력
        
        try {
        	
        	// Validation Check
        	if(StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("CI내용")) 
        			&& StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("고객식별자")) 
        			&& StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("준회원식별자"))) {
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	
        	// Ebc 호출
    		LData iSelectUsrUnoIn = new LData(); // 사용자고유번호조회입력
    		LMultiData rSelectUsrUnoOut = new LMultiData(); // 사용자고유번호조회출력
    		iSelectUsrUnoIn.setString("CI_내용",		iRetvUsrUnoP.getString("CI내용"));
    		iSelectUsrUnoIn.setString("고객식별자",	iRetvUsrUnoP.getString("고객식별자"));
    		iSelectUsrUnoIn.setString("준회원식별자",	iRetvUsrUnoP.getString("준회원식별자"));
    		
    		try {
    			rSelectUsrUnoOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectUsrUno", iSelectUsrUnoIn);
    		} catch (LException e) {
    			throw new LBizException(ObsErrCode.ERR_1006.getCode(), ObsErrCode.ERR_1006.getName());
    		}

    		if(rSelectUsrUnoOut.getDataCount() == 1) { // 사용자고유번호 한 건 존재 시
    			rRetvUsrUnoP.setString("오픈뱅킹사용자고유번호", rSelectUsrUnoOut.getLData(0).getString("오픈뱅킹사용자고유번호"));
    		}
    		else if(rSelectUsrUnoOut.getDataCount() > 1) { // 사용자고유번호 여러 건 존재 시 가장 최근에 insert된 값 출력
    			rRetvUsrUnoP.setString("오픈뱅킹사용자고유번호", rSelectUsrUnoOut.getLData(rSelectUsrUnoOut.getDataCount()-1).getString("오픈뱅킹사용자고유번호"));
    		}
    		else { // 사용자고유번호 미존재 시
    			throw new LBizException(ObsErrCode.ERR_1000.getCode(), ObsErrCode.ERR_1000.getName());
    		}
    		
		} catch (LException e) {
			e.printStackTrace();
			throw new LBizException(e.getMessage(), e.getOptionalInfo().getMessageAddContent());
		}
        
        if(LLog.debug.isEnabled()) {
        	LLog.debug.println("----------[rRetvUsrUnoP]----------");
        	LLog.debug.println(rRetvUsrUnoP);
        	LLog.debug.println("사용자고유번호조회 END ☆★☆☆★☆☆★☆" );
        }
		
        return rRetvUsrUnoP;
    }
    
    /**
     * - 오픈뱅킹 무료거래내역등록
     * 
     * 1. 오픈뱅킹사용자고유번호와 거래정보로 거래내역 등록을 요청
     * 2. 무료이용대상에 해당하는지 확인 
     * 3. 현재거래일에 해당하는 유효한 오프뱅킹루료정책중 일련번호가 가장 작은것부터 조회하여 건수가 존재하는 것부터 차감
     * 4. 처리결과 리턴"
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * 
     * <INPUT>
     * 채널세부업무구분코드(필수)
     * 오픈뱅킹사용자고유번호(필수)
     * 오픈뱅킹전문거래년월일
     * 참가기관출금거래고유번호
     * 참가기관입금거래고유번호
     * 
     * @logicalName 무료거래내역등록
     * @method regtCfreTrHis
     * @method(한글명) 거래 내역 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtCfreTrHis(LData input) throws LException {
    	LData result = new LData();		
    	
     	LData inpLData = new LData();
        LData iRegData = new LData();
        
        String curDateTm = DateUtil.getDateTimeStr();
         
	    LLog.debug.println("오픈뱅킹 무료거래내역등록");
		LLog.debug.println(input);
		
		LLog.debug.println("로그☆☆☆☆☆☆☆☆☆☆☆☆ " + input.getString("채널세부업무구분코드"));
		
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("채널세부업무구분코드가 존재하지 않습니다. " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("오픈뱅킹사용자고유번호가 존재하지 않습니다. " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}
			 
		//오픈뱅킹무제한거래대상정책조회
		
		try {
		
			LData iNbRccTrTgPlcy = new LData();
			LData rNbRccTrTgPlcy = new LData();
			
			iNbRccTrTgPlcy.setString("채널세부업무구분코드", input.getString("채널세부업무구분코드"));
			iNbRccTrTgPlcy.setString("거래일시", curDateTm);
			rNbRccTrTgPlcy = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectOpnbNbRccTrTgPlcy" , iNbRccTrTgPlcy);
			
			//무제한정책이 존재할경우 무조건 insert
			iRegData.clear();
			iRegData.setString("오픈뱅킹사용자고유번호"       , input.getString("오픈뱅킹사용자고유번호"));
			iRegData.setInt("오픈뱅킹무료정책일련번호"         , rNbRccTrTgPlcy.getInt("오픈뱅킹무료정책일련번호"));
			iRegData.setString("채널세부업무구분코드"         , rNbRccTrTgPlcy.getString("채널세부업무구분코드"));
			//iRegData.setInt("오픈뱅킹무료거래건수"            , rNbRccTrTgPlcy.getInt("오픈뱅킹무료제공건수"));
			iRegData.setInt("오픈뱅킹무료거래건수"            , 99999);
			iRegData.setString("오픈뱅킹무료건수갱신일시"      , curDateTm);
			iRegData.setString("적용시작일시"               , rNbRccTrTgPlcy.getString("오픈뱅킹무료정책시작일시"));
			iRegData.setString("적용종료일시"               , rNbRccTrTgPlcy.getString("오픈뱅킹무료정책종료일시"));
			iRegData.setString("오픈뱅킹전문거래년월일"        , input.getString("오픈뱅킹전문거래년월일"));
			iRegData.setString("참가기관출금거래고유번호"      , input.getString("참가기관출금거래고유번호"));
			iRegData.setString("참가기관입금거래고유번호"      , input.getString("참가기관입금거래고유번호"));			
			iRegData.setString("시스템최초생성식별자"         , "UBF2020101");
			iRegData.setString("시스템최종갱신식별자"         , "UBF2020101");
			
			LLog.debug.println("오픈뱅킹무제한거래내역등록 Ldata");
			LLog.debug.println(iRegData);
			
			int insCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "insertTrHis", iRegData); // 오픈뱅킹무료거래내역등록
			
			if(insCnt > 0) {
				result.setString("처리결과_V1", "Y");//무료적용여부 Y
			}else {
				result.setString("처리결과_V1", "N");//무료적용여부 N
			}
		
		}catch(LNotFoundException nfe1) {
			
			//무제한정책이 존재하지 않는다면 현재 정책들을 읽어서 차감
	        String aplStYms = "";//적용시작일시
	        String aplEdYms = "";//적용종료일시 
			
			int sBfOpnbCfrePlcySno = 0; // 이전오픈뱅킹무료정책일련번호
			int sAfOpnbCfrePlcySno = 0; // 이후오픈뱅킹무료정책일련번호
			
	        int sOpnbCfreTrNcn = 0;//무료정책제공건수
	        int sOpnbFreeCnt = 0;//무료거래건수
	        int opnbTrRmgNcn = 0; //무료거래잔여건수
	        
	        String sNcnYn = "Y";
	        String trmDtcd = "";//오픈뱅킹기간구분코드
	        String chDtlDvCd = "";//채널세부업무구분코드
	        
	        int readCnt = 0;
	        List lstOpnbSno = new ArrayList(); //오픈뱅킹 일련번호 IN
			
			while(true) {
			 	
				//오픈뱅킹무료정책조회
				try {
					
					LData rCdcCrdLst = new LData();
					
					inpLData.clear();
					inpLData.setString("채널세부업무구분코드", input.getString("채널세부업무구분코드"));
					inpLData.setString("거래일시", curDateTm);
					
					if(readCnt > 0) {
						inpLData.setString("COND", "NOTIN");
						lstOpnbSno.add(sBfOpnbCfrePlcySno);
						inpLData.set("오픈뱅킹무료정책일련번호", lstOpnbSno);
					}
					
					readCnt++;
					
					rCdcCrdLst = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectOpnbChnPrCfrePlcySno" , inpLData);
					                                                            
					sAfOpnbCfrePlcySno = rCdcCrdLst.getInt("오픈뱅킹무료정책일련번호");
					sOpnbCfreTrNcn   = rCdcCrdLst.getInt("오픈뱅킹무료제공건수");
					aplStYms = rCdcCrdLst.getString("오픈뱅킹무료정책시작일시");
					aplEdYms = rCdcCrdLst.getString("오픈뱅킹무료정책종료일시");
					trmDtcd = rCdcCrdLst.getString("오픈뱅킹기간구분코드");
					chDtlDvCd = rCdcCrdLst.getString("채널세부업무구분코드");
					
				} catch (LNotFoundException nfe2) {
					
					sNcnYn = "N";
					break;//무료정책존재하지 않음 					
				}
				
				//고객별무료거래내역조회
				LData rFreeCnt = new LData();
				
				inpLData.clear();
				inpLData.setString("오픈뱅킹사용자고유번호"  , input.getString("오픈뱅킹사용자고유번호"));
				inpLData.setString("채널세부업무구분코드"   , chDtlDvCd);
				inpLData.setInt("오픈뱅킹무료정책일련번호"   , sAfOpnbCfrePlcySno);
				 
				String yyyyMmDd = StringUtil.substring(curDateTm, 0, 8);
				String yyyyMm = StringUtil.substring(curDateTm, 0, 6);
				String yyyy = StringUtil.substring(curDateTm, 0, 4);
						
				if("Y".equals(trmDtcd)) {		
							
					aplStYms = StringUtil.mergeStr(yyyy,"0101000000");//적용시작일시
				    aplEdYms = StringUtil.mergeStr(yyyy,"1231235959");//적용종료일시 
							
				}else if("M".equals(trmDtcd)) {
							
					aplStYms = StringUtil.mergeStr(yyyyMm,"01000000");//적용시작일시
			        aplEdYms = StringUtil.mergeStr(yyyyMm,DateUtil.getLastDayOfMonth(yyyyMm),"235959");//적용종료일시
			     		    
				}else if("D".equals(trmDtcd)) {
							
					aplStYms = StringUtil.mergeStr(yyyyMmDd,"000000");//적용시작일시
			        aplEdYms = StringUtil.mergeStr(yyyyMmDd,"235959");//적용종료일시
		
				}
				
				inpLData.setString("거래시작일시"   , aplStYms);
				inpLData.setString("거래종료일시"   , aplEdYms);
				
				rFreeCnt = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "selectCfreTrRmgNcn" , inpLData);
				sOpnbFreeCnt = rFreeCnt.getInt("오픈뱅킹무료거래건수");
				
				if(sOpnbFreeCnt == 0) {
					
					opnbTrRmgNcn = sOpnbCfreTrNcn - 1;//오픈뱅킹무료거래건수가 존재하지 않는다면 무료정책건수에서  -1					
								
				}else {
				
					opnbTrRmgNcn = (sOpnbCfreTrNcn-sOpnbFreeCnt)- 1; //오픈뱅킹무료거래건수가 존재한다면 -1
				
				}
							
				LLog.debug.println("정책무로건수" + sOpnbCfreTrNcn);
				LLog.debug.println("현재무료거래건수" + sOpnbFreeCnt);
				LLog.debug.println("잔여건수" + opnbTrRmgNcn);
				
				if( opnbTrRmgNcn >= 0) {
					
					//정책에 의한 건수가 존재할경우만  등록
					iRegData.clear();
					iRegData.setString("오픈뱅킹사용자고유번호"       , input.getString("오픈뱅킹사용자고유번호"));
					iRegData.setInt("오픈뱅킹무료정책일련번호"         , sAfOpnbCfrePlcySno);
					iRegData.setString("채널세부업무구분코드"         , chDtlDvCd);
					iRegData.setInt("오픈뱅킹무료거래건수"            , opnbTrRmgNcn);
					iRegData.setString("오픈뱅킹무료건수갱신일시"      , curDateTm);
					iRegData.setString("적용시작일시"               , aplStYms);
					iRegData.setString("적용종료일시"               , aplEdYms);
					iRegData.setString("오픈뱅킹전문거래년월일"        , input.getString("오픈뱅킹전문거래년월일"));
					iRegData.setString("참가기관출금거래고유번호"       , input.getString("참가기관출금거래고유번호"));
					iRegData.setString("참가기관입금거래고유번호"      , input.getString("참가기관입금거래고유번호"));
					
					iRegData.setString("시스템최초생성식별자"         , "UBF2020101");
					iRegData.setString("시스템최종갱신식별자"         , "UBF2020101");
					
					LLog.debug.println("오픈뱅킹무료거래내역등록 Ldata");
					LLog.debug.println(iRegData);
					
					int insCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "insertTrHis", iRegData); // 오픈뱅킹무료거래내역등록
					
					if(insCnt > 0) {
						sNcnYn = "Y";
					}else {
						sNcnYn = "N";
					}
			
					break; //건수가 존재하면 정상적으로 등록후 break;
					
				}else {
					
					sBfOpnbCfrePlcySno = 0;
					sBfOpnbCfrePlcySno = sAfOpnbCfrePlcySno;
					//다른 무료정책조회
					//존재하지 않는다면 다른무료정책이 잇는지 조회 continue
				}
				
			}//end_while
			
			result.setString("처리결과_V1", sNcnYn);//무료적용여부 N
			
		}//catch_end
    	
		return result;
    }
    
}

