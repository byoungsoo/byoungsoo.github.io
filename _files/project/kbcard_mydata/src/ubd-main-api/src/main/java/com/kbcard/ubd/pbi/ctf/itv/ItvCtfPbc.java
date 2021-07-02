package com.kbcard.ubd.pbi.ctf.itv;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.lang.Integer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.certicom.security.cert.internal.x509.Base64;
import com.initech.core.util.FileUtil;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.RestHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.penta.sdk.bs.IssacBASE64;
import com.penta.sdk.bs.IssacCERTIFICATE;
import com.penta.sdk.bs.IssacContentInfoType;
import com.penta.sdk.bs.IssacHex;
import com.penta.sdk.bs.IssacPRIVATEKEY;
import com.penta.sdk.bs.IssacSDKException;
import com.penta.sdk.se.IssacSECONTEXT;
import com.penta.sdk.sg.IssacSG;
import com.penta.sdk.ucpid.IssacISPREQINFO;
import com.penta.sdk.ucpid.IssacPERSONINFOV2;
import com.penta.sdk.ucpid.IssacUCPIDREQUEST;
import com.penta.sdk.ucpid.IssacUCPIDREQUESTINFO;
import com.penta.sdk.ucpid.IssacUCPIDRESPONSE;
import com.penta.sdk.ucpid.IssacUCPIDConstant;
//import com.rsa.certj.provider.pki.URLDecoder;
import com.rsa.certj.spi.pki.PKIException;

import devon.batch.common.BatchUtil.JSONUtil;
import devon.batch.common.LStringUtil;
import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.exception.LExceptionOptionalInfo;
import devon.core.log.LLog;
import devon.core.util.context.LContextManager;
import devon.core.util.context.LContextUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;
import devonframework.front.channel.context.LActionContext;
import devonframework.service.message.LMessage;
import devonframework.service.message.LMessageUtil;

public class ItvCtfPbc {
	
	/**
	 * @logicalName 마이데이터 통합인증
	 * <STEP1>서비스앱의 통합인증 요청 -> 트랜잭션ID, 고객CI, 인증기관코드, Nonce값, 전자서명(Nonce포함)값 을 수신
	 * <STEP2>
	 * 
	 * <INPUT>
	 * 	(header)x-api-tran-id 				[거래고유번호] 
	  	(body)	tx_id						[트랜잭션ID]
	  			org_code					[기관코드]	(정보제공자)
	  			grant_type					[권한부여방식] 				password
	  			client_id					[클라이언트ID]
	  	        client_secret				[클라이언트Secret]
	  	        ca_code						[통합인증기관코드]
	  	        consent_type				[전자서명유형]				0:원문에서명 / 1:해시값에서명
	  	        consent_len					[consent항목길이]
	  	        consent						[전송요구내역]
	  	        username					[고객CI정보]
	  	        password_len				[password항목길이]
	  	        password					[전송요구내역전자서명]
	  	        auth_type					[본인확인이용여부]			0:본인확인기관이용 / 1:전자서명인증사업자이용
	  	        signed_person_info_req_len	[본인확인이용동의전자서명항목길이]
	  	        signed_person_info_req		[본인확인이용동의전자서명]
	  	        consent_nonce				[재전송공격방지정보1]
	  	        ucpid_nonce					[재전송공격방지정보2]
	  	        cert_tx_id					[인증사업자트랜잭션아이디]
	  	        service_id					[서비스번호]
	 * </INPUT>
	 * <OUTPUT>
	 * 	(header)x-api-tran-id 				[거래고유번호] 
	 * 	(body)	tx_id						[트랜잭션ID]
	 * 			token_type					[접근토큰유형]
	 * 			access_token				[접근토큰]
	 * 			expires_in					[접근토큰유효기간]
	 * 			refresh_token				[리프레시토큰]
	 * 			refresh_token_expires_in	[리프레시토큰유효기간]
	 * 			scope						[권한범위]
	 * </OUTPUT>
	 * <ERROR>
	 * 	(header)x-api-tran-id 				[거래고유번호]
	 * 	(body)	tx_id						[트랜잭션ID]
	 * 			error						[에러코드]
	 * 			error_description			[에러메시지]
	 * </ERROR>
	 * @exception LException
	 * @information 마이데이터 통합인증-002 API(API)
	 *              HTTP URL : https://openapi.mydata.or.kr/oauth/2.0/token
	 *              HTTP Method : POST
	 *              
	 *              통합인증을 통한 정보제공 API 호출용 접근토큰 발급
	 *              
	 */
	
	//1.서비스앱의 통합인증 요청 -> 트랜잭션ID, 고객CI, 인증기관코드, Nonce값, 전자서명(Nonce포함)값 을 수신
	//
	//user_name (CI)
	//tx_id
	//--------- signedDataList(json[]) ---------------------------------------------------------
	//signedDataList.orgCode (정보제공자 기관코드)
	//signedDataList.signedPersonInfoReq (base64 인코딩 된 전자서명값 : 본인확인이용동의 및 본인확인정보 / Nonce)
	//signedDataList.signedConsent (base64 인코딩 된 전자서명값 : 전송요구내역 / Nonce)
	//------------------------------------------------------------------------------------------
	//ca_org (고객인증서 발급기관)
	@SuppressWarnings("unchecked")
	public static LData getWhAuthReqData(LData input) throws LException {
		
		LLog.debug.println("ItvCtfPbc.getWhAuthReqData[마이데이터통합인증.통합인증요청] START ###" +  input);
		LLog.debug.println("SHSTest ### 통합인증 - 요청 데이터 Get ");
		
		LData result = new LData();			//결과 셋
		LData set_verify = new LData();		//인증서모듈 파라메터 셋
		LData set_certify = new LData();	//본인확인모듈 파라메터 셋
		
		String currentDateYMD = DateUtil.getCurrentDate("yyyyMMddHHmmss").substring(0, 8);
		String currentDateHM = DateUtil.getCurrentDate("yyyyMMddHHmmss");
		
		//거래고유번호(HTTP header 추출)
		//ContextUtil.setHttpRequestHeaderParam();
		String strApiTranId   = ContextUtil.getHttpRequestHeaderParam("x-api-tran-id");	//거래고유번호
		LLog.debug.println("SHSTest ### httpheader get " + strApiTranId);
		LLog.debug.println("SHSTest ### 시스템최종갱신식별자 get " + ContextUtil.getSDT_CHN_DTLS_BWK_DTCD());
		
		//트랜잭션ID
		String strTranId        = (String)input.get("tx_id");
		//기관코드
		String strOrgCode   = (String)input.get("org_code");
		//권한부여방식
		String strGrantType   = (String)input.get("grant_type");
		//클라이언트ID
		String strClientId       = (String)input.get("client_id");
		//클라이언트Secret
		String strClientScr       = (String)input.get("client_secret");
		//통합인증기관코드
		String strCaCode	    = (String)input.get("ca_code");
		
		//고객CI정보
		String strRcvUserName      = (String)input.get("username");
		//password항목길이
		String strPwLen      		= Integer.toString(input.getInt(("password_len")));
		//전송요구내역전자서명
		String strPwData      = (String)input.get("password");
		
		//본인확인이용여부
		String strAuthType      = Integer.toString(input.getInt(("auth_type")));	// 0 : 본인확인기관이용 / 1 : 전자서명인증사업자이용
		if("0".equals(strAuthType)) {
			//본인확인이용동의전자서명항목길이
			String strPersInfoLen      = Integer.toString(input.getInt(("signed_person_info_req_len")));
			//본인확인이용동의전자서명
			String strPersInfoData      = (String)input.get("signed_person_info_req");
			//재전송공격방지정보1
			String strConstNonce      = (String)input.get("consent_nonce");
			//재전송공격방지정보2
			String strUcpidNonce      = (String)input.get("ucpid_nonce");
			
			set_verify.put("signed_person_info_req", 	strPersInfoData);	//본인확인이용동의전자서명
			set_verify.put("consent_nonce", 			strConstNonce); 	//전송요구내역nonce
			set_verify.put("ucpid_nonce", 				strUcpidNonce);	//고객정보nonce
		} else if("1".equals(strAuthType)) {
			//consent항목길이
			String strConstLen      = Integer.toString(input.getInt(("consent_len")));
			//전송요구내역(consent)
			String strConstData      = (String)input.get("consent");
			//인증사업자트랜잭션아이디
			String strCertTranId      = (String)input.get("cert_tx_id");
			
			set_verify.put("consent", 					strConstData);	//전송요구내역전자서명
		}
		
		//전자서명유형
		String strConstType      = Integer.toString(input.getInt(("consent_type")));	// 0 : 전송요구내역 원문에 서명(본인확인기관) / 1 : 전송요구내역 해시값에 서명(전자서명인증사업자)
		
		//서비스번호
		String strServiceId      = (String)input.get("service_id");
		
		//
		//---------------- Service 수신 요청 데이터 End ----------------//
		
		//검증&본인인증 함수 input set
//		set_verify.put("tx_id", 					strTranId);	//트랜잭션ID
//		set_verify.put("x-api-tran-id", 			strApiTranId);	//거래고유번호
//		
//		set_verify.put("username", 					strRcvUserName);	//username - CI
//		set_verify.put("ca_code", 					strCaCode);		//strCaCode - 통합 인증기관코드
		
//		set_verify.put("consent", 					strConstData);	//전송요구내역전자서명
//		set_verify.put("signed_person_info_req", 	strPersInfoData);	//본인확인이용동의전자서명
//		set_verify.put("consent_nonce", 			strConstNonce); 	//전송요구내역nonce
//		set_verify.put("ucpid_nonce", 				strUcpidNonce);	//고객정보nonce
		
		//#####################################################################################
		//############ 마이데이터 사업자에서 송신한 TEST 데이터 생성 ##########################
		//#####################################################################################
		//
		//
		//1. 마이데이터서버에서 16byte(128bit) ucpidNonce와 consentNonce를 생성하고 Base64 url-safe인코딩 한다.
		try {
		String b64_ucpidNonce = "/";
		while(b64_ucpidNonce.contains("/") == true)
		{
			//byte[] ucpidNonce = IssacSECONTEXT.MakeRandom(16); 	//라이센스 문제로 변경해서 테스트해봐야 함
			Random rnd = new Random();
			byte[] ucpidNonce = new byte[16];
			rnd.nextBytes(ucpidNonce);
			b64_ucpidNonce = IssacBASE64.Encode(ucpidNonce);
		}
		
			String urlencode_b64_ucpidNonce = URLEncoder.encode(b64_ucpidNonce, "UTF-8");	//해당 Nonce를 어플리케이션에 전달한다.
			LLog.debug.println(b64_ucpidNonce);

		//consentNonce에 / 가 들어가면 나중에 JSON조합에 \/로 표시된다.
		String b64_consentNonce = "/";
		while(b64_consentNonce.contains("/") == true)
		{
			//byte[] consentNonce = IssacSECONTEXT.MakeRandom(16);	//라이센스 문제로 변경해서 테스트해봐야 함
			Random rnd = new Random();
			byte[] consentNonce = new byte[16];
			rnd.nextBytes(consentNonce);
			b64_consentNonce = IssacBASE64.Encode(consentNonce);
		}
		LLog.debug.println(b64_consentNonce);
			String urlencode_b64_consentNonce = URLEncoder.encode(b64_consentNonce, "UTF-8");	//해당 Nonce를 어플리케이션에 전달한다.
		
		//2. 마이데이터서버는 Base64 url-safe인코된 Nonce를 마이데이터앱으로 전송한다.
		
		//3. 마이데이터앱은 약관동의, 전송요구내역, Nonce등을 입력하여 전자서명 데이터를 생성한다.
		//3.1 사용자 공동인증서와 개인키 읽기 (현재는 정보인증 테스트용 공동인증서 사용)
		byte[] file_cert;
			file_cert = readToFile(".\\certs\\user\\signCert.der");
		
		IssacCERTIFICATE userCert = new IssacCERTIFICATE();
		userCert.Read_Memory(file_cert);
		byte[] file_prikey;
			file_prikey = readToFile(".\\certs\\user\\signPri.key");
		
		IssacPRIVATEKEY userPrikey = new IssacPRIVATEKEY();
		//개인키에 대한 비밀번호는 앱에서 인증서비밀번호 입력을 사용자에게 받아야 한다.
		userPrikey.Read_Memory(file_prikey, "signgate1!");

		//3.2 정보제공자 기관코드 입력(테스트로 정보인증 테스트 기관코드 입력)
		String org_code = "AMdNdQPZ0GET";
		
		//3.3 ucpidRequestInfo 세팅 및 signedPersonInfoReq 생성
		
		IssacUCPIDREQUESTINFO ucpid_reqInfo = new IssacUCPIDREQUESTINFO();
		 
		// 약관 동의 관련 내용 (아래 약관동의 내용은 샘플임, 각 사이트정책에 맞는 약관등의 내용입력 필요)
		String userAgreement =
		"인증서 기반 본인확인서비스는 인증기관(한국정보인증, 금융결제원, 한국무역정보통신, 한국증권전산, 한국전자인증)에 저장된 가입자 정보를 본인확인을 목적으로 가입자가 제공을\n" + 
		"요청한 [업체명]에게 제공하는 서비스로 '정보통신망이용촉진 등에 관한 법률' 및 '개인정보 보호법' 등에 따라 타인에게 제공 시 본인의 동의를 얻어야 하는 정보입니다.\n" + 
		"\n" + 
		"이에 본인은 아래 내용과 같이 가입자 정보의 제공에 동의하며 주민등록번호 보호 수단인 본인의 공인인증서 활용을 신청합니다.\n" + 
		"\n" + 
		"제 1 조 (제공할 가입자 정보 및 제공 대상)\n" + 
		"① 성명, 성별, 생년월일, 내/외국인정보, 연계정보(CI/DI)\n" + 
		"② 상기 제1조 1항의 가입자 정보는 본인이 전송을 요청한 [업체명]에게만 제공됩니다.\n" + 
		"\n" + 
		"제 2 조 (제공의 목적)\n" + 
		"[업체명]게 제공되는 가입자 정보는 본인확인 목적으로 제공됩니다.\n" + 
		"\n" + 
		"제 3 조 (이용자의 가입자 정보 보유 및 이용기간)\n" + 
		"본인의 가입자 정보는 제공받은 목적이 소멸되면 지체 없이 파기됩니다. 단, [업체명]의 정책에 따라 일정기간 보관 후 파기될 수 있습니다.\n" + 
		"인증서의 유효기간이 만료되거나 효력정지, 폐지 및 이용동의 철회 그리고 고객의 동의 없이는 [업체명]에게 정보를 제공하지 않으며, 이 경우 인증서 기반 본인확인서비스를 활용할 수 없습니다.";
		ucpid_reqInfo.SetUserAgreement(userAgreement);
		
		//요청하고자하는 본인확인 정보(실명, 성별, 국적, 생년월일, CI정보)
		ucpid_reqInfo.AddUserAgreeInfo(IssacUCPIDConstant.UserAgreeInfo_realName);
		ucpid_reqInfo.AddUserAgreeInfo(IssacUCPIDConstant.UserAgreeInfo_gender);
		ucpid_reqInfo.AddUserAgreeInfo(IssacUCPIDConstant.UserAgreeInfo_nationalInfo);
		ucpid_reqInfo.AddUserAgreeInfo(IssacUCPIDConstant.UserAgreeInfo_birthDate);
		ucpid_reqInfo.AddUserAgreeInfo(IssacUCPIDConstant.UserAgreeInfo_ci);
    
		//마이데터서비스 도메인 정보(테스트 시에는 펜타시큐리티 주소로 처리) 
		String ispUrlInfo = "www.pentasecurity.com";
		ucpid_reqInfo.SetIspUrlInfo(ispUrlInfo);
		
		//마이데이터서버로 부터 전송받은 ucpidNonce set
		//url-decoding 후 base64 decoding
		byte[] client_ucpid_nonce = IssacBASE64.Decode(URLDecoder.decode(urlencode_b64_ucpidNonce, "UTF-8"));
		LLog.debug.println(URLDecoder.decode(urlencode_b64_ucpidNonce, "UTF-8"));
		LLog.debug.println(IssacHex.Encode(client_ucpid_nonce, true));
		ucpid_reqInfo.SetUCPIDNonce(client_ucpid_nonce);
		
		//UCPIDRequestInfo(PersonInfoReq에 인증모듈 정보등을 추가)를 서명한 CMS SignedData 생성
		//LocalDateTime localDateTime = LocalDateTime.of(2021, 4, 30, 14, 44, 53);
        //Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
		//byte[] result = ucpid_reqInfo.GenSignedPersonInfoReq(signerPriKey, signerCert, Date.from(instant), IssacUCPIDConstant.NID_SHA256);
		Date sign_time = new Date();
		byte[] signedPersonInfoReq = ucpid_reqInfo.GenSignedPersonInfoReq(userPrikey, userCert, sign_time, IssacUCPIDConstant.NID_SHA256);
		//서명데이터 임시파일 저장
		try {
			writeToFile(signedPersonInfoReq, "penta_signedPersonInfoReq.ber");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//마이데이터서버전송을 위한 base64 encoding 후 url encoding
		String b64_signedPersonInfoReq = IssacBASE64.Encode(signedPersonInfoReq);		
		String urlencode_b64_signedPersonInfoReq = URLEncoder.encode(b64_signedPersonInfoReq, "UTF-8");
		
		//3.4 cosentInfo 세팅 및 signedConsent 생성
		// JSON 형태로 consetInfo 조합
		// {"consentNonce":"bgY0bSvBsYFUFIFxe1dvGg==","consent":"계좌정보"}
		String consent = "계좌정보";
		String client_consent_nonce = URLDecoder.decode(urlencode_b64_consentNonce, "UTF-8");
		// JSON조합
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("consent", consent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jsonObj.put("consentNonce", client_consent_nonce);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String consentInfo = jsonObj.toString();
		LLog.debug.println(consentInfo);
		//signedConsent 생성
		IssacSG signer = new IssacSG();
		sign_time = new Date();
		signer.MakeSignature_WithHashNid(consentInfo.getBytes(), userPrikey, userCert, sign_time, IssacSG.SHA256);
		//서명데이터에 contentInfo 추가
		IssacContentInfoType contentInfo = new IssacContentInfoType();
		byte[] signedConsent = contentInfo.AddContentInfoType(signer.getSignature(), 69);
		//서명데이터 임시파일 저장
		LLog.debug.println(new String(signer.GetOriginalMessage()));
		try {
			writeToFile(signedConsent, "penta_signedConsent.ber");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//마이데이터서버전송을 위한 base64 encoding 후 url encoding
		String b64_signedConsent = IssacBASE64.Encode(signedConsent);		
		String urlencode_b64_signedConsent = URLEncoder.encode(b64_signedConsent, "UTF-8");
		
		//4. 서명 인증서에서 caOrg 추출
		//사용자 공동인증서 IssuerName에서 o에 해당값만 파싱
		LLog.debug.println(userCert.GetIssuerName());
		StringTokenizer st = new StringTokenizer(userCert.GetIssuerName(), ",");
		st.nextToken();
		st.nextToken();
		StringTokenizer st1 = new StringTokenizer(st.nextToken(), "=");
		st1.nextToken();
		String caOrg = st1.nextToken();
        LLog.debug.println("발급기관 구분자(caOrg) : "+ caOrg);
        
        
        //5. 마이데이터 앱에서 마이데이터 서버로 org_code, signedPersonInfoReq, signedConsent, caOrg 로 전송하고 마이데이터 서버는 규격서 
        //통합인증-002 API 규격으로 ISP업체에 통합인증요청
		
		set_verify.put("tx_id", 					strTranId);	//트랜잭션ID
		set_verify.put("x-api-tran-id", 			strApiTranId);	//거래고유번호
		set_verify.put("username", 					strRcvUserName);	//username - CI
				
		set_verify.put("org_code", 					org_code);	//정보제공자 기관코드
		set_verify.put("signedPersonInfoReq", 		urlencode_b64_signedPersonInfoReq);
		set_verify.put("signedConsent", 			b64_signedConsent);
		set_verify.put("caOrg", 					caOrg);	//고객인증서 발급기관
		set_verify.put("consent_nonce", 			urlencode_b64_consentNonce); 	//전송요구내역nonce
		set_verify.put("ucpid_nonce", 				urlencode_b64_ucpidNonce);	//고객정보nonce

		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//############ 마이데이터 사업자에서 송신한 TEST 데이터 생성 END  #####################
		///////////////////////////////////////////////////////////////////////////////////////

		
		//--------------------------------------------------------------------------------------------
		//*CI 비교
		//
		//수신 된 CI로 DB조회해서 CI정보 있는지 확인
		//
		//고객CI값(DB조회)
		
		//조회쿼리에 맞춰 파라메터 set 필요
		//
		try {
			
			LData trsSignedConst = new LData();
			
			LMultiData cnfCustCi = BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "selectCnfCustCi", set_verify);
			
			if(cnfCustCi.size() > 0) {	//CI에 해당하는 고객이 존재
				
				LLog.debug.println("SHSTest ### 통합인증 - 수신 CI : " + strRcvUserName);
				LLog.debug.println("SHSTest ### 통합인증 - DB조회 CI : " + cnfCustCi.get(0).get("CI내용"));
				
				if(strRcvUserName.equals(cnfCustCi.get(0).get("CI내용")) ) {	//CI값이 서로 일치하면
					//CI 검증 성공
					LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 고객CI비교 성공 ### ");
					
					try {
						//*서명 된 전송요구내역 DB에 저장
						//
						//데이터 저장시각 등 나머지 데이터도 insert 필요?
						trsSignedConst.put("CI내용", strRcvUserName);
						trsSignedConst.put("username", cnfCustCi.get(0).get("CI내용"));
						//trsSignedConst.put("", frSvcConsent);
						//
						//전송요구내역 Insert
						//BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "insertSignedConst", trsSignedConst);
						//LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 전송요구내역 DB저장 성공 ### :: ");
						int con = BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "insertSignedConst", trsSignedConst);	//TEST중 - 삭제예정
						//LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - insertSignedConst ### :: " + con);
						
					} catch (LException e) {
						throw new LBizException("ErrXXXX", "전송요구내역 DB저장 실패");
					}
				} else {
					LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 고객CI비교 실패 ### ");
					throw new LBizException("SIGN002", "고객 CI가 일치하지 않습니다.");
				}
				
			} else {
				//Err_고객CI DB에 없음
				LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 고객CI 없음 ### ");
				throw new LBizException("SIGN001", "존재하지 않는 고객입니다.");
			}
	
		} catch (LException e) {
			return error_RespSet(strApiTranId, strTranId, "SIGN_001");
			//throw new LBizException("SIGN002", "고객 CI가 일치하지 않습니다.");
			
		}
		
		//HTTP 응답코드 셋팅
		//ContextUtil.setHttpResponseHeaderParam("status", "302");
		
		//인증서검증 함수로 이동
		//
		LData verifResult = verifyReqData(set_verify);
		
		//검증 응답값 : 
		//int iResCode = verifResult.getInt("res_code");
		String strResKey = verifResult.getKeyWithIndex(0).toString();
		String strResVal = verifResult.getString(0);
		LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수  ### " + strResKey);
		LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수  ### " + strResVal);
		//iResCode = 0;
		if(verifResult.containsKey("error")) {	//실패 응답
		//if(verifResult.size() > 0) {	//성공 응답이면
			LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 실패 ### " + verifResult);
			return error_RespSet(strApiTranId, strTranId, verifResult.getString("error_description"));
			
		} else {
			//성공
			LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 - 성공응답 ### " + verifResult);
			//return error_RespSet(strApiTranId, strTranId, "SIGN_115");
		}
		//*인증서 검증 완료
		
		
		set_certify.put("username", strRcvUserName);	//username - CI
		set_certify.put("", 					"");	//username - CI
		
		//본인확인 모듈 함수로 이동
		//
		LData certiResult = verifyCertSelf(set_verify);
		String cert_res_code = certiResult.getString("res_code");
		String cert_res = certiResult.getString(0);
		//cert_res_code = "A0000";
		if(certiResult.size() > 0) {
		//if(cert_res_code.equals("A0000")) {	//성공 응답이면
			LLog.debug.println("SHSTest ### 통합인증 - 본인확인(모듈)함수 - 성공응답 ### " + cert_res_code);
			LLog.debug.println("SHSTest ### 통합인증 - 본인확인(모듈)함수 - 성공응답 ### " + cert_res);
			
		} else {
			//실패 응답
			return error_RespSet(strApiTranId, strTranId, "SIGN_130");
		}
		//*본인확인 완료
		
		//
		//모듈에서 SignedConsent를 JSON타입으로 복호화해 받고, 각 정보를 추출해야 함. 
		//
		//target_info[]
		
		
		
		//-------------------------------------------------
		//* 인증서검증 & 본인확인 최종성공 result set & Return
		//
		//어느 모듈 함수에서 어떤 응답값이 추출되는지 확인이 필요
		//
		// -> 접근토큰생성 함수를 거쳐서 토큰값을 받아와야 함.
		//
		
		
		//DataConvertUtil.exchangeJsonToLData();
		
		
		
		
		
		//----------- 최종 응답 http header set -------------------------------------------//
		
		//ContextUtil.setHttpResponseHeaderParam("status", "302");
		ContextUtil.setHttpResponseHeaderParam("x_api_tran_id", strApiTranId);	//성공시에도 set
		
		//----------- 최종 응답 result set -------------------------------------------//
		
		result.setString("lastRes_tx_id", strTranId);	//트랜잭션ID
		//result.setInt("expires_in", certiResult.getInt("expires_in"));	//접근토큰유효기간(초 단위 Integer로 표시)
		result.set("token_type", "test");
		result.set("access_token", "test");
		result.set("expires_in", "test");
		result.set("refresh_token", "test");
		result.set("refresh_token_expires_in", "test");
		result.set("scope", "test");
		//----------------------------------------------------------------------------//
		LLog.debug.println("SHSTest ### 최종 응답값 result : [ " + result + " ]");
		
		
		
		return result;

	}
	
	
	//2.전자서명 검증모듈을 통해 전자서명 검증 (각 순서는 변경 가능)
	//
	//1) CI 비교 - 자사(국민카드)고객 중 일치하는 CI 확인
	//2) 허용 인증서 여부 검증 - 인증서 식별정보 확인
	//3) 전자서명 서명시간이 유효한지 확인 / 수신 된 Nonce와 전자서명에 포함 된 Nonce값의 비교
	//4) 전자서명 검증 - 두개 전자서명 자체를 검증
	//5) 본인확인 전자서명과 전송요구내역 전자서명에 사용 된 인증서가 서로 동일한지 확인
	/////////////////
	//- 필요 Data -
	//username - CI
	//strCaCode - 통합 인증기관코드
	//전송요구내역전자서명
	//본인확인이용동의전자서명
	//고객정보nonce
	//전송요구내역nonce
	@SuppressWarnings("deprecation")
	public static LData verifyReqData(LData ld) throws LException {
		
		LLog.debug.println("SHSTest ### 통합인증 - 인증서 검증(모듈)함수 START ### ");
		LLog.debug.println("SHSTest ### 검증모듈함수 input : [ " + ld + " ]");
		
		LData result = new LData();	//결과값 리턴 셋
		
		LData modResult = new LData();	//모듈 응답
		LData header = new LData();		//모듈 인터페이스 헤더 셋
		LData trsSignedConst = new LData();	//DB 파라메터 셋
		
		String frScvTx_id		= (String) ld.get("tx_id");	//트랜잭션ID
		String frScv_Api_Tx_id	= (String) ld.get("x_api_tran_id");	//거래고유번호
		//String frSvcCI 			= (String) ld.get("username");	//서비스수신 CI
		//String frSvcCacode 		= (String) ld.get("ca_code");	//통합 인증기관코드
		//String frSvcConsent 	= (String) ld.get("consent");	//전송요구내역전자서명
		//String frSvcPersData 	= (String) ld.get("signed_person_info_req");	//본인확인이용동의전자서명
		//String frSvcConsNonce 	= (String) ld.get("consent_nonce");	//전송요구내역nonce
		//String frSvcUcpidNonce	= (String) ld.get("ucpid_nonce");	//고객정보nonce
		
		String frSvcCacode 			= (String) ld.get("caOrg");	//통합 인증기관코드
		String frSvcConsent 		= (String) ld.get("signedConsent");	//전송요구내역전자서명
		String frSvcPersData 		= (String) ld.get("signedPersonInfoReq");	//본인확인이용동의전자서명
		String frSvcConsNonce 		= (String) ld.get("consent_nonce");	//전송요구내역nonce
		String frSvcUcpidNonce		= (String) ld.get("ucpid_nonce");	//고객정보nonce
		
		//*전자서명 검증
		//
		//-인증서 식별정보 확인하기
		//
		//인증서 식별값 - OID 이
		//인증서정보 issuer DN - o : 인증기관 식별값에 해당하는 목록에 존재하는지 확인
		//
		//-전자서명 서명시간 확인 및 수신 Nonce와 (복호화 된)전자서명내 Nonce 대조
		//전자서명 두 가지(consent / ucpid)에 대해 모두 수행
		//
		//-전자서명을 검증
		//
		//-각 전자서명에 사용 된 인증서 식별정보가 같은지 확인

			
		// ISP서버에서 마이데이터서버로 온 전자서명데이터에 대해 검증
		// 1. signedPersonInfoReq 전자서명 검증
		IssacSG signedPersonInfoReq_verifyer = new IssacSG();
		//서명데이터에 contenInfo 제거
		IssacContentInfoType contentInfo_rev = new IssacContentInfoType();
		
		//통합인증API-002로 확보한 signedPersonInfoReq 정보를 입력
		byte[] isp_signedPersonInfoReq = null;
		try {
			isp_signedPersonInfoReq = IssacBASE64.Decode(URLDecoder.decode(frSvcPersData));
			byte[] rev_content_isp_signedPersonInfoReq = contentInfo_rev.DeleteContentInfoType(isp_signedPersonInfoReq, 69);
			
			//서명검증	        
			signedPersonInfoReq_verifyer.setSignature(rev_content_isp_signedPersonInfoReq);
			
		} catch (Exception e) {
			// Person 전자서명 검증 실패
			e.printStackTrace();
			LLog.debug.println("signedPersonInfoReq 검증실패 UCPID_100 ");
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "UCPID_100");	//(signedPersonInfo) 전자서명 검증에 실패하였습니다.
		}
		
		//서명검증 0이면 성공
		int verify_result = signedPersonInfoReq_verifyer.VerifySignature();
		LLog.debug.println("signedPersonInfoReq 검증결과 : " + verify_result);
		if(verify_result == 0) {
			//signedPersonInfoReq 서명검증성공
			LLog.debug.println("SHSTest ### signedPersonInfoReq 서명검증성공 ### ");
			result.set("res_code", 0);	//성공응답 set
			result.put("res_code", "0");
		} else {
			LLog.debug.println("signedPersonInfoReq 검증실패 UCPID_100 ");
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "UCPID_100");	//(signedPersonInfo) 전자서명 검증에 실패하였습니다.
		}
		
		// 1-1. sigendPersonInfo와 UCPIDNonce 비교
		//contenInfo를 제거하지 않은 서명 내의 Nonce와 전달받은 Nonce가 일치하는 지 확인
		byte[] b64_UcpidNonce;
		try {
			b64_UcpidNonce = IssacBASE64.Decode(URLDecoder.decode(frSvcUcpidNonce));	//전달받은 UcpidNonce 복호화
			
			if (IssacUCPIDREQUESTINFO.VerifyUCPIDNonce(isp_signedPersonInfoReq, b64_UcpidNonce))
			{
				LLog.debug.println("ucpidNonce compare success");
				LLog.debug.println("SHSTest ### signedPersonInfoReq Nonce검증성공 ### ");
				result.set("res_code", 0);	//성공응답 set
				result.put("res_code", "0");
			} else {
				LLog.debug.println("ucpidNonce compare fail");
				return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "UCPID_122");	//(UCPID)NONCE 검증에 실패하였습니다.
			}
			
		} catch (Exception e) {
			// UCPIDNonce 검증 실패
			e.printStackTrace();
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "UCPID_122");	//(UCPID)NONCE 검증에 실패하였습니다.
		}
		
		
		// 2. signedConsent 전자서명 검증
		IssacSG signedConsent_verifyer = new IssacSG();
		//통합인증API-002로 확보한 signedConsent 값을 입력 정보를 입력
		//서명데이터에 contenInfo 제거
		try {
			byte[] isp_signedConsent = contentInfo_rev.DeleteContentInfoType(IssacBASE64.Decode(URLDecoder.decode(frSvcConsent)), 69);
			signedConsent_verifyer.setSignature(isp_signedConsent);
		} catch (Exception e) {
			// Consent 전자서명 검증 실패
			e.printStackTrace();
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "SIGN_100");	//(signedConsent)전자서명 검증에 실패하였습니다.
		}
		
		//서명검증 0이면 성공
		verify_result = signedConsent_verifyer.VerifySignature();
		LLog.debug.println("signedConsent 검증결과 : " + verify_result);
		if(verify_result == 0) {
			//signedConsent 서명검증성공
			LLog.debug.println("SHSTest ### signedConsent 서명검증성공 ### ");
			result.set("res_code", 0);	//성공응답 set
			result.put("res_code", "0");
		} else {
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "SIGN_115");	//인증서 검증에 실패하였습니다.
		}
		
		// 2-1. sigendConsentInfo 서명내 원문을 추출하여 Nonce비교
		//서명문의 원문 Nonce와 전달받은 Nonce가 일치하는 지 확인
		String signedConsent_org_msg = new String(signedConsent_verifyer.GetOriginalMessage());	//서명 암호화 된 메세지 그대로 비교
		LLog.debug.println(signedConsent_org_msg);
		if (signedConsent_org_msg.contains(frSvcConsNonce))
        {
        	LLog.debug.println("consentNonce compare success");
        	LLog.debug.println("SHSTest ### signedConsent Nonce검증성공 ### ");
			result.set("res_code", 0);	//성공응답 set
			result.put("res_code", "0");
        } else {
        	LLog.debug.println("consentNonce compare fail");
        	return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "SIGN_122");	//(CONSENT)NONCE 검증에 실패하였습니다.
        }
		
		//result.set("tx_id", frScvTx_id);	//트랜잭션 ID
		
		LLog.debug.println("SHSTest ### signedConsent 서명검증성공 ### " + result.getString(0).toString());
		
		return result;

	}
	
	
	//3.본인확인 요청 모듈을 통해 인증기관에 고객 본인확인 요청
	//
	//본인확인 요청메세지(UCPIDReqeust) 생성, 인증기관(코스콤, 금융결제원 등)으로 요청 (Nonce 포함) - 모듈이 처리?
	//본인확인 응답값(UCPIDResponse) - 응답코드(status)확인 / 본인확인정보(CI 등), Nonce값 수신
	//서버용 인증서로 수신메세지 복호화, 인증서로 전자서명 검증 후 각 값 추출 -> 모듈이 처리? 확인 필요
	//응답값에서 추출한 CI와 고객DB 추출한 CI 비교
	//Nonce값 비교
	//오류가 없다면 본인인증 완료
	public static LData verifyCertSelf(LData input) throws LException {
		LLog.debug.println("SHSTest ### 통합인증 - 본인확인요청 - START ### ");
		LLog.debug.println("SHSTest ### 본인확인요청 input : [ " + input + " ]");
		
		LData result = new LData();
		LData modResult = new LData();	//모듈 응답
		LData certInput = new LData();
		
		String frSvcCI 			= (String) input.get("username");	//서비스수신 CI
		String frSvcCacode 		= (String) input.get("ca_code");	//통합 인증기관코드
		String frSvcConsent 	= (String) input.get("consent");	//전송요구내역전자서명
		String frSvcPersData 	= (String) input.get("signed_person_info_req");	//본인확인이용동의전자서명
		String frSvcConsNonce 	= (String) input.get("consent_nonce");	//고객정보nonce
		String frSvcUcpidNonce	= (String) input.get("ucpid_nonce");	//전송요구내역nonce
		String frScvTx_id		= (String) input.get("tx_id");	//트랜잭션ID
		
		String frScv_Api_Tx_id	= (String) input.get("x_api_tran_id");	//거래고유번호
		
		
		//-- 본인확인 모듈 준비 --//
		//
		
		//인증기관에 보낼 UCPIDReqeust 생성
		//ISP서버용 암호용도 공동인증서를 읽는다.
		//Files.readAllBytes(".\\certs\\isp\\kmCert.der");
		//byte[] km_server_file_cert = readToFile(".\\certs\\isp\\kmCert.der");
		
		try {
			byte[] km_server_file_cert = readToFile(".\\certs\\isp\\kmCert.der");
	        IssacCERTIFICATE km_serverCert = new IssacCERTIFICATE();
	        km_serverCert.Read_Memory(km_server_file_cert);

	        byte[] sign_server_file_cert = readToFile(".\\certs\\isp\\signCert.der");
	        IssacCERTIFICATE sign_serverCert = new IssacCERTIFICATE();
	        sign_serverCert.Read_Memory(sign_server_file_cert);
			

	        byte[] sign_server_file_prikey = readToFile(".\\certs\\isp\\signPri.key");
			IssacPRIVATEKEY sign_serverPrikey = new IssacPRIVATEKEY();
			//개인키에 대한 비밀번호는 앱에서 인증서비밀번호 입력을 사용자에게 받아야 한다. (X)
			sign_serverPrikey.Read_Memory(sign_server_file_prikey, "signgate1!");	//발급받은 공동인증서 비밀번호 입력
	        
	        IssacUCPIDREQUEST ispucpidReq = new IssacUCPIDREQUEST();
	        ispucpidReq.SetCpCode(frSvcCacode);	//<- 통합인증기관코드
	        
	        //인증서에서 기관키식별자에 KeyID를 추출할 수 있는 함수가 추가적으로 필요하다.
	  		ispucpidReq.SetIssuerKeyHash(sign_serverCert.GetIssuerKeyID());
	  		LLog.debug.println(IssacHex.Encode(sign_serverCert.GetIssuerKeyID(), true));
		     /*byte[] sample_issuerKeyHash = {
            (byte) 0x8F, (byte) 0x6F, (byte) 0xF6, (byte) 0xE8, (byte) 0x89, (byte) 0x71, (byte) 0x6A, (byte) 0x02,
            (byte) 0x03, (byte) 0xDB, (byte) 0x7A, (byte) 0x2A, (byte) 0x8E, (byte) 0x55, (byte) 0xA8, (byte) 0x53,
            (byte) 0xE7, (byte) 0xD0, (byte) 0xCC, (byte) 0x33
			 };*/
			 //ispucpidReq.SetIssuerKeyHash(sample_issuerKeyHash);

			//ISPReqInfo 생성
			IssacISPREQINFO ispreqInfo = new IssacISPREQINFO();
			//ispreqInfo 세팅
			//String sample_cpRequestNumber = "MydataCode_AuthorityCACode_YYMMDDHHMMSS_1615775844189";	//? sample이 아닌 cp request 확인 필요
			String cpRequestNumber = frScvTx_id;//트랜잭션ID (74자리)
			ispreqInfo.SetCpRequestNumber(cpRequestNumber);	//cpRequestNumber set
			ispreqInfo.SetIspKmCert(km_serverCert);
			ispreqInfo.SetSignedPersonInfoReq(IssacBASE64.Decode(URLDecoder.decode(frSvcPersData)));
			
			String b64_ucpidNonce = "/";
			byte[] ucpidNonce = null;
			while(b64_ucpidNonce.contains("/") == true)
			{
				ucpidNonce = IssacSECONTEXT.MakeRandom(16);
				b64_ucpidNonce = IssacBASE64.Encode(ucpidNonce);
			}
			String urlencode_b64_ucpidNonce = URLEncoder.encode(b64_ucpidNonce, "UTF-8");
			
			ispreqInfo.SetUCPIDNonce(ucpidNonce);	//client_ucpid_nonce : 별도 생성된 ucpidNonce set

			
			//isp 서버인증서로 전자서명
			Date sign_time = new Date();
			ispucpidReq.SetContentIspReqInfo(ispreqInfo, sign_serverPrikey, sign_serverCert, sign_time, IssacSG.SHA256);
			
			byte[] isp_ucpid_request = ispucpidReq.Write_Memory();
			//서명데이터 임시파일 저장
			writeToFile(isp_ucpid_request, "penta_UCPIDRequest.ber");
			
			//메모리릭 방지를 위한 메모리 환원
			//userCert.close();
			//userPrikey.close();
			//ucpid_reqInfo.close();
	        ispreqInfo.close(); 
	        ispucpidReq.close();
			sign_serverCert.close();
			sign_serverPrikey.close();
			
			
			// http POST 파라메터 조합하여 인증기관에 UCPIDRequest 전달
			//UCPIDReqeust 길이값 4byte 생성
		    byte[] byteLength = intToByteArray(isp_ucpid_request.length);
		    LLog.debug.println(isp_ucpid_request.length);
		    LLog.debug.println(byteLength.length);
		    
		    //길이 4byte+UCPIDRequest byte[];
		    byte[] send_packet_data = new byte[byteLength.length+isp_ucpid_request.length];
		    System.arraycopy(byteLength, 0, send_packet_data, 0, byteLength.length);
		    System.arraycopy(isp_ucpid_request, 0, send_packet_data, byteLength.length, isp_ucpid_request.length);
		    LLog.debug.println(send_packet_data.length);
		    
			String hostname = "121.254.188.161"; 	//통합인증기관의 IP, Port 확인 및 변경 필요
			int port = 9090; 
			
			//IP주소와 포트번호를 입력하여 Socket 통신을 시작합니다.
            Socket sock = new Socket(hostname, port);
            
            OutputStream os = sock.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            
            dos.write(send_packet_data);
            dos.flush();
            
            //수신데이터도 길이4yte + UCPIDResponse byte[]
           
            InputStream is = sock.getInputStream();
            
            
            byte[] receive_packet_data = new byte[1024*10];
            is.read(receive_packet_data);
            
            byte[] response_length = new byte[4];
            System.arraycopy(receive_packet_data, 0, response_length, 0, 4);
            int response_len = byteArrayToInt(response_length);
            LLog.debug.println(response_len);
            byte[] isp_ucpid_response = new byte[response_len];
            System.arraycopy(receive_packet_data, 4, isp_ucpid_response, 0, response_len);
            writeToFile(isp_ucpid_response, "penta_UCPIDResponse.ber");
                  
            dos.close();
            os.close();

            is.close();
            sock.close();
			
            ///////////////////////////////////////////////////////
            //수신받은 UCPIDResponse 복호 및 서명검증, 원문추출
    		byte[] km_server_prikey_file = readToFile(".\\certs\\isp\\kmPri.key");
    		isp_ucpid_response = readToFile("penta_UCPIDResponse_old.ber");
    		IssacPRIVATEKEY km_serverPrikey = new IssacPRIVATEKEY();
    		km_serverPrikey.Read_Memory(km_server_prikey_file, "signgate1!");
    		//정보인증 ucpid_response 읽기
    		LLog.debug.println("ucpid_response length is " + isp_ucpid_response.length);
    		LLog.debug.println();		        
    		//UCPIDResponse 객체에 담기
    		IssacUCPIDRESPONSE ispucpidRes = new IssacUCPIDRESPONSE();
    		ispucpidRes.Read_Memory(isp_ucpid_response);
    		//isp 암호용 인증서와 개인키를 이용하여 UCPIDResponse를 복호화여 내부의 CI 등 정보를 추출한다.
    		IssacPERSONINFOV2 personinfo2 = ispucpidRes.GetPersonInfoV2(km_serverCert, km_serverPrikey);
    		
    		//-------------------------------------------------------------------------------------------
    		LLog.debug.println("SHSTest ### 통합인증 - 본인확인 수신 UCPID 원문 추출 ### ");
    		LLog.debug.println("GetBirthDate : "+personinfo2.GetBirthDate());	//고객 생년월일
    		LLog.debug.println("GetCertDn : "+personinfo2.GetCertDn());	//DN?
    		LLog.debug.println("GetCi : "+personinfo2.GetCi());	//CI 1
    		LLog.debug.println("GetCi2 : "+personinfo2.GetCi2());	//CI 2
    		LLog.debug.println("GetCiUpdate : "+personinfo2.GetCiUpdate());	//CI Update?
    		LLog.debug.println("GetCpCode : "+personinfo2.GetCpCode());	//인증기관코드
    		LLog.debug.println("GetCpRequestNumber : "+personinfo2.GetCpRequestNumber());	//인증요청 번호
    		LLog.debug.println("GetDi : "+personinfo2.GetDi());	//인증서 DI	(서비스별 이용자 중복가입 확인정보)
    		LLog.debug.println("GetGender : "+personinfo2.GetGender());	//고객 국적정보
    		LLog.debug.println("GetRealName : "+personinfo2.GetRealName());	//고객 실명
    		LLog.debug.println("GetUCPIDNonce : "+IssacHex.Encode(personinfo2.GetUCPIDNonce(), true));	//UCPIDNonce
    		
    		//ber파일로 persioninfo저장
    		byte[] isp_personinfo = personinfo2.Write_Memory();
    		writeToFile(isp_personinfo, "penta_PersonInfo.ber");
            
    		//result set
    		result.set("getCtfCi", personinfo2.GetCi());
    		result.set("getCtfCi", personinfo2.GetCi());
    		
    		
    		//메모리릭 방지를 위한 메모리 환원
	        km_serverCert.close();
	        km_serverPrikey.close(); 
	        ispucpidRes.close();
	        personinfo2.close();
			
		} catch (IOException e1) {
			// 파일 로드 에러 - 인증서파일 경로 문제 / 로드 문제 / 효력 문제
			e1.printStackTrace();
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "SIGN_115");	//인증서 검증에 실패하였습니다.
		} 
		catch (Exception e) {
			// 전자서명 검증 실패
			e.printStackTrace();
			return error_RespSet(frScv_Api_Tx_id, frScvTx_id, "SIGN_100");	//인증서 검증에 실패하였습니다.
		}

		
		result.set("", modResult.getString(""));
		
		return result;
		
	}
	
	//4.
	//Scope / asset Table에 적재
	//TBUBDC015, TBUBDC016
	//
	public int setDbScop(LData input) {
		
		LData set_Scop = new LData();
		//
		set_Scop.put("", "");
		
		try {
			
			int con = BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "insertScope", set_Scop);
			
		} catch (LException e) {
			// 
			
		} finally {
			
		}
		return 0;
		
	}
	
	
	//5.접근토큰 발급 및 제공
	//
	//접근토큰을 생성해 Table에 적재 TBUBDC023 TBUBDC024(?)
	//마이데이터사업자측으로 접근토큰 제공 (통합인증-002 API에 대한 응답)
	public LData issueAccToken(LData input) throws LException {
		
		//서비스 응답 전문 Set
		//
		//param : scopt / org_code
		// -> Token 발급 가능한지?
		//
		
		return input;
	
	}
	
	//접근토큰요청내역+전송요구내역 DB 적재 TBUBDC110
	@SuppressWarnings("unchecked")
	public void insReqTokenCtt(LData set_inst) throws LBizException {
		//LData set_inst = new LData();		//접근토큰요청내역 DB Insert param 셋
		set_inst.put("x_api_tran_id", set_inst.get(""));
		set_inst.put("tran_req_ymd", set_inst.get(""));
		set_inst.put("trs_tgt_code", "t");
		set_inst.put("ctf_dt_code", "t");
		set_inst.put("acctk_req_code", "t");
		set_inst.put("ofer_orgcode", "t");
		set_inst.put("comp_orgcode", "t");
		set_inst.put("cp_orgcode", "t");
		set_inst.put("ca_code", "t");
		set_inst.put("req_author_type_name", "t");
		set_inst.put("client_id", "t");
		set_inst.put("client_secret", "t");
		set_inst.put("ahthcode_cont", "t");
		set_inst.put("callback_url", "t");
		set_inst.put("req_refrtk_cont", "t");
		set_inst.put("req_tk_cont", "t");
		set_inst.put("tk_div_code", "t");
		set_inst.put("itv_tran_num", "t");
		set_inst.put("itv_sign_type_code", "t");
		set_inst.put("const_len", 1);
		set_inst.put("const", "t");
		set_inst.put("username", "t");
		set_inst.put("signed_const_len", 1);
		set_inst.put("signed_const", "t");
		set_inst.put("cert_yn", "t");
		set_inst.put("signed_cert_len", 1);
		set_inst.put("sifned_cert_cont", "t");
		set_inst.put("const_nonce", "t");
		set_inst.put("pers_info_nonce", "t");
		set_inst.put("acctk_type", "t");
		set_inst.put("res_tk_cont", "t");
		set_inst.put("res_refrkt_cont", "t");
		set_inst.put("acctk_expire_in", 11111111);
		set_inst.put("refrtk_expire_in", 22222222);
		set_inst.put("scope_cont", "t");
		set_inst.put("acctk_process", "t");
		//set_inst.put("sys_last_udt_id", "7119");	//하드코딩 - 추후 확인 후 변경예정
		try {
			//TBUBDC110 - UBD접근토큰요청내역 Insert
			BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "insertAcsTkReq", set_inst);	//TEST중
		} catch (Exception e) {
			LLog.debug.println("SHSTest ### 통합인증 - 접근토큰요청내역 Insert 실패 ### ");
			throw new LBizException("DB001", "접근토큰요청내역 Insert 실패");
		}
		
	}
	
	//마이데이터거래이력 DB 적재 TBUBDC100
	//
	public void insMydyTranCtt(LData set_inst) throws LBizException {
		
		try {
			//TBUBDC100 - UBD인증및지원API거래내역 Insert
			BizCommand.execute("com.kbcard.ubd.ebi.ctf.itv.ItvCtfEbc", "insertAcsTkReq", set_inst);
		} catch (LException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//error 처리
	public static LData error_RespSet(String strAPItranID, String strTranId, String strErrorDscr) throws LException {
		LData result = new LData();
		//http 응답 header error set
		ContextUtil.setHttpResponseHeaderParam("status", "400");	//error 응답 - 상태코드
		ContextUtil.setHttpResponseHeaderParam("x-api-tran-id", strAPItranID);	//
		//ContextUtil.setHttpResponseHeaderParam("error", "invalid_request");	//error 응답 - error
		//ContextUtil.setHttpResponseHeaderParam("error_description", strErrorDscr);	//error 응답 - error_description
		
		//body error set
		result.set("tx_id", strTranId);
		result.set("error", "invalid_request");
		result.set("error_description", strErrorDscr); //각 에러시 수신한 error code
		
		return result;
		
	}
	
	
	private static void writeToFile(byte[] data, String file) throws FileNotFoundException, IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
            out.close();
        }
    }
    
    private static byte[] readToFile(String file) throws FileNotFoundException, IOException {
        try (FileInputStream in = new FileInputStream(file)) {
        	
        	int bufSize = in.available(); //읽어올 수 있는 byte의 수를 반환한다.
        	byte[] buf = new byte[bufSize]; //bufSize 만큼의 byte 배열을 선언한다.
            in.read(buf);
            in.close();
            
            return buf;
        }
    }
    
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    
    private static int byteArrayToInt(byte[] bytes) {
    	final int size = Integer.SIZE / 8;
    	ByteBuffer buff = ByteBuffer.allocate(size);
    	final byte[] newBytes = new byte[size];
    	for (int i = 0; i < size; i++) {
    		if (i + bytes.length < size) {
    			newBytes[i] = (byte) 0x00;
    		} else {
    			newBytes[i] = bytes[i + bytes.length - size];
    		}
    	}
    	buff = ByteBuffer.wrap(newBytes);
    	buff.order(ByteOrder.BIG_ENDIAN); // Endian에 맞게 세팅
    	return buff.getInt();
    }

}
