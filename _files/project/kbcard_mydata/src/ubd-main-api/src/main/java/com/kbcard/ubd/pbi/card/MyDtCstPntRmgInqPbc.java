package com.kbcard.ubd.pbi.card;

import com.kbcard.ubd.UBDcommon.MainCommon;
import com.kbcard.ubd.common.crypto.util.UtilConversion;
import com.kbcard.ubd.common.pbi.token.TokenCommonPbc;
import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.common.util.CryptoDataUtil;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;
import com.kbcard.ubd.cpbi.card.MydtCstPntRmgInqCpbc;
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCdcSptFntCpbc;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;

import devon.core.collection.LData;
import devon.core.config.ConfigurationUtil;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.StackTraceUtil;
import devonenterprise.ext.util.StringUtil;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 
 *
 * @logicalName   마이데이터고객포인트잔여조회Pbi
 * @lastDate      2021-05-31
 */
public class MyDtCstPntRmgInqPbc {
	TokenCommonPbc tokenCommonPbc = new TokenCommonPbc(); //공통 토큰 호출
	UbdCommon ubdCommon   = new UbdCommon();	 //ubd공통 호출
	LData 	  header_info = new LData(); 		 //헤더정보
	LData 	  rUbdCommon  = new LData();		 //공통common
	MainCommon mainCommon = new MainCommon();
	/**
	 * 
	 *
	 * @serviceID     UBD1000340
	 * @logicalName   마이데이터포인트정보목록조회
	 * @param         LData input 
	 *                            LData iRetvLstMydtPntInfP i마이데이터포인트정보목록조회P : 포인트정보목록조회입력Dto
	 * @return        LData rData 
	 *                            LData rRetvLstMydtPntInfP r마이데이터포인트정보목록조회P : 포인트정보목록조회결과Dto
	 * @exception     LException occurs error 
	 * 
	 */
	public LData retvLstMydtPntInf( LData input ) throws LException {
		LLog.debug.println( " ☆☆☆ 마이데이터포인트정보목록조회 pbi ☆☆☆ START " );
		//PORCDC, null, "" 테스트
//		LData test1 =  mainCommon.get_header();
//		LLog.debug.println( test1 );
		
//		TokenCommonPbc tokenCommonPbc = new TokenCommonPbc();
//		LData result = tokenCommonPbc.verifyToken("5VZ/V6vfWV8xWT91gLgn5yswVWPFG8RL+cr3JHxqILrVLf9ixcdLx9FogFmAXPsIy7DtGUeVHm+p+N583y5WAQ==", "capital");
//		String rep_code = result.getString( "error" );
//		if( ! rep_code.equals( UBD_CONST.REP_CD_SUCCESS ) ) {
//			result.setString("세부응답코드"	   	, 	result.getString( "error" ) 			 ); 
//			result.setString("세부응답메시지"	, 	result.getString( "error_discription" )	 ); 
//			return result;
//		}
//		LLog.debug.println( ConfigurationUtil.getEnvType()  );
		MydtCstPntRmgInqCpbc mydtCstPntRmgInqCpbc = new MydtCstPntRmgInqCpbc(); //마이데이터 포인트 정보목록조회 cpbi
		
		LData iRetvLstMydtPntInfP = new LData(); //마이데이터포인트정보목록조회pbi 입력
		LData rRetvLstMydtPntInfP = new LData(); //마이데이터포인트정보목록조회pbi 출력
		
		LData iMydtCstPntRmgInqC = new LData(); //마이데이터포인트정보목록조회cpbi 입력
		LData rMydtCstPntRmgInqC = new LData(); //마이데이터포인트정보목록조회cpbi 출력
		
		
		LData rVerifyIn = new LData(); //입력값 검증결과
		

		LLog.debug.println( input );
		String test = "test";
		if( !StringUtil.trimNisEmpty(input.getString("기관코드")) && test.equals(input.getString("기관코드").substring(0, 4)) ) {
			
			String plag = input.getString("기관코드").substring( 4);
			switch (plag) {
			case "mci":
				test_mci();
				break;
			case "eai":
				test_eai();
				break;
			case "enc":
				test_encrypt(DataConvertUtil.exchangeLDataToJson(input));
				break;
			default:
				test_mci();
				test_eai();
				test_encrypt(DataConvertUtil.exchangeLDataToJson(input));
				break;
			}
			rRetvLstMydtPntInfP.setString("세부응답메시지", "성공");
		}else{
			try {
				LData iHeader_info = new LData();
				iHeader_info.setString("apiDtcd", UBD_CONST.API_DTCD_POINT_INF_INQ);
				header_info = ubdCommon.get_header( iHeader_info );
				rVerifyIn = verifyIn( input ); //입력값 검증
				
				if( rVerifyIn.getBoolean("pass_flag") == false ){ //입력값 오류시
					rRetvLstMydtPntInfP.setString("세부응답코드", rVerifyIn.getString("세부응답코드") );
					rRetvLstMydtPntInfP.setString("세부응답메시지", rVerifyIn.getString("세부응답메시지") );
				}else {
					iRetvLstMydtPntInfP = input; //입력 json 수신
					
					switch (header_info.getString("tran_dv_cd")) {
						case UBD_CONST.CLLGL_SYS_DTCD_CDC:
							rRetvLstMydtPntInfP = mydtCstPntRmgInqCpbc.retvCdcPntRmg(iRetvLstMydtPntInfP); //CDC포인트잔여조회
							break;
	
						case UBD_CONST.CLLGL_SYS_DTCD_MCI:
							rRetvLstMydtPntInfP = mydtCstPntRmgInqCpbc.retvMciPntRmg(iRetvLstMydtPntInfP); //MCI포인트잔여조회
							break;
					}
				}
				
			} 
			catch ( LException e) {
				LLog.debug.println( "마이데이터포인트정보목록조회 pbi 거래중 오류 발생 LException ☆☆☆" );
				LLog.debug.println( e.getStackTraceString() );
				
				rRetvLstMydtPntInfP.setString("세부응답코드"	, UBD_CONST.REP_CD_SERVER_ERR_50001	 	 ); // 응답코드(50001) //시스템 장애
				rRetvLstMydtPntInfP.setString("세부응답메시지"		, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001	 ); // 응답메시지(시스템장애)
			}finally {
				ubdCommon.set_header(); //헤더정보 입력(거래고유번호 입력 X)
			}
		}
		LLog.debug.println( rRetvLstMydtPntInfP );
		LLog.debug.println( " ☆☆☆ 마이데이터포인트정보목록조회 pbi ☆☆☆ END " );
		return rRetvLstMydtPntInfP;
	}
	
	//입력값 검증
	private LData verifyIn( LData input ) throws LException{
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
			iDupDmd.setString("거래구분코드_V6"		, UBD_CONST.API_DTCD_POINT_INF_INQ  ); // API구분코드(포인트)
			
			bRtn = cdcSptFntCpbc.dupDmdTrVln(iDupDmd);      //중복요청거래 검증 결과 수신
			
			if( ! bRtn ) { //false : 중복거래 시
				//TODO MS05722중복거래 오류 처리 ?> 
			}
			
			//토큰 검증
			String token 	= header_info.getString("Authorization");
			String industry = UBD_CONST.BSZ_DTCD_CARD;
			String orgCode 	= input.getString("기관코드");
			String apiId 	= UBD_CONST.API_DTCD_POINT_INF_INQ;
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
	
	
	void test_eai() throws LException {
		
		LinkHttpAdaptor httpAdaptor 	= new LinkHttpAdaptor();

		LData eai_input = new LData();
		LData result = new LData();

		
//		try {
			eai_input.setString("검색번호구분코드_V1", "5");
			eai_input.setString("CI내용", "98302jdienndi1foio13mn98302jdienndi1foio13mn98302jdienndi1foio13mn98302jdienndi1foio13mn");
			LLog.debug.println( "EAI 진입 ☆☆☆" );
			result = httpAdaptor.sendOutboundMessage(TargetTypeConst.EAI, "UBD_2_GABS00001", eai_input);
			LLog.debug.println( "EAI 성공 ☆☆☆" );
			LLog.debug.println( result );
//		} 
//		catch ( LBizException e) {
//			LLog.debug.println( "EAI 고객조회 거래중 오류 발생 LBizException ☆☆☆" );
//			LLog.debug.println( "e.getcode ☆☆☆", e.getCode() );
//			throw e;
//		}
		
	}
	
	
	void test_mci() throws LException {
		
		LinkHttpAdaptor httpClient = new LinkHttpAdaptor();
		
		LData mci_input = new LData();
		LData result = new LData();
		
//		try {
			LLog.debug.println( "MCI 진입 ☆☆☆" );
			mci_input.setString("CI내용", "98302jdienndi1foio13mn98302jdienndi1foio13mn98302jdienndi1foio13mn98302jdienndi1foio13mn");
//			mci_input.setString("조회시작년월", "202005");
//			mci_input.setString("조회종료년월", "202012");
//			mci_input.setString("다음조회키_V1000", "");
//			mci_input.setString("페이지사이즈_N5", "5");
			result = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GABS00001", mci_input);  /// 포인트
//			result = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00005", mci_input);  /// 리볼빙
//			result = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GBCS00004", mci_input);  /// 대출상품목록조회
//			result = httpClient.sendOutboundMessage(TargetTypeConst.MCI, "UBD_1_GEAS00003", mci_input);  /// 대출상품계좌기본정보조회
			LLog.debug.println( "MCI 성공 ☆☆☆" );
			LLog.debug.println( result );
//		} 
//		catch ( LBizException e) {
//			LLog.debug.println( "MCI 고객조회 거래중 오류 발생 LBizException ☆☆☆" );
//			LLog.debug.println( "e.getcode ☆☆☆", e.getCode() );
//			throw e;
//		}
	}
	public String test_encrypt(String plain) throws LException {
		LLog.debug.println("☆☆☆☆ 압축암호화 테스트 ☆☆☆");
		String sEncCtt = null;
		String rRtnCtt = null;		
		byte[] zip_ByteData = null;

			LLog.debug.println("################################# Start Compress & Enc ");
			LLog.debug.println("################################# plain = " + plain);
			LLog.debug.println("################################# plain.byte = " + plain.getBytes());
			
			sEncCtt = UtilConversion.compress(plain);
//			zip_ByteData = UtilConversion.compress(plain.getBytes());
			
			LLog.debug.println("################################# zip_ByteData = " + zip_ByteData);
			
			// TODO : 암호화키 적용 필요
			LLog.debug.println("☆☆☆☆ 압축암호화 이전 : ", sEncCtt );
			rRtnCtt = CryptoDataUtil.encryptSplit(  CryptoDataUtil.SCP_DEF_INI_PATH
					  , CryptoDataUtil.KB_UBE_SERVER_KEY
					  , sEncCtt.getBytes());
			if( rRtnCtt == null ) {
				throw new LException();
			}

			LLog.debug.println("☆☆☆☆ 압축암호화 이후 : ", rRtnCtt );
			
			LLog.debug.println("################################# encData = " + rRtnCtt);
		
		return rRtnCtt;
	}
}



class LCommonDaoDefault extends LCommonDao{
	public LCommonDao LCommonDaoDefault( String path, LData input ) {
		
		return null;
	}
}