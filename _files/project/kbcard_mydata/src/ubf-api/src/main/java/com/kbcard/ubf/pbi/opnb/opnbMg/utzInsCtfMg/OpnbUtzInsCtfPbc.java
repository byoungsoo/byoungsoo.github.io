package com.kbcard.ubf.pbi.opnb.opnbMg.utzInsCtfMg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LNotAffectedException;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.util.StringUtil;

/** 
 * OpnbUtzInsCtfPbc
 * 
 * @logicalname  : 오픈뱅킹이용기관인증Pbc
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

public class OpnbUtzInsCtfPbc {
	
    /**
     * - 사용자인증 API의 호출없이 바로 토큰 획득
     * 
     * 1. 금결원 토큰발급 API호출
     * 2. [오픈뱅킹접근토큰내용, 오픈뱅킹토큰발급일시, 오픈뱅킹토큰만료일시, 오픈뱅킹범위구분코드, 오픈뱅킹토큰최종여부] 항목을 UBF오픈뱅킹토큰기본 테이블 insert
     * 3. 등록결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹토큰기본
     * <INPUT>
     * 오픈뱅킹접근토큰내용, 오픈뱅킹토큰발급일시, 오픈뱅킹토큰만료일시, 오픈뱅킹범위구분코드, 오픈뱅킹토큰최종여부
     * <OUTPUT>
     * 등록결과(T/F)
     * 
     * @serviceId UBF1010101
     * @method issueTken
     * @method(한글명) 토큰 발급
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData issueTken(LData input) throws LException {
        LLog.debug.println("OpnbUtzInsCtfPbc.issueTken[오픈뱅킹이용기관인증Pbc.토큰 발급] START ☆★☆☆★☆☆★☆" + input);
        LData iIssueTkenP = input; //i토큰발급입력
        LData rIssueTkenP = new LData(); //r토큰발급결과


		//Validation Check
        if(StringUtil.trimNisEmpty(iIssueTkenP.getString("채널세부업무구분코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
		if(StringUtil.trimNisEmpty(iIssueTkenP.getString("CLIENT_ID_V40"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"CLIENT_ID_V40"}, ObsErrCode.ERR_9001.getName());
		}
		if(StringUtil.trimNisEmpty(iIssueTkenP.getString("CLIENT_SECRET_V40"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"CLIENT_SECRET_V40"}, ObsErrCode.ERR_9001.getName());
		}
		
		//API호출
		LData iIssueTkenAPICall = new LData(); //i토큰발급API호출입력
        LData rIssueTkenAPICall = new LData(); //r토큰발급API호출결과
        
        iIssueTkenAPICall.setString("client_id", iIssueTkenP.getString("CLIENT_ID_V40")); //오픈뱅킹에서 발급한 이용기관 앱의 Client ID
        iIssueTkenAPICall.setString("client_secret", iIssueTkenP.getString("CLIENT_SECRET_V40")); //오픈뱅킹에서 발급한 이용기관 앱의 Client Secret
        iIssueTkenAPICall.setString("scope",  StringUtil.initEmptyValue(iIssueTkenP.getString("권한범위_V3"),"sa")); //Access Token 권한 범위. 고정값: sa
        iIssueTkenAPICall.setString("grant_type", StringUtil.initEmptyValue(iIssueTkenP.getString("권한부여방식_V40"),"client_credentials")); //권한부여방식 지정. 고정값: client_credentials
        iIssueTkenAPICall.setString("채널세부업무구분코드",iIssueTkenP.getString("채널세부업무구분코드")); //채널세부업무구분코드
        
        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
       	        
        rIssueTkenAPICall = opnbApi.issueTkenAPICall(iIssueTkenAPICall); //토큰발급 API호출
        LLog.debug.println("OpnbApiCpbc.issueTkenAPICall RESULT ☆★☆☆★☆☆★☆" + rIssueTkenAPICall);
        
				
		rIssueTkenP.setString("ACCESS_TOKEN_V400", rIssueTkenAPICall.getString("오픈뱅킹접근토큰내용")); //오픈뱅킹에서 발급한 이용기관 앱의 Client ID
		rIssueTkenP.setString("토큰유형_V10", rIssueTkenAPICall.getString("오픈뱅킹토큰유형")); //오픈뱅킹에서 발급한 이용기관 앱의 Client Secret
		rIssueTkenP.setLong("만료기간_N9", rIssueTkenAPICall.getLong("오픈뱅킹토큰만료기간")); //만료기간
		rIssueTkenP.setString("권한범위_V3", rIssueTkenAPICall.getString("오픈뱅킹토큰범위구분코드")); //Access Token 권한 범위. 고정값: sa
		rIssueTkenP.setString("오픈뱅킹이용기관코드", rIssueTkenAPICall.getString("오픈뱅킹이용기관코드")); //이용기관코드

		if(LDataUtil.isKeys(rIssueTkenAPICall, "rsp_code")
			&& !"O0000".equals(rIssueTkenAPICall.getString("rsp_code"))){
				throw new LBizException(rIssueTkenAPICall.getString("rsp_code"), rIssueTkenAPICall.getString("rsp_message"));
		}

		LLog.debug.println("OpnbUtzInsCtfPbc.issueTken[오픈뱅킹이용기관인증Pbc.토큰 발급] END ☆★☆☆★☆☆★☆" + rIssueTkenP);
        
        return rIssueTkenP;
    }

    /**
     * - Access Token의 유효기간 만료되기 전에 폐기 처리
     * 
     * 1. [오픈뱅킹접근토큰내용] 항목으로 UBF오픈뱅킹토큰기본 테이블의 [오픈뱅킹토큰최종여부]를 N으로 Update
     * 2.  수정결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹토큰기본
     * <INPUT>
     * 오픈뱅킹접근토큰내용, 오픈뱅킹토큰최종여부
     * <OUTPUT>
     * 수정결과(T/F)
     * 
     * @serviceId UBF1010102
     * @method prcTkenDscr
     * @method(한글명) 토큰 폐기 처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcTkenDscr(LData input) throws LException {
        LLog.debug.println("OpnbUtzInsCtfPbc.prcTkenDscr[오픈뱅킹이용기관인증Pbc.토큰 폐기 처리] START ☆★☆☆★☆☆★☆" + input);
        LData iDscrTkenP = input; //i토큰폐기입력
        LData rDscrTkenP = input; //r토큰폐기결과

			
		//Validation Check
        if(StringUtil.trimNisEmpty(iDscrTkenP.getString("채널세부업무구분코드"))) {
        	throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
		if(StringUtil.trimNisEmpty(iDscrTkenP.getString("CLIENT_ID_V40"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"CLIENT_ID_V40"}, ObsErrCode.ERR_9001.getName());
		}
		if(StringUtil.trimNisEmpty(iDscrTkenP.getString("CLIENT_SECRET_V40"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"CLIENT_SECRET_V40"}, ObsErrCode.ERR_9001.getName());
		}
		if(StringUtil.trimNisEmpty(iDscrTkenP.getString("ACCESS_TOKEN_V400"))) {
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"ACCESS_TOKEN_V400"}, ObsErrCode.ERR_9001.getName());
		}
		
		//API호출
		LData iPrcTkenDscrAPICall = new LData(); //i토큰폐기API호출입력
        LData rPrcTkenDscrAPICall = new LData(); //r토큰폐기API호출결과
        
        iPrcTkenDscrAPICall.setString("client_id", iDscrTkenP.getString("CLIENT_ID_V40")); //오픈뱅킹에서 발급한 이용기관 앱의 Client ID
        iPrcTkenDscrAPICall.setString("client_secret", iDscrTkenP.getString("CLIENT_SECRET_V40")); //오픈뱅킹에서 발급한 이용기관 앱의 Client Secret
        iPrcTkenDscrAPICall.setString("access_token", iDscrTkenP.getString("ACCESS_TOKEN_V400")); //Access Token 
        iPrcTkenDscrAPICall.setString("채널세부업무구분코드", iDscrTkenP.getString("채널세부업무구분코드")); //채널세부업무구분코드
        
        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
    
        rPrcTkenDscrAPICall = opnbApi.prcTkenDscrAPICall(iPrcTkenDscrAPICall);
        LLog.debug.println("OpnbApiCpbc.prcTkenDscrAPICall RESULT ☆★☆☆★☆☆★☆" + rPrcTkenDscrAPICall);
        String apiRspCode = rPrcTkenDscrAPICall.getString("rsp_code");//응답코드(API)
		
	
		if(!"O0000".equals(apiRspCode)) {	//인증실패시 오류 발생
			throw new LBizException(rPrcTkenDscrAPICall.getString("rsp_code"), rPrcTkenDscrAPICall.getString("rsp_message"));
		}else { //정상응답이면 DB반영
			try {
				//Ebc 호출
				LData iUpdateTkenIn = new LData(); // 토큰등록입력
		
				iUpdateTkenIn.setString("오픈뱅킹접근토큰내용", "Bearer "+rPrcTkenDscrAPICall.getString("access_token"));//오픈뱅킹접근토큰내용
				iUpdateTkenIn.setString("오픈뱅킹토큰최종여부", "N");// 오픈뱅킹토큰최종여부
				iUpdateTkenIn.setString("시스템최종갱신식별자",  iDscrTkenP.getString("채널세부업무구분코드"));
				
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.utzInsCtfMg.OpnbUtzInsCtfEbc", "updateTken", iUpdateTkenIn);  
	        
			}catch(LNotAffectedException lnae) {	
				lnae.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_A001.getCode(), ObsErrCode.ERR_A001.getName());
			}catch(LException le) {
				le.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_A002.getCode(), ObsErrCode.ERR_A002.getName());	
			}
        }
		
		rDscrTkenP.setString("API응답코드_V5", rPrcTkenDscrAPICall.getString("rsp_code")); //API 응답코드
		rDscrTkenP.setString("API응답메시지_V300", rPrcTkenDscrAPICall.getString("rsp_message")); //API 응답메시지
		rDscrTkenP.setString("CLIENT_ID_V40", rPrcTkenDscrAPICall.getString("client_id")); //오픈뱅킹에서 발급한 이용기관 앱의 Client ID
		rDscrTkenP.setString("CLIENT_SECRET_V40", rPrcTkenDscrAPICall.getString("client_secret")); //오픈뱅킹에서 발급한 이용기관 앱의 Client Secret
		rDscrTkenP.setString("ACCESS_TOKEN_V400", rPrcTkenDscrAPICall.getString("access_token")); //폐기한 Access Token 

		LLog.debug.println("OpnbUtzInsCtfPbc.prcTkenDscr[오픈뱅킹이용기관인증Pbc.토큰 폐기 처리] END ☆★☆☆★☆☆★☆"+rDscrTkenP);
        
        return rDscrTkenP;
    }

    /**
     * - 현재 보유한 Access Token 정보 조회
     * 
     * 1. [오픈뱅킹접근토큰내용] 항목으로 UBF오픈뱅킹토큰기본 테이블 조회
     * 2. Access Token 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹토큰기본
     * <INPUT>
     * 1. 오픈뱅킹접근토큰내용이 존재
     * 2. 오픈뱅킹접근토큰내용이 존재하지 않음
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹접근토큰내용, 오픈뱅킹토큰발급일시, 오픈뱅킹토큰만료일시, 오픈뱅킹토큰범위구분코드, 오픈뱅킹토큰최종여부
     * 
     * @serviceId UBF1010103
     * @method retvTken
     * @method(한글명) 토큰 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvTken(LData input) throws LException {
        LLog.debug.println("OpnbUtzInsCtfPbc.retvTken[오픈뱅킹이용기관인증Pbc.토큰 조회] START ☆★☆☆★☆☆★☆" +  input);
        LData iRetvTkenP = input; //i토큰조회입력
        LData rRetvTkenP = new LData(); //r토큰조회결과

		//Validation Check
        if(StringUtil.trimNisEmpty(iRetvTkenP.getString("채널세부업무구분코드"))) {//채널세부업무구분코드
        	  throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
		//Ebc 호출
		LData iTkenInfCtgIn = new LData(); // 토큰정보목록입력
		LMultiData rTkenInfCtgOut = new LMultiData(); // 토큰정보목록출력
        try {
			iTkenInfCtgIn.setString("오픈뱅킹접근토큰내용", iRetvTkenP.getString("오픈뱅킹접근토큰내용"));
			rTkenInfCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.utzInsCtfMg.OpnbUtzInsCtfEbc", "selectTkenInfCtg", iTkenInfCtgIn);
        }catch(LException le) {
			le.printStackTrace();
			throw new LBizException(ObsErrCode.ERR_A004.getCode(), ObsErrCode.ERR_A004.getName());	
		}
		//오픈뱅킹접근토큰내용이 존재하는데, 목록이 없으면 에러 
		if((StringUtil.trimNisEmpty(iRetvTkenP.getString("오픈뱅킹접근토큰내용")) == false) &&  rTkenInfCtgOut.getDataCount() == 0) {
			throw new LBizException(ObsErrCode.ERR_A003.getCode(), ObsErrCode.ERR_A003.getName());	
		}
		rRetvTkenP.setInt("그리드_cnt", rTkenInfCtgOut.getDataCount());
		rRetvTkenP.set("그리드", rTkenInfCtgOut);
		LLog.debug.println("OpnbUtzInsCtfPbc.retvTken[오픈뱅킹이용기관인증Pbc.토큰 조회] END ☆★☆☆★☆☆★☆" + rRetvTkenP );
      
		return rRetvTkenP;
    }

}

