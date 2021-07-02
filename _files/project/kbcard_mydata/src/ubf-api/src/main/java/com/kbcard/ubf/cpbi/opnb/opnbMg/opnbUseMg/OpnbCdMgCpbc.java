package com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg;

import org.apache.commons.lang.StringUtils;

import com.kbcard.ubf.cpbi.cmn.AccessToken;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.AuthInfo;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.util.StringUtil;

/** 
 * opnbCdMgCPbc
 * 
 * @logicalname  : 오픈뱅킹코드관리Pbc
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

public class OpnbCdMgCpbc {
	
	static AccessToken accessToken = AccessToken.createInstance();

	static {
			/** 토큰 조회/설정 */
			try {
				setAccessToken();
			} catch (LBizException e) {
				e.printStackTrace();
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
		/** Valiable Set */
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
				throw new LBizException(ObsErrCode.ERR_9999.getCode(), ObsErrCode.ERR_9999.getName());				
			}

			accessToken.setAccessToken(accessTokenNo.getString("오픈뱅킹접근토큰내용"));
			accessToken.setExpYms(accessTokenNo.getString("오픈뱅킹토큰만료일시"));
			accessToken.setTokenRngDtcd(accessTokenNo.getString("오픈뱅킹토큰범위구분코드"));
			accessToken.setOpnbUtzInsCd(accessTokenNo.getString("오픈뱅킹이용기관코드"));

			LLog.debug.println("accessToken::", accessToken.getAccessToken());
		}
	}
	
	
	 /**
     * - 금융기관, 상호금융기관, 금융투자회사, 카드사 등의 코드정보 조회
     * - OAuth 응답코드정보 조회
     * - 세부 응답코드정보 조회
     * - API 응답코드정보 조회
     * - 참가기관 응답코드정보 조회
     * - 이체 용도 구분코드정보 조회
     * - 거래유형 코드정보 조회
     * 
     * 1. 요청된 조회유형에 따라 응답값 리턴
     * 
     * <관련 테이블>
     *  UBD코드그룹기본
     *  UBD코드상세기본
     * <INPUT>
     *  오픈API포탈코드그룹한글명
     * <OUTPUT>
     * LIST
     *  -   오픈API포탈코드그룹한글명
     *      오픈API포탈코드그룹영문명
     *      오픈API포탈코드식별번호
     *      오픈API포탈코드명
     * 
     * @serviceID UBF1010301
     * @method retvCdInf
     * @method(한글명) 코드정보 조회
     * @param String
     * @return LData
     * @throws LException 
     */ 
    public LData retvCdInf(String inputCd, String inputNm) throws LException {

    	LData result = new LData();
    	LData iRegData = new LData();
        
    	LLog.debug.println("코드정보 조회::Input::");
  		LLog.debug.println(inputCd + "/" + inputNm);
    	
    	if(StringUtil.trimNisEmpty(inputCd)) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 통합코드명"));//처리중 오류가 발생했습니다.
    	}
    	
    	if(StringUtil.trimNisEmpty(inputNm)) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 통합코드"));//처리중 오류가 발생했습니다.
    	}

    	iRegData.setString("통합코드명", inputCd);
    	iRegData.setString("통합코드", inputNm);
    	
    	try {
    		
    		result = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCdMgCEbc", "selectCdInf", iRegData); // 코드정보 조회
    		
    		LLog.debug.println("코드정보 조회::result::");
      		LLog.debug.println(result);
			
    	} catch(LException e) {
    		throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "코드정보 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
		}
    	
        return result;
    }
    

    /**
     * - 거래고유번호(참가기관)는 참가기관에 전송되는 값
     * 형식 : 이용기관코드(10자리) + 생성주체구분코드(“U”)*+ 이용기관 부여번호(9자리)
     * - 하루동안 유일성을 보장해야 함(날짜가 다를 경우에는 중복발생이 가능)
     * 
     * 1. API 호출
     * 2. 거래고유번호 생성
     * 3. 생성된 거래고유번호 리턴
     * 
     * * 이용기관코드(10:고정) + "U"(고정) + 업무코드(1:오픈뱅킹 "F") + 일련번호(8:시퀀스번호-각업무에서알아서채번)
     * 
     * 
     * <관련 테이블>
     * 
     * <INPUT>
     * 
     * <OUTPUT>
     * 거래고유번호
     * 
     * @serviceID UBF1010302
     * @method crtTrUno
     * @method(한글명) 거래고유번호 생성
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData crtTrUno(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("거래고유번호생성 START ☆★☆☆★☆☆★☆");
    	}
    	
    	LData rCrtTrUnoP = new LData(); // r거래고유번호생성출력
    	LData rSelectTrUnoSeqOut = new LData(); // 거래고유번호조회출력
        StringBuilder bankTranId = new StringBuilder();
        
        try {
        	rSelectTrUnoSeqOut = (LData)BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCdMgCEbc", "selectTrUnoSeq", input);
        	bankTranId.append(accessToken.getOpnbUtzInsCd());
        	bankTranId.append("U"); // 고정값
        	bankTranId.append("F"); // 업무코드(1:오픈뱅킹 "F")
        	bankTranId.append(String.format("%08d", Integer.parseInt(rSelectTrUnoSeqOut.getString("거래고유번호일련번호"))));

        	rCrtTrUnoP.setString("거래고유번호", bankTranId.toString());
		} catch (LException e) {
			throw new LBizException(ObsErrCode.ERR_9003.getCode(), ObsErrCode.ERR_9003.getName());
		}
        
        if(LLog.debug.isEnabled()) {
        	LLog.debug.println("----------[rCrtTrUnoP]----------");
        	LLog.debug.println(rCrtTrUnoP);
        	LLog.debug.println("거래고유번호생성 END ☆★☆☆★☆☆★☆");
        }
        
        return rCrtTrUnoP;
        
    }

}

