package com.kbcard.ubf.pbi.opnb.opnbPuse.utzRg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCstMgCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotAffectedException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

/** 
 * opnbUtzCnsMgPbc
 * 
 * @logicalname  : 오픈뱅킹이용동의관리Pbc
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

public class OpnbUtzCnsMgPbc {

    /**
     * - 사용자계좌 이용동의 갱신
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용], 채널세부업무구분코드로 이용동의갱신일시 업데이트
     * 2. 계좌 상세의 이용동의항목 등록
     * 3. 계좌 기본의 이용동의항목 업데이트
     * 4. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용, 채널세부업무구분코드
     * <OUTPUT>
     * 갱신결과(T/F)
     * 
     * @serviceId UBF2010102
     * @method chngUsrAccUtzCns
     * @method(한글명) 사용자계좌 이용동의 갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData chngUsrAccUtzCns(LData input) throws LException {

        LLog.debug.println("OpnbUtzCnsMgPbc.chngUsrAccUtzCns[오픈뱅킹이용동의관리Pbc.사용자계좌 이용동의 갱신] START ☆★☆☆★☆☆★☆" +  input);
        LData iChngUsrAccUtzCnsP = input; //i사용자계좌이용동의갱신입력
        LData rChngUsrAccUtzCnsP = new LData(); //r사용자계좌이용동의갱신결과
        String chnDtlsBwkDtcd = ""; //채널세부업무구분코드

        //Validation Check
        if(StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("채널세부업무구분코드"))) {//채널세부업무구분코드
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
 		if(StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("준회원식별자")) &&
 				StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 		chnDtlsBwkDtcd = iChngUsrAccUtzCnsP.getString("채널세부업무구분코드"); //채널세부업무구분코드
 		
		//Ebc 호출
		LData iUpdateUsrAccUtzCnsIn = new LData(); // 사용자계좌이용동의갱신입력
  		LData iRegOpnbCstCnsPhsIn = new LData(); // 오픈뱅킹고객동의이력등록입력
		
		iUpdateUsrAccUtzCnsIn.setString("채널세부업무구분코드", chnDtlsBwkDtcd);
		iUpdateUsrAccUtzCnsIn.setString("오픈뱅킹사용자고유번호", iChngUsrAccUtzCnsP.getString("오픈뱅킹사용자고유번호"));
		iUpdateUsrAccUtzCnsIn.setString("시스템최종갱신식별자", chnDtlsBwkDtcd);
		
		
        if(StringUtil.trimNisEmpty(iChngUsrAccUtzCnsP.getString("OPNB_USR_UNO"))) { //사용자고유번호가 없으면
	        //사용자고유번호조회
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iChngUsrAccUtzCnsP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			
			iUpdateUsrAccUtzCnsIn.setString("오픈뱅킹사용자고유번호", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
        }
        iUpdateUsrAccUtzCnsIn.setString("동의일시", DateUtil.getCurrentTime( "yyyy-MM-dd HH:mm:ss" ));
        

  		iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시", DateUtil.getCurrentTime("yyyyMMddHHmmss"));
		iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호", iUpdateUsrAccUtzCnsIn.getString("오픈뱅킹사용자고유번호"));
	  	iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드", chnDtlsBwkDtcd);
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드", "3"); //통합계좌조회(오픈뱅킹계좌서비스)
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드", "2");
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드", StringUtil.initEmptyValue(iChngUsrAccUtzCnsP.getString("오픈뱅킹동의자료구분코드"),"6") );
		iRegOpnbCstCnsPhsIn.setString("시스템최초생성식별자", chnDtlsBwkDtcd);
  	  	iRegOpnbCstCnsPhsIn.setString("시스템최종갱신식별자", chnDtlsBwkDtcd);
		
  	  	try {
	        //오픈뱅킹계좌상세동의갱신
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "uptOpnbAccDtlCns", iUpdateUsrAccUtzCnsIn);
	       
	        //오픈뱅킹계좌기본동의갱신
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "uptOpnbAccBasCns", iUpdateUsrAccUtzCnsIn);
	
	    	//오픈뱅킹고객동의이력등록 - 통합계좌조회(오픈뱅킹계좌서비스)
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "regOpnbCstCnsPhs", iRegOpnbCstCnsPhsIn);
	        
        
  	  	}catch(LNotAffectedException lnae) {
  	  		lnae.printStackTrace();
  	  		throw new LBizException(ObsErrCode.ERR_H002.getCode(), ObsErrCode.ERR_H002.getName());
  	  	}
        rChngUsrAccUtzCnsP.setString("오픈뱅킹약관동의일시",iRegOpnbCstCnsPhsIn.getString("오픈뱅킹약관동의일시"));
        
		LLog.debug.println("OpnbUtzCnsMgPbc.chngUsrAccUtzCns[오픈뱅킹이용동의관리Pbc.사용자계좌 이용동의 갱신] END ☆★☆☆★☆☆★☆" + rChngUsrAccUtzCnsP );
      
		return rChngUsrAccUtzCnsP;

    }

    /**
     * - 카드사용자 이용동의 갱신
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용], 채널세부업무구분코드로 이용동의갱신일시 업데이트
     * 2. 카드 상세의 이용동의항목 등록
     * 3. 카드 기본의 이용동의항목 업데이트
     * 4. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 갱신결과(T/F)
     * 
     * @serviceId UBF2010103
     * @method chngCrdUsrUtzCns
     * @method(한글명) 카드사용자 이용동의 갱신
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData chngCrdUsrUtzCns(LData input) throws LException {

        LLog.debug.println("OpnbUtzCnsMgPbc.chngCrdUsrUtzCns[오픈뱅킹이용동의관리Pbc.카드사용자 이용동의 갱신] START ☆★☆☆★☆☆★☆" +  input);
        LData iChngCrdUsrUtzCnsP = input; //i카드사용자이용동의갱신입력
        LData rChngCrdUsrUtzCnsP = new LData(); //r카드사용자이용동의갱신결과
        String chnDtlsBwkDtcd = ""; //채널세부업무구분코드
        
        //Validation Check
        if(StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("채널세부업무구분코드"))) {//채널세부업무구분코드
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
 		if(StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("준회원식별자")) &&
 				StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 		chnDtlsBwkDtcd = iChngCrdUsrUtzCnsP.getString("채널세부업무구분코드"); //채널세부업무구분코드
 		
		//Ebc 호출
		LData iUpdateCrdUsrUtzCnsIn = new LData(); // 카드사용자이용동의갱신입력
		LData iRegOpnbCstCnsPhsIn = new LData(); // 오픈뱅킹고객동의이력등록입력
		
		iUpdateCrdUsrUtzCnsIn.setString("채널세부업무구분코드", chnDtlsBwkDtcd);
		iUpdateCrdUsrUtzCnsIn.setString("오픈뱅킹사용자고유번호", iChngCrdUsrUtzCnsP.getString("오픈뱅킹사용자고유번호"));
		iUpdateCrdUsrUtzCnsIn.setString("시스템최종갱신식별자", chnDtlsBwkDtcd);
		
		
        if(StringUtil.trimNisEmpty(iChngCrdUsrUtzCnsP.getString("오픈뱅킹사용자고유번호"))) { //사용자고유번호가 없으면
	        //사용자고유번호조회
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iChngCrdUsrUtzCnsP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			
			iUpdateCrdUsrUtzCnsIn.setString("오픈뱅킹사용자고유번호", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
        }
        iUpdateCrdUsrUtzCnsIn.setString("동의일시", DateUtil.getCurrentTime( "yyyy-MM-dd HH:mm:ss" ));
        

  		iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시", DateUtil.getCurrentTime("yyyyMMddHHmmss"));
		iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호", iUpdateCrdUsrUtzCnsIn.getString("오픈뱅킹사용자고유번호"));
	  	iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드", chnDtlsBwkDtcd);
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드", "4"); //카드정보조회(오픈뱅킹카드서비스)
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드", "2");
	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드", StringUtil.initEmptyValue(iChngCrdUsrUtzCnsP.getString("오픈뱅킹동의자료구분코드"),"6") );
		iRegOpnbCstCnsPhsIn.setString("시스템최초생성식별자", chnDtlsBwkDtcd);
  	  	iRegOpnbCstCnsPhsIn.setString("시스템최종갱신식별자", chnDtlsBwkDtcd);
  	  	
  	  	try {
		
	        //오픈뱅킹카드고객상세동의갱신
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "uptOpnbCrdCstDtlCns", iUpdateCrdUsrUtzCnsIn);
	       
	        //오픈뱅킹카드고객기본동의갱신
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "uptOpnbCrdCstBasCns", iUpdateCrdUsrUtzCnsIn);
	
	        //오픈뱅킹고객동의이력등록 - 카드정보조회(오픈뱅킹카드서비스)
	        BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "regOpnbCstCnsPhs", iRegOpnbCstCnsPhsIn);
	        
  	 	}catch(LNotAffectedException lnae) {
  	  		lnae.printStackTrace();
  	  		throw new LBizException(ObsErrCode.ERR_H002.getCode(), ObsErrCode.ERR_H002.getName());
  	  	}
  	  	
        rChngCrdUsrUtzCnsP.setString("오픈뱅킹약관동의일시",iRegOpnbCstCnsPhsIn.getString("오픈뱅킹약관동의일시"));
		
		LLog.debug.println("OpnbUtzCnsMgPbc.chngCrdUsrUtzCns[오픈뱅킹이용동의관리Pbc.카드사용자 이용동의 갱신] END ☆★☆☆★☆☆★☆" + rChngCrdUsrUtzCnsP );
      
		return rChngCrdUsrUtzCnsP;
		
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
     *  - 채널세부업무구분코드, 오픈뱅킹통합계좌조회동의여부, 계좌조회동의여부, 출금동의여부, 카드정보조회동의여부
     * 
     * @serviceId UBF2010104
     * @method retvUtzCns
     * @method(한글명) 이용동의 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvUtzCns(LData input) throws LException {

        LLog.debug.println("OpnbUtzCnsMgPbc.retvUtzCns[오픈뱅킹이용동의관리Pbc.이용동의 조회] START ☆★☆☆★☆☆★☆" +  input);
        LData iRetvUtzCnsP = input; //i이용동의조회입력
        LData rRetvUtzCnsP = new LData(); //r이용동의조회결과
        
        //Validation Check
        if(StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
 		if(StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("준회원식별자")) &&
 				StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
    		
		//Ebc 호출
		LData iSelectUtzCnsIn = new LData(); // 이용동의조회입력
		LData rSelectOpnbFstRgYms = new LData(); //최초등록일시조회
		LData rSelectOpnbLstUseYms = new LData(); //최종사용일시조회
		LData rSelectOpnbUtzCnsLstUpdYms = new LData(); //이용동의최종갱신일시조회
	
		iSelectUtzCnsIn.setString("오픈뱅킹사용자고유번호", iRetvUtzCnsP.getString("오픈뱅킹사용자고유번호"));
    		
        if(StringUtil.trimNisEmpty(iRetvUtzCnsP.getString("오픈뱅킹사용자고유번호"))) { //사용자고유번호가 없으면
	        //사용자고유번호조회
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iRetvUtzCnsP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			iSelectUtzCnsIn.setString("오픈뱅킹사용자고유번호", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
        }
        
        //최초등록일시조회
        rSelectOpnbFstRgYms = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectOpnbFstRgYms", iSelectUtzCnsIn);
        //이용동의최종갱신일시조회
        rSelectOpnbUtzCnsLstUpdYms = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectOpnbUtzCnsLstUpdYms", iSelectUtzCnsIn);
        //최종사용일시조회
        rSelectOpnbLstUseYms =  BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectOpnbLstUseYms", iSelectUtzCnsIn);
		 
		
		rRetvUtzCnsP.setString("오픈뱅킹사용자고유번호", iSelectUtzCnsIn.getString("오픈뱅킹사용자고유번호"));
		rRetvUtzCnsP.setString("오픈뱅킹최초등록일시", rSelectOpnbFstRgYms.getString("오픈뱅킹최초등록일시"));
		rRetvUtzCnsP.setString("오픈뱅킹이용동의최종갱신일시", rSelectOpnbUtzCnsLstUpdYms.getString("오픈뱅킹이용동의최종갱신일시"));
		rRetvUtzCnsP.setString("오픈뱅킹최종사용일시", rSelectOpnbLstUseYms.getString("오픈뱅킹최종사용일시"));
		
		LLog.debug.println("OpnbUtzCnsMgPbc.retvUtzCns[오픈뱅킹이용동의관리Pbc.이용동의 조회] END ☆★☆☆★☆☆★☆" + rRetvUtzCnsP );
      
		return rRetvUtzCnsP;
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
     * @serviceID UBF2010105
     * @logicalName 계좌별이용동의조회
     * @method retvAccPrUtzCns
     * @method(한글명) 계좌별이용동의 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAccPrUtzCns(LData input) throws LException {
    	
        LData result = new LData();       
    	
        LLog.debug.println("계좌별이용동의조회 입력값");
		LLog.debug.println(input);
		
		if(StringUtil.trimNisEmpty(input.getString("고객계좌번호"))) {
			LLog.debug.println("로그 " + input.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("계좌개설은행코드"))) {
			LLog.debug.println("로그 " + input.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
	 
        try {
        	
        	LData inputData = new LData();
        	LData rtnData = new LData();
        	
        	inputData.clear();
        	
        	inputData.setString("고객계좌번호"        , input.getString("고객계좌번호"));
        	inputData.setString("계좌개설은행코드"     , input.getString("계좌개설은행코드"));
          	inputData.setString("오픈뱅킹이용기관코드"  , UBF_CONST.AuthInfo.UTZ_INS_CD.getCode());
        	inputData.setString("오픈뱅킹사용자고유번호" , input.getString("오픈뱅킹사용자고유번호"));
        	        	                                                
        	rtnData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectAccPrUtzCns" , inputData); // 오픈뱅킹API사용자관리EBC.오픈뱅킹API사용자등록
        	
        	result.setString("계좌조회동의여부"     , rtnData.getString("계좌조회동의여부"));
        	result.setString("출금동의여부"        , rtnData.getString("출금동의여부"));
        	result.setString("고객계좌번호"        , rtnData.getString("고객계좌번호"));
        	
        	LLog.debug.println("계좌별이용동의조회 출력값");
         	LLog.debug.println(result);
        
		} catch (LNotFoundException nfe) {
			
			throw new LBizException(ObsErrCode.ERR_7000.getCode() , StringUtil.mergeStr(ObsErrCode.ERR_7000.getName()));//계좌가 존재하지 않습니다.
		}
		
        return result;
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
     * 
     * @ServiceID UBF2010106
     * @method retvCrdPrUtzCns
     * @method(한글명) 카드사별이용동의 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCrdPrUtzCns(LData input) throws LException {
    	
    	/** INPUT VALIDATION CHECK */
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
 		}
    	
    	LData iSelectUsrUno = new LData();
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회
    		
    		try {
    			
    			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
    			
    			iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
        		
        		input.setString("오픈뱅킹사용자고유번호", opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호"));
             	
             	LLog.debug.println("CI_사용자 고유번호 조회::input::");
           		LLog.debug.println(input);
           		
    		} catch(LException e) {
    			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
      		}
    		
    	}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
 		}
      	
    	LData result = new LData();
    	
    	LData iRegData = new LData();
    	LData rResData = new LData();

    	/** [오픈뱅킹카드고객기본] 원장 조회(동의정보 조회) */
    	
     	iRegData.setString("오픈뱅킹사용자고유번호"  , input.getString("오픈뱅킹사용자고유번호"));
     	iRegData.setString("오픈뱅킹회원금융회사코드" , input.getString("오픈뱅킹회원금융회사코드"));
     	
        try {
        	
        	result = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectCrdPrUtzCns", iRegData);
        	
           	rResData.setInt("오픈뱅킹사용자고유번호"      , result.getInt("오픈뱅킹사용자고유번호"));
           	rResData.setString("오픈뱅킹회원금융회사코드"  , result.getString("오픈뱅킹회원금융회사코드"));
           	rResData.setString("오픈뱅킹개설기관코드"     , result.getString("오픈뱅킹개설기관코드"));
           	rResData.setString("카드정보조회동의여부"     , result.getString("카드정보조회동의여부"));
        	
		} catch (LNotFoundException nfe) {
			throw new LBizException(ObsErrCode.ERR_7001.getCode() , StringUtil.mergeStr(ObsErrCode.ERR_7001.getName()));//카드사 정보가 존재하지 않습니다.
		}

        return rResData;
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
     * @serviceId UBF2010107
     * @method retvCstCnsHisCtg
     * @method(한글명) 고객동의이력 목록 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCstCnsHisCtg(LData input) throws LException {

        LLog.debug.println("OpnbUtzCnsMgPbc.retvCstCnsHisCtg[오픈뱅킹이용동의관리Pbc.고객동의이력 목록 조회] START ☆★☆☆★☆☆★☆" +  input);
        LData iRetvCstCnsHisCtgP = input; //i고객동의이력목록조회입력
        LData rRetvCstCnsHisCtgP = new LData(); //r고객동의이력목록조회입력

        //Validation Check
        if(StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
 		if(StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("준회원식별자")) &&
 				StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 	
		//Ebc 호출
		LData iSelectCstCnsHisCtgIn = new LData(); // 고객동의이력목록조회입력
		LMultiData rSelectCstCnsHisCtgOut = new LMultiData(); // 고객동의이력목록조회출력
		
		iSelectCstCnsHisCtgIn.setString("채널세부업무구분코드", iRetvCstCnsHisCtgP.getString("채널세부업무구분코드"));
		iSelectCstCnsHisCtgIn.setString("오픈뱅킹사용자고유번호", iRetvCstCnsHisCtgP.getString("오픈뱅킹사용자고유번호"));
			
        if(StringUtil.trimNisEmpty(iRetvCstCnsHisCtgP.getString("오픈뱅킹사용자고유번호"))) { //사용자고유번호가 없으면
	        //사용자고유번호조회
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iRetvCstCnsHisCtgP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			iSelectCstCnsHisCtgIn.setString("오픈뱅킹사용자고유번호", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
        }
        
        rSelectCstCnsHisCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "selectCstCnsHisCtg", iSelectCstCnsHisCtgIn);

		rRetvCstCnsHisCtgP.setInt("그리드_cnt", rSelectCstCnsHisCtgOut.getDataCount());
		rRetvCstCnsHisCtgP.set("그리드", rSelectCstCnsHisCtgOut);
		LLog.debug.println("OpnbUtzCnsMgPbc.retvCstCnsHisCtg[오픈뱅킹이용동의관리Pbc.고객동의이력 목록 조회] END ☆★☆☆★☆☆★☆" + rRetvCstCnsHisCtgP );
      
		return rRetvCstCnsHisCtgP;
    }
}

