package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;

/** 
 * opnbAccInfPbc
 * 
 * @logicalname  : 오픈뱅킹어카운트인포Pbc
 * @author       : 정영훈
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       정영훈       최초 생성
 *
 */

public class OpnbAccInfPbc {

    /**
     * - 고객의 계좌 리스트를 조회
     * 
     * 1. 사용자정보확인으로 수신한 키를 사용하여 금융기관 업권을 구분하여 조회요청
     * 2. 조회결과 리턴
     *
     * @serviceID UBF2030801
     * @logicalName 계좌통합 조회
     * @method retvAccIng
     * @method(한글명) 계좌통합 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvAccIng(LData input) throws LException {
        LData result = new LData();
      
		String itfCd = "UAA_3KFTCS00025";
		String tlgMgNo = "";
		
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {			
  			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
  			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
  			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
  		}
        
    	if(StringUtil.trimNisEmpty(input.getString("업권구분코드"))) {
			LLog.debug.println("로그 " + input.getString("업권구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹주민등록번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹주민등록번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹주민등록번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹플랫폼식별번호" ));//오픈뱅킹플랫폼식별번호가 존재하지 않습니다.
		}

		
//		dec_ssno = scpDbService.decStr(agt, param.get("enc_ssno"));
//		custNo = param.get("custNo");
//		
//		bszDtcd = param.get("bszDtcd"); 
		
		
		LData inData = new LData();
		inData.setString("BWK_DC"                   , "ACI");												// 업무구분_V3
		inData.setString("INS_CD"                   , "381");												// 기관코드_V3
		inData.setString("TELGM_PERTYP_CD"          , "0200");												// 전문종별코드_V4
		inData.setString("TLG_TR_DTCD"              , "490004");											// 전문거래구분코드 : (카드)690002 | (계좌) 490004
		inData.setString("STUS_CD"                  , "000");												// 상태코드_V3
		inData.setString("SNDRCV_FLAG"              , "L");													// 송수신플래그_V1
		inData.setString("RSP_CD_DC"                , "");													// 응답코드구분_V1
		inData.setString("RSP_CD"                   , "");													// 응답코드_V4
		inData.setString("TRS_YMS"                  , DateUtil.getDateTimeStr());	                        // 전송일시_V14
		inData.setString("TLG_MG_NO_V12"            , tlgMgNo);												// 전문관리번호_V12
		inData.setString("SPR"                      , "");													// 예비_V19
		inData.setString("CUST_RELNM_NO"            , input.getString("오픈뱅킹주민등록번호"));       			// 고객실명번호_V13
		inData.setString("FNC_CO_CD"                , "000");												// 금융회사코드_V3
		inData.setString("BSZ_DTCD"                 , input.getString("업권구분코드"));							// 업권구분코드
		inData.setString("OGTN_UNO"                 , "");													// 원거래고유번호_V12
		inData.setString("DMD_CHNDC"                , "M");													// 요청채널구분_V1
		inData.setString("INQ_EXC_FIOR_CNT"         , "00");												// 조회제외금융기관수
		inData.setString("INQ_EXC_FIOR_CD_CTG_CTT"  , "");													// 조회제외금융기관코드목록내용
		inData.setString("KFTC_RG_WHL_ACC_NCN"      , "000000");											// 금융결제원등록전체계좌건수
		inData.setString("KFTC_RG_ACC_INQ_ST_NCN"   , "000001");											// 금융결제원등록계좌조회시작건수
		inData.setString("KFTC_RG_ACC_INQ_ACC_NCN"  , "000030");											// 금융결제원등록계좌조회계좌건수 
		inData.setString("SPARE"                    ,  "");													// 예비_V20
		//inData.setString("custNo"                   ,  custNo);											// 리브메이트 고객번호
		inData.setString("채널세부업무구분코드"           ,  input.getString("채널세부업무구분코드"));					// 채널세부업무구분코드
		LLog.debug.println("대외EAI 호출 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", inData);
		
		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
		LData rRetvAccIngAPICall = opnbApi.retvAccIngAPICall(inData);
		
        return result;
    }

    /**
     * - 고객의 카드 리스트를 조회
     * 
     * 1. 사용자정보확인으로 수신한 키를 사용하여 금융기관 업권을 구분하여 조회요청
     * 2. 조회결과 리턴
     * 
     * @serviceID UBF2030802
     * @logicalName 카드통합 조회 
     * @method retvCrdIng
     * @method(한글명) 카드통합 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LMultiData retvCrdIng(LData input) throws LException {
        LMultiData result = new LMultiData();

        return result;
    }

}

