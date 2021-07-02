package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import org.apache.commons.lang.StringUtils;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCdMgCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.core.exception.LTooManyRowException;
import devonenterprise.ext.persistent.page.PageConstants;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

/** 
 * opnbAccInqPbc
 * 
 * @logicalname  : 오픈뱅킹계좌조회Pbc
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

public class OpnbAccInqPbc {

    /**
     * - 사용자 계좌의 잔액 정보 조회
     * 
     * 1. 실 계좌번호로 계좌의 잔액 정보 조회 요청(자체인증 이용기관만 이용가능)
     * 2. 오픈뱅킹으로부터 전달받은 응답 정보 오픈뱅킹기관거래내역 원장에 적재
     * 3. 오픈뱅킹으로부터 전달받은 잔액 정보 및 계좌 종류, 상품명 함께 전달
     * 4. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 개설기관코드, 계좌번호
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지, 기관응답코드, 기관응답메시지, 개설기관명, 개설저축은행명, 계좌잔액, 출금가능금액, 계좌종류구분코드, 계좌상품명, 계좌개설일, 만기일, 최종거래일자
     * 
     * @serviceID UBF0100640
     * @logicalName 계좌잔액조회
     * @method retvAcnoPrBl
     * @method(한글명) 계좌번호 별 잔액 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAcnoPrBl(LData input) throws LException {
        LLog.debug.println("OpnbAccInqPbc.retvAcnoPrBl START ☆★☆☆★☆☆★☆");
			
        LData rRetvBlByAcnoP = new LData(); //계좌잔액조회 결과값 리턴
        
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("계좌개설은행코드"))) {
			LLog.debug.println("로그 " + input.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("고객계좌번호"))) {
			LLog.debug.println("로그 " + input.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
		}
			 
		 
		// 계좌기본조회 Ebc호출 
		LData iRetvAccInf = new LData();
		LData rRetvAccInf = new LData();
		
		try {
			iRetvAccInf.setString("계좌개설은행코드", input.getString("계좌개설은행코드"));
			iRetvAccInf.setString("고객계좌번호"    , input.getString("고객계좌번호"));
			rRetvAccInf = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectAcnoPrBl", iRetvAccInf); //
		} catch (LTooManyRowException e) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr("고객계좌정보 조회 ", ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		} catch (LNotFoundException nfe) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr("고객계좌정보 조회 ", ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		}
		
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		LData rCdMg = new LData();
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
		rCdMg = opnbCdMg.crtTrUno(iCdMg);
		
		// 계좌잔액조회 API호출
		LData iRetvBlByAcnoAPICall = new LData(); //i계좌잔액조회API호출입력
        LData rRetvBlByAcnoAPICall = new LData(); //r계좌잔액조회API호출결과
        
		try {

			iRetvBlByAcnoAPICall.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
	        iRetvBlByAcnoAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
	        iRetvBlByAcnoAPICall.setString("bank_code_std"        , rRetvAccInf.getString("계좌개설은행코드")); //계좌개설은행코드
	        iRetvBlByAcnoAPICall.setString("account_num"          , rRetvAccInf.getString("고객계좌번호")); //고객계좌번호
	        //iRetvBlByAcnoAPICall.setString("account_num"          , CryptoDataUtil.decryptKey(rRetvAccInf.getString("고객계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객계좌번호
	        iRetvBlByAcnoAPICall.setString("account_seq"          , rRetvAccInf.getString("계좌납입회차")); //계좌납입회차
	        iRetvBlByAcnoAPICall.setString("user_seq_no"          , rRetvAccInf.getString("오픈뱅킹사용자고유번호")); //핀테크이용번호
	        iRetvBlByAcnoAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시

	        iRetvBlByAcnoAPICall.setString("fintech_use_num"      , rRetvAccInf.getString("핀테크이용번호")); //핀테크이용번호
	        
			LLog.debug.println("--- 계좌잔액조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvBlByAcnoAPICall);
			
	        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
	        
	        rRetvBlByAcnoAPICall = opnbApi.retvBlByAcnoAPICall(iRetvBlByAcnoAPICall); //계좌번호로 잔액조회
	        //rRetvBlByAcnoAPICall = opnbApi.retvBlByFntcUtzNoAPICall(iRetvBlByAcnoAPICall); //핀테크번호로 잔액조회
			
			//rRetvBlByAcnoP = rRetvBlByAcnoAPICall;
	        //API 호출이후 처리
	        rRetvBlByAcnoP.setString("API거래고유번호_V40"           , rRetvBlByAcnoAPICall.getString("api_tran_id"));              //거래고유번호(API)
	        rRetvBlByAcnoP.setString("API거래일시_V17"              , rRetvBlByAcnoAPICall.getString("api_tran_dtm"));             //거래일시(밀리세컨드)
	        rRetvBlByAcnoP.setString("API응답코드_V5"               , rRetvBlByAcnoAPICall.getString("rsp_code"));                 //API응답코드
	        rRetvBlByAcnoP.setString("API응답메시지_V300"           , rRetvBlByAcnoAPICall.getString("rsp_message"));              //API응답메시지
	        rRetvBlByAcnoP.setString("참가기관거래고유번호_V20"        , rRetvBlByAcnoAPICall.getString("bank_tran_id"));             //참가기관거래고유번호
	        rRetvBlByAcnoP.setString("참가기관거래일자_V8"            , rRetvBlByAcnoAPICall.getString("bank_tran_date"));           //거래일자
	        rRetvBlByAcnoP.setString("참가기관표준코드_V3"            , rRetvBlByAcnoAPICall.getString("bank_code_tran"));           //응답코드를 부여한 참가기관.표준코드
	        rRetvBlByAcnoP.setString("참가기관응답코드_V3"            , rRetvBlByAcnoAPICall.getString("bank_rsp_code"));   		 //오픈뱅킹참가기관응답구분코드  
	        rRetvBlByAcnoP.setString("참가기관응답메시지_V100"         , rRetvBlByAcnoAPICall.getString("bank_rsp_message"));		 //오픈뱅킹참가기관응답메시지내용
	        rRetvBlByAcnoP.setString("오픈뱅킹개설기관명"              , rRetvBlByAcnoAPICall.getString("bank_name"));          		 //오픈뱅킹개설기관명            
	        rRetvBlByAcnoP.setString("개별저축은행명"                 , rRetvBlByAcnoAPICall.getString("savings_bank_name"));        //개별저축은행명                
	        rRetvBlByAcnoP.setLong("계좌잔액_N15"                   , rRetvBlByAcnoAPICall.getLong("balance_amt"));                //계좌잔액_N15                  
	        rRetvBlByAcnoP.setLong("출금가능금액"                    , rRetvBlByAcnoAPICall.getLong("available_amt"));              //출금가능금액                  
	        rRetvBlByAcnoP.setString("오픈뱅킹계좌종류구분코드"          , rRetvBlByAcnoAPICall.getString("account_type"));         	 //오픈뱅킹계좌종류구분코드      
	        rRetvBlByAcnoP.setString("오픈뱅킹계좌상품명"              , rRetvBlByAcnoAPICall.getString("product_name"));           	 //오픈뱅킹계좌상품명            
	        rRetvBlByAcnoP.setString("계좌개설년월일"                 , rRetvBlByAcnoAPICall.getString("account_issue_date"));       //계좌개설년월일                
	        rRetvBlByAcnoP.setString("계좌만기년월일_V8"              , rRetvBlByAcnoAPICall.getString("maturity_date"));            //계좌만기년월일_V8             
	        rRetvBlByAcnoP.setString("최종거래년월일"                 , rRetvBlByAcnoAPICall.getString("last_tran_date"));           //최종거래년월일                

	        
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbAccInqPbc.retvAcnoPrBl END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvBlByAcnoP);
        
        
        return rRetvBlByAcnoP;

    }

    /**
     * - 사용자 계좌의 잔액 정보 조회
     * 
     * 1. 요청고객의 등록계좌의 모든 잔액을 조회.
     * 2. 등록 계좌번호로 계좌의 잔액 정보 조회 요청(자체인증 이용기관만 이용가능)
     * 3. 오픈뱅킹으로부터 전달받은 응답 정보 이용기관거래내역 원장에 적재
     * 4. 오픈뱅킹으로부터 전달받은 잔액 정보 및 계좌 종류, 상품명 함께 전달
     * 5. 응답값 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI번호
     * 
     * <OUTPUT>
     * LIST
     * - 개설기관명, 개설저축은행명, 계좌잔액, 출금가능금액, 계좌종류구분코드, 계좌상품명, 계좌개설일, 만기일, 최종거래일자
     * 
     * @serviceID UBF0100641
     * @logicalName 고객보유계좌잔액목록조회
     * @method retvAcnoPrWhlBl
     * @method(한글명) 계좌번호 별 전체잔액 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
//    public LData retvAcnoPrWhlBl(LData input) throws LException {
//        LLog.debug.println("OpnbAccInqPbc.retvAcnoPrWhlBl START ☆★☆☆★☆☆★☆");
//		
//        LData rAcnoPrWhlBl = new LData(); //고객보유계좌잔액조회 결과값 리턴
//        
//		//Validation Check
//		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
//			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
//			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
//		}
//		
//		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
//			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
//			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
//		}
//			 
//		// 계좌기본조회 Ebc호출 
//		LData iRetvAccInf = new LData();
//		LMultiData rRetvAccInf = new LMultiData();
//		
//	    LLog.debug.println("입력값 출력 ----"); 
//		LLog.debug.println(input);
//		
//		iRetvAccInf.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호")); //사용자별 보유계좌 조회
//		iRetvAccInf.setString(PageConstants.PGE_SIZE	, "20");
//
//		if (StringUtil.trimNisEmpty(input.getString("다음조회키_V100"))) {
//			iRetvAccInf.setString(PageConstants.NEXT_INQ_KY, "SQ_계좌등록일시=0|NK_계좌등록일시=");
//		} else {
//			iRetvAccInf.setString(PageConstants.NEXT_INQ_KY,StringUtil.mergeStr("SQ_계좌등록일시=0|NK_계좌등록일시=", input.getString("다음조회키_V100")));
//		}
//		
//		rRetvAccInf = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "retvLstAcnoPrWhlBl", iRetvAccInf); //
//		
//		if (DataConvertUtil.equals(LDataUtil.getNextYn(), "Y")) {
//			rAcnoPrWhlBl.setString("다음페이지존재여부_V1"	, LDataUtil.getNextYn());
//			//LLog.debug.println("lht next key 출력 ----");
//			//LLog.debug.println(LDataUtil.getNextKey());
//			//LLog.debug.println(StringUtil.trim(StringUtil.substring(LDataUtil.getNextKey(), 22)));
//			rAcnoPrWhlBl.setString("다음조회키_V100"			, StringUtil.trim(StringUtil.substring(LDataUtil.getNextKey(), 22)));
//		} else {
//			rAcnoPrWhlBl.setString("다음페이지존재여부_V1"	, "N");
//			rAcnoPrWhlBl.setString("다음조회키_V100"			, " ");
//		}
//
//		// 계좌잔액조회 API호출
//		LData iRetvBlByAcnoAPICall = new LData(); //i계좌잔액조회API호출입력
//        LData rRetvBlByAcnoAPICall = new LData(); //r계좌잔액조회API호출결과
//		LMultiData 	tmUtzFeeEca 	= new LMultiData(); //tm계좌잔액조회(output)
//        
//		if(rRetvAccInf.getDataCount() == 0) {
//			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
//			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("보유 계좌정보가 없습니다.", ObsErrCode.ERR_7011.getName()));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
//		}
//			 
//        for (int i=0; i < rRetvAccInf.getDataCount(); i++) {
//    		try {
//
//    			// 전문거래고유번호 생성 Cpbc 호출
//    			LData iCdMg = new LData();
//    			LData rCdMg = new LData();
//    			
//    			OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
//    			
//    			rCdMg = opnbCdMg.crtTrUno(iCdMg);
//    			
//    	        LData rRetvBlByAcnoP = new LData(); //계좌잔액조회 결과값 리턴
//    	        
//    			iRetvBlByAcnoAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
//    	        iRetvBlByAcnoAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
//    	        iRetvBlByAcnoAPICall.setString("bank_code_std"        , rRetvAccInf.getLData(i).getString("계좌개설은행코드")); //계좌개설은행코드
//    	        iRetvBlByAcnoAPICall.setString("account_num"          , rRetvAccInf.getLData(i).getString("고객계좌번호")); //고객계좌번호
//    	        //iRetvBlByAcnoAPICall.setString("account_num"          , CryptoDataUtil.decryptKey(rRetvAccInf.getLData(i).getString("고객계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객계좌번호
//    	        iRetvBlByAcnoAPICall.setString("account_seq"          , rRetvAccInf.getLData(i).getString("계좌납입회차")); //계좌납입회차
//    	        iRetvBlByAcnoAPICall.setString("user_seq_no"          , rRetvAccInf.getLData(i).getString("오픈뱅킹사용자고유번호")); //오픈뱅킹사용자고유번호
//    	        iRetvBlByAcnoAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시
//
//    	        iRetvBlByAcnoAPICall.setString("fintech_use_num"      , rRetvAccInf.getLData(i).getString("핀테크이용번호")); //핀테크이용번호
//    	        
//    		    LLog.debug.println("--- 계좌잔액조회 API호출 입력값 ----"); 
//    			LLog.debug.println(iRetvBlByAcnoAPICall);
//    			
//    	        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
//    	        
//    	        rRetvBlByAcnoAPICall = opnbApi.retvBlByAcnoAPICall(iRetvBlByAcnoAPICall); //계좌번호로 잔액조회
//    	        //rRetvBlByAcnoAPICall = opnbApi.retvBlByFntcUtzNoAPICall(iRetvBlByAcnoAPICall); //핀테크번호로 잔액조회
//    			
//    			//rRetvBlByAcnoP = rRetvBlByAcnoAPICall;
//    	        //API 호출이후 처리
//    	        rRetvBlByAcnoP.setString("API응답코드_V5"                , rRetvBlByAcnoAPICall.getString("rsp_code"));        		//API응답코드_V5       
//    	        rRetvBlByAcnoP.setString("API응답메시지_V300"            , rRetvBlByAcnoAPICall.getString("rsp_message"));         		//API응답메시지_V300        
//    	        rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답구분코드"  , rRetvBlByAcnoAPICall.getString("bank_rsp_code"));   		//오픈뱅킹참가기관응답구분코드  
//    	        rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답메시지내용", rRetvBlByAcnoAPICall.getString("bank_rsp_message"));		//오픈뱅킹참가기관응답메시지내용
//            	if(StringUtil.trimNisEmpty(rRetvBlByAcnoAPICall.getString("bank_rsp_message")) &&
//                	!StringUtil.trimNisEmpty(rRetvBlByAcnoAPICall.getString("bank_rsp_code"))) {
//            		rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답메시지내용", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드",rRetvBlByAcnoAPICall.getString("bank_rsp_code")).getString("통합코드내용"));
//                }
//    	        rRetvBlByAcnoP.setString("계좌개설은행코드"              , rRetvAccInf.getLData(i).getString("계좌개설은행코드"));          		//계좌개설은행코드            
//    	        rRetvBlByAcnoP.setString("고객계좌번호"                , rRetvAccInf.getLData(i).getString("고객계좌번호"));          		//고객계좌번호            
//    	        rRetvBlByAcnoP.setString("오픈뱅킹개설기관명"            , rRetvBlByAcnoAPICall.getString("bank_name"));          		//오픈뱅킹개설기관명            
//    	        rRetvBlByAcnoP.setString("개별저축은행명"                , rRetvBlByAcnoAPICall.getString("savings_bank_name"));            		//개별저축은행명                
//    	        rRetvBlByAcnoP.setLong("계좌잔액_N15"                    , rRetvBlByAcnoAPICall.getLong("balance_amt"));                  		//계좌잔액_N15                  
//    	        rRetvBlByAcnoP.setLong("출금가능금액"                    , rRetvBlByAcnoAPICall.getLong("available_amt"));                		//출금가능금액                  
//    	        rRetvBlByAcnoP.setString("오픈뱅킹계좌종류구분코드"      , rRetvBlByAcnoAPICall.getString("account_type"));         		//오픈뱅킹계좌종류구분코드      
//    	        rRetvBlByAcnoP.setString("오픈뱅킹계좌상품명"            , rRetvBlByAcnoAPICall.getString("product_name"));           		//오픈뱅킹계좌상품명            
//    	        rRetvBlByAcnoP.setString("계좌개설년월일"                , rRetvBlByAcnoAPICall.getString("account_issue_date"));              		//계좌개설년월일                
//    	        rRetvBlByAcnoP.setString("계좌만기년월일_V8"             , rRetvBlByAcnoAPICall.getString("maturity_date"));             		//계좌만기년월일_V8             
//    	        rRetvBlByAcnoP.setString("최종거래년월일"                , rRetvBlByAcnoAPICall.getString("last_tran_date"));               		//최종거래년월일                
//
//    	        tmUtzFeeEca.addLData(rRetvBlByAcnoP);
//    	        
//    		} catch ( LException e) {
//    			e.printStackTrace(LLog.err); 
//    		}
//
//        }
//        
//        LData iRetvCnt = new LData(); //보유계좌건수조회 입력
//        LData rRetvCnt = new LData(); //보유계좌건수조회 입력
//        
//        iRetvCnt.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호")); //사용자별 보유계좌 조회
//        
//        rRetvCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "retvAcnoCntPrWhlBl", iRetvCnt); //
//        
//        rAcnoPrWhlBl.setLong("보유계좌수", rRetvCnt.getLong("계좌건수_N3")); //보유계좌수
//        rAcnoPrWhlBl.setLong("그리드_cnt", rRetvAccInf.getDataCount()); //그리드count
//        rAcnoPrWhlBl.set("그리드", tmUtzFeeEca); //그리드셋
//        
//		LLog.debug.println("OpnbAccInqPbc.retvAcnoPrWhlBl END ☆★☆☆★☆☆★☆");
//		LLog.debug.println(rAcnoPrWhlBl);
//        
//        
//        return rAcnoPrWhlBl; 
//
//    }
    
    public LData retvAcnoPrWhlBl(LData input) throws LException {
        LLog.debug.println("OpnbAccInqPbc.retvAcnoPrWhlBl START ☆★☆☆★☆☆★☆");
		
        LData rAcnoPrWhlBl = new LData(); //고객보유계좌잔액조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}
			 
		// 계좌기본조회 Ebc호출 
		LData iRetvAccInf = new LData();
		LMultiData rRetvAccInf = new LMultiData();
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
		iRetvAccInf.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호")); //사용자별 보유계좌 조회
		iRetvAccInf.setString(PageConstants.PGE_SIZE	, "20");

		if (StringUtil.trimNisEmpty(input.getString("다음조회키_V100"))) {
			iRetvAccInf.setString(PageConstants.NEXT_INQ_KY, "SQ_계좌등록일시=0|NK_계좌등록일시=");
		} else {
			iRetvAccInf.setString(PageConstants.NEXT_INQ_KY,StringUtil.mergeStr("SQ_계좌등록일시=0|NK_계좌등록일시=", input.getString("다음조회키_V100")));
		}
		
		rRetvAccInf = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "retvLstAcnoPrWhlBl", iRetvAccInf); //
		
		if (DataConvertUtil.equals(LDataUtil.getNextYn(), "Y")) {
			rAcnoPrWhlBl.setString("다음페이지존재여부_V1"	, LDataUtil.getNextYn());
			//LLog.debug.println("lht next key 출력 ----");
			//LLog.debug.println(LDataUtil.getNextKey());
			//LLog.debug.println(StringUtil.trim(StringUtil.substring(LDataUtil.getNextKey(), 22)));
			rAcnoPrWhlBl.setString("다음조회키_V100"			, StringUtil.trim(StringUtil.substring(LDataUtil.getNextKey(), 22)));
		} else {
			rAcnoPrWhlBl.setString("다음페이지존재여부_V1"	, "N");
			rAcnoPrWhlBl.setString("다음조회키_V100"			, " ");
		}
        
        LMultiData 	tmpInList 	= new LMultiData(); //tm계좌잔액조회(output)
        LMultiData 	tmpOutList 	= new LMultiData(); //tm계좌잔액조회(output)
		LMultiData 	tmUtzFeeEca 	= new LMultiData(); //tm계좌잔액조회(output)
        
		if(rRetvAccInf.getDataCount() == 0) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("보유 계좌정보가 없습니다.", ObsErrCode.ERR_7011.getName()));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}

        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
        for (int i=0; i < rRetvAccInf.getDataCount(); i++) {
    		try {

    			// 전문거래고유번호 생성 Cpbc 호출
    			LData iCdMg = new LData();
    			LData rCdMg = new LData();
    			
    			rCdMg = opnbCdMg.crtTrUno(iCdMg);
    			
    			LData iRetvBlByAcnoAPICall = new LData(); //i계좌잔액조회API호출입력
    	        
    	        
    			iRetvBlByAcnoAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
    	        iRetvBlByAcnoAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
    	        iRetvBlByAcnoAPICall.setString("bank_code_std"        , rRetvAccInf.getLData(i).getString("계좌개설은행코드")); //계좌개설은행코드
    	        iRetvBlByAcnoAPICall.setString("account_num"          , rRetvAccInf.getLData(i).getString("고객계좌번호")); //고객계좌번호
    	        //iRetvBlByAcnoAPICall.setString("account_num"          , CryptoDataUtil.decryptKey(rRetvAccInf.getLData(i).getString("고객계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객계좌번호
    	        iRetvBlByAcnoAPICall.setString("account_seq"          , rRetvAccInf.getLData(i).getString("계좌납입회차")); //계좌납입회차
    	        iRetvBlByAcnoAPICall.setString("user_seq_no"          , rRetvAccInf.getLData(i).getString("오픈뱅킹사용자고유번호")); //오픈뱅킹사용자고유번호
    	        iRetvBlByAcnoAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시

    	        iRetvBlByAcnoAPICall.setString("fintech_use_num"      , rRetvAccInf.getLData(i).getString("핀테크이용번호")); //핀테크이용번호
    	        
    		    LLog.debug.println("--- 계좌잔액조회 API호출 입력값 ----"); 
    			LLog.debug.println(iRetvBlByAcnoAPICall);
    			
    			tmpInList.addLData(iRetvBlByAcnoAPICall);
    		} catch ( LException e) {
    			e.printStackTrace(LLog.err); 
    		}
        }
        
        tmpOutList = opnbApi.retvBlByAcnoAPICall(tmpInList); //계좌번호로 잔액조회
        
        for (int i=0; i < tmpOutList.getDataCount(); i++) {
    		// 계좌잔액조회 API호출
            LData rRetvBlByAcnoAPICall = tmpOutList.getLData(i); //r계좌잔액조회API호출결과
            LData rRetvBlByAcnoP = new LData(); //계좌잔액조회 결과값 리턴
            
			//rRetvBlByAcnoP = rRetvBlByAcnoAPICall;
	        //API 호출이후 처리
	        rRetvBlByAcnoP.setString("API응답코드_V5"                , rRetvBlByAcnoAPICall.getString("rsp_code"));        		//API응답코드_V5       
	        rRetvBlByAcnoP.setString("API응답메시지_V300"            , rRetvBlByAcnoAPICall.getString("rsp_message"));         		//API응답메시지_V300        
	        rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답구분코드"  , rRetvBlByAcnoAPICall.getString("bank_rsp_code"));   		//오픈뱅킹참가기관응답구분코드  
	        rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답메시지내용", rRetvBlByAcnoAPICall.getString("bank_rsp_message"));		//오픈뱅킹참가기관응답메시지내용
        	if(StringUtil.trimNisEmpty(rRetvBlByAcnoAPICall.getString("bank_rsp_message")) &&
            	!StringUtil.trimNisEmpty(rRetvBlByAcnoAPICall.getString("bank_rsp_code"))) {
        		rRetvBlByAcnoP.setString("오픈뱅킹참가기관응답메시지내용", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드",rRetvBlByAcnoAPICall.getString("bank_rsp_code")).getString("통합코드내용"));
            }
	        rRetvBlByAcnoP.setString("계좌개설은행코드"              , rRetvAccInf.getLData(i).getString("계좌개설은행코드"));          		//계좌개설은행코드            
	        rRetvBlByAcnoP.setString("고객계좌번호"                 , rRetvAccInf.getLData(i).getString("고객계좌번호"));          		//고객계좌번호            
	        rRetvBlByAcnoP.setString("오픈뱅킹개설기관명"            , rRetvBlByAcnoAPICall.getString("bank_name"));          		//오픈뱅킹개설기관명            
	        rRetvBlByAcnoP.setString("개별저축은행명"                , rRetvBlByAcnoAPICall.getString("savings_bank_name"));            		//개별저축은행명
	        if(LDataUtil.isKeys(rRetvBlByAcnoAPICall, "balance_amt")) {
	        	rRetvBlByAcnoP.setLong("계좌잔액_N15"                    , (long) StringUtil.nvl(rRetvBlByAcnoAPICall.getLong("balance_amt"), 0L));                  		//계좌잔액_N15	
	        }
	        if(LDataUtil.isKeys(rRetvBlByAcnoAPICall, "available_amt")) {
	        	rRetvBlByAcnoP.setLong("출금가능금액"                    , (long) StringUtil.nvl(rRetvBlByAcnoAPICall.getLong("available_amt"), 0L));                		//출금가능금액
	        }
	        rRetvBlByAcnoP.setString("오픈뱅킹계좌종류구분코드"      , rRetvBlByAcnoAPICall.getString("account_type"));         		//오픈뱅킹계좌종류구분코드      
	        rRetvBlByAcnoP.setString("오픈뱅킹계좌상품명"            , rRetvBlByAcnoAPICall.getString("product_name"));           		//오픈뱅킹계좌상품명            
	        rRetvBlByAcnoP.setString("계좌개설년월일"                , rRetvBlByAcnoAPICall.getString("account_issue_date"));              		//계좌개설년월일                
	        rRetvBlByAcnoP.setString("계좌만기년월일_V8"             , rRetvBlByAcnoAPICall.getString("maturity_date"));             		//계좌만기년월일_V8             
	        rRetvBlByAcnoP.setString("최종거래년월일"                , rRetvBlByAcnoAPICall.getString("last_tran_date"));               		//최종거래년월일                

	        tmUtzFeeEca.addLData(rRetvBlByAcnoP);
        }
        
        LData iRetvCnt = new LData(); //보유계좌건수조회 입력
        LData rRetvCnt = new LData(); //보유계좌건수조회 입력
        
        iRetvCnt.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호")); //사용자별 보유계좌 조회
        
        rRetvCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "retvAcnoCntPrWhlBl", iRetvCnt); //
        
        rAcnoPrWhlBl.setLong("보유계좌수", rRetvCnt.getLong("계좌건수_N3")); //보유계좌수
        rAcnoPrWhlBl.setLong("그리드_cnt", rRetvAccInf.getDataCount()); //그리드count
        rAcnoPrWhlBl.set("그리드", tmUtzFeeEca); //그리드셋
        
		LLog.debug.println("OpnbAccInqPbc.retvAcnoPrWhlBl END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rAcnoPrWhlBl);
        
        
        return rAcnoPrWhlBl; 

    }

    /**
     * - 사용자가 등록한 계좌의 거래내역 조회
     * - INPUT에 핀테크 이용번호가 있으면 핀테크 이용번호로 조회
     * - INPUT에 핀테크 이용번호가 없으면 계좌번호로 조회
     * 
     * 1. 금결원 API 호출
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹이체내역
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 입출금금융기관코드, 고객계좌번호, 조회시작일자, 조회종료일자
     * <OUTPUT>
     * LIST
     *   - 입출금구분코드, 입출금금액, 입출금계좌인자내용, 입출금요청고객명, 입출금거래일시
     * 
     * @serviceID UBF2030403
     * @method retvTrHis
     * @method(한글명) 거래 내역 조회
     * @param LData
     * @return LData
     * @throws LException
     */ 
    public LData retvTrHis(LData input) throws LException {
        
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("거래내역조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
        LData iRetvTrHisP = input; //i거래내역조회입력
        LData rRetvTrHisP = new LData(); //r거래내역조회출력
        
    	try {
    		
    		//Validation Check
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("거래내역조회구분코드_V1"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("조회기준코드_V1"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		
    		if(StringUtils.equals(iRetvTrHisP.getString("조회기준코드_V1"), "T")) {			
    			if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("오픈뱅킹조회시작시간_V6"))) {			
    				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    			}
    			if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("오픈뱅킹조회종료시간_V6"))) {			
    				throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    			}
    		}
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("거래내역조회시작일자_V8"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("거래내역조회종료일자_V8"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("정렬순서_V1"))) {			
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
			
    		LData iAPICall = new LData(); // API입력
      		LData rAPICall = new LData(); // API출력
        	
        	OpnbApiCpbc opnbApi = new OpnbApiCpbc();
        	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
        	
        	iAPICall.setString("채널세부업무구분코드",			iRetvTrHisP.getString("채널세부업무구분코드"));
        	iAPICall.setString("bank_tran_id",				opnbCdMg.crtTrUno(input).getString("거래고유번호"));
        	iAPICall.setString("inquiry_type",				iRetvTrHisP.getString("거래내역조회구분코드_V1"));
        	iAPICall.setString("inquiry_base",				iRetvTrHisP.getString("조회기준코드_V1"));
        	iAPICall.setString("from_date",					iRetvTrHisP.getString("거래내역조회시작일자_V8"));
        	iAPICall.setString("to_date",					iRetvTrHisP.getString("거래내역조회종료일자_V8"));
        	iAPICall.setString("sort_order",				iRetvTrHisP.getString("정렬순서_V1"));
        	iAPICall.setString("tran_dtime",				DateUtil.getCurrentTime("yyyyMMddHHmmss"));
        	
        	if(!StringUtil.trimNisEmpty(iRetvTrHisP.getString("직전조회추적정보_V20"))) {
        		iAPICall.setString("befor_inquiry_trace_info",	iRetvTrHisP.getString("직전조회추적정보_V20"));
        	}
        	
        	if("T".equals(iRetvTrHisP.getString("조회기준코드_V1"))){
        		iAPICall.setString("from_time",					iRetvTrHisP.getString("오픈뱅킹조회시작시간_V6"));
        		iAPICall.setString("to_time",					iRetvTrHisP.getString("오픈뱅킹조회종료시간_V6"));
        	}
        	
        	if(!StringUtil.trimNisEmpty(iRetvTrHisP.getString("핀테크이용번호"))) {
        		iAPICall.setString("fintech_use_num", iRetvTrHisP.getString("핀테크이용번호"));
        		rAPICall = opnbApi.retvTnhsByFntcUtzNoAPICall(iAPICall);
        	}
        	else {
        		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("계좌개설은행코드"))) {			
        			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        		}
        		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("고객계좌번호"))) {			
        			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        		}
        		if(StringUtil.trimNisEmpty(iRetvTrHisP.getString("오픈뱅킹사용자고유번호"))) {			
        			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        		}
        		iAPICall.setString("bank_code_std",		iRetvTrHisP.getString("계좌개설은행코드"));
        		iAPICall.setString("account_num",		iRetvTrHisP.getString("고객계좌번호"));
        		if(!StringUtil.trimNisEmpty(iRetvTrHisP.getString("계좌납입회차번호"))) {
        			iAPICall.setString("account_seq",		iRetvTrHisP.getString("계좌납입회차번호"));
        		}
        		iAPICall.setString("user_seq_no",		iRetvTrHisP.getString("오픈뱅킹사용자고유번호"));
        		rAPICall = opnbApi.retvTnhsByAcnoAPICall(iAPICall);
        	}
    		
        	if("A0002".equals(rAPICall.getString("rsp_code"))) {
 	  			throw new LBizException(rAPICall.getString("bank_rsp_code"), StringUtil.mergeStr(rAPICall.getString("rsp_message"), " [", rAPICall.getString("bank_rsp_message"), "]"));
 	  		}
        	
        	if(!"A0000".equals(rAPICall.getString("rsp_code")) && !"A0018".equals(rAPICall.getString("rsp_code"))) {
        		throw new LBizException(rAPICall.getString("rsp_code"), rAPICall.getString("rsp_message"));
        	}
        	
        	rRetvTrHisP.setString("API거래고유번호_V40",		rAPICall.getString("api_tran_id"));
			rRetvTrHisP.setString("API거래일시_V17",			rAPICall.getString("api_tran_dtm"));
			rRetvTrHisP.setString("API응답코드_V5",			rAPICall.getString("rsp_code"));
			rRetvTrHisP.setString("API응답메시지_V300",		rAPICall.getString("rsp_message"));
			rRetvTrHisP.setString("참가기관거래고유번호_V20",		rAPICall.getString("bank_tran_id"));
			rRetvTrHisP.setString("참가기관거래일자_V8",			rAPICall.getString("bank_tran_date"));
			rRetvTrHisP.setString("계좌개설은행코드",			rAPICall.getString("bank_code_tran"));
			rRetvTrHisP.setString("참가기관응답코드_V3",			rAPICall.getString("bank_rsp_code"));
			rRetvTrHisP.setString("참가기관응답메시지_V100",		rAPICall.getString("bank_rsp_message"));
    		rRetvTrHisP.setString("오픈뱅킹개설기관명",			rAPICall.getString("bank_name"));
    		rRetvTrHisP.setString("개별저축은행명",				rAPICall.getString("savings_bank_name"));
    		rRetvTrHisP.setString("핀테크이용번호",				rAPICall.getString("fintech_use_num"));
    		rRetvTrHisP.setString("고객계좌번호",				rAPICall.getString("account_num"));
    		rRetvTrHisP.setString("계좌납입회차번호",			rAPICall.getString("account_seq"));
    		rRetvTrHisP.setString("계좌잔액_N15",				rAPICall.getString("balance_amt"));
    		rRetvTrHisP.setString("현재페이지레코드건수_N2",		rAPICall.getString("page_record_cnt"));
    		rRetvTrHisP.setString("거래내역다음페이지존재여부_V1",	rAPICall.getString("next_page_yn"));
    		rRetvTrHisP.setString("직전조회추적정보_V20",		rAPICall.getString("befor_inquiry_trace_info"));

    		LMultiData tmpLMultiData = new LMultiData();
    		
    		for(int i=0; i<rAPICall.getLMultiData("res_list").getDataCount(); i++) {
      			
      			LData tmpLData = new LData();
      			
      			tmpLData.setString("오픈뱅킹거래일자_V8",		rAPICall.getLMultiData("res_list").getLData(i).getString("tran_date"));
      			tmpLData.setString("오픈뱅킹거래시간_V6",		rAPICall.getLMultiData("res_list").getLData(i).getString("tran_time"));
      			tmpLData.setString("입출금구분_V8",			rAPICall.getLMultiData("res_list").getLData(i).getString("inout_type"));
      			tmpLData.setString("오픈뱅킹거래구분_V20",		rAPICall.getLMultiData("res_list").getLData(i).getString("tran_type"));
      			tmpLData.setString("오픈뱅킹통장인자내용_V40",	rAPICall.getLMultiData("res_list").getLData(i).getString("print_content"));
      			tmpLData.setString("입출금금액",				rAPICall.getLMultiData("res_list").getLData(i).getString("tran_amt"));
      			tmpLData.setString("거래후잔액_N13",			rAPICall.getLMultiData("res_list").getLData(i).getString("after_balance_amt"));
      			tmpLData.setString("거래점명_V40",				rAPICall.getLMultiData("res_list").getLData(i).getString("branch_name"));

      			tmpLMultiData.addLData(tmpLData);
      			
      		}
    		
    		rRetvTrHisP.set("그리드", tmpLMultiData);
    		
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}

    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("----------[rRetvTrHisP]----------");
    		LLog.debug.println(rRetvTrHisP);
    		LLog.debug.println("거래내역조회 END ☆★☆☆★☆☆★☆" );
    	}
		
        return rRetvTrHisP;
    }

}

