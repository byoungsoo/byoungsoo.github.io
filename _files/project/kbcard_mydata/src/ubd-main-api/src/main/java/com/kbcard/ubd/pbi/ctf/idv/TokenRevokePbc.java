package com.kbcard.ubd.pbi.ctf.idv;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.util.StringUtil;

/** 
 * IdvCtfPbc
 * 
 * @logicalname  : 개별인증 접근토큰삭제
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
public class TokenRevokePbc {
	

    /**
     * - 개별인증 인가코드요청
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
    public LData accessTokenRevokeDmd(LData input) throws LException {
    	LData result = new LData();
		

    	ContextUtil.setHttpRequestHeaderParam();
    	
    	LLog.debug.println("accessTokenRevokeDmd !!!!!!!!!!!! ");

		// 거래고유번호 ( 헤더 )
		String rsp_x_api_tran_id   = ContextUtil.getHttpRequestHeaderParam("x-api-tran-id");
		
		//정보제공자 기관코드
		String org_code        = (String)input.get("org_code");
		//삭제대상 접근토큰
		String token   = (String)input.get("token");
		//클라이언트 아이디
		String client_id       = (String)input.get("client_id");
		//callback client_secret
		String client_secret    = (String)input.get("client_secret");

		
		//거래내역 테이블 ( TBUBDC100) 마이데이터 전문요청내용
		String mydt_tlg_dmd_ctt = "";
		input.put("mydt_tlg_dmd_ctt", mydt_tlg_dmd_ctt);
		//거래내역 테이블 ( TBUBDC100) 마이데이터 전문응답내용
		String mydt_tlg_rsp_ctt = "";
		input.put("mydt_tlg_rsp_ctt", mydt_tlg_rsp_ctt);
		
		String error = "";
		String error_description = "";

		//<step1> 응답메시지 properties 가 담길 Map
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

			//<step1> 파라미터 유효성 검증
			if (  StringUtil.trimNisEmpty(rsp_x_api_tran_id) 
					|| StringUtil.trimNisEmpty(org_code) 
					|| StringUtil.trimNisEmpty(client_id)
					|| StringUtil.trimNisEmpty(client_secret) 
					|| StringUtil.trimNisEmpty(token) 
				) 
			{
				error = "invalid_request";
				error_description = rspMessageMap.get("idv_ac_isu.bad_request." + error);

				//400 에러 log insert
//				errorLogInsert(result, input, redirect_uri, error, error_description);

			}else {
				LMultiData isClientId = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfEbc", "isClientId", input);
	
				//<step3> client_id 유효성 체크
				if(isClientId.size() > 0) {
					

    				//기발행 접근토큰

					input.put("mydt_tken_dtcd", "02"); // 마이데이터 접근토큰구분코드 ( 01 : 리프레시토큰 , 02 : 접근토큰)
					LMultiData usrAt = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfAcEbc", "selectUsrATHis", input);
					
    				if(usrAt.size() > 0 && ( usrAt.get(0).get("AT_DSCR_YN") == null || usrAt.get(0).get("AT_DSCR_YN").equals("N"))) {

						//접근토큰내역테이블 접근토큰, 리프레시토큰 insert
//						input.put("accs_tken_dscr_rsn_dtcd","03"); // 폐기사유구분코드 ( 03 : 폐기요청 )
//						int acHisInsertResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfAcEbc", "insertUsrAcHis", input);
						
						//기발급 접근토큰 폐기
						int acDelResult = BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfAcEbc", "updateUsrAcHis", input);

						ContextUtil.setHttpResponseHeaderParam( "x-api-tran-id",  rsp_x_api_tran_id);
    					result.set("rsp_code","00000"); // 폐기성공
    					result.set("rsp_msg","접근토큰 및 리프레시토큰을 폐기하였습니다.");
    				}else {
						ContextUtil.setHttpResponseHeaderParam( "x-api-tran-id",  rsp_x_api_tran_id);
    					result.set("rsp_code","99999"); // 폐기하고자 하는 토큰이 유효하지 않은 경우
    					result.set("rsp_msg","폐기하고자 하는 토큰이 유효하지 않습니다.");
    				}
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
	    	
			BizCommand.execute("com.kbcard.ubd.ebi.ctf.idv.IdvCtfAcEbc", "insertCnfCdDmd", input);
			
			
		}
		
    	return result;
    }
    
}
