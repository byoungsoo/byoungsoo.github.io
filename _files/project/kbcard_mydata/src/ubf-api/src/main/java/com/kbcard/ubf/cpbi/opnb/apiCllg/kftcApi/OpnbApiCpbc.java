package com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.kbcard.frw.cmd.RestHttpAdaptor;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.common.util.EnterpriseRestApiUtil;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubf.cpbi.cmn.AccessToken;
import com.kbcard.ubf.cpbi.cmn.AsyncRunner;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ApiErrCode;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ApiSeq;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.AuthInfo;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.core.parameter.LinkConnectionParameter;
import devonenterprise.ext.core.exception.ExtensionExceptionPitcher;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

/**
 * opnbApiCpbc
 * 
 * @logicalname : 오픈뱅킹APICpbc
 * @author : 김정화
 * @since : 2021-04-30
 * @version : 1.0
 * @see :
 * 
 *      << 개정이력(Modification Information) >>
 *
 *      수정일 수정자 수정내용 --------------- --------- ---------------------------
 *      2021-04-30 김정화 최초 생성
 *
 */

public class OpnbApiCpbc {
	/** 금결원 API 정보 */
	static LMultiData apiInfList;	
	/** AccessToken */
	static AccessToken accessToken = AccessToken.createInstance();

	static {
			/** 토큰 조회/설정 */
			try {
				setInit();
			} catch (LBizException e) {
				e.printStackTrace();
			}
	}
	
	public static void setInit() throws LBizException {
		setApiInfList(); // 금결원API정보 목록조회/설정
		setAccessToken(); // 토큰 조회/설정
	}
	
	/**
	 * <pre>
	 * - 금결원API정보 목록조회/설정
	 * 
	 * 1. API를 호출하기 위한 정보를 DB에서 조회하여 목록값을 설정한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본, UBF오픈뱅킹API수수료기본
	 * </pre>
	 * 
	 * @method setApiInfList
	 * @method(한글명) 금결원API 정보 조회/설정
	 * @param
	 * @return
	 * @throws LBizException
	 */
	public static void setApiInfList() throws LBizException {
		/** Variable Set */
		if(LNullUtils.isNone(apiInfList) ){
			/** 오픈뱅킹API정보조회IN */
			LData selectAPIInfIn = new LData();			
			/** 오픈뱅킹G/W정보조회 */
			LinkConnectionParameter connectionParameter;
			URL targetUrl = null;

	        try {
	            connectionParameter = new LinkConnectionParameter("OGW","OGW_SYNC");
	            targetUrl = EnterpriseRestApiUtil.getTargetUrl(connectionParameter, selectAPIInfIn);

				//selectAPIInfIn.setString("APIGW도메인", AuthInfo.API_GW_DOMAIN.getCode());
				selectAPIInfIn.setString("APIGW도메인", targetUrl.toString());
				
				apiInfList = BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "selectAPIInfList",
						selectAPIInfIn);
	        } catch (LException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9997.getCode(), ObsErrCode.ERR_9997.getName());
			}

			LLog.debug.println("apiInfList::", apiInfList);
		}
	}
	
	/**
	 * <pre>
	 * - 토큰 조회/설정
	 * 
	 * 1. API를 호출하기 위한 AccessToken을 DB에서 조회하여 값을 설정한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹토큰기본
	 * </pre>
	 * 
	 * @method setAccessToken
	 * @method(한글명) 토큰 조회/설정
	 * @param
	 * @return
	 * @throws LBizException
	 */
	public static void setAccessToken() throws LBizException {
		/** Variable Set */
		if (StringUtils.isEmpty(accessToken.getAccessToken())) {

			LData input = new LData(); // 토큰조회입력
			input.setString("오픈뱅킹토큰최종여부", "Y");
			input.setString("오픈뱅킹토큰범위구분코드", AuthInfo.TKEN_SCOPE_SA.getCode());

			/** accessToken 조회 */
			LData accessTokenNo = new LData();
			try {
				accessTokenNo = BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc",
						"selectAccessToken", input);
			} catch (LException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9999.getCode(), ObsErrCode.ERR_9999.getName());
				
				// LData header = new LData();
				// LData body = new LData();
				// LData result = new LData();
				// 
				// header.setString("client_id", AuthInfo.CLIENT_ID.getCode());
				// header.setString("client_secret", AuthInfo.CLIENT_SECRET.getCode());
				// header.setString("scope", AuthInfo.TKEN_SCOPE_SA.getCode());
				// header.setString("grant_type", AuthInfo.DEFAULT_GRANT_TYPE.getCode());
				// 
				// /** 오픈뱅킹API정보조회 */
				// LData apiInf = getApiInf(ApiSeq.API_0001.getSeq());
				// RestHttpAdaptor restHttpAdaptor = new RestHttpAdaptor(apiInf);
				// 
				// try {
				// 	result = restHttpAdaptor.sendMessage(header, body);
				// } catch (LException e1) {
				// 	e1.printStackTrace();
				// }
			}

			accessToken.setAccessToken(accessTokenNo.getString("오픈뱅킹접근토큰내용"));
			accessToken.setExpYms(accessTokenNo.getString("오픈뱅킹토큰만료일시"));
			accessToken.setTokenRngDtcd(accessTokenNo.getString("오픈뱅킹토큰범위구분코드"));
			accessToken.setOpnbUtzInsCd(accessTokenNo.getString("오픈뱅킹이용기관코드"));

			LLog.debug.println("accessToken::", accessToken.getAccessToken());
		}
	}
	
	/**
	 * <pre>
	 * - API 정보조회
	 * 
	 * 1. 금결원API정보 목록에서 특정 API정보를 조회한다.
	 * 
	 * </pre>
	 * 
	 * @method getApiInf
	 * @method(한글명) API 정보조회
	 * @param int 오픈뱅킹URL일련번호
	 * @return LData API정보
	 * @throws LBizException
	 */
	public static LData getApiInf(int urlSeq) {
		/** Variable Set */
		LData result = new LData();
		
		/** 오픈뱅킹API정보조회 */
        if(!LNullUtils.isNone(apiInfList) ){
            for(int idx=0; idx < apiInfList.getDataCount(); idx++){
                LData apiInf = apiInfList.getLData(idx);
                
                if(apiInf.getInt("오픈뱅킹URL일련번호") == urlSeq) {
                	result = apiInf;                	
                	break;
                }
            }		
        }
        
        return result;
	}
	
	/**
	 * <pre>
	 * - 연동 호출
	 * 
	 * 1. EAI 또는 API G/W를 호출한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method sendAPIData
	 * @method(한글명)EAI/APIGateway 데이터 전송
	 * @param LData API정보
	 * @param LData 전송바디데이터
	 * @param String 인터페이스ID
	 * @param boolean APIGateway호출여부
	 * @return LData 응답결과
	 * @throws LException 
	 * @throws LBizException
	 */
	@Deprecated
	public LData sendAPIData(LData apiInf, LData body, String interfaceId, boolean gwYn) throws LBizException {
		/** Variable Set */
		LData result = new LData();
		LData header = new LData(); // API호출 HEADER
		
		if("N".equals(apiInf.getString("오픈뱅킹API콘텐츠JSON유형"))) {
			if(ApiSeq.API_0001.getSeq() == apiInf.getInt("오픈뱅킹URL일련번호")) { //토큰발급
				header.setString("client_id", body.getString("client_id"));
				header.setString("client_secret", body.getString("client_secret"));
				header.setString("scope", body.getString("scope"));
				header.setString("grant_type", "client_credentials");
			}else if(ApiSeq.API_0002.getSeq() == apiInf.getInt("오픈뱅킹URL일련번호")) {//토큰폐기
				header.setString("client_id", body.getString("client_id"));
				header.setString("client_secret", body.getString("client_secret"));
				header.setString("access_token", body.getString("access_token"));
			}
		} else {
			header.setString("Authorization", accessToken.getAccessToken());
		}

		header.setString("chn_dtls_bwk_dtcd", body.getString("채널세부업무구분코드"));
		body.remove("오픈뱅킹사용자고유번호");
		body.remove("채널세부업무구분코드");
		
		if(LDataUtil.isKeys(body, "user_ci")
		  || LDataUtil.isKeys(body, "user_seq_no")) {
			LLog.debug.println("user_ci, user_seq_no 검색");	
			
			//CI_CTT
			LData ciCtt = new LData();
			
			if(LDataUtil.isKeys(body, "user_ci")) {				
				header.setString("ci_ctt", body.getString("user_ci"));
			}else if(LDataUtil.isKeys(body, "user_seq_no")) {
				LData selectCiCttIn = new LData();
				selectCiCttIn.setString("오픈뱅킹사용자고유번호", body.getString("user_seq_no"));
				
		        try {				
		        	ciCtt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "selectCiCtt",
		        			selectCiCttIn);
		        	
		        	header.setString("ci_ctt", ciCtt.getString("CI내용"));
		        } catch (LException e) {
		        	LLog.debug.println("user_ci 정보 없음 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					e.printStackTrace();
				}	
			}
		}

		LLog.debug.println("sendAPIMessage >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", interfaceId);
		try {
			if(gwYn) {								
				/** 오픈뱅킹API호출 */
				LLog.debug.println("API G/W 호출 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", body);
		        RestHttpAdaptor restHttpAdaptor = new RestHttpAdaptor(apiInf);
		        result = restHttpAdaptor.sendMessage(header, body);
			} else {
				/** 대외EAI 호출  */
				LLog.debug.println("대외EAI 호출 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", body);
				LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
				result = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, interfaceId, body); // 대상타입, 인터페이스ID, Body데이터
				
			}			
		} catch (LException e) {
			e.printStackTrace();
			if(gwYn) {
				throw new LBizException(ObsErrCode.ERR_9995.getCode(), ObsErrCode.ERR_9995.getName());
			} else {
				throw new LBizException(ObsErrCode.ERR_9996.getCode(), ObsErrCode.ERR_9996.getName());
			}
		}
		
		LLog.debug.println("API 호출결과::", result);
		return result;
			
	}
	
	/**
	 * <pre>
	 * - 연동 호출
	 * 
	 * 1. API G/W를 호출한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method sendAPI
	 * @method(한글명)APIGateway 데이터 전송
	 * @param LMultiData API호출정보s
	 * @return LMultiData 응답결과s
	 * @throws LException 
	 * @throws LBizException
	 */
	public LMultiData sendAPI(LMultiData apiCalls) throws LException {
		LMultiData tmpLMultiData = new LMultiData();
		
        if(!LNullUtils.isNone(apiCalls) ){
            for(int idx=0; idx < apiCalls.getDataCount(); idx++){
            	LData tmpLData = apiCalls.getLData(idx);
            	LData tmpLBodyData= apiCalls.getLData(idx).getLData("apiBody");
            	tmpLData.set("apiInf", apiCalls.getLData(idx).getLData("apiInf"));
            	tmpLData.set("apiHeader", getAPIHeader(apiCalls.getLData(idx).getLData("apiInf"), apiCalls.getLData(idx).getLData("apiBody")));            	
            	tmpLBodyData.remove("오픈뱅킹사용자고유번호");
            	tmpLBodyData.remove("채널세부업무구분코드");
            	tmpLData.set("apiBody", tmpLBodyData);
            	tmpLMultiData.addLData(tmpLData);
            }
        }

		return AsyncRunner.httpAsyncExecute(tmpLMultiData);
	}
	
	/**
	 * <pre>
	 * - 연동 호출
	 * 
	 * 1. API G/W를 호출한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method sendAPI
	 * @method(한글명)APIGateway 데이터 전송
	 * @param LData API정보
	 * @param LData 전송바디데이터
	 * @return LData 응답결과
	 * @throws LException 
	 * @throws LBizException
	 */
	public LData sendAPI(LData apiInf, LData body) throws LException {
		/** Variable Set */
        LData header = getAPIHeader(apiInf, body);
		return AsyncRunner.httpAsyncExecute(apiInf, header, body);
	}
	
	public LData getAPIHeader(LData apiInf, LData body) throws LException {
		/** Variable Set */
        LData header = new LData();
        
		if("N".equals(apiInf.getString("오픈뱅킹API콘텐츠JSON유형"))) {
			if(ApiSeq.API_0001.getSeq() == apiInf.getInt("오픈뱅킹URL일련번호")) { //토큰발급
				header.setString("client_id", body.getString("client_id"));
				header.setString("client_secret", body.getString("client_secret"));
				header.setString("scope", body.getString("scope"));
				header.setString("grant_type", "client_credentials");
			}else if(ApiSeq.API_0002.getSeq() == apiInf.getInt("오픈뱅킹URL일련번호")) {//토큰폐기
				header.setString("client_id", body.getString("client_id"));
				header.setString("client_secret", body.getString("client_secret"));
				header.setString("access_token", body.getString("access_token"));
			}
		} else {
			header.setString("Authorization", accessToken.getAccessToken());
		}
		
		header.setString("chn_dtls_bwk_dtcd", body.getString("채널세부업무구분코드"));
		body.remove("오픈뱅킹사용자고유번호");
		body.remove("채널세부업무구분코드");
		
		if(LDataUtil.isKeys(body, "user_ci")
		  || LDataUtil.isKeys(body, "user_seq_no")) {
			LLog.debug.println("user_ci, user_seq_no 검색");	
			
			//CI_CTT
			LData ciCtt = new LData();
			
			if(LDataUtil.isKeys(body, "user_ci")) {				
				header.setString("ci_ctt", body.getString("user_ci"));
			}else if(LDataUtil.isKeys(body, "user_seq_no")) {
				LData selectCiCttIn = new LData();
				selectCiCttIn.setString("오픈뱅킹사용자고유번호", body.getString("user_seq_no"));
				
		        try {				
		        	ciCtt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "selectCiCtt",
		        			selectCiCttIn);
		        	
		        	header.setString("ci_ctt", ciCtt.getString("CI내용"));
		        } catch (LException e) {
		        	LLog.debug.println("user_ci 정보 없음 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					e.printStackTrace();
				}	
			}
		}
		
		return header;
	}
	
	/**
	 * <pre>
	 * - 연동 호출
	 * 
	 * 1. EAI를 호출한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method sendAPIData
	 * @method(한글명)EAI 데이터 전송
	 * @param String 인터페이스ID
	 * @param LData 전송바디데이터
	 * @return LData 응답결과
	 * @throws LException 
	 * @throws LBizException
	 */
	public LData sendEAI(String interfaceId, LData body) throws LBizException {
		/** Variable Set */
		LData result = new LData();

		LLog.debug.println("sendEAI Message >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", interfaceId, body);
		try {
			/** EAI 호출  */
			LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
			result = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, interfaceId, body); // 대상타입, 인터페이스ID, Body데이터
		} catch (LException e) {
			e.printStackTrace();
			throw new LBizException(ObsErrCode.ERR_9996.getCode(), ObsErrCode.ERR_9996.getName());
		}
		LLog.debug.println("EAI 호출결과::", result);
		return result;
	}
	
	/**
	 * <pre>
	 * - 연동이력 등록
	 * 
	 * 1. 오픈뱅킹기관 거래내역을 등록한다.
	 * 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method regtAPICallPhs
	 * @method(한글명)오픈뱅킹기관 거래내역 등록
	 * @param LData 거래내역 Input
	 * @param LData 거래내역 Output
	 * @return LData 등록결과
	 * @throws LBizException
	 */
	public LData regtAPICallPhs(LData info, LData input, LData output, LData apiInf) throws LException {
		/** Variable Set */
		LData result = new LData(); // 오픈뱅킹기관거래내역등록 IN & 결과 Return
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(info.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		info.setString("시스템최초생성식별자", info.getString("채널세부업무구분코드"));
		info.setString("시스템최종갱신식별자", info.getString("채널세부업무구분코드"));
		info.setString("오픈뱅킹금융기관코드", StringUtil.initEmptyValue(info.getString("오픈뱅킹금융기관코드"), ""));
		
		result.setString("오픈뱅킹전문거래년월일", DateUtil.getCurrentDate());
		result.setInt("오픈뱅킹URL일련번호", apiInf.getInt("오픈뱅킹URL일련번호"));
		
		if(StringUtils.isNotEmpty(info.getString("user_seq_no"))) {
			result.setString("오픈뱅킹사용자고유번호", info.getString("user_seq_no"));	
		} else if(StringUtils.isNotEmpty(info.getString("오픈뱅킹사용자고유번호"))) {
			result.setString("오픈뱅킹사용자고유번호", info.getString("오픈뱅킹사용자고유번호"));	
		}

		if(StringUtils.isNotEmpty(output.getString("api_tran_id"))) {
			result.setString("오픈뱅킹API거래고유번호", output.getString("api_tran_id"));
		}
		
		if(StringUtils.isNotEmpty(output.getString("bank_tran_id"))) {
			result.setString("참가기관거래고유번호", output.getString("bank_tran_id"));
		}
		
		if(output.getLMultiData("res_list").size() > 0) {
			if(StringUtils.isNotEmpty(output.getLMultiData("res_list").getLData(0).getString("bank_tran_id"))) {
				result.setString("참가기관거래고유번호", output.getLMultiData("res_list").getLData(0).getString("bank_tran_id"));	
			}
		}
		
		if(StringUtils.isEmpty(result.getString("참가기관거래고유번호"))
			&& StringUtils.isNotEmpty(input.getString("bank_tran_id"))) {
			result.setString("참가기관거래고유번호", input.getString("bank_tran_id"));
		}

		result.setString("채널세부업무구분코드", info.getString("채널세부업무구분코드"));
		result.setString("대외기관전문송신일시", info.getString("대외기관전문송신일시"));
		
		if(StringUtils.isNotEmpty(info.getString("오픈뱅킹금융기관코드"))) {
			result.setString("오픈뱅킹금융기관코드", info.getString("오픈뱅킹금융기관코드"));	
		}
		
		result.setString("오픈뱅킹입력전문내용", DataConvertUtil.exchangeLDataToJson(input));
		
		if(StringUtils.isNotEmpty(output.getString("rsp_code"))) {
			result.setString("오픈뱅킹API응답구분코드", output.getString("rsp_code"));	
		}
		
		if(StringUtils.isNotEmpty(output.getString("rsp_message"))) {
			result.setString("오픈뱅킹API응답메시지내용", output.getString("rsp_message"));	
		}
		
		if(output.getLMultiData("res_list").size() > 0) {
			if(StringUtils.isNotEmpty(output.getLMultiData("res_list").getLData(0).getString("rsp_message"))) {
				result.setString("오픈뱅킹API응답메시지내용", output.getLMultiData("res_list").getLData(0).getString("rsp_message"));	
			}
		}
		
		if(StringUtils.isNotEmpty(output.getString("bank_code_tran"))) {
			result.setString("오픈뱅킹응답금융기관코드", output.getString("bank_code_tran"));	
		}
		
		if(output.getLMultiData("res_list").size() > 0) {
			if(StringUtils.isNotEmpty(output.getLMultiData("res_list").getLData(0).getString("bank_code_tran"))) {
				result.setString("오픈뱅킹응답금융기관코드", output.getLMultiData("res_list").getLData(0).getString("bank_code_tran"));	
			}
		}
		
		if(StringUtils.isNotEmpty(output.getString("bank_rsp_code"))) {
			result.setString("오픈뱅킹참가기관응답구분코드", output.getString("bank_rsp_code"));
		}
		
		if(output.getLMultiData("res_list").size() > 0) {
			if(StringUtils.isNotEmpty(output.getLMultiData("res_list").getLData(0).getString("bank_rsp_code"))) {
				result.setString("오픈뱅킹참가기관응답구분코드", output.getLMultiData("res_list").getLData(0).getString("bank_rsp_code"));	
			}
		}
		
		if(StringUtils.isNotEmpty(output.getString("bank_rsp_message"))) {
			result.setString("오픈뱅킹참가기관응답메시지내용", output.getString("bank_rsp_message"));	
		}
		
		if(output.getLMultiData("res_list").size() > 0) {
			if(StringUtils.isNotEmpty(output.getLMultiData("res_list").getLData(0).getString("bank_rsp_message"))) {
				result.setString("오픈뱅킹참가기관응답메시지내용", output.getLMultiData("res_list").getLData(0).getString("bank_rsp_message"));	
			}
		}
		
		result.setString("전문응답일시", info.getString("전문응답일시"));		
		result.setString("오픈뱅킹출력전문내용", DataConvertUtil.exchangeLDataToJson(output));
		result.setLong("오픈뱅킹이용기관수수료", apiInf.getLong("오픈뱅킹이용기관수수료"));
		result.setString("오픈뱅킹수수료대상여부", apiInf.getString("오픈뱅킹수수료대상여부"));		
		result.setString("시스템최초생성식별자", info.getString("시스템최초생성식별자"));
		result.setString("시스템최종갱신식별자", info.getString("시스템최종갱신식별자"));
		
		/** 거래내역등록 */
		try {
			BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "insertAPICallPhs", result);
		} catch (LException e) {
			e.printStackTrace();
			throw new LBizException(ObsErrCode.ERR_9998.getCode(), ObsErrCode.ERR_9998.getName());
		}
		
		return result;
	}

	/**
	 * <pre>
	 * - 연동 호출 및 연동이력 등록요청 
	 * 1. API G/W를 호출하고 오픈뱅킹기관 거래내역을 등록한다. 
	 * <관련 테이블> UBF오픈뱅킹기관거래내역
	 * </pre>
	 * 
	 * @method reqAPI
	 * @method(한글명)APIGateway 데이터 전송 및 오픈뱅킹기관 거래내역 등록
	 * @param LData 거래내역 Input
	 * @param LData 거래내역 apiBody
	 * @param LData 거래내역 apiInf
	 * @return LData 등록결과
	 * @throws LBizException
	 */
	public LData reqAPI(LData input, LData apiBody, LData apiInf) throws LException {
		/** Variable Set */
		LData apiResult = new LData(); //  API호출 Output		
		
		/** 오픈뱅킹API 호출 정보설정 */
		input.setString("대외기관전문송신일시", DateUtil.getDateTimeStr());

		try {
			apiResult = sendAPI(apiInf, apiBody);
		} catch(LBizException e) {			
			throw new LBizException(e.getCode(), e.getAddCtnt());			
		} finally {
			/** 전문응답일시 설정 */
			input.setString("전문응답일시", DateUtil.getDateTimeStr());
			
			if(HttpURLConnection.HTTP_OK != apiResult.getInt("response_code")) {
				LData apiResErr = apiResult.getLData("response");    						
				apiResErr.setString("rsp_code", "999");    						
				apiResErr.setString("rsp_message", apiResult.getLData("response").getString("Exception"));
				apiResult.set("response", apiResErr);
			}
			
			/** 오픈뱅킹기관거래내역등록 */
			this.regtAPICallPhs(input, apiBody, apiResult.getLData("response"), apiInf);
		}
		
		return apiResult;
	}
	
	/**
	 * <pre>
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * </pre>
	 *
	 * @serviceId UBF3010101
	 * @method issueTkenAPICall
	 * @method(한글명) 토큰발급 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData issueTkenAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.issueTkenAPICall[오픈뱅킹APICpbc.토큰발급 API호출] START ☆★☆☆★☆☆★☆" + input);

		/** Variable Set */
		LData result = new LData(); // 결과 Return		
		LData apiInf = getApiInf(ApiSeq.API_0001.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 토큰발급 API호출 Input
		LData apiResult = new LData(); // 토큰발급 API호출 Output		
		StringBuilder accessTokenSb = new StringBuilder();
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtils.isEmpty(input.getString("client_id"))) {
			// throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Client ID");
			input.setString("client_id", AuthInfo.CLIENT_ID.getCode());
		}
		
		if(StringUtils.isEmpty(input.getString("client_secret"))) {
			// throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Client Secret");
			input.setString("client_secret", AuthInfo.CLIENT_SECRET.getCode());
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			// throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Access Token 권한 범위");
			input.setString("scope", AuthInfo.TKEN_SCOPE_SA.getCode());
		}
		
		if(StringUtils.isEmpty(input.getString("grant_type"))) {
			// throw new LBizException(ObsErrCode.ERR_9001.getCode(), "권한부여 방식");
			input.setString("grant_type", AuthInfo.DEFAULT_GRANT_TYPE.getCode());
		}

		/** 오픈뱅킹API 호출 정보설정 */
		input.setString("대외기관전문송신일시", DateUtil.getDateTimeStr());
		input.setString("오픈뱅킹토큰발급일시", DateUtil.getDateTimeStr());
		
		/** 금융결제원 API-호출 */
		try {
			apiBody.setString("client_id", input.getString("client_id"));
			apiBody.setString("client_secret", input.getString("client_secret"));
			apiBody.setString("scope", input.getString("scope"));
			apiBody.setString("grant_type", input.getString("grant_type"));
			
			//apiResult = sendAPIData(apiInf, apiBody, "", true);
			apiResult = sendAPI(apiInf, apiBody);			
		} catch(LBizException e) {			
			throw new LBizException(e.getCode(), e.getAddCtnt());			
		} finally {			
			/** 전문응답일시 설정 */
			input.setString("전문응답일시", DateUtil.getDateTimeStr());

			if(HttpURLConnection.HTTP_OK != apiResult.getInt("response_code")) {
				LData apiResErr = apiResult.getLData("response");    						
				apiResErr.setString("rsp_code", "999");    						
				apiResErr.setString("rsp_message", apiResult.getLData("response").getString("Exception"));
				apiResult.set("response", apiResErr);
			}
			
			/** 오픈뱅킹기관거래내역등록 */
			this.regtAPICallPhs(input, apiBody, apiResult.getLData("response"), apiInf);
		}
		
		if(LDataUtil.isKeys(apiResult.getLData("response"), "rsp_code")) {
			result.setString("rsp_code", apiResult.getLData("response").getString("rsp_code"));
			result.setString("rsp_message", apiResult.getLData("response").getString("rsp_message"));
			
			// throw new LBizException(ObsErrCode.ERR_9994.getCode(), ObsErrCode.ERR_9994.getName());
		} else {
			/** 결과 설정 */
			accessTokenSb.append(apiResult.getLData("response").getString("token_type"));
			accessTokenSb.append(" ");
			accessTokenSb.append(apiResult.getLData("response").getString("access_token"));
			
			result.setString("오픈뱅킹토큰발급일시", DateUtil.getDateTimeStr());
			result.setString("오픈뱅킹접근토큰내용", accessTokenSb.toString());
			result.setLong("오픈뱅킹토큰만료기간", apiResult.getLData("response").getLong("expires_in"));
			result.setString("오픈뱅킹토큰만료일시", DateUtil.addSec(result.getString("오픈뱅킹토큰발급일시"), apiResult.getLData("response").getLong("expires_in")));
			result.setString("오픈뱅킹토큰범위구분코드", apiResult.getLData("response").getString("scope"));
			result.setString("오픈뱅킹토큰유형", apiResult.getLData("response").getString("token_type"));
			result.setString("오픈뱅킹이용기관코드", apiResult.getLData("response").getString("client_use_code"));
			result.setString("오픈뱅킹토큰최종여부", "Y");
			result.setString("대외기관전문송신일시", input.getString("대외기관전문송신일시"));
			result.setString("전문응답일시", input.getString("전문응답일시"));
			result.setString("시스템최초생성식별자", input.getString("채널세부업무구분코드"));
			result.setString("시스템최종갱신식별자", input.getString("채널세부업무구분코드"));
			
			/** accessToken 등록 */ 
			try {
				result.setString("오픈뱅킹토큰범위구분코드", AuthInfo.TKEN_SCOPE_SA.getCode());
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "updateAccessToken", result);
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.apiCllg.kftcApi.OpnbApiEbc", "insertAccessToken", result);
			} catch (LException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9993.getCode(), ObsErrCode.ERR_9993.getName());
			}

			accessToken.setAccessToken(result.getString("오픈뱅킹접근토큰내용"));
			accessToken.setExpYms(result.getString("오픈뱅킹토큰만료일시"));
			accessToken.setTokenRngDtcd(result.getString("오픈뱅킹토큰범위구분코드"));
			accessToken.setOpnbUtzInsCd(result.getString("오픈뱅킹이용기관코드"));
		}

		LLog.debug.println("OpnbApiCpbc.issueTkenAPICall[오픈뱅킹APICpbc.토큰발급 API호출] END ☆★☆☆★☆☆★☆" + result);
		
		return result;
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010102
	 * @method prcTkenDscrAPICall
	 * @method(한글명) 토큰폐기 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData prcTkenDscrAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.prcTkenDscrAPICall[오픈뱅킹APICpbc.토큰폐기 API호출] START ☆★☆☆★☆☆★☆" + input);

		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0002.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); //  API호출 Input
		LData apiResult = new LData(); //  API호출 Output		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtils.isEmpty(input.getString("client_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Client ID");
		}

		if(StringUtils.isEmpty(input.getString("client_secret"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Client Secret");
		}
		
		if(StringUtils.isEmpty(input.getString("access_token"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "Access Token");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.prcTkenDscrAPICall[오픈뱅킹APICpbc.토큰폐기 API호출] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF1010201
	 * @method retvPtcpInsStsAPICall
	 * @method(한글명) 참가기관상태조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvPtcpInsStsAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvPtcpInsStsAPICall[오픈뱅킹APICpbc.참가기관상태조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */		
		LData apiInf = getApiInf(ApiSeq.API_0043.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); //  API호출 Input
		LData apiResult = new LData(); //  API호출 Output		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
			
		/** 오픈뱅킹API 호출 정보설정 */
		input.setString("대외기관전문송신일시", DateUtil.getDateTimeStr());
	
		LLog.debug.println("OpnbApiCpbc.retvPtcpInsStsAPICall[오픈뱅킹APICpbc.참가기관상태조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF1010202
	 * @method retvFeeAPICall
	 * @method(한글명) 수수료조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvFeeAPICall(LData input) throws LException {		
		LLog.debug.println("OpnbApiCpbc.retvFeeAPICall[오픈뱅킹APICpbc.수수료조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0044.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 수수료조회 API호출 Input
		LData apiResult = new LData(); // 수수료조회 API호출 Output		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
		
		LLog.debug.println("OpnbApiCpbc.retvFeeAPICall[오픈뱅킹APICpbc.수수료조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");		
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF1010203
	 * @method retvSmmAPICall
	 * @method(한글명) 집계조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvSmmAPICall(LData input) throws LException {		
		LLog.debug.println("OpnbApiCpbc.retvSmmAPICall[오픈뱅킹APICpbc.집계조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0045.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 집계조회 API호출 Input
		LData apiResult = new LData(); // 집계조회 API호출 Output		

		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
		
		LLog.debug.println("OpnbApiCpbc.retvSmmAPICall[오픈뱅킹APICpbc.집계조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");		
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF1010204
	 * @method retvOdwTracLmAPICall
	 * @method(한글명) 출금이체한도 조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvOdwTracLmAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvOdwTracLmAPICall[오픈뱅킹APICpbc.출금이체한도 조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */		
		LData apiInf = getApiInf(ApiSeq.API_0046.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvOdwTracLmAPICall[오픈뱅킹APICpbc.출금이체한도 조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF1010205
	 * @method retvAbnFncTrDtcHisAPICall
	 * @method(한글명) 이상금융거래 탐지내역조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvAbnFncTrDtcHisAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvAbnFncTrDtcHisAPICall[오픈뱅킹APICpbc.이상금융거래 탐지내역조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0047.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvAbnFncTrDtcHisAPICall[오픈뱅킹APICpbc.이상금융거래 탐지내역조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010108
	 * @method retvUsrInfAPICall
	 * @method(한글명) 사용자정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvUsrInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvUsrInfAPICall[오픈뱅킹APICpbc.사용자정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0003.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); //  API호출 Input
		LData apiResult = new LData(); //  API호출 Output		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvUsrInfAPICall[오픈뱅킹APICpbc.사용자정보조회] END ☆★☆☆★☆☆★☆" + apiResult);		

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010109
	 * @method retvRgAccAPICall
	 * @method(한글명) 등록계좌조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvRgAccAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvRgAccAPICall[오픈뱅킹APICpbc.등록계좌조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */	
		LData apiInf = getApiInf(ApiSeq.API_0004.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input);
		LData apiResult = new LData();		

		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtils.isEmpty(input.getString("include_cancel_yn"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "해지계좌포함여부");
		}
		
		if(StringUtils.isEmpty(input.getString("sort_order"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "정렬순서");
		}
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
		
		LLog.debug.println("OpnbApiCpbc.retvRgAccAPICall[오픈뱅킹APICpbc.등록계좌조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}
	
	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId 
	 * @method retvAccIngAPICall
	 * @method(한글명) 통합계좌조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvAccIngAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvAccIngAPICall[오픈뱅킹APICpbc.통합계좌조회] START ☆★☆☆★☆☆★☆" + input);
		
		// String itfCd = "UAA_3KFTCS00025";
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0041.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input);
		LData apiResult = new LData();		

		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}		
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvRgAccAPICall[오픈뱅킹APICpbc.통합계좌조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010110
	 * @method chngAccInfAPICall
	 * @method(한글명) 계좌정보변경 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData chngAccInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.chngAccInfAPICall[오픈뱅킹APICpbc.계좌정보변경 API호출] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0005.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 계좌변경 API호출 Input
		LData apiResult = new LData(); // 계좌변경 API호출 Output		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtils.isEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtils.isEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "개설기관.표준코드");
		}
		
		if(StringUtils.isEmpty(input.getString("account_num"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "계좌번호");
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
		}
		
		if(StringUtils.isEmpty(input.getString("update_user_email"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "변경할 이메일주소");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
		
		LLog.debug.println("OpnbApiCpbc.chngAccInfAPICall[오픈뱅킹APICpbc.계좌정보변경 API호출] END ☆★☆☆★☆☆★☆" + apiResult);
		LLog.debug.println("OpnbApiCpbc.chngAccInfAPICall END ☆★☆☆★☆☆★☆");

		return apiResult.getLData("response");
		
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010111
	 * @method retvAccInfAPICall
	 * @method(한글명) 계좌정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvAccInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvAccInfAPICall[오픈뱅킹APICpbc.계좌정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0006.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}

		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvAccInfAPICall[오픈뱅킹APICpbc.계좌정보조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010112
	 * @method closAccAPICall
	 * @method(한글명) 계좌해지 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData closAccAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.closAccAPICall[오픈뱅킹APICpbc.계좌해지] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0007.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input);
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtils.isEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtils.isEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "개설기관.표준코드");
		}
		
		if(StringUtils.isEmpty(input.getString("account_num"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "계좌번호");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.closAccAPICall[오픈뱅킹APICpbc.계좌해지] END ☆★☆☆★☆☆★☆" + apiResult);
		return apiResult.getLData("response");
	}


	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010113
	 * @method regtCrdUsrAPICall
	 * @method(한글명) 카드사용자등록 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData regtCrdUsrAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.regtCrdUsrAPICall[오픈뱅킹APICpbc.카드사용자등록] START ☆★☆☆★☆☆★☆" + input);
		 
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0008.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
//		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
//		}
//		
//		if(StringUtil.trimNisEmpty(input.getString("bank_tran_id"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
//		}
//		
//		if(StringUtil.trimNisEmpty(input.getString("bank_code_std"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹개설기관코드");
//		}
//		
//		if(StringUtil.trimNisEmpty(input.getString("member_bank_code"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹회원금융회사코드");
//		}
//		
//		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
//		}
//		
//		if(StringUtils.isEmpty(input.getString("scope"))) {
//			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
//		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.regtCrdUsrAPICall[오픈뱅킹APICpbc.카드사용자등록] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010114
	 * @method chngCrdInfAPICall
	 * @method(한글명) 카드정보변경 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData chngCrdInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.chngCrdInfAPICall[오픈뱅킹APICpbc.카드정보변경] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0009.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹개설기관코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("member_bank_code"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹회원금융회사코드");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
		}

		if(StringUtil.trimNisEmpty(input.getString("update_user_email"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "변경할 이메일주소");
		}
			
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.chngCrdInfAPICall[오픈뱅킹APICpbc.카드정보변경] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010115
	 * @method retvCrdInfAPICall
	 * @method(한글명) 카드정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvCrdInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvCrdInfAPICall[오픈뱅킹APICpbc.카드정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0010.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹개설기관코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("member_bank_code"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹회원금융회사코드");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
		}
			
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvCrdInfAPICall[오픈뱅킹APICpbc.카드정보조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");		
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010116
	 * @method closCrdInqAPICall
	 * @method(한글명) 카드조회해지 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData closCrdInqAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.closCrdInqAPICall[오픈뱅킹APICpbc.카드조회해지] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0011.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹개설기관코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("member_bank_code"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹회원금융회사코드");
		}

		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.closCrdInqAPICall[오픈뱅킹APICpbc.카드조회해지] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010117
	 * @method prcUsrScsnAPICall
	 * @method(한글명) 사용자탈퇴 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData prcUsrScsnAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.prcUsrScsnAPICall[오픈뱅킹APICpbc.사용자탈퇴] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0016.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); //  API호출 Input
		LData apiResult = new LData(); //  API호출 Output
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		if(StringUtils.isEmpty(input.getString("client_use_code"))) {
			input.setString("client_use_code", accessToken.getOpnbUtzInsCd());
		}

		if(StringUtils.isEmpty(input.getString("user_seq_no"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자일련번호");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.prcUsrScsnAPICall[오픈뱅킹APICpbc.사용자탈퇴] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010118
	 * @method regtUsrAccAPICall
	 * @method(한글명) 사용자계좌등록 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData regtUsrAccAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.regtUsrAccAPICall START ☆★☆☆★☆☆★☆");		
		LLog.debug.println("OpnbApiCpbc.regtUsrAccAPICall[오픈뱅킹APICpbc.사용자계좌등록 API호출] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0017.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 사용자계좌등록 API호출 Input
		LData apiResult = new LData(); // 사용자계좌등록 API호출 Output
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_tran_id"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "거래고유번호(참가기관)");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("bank_code_std"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "오픈뱅킹개설기관코드");
		}
		
		if(StringUtil.trimNisEmpty(input.getString("register_account_num"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "등록계좌번호");
		}
		
		if(StringUtils.isEmpty(input.getString("user_info"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "생년월일");
		}
		
		if(StringUtils.isEmpty(input.getString("user_name"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "사용자명");
		}

		if(StringUtil.trimNisEmpty(input.getString("user_ci"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "CI(Connect Info)");
		}
		
		if(StringUtils.isEmpty(input.getString("scope"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "서비스구분");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
		
		LLog.debug.println("OpnbApiCpbc.regtUsrAccAPICall[오픈뱅킹APICpbc.사용자계좌등록 API호출] END ☆★☆☆★☆☆★☆" + apiResult);
		LLog.debug.println("OpnbApiCpbc.regtUsrAccAPICall END ☆★☆☆★☆☆★☆");

		return apiResult.getLData("response");	
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010119
	 * @method retvBlByFntcUtzNoAPICall
	 * @method(한글명) 핀테크 이용번호 별 잔액조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvBlByFntcUtzNoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvBlByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호계좌잔액조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0018.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // API호출 Input
		LData apiResult = new LData(); // API호출 Output
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvBlByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호계좌잔액조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010120
	 * @method retvBlByAcnoAPICall
	 * @method(한글명) 계좌번호 별 잔액조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvBlByAcnoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvBlByAcnoAPICall[오픈뱅킹APICpbc.계좌잔액조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0019.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // API호출 Input
		LData apiResult = new LData(); // API호출 Output
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}

		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvBlByAcnoAPICall[오픈뱅킹APICpbc.계좌잔액조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");
	}
	
	
	public LMultiData retvBlByAcnoAPICall(LMultiData inputList) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvBlByAcnoAPICall[오픈뱅킹APICpbc.고객보유계좌잔액목록조회] START ☆★☆☆★☆☆★☆" + inputList);
		
		/** Variable Set */
		LMultiData apiReqList = new LMultiData(); // API호출 Input
		LMultiData apiResList = new LMultiData(); // API호출 Output
		LMultiData apiResults = new LMultiData(); // API호출 Output
		LData apiInf = getApiInf(ApiSeq.API_0019.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		
		
        if(!LNullUtils.isNone(inputList) ){
            for(int idx=0; idx < inputList.getDataCount(); idx++){
            	LData input = inputList.getLData(idx);
                LData apiBody = LDataUtil.deepCopyLData(inputList.getLData(idx));
                LData apiReq = new LData();
        		/** INPUT Validation */
        		if(StringUtils.isEmpty(apiBody.getString("채널세부업무구분코드"))) {
        			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
        		}

        		input.setString("대외기관전문송신일시", DateUtil.getDateTimeStr());        		
        		apiReq.set("input", input);
        		apiReq.set("apiInf", apiInf);
        		apiReq.set("apiBody", apiBody);
        		apiReqList.addLData(apiReq);
            }    		
    		
    		/** 금융결제원 API-호출 */
    		try {
    			apiResList = sendAPI(apiReqList);
    		} catch(LBizException e) {			
    			throw new LBizException(e.getCode(), e.getAddCtnt());			
    		} finally {    			
    			if(!LNullUtils.isNone(apiResList) ){
    				for(int idx=0; idx < apiReqList.getDataCount(); idx++){
    					LData apiRes = apiResList.getLData(idx);

    					if(HttpURLConnection.HTTP_OK != apiRes.getInt("response_code")) {
    						LData apiResErr = apiRes.getLData("response");    						
    						apiResErr.setString("rsp_code", "999");    						
    						apiResErr.setString("rsp_message", apiRes.getLData("response").getString("Exception"));    						
    						apiResults.addLData(apiResErr);
    						ExtensionExceptionPitcher.createExceptionMessageWithoutThrow(ApiErrCode.ERR_A0009);
    					} else {
    						apiResults.addLData(apiRes.getLData("response"));	
    					}
						    							
						LData tmpInput = apiReqList.getLData(idx).getLData("input"); 
						/** 전문응답일시 설정 */
						tmpInput.setString("전문응답일시", DateUtil.getDateTimeStr());    							
						/** 오픈뱅킹기관거래내역등록 */
    	    			this.regtAPICallPhs(tmpInput, apiReqList.getLData(idx).getLData("apiBody"), apiResList.getLData(idx).getLData("response"), apiInf);					
    				}
    			}
    		}
        }
		
		return apiResults;
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010121
	 * @method retvTnhsByFntcUtzNoAPICall
	 * @method(한글명) 핀테크 이용번호 별 거래내역조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvTnhsByFntcUtzNoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvTnhsByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크 이용번호 별 거래내역조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0020.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();
				
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvTnhsByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크 이용번호 별 거래내역조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010122
	 * @method retvTnhsByAcnoAPICall
	 * @method(한글명) 계좌번호 별 거래내역조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvTnhsByAcnoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvTnhsByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 거래내역조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0021.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input);
		LData apiResult = new LData();
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
	
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvTnhsByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 거래내역조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010123
	 * @method retvAccRlnmAPICall
	 * @method(한글명) 계좌실명조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvAccRlnmAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvAccRlnmAPICall[오픈뱅킹APICpbc.계좌실명조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0022.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 계좌실명조회 API호출 Input
		LData apiResult = new LData(); // 계좌실명조회 API호출 Output	
				
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvAccRlnmAPICall[오픈뱅킹APICpbc.계좌실명조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010124
	 * @method retvRmtrInfAPICall
	 * @method(한글명) 송금인정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvRmtrInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvRmtrInfAPICall[오픈뱅킹APICpbc.송금인정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0023.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); // 송금인정보조회 API호출 Input
		LData apiResult = new LData(); // 송금인정보조회 API호출 Output
				
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);

		LLog.debug.println("OpnbApiCpbc.retvRmtrInfAPICall[오픈뱅킹APICpbc.송금인정보조회] END ☆★☆☆★☆☆★☆" + apiResult);
		
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010125
	 * @method retvRcevAPICall
	 * @method(한글명) 수취조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvRcevAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvRcevAPICall[오픈뱅킹APICpbc.수취조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0024.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();
				
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvRcevAPICall[오픈뱅킹APICpbc.수취조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010126
	 * @method tnsfOdwByFntcUtzNoAPICall
	 * @method(한글명) 핀테크이용번호 별 출금이체 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData tnsfOdwByFntcUtzNoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.tnsfOdwByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호 별 출금이체] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */		
		LData apiInf = getApiInf(ApiSeq.API_0025.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
		
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
			
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.tnsfOdwByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호 별 출금이체] END ☆★☆☆★☆☆★☆" + apiResult);
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010127
	 * @method tnsfOdwByAcnoAPICall
	 * @method(한글명) 계좌번호 별 출금이체 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData tnsfOdwByAcnoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.tnsfOdwByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 출금이체] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */		
		LData apiInf = getApiInf(ApiSeq.API_0026.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호	
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("wd_bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)

		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.tnsfOdwByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 출금이체] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010128
	 * @method tnsfMrvByFntcUtzNoAPICall
	 * @method(한글명) 핀테크이용번호 별 입금이체 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData tnsfMrvByFntcUtzNoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.tnsfMrvByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호 별 입금이체] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0027.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.tnsfMrvByFntcUtzNoAPICall[오픈뱅킹APICpbc.핀테크이용번호 별 입금이체] END ☆★☆☆★☆☆★☆" + apiResult);
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010129
	 * @method tnsfMrvByAcnoAPICall
	 * @method(한글명) 계좌번호 별 입금이체 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData tnsfMrvByAcnoAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.tnsfMrvByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 입금이체] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */	
		LData apiInf = getApiInf(ApiSeq.API_0028.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.tnsfMrvByAcnoAPICall[오픈뱅킹APICpbc.계좌번호 별 입금이체] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010130
	 * @method retvTracRstAPICall
	 * @method(한글명) 이체결과조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvTracRstAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvTracRstAPICall[오픈뱅킹APICpbc.이체결과조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0031.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();
					
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvTracRstAPICall[오픈뱅킹APICpbc.이체결과조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010131
	 * @method dmndFndsRtunAPICall
	 * @method(한글명) 자금반환청구 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData dmndFndsRtunAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.dmndFndsRtunAPICall[오픈뱅킹APICpbc.자금반환청구] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0032.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.dmndFndsRtunAPICall[오픈뱅킹APICpbc.자금반환청구] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역
	 * 
	 * <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT> 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010132
	 * @method retvFndsRtunRstAPICall
	 * @method(한글명) 자금반환결과 조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvFndsRtunRstAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvLstCrdCtgAPICall[오픈뱅킹APICpbc.자금반환결과 조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0033.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvLstCrdCtgAPICall[오픈뱅킹APICpbc.자금반환결과 조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010133
	 * @method retvLstCrdCtgAPICall
	 * @method(한글명) 카드목록조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvLstCrdCtgAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvLstCrdCtgAPICall[오픈뱅킹APICpbc.카드목록조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0034.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvLstCrdCtgAPICall[오픈뱅킹APICpbc.카드목록조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010134
	 * @method retvCrdBasInfAPICall
	 * @method(한글명) 카드기본정보 조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvCrdBasInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvCrdBasInfAPICall[오픈뱅킹APICpbc.카드기본정보 조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */	
		LData apiInf = getApiInf(ApiSeq.API_0035.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("wd_bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvCrdBasInfAPICall[오픈뱅킹APICpbc.카드기본정보 조회] END ☆★☆☆★☆☆★☆" + apiResult);
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010135
	 * @method retvCrdBilBasInfAPICall
	 * @method(한글명) 카드청구기본 정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvCrdBilBasInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvCrdBilBasInfAPICall[오픈뱅킹APICpbc.카드청구기본 정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0036.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("wd_bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvCrdBilBasInfAPICall[오픈뱅킹APICpbc.카드청구기본 정보조회] END ☆★☆☆★☆☆★☆" + apiResult);
		return apiResult.getLData("response");
	}

	/**
	 * - 금융결제원 API를 호출하고 응답결과를 리턴.
	 * 
	 * 1. API를 호출하기 위한 요청 메시지를 설정 - 헤더 정보 설정 (AccessToken, 채널세부업무구분코드) - 바디 정보 설정
	 * (오픈뱅킹공동업무 자체인증이용기관 API명세에 따름) 2. 오픈뱅킹 G/W를 통해 금융결제원 API 호출 3. 요청 메시지, 응답 메시지,
	 * 호출 정보를 UBF오픈뱅킹기관거래내역 테이블에 INSERT
	 * 
	 * <관련 테이블> UBF오픈뱅킹금융결제원API기본 UBF오픈뱅킹기관거래내역 <INPUT> 금융결제원 API 호출 요청메시지 <OUTPUT>
	 * 금융결제원 API 호출 응답메시지
	 * 
	 * @serviceId UBF3010136
	 * @method retvCrdBilDtlInfAPICall
	 * @method(한글명) 카드청구상세 정보조회 API호출
	 * @param LData
	 * @return LData
	 * @throws LException
	 */
	public LData retvCrdBilDtlInfAPICall(LData input) throws LException {
		LLog.debug.println("OpnbApiCpbc.retvCrdBilDtlInfAPICall[오픈뱅킹APICpbc.카드청구상세 정보조회] START ☆★☆☆★☆☆★☆" + input);
		
		/** Variable Set */
		LData apiInf = getApiInf(ApiSeq.API_0037.getSeq()); // UBF오픈뱅킹금융결제원API기본 - 오픈뱅킹URL일련번호
		LData apiBody = LDataUtil.deepCopyLData(input); 
		LData apiResult = new LData();		
			
		/** INPUT Validation */
		if(StringUtils.isEmpty(input.getString("채널세부업무구분코드"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), "채널세부업무구분코드");
		}
		
		input.setString("오픈뱅킹금융기관코드", input.getString("wd_bank_code_std"));//오픈뱅킹금융기관코드 셋팅. 입력값우선(없으면 응답값에서 셋팅)
		
		/** 금융결제원 API-호출 */
		apiResult = reqAPI(input, apiBody, apiInf);
	
		LLog.debug.println("OpnbApiCpbc.retvCrdBilDtlInfAPICall[오픈뱅킹APICpbc.카드청구상세 정보조회] END ☆★☆☆★☆☆★☆" + apiResult);

		return apiResult.getLData("response");
	}
}