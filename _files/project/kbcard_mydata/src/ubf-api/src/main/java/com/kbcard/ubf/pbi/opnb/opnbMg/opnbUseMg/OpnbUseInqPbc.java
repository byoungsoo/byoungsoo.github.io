package com.kbcard.ubf.pbi.opnb.opnbMg.opnbUseMg;

import org.apache.commons.lang.StringUtils;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCdMgCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;

/** 
 * opnbUseInqPbc
 * 
 * @logicalname  : 오픈뱅킹사용조회Pbc
 * @author       : 김상현
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       김상현       최초 생성
 *
 */

public class OpnbUseInqPbc {

    /**
     * -  참가기관의 서비스 상태(거래가능, 장애, 개시이전, 종료 등)를 확인
     * 
     * 1. API 호출
     * 2. 참가기관상태목록 리턴
     * 
     * <관련 테이블>
     * 
     * <INPUT>
     * 
     * <OUTPUT>
     * LIST
     *   - 참가기관상태(거래가능, 장애, 개시이전, 종료)
     * 
     * @method retvPtcpInsSts
     * @method(한글명) 참가기관 상태 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvPtcpInsSts(LData input) throws LException {
        
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("참가기관상태조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
        LData iRetvPtcpInsStsP = input; //i참가기관상태조회입력
        LData rRetvPtcpInsStsP = new LData(); //r참가기관상태조회출력
        
 		try {
 			
 			//Validation Check
 			if(StringUtils.isEmpty(iRetvPtcpInsStsP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
 				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
 			}
 			
 			//API 호출
 	  		LData iRetvPtcpInsStsAPICall = new LData(); // 참가기관상태조회입력
 	  		LData rRetvPtcpInsStsAPICall = new LData(); // 참가기관상태조회출력
 	        
 	  		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
 	  		
 	  		iRetvPtcpInsStsAPICall.setString("채널세부업무구분코드", iRetvPtcpInsStsP.getString("채널세부업무구분코드"));
 	  		
 	  		rRetvPtcpInsStsAPICall = opnbApi.retvPtcpInsStsAPICall(iRetvPtcpInsStsAPICall);
 	  		
 	  		if("A0002".equals(rRetvPtcpInsStsAPICall.getString("rsp_code"))) {
 	  			throw new LBizException(rRetvPtcpInsStsAPICall.getString("bank_rsp_code"), "", StringUtil.mergeStr(rRetvPtcpInsStsAPICall.getString("rsp_message"), " [", rRetvPtcpInsStsAPICall.getString("bank_rsp_message"), "]"));
 	  		}
 	  		
 	  		if(!"A0000".equals(rRetvPtcpInsStsAPICall.getString("rsp_code"))) {
 	  			throw new LBizException(rRetvPtcpInsStsAPICall.getString("rsp_code"), rRetvPtcpInsStsAPICall.getString("rsp_message"));
 	  		}
 	  		
 	  		rRetvPtcpInsStsP.setString("오픈뱅킹API거래고유번호",		rRetvPtcpInsStsAPICall.getString("api_tran_id"));
 	  		rRetvPtcpInsStsP.setString("API거래일시_V17",			rRetvPtcpInsStsAPICall.getString("api_tran_dtm"));
 	  		rRetvPtcpInsStsP.setString("오픈뱅킹API응답구분코드",		rRetvPtcpInsStsAPICall.getString("rsp_code"));
 	  		rRetvPtcpInsStsP.setString("오픈뱅킹API응답메시지내용",	rRetvPtcpInsStsAPICall.getString("rsp_message"));
 	  		rRetvPtcpInsStsP.setInt("그리드_cnt",					rRetvPtcpInsStsAPICall.getInt("res_cnt"));

 	  		LMultiData tmpLMultiData = new LMultiData();
 	  		
 	  		for(int i=0; i<rRetvPtcpInsStsAPICall.getLMultiData("res_list").getDataCount(); i++) {
 	  			
 	  			LData tmpLData = new LData();
 	  			tmpLData.setString("참가기관구분_V1",				rRetvPtcpInsStsAPICall.getLMultiData("res_list").getLData(i).getString("bank_type"));
 	  			tmpLData.setString("오픈뱅킹개설기관코드",			rRetvPtcpInsStsAPICall.getLMultiData("res_list").getLData(i).getString("bank_code_std"));
 	  			tmpLData.setString("오픈뱅킹개설기관명",				rRetvPtcpInsStsAPICall.getLMultiData("res_list").getLData(i).getString("bank_name"));
 	  			tmpLData.setString("오픈뱅킹기관상태구분코드",			rRetvPtcpInsStsAPICall.getLMultiData("res_list").getLData(i).getString("bank_status"));
 	  			
 	  			tmpLMultiData.addLData(tmpLData);
 	  			
 	  		}
 	  		
 	  		rRetvPtcpInsStsP.set("그리드", tmpLMultiData);
 	  		
 		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
        
 		if(LLog.debug.isEnabled()) {
 			LLog.debug.println("----------[rRetvPtcpInsStsP]----------");
 			LLog.debug.println(rRetvPtcpInsStsP);
 			LLog.debug.println("참가기관상태조회 END ☆★☆☆★☆☆★☆" );
 		}
		
        return rRetvPtcpInsStsP;
    }

    /**
     * - 이용기관이 사용한 API에 대한 이용 수수료 조회
     * 
     * 1. 조회시작일자, 조회종료일자로 API 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * -
     * <INPUT>
     * 조회시작일자, 조회종료일자
     * <OUTPUT>
     * 오픈뱅킹API거래고유번호, API거래일시, 오픈뱅킹API응답구분코드, 오픈뱅킹API응답메시지내용
     * LIST
     *  - 오픈뱅킹API관리구분코드, 이용기관수수료, 처리대행비용 
     * 
     * @method retvFee
     * @method(한글명) 수수료 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvFee(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("수수료조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
        LData iRetvFeeP = input; //i수수료조회입력
        LData rRetvFeeP = new LData(); //r수수료조회출력
    	
    	try {
			
    		//Validation Check
        	if(StringUtils.isEmpty(iRetvFeeP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
     			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
        	if(StringUtils.isEmpty(iRetvFeeP.getString("거래내역조회시작일자_V8"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
        	if(StringUtils.length(iRetvFeeP.getString("거래내역조회시작일자_V8")) != 8) {
        		throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회시작일자 자릿수 오류"));
        	}
        	if(StringUtils.isEmpty(iRetvFeeP.getString("거래내역조회종료일자_V8"))) {		
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
        	if(StringUtils.length(iRetvFeeP.getString("거래내역조회종료일자_V8")) != 8) {
        		throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회종료일자 자릿수 오류"));
        	}
        	
    		//API 호출
      		LData iRetvFeeAPICall = new LData(); //수수료조회입력
      		LData rRetvFeeAPICall = new LData(); //수수료조회출력
            
      		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
      		
      		iRetvFeeAPICall.setString("채널세부업무구분코드",			iRetvFeeP.getString("채널세부업무구분코드"));
      		iRetvFeeAPICall.setString("from_date",				iRetvFeeP.getString("거래내역조회시작일자_V8"));
      		iRetvFeeAPICall.setString("to_date",				iRetvFeeP.getString("거래내역조회종료일자_V8"));

      		rRetvFeeAPICall = opnbApi.retvFeeAPICall(iRetvFeeAPICall);

      		if("A0002".equals(rRetvFeeAPICall.getString("rsp_code"))) {
      			throw new LBizException(rRetvFeeAPICall.getString("bank_rsp_code"), "", StringUtil.mergeStr(rRetvFeeAPICall.getString("rsp_message"), " [", rRetvFeeAPICall.getString("bank_rsp_message"), "]"));
 	  		}
      		
      		if(!"A0000".equals(rRetvFeeAPICall.getString("rsp_code"))) {
 	  			throw new LBizException(rRetvFeeAPICall.getString("rsp_code"), rRetvFeeAPICall.getString("rsp_message"));
 	  		}
      		
      		rRetvFeeP.setString("오픈뱅킹API거래고유번호",			rRetvFeeAPICall.getString("api_tran_id"));
      		rRetvFeeP.setString("API거래일시_V17",					rRetvFeeAPICall.getString("api_tran_dtm"));
      		rRetvFeeP.setString("오픈뱅킹API응답구분코드",			rRetvFeeAPICall.getString("rsp_code"));
      		rRetvFeeP.setString("오픈뱅킹API응답메시지내용",			rRetvFeeAPICall.getString("rsp_message"));
      		rRetvFeeP.setInt("그리드_cnt",						rRetvFeeAPICall.getInt("res_cnt"));

    		LMultiData tmpLMultiData = new LMultiData();
    		
    		for(int i=0; i<rRetvFeeAPICall.getLMultiData("res_list").getDataCount(); i++) {
      			
      			LData tmpLData = new LData();
      			
      			tmpLData.setString("오픈뱅킹API관리구분코드",			rRetvFeeAPICall.getLMultiData("res_list").getLData(i).getString("api_manage_id"));
      			tmpLData.setString("이용기관수수료_V12",			rRetvFeeAPICall.getLMultiData("res_list").getLData(i).getString("use_org_fee"));
      			tmpLData.setString("처리대행비용_V12",				rRetvFeeAPICall.getLMultiData("res_list").getLData(i).getString("hdl_agt_fee"));

      			tmpLMultiData.addLData(tmpLData);
      			
      		}
    		
    		rRetvFeeP.set("그리드", tmpLMultiData);
    		
    	} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
		
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("----------[rRetvFeeP]----------");
    		LLog.debug.println(rRetvFeeP);
    		LLog.debug.println("수수료조회 END ☆★☆☆★☆☆★☆" );
    	}
      
		return rRetvFeeP;
		
    }

    /**
     * - 이용기관이 사용한 API에 대한 집계 조회.
     * 
     * 1. 조회일자로 API 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * -
     * <INPUT>
     * 조회일자
     * <OUTPUT>
     * 오픈뱅킹API거래고유번호, API거래일시, 오픈뱅킹API응답구분코드, 오픈뱅킹API응답메시지내용
     * LIST
     *  - 오픈뱅킹API관리구분코드, API정상건수, API정상이체금액
     * @method retvSmm
     * @method(한글명) 집계 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvSmm(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("집계조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
        LData iRetvSmmP = input; //i집계조회입력
        LData rRetvSmmP = new LData(); //r집계조회출력
    	
    	try {
    		
    		//Validation Check
    		if(StringUtils.isEmpty(iRetvSmmP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtils.isEmpty(iRetvSmmP.getString("오픈뱅킹조회일자_V8"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtils.length(iRetvSmmP.getString("오픈뱅킹조회일자_V8")) != 8) {
    			throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회일자 자릿수 오류"));
    		}
			
    		//API 호출
      		LData iRetvSmmAPICall = new LData(); //집계조회입력
      		LData rRetvSmmAPICall = new LData(); //집계조회출력
            
      		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
      		
      		iRetvSmmAPICall.setString("채널세부업무구분코드",			iRetvSmmP.getString("채널세부업무구분코드"));
      		iRetvSmmAPICall.setString("inquiry_date",			iRetvSmmP.getString("오픈뱅킹조회일자_V8"));

      		rRetvSmmAPICall = opnbApi.retvSmmAPICall(iRetvSmmAPICall);

      		if("A0002".equals(rRetvSmmAPICall.getString("rsp_code"))) {
      			throw new LBizException(rRetvSmmAPICall.getString("bank_rsp_code"), "", StringUtil.mergeStr(rRetvSmmAPICall.getString("rsp_message"), " [", rRetvSmmAPICall.getString("bank_rsp_message"), "]"));
 	  		}
      		
      		if(!"A0000".equals(rRetvSmmAPICall.getString("rsp_code"))) {
 	  			throw new LBizException(rRetvSmmAPICall.getString("rsp_code"), rRetvSmmAPICall.getString("rsp_message"));
 	  		}
      		
      		rRetvSmmP.setString("오픈뱅킹API거래고유번호",			rRetvSmmAPICall.getString("api_tran_id"));
      		rRetvSmmP.setString("API거래일시_V17",					rRetvSmmAPICall.getString("api_tran_dtm"));
      		rRetvSmmP.setString("오픈뱅킹API응답구분코드",			rRetvSmmAPICall.getString("rsp_code"));
      		rRetvSmmP.setString("오픈뱅킹API응답메시지내용",			rRetvSmmAPICall.getString("rsp_message"));
      		rRetvSmmP.setInt("그리드_cnt",						rRetvSmmAPICall.getInt("res_cnt"));

    		LMultiData tmpLMultiData = new LMultiData();
    		
    		for(int i=0; i<rRetvSmmAPICall.getLMultiData("res_list").getDataCount(); i++) {
      			
      			LData tmpLData = new LData();
      			
      			tmpLData.setString("오픈뱅킹API관리구분코드",			rRetvSmmAPICall.getLMultiData("res_list").getLData(i).getString("api_manage_id"));
      			tmpLData.setString("API정상건수_V12",				rRetvSmmAPICall.getLMultiData("res_list").getLData(i).getString("normal_count"));
      			tmpLData.setString("API정상이체금액_V18",			rRetvSmmAPICall.getLMultiData("res_list").getLData(i).getString("normal_tran_amt"));

      			tmpLMultiData.addLData(tmpLData);
      			
      		}
    		
    		rRetvSmmP.set("그리드", tmpLMultiData);
    		
    	} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
		
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("----------[rRetvSmmP]----------");
    		LLog.debug.println(rRetvSmmP);
    		LLog.debug.println("집계조회 END ☆★☆☆★☆☆★☆" );
    	}
      
		return rRetvSmmP;
		
    }

    /**
     * - 이용기관의 출금이체한도를 실시간으로 조회. 요청 당일의 출금이체한도만 조회할 수 있음.
     * 
     * 1. 출금이체한도조회API 호출
     * 2. 응답받는 값을 오픈뱅킹기관거래내역 테이블에 적재
     * 3. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - NONE
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * LIST
     * - 참가기관코드, 일간출금이체한도금액, 당일누적출금이체금액, 건당출금이체한도금액
     * 
     * @serviceID UBF1010204
     * @logicalName 출금이체한도 조회
     * @method retvOdwTracLm
     * @method(한글명) 출금이체한도 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOdwTracLm(LData input) throws LException {
        LLog.debug.println("OpnbUseInqPbc.retvOdwTracLm START ☆★☆☆★☆☆★☆");
		
        LData rOdwTracLm = new LData(); //송금인정보조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		LData rCdMg = new LData();
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
		rCdMg = opnbCdMg.crtTrUno(iCdMg);
		
		// 출금이체한도조회 API호출
		LData iOdwTracLmAPICall = new LData(); //i출금이체한도조회API호출입력
        LData rOdwTracLmAPICall = new LData(); //r출금이체한도조회API호출결과
        
        LMultiData tmpRmtrInf = new LMultiData(); //결과값 출력 Group
        
		try {

			iOdwTracLmAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
			iOdwTracLmAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
			iOdwTracLmAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시

	        LLog.debug.println("--- 출금이체한도조회 API호출 입력값 ----"); 
			LLog.debug.println(iOdwTracLmAPICall);
			
	        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
	        
	        rOdwTracLmAPICall = opnbApi.retvOdwTracLmAPICall(iOdwTracLmAPICall); //출금이체한도 조회
			
			//rOdwTracLm = rOdwTracLmAPICall;
	        //API 호출이후 처리
	        rOdwTracLm.setString("API거래고유번호_V40" , rOdwTracLmAPICall.getString("api_tran_id"));        	//API거래고유번호_V40       
	        rOdwTracLm.setString("API거래일시_V17"     , rOdwTracLmAPICall.getString("api_tran_dtm"));        	//API거래일시_V17  
	        rOdwTracLm.setString("API응답코드_V5"      , rOdwTracLmAPICall.getString("rsp_code"));        		//API응답코드_V5    
	        rOdwTracLm.setString("API응답메시지_V300"  , rOdwTracLmAPICall.getString("rsp_message"));         	//API응답메시지_V300       
	        rOdwTracLm.setLong("참가기관개수_N5"       , rOdwTracLmAPICall.getLong("res_cnt"));         		//참가기관개수_N5        

	        for(int i=0; i < rOdwTracLmAPICall.getLMultiData("res_list").getDataCount(); i++) {
	        	LData tmpSelRec = new LData();
	        	
	        	tmpSelRec.setString("참가기관코드_V3"        , rOdwTracLmAPICall.getLMultiData("res_list").getLData(i).getString("bank_code_std"));   //참가기관코드_V3    
	        	tmpSelRec.setLong("일간출금이체한도금액_N18" , rOdwTracLmAPICall.getLMultiData("res_list").getLData(i).getLong("day_wd_limit_amt"));  //일간출금이체한도금액_N18
	        	tmpSelRec.setLong("당일누적출금이체금액_N18" , rOdwTracLmAPICall.getLMultiData("res_list").getLData(i).getLong("day_wd_amt"));        //당일누적출금이체금액_N18
	        	tmpSelRec.setLong("건당출금이체한도금액_N18" , rOdwTracLmAPICall.getLMultiData("res_list").getLData(i).getLong("perc_wd_limit_amt")); //건당출금이체한도금액_N18
	        	
	        	tmpRmtrInf.addLData(tmpSelRec); //출력 Group 추가
	        }
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		rOdwTracLm.setLong("그리드_cnt", rOdwTracLmAPICall.getLong("res_cnt")); //그리드count
		rOdwTracLm.set("그리드", tmpRmtrInf); //그리드셋
        
		LLog.debug.println("OpnbUseInqPbc.retvOdwTracLm END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rOdwTracLm);
        
        
        return rOdwTracLm;
    }

    /**
     * - 이용기관 관련된 이상금융거래 탐지내역을 조회
     * - 요청 당일 기준으로 1개월 이내의 내역에 대해서만 확인이 가능하며 요청 일시 기준으로 1분 이전까지만 탐지내역 조회가 가능
     * - 최대 조회기간은 10분
     * 
     * 1. 조회일자, 조회시작시간, 조회종료시간으로 API 호출
     * 2. 탐지내역 건수 초과여부, 이상금융거래 탐지 건수, 탐지 시간, 이상금융거래 탐지 정책룰, 사용자일련번호, 참가기관 표준코드, 계좌번호, 거래금액 리턴
     * 
     * - 탐지내역 건수 초과여부 : 조회범위 내에 탐지내역 건수가 API 응답건수(현재 100건)를 초과할 경우 세팅되며, 이 경우 조회범위를 작게 하여 다시 요청해야 함
     * - 이상금융거래 탐지 건수 : 탐지내역은 탐지시간 기준으로 오름차순으로 정렬하여 응답하며, 탐지 건수가 100건이 초과되면 정렬순으로 처음 100건만 응답.
     * - 이상금융거래 탐지 정책룰
     * ex) "동일 사용자의 지속적인 출금이체 요청 발생"
     * 
     * <관련 테이블>
     * 
     * <INPUT>
     * 조회일자, 조회시작시간, 조회종료시간
     * <OUTPUT>
     * 오픈뱅킹API거래고유번호, API거래일시, 오픈뱅킹API응답구분코드, 오픈뱅킹API응답메시지내용, 탐지내역 건수 초과여부, 이상금융거래 탐지 건수
     * LIST
     *   - 탐지 시간, 이상금융거래 탐지 정책룰, 사용자일련번호, 참가기관 표준코드, 계좌번호, 핀테크이용번호, 거래금액
     * 
     * @method retvAbnFncTrDtcHis
     * @method(한글명) 이상금융거래 탐지 내역 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAbnFncTrDtcHis(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("이상금융거래탐지내역조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
        LData iRetvAbnFncTrDtcHisP = input; //i이상금융거래탐지내역조회입력
        LData rRetvAbnFncTrDtcHisP = new LData(); //r이상금융거래탐지내역조회출력
        
  		try {
  			
  			//Validation Check
  			if(StringUtils.isEmpty(iRetvAbnFncTrDtcHisP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
  			}
  			if(StringUtils.isEmpty(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회일자_V8"))) {	
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
  			}
  			if(StringUtils.length(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회일자_V8")) != 8) {
  				throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회일자 자릿수 오류"));
  			}
  			if(StringUtils.isEmpty(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회시작시간_V6"))) {	
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
  			}
  			if(StringUtils.length(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회시작시간_V6")) != 6) {
  				throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회시작시간 자릿수 오류"));
  			}
  			if(StringUtils.isEmpty(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회종료시간_V6"))) {
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
  			}
  			if(StringUtils.length(iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회종료시간_V6")) != 6) {
  				throw new LBizException(ObsErrCode.ERR_9001.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9001.getName(), " - 조회종료시간 자릿수 오류"));
  			}
  			
  			//API 호출
  			LData iRetvAbnFncTrDtcHisAPICall = new LData(); // 이상금융거래탐지내역조회입력
  			LData rRetvAbnFncTrDtcHisAPICall = new LData(); // 이상금융거래탐지내역조회출력
  			
  			OpnbApiCpbc opnbApi = new OpnbApiCpbc();
  			
  			iRetvAbnFncTrDtcHisAPICall.setString("채널세부업무구분코드",		iRetvAbnFncTrDtcHisP.getString("채널세부업무구분코드"));
  			iRetvAbnFncTrDtcHisAPICall.setString("inquiry_date",		iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회일자_V8"));
  			iRetvAbnFncTrDtcHisAPICall.setString("from_time",			iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회시작시간_V6"));
  			iRetvAbnFncTrDtcHisAPICall.setString("to_time",				iRetvAbnFncTrDtcHisP.getString("오픈뱅킹조회종료시간_V6"));
  			
  			rRetvAbnFncTrDtcHisAPICall = opnbApi.retvAbnFncTrDtcHisAPICall(iRetvAbnFncTrDtcHisAPICall);
  			
  			if("A0002".equals(rRetvAbnFncTrDtcHisAPICall.getString("rsp_code"))) {
  				throw new LBizException(rRetvAbnFncTrDtcHisAPICall.getString("bank_rsp_code"), "", StringUtil.mergeStr(rRetvAbnFncTrDtcHisAPICall.getString("rsp_message"), " [", rRetvAbnFncTrDtcHisAPICall.getString("bank_rsp_message"), "]"));
 	  		}
  			
  			if(!"A0000".equals(rRetvAbnFncTrDtcHisAPICall.getString("rsp_code"))) {
  				throw new LBizException(rRetvAbnFncTrDtcHisAPICall.getString("rsp_code"), rRetvAbnFncTrDtcHisAPICall.getString("rsp_message"));
  			}
  			
  			rRetvAbnFncTrDtcHisP.setString("오픈뱅킹API거래고유번호",		rRetvAbnFncTrDtcHisAPICall.getString("api_tran_id"));
  			rRetvAbnFncTrDtcHisP.setString("API거래일시_V17",			rRetvAbnFncTrDtcHisAPICall.getString("api_tran_dtm"));
  			rRetvAbnFncTrDtcHisP.setString("오픈뱅킹API응답구분코드",		rRetvAbnFncTrDtcHisAPICall.getString("rsp_code"));
  			rRetvAbnFncTrDtcHisP.setString("오픈뱅킹API응답메시지내용",	rRetvAbnFncTrDtcHisAPICall.getString("rsp_message"));
  			rRetvAbnFncTrDtcHisP.setString("탐지내역건수초과여부_V1",		rRetvAbnFncTrDtcHisAPICall.getString("cnt_exceed_yn"));
  			rRetvAbnFncTrDtcHisP.setInt("그리드_cnt",					rRetvAbnFncTrDtcHisAPICall.getInt("res_cnt"));
			
      		LMultiData tmpLMultiData = new LMultiData();
      		
      		for(int i=0; i<rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getDataCount(); i++) {
      			
      			LData tmpLData = new LData();
      			tmpLData.setString("탐지시간_V14",				rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("detect_time"));
      			tmpLData.setString("이상금융거래탐지내용",		rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("policy_rule"));
      			tmpLData.setString("오픈뱅킹사용자고유번호",		rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("user_seq_no"));
      			tmpLData.setString("계좌개설은행코드",			rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("bank_code"));
      			tmpLData.setString("고객계좌번호",				rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("account_num"));
      			tmpLData.setString("핀테크이용번호",			rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("fintech_use_num"));
      			tmpLData.setString("입출금금액",				rRetvAbnFncTrDtcHisAPICall.getLMultiData("res_list").getLData(i).getString("tran_amt"));

      			tmpLMultiData.addLData(tmpLData);
      			
      		}
      		
      		rRetvAbnFncTrDtcHisP.set("그리드", tmpLMultiData);
      		
  		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
        
  		if(LLog.debug.isEnabled()) {
  			LLog.debug.println("----------[rRetvAbnFncTrDtcHisP]----------");
  			LLog.debug.println(rRetvAbnFncTrDtcHisP);
  			LLog.debug.println("이상금융거래탐지내역조회 END ☆★☆☆★☆☆★☆" );
  		}
		
        return rRetvAbnFncTrDtcHisP;
    }

}

