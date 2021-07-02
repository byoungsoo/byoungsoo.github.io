package com.kbcard.ubd.pbi.card;


import com.kbcard.ubd.common.pbi.token.TokenCommonPbc;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubd.cpbi.card.MydtCstPntRmgInqCpbc;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.ebi.card.MyDtLnPdCtgEbc;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;

/**
 * 
 *
 * @logicalName 마이데이터대출상품목록조회Pbi
 * @lastDate 2021-05-31
 */
public class MyDtLnPdCtgInqPbc {
	TokenCommonPbc tokenCommonPbc = new TokenCommonPbc(); //공통 토큰 호출
	UbdCommon ubdCommon = new UbdCommon(); // ubd공통 호출
	LData header_info = new LData(); // 헤더정보
	LData rUbdCommon = new LData(); // 공통common
	/**
	 * 
	 *
	 * @serviceID UBD1000940
	 * @logicalName 마이데이터대출상품목록조회
	 * @param LData input LData iMydtLnPdCtgInqPbcP i마이데이터대출상품목록조회P : 대출상품목록조회입력Dto
	 * @return LData rData LData rMydtLnPdCtgInqPbcP r마이데이터대출상품목록조회P : 대출상품목록조회결과Dto
	 * @exception LException occurs error
	 * 
	 */
	public LData retvMydtLnPd(LData input) throws LException {

		LLog.debug.println(input);
		LLog.debug.println(" ☆☆☆ 마이데이터대출상품목록조회 pbi ☆☆☆ START ");

		MydtLnPdCtgInqCpbc mydtLnPdInqCpbc = new MydtLnPdCtgInqCpbc(); // 마이데이터대출상품목록조회 cpbi

		

		LData iMydtLnPdCtgInqPbcP = new LData(); // 마이데이터대출상품목록조회 입력 pbi
		LData rMydtLnPdCtgInqPbcP = new LData(); // 마이데이터대출상품목록조회 출력 pbi


		LData rVerifyIn = new LData(); // 입력값 검증결과

		try {
			
			LData iHeader_info = new LData();
			iHeader_info.setString("apiDtcd", UBD_CONST.API_DTCD_LOANS_LST_INQ);
			header_info = ubdCommon.get_header( iHeader_info );
			
			rVerifyIn = verifyIn(input); // 입력값 검증
			if (rVerifyIn.getBoolean("pass_flag") == false) { // 입력값 오류시
				rMydtLnPdCtgInqPbcP.setString("세부응답코드", rVerifyIn.getString("세부응답코드"));
				rMydtLnPdCtgInqPbcP.setString("세부응답메시지", rVerifyIn.getString("세부응답메시지"));
			} else {
				
				iMydtLnPdCtgInqPbcP = input; // 입력 json 수신
				
				
				switch (header_info.getString("tran_dv_cd")) {
					case UBD_CONST.CLLGL_SYS_DTCD_CDC:
						rMydtLnPdCtgInqPbcP = mydtLnPdInqCpbc.retvLstCdcLnPd(iMydtLnPdCtgInqPbcP); // CDC대출상품목록조회
						break;

					case UBD_CONST.CLLGL_SYS_DTCD_MCI:
						rMydtLnPdCtgInqPbcP = mydtLnPdInqCpbc.retvLstMciLnPd(iMydtLnPdCtgInqPbcP); // MCI대출상품목록조회
						break;
				}
			}
			

		} catch (LException e) {
			LLog.debug.println("마이데이터대출상품조회 pbi 거래중 오류 발생 LException ☆☆☆");
			LLog.debug.println(e.getStackTraceString());
			rMydtLnPdCtgInqPbcP.setString("세부응답코드", UBD_CONST.REP_CD_SERVER_ERR_50001); // 응답코드(50001) //시스템 장애
			rMydtLnPdCtgInqPbcP.setString("세부응답메시지", UBD_CONST.REP_CD_MSG_SERVER_ERR_50001); // 응답메시지(시스템장애)
		}finally {
			ubdCommon.set_header(); // 헤더정보 입력(거래고유번호 입력 X)
		}

		LLog.debug.println(rMydtLnPdCtgInqPbcP);
		LLog.debug.println(" ☆☆☆ 마이데이터대출상품조회 pbi ☆☆☆ END ");
		return rMydtLnPdCtgInqPbcP;
	}

	// 입력값 검증
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
			
			// 2.2 거래고유번호 중복체크			  
			boolean bRtn = false;       //중복요청거래검증 결과 boolean 생성
			LData iDupDmd = new LData();
			
			UbdCdcSptFntCpbc cdcSptFntCpbc = new UbdCdcSptFntCpbc();
			
			iDupDmd.setString("거래발생일_V8"		, DateUtil.getCurrentDate() 	  );
			iDupDmd.setString("거래고유번호_V25"	, sMydtTrUno					  ); // 거래고유번호
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_LOANS_LST_INQ  ); // API구분코드(대출상품목록)
			
			bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			if( ! bRtn ) { //false : 중복거래 시
				//TODO MS05722중복거래 오류 처리 ?> 
			}
			
			//토큰 검증
			String token 	= header_info.getString("Authorization");
			String industry = UBD_CONST.BSZ_DTCD_CARD;
			String orgCode 	= input.getString("기관코드");
			String apiId 	= UBD_CONST.API_DTCD_LOANS_LST_INQ;
			LData rToken 	= tokenCommonPbc.verifyToken( token, industry, orgCode, apiId );
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



	/** @logicalName 마이데이터대출상품목록조회Cpbi */
	class MydtLnPdCtgInqCpbc {
		MydtCstPntRmgInqCpbc mydtCstPntRmgInqCpbc = new MydtCstPntRmgInqCpbc();
		UbdCommon ubdCommon = new UbdCommon(); // ubd공통 호출
		LData rUbdCommon = new LData(); // ubd공통 결과

		/**
		 * @logicalName CDC대출상품목록조회
		 * @param input
		 * @return
		 * @throws LException 
		 */
		public LData retvLstCdcLnPd(LData input) throws LException {

			LLog.debug.println( " ☆☆☆ CDC대출상품목록조회 cpbi ☆☆☆ START " );
			LLog.debug.println( input );
			
			LData result 	= new LData(); //결과값 LData
			LData rRetvPntCstinf = new LData(); //포인트고객정보조회 출력
			header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 

			MyDtLnPdCtgEbc mydtLnPdCtgInqEbc = new MyDtLnPdCtgEbc(); //CDC대출상품목록조회EBC
			
			LData iMydtLnPdCtgInqEbc = new LData(); //CDC대출상품목록조회EBC입력
			LData rMydtLnPdCtgInqEbc = new LData(); //CDC대출상품목록조회EBC결과
			
			LData rRetvCstCmn = new LData(); //고객정보조회결과
			LData cust_info = new LData(); //고객정보조회결과 
			
			String token = "";
			String ci="";
			
			try {
				header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 
				token = header_info.getString("Authorization"); //접근토큰
				
				if( UBD_CONST.PRTL_DTCD_PRTL.equals( header_info.getString("potal-dc-cd") ) ) { //검증거래 일시
					ci = header_info.getString("ci_ctt"); // 접근토큰
				}else {
					cust_info = ubdCommon.select_cust_info(token); // 포털에서 ci정보 조회
					ci = cust_info.getString("CI내용");
				}
				
				rRetvCstCmn = ubdCommon.retvCstCmn(ci); // 고객공통조회

				//고객존재검증
				if( rRetvCstCmn.isEmpty() || StringUtil.trimNisEmpty( rRetvCstCmn.getString("고객식별자") ) ) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 	 ); // 응답코드(40403)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 ); // 응답메시지(정보주체(고객) 미존재)
					return result;
				}
				
				iMydtLnPdCtgInqEbc.setString("고객식별자", rRetvCstCmn.getString("고객식별자") );
				iMydtLnPdCtgInqEbc.setString("고객관리번호", rRetvCstCmn.getString("고객관리번호") );
				iMydtLnPdCtgInqEbc.setString("회원일련번호", rRetvCstCmn.getString("회원일련번호") );
				rMydtLnPdCtgInqEbc = mydtLnPdCtgInqEbc.selectLnPdCtg(iMydtLnPdCtgInqEbc);
				
				result.setString("단기대출여부", rMydtLnPdCtgInqEbc.getString("단기대출여부")); // 단기대출여부
				result.setString("장기대출여부", rMydtLnPdCtgInqEbc.getString("장기대출여부")); // 장기대출여부
				//응답코드 입력
				result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
				result.setString("세부응답메시지", "성공");
				
				
			} catch (LBizException e) {
				LLog.debug.println( "CDC 대출상품목록조회 거래중 오류 발생 LBizException ☆☆☆" );
				result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50002	 	 ); // 응답코드(50002)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002	 ); // 응답메시지(API 요청 처리 실패)
				return result;
			}
			 catch (LException e) {
				 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			 }
			finally {
				rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_LOANS_LST_INQ);
				rUbdCommon.setString("API거래코드", "UBD1000940");
				
				LData sppinf = new LData(); //제공테이블 입력용 추가 정보 
				sppinf = rRetvCstCmn; //고객정보
				sppinf.setString("CI내용", ci);
				sppinf.setString("연계거래응답코드", rUbdCommon.getString("연계거래응답코드"));
				sppinf.setString("연계거래응답메시지", rUbdCommon.getString("연계거래응답메시지"));
				sppinf.setString("인터페이스ID", rUbdCommon.getString("인터페이스ID")); 	
				sppinf.setString("API거래코드", rUbdCommon.getString("API거래코드" ));		
				sppinf.setString("API구분코드", rUbdCommon.getString("API구분코드" ));		
				
				input 	= reset_Req_Ldata	 (input		);
				result 	= reset_Rsp_Ldata	 (result	);

				mydtCstPntRmgInqCpbc.regtTlgDmdRspHis( input, result, sppinf ) ; //전문요청응답내역등록(CDC) 
			}
			LLog.debug.println( " ☆☆☆ CDC대출상품목록조회 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}

		/**
		 * @logicalName MCI대출상품목록조회
		 * @param input
		 * @return
		 * @throws LException
		 */
		public LData retvLstMciLnPd(LData input) throws LException {
			LLog.debug.println(" ☆☆☆ MCI대출상품목록조회 cpbi ☆☆☆ START ");
			LLog.debug.println(input);
			LinkHttpAdaptor httpClient = new LinkHttpAdaptor(); // 대내거래 어댑터
			LData linkResponseHeader = new LData(); // eai 헤더

			LData iMci = new LData(); // mci거래 입력
			LData rMci = new LData(); // mci거래 출력

			LData rRetvCstCmn = new LData(); //고객정보조회결과
			
			LData result = new LData(); // 결과

			String sdtErrMsgCd = ""; // mci응답코드
			
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
				
				iMci.setString("CI내용", ci );
				rRetvCstCmn = ubdCommon.retvCstCmn( ci );
				
				//고객존재검증
				if( rRetvCstCmn.isEmpty() || StringUtil.trimNisEmpty( rRetvCstCmn.getString("고객식별자") ) ) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 	 ); // 응답코드(40403)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 ); // 응답메시지(정보주체(고객) 미존재)
					return result;
				}
				
				rMci = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00004", iMci); // 마이데이터대출상품목록조회
				linkResponseHeader = (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
				if (linkResponseHeader != null) {
					sdtErrMsgCd = SystemHeaderManager.getValueFromLData(linkResponseHeader,
							KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD); // mci응답코드
				}

				if (!sdtErrMsgCd.equals("N0000000")) {
					LLog.debug.println("MCI 마이데이터대출상품목록조회중 오류 발생 LBizException ☆☆☆");
					LLog.debug.println("e.getcode ☆☆☆", sdtErrMsgCd);
					rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq(sdtErrMsgCd, "MCI");
				}

				String shtmLnYn = "false"; // 단기대출여부
				String lhtmLnYn = "false"; // 장기대출여부
				
				shtmLnYn = "Y".equals(rMci.getString("단기대출여부_V1")) ? "true" : "false";
				lhtmLnYn = "Y".equals(rMci.getString("장기대출여부_V1")) ? "true" : "false";
				
				result.setString("단기대출여부", shtmLnYn);
				result.setString("장기대출여부", lhtmLnYn);
				result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
				result.setString("세부응답메시지", "성공");
				rUbdCommon.setString("연계거래응답코드", SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD));
			} catch (LBizException e) {
				LLog.debug.println("MCI 대출상품목록조회 거래중 오류 발생 LBizException ☆☆☆");
				LLog.debug.println("e.getcode ☆☆☆", e.getCode());
				rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq( sdtErrMsgCd , e ,"MCI" );
				result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
				result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
			}catch (LException e) {
				 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			 }finally {
				rUbdCommon.setString("인터페이스ID", "UBD_1_GBCS00004");
				rUbdCommon.setString("API거래코드", "UBD1000940");
				rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_LOANS_LST_INQ);
				
				LData sppinf = new LData(); //제공테이블 입력용 추가 정보 
				sppinf = rRetvCstCmn; //고객정보
				sppinf.setString("CI내용", ci);
				sppinf.setString("연계거래응답코드", rUbdCommon.getString("연계거래응답코드"));
				sppinf.setString("연계거래응답메시지", rUbdCommon.getString("연계거래응답메시지"));
				sppinf.setString("인터페이스ID", rUbdCommon.getString("인터페이스ID")); 	
				sppinf.setString("API거래코드", rUbdCommon.getString("API거래코드" ));		
				sppinf.setString("API구분코드", rUbdCommon.getString("API구분코드" ));		
				
				input 	= reset_Req_Ldata	 (input		);
				result 	= reset_Rsp_Ldata	 (result	);
				iMci 	= reset_Req_Mci_Ldata(iMci	);
				rMci 	= reset_Rsp_Mci_Ldata(rMci	);
				
				mydtCstPntRmgInqCpbc.regtTlgDmdRspHis( input, result, iMci, rMci , sppinf ) ;  //전문요청응답내역등록(MCI)
			}

			LLog.debug.println(" ☆☆☆ MCI대출상품목록조회 cpbi ☆☆☆ END ");
			LLog.debug.println(result);

			return result;
		}
		
	
		/**
		 * @serviceID initRtnSetting
		 * @logicalName 전문 초기화 세팅
		 * @param LData
		 */
		private LData reset_Req_Ldata(LData input) {
			LData result = new LData();

			result.setString("기관코드", input.getString("기관코드")); // 기관코드
			result.setString("조회타임스탬프", input.getString("조회타임스탬프")); // 조회타임스탬프

			return result;
		}

		private LData reset_Rsp_Ldata(LData input) {
			LData result = new LData();

			result.setString("세부응답코드", input.getString("세부응답코드")); // 응답코드
			result.setString("세부응답메시지", input.getString("세부응답메시지")); // 응답메세지

			if (UBD_CONST.REP_CD_SUCCESS.equals(input.getString("세부응답코드"))) {// 응답코드(성공)
				result.setString("단기대출여부", input.getString("단기대출여부")); // 단기대출여부
				result.setString("장기대출여부", input.getString("장기대출여부")); // 장기대출여부
			}

			return result;
		}

		private LData reset_Req_Mci_Ldata(LData input) {
			LData result = new LData();

			result.setString("CI내용", input.getString("CI내용"));

			return result;
		}

		private LData reset_Rsp_Mci_Ldata(LData input) {
			LData result = new LData();

			result.setString("CI내용", input.getString("CI내용"));
			result.setString("고객식별자", input.getString("고객식별자"));
			result.setString("고객관리번호", input.getString("고객관리번호"));
			result.setString("단기대출여부_V1", input.getString("단기대출여부_V1"));
			result.setString("장기대출여부_V1", input.getString("장기대출여부_V1"));

			return result;
		}
	}

}
