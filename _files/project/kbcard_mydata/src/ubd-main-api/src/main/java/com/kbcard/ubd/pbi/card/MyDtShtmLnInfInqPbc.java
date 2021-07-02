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
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.StringUtil;

/**
 * 프로그램명 		: 마이데이터 API 제공 단기대출정보조회
 * 거래코드 		: UBD0101140
 * 설        명 	: 마이데이터 API 제공 단기대출정보조회 pbi
 * 
 * 연계시스템 		: 상품처리계 호출
 * 연계구분 		: MCI
 * 거래코드 		: GBC0193041
 * 인터페이스ID 	: UBD_1_GBCS00006
 * 인터페이스명 	: 마이데이터 API 제공 단기대출정보조회
 * 전문정보 input 	: GBC0193041_I
 * 전문정보 output 	: GBC0193041_O
 * 
 * 작성자 			: 김형구
 * 작성일자 		: 2021-06-03
 * 변경이력 	
 */
/**
 * @serviceID UBD0101140
 * @logicalName 마이데이터 API 제공 단기대출정보조회
 * @param LData
 *            	iShtmLnInf i단기대출정보입력
 * @return LData 
 * 			  	rShtmLnInf r단기대출정보결과
 * @exception LException. 
 * 
 *           ※
 */
public class MyDtShtmLnInfInqPbc {
	
	// 전역변수 선언부
	LData tShtmLnInf = new LData();	// 처리계 LData
	LData rShtmLnInf = new LData();	// result LData
	
	/**
	 * 함수명 		: 마이데이터 API 제공 단기대출정보조회
	 * 작성자 		: 김형구
	 * 작성일자 	: 2021-06-03
	 */
	public LData retvMyDtShtmLnInf(LData iShtmLnInf) throws LException {
		
		LLog.debug.println( "###########################################" );
		LLog.debug.println( "[마이데이터 API 제공 단기대출정보조회] PBI START #####" );
		LLog.debug.println( "[API구분코드 = " + UBD_CONST.API_DTCD_LOANS_STERM_INF_INQ + " ]" );
		
		LLog.debug.println( "GUID      : [" + ContextHandler.getContextObject(ContextKey.GUID		) + "]");
		LLog.debug.println( "SITE_CODE : [" + ContextHandler.getContextObject(ContextKey.SITE_CODE	) + "]");
		LLog.debug.println( "TRAN_ID   : [" + ContextHandler.getContextObject(ContextKey.TRAN_ID	) + "]");
		
		LLog.debug.println( "[iShtmLnInf]" + iShtmLnInf );	

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
		// 1  Authorization 		접근토큰			String  1500
		// 2  x-api-tran-id 		거래고유번호 		String  25
		// [body]
		// 1  ORG_CODE       		기관코드			String  10          
		// 2  SEARCH_TIMESTAMP		조회타임스탬프		String  14     
		// [output] ----------------------------------------------------------------------------
		// [header]
		// 1  x-api-tran-id 		거래고유번호 		String  25		
		// [body]		
		// 1  RSP_CODE	  			세부응답코드		String  5      
		// 2  RSP_MSG				세부응답메시지  	String  450      
		// 3  SEARCH_TIMESTAMP		조회타임스탬프		String  14
		//
		// 4  SHORT_TERM_LIST_cnt 	단기대출목록_cnt	Numeric 3     	
		// 5  SHORT_TERM_LIST       단기대출목록  그룹  Group  3                               
		// 6   ┖  LOAN_DTIME     	이용일시			String  14           
		// 7   ┖  LOAN_AMT  	    이용금액  		    Numeric 15
		// 8   ┖  BALANCE_AMT      단기대출잔액  		Numeric 15              
		// 9   ┖  PAY_DUE_DATE  	결제예정일  		String  8
		// 10  ┖  INT_RATE      	이자율  		    Numeric 5,3              
		// ------------------------------------------------------------------------------------------	
		
		
		LData 		iCstidf 			= new LData(); 		// 고객식별자조회(input)
		LData 		tCstidf 			= new LData(); 		// 고객식별자조회(output)
		LData 		itmp_ShtmLnInf    	= new LData(); 		// 단기대출정보조회(input)
		LMultiData 	tmShtmLnInf 		= new LMultiData(); // 단기대출정보조회(output)
		LData   	iRspCdMap			= new LData(); 		// 음답코드매핑조회(input)
		LData   	tRspCdMap			= new LData(); 		// 음답코드매핑조회(output)

		String sGwGuid 				= ContextHandler.getContextObject(ContextKey.GUID	);	// 게이트웨이거래식별번호
		String sTranId				= ContextHandler.getContextObject(ContextKey.TRAN_ID);	// 거래코드
		String sDmdCtt 				= ""; 					// 압축 및 암호화
		String sDmdCttCmpsEcy 		= ""; 					// 압축 및 암호화 모듈 호출
		String sRtvlTrsYN   		= "N"; 					// 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
		String sPrtlDcCd 			= "HDR";				// 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
		String sCdcMciGb			= "MCI"; 				// 처리계시스템구분(CDC, MCI)
		String sTrsTgDtcd			= "01";					// 전송대상구분코드(01:마이데이터 사업자, 02:본인, 03:기관)
		String sClintIdiNo 			= ""; 					// 마이데이터클라이언트식별번호
		String sInnMciLinkIntfId 	= "UBD_1_GBCS00006"; 	// MCI LINK 인터페이스ID
		
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
			rShtmLnInf = init_Input_Ldata(iShtmLnInf);	// 수신정보 => 전송정보 초기화	
			
			// 1-1 헤더값 체크 -------------------------------------------------------------------------------------------
			UbdCommon 	ubdCommon 	= new UbdCommon(); // UbdCommon 공통 호출
			
			LData 		tdHeader 	= new LData();
			LData 		iHeader 	= new LData();
			
			iHeader.setString("apiDtcd", UBD_CONST.API_DTCD_LOANS_STERM_INF_INQ);	//API구분코드			
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
			
			if (!UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
				if(LNullUtils.isNone(iShtmLnInf) || StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
					rShtmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002	); // 응답코드(40002)
					rShtmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
					rShtmLnInf.setString("조회타임스탬프"	, ""									); // 조회타임스탬프
					
					return rShtmLnInf;			
				}	
			}
						
			// 2. 요청파라미터 체크 --------------------------------------------------------------------------------------
			// 2.1 수신정보검증
			Boolean bTlgFormatErr 	= false; // 전문포멧에러여부
			if(StringUtil.trimNisEmpty(iShtmLnInf.getString("기관코드")) && !UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
				bTlgFormatErr = true;
			}  
			if(bTlgFormatErr) {
				rShtmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001	); // 응답코드(40001)
				rShtmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001); // 응답메시지(요청파라미터 오류)
				rShtmLnInf.setString("조회타임스탬프"	, ""									); // 조회타임스탬프
				
				return rShtmLnInf;
			}				
			
			
			// 2.2 거래고유번호 중복체크			  
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();
			
			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_BIL_INF_INQ  ); // API구분코드
			
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
			
			if(sPrtlDcCd.equals("POR")) { //포탈거래일 경우에
				// CI내용, 고객식별자 헤더 데이터는 필수값임.
				if(StringUtil.trimNisEmpty(sCICtt) || StringUtil.trimNisEmpty(sCstIdf) ) {
					rShtmLnInf.setString("세부응답코드"			, UBD_CONST.REP_CD_BAD_REQUEST_40002	); // 응답코드(40002)
					rShtmLnInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002); // 응답메시지(헤더값 미존재)
					rShtmLnInf.setString("조회타임스탬프"		, ""								 ); // 조회타임스탬프
					
					return rShtmLnInf;			
				}					
			} else {
				// 포털에서 접근토큰으로 CI정보를 조회
				tCustInf 	= ubdCommon.select_cust_info(sAccsTken); 
				
				LLog.debug.println( " tCustInf = " + tCustInf);
				
				if(StringUtil.trimNisEmpty(tCustInf.getString("CI내용"))) {
					rShtmLnInf.setString("세부응답코드"			, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
					rShtmLnInf.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
					rShtmLnInf.setString("조회타임스탬프"		, ""								 ); // 조회타임스탬프
					
					return rShtmLnInf;
					
				} else {
					sCICtt		= tCustInf.getString("CI내용"		); // CI내용	
					sCstIdf 	= tCustInf.getString("고객식별자"	); // 고객식별자
					sCstMgNo 	= tCustInf.getString("고객관리번호"	); // 고객관리번호
				}				
			} 
			
			// 2.4 고객정보 존재여부 체크			
			LData iUsrRgInf = new LData();	// input
			LData rUsrRgInf = new LData();	// output
			
//			sCICtt = "32vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa93232vicmi2alskdf289qa932";

			iUsrRgInf.setString("CI내용", sCICtt);		
			rUsrRgInf = ubdCommon.retvCstCmn(sCICtt);
			
			if(LNullUtils.isNone(rUsrRgInf)) {
				rShtmLnInf.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
				rShtmLnInf.setString("세부응답메시지"	, UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
				rShtmLnInf.setString("조회타임스탬프"	, ""								 ); // 조회타임스탬프
				
				return rShtmLnInf;
				
			} else {
				sCstIdf 	= rUsrRgInf.getString("고객식별자");
				sCstMgNo 	= rUsrRgInf.getString("고객관리번호");
				sMmsn		= rUsrRgInf.getString("회원일련번호"); 
			}

//=======================================================================================================================================================
//=======================================================================================================================================================			

			try {

				// 호출시스템분기(CDC, MCI) 분기 로직
				if(sCdcMciGb.equals("CDC")) {
					
					// CDC 호출 로직 ================================
					LLog.debug.println("****** CDC 호출 START *******");
					
					// 입력부
					itmp_ShtmLnInf.setString	("CI내용"			, sCICtt		 );
					itmp_ShtmLnInf.setString	("고객식별자"		, sCstIdf		 );
					itmp_ShtmLnInf.setString	("고객관리번호"		, sCstMgNo		 );
					itmp_ShtmLnInf.setString	("회원일련번호"		, sMmsn  		 );
					
					LLog.debug.println("itmp_ShtmLnInf = " + itmp_ShtmLnInf);
					
					tmShtmLnInf = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtShtmLnInfEbc", "selectMyDtShtmLnInfForPaging", itmp_ShtmLnInf);
					
					LMultiData itmp_tmShtmLnInf = new LMultiData();
					for ( int anx = 0; anx < tmShtmLnInf.getDataCount(); anx++ ) { 		
						
						LData ta_ShtmLnInf 	= new LData(); // temp_그리드			
						LData ta_ShtmLnInfP = tmShtmLnInf.getLData( anx );	
						
//						sCstIdf 	= ta_ShtmLnInfP.getString("고객식별자"      ); // 고객식별자
//						sCstMgNo 	= ta_ShtmLnInfP.getString("고객관리번호"	); // 고객관리번호
						
						ta_ShtmLnInf.setString	     ("이용일시"	 , ta_ShtmLnInfP.getString	    ("이용일시"         )); //전표매출년월일||매출승인시각
						ta_ShtmLnInf.setBigDecimal	 ("이용금액"	 , ta_ShtmLnInfP.getBigDecimal	("매출금액" 		)); //매출금액
						ta_ShtmLnInf.setBigDecimal	 ("단기대출잔액" , ta_ShtmLnInfP.getBigDecimal	("회수후잔액"		)); //회수후잔액
						ta_ShtmLnInf.setString       ("결제예정일"	 , ta_ShtmLnInfP.getString      ("청구년월일" 		)); //청구년월일
						ta_ShtmLnInf.setBigDecimal	 ("이자율"	     , ta_ShtmLnInfP.getBigDecimal	("수수료율"  		)); //수수료율
						
						itmp_tmShtmLnInf.addLData(ta_ShtmLnInf);
						
						ta_ShtmLnInfP = null;					
					} 		
					rShtmLnInf.set("단기대출목록", itmp_tmShtmLnInf);	// LData에 LMultiData 추가하기
					
					itRowCnt = tmShtmLnInf.getDataCount();	// 데이터조회건수
					rShtmLnInf.setInt("단기대출목록_cnt", itRowCnt);
					
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
										
					rShtmLnInf.setString("세부응답코드"		, sErrCode	); // 응답코드
					rShtmLnInf.setString("세부응답메시지"	, sErrMsg	); // 응답메시지
					rShtmLnInf.setString("조회타임스탬프"	, ""    	); // 조회타임스탬프	
					
					LLog.debug.println( " rShtmLnInf = " + rShtmLnInf );
					LLog.debug.println("****** CDC 호출 End *******");
					
				} else {
					
					// MCI 호출 로직 ================================
					LLog.debug.println("****** MCI 호출 START *******");
					
					// 입력부
					itmp_ShtmLnInf.setString	("CI내용"		, sCICtt		 );
					itmp_ShtmLnInf.setString	("고객식별자"	, sCstIdf		 );
					itmp_ShtmLnInf.setString	("고객관리번호"	, sCstMgNo		 );
					itmp_ShtmLnInf.setString	("회원일련번호"	, sMmsn  		 );
					
					LLog.debug.println("itmp_ShtmLnInf = " + itmp_ShtmLnInf);
					
					tShtmLnInf = (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciShtmLnInf", itmp_ShtmLnInf);
					
//					sCstIdf 	= tShtmLnInf.getString("고객식별자"		); // 고객식별자
//					sCstMgNo 	= tShtmLnInf.getString("고객관리번호"	); // 고객관리번호
					
					for ( int anx = 0; anx < tShtmLnInf.getLMultiData("단기대출정보그리드").getDataCount(); anx++ ) { 
						
						LData 	ta_ShtmLnInf 	= new LData(); // temp_그리드
						LData 	ta_ShtmLnInfP 	= tShtmLnInf.getLMultiData("단기대출정보그리드").getLData( anx );
						
						ta_ShtmLnInf.setString	     ("이용일시"	 , ta_ShtmLnInfP.getString	    ("CA승인일시_V14"   )); //CA승인일시_V14
						ta_ShtmLnInf.setBigDecimal	 ("이용금액"	 , ta_ShtmLnInfP.getBigDecimal	("CA승인금액_N15"	)); //CA승인금액_N15
						ta_ShtmLnInf.setBigDecimal	 ("단기대출잔액" , ta_ShtmLnInfP.getBigDecimal	("CA잔액"			)); //CA잔액
						ta_ShtmLnInf.setString       ("결제예정일"	 , ta_ShtmLnInfP.getString      ("청구년월일1_V8"	)); //청구년월일1_V8
						ta_ShtmLnInf.setBigDecimal	 ("이자율"	     , ta_ShtmLnInfP.getBigDecimal	("수수료율_N5_3" 	)); //수수료율_N5_3
						
						tmShtmLnInf.addLData(ta_ShtmLnInf);
						
						ta_ShtmLnInfP = null;	
					
					} 
					rShtmLnInf.set("단기대출목록",tmShtmLnInf);	// LData에 LMultiData 추가하기
										
					itRowCnt = tShtmLnInf.getLMultiData("단기대출정보그리드").getDataCount();	// 데이터조회건수
					rShtmLnInf.setInt("단기대출목록_cnt", itRowCnt);
					
					//-------------------------------------------------------------------------------------------------------------------------------
					
					linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
					sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // 오류메시지코드
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID 
					sdtGidNo 			= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			); // 거래 GUID
					
					LLog.debug.println( " itmp_ShtmLnInf = " + itmp_ShtmLnInf );
					LLog.debug.println( " tShtmLnInf 	 = " + tShtmLnInf 	);
										
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
								
					rShtmLnInf.setString("세부응답코드"			, sErrCode			 ); // 응답코드
					rShtmLnInf.setString("세부응답메시지"		, sErrMsg			 ); // 응답메시지
					rShtmLnInf.setString("조회타임스탬프"	    , ""                 ); // 조회타임스탬프	
					
					LLog.debug.println( " rShtmLnInf = " + rShtmLnInf );
					LLog.debug.println("****** MCI(상품처리계) 호출 End *******");
				
				}
				
				LLog.debug.println( " 고객식별자 			 = " + sCstIdf 		);
				LLog.debug.println( " 고객관리번호 			 = " + sCstMgNo 	);
				LLog.debug.println( " 회원일련번호           = " + sMmsn  		);
				LLog.debug.println( " [return] RowCount 	 = " + itRowCnt 	);
				LLog.debug.println( " [return] iShtmLnInf    = " + iShtmLnInf   );
				LLog.debug.println( " [return] rShtmLnInf    = " + rShtmLnInf   );
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
				
				rShtmLnInf.setString("세부응답코드"			, sErrCode		); // 응답코드
				rShtmLnInf.setString("세부응답메시지"		, sErrMsg		); // 응답메시지
				rShtmLnInf.setString("조회타임스탬프"	    , ""    	    ); // 조회타임스탬프
				
				return rShtmLnInf;
				
			} catch (LException e) {
				
				LLog.debug.println("MCI호출 LException ");
				
				// 요청내역관리 오류코드 세팅
				sErrCode	= UBD_CONST.REP_CD_SERVER_ERR_50001; 
				sErrMsg		= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				sErrCodePrc	= UBD_CONST.REP_CD_SERVER_ERR_50001;  
				sErrMsgPrc 	= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				rShtmLnInf.setString("세부응답코드"			, sErrCode		 ); // 응답코드
				rShtmLnInf.setString("세부응답메시지"		, sErrMsg		 ); // 응답메시지
				rShtmLnInf.setString("조회타임스탬프"	    , ""             ); // 조회타임스탬프
				
				return rShtmLnInf;
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
			
			sErrCode 	= rShtmLnInf.getString("세부응답코드");	
			sErrMsg 	= rShtmLnInf.getString("세부응답메시지");	

			lEncInf.setString("거래고유번호"				, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_LOANS_STERM_INF_INQ);
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
			
			input 		= reset_Req_Ldata	 (iShtmLnInf		);
			output 		= reset_Rsp_Ldata	 (rShtmLnInf		);
			iMciInput 	= reset_Req_Mci_Ldata(itmp_ShtmLnInf	);
			rMciInput 	= reset_Rsp_Mci_Ldata(tShtmLnInf		);
			
//			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf, "L");
			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();
			
			LLog.debug.println( "=======================================" );		
			LLog.debug.println( "[마이데이터API 단기대출정보조회] End ============" );
			LLog.debug.println( "=======================================" );
			
		}
		
		return rShtmLnInf;
		
	}
	
	/**
	 * @serviceID initRtnSetting
	 * @logicalName 전문 초기화 세팅
	 * @param LData 
	 */	
	public LData initRtnSetting(LData input) throws LException {
		
		LData rRtnInf = new LData();
		
		rRtnInf.setString("조회타임스탬프"	, input.getString("조회타임스탬프"  ));
		
		return rRtnInf;
	}
	
	static LData init_Input_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"			, input.getString("기관코드"		));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"  ));

		return output;
	}
	
	static LData reset_Req_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"			, input.getString("기관코드"		));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"  ));

		return output;
	}
	
	static LData reset_Rsp_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData

		output.setString("세부응답코드"		, input.getString("세부응답코드"	));		
		output.setString("세부응답메시지"	, input.getString("세부응답메시지"	));
		output.setString("조회타임스탬프"	, input.getString("조회타임스탬프"  ));

		for ( int anx = 0; anx < input.getLMultiData("단기대출목록").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("단기대출목록").getLData( anx );
					
			ta_RspData.setString    ("이용일시"		, ta_RspDataP.getString    ("이용일시" 		    )); 
			ta_RspData.setBigDecimal("이용금액"		, ta_RspDataP.getBigDecimal("이용금액" 		    ));
			ta_RspData.setBigDecimal("단기대출잔액"	, ta_RspDataP.getBigDecimal("단기대출잔액" 	    ));				
			ta_RspData.setString    ("결제예정일"	, ta_RspDataP.getString    ("결제예정일" 	    )); 
			ta_RspData.setBigDecimal("이자율"		, ta_RspDataP.getBigDecimal("이자율" 		    )); 
					
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("단기대출목록", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	static LData reset_Req_Mci_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("CI내용"					, input.getString("CI내용"				));
		
		return output;
	}
	
	static LData reset_Rsp_Mci_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData
		
		output.setString("CI내용"					, input.getString("CI내용"				));
		output.setString("고객식별자"				, input.getString("고객식별자"			));
		output.setString("고객관리번호"				, input.getString("고객관리번호"		));
		
		for ( int anx = 0; anx < input.getLMultiData("단기대출정보그리드").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_카드청구기본목록			
			LData 		ta_RspDataP = input.getLMultiData( "단기대출정보그리드" ).getLData( anx );			
			
			ta_RspData.setString	 ("CA승인일시_V14"	 , ta_RspDataP.getString	 ("CA승인일시_V14" 		));
			ta_RspData.setBigDecimal ("CA승인금액_N15"	 , ta_RspDataP.getBigDecimal ("CA승인금액_N15" 		)); 
			ta_RspData.setBigDecimal ("CA잔액"	         , ta_RspDataP.getBigDecimal ("CA잔액" 			    )); 
			ta_RspData.setString     ("청구년월일1_V8"	 , ta_RspDataP.getString     ("청구년월일1_V8"		));
			ta_RspData.setBigDecimal ("수수료율_N5_3"	 , ta_RspDataP.getBigDecimal ("수수료율_N5_3" 		));
					
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
		} 
		output.set	("단기대출정보그리드", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
}
