package com.kbcard.ubd.pbi.card;

import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.cpbi.cmn.UbdDmdRspPhsLdinCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdMdulSptFntCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.persistent.page.PageConstants;
import devonenterprise.ext.persistent.page.ScrollPageData;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.FormatUtil;
import devonenterprise.ext.util.StringUtil;
import devonenterprise.ext.util.TypeConvertUtil;

/**
 * 프로그램명 		: 마이데이터 API 제공 청구추가정보조회
 * 거래코드 		: UBD0100540
 * 설        명 	: 마이데이터 API 제공 청구추가정보조회 pbi
 * 
 * 연계시스템 		: 상품처리계 호출
 * 연계구분 		: MCI
 * 거래코드 		: GBC0123340
 * 인터페이스ID 	: UBD_1_GBCS00002
 * 인터페이스명 	: 마이데이터 API 제공 청구추가정보조회
 * 전문정보 input 	: GBC0123340_I
 * 전문정보 output 	: GBC0123340_O
 * 
 * 작성자 			: 김형구
 * 작성일자 		: 2021-06-03
 * 변경이력 	
 */
/**
 * @serviceID UBD0100540
 * @logicalName 마이데이터 API 제공 청구추가정보조회
 * @param LData
 *            	iRegtOpnbApiUsrP i청구상세정보입력
 * @return LData 
 * 			  	rRegtOpnbApiUsrP r청구상세정보결과
 * @exception LException. 
 * 
 *            ※
 */
public class MyDtCrdBilDtlInfPbc {
	
	// 전역변수 선언부
	LData tCrdBilDtlInf = new LData();	// 처리계 LData
	LData rCrdBilDtlInf = new LData();	// result LData
	
	/**
	 * 함수명 		: 마이데이터 API 제공 청구추가정보조회
	 * 작성자 		: 김형구
	 * 작성일자 	: 2021-06-03
	 */
	public LData retvMyDtApiCrdBilDtlInfForPaging(LData iCrdBilDtlInf) throws LException {
		
		LLog.debug.println( "###########################################" );
		LLog.debug.println( "[마이데이터 API 제공 청구추가정보조회] BPI START #####" );
		LLog.debug.println( "[API구분코드 = " + UBD_CONST.API_DTCD_BIL_DTL_INQ + " ]" );
		
		LLog.debug.println( "GUID      : [" + ContextHandler.getContextObject(ContextKey.GUID		) + "]");
		LLog.debug.println( "SITE_CODE : [" + ContextHandler.getContextObject(ContextKey.SITE_CODE	) + "]");
		LLog.debug.println( "TRAN_ID   : [" + ContextHandler.getContextObject(ContextKey.TRAN_ID	) + "]");
		
		LLog.debug.println( "[iCrdBilDtlInf]" + iCrdBilDtlInf );	

		int   itRowCnt		= 0;								// 데이터조회건수
		String sErrCode		= UBD_CONST.REP_CD_SUCCESS;			// 에러코드(00000)
		String sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS;		// 에러메시지(응답내역)
		String sErrCodePrc	= ""; // 에러코드(처리계)
		String sErrMsgPrc	= ""; // 에러메시지(처리계메시지)
		
		String sCICtt		= "";	// CI내용
		String sCstIdf 		= "";	// 고객식별자
		String sCstMgNo 	= "";	// 고객관리번호	
		String sMmsn		= "";   // 회원일련번호
		
// [input] ----------------------------------------------------------------------------
// [header]
// 1  Authorization 	접근토큰			String  1500
// 2  x-api-tran-id 	거래고유번호 		String  25
// [body]
// 1  ORG_CODE       	기관코드			String  10   
// 2  SEQNO      		결제순번  			String  4       
// 3  CHARGE_MONTH    	청구년월  			String  6        
// 4  NEXT_PAGE    		다음페이지기준개체 	String  1000   
// 5  LIMIT        		최대조회갯수 		Numeric 3      
// [output] ----------------------------------------------------------------------------
// [header]
// 1  x-api-tran-id 		거래고유번호 				String  25		
// [body]		
// 1  RSP_CODE	  			세부응답코드				String  5  
// 2  RSP_MSG				세부응답메시지  			String  450    
// 3  NEXT_PAGE    			다음페이지기준개체  		String  1000      
// 4  BILL_DETAIL_LIST_CNT  청구상세목록_cnt  			Numeric 3
// 5  BILL_DETAIL_LIST  청구상세목록  그룹  Group  3      
// 6   ┖  CARD_ID        		카드식별자  		String  64  
// 7   ┖  PAID_DTIME        	사용일시 	 		String  14  
// 8   ┖  TRANS_NO        		거래번호  			String  64     
// 9   ┖  PAID_AMT        		이용금액  			Numeric 18,3
// 10  ┖  CURRENCY_CODE	   	통화코드  			String  3
// 11  ┖  MERCHANT_NAME       	가맹점명  			String  75   
// 12  ┖  CREDIT_FEE_AMT      	신용판매수수료  	Numeric 15  
// 13  ┖  TOTAL_INSTALL_CNT	전체할부회차  		Numeric 5  
// 14  ┖  CUR_INSTALL_CNT     	현재할부회차  		Numeric 5  
// 15  ┖  BALANCE_AMT        	할부결제후잔액  	Numeric 15  
// 16  ┖  PROD_TYPE        	상품구분  			String  2 
// ------------------------------------------------------------------------------------------
		
		
		LData 		iCstidf 			= new LData(); 		// 고객식별자조회(input)
		LData 		tCstidf 			= new LData(); 		// 고객식별자조회(output)
		LData 		itmp_CrdBilDtlInf 	= new LData(); 		// 카드청구상세정보조회(input)
		LMultiData 	tmCrdBilDtlInf 		= new LMultiData(); // 카드청구상세정보조회(output)
		LData   	iRspCdMap			= new LData(); 		// 음답코드매핑조회(input)
		LData   	tRspCdMap			= new LData(); 		// 음답코드매핑조회(output)

		String sGwGuid 				= ContextHandler.getContextObject(ContextKey.GUID	);	// 게이트웨이거래식별번호
		String sTranId				= ContextHandler.getContextObject(ContextKey.TRAN_ID);	// 거래코드
		String sDmdCtt 				= ""; 					// 압축 및 암호화
		String sDmdCttCmpsEcy 		= ""; 					// 압축 및 암호화 모듈 호출
		String sRtvlTrsYN   		= "N"; 					// 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
		String sPrtlDcCd 			= "HDR";				// 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
		String sCdcMciGb			= "MCI"; 				// 호출시스템분기(CDC, MCI)
		String sTrsTgDtcd			= "01";					// 전송대상구분코드(01:마이데이터 사업자, 02:본인, 03:기관)
		String sClintIdiNo 			= ""; 					// 마이데이터클라이언트식별번호	
		String sInnMciLinkIntfId 	= "UBD_1_GBCS00002"; 	// MCI LINK 인터페이스ID
		
		String sdtErrMsgCd 			= ""; // 오류메시지코드
		String sdtLnkdOgtnGidNo 	= ""; // 연계원거래 GUID 
		String sdtGidNo 			= ""; // 거래 GUID	
		String sEmpNo				= ""; // 사용자ID

		LData tCustInf 				= new LData(); // 고객정보
		String sAccsTken			= ""; // 접근토큰
		String sMydtTrUno			= ""; // 마이데이터거래고유번호
		String sMydtUtzInsCd 		= ""; // 마이데이터이용기관코드
		
		LData linkRequestHeader 	= new LData();		
		LData linkResponseHeader 	= new LData();
		
		LData linkReqHeader 		= new LData();		
		LData linkResHeader 		= new LData();
		
		try {			
			
			// 1. 요청데이터 수신
			rCrdBilDtlInf = init_Input_Ldata(iCrdBilDtlInf);	// 수신정보 => 전송정보 초기화	
			
			// 1-1 헤더값 체크 -------------------------------------------------------------------------------------------
			UbdCommon 	ubdCommon 	= new UbdCommon(); // UbdCommon 공통 호출
			
			LData 		tdHeader 	= new LData();
			LData 		iHeader 	= new LData();
			
			iHeader.setString("apiDtcd", UBD_CONST.API_DTCD_BIL_DTL_INQ);	//API구분코드			
			tdHeader 		= ubdCommon.get_header(iHeader);
			
			sAccsTken		= tdHeader.getString("Authorization"); // 접근토큰
			sMydtTrUno		= tdHeader.getString("x-api-tran-id"); // 마이데이터거래고유번호
			sRtvlTrsYN   	= tdHeader.getString("x-api-type"	); // 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
			sPrtlDcCd 		= tdHeader.getString("potal-dc-cd"	); // 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
			sCdcMciGb 		= tdHeader.getString("tran_dv_cd"	); // 처리계시스템구분(CDC, MCI) - POR 일 경우에만 값이 들어옴. 
			sTrsTgDtcd		= tdHeader.getString("x-client-type"); // 전송대상구분코드(01:마이데이터 사업자, 02:본인, 03:기관)
			sCICtt			= tdHeader.getString("ci_ctt"		); // CI내용
			sCstIdf 		= tdHeader.getString("cst_idf"		); // 고객식별자
			sCstMgNo 		= "00000";							   // 고객관리번호
			sMydtUtzInsCd 	= tdHeader.getString("UTZ_INS_CD"	); // 마이데이터이용기관코드
			
			LLog.debug.println( "************* [ 헤더값 ] *****************");
			LLog.debug.println( " 접근토큰 = " 				+ sAccsTken		);
			LLog.debug.println( " 거래고유번호 = " 			+ sMydtTrUno	);
			LLog.debug.println( " 정기적전송여부 = " 		+ sRtvlTrsYN	);
			LLog.debug.println( " 마이데이터이용기관코드 = "+ sMydtUtzInsCd	);
			LLog.debug.println( " 포탈분기구분코드 = "		+ sPrtlDcCd		);
			LLog.debug.println( " 처리계시스템구분 = "		+ sCdcMciGb		);
			LLog.debug.println( " 전송대상구분코드 = "		+ sTrsTgDtcd	);
			LLog.debug.println( " CI내용 = "				+ sCICtt		);
			
			// 1-2. 응답헤더부 추가
			ContextUtil.setHttpResponseHeaderParam("x-api-tran-id", sMydtTrUno);	// 마이데이터거래고유번호

			if (!sPrtlDcCd.equals("POR")) {
				if(LNullUtils.isNone(iCrdBilDtlInf) || StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
					rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40002		 	 ); // 응답코드(40002)
					rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002	 	 ); // 응답메시지(헤더값 미존재)
					rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
					
					return rCrdBilDtlInf;			
				}	
			}
						
			// 2. 요청파라미터 체크 --------------------------------------------------------------------------------------
			// 2.1 수신정보검증
			Boolean bTlgFormatErr 	= false; // 전문포멧에러여부
			if(StringUtil.trimNisEmpty(iCrdBilDtlInf.getString("기관코드")) && !sPrtlDcCd.equals("POR")) {
				bTlgFormatErr = true;
			} else if(StringUtil.trimNisEmpty(iCrdBilDtlInf.getString("결제순번")) || !FormatUtil.isCharOfNum(iCrdBilDtlInf.getString("결제순번"))) {
				bTlgFormatErr = true;
			} else if(StringUtil.trimNisEmpty(iCrdBilDtlInf.getString("청구년월")) || !FormatUtil.isCharOfNum(iCrdBilDtlInf.getString("청구년월"))) {
				bTlgFormatErr = true;
			}   
			if(bTlgFormatErr) {
				rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40001		 	 ); // 응답코드(40001)
				rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 	 ); // 응답메시지(요청파라미터 오류)
				rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
				
				return rCrdBilDtlInf;			
			}
	
			
			// 2.2 거래고유번호 중복체크			   
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();

			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_BIL_DTL_INQ  ); // API구분코드
			
			// 포탈조회시 요청검증거래내역 중복 체크 검증
			if(sPrtlDcCd.equals("POR")) {				
				bRtn = cdcSptFntCpbc.dupDmdVlnTrVln(iDupDmd);      //중복요청거래 검증 결과 수신			
				LLog.debug.println( " 요청검증거래내역 = " + bRtn);
			} else {				
				bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
				LLog.debug.println( " 요청거래내역 = " + bRtn);
			}
			// TODO OPEN
//			if( ! bRtn ) { //false : 중복거래 시 
//			   throw new LException(); //예외처리 유발
//			}
			
			// 2.3 CI내용 정보 가져오기(포탈함수 호출) -----------------------------------------------------------------			
			LData iUsrRgInf = new LData();	// input
			LData rUsrRgInf = new LData();	// output
			
//			sCICtt = "32vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa932";

			if(sPrtlDcCd.equals("POR")) { //포탈거래일 경우에
				// CI내용, 고객식별자 헤더 데이터는 필수값임.
				if(StringUtil.trimNisEmpty(sCICtt) || StringUtil.trimNisEmpty(sCstIdf) ) {
					rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40002	); // 응답코드(40002)
					rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
					rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
					
					return rCrdBilDtlInf;			
				}					
			} else {
				// 포털에서 접근토큰으로 CI정보를 조회
				tCustInf 	= ubdCommon.select_cust_info(sAccsTken); 
				
				LLog.debug.println( " tCustInf = " + tCustInf);
				
				if(StringUtil.trimNisEmpty(tCustInf.getString("CI내용"))) {
					rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
					rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
					rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
					
					return rCrdBilDtlInf;
					
				} else {
					sCICtt		= tCustInf.getString("CI내용"		); // CI내용	
					sCstIdf 	= tCustInf.getString("고객식별자"	); // 고객식별자
					sCstMgNo 	= tCustInf.getString("고객관리번호"	); // 고객관리번호
				}				
			} 
			
			// 2.4 고객정보 존재여부 체크				
			iUsrRgInf.setString("CI내용", sCICtt);		
			rUsrRgInf = ubdCommon.retvCstCmn(sCICtt); // GAA고객기본 정보 조회 
			
			if(LNullUtils.isNone(rUsrRgInf)) {
				rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_NOTFOUND_40403	 	 		 ); // 응답코드(40403)
				rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 		 ); // 응답메시지(정보주체(고객) 미존재)
				rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
				
				return rCrdBilDtlInf;
				
			} else {
				sCstIdf 	= rUsrRgInf.getString("고객식별자");
				sCstMgNo 	= rUsrRgInf.getString("고객관리번호");
				sMmsn		= rUsrRgInf.getString("회원일련번호"); 
			}
				
			// 2.5 페이지사이즈 : 최대500 건 
			int iPgeSize 	= iCrdBilDtlInf.getInt("최대조회갯수");  
			
			if (TypeConvertUtil.toString(iPgeSize) == "" || iPgeSize <= 0) {
				iPgeSize = 500;
			}
			
			if(iPgeSize > 500) {
				rCrdBilDtlInf.setString("세부응답코드"			, UBD_CONST.REP_CD_TOO_MANY_42901		 		 ); // 응답코드(42901)
				rCrdBilDtlInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_TOO_MANY_42901	 		 ); // 응답메시지(정보제공 요청한도 초과)
				rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	));
				
				return rCrdBilDtlInf;
			}
//=======================================================================================================================================================
//=======================================================================================================================================================			
		
			
			LLog.debug.println( " MCI(상품처리계) 청구추가정보조회 호출 Start ==========" );
			
//			sCstIdf 	= "1154354531"; // 고객식별자
//			sCstMgNo 	= "00000";		// 고객관리번호

			
			try {

				// 호출시스템분기(CDC, MCI) 분기 로직
				if(sCdcMciGb.equals("CDC")) {
					
					LLog.debug.println("****** CDC 호출 START *******");
					
					// 입력부
					itmp_CrdBilDtlInf.setString	("결제년월"			, iCrdBilDtlInf.getString("청구년월"	));
					itmp_CrdBilDtlInf.setString	("청구일련번호"		, iCrdBilDtlInf.getString("결제순번"	));
					itmp_CrdBilDtlInf.setString	("고객식별자"		, sCstIdf								 );
					itmp_CrdBilDtlInf.setString	("고객관리번호"		, sCstMgNo								 );
					
					itmp_CrdBilDtlInf.setString(PageConstants.PGE_SIZE	, TypeConvertUtil.toString(iPgeSize));
					if (StringUtil.trimNisEmpty(iCrdBilDtlInf.getString("다음페이지기준개체"))) {
						itmp_CrdBilDtlInf.setString(PageConstants.NEXT_INQ_KY, "SQ_결제년월일=0,SQ_청구일련번호=0,SQ_명세서상세일련번호=0|NK_결제년월일=,NK_청구일련번호=,NK_명세서상세일련번호=");
					} else {
						itmp_CrdBilDtlInf.setString(PageConstants.NEXT_INQ_KY, iCrdBilDtlInf.getString("다음페이지기준개체"));
					}
					
					tmCrdBilDtlInf = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBilBasInfEbc", "selectMyDtCrdBilDtlInfForPaging", itmp_CrdBilDtlInf);
					
					LMultiData itmp_tmCrdBilBasInf = new LMultiData();
					for ( int anx = 0; anx < tmCrdBilDtlInf.getDataCount(); anx++ ) { 		
						
						LData ta_CrdBilDtlInf 	= new LData(); // temp_그리드			
						LData ta_CrdBilDtlInfP 	= tmCrdBilDtlInf.getLData( anx );	
						
						sCstIdf 	= ta_CrdBilDtlInfP.getString("고객식별자"	); // 고객식별자
						sCstMgNo 	= ta_CrdBilDtlInfP.getString("고객관리번호"	); // 고객관리번호
						
						/**
						 * 마스킹된 가맹점명 : (21.01.22) 설계서 수정 의견반영  가맹점명의 50%이내 마스킹
	                     * ① 가맹점명이 1자(한글/영문/숫자/특수문자 포함)인 경우 마스킹
	                     * ② 2자이상 짝수 : 가맹점명의 뒷자리부터 전체 글자수의 50%에 해당하는 글자수 만큼 마스킹
	                     * ③ 2자이상 홀수 : 전체 글자수에서 1을 차감한 후 가맹점명의 뒷자리부터 전체 글자수의 50%에 해당하는  글자수 만큼 마스킹
						 */
						String sMmcNm = ubdCommon.convertMaskMmcNm(ta_CrdBilDtlInfP.getString("가맹점명"));
						
						ta_CrdBilDtlInf.setString	 ("카드식별자"			, ta_CrdBilDtlInfP.getString	("카드대체번호" 			)); //카드대체번호
						ta_CrdBilDtlInf.setString	 ("사용일시"			, ta_CrdBilDtlInfP.getString	("카드사용년월일" 			)); //카드사용년월일
						ta_CrdBilDtlInf.setString	 ("거래번호"			, ta_CrdBilDtlInfP.getString	("매출전표번호"				)); //전표번호_V64
						ta_CrdBilDtlInf.setBigDecimal("이용금액"			, ta_CrdBilDtlInfP.getBigDecimal("청구원금" 				)); //청구금액_N18_3
						ta_CrdBilDtlInf.setString	 ("통화코드"			, ta_CrdBilDtlInfP.getString	("통화코드" 				)); //통화코드
						ta_CrdBilDtlInf.setString	 ("가맹점명"			, sMmcNm								  		  			 ); //가맹점명_V50
						ta_CrdBilDtlInf.setBigDecimal("신용판매수수료"		, ta_CrdBilDtlInfP.getBigDecimal("청구수수료"				)); //청구수수료_N15
						ta_CrdBilDtlInf.setBigDecimal("전체할부회차"		, ta_CrdBilDtlInfP.getBigDecimal("할부개월수"				)); //할부개월수
						ta_CrdBilDtlInf.setBigDecimal("현재할부회차"		, ta_CrdBilDtlInfP.getBigDecimal("청구회차"					)); //청구회차
						ta_CrdBilDtlInf.setBigDecimal("할부결제후잔액"		, ta_CrdBilDtlInfP.getBigDecimal("분할전표청구후잔액"		)); //분할전표청구후잔액
						ta_CrdBilDtlInf.setString	 ("상품구분"			, ta_CrdBilDtlInfP.getString	("명세서전표분류구분코드" 	)); //명세서전표분류구분코드
						
						itmp_tmCrdBilBasInf.addLData(ta_CrdBilDtlInf);
						
						ta_CrdBilDtlInfP = null;					
					} 			
					
					rCrdBilDtlInf.set("청구상세목록", itmp_tmCrdBilBasInf);	// LData에 LMultiData 추가하기
					
					itRowCnt = tmCrdBilDtlInf.getDataCount();	// 데이터조회건수
					rCrdBilDtlInf.setInt("청구상세목록_cnt", itRowCnt);	
					
					if(itRowCnt == 0){
						sErrCode	= UBD_CONST.REP_CD_NOTFOUND_40401; 				// 응답코드(40401)
						sErrMsg		= UBD_CONST.REP_CD_MSG_NOTFOUND_40401; 			// 응답메시지(NOTFOUND)	
						sErrCodePrc	= sdtErrMsgCd; 									// 에러코드(처리계)
						sErrMsgPrc  = UBD_CONST.REP_CD_MSG_NOTFOUND_40401; 			// NOTFOUND	
					} else {
						if(sdtErrMsgCd.equals("N0000000")) {
							sErrCode	= UBD_CONST.REP_CD_SUCCESS; 					// 응답코드(00000)
							sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS; 				// 응답메시지(성공)
							sErrCodePrc	= sdtErrMsgCd; 									// 에러코드(처리계)
							sErrMsgPrc  = UBD_CONST.REP_CD_MSG_SUCCESS; 				// 응답메시지(성공)
						} else {
							sErrCodePrc	= sdtErrMsgCd; // 에러코드(처리계)
						}
					}
										
					rCrdBilDtlInf.setString("세부응답코드"		, sErrCode	); // 응답코드
					rCrdBilDtlInf.setString("세부응답메시지"	, sErrMsg	); // 응답메시지
					if(DataConvertUtil.equals(ScrollPageData.getNextYn(), "Y")){
						rCrdBilDtlInf.setString("다음페이지기준개체"	, ScrollPageData.getNextKey() ); // 다음조회키
					} else {
						rCrdBilDtlInf.setString("다음페이지기준개체"	, ""		); // 다음조회키
					}		
					
				} else {
					
					// MCI 호출 로직 ================================
					LLog.debug.println("****** MCI 호출 START *******");
					
					// 입력부
					itmp_CrdBilDtlInf.setString	("CI내용"			, sCICtt								 );
					itmp_CrdBilDtlInf.setString	("결제년월"			, iCrdBilDtlInf.getString("청구년월"	));
					itmp_CrdBilDtlInf.setInt	("청구일련번호"		, TypeConvertUtil.parseToInteger(iCrdBilDtlInf.getString("결제순번")));
					if (StringUtil.trimNisEmpty(iCrdBilDtlInf.getString("다음페이지기준개체"))) {
						itmp_CrdBilDtlInf.setString("다음조회키_V1000", "SQ_결제년월일=0,SQ_청구일련번호=0,SQ_명세서상세일련번호=0|NK_결제년월일=,NK_청구일련번호=,NK_명세서상세일련번호=");
					} else {
						itmp_CrdBilDtlInf.setString("다음조회키_V1000", iCrdBilDtlInf.getString("다음페이지기준개체"));
					}
					itmp_CrdBilDtlInf.setInt	("페이지사이즈_N5"	, iPgeSize		); 
				
					LLog.debug.println("itmp_CrdBilDtlInf = " + itmp_CrdBilDtlInf);
					
					tCrdBilDtlInf = (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciCrdBilDtlInfForPaging", itmp_CrdBilDtlInf);
					
					sCstIdf 	= tCrdBilDtlInf.getString("고객식별자"		); // 고객식별자
					sCstMgNo 	= tCrdBilDtlInf.getString("고객관리번호"	); // 고객관리번호
					
					for ( int anx = 0; anx < tCrdBilDtlInf.getLMultiData("그리드").getDataCount(); anx++ ) { 
						
						LData 	ta_CrdBilDtlInf 	= new LData(); // temp_그리드
						LData 	ta_CrdBilDtlInfP 	= tCrdBilDtlInf.getLMultiData("그리드").getLData( anx );
										
						/**
						 * 마스킹된 가맹점명 : (21.01.22) 설계서 수정 의견반영  가맹점명의 50%이내 마스킹
                         * ① 가맹점명이 1자(한글/영문/숫자/특수문자 포함)인 경우 마스킹
                         * ② 2자이상 짝수 : 가맹점명의 뒷자리부터 전체 글자수의 50%에 해당하는 글자수 만큼 마스킹
                         * ③ 2자이상 홀수 : 전체 글자수에서 1을 차감한 후 가맹점명의 뒷자리부터 전체 글자수의 50%에 해당하는  글자수 만큼 마스킹
						 */
						String sMmcNm = ubdCommon.convertMaskMmcNm(ta_CrdBilDtlInfP.getString("가맹점명"));
						
						ta_CrdBilDtlInf.setString	 ("카드식별자"			, ta_CrdBilDtlInfP.getString	("카드대체번호" 			)); //카드대체번호
						ta_CrdBilDtlInf.setString	 ("사용일시"			, ta_CrdBilDtlInfP.getString	("카드사용년월일" 			)); //카드사용년월일
						ta_CrdBilDtlInf.setString	 ("거래번호"			, ta_CrdBilDtlInfP.getString	("전표번호_V64" 			)); //전표번호_V64
						ta_CrdBilDtlInf.setBigDecimal("이용금액"			, ta_CrdBilDtlInfP.getBigDecimal("청구금액_N18_3" 			)); //청구금액_N18_3
						ta_CrdBilDtlInf.setString	 ("통화코드"			, ta_CrdBilDtlInfP.getString	("통화코드" 				)); //통화코드
						ta_CrdBilDtlInf.setString	 ("가맹점명"			, sMmcNm								  		  			 ); //가맹점명_V50
						ta_CrdBilDtlInf.setBigDecimal("신용판매수수료"		, ta_CrdBilDtlInfP.getBigDecimal("청구수수료"				)); //청구수수료_N15
						ta_CrdBilDtlInf.setBigDecimal("전체할부회차"		, ta_CrdBilDtlInfP.getBigDecimal("할부개월수"				)); //할부개월수
						ta_CrdBilDtlInf.setBigDecimal("현재할부회차"		, ta_CrdBilDtlInfP.getBigDecimal("청구회차"					)); //청구회차
						ta_CrdBilDtlInf.setBigDecimal("할부결제후잔액"		, ta_CrdBilDtlInfP.getBigDecimal("분할전표청구후잔액"		)); //분할전표청구후잔액
						ta_CrdBilDtlInf.setString	 ("상품구분"			, ta_CrdBilDtlInfP.getString	("명세서전표분류구분코드" 	)); //명세서전표분류구분코드
												
						tmCrdBilDtlInf.addLData(ta_CrdBilDtlInf);
						
						ta_CrdBilDtlInfP = null;
						
					} 
					rCrdBilDtlInf.set("청구상세목록",tmCrdBilDtlInf);	// LData에 LMultiData 추가하기
										
					itRowCnt = tCrdBilDtlInf.getLMultiData("그리드").getDataCount();	// 데이터조회건수
					rCrdBilDtlInf.setInt("청구상세목록_cnt", itRowCnt);	
					//-------------------------------------------------------------------------------------------------------------------------------
					
					linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
					sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // 오류메시지코드
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID 
					sdtGidNo 			= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			); // 거래 GUID

					LLog.debug.println( " linkResponseHeader= " + linkResponseHeader);
					LLog.debug.println( " sdtErrMsgCd 		= " + sdtErrMsgCd 		);
					LLog.debug.println( " sdtLnkdOgtnGidNo 	= " + sdtLnkdOgtnGidNo 	);
					LLog.debug.println( " sdtGidNo 			= " + sdtGidNo 			);
					
	//				String _next_page_exis_yn 	= ScrollPageData.getNextYn();
	//				String _next_key 			= ScrollPageData.getNextKey();
	//				
	//				rCrdBilBasInf.setString("다음페이지존재여부_V1"	, _next_page_exis_yn);
	//				rCrdBilBasInf.setString("다음조회키_V40"		, _next_key			);
	
					
					LLog.debug.println( " itmp_CrdBilDtlInf = " + itmp_CrdBilDtlInf );
					LLog.debug.println( " tCrdBilDtlInf 	= " + tCrdBilDtlInf 	);
										
					if(itRowCnt == 0){
						sErrCode	= UBD_CONST.REP_CD_NOTFOUND_40401; 					// 응답코드(40401)
						sErrMsg		= UBD_CONST.REP_CD_MSG_NOTFOUND_40401; 				// 응답메시지(NOTFOUND)
						sErrCodePrc	= sdtErrMsgCd; 										// 에러코드(처리계)
						sErrMsgPrc  = UBD_CONST.REP_CD_MSG_NOTFOUND_40401; 				// 응답메시지(NOTFOUND)
					} else {
						if(sdtErrMsgCd.equals("N0000000")) {
							sErrCode	= UBD_CONST.REP_CD_SUCCESS; 					// 응답코드(00000)
							sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS; 				// 응답메시지(성공)
							sErrCodePrc	= sdtErrMsgCd; 									// 에러코드(처리계)
							sErrMsgPrc  = UBD_CONST.REP_CD_MSG_SUCCESS; 				// 응답메시지(성공)
						} else {
							sErrCodePrc	= sdtErrMsgCd; // 에러코드(처리계)
						}
					}
								
					rCrdBilDtlInf.setString("세부응답코드"			, sErrCode									 ); // 응답코드
					rCrdBilDtlInf.setString("세부응답메시지"		, sErrMsg									 ); // 응답메시지
					
					if(tCrdBilDtlInf.getString("다음존재여부_V1").equals("Y")) {
						rCrdBilDtlInf.setString("다음페이지기준개체"	, tCrdBilDtlInf.getString("다음조회키_V1000"));
					} else {
						rCrdBilDtlInf.setString("다음페이지기준개체"	, ""										 );
					}
					
					LLog.debug.println( " rCrdBilDtlInf = " + rCrdBilDtlInf );
					LLog.debug.println( " MCI(상품처리계) 청구추가정보조회 호출 End ==========" );
				
				}
				
				LLog.debug.println( " 고객식별자 			 = " + sCstIdf 		);
				LLog.debug.println( " 고객관리번호 			 = " + sCstMgNo 	);
				LLog.debug.println( " [return] RowCount 	 = " + itRowCnt 	);
				LLog.debug.println( " [return] iCrdBilDtlInf = " + iCrdBilDtlInf);
				LLog.debug.println( " [return] rCrdBilDtlInf = " + rCrdBilDtlInf);
				LLog.debug.println( "=========================================" );
				
			} catch (LBizException e) {
				
				LLog.debug.println("MCI호출 LBizException ");
				
				String rErrCode = e.getCode();// 에러코드(처리계)
				//String rErrMsg 	= e.getTrmlMsg();
				String rErrMsg = ErrorCodeMessageService.getInstance().getErrorCodeMessage(rErrCode);// 에러메시지(처리계메시지)
								
				// 요청내역관리 오류코드 세팅
				sErrCodePrc	= e.getCode();  
				sErrMsgPrc 	= ErrorCodeMessageService.getInstance().getErrorCodeMessage(rErrCode);
				
				// 응답코드매핑조회
				iRspCdMap.setString("오픈API언어구분코드"	, "KOR"		);
				iRspCdMap.setString("오픈API업무구분코드"	, "UBD"		); 
				iRspCdMap.setString("언어구분코드"			, "KOR"		);
				iRspCdMap.setString("메시지채널구분코드"	, "01"		);	// 01(단말)
				iRspCdMap.setString("오류메시지코드"		, rErrCode	);		
				iRspCdMap.setString("오류메시지출력내용"	, rErrMsg	);		
				iRspCdMap.setString("처리계호출방식"		, "MCI"		); // 처리계호출방식(CDC, MCI, EAI)  
				
				// 응답코드 매핑조회 오픈API응답코드와 처리계응답코드간 매핑된 응답코드를 반환한다.
//				tRspCdMap 	= (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MdulSptFntCpbc", "retvRspCdMapping", iRspCdMap);  

				UbdMdulSptFntCpbc mdulSptFntCpbc = new UbdMdulSptFntCpbc();
				tRspCdMap = mdulSptFntCpbc.retvRspCdMapping(iRspCdMap);
				
				sErrCode	= tRspCdMap.getString("오픈API응답코드"			); // 응답코드
				sErrMsg  	= tRspCdMap.getString("오픈API응답메시지내용"	); // 응답메시지(응답내역)
				
				rCrdBilDtlInf.setString("세부응답코드"			, sErrCode							  			 ); // 응답코드
				rCrdBilDtlInf.setString("세부응답메시지"		, sErrMsg							  			 ); // 응답메시지
				rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체"	)); 
				
				return rCrdBilDtlInf;
				
			} catch (LException e) {
				
				LLog.debug.println("MCI호출 LException ");
				
				// 요청내역관리 오류코드 세팅
				sErrCode	= UBD_CONST.REP_CD_SERVER_ERR_50001; 
				sErrMsg		= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				sErrCodePrc	= UBD_CONST.REP_CD_SERVER_ERR_50001;  
				sErrMsgPrc 	= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				rCrdBilDtlInf.setString("세부응답코드"			, sErrCode										); // 응답코드(50001)
				rCrdBilDtlInf.setString("세부응답메시지"		, sErrMsg										); // 응답메시지(시스템장애)
				rCrdBilDtlInf.setString("다음페이지기준개체"	, iCrdBilDtlInf.getString("다음페이지기준개체" )); 	
				
				return rCrdBilDtlInf;
			}
			
		} finally {

			/**
			 * 마이데이터 요청내역관리/요청검증내역관리 비동기방식
			 * 1. 이용기관의 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청내역관리
			 * 2. KB포탈이 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청검증내역관리
			 */
			
			String sPrcMciInsGb = "N"; // 요청내역상세 - MCI insert 입력여부
			String sPrcEaiInsGb = "N"; // 요청내역상세 - EAI insert 입력여부
			
			LLog.debug.println(" ========== 마이데이터 요청내역관리/요청검증내역관리 비동기방식 ========= ");
			LLog.debug.println("sErrCodePrc = " + sErrCodePrc);

			if(sCdcMciGb.equals("CDC")) {
				sdtLnkdOgtnGidNo  	= "";
				sPrcMciInsGb 		= "N"; // 요청상세내역 미입력 처리.
			} else {
				// MCI, EAI 일 경우에만 입력 처리함.
				linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);			
				if(!LNullUtils.isNone(linkResponseHeader)) {
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID
					
					if(!StringUtil.trimNisEmpty(sdtLnkdOgtnGidNo)) {
						sPrcMciInsGb 		= "Y"; // 요청상세내역 입력 처리.
					}
				}
			} 
			
			UbdDmdRspPhsLdinCpbc AsyncRunner = new UbdDmdRspPhsLdinCpbc();
			
			LData input 	= new LData();
			LData output 	= new LData();
			LData iMciInput = new LData();
			LData rMciInput = new LData();
			LData iEaiInput = new LData();
			LData rEaiInput = new LData();
			LData lEncInf 	= new LData();
			
			sErrCode 	= rCrdBilDtlInf.getString("세부응답코드");	
			sErrMsg 	= rCrdBilDtlInf.getString("세부응답메시지");	

			lEncInf.setString("거래고유번호"				, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_BIL_DTL_INQ);
			lEncInf.setString("포탈분기구분코드"			, sPrtlDcCd						);
			lEncInf.setString("처리계시스템구분"			, sCdcMciGb						);
			lEncInf.setString("CI내용"						, sCICtt						);
			lEncInf.setString("고객식별자"					, sCstIdf						);
			lEncInf.setString("고객관리번호"				, sCstMgNo						);
			lEncInf.setString("마이데이터정기전송여부"		, sRtvlTrsYN					);			
			lEncInf.setString("오픈API응답코드"				, sErrCode						);
			lEncInf.setString("오픈API응답메시지내용"		, sErrMsg						);
			lEncInf.setString("MCI오류메시지코드"			, sErrCodePrc					);
			lEncInf.setString("MCI오류메시지출력내용"		, sErrMsgPrc					);
			lEncInf.setString("EAI오류메시지코드"			, ""							);
			lEncInf.setString("EAI오류메시지출력내용"		, ""							);
			lEncInf.setString("MCI원거래GUID"				, sdtLnkdOgtnGidNo				);
			lEncInf.setString("EAI원거래GUID"				, ""							);
			lEncInf.setString("MCI인터페이스ID"				, sInnMciLinkIntfId				);
			lEncInf.setString("EAI인터페이스ID"				, ""							);
			lEncInf.setString("시스템최종갱신식별자"		, sTranId						);			
			lEncInf.setString("MCI요청상세입력여부"			, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"			, sPrcEaiInsGb					);
			lEncInf.setString("마이데이터전송대상구분코드"	, sTrsTgDtcd					);	
			lEncInf.setString("마이데이터클라이언트식별번호", sClintIdiNo					);	
			
			input 		= reset_Req_Ldata	 (iCrdBilDtlInf		);
			output 		= reset_Rsp_Ldata	 (rCrdBilDtlInf		);
			iMciInput 	= reset_Req_Mci_Ldata(itmp_CrdBilDtlInf	);
			rMciInput 	= reset_Rsp_Mci_Ldata(tCrdBilDtlInf		);
				
//			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf, "L");
			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();

			
			LLog.debug.println( "=======================================" );		
			LLog.debug.println( "[마이데이터API 청구추가정보조회] End ============" );
			LLog.debug.println( "=======================================" );
			
		}
		
		return rCrdBilDtlInf;
		
	}
	
	/**
	 * @serviceID initRtnSetting
	 * @logicalName 전문 초기화 세팅
	 * @param LData 
	 */	
	public LData initRtnSetting(LData input) throws LException {
		
		LData rRtnInf = new LData();
		
		rRtnInf.setString("카드식별자"			, " " 		); //카드식별자_V64
		rRtnInf.setString("사용일시"			, "00000000000000"); //사용일시_V14
		rRtnInf.setString("거래번호"			, "" 		); //거래번호_V64
		rRtnInf.setString("이용금액"			, "0" 		); //이용금액_N18,3
		rRtnInf.setString("통화코드"			, "KRW" 	); //통화코드_V3
		rRtnInf.setString("가맹점명"			, ""		); //가맹점명_V75
		rRtnInf.setString("신용판매수수료"		, "0"	 	); //신용판매수수료_N15
		rRtnInf.setString("전체할부회차"		, "0"	 	); //전체할부회차_N5
		rRtnInf.setString("현재할부회차"		, "0"	 	); //현재할부회차_N5
		rRtnInf.setString("할부결제후잔액"		, "0"	 	); //할부결제후잔액_N15
		rRtnInf.setString("상품구분코드"		, "00" 	 	); //상품구분코드_V2
		
		return rRtnInf;
	}
	
	static LData init_Input_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"				, input.getString("기관코드"			));
		output.setString("결제순번"				, input.getString("결제순번"			));
		output.setString("청구년월"				, input.getString("청구년월"			));		
		output.setString("다음페이지기준개체"	, input.getString("다음페이지기준개체"	));
		output.setString("최대조회갯수"			, input.getString("최대조회갯수"		));

		return output;
	}
	
	static LData reset_Req_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"				, input.getString("기관코드"			));
		output.setString("결제순번"				, input.getString("결제순번"			));
		output.setString("청구년월"				, input.getString("청구년월"			));		
		output.setString("다음페이지기준개체"	, input.getString("다음페이지기준개체"	));
		output.setString("최대조회갯수"			, input.getString("최대조회갯수"		));

		return output;
	}
	
	static LData reset_Rsp_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData

		output.setString("세부응답코드"				, input.getString("세부응답코드"		));		
		output.setString("세부응답메시지"			, input.getString("세부응답메시지"		));
		output.setString("다음페이지기준개체"		, input.getString("다음페이지기준개체"	));

		for ( int anx = 0; anx < input.getLMultiData("청구상세목록").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("청구상세목록").getLData( anx );
			
			ta_RspData.setString("카드식별자"		, ta_RspDataP.getString("카드식별자" 		)); 
			ta_RspData.setString("사용일시"			, ta_RspDataP.getString("사용일시" 			));
			ta_RspData.setString("거래번호"			, ta_RspDataP.getString("거래번호" 			));				
			ta_RspData.setString("이용금액"			, ta_RspDataP.getString("이용금액" 			)); 
			ta_RspData.setString("통화코드"			, ta_RspDataP.getString("통화코드" 			)); 
			ta_RspData.setString("가맹점명"			, ta_RspDataP.getString("가맹점명" 			));
			ta_RspData.setString("신용판매수수료"	, ta_RspDataP.getString("신용판매수수료"	));
			ta_RspData.setString("전체할부회차"		, ta_RspDataP.getString("전체할부회차"		));
			ta_RspData.setString("현재할부회차"		, ta_RspDataP.getString("현재할부회차"		));
			ta_RspData.setString("할부결제후잔액"	, ta_RspDataP.getString("할부결제후잔액"	));
			ta_RspData.setString("상품구분코드"		, ta_RspDataP.getString("상품구분코드"		));
					
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("청구상세목록", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	static LData reset_Req_Mci_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("CI내용"					, input.getString("CI내용"				));
		output.setString("결제년월"					, input.getString("결제년월"			));
		output.setString("청구일련번호"				, input.getString("청구일련번호"		));
		output.setString("다음조회키_V1000"			, input.getString("다음조회키_V1000"	));
		output.setString("페이지사이즈_N5"			, input.getString("페이지사이즈_N5"		));
		
		return output;
	}
	
	static LData reset_Rsp_Mci_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData
		
		output.setString("CI내용"					, input.getString("CI내용"				));
		output.setString("고객식별자"				, input.getString("고객식별자"			));
		output.setString("고객관리번호"				, input.getString("고객관리번호"		));
		output.setString("결제년월"					, input.getString("결제년월"			));
		output.setString("청구일련번호"				, input.getString("청구일련번호"		));
		output.setString("다음조회키_V1000"			, input.getString("다음조회키_V1000"	));
		output.setString("다음존재여부_V1"			, input.getString("다음존재여부_V1"		));
		
		for ( int anx = 0; anx < input.getLMultiData("그리드").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_카드청구기본목록			
			LData 		ta_RspDataP = input.getLMultiData( "그리드" ).getLData( anx );			
			
			ta_RspData.setString	("카드대체번호"				, ta_RspDataP.getString	   ("카드대체번호" 			));
			ta_RspData.setString	("카드사용년월일"			, ta_RspDataP.getString	   ("카드사용년월일" 		)); 
			ta_RspData.setString	("전표번호_V64"				, ta_RspDataP.getString	   ("전표번호_V64" 			)); 
			ta_RspData.setBigDecimal("청구금액_N18_3"			, ta_RspDataP.getBigDecimal("청구금액_N18_3"		));
			ta_RspData.setString	("통화코드"					, ta_RspDataP.getString	   ("통화코드" 				));
			ta_RspData.setString	("가맹점명"					, ta_RspDataP.getString	   ("가맹점명" 				));
			ta_RspData.setBigDecimal("청구수수료"				, ta_RspDataP.getBigDecimal("청구수수료" 			));
			ta_RspData.setBigDecimal("할부개월수"				, ta_RspDataP.getBigDecimal("할부개월수" 			));
			ta_RspData.setBigDecimal("청구회차"					, ta_RspDataP.getBigDecimal("청구회차" 				));
			ta_RspData.setBigDecimal("분할전표청구후잔액"		, ta_RspDataP.getBigDecimal("분할전표청구후잔액" 	));
			ta_RspData.setString	("명세서전표분류구분코드"	, ta_RspDataP.getString   ("명세서전표분류구분코드"	));
					
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("그리드", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
}
