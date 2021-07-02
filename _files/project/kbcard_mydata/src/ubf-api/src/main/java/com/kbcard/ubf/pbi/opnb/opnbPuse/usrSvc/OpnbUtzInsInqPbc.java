package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

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
 * opnbUtzInsInqPbc
 * 
 * @logicalname  : 오픈뱅킹이용기관조회Pbc
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

public class OpnbUtzInsInqPbc {

    /**
     * - 이용기관이 특정 계좌의 계좌번호와 예금주 실명번호를 보유하고 있는 경우 해당 계좌의 유효성 및 예금주 성명 확인
     * 
     * 1. 계좌실명조회 요청
     * 1-1. 예금주 실명번호 구분코드
     * - 오픈 뱅킹은 "개인"을 대상으로 하므로 고정 값
     * 1-2. 예금주 실명번호
     * - 오픈뱅킹고객정보 원장의 암호화주민등록번호 값
     * 2. 오픈뱅킹으로부터 전달받은 응답 정보 이용기관거래내역 원장에 적재
     * 3. 오픈뱅킹으로부터 전달받은 계좌 유효성 및 예금주 성명 정보 전달
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본
     * <INPUT>
     * 고객계좌번호, 오픈뱅킹주민등록번호
     * <OUTPUT>
     * 계좌사용여부, 고객명
     * 
     * @serviceID UBF0100740
     * @method retvAccRlnm
     * @method(한글명) 계좌실명 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
	public LData retvAccRlnm(LData input) throws LException {
    	
		if(LLog.debug.isEnabled()) {
			LLog.debug.println("계좌실명조회 START ☆★☆☆★☆☆★☆");
			LLog.debug.println("----------[input]----------");
			LLog.debug.println(input);
		}
		
    	LData iRetvAccRlnmP = input; //i계좌실명조회입력
        LData rRetvAccRlnmP = new LData(); //r계좌실명조회출력
        
        try {
        	
	    	//Validation Check
	    	if(StringUtil.trimNisEmpty(iRetvAccRlnmP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
	 			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
			}
	    	if(StringUtil.trimNisEmpty(iRetvAccRlnmP.getString("계좌개설은행코드"))) {
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
			}
	    	if(StringUtil.trimNisEmpty(iRetvAccRlnmP.getString("고객계좌번호"))) {
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
			}
	    	if(StringUtil.trimNisEmpty(iRetvAccRlnmP.getString("실명번호구분코드_V1"))) {
	    		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
	    	}
	    	
    		//API 호출
      		LData iRetvAccRlnmAPICall = new LData();; // 계좌실명조회입력
      		LData rRetvAccRlnmAPICall = new LData(); // 계좌실명조회출력
            
      		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
      		OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc();

      		iRetvAccRlnmAPICall.setString("채널세부업무구분코드",					iRetvAccRlnmP.getString("채널세부업무구분코드"));
      		iRetvAccRlnmAPICall.setString("bank_tran_id",					opnbCdMgCpbc.crtTrUno(input).getString("거래고유번호"));
      		iRetvAccRlnmAPICall.setString("bank_code_std",					iRetvAccRlnmP.getString("계좌개설은행코드"));
      		iRetvAccRlnmAPICall.setString("account_num",					iRetvAccRlnmP.getString("고객계좌번호"));
      		
      		if(!StringUtil.trimNisEmpty(iRetvAccRlnmP.getString("계좌납입회차번호"))) {
      			iRetvAccRlnmAPICall.setString("account_seq",				iRetvAccRlnmP.getString("계좌납입회차번호"));
      		}
      		
      		if("0".equals(iRetvAccRlnmP.getString("실명번호구분코드_V1"))) {
      			iRetvAccRlnmAPICall.setString("account_holder_info_type",	" ");
      		}
      		else {
      			iRetvAccRlnmAPICall.setString("account_holder_info_type",	iRetvAccRlnmP.getString("실명번호구분코드_V1"));
      		}
      		
      		if(!"N".equals(iRetvAccRlnmP.getString("실명번호구분코드_V1"))) {
      			iRetvAccRlnmAPICall.setString("account_holder_info",		iRetvAccRlnmP.getString("오픈뱅킹예금주실명번호_V13"));
      		}
      		
      		iRetvAccRlnmAPICall.setString("tran_dtime",						DateUtil.getCurrentTime("yyyyMMddHHmmss"));
            
      		rRetvAccRlnmAPICall = opnbApi.retvAccRlnmAPICall(iRetvAccRlnmAPICall);
      		
      		if("A0000".equals(rRetvAccRlnmAPICall.getString("rsp_code"))) {
      			rRetvAccRlnmP.setString("API거래고유번호_V40",					rRetvAccRlnmAPICall.getString("api_tran_id"));
      			rRetvAccRlnmP.setString("API거래일시_V17",						rRetvAccRlnmAPICall.getString("api_tran_dtm"));
      			rRetvAccRlnmP.setString("API응답코드_V5",						rRetvAccRlnmAPICall.getString("rsp_code"));
      			rRetvAccRlnmP.setString("API응답메시지_V300",					rRetvAccRlnmAPICall.getString("rsp_message"));
      			rRetvAccRlnmP.setString("참가기관거래고유번호_V20",				rRetvAccRlnmAPICall.getString("bank_tran_id"));
      			rRetvAccRlnmP.setString("참가기관거래일자_V8",					rRetvAccRlnmAPICall.getString("bank_tran_date"));
      			rRetvAccRlnmP.setString("계좌개설은행코드",						rRetvAccRlnmAPICall.getString("bank_code_tran"));
      			rRetvAccRlnmP.setString("참가기관응답코드_V3",					rRetvAccRlnmAPICall.getString("bank_rsp_code"));
      			rRetvAccRlnmP.setString("참가기관응답메시지_V100",				rRetvAccRlnmAPICall.getString("bank_rsp_message"));
      			rRetvAccRlnmP.setString("오픈뱅킹개설기관코드",					rRetvAccRlnmAPICall.getString("bank_code_std"));
      			rRetvAccRlnmP.setString("개설기관점별코드_V7",					rRetvAccRlnmAPICall.getString("bank_code_sub"));
      			rRetvAccRlnmP.setString("오픈뱅킹개설기관명",						rRetvAccRlnmAPICall.getString("bank_name"));
      			rRetvAccRlnmP.setString("개별저축은행명",						rRetvAccRlnmAPICall.getString("savings_bank_name"));
      			rRetvAccRlnmP.setString("고객계좌번호",							rRetvAccRlnmAPICall.getString("account_num"));
      			rRetvAccRlnmP.setString("계좌납입회차번호",						rRetvAccRlnmAPICall.getString("account_seq"));
      			rRetvAccRlnmP.setString("실명번호구분코드_V1",					rRetvAccRlnmAPICall.getString("account_holder_info_type"));
      			rRetvAccRlnmP.setString("오픈뱅킹예금주실명번호_V13",				rRetvAccRlnmAPICall.getString("account_holder_info"));
      			rRetvAccRlnmP.setString("예금주명",							rRetvAccRlnmAPICall.getString("account_holder_name"));
      			rRetvAccRlnmP.setString("오픈뱅킹계좌종류구분코드",				rRetvAccRlnmAPICall.getString("account_type"));
      		}
      		else {
      			// 참가기관 응답 오류
      			if("A0002".equals(rRetvAccRlnmAPICall.getString("rsp_code"))) {
      				throw new LBizException(rRetvAccRlnmAPICall.getString("bank_rsp_code"), StringUtil.mergeStr(rRetvAccRlnmAPICall.getString("rsp_message"), " [", rRetvAccRlnmAPICall.getString("bank_rsp_message"), "]"));
     	  		}
      			// API 응답 오류
      			throw new LBizException(rRetvAccRlnmAPICall.getString("rsp_code"), rRetvAccRlnmAPICall.getString("rsp_message"));
      		}
      		
        } catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
    	
        if(LLog.debug.isEnabled()) {
        	LLog.debug.println("----------[rRetvAccRlnmP]----------");
        	LLog.debug.println(rRetvAccRlnmP);
        	LLog.debug.println("계좌실명조회 END ☆★☆☆★☆☆★☆" );
        }
		
        return rRetvAccRlnmP;
    }

    /**
     * - 소액해외송금업자의 경우 외화송금을 위하여 이용기관의 수취계좌로 입금한 송금인의 성명과 계좌번호를 확인하여 송금인의 신분을 확인할 의무가 있음
     * 
     * 1. 송금인정보조회요청
     * 2. 오픈뱅킹으로부터 전달받은 응답 정보 오픈뱅킹기관거래내역 원장에 적재
     * 3. 오픈뱅킹으로부터 전달받은 기 등록된 수취계좌에 대하여 특정 기간 동안 입금된 송금인 성명과 계좌내역 번호 전달(소액해외송금업자의 경우만 이용 가능)
     * 4. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 이용기관수취계좌번호, 조회기간
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지, 기관응답코드, 기관응답메시지, 계좌잔액
     * LIST
     * - 거래일시, 거래구분코드, 통장인자내용, 거래금액, 거래후 잔액, 거래점명, 송금인명, 입금은행코드, 입금계좌번호
     * 
     * @serviceID UBF0100741
     * @logicalName 송금인정보 조회
     * @method retvRmtrInf
     * @method(한글명) 송금인정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvRmtrInf(LData input) throws LException {
        LLog.debug.println("OpnbUtzInsInqPbc.retvRmtrInf START ☆★☆☆★☆☆★☆");
		
        LData rRetvRmtrInf = new LData(); //송금인정보조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("개설기관대표코드_V3"))) {
			LLog.debug.println("로그 " + input.getString("개설기관대표코드_V3"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-개설기관대표코드_V3" ));//오픈뱅킹개설기관대표코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("수취계좌번호_V16"))) {
			LLog.debug.println("로그 " + input.getString("수취계좌번호_V16"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-수취계좌번호_V16" ));//수취계좌번호_V16이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("조회시작일자_V8"))) {
			LLog.debug.println("로그 " + input.getString("조회시작일자_V8"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-조회시작일자_V8" ));//조회시작일자_V8이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹조회시작시간_V6"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹조회시작시간_V6"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹조회시작시간_V6" ));//조회시작시각_V6이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("조회종료일자_V8"))) {
			LLog.debug.println("로그 " + input.getString("조회종료일자_V8"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-조회종료일자_V8" ));//조회종료일자_V8이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹조회종료시간_V6"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹조회종료시간_V6"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹조회종료시간_V6" ));//조회종료시각_V6이 존재하지 않습니다.
		}
		 
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		LData rCdMg = new LData();
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
		rCdMg = opnbCdMg.crtTrUno(iCdMg);
		
		// 송금인정보조회 API호출
		LData iRetvRmtrInfAPICall = new LData(); //i송금인정보조회API호출입력
        LData rRetvRmtrInfAPICall = new LData(); //r송금인정보조회API호출결과
        
        LMultiData tmpRmtrInf = new LMultiData(); //결과값 출력 Group
        
		try {

			iRetvRmtrInfAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
			iRetvRmtrInfAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
			iRetvRmtrInfAPICall.setString("bank_code_std"        , input.getString("개설기관대표코드_V3"));//조회하고자 하는 이용기관수취계좌의 개설기관.표준코드 KB국민카드 381
			iRetvRmtrInfAPICall.setString("account_num"          , input.getString("수취계좌번호_V16")); //수취계좌번호
			iRetvRmtrInfAPICall.setString("from_date"            , input.getString("조회시작일자_V8")); //조회시작일자
			iRetvRmtrInfAPICall.setString("from_time"            , input.getString("오픈뱅킹조회시작시간_V6")); //조회시작시각
			iRetvRmtrInfAPICall.setString("to_date"              , input.getString("조회종료일자_V8")); //조회종료일자
			iRetvRmtrInfAPICall.setString("to_time"              , input.getString("오픈뱅킹조회종료시간_V6")); //조회종료시각
			iRetvRmtrInfAPICall.setString("sort_order"           , "D");//정렬순서 - “D”:Descending, “A”:Ascending 
			iRetvRmtrInfAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시

	        LLog.debug.println("--- 송금인정보조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvRmtrInfAPICall);
			
	        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
	        
	        rRetvRmtrInfAPICall = opnbApi.retvRmtrInfAPICall(iRetvRmtrInfAPICall); //송금인정보 조회
			
			//rRetvRmtrInf = rRetvRmtrInfAPICall;
	        //API 호출이후 처리
	        rRetvRmtrInf.setString("API거래고유번호_V40"            , rRetvRmtrInfAPICall.getString("api_tran_id"));              //거래고유번호(API)
	        rRetvRmtrInf.setString("API거래일시_V17"                , rRetvRmtrInfAPICall.getString("api_tran_dtm"));             //거래일시(밀리세컨드)
	        rRetvRmtrInf.setString("API응답코드_V5"                 , rRetvRmtrInfAPICall.getString("rsp_code"));                 //API응답코드
	        rRetvRmtrInf.setString("API응답메시지_V300"             , rRetvRmtrInfAPICall.getString("rsp_message"));              //API응답메시지
	        rRetvRmtrInf.setString("참가기관거래고유번호"           , rRetvRmtrInfAPICall.getString("bank_tran_id"));   		//참가기관거래고유번호  
	        rRetvRmtrInf.setString("참가기관거래일자_V8"            , rRetvRmtrInfAPICall.getString("bank_tran_date"));   		//참가기관거래일자_V8  
	        rRetvRmtrInf.setString("참가기관표준코드"               , rRetvRmtrInfAPICall.getString("bank_code_tran"));   		//참가기관표준코드  
	        rRetvRmtrInf.setString("참가기관응답코드_V3"   , rRetvRmtrInfAPICall.getString("bank_rsp_code"));   		//오픈뱅킹참가기관응답구분코드  
	        rRetvRmtrInf.setString("참가기관응답메시지_V100" , rRetvRmtrInfAPICall.getString("bank_rsp_message"));		//오픈뱅킹참가기관응답메시지내용
	        rRetvRmtrInf.setString("개설기관대표코드_V3"               , rRetvRmtrInfAPICall.getString("bank_code_std"));          		//개설기관대표코드_V3            
	        rRetvRmtrInf.setString("수취계좌번호_V16"               , rRetvRmtrInfAPICall.getString("account_num"));          		//수취계좌번호_V16            
	        rRetvRmtrInf.setString("오픈뱅킹개설기관명"             , rRetvRmtrInfAPICall.getString("bank_name"));          		//오픈뱅킹개설기관명            
	        rRetvRmtrInf.setString("개별저축은행명"                 , rRetvRmtrInfAPICall.getString("savings_bank_name"));            		//개별저축은행명                
	        rRetvRmtrInf.setLong("계좌잔액_N15"                     , rRetvRmtrInfAPICall.getLong("balance_amt"));                  		//계좌잔액_N15                  
	        rRetvRmtrInf.setLong("총조회건수_N7"                    , rRetvRmtrInfAPICall.getLong("total_record_cnt"));                  		//총 조회건수                 
	        rRetvRmtrInf.setLong("현재페이지건수_N8"                , rRetvRmtrInfAPICall.getLong("page_record_cnt"));                  		//현재페이지 레코드건수 한 페이지는 최대 18건 가능                 
	        rRetvRmtrInf.setString("다음페이지존재여부_V1"          , rRetvRmtrInfAPICall.getString("next_page_yn"));                  		//다음페이지 존재여부                  
	        rRetvRmtrInf.setString("직전조회추적정보_V20"           , rRetvRmtrInfAPICall.getString("befor_inquiry_trace_info"));                  		//직전조회 추적정보                 

	        for(int i=0; i < rRetvRmtrInfAPICall.getLMultiData("res_list").getDataCount(); i++) {
	        	LData tmpSelRec = new LData();
	        	LData rAPIData = new LData();
	        	
	        	rAPIData = rRetvRmtrInfAPICall.getLMultiData("res_list").getLData(i);
	        	
	        	tmpSelRec.setString("오픈뱅킹거래일자_V8"        , rAPIData.getString("tran_date")); //거래일자
	        	tmpSelRec.setString("오픈뱅킹거래시간_V6"        , rAPIData.getString("tran_time")); //거래시각
	        	tmpSelRec.setString("거래구분명_V10"           , rAPIData.getString("tran_type")); //거래구분명
	        	tmpSelRec.setString("오픈뱅킹통장인자내용_V40"    , rAPIData.getString("print_content")); //통장인자내용
	        	tmpSelRec.setLong("오픈뱅킹거래금액"             , rAPIData.getLong("tran_amt")); //거래금액
	        	tmpSelRec.setLong("거래후잔액_N13"             , rAPIData.getLong("after_balance_amt")); //거래후잔액
	        	tmpSelRec.setString("거래점명_V40"            , rAPIData.getString("branch_name")); //거래점명
	        	tmpSelRec.setString("송금의뢰인명"            , rAPIData.getString("remitter_name")); //송금인성명
	        	//tmpSelRec.setString("송금의뢰인명"          , CryptoDataUtil.encryptKey(rAPIData.getString("remitter_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //송금인성명
	        	tmpSelRec.setString("입금은행코드"             , rAPIData.getString("remitter_bank_code")); //입금은행코드
	        	tmpSelRec.setString("오픈뱅킹입금계좌번호"       , rAPIData.getString("remitter_account_num")); //오픈뱅킹입금계좌번호
	        	//tmpSelRec.setString("입금계좌번호IF_V48" , CryptoDataUtil.encryptKey(rAPIData.getString("remitter_account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //오픈뱅킹입금계좌번호
	        	
	        	tmpRmtrInf.addLData(tmpSelRec); //출력 Group 추가
	        }
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		rRetvRmtrInf.setInt("그리드_cnt", rRetvRmtrInfAPICall.getLMultiData("res_list").getDataCount()); //그리드count
		rRetvRmtrInf.set("그리드", tmpRmtrInf); //그리드셋
        
		LLog.debug.println("OpnbUtzInsInqPbc.retvRmtrInf END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvRmtrInf);
        
        
        return rRetvRmtrInf;

    }

    /**
     * - 이용기관이 입금이체 요청 전 수취계좌의 입금가능여부 및 수취인성명을 사전에 조회합니다.
     * 
     * 1. 수취조회요청
     * 2. 오픈뱅킹으로부터 전달받은 응답 정보 오픈뱅킹기관거래내역 원장에 적재
     * 3. 오픈뱅킹으로부터 전달받은 수취계좌 정보 조회
     * 4. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 출금계좌인자내역, 입금이체용 암호문구, 수취인성명검증여부
     * - 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도, 입금계좌인자내역
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * - 거래고유번호(참가기관),핀테크이용번호, 참가기관응답코드, 참가기관응답메시지
     * 
     * @serviceID UBF0100742
     * @logicalName 수취조회
     * @method retvRmtrInf
     * @method(한글명) 수쥐조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvRcev(LData input) throws LException {
        LLog.debug.println("OpnbUtzInsInqPbc.retvRcev START ☆★☆☆★☆☆★☆");
		
		LLog.debug.println("수취조회 입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRcevInq = new LData(); //수취조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹계좌계정구분코드"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹계좌계정구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹계좌계정구분코드" ));//오픈뱅킹계좌계정구분코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("집금모계좌번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("집금모계좌번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-집금모계좌번호_V20" ));//집금모계좌번호_V20가 존재하지 않습니다.
		}
			 
        if (StringUtil.trimNisEmpty(input.getString("입금계좌핀테크이용번호"))) {
    		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹입금기관대표코드"))) {
    			LLog.debug.println("로그 " + input.getString("오픈뱅킹입금기관대표코드"));
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹입금기관대표코드" ));//오픈뱅킹입금기관대표코드가 존재하지 않습니다.
    		}
    		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹입금계좌번호"))) {
    			LLog.debug.println("로그 " + input.getString("오픈뱅킹입금계좌번호"));
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹입금계좌번호" ));//오픈뱅킹입금계좌번호가 존재하지 않습니다.
    		}
        }
		
        if (!StringUtil.trimNisEmpty(input.getString("입금계좌핀테크이용번호"))) {
        	if (!StringUtil.trimNisEmpty(input.getString("오픈뱅킹입금기관대표코드")) ||
        		!StringUtil.trimNisEmpty(input.getString("입금계좌번호IF_V48"))) {
    			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입금계좌핀테크번호 입력시에는 입금계좌번호 및 입금계좌기관코드 중복입력은 불가합니다. ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
        	}        	
        }
		
		if(input.getLong("오픈뱅킹거래금액") == 0) {
			LLog.debug.println("로그 " + input.getLong("오픈뱅킹거래금액"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹거래금액" ));//오픈뱅킹거래금액이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("입출금요청고객명_V50"))) {
			LLog.debug.println("로그 " + input.getString("입출금요청고객명_V50"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입출금요청고객명_V50" ));//입출금요청고객명_V50이 존재하지 않습니다.
		}
			 
        if (StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
    		if(StringUtil.trimNisEmpty(input.getString("요청고객계좌기관코드_V3"))) {
    			LLog.debug.println("로그 " + input.getString("요청고객계좌기관코드_V3"));
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객계좌기관코드_V3" ));//요청고객계좌기관코드_V3가 존재하지 않습니다.
    		}
    		if(StringUtil.trimNisEmpty(input.getString("요청고객계좌번호_V48"))) {
    			LLog.debug.println("로그 " + input.getString("요청고객계좌번호_V48"));
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객계좌번호_V48" ));//요청고객계좌번호_V48가 존재하지 않습니다.
    		}
        }

        if (!StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
        	if (!StringUtil.trimNisEmpty(input.getString("요청고객계좌기관코드_V3")) ||
        		!StringUtil.trimNisEmpty(input.getString("요청고객계좌번호_V48"))) {
    			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("요청고객핀테크번호 입력시에는 요청고객계좌번호 및 계좌개설기관코드 중복입력은 불가합니다. ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
        	}
        	
        }

		if(StringUtil.trimNisEmpty(input.getString("요청고객회원번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("요청고객회원번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객회원번호_V20" ));//요청고객회원번호_V20가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("이체용도구분코드_V2"))) {
			LLog.debug.println("로그 " + input.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-이체용도구분코드_V2" ));//이체용도구분코드_V2이 존재하지 않습니다.
		}

		if(input.getString("이체용도구분코드_V2").compareTo("TR") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("ST") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("AU") != 0) {
			LLog.debug.println("로그 " + input.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체용도구분코드_V2 ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
		}
			
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		
		OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
		String sPrcRcev = opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"); //수취조회 거래고유번호
		
		LLog.debug.println("lht 수취조회 거래고유번호 = " + sPrcRcev); 
		
		// 수취조회 API호출
		LData iPrcRcevAPICall = new LData(); //i수취조회API호출입력
        LData rPrcRcevAPICall = new LData(); //r수취조회API호출결과
        
		try {
			iPrcRcevAPICall.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			iPrcRcevAPICall.setString("bank_tran_id"              , sPrcRcev); //참가기관거래고유번호  
			iPrcRcevAPICall.setString("cntr_account_type"         , input.getString("오픈뱅킹계좌계정구분코드")); //오픈뱅킹계좌계정구분코드
			iPrcRcevAPICall.setString("cntr_account_num"          , input.getString("집금모계좌번호_V20")); //집금모계좌번호
			iPrcRcevAPICall.setString("bank_code_std"             , input.getString("오픈뱅킹입금기관대표코드")); //오픈뱅킹입금기관대표코드     
			iPrcRcevAPICall.setString("account_num"               , input.getString("오픈뱅킹입금계좌번호")); //오픈뱅킹입금계좌번호     
			//iPrcRcevAPICall.setString("account_num"               , CryptoDataUtil.decryptKey(input.getString("오픈뱅킹입금계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //오픈뱅킹입금계좌번호     
	        if(!StringUtil.trimNisEmpty(input.getString("회차번호_V3"))) {
	        	iPrcRcevAPICall.setString("account_seq"               , input.getString("회차번호_V3")); //회차번호
	        }
	        if(!StringUtil.trimNisEmpty(input.getString("입금계좌핀테크이용번호"))) {
	        	iPrcRcevAPICall.setString("fintech_use_num"           , input.getString("입금계좌핀테크이용번호")); //입금계좌핀테크이용번호
	        }
			iPrcRcevAPICall.setString("print_content"             , input.getString("입금계좌인자내용")); //입금계좌인자내용      
			iPrcRcevAPICall.setLong("tran_amt"                    , input.getLong("오픈뱅킹거래금액")); //거래금액_N12          
			iPrcRcevAPICall.setString("req_client_name"           , input.getString("입출금요청고객명_V50")); //입출금요청고객명      
			//iPrcRcevAPICall.setString("req_client_name"           , CryptoDataUtil.decryptKey(input.getString("입출금요청고객명_V50"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //입출금요청고객명      
			iPrcRcevAPICall.setString("req_client_bank_code"      , input.getString("요청고객계좌기관코드_V3")); //계좌개설은행코드      
			iPrcRcevAPICall.setString("req_client_account_num"    , input.getString("요청고객계좌번호_V48")); //고객계좌번호_V48      
			//iPrcRcevAPICall.setString("req_client_account_num"    , CryptoDataUtil.decryptKey(input.getString("요청고객계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객계좌번호_V48      
	        if(!StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
	        	iPrcRcevAPICall.setString("req_client_fintech_use_num", input.getString("요청자핀테크이용번호_V24")); //핀테크이용번호
	        }
			iPrcRcevAPICall.setString("req_client_num"            , input.getString("요청고객회원번호_V20")); //요청고객회원번호_V20
			iPrcRcevAPICall.setString("transfer_purpose"          , input.getString("이체용도구분코드_V2")); //이체용도구분코드      
			iPrcRcevAPICall.setString("sub_frnc_name"             , input.getString("하위가맹점명_V100")); //하위가맹점명      
	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점번호_V15"))) {
	        	iPrcRcevAPICall.setString("sub_frnc_num"              , input.getString("하위가맹점번호_V15")); //하위가맹점번호
	        }
	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점사업자등록번호"))) {
	        	iPrcRcevAPICall.setString("sub_frnc_business_num"     , input.getString("하위가맹점사업자등록번호")); //하위가맹점 사업자등록번호
	        }
	        if(!StringUtil.trimNisEmpty(input.getString("CMS번호_V32"))) {
	        	iPrcRcevAPICall.setString("cms_num"                   , input.getString("CMS번호_V32")); //CMS번호_V32
	        }

	        rPrcRcevAPICall = opnbApi.retvRcevAPICall(iPrcRcevAPICall); //수취조회 API호출
			
	        //API 호출이후 처리
	        rRcevInq.setString("API거래고유번호_V40"      , rPrcRcevAPICall.getString("api_tran_id"));              //거래고유번호(API)
	        rRcevInq.setString("API거래일시_V17"          , rPrcRcevAPICall.getString("api_tran_dtm"));             //거래일시(밀리세컨드)
	        rRcevInq.setString("API응답코드_V5"           , rPrcRcevAPICall.getString("rsp_code"));                 //API응답코드
	        rRcevInq.setString("API응답메시지_V300"       , rPrcRcevAPICall.getString("rsp_message"));              //API응답메시지
        	rRcevInq.setString("오픈뱅킹입금기관대표코드" , rPrcRcevAPICall.getString("bank_code_std")); //입금기관대표코드_V3
        	rRcevInq.setString("입금기관지점별코드_V7"    , rPrcRcevAPICall.getString("bank_code_sub")); //입금기관지점별코드_V7
        	rRcevInq.setString("입금기관명_V20"           , rPrcRcevAPICall.getString("bank_name")); //입금기관명_V20
        	rRcevInq.setString("개별저축은행명_V20"       , rPrcRcevAPICall.getString("savings_bank_name")); //입금개별저축은행명
        	rRcevInq.setString("오픈뱅킹입금계좌번호"       , rPrcRcevAPICall.getString("account_num")); //입금계좌번호IF_V48(자체인증)
        	//rRcevInq.setString("입금계좌번호IF_V48"       , CryptoDataUtil.encryptKey(rPrcRcevAPICall.getString("account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //입금계좌번호IF_V48(자체인증)
        	rRcevInq.setString("회차번호_V3"              , rPrcRcevAPICall.getString("account_seq")); //회차번호(자체인증)
        	rRcevInq.setString("입금출력계좌번호_V20"     , rPrcRcevAPICall.getString("account_num_masked")); //입금출력계좌번호_V20
        	rRcevInq.setString("입금계좌인자내용"         , rPrcRcevAPICall.getString("print_content")); //입금계좌인자내용
        	rRcevInq.setString("수취인명_V48"             , rPrcRcevAPICall.getString("account_holder_name")); //수취인명_V48
        	//rRcevInq.setString("수취인명_V48"             , CryptoDataUtil.encryptKey(rPrcRcevAPICall.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //수취인명_V48
        	rRcevInq.setString("참가기관거래고유번호_V20" , rPrcRcevAPICall.getString("bank_tran_id")); //참가기관거래고유번호
        	rRcevInq.setString("참가기관거래일자_V8"      , rPrcRcevAPICall.getString("bank_tran_date")); //참가기관거래일자
        	rRcevInq.setString("참가기관표준코드_V3"      , rPrcRcevAPICall.getString("bank_code_tran")); //참가기관표준코드_V3
        	rRcevInq.setString("참가기관응답코드_V3"      , rPrcRcevAPICall.getString("bank_rsp_code")); //참가기관응답코드
        	rRcevInq.setString("참가기관응답메시지_V100"  , rPrcRcevAPICall.getString("bank_rsp_message")); //참가기관응답메시지
        	if(StringUtil.trimNisEmpty(rPrcRcevAPICall.getString("bank_rsp_message")) &&
        		!StringUtil.trimNisEmpty((rPrcRcevAPICall.getString("bank_rsp_code")))) {
        		rRcevInq.setString("참가기관응답메시지_V100", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드", rPrcRcevAPICall.getString("bank_rsp_code")).getString("통합코드내용"));
        	}
	        rRcevInq.setString("출금기관대표코드_V3"      , rPrcRcevAPICall.getString("wd_bank_code_std"));//출금기관대표코드_V3  
	        rRcevInq.setString("출금기관명_V20"           , rPrcRcevAPICall.getString("wd_bank_name"));//출금기관명_V20            
	        rRcevInq.setString("오픈뱅킹출금계좌번호"         , rPrcRcevAPICall.getString("wd_account_num"));//오픈뱅킹출금계좌번호                
	        //rRcevInq.setString("출금계좌번호_V48"         , CryptoDataUtil.encryptKey(rPrcRcevAPICall.getString("wd_account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));//오픈뱅킹출금계좌번호                
	        if (!StringUtil.isEmpty(rPrcRcevAPICall.getString("tran_amt"))) {
	        	rRcevInq.setLong("오픈뱅킹거래금액"           , rPrcRcevAPICall.getLong("tran_amt")); //오픈뱅킹거래금액
	        } else {
	        	rRcevInq.setLong("오픈뱅킹거래금액"           , 0); //오픈뱅킹거래금액
	        }
        	rRcevInq.setString("CMS번호_V32"              , rPrcRcevAPICall.getString("cms_num")); //CMS번호_V32
	        	
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbUtzInsInqPbc.retvRcev END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRcevInq);
        
        
        return rRcevInq;

    }

}

