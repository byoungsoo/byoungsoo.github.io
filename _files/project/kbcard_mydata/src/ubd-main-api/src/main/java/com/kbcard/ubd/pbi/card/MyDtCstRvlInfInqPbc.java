package com.kbcard.ubd.pbi.card;

import java.math.BigDecimal;

import com.kbcard.ubd.common.pbi.token.TokenCommonPbc;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubd.cpbi.card.MydtCstPntRmgInqCpbc;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.context.ContextHandler;
import devon.core.exception.DevonException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 
 *
 * @logicalName   마이데이터고객리볼빙정보조회Pbi
 * @lastDate      2021-05-31
 */
public class MyDtCstRvlInfInqPbc {
	TokenCommonPbc tokenCommonPbc = new TokenCommonPbc(); //공통 토큰 호출
	UbdCommon ubdCommon = new UbdCommon(); // ubd공통 호출
	LData header_info = new LData(); // 헤더정보
	LData rUbdCommon = new LData(); // 공통common
	
	/**
	 * 
	 *
	 * @serviceID     UBD1001040
	 * @logicalName   마이데이터고객리볼빙정보조회
	 * @param         LData input 
	 *                            LData iRetvMydtCstRvlInfP i마이데이터고객리볼빙정보조회P : 리볼빙정보조회입력Dto
	 * @return        LData rData 
	 *                            LData rRetvMydtCstRvlInfP r마이데이터고객리볼빙정보조회P : 리볼빙정보조회결과Dto
	 * @exception     LException occurs error 
	 * 
	 */
	public LData retvMydtCstRvlInf( LData input ) throws LException {
		
		LLog.debug.println( input );
		LLog.debug.println( " ☆☆☆ 마이데이터고객리볼빙정보조회 pbi ☆☆☆ START " );
		MydtCstRvlInfInqCpbc mydtCstRvlInfInqCpbc = new MydtCstRvlInfInqCpbc(); //마이데이터 리볼빙 정보조회 cpbi
		
		LData iRetvMydtCstRvlInfP = new LData(); //마이데이터고객리볼빙정보조회 입력 pbi
		LData rRetvMydtCstRvlInfP = new LData(); //마이데이터고객리볼빙정보조회 출력 pbi
		
		
		LData rVerifyIn = new LData(); // 입력값 검증결과
		
		try {
			
			LData iHeader_info = new LData();
			iHeader_info.setString("apiDtcd", UBD_CONST.API_DTCD_REVOLVING_INF_INQ);
			header_info = ubdCommon.get_header( iHeader_info );
			
			rVerifyIn = verifyIn(input); // 입력값 검증
			if (rVerifyIn.getBoolean("pass_flag") == false) { // 입력값 오류시
				rRetvMydtCstRvlInfP.setString("세부응답코드", rVerifyIn.getString("세부응답코드"));
				rRetvMydtCstRvlInfP.setString("세부응답메시지", rVerifyIn.getString("세부응답메시지"));
			} else {
				iRetvMydtCstRvlInfP = input; //입력 json 수신
				
				
				
				switch (header_info.getString("tran_dv_cd")) {
					case UBD_CONST.CLLGL_SYS_DTCD_CDC:
						rRetvMydtCstRvlInfP = mydtCstRvlInfInqCpbc.retvCdcRvlInf(iRetvMydtCstRvlInfP); // CDC리볼빙정보조회
						break;

					case UBD_CONST.CLLGL_SYS_DTCD_MCI:
						rRetvMydtCstRvlInfP = mydtCstRvlInfInqCpbc.retvMciRvlInf(iRetvMydtCstRvlInfP); // MCI리볼빙정보조회
						break;
				}
			}
			

		} catch (LException e) {
			LLog.debug.println("마이데이터대출상품조회 pbi 거래중 오류 발생 LException ☆☆☆");
			LLog.debug.println(e.getStackTraceString());
			rRetvMydtCstRvlInfP.setString("세부응답코드", UBD_CONST.REP_CD_SERVER_ERR_50001); // 응답코드(50001) //시스템 장애
			rRetvMydtCstRvlInfP.setString("세부응답메시지", UBD_CONST.REP_CD_MSG_SERVER_ERR_50001); // 응답메시지(시스템장애)
		}finally {
			ubdCommon.set_header(); // 헤더정보 입력(거래고유번호 입력 X)
		}

		
		LLog.debug.println( rRetvMydtCstRvlInfP );
		LLog.debug.println( " ☆☆☆ 마이데이터고객리볼빙정보조회 pbi ☆☆☆ END " );
		return rRetvMydtCstRvlInfP;
	}
	
	// 입력값 검증
	private LData verifyIn(LData input) throws LException {
		LData result = new LData(); //검증결과
		boolean pass_flag = false;
		
		result.setBoolean("pass_flag", pass_flag);
		
		String sAccsTken		= ContextUtil.getHttpRequestHeaderParam("Authorization"); 	// 접근토큰
		String sMydtTrUno		= ContextUtil.getHttpRequestHeaderParam("x-api-tran-id"); 	// 마이데이터거래고유번호
		
		//검증거래 헤더값 검증
		if( UBD_CONST.PRTL_DTCD_PRTL.equals( header_info.getString("potal-dc-cd") ) ) {
			
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
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_REVOLVING_INF_INQ  ); // API구분코드(리볼빙)
			
			bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			if( ! bRtn ) { //false : 중복거래 시
				//TODO MS05722중복거래 오류 처리 ?> 
			}
			
			//토큰 검증
			String token = header_info.getString("Authorization");
			String industry = UBD_CONST.BSZ_DTCD_CARD;
			String orgCode = input.getString("기관코드");
			String apiId = UBD_CONST.API_DTCD_REVOLVING_INF_INQ;
			LData rToken = tokenCommonPbc.verifyToken( token, industry, orgCode, apiId );
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

	
	class MydtCstRvlInfInqCpbc{
		MydtCstPntRmgInqCpbc mydtCstPntRmgInqCpbc = new MydtCstPntRmgInqCpbc();
		UbdCommon 	ubdCommon  = new UbdCommon();	 //ubd공통 호출
		LData  		rUbdCommon  = new LData();		 //ubd공통 결과
		LData  		header_info = new LData();		 	 //ubd헤더 결과
		
		
		/** MCI리볼빙정보조회   
		 * @throws LException */
		public LData retvMciRvlInf(LData input) throws LException {
			LLog.debug.println(" ☆☆☆ MCI리볼빙정보조회 cpbi ☆☆☆ START ");
			LLog.debug.println(input);
			LinkHttpAdaptor httpClient = new LinkHttpAdaptor(); // 대내거래 어댑터
			LData linkResponseHeader = new LData(); // eai 헤더
			
			LData iMci = new LData(); // mci거래 입력
			LData rMci = new LData(); // mci거래 출력
			
			LData rRetvCstCmn = new LData(); //고객정보조회결과
			
			LData result 	= new LData(); //결과값 LData
			
			String sdtErrMsgCd = ""; // mci응답코드
			
			String token = "";
			String ci="";
			LData cust_info = new LData();
			
			try {
				header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 
				
				token = header_info.getString("Authorization"); // 접근토큰
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
				
				rMci = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00005", iMci); // 마이데이터리볼빙정보조회
				linkResponseHeader = (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
				
				if (linkResponseHeader != null) {
					sdtErrMsgCd = SystemHeaderManager.getValueFromLData(linkResponseHeader,
							KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD); // mci응답코드
				}
				
				//업무에러 처리
				if (!sdtErrMsgCd.equals("N0000000")) {
					LLog.debug.println("MCI 리볼빙정보조회중 오류 발생 LBizException ☆☆☆");
					LLog.debug.println("e.getcode ☆☆☆", sdtErrMsgCd);
					rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq(sdtErrMsgCd, "MCI");
					result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
					result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
				}else {
					result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
					result.setString("세부응답메시지", "성공");
					LMultiData result_arr = new LMultiData();
					for (int inx = 0; inx < rMci.getLMultiData("그리드").size(); inx++) {
						LData as_item = rMci.getLMultiData("그리드").getLData(inx);
						LData to_item = new LData();
						
						to_item.setString	 ("신청일"		, as_item.getString	   ("리볼빙약정신청일자_V8"		) 	);
						to_item.setBigDecimal("최소결제비율"	, as_item.getBigDecimal("리볼빙최소결제비율_N6_3"	) 	);
						to_item.setBigDecimal("최소결제금액"		, as_item.getBigDecimal("리볼빙최소결제금액"		) 	);
						to_item.setBigDecimal("약정결제비율"	, as_item.getBigDecimal("리볼빙약정결제비율_N6_3"	) 	);
						to_item.setBigDecimal("약정결제금액"	, as_item.getBigDecimal("리볼빙약정결제금액"		) 	);
						to_item.setBigDecimal("리볼빙이월잔액"	, as_item.getBigDecimal("리볼빙이월잔액_N15"		) 	);
						
						result_arr.addLData(to_item);
						result.setInt("리볼빙목록수", result_arr.size());
						result.set("리볼빙목록", result_arr);
					}
				}
				
				
				//리볼빙 자산 없음 에러
				if(    rMci.getLMultiData("그리드").size() == 0 ) {
						result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40402	 	 ); // 응답코드(40402)
						result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40402	 ); // 요청한 정보(자산,기관정보, 전송요구내역)가 존재하지 않음
						return result;
				}
				
				rUbdCommon.setString("연계거래응답코드", SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD));
			} catch (LBizException e) {
				LLog.debug.println("MCI 리볼빙정보조회중 거래중 오류 발생 LBizException ☆☆☆");
				LLog.debug.println("e.getcode ☆☆☆", e.getCode());
				rUbdCommon = mydtCstPntRmgInqCpbc.isidTrErrMsgInq( sdtErrMsgCd , e ,"MCI" );
				result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
				result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
			}catch (LException e) {
				 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			 }finally {
				rUbdCommon.setString("인터페이스ID", "UBD_1_GBCS00005");
				rUbdCommon.setString("API거래코드", "UBD1001040");
				rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_REVOLVING_INF_INQ);
				
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
			
			LLog.debug.println( " ☆☆☆ MCI리볼빙정보조회중 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}
		
		
		/**
		 * @logicalName CDC리볼빙정보조회
		 * @param input
		 * @return
		 * @throws LException 
		 */
		public LData retvCdcRvlInf(LData input) throws LException {
			
			LLog.debug.println( " ☆☆☆ CDC리볼빙정보조회 cpbi ☆☆☆ START " );
			LLog.debug.println( input );
			
			MydtCstRvlInfInqEbc mydtCstRvlInfInqEbc = new MydtCstRvlInfInqEbc();
			
			LData result 	= new LData(); //결과값 LData
			
			LData iMydtCstRvlInfInqEbc = new LData();//리볼빙정보조회ebc 입력
			LData rMydtCstRvlInfInqEbc = new LData();//리볼빙정보조회ebc 출력
			
			String token = "";
			String ci="";
			LData cust_info = new LData();
			LData rRetvCstCmn = new LData();
			LData rvl_item = new LData(); 			///리볼빙 단건
			LMultiData rvl_arr = new LMultiData();  //리볼빙 멀티
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
				
				//고객존재검증
				if( rRetvCstCmn.isEmpty() || StringUtil.trimNisEmpty( rRetvCstCmn.getString("고객식별자") ) ) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 	 ); // 응답코드(40403)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 ); // 응답메시지(정보주체(고객) 미존재)
					return result;
				}
				
				//리볼빙잔액존재여부조회
				iMydtCstRvlInfInqEbc.clear();
				rMydtCstRvlInfInqEbc.clear();
				iMydtCstRvlInfInqEbc.setString( "회원일련번호" , rRetvCstCmn.getString("회원일련번호") );
				rMydtCstRvlInfInqEbc	= mydtCstRvlInfInqEbc.retvRvlBlExistYn( iMydtCstRvlInfInqEbc ); 
				
				if( ! rMydtCstRvlInfInqEbc.getString("존재여부_V1").equals("1")  ) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40402	 	 ); // 응답코드(40402)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40402	 ); // 요청한 정보(자산,기관정보, 전송요구내역)가 존재하지 않음
					return result =empty_result(); //공백응답
				}
				
				//회원기본변경페이플랜예약조회
				iMydtCstRvlInfInqEbc.clear();
				rMydtCstRvlInfInqEbc.clear();
				iMydtCstRvlInfInqEbc.setString( "회원일련번호" , rRetvCstCmn.getString("회원일련번호") );
				iMydtCstRvlInfInqEbc.setString( "처리년월일" , DateUtil.getCurrentDate() );
				rMydtCstRvlInfInqEbc = mydtCstRvlInfInqEbc.retvMbrBasMdPplPrg( iMydtCstRvlInfInqEbc ); 	
				
				if( LNullUtils.isNone( rMydtCstRvlInfInqEbc )  ) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40402	 	 ); // 응답코드(40402)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40402	 ); // 요청한 정보(자산,기관정보, 전송요구내역)가 존재하지 않음
					return result =empty_result(); //공백응답
				}else {
					rvl_item.setString("신청일", rMydtCstRvlInfInqEbc.getString("처리년월일"));
					rvl_item.setBigDecimal("최소결제비율", rMydtCstRvlInfInqEbc.getBigDecimal("리볼빙최소상환비율"));
					rvl_item.setBigDecimal("약정결제비율", rMydtCstRvlInfInqEbc.getBigDecimal("리볼빙약정률"));
					rvl_item.setBigDecimal("최소결제금액"	, new BigDecimal(0) );
				}
				
				//리볼빙이월잔액조회
				iMydtCstRvlInfInqEbc.clear();
				rMydtCstRvlInfInqEbc.clear();
				iMydtCstRvlInfInqEbc.setString( "회원일련번호" , rRetvCstCmn.getString("회원일련번호") );
				rMydtCstRvlInfInqEbc 		= mydtCstRvlInfInqEbc.retvRvlCrfBl( iMydtCstRvlInfInqEbc); 	
				
				rvl_item.setBigDecimal("리볼빙이월잔액", rMydtCstRvlInfInqEbc.getBigDecimal("리볼빙이월잔액_N15"));
				rvl_arr.addLData(rvl_item);
				
				//응답코드 입력
				result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
				result.setString("세부응답메시지", "성공");
				result.setInt("리볼빙목록수", rvl_arr.size());
				result.set("리볼빙목록", rvl_arr);
			} catch (LBizException e) {
				LLog.debug.println( "CDC 리볼빙정보조회 거래중 오류 발생 LBizException ☆☆☆" );
				result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50002	 	 ); // 응답코드(50002)
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002	 ); // 응답메시지(API 요청 처리 실패)
				return result;
			}
			 catch (LException e) {
				 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			 }
			finally {
				rUbdCommon.setString("API거래코드", "UBD1001040");
				rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_REVOLVING_INF_INQ);
				
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
				
				mydtCstPntRmgInqCpbc.regtTlgDmdRspHis( input, result, sppinf ) ;  //전문요청응답내역등록(MCI) 
			}
			LLog.debug.println( " ☆☆☆ CDC리볼빙정보조회 cpbi ☆☆☆ END " );
			LLog.debug.println( result );
			
			return result;
		}
		//공백 응답
		private LData empty_result(  ) {
			
			LData result = new LData();
			result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
			result.setString("세부응답메시지", "성공");
			
			LMultiData result_arr = new LMultiData();
			LData to_item = new LData();
				
			to_item.setString	 ("신청일"			, ""	);
			to_item.setBigDecimal("최소결제비율"	, new BigDecimal(0.0) );
			to_item.setBigDecimal("최소결제금액"	, new BigDecimal(0) );
			to_item.setBigDecimal("약정결제비율"	, new BigDecimal(0.0) );
			to_item.setBigDecimal("약정결제금액"	, new BigDecimal(0) );
			to_item.setBigDecimal("리볼빙이월잔액"	, new BigDecimal(0) );
			
			result_arr.addLData(to_item);
			
			result.setInt("리볼빙목록수", result_arr.size());
			result.set("리볼빙목록", result_arr);
			
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
			LMultiData result_arr = new LMultiData();
			result.setString("세부응답코드", input.getString("세부응답코드")); // 응답코드
			result.setString("세부응답메시지", input.getString("세부응답메시지")); // 응답메세지
			
			if (UBD_CONST.REP_CD_SUCCESS.equals(input.getString("세부응답코드"))) {// 응답코드(성공)
				for (int inx = 0; inx < input.getLMultiData("리볼빙목록").size(); inx++) {
					LData as_item = input.getLMultiData("리볼빙목록").getLData(inx);
					LData to_item = new LData();
					
					to_item.setString("신청일", as_item.getString("신청일") );
					to_item.setString("최소결제비율"	, as_item.getString("최소결제비율") 	);
					to_item.setBigDecimal("최소결제금액"		, as_item.getBigDecimal("최소결제금액") 		);
					to_item.setBigDecimal("약정결제비율"	, as_item.getBigDecimal("약정결제비율") 	);
					to_item.setBigDecimal("약정결제금액"	, as_item.getBigDecimal("약정결제금액") 	);
					to_item.setBigDecimal("리볼빙이월잔액"	, as_item.getBigDecimal("리볼빙이월잔액") 	);
					
					result_arr.addLData(to_item);
				}
				
				result.setInt("리볼빙목록수", result_arr.size());
				result.set("리볼빙목록", result_arr);
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
			result.set("그리드", input.getLMultiData("그리드"));
			
			return result;
		}
	}
	

	

	class MydtCstRvlInfInqEbc{
		
		//리볼빙잔액존재여부조회
		public LData retvRvlBlExistYn(LData iMydtCstRvlInfInqEbc) throws DevonException {
			LCommonDao dao = new LCommonDao("card/MydtCstRvlInfInqEbc.xml/selectRvlBlExistYn",iMydtCstRvlInfInqEbc);
			return dao.executeQueryForSingle();
		}
		
		//리볼빙이월잔액조회
		public LData retvRvlCrfBl(LData iMydtCstRvlInfInqEbc) throws DevonException {
			LCommonDao dao = new LCommonDao("card/MydtCstRvlInfInqEbc.xml/selectRvlCrfBl",iMydtCstRvlInfInqEbc);
			return dao.executeQueryForSingle();
		}
		
		//회원기본변경페이플랜예약조회
		public LData retvMbrBasMdPplPrg(LData iMydtCstRvlInfInqEbc) throws DevonException {
			iMydtCstRvlInfInqEbc.setString("해제여부_V1", "0");
			iMydtCstRvlInfInqEbc.setString("처리여부_V1", "2");
			LCommonDao dao = new LCommonDao("card/MydtCstRvlInfInqEbc.xml/selectMbrBasMdPplPrg",iMydtCstRvlInfInqEbc);
			return dao.executeQueryForSingle();
		}
		
	}
}


