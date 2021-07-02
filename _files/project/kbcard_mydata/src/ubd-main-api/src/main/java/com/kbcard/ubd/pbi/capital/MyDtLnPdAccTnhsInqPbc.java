package com.kbcard.ubd.pbi.capital;

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
import devonenterprise.ext.util.FormatUtil;
import devonenterprise.ext.util.StringUtil;
import devonenterprise.ext.util.TypeConvertUtil;

/**
 * 프로그램명 		: 마이데이터 API 제공 대출상품계좌거래내역조회
 * 거래코드 		: UBD0101640
 * 설        명 	: 마이데이터 API 제공 대출상품계좌거래내역조회 pbi
 * 
 * 연계시스템 		: 상품처리계 호출
 * 연계구분 		: 카드론(MCI)
 * 거래코드 		: GEA0156440
 * 인터페이스ID 	: UBD_1_GEAS00005
 * 인터페이스명 	: 마이데이터 API 제공 대출상품계좌거래내역조회
 * 전문정보 input 	: GEA0156440_I
 * 전문정보 output 	: GEA0156440_O
 * 
 * 연계시스템 		: 할부금융 호출
 * 연계구분 		: 할부금융(EAI)
 * 거래코드 		: KIW72RF0S4
 * 인터페이스ID 	: UBE_2_KIWS00004
 * 인터페이스명 	: 마이데이터 API 제공 할부리스금융거래내역조회
 * 전문정보 input 	: KIW72RF0S4_I
 * 전문정보 output 	: KIW72RF0S4_O
 * 
 * 
 * 작성자 			: 김형구
 * 작성일자 		: 2021-06-03
 * 변경이력 	
 */
/**
 * @serviceID UBD0101640
 * @logicalName 마이데이터 API 제공 대출상품계좌거래내역조회
 * @param LData
 *            	iLnPdAccTnhs i대출상품계좌거래내역입력
 * @return LData 
 * 			  	rLnPdAccTnhs r대출상품계좌거래내역결과
 * @exception LException. 
 * 
 *            ※
 */
public class MyDtLnPdAccTnhsInqPbc {
	
	// 전역변수 선언부
	LData tLnPdAccTnhs = new LData();	// 처리계 LData
	LData rLnPdAccTnhs = new LData();	// result LData
	
	/**
	 * 함수명 		: 마이데이터 API 제공 대출상품계좌거래내역조회
	 * 작성자 		: 김형구
	 * 작성일자 	: 2021-06-03
	 */
	public LData retvMyDtApiLnPdAccTnhsForPaging(LData iLnPdAccTnhs) throws LException {
		
		LLog.debug.println( "###########################################" );
		LLog.debug.println( "[마이데이터 API 제공 대출상품계좌거래내역조회] PBI START #####" );
		LLog.debug.println( "[API구분코드 = " + UBD_CONST.API_DTCD_CAPITAL_TRAN_INQ + " ]" );
		
		LLog.debug.println( "GUID      : [" + ContextHandler.getContextObject(ContextKey.GUID		) + "]");
		LLog.debug.println( "SITE_CODE : [" + ContextHandler.getContextObject(ContextKey.SITE_CODE	) + "]");
		LLog.debug.println( "TRAN_ID   : [" + ContextHandler.getContextObject(ContextKey.TRAN_ID	) + "]");
		
		LLog.debug.println( "[iLnPdAccTnhs]" + iLnPdAccTnhs );	

		int   itRowCnt		= 0;								// 데이터조회건수
		int   istRowCnt		= 0;								// 데이터조회건수
		String sErrCode		= UBD_CONST.REP_CD_SUCCESS;			// 에러코드(00000)
		String sErrMsg		= UBD_CONST.REP_CD_MSG_SUCCESS;		// 에러메시지(응답내역)
		String sErrCodePrc	= ""; // 에러코드(처리계)
		String sErrMsgPrc	= ""; // 에러메시지(처리계메시지)
		
		String sCICtt		= "";	// CI내용
		String sCstIdf 		= "";	// 고객식별자
		String sCstMgNo 	= "";	// 고객관리번호	
		String sMmsn		= "";   // 회원일련번호
		String sAccNo		= "";   // 계좌번호
		String sSeqNo       = "";   // 회차번호
		
		// [input] ----------------------------------------------------------------------------
		// [header]
		// 1  Authorization 	접근토큰			String  1500
		// 2  x-api-tran-id 	거래고유번호 		String    25
		// [body]
		// 1  ORG_CODE       	기관코드			String    10          
		// 2  ACCOUNT_NUM    	계좌번호     		String    20     
		// 3  SEQNO    	     	회차번호     		String     7
		// 4  FROM_DATE    		시작일자     		String     8
		// 5  TO_DATE    		종료일자     		String     8
		// 6  NEXT_PAGE    		다음페이지기준개체	String  1000
		// 7  LIMIT    		    최대조회갯수   		String     3
		// [output] ----------------------------------------------------------------------------
		// [header]
		// 1  x-api-tran-id 		거래고유번호 		String  25		
		// [body]		
		// 1  RSP_CODE	  			세부응답코드		String  5      
		// 2  RSP_MSG				세부응답메시지  	String  450      
		// 3  NEXT_PAGE	        	다음페이지기준개체	 	String  1000
		//
		// 4  TRANS_LIST_CNT    	거래목록_cnt	    Numeric 3     	
		// 5  TRANS_LIST            거래목록  그룹      Group  3                               
		// 6    ┖  TRANS_DTIME     거래일시			String 14           
		// 7    ┖  TRANS_NO  	    거래번호  		    String 64
		// 8    ┖  TRANS_TYPE  	거래유형  		    String  2
		// 9    ┖  CURRENCY_CODE  	통화코드  		    String  3
		// 10   ┖  TRANS_AMT  	    거래금액  		    Numeric 18,3
		// 11   ┖  BALANCE_AMT  	거래후대출잔액  	Numeric 18,3
		// 12   ┖  PRINCIPAL_AMT  	거래금액중원금  	Numeric 18,3
		// 13   ┖  INT_AMT  	    거래금액중이자  	Numeric 18,3
		// 14   ┖  RET_INT_AMT  	환출이자  		    Numeric 18,3
		// 15   ┖  INT_LIST_CNT  	이자적용목록_cnt  	Numeric 3	
		// 16   ┖  INT_LIST        이자적용목록  그룹  Group  3
		// 17   ┖━  INT_START_DATE    이자적용시작일  String  8              
		// 18   ┖━  INT_END_DATE  	이자적용종료일  String  8
		// 19   ┖━  INT_RATE      	적용이율  		Numeric 5,3              
		// 20   ┖━  APPLIED_INT_AMT  	이자금액  		Numeric 18,3
		// 21   ┖━  INT_TYPE      	이자종류코드  	String  2
		// ------------------------------------------------------------------------------------------	
		
		
		LData 		iCstidf 			= new LData(); 		// 고객식별자조회(input)
		LData 		tCstidf 			= new LData(); 		// 고객식별자조회(output)
		LData 		itmp_LnPdAccTnhs   	= new LData(); 		// 대출상품계좌거래내역조회(input)
		LMultiData 	tmLnPdAccTnhs 		= new LMultiData(); // 대출상품계좌거래내역조회(output)
		LData   	iRspCdMap			= new LData(); 		// 음답코드매핑조회(input)
		LData   	tRspCdMap			= new LData(); 		// 음답코드매핑조회(output)

		String sGwGuid 				= ContextHandler.getContextObject(ContextKey.GUID	);	// 게이트웨이거래식별번호
		String sTranId				= ContextHandler.getContextObject(ContextKey.TRAN_ID);	// 거래코드
		String sDmdCtt 				= ""; 					// 압축 및 암호화
		String sDmdCttCmpsEcy 		= ""; 					// 압축 및 암호화 모듈 호출
		String sRtvlTrsYN   		= "N"; 					// 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
		String sPrtlDcCd 			= "HDR";				// 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
		String sCdcMciGb			= "MCI"; 				// 처리계시스템구분(CDC, MCI)
		String sMciEaiGb			= "MCI"; 				// 호출시스템분기(MCI, EAI)
		String sTrsTgDtcd			= "01";					// 마이테이터전송대상구분코드 (01:마이데이터 사업자, 02:본인, 03:기관)
		String sClintIdiNo 			= ""; 					// 마이데이터클라이언트식별번호	
		String sInnMciLinkIntfId 	= "UBD_1_GEAS00005"; 	// MCI LINK 인터페이스ID
		String sInnEaiLinkIntfId 	= "UBE_2_KIWS00004"; 	// 대내EAI LINK 인터페이스ID
		
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
			rLnPdAccTnhs = init_Input_Ldata(iLnPdAccTnhs);	// 수신정보 => 전송정보 초기화	
			
			// 1-1 헤더값 체크 -------------------------------------------------------------------------------------------
			UbdCommon 	ubdCommon 	= new UbdCommon(); // UbdCommon 공통 호출
			
			LData 		tdHeader 	= new LData();
			LData 		iHeader 	= new LData();
			
			iHeader.setString("apiDtcd", UBD_CONST.API_DTCD_CAPITAL_TRAN_INQ);	//API구분코드			
			tdHeader 		= ubdCommon.get_header(iHeader);
			
			sAccsTken		= tdHeader.getString("Authorization"); // 접근토큰
			sMydtTrUno		= tdHeader.getString("x-api-tran-id"); // 마이데이터거래고유번호
			sRtvlTrsYN   	= tdHeader.getString("x-api-type"	); // 정기적전송여부(Y:정기적전송-"x-api-type: scheduled", N:비정기적전송(정보주체 개입)-생략)
			sPrtlDcCd 		= tdHeader.getString("potal-dc-cd"	); // 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
			sCdcMciGb 		= tdHeader.getString("tran_dv_cd"	); // 처리계시스템구분(CDC, MCI) - POR 일 경우에만 값이 들어옴. 
			sTrsTgDtcd		= tdHeader.getString("x-client-type"); // 마이테이터전송대상구분코드 (01:마이데이터 사업자, 02:본인, 03:기관)
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
				if(LNullUtils.isNone(iLnPdAccTnhs) || StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {			
					rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002			); // 응답코드(40002)
					rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002		); // 응답메시지(헤더값 미존재)
					rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체" 	));
					
					return rLnPdAccTnhs;	
				}	
			}
						
			// 2. 요청파라미터 체크 --------------------------------------------------------------------------------------
			// 2.1 수신정보검증
			Boolean bTlgFormatErr 	= false; // 전문포멧에러여부
			if(StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("기관코드")) && !UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
				bTlgFormatErr = true;
			} else if(StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("계좌번호")) || !FormatUtil.isCharOfNum(iLnPdAccTnhs.getString("계좌번호"))) {
				bTlgFormatErr = true;
			} else if(StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("회차번호")) || !FormatUtil.isCharOfNum(iLnPdAccTnhs.getString("회차번호"))) {
				bTlgFormatErr = true;  
			} else if(StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("시작일자")) || !FormatUtil.isCharOfNum(iLnPdAccTnhs.getString("시작일자"))) {
				bTlgFormatErr = true;
			} else if(StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("종료일자")) || !FormatUtil.isCharOfNum(iLnPdAccTnhs.getString("종료일자"))) {
				bTlgFormatErr = true;
			}    
			if(bTlgFormatErr) {
				rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001			); // 응답코드(40001)
				rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001		); // 응답메시지(요청파라미터 오류)
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체" 	));
				
				return rLnPdAccTnhs;		
			}	
			
			sAccNo = iLnPdAccTnhs.getString("계좌번호");
			sSeqNo = iLnPdAccTnhs.getString("회차번호");			
			
			// 2.2 거래고유번호 중복체크			  
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();
			
			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  		);
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  		); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_CAPITAL_TRAN_INQ  	); // API구분코드
			
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
					rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002			); // 응답코드(40002)
					rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002		); // 응답메시지(헤더값 미존재)
					rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체"	));
					
					return rLnPdAccTnhs;			
				}					
			} else {
				// 포털에서 접근토큰으로 CI정보를 조회
				tCustInf 	= ubdCommon.select_cust_info(sAccsTken); 
				
				LLog.debug.println( " tCustInf = " + tCustInf);
				
				if(StringUtil.trimNisEmpty(tCustInf.getString("CI내용"))) {
					rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 			); // 응답코드(40403)
					rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403			); // 응답메시지(정보주체(고객) 미존재)
					rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체"	));
					
					return rLnPdAccTnhs;
					
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
				rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 ); // 응답코드(40403)
				rLnPdAccTnhs.setString("세부응답메시지"	    , UBD_CONST.REP_CD_MSG_NOTFOUND_40403); // 응답메시지(정보주체(고객) 미존재)
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체" ));
				
				return rLnPdAccTnhs;
				
			} else {
				sCstIdf 	= rUsrRgInf.getString("고객식별자");
				sCstMgNo 	= rUsrRgInf.getString("고객관리번호");
				sMmsn		= rUsrRgInf.getString("회원일련번호"); 
			}		
			
			// 2.5 조회기간(* 일단위 최대 30일, 월단위 최대 3개월 )
			String sStartDate 	= iLnPdAccTnhs.getString("시작일자");    
			String sEndDate 	= iLnPdAccTnhs.getString("종료일자");
			LLog.debug.println( " 시작일자 = " + sStartDate);
			LLog.debug.println( " 종료일자 = " + sEndDate);
			
			int itTermInfo = DateUtil.getDayInterval( sStartDate, sEndDate );
			if(itTermInfo > 30) {
				rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_TOO_MANY_42901 		 		 ); // 응답코드(42901)
				rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_TOO_MANY_42901	 		 ); // 응답메시지(정보제공 요청한도 초과)
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체"	));
				
				return rLnPdAccTnhs;
			}

			// 2.6 페이지사이즈 : 최대500 건 
			int iPgeSize 	= iLnPdAccTnhs.getInt("최대조회갯수");  
			
			if (TypeConvertUtil.toString(iPgeSize) == "" || iPgeSize <= 0) {
				iPgeSize = 500;
			}
			
			if(iPgeSize > 500) {
				rLnPdAccTnhs.setString("세부응답코드"		, UBD_CONST.REP_CD_TOO_MANY_42901			 	 ); // 응답코드(42901)
				rLnPdAccTnhs.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_TOO_MANY_42901	 		 ); // 응답메시지(정보제공 요청한도 초과)
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체"	));
				
				return rLnPdAccTnhs;
			}
//=======================================================================================================================================================
//=======================================================================================================================================================			
			
			LLog.debug.println( " MCI(상품처리계) 대출상품계좌거래내역조회 호출 Start ==========" );
			
//			sCstIdf 	= "1154354531"; // 고객식별자
//			sCstMgNo 	= "00000";		// 고객관리번호
			
			try {
//			* 1) 할부금융 CI내용 미관리, KB핀번호(고객식별자+고객관리번호)로 요청
//			-> 장애로 인한 KB핀번호(고객식별자+고객관리번호) 조회 불가시 별도 CI내용으로 EAI(MCI) 통해 조회 후 요청
//			2) 할부금융 기존 서비스 대신 신규로 개발 지원, 조회구분 추가하여 API 구분
//			-> 조회구분 추가, 응답코드 매핑
//			3) 신차할부는201901 이전 카드론에서 관리, 이후 할부금융에서 관리
//			-> 카드론,할부금융 동일 LAYOUT 제공, 조회구분 추가
//			4) 대출회차(회차번호) 기본값 1 SET
//			5) 대출번호로 분기(카드론, 할부금융:  5,6자리가 '16' or '38'이면 카드론, 그외 할부금융)
//			* 대출번호의 5번째와 6번째자리 값이 '16' or '38' 인 경우 카드론, 그외는 할부금융으로 인터페이스 분기 처리
//			6) 취소, 정정(원인거래 포함) 제외
//			7) 대출 조건변경 거래내역은 구분코드 '기타'로 조회 (워크아웃 포함, 특채 제외)	

				//0123456789
				//sAccNo.substring(4, 6)

				// 대출번호로 카드론(MCI)/할부금융(EAI) 분기 ================================
				if (sAccNo.substring(4, 6).equals("16") || sAccNo.substring(4, 6).equals("38")) {
					sMciEaiGb = "MCI"; // 카드론
				} else {
					sMciEaiGb = "EAI"; // 할부금융
				}
				//sMciEaiGb = sAccNo.substring(4, 6);
				
				LLog.debug.println("****** MCI/EAI ******* [" + sMciEaiGb + "][" + sAccNo.substring(4, 6) +"]");
				
				// 호출시스템분기(CDC, MCI) 분기 로직
				if(sMciEaiGb.equals("MCI")) {
				
					// MCI 호출 로직 ================================
					LLog.debug.println("****** MCI 호출 START *******");
					
					// 입력부
					itmp_LnPdAccTnhs.setString	("CI내용"		    , sCICtt					);
					itmp_LnPdAccTnhs.setString	("고객식별자"	    , sCstIdf					);
					itmp_LnPdAccTnhs.setString	("고객관리번호"	    , sCstMgNo					);
					itmp_LnPdAccTnhs.setString	("회원일련번호"	    , sMmsn  					);
					
					itmp_LnPdAccTnhs.setString	("대출번호"	        , sAccNo  					);
					itmp_LnPdAccTnhs.setString	("대출회차"	        , sSeqNo  					);
					itmp_LnPdAccTnhs.setString	("조회시작년월일"	, sStartDate  	     		);
					itmp_LnPdAccTnhs.setString	("조회종료년월일"	, sEndDate  				);			
					
					// SQ_카드론거래년월일=0,SQ_카드론거래일련번호=0|NK_카드론거래년월일=,NK_카드론거래일련번호=
					if (StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("다음페이지기준개체"))) {
//						itmp_LnPdAccTnhs.setString("다음조회키_V1000", "SQ_카드론거래년월일=0,SQ_카드론거래일련번호=0|NK_카드론거래년월일=,NK_카드론거래일련번호=");  //SQ 1 desc
						itmp_LnPdAccTnhs.setString("다음조회키_V1000", "");  //SQ 1 desc
					} else {
						itmp_LnPdAccTnhs.setString("다음조회키_V1000", iLnPdAccTnhs.getString("다음페이지기준개체"));
					}
					itmp_LnPdAccTnhs.setInt	("페이지사이즈_N5"	, iPgeSize		); 
					
					tLnPdAccTnhs = (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciLnPdAccTnhsInf", itmp_LnPdAccTnhs);
					
//					sCstIdf 	= tLnPdAccTnhs.getString("고객식별자"		); // 고객식별자
//					sCstMgNo 	= tLnPdAccTnhs.getString("고객관리번호"	); // 고객관리번호
					
					for ( int anx = 0; anx < tLnPdAccTnhs.getLMultiData("GEA0156440_그리드").getDataCount(); anx++ ) { 
						
						LData 	ta_LnPdAccTnhs 	= new LData(); // temp_그리드
						LData 	ta_LnPdAccTnhsP = tLnPdAccTnhs.getLMultiData("GEA0156440_그리드").getLData( anx );
						
						ta_LnPdAccTnhs.setString	 ("거래일시"	   , ta_LnPdAccTnhsP.getString	    ("카드론거래처리일시"   )); //카드론거래처리일시
						ta_LnPdAccTnhs.setString	 ("거래번호"	   , ta_LnPdAccTnhsP.getString	    ("카드론거래일련번호"	)); //카드론거래일련번호
						ta_LnPdAccTnhs.setString	 ("거래유형"       , ta_LnPdAccTnhsP.getString	    ("거래유형구분코드_V2"	)); //거래유형구분코드_V2
						ta_LnPdAccTnhs.setString     ("통화코드"	   , ta_LnPdAccTnhsP.getString      ("통화코드"	            )); //통화코드
						ta_LnPdAccTnhs.setBigDecimal ("거래금액"	   , ta_LnPdAccTnhsP.getBigDecimal	("카드론거래금액" 	    )); //카드론거래금액
						ta_LnPdAccTnhs.setBigDecimal ("거래후대출잔액" , ta_LnPdAccTnhsP.getBigDecimal	("카드론거래후잔액"     )); //카드론거래후잔액
						ta_LnPdAccTnhs.setBigDecimal ("거래금액중원금" , ta_LnPdAccTnhsP.getBigDecimal	("카드론거래원금"	    )); //카드론거래원금
						ta_LnPdAccTnhs.setBigDecimal ("거래금액중이자" , ta_LnPdAccTnhsP.getBigDecimal	("상환이자"			    )); //상환이자
						ta_LnPdAccTnhs.setBigDecimal ("환출이자"	   , ta_LnPdAccTnhsP.getBigDecimal  ("환출이자_N15"	        )); //환출이자_N15
											
						LMultiData tmLnPdAccTnhs_sub = new LMultiData();
						for ( int bnx = 0; bnx < ta_LnPdAccTnhsP.getLMultiData("GEA0156440_그리드2").getDataCount(); bnx++ ) { 			
							LData 		ta_LnPdAccTnhs2		= new LData(); // temp_그리드			
							LData 		ta_LnPdAccTnhsP2 	= ta_LnPdAccTnhsP.getLMultiData("GEA0156440_그리드2").getLData( bnx );
							
							ta_LnPdAccTnhs2.setString("이자적용시작일"	    , ta_LnPdAccTnhsP2.getString("이자계산시작년월일" 		)); 
							ta_LnPdAccTnhs2.setString("이자적용종료일"	    , ta_LnPdAccTnhsP2.getString("이자계산종료년월일" 		));
							ta_LnPdAccTnhs2.setString("적용이율"	        , ta_LnPdAccTnhsP2.getString("적용이율" 	            ));				
							ta_LnPdAccTnhs2.setString("이자금액"      	    , ta_LnPdAccTnhsP2.getString("이자금액_N15" 	        )); 
							ta_LnPdAccTnhs2.setString("이자종류코드"	    , ta_LnPdAccTnhsP2.getString("이자종류구분코드" 		)); 							
							
							//이자적용내역
							tmLnPdAccTnhs_sub.addLData(ta_LnPdAccTnhs2); // LData -> LMultiData

						}
						
						ta_LnPdAccTnhs.set("이자적용목록",tmLnPdAccTnhs_sub);	// LData에 LMultiData 추가하기
						
						istRowCnt = ta_LnPdAccTnhsP.getLMultiData("GEA0156440_그리드2").getDataCount();	// 데이터조회건수
						ta_LnPdAccTnhs.setInt("이자적용목록_cnt", istRowCnt);
						
						// 대출상품계좌거래내역
						tmLnPdAccTnhs.addLData(ta_LnPdAccTnhs);							
						ta_LnPdAccTnhsP = null;					
					} 
					
					rLnPdAccTnhs.set("거래목록",tmLnPdAccTnhs);	// LData에 LMultiData 추가하기
					
					itRowCnt = tLnPdAccTnhs.getLMultiData("GEA0156440_그리드").getDataCount();	// 데이터조회건수
					rLnPdAccTnhs.setInt("거래목록_cnt", itRowCnt);
					
					//-------------------------------------------------------------------------------------------------------------------------------
					
					linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
					sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // 오류메시지코드
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID 
					sdtGidNo 			= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			); // 거래 GUID
	
	//				String _next_page_exis_yn 	= ScrollPageData.getNextYn();
	//				String _next_key 			= ScrollPageData.getNextKey();
	//				
	//				rCrdBilBasInf.setString("다음페이지존재여부_V1"	, _next_page_exis_yn);
	//				rCrdBilBasInf.setString("다음조회키_V40"		, _next_key			);
	
					LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■■");
					LLog.debug.println( " itmp_LnPdAccTnhs = " + itmp_LnPdAccTnhs 	);
					LLog.debug.println( " tLnPdAccTnhs     = " + tLnPdAccTnhs 		);
										
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
								
					rLnPdAccTnhs.setString("세부응답코드"		, sErrCode									 ); // 응답코드
					rLnPdAccTnhs.setString("세부응답메시지"		, sErrMsg									 ); // 응답메시지
					
					if(tLnPdAccTnhs.getString("다음존재여부_V1").equals("Y")) {
						rLnPdAccTnhs.setString("다음페이지기준개체"	, tLnPdAccTnhs.getString("다음조회키_V1000"));
					} else {
						rLnPdAccTnhs.setString("다음페이지기준개체"	, ""										 );
					}
					
					LLog.debug.println( " rLnPdAccTnhs = " + rLnPdAccTnhs );
					LLog.debug.println( " MCI(상품처리계) 대출상품계좌거래내역조회 호출 End ==========" );
				
				} else {

					// EAI 호출 로직 ================================
					LLog.debug.println("****** EAI 호출 START *******");
					
					// 입력부
					itmp_LnPdAccTnhs.setString	("CI내용"		    	, sCICtt					);
					itmp_LnPdAccTnhs.setString	("고객식별자"	    	, sCstIdf					);
					itmp_LnPdAccTnhs.setString	("고객관리번호"	    	, sCstMgNo					);
					itmp_LnPdAccTnhs.setString	("회원일련번호"	    	, sMmsn  					);
					
					itmp_LnPdAccTnhs.setString	("대출번호_V14"	        , sAccNo  					);
					itmp_LnPdAccTnhs.setString	("대출회차_V3"	        , sSeqNo  					);
					itmp_LnPdAccTnhs.setString	("GAB조회시작년월일_V8"	, sStartDate  	     		);
					itmp_LnPdAccTnhs.setString	("GAB조회종료년월일_V8"	, sEndDate  				);					
					if (StringUtil.trimNisEmpty(iLnPdAccTnhs.getString("다음페이지기준개체"))) {
						itmp_LnPdAccTnhs.setString("다음조회키_V1000", "");
					} else {
						itmp_LnPdAccTnhs.setString("다음조회키_V1000", iLnPdAccTnhs.getString("다음페이지기준개체"));
					}
					itmp_LnPdAccTnhs.setInt	("페이지사이즈_N5"	, iPgeSize		); 
					
					tLnPdAccTnhs = (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.EaiSptFntCpbc", "retvEaiLnPdAccTnhsForPaging", itmp_LnPdAccTnhs);

					tLnPdAccTnhs.setString	 ("대출번호_V14"	      , tLnPdAccTnhs.getLData("출력데이터").getString ("대출번호_V14"          )); //대출번호_V14
					tLnPdAccTnhs.setString	 ("대출회차_V3"	          , tLnPdAccTnhs.getLData("출력데이터").getString ("대출회차_V3"           )); //대출회차_V3
					tLnPdAccTnhs.setString	 ("조회시작년월일_V8"     , tLnPdAccTnhs.getLData("출력데이터").getString ("조회시작년월일_V8"     )); //GAB조회시작년월일_V8
					tLnPdAccTnhs.setString   ("조회종료년월일_V8"     , tLnPdAccTnhs.getLData("출력데이터").getString ("조회종료년월일_V8"	   )); //GAB조회종료년월일_V8
					tLnPdAccTnhs.setString	 ("다음조회키_V1000"      , tLnPdAccTnhs.getLData("출력데이터").getString ("다음조회키_V1000"	   )); //다음조회키_V1000
					tLnPdAccTnhs.setString   ("다음페이지존재여부_V1" , tLnPdAccTnhs.getLData("출력데이터").getString ("다음페이지존재여부_V1" )); //다음페이지존재여부_V1
					
//					sCstIdf 	= tLnPdAccTnhs.getString("고객식별자"		); // 고객식별자
//					sCstMgNo 	= tLnPdAccTnhs.getString("고객관리번호"	); // 고객관리번호
					
					for ( int anx = 0; anx < tLnPdAccTnhs.getLMultiData("거래목록").getDataCount(); anx++ ) { 
						
						LData 	ta_LnPdAccTnhs	= new LData(); // temp_그리드
						LData 	ta_LnPdAccTnhsP	= tLnPdAccTnhs.getLMultiData("거래목록").getLData( anx );
						
						ta_LnPdAccTnhs.setString	 ("거래일시"	   , ta_LnPdAccTnhsP.getString	    ("거래일시_V14"     )); //거래일시_V14
						ta_LnPdAccTnhs.setString	 ("거래번호"	   , ta_LnPdAccTnhsP.getString	    ("거래번호_V30"     )); //거래번호_V30
						ta_LnPdAccTnhs.setString	 ("거래유형"       , ta_LnPdAccTnhsP.getString	    ("거래유형_V2"	    )); //거래유형구분코드_V2
						ta_LnPdAccTnhs.setString     ("통화코드"	   , ta_LnPdAccTnhsP.getString	    ("거래통화코드"	    )); //거래통화코드
						ta_LnPdAccTnhs.setBigDecimal ("거래금액"	   , ta_LnPdAccTnhsP.getBigDecimal	("GAB거래금액_N15"  )); //GAB거래금액_N15
						ta_LnPdAccTnhs.setBigDecimal ("거래후대출잔액" , ta_LnPdAccTnhsP.getBigDecimal	("거래후잔액_N15"   )); //거래후잔액_N15
						ta_LnPdAccTnhs.setBigDecimal ("거래금액중원금" , ta_LnPdAccTnhsP.getBigDecimal	("거래원금_N15"	    )); //거래원금_N15
						ta_LnPdAccTnhs.setBigDecimal ("거래금액중이자" , ta_LnPdAccTnhsP.getBigDecimal	("이자금액_N15"		)); //이자금액_N15
						ta_LnPdAccTnhs.setBigDecimal ("환출이자"	   , ta_LnPdAccTnhsP.getBigDecimal  ("환출이자금액_V15"	)); //환출이자금액_V15								
						
						LMultiData tmLnPdAccTnhs_sub = new LMultiData();
//						LLog.debug.println("ta_LnPdAccTnhsP     \n " + ta_LnPdAccTnhsP);
//						LLog.debug.println("이자적용목록     \n " + ta_LnPdAccTnhsP.getLMultiData("이자적용목록"));
//						LLog.debug.println("이자적용목록  cnt : " + ta_LnPdAccTnhsP.getLMultiData("이자적용목록").getDataCount());
						
						
						for ( int bnx = 0; bnx < ta_LnPdAccTnhsP.getLMultiData("이자적용목록").getDataCount(); bnx++ ) { 			
							LData 		ta_LnPdAccTnhs2 	= new LData(); // temp_그리드			
							LData 		ta_LnPdAccTnhsP2 = ta_LnPdAccTnhsP.getLMultiData("이자적용목록").getLData( bnx );
							
							ta_LnPdAccTnhs2.setString("이자적용시작일"	    , ta_LnPdAccTnhsP2.getString("이자계산시작년월일" 		)); 
							ta_LnPdAccTnhs2.setString("이자적용종료일"	    , ta_LnPdAccTnhsP2.getString("이자계산종료년월일" 		));
							ta_LnPdAccTnhs2.setString("적용이율"	        , ta_LnPdAccTnhsP2.getString("적용이율" 	            ));				
							ta_LnPdAccTnhs2.setString("이자금액"     	    , ta_LnPdAccTnhsP2.getString("이자금액_N15" 	        )); 
							ta_LnPdAccTnhs2.setString("이자종류코드"	    , ta_LnPdAccTnhsP2.getString("이자종류구분코드" 		)); 							
							
							//이자적용내역
							tmLnPdAccTnhs_sub.addLData(ta_LnPdAccTnhs2); // LData -> LMultiData

						}
						
						ta_LnPdAccTnhs.set("이자적용목록",tmLnPdAccTnhs_sub);	// LData에 LMultiData 추가하기 
						
						istRowCnt = ta_LnPdAccTnhsP.getLMultiData("이자적용목록").getDataCount();	// 데이터조회건수
						ta_LnPdAccTnhs.setInt("이자적용목록_cnt", istRowCnt);
						
						// 대출상품계좌거래내역
						tmLnPdAccTnhs.addLData(ta_LnPdAccTnhs);	// LData -> LMultiData						
						ta_LnPdAccTnhsP = null;	
					
					} 

					rLnPdAccTnhs.set("거래목록",tmLnPdAccTnhs);	// LData에 LMultiData 추가하기
					
					itRowCnt = tLnPdAccTnhs.getLMultiData("거래목록").getDataCount();	// 데이터조회건수
					rLnPdAccTnhs.setInt("거래목록_cnt", itRowCnt);
					
					//-------------------------------------------------------------------------------------------------------------------------------
					
					linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
					sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // 오류메시지코드
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID 
					sdtGidNo 			= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_GID_NO			); // 거래 GUID
	
	//				String _next_page_exis_yn 	= ScrollPageData.getNextYn();
	//				String _next_key 			= ScrollPageData.getNextKey();
	//				
	//				rCrdBilBasInf.setString("다음페이지존재여부_V1"	, _next_page_exis_yn);
	//				rCrdBilBasInf.setString("다음조회키_V40"		, _next_key			);
	
					LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■■");
					LLog.debug.println( " itmp_LnPdAccTnhs = " + itmp_LnPdAccTnhs 	);
					LLog.debug.println( " tLnPdAccTnhs     = " + tLnPdAccTnhs 		);
										
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
								
					rLnPdAccTnhs.setString("세부응답코드"		, sErrCode									 ); // 응답코드
					rLnPdAccTnhs.setString("세부응답메시지"		, sErrMsg									 ); // 응답메시지
					
					if(tLnPdAccTnhs.getString("다음페이지존재여부_V1").equals("Y")) {
						rLnPdAccTnhs.setString("다음페이지기준개체"	, tLnPdAccTnhs.getString("다음조회키_V1000"));
					} else {
						rLnPdAccTnhs.setString("다음페이지기준개체"	, ""										);
					}				
					
					LLog.debug.println( " rLnPdAccTnhs = " + rLnPdAccTnhs );
					LLog.debug.println( " EAI(할부금융) 대출상품계좌거래내역조회 호출 End ==========" );
				}
				
				LLog.debug.println( " 고객식별자            = " + sCstIdf 		);
				LLog.debug.println( " 고객관리번호          = " + sCstMgNo 		);
				LLog.debug.println( " [return] RowCount     = " + itRowCnt 		);
				LLog.debug.println( " [return] iLnPdAccTnhs = " + iLnPdAccTnhs	);
				LLog.debug.println( " [return] rLnPdAccTnhs = " + rLnPdAccTnhs	);
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
				
				rLnPdAccTnhs.setString("세부응답코드"		, sErrCode							  			 ); // 응답코드
				rLnPdAccTnhs.setString("세부응답메시지"		, sErrMsg							  			 ); // 응답메시지
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체" ));
				
				return rLnPdAccTnhs;
				
			} catch (LException e) {
				
				LLog.debug.println("MCI호출 LException ");
				
				// 요청내역관리 오류코드 세팅
				sErrCode	= UBD_CONST.REP_CD_SERVER_ERR_50001; 
				sErrMsg		= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				sErrCodePrc	= UBD_CONST.REP_CD_SERVER_ERR_50001;  
				sErrMsgPrc 	= UBD_CONST.REP_CD_MSG_SERVER_ERR_50001;
				
				rLnPdAccTnhs.setString("세부응답코드"		, sErrCode							  			 ); // 응답코드
				rLnPdAccTnhs.setString("세부응답메시지"		, sErrMsg							  			 ); // 응답메시지
				rLnPdAccTnhs.setString("다음페이지기준개체"	, iLnPdAccTnhs.getString("다음페이지기준개체"   ));
				
				return rLnPdAccTnhs;
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
			
			sErrCode 	= rLnPdAccTnhs.getString("세부응답코드");	
			sErrMsg 	= rLnPdAccTnhs.getString("세부응답메시지");	

			lEncInf.setString("거래고유번호"				, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_CAPITAL_TRAN_INQ);
			lEncInf.setString("포탈분기구분코드"			, sPrtlDcCd						);
			lEncInf.setString("처리계시스템구분"			, sCdcMciGb						);
			lEncInf.setString("CI내용"						, sCICtt						);
			lEncInf.setString("고객식별자"					, sCstIdf						);
			lEncInf.setString("고객관리번호"				, sCstMgNo						);
			lEncInf.setString("마이데이터정기전송여부"		, sRtvlTrsYN					);			
			lEncInf.setString("오픈API응답코드"				, sErrCode						);
			lEncInf.setString("오픈API응답메시지내용"		, sErrMsg						);
			lEncInf.setString("MCI오류메시지코드"			, sErrCodePrc					);  // 에러코드(처리계)
			lEncInf.setString("MCI오류메시지출력내용"		, sErrMsgPrc					);  // 에러메시지(처리계메시지)
			lEncInf.setString("EAI오류메시지코드"			, sErrCodePrc					);  // 에러코드(처리계)
			lEncInf.setString("EAI오류메시지출력내용"		, sErrMsgPrc				    );  // 에러메시지(처리계메시지)
			lEncInf.setString("MCI원거래GUID"				, sdtLnkdOgtnGidNo				);  // 연계원거래 GUID
			lEncInf.setString("EAI원거래GUID"				, sdtLnkdOgtnGidNo				);  // 연계원거래 GUID
			lEncInf.setString("MCI인터페이스ID"				, sInnMciLinkIntfId				);
			lEncInf.setString("EAI인터페이스ID"				, sInnEaiLinkIntfId				);
			lEncInf.setString("시스템최종갱신식별자"		, sTranId						);
			lEncInf.setString("MCI요청상세입력여부"			, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"			, sPrcEaiInsGb					);
			lEncInf.setString("마이데이터전송대상구분코드"	, sTrsTgDtcd					);	
			lEncInf.setString("마이데이터클라이언트식별번호", sClintIdiNo					);	
			
			input 		= reset_Req_Ldata	 (iLnPdAccTnhs		);
			output 		= reset_Rsp_Ldata	 (rLnPdAccTnhs		);
			
			if(sMciEaiGb.equals("MCI")) {
				iMciInput 	= reset_Req_Mci_Ldata(itmp_LnPdAccTnhs	);
				rMciInput 	= reset_Rsp_Mci_Ldata(tLnPdAccTnhs		);				
			}

			if(sMciEaiGb.equals("EAI")) { 
				iEaiInput 	= reset_Req_Eai_Ldata(itmp_LnPdAccTnhs	);
				rEaiInput 	= reset_Rsp_Eai_Ldata(tLnPdAccTnhs		);				
			}
			
			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf, "L");
//			AsyncRunner.setLogParam(input, output, iMciInput, rMciInput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();
			
			LLog.debug.println( "=======================================" );		
			LLog.debug.println( "[마이데이터API 대출상품계좌거래내역조회] End ============" );
			LLog.debug.println( "=======================================" );
			
		}
		
		return rLnPdAccTnhs;
		
	}
	
	/**
	 * @serviceID initRtnSetting
	 * @logicalName 전문 초기화 세팅
	 * @param LData 
	 */	
	public LData initRtnSetting(LData input) throws LException {
		
		LData rRtnInf = new LData();
		
		return rRtnInf;
	}
	
	static LData init_Input_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"				, input.getString("기관코드"			));
		output.setString("계좌번호"				, input.getString("계좌번호"			));
		output.setString("회차번호"				, input.getString("회차번호"			));
		output.setString("시작일자"				, input.getString("시작일자"			));
		output.setString("종료일자"				, input.getString("종료일자"			));
		output.setString("다음페이지기준개체"	, input.getString("다음페이지기준개체"	));
		output.setString("최대조회갯수"			, input.getString("최대조회갯수"		));

		return output;
	}
	
	static LData reset_Req_Ldata(LData input) {

		LData output = new LData();

		output.setString("기관코드"				, input.getString("기관코드"			));
		output.setString("계좌번호"				, input.getString("계좌번호"			));
		output.setString("회차번호"				, input.getString("회차번호"			));
		output.setString("시작일자"				, input.getString("시작일자"			));
		output.setString("종료일자"				, input.getString("종료일자"			));
		output.setString("다음페이지기준개체"	, input.getString("다음페이지기준개체"	));
		output.setString("최대조회갯수"			, input.getString("최대조회갯수"		));

		return output;
	}
	
	static LData reset_Rsp_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData
		LMultiData 	mta_RspData2 = new LMultiData(); // LMultiData

		output.setString("세부응답코드"		    , input.getString("세부응답코드"	    ));		
		output.setString("세부응답메시지"	    , input.getString("세부응답메시지"   	));
		output.setString("다음페이지기준개체"	, input.getString("다음페이지기준개체"  ));

		for ( int anx = 0; anx < input.getLMultiData("거래목록").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("거래목록").getLData( anx );
					
			ta_RspData.setString("거래일시"		   , ta_RspDataP.getString("거래일시" 		    )); 
			ta_RspData.setString("거래번호"		   , ta_RspDataP.getString("거래번호" 		    ));
			ta_RspData.setString("거래유형"	       , ta_RspDataP.getString("거래유형" 	        ));				
			ta_RspData.setString("통화코드"  	   , ta_RspDataP.getString("통화코드" 	        )); 
			ta_RspData.setString("거래금액"		   , ta_RspDataP.getString("거래금액" 		    )); 
			ta_RspData.setString("거래후대출잔액"  , ta_RspDataP.getString("거래후대출잔액" 	)); 
			ta_RspData.setString("거래금액중원금"  , ta_RspDataP.getString("거래금액중원금" 	));
			ta_RspData.setString("거래금액중이자"  , ta_RspDataP.getString("거래금액중이자" 	));				
			ta_RspData.setString("환출이자"  	   , ta_RspDataP.getString("환출이자" 	        )); 
			
			mta_RspData.addLData(ta_RspData);			
			ta_RspDataP = null;			
			
			for ( int bnx = 0; bnx < ta_RspData.getLMultiData("이자적용목록").getDataCount(); bnx++ ) { 			
				LData 		ta_RspData2 	= new LData(); // temp_그리드			
				LData 		ta_RspDataP2 = ta_RspData.getLMultiData("이자적용목록").getLData( bnx );
				
				ta_RspData2.setString("거래일시"	    , ta_RspDataP2.getString("거래일시" 		)); 
				ta_RspData2.setString("거래번호"	    , ta_RspDataP2.getString("거래번호" 		));
				ta_RspData2.setString("거래유형"	    , ta_RspDataP2.getString("거래유형" 	    ));				
				ta_RspData2.setString("통화코드"  	    , ta_RspDataP2.getString("통화코드" 	    )); 
				ta_RspData2.setString("거래금액"	    , ta_RspDataP2.getString("거래금액" 		)); 
				ta_RspData2.setString("거래후대출잔액"  , ta_RspDataP2.getString("거래후대출잔액" 	)); 
				ta_RspData2.setString("거래금액중원금"  , ta_RspDataP2.getString("거래금액중원금" 	));
				ta_RspData2.setString("거래금액중이자"  , ta_RspDataP2.getString("거래금액중이자" 	));				
				ta_RspData2.setString("환출이자"  	    , ta_RspDataP2.getString("환출이자" 	    )); 
				
				mta_RspData2.addLData(ta_RspData2);			
				ta_RspDataP2 = null;	
			}
			
			ta_RspData.set	("이자적용목록", mta_RspData2);	// LData에 LMultiData 추가하기
			
		} 
		output.set	("거래목록", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	static LData reset_Req_Mci_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("대출번호"				, input.getString("대출번호"			));
		output.setString("대출회차"				, input.getString("대출회차"			));
		output.setString("조회시작년월일"		, input.getString("조회시작년월일"		));
		output.setString("조회종료년월일"		, input.getString("조회종료년월일"		));
		output.setString("다음조회키_V1000"		, input.getString("다음조회키_V1000"	));		
		output.setString("페이지사이즈_N5"		, input.getString("페이지사이즈_N5"		));
		
		return output;
	}
	
	static LData reset_Rsp_Mci_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData
		
		output.setString("대출번호"				, input.getString("대출번호"			));
		output.setString("대출회차"				, input.getString("대출회차"			));
		output.setString("조회시작년월일"		, input.getString("조회시작년월일"		));
		output.setString("조회종료년월일"		, input.getString("조회종료년월일"		));
		output.setString("다음존재여부_V1"		, input.getString("다음존재여부_V1"		));
		output.setString("다음조회키_V1000"		, input.getString("다음조회키_V1000"	));
		
		for ( int anx = 0; anx < input.getLMultiData("GEA0156440_그리드").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("GEA0156440_그리드").getLData( anx );
					
			ta_RspData.setString("카드론거래처리일시"	   , ta_RspDataP.getString("카드론거래처리일시" 	)); 
			ta_RspData.setString("카드론거래일련번호"	   , ta_RspDataP.getString("카드론거래일련번호" 	));
			ta_RspData.setString("거래유형구분코드_V2"	   , ta_RspDataP.getString("거래유형구분코드_V2" 	));				
			ta_RspData.setString("통화코드"  	           , ta_RspDataP.getString("통화코드" 	            )); 
			ta_RspData.setString("카드론거래금액"		   , ta_RspDataP.getString("카드론거래금액" 		)); 
			ta_RspData.setString("카드론거래후잔액"        , ta_RspDataP.getString("카드론거래후잔액" 	    )); 
			ta_RspData.setString("카드론거래원금"          , ta_RspDataP.getString("카드론거래원금" 	    ));
			ta_RspData.setString("상환이자"                , ta_RspDataP.getString("상환이자"            	));				
			ta_RspData.setString("환출이자_N15"  	       , ta_RspDataP.getString("환출이자_N15" 	        )); 
			
			LMultiData mta_RspData_sub = new LMultiData();
			
			for ( int bnx = 0; bnx < ta_RspData.getLMultiData("GEA0156440_그리드2").getDataCount(); bnx++ ) { 			
				LData 		ta_RspData2 	= new LData(); // temp_그리드			
				LData 		ta_RspDataP2 = ta_RspData.getLMultiData("GEA0156440_그리드2").getLData( bnx );
				
				ta_RspData2.setString("이자계산시작년월일"	    , ta_RspDataP2.getString("이자계산시작년월일" 		)); 
				ta_RspData2.setString("이자계산종료년월일"	    , ta_RspDataP2.getString("이자계산종료년월일" 		));
				ta_RspData2.setString("적용이율"	            , ta_RspDataP2.getString("적용이율" 	            ));				
				ta_RspData2.setString("이자금액_N15"  	        , ta_RspDataP2.getString("이자금액_N15" 	        )); 
				ta_RspData2.setString("이자종류구분코드"	    , ta_RspDataP2.getString("이자종류구분코드" 		)); 
				
				mta_RspData_sub.addLData(ta_RspData2);			
				ta_RspDataP2 = null;	
			}
			
			ta_RspData.set	("GEA0156440_그리드2", mta_RspData_sub);	// LData에 LMultiData 추가하기
			
			mta_RspData.addLData(ta_RspData); // LData -> LMultiData			
			ta_RspDataP = null;
			
		} 
		
		output.set	("GEA0156440_그리드", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}
	
	static LData reset_Req_Eai_Ldata(LData input) {

		LData output = new LData();
		
		output.setString("대출번호_V14"				, input.getString("대출번호_V14"			));
		output.setString("대출회차_V3"				, input.getString("대출회차_V3"			    ));
		output.setString("GAB조회시작년월일_V8"		, input.getString("GAB조회시작년월일_V8"	));
		output.setString("GAB조회종료년월일_V8"		, input.getString("GAB조회종료년월일_V8"	));
		output.setString("다음조회키_V1000"		    , input.getString("다음조회키_V1000"       	));		
		output.setString("페이지사이즈_N5"		    , input.getString("페이지사이즈_N5"		    ));
		
		return output;
	}
	
	static LData reset_Rsp_Eai_Ldata(LData input) {

		LData output = new LData();
		LMultiData 	mta_RspData = new LMultiData(); // LMultiData
	
		output.setString("대출번호_V14"				, input.getString("대출번호_V14"	     	));
		output.setString("대출회차_V3"				, input.getString("대출회차_V3"			    ));
		output.setString("조회시작년월일_V8"		, input.getString("조회시작년월일_V8"	    ));
		output.setString("조회종료년월일_V8"		, input.getString("조회종료년월일_V8"		));
		output.setString("다음페이지존재여부_V1"	, input.getString("다음페이지존재여부_V1"	));
		output.setString("다음조회키_V1000"		    , input.getString("다음조회키_V1000"    	));
		
		for ( int anx = 0; anx < input.getLMultiData("거래목록").getDataCount(); anx++ ) { 			
			LData 		ta_RspData 	= new LData(); // temp_그리드			
			LData 		ta_RspDataP = input.getLMultiData("거래목록").getLData( anx );
					
			ta_RspData.setString("거래일시_V14"	      , ta_RspDataP.getString("거래일시_V14" 	    )); 
			ta_RspData.setString("거래번호_V30"	      , ta_RspDataP.getString("거래번호_V30" 	    ));
			ta_RspData.setString("거래유형_V2"	      , ta_RspDataP.getString("거래유형_V2" 	    ));				
			ta_RspData.setString("통화코드"  	      , ta_RspDataP.getString("통화코드" 	        )); //////////
			ta_RspData.setString("GAB거래금액_N15"    , ta_RspDataP.getString("GAB거래금액_N15" 	)); 
			ta_RspData.setString("거래후잔액_N15"     , ta_RspDataP.getString("거래후잔액_N15" 	    )); 
			ta_RspData.setString("거래원금_N15"       , ta_RspDataP.getString("거래원금_N15" 	    ));
			ta_RspData.setString("이자금액_N15"       , ta_RspDataP.getString("이자금액_N15"        ));				
			ta_RspData.setString("환출이자금액_V15"   , ta_RspDataP.getString("환출이자금액_V15" 	)); 
		
			LMultiData mta_RspData_sub = new LMultiData();
			for ( int bnx = 0; bnx < ta_RspDataP.getLMultiData("이자적용목록").getDataCount(); bnx++ ) { 			
				LData 		ta_RspData2 	= new LData(); // temp_그리드	
				LData 		ta_RspDataP2 = ta_RspDataP.getLMultiData("이자적용목록").getLData( bnx );
				
				ta_RspData2.setString("이자계산시작년월일"	    , ta_RspDataP2.getString("이자계산시작년월일" 		)); 
				ta_RspData2.setString("이자계산종료년월일"	    , ta_RspDataP2.getString("이자계산종료년월일" 		));
				ta_RspData2.setString("적용이율"	            , ta_RspDataP2.getString("적용이율" 	            ));				
				ta_RspData2.setString("이자금액_N15"  	        , ta_RspDataP2.getString("이자금액_N15" 	        )); 
				ta_RspData2.setString("이자종류구분코드"	    , ta_RspDataP2.getString("이자종류구분코드" 		)); 
				
				mta_RspData_sub.addLData(ta_RspData2);	// LData -> LMultiData		
				ta_RspDataP2 = null;	
			}
			
			ta_RspData.set	("이자적용목록", mta_RspData_sub);	// LData에 LMultiData 추가하기

			mta_RspData.addLData(ta_RspData); // LData -> LMultiData			
			ta_RspDataP = null;
		} 
		
		output.set	("거래목록", mta_RspData);	// LData에 LMultiData 추가하기
		
		return output;
	}	
	
}
