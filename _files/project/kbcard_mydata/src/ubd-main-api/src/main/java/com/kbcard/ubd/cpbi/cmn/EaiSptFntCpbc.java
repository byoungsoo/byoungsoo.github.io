package com.kbcard.ubd.cpbi.cmn;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.BizExceptionPitcher;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.util.StringUtil;

/**
 * 프로그램명 	: 오픈뱅킹 EAI지원함수
 * 작성자 		: 임용택
 * 작성일자 		: 2021-04-07
 * 설      명 		: 오픈뱅킹 EAI지원함수 cpbi
 */
public class EaiSptFntCpbc {
	
	// 대내EAI 카드청구기본정보조회(홈웹서비스)
	// CI내용 => 카드청구기본목록 구하기
	// << EAI 연계정보 >>
	// 인터페이스ID 	: UBE_2_CXWS00001
	// 거래코드 		: UBE0100141
	// 연계구분 		: EAI
	// 인터페이스명 	: 오픈뱅킹API 카드청구기본정보조회 
	// input ---------------------------------------------------------------
	//	1      CST_IDF         		고객식별자  		String  10     
	//	2      CST_MG_NO         	고객관리번호  		String  5    
	//	3      INQ_ST_YM         	조회시작년월  		String  6    
	//	4      INQ_ED_YM         	조회종료년월  		String  6    
	//	5      NEXT_INQ_KY_V20    	다음조회키_V20  	String  20 
	//	6      PGE_SIZE        	 	페이지사이즈_N5  	Numeric  5 
	// output --------------------------------------------------------------
	//	1      CST_IDF         		고객식별자  			String  10    
	//	2      CST_MG_NO         	고객관리번호  			String  5   
	//	3      INQ_ST_YM         	조회시작년월  			String  6   
	//	4      INQ_ED_YM         	조회종료년월  			String  6   
	//	5      NEXT_INQ_KY_V20    	다음조회키값_V20  		String  20     
	//	6      NEXT_PAGE_YN       	다음페이지존재여부_V1  String  1 
	//	7    GRID           청구일련번호별목록  Group  5     
	//	8		┖  PMA_YMD        결제년월_V6  		String  6   
	//	9      	┖  BIL_SNO        청구일련번호  		Numeric 5  
	//	10     	┖  BIL_AMT        청구금액_N13  		Numeric 13 
	//	11     	┖  PMA_DD		   결제일  				String  2  
	//	12     	┖  BIL_YMD        청구년월일  			String  8  
	//	13     	┖  CRT_CHK_DC     신용체크구분_V2 		String  2 
	// ---------------------------------------------------------------------
	public LData retvEaiCrdBilBasInfForPaging(LData iCrdBilBasInf) throws LException {		
		
		LData 			rCrdBilBasInf 	= new LData();
		LinkHttpAdaptor httpAdaptor 	= new LinkHttpAdaptor();		
		
		// 인터페이스ID 	: UBE_2_CXWS00001
		// 거래코드 		: UBE0100141
		// 연계구분 		: EAI
		// 인터페이스명 	: 오픈뱅킹API 카드청구기본정보조회 
		try {
			
			rCrdBilBasInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBE_2_CXWS00001", iCrdBilBasInf);
			
		} catch (LBizException e) {
			BizExceptionPitcher.throwException(e);
		} catch (LException e) {
			BizExceptionPitcher.throwException(e);
		}
		
		return rCrdBilBasInf;
	}

	// 대내EAI 카드청구상세정보조회(홈웹서비스)
	// 고객식별자/고객관리번호 => 카드청구카드별청구목록 구하기
	// << EAI 연계정보 >>
	// 인터페이스ID 	: UBE_2_CXWS00002
	// 거래코드 		: UBE0100241
	// 연계구분 		: EAI
	// 인터페이스명 	: 오픈뱅킹API 카드청구상세정보조회 
	// input ---------------------------------------------------------------
	// 1      CST_IDF         	고객식별자  			String  10  
	// 2      CST_MG_NO         고객관리번호  			String  5   
	// 3      BIL_YM         	청구년월_V6  			String  6   
	// 4      BIL_SNO         	청구일련번호	  		Numeric 5   
	// 5      NEXT_INQ_KY_V20   다음조회키_V20  		String  20  
	// 6      PGE_SIZE         	페이지사이즈_N5  		Numeric  5 
	// output --------------------------------------------------------------
	// 1      CST_IDF         	고객식별자  			String  10  
	// 2      CST_MG_NO         고객관리번호  			String  5   
	// 3      BIL_YM         	청구년월_V6  			String  6   
	// 4      BIL_SNO         	청구일련번호	  		Numeric 5   
	// 5      NEXT_INQ_KY_V20   다음조회키값_V20  		String  20  
	// 6      NEXT_PAGE_YN      다음페이지존재여부_V1  	String  1   
	// 7    GRID              카드청구카드별청구목록  Group  1       
	// 8	┖  CAD_IDF_VL        	카드식별값_V4  		String  4   
	// 9  	┖  CRD_USE_DTE       	카드사용일자_V8  	String  8   
	// 10	┖  CRD_USE_HMS       	카드사용시각_V6  	String  6   
	// 11	┖  BIL_AMT_V13       	청구금액_V13  		String  13  
	// 12	┖  MMC_NM        		가맹점명_V40  		String  40  
	// 13	┖  CRD_SLC_FEE       	신용판매수수료_N13  Numeric 13  
	// 14	┖  CRD_GDS_DVCD      	상품구분코드_V2  	String  2 
	public LData retvEaiCrdBilDtlInfForPaging(LData iCrdBilDtlInf) throws LException {		
		
		LData 			rCrdBilDtlInf = new LData();		
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();		
		
		// 인터페이스ID 	: UBE_2_CXWS00002
		// 거래코드 		: UBE0100241
		// 연계구분 		: EAI
		// 인터페이스명 	: 오픈뱅킹API 카드청구상세정보조회 
		try {
			
			rCrdBilDtlInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBE_2_CXWS00002", iCrdBilDtlInf);
			
		} catch (LBizException e) {
			BizExceptionPitcher.throwException(e);
		} catch (LException e) {
			BizExceptionPitcher.throwException(e);
		}
		
		return rCrdBilDtlInf;
	}
	
	
	
	public LData retvEaiLstLnPdAccInf(LData iAccCtgInf) throws LException {
		
		LData 			rAccCtgInf = new LData();		
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();
		LData  linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		 
		// 인터페이스ID 	: UBD_2_KIWS00001
		// 거래코드 		: GEA0156442
		// 연계구분 		: MCI
		// 인터페이스명 	: 	  할부금융계좌목록조회
		// 연계처리계		: 상품처리계
		try {
			LLog.debug.println("■■■■■■■ EAI 할부금융계좌목록조회 START ■■■■■■■" , iAccCtgInf);
			rAccCtgInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_KIWS00001", iAccCtgInf);
		} catch (Exception e) {
			LLog.debug.println("■■■■■■■ Exception 처리하지 않는다 ■■■■■■■");
		}
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
	
			rAccCtgInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rAccCtgInf.setString("오류메시지"	    , sdtErrMsg			);
			rAccCtgInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rAccCtgInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ EAI 할부금융계좌목록조회 RETURN ■■■■■■■" , rAccCtgInf);
			
		return rAccCtgInf;
	}	

	public LData retvEaiLnPdAccTnhsForPaging(LData iLnPdAccTnhsInf) throws LException {		
		
		LData 			rLnPdAccTnhsInf = new LData();		
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();		
		
		LData  linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		
		// 인터페이스ID 	: UBD_2_KIWS00004
		// 거래코드 		: KIW72RF0S4
		// 연계구분 		: EAI
		// 인터페이스명 	: 마이데이터 API 대출상품계좌거래내역조회 
		try {
			LLog.debug.println("■■■■■■■ EAI 대출상품계좌거래내역조회 START ■■■■■■■" , iLnPdAccTnhsInf);
			
			rLnPdAccTnhsInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_KIWS00004", iLnPdAccTnhsInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rLnPdAccTnhsInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rLnPdAccTnhsInf.setString("오류메시지"	    , sdtErrMsg			);
			rLnPdAccTnhsInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rLnPdAccTnhsInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ EAI 대출상품계좌거래내역조회 RETURN ■■■■■■■" , rLnPdAccTnhsInf);
		} catch (LBizException e) {
			BizExceptionPitcher.throwException(e);
		} catch (LException e) {
			BizExceptionPitcher.throwException(e);
		}
		
		return rLnPdAccTnhsInf;
	}	
	
	
	public LData retvEaiLnPdAccPlInf(LData iLnPdAccPlInf) throws LException {		
		
		LData 			rLnPdAccPlInf = new LData();		
		LinkHttpAdaptor httpAdaptor = new LinkHttpAdaptor();		
		
		LData  linkResponseHeader 	= new LData();
		String sdtErrMsgCd          = "";
		String sdtErrMsg   			= "";
		String sdtLnkdOgtnGidNo	    = "";
		String sdtGidNo 			= "";
		
		// 인터페이스ID 	: UBD_2_KIWS00003
		// 거래코드 		: KIW72RF0S7
		// 연계구분 		: EAI
		// 인터페이스명 	: 마이데이터 API 대출상품계좌추가정보조회 
		try {
			LLog.debug.println("■■■■■■■ EAI 대출상품계좌추가정보조회 START ■■■■■■■" , iLnPdAccPlInf);
			
			rLnPdAccPlInf = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_KIWS00003", iLnPdAccPlInf);
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			sdtErrMsgCd 		= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		), ""); // 오류메시지코드
			sdtLnkdOgtnGidNo 	= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	), ""); // 연계원거래 GUID 
			sdtGidNo 			= LNullUtils.NVL(SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			), ""); // 거래 GUID
			sdtErrMsg 			= ErrorCodeMessageService.getInstance().getErrorCodeMessage(sdtErrMsgCd);
 
			rLnPdAccPlInf.setString("오류메시지코드"	, sdtErrMsgCd		);
			rLnPdAccPlInf.setString("오류메시지"	    , sdtErrMsg			);
			rLnPdAccPlInf.setString("연계원거래GUID"	, sdtLnkdOgtnGidNo	);
			rLnPdAccPlInf.setString("거래GUID"			, sdtGidNo			);
			
			LLog.debug.println("■■■■■■■ EAI 대출상품계좌추가정보조회 RETURN ■■■■■■■" , rLnPdAccPlInf);
		} catch (LBizException e) {
			BizExceptionPitcher.throwException(e);
		} catch (LException e) {
			BizExceptionPitcher.throwException(e);
		}
		
		return rLnPdAccPlInf;
	}	
}
