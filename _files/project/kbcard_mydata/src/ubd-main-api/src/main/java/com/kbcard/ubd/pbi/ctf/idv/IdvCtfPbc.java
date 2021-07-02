package com.kbcard.ubd.pbi.ctf.idv;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

/** 
 * IdvCtfPbc
 * 
 * @logicalname  : 개별인증 인가코드발급 PBC
 * @author       : 이요한
 * @since        : 2021-06-01
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-06-01       이요한       최초 생성
 *
 */
public class IdvCtfPbc {
	

    /**
     * - 개별인증 인가코드요청
     * 
     * <step1> client_id null 체크
     * <step2> redirect_uri null 체크
     * <step3> client_id 유효성 체크
     * <step4> redirect_uri 유효성 체크
     * <step5> 파라미터 유효성 검증
     * <step6> 거래고유번호 중복 검사
     * <step7> appScheme 유효성 체크
     * <step8> 인가코드생성 ( 클라이언트ID + 거래번호 + 거래일자 + 고객CI )
     * <step9> 인가코드요청 저장
     * <step10> 인가코드 전달 ( 헤더 location setting )
     * 
     * <관련 테이블>
     * TBUBDC012 UBD인가코드발급요청내역
     * <INPUT>
     * <HEAD>
     * x-user-ci : 고객 CI
     * x-api-tran-id : 거래고유번호
     * <INPUT>
     * <BODY>
     * org_code
     * response_type
     * client_id
     * redirect_uri
     * app_scheme
     * state
     * <OUTPUT>
     * <STATUS>
     * 302 FOUND
     * <HEAD>
     * - 정상
     * location ? 거래고유번호 & 일자
     * - 에러
     * location ( 고객 요청 callback_url ) ? code & state
     * <BODY>
     * - 정상
     * - 에러
     * error
     * error_description
     * state 
     * 
     * @serviceId UBD9000110
     * @method idvCtfCnfCdDmd
     * @method(한글명) 개별인증 인가코드요청
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData idvCtfCnfCdDmd(LData input) {
    	LData result = new LData();
		
    	LLog.debug.println("idvCtfCnfCdDmd !!!!!!!!!!!! ");

    	ContextUtil.setHttpRequestHeaderParam();

    	
    	try {
			//거래내역 테이블 ( TBUBDC100) 마이데이터 전문요청내용
			String mydt_tlg_dmd_ctt = "";
			input.put("mydt_tlg_dmd_ctt", mydt_tlg_dmd_ctt);
			//거래내역 테이블 ( TBUBDC100) 마이데이터 전문응답내용
			String mydt_tlg_rsp_ctt = "";
			input.put("mydt_tlg_rsp_ctt", mydt_tlg_rsp_ctt);
	
			//마이데이터 API 식별번호
			input.put("mydt_api_idi_no", "idv001");
	    	
			
	    	//거래요청일자 set
			String cnf_cd_isu_dmd_yms = DateUtil.getCurrentDate("yyyyMMddHHmmss");
			String mydt_tr_dmd_hms = DateUtil.getCurrentDate("HHmmss");
			String mydt_tr_dmd_ymd = DateUtil.getCurrentDate("yyyyMMdd");
			
			input.put("cnf_cd_isu_dmd_yms", cnf_cd_isu_dmd_yms);
			input.put("mydt_tr_dmd_hms", mydt_tr_dmd_hms);
			input.put("mydt_tr_dmd_ymd", mydt_tr_dmd_ymd);
			
	    	
			// 정보주체 식별값 ( 게이트웨이ID )
			String open_api_gt_idi_no       = ContextUtil.getHttpRequestHeaderParam("gwid");
			input.put("open_api_gt_idi_no", open_api_gt_idi_no);
			
			// 거래고유번호 ( 헤더 )
			String mydt_tr_uno   = ContextUtil.getHttpRequestHeaderParam("x-api-tran-id");
			
			//<step1> 헤더값 체크
			LLog.debug.println("<step1> start !!!");
			if(StringUtil.trimNisEmpty(mydt_tr_uno)) {
				// 헤더값 미존재
				String mydt_err_cd_nm = UBD_CONST.REP_CD_BAD_REQUEST_40002;
				String mydt_err_msg_ctt = UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002;
				
				input.put("mydt_tr_uno","undefined");
			     
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			input.put("mydt_tr_uno", mydt_tr_uno);
	
			//거래고유번호 중복 검사
			LMultiData isTranId = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "isTranId", input);
			if(isTranId.size() > 0) {
				//요청파라미터 오류
				String mydt_err_cd_nm = UBD_CONST.REP_CD_BAD_REQUEST_40001;
				String mydt_err_msg_ctt = UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001;
	
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			
			// 정보주체 식별값 ( 헤더 )
			String ci_ctt  = ContextUtil.getHttpRequestHeaderParam("x-user-ci");
			//CI값 체크
			if(StringUtil.trimNisEmpty(mydt_tr_uno)) {
				// 헤더값 미존재
				String mydt_err_cd_nm = UBD_CONST.REP_CD_BAD_REQUEST_40002;
				String mydt_err_msg_ctt = UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002;
				
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			input.put("ci_ctt", ci_ctt);
	
			//<step1> end
			
			
			//정보제공자 기관코드
			String mydt_ofer_ins_cd = (String)input.get("org_code");
			//타입 ( code 고정값 )
			String cnf_cd_rsp_typ_nm   = (String)input.get("response_type");
			//클라이언트 아이디
			String mydt_clint_idi_no       = (String)input.get("client_id");
			//callback uri
			String mydt_clbc_url_adr    = (String)input.get("redirect_uri");
			//마이데이터 서비스 앱 url 스킴
			String mydt_ap_us_nm      = (String)input.get("app_scheme");
			//상태값 ( csrf 보안위협 대응 )
			String mydt_sess_sts_vl           = (String)input.get("state");
	
			//<step2> client_id, redirect_uri 체크
			LLog.debug.println("<step2> start !!!");
			//client_id 체크
			if( StringUtil.trimNisEmpty(mydt_clint_idi_no)) {
				String mydt_err_cd_nm = "invalid_request";
				String mydt_err_msg_ctt = "invalid_client_id";
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			
				return result;
			}
			input.put("mydt_clint_idi_no", mydt_clint_idi_no);
			
			//redirect_uri null 체크
			if(StringUtil.trimNisEmpty(mydt_clbc_url_adr)) {
				String mydt_err_cd_nm = "invalid_request";
				String mydt_err_msg_ctt = "invalid_redirection";
				//400 에러 log insert				
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			input.put("mydt_clbc_url_adr", mydt_clbc_url_adr);
	
			
			// client_id 유효성 체크
			LData isClientId = new LData();
			try {
				isClientId = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "isClientId", input);
			}catch(LException e) {
				String mydt_err_cd_nm= "invalid_request";
				String mydt_err_msg_ctt = "invalid_client_id";
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			
			LLog.debug.println("isClientId =================== ");
			LLog.debug.println(isClientId);
			LLog.debug.println(isClientId.get("MYDT_INS_CD"));
	//		//사업자 서비스명
	//		input.put("mydt_svc_nm" , isClientId.get("MYDT_SVC_NM"));
	//		//사업자 기관코드
	//		input.put("mydt_bzm_ins_cd" , isClientId.get("MYDT_INS_CD"));
	//		//업권구분코드
	//		input.put("mydt_bsz_dtcd" , isClientId.get("MYDT_BSZ_DTCD"));
	
			
			
			//응답메시지 properties 가 담길 Map
			Map<String, String> rspMessageMap = new HashMap<String, String>();
	
			//응답메시지 properties 
			ResourceBundle bundle = ResourceBundle.getBundle("rsp_message", Locale.getDefault());
	
			try {
				Enumeration<String> keys = bundle.getKeys();
				while (keys.hasMoreElements()) {
					String mapKey = keys.nextElement();
					rspMessageMap.put(mapKey,
								bundle.getLocale().getLanguage().equals("ko")
										? new String(bundle.getString(mapKey).getBytes("8859_1"), "ksc5601")
										: bundle.getString(mapKey));
				}
				
			}catch(UnsupportedEncodingException e) {
				String mydt_err_cd_nm = "server_error";
				String mydt_err_msg_ctt = "서버장애가 발생하였습니다.";
	
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
	
			// redirect_uri 유효성 체크
			String isMydtClbcUrlCtt = (String)isClientId.get("MYDT_CLBC_URL_CTT");
			if(isMydtClbcUrlCtt.indexOf(mydt_clbc_url_adr) < 0) {
				String mydt_err_cd_nm = "invalid_request";
				String mydt_err_msg_ctt = "invalid_redirection";
				//400 에러 log insert
				errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			//<step2> end
			
			//<step3> 파라미터 유효성 검증
			LLog.debug.println("<step3> start !!!");
			if ( 
				  StringUtil.trimNisEmpty(mydt_ofer_ins_cd) 
				|| StringUtil.trimNisEmpty(cnf_cd_rsp_typ_nm) 
				|| StringUtil.trimNisEmpty(mydt_ap_us_nm) 
				|| StringUtil.trimNisEmpty(mydt_sess_sts_vl)
				) 
			{
				String mydt_err_cd_nm = "invalid_request";
				String mydt_err_msg_ctt = rspMessageMap.get("cnf_cd_isu.found." + mydt_err_cd_nm);
	
				//302 에러 log insert
				errorLogInsert("I", result, input, mydt_clbc_url_adr, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			
			input.put("mydt_ofer_ins_cd" , mydt_ofer_ins_cd);
			input.put("cnf_cd_rsp_typ_nm", cnf_cd_rsp_typ_nm);
			input.put("mydt_ap_us_nm"    , mydt_ap_us_nm);
			input.put("mydt_sess_sts_vl" , mydt_sess_sts_vl);
			
			
			 // response type 검증
			if(!cnf_cd_rsp_typ_nm.equals("code")){
	
				String mydt_err_cd_nm =  "unsupported_response_type";
				String mydt_err_msg_ctt = rspMessageMap.get("cnf_cd_isu.found." + mydt_err_cd_nm);
	
				//302 에러 log insert
				errorLogInsert("I", result, input, mydt_clbc_url_adr, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
	
			//appScheme 유효성 체크
			String isMydtApUsCtt    = (String)isClientId.get("MYDT_AP_US_CTT");
	
			if(isMydtApUsCtt.indexOf(mydt_ap_us_nm) < 0) {
	
				String mydt_err_cd_nm = "invalid_request";
				String mydt_err_msg_ctt = rspMessageMap.get("cnf_cd_isu.found." + mydt_err_cd_nm);
	
				//302 에러 log insert
				errorLogInsert("I", result, input, mydt_clbc_url_adr, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			//<step3> end
			
			
	
			//<step4> 인가코드생성 ( 클라이언트ID + 거래번호 + 거래일자 + 고객CI )
			LLog.debug.println("<step4> start !!!");
			String cnf_cd_ctt = textSHA128(mydt_clint_idi_no + mydt_tr_uno + mydt_tr_dmd_ymd + ci_ctt);
			input.put("cnf_cd_ctt",cnf_cd_ctt);
			
			// 인가코드요청 저장
			int dmdResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertCnfCdDmd", input);
			
	
			if(dmdResult < 1) {
				String mydt_err_cd_nm = "server_error";
				String mydt_err_msg_ctt = rspMessageMap.get("cnf_cd_isu.found." + mydt_err_cd_nm);
	
				//302 에러 log insert
				errorLogInsert("I", result, input, mydt_clbc_url_adr, mydt_err_cd_nm, mydt_err_msg_ctt);
				
				return result;
			}
			//<step4> end
	
			
			//<step5> 인가코드 전달 ( 헤더 location setting )
			LLog.debug.println("<step5> start !!!");
			ContextUtil.setHttpResponseHeaderParam( "location", "http://mydata.kbcard.com/kbpay/idvctf?xati=" + cnf_cd_ctt  );
    	}catch(LException e) {
    		//HTTP 응답코드 셋팅
    		ContextUtil.setHttpResponseHeaderParam("status", "500");
    		result.put("rsp_code", UBD_CONST.REP_CD_SERVER_ERR_50001);
    		result.put("rsp_msg", UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);
    		
    		e.printStackTrace();
    		
    	}
    	return result;
    }
    
    

    /**
     * - WEBVIEW 인증정보입력 CI 검증 및 제공가능 SCOPE 목록 조회
     * 
     * <step1> 입력값 validation
     * <step2> 인가코드요청데이터 select
     * <step3> 입력값 CI , 요청데이터 CI 비교
     * <step4> 유효 SCOPE 정보 리턴
     * 
     * <관련 테이블>
     * TBUBDC012 UBD인가코드발급요청내역
     * <INPUT>
     * CI
     * 거래고유번호
     * <OUTPUT>
     * 업권
     * SCOPE 목록
     * 자산 목록
     * 응답코드
     * 응답상세
     * 
     * @serviceId UBD9000140
     * @method ApiTranIdVln
     * @method(한글명) SCOPE목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData ApiTranIdVln(LData input) {
    	LData result = new LData();
    	
    	LLog.debug.println("ApiTranIdVln !!!!!!!!!!!! ");

		//HTTP 응답코드 셋팅
		ContextUtil.setHttpResponseHeaderParam("status", "200");
		result.put("rsp_code", "00000");
		result.put("rsp_msg", "성공");
		
    	
    	try {
			//검증대상 인가코드
			String cnf_cd_ctt               = (String)input.get("xati");
			//검증대상 CI
			String ci_ctt                   = (String)input.get("ci_ctt");
	
			LLog.debug.println("ddddddddddddddddddd-===========");
			LLog.debug.println(cnf_cd_ctt);
			LLog.debug.println(ci_ctt);
			
			//<step1> 필수 파라미터 검증
			if(StringUtil.trimNisEmpty(cnf_cd_ctt)) {
				//HTTP 응답코드 셋팅
				ContextUtil.setHttpResponseHeaderParam("status", "400");
				result.put("rsp_code", "40001");
				result.put("rsp_msg", "요청파라미터에 문제가 있습니다.");
	
				return result;
			}
			
			
			if(StringUtil.trimNisEmpty(ci_ctt)) {
				//HTTP 응답코드 셋팅
				ContextUtil.setHttpResponseHeaderParam("status", "400");
				result.put("rsp_code", "40001");
				result.put("rsp_msg", "요청파라미터에 문제가 있습니다.");
	
				return result;
			}
			//<step1> end
			LLog.debug.println("step1 end");
	
			
			//<step2> 인가코드요청데이터 검증
			//인가코드요청데이터
	
			LLog.debug.println("step2 start");
			LLog.debug.println("step2 input");
			LLog.debug.println(input);
			LMultiData cnfCdDmdHisLi = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "selectCnfCdDmdHis", input);
	
			LLog.debug.println(cnfCdDmdHisLi);
			if(cnfCdDmdHisLi.size() < 1) {
				//HTTP 응답코드 셋팅
				ContextUtil.setHttpResponseHeaderParam("status", "404");
				result.put("rsp_code", "40402");
				result.put("rsp_msg", "요청정보가 없는 거래고유번호");
				
				return result;
				
			}
			LData cnfCdDmdHis = cnfCdDmdHisLi.getLData(0);
			
			LLog.debug.println("step2 cnfCdDmdHis");
			
			if(cnfCdDmdHis.get("CNF_CD_ERR_YN").equals("Y")) {
				ContextUtil.setHttpResponseHeaderParam("status", "401");
				result.put("rsp_code", "40101");
				result.put("rsp_msg", "정상진행이 되지 않은 요청정보");
				
				return result;
			}
			
			//인가코드요청 CI
			String reqCiCtt = (String)cnfCdDmdHis.get("CI_CTT");
			if(!ci_ctt.equals(reqCiCtt)) {
				ContextUtil.setHttpResponseHeaderParam("status", "404");
				result.put("rsp_code", "40403");
				result.put("rsp_msg", "요청정보와 다른 CI정보");
				
				return result;
			}
			
			//요청업권
			String mydt_bsz_dtcd = (String)cnfCdDmdHis.get("MYDT_BSZ_DTCD");
			if(StringUtil.trimNisEmpty(mydt_bsz_dtcd)) {
				//HTTP 응답코드 셋팅
				ContextUtil.setHttpResponseHeaderParam("status", "400");
				result.put("rsp_code", "40001");
				result.put("rsp_msg", "요청파라미터에 문제가 있습니다.");
	
				return result;
			}
			//result 업권
			result.set("bsz"    , mydt_bsz_dtcd);
			//<step2> end
			LLog.debug.println("step2 end");
	
			
			//<step3> scope 목록 조회
//			input.put("mydt_bsz_dtcd", mydt_bsz_dtcd);
//			
//			//SCOPE 선택목록 조회
//			LMultiData atrScopCtt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "selectAtrScopCtt", input);
//	
//		    LMultiData scopLi = new LMultiData();
//		    
//			for(LinkedHashMap mp :atrScopCtt) {
//				
//				LData scopMp = new LData();
//				
//				Iterator<String> keys = mp.keySet().iterator();
//				while(keys.hasNext()) {
//					String key = keys.next();
//					String lowerKey = key.toLowerCase();
//					scopMp.put(lowerKey, mp.get(key));
//				}
//				scopLi.add(scopMp);
//			}
//			//result SCOPE
//			result.set("scope"    , scopLi);
			//<step3> end
	
			LLog.debug.println("step3 end");
			
	
			//<step4> 고객식별번호 조회
			//고객식별번호
			LData rCsidInqOut = new LData();
			try {
				UbdCommon uCom = new UbdCommon();
				rCsidInqOut = uCom.retvCstCmn( (String)cnfCdDmdHis.get("CI_CTT"));
			}catch(Exception e) {
				ContextUtil.setHttpResponseHeaderParam("status", "400");
				result.put("rsp_code", "error");
				result.put("rsp_msg", "고객정보 미존재");
				
		    	return result;
			}
			LLog.debug.println(rCsidInqOut);
			LLog.debug.println("step4 end");
			//<step4> end
			
			LData data = new LData();
			
			data.put("CI","FZWesE25ls3rQDyt2QFBNaK06pjh/j8a4P1AOls6OlbNvJpzDn9PRrFrSYR1jFyz4MCx6YlFIXUwo9eSMk4VXg==");
			data.put("고객식별자","4086138145");
			data.put("호출시스템분기",UBD_CONST.CLLGL_SYS_DTCD_CDC);
			
			LData cardData = (LData)BizCommand.execute("com.kbcard.ubd.pbi.card.MyDtCrdCtgPbc", "retvCardListVln", data);

			if(cardData != null) {
				LMultiData cardLi = (LMultiData) cardData.get("보유카드목록");
				
	    		List<Map<String,Object>> card_list = new ArrayList<Map<String,Object>>();
	    		for(Map cardMp:cardLi) {
	    			LLog.debug.println(cardMp);
	    			
	        		Map<String,Object> card_mp = new HashMap<String,Object>();

	        		card_mp.put("card_id"   , cardMp.get("카드식별자"));
	        		card_mp.put("is_consent", cardMp.get("전송요구여부"));
	        		card_mp.put("card_name" , cardMp.get("카드상품명"));
//	        		card_mp.put("card_num"  , cardMp.get("카드번호"));
	        		card_mp.put("card_num"  , "943646******0027");
	        		
	        		card_mp.put("card_type"  , cardMp.get("카드구분"));
	        		card_mp.put("card_member" , cardMp.get("소지자구분"));
	    			
		    		card_list.add(card_mp);
	    		}
	    		result.put("card_list", card_list);
			}
		
    	}catch( LException e) {

    		//HTTP 응답코드 셋팅
    		ContextUtil.setHttpResponseHeaderParam("status", "500");
    		result.put("rsp_code", UBD_CONST.REP_CD_SERVER_ERR_50001);
    		result.put("rsp_msg", UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);
    		
    		e.printStackTrace();
    		
    	}
		//자산요구내용 result put

		
    	return result;
    }

    /**
     * - 인가코드발급
     * 
     * <step1> 인가코드 요청데이터 검증
     * <step2> 인가코드요청데이터 select
     * <step3> 입력값 CI , 요청데이터 CI 비교
     * <step4> 유효 SCOPE 정보 리턴
     * 
     * <관련 테이블>
     * TBUBDC012 UBD인가코드발급요청내역
     * TBUBDC014 UBD전송요구기본
     * TBUBDC015 UBD전송요구권한영역내역
     * TBUBDC016 UBD전송요구자산내역
     * <INPUT>
     * CI
     * 거래고유번호
     * 거래일자
     * 업권
     * <OUTPUT>
     * 인가코드 요청 CI
     * 인가코드 요청 에러 여부
     * SCOPE 목록
     * 
     * @serviceId UBD9000170
     * @method IdvCtfCnfCdRsp
     * @method(한글명) SCOPE목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData IdvCtfCnfCdRsp(LData input) throws LException {
    	
    	
    	LData result = new LData();
    	
    	LLog.debug.println("IdvCtfCnfCdRsp !!!!!!!!!!!! ");


		// 정보동의 완료 여부
		String rsp_cnsYn              = (String) input.get("cns_yn");
    	
		//인가코드
		String rsp_ctfCnfCd        = (String)input.get("xati");

		
		//마이데이터정기전송여부
		String mydt_rtvl_trs_yn = (String) input.get("mydt_rtvl_trs_yn");
		//기본정보정기전송주기구분코드
		String bas_inf_rtvl_trs_cyc_dtcd = (String) input.get("bas_inf_rtvl_trs_cyc_dtcd");
		//기본정보정기전송주기값 
		String bas_inf_rtvl_trs_cyc_vl = (String) input.get("bas_inf_rtvl_trs_cyc_vl");
		//추가정보정기전송주기구분코드
		String spp_inf_rtvl_trs_cyc_dtcd = (String) input.get("spp_inf_rtvl_trs_cyc_dtcd");
		//추가정보정기전송주기값
		String spp_inf_rtvl_trs_cyc_vl = (String) input.get("spp_inf_rtvl_trs_cyc_vl");
		//마이데이터전송요구종료시점값
		String mydt_trs_rqst_ed_pit_vl = (String) input.get("mydt_trs_rqst_ed_pit_vl");
		//마이데이터전송요구종료일시
		String mydt_trs_rqst_ed_yms = (String) input.get("mydt_trs_rqst_ed_yms");
		//마이데이터전송요구목적내용
		String mydt_trs_rqst_obj_ctt = (String) input.get("mydt_trs_rqst_obj_ctt");
		//마이데이터보유가능일시
		String mydt_hld_abl_yms = (String) input.get("mydt_hld_abl_yms");
		//보유가능기간개월수
		String hld_abl_trm_mncn = (String) input.get("hld_abl_trm_mncn");

		
		// 전송요구Scope
		String rsp_scope              = (String) input.get("scope");
		
		// 전송요구자산내역
		LMultiData rsp_astCtt         = (LMultiData) input.get("ast_ctt");
		
		//<step1> 인가코드 요청데이터 검증
    	LLog.debug.println("<step1> ");
		//인가코드요청데이터
		LMultiData cnfCdDmdHisLi = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "selectCnfCdDmdHis", input);
    	LLog.debug.println(cnfCdDmdHisLi);
		if(cnfCdDmdHisLi.size() < 1) {
			//HTTP 응답코드 셋팅
			ContextUtil.setHttpResponseHeaderParam("status", "404");
			result.put("rsp_code", "40402");
			result.put("rsp_msg", "요청정보가 없는 거래고유번호");
			
			return result;
			
		}
		LData cnfCdDmdHis = cnfCdDmdHisLi.getLData(0);
		
		// 인가코드 요청내역이 없을 경우
		if(cnfCdDmdHis.size() < 1) {
			//HTTP 응답코드 셋팅
			ContextUtil.setHttpResponseHeaderParam("status", "400");
			result.put("rsp_code", "40001");
			result.put("rsp_msg", "요청파라미터에 문제가 있습니다.");
			
			return result;
		}
		//<step1> end


		//응답메시지 properties 가 담길 Map
		Map<String, String> rspMessageMap = new HashMap<String, String>();
		try {
			//응답메시지 properties 
			ResourceBundle bundle = ResourceBundle.getBundle("rsp_message", Locale.getDefault());
	
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String mapKey = keys.nextElement();
				rspMessageMap.put(mapKey,
							bundle.getLocale().getLanguage().equals("ko")
									? new String(bundle.getString(mapKey).getBytes("8859_1"), "ksc5601")
									: bundle.getString(mapKey));
			}
		}catch(UnsupportedEncodingException e) {

			ContextUtil.setHttpResponseHeaderParam("status", "500");
			result.put("rsp_code", "50001");
			result.put("rsp_msg", "서버장애가 발생하였습니다.");
			
			return result;
		}

		//<step2> 고객식별번호 조회
		//고객식별번호
    	LLog.debug.println("<step2> ");
		
		LData rCsidInqOut = new LData();
		try {
			UbdCommon uCom = new UbdCommon();
			rCsidInqOut = uCom.retvCstCmn( (String)cnfCdDmdHis.get("CI_CTT"));
		}catch(Exception e) {
			ContextUtil.setHttpResponseHeaderParam("status", "400");
			result.put("rsp_code", "error");
			result.put("rsp_msg", "고객정보 미존재");
			
	    	return result;
		}
    	LLog.debug.println(rCsidInqOut);
		//<step2> end
		

		//<step3> 전송요구기본 저장 ( TBUBDC014 )
		//전송요구기본 insert
    	LLog.debug.println("<step3> ");
		LData trsRqst = new LData();

	    trsRqst.put("mydt_ofer_ins_cd"            , (String)cnfCdDmdHis.get("MYDT_OFER_INS_CD"));               // 마이데이터제공기관코드
	    trsRqst.put("mydt_bsz_dtcd"               , (String)cnfCdDmdHis.get("MYDT_BSZ_DTCD"));                  // 마이데이터업권구분코드
	    trsRqst.put("mydt_bzm_ins_cd"             , (String)cnfCdDmdHis.get("MYDT_BZM_INS_CD"));                // 마이데이터사업자기관코드
	    trsRqst.put("mydt_clint_idi_no"           , (String)cnfCdDmdHis.get("MYDT_CLINT_IDI_NO"));              // 마이데이터클라이언트식별번호
	    trsRqst.put("ci_ctt"                      , (String)cnfCdDmdHis.get("CI_CTT"));                         // CI내용
		
	    trsRqst.put("cst_idf"                     , rCsidInqOut.get("고객식별자"));                        // 고객식별자
	    trsRqst.put("cst_mg_no"                   , rCsidInqOut.get("고객관리번호"));                      // 고객관리번호
	     
	    trsRqst.put("mydt_rtvl_trs_yn"             , mydt_rtvl_trs_yn                  ); // 마이데이터정기전송여부
	    trsRqst.put("bas_inf_rtvl_trs_cyc_dtcd"    , bas_inf_rtvl_trs_cyc_dtcd         ); // 기본정보정기전송주기구분코드
	    trsRqst.put("bas_inf_rtvl_trs_cyc_vl"      , bas_inf_rtvl_trs_cyc_vl           ); // 기본정보정기전송주기값
	    trsRqst.put("spp_inf_rtvl_trs_cyc_dtcd"    , spp_inf_rtvl_trs_cyc_dtcd         ); // 추가정보정기전송주기구분코드
	    trsRqst.put("spp_inf_rtvl_trs_cyc_vl"      , spp_inf_rtvl_trs_cyc_vl           ); // 추가정보정기전송주기값
	    trsRqst.put("mydt_trs_rqst_ed_pit_vl"      , mydt_trs_rqst_ed_pit_vl           ); // 마이데이터전송요구종료시점값
	    trsRqst.put("mydt_trs_rqst_ed_yms"         , mydt_trs_rqst_ed_yms              ); // 마이데이터전송요구종료일시
	    trsRqst.put("mydt_trs_rqst_obj_ctt"        , mydt_trs_rqst_obj_ctt             ); // 마이데이터전송요구목적내용
	    trsRqst.put("mydt_hld_abl_yms"             , mydt_hld_abl_yms                  ); // 마이데이터보유가능일시
	    trsRqst.put("hld_abl_trm_mncn"             , hld_abl_trm_mncn                  ); // 보유가능기간개월수
	     
	    LLog.debug.println(trsRqst);
	    // 전송요구기본 INSERT 
	    int trsResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertTrsRqst", trsRqst);
	    // 전송요구이력 INSERT 
	    int trsHisResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertTrsRqstHis", trsRqst);
	    //<step3> end
	    LLog.debug.println(trsResult);
		
	     
	    String mydt_ofer_ins_cd  =  (String)cnfCdDmdHis.get("MYDT_OFER_INS_CD") ;  // 마이데이터제공기관코드
	    String mydt_bsz_dtcd     =  (String)cnfCdDmdHis.get("MYDT_BSZ_DTCD") ; // 마이데이터업권구분코드
	    String mydt_bzm_ins_cd   =  (String)cnfCdDmdHis.get("MYDT_BZM_INS_CD") ; // 마이데이터사업자기관코드
	    String mydt_clint_idi_no =  (String)cnfCdDmdHis.get("MYDT_CLINT_IDI_NO") ; // 마이데이터클라이언트식별번호
	    String ci_ctt            =  (String)cnfCdDmdHis.get("CI_CTT") ; // CI내용
	    String mydt_tr_uno       =  (String)cnfCdDmdHis.get("MYDT_TR_UNO") ; // 마이데이터거래고유번호
	    String mydt_tr_dmd_ymd   =  (String)cnfCdDmdHis.get("MYDT_TR_DMD_YMD") ; // 마이데이터거래요청년월일

		
		//<step4> 전송요구권한영역 insert ( scope)
		LData scope = new LData();

	    scope.put("mydt_ofer_ins_cd"            , mydt_ofer_ins_cd);               // 마이데이터제공기관코드
		scope.put("mydt_bsz_dtcd"               , mydt_bsz_dtcd);                  // 마이데이터업권구분코드
		scope.put("mydt_bzm_ins_cd"             , mydt_bzm_ins_cd);                // 마이데이터사업자기관코드
		scope.put("mydt_clint_idi_no"           , mydt_clint_idi_no);              // 마이데이터클라이언트식별번호
		scope.put("ci_ctt"                      , ci_ctt);                         // CI내용
		scope.put("mydt_atr_scop_ctt"           , rsp_scope        ); //권한영역내역

	    // 권한영역 Insert
		int scopeResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertScope", scope);

		scope.put("mydt_tr_uno"          , mydt_tr_uno     ); //마이데이터거래고유번호      
		scope.put("mydt_tr_dmd_ymd"      , mydt_tr_dmd_ymd ); //마이데이터거래요청년월일     
		
		// 권한영역이력 Insert
		int scopeHisResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertScopeHis", scope);
		//<step4> end
		System.out.println("<step4> end");
		System.out.println(scopeHisResult);
		


		//<step5> 전송요구자산내역 insert

		for(LinkedHashMap astMp:rsp_astCtt) {
			LData astCtt = new LData();

		    astCtt.put("mydt_ofer_ins_cd"            , mydt_ofer_ins_cd);               // 마이데이터제공기관코드
			astCtt.put("mydt_bsz_dtcd"               , mydt_bsz_dtcd);                  // 마이데이터업권구분코드
			astCtt.put("mydt_bzm_ins_cd"             , mydt_bzm_ins_cd);                // 마이데이터사업자기관코드
			astCtt.put("mydt_clint_idi_no"           , mydt_clint_idi_no);              // 마이데이터클라이언트식별번호
			astCtt.put("ci_ctt"                      , ci_ctt);                         // CI내용
			astCtt.put("mydt_ast_ctt"                , astMp.get("mydt_ast_ctt")        ); //마이데이터 자산내용
			System.out.println("step5");
			System.out.println(astCtt);
		     // 자산목록 Insert
			int astResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertAstCtt", astCtt);

			astCtt.put("mydt_tr_uno"          , mydt_tr_uno     ); //마이데이터거래고유번호      
			astCtt.put("mydt_tr_dmd_ymd"      , mydt_tr_dmd_ymd ); //마이데이터거래요청년월일  
			// 자산목록이력 Insert
			int astHisResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertAstCttHis", astCtt);
		}
		//<step5> end
		
		
		//<step6> 인가코드발급요청내역 정상발급 update
	    input.put("mydt_tr_uno"                 , mydt_tr_uno);      // 마이데이터거래고유번호  
	    input.put("mydt_tr_dmd_ymd"             , mydt_tr_dmd_ymd ); //마이데이터거래요청년월일  
		
		BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "updateCnfCdDmd", input);
		
		//개별인증001 return

		//마이데이터콜백URL주소
		String mydt_clbc_url_adr        = (String)cnfCdDmdHis.get("MYDT_CLBC_URL_ADR");
		//마이데이터세션상태값
		String mydt_sess_sts_vl         = (String)cnfCdDmdHis.get("MYDT_SESS_STS_VL");
		
		//location
		ContextUtil.setHttpResponseHeaderParam( "location", mydt_clbc_url_adr + "?code=" + rsp_ctfCnfCd + "&state=" + mydt_sess_sts_vl + "&api_tran_id=" + mydt_tr_uno );
		
    	return result;
    }
    
    
    
    
    
    public void errorLogInsert(String IUgbn, LData rstData,  LData input, String redirect_uri, String mydt_err_cd_nm, String mydt_err_msg_ctt) throws LException {
    	
    	String api_tran_id  = (String)input.get("x_user_ci");
    	String state        = (String)input.get("state");
    	
		//location
		ContextUtil.setHttpResponseHeaderParam( "location", redirect_uri + "?error=" + mydt_err_cd_nm + "&state=" + state + "&api_tran_id=" + api_tran_id );
		//에러코드
		rstData.set( "mydt_err_cd_nm",  mydt_err_cd_nm);
		//에러메시지
		rstData.set( "mydt_err_msg_ctt",  mydt_err_msg_ctt);
		//상태값
		rstData.set( "state", input.get("state") );
		//상태값
		rstData.set( "api_tran_id", input.get("api_tran_id") );

		//인가코드요청 테이블 error insert
		input.put("mydt_err_cd_nm", mydt_err_cd_nm);
		input.put("mydt_err_msg_ctt", mydt_err_msg_ctt);
		try {
			if(IUgbn.equals("I")) {
				BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertCnfCdDmd", input);
				BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertTranLog", input);
			}else {
				BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "updateCnfCdDmd", input);
				BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertTranLog", input);
				
			}
		}catch(LException e) {

			mydt_err_cd_nm = UBD_CONST.REP_CD_SERVER_ERR_50001;
			mydt_err_msg_ctt = UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
			
        	rstData.set( "error",  mydt_err_cd_nm);
        	rstData.set( "error_description",  mydt_err_msg_ctt);
    	}
    }
    public void errorLogInsert(LData rstData,  LData input, String mydt_err_cd_nm, String mydt_err_msg_ctt) throws LException {

		//HTTP 응답코드 셋팅
		ContextUtil.setHttpResponseHeaderParam("status", "400");
		
    	//에러코드
    	rstData.set( "error",  mydt_err_cd_nm);
    	//에러메시지
    	rstData.set( "error_description",  mydt_err_msg_ctt);
    	
    	//인가코드요청 테이블 error insert
    	input.put("mydt_err_cd_nm", mydt_err_cd_nm);
    	input.put("mydt_err_msg_ctt", mydt_err_msg_ctt);
    	
    	//TBUBDC100 테이블에만 insert
    	try {
    		BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "insertTranLog", input);
    	}catch(LException e) {

			mydt_err_cd_nm = UBD_CONST.REP_CD_SERVER_ERR_50001;
			mydt_err_msg_ctt = UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
			
			
        	rstData.set( "error",  mydt_err_cd_nm);
        	rstData.set( "error_description",  mydt_err_msg_ctt);
    	}
    }


	public static String textSHA128(String key) {
		try{

			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] hash = digest.digest(key.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			//출력
			return hexString.toString();
			
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}	
}
