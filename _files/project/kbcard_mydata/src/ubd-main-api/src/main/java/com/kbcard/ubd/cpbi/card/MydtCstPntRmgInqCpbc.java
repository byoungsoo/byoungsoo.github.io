package com.kbcard.ubd.cpbi.card;

import com.kbcard.ubd.UBDcommon.MainCommon;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.cpbi.cmn.UbdDmdRspPhsLdinCpbc;
import com.kbcard.ubd.ebi.card.MydtCstPntRmgInqEbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.context.ContextHandler;
import devon.core.exception.DevonException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.channel.message.header.SystemHeaderManager;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.core.context.ExtensionContextKey.LContextKey;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.persistent.externalinterface.constants.EAICodeConstants;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;

public class MydtCstPntRmgInqCpbc {

	UbdCommon 	ubdCommon  	= new UbdCommon();	 //UbdCommon공통 호출
	MainCommon mainCommon = new MainCommon();
	LData  		rUbdCommon  = new LData();		 //ubd공통 결과
	LData  		header_info = new LData();		 //ubd헤더 결과
	
	MydtCstPntRmgInqEbc mydtCstPntRmgInqEbc = new MydtCstPntRmgInqEbc(); //포인트
	
	LData point_item = new LData(); 		 //마이데이터포인트정보
	LMultiData point_ary = new LMultiData(); //마이데이터포인트목록
	
	LData iMydtCstPntRmgInqCpbc = new LData(); //마이데이터고객포인트잔여조회Cpbi 입력
	LData rMydtCstPntRmgInqCpbc = new LData(); //마이데이터고객포인트잔여조회Cpbi 출력		
	
	LData iMydtCstPntRmgInqEbc = new LData(); //마이데이터고객포인트잔여조회Ebi 입력
	LData rMydtCstPntRmgInqEbc = new LData(); //마이데이터고객포인트잔여조회Ebi 출력
	
	POINT_CONST pointConst = new POINT_CONST();
	/** @throws LException 
	 * @logicalName   CDC포인트잔여조회 */
	public LData retvCdcPntRmg( LData input ) throws LException {
		LData result 	= new LData(); //결과값 LData
		LData rRetvPntCstinf = new LData(); //포인트고객정보조회 출력
		header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 
		
		String token = "";
		String ci="";
		LData cust_info = new LData();
		
		LLog.debug.println( " ☆☆☆ CDC포인트잔여조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		
		try {
			
			header_info = ubdCommon.get_header( ); 		//헤더값 가져오기 
			token = header_info.getString("Authorization"); //접근토큰

			if( UBD_CONST.PRTL_DTCD_PRTL.equals( header_info.getString("potal-dc-cd") ) ) { //검증거래 일시
				ci = header_info.getString("ci_ctt"); // 접근토큰
			}else {
				cust_info = ubdCommon.select_cust_info(token); // 포털에서 ci정보 조회
				ci = cust_info.getString("CI내용");
			}
			
			rRetvPntCstinf = retvPntCstinf( ci ); //포인트고객정보조회
			
			//고객존재검증
			if( rRetvPntCstinf.isEmpty() || StringUtil.trimNisEmpty( rRetvPntCstinf.getString("고객식별자") ) ) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40403	 	 ); // 응답코드(40403)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40403	 ); // 응답메시지(정보주체(고객) 미존재)
				return result;
			}
			
			iMydtCstPntRmgInqCpbc.setString("고객식별자",   rRetvPntCstinf.getString("고객식별자"));
			iMydtCstPntRmgInqCpbc.setString("고객관리번호", rRetvPntCstinf.getString("고객관리번호"));
			
			rMydtCstPntRmgInqCpbc = retvPnt(iMydtCstPntRmgInqCpbc); //잔여포인트조회 
			
			if(    StringUtil.trimNisEmpty( rMydtCstPntRmgInqCpbc.getString("포인트소멸점수") )
				&& StringUtil.trimNisEmpty( rMydtCstPntRmgInqCpbc.getString("포인트잔여점수") )) {
				result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40402	 	 ); // 응답코드(40402)
				result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40402	 ); // 요청한 정보(자산,기관정보, 전송요구내역)가 존재하지 않음
				return result;
			}
			
			//결과값 입력
			point_item.clear();
			point_item.setString( "소멸예정포인트" , rMydtCstPntRmgInqCpbc.getString("포인트소멸점수")); //M+2월 소멸예정포인트
			point_item.setString( "잔여포인트" , rMydtCstPntRmgInqCpbc.getString("포인트잔여점수"));
			point_item.setString( "포인트명" , POINT_CONST.PNT_DV_NM ); //포인트명
			
			point_ary.clear();
			point_ary.addLData(point_item);
			result.set("포인트목록", point_ary);
			result.setInt("포인트수", point_ary.size());
			
			//응답코드 입력
			result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
			result.setString("세부응답메시지", "성공");
			
			
		} catch (LBizException e) {
			LLog.debug.println( "CDC 포인트잔여조회 거래중 오류 발생 LBizException ☆☆☆" );
			result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50002	 	 ); // 응답코드(50002)
			 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002	 ); // 응답메시지(API 요청 처리 실패)
			return result;
		}
		 catch (LException e) {
			 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
			 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
		 }
		finally {
			rUbdCommon.setString("API거래코드", "UBD1000340");
			rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_POINT_INF_INQ);
			
			LData sppinf = new LData(); //제공테이블 입력용 추가 정보 
			sppinf = cust_info; //고객정보
			sppinf.setString("CI내용", ci);
			sppinf.setString("연계거래응답코드", rUbdCommon.getString("연계거래응답코드"));
			sppinf.setString("연계거래응답메시지", rUbdCommon.getString("연계거래응답메시지"));
			sppinf.setString("인터페이스ID", rUbdCommon.getString("인터페이스ID")); 	
			sppinf.setString("API거래코드", rUbdCommon.getString("API거래코드" ));		
			sppinf.setString("API구분코드", rUbdCommon.getString("API구분코드" ));		
			
			input 	= reset_Req_Ldata	 (input		);
			result 	= reset_Rsp_Ldata	 (result	);
			
			regtTlgDmdRspHis( input, result, sppinf ) ; //전문요청응답내역등록(CDC) 
		}
		LLog.debug.println( " ☆☆☆ CDC포인트잔여조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		return result;
	}
	
	/** @throws LException 
	 * @logicalName   MCI포인트잔여조회 */
	public LData retvMciPntRmg( LData input ) throws LException {
		LLog.debug.println( " ☆☆☆ MCI포인트잔여조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		LinkHttpAdaptor httpClient = new LinkHttpAdaptor(); //대내거래 어댑터
		LData linkResponseHeader = new LData(); //eai 헤더 
		
		
		
		LData iMci 	= new LData(); //mci거래 입력
		LData rMci 	= new LData(); //mci거래 출력
		
		LData point_item_convert 	= new LData(); //포인트 전문전환용 LData
		LMultiData point_ary_convert = new LMultiData(); //마이데이터포인트목록
		
		LData rRetvCstCmn = new LData(); //고객정보조회결과
		
		LData result 	= new LData(); //결과
		
		String sdtErrMsgCd = ""; //mci응답코드
		String token = "";
		String ci="";
		LData cust_info = new LData();
		
		LLog.debug.println( " ☆☆☆ MCI포인트잔여조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		
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
			
			iMci.setString("CI내용", ci );
			
			
			rMci = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GABS00001", iMci); //마이데이터 포인트정보목록조회
			
			linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);
			if(linkResponseHeader != null) {
				sdtErrMsgCd 		= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD		); // mci응답코드
			}

			if( !sdtErrMsgCd.equals("N0000000") ) {
				LLog.debug.println( "MCI 포인트정보목록조회 거래중 오류 발생 LBizException ☆☆☆" );
				LLog.debug.println( "e.getcode ☆☆☆", sdtErrMsgCd );
				rUbdCommon.clear();
				rUbdCommon = isidTrErrMsgInq( sdtErrMsgCd , "MCI" );
				
				result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
				result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
				return result;
			}
			
			
			
			rUbdCommon.setString("연계거래응답코드", SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_ERR_MSG_CD));
			point_ary_convert = rMci.getLMultiData("포인트그리드");
			
			
			
			for (int i = 0; i < point_ary_convert.size(); i++) {
				point_item_convert = point_ary_convert.getLData(i);
				point_item.setString("포인트명", point_item_convert.getString("포인트구분명_V60") );		//포인트명
				point_item.setInt("잔여포인트", point_item_convert.getInt("포인트잔여점수") );	//잔여포인트
				point_item.setInt("소멸예정포인트",  point_item_convert.getInt("포인트소멸점수") ); //M+2월 소멸예정 포인트
				point_ary.add(point_item);
			}
			//포인트 자산 없음 에러
			if(    StringUtil.trimNisEmpty( point_item.getString("잔여포인트") )
					&& StringUtil.trimNisEmpty( point_item.getString("소멸예정포인트") )) {
					result.setString("세부응답코드"		, UBD_CONST.REP_CD_NOTFOUND_40402	 	 ); // 응답코드(40402)
					result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_NOTFOUND_40402	 ); // 요청한 정보(자산,기관정보, 전송요구내역)가 존재하지 않음
					return result;
			}
			
			result.setInt("포인트수", point_ary.size());
			result.set("포인트목록", point_ary);
			result.setString("세부응답코드", UBD_CONST.REP_CD_SUCCESS);
			result.setString("세부응답메시지", "성공");
		} 
		catch ( LBizException e) {
			LLog.debug.println( "MCI 고객조회 거래중 오류 발생 LBizException ☆☆☆" );
			LLog.debug.println( "e.getcode ☆☆☆", e.getCode() );
			rUbdCommon = isidTrErrMsgInq( sdtErrMsgCd , e ,"MCI" );
			result.setString("세부응답코드", rUbdCommon.getString("세부응답코드")	);
			result.setString("세부응답메시지", rUbdCommon.getString("세부응답메시지"	)	);
			return result;
		} catch (LException e) {
			 result.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
			 result.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
		 }finally {
			rUbdCommon.setString("인터페이스ID", "UBD_1_GABS00001");
			rUbdCommon.setString("API거래코드", "UBD1000340");
			rUbdCommon.setString("API구분코드", UBD_CONST.API_DTCD_POINT_INF_INQ);
			
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
			
			regtTlgDmdRspHis( input, result, iMci, rMci , sppinf ) ;  //전문요청응답내역등록(MCI) 
		}
		
		LLog.debug.println( " ☆☆☆ MCI포인트잔여조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		return result;
	}
	
	
	/** 포인트조회(CDC) 
	 * @throws DevonException */
	private LData retvPnt( LData input ) throws DevonException {
		LLog.debug.println( " ☆☆☆ 잔여포인트조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		
		LData result = new LData();
		
		LData rRetvRmgPnt = new LData(); //잔여포인트조회 결과
		LData rRetvExtgSchSc = new LData(); //소멸예정점수조회 결과
		
		iMydtCstPntRmgInqEbc.clear();
		iMydtCstPntRmgInqEbc.setString("고객식별자", input.getString("고객식별자"));
		iMydtCstPntRmgInqEbc.setString("고객관리번호", input.getString("고객관리번호"));
		
		rRetvRmgPnt = retvRmgPnt(iMydtCstPntRmgInqEbc); //잔여포인트조회 
		
		iMydtCstPntRmgInqEbc.clear();
		iMydtCstPntRmgInqEbc.setString("고객식별자",				input.getString("고객식별자"));
		iMydtCstPntRmgInqEbc.setString("고객관리번호", 				input.getString("고객관리번호"));
		
		rRetvExtgSchSc = retvExtgSchSc( iMydtCstPntRmgInqEbc ); //소멸예정점수조회
		
		
		//결과값 입력
		result.setString( "포인트잔여점수" , rRetvRmgPnt.getString("포인트잔여점수"));
		result.setString( "포인트소멸점수" , rRetvExtgSchSc.getString("포인트소멸점수")); //M+2월 소멸예정포인트
		
		LLog.debug.println( " ☆☆☆ 잔여포인트조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		
		return result;
	}
	
	/** 잔여포인트조회 
	 * @throws DevonException */
	private LData retvRmgPnt( LData input ) throws DevonException {
		LLog.debug.println( " ☆☆☆ 잔여포인트조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		
		LData result = new LData();
		
		input = (LData) iMydtCstPntRmgInqEbc.clone();
		
		iMydtCstPntRmgInqEbc.clear();
		
		iMydtCstPntRmgInqEbc.setString("고객식별자", input.getString("고객식별자"));
		iMydtCstPntRmgInqEbc.setString("고객관리번호", input.getString("고객관리번호"));
		iMydtCstPntRmgInqEbc.setString("고객관리번호2", input.getString("고객관리번호"));
		
		rMydtCstPntRmgInqEbc.clear();
		rMydtCstPntRmgInqEbc = mydtCstPntRmgInqEbc.retvPntRmgBasByPntUseGrp( iMydtCstPntRmgInqEbc ); //포인트잔여기본By포인트사용그룹조회
		
		result.setString("포인트잔여점수", rMydtCstPntRmgInqEbc.getString("포인트잔여점수"));
		
		LLog.debug.println( " ☆☆☆ 잔여포인트조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		
		return result;
	}
	
	/** 소멸예정점수조회 */
	private LData retvExtgSchSc( LData input ) throws DevonException {
		LLog.debug.println( " ☆☆☆ 소멸예정점수조회 cpbi ☆☆☆ START " );
		LLog.debug.println( input );
		
		LData result = new LData();
		LMultiData rRetvLstPntMnAcmBasByExtgSchSmm = new LMultiData(); //포인트월누적기본By소멸예정집계목록조회 결과
		
		int pntExtgSc = 0;  //포인트 소멸점수
		String mydtPntEfcvSchYmd = ""; //마이데이터포인트 실효예정년월일
			   mydtPntEfcvSchYmd =  DateUtil.getDateStr();						  //당일 기준
			   mydtPntEfcvSchYmd = DateUtil.retrieveMonthlyLastSysDate(mydtPntEfcvSchYmd, POINT_CONST.PNT_EXTG_SCH_MM); //2개월후 말일
		iMydtCstPntRmgInqEbc.clear();
		iMydtCstPntRmgInqEbc.setString("고객식별자", input.getString("고객식별자") );
		iMydtCstPntRmgInqEbc.setString("고객관리번호", input.getString("고객관리번호") );
		iMydtCstPntRmgInqEbc.setString("조회시작년월일", DateUtil.addMonth( DateUtil.getDateStr(), -POINT_CONST.PNT_INQ_ST_TRM_MM)); //61개월 이전
		iMydtCstPntRmgInqEbc.setString("포인트실효예정년월일시작", DateUtil.getDateStr() ); //거래년월일 : 99991231
		iMydtCstPntRmgInqEbc.setString("포인트실효예정년월일종료", mydtPntEfcvSchYmd ); //거래년월의 2개월 후 말일  
		
		rRetvLstPntMnAcmBasByExtgSchSmm = mydtCstPntRmgInqEbc.retvLstPntMnAcmBasByExtgSchSmm( iMydtCstPntRmgInqEbc ); //포인트월누적기본By소멸예정집계목록조회 
		
		LData item = new LData();
		for (int inx = 0; inx < rRetvLstPntMnAcmBasByExtgSchSmm.size(); inx++) {
			item = rRetvLstPntMnAcmBasByExtgSchSmm.getLData(inx);
			pntExtgSc = item.getInt("포인트소멸점수") + pntExtgSc;
		}
		
		result.setInt("포인트소멸점수", pntExtgSc);
		
		LLog.debug.println( " ☆☆☆ 소멸예정점수조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		
		return result;
	}
	
	/** 포인트고객정보조회 
	 * @throws DevonException */
	private LData retvPntCstinf( String ci ) throws DevonException {
		LLog.debug.println( " ☆☆☆ 포인트고객정보조회 cpbi ☆☆☆ START " );
		LLog.debug.println( ci );
		LData result = new LData();
		
		rMydtCstPntRmgInqEbc.clear();
		rMydtCstPntRmgInqEbc = retvCiLnkdInfCnoMtlt( ci ); //CI연계정보고객번호상호조회
		
		//고객정보 없을시 준회원 정보조회
		if( rMydtCstPntRmgInqEbc.isEmpty() ) {
			rMydtCstPntRmgInqEbc.clear();
			rMydtCstPntRmgInqEbc = retvSemiMbrBas( ci ); //준회원기본조회 
		}
		
		if( !rMydtCstPntRmgInqEbc.isEmpty() ) {
			result.setString("고객식별자", rMydtCstPntRmgInqEbc.getString("고객식별자"));
			result.setString("고객관리번호", "00000");
			result.setString("CI내용", ci);
		}
		
		LLog.debug.println( " ☆☆☆ 포인트고객정보조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		
		return result;
	}
	
	/**CI연계정보고객번호상호조회
	 * @throws DevonException */
	private LData retvCiLnkdInfCnoMtlt ( String ci ) throws DevonException {
		LLog.debug.println( " ☆☆☆ CI연계정보고객번호상호조회 cpbi ☆☆☆ START " );
		LLog.debug.println( ci );
		LData result = new LData();
		result =  mydtCstPntRmgInqEbc.retvCiLnkdNoCsid( ci );
		LLog.debug.println( " ☆☆☆ CI연계정보고객번호상호조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		return result;
	}
	
	/** 준회원기본조회 
	 * @throws DevonException */
	private LData retvSemiMbrBas ( String ci ) throws DevonException {
		LLog.debug.println( " ☆☆☆ CI연계정보고객번호상호조회 cpbi ☆☆☆ START " );
		LLog.debug.println( ci );
		LData result = new LData();
		result =  mydtCstPntRmgInqEbc.retvSemiMbrBasInqByCiCtt( ci ); //준회원기본조회ByCI내용조회
		
		if( StringUtil.trimNisEmpty( result.getString("고객식별자") )){
			result.setString("고객식별자", result.getString("준회원식별자") );
		} 
		
		LLog.debug.println( " ☆☆☆ CI연계정보고객번호상호조회 cpbi ☆☆☆ END " );
		LLog.debug.println( result );
		return result;
	}
	
	/** @logicalName 대내거래 에러 메세지 조회 */
	public LData isidTrErrMsgInq( String errcode , String tran_type) throws LException{
		LBizException e = new LBizException();
		return isidTrErrMsgInq( errcode, e ,tran_type );
	}
	
	/** @logicalName 대내거래 에러 메세지 조회 */
	public LData isidTrErrMsgInq(String errcode, LBizException e, String tran_type) throws LException {
		LData iRespon_data = new LData();
		LData rRespon_data = new LData();
		LData rRetvRspCdMapping = new LData(); //UDB오픈API응답코드결과
		
		ErrorCodeMessageService err_info = ErrorCodeMessageService.getInstance();

		String sErrCode = "";
		String sErrMsg = "";
		String sPrcdpErrCd = "";  // 처리계 에러코드
		String sPrcdpErrMsg = ""; // 처리계 에러메세지
		
		
		LLog.debug.println("응답코드 매핑 입력 errcod 수신 ☆☆☆", errcode);

		// 에러코드가 없다면 exception내의 에러코드 캐치
		if (StringUtil.trimNisEmpty(errcode)) {
			sErrCode = e.getCode();
		} else {
			sErrCode = errcode;
		}

		/*************************************************************************************************************************
		 * 에러 메세지 추출
		 *************************************************************************************************************************/
		String err_msg = "";
		/*************************************************************************************************************************
		 *  MCI오류코드 조회 : GCB오류메시지코드 테이블( TBUBDJ001 )에서 에러코드 조회 
		 *************************************************************************************************************************/
		if ( StringUtil.trimNisEmpty( sErrMsg ) ) {
			err_msg = err_info.getErrorCodeMessage(sErrCode);
			LLog.debug.println("GCB오류메시지코드 테이블( TBUBDJ001 )에서 에러코드 조회 수신 결과 ☆☆☆", err_msg);
		}
		/*************************************************************************************************************************
		 * EAI오류코드 조회
		 *************************************************************************************************************************/
		if ( StringUtil.trimNisEmpty( sErrMsg ) ) {
			try {
				err_msg = EAICodeConstants.EAI_ERRCODE.valueOf(sErrCode).getMessage();
				LLog.debug.println("EAI오류코드 조회 수신 결과 ☆☆☆", err_msg);
			} catch (IllegalArgumentException le) {
				LLog.debug.println("EAI오류없음 SKIP-- ☆☆☆");
			}
		}
		//처리계 에러코드, 메세지 입력
		sPrcdpErrCd = sErrCode;
		sPrcdpErrMsg = err_msg;
		/*************************************************************************************************************************
		 * 오픈뱅킹 에러코드 조회
		 *************************************************************************************************************************/
		LData iRspCdMap = new LData();
		iRspCdMap.setString("오픈API언어구분코드"	, "KOR"		);
		iRspCdMap.setString("오픈API업무구분코드"	, "UBD"		); 
		iRspCdMap.setString("언어구분코드"			, "KOR"		);
		iRspCdMap.setString("메시지채널구분코드"	, "01"		);	// 01(단말)
		iRspCdMap.setString("오류메시지코드"		, sPrcdpErrCd	);		
		iRspCdMap.setString("오류메시지출력내용"	, sPrcdpErrMsg	);	
		iRspCdMap.setString("처리계호출방식"		, tran_type	); // 처리계호출방식(CDC, MCI, EAI)  
		rRetvRspCdMapping 	= (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.UbdMdulSptFntCpbc", "retvRspCdMapping", iRspCdMap);  ;
		
		if( ! rRetvRspCdMapping.isEmpty() ) {
			rRespon_data.setString("세부응답코드"		, rRetvRspCdMapping.getString("오픈API응답코드"		)	 	 ); // 응답코드(50001) //시스템 장애
			rRespon_data.setString("세부응답메시지"		, rRetvRspCdMapping.getString("오픈API응답메시지내용") 		 ); // 응답메시지(정보주체(고객) 미존재)
		}else {
			if( StringUtil.trimNisEmpty(err_msg) ) { err_msg = UBD_CONST.REP_CD_MSG_SERVER_ERR_50001; } //응답메세지 없을시 시스템장애 입력 
			rRespon_data.setString("세부응답코드"		, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
			rRespon_data.setString("세부응답메시지"		, sPrcdpErrMsg	 ); // 응답메시지(정보주체(고객) 미존재)
		}
		
		rRespon_data.setString("연계거래응답코드"		, sPrcdpErrCd 	 ); 
		rRespon_data.setString("연계거래응답메시지"		, sPrcdpErrMsg	 ); 
		
		return rRespon_data;
	}

	/** 전문요청응답내역등록(CDC) 
	 * @throws LException */
	public void regtTlgDmdRspHis( LData input, LData result,  LData tCstidf ) throws LException {
		regtTlgDmdRspHis( input, result, null, null , tCstidf );
	}
	
	/** 전문요청응답내역등록(MCI) 
	 * @throws LException */
	public void regtTlgDmdRspHis( LData input, LData output, LData mciInput, LData mciOutput , LData tCstidf ) throws LException {
		regtTlgDmdRspHis( input, output, mciInput, mciOutput , null , null , tCstidf );
	}
	
	/** 전문요청응답내역등록(MCI, EAI) 
	* @throws LException */
	public void regtTlgDmdRspHis( LData input, LData output, LData mciInput, LData mciOutput , LData eaiInput , LData eaiOutput , LData sppinf ) throws LException {

			LData syncInput 	= new LData();
			LData syncOutput 	= new LData();
			LData syncMciInput 	= new LData();
			LData syncMciOutput = new LData();
			LData syncEaiInput 	= new LData();
			LData syncEaiOutput = new LData();
			LData lEncInf 		= new LData();
			LData iHeader_info 	= new LData();
			
			LData linkResponseHeader = new LData(); //MCI헤더 객체
			String sdtLnkdOgtnGidNo 	= "";	//MCI GUID
			
			iHeader_info.setString("apiDtcd", sppinf.getString("API구분코드"));
			header_info = ubdCommon.get_header( iHeader_info );
			
			String sPrtlDcCd       		= header_info.getString("potal-dc-cd");   	//포탈구분코드
			String sMydtUtzInsCd    	= header_info.getString("UTZ_INS_CD");   	//이용기관코드
			String sCdcMciGb       		= header_info.getString("tran_dv_cd");   	//거래구분코드
			String sRtvlTrsYN   		= header_info.getString("x-api-type");    	//정기적전송여부
			String sMydtTrUno 			= header_info.getString("x-api-tran-id");	// 마이데이터거래고유번호
			String sMydtTrsTgDtcd		= header_info.getString("x-client-type");	// API마이데이터전송대상구분코드
			
			
			String sPrcMciInsGb = "N"; // 요청내역상세 - MCI insert 입력여부
			String sPrcEaiInsGb = "N"; // 요청내역상세 - EAI insert 입력여부
			
			String sErrCodePrc 			= sppinf.getString("연계거래응답코드"); //MCI응답 코드
			String sErrMsgPrc  			= sppinf.getString("연계거래응답메시지");; //MCI응답메세지
			
			String sErrCode 			= output.getString("세부응답코드");	
			String sErrMsg 				= output.getString("세부응답메시지");
			
			String sCICtt    			= sppinf.getString("CI내용"); 
			String sCstIdf   			= sppinf.getString("고객식별자"); 
			String sCstMgNo  			= sppinf.getString("고객관리번호");
			String sMydtClintIdiNo		= sppinf.getString("마이데이터클라이언트식별번호");
			
			String sInnMciLinkIntfId 	= sppinf.getString("인터페이스ID"); 	// MCI LINK 인터페이스ID(대출상품목록조회)
			String sTranId				= sppinf.getString("API거래코드");		// 거래코드
			String sApiTrDtcd			= sppinf.getString("API구분코드");		// 거래코드
			
			
			
			
			lEncInf.setString("마이데이터전송대상구분코드"		, sMydtTrsTgDtcd				);
			lEncInf.setString("마이데이터클라이언트식별번호"	, sMydtClintIdiNo				);
			if(!sCdcMciGb.equals("CDC")) {
				// MCI, EAI 일 경우에만 입력 처리함.
				linkResponseHeader	= (LData) ContextHandler.getContextObject(LContextKey.LINK_SYSTEM_HEADER);			
				if(!LNullUtils.isNone(linkResponseHeader)) {
					sdtLnkdOgtnGidNo 	= SystemHeaderManager.getValueFromLData(linkResponseHeader, KBCDSYSTEM_HEADERFIELD.SDT_LNKD_OGTN_GID_NO	); // 연계원거래 GUID
					
					if(!StringUtil.trimNisEmpty(sdtLnkdOgtnGidNo)) {
						if(!LNullUtils.isNone(mciInput)) {
							sPrcMciInsGb 		= "Y"; // MCI 요청상세내역 입력 처리.
						}else if(!LNullUtils.isNone(eaiInput)) {
							sPrcEaiInsGb 		= "Y"; // EAI 요청상세내역 입력 처리.
						}
						
						if( StringUtil.trimNisEmpty( sErrMsgPrc )  ) {
							if( sErrCodePrc.equals( "N0000000" ) ) {
								sErrMsgPrc = "성공";
							}
						}
					}
				}
			} 
			
			//비동기 거래내역 등록 호출
//			DmdRspPhsLdinCpbc AsyncRunner = new DmdRspPhsLdinCpbc();
			UbdDmdRspPhsLdinCpbc AsyncRunner = new UbdDmdRspPhsLdinCpbc();
			
			lEncInf.setString("거래고유번호"			, sMydtTrUno					);
			lEncInf.setString("마이데이터이용기관코드"	, sMydtUtzInsCd					);
			lEncInf.setString("API구분코드"				, sApiTrDtcd					);
			lEncInf.setString("포탈분기구분코드"		, sPrtlDcCd						);
			lEncInf.setString("처리계시스템구분"		, sCdcMciGb						);
			lEncInf.setString("마이데이터전송대상구분코드"		, sMydtTrsTgDtcd				);
			lEncInf.setString("마이데이터클라이언트식별번호"	, sMydtClintIdiNo				);
			lEncInf.setString("CI내용"					, sCICtt						);
			lEncInf.setString("고객식별자"				, sCstIdf						);
			lEncInf.setString("고객관리번호"			, sCstMgNo						);
			lEncInf.setString("마이데이터정기전송여부"	, sRtvlTrsYN					);			
			lEncInf.setString("오픈API응답코드"			, sErrCode						);
			lEncInf.setString("오픈API응답메시지내용"	, sErrMsg						);
			lEncInf.setString("오류메시지코드"			, sErrCodePrc					);
			lEncInf.setString("오류메시지출력내용"		, sErrMsgPrc					);
			lEncInf.setString("EAI원거래GUID"			, sdtLnkdOgtnGidNo				);
			lEncInf.setString("EAI인터페이스ID"			, sInnMciLinkIntfId				);
			lEncInf.setString("EAI요청상세입력여부"		, sPrcEaiInsGb					);
			lEncInf.setString("EAI오류메시지코드"		, sErrCodePrc					);
			lEncInf.setString("EAI오류메시지출력내용"	, sErrMsgPrc					);
			lEncInf.setString("MCI원거래GUID"			, sdtLnkdOgtnGidNo				);
			lEncInf.setString("MCI인터페이스ID"			, sInnMciLinkIntfId				);
			lEncInf.setString("MCI오류메시지코드"		, sErrCodePrc					);
			lEncInf.setString("MCI요청상세입력여부"		, sPrcMciInsGb					);
			lEncInf.setString("MCI오류메시지출력내용"	, sErrMsgPrc					);
			lEncInf.setString("시스템최종갱신식별자"	, sTranId						);		

			
			AsyncRunner.setLogParam( input, output, mciInput, mciOutput, eaiInput, eaiOutput, lEncInf );
//			AsyncRunner.setLogParam( input, output, mciInput, mciOutput, eaiInput, eaiOutput, lEncInf );
			AsyncRunner.start();
		}


	/**
	 * @serviceID initRtnSetting
	 * @logicalName 전문 초기화 세팅
	 * @param LData 
	 */	
	private LData reset_Req_Ldata(LData input) {
		LData result = new LData();

		result.setString("기관코드"			, input.getString("기관코드") 			); //기관코드		
		result.setString("조회타임스탬프"	, input.getString("조회타임스탬프") 	); //조회타임스탬프
		
		return result;
	}

	private LData reset_Rsp_Ldata(LData input) {
		LData result = new LData();
		
		result.setString("세부응답코드"			, input.getString("세부응답코드") 			); //응답코드		
		result.setString("세부응답메시지"			, input.getString("세부응답메시지") 			); //응답메세지
		
		if( UBD_CONST.REP_CD_SUCCESS.equals(input.getString("세부응답코드")) ) {// 응답코드(성공)
			result.setInt("포인트수"			, input.getInt("포인트수") 			); //포인트수		
			result.set("포인트목록"				, input.getLMultiData("포인트목록") 	); //포인트목록
		} 
		
		return result;
	}

	private LData reset_Req_Mci_Ldata(LData input) {
		LData result = new LData();

		result.setString("CI내용"			, input.getString("CI내용") 			); 		
		result.setString("다음조회키_V1000"	, input.getString("다음조회키_V1000") 	); 
		result.setString("페이지사이즈_N5"	, input.getString("페이지사이즈_N5") 	); 
		
		return result;
	}
	
	private LData reset_Rsp_Mci_Ldata(LData input) {
		LData result = new LData();
		
		result.setString("CI내용"			,input.getString("CI내용")				);
		result.setString("고객식별자"		,input.getString("고객식별자")			);
		result.setString("고객관리번호"		,input.getString("고객관리번호")		);
		result.setString("다음존재여부_V1"	,input.getString("다음존재여부_V1")		);
		result.setString("다음조회키_V1000"	,input.getString("다음조회키_V1000")	);
		
		if( !LNullUtils.isNone(input) ) {
			result.setInt("포인트그리드_cnt"	,input.getInt("포인트그리드_cnt")		);
			result.setString("포인트그리드"		,input.getString("포인트그리드")		);
			result.setString("포인트구분코드"	,input.getString("포인트구분코드")		);
			result.setString("포인트구분명_V60"	,input.getString("포인트구분명_V60")	);
			result.setInt("포인트잔여점수"		,input.getInt("포인트잔여점수")			);
			result.setInt("포인트소멸점수"		,input.getInt("포인트소멸점수")			);
		}
		
		return result;
	}
	
	class POINT_CONST{
		//TODO MS05722 하단의 코드가 어떤의미인지 .. 어디서 찾는지..
		final static String PNT_DV_NM 				= "포인트리"; 	//포인트 구분명
		final static int	PNT_INQ_ST_TRM_MM		= 61; 	//포인트조회시작기간월
		final static int	PNT_EXTG_SCH_MM 		= 2; 	//포인트소멸예정월
		
		String 세부응답코드 = "";
		String 세부응답메시지 = "";
		
		
	}


}
