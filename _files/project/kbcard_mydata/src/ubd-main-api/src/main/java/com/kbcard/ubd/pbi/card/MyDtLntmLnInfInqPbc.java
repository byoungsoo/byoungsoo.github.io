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
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LInterfaceException;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.StringUtil;

/**
 * 프로그램명 		: 마이데이터 API 제공 장기대출정보조회
 * 거래코드 		: UBD0101240
 * 설        명 	: 마이데이터 API제공 장기대출정보조회 pbi
 * 
 * 연계시스템 		: 상품처리계 호출
 * 연계구분 		: MCI
 * 거래코드 		: GEA0156441
 * 인터페이스ID 	: UBD_1_GEAS00001 
 * 인터페이스명 	: 마이데이터 API 제공 장기대출정보조회
 * 전문정보 input 	: GEA0156441_I
 * 전문정보 output 	: GEA0156441_O
 *       
 * 작성자 			: 임용택
 * 작성일자 		: 2021-06-10
 * 변경이력 	 
 */
/**
 * @serviceID UBD0101240
 * @logicalName 마이데이터 API 제공 장기대출정보조회
 * @param LData 
 *				iLntmLnInf i장기대출정보입력
 * @return LData 
 * 				rLntmLnInf r장기대출정보결과
 * @exception LException.
 * 
 *            ※ 
 */
public class MyDtLntmLnInfInqPbc {
	
	// 전역변수 선언부
	LData tLntmLnInf = new LData(); // 처리계 LData
	LData rLntmLnInf = new LData(); // result LData  
	
	/**
	 * 함수명 		: 마이데이터 API 제공 장기대출정보조회
	 * 작성자 		: 임용택
	 * 작성일자 	: 2021-06-10
	 */
	public LData retvMyDtLntmLnInf(LData iLntmLnInf) throws LException {
		
		LLog.debug.println( "##############################################" );
		LLog.debug.println( "[마이데이터 API 제공 장기대출정보조회] PBI START #####" );
		LLog.debug.println( "[API구분코드 = " + UBD_CONST.API_DTCD_LOANS_LTERM_INF_INQ + " ]" );
		
		LLog.debug.println( "GUID      : [" + ContextHandler.getContextObject(ContextKey.GUID		) + "]");
		LLog.debug.println( "SITE_CODE : [" + ContextHandler.getContextObject(ContextKey.SITE_CODE	) + "]");
		LLog.debug.println( "TRAN_ID   : [" + ContextHandler.getContextObject(ContextKey.TRAN_ID	) + "]");
		
		LLog.debug.println( "[iLntmLnInf]" + iLntmLnInf );	

		int   itRowCnt		= 0;											// 데이터조회건수
		String sErrCode		= UBD_CONST.REP_CD_SUCCESS;						// 에러코드(00000)
		String sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS;					// 에러메시지(응답내역)
		String sErrCodePrc	= ""; // 에러코드(처리계)
		String sErrMsgPrc	= ""; // 에러메시지(처리계메시지)
		
		String sCICtt		= ""; // CI내용
		String sCstIdf 		= ""; // 고객식별자
		String sCstMgNo 	= ""; // 고객관리번호	
		String sMmsn		= ""; // 회원일련번호
		String sStdYMD		= ""; // 조회기준년월일
		
// [input] ----------------------------------------------------------------------------
// [header]
// 1  Authorization 	접근토큰			String  1500
// 2  x-api-tran-id 	거래고유번호 		String  25
// [body]
// 1  ORG_CODE       	기관코드			String  10          
// 2  SEARCH_TIMESTAMP	조회타임스탬프		String  14     
// [output] ----------------------------------------------------------------------------
// [header]
// 1  x-api-tran-id 		거래고유번호 	String  25		
// [body]		
// 1 RSP_CODE				세부응답코드	String  5		
// 2 RSP_MSG				세부응답메시지	String  450		
// 3 SEARCH_TIMESTAMP		조회타임스탬프	String  14		
// 4 LONG_TERM_LIST_CNT		장기대출목록수	Numeric 3     	
// 5 LONG_TERM_LIST			장기대출목록	
// 6   ┖   LOAN_DTIME		대출일시		String 	14	
// 7   ┖   LOAN_CNT		대출회차		Numeric	5	
// 8   ┖   LOAN_TYPE		대출종류		String 	15	
// 9   ┖   LOAN_NAME		상품명			String 	300
//10   ┖   LOAN_AMT		이용금액		Numeric	18,3
//11   ┖   INT_RATE		이자율			Numeric	5,3
//12   ┖   EXP_DATE		만기일			String 	8	
//13   ┖   BALANCE_AMT		장기대출잔액	Numeric	18,3
//14   ┖   REPAY_METHOD	상환방법		String 	2	
//15   ┖   INT_AMT			상환액중이자	Numeric	18,3
// ------------------------------------------------------------------------------------------	
		
		LData 		iCstidf 		= new LData(); 		// 고객식별자조회(input)
		LData 		tCstidf 		= new LData(); 		// 고객식별자조회(output)
		LData 		itmp_LntmLnInf 	= new LData(); 		// 장기대출정보조회(input)
		LMultiData 	tmLntmLnInf 	= new LMultiData(); // 장기대출정보조회(output)
		LData   	iRspCdMap		= new LData(); 		// 음답코드매핑조회(input)
		LData   	tRspCdMap		= new LData(); 		// 음답코드매핑조회(output)
		
		String sGwGuid 				= ContextHandler.getContextObject(ContextKey.GUID	);	// 게이트웨이거래식별번호
		String sTranId				= ContextHandler.getContextObject(ContextKey.TRAN_ID);	// 거래코드
		String sDmdCtt 				= ""; 					// 압축 및 암호화
		String sDmdCttCmpsEcy 		= ""; 					// 압축 및 암호화 모듈 호출
		String sRtvlTrsYN   		= "N"; 					// 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
		String sPrtlDcCd 			= "HDR";				// 포탈분기구분코드 (HDR:신용정보원,POR:포탈)
		String sCdcMciGb			= "MCI"; 				// 호출시스템분기(CDC, MCI)
		String sTrsTgDtcd			= "01";					// 전송대상구분코드(01:마이데이터 사업자, 02:본인, 03:기관)
		String sClintIdiNo 			= ""; 					// 마이데이터클라이언트식별번호	
		String sInnMciLinkIntfId 	= "UBD_1_GEAS00001"; 	// MCI LINK 인터페이스ID
		
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
			
			// 1. 요청데이터 수신 ----------------------------------------------------------------------------------------
			rLntmLnInf = init_Input_Ldata(iLntmLnInf);	// 수신정보 => 전송정보 초기화
			
			// 1-1 헤더값 체크 -------------------------------------------------------------------------------------------
			UbdCommon 	ubdCommon 	= new UbdCommon(); // UbdCommon 공통 호출
			
			LData 		tdHeader 	= new LData();
			LData 		iHeader 	= new LData();
			
			iHeader.setString("apiDtcd", UBD_CONST.API_DTCD_LOANS_LTERM_INF_INQ);	//API구분코드			
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
				if(LNullUtils.isNone(iLntmLnInf) || StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
					rLntmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002	); // 응답코드(40002)
					rLntmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
					rLntmLnInf.setString("조회타임스탬프"	, ""									); // 조회타임스탬프
					
					return rLntmLnInf;			
				}	
			}
			
			// 2. 요청파라미터 체크 --------------------------------------------------------------------------------------
			// 2.1 수신정보검증
			Boolean bTlgFormatErr 	= false; // 전문포멧에러여부
			if(StringUtil.trimNisEmpty(iLntmLnInf.getString("기관코드")) && !sPrtlDcCd.equals("POR")) {
				bTlgFormatErr = true;
			}  
			if(bTlgFormatErr) {
				rLntmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001	); // 응답코드(40001)
				rLntmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001); // 응답메시지(요청파라미터 오류)
				rLntmLnInf.setString("조회타임스탬프"	, ""									); // 조회타임스탬프
				
				return rLntmLnInf;			
			}			
			
			// 2.2 거래고유번호 중복체크			  
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();

			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  		);
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  		); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_LOANS_LTERM_INF_INQ); // API구분코드
			
			// 포탈조회시 요청검증거래내역 중복 체크 검증
			if(sPrtlDcCd.equals("POR")) {				
				bRtn = cdcSptFntCpbc.dupDmdVlnTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
				LLog.debug.println( " 요청검증거래내역 = " + bRtn);
			} else {						
				bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
				LLog.debug.println( " 요청거래내역 = " + bRtn);
			}
			// TODO OPEN
			if( ! bRtn ) { //false : 중복거래 시 
			   throw new LException(); //예외처리 유발
			}
			
			// 2.3 CI내용 정보 가져오기(포탈함수 호출) -----------------------------------------------------------------
			LData iUsrRgInf = new LData();	// input
			LData rUsrRgInf = new LData();	// output
			
//			sCICtt = "32vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa932";

			if(sPrtlDcCd.equals("POR")) { //포탈거래일 경우에
				// CI내용, 고객식별자 헤더 데이터는 필수값임.
				if(StringUtil.trimNisEmpty(sCICtt) || StringUtil.trimNisEmpty(sCstIdf) ) {
					rLntmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002	); // 응답코드(40002)
					rLntmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
					rLntmLnInf.setString("조회타임스탬프"	, ""									); // 조회타임스탬프
					
					return rLntmLnInf;			
				}				
//				// 2.4 고객정보 존재여부 체크
//				iUsrRgInf.setString("CI내용", sCICtt);		
//				rUsrRgInf = ubdCommon.retvCstCmn(sCICtt);
//				
//				if(LNullUtils.isNone(rUsrRgInf)) {
//					rLntmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
//					rLntmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
//					rLntmLnInf.setString("조회타임스탬프"	, ""								 ); // 조회타임스탬프
//					
//					return rLntmLnInf;
//					
//				} else {
//					sCstIdf 	= rUsrRgInf.getString("고객식별자");
//					sCstMgNo 	= rUsrRgInf.getString("고객관리번호");
//					sMmsn		= rUsrRgInf.getString("회원일련번호"); 
//				}
			} else {
				// 포털에서 접근토큰으로 CI정보를 조회
				tCustInf 	= ubdCommon.select_cust_info(sAccsTken); 
				
				LLog.debug.println( " tCustInf = " + tCustInf);
				
				if(LNullUtils.isNone(tCustInf)) {
					rLntmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
					rLntmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
					rLntmLnInf.setString("조회타임스탬프"	, ""								 ); // 조회타임스탬프
					
					return rLntmLnInf;
					
				} else {
					sCICtt		= tCustInf.getString("CI내용"						); 	
					sCstIdf 	= tCustInf.getString("고객식별자"					); 
					sCstMgNo 	= tCustInf.getString("고객관리번호"					); 
					sTrsTgDtcd 	= tCustInf.getString("마이데이터전송대상구분코드"	);
					sClintIdiNo = tCustInf.getString("마이데이터클라이언트식별번호"	);
				}				
			} 
			

//=======================================================================================================================================================
//=======================================================================================================================================================			
			
			
			LLog.debug.println( " MCI(상품처리계) 장기대출정보조회 호출 Start ==========" );
			
//			sCstIdf 	= "1154354531"; // 고객식별자
//			sCstMgNo 	= "00000";		// 고객관리번호
			
			try {
				
				// 호출시스템분기(CDC, MCI) 부분 분기 로직
				if(sCdcMciGb.equals("CDC")) {
					
					LLog.debug.println("****** CDC 호출 START *******");
					
					//CDC 지원하지 않음................................
					
					rLntmLnInf.setString("세부응답코드"			, UBD_CONST.REP_CD_SERVER_ERR_50002		); // 응답코드(50002)
					rLntmLnInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002	); // 응답메시지(API 요청 처리 실패)
					rLntmLnInf.setString("조회타임스탬프"		, ""									); // 조회타임스탬프
					rLntmLnInf.setString("리볼빙여부"			, "false"								); // 리볼빙여부
					
					return rLntmLnInf;
					
				} else {				
					
					// MCI 호출 로직 ================================
					LLog.debug.println("****** MCI 호출 START *******");
					
					// 입력부					
					itmp_LntmLnInf.setString	("고객식별자"		, sCstIdf	);
					itmp_LntmLnInf.setString	("고객관리번호"		, sCstMgNo	);
										
					tLntmLnInf = (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciLntmLnInf", itmp_LntmLnInf);
				
					sCstIdf 	= tLntmLnInf.getString("고객식별자"		); // 고객식별자
					sCstMgNo 	= tLntmLnInf.getString("고객관리번호"	); // 고객관리번호
					sStdYMD		= tLntmLnInf.getString("기준년월일"		); // 기준년월일					
					
//					rLntmLnInf.setString("고객식별자"	, tLntmLnInf.getString("고객식별자"		)); // 고객식별자
//					rLntmLnInf.setString("고객관리번호"	, tLntmLnInf.getString("고객관리번호"	)); // 고객관리번호
//					rLntmLnInf.setString("기준년월일"	, tLntmLnInf.getString("기준년월일"		)); // 기준년월일
					
					for ( int anx = 0; anx < tLntmLnInf.getLMultiData("GEA0156441_그리드").getDataCount(); anx++ ) { 			
						LData ta_LntmLnInf 	= new LData(); // temp_그리드			
						LData ta_LntmLnInfP = tLntmLnInf.getLMultiData("GEA0156441_그리드").getLData( anx );		
											
						ta_LntmLnInf.setString	  ("대출일시"		, ta_LntmLnInfP.getString	 ("대출약정년월일" 	));
						ta_LntmLnInf.setString	  ("대출회차"		, ta_LntmLnInfP.getString	 ("대출회차" 		));
						ta_LntmLnInf.setString	  ("대출종류"		, ta_LntmLnInfP.getString	 ("대출상품분류명" 	));
						ta_LntmLnInf.setString	  ("상품명"			, ta_LntmLnInfP.getString	 ("대출상품명" 		));
						ta_LntmLnInf.setBigDecimal("이용금액"		, ta_LntmLnInfP.getBigDecimal("대출금액" 		));
						ta_LntmLnInf.setBigDecimal("이자율"			, ta_LntmLnInfP.getBigDecimal("카드론적용금리" 	));
						ta_LntmLnInf.setString	  ("만기일"			, ta_LntmLnInfP.getString	 ("대출만기년월일" 	));
						ta_LntmLnInf.setBigDecimal("장기대출잔액"	, ta_LntmLnInfP.getBigDecimal("대출잔액" 		));						
						ta_LntmLnInf.setString	  ("상환방법"		, ta_LntmLnInfP.getString	 ("상환방법구분코드"));
						ta_LntmLnInf.setBigDecimal("상환액중이자"	, ta_LntmLnInfP.getBigDecimal("상환이자" 		));						
						
						tmLntmLnInf.addLData(ta_LntmLnInf);
						
						ta_LntmLnInfP = null;						
					} 
					rLntmLnInf.set("장기대출목록", tmLntmLnInf);	// LData에 LMultiData 추가하기					
					
					itRowCnt = tLntmLnInf.getLMultiData("GEA0156441_그리드").getDataCount();	// 데이터조회건수
					rLntmLnInf.setInt("장기대출목록_cnt", itRowCnt);					
					
					//-------------------------------------------------------------------------------------------------------------------------------
					
					linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
					sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // 오류메시지코드
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID 
					sdtGidNo 			= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			); // 거래 GUID
	
					LLog.debug.println( " linkResponseHeader= " + linkResponseHeader);
					LLog.debug.println( " sdtErrMsgCd       = " + sdtErrMsgCd 		);
					LLog.debug.println( " sdtLnkdOgtnGidNo  = " + sdtLnkdOgtnGidNo 	);
					LLog.debug.println( " sdtGidNo          = " + sdtGidNo 			);
					
	//				String _next_page_exis_yn 	= ScrollPageData.getNextYn();
	//				String _next_key 			= ScrollPageData.getNextKey();
	//				
	//				rLntmLnInf.setString("다음페이지존재여부_V1", _next_page_exis_yn);
	//				rLntmLnInf.setString("다음조회키_V40"		, _next_key			);
					
					LLog.debug.println( " itmp_LntmLnInf = " 	+ itmp_LntmLnInf 	);
					LLog.debug.println( " tLntmLnInf 	= " 	+ tLntmLnInf 		);
					
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
				
					rLntmLnInf.setString("세부응답코드"		, sErrCode		); // 응답코드
					rLntmLnInf.setString("세부응답메시지"	, sErrMsg	 	); // 응답메시지
					rLntmLnInf.setString("조회타임스탬프"	, ""			); // 조회타임스탬프
					
					LLog.debug.println( " rLntmLnInf = " + rLntmLnInf );
					LLog.debug.println( " MCI(상품처리계) 결제정보조회 호출 End ==========" );
					
				}
				
				LLog.debug.println( " 고객식별자 		= " + sCstIdf 	);
				LLog.debug.println( " 고객관리번호 		= " + sCstMgNo 	);
				LLog.debug.println( " [return] RowCount = " + itRowCnt 	);
				LLog.debug.println( " [return] iLntmLnInf 	= " + iLntmLnInf	);
				LLog.debug.println( " [return] rLntmLnInf 	= " + rLntmLnInf 	);
				LLog.debug.println( "=============================================" );
			
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
				sErrMsg		= tRspCdMap.getString("오픈API응답메시지내용"	); // 응답메시지

				rLntmLnInf.setString("세부응답코드"		, sErrCode			); // 응답코드
				rLntmLnInf.setString("세부응답메시지"	, sErrMsg	 		); // 응답메시지
				rLntmLnInf.setString("조회타임스탬프"	, ""				); // 조회타임스탬프
				
				return rLntmLnInf;
				
			} catch (LInterfaceException lfe) {
							
				LLog.debug.println("MCI호출 LInterfaceException ");
				
				sErrCodePrc = lfe.getCode();
				sErrMsgPrc 	= lfe.getErrMsg();
				
				LLog.debug.println("sErrCodePrc = " + sErrCodePrc	);
				LLog.debug.println("sErrMsgPrc  = " + sErrMsgPrc	);
				
				// 요청내역관리 오류코드 세팅
				sErrCode	= UBD_CONST.REP_CD_SERVER_ERR_50002; 
				sErrMsg		= UBD_CONST.REP_CD_MSG_SERVER_ERR_50002;
				
				rLntmLnInf.setString("세부응답코드"		, sErrCode		); // 응답코드(50002)
				rLntmLnInf.setString("세부응답메시지"	, sErrMsg		); // 응답메시지(API 요청 처리 실패)
				rLntmLnInf.setString("조회타임스탬프"	, ""			); // 조회타임스탬프
				
				return rLntmLnInf;
			
			} catch (LException e) {
								
				LLog.debug.println("MCI호출 LException ");
				
				// 요청내역관리 오류코드 세팅
				sErrCode	= UBD_CONST.REP_CD_SERVER_ERR_50001; 
				sErrMsg		= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				sErrCodePrc	= UBD_CONST.REP_CD_SERVER_ERR_50001;  
				sErrMsgPrc 	= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				rLntmLnInf.setString("세부응답코드"		, sErrCode		); // 응답코드(50001)
				rLntmLnInf.setString("세부응답메시지"	, sErrMsg		); // 응답메시지(시스템장애)
				rLntmLnInf.setString("조회타임스탬프"	, ""			); // 조회타임스탬프
				
				return rLntmLnInf;
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
			
			sErrCode 	= rLntmLnInf.getString("세부응답코드");	
			sErrMsg 	= rLntmLnInf.getString("세부응답메시지");	

			lEncInf.setString("거래고유번호"				, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_LOANS_LTERM_INF_INQ);
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
			
			input 		= reset_Req_Ldata	 (iLntmLnInf		);
			output 		= reset_Rsp_Ldata	 (rLntmLnInf		);
			iMciInput 	= reset_Req_Mci_Ldata(itmp_LntmLnInf	);
			rMciInput 	= reset_Rsp_Mci_Ldata(tLntmLnInf		);
				
//			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf, "L");
			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();

			
			LLog.debug.println( "=======================================" );				
			LLog.debug.println( "[마이데이터API 장기대출정보조회] End ============" );
			LLog.debug.println( "=======================================" );
			
		}
		
		return rLntmLnInf;
		
	}
	
	/**
	 * @serviceID initRtnSetting
	 * @logicalName 전문 초기화 세팅
	 * @param LData 
	 */	
	public LData initRtnSetting(LData input) throws LException {
		
		LData rRtnInf = new LData();
		
		rRtnInf.setString("대출일시"		, "00000000000000"	); //대출일시_V14	
		rRtnInf.setString("대출회차"		, "0" 				); //대출회차
		rRtnInf.setString("이용금액"		, "0" 				); //이용금액
		rRtnInf.setString("이자율"			, "0" 				); //이자율
		rRtnInf.setString("만기일"			, "00000000" 		); //만기일
		rRtnInf.setString("장기대출잔액"	, "0"				); //장기대출잔액		
		rRtnInf.setString("상환액중이자"	, "0"				); //상환액중이자		
		
		return rRtnInf;
	}

	static LData init_Input_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("기관코드"			, input.getString("기관코드"		));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"));

		return output;
	}
	
	static LData reset_Req_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("기관코드"			, input.getString("기관코드"		));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"));

		return output;
	}
	
	static LData reset_Rsp_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData

		output.setString("세부응답코드"		, input.getString("세부응답코드"	));		
		output.setString("세부응답메시지"	, input.getString("세부응답메시지"	));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"	));

		for ( int anx = 0; anx < input.getLMultiData("장기대출목록").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("장기대출목록").getLData( anx );		
			
			ta_RspData.setString("대출일시"			, ta_RspDataP.getString("대출일시" 		)); 
			ta_RspData.setString("대출회차"			, ta_RspDataP.getString("대출회차" 		)); 	
			ta_RspData.setString("대출종류"			, ta_RspDataP.getString("대출종류" 		));
			ta_RspData.setString("상품명"			, ta_RspDataP.getString("상품명" 		)); 
			ta_RspData.setString("이용금액"			, ta_RspDataP.getString("이용금액" 		)); 	
			ta_RspData.setString("이자율"			, ta_RspDataP.getString("이자율" 		));
			ta_RspData.setString("만기일"			, ta_RspDataP.getString("만기일" 		)); 
			ta_RspData.setString("장기대출잔액"		, ta_RspDataP.getString("장기대출잔액" 	)); 	
			ta_RspData.setString("상환방법"			, ta_RspDataP.getString("상환방법"		));
			ta_RspData.setString("상환액중이자"		, ta_RspDataP.getString("상환액중이자" 	));
			
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("장기대출목록", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	static LData reset_Req_Mci_Ldata(LData input) {

		LData output = new LData();

		output.setString("고객식별자"				, input.getString("고객식별자"		));
		output.setString("고객관리번호"				, input.getString("고객관리번호"	));
				
		return output;
	}
	
	static LData reset_Rsp_Mci_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData

		output.setString("고객식별자"				, input.getString("고객식별자"		));
		output.setString("고객관리번호"				, input.getString("고객관리번호"	));
		output.setString("기준년월일"				, input.getString("기준년월일"		));
		
		for ( int anx = 0; anx < input.getLMultiData("GEA0156441_그리드").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_GEA0156441_그리드	
			LData 		ta_RspDataP = input.getLMultiData( "GEA0156441_그리드" ).getLData( anx );			
			
			ta_RspData.setString("대출번호"			, ta_RspDataP.getString("대출번호" 			)); 
			ta_RspData.setString("대출회차"			, ta_RspDataP.getString("대출회차" 			)); 
			ta_RspData.setString("대출약정년월일"	, ta_RspDataP.getString("대출약정년월일" 	)); 
			ta_RspData.setString("대출상품분류명"	, ta_RspDataP.getString("대출상품분류명" 	)); 
			ta_RspData.setString("대출상품명"		, ta_RspDataP.getString("대출상품명" 		)); 
			ta_RspData.setString("대출금액"			, ta_RspDataP.getString("대출금액" 			)); 
			ta_RspData.setString("카드론적용금리"	, ta_RspDataP.getString("카드론적용금리" 	)); 
			ta_RspData.setString("대출만기년월일"	, ta_RspDataP.getString("대출만기년월일" 	)); 
			ta_RspData.setString("대출잔액"			, ta_RspDataP.getString("대출잔액" 			)); 
			ta_RspData.setString("상환방법구분코드"	, ta_RspDataP.getString("상환방법구분코드" 	)); 
			ta_RspData.setString("상환이자"			, ta_RspDataP.getString("상환이자" 			)); 
					
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("GEA0156441_그리드", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	
}
