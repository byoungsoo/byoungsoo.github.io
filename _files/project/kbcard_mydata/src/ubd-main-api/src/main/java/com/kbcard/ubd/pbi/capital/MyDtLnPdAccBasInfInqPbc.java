package com.kbcard.ubd.pbi.capital;

import com.kbcard.ubd.common.pbi.token.TokenCommonPbc;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubd.cpbi.card.MydtCstPntRmgInqCpbc;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataCryptUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;

/**
 * 
 *
 * @logicalName   마이데이터 대출상품계좌기본정보조회Pbi
 * @lastDate      2021-05-31
 */
public class MyDtLnPdAccBasInfInqPbc {
	TokenCommonPbc tokenCommonPbc = new TokenCommonPbc(); //공통 토큰 호출
	UbdCommon 	ubdCommon   = new UbdCommon();	 //ubd공통 호출
	LData 	  	header_info = new LData(); 		 //헤더정보
	LData 	  	rUbdCommon  = new LData();		 //공통common
	
	/**
	 * 
	 *
	 * @serviceID     UBD2000240
	 * @logicalName   마이데이터대출상품계좌기본정보조회
	 * @param         LData input 
	 *                            LData iRetvLnPdAccBasInfP i마이데이터대출상품계좌기본정보조회P : 마이데이터대출상품계좌기본정보조회입력Dto
	 * @return        LData rData 
	 *                            LData rRetvLnPdAccBasInfP r마이데이터대출상품계좌기본정보조회P : 마이데이터대출상품계좌기본정보조회결과Dto
	 * @exception     LException occurs error 
	 * 
	 */
	public LData retvLnPdAccBasInf( LData input ) throws LException {
		LLog.debug.println( input );
		LLog.debug.println( " ☆☆☆ 마이데이터대출상품계좌기본정보조회 pbi ☆☆☆ START " );
		
		
		MyDtLnPdAccBasInfInqCpbc myDtLnPdAccBasInfInqCpbc = new MyDtLnPdAccBasInfInqCpbc(); //마이데이터대출상품계좌기본정보조회cpbi
		
		LData iRetvLnPdAccBasInfP = new LData(); //마이데이터대출상품계좌기본정보조회pbi 입력
		LData rRetvLnPdAccBasInfP = new LData(); //마이데이터대출상품계좌기본정보조회pbi 출력
		
		
		LData rVerifyIn = new LData(); //입력값 검증결과
		
		try {
			LData iHeader_info = new LData();
			iHeader_info.setString("apiDtcd", UBD_CONST.API_DTCD_CAPITAL_BAS_INF_INQ);
			header_info = ubdCommon.get_header( iHeader_info );
			
			rVerifyIn = verifyIn( input ); //입력값 검증
			
			if( rVerifyIn.getBoolean("pass_flag") == false ){ //입력값 오류시
				rRetvLnPdAccBasInfP.setString("세부응답코드", rVerifyIn.getString("세부응답코드") );
				rRetvLnPdAccBasInfP.setString("세부응답메시지", rVerifyIn.getString("세부응답메시지") );
			}else {
				iRetvLnPdAccBasInfP = input; //입력 json 수신
				
				rRetvLnPdAccBasInfP = myDtLnPdAccBasInfInqCpbc.retvLnPdAccBasInfLnkd(iRetvLnPdAccBasInfP); //대출상품계좌기본정보연계조회
			}
			
		} catch (Exception e) {
			LLog.debug.println( "마이데이터대출상품계좌기본정보조회 pbi 거래중 오류 발생 LException ☆☆☆" );
			rRetvLnPdAccBasInfP.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
			rRetvLnPdAccBasInfP.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
		}finally {
			ubdCommon.set_header(); //헤더정보 입력(거래고유번호 입력 X) 
		}
		
		LLog.debug.println( rRetvLnPdAccBasInfP );
		LLog.debug.println( " ☆☆☆ 마이데이터대출상품계좌기본정보조회 pbi ☆☆☆ END " );
		return rRetvLnPdAccBasInfP;
	}
	

	//입력값 검증
	private LData verifyIn(LData input) throws LException {
		LData result = new LData(); //검증결과
		boolean pass_flag = false;
		
		result.setBoolean("pass_flag", pass_flag);
		
		String sAccsTken		= ContextUtil.getHttpRequestHeaderParam("Authorization"); 	// 접근토큰
		String sMydtTrUno		= ContextUtil.getHttpRequestHeaderParam("x-api-tran-id"); 	// 마이데이터거래고유번호
		
		//헤더값 검증
		if( UBD_CONST.PRTL_DTCD_PRTL.equals( header_info.getString("potal-dc-cd") ) ) { //검증거래 skip
			
			if( StringUtil.trimNisEmpty( header_info.getString("ci_ctt") )  ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002		 ); // 응답코드(40002)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002	 ); // 응답메시지(헤더값 미존재)
				return result;
			}
			
			pass_flag = true; 
			result.setBoolean("pass_flag", pass_flag);
			return result;
		}else { //일반거래 검증
			//헤더값 검증
			if( StringUtil.trimNisEmpty(sAccsTken) ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002		 ); // 응답코드(40002)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002	 ); // 응답메시지(헤더값 미존재)
				return result;
			}
			if( StringUtil.trimNisEmpty(sMydtTrUno) ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40002		 ); // 응답코드(40002)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002	 ); // 응답메시지(헤더값 미존재)
				return result;
			}
			
			//수신정보검증
			if( input.getString("기관코드").isEmpty() ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001		 ); // 응답코드(40001)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 ); // 응답메시지(요청파라미터 오류)
				return result;
			}
			if (input.getString("조회타임스탬프").isEmpty() ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001		 ); // 응답코드(40001)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 ); // 응답메시지(요청파라미터 오류)
				return result;
			}
			if( input.getString("계좌번호").isEmpty() ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001		 ); // 응답코드(40001)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 ); // 응답메시지(요청파라미터 오류)
				return result;
			}
			if (input.getString("회차번호").isEmpty() ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_BAD_REQUEST_40001		 ); // 응답코드(40001)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001	 ); // 응답메시지(요청파라미터 오류)
				return result;
			}
			// 2.2 거래고유번호 중복체크			  
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();
			
			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_CAPITAL_BAS_INF_INQ  ); // API구분코드(대출상품계좌기본정보조회)
			
			bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			if( ! bRtn ) { //false : 중복거래 시
				//TODO MS05722중복거래 오류 처리 ?> 
			}
			
			//토큰 검증
			String token 	= header_info.getString("Authorization");
			String industry = UBD_CONST.BSZ_DTCD_CARPTAL;
			String orgCode 	= input.getString("기관코드");
			String apiId 	= UBD_CONST.API_DTCD_POINT_INF_INQ;
			String attId 	= input.getString("계좌번호");
			
			LData rToken = tokenCommonPbc.verifyToken( token, industry, orgCode, apiId, attId );
			if( UBD_CONST.REP_CD_SUCCESS.equals( rToken.getString("rsp_code") )  ) { //정상응답이 아닐 시 
				result.setString("세부응답코드"		, rToken.getString("rsp_code")	 ); // 응답코드 세팅
				result.setString("세부응답메시지"	, rToken.getString("rsp_msg" )	 ); // 응답메시지 세팅
				return result;
			}
		}
		
		pass_flag = true; //검증오류 없으면 true 입력
		result.setBoolean("pass_flag", pass_flag);
		
		return result;
	}



	/** @logicalName   마이데이터대출상품계좌기본정보조회Cpbi */
	//* 대출번호의 5번째와 6번째자리 값이 '16' or '38' 인 경우 카드론, 그외는 할부금융으로 인터페이스 분기 처리
	class MyDtLnPdAccBasInfInqCpbc{
		MydtCstPntRmgInqCpbc mydtCstPntRmgInqCpbc = new MydtCstPntRmgInqCpbc();
		LinkHttpAdaptor httpClient = new LinkHttpAdaptor(); // 대내거래 어댑터
		LData linkResponseHeader = new LData(); // eai 헤더

		LData iEai = new LData(); // eai거래 입력
		LData rEai = new LData(); // eai거래 출력
		
		LData iMci = new LData(); // mci거래 입력
		LData rMci = new LData(); // mci거래 출력
		
		/** 대출상품계좌기본정보연계조회 
		 * @throws LException */
		public LData retvLnPdAccBasInfLnkd(LData input) throws LException {
			LLog.debug.println(" ☆☆☆ 대출상품계좌기본정보연계조회 cpbi ☆☆☆ START ");
			LLog.debug.println(input);
			
			LData rRetvCstCmn = new LData(); //고객정보조회결과
			LData result 	= new LData(); //결과값 LData
			
			String intfDtcd =""; 		//인터페이스 구분코드
			String lnNo = ""; 			//대출번호
			String sdtErrMsgCd = ""; 	//연계에러코드
			String bwkDtcd = ""; 		//업무구분코드
			String intfId = ""; 		//인터페이스ID
			String lnkdDc = "";			//연계구분
			
			String token = "";
			String ci="";
			LData cust_info = new LData();

			try {
				
				header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 
				token = header_info.getString("Authorization"); //접근토큰
				
				if( UBD_CONST.PRTL_DTCD_PRTL.equals( header_info.getString("potal-dc-cd") ) ) { //검증거래 일시
					ci = header_info.getString("ci_ctt"); // 접근토큰
				}else {
					cust_info = ubdCommon.select_cust_info(token); // 포털에서 ci정보 조회
					ci = cust_info.getString("CI내용");
				}
				
				rRetvCstCmn = ubdCommon.retvCstCmn( ci );
				
				lnNo = input.getString("계좌번호"); //대출번호
				intfDtcd = lnNo.substring(4,6);
				if( (intfDtcd.equals("16") || intfDtcd.equals("38")) ) { //카드론
					bwkDtcd = "카드론"; 
					intfId = "UBD_2_KIWS00002";
					lnkdDc = "EAI";
					result = retvEaiLnPdAccBasInf(input); //EAI대출상품계좌기본조회 (카드론)
				}else { //할부금융
					bwkDtcd = "할부금융"; 
					intfId = "UBD_1_GEAS00003";
					lnkdDc = "MCI";
					result = retvMciLnPdAccBasInf(input); //MCI대출상품계좌기본조회 (할부금융)
				}
				rUbdCommon.setString("연계거래응답코드", SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD));
				result.setString("상환계좌번호", DataCryptUtil.decryptAcNo( result.getString("상환계좌번호") ) );
				
//				if(  )
				result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
				result.setString("세부응답메시지", "성공");
			} catch (LBizException e) {
				LLog.debug.println( bwkDtcd , " 대출상품계좌기본정보조회 연계 거래중 오류 발생 LBizException ☆☆☆");
				LLog.debug.println("e.getcode ☆☆☆", e.getCode());
				rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq( sdtErrMsgCd , e ,lnkdDc );
				result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
				result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
			} catch (LException e) {
				 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			 }finally {
				rUbdCommon.setString("인터페이스ID", intfId);
				rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_CAPITAL_BAS_INF_INQ);
				rUbdCommon.setString("API거래코드", "UBD2000240");
				 
				input 	= reset_Req_Ldata	 (input		);
				result 	= reset_Rsp_Ldata	 (result	);
				
				if( !LNullUtils.isNone(iMci) ) {
					iMci 	= reset_Req_Mci_Ldata(iMci	);
					rMci 	= reset_Rsp_Mci_Ldata(rMci	);
				}
				
				if( !LNullUtils.isNone(iEai) ) {
					iEai 	= reset_Req_Eai_Ldata(iEai	);
					rEai 	= reset_Rsp_Eai_Ldata(rEai	);
				}
				
				LData sppinf = new LData(); //제공테이블 입력용 추가 정보 
				sppinf = rRetvCstCmn; //고객정보
				sppinf.setString("CI내용", ci);
				sppinf.setString("연계거래응답코드", rUbdCommon.getString("연계거래응답코드"));
				sppinf.setString("연계거래응답메시지", rUbdCommon.getString("연계거래응답메시지"));
				sppinf.setString("인터페이스ID", rUbdCommon.getString("인터페이스ID")); 	
				sppinf.setString("API거래코드", rUbdCommon.getString("API거래코드" ));		
				sppinf.setString("API구분코드", rUbdCommon.getString("API구분코드" ));		
				
				mydtCstPntRmgInqCpbc.regtTlgDmdRspHis( input, result, iMci , rMci , iEai, rEai , sppinf );//전문요청응답내역등록(MCI) 
			}
			
			LLog.debug.println( " ☆☆☆ 대출상품계좌기본정보연계조회 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}
		//EAI대출상품계좌기본조회 (카드론)
		private LData retvEaiLnPdAccBasInf( LData input ) throws LException {
			
			LLog.debug.println( " ☆☆☆ 카드론 EAI대출상품계좌기본정보조회 cpbi ☆☆☆ START " );
			LLog.debug.println(input);
			
			LData result = new LData();
			
			String sdtErrMsgCd = ""; // mci응답코드
			
			iEai.setString("대출번호", input.getString("계좌번호")); // 기관코드
			iEai.setString("대출회차", input.getString("회차번호")); // 계좌번호
			
			rEai = httpClient.sendOutboundMessage(TargetTypeConst.EAI, "UBD_1_GEAS00003", iEai); // 대출상품 계좌 기본정보 조회 (카드론)
			linkResponseHeader = (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			
			if (linkResponseHeader != null) {
				sdtErrMsgCd = SystemHeaderManager.getValueFromLData(linkResponseHeader,KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD); // mci응답코드
			}

			if (!sdtErrMsgCd.equals("N0000000")) {
				LLog.debug.println("MCI 장기카드대출 대출상품계좌기본정보조회 오류 발생 LBizException ☆☆☆");
				LLog.debug.println("e.getcode ☆☆☆", sdtErrMsgCd);
				rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq(sdtErrMsgCd, "MCI");
			}
			
			result.setString("대출일", rEai.getString("대출약정년월일") );
			result.setString("만기일", rEai.getString("대출만기년월일") );
			result.setString("최종적용금리", rEai.getString("카드론적용금리") );
			result.setString("월상환일", rEai.getString("약정납입일") );
			result.setString("상환방식", rEai.getString("상환방법구분코드") );
			result.setString("자동이체기관", rEai.getString("결제기관구분코드") );
			result.setString("상환계좌번호", rEai.getString("고객계좌번호") );
			
			LLog.debug.println( " ☆☆☆ 카드론 EAI대출상품계좌기본정보조회 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}
		
		//EAI대출상품계좌기본조회 (할부금융)
		private LData retvMciLnPdAccBasInf( LData input ) throws LException {
			
			LLog.debug.println( " ☆☆☆ 할부금융 MCI대출상품계좌기본정보조회 cpbi ☆☆☆ START " );
			LLog.debug.println(input);

			LData result = new LData();
			
			String sdtErrMsgCd = ""; // mci응답코드
			
			iMci.setString("대출번호_V14", input.getString("계좌번호")); // 기관코드
			iMci.setString("대출회차_V3", input.getString("회차번호")); // 계좌번호
			
			rMci = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_2_KIWS00002", iMci); // 대출상품 계좌 기본정보 조회 (할부금융)
			linkResponseHeader = (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			
			if (linkResponseHeader != null) {
				sdtErrMsgCd = SystemHeaderManager.getValueFromLData(linkResponseHeader,KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD); // eai응답코드
			}

			if (!sdtErrMsgCd.equals("N0000000")) {
				LLog.debug.println("EAI 장기카드대출 대출상품계좌기본정보조회 오류 발생 LBizException ☆☆☆");
				LLog.debug.println("e.getcode ☆☆☆", sdtErrMsgCd);
				rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq(sdtErrMsgCd, "MCI");
			}
			
			result.setString("대출일", rMci.getString("대출약정년월일") );
			result.setString("만기일", rMci.getString("대출만기년월일") );
			result.setString("최종적용금리", rMci.getString("최종적용금리_N7_5") );
			result.setString("월상환일", rMci.getString("약정납입일") );
			result.setString("상환방식", rMci.getString("상환방법구분코드") );
			result.setString("자동이체기관", rMci.getString("은행코드") );
			result.setString("상환계좌번호", rMci.getString("결제계좌번호_V20") );
			
			LLog.debug.println( " ☆☆☆ 할부금융 MCI대출상품계좌기본정보조회 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}
		
		/**
		 * @serviceID initRtnSetting
		 * @logicalName 전문 초기화 세팅
		 * @param LData
		 */
		private LData reset_Req_Ldata(LData input) {
			LData result = new LData();
			result.setString("조회타임스탬프"	, input.getString("조회타임스탬프"));  
			result.setString("기관코드", input.getString("기관코드")); // 기관코드
			result.setString("계좌번호", input.getString("계좌번호")); // 계좌번호
			result.setString("회차번호", input.getString("회차번호")); // 회차번호
			return result;
		}

		private LData reset_Rsp_Ldata(LData input) {
			LData result = new LData();
			
			result.setString("세부응답코드"		, input.getString("세부응답코드")); 
			result.setString("세부응답메시지"	, input.getString("세부응답메시지"));  
			result.setString("대출일"			, input.getString("대출일"));  
			result.setString("만기일"			, input.getString("만기일")); 
			result.setString("최종적용금리"		, input.getString("최종적용금리"));  
			result.setString("월상환일"			, input.getString("월상환일"));  
			result.setString("상환방식"			, input.getString("상환방식")); 
			result.setString("자동이체기관"		, input.getString("자동이체기관"));  
			result.setString("상환계좌번호"		, input.getString("상환계좌번호")); 

			return result;
		}

		private LData reset_Req_Mci_Ldata(LData input) {
			LData result = new LData();

			result.setString("대출번호_V14", input.getString("대출번호_V14"));
			result.setString("대출회차_V3", input.getString("대출회차_V3"));

			return result;
		}

		private LData reset_Rsp_Mci_Ldata(LData input) {
			LData result = new LData();
			
			result.setString("고객식별자"		, input.getString("고객식별자"));
			result.setString("고객관리번호"		, input.getString("고객관리번호"));
			result.setString("대출번호_V14"		, input.getString("대출번호_V14"));
			result.setString("대출회차_V3"		, input.getString("대출회차_V3"));
			result.setString("대출약정년월일"	, input.getString("대출약정년월일"));
			result.setString("대출만기년월일"	, input.getString("대출만기년월일"));
			result.setString("최종적용금리_N7_5", input.getString("최종적용금리_N7_5"));
			result.setString("약정납입일"		, input.getString("약정납입일"));
			result.setString("상환방법구분코드"	, input.getString("상환방법구분코드"));
			result.setString("은행코드"			, input.getString("은행코드"));
			result.setString("결제계좌번호_V20"	, input.getString("결제계좌번호_V20"));
			
			return result;
		}
		
		private LData reset_Req_Eai_Ldata(LData input) { //GEA
			LData result = new LData();

			result.setString("대출번호", input.getString("대출번호"));
			result.setString("대출회차", input.getString("대출회차"));

			return result;
		}

		private LData reset_Rsp_Eai_Ldata(LData input) { //KIW
			LData result = new LData();

			result.setString("고객식별자", input.getString("고객식별자"));
			result.setString("고객관리번호", input.getString("고객관리번호"));
			result.setString("대출번호", input.getString("대출번호"));
			result.setString("대출회차", input.getString("대출회차"));
			result.setString("대출약정년월일", input.getString("대출약정년월일"));
			result.setString("대출만기년월일", input.getString("대출만기년월일"));
			result.setString("카드론적용금리", input.getString("카드론적용금리"));
			result.setString("약정납입일", input.getString("약정납입일"));
			result.setString("상환방법구분코드", input.getString("상환방법구분코드"));
			result.setString("결제기관구분코드", input.getString("결제기관구분코드"));
			result.setString("고객계좌번호", input.getString("고객계좌번호"));
			
			return result;
		}
	}
	

	

	
}



