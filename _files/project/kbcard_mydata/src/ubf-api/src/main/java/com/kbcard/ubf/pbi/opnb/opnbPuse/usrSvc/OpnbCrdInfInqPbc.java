package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCdMgCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCstMgCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.util.StringUtil;

/** 
 * opnbCrdInfInqPbc
 * 
 * @logicalname  : 오픈뱅킹카드정보조회Pbc
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

public class OpnbCrdInfInqPbc {

    /**
     * - 요청자의 카드사별 오픈뱅킹이용동의(제3자정보제공동의) 등록이 되있는 카드사의 보유카드 정보 조회
     * 
     * 1. 오픈뱅킹사용자고유번호를 이용하여 오픈뱅킹보유카드정보기본 원장에 있는 요청자의 보유카드 정보 조회
     * 2. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드발급기관기본, UBF오픈뱅킹보유카드정보기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드
     * <OUTPUT>
     *  LIST
     *  - 오픈뱅킹사용자고유번호, 오픈뱅킹회원금융회사코드, 오픈뱅킹카드일련번호, 오픈뱅킹개설기관코드, 오픈뱅킹카드식별자, 오픈뱅킹마스킹카드번호, 카드상품명, 가족카드여부, 오픈뱅킹카드구분코드, 결제은행코드, 오픈뱅킹마스킹계좌번호, 카드발급년월일, 오픈뱅킹카드결제일
     * 
     * @serviceID UBF2030701
     * @method retvCrdCtg
     * @method(한글명) 카드 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvCrdCtg(LData iRegData) throws LException {
    	LData result = new LData();
    	LData apiBody = new LData();
        LData callOutput = new LData();
         
		String userSeqNo = ""; //오픈뱅킹사용자고유번호
		
        LData rCrdCtgInf = new LData();
 	   
	    /** CI내용으로 조회 요청 */
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
	  	}
	    
	    LData iSelectUsrUno = new LData();
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
	    	
	    	iSelectUsrUno.setString("CI내용", iRegData.getString("CI내용"));
	    	
	    	try {
	    		
	    		OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	        	
	         	userSeqNo = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
	         			
	          	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
	        	LLog.debug.println(userSeqNo);
	        		
	    	} catch(LException e) {
	    		throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	    	}
	    } else {
	    	userSeqNo = iRegData.getString("오픈뱅킹사용자고유번호");
	    }
	    
	 	/** INPUT VALIDATION */
	 	
	 	if(StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) {
	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
		}
	 	 
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹개설기관코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹회원금융회사코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
	    
	    if(StringUtil.trimNisEmpty(userSeqNo)) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("채널세부업무구분코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
	    
	    /** 거래고유번호 채번  */
        
	    LData rCdMg = new LData();        	
	    OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
	    
	    rCdMg = opnbCdMg.crtTrUno(new LData());
	    
	    /** API INPUT SETTING */
	    
		apiBody.setString("채널세부업무구분코드"           , iRegData.getString("채널세부업무구분코드"));
	    apiBody.setString("bank_tran_id"             , rCdMg.getString("거래고유번호")); // 참가기관 거래고유번호
	    apiBody.setString("user_seq_no"              , userSeqNo); // 사용자일련번호
	    apiBody.setString("bank_code_std"            , iRegData.getString("오픈뱅킹개설기관코드")); // 카드사 대표코드(오픈뱅킹개설기관코드)
	    apiBody.setString("member_bank_code"         , iRegData.getString("오픈뱅킹회원금융회사코드")); // 회원 금융회사 코드
	    
	    if(!StringUtil.isEmpty(iRegData.getString("직전조회추적정보_V40"))) {
	    	apiBody.setString("befor_inquiry_trace_info" , iRegData.getString("직전조회추적정보_V40")); // 직전조회추적정보
	    }
	    
	    /** API CALL */
		
	    OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
	    
	    callOutput = opnbApiCpbc.retvLstCrdCtgAPICall(apiBody);
	    
	    /** API OUTPUT SETTING */
	   
	    result.setString("API거래고유번호_V40"    , callOutput.getString("api_tran_id"));
        result.setString("API거래일시_V17"       , callOutput.getString("api_tran_dtm"));
		result.setString("API응답코드_V5"        , callOutput.getString("rsp_code"));
		result.setString("API응답메시지_V300"     , callOutput.getString("rsp_message"));
	
		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 API 정상 조회 시
			
			result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id"));
			result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date"));
			result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran"));
			result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code"));
			result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message"));
			
			result.setLong("오픈뱅킹사용자고유번호"          , callOutput.getLong("user_seq_no")); 
			result.setString("거래내역다음페이지존재여부_V1"  , callOutput.getString("next_page_yn")); 
			result.setString("직전조회추적정보_V40"        , callOutput.getString("befor_inquiry_trace_info"));
			result.setInt("현재페이지조회건수_N2"           , callOutput.getInt("card_cnt")); // 현재 페이지 조회 건수 (최대 20건)
			
			LMultiData icrdList = (LMultiData) callOutput.get("card_list");
			LMultiData rCrdList = new LMultiData();
			
			for(int j = 0; j < callOutput.getInt("card_cnt"); j++) {
				
				/** 카드정보 응답값 설정 */
				
				LData tempData = icrdList.getLData(j);
				
				LData crdInfo = new LData();

				crdInfo.setString("오픈뱅킹카드식별자"     , tempData.getString("card_id")); // 카드식별자
				crdInfo.setString("오픈뱅킹마스킹카드번호"  , tempData.getString("card_num_masked")); // 마스킹된 카드번호
				crdInfo.setString("카드상품명"          , tempData.getString("card_name")); // 상품명
				crdInfo.setInt("가족카드여부"            , tempData.getInt("card_member_type")); // 가족카드여부 - 1: 본인, 2: 가족
				
				rCrdList.addLData(crdInfo);
				
				/** [오픈뱅킹카드정보기본] 원장 적재 (카드목록조회 API SPEC) */
				
				LData regCrdInfo = new LData();
				
				regCrdInfo.setString("오픈뱅킹회원금융회사코드"   , iRegData.getString("오픈뱅킹회원금융회사코드"));
				regCrdInfo.setString("오픈뱅킹사용자고유번호"    , userSeqNo);   
				regCrdInfo.setString("오픈뱅킹카드식별자"       , tempData.getString("card_id"));
				regCrdInfo.setString("오픈뱅킹개설기관코드"      , iRegData.getString("오픈뱅킹개설기관코드"));
				regCrdInfo.setString("오픈뱅킹마스킹카드번호"     , tempData.getString("card_num_masked"));
				regCrdInfo.setString("카드상품명"             , tempData.getString("card_name"));
				regCrdInfo.setInt("가족카드여부"               , tempData.getInt("card_member_type"));
				
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdInfInqEbc", "insertCrdCtg" , regCrdInfo);
				
			} // END_FOR
			
			result.setInt("그리드_cnt", callOutput.getInt("card_cnt"));
			result.set("그리드", rCrdList);
			
		} else {
			
			if("A0002".equals(callOutput.getString("rsp_code"))) { // 참가기관오류
				throw new LBizException(callOutput.getString("bank_rsp_code"), callOutput.getString("rsp_message"));
			} else {// API 오류
				throw new LBizException(callOutput.getString("rsp_code"), callOutput.getString("rsp_message"));
			}
	 		
		}
		
        return result;
    }

    /**
     * - 카드 식별자를 이용하여 해당 카드의 (신용/체크)카드 구분, 결제계좌 등의 카드 기본정보 조회
     * 
     * 1. 카드기본정보조회 API 호출
     * 2. 오픈뱅킹카드정보기본 원장 내 응답값과 일치하는 카드 식별자를 가지고 있는 동일 정보의 상세 속성 업데이트
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드정보기본
     * <INPUT>
     *  오픈뱅킹사용자고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 카드식별자
     * <OUTPUT>
     *  오픈뱅킹카드구분코드, 결제은행코드, 결제 계좌번호, 오픈뱅킹마스킹계좌번호, 카드발급년월일
     * 
     * @serviceID UBF2030702
     * @method retvCrdBasInf
     * @method(한글명) 카드기본정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCrdBasInf(LData input) throws LException {
    	LData result = new LData();
        
    	LData apiBody = new LData();
        LData callOutput = new LData();
        
        String userSeqNo = "";
        
        /** CI내용으로 조회 요청 */
       
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) {  //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
  		}
     	
     	LData iSelectUsrUno = new LData();
     	
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회
     		
     		iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
     		
     		try {
     			
     			OpnbCstMgCpbc opbnCstMgCpbc = new OpnbCstMgCpbc();
     	    	
             	userSeqNo = opbnCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
             			
              	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
            	LLog.debug.println(userSeqNo);
            		
      		} catch(LException e) {
      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
      		}
     		
     	} else {
     		userSeqNo = input.getString("오픈뱅킹사용자고유번호");
     	}
 	   	
 	 	/** INPUT VALIDATION */
     	
        if(StringUtil.trimNisEmpty(userSeqNo)) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
  		}
        
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹개설기관코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
       
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹카드식별자"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹카드식별자"));
		}
        
        /** 거래고유번호 채번  */
    	
       	LData rCdMg = new LData();        	
       	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
       	
       	rCdMg = opnbCdMg.crtTrUno(new LData());
        
    	/** API INPUT SETTING */
       	
		apiBody.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
       	apiBody.setString("bank_tran_id"      , rCdMg.getString("거래고유번호")); // 참가기관 거래고유번호
       	apiBody.setString("user_seq_no"       , userSeqNo); // 사용자일련번호
       	apiBody.setString("bank_code_std"     , input.getString("오픈뱅킹개설기관코드")); // 카드사 대표코드(오픈뱅킹개설기관코드)
       	apiBody.setString("member_bank_code"  , input.getString("오픈뱅킹회원금융회사코드")); // 회원 금융회사 코드
       	apiBody.setString("card_id"           , input.getString("오픈뱅킹카드식별자")); // 카드식별자
       	
        /** API CALL */
		
        OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
        
        callOutput = opnbApiCpbc.retvCrdBasInfAPICall(apiBody);
       	
        /** API OUTPUT SETTING */
 	   
        LData iRegData = new LData();
        
		result.setString("API거래고유번호_V40"    , callOutput.getString("api_tran_id"));
	    result.setString("API거래일시_V17"       , callOutput.getString("api_tran_dtm"));
		result.setString("API응답코드_V5"        , callOutput.getString("rsp_code"));
		result.setString("API응답메시지_V300"     , callOutput.getString("rsp_message"));
		
		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 카드정보조회 API 정상 조회 시
			
			result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id"));
			result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date"));
			result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran"));
			result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code"));
			result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message"));
			
			result.setString("오픈뱅킹카드구분코드"         , callOutput.getString("card_type")); // 카드구분코드
			result.setString("결제은행코드"               , callOutput.getString("settlement_bank_code")); // 결제은행코드
			result.setString("카드결제계좌번호"            , callOutput.getString("settlement_account_num")); // 결제 계좌번호
			//result.setString("카드결제계좌번호"            , CryptoDataUtil.encryptKey( callOutput.getString("settlement_account_num") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
			result.setString("마스킹카드결제계좌번호"        , callOutput.getString("settlement_account_num_masked")); // 마스킹된 출력용 결제 계좌번호
			result.setString("카드발급년월일"              , callOutput.getString("issue_date")); // 카드 발급일자
			
			/** [오픈뱅킹카드정보기본] 원장 Merge */
			
			iRegData.setString("오픈뱅킹회원금융회사코드"  , input.getString("오픈뱅킹회원금융회사코드"));
			iRegData.setString("오픈뱅킹사용자고유번호"   , userSeqNo);
			iRegData.setString("오픈뱅킹카드식별자"      , input.getString("오픈뱅킹카드식별자"));
			iRegData.setString("오픈뱅킹개설기관코드"     , input.getString("오픈뱅킹개설기관코드"));
			iRegData.setString("오픈뱅킹카드구분코드"     , callOutput.getString("card_type")); // 카드구분코드
			iRegData.setString("결제은행코드"           , callOutput.getString("settlement_bank_code")); // 결제은행코드
			
			if(StringUtil.trimNisEmpty(callOutput.getString("settlement_account_num"))) { // 카드결제계좌번호가 NULL - 암호화 X
				iRegData.setString("카드결제계좌번호"    , callOutput.getString("settlement_account_num")); // 결제 계좌번호
			} else {
				iRegData.setString("카드결제계좌번호"    , callOutput.getString("settlement_account_num")); // 결제 계좌번호
				//iRegData.setString("카드결제계좌번호"    , CryptoDataUtil.encryptKey( callOutput.getString("settlement_account_num") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
			}
			
			iRegData.setString("마스킹카드결제계좌번호"    , callOutput.getString("settlement_account_num_masked")); // 마스킹된 출력용 결제 계좌번호
			iRegData.setString("카드발급년월일"          , callOutput.getString("issue_date")); // 카드 발급일자
			
			try {
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdInfInqEbc", "insertCrdCtg" , iRegData);
			} catch(LException e) {
				throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "카드정보기본등록,변경(insertCrdCtg) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
			}
		
		} else {
			if("A0002".equals(callOutput.getString("rsp_code"))) { // 참가기관오류
				throw new LBizException(callOutput.getString("bank_rsp_code"), callOutput.getString("rsp_message"));
			} else {// API 오류
				throw new LBizException(callOutput.getString("rsp_code"), callOutput.getString("rsp_message"));
			}
 			
		}
       
        return result;
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
     * @serviceID UBF2030703
     * @method retvCrdBilBasInf
     * @method(한글명) 카드청구기본정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCrdBilBasInf(LData input) throws LException {
    	LData result = new LData();
        
    	LData apiBody = new LData();
        LData callOutput = new LData();
        
        String userSeqNo = "";
        
        /** CI내용으로 조회 요청 */
        
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 사용자 고유번호, CI내용"));
  		}
     	
     	LData iSelectUsrUno = new LData();
     	
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
     		
     		iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
     		
     		try {
     			
     	    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
     	    	
             	userSeqNo = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
             			
              	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
            	LLog.debug.println(userSeqNo);
            		
      		} catch(LException e) {
      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
      		}
     		
     	} else {
     		userSeqNo = input.getString("오픈뱅킹사용자고유번호");
     	}
     	
 	 	/** INPUT VALIDATION */
     	
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
  		}
        
        if(StringUtil.trimNisEmpty(userSeqNo)) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹개설기관코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
       
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("조회시작년월"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 조회시작년월"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("조회종료년월"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 조회종료년월"));
		}
        
        /** 거래고유번호 채번  */
    	
       	LData rCdMg = new LData();        	
       	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
       	
       	rCdMg = opnbCdMg.crtTrUno(new LData());
        
    	/** API INPUT SETTING */
       	
		apiBody.setString("채널세부업무구분코드"           , input.getString("채널세부업무구분코드"));
       	apiBody.setString("bank_tran_id"             , rCdMg.getString("거래고유번호")); // 참가기관 거래고유번호
       	apiBody.setString("user_seq_no"              , userSeqNo); // 사용자일련번호
       	apiBody.setString("bank_code_std"            , input.getString("오픈뱅킹개설기관코드")); // 카드사 대표코드(오픈뱅킹개설기관코드)
       	apiBody.setString("member_bank_code"         , input.getString("오픈뱅킹회원금융회사코드")); // 회원 금융회사 코드
       	apiBody.setString("from_month"               , input.getString("조회시작년월")); // 조회 시작월
       	apiBody.setString("to_month"                 , input.getString("조회종료년월")); // 조회 종료월
       	
       	if(!StringUtil.isEmpty(input.getString("직전조회추적정보_V40"))) {
       		apiBody.setString("befor_inquiry_trace_info" , input.getString("직전조회추적정보_V40")); // 직전조회추적정보_V40
        }
       	
        /** API CALL */
		
        OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
        
        callOutput = opnbApiCpbc.retvCrdBilBasInfAPICall(apiBody);
       	
        /** API OUTPUT SETTING */
 	   
        result.setString("API거래고유번호_V40"    , callOutput.getString("api_tran_id"));
	    result.setString("API거래일시_V17"       , callOutput.getString("api_tran_dtm"));
		result.setString("API응답코드_V5"        , callOutput.getString("rsp_code"));
		result.setString("API응답메시지_V300"     , callOutput.getString("rsp_message"));

		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 카드정보조회 API 정상 조회 시
			
			result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id"));
			result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date"));
			result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran"));
			result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code"));
			result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message"));
			
			result.setLong("오픈뱅킹사용자고유번호"             , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
			result.setString("거래내역다음페이지존재여부_V1"     , callOutput.getString("next_page_yn")); //  다음페이지 존재여부 “Y”:다음 페이지 존재, “N”:마지막 페이지
			result.setString("직전조회추적정보_V40"           , callOutput.getString("befor_inquiry_trace_info")); // 직전조회추적정보
			result.setInt("청구목록현재페이지조회건수_N2"        , callOutput.getInt("bill_cnt")); // 현재 페이지 조회 건수 (최대 20건)
			
			LMultiData iBilList = (LMultiData) callOutput.get("bill_list");
			LMultiData rBilList = new LMultiData();
			
			for(int j = 0; j < callOutput.getInt("bill_cnt"); j++) {
				
				/** 카드청구기본정보 응답값 설정 */
				
				LData tempData = iBilList.getLData(j);
				
				LData bilInfo = new LData();

				bilInfo.setString("청구년월"         , tempData.getString("charge_month")); // 청구년월
				bilInfo.setInt("카드결제일련번호"      , tempData.getInt("settlement_seq_no")); // 카드결제일련번호
				bilInfo.setString("오픈뱅킹카드식별자"  , tempData.getString("card_id")); // 카드식별자
				bilInfo.setLong("청구금액"           , tempData.getLong("charge_amt")); // 청구금액
				bilInfo.setString("오픈뱅킹카드결제일"  , tempData.getString("settlement_day")); // 오픈뱅킹카드결제일
				bilInfo.setString("실제결제년월일"     , tempData.getString("settlement_date")); // 실제결제년월일
				bilInfo.setString("오픈뱅킹카드구분코드" , tempData.getString("credit_check_type")); // 오픈뱅킹카드구분코드
				
				rBilList.addLData(bilInfo);
				
				/** [오픈뱅킹카드청구기본] 원장 적재 (카드청구기본정보 조회 API SPEC) */
				
				LData regBilInfo = new LData();
				
				regBilInfo.setString("오픈뱅킹사용자고유번호"   , userSeqNo);
				regBilInfo.setString("청구년월"             , tempData.getString("charge_month"));
				regBilInfo.setString("오픈뱅킹회원금융회사코드"  , input.getString("오픈뱅킹회원금융회사코드"));
				regBilInfo.setInt("카드결제일련번호"          , tempData.getInt("settlement_seq_no"));
				regBilInfo.setString("오픈뱅킹카드식별자"      , tempData.getString("card_id"));
				
				regBilInfo.setString("오픈뱅킹개설기관코드"    , input.getString("오픈뱅킹개설기관코드"));
				regBilInfo.setLong("청구금액"               , tempData.getLong("charge_amt"));
				regBilInfo.setString("오픈뱅킹카드결제일"      , tempData.getString("settlement_day"));
				regBilInfo.setString("실제결제년월일"         , tempData.getString("settlement_date"));
				regBilInfo.setString("오픈뱅킹카드구분코드"     , tempData.getString("credit_check_type"));
				
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdInfInqEbc", "insertCrdBilBasInf" , regBilInfo);
				
			} // END_FOR
			
			result.setInt("그리드_cnt", callOutput.getInt("bill_cnt"));
			result.set("그리드", rBilList);
		
		} else {
			
			if("A0002".equals(callOutput.getString("rsp_code"))) { // 참가기관 오류
				throw new LBizException(callOutput.getString("bank_rsp_code"), callOutput.getString("rsp_message"));
			} else { // API 오류
				throw new LBizException(callOutput.getString("rsp_code"), callOutput.getString("rsp_message"));
			}
 			
		}
       
        return result;
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
     * @serviceID UBF2030704
     * @method retvCrdBilDtlInf
     * @method(한글명) 카드청구상세정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCrdBilDtlInf(LData input) throws LException {
    	LData result = new LData();
        
    	LData apiBody = new LData();
        LData callOutput = new LData();
        
        String userSeqNo = "";
        
        /** CI내용으로 조회 요청 */
        
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 사용자고유번호, CI내용"));
  		}
     	
     	LData iSelectUsrUno = new LData();
     	
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
     		
     		iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
     		
     		try {
     			
     	    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
     	    	
             	userSeqNo = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
             			
              	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
            	LLog.debug.println(userSeqNo);
            		
      		} catch(LException e) {
      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
      		}
     		
     	} else {
     		userSeqNo = input.getString("오픈뱅킹사용자고유번호");
     	}
     	
 	 	/** INPUT VALIDATION */
     	
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
  		}
        
        if(StringUtil.trimNisEmpty(userSeqNo)) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹개설기관코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
       
        if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("청구년월"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 청구년월"));
		}
        
        if(StringUtil.trimNisEmpty(input.getString("카드결제일련번호"))) {
        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 카드결제일련번호"));
		}
        
        /** 거래고유번호 채번  */
    	
       	LData rCdMg = new LData();        	
       	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
       	
       	rCdMg = opnbCdMg.crtTrUno(new LData());
        
    	/** API INPUT SETTING */
       	
		apiBody.setString("채널세부업무구분코드"           , input.getString("채널세부업무구분코드"));
       	apiBody.setString("bank_tran_id"             , rCdMg.getString("거래고유번호")); // 참가기관 거래고유번호
       	apiBody.setString("user_seq_no"              , userSeqNo); // 사용자일련번호
       	apiBody.setString("bank_code_std"            , input.getString("오픈뱅킹개설기관코드")); // 카드사 대표코드(오픈뱅킹개설기관코드)
       	apiBody.setString("member_bank_code"         , input.getString("오픈뱅킹회원금융회사코드")); // 회원 금융회사 코드
       	apiBody.setString("charge_month"             , input.getString("청구년월")); // 청구년월
       	apiBody.setString("settlement_seq_no"        , input.getString("카드결제일련번호")); // 결제순번(카드결제일련번호)
       	
       	if(!StringUtil.isEmpty(input.getString("직전조회추적정보_V40"))) {
       		apiBody.setString("befor_inquiry_trace_info" , input.getString("직전조회추적정보_V40")); // 직전조회추적정보_V40
        }
       	
        /** API CALL */
		
        OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
        
        callOutput = opnbApiCpbc.retvCrdBilDtlInfAPICall(apiBody);
       	
        /** API OUTPUT SETTING */
 	   
        result.setString("API거래고유번호_V40"    , callOutput.getString("api_tran_id"));
	    result.setString("API거래일시_V17"       , callOutput.getString("api_tran_dtm"));
		result.setString("API응답코드_V5"        , callOutput.getString("rsp_code"));
		result.setString("API응답메시지_V300"     , callOutput.getString("rsp_message"));

		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 카드정보조회 API 정상 조회 시
			
			result.setString("참가기관거래고유번호_V20"        , callOutput.getString("bank_tran_id"));
			result.setString("참가기관거래일자_V8"            , callOutput.getString("bank_tran_date"));
			result.setString("참가기관표준코드_V3"            , callOutput.getString("bank_code_tran"));
			result.setString("참가기관응답코드_V3"            , callOutput.getString("bank_rsp_code"));
			result.setString("참가기관응답메시지_V100"         , callOutput.getString("bank_rsp_message"));
			
			result.setLong("오픈뱅킹사용자고유번호"             , callOutput.getLong("user_seq_no")); 
			result.setString("거래내역다음페이지존재여부_V1"     , callOutput.getString("next_page_yn")); //  다음페이지 존재여부 “Y”:다음 페이지 존재, “N”:마지막 페이지
			result.setString("직전조회추적정보_V40"           , callOutput.getString("befor_inquiry_trace_info")); // 직전조회추적정보
			result.setInt("청구상세목록현재페이지조회건수_N2"     , callOutput.getInt("bill_detail_cnt")); // 현재 페이지 조회 건수 (최대 20건)
			result.setString("오픈뱅킹카드청구상세거래일시"       , callOutput.getString("api_tran_dtm")); // 거래일시(밀리세컨드) // 오픈뱅킹카드청구상세거래일시 
			
			LMultiData iBilDtlList = (LMultiData) callOutput.get("bill_detail_list");
			LMultiData rBilDtlList = new LMultiData();
			
			int opnbCrdBilDtlSno = 0; //오픈뱅킹카드청구상세일련번호
			
			for(int j = 0; j < callOutput.getInt("bill_detail_cnt"); j++) {
				
				/** 카드청구기본정보 응답값 설정 */
				
				LData tempData = iBilDtlList.getLData(j);
				
				LData bilDtlInfo = new LData();

				bilDtlInfo.setString("오픈뱅킹카드식별번호"     , tempData.getString("card_value")); // 카드식별값
				bilDtlInfo.setString("카드사용년월일"         , tempData.getString("paid_date")); // 사용일자
				bilDtlInfo.setString("오픈뱅킹카드사용시각"     , tempData.getString("paid_time")); // 사용시간
				bilDtlInfo.setLong("카드이용금액"             , tempData.getLong("paid_amt")); // 이용금액
				bilDtlInfo.setString("오픈뱅킹마스킹가맹점명"    , tempData.getString("merchant_name_masked")); // 마스킹 가맹점명
				bilDtlInfo.setLong("신용판매거래수수료"         , tempData.getLong("credit_fee_amt")); // 신용판매수수료
				bilDtlInfo.setString("오픈뱅킹카드상품구분코드"   , tempData.getString("product_type")); // 상품구분 01:일시불, 02:신용판매할부, 03:현금서비스
				
				/** 오픈뱅킹카드청구상세일련번호 채번 */

				LData iOpnbCrdBilDtlSno = new LData();
		    	LData rOpnbCrdBilDtlSno = new LData();						      
		    	rOpnbCrdBilDtlSno = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdInfInqEbc", "selectOpnbCrdBilDtlSnoBbr" , iOpnbCrdBilDtlSno);
		    	opnbCrdBilDtlSno = rOpnbCrdBilDtlSno.getInt("오픈뱅킹카드청구상세일련번호");
				
				bilDtlInfo.setInt("오픈뱅킹카드청구상세일련번호"   , opnbCrdBilDtlSno); // 오픈뱅킹카드청구상세일련번호
				
				rBilDtlList.addLData(bilDtlInfo);
				
				/** [오픈뱅킹카드청구상세] 원장 적재 (카드청구상세정보 조회 API SPEC) */
				
				LData regBilDtlInfo = new LData();
				
				regBilDtlInfo.setString("오픈뱅킹사용자고유번호"      , userSeqNo);
				regBilDtlInfo.setString("청구년월"                , input.getString("청구년월"));
				regBilDtlInfo.setString("카드결제일련번호"          , input.getString("카드결제일련번호"));
				regBilDtlInfo.setString("오픈뱅킹회원금융회사코드"    , input.getString("오픈뱅킹회원금융회사코드"));
				regBilDtlInfo.setString("오픈뱅킹카드청구상세거래일시" , callOutput.getString("api_tran_dtm"));
				regBilDtlInfo.setInt("오픈뱅킹카드청구상세일련번호"    , opnbCrdBilDtlSno);
				
				regBilDtlInfo.setString("오픈뱅킹개설기관코드"       , input.getString("오픈뱅킹개설기관코드"));
				regBilDtlInfo.setString("오픈뱅킹카드식별값"        , tempData.getString("card_value"));
				regBilDtlInfo.setString("카드사용년월일"           , tempData.getString("paid_date"));
				regBilDtlInfo.setString("오픈뱅킹카드사용시각"       , tempData.getString("paid_time"));
				regBilDtlInfo.setLong("카드이용금액"               , tempData.getLong("paid_amt"));
				regBilDtlInfo.setString("오픈뱅킹마스킹가맹점명"      , tempData.getString("merchant_name_masked"));
				regBilDtlInfo.setLong("신용판매거래수수료"           , tempData.getLong("credit_fee_amt"));
				regBilDtlInfo.setString("오픈뱅킹카드상품구분코드"     , tempData.getString("product_type"));

				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdInfInqEbc", "insertCrdBilDtlInf" , regBilDtlInfo);
				
			} // END_FOR
			
			result.setInt("그리드_cnt", callOutput.getInt("bill_detail_cnt"));
			result.set("그리드", rBilDtlList);
		
		} else {
			
			if("A0002".equals(callOutput.getString("rsp_code"))) { // 참가기관 오류
				throw new LBizException(callOutput.getString("bank_rsp_code"), callOutput.getString("rsp_message"));
			} else { // API 오류
				throw new LBizException(callOutput.getString("rsp_code"), callOutput.getString("rsp_message"));
			}
			
		}
       
        return result;
    }

}
