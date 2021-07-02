package com.kbcard.ubd.cpbi.cmn;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.BizExceptionPitcher;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;

/**
 * 프로그램명 	: 오픈뱅킹 MCI지원함수
 * 작성자 		: 임용택
 * 작성일자 	: 2021-04-07
 * 설      명 	: 오픈뱅킹 MCI지원함수 cpbi
 */
public class MciSptFntCpbc {
	
	/**
	 *  MCI 고객정보조회
	 * @param iCstInf
	 * 				CI내용 			
	 * @return rCstInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * << MCI 연계정보 >>
	 * 인터페이스ID 	: CGT_1_GAAS00046
	 * 거래코드 		: GAA0101442
	 * 연계구분 		: MCI
	 * 인터페이스명 	: CI연계번호조회
	 * input ---------------------------------------------------------------
	 *	 1      TR_PRC_DTCD_V2     	 처리구분코드_V2  			String  2   
	 *	 2      SRCH_NO_DTCD    	검색번호구분코드_V1  		String  1   
	 *	 3      CQNO         		고객고유번호  				String  20 (양방향)   
	 *	 4      CST_IDF         	고객식별자  				String  10   
	 *	 5      CST_MG_NO       	고객관리번호  				String  5    
	 *	 6      CI_CTT         		CI내용  					String  88   
	 *	 7      DI_NO         		DI번호  					String  64   
	 *	 8      TR_DTCD         	거래구분코드_V1  			String  1
	 * output --------------------------------------------------------------
	 *	 1      CQNO         		고객고유번호  				String  20 (양방향)
	 *	 2      CSNM         		고객명 	 					String  50 (양방향)   
	 *	 3      CST_IDF         	고객식별자  				String  10   
	 *	 4      CST_MG_NO         	고객관리번호  				String  5    
	 *	 5      CI_CTT         		CI내용  					String  88   
	 *	 6      POTEN_CST_YN        잠재고객여부  				String  1    
	 *	 7      CST_PT_CL_DTCD      고객유형분류구분코드  		String  1    
	 *	 8      CST_PT_CL_DC_CDNM   고객유형분류구분코드명_V50  String  50   
	 *	 9      CI_LST_PRC_YMS      CI최종처리일시  			String  20   
	 *	 10     SYS_LST_TR_YMS      시스템최종거래일시 	 		String  20   
	 *	 11     SYS_LST_UPD_IDF     시스템최종갱신식별자  		String  12   
	 *	 12     DI_NO         		DI번호  					String  64 
	 */
	public LData retvMciCstInf(LData iCstInf) throws LException {		
		
		LData 			rCstInf 	= new LData();		
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();		
		
		// 인터페이스ID 	: CGT_1_GAAS00046
		// 거래코드 		: GAA0101442
		// 연계구분 		: MCI
		// 인터페이스명 	: CI연계번호조회		
		try {
			
			iCstInf.setString("검색번호구분코드_V1", "5");
			
			LLog.debug.println("MCI고객정보조회 : " , iCstInf);			
			rCstInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBE_1_GAAS00001", iCstInf);
			
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		}
		
		return rCstInf;
	}

	/**
	 * 마이데이터 카드목록조회
	 * @param iCrdCtgInq CI내용 			
	 * @return rCrdCtgIn 고객식별자, 고객관리번호
	 * @throws LException
	 */
	public LData retvCrdCtgInq(LData iCrdCtgInq) throws LException {		
		
		LData 			rCrdCtgInq = new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GAGS00001
		// 거래코드 		: GAG0107941
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 카드목록조회 
		try {
			
			LLog.debug.println("마이데이터 카드목록조회 :" , iCrdCtgInq);
			rCrdCtgInq = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GAGS00001", iCrdCtgInq);
			LLog.debug.println("마이데이터 카드목록출력 :" , rCrdCtgInq);
			
		} catch (Exception e) {
			LLog.debug.println("■■■■■■■ Exception 처리하지 않는다 ■■■■■■■");
		}
		linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
		sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
		sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
		sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
		sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);

		rCrdCtgInq.setString("오류메시지코드"	, sdtErrMsgCd		);
		rCrdCtgInq.setString("오류메시지"	    , sdtErrMsg			);
		rCrdCtgInq.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
		rCrdCtgInq.setString("거래GUID"			, sdtGidNo			);
	
		return rCrdCtgInq;
	}

	/**
	 * 마이데이터 카드기본정보조회
	 * @param iCrdBasInf CI내용 			
	 * @return rCrdBasInf 고객식별자, 고객관리번호
	 * @throws LException
	 */	
	public LData retvCrdBasInf(LData iCrdBasInf) throws LException {		
		
		LData 			rCrdBasInf  = new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		// 인터페이스ID 	: UBD_1_GAGS00002
		// 거래코드 		: 
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 카드기본정보조회		
		try {
			LLog.debug.println("마이데이터 카드기본정보조회 :" , iCrdBasInf);
			rCrdBasInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GAGS00002", iCrdBasInf);
			LLog.debug.println("마이데이터 카드기본정보출력 :" , rCrdBasInf);

		} catch (Exception e) {
			LLog.debug.println("■■■■■■■ Exception 처리하지 않는다 ■■■■■■■");
		}
		linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
		sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
		sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
		sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
		sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);

		rCrdBasInf.setString("오류메시지코드"	, sdtErrMsgCd		);
		rCrdBasInf.setString("오류메시지"	    , sdtErrMsg			);
		rCrdBasInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
		rCrdBasInf.setString("거래GUID"			, sdtGidNo			);
		
		return rCrdBasInf;
	}		
	
	/**
	 * MCI 마이데이터 API 제공 청구기본정보조회
	 * @param iCrdBilBasInf
	 * 				CI내용 			
	 * @return rCrdBilBasInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 INQ_ST_YM	   		        조회시작년월	
	 * 3 INQ_ED_YM	   		        조회종료년월	
	 * 4 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 5 PGE_SIZE	   		        페이지사이즈_N5	
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 INQ_ST_YM	   		        조회시작년월	
	 * 3 INQ_ED_YM	   		        조회종료년월	
	 * 4 CST_IDF	   		        고객식별자	
	 * 5 CST_MG_NO	   		        고객관리번호	
	 * 6 NEXT_INQ_KY	            다음조회키_V1000
	 * 7 NEXT_EXST_YN	           	다음존재여부_V1
	 * 8 GRID	  			그리드
	 * 9 ┖  BIL_SNO	   	청구일련번호
	 *10 ┖  BIL_AMT	   	청구금액
	 *11 ┖  PMA_DD	        결제일	
	 *12 ┖  PMA_YM			결제년월	
	 *13 ┖  BIL_YMD	    청구년월일	
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMciCrdBilBasInfForPaging(LData iCrdBilBasInf) throws LException {		
		
		LData 			rCrdBilBasInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor 	= new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBCS00001
		// 거래코드 		: GBC0123240
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 청구기본정보조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 청구기본정보조회 START ■■■■■■■" , iCrdBilBasInf);
			
			rCrdBilBasInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00001", iCrdBilBasInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rCrdBilBasInf.setString("오류메시지코드"	, sdtErrMsgCd);
			rCrdBilBasInf.setString("오류메시지"	    , sdtErrMsg);
			rCrdBilBasInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo);
			rCrdBilBasInf.setString("거래GUID"			, sdtGidNo);
			
			LLog.debug.println("■■■■■■■ 청구기본정보조회 RETURN ■■■■■■■" , rCrdBilBasInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rCrdBilBasInf;
	}
	
	/**
	 * MCI 마이데이터 API 제공 청구추가정보조회
	 * @param iCrdBilDtlInf
	 * 				CI내용 			
	 * @return rCrdBilDtlInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 PMA_YM	   		        	결제년월	
	 * 3 BIL_SNO	   		        청구일련번호	
	 * 4 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 5 PGE_SIZE	   		        페이지사이즈_N5	
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CST_IDF	   		        고객식별자	
	 * 3 CST_MG_NO	   		        고객관리번호	
	 * 4 PMA_YM	   		        	결제년월	
	 * 5 BIL_SNO	   		        청구일련번호	
	 * 6 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 7 NEXT_EXST_YN	           	다음존재여부_V1
	 * 8 GRID	  			그리드
	 * 9 ┖  CRD_ALTR_NO			카드대체번호
	 *10 ┖  CRD_USE_YMD			카드사용년월일
	 *11 ┖  SLIP_NO	   			전표번호_V64
	 *12 ┖  BIL_AMT	   			청구금액_N18_3
	 *13 ┖  CRCD	        		통화코드	
	 *14 ┖  MMC_NM					가맹점명	
	 *15 ┖  BIL_FEE	    		청구수수료
	 *16 ┖  ISL_MNCN	    		할부개월수
	 *17 ┖  BIL_NTH	    		청구회차
	 *18 ┖  PTIN_SLP_BIL_AF_BL	    분할전표청구후잔액
	 *19 ┖  DLD_SLP_CL_DTCD	    명세서전표분류구분코드	
	 * ---------------------------------------------------------------------
	 */			
	public LData retvMciCrdBilDtlInfForPaging(LData iCrdBilDtlInf) throws LException {		
		
		LData 			rCrdBilDtlInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor 	= new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBCS00002
		// 거래코드 		: GBC0123340
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 청구추가정보조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 청구추가정보조회 START ■■■■■■■" , iCrdBilDtlInf);
			
			rCrdBilDtlInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00002", iCrdBilDtlInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rCrdBilDtlInf.setString("오류메시지코드"	, sdtErrMsgCd);
			rCrdBilDtlInf.setString("오류메시지"	    , sdtErrMsg);
			rCrdBilDtlInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo);
			rCrdBilDtlInf.setString("거래GUID"			, sdtGidNo);
			
			LLog.debug.println("■■■■■■■ 청구추가정보조회 RETURN ■■■■■■■" , rCrdBilDtlInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rCrdBilDtlInf;
	}
	
	/**
	 * MCI 마이데이터 API 제공 결제정보조회
	 * @param iPmaInf
	 * 				CI내용 			
	 * @return rPmaInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CST_IDF	   		        고객식별자	
	 * 3 CST_MG_NO	   		        고객관리번호	
	 * 4 INQ_BSE_YMD	   		    조회기준년월일	
	 * 5 RVL_YN	   		        	리볼빙여부_V1
	 * 8 PMA_INF_GRID		결제정보그리드
	 * 9 ┖  BIL_SNO				청구일련번호
	 *10 ┖  BIL_YMD				청구년월일
	 *11 ┖  PMA_AMT	   			결제금액
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMciPmaInf(LData iPmaInf) throws LException {		
		
		LData 			rPmaInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBCS00003
		// 거래코드 		: GBC0112440
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 결제정보조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 결제정보조회 START ■■■■■■■" , iPmaInf);
			
			rPmaInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00003", iPmaInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rPmaInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rPmaInf.setString("오류메시지"	    , sdtErrMsg			);
			rPmaInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rPmaInf.setString("거래GUID"		, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 결제정보조회 RETURN ■■■■■■■" , rPmaInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rPmaInf;
	}

	/**
	 * MCI 마이데이터 API 제공 장기대출정보조회
	 * @param iLntmLnInf
	 * 				고객식별자 			
	 * @return rLntmLnInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CST_IDF	   		고객식별자	
	 * 2 CST_MG_NO	   		고객관리번호
	 * [output] ------------------------------------------------------------
	 * 1 CST_IDF	   		고객식별자	
	 * 2 CST_MG_NO	   		고객관리번호	
	 * 3 BSE_YMD	   		기준년월일
	 * 4 GEA0156441_GRID	GEA0156441_그리드	
	 * 5 ┖  	LN_NO			대출번호				
	 * 6 ┖  	LN_NTH			대출회차				
	 * 7 ┖  	LN_AGM_YMD		대출약정년월일	
	 * 8 ┖  	LN_PD_CL_NM		대출상품분류명	
	 * 9 ┖  	LN_PD_NM		대출상품명			
	 *10 ┖  	LN_AMT			대출금액				
	 *11 ┖  	CRLN_APL_IRT	카드론적용금리	
	 *12 ┖  	LN_EPN_YMD		대출만기년월일	
	 *13 ┖  	LN_BL			대출잔액				
	 *14 ┖  	RPY_MTH_DTCD	상환방법구분코드
	 *15 ┖  	RPY_IST			상환이자	
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMciLntmLnInf(LData iLntmLnInf) throws LException {		
		
		LData 			rLntmLnInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GEAS00001
		// 거래코드 		: GEA0156441
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 장기대출정보조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 장기대출정보조회 START ■■■■■■■" , iLntmLnInf);
			
			rLntmLnInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GEAS00001", iLntmLnInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rLntmLnInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rLntmLnInf.setString("오류메시지"	    , sdtErrMsg			);
			rLntmLnInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rLntmLnInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 장기대출정보조회 RETURN ■■■■■■■" , rLntmLnInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rLntmLnInf;
	}

	/**
	 * MCI 마이데이터 API 제공 단기대출정보조회
	 * @param iLntmLnInf
	 * 				고객식별자 			
	 * @return rLntmLnInf
	 * 				고객식별자, 고객관리번호
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CST_IDF	   		        고객식별자	
	 * 3 CST_MG_NO	   		        고객관리번호	
	 * 4 SHTM_LN_INF_GRID	        단기대출정보그리드
	 * 5 ┖  CA_ATH_YMS				CA승인일시_V14
	 * 6 ┖  CA_ATH_AMT				CA승인금액_N15
	 * 7 ┖  CA_BL	   	    		CA잔액
	 * 8 ┖  BIL_YMD1				청구년월일1_V8
	 * 9 ┖  FEE_RT	   		    	수수료율_N5_3
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMciShtmLnInf(LData iShtmLnInf) throws LException {		
		
		LData 			rShtmLnInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBCS00006
		// 거래코드 		: GBC0193041
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 단기대출정보조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 단기대출정보조회 START ■■■■■■■" , iShtmLnInf);
			
			rShtmLnInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00006", iShtmLnInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rShtmLnInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rShtmLnInf.setString("오류메시지"	    , sdtErrMsg			);
			rShtmLnInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rShtmLnInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 단기대출정보조회 RETURN ■■■■■■■" , rShtmLnInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rShtmLnInf;
	}	
	
	/**
	 * MCI 마이데이터 API 제공 국내승인내역조회
	 * @param iPmaInf
	 * 				카드대체번호, 조회시작년월일, 조회종료년월일 , 다음조회키_V1000 , 페이지사이즈_N5
	 * @return rPmaInf
	 * 				
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CRD_ALTR_NO	   		    카드대체번호	
	 * 3 INQ_ST_YMD	   		        조회시작년월일	
	 * 4 INQ_ED_YMD                 조회종료년월일
	 * 5 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 6 PGE_SIZE	   		        페이지사이즈_N5	
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CST_IDF	   		        고객식별자	
	 * 3 CST_MG_NO	   		        고객관리번호	
	 * 4 CRD_ALTR_NO                카드대체번호
	 * 5 INQ_ST_YMD	   		        조회시작년월일	
	 * 6 INQ_ED_YMD                 조회종료년월일
	 * 7 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 8 NEXT_EXST_YN	           	다음존재여부_V1
	 * 9 ATH_HIS_GRID1	  			승인내역그리드1
	 *10 ┖  ATH_NO			        승인번호
	 *11 ┖  ATH_YMD	   			승인년월일
	 *12 ┖  SL_ATH_HMS	   			매출승인시각
	 *13 ┖  CRD_ATH_STS_DTCD		카드승인상태구분코드
	 *14 ┖  MYDT_CRD_DTCD			마이데이터카드구분코드
	 *13 ┖  ATH_CNC_YMD	        승인취소년월일	
	 *14 ┖  ATH_CNC_HMS			승인취소시각	
	 *14 ┖  BZNO					사업자등록번호	
	 *15 ┖  MMC_NM_V75	    		가맹점명_V75
	 *16 ┖  SL_ATH_AMT	    		매출승인금액
	 *17 ┖  CRR_AMT	    		정정금액
	 *18 ┖  ISL_MNCN_N5	        할부개월수_N5
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMciDomAthInf(LData iDomAthInf) throws LException {		
		
		LData 			rDomAthInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBHS00001
		// 거래코드 		: GBH0176044
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터국내승인내역조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 국내승인내역조회 START ■■■■■■■" , iDomAthInf);
			
			rDomAthInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBHS00001", iDomAthInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rDomAthInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rDomAthInf.setString("오류메시지"	    , sdtErrMsg			);
			rDomAthInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rDomAthInf.setString("거래GUID"		, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 국내승인내역조회 RETURN ■■■■■■■" , rDomAthInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rDomAthInf;
	}
	/**
	 * MCI 마이데이터 API 제공 국내승인내역조회
	 * @param iPmaInf
	 * 				카드대체번호, 조회시작년월일, 조회종료년월일 , 다음조회키_V1000 , 페이지사이즈_N5
	 * @return rPmaInf
	 * 				
	 * @throws LException
	 * 
	 * [input] -------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CRD_ALTR_NO	   		    카드대체번호	
	 * 3 INQ_ST_YMD	   		        조회시작년월일	
	 * 4 INQ_ED_YMD                 조회종료년월일
	 * 5 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 6 PGE_SIZE	   		        페이지사이즈_N5	
	 * [output] ------------------------------------------------------------
	 * 1 CI_CTT	  					CI내용
	 * 2 CST_IDF	   		        고객식별자	
	 * 3 CST_MG_NO	   		        고객관리번호	
	 * 4 CRD_ALTR_NO                카드대체번호
	 * 5 INQ_ST_YMD	   		        조회시작년월일	
	 * 6 INQ_ED_YMD                 조회종료년월일
	 * 7 NEXT_INQ_KY	  	        다음조회키_V1000
	 * 8 NEXT_EXST_YN	           	다음존재여부_V1
	 * 9 ATH_HIS_GRID2	  			승인내역그리드2
	 *10 ┖  ATH_NO			        승인번호
	 *11 ┖  ATH_YMD	   			승인년월일
	 *12 ┖  SL_ATH_HMS	   			매출승인시각
	 *13 ┖  CRD_ATH_STS_DTCD		카드승인상태구분코드
	 *14 ┖  MYDT_CRD_DTCD			마이데이터카드구분코드
	 *15 ┖  ATH_CNC_YMD	        승인취소년월일	
	 *16 ┖  ATH_CNC_HMS			승인취소시각	
	 *17 ┖  MMC_NM_V75	    		가맹점명_V75
	 *18 ┖  CRD_ATH_AMT_N18_3	    카드승인금액_N18_3
	 *19 ┖  CRR_AMT_N18_3	    	정정금액_N18_3
	 *20 ┖  ISO_NTL_CD	    		ISO국가코드
	 *21 ┖  ISO_CRCD	    		ISO통화코드
	 *22 ┖  SLP_BUY_CUAMT	        전표매입누계금액
	 * ---------------------------------------------------------------------
	 */		
	public LData retvMyDtVovsAthHis(LData iVovAthInf) throws LException {		
		
		LData 			rVovAthInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GBHS00002
		// 거래코드 		: GBH0176045
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터해외승인내역조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 마이데이터해외승인내역조회 START ■■■■■■■" , iVovAthInf);
			
			rVovAthInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBHS00002", iVovAthInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rVovAthInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rVovAthInf.setString("오류메시지"	    , sdtErrMsg			);
			rVovAthInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rVovAthInf.setString("거래GUID"		, sdtGidNo			);
			
			
			LLog.debug.println("■■■■■■■ MCI 마이데이터해외승인내역조회 RETURN ■■■■■■■" , rVovAthInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rVovAthInf;
	}

	/**
	 * MCI 마이데이터 API 제공 대출상품계좌목록조회
	 * @param iLnPdAccTnhsInf
	 * 				GEA0156440_I 			
	 * @return rLnPdAccTnhsInf
	 * 				GEA0156440_O
	 * @throws LException
	 */		
public LData retvMciLstLnPdAccInf(LData ilnPdAccCtgIn) throws LException {		
		
		LData 			rlnPdAccCtgOut 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData  linkResponseHeader 	= new LData();
		linkResponseHeader.clear();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GEAS00002
		// 거래코드 		: GEA0156442
		// 연계구분 		: MCI
		// 인터페이스명 	: 할부금융계좌목록조회
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 할부금융계좌목록조회 START ■■■■■■■" , ilnPdAccCtgIn);
			rlnPdAccCtgOut = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GEAS00002", ilnPdAccCtgIn);
			LLog.debug.println("■■■■■■■ MCI 할부금융계좌목록조회 RETURN ■■■■■■■" , rlnPdAccCtgOut);
	
		} catch (Exception e) {
			LLog.debug.println("■■■■■■■ Exception 처리하지 않는다 ■■■■■■■");
		}
		linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
		sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
		sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
		sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
		sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);

		rlnPdAccCtgOut.setString("오류메시지코드"	, sdtErrMsgCd		);
		rlnPdAccCtgOut.setString("오류메시지"	    , sdtErrMsg			);
		rlnPdAccCtgOut.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
		rlnPdAccCtgOut.setString("거래GUID"			, sdtGidNo			);
		
		return rlnPdAccCtgOut;
	}		
	
	/**
	 * MCI 마이데이터 API 제공 대출상품계좌거래내역조회
	 * @param iLnPdAccTnhsInf
	 * 				GEA0156440_I 			
	 * @return rLnPdAccTnhsInf
	 * 				GEA0156440_O
	 * @throws LException
	 */		
	public LData retvMciLnPdAccTnhsInf(LData iLnPdAccTnhsInf) throws LException {		
		
		LData 			rLnPdAccTnhsInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GEAS00005
		// 거래코드 		: GEA0156440
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 rLnPdAccTnhsInf
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 대출상품계좌거래내역조회 START ■■■■■■■" , iLnPdAccTnhsInf);
			
			rLnPdAccTnhsInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GEAS00005", iLnPdAccTnhsInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rLnPdAccTnhsInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rLnPdAccTnhsInf.setString("오류메시지"	    , sdtErrMsg			);
			rLnPdAccTnhsInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rLnPdAccTnhsInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 대출상품계좌거래내역조회 RETURN ■■■■■■■" , rLnPdAccTnhsInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rLnPdAccTnhsInf;
	}		
	
	/**
	 * MCI 마이데이터 API 제공 대출상품계좌추가정보조회
	 * @param iLnPdAccTnhsInf
	 * 				GEA0156440_I 			
	 * @return rLnPdAccTnhsInf
	 * 				GEA0156440_O
	 * @throws LException
	 */		
	public LData retvMciLnPdAccPlInf(LData iLnPdAccPlInf) throws LException {		
		
		LData 			rLnPdAccPlInf 	= new LData();	
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		
		LData linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_1_GEAS00004
		// 거래코드 		: GEA0156440
		// 연계구분 		: MCI
		// 인터페이스명 	: 마이데이터 rLnPdAccTnhsInf
		// 연계처리계		: 상품처리계
		try {
			
			LLog.debug.println("■■■■■■■ MCI 대출상품계좌추가정보조회 START ■■■■■■■" , iLnPdAccPlInf);
			
			rLnPdAccPlInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GEAS00004", iLnPdAccPlInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rLnPdAccPlInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rLnPdAccPlInf.setString("오류메시지"	    , sdtErrMsg			);
			rLnPdAccPlInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rLnPdAccPlInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ MCI 대출상품계좌거래내역조회 RETURN ■■■■■■■" , rLnPdAccPlInf);
		} catch (LNotFoundException nfe) {
			BizExceptionPitcher.throwException(nfe);
		} catch (LBizException be) {
			BizExceptionPitcher.throwException(be);
		} 
	
		return rLnPdAccPlInf;
	}		
}
