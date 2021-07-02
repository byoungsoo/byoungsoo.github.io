package com.kbcard.ubd.pbi.ctf.idv;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * @logicalname  : 개별인증 접근토큰발급 PBC
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
public class IdvCtfAcPbc {
	

    /**
     * - 개별인증 접근토큰요청
     * 
     * <step1> 응답메시지 properties 조회
     * <step2> 파라미터 유효성 검증
     * <step3> response type 검증
     * <step4> client_id 유효성 검증
     * <step5> 인가코드요청 저장
     * <step6> 인가코드요청 webview 정보 리턴
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
    public LData idvCtfAcDmd(LData input) throws LException {
    	LData result = new LData();

    	ContextUtil.setHttpRequestHeaderParam();
    	
    	LLog.debug.println("idvCtfAcDmd !!!!!!!!!!!! ");

		// 거래고유번호 ( 헤더 )
		String rsp_x_api_tran_id   = ContextUtil.getHttpRequestHeaderParam("x-api-tran-id");

		String mydt_tr_dmd_ymd = DateUtil.getCurrentDate("yyyyMMdd");
		
		//정보제공자 기관코드
		String org_code        = (String)input.get("org_code");
		//권한부여방식
		String grant_type   = (String)input.get("grant_type");
		//인가코드
		String cnf_cd_ctt       = (String)input.get("code");
		//클라이언트 ID
		String client_id    = (String)input.get("client_id");
		//클라이언트 Secret
		String client_secret      = (String)input.get("client_secret");
		//Callback URL
		String redirect_uri           = (String)input.get("redirect_uri");

		input.put("mydt_tr_uno", rsp_x_api_tran_id);
		input.put("mydt_tr_dmd_ymd", mydt_tr_dmd_ymd);
		input.put("mydt_trs_tg_dtcd", "02"); //전송대상구분. ( 02 : 본인 ) 
		input.put("mydt_ctf_dtcd", "01");  // 인증구분 ( 01 : 개별인증 )
		
		//거래내역 테이블 ( TBUBDC100) 마이데이터 전문요청내용
		String mydt_tlg_dmd_ctt = "";
		input.put("mydt_tlg_dmd_ctt", mydt_tlg_dmd_ctt);
		//거래내역 테이블 ( TBUBDC100) 마이데이터 전문응답내용
		String mydt_tlg_rsp_ctt = "";
		input.put("mydt_tlg_rsp_ctt", mydt_tlg_rsp_ctt);


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
		
		//<step1> 파라미터 유효성 검증
		if (  StringUtil.trimNisEmpty(rsp_x_api_tran_id) 
				|| StringUtil.trimNisEmpty(org_code) 
				|| StringUtil.trimNisEmpty(grant_type)
				|| StringUtil.trimNisEmpty(cnf_cd_ctt)
				|| StringUtil.trimNisEmpty(client_id)
				|| StringUtil.trimNisEmpty(client_secret) 
				|| StringUtil.trimNisEmpty(redirect_uri) 
			) 
		{
			String mydt_err_cd_nm = "invalid_request";
			String mydt_err_msg_ctt = rspMessageMap.get("idv_ac_isu.bad_request." + mydt_err_cd_nm);

			//400 에러 log insert
			errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			
			return result;
			
		}
		//<step1> end

		
		//<step2> client_id 유효성 체크
		LData isClientId = new LData();
		try {
			isClientId = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "isClientId", input);
		}catch(LException e) {
			String mydt_err_cd_nm= "invalid_client";
			String mydt_err_msg_ctt = rspMessageMap.get("idv_ac_isu.bad_request." + mydt_err_cd_nm);
			
			//400 에러 log insert
			errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			
			return result;
		}
		//<step2> end

		//<step3> 인가코드 요청정보 검증
		input.put("xati", cnf_cd_ctt);
		//인가코드요청데이터
		LMultiData cnfCdDmdHisLi = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "selectCnfCdDmdHis", input);
		
		if(cnfCdDmdHisLi.size() < 1) {
			String mydt_err_cd_nm= "invalid_grant";
			String mydt_err_msg_ctt = rspMessageMap.get("idv_ac_isu.bad_request." + mydt_err_cd_nm);
			
			//400 에러 log insert
			errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			
			return result;
		}
		LData cnfCdDmdHis = cnfCdDmdHisLi.getLData(0);
		
		
		String cnf_cd_prc_yn = cnfCdDmdHis.getString("CNF_CD_PRC_YN"); //인가코드 발급여부
		
		if( cnf_cd_prc_yn == null || !cnf_cd_prc_yn.equals("Y")) {
			String mydt_err_cd_nm= "invalid_grant";
			String mydt_err_msg_ctt = rspMessageMap.get("idv_ac_isu.bad_request." + mydt_err_cd_nm);
			
			//400 에러 log insert
			errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			
			return result;
		}
		//<step3> end
		
		String dmd_mydt_ofer_ins_cd  = (String)cnfCdDmdHis.get("MYDT_OFER_INS_CD"); // 제공기관코드
		String dmd_mydt_bzm_ins_cd   = (String)cnfCdDmdHis.get("MYDT_BZM_INS_CD"); // 사업자 기관코드
		String dmd_mydt_bsz_dtcd     = (String)cnfCdDmdHis.get("MYDT_BSZ_DTCD"); // 업권구분코드
		String dmd_mydt_clint_idi_no = (String)cnfCdDmdHis.get("MYDT_CLINT_IDI_NO"); // 클라이언트식별번호
		String dmd_ci_ctt            = (String)cnfCdDmdHis.get("CI_CTT"); // CI내용
		
		input.put("mydt_ofer_ins_cd"  ,  dmd_mydt_ofer_ins_cd );
		input.put("mydt_bzm_ins_cd"   ,  dmd_mydt_bzm_ins_cd  );
		input.put("mydt_bsz_dtcd"     ,  dmd_mydt_bsz_dtcd    );
		input.put("mydt_clint_idi_no" ,  dmd_mydt_clint_idi_no);
		input.put("ci_ctt"            ,  dmd_ci_ctt           );

		//<step4> scope 목록 조회
		LMultiData usrScopeCtt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "selectUsrScopCtt", input);
		//scope 유효성 체크
		if(usrScopeCtt.size() < 1){
			//scope 유효하지않음
			String mydt_err_cd_nm = "invalid_scope";
			String mydt_err_msg_ctt = rspMessageMap.get("idv_ac_isu.bad_request." + mydt_err_cd_nm);
			
			//400 에러 log insert
			errorLogInsert(result, input, mydt_err_cd_nm, mydt_err_msg_ctt);
			return result;
		}
		

		//<step5> 기발급 접근토큰 확인
		LMultiData usrAt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "selectUsrAtHis", input);
		if(usrAt.size() > 0) {
			//접근토큰이력 테이블 접근토큰, 리프레시토큰 DELETE
			BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "deleteUsrAcHis", input);
			
			//접근토큰이력테이블 접근토큰, 리프레시토큰 insert
			input.put("accs_tken_dscr_rsn_dtcd","02"); // 폐기사유구분코드 ( 02 : 갱신 )
			input.put("at_dscr_yn","Y"); // 폐기사유구분코드 ( 02 : 갱신 )
			int acHisInsertResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAcHisAll", input);
			
			//기발급 접근토큰 ,리프레시토큰 폐기 & 삭제 처리
			int acDelResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "deleteUsrAc", input);
		}
		//<step5> end

		
		//<step6> 접근토큰발급
		String iss         = org_code; //접근토큰발급자기관코드
		String aud         = (String) isClientId.get("MYDT_INS_CD"); //접근토큰수신자기관코드
		String jti         = "I" + "_" +rsp_x_api_tran_id + "_" + mydt_tr_dmd_ymd; //접근토큰식별자
		Long expiredTime   = 1000 * 60L * 60L * 24L * 90L; //접근토큰유효시간 ( 90일 )
		String scope       = (String) usrScopeCtt.get(0).get("MYDT_ATR_SCOP_CTT"); //scope
		
		LData data = new LData();
		
		data.put("iss",iss);
		data.put("aud",aud);
		data.put("jti",jti);
		data.put("expiredTime",expiredTime);
		data.put("scope",scope);
		
		String token = (String)BizCommand.execute("com.kbcard.ubd.pbi.token.TokenCommonPbc", "makeToken", data);

		input.put("at_expt_ss_cnt"           , expiredTime);
		input.put("mydt_atr_scop_ctt"        , scope);
		input.put("at_idi_no"                , jti);
		input.put("mydt_tken_ctt"            , token);
		

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
		//<step4> end

		input.put("cst_idf"                     , rCsidInqOut.get("고객식별자"));                        // 고객식별자
		input.put("cst_mg_no"                   , rCsidInqOut.get("고객관리번호"));                      // 고객관리번호
		
		//<step7> 발급 접근토큰 저장
		input.put("mydt_tken_dtcd", "02"); // 마이데이터 접근토큰구분코드 ( 01 : 리프레시토큰 , 02 : 접근토큰)
		//접근토큰 저장
		System.out.println("input ==============");
		System.out.println(input);
		int insertUsrAcResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAc", input);
		//<step6> end
		
		//<step8> 리프레시토큰발급
		String refreshJti         = "R" + "_" +rsp_x_api_tran_id + "_" + mydt_tr_dmd_ymd;
		Long refreshExpiredTime   = 1000 * 60L * 60L * 24L * 365L; //접근토큰유효시간 ( 365일 )
		
		data = new LData();
		
		data.put("iss",iss);
		data.put("aud",aud);
		data.put("jti",refreshJti);
		data.put("expiredTime",refreshExpiredTime);
		data.put("scope",scope);
		
		String refreshToken = (String)BizCommand.execute("com.kbcard.ubd.pbi.token.TokenCommonPbc", "makeToken", data);

		input.put("at_expt_ss_cnt"           , expiredTime);
		
		//<step9> 발급 접근토큰 저장
		input.put("mydt_tken_dtcd", "01"); // 마이데이터 접근토큰구분코드 ( 01 : 리프레시토큰 , 02 : 접근토큰)
		//접근토큰 저장
		int insertUsrAcRefreshResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAc", input);
		//<step9>  end

		input.put("at_dscr_yn", "N"); /*  접근토큰폐기여부  */
	    input.put("accs_tken_dscr_rsn_dtcd", null); /*  접근토큰폐기여부  */
	        
		//토큰 이력 저장
		int insertUsrAcHisResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAcHisAll", input);
		//<step8> end
		

		//<step9> 개별인증 결과 리턴
		ContextUtil.setHttpResponseHeaderParam( "x-api-tran-id",  rsp_x_api_tran_id);
		result.set("token_type","Bearer"); // 접근토큰유형
		result.set("access_token",token); // 발급된 접근토큰
		result.set("expires_in",expiredTime); // 유효기간
		result.set("refresh_token",refreshToken); // 리프레시토큰
		result.set("refresh_token_expires_in",refreshExpiredTime); // 리프레시토큰
		result.set("scope",scope); // scope
		//<step9> end
		
		
    	return result;
    }
    /**
     * - 개별인증 접근토큰갱신요청
     * 
     * <step1> 응답메시지 properties 조회
     * <step2> 파라미터 유효성 검증
     * <step3> response type 검증
     * <step4> client_id 유효성 검증
     * <step5> 인가코드요청 저장
     * <step6> 인가코드요청 webview 정보 리턴
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
    public LData idvCtfRtAcDmd(LData input) throws LException {
    	LData result = new LData();
    	
    	ContextUtil.setHttpRequestHeaderParam();
    	
    	LLog.debug.println("idvCtfRtAcDmd !!!!!!!!!!!! ");
    	
    	// 거래고유번호 ( 헤더 )
    	String rsp_x_api_tran_id   = ContextUtil.getHttpRequestHeaderParam("x-api-tran-id");
    	
    	//정보제공자 기관코드
    	String org_code        = (String)input.get("org_code");
    	//권한부여방식
    	String grant_type   = (String)input.get("grant_type");
    	//리프레시토큰
    	String refresh_token       = (String)input.get("refresh_token");
    	//클라이언트 ID
    	String client_id    = (String)input.get("client_id");
    	//클라이언트 Secret
    	String client_secret      = (String)input.get("client_secret");
    	
    	
    	//거래내역 테이블 ( TBUBDC100) 마이데이터 전문요청내용
    	String mydt_tlg_dmd_ctt = "";
    	input.put("mydt_tlg_dmd_ctt", mydt_tlg_dmd_ctt);
    	//거래내역 테이블 ( TBUBDC100) 마이데이터 전문응답내용
    	String mydt_tlg_rsp_ctt = "";
    	input.put("mydt_tlg_rsp_ctt", mydt_tlg_rsp_ctt);
    	
    	//<step1> 응답메시지 properties 가 담길 Map
    	Map<String, String> rspMessageMap = new HashMap<String, String>();
    	
    	String error = "";
    	String error_description = "";
    	
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
    		
    		//<step1> 파라미터 유효성 검증
    		if (  StringUtil.trimNisEmpty(rsp_x_api_tran_id) 
    				|| StringUtil.trimNisEmpty(org_code) 
    				|| StringUtil.trimNisEmpty(grant_type)
    				|| StringUtil.trimNisEmpty(refresh_token)
    				|| StringUtil.trimNisEmpty(client_id)
    				|| StringUtil.trimNisEmpty(client_secret) 
    				) 
    		{
    			error = "invalid_request";
    			error_description = rspMessageMap.get("idv_ac_isu.bad_request." + error);
    			
    			//400 에러 log insert
//				errorLogInsert(result, input, redirect_uri, error, error_description);
    			
    			//<step2> grant_type 유효성 검증
    		}else if(!grant_type.equals("refresh_token")) {
    			
    			error = "unsupported_grant_type";
    			error_description = rspMessageMap.get("idv_ac_isu.unsupported_grant_type." + error);
    			
    			//400 에러 log insert
//				errorLogInsert(result, input, redirect_uri, error, error_description);
    		}else {
    			LMultiData isClientId = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "isClientId", input);
    			
    			//<step3> client_id 유효성 체크
    			if(isClientId.size() > 0) {
    				
    				//기발행 리프레시 접근토큰

					input.put("mydt_tken_dtcd", "01"); // 마이데이터 접근토큰구분코드 ( 01 : 리프레시토큰 , 02 : 접근토큰)
					LMultiData usrAt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "selectUsrAtHis", input);
					
    				if(usrAt.size() > 0 && ( usrAt.get(0).get("AT_DSCR_YN") == null || usrAt.get(0).get("AT_DSCR_YN").equals("N"))) {
    					String dmd_mydt_tr_uno = (String)usrAt.get(0).get("MYDT_TR_UNO");
    					String dmd_mydt_tr_dmd_ymd = (String)usrAt.get(0).get("MYDT_TR_DMD_YMD");
    					String dmd_ci_ctt = (String)usrAt.get(0).get("CI_CTT");
    					
    					input.put("dmd_mydt_tr_uno", dmd_mydt_tr_uno);
    					input.put("dmd_mydt_tr_dmd_ymd", dmd_mydt_tr_dmd_ymd);
    					input.put("dmd_ci_ctt", dmd_ci_ctt);
    					
    					//scope 목록
    					LMultiData usrScopeCtt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "selectUsrScopCtt", input);
    					//<step4> scope 유효성 체크
    					if(usrScopeCtt.size() > 0){

							//접근토큰내역테이블 접근토큰, 리프레시토큰 insert
							input.put("accs_tken_dscr_rsn_dtcd","02"); // 폐기사유구분코드 ( 02 : 갱신 )
							int acHisInsertResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAcHis", input);
							
							//기발급 접근토큰 폐기 & 삭제 처리
							int acDelResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "deleteUsrAc", input);
							
    						//접근토큰발급
    						String iss         = org_code; //접근토큰발급자기관코드
    						String aud         = (String) isClientId.get(0).get("MYDT_INS_CD"); //접근토큰수신자기관코드
    						String jti         = "jjwt123"; //접근토큰식별자
    						Long expiredTime   = 1000 * 60L * 60L * 24L * 90L; //접근토큰유효시간 ( 90일 )
    						String scope       = (String) usrScopeCtt.get(0).get("MYDT_ATR_SCOP_CTT"); //scope
    						
    						LData data = new LData();
    						
    						data.put("iss",iss);
    						data.put("aud",aud);
    						data.put("jti",jti);
    						data.put("expiredTime",expiredTime);
    						data.put("scope",scope);
    						
    						String token = (String)BizCommand.execute("com.kbcard.ubd.pbi.token.TokenCommonPbc", "makeToken", data);

							input.put("mydt_tken_dtcd", "02"); // 마이데이터 접근토큰구분코드 ( 01 : 리프레시토큰 , 02 : 접근토큰)
							//접근토큰 저장
							int insertUsrAcResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertUsrAc", input);

							//리턴
							ContextUtil.setHttpResponseHeaderParam( "x-api-tran-id",  rsp_x_api_tran_id);
							result.set("token_type","Bearer"); // 접근토큰유형
							result.set("access_token",token); // 발급된 접근토큰
							result.set("expires_in",expiredTime); // 유효기간
							
    					}else {
    						//scope 유효하지않음
    						error = "invalid_scope";
    						error_description = rspMessageMap.get("idv_ac_isu.bad_request." + error);
    						//400 에러 log insert
//							errorLogInsert("I", result, input, redirect_uri, error, error_description);
    					}
    				}else {
    					error = "invalid_request";
    					error_description = rspMessageMap.get("idv_ac_isu.bad_request." + error);
    					
    					//400 에러 log insert
//						errorLogInsert("I", result, input, redirect_uri, error, error_description);
    				}
    				
    				
    				
    			}else {
    				//인가코드 유효하지 않음
    				error = "invalid_grant";
    				error_description = rspMessageMap.get("idv_ac_isu.bad_request." + error);
    				//400 에러 log insert
//					errorLogInsert(result, input, redirect_uri, error, error_description);
    			}
    			
    		}
    		
    	}catch (MissingResourceException e) {
    		//302 에러 log insert
//			errorLogInsert("I", result, input, redirect_uri, error, error_description);
    	}catch(UnsupportedEncodingException e) {
    		
    		error = "50001";
    		error_description = "시스템 장애가 발생하였습니다.";
    		
    		//HTTP 응답코드 셋팅
    		ContextUtil.setHttpResponseHeaderParam("status", "500");
    		
    		//에러코드
    		result.set( "error",  error);
    		//에러메시지
    		result.set( "error_description",  error_description);
    		
    		//인가코드요청 테이블 error insert
    		input.put("error", error);
    		input.put("error_description", error_description);
    		
    		BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfACEbc", "insertCnfCdDmd", input);
    		
    		
    	}
    	
    	return result;
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
    	
    }

}
