package com.kbcard.ubd.pbi.card;
 
import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.cpbi.cmn.UbdDmdRspPhsLdinCpbc;

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
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.TypeConvertUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;
import devonframework.front.channel.context.LActionContext;

public class MyDtCrdBasInfPbc {

	/**
	 * @serviceID 마이데이터API 카드기본정보조회
	 * @logicalName 
	 * @param  LData iMyDtApiTlgIn
	 * @return LData rMyDtApiTlgOut
	 * @exception 
	 */
	
	/*마이데이터API INPUT*/
	LData iMyDtApiTlgIn = new LData();
	/*마이데이터API OUTPUT*/
	LData rMyDtApiTlgOut = new LData();
	/*MCI호출 INPUT*/
	LData iMciInput = new LData();
	/*MCI호출 OUTPUT*/
	LData rMciOutput = new LData();
	/*요청제공상세등록 테이블 기타 LData*/
	LData lEncInf = new LData();
	
	public LData retvMyDtApiCrdBasInf(LData input) throws LException {
		
		iMyDtApiTlgIn = (LData) input;
		
		LData iCrdBasInfInqIn = new LData(); //카드기본정보조회 
		LData rCrdBasInfInqOut = new LData(); //카드기본정보출력
		
		LData iCardPdIsuCtgInqIn = new LData(); //카드상품발급목록조회
		LMultiData rCardPdIsuCtgInqOut = new LMultiData(); //카드상품발급목록출력
		
		LData iPrcPtRceBhCrdDtlImpaHisInfIn = new LData(); //처리유형수령부점카드상세중요내역정보조회
		LData rPrcPtRceBhCrdDtlImpaHisInfOut = new LData(); //처리유형수령부점카드상세중요내역정보출력
		
		LData iCrdAnfInfOferInqIn = new LData(); //카드연회비정보제공조회
		LData rCrdAnfInfOferInqOut = new LData(); //카드연회비정보제공출력
		
		LData iCardFuncPtExstVldCfmIn = new LData(); //카드기능유형존재유효성확인조회
		LMultiData rCardFuncPtExstVldCfmOut = new LMultiData(); //카드기능유형존재유효성확인출력
		
		LData iCrdPdGnrAliAnfCtgInqIn = new LData(); //카드상품일반제휴연회비목록조회
		LMultiData rCrdPdGnrAliAnfCtgInqOut = new LMultiData(); //카드상품일반제휴연회비목록출력	
		
		LData iCrdPdPrePlagCtgInqIn = new LData(); //카드상품가격설계목록조회
		LMultiData rCrdPdPrePlagCtgInqOut = new LMultiData(); //카드상품가격설계목록출력

		LData iBasAnfBseCrdBrdDtcdIn = new LData(); //기본연회비기준카드브랜드구분코드조회
		LMultiData rBasAnfBseCrdBrdDtcdOut = new LMultiData(); //기본연회비기준카드브랜드구분코드출력
		
		LData iBasAnfBseInqByInqBseYmdInqIn = new LData(); //기본연회비기준조회BY조회기준년월일조회
		LData rBasAnfBseInqByInqBseYmdInqOut = new LData(); //기본연회비기준조회BY조회기준년월일출력
		
		LData iCrdPdBrdCtgIn = new LData(); //카드상품브랜드목록조회
		LMultiData rCrdPdBrdCtgOut = new LMultiData(); //카드상품브랜드목록출력
		
		LMultiData iGnrAliAnfInf =  new LMultiData(); 		//상품연회비정보 LMultiData
		
		/*카드전송요구조회*/
		LData iTrsRqstYnCdnoInqIn = new LData();
		LData rTrsRqstYnCdnoInqOut = new LData();
		
		LData cust_info = new LData();		//고객정보
		/*에러코드*/
		String rErrCode = "";
		/*에러메시지*/
		String rErrMsg  = "";
		/*고객식별자*/
		String sCstIdf      = "";
		/*고객관리번호*/
		String sCstMgNo 	= "00000";
		/*마이데이터전송대상구분코드*/
		String sMydtTrsTgDtcd = "";
		/*마이데이터클라이언트식별번호*/
		String sMydtClintIdiNo ="";
		String sRfCrdYn 	= "";
		String sIsuBseYmd 	= "";
		
		long lBasAnf = 0; 
		long lAliAnf = 0;
		
		//카드기능유형코드값 셋팅
		String s029Yn 		= "";
		String s314Yn 		= "";
		String s330Yn 		= "";
		String s580Yn 		= "";
		
		//20210617 CDC거래 테스트케이스작성
		String sCdcMciGb	= "";
		
		// 1. 요청데이터 수신 ----------------------------------------------------------------------------------------
		LMultiData pathParam = (LMultiData) LActionContext.get(LActionContext.PATH_PARAM);
		// 1-1 헤더값 체크 -------------------------------------------------------------------------------------------
		/** SELECT * FROM INSTC.TBUBDS301  UBD오픈API별N서비스상태관리기본 
		 *  오픈API서비스정상여부 ='Y' CDC
		 *  오픈API서비스정상여부 ='N' MCI 
		 * */
		LData apiInput = new LData();
		apiInput.setString("apiDtcd", UBD_CONST.API_DTCD_CRD_BAS_INF_INQ);
		
		// =============================================================================
		// ######### ##마이데이터 API 헤더값 셋팅 
		// =============================================================================
		LData tdHeader = new LData();
		UbdCommon uCom = new UbdCommon();
		tdHeader = uCom.get_header(apiInput);
		
		String sAccsTken		= tdHeader.getString("Authorization"); // 접근토큰
		String sMydtTrUno		= tdHeader.getString("x-api-tran-id"); // 마이데이터거래고유번호
		String sRtvlTrsYN   	= tdHeader.getString("x-api-type"	); // 정기적전송여부(1:정기적전송-"x-api-type: scheduled", 2:비정기적전송(정보주체 개입)-생략)
		String sPrtlDcCd 		= tdHeader.getString("potal-dc-cd"	); // 포탈분기구분코드 (HDR:금융결제원,POR:포탈)
		String ci_ctt			= tdHeader.getString("ci_ctt"		); // CI내용
		String sMydtUtzInsCd 	= tdHeader.getString("UTZ_INS_CD"	); // 마이데이터이용기관코드
		String cst_idf 			= tdHeader.getString("cst_idf"		); // 고객식별자
		sCdcMciGb 				= tdHeader.getString("tran_dv_cd"		); // CDC와 MCI거래를 구분하기위한 헤더값 확인
		sMydtTrsTgDtcd          = tdHeader.getString("x-client-type"	); // 마이데이터전송대상구분코드
		
		/*마이데이터 고유번호 헤더부 RETURN*/
		ContextUtil.setHttpResponseHeaderParam("x-api-tran-id", sMydtTrUno);	// 마이데이터거래고유번호
		/*cllgl_sys_dtcd 미존재시 MCI거래*/
		if (StringUtil.trimNisEmpty(sCdcMciGb)) {
			sCdcMciGb	= UBD_CONST.CLLGL_SYS_DTCD_MCI;
		}
		
		LLog.debug.println( "************* [ 헤더값 ] *****************");
		LLog.debug.println( "tdHeader : " , tdHeader);
			
		
		// =============================================================================
		// ######### ##마이데이터 API 유효성검증 (접근토큰,마이데이터 거래고유번호)
		// =============================================================================
		if (!UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
			if(StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
				setRspReturn(UBD_CONST.REP_CD_BAD_REQUEST_40002
						   , UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002 );
				return rMyDtApiTlgOut;			
			}	
			/**
			 *  ※ 거래고유번호는 API요청기관에서 생성하여 API처리기관에 전송되는 값으로, API처리기관은 HTTP 응답 헤더*에 동일한 거래고유번호를
			 *     설정하여 API요청기관에 회신 * API 정상 응답뿐만 아니라 에러응답 시에도 반드시 거래고유번호를 회신
			 *  ■ 거래고유번호 : 기관코드(10자리) + 생성주체구분코드(1자리) + 부여번호(9자리)  
			 *     "M" : 마이데이터사업자 , "S" : 정보제공자 , "R" : 중계기관 , "C" : 정보수신자 , "P" : 종합포털
			 *     
			 *  ■ INPUT  : 접근토큰
			 *  ■ OUTPUT : CI
			 */
			cust_info = uCom.select_cust_info(sAccsTken);
			
			if(StringUtil.trimNisEmpty(cust_info.getString("CI내용"))) {
				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403, UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
				return rMyDtApiTlgOut;
			}
			
			sCstIdf   = cust_info.getString("고객식별자");
			ci_ctt 	  = cust_info.getString("CI내용");
			sMydtClintIdiNo  = cust_info.getString("클라이언트식별번호");
		}else {
			
			if(StringUtil.trimNisEmpty(cst_idf) || StringUtil.trimNisEmpty(ci_ctt) ) {
				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403, UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
				return rMyDtApiTlgOut;
			}
			
			sCstIdf   = cst_idf;
		}
		
		// =============================================================================
		// ######### ##마이데이터 API 입력카드번호 존재유무 확인 
		// =============================================================================
		if (StringUtil.trimNisEmpty(pathParam.getString("card_id", 0))) {
			setRspReturn(UBD_CONST.REP_CD_BAD_REQUEST_40001
					   , UBD_CONST.REP_CD_MSG_BAD_REQUEST_40001 );
			return rMyDtApiTlgOut;
		}else {
			iMyDtApiTlgIn.setString("card_id",pathParam.getString("card_id", 0));	
		}
		
		try {
			// 호출시스템분기(CDC, MCI) 분기 로직
			if(sCdcMciGb.equals(UBD_CONST.CLLGL_SYS_DTCD_CDC)) {
				
				// =============================================================================
				// ######### ##마이데이터 API 카드식별자(card_id) 카드기본정보 조회
				// =============================================================================
				iCrdBasInfInqIn.setString("카드대체번호", iMyDtApiTlgIn.getString("card_id"));
				
				LLog.debug.println( "■■■■■카드기본정보입력■■■■■ " , iCrdBasInfInqIn);
				rCrdBasInfInqOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvMydtCrdBasInf",iCrdBasInfInqIn);
				LLog.debug.println( "■■■■■카드기본정보출력■■■■■ " , rCrdBasInfInqOut);
				if (LNullUtils.isNone(rCrdBasInfInqOut)) {
					setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403
							   , UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
					return rMyDtApiTlgOut;
				}
				/*카드만료년월일이 < 기준년월일보다 작고 해지년월일이 NULL이 아니면 만료되거나,해지된카드*/
				if (DateUtil.compareTo(rCrdBasInfInqOut.getString("카드만료년월일") , DateUtil.getCurrentDate()) < 0 || !StringUtil.trimNisEmpty(rCrdBasInfInqOut.getString("해지년월일"))) {
					// =============================================================================
					// ######### ##마이데이터 API 카드 전송요구여부 확인  
					// =============================================================================
					iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, iMyDtApiTlgIn.getString("card_id"));
					iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "card");
					iTrsRqstYnCdnoInqIn.setString("고객식별자"					, sCstIdf);
					rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);

					if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
						setRspReturn(UBD_CONST.REP_CD_FORBIDDEN_40305
								   , UBD_CONST.REP_CD_MSG_FORBIDDEN_40305);
						return rMyDtApiTlgOut;						
					}else {
						setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40404
								   , UBD_CONST.REP_CD_MSG_NOTFOUND_40404);
						return rMyDtApiTlgOut;						
					}
				}
				
				LLog.debug.println( "■■■■■카드기본정보출력■■■■■ " , rCrdBasInfInqOut);

				// =============================================================================
				// ######### ##마이데이터 API  카드상품발급목록조회 NotFoundException 예외
				// =============================================================================
				if (!StringUtil.trimNisEmpty(rCrdBasInfInqOut.getString("카드발행년월일"))) {
					iCardPdIsuCtgInqIn.setString("기준년월일", rCrdBasInfInqOut.getString("카드발행년월일"));
				}else {
					iCardPdIsuCtgInqIn.setString("기준년월일", rCrdBasInfInqOut.getString("등록년월일"));
				}
				
				iCardPdIsuCtgInqIn.setString("상품코드", rCrdBasInfInqOut.getString("상품코드"));
				LLog.debug.println( "■■■■■카드상품발급목록조회■■■■■ " ,iCardPdIsuCtgInqIn);
				rCardPdIsuCtgInqOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvLstCrdPdIsu",iCardPdIsuCtgInqIn);
				LLog.debug.println( "■■■■■카드상품발급목록출력■■■■■ " ,rCardPdIsuCtgInqOut);

				
				LData tCardPdIsuCtgInqOut = rCardPdIsuCtgInqOut.getLData(0); 
				if (UBD_CONST.STR_2.equals(tCardPdIsuCtgInqOut.getString("카드칩구분코드")) || UBD_CONST.STR_4.equals(tCardPdIsuCtgInqOut.getString("카드칩구분코드"))) {
					sRfCrdYn	= UBD_CONST.STR_1;
				}
				if (UBD_CONST.STR_3.equals(rCrdBasInfInqOut.getString("카드소유자구분코드"))){
					sRfCrdYn	= "";	
				}
				
				// =============================================================================
				// ######### ##마이데이터 API  처리유형수령부점카드상세중요내역정보조회 
				// =============================================================================
				iPrcPtRceBhCrdDtlImpaHisInfIn.setString("카드식별자", rCrdBasInfInqOut.getString("카드식별자"));
				iPrcPtRceBhCrdDtlImpaHisInfIn.setString("카드상세일련번호", rCrdBasInfInqOut.getString("카드상세일련번호"));
				iPrcPtRceBhCrdDtlImpaHisInfIn.setString("카드상세중요변경유형구분코드", "160");
				iPrcPtRceBhCrdDtlImpaHisInfIn.setString("해제구분코드_V1", "1");
				
				LLog.debug.println( "■■■■■처리유형수령부점카드상세중요내역정보조회■■■■■ ",iPrcPtRceBhCrdDtlImpaHisInfIn);
				rPrcPtRceBhCrdDtlImpaHisInfOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvPrcPtRceBhCrdDtlImpaHisInf",iPrcPtRceBhCrdDtlImpaHisInfIn);
				//rPrcPtRceBhCrdDtlImpaHisInfOut.setString("카드정보변경유형일련번호","00");
				LLog.debug.println( "■■■■■처리유형수령부점카드상세중요내역정보출력■■■■■ ",rPrcPtRceBhCrdDtlImpaHisInfOut);

				
				// =============================================================================
				// ######### ##마이데이터 API  카드기능유형존재유효성확인 - 현금카드기능 
				// =============================================================================
				sIsuBseYmd = rCrdBasInfInqOut.getString( "등록년월일" ); 
				
				if (!StringUtil.trimNisEmpty(rCrdBasInfInqOut.getString( "카드만료년월일" ))) {
					if (!StringUtil.trimNisEmpty(rCrdBasInfInqOut.getString( "카드발행년월일" ))) {
						sIsuBseYmd = rCrdBasInfInqOut.getString( "카드발행년월일" );
					}	
				}

				if (!StringUtil.trimNisEmpty(sIsuBseYmd)) {
					iCardFuncPtExstVldCfmIn.setString("조회기준년월일", sIsuBseYmd);	
				}else {
					iCardFuncPtExstVldCfmIn.setString("조회기준년월일", DateUtil.getCurrentDate());
				}
				
				
				iCardFuncPtExstVldCfmIn.setString("상품코드", rCrdBasInfInqOut.getString("상품코드"));
				iCardFuncPtExstVldCfmIn.setString("카드기능유형구분코드", "");
				
				LLog.debug.println( "■■■■■카드기능유형존재유효성조회■■■■■ " , iCardFuncPtExstVldCfmIn);
				rCardFuncPtExstVldCfmOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvCrdFuncPtExstVldCfm",iCardFuncPtExstVldCfmIn);
				LLog.debug.println( "■■■■■카드기능유형존재유효성출력■■■■■ " , rCardFuncPtExstVldCfmOut);

				
				for (int anx = 0; anx < rCardFuncPtExstVldCfmOut.getDataCount(); anx++) {
					LData tCardFuncPtExstVldCfmOut = rCardFuncPtExstVldCfmOut.getLData(anx);
					
					if ( DataConvertUtil.equals( tCardFuncPtExstVldCfmOut.getString( "카드기능유형구분코드" ), UBD_CONST.CRD_FUNC_PT_DTCD_029 ) ) {
						s029Yn = "Y"; 
					}
					else if ( DataConvertUtil.equals( tCardFuncPtExstVldCfmOut.getString( "카드기능유형구분코드" ), UBD_CONST.CRD_FUNC_PT_DTCD_314 ) ) {
						s314Yn = "Y"; 
					}
					else if ( DataConvertUtil.equals( tCardFuncPtExstVldCfmOut.getString( "카드기능유형구분코드" ), UBD_CONST.CRD_FUNC_PT_DTCD_330 ) ) {
						s330Yn = "Y"; 
					}
					else if ( DataConvertUtil.equals( tCardFuncPtExstVldCfmOut.getString( "카드기능유형구분코드" ), UBD_CONST.CRD_FUNC_PT_DTCD_580 ) ) {
						s580Yn = "Y"; 
					}
				}
				
				// =============================================================================
				// ######### ##마이데이터 API  카드연회비정보제공조회 - 연회비 
				// =============================================================================
				iCrdAnfInfOferInqIn.setString("상품코드"				, rCrdBasInfInqOut.getString("상품코드"));
				iCrdAnfInfOferInqIn.setString("기준년월일"				, DateUtil.getCurrentDate());
				iCrdAnfInfOferInqIn.setString("카드브랜드구분코드"		, rCrdBasInfInqOut.getString("카드브랜드구분코드"));
				iCrdAnfInfOferInqIn.setString("카드등급구분코드"		, rCrdBasInfInqOut.getString("카드등급구분코드"));
				
				if (UBD_CONST.STR_4.equals(rCrdBasInfInqOut.getString("모바일카드종류구분코드"))) {
					iCrdAnfInfOferInqIn.setString("모바일단독발급상품여부"		,UBD_CONST.STR_1);	
				}else {
					iCrdAnfInfOferInqIn.setString("모바일단독발급상품여부"		,UBD_CONST.STR_0);
				}
				
				//상품기본정보조회
				LLog.debug.println( "■■■■■카드연회비정보제공입력■■■■■ " ,iCrdAnfInfOferInqIn);
				rCrdAnfInfOferInqOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvCrdAnfInfOfer",iCrdAnfInfOferInqIn);
				LLog.debug.println( "■■■■■카드연회비정보제공출력■■■■■ " ,rCrdAnfInfOferInqOut);
				
				if (UBD_CONST.STR_2.equals(rCrdAnfInfOferInqOut.getString("제휴연회비적용구분코드"))) {
					
					// =============================================================================
					// ######### ##마이데이터 API  카드상품일반제휴연회비목록조회 
					// =============================================================================
					iCrdPdGnrAliAnfCtgInqIn.setString("상품코드", iCrdAnfInfOferInqIn.getString("상품코드"));
					iCrdPdGnrAliAnfCtgInqIn.setString("기준년월일", iCrdAnfInfOferInqIn.getString("기준년월일"));
					
					LLog.debug.println( "■■■■■카드상품일반제휴연회비목록조회■■■■■ " ,iCrdPdGnrAliAnfCtgInqIn); 
					rCrdPdGnrAliAnfCtgInqOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvLstCrdPdGnrAliAnf",iCrdPdGnrAliAnfCtgInqIn);
					LLog.debug.println( "■■■■■카드상품일반제휴연회비목록출력■■■■■ ", rCrdPdGnrAliAnfCtgInqOut );

					
					for (int anx = 0; anx < rCrdPdGnrAliAnfCtgInqOut.getDataCount(); anx++) {
						
						LData ta_CrdPdGnrAliAnfCtgInqOut = new LData();
						LData tCrdPdGnrAliAnfCtgInqOut = rCrdPdGnrAliAnfCtgInqOut.getLData(anx);
						
						ta_CrdPdGnrAliAnfCtgInqOut.setString("연회비구분코드"			, UBD_CONST.STR_2);
						ta_CrdPdGnrAliAnfCtgInqOut.setString("기본연회비"				, tCrdPdGnrAliAnfCtgInqOut.getString("제휴연회비"));
						ta_CrdPdGnrAliAnfCtgInqOut.setString("정보적용시작년월일"		, tCrdPdGnrAliAnfCtgInqOut.getString("정보적용시작년월일"));
						ta_CrdPdGnrAliAnfCtgInqOut.setString("정보적용종료년월일"		, tCrdPdGnrAliAnfCtgInqOut.getString("정보적용종료년월일"));						
						ta_CrdPdGnrAliAnfCtgInqOut.setString("제휴연회비적용구분코드"	, rCrdAnfInfOferInqOut.getString("제휴연회비적용구분코드"));
						
						iGnrAliAnfInf.addLData(ta_CrdPdGnrAliAnfCtgInqOut);
					}
				}
				
				if (UBD_CONST.STR_3.equals(rCrdAnfInfOferInqOut.getString("제휴연회비적용구분코드"))) {
					// =============================================================================
					// ######### ##마이데이터 API  카드상품가격설계목록조회 
					// =============================================================================
					
					iCrdPdPrePlagCtgInqIn.setString("상품코드", iCrdAnfInfOferInqIn.getString("상품코드"));
					iCrdPdPrePlagCtgInqIn.setString("기준년월일", iCrdAnfInfOferInqIn.getString("기준년월일"));
					
					LLog.debug.println( "■■■■■카드상품가격설계목록조회■■■■■ " ,iCrdPdPrePlagCtgInqIn);
					rCrdPdPrePlagCtgInqOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvLstCrdPdPrePlag",iCrdPdPrePlagCtgInqIn);
					LLog.debug.println( "■■■■■카드상품가격설계목록출력■■■■■ " , rCrdPdPrePlagCtgInqOut );

					
					for (int anx = 0; anx < rCrdPdPrePlagCtgInqOut.getDataCount(); anx++) {
						
						LData ta_CrdPdPrePlagCtgInqOut = new LData();
						LData tCrdPdPrePlagCtgInqOut = rCrdPdPrePlagCtgInqOut.getLData(anx);
						
						ta_CrdPdPrePlagCtgInqOut.setString("연회비구분코드"			, UBD_CONST.STR_2);
						ta_CrdPdPrePlagCtgInqOut.setString("개인기업구분코드"		, tCrdPdPrePlagCtgInqOut.getString("개인기업구분코드"));
						ta_CrdPdPrePlagCtgInqOut.setString("카드브랜드구분코드"		, tCrdPdPrePlagCtgInqOut.getString("카드브랜드구분코드"));
						ta_CrdPdPrePlagCtgInqOut.setString("카드브랜드상세구분코드"	, UBD_CONST.STR_0);
						ta_CrdPdPrePlagCtgInqOut.setString("카드등급구분코드"		, tCrdPdPrePlagCtgInqOut.getString("카드등급구분코드"));
						ta_CrdPdPrePlagCtgInqOut.setString("기본연회비"				, tCrdPdPrePlagCtgInqOut.getString("카드가격설계금액"));
						ta_CrdPdPrePlagCtgInqOut.setString("정보적용시작년월일"		, tCrdPdPrePlagCtgInqOut.getString("정보적용시작년월일"));
						ta_CrdPdPrePlagCtgInqOut.setString("정보적용종료년월일"		, tCrdPdPrePlagCtgInqOut.getString("정보적용종료년월일"));
						
						ta_CrdPdPrePlagCtgInqOut.setString("제휴연회비적용구분코드"	, rCrdAnfInfOferInqOut.getString("제휴연회비적용구분코드"));
						
						iGnrAliAnfInf.addLData(ta_CrdPdPrePlagCtgInqOut);
					}
				}
				
				if (UBD_CONST.STR_2.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드")) || UBD_CONST.STR_4.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드")) ||
					UBD_CONST.STR_5.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드"))) {
					
					// =============================================================================
					// ######### ##마이데이터 API  기본연회비기준BY카드브랜드구분코드조회 
					// =============================================================================
					iBasAnfBseCrdBrdDtcdIn.setString("상품코드", iCrdAnfInfOferInqIn.getString("상품코드"));
					iBasAnfBseCrdBrdDtcdIn.setString("조회기준년월일", iCrdAnfInfOferInqIn.getString("기준년월일"));
					
					LLog.debug.println( "■■■■■GDA기본연회비기준BY카드브랜드구분코드조회■■■■■ " ,iBasAnfBseCrdBrdDtcdIn);
					rBasAnfBseCrdBrdDtcdOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvBasAnfBseCrdBrdDtcd",iBasAnfBseCrdBrdDtcdIn);
					LLog.debug.println( "■■■■■GDA기본연회비기준BY카드브랜드구분코드출력■■■■■ " , rBasAnfBseCrdBrdDtcdOut );
				

					for (int anx = 0; anx < rBasAnfBseCrdBrdDtcdOut.getDataCount(); anx++) {
						
						LData ta_BasAnfBseCrdBrdDtcdOut = new LData();
						LData tBasAnfBseCrdBrdDtcdOut = rBasAnfBseCrdBrdDtcdOut.getLData(anx);
						
						ta_BasAnfBseCrdBrdDtcdOut.setString("연회비구분코드"			, UBD_CONST.STR_1);
						ta_BasAnfBseCrdBrdDtcdOut.setString("개인기업구분코드"			, tBasAnfBseCrdBrdDtcdOut.getString("개인기업구분코드"));
						ta_BasAnfBseCrdBrdDtcdOut.setString("카드브랜드구분코드"		, tBasAnfBseCrdBrdDtcdOut.getString("카드브랜드구분코드"));
						ta_BasAnfBseCrdBrdDtcdOut.setString("카드브랜드상세구분코드"	, tBasAnfBseCrdBrdDtcdOut.getString("카드브랜드상세구분코드"));
						ta_BasAnfBseCrdBrdDtcdOut.setString("카드등급구분코드"			, tBasAnfBseCrdBrdDtcdOut.getString("카드등급구분코드"));
						
						if ( DataConvertUtil.equals( iCrdAnfInfOferInqIn.getString("상품코드"), "CPP08621") || DataConvertUtil.equals( iCrdAnfInfOferInqIn.getString("상품코드"), "CPP08622")){
							ta_BasAnfBseCrdBrdDtcdOut.setInt("기본연회비"				, 10000);
						}else {
							ta_BasAnfBseCrdBrdDtcdOut.setString("기본연회비"			, tBasAnfBseCrdBrdDtcdOut.getString("기본연회비"));
						}
						
						ta_BasAnfBseCrdBrdDtcdOut.setString("브랜드시작년월일"			, tBasAnfBseCrdBrdDtcdOut.getString("브랜드시작년월일"));
						ta_BasAnfBseCrdBrdDtcdOut.setString("브랜드종료년월일"			, tBasAnfBseCrdBrdDtcdOut.getString("브랜드종료년월일"));
						ta_BasAnfBseCrdBrdDtcdOut.setString("모바일기본연회비"			, tBasAnfBseCrdBrdDtcdOut.getString("모바일기본연회비"));
						
						iGnrAliAnfInf.addLData(ta_BasAnfBseCrdBrdDtcdOut);
					}
				}
				
				
				if (UBD_CONST.STR_6.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드"))) {
					// =============================================================================
					// ######### ##마이데이터 API  GDA기본연회비기준조회BY조회기준년월일조회 
					// =============================================================================
					iBasAnfBseInqByInqBseYmdInqIn.setString("조회기준년월일", iCrdAnfInfOferInqIn.getString("기준년월일"));
					
					LLog.debug.println( "■■■■■GDA기본연회비기준조회BY조회기준년월일조회■■■■■ " ,iBasAnfBseInqByInqBseYmdInqIn);				
					rBasAnfBseInqByInqBseYmdInqOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvBasAnfBseInqBseYmd",iBasAnfBseInqByInqBseYmdInqIn);
					LLog.debug.println( "■■■■■GDA기본연회비기준조회BY조회기준년월일출력■■■■■ " , rBasAnfBseInqByInqBseYmdInqOut );
					
	
					// =============================================================================
					// ######### ##마이데이터 API  카드상품브랜드목록조회 NotFoundException 발생예외
					// =============================================================================
					iCrdPdBrdCtgIn.setString("기준년월일", iCrdAnfInfOferInqIn.getString("기준년월일"));
					iCrdPdBrdCtgIn.setString("상품코드"	 , iCrdAnfInfOferInqIn.getString("상품코드"));
					
					LLog.debug.println( "■■■■■카드상품브랜드목록조회■■■■■ " ,iCrdPdBrdCtgIn);
					rCrdPdBrdCtgOut = (LMultiData)BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdBasInfEbc", "retvLstCrdPdBrd",iCrdPdBrdCtgIn);
					LLog.debug.println( "■■■■■카드상품브랜드목록출력■■■■■ " ,rCrdPdBrdCtgOut );
					
					
					for (int anx = 0; anx < rCrdPdBrdCtgOut.getDataCount(); anx++) {
						
						LData ta_CrdPdBrdCtgOut = new LData();
						LData tCrdPdBrdCtgOut = rCrdPdBrdCtgOut.getLData(anx);
						
						ta_CrdPdBrdCtgOut.setString("연회비구분코드"			, UBD_CONST.STR_1);
						ta_CrdPdBrdCtgOut.setString("개인기업구분코드"			, tCrdPdBrdCtgOut.getString("개인기업구분코드"));
						ta_CrdPdBrdCtgOut.setString("카드브랜드구분코드"		, tCrdPdBrdCtgOut.getString("카드브랜드구분코드"));
						ta_CrdPdBrdCtgOut.setString("카드브랜드상세구분코드"	, tCrdPdBrdCtgOut.getString("카드브랜드상세구분코드"));
						ta_CrdPdBrdCtgOut.setString("카드등급구분코드"			, tCrdPdBrdCtgOut.getString("카드등급구분코드"));
						ta_CrdPdBrdCtgOut.setString("기본연회비"				, rBasAnfBseInqByInqBseYmdInqOut.getString("기본연회비"));
						ta_CrdPdBrdCtgOut.setString("모바일기본연회비"			, rBasAnfBseInqByInqBseYmdInqOut.getString("모바일기본연회비"));
						ta_CrdPdBrdCtgOut.setString("정보적용시작년월일"		, tCrdPdBrdCtgOut.getString("브랜드시작년월일"));
						ta_CrdPdBrdCtgOut.setString("정보적용종료년월일"		, tCrdPdBrdCtgOut.getString("브랜드종료년월일"));
	
						iGnrAliAnfInf.addLData(ta_CrdPdBrdCtgOut);
					}
				}				
				LLog.debug.println("연화비정보 확인" , iGnrAliAnfInf);
				for (int anx = 0; anx < iGnrAliAnfInf.getDataCount(); anx++) {

					LData ta_GnrAliAnfInf = iGnrAliAnfInf.getLData(anx);
					
					if (UBD_CONST.STR_2.equals(ta_GnrAliAnfInf.getString("연회비구분코드")) && UBD_CONST.STR_2.equals(rCrdAnfInfOferInqOut.getString("제휴연회비적용구분코드"))) {
						ta_GnrAliAnfInf.setString("카드브랜드구분코드",rCrdBasInfInqOut.getString("카드브랜드구분코드"));
						ta_GnrAliAnfInf.setString("카드등급구분코드",rCrdBasInfInqOut.getString("카드등급구분코드"));
					}
					
					if (DataConvertUtil.equals(ta_GnrAliAnfInf.getString("카드브랜드구분코드") , rCrdBasInfInqOut.getString("카드브랜드구분코드") )) {
					
						if (UBD_CONST.STR_1.equals(ta_GnrAliAnfInf.getString("연회비구분코드"))) {
							/**
							 * 기본연회비적용구분코드 = "2" (일반)
							 * 기본연회비적용구분코드 = "4" (2회차이후면제[카드별])
							 * 기본연회비적용구분코드 = "5" (2회차이후면제[회원별])
							 **/
							if (UBD_CONST.STR_2.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드")) || UBD_CONST.STR_4.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드")) ||
									UBD_CONST.STR_5.equals(rCrdAnfInfOferInqOut.getString("기본연회비적용구분코드"))) {
								
								if (DataConvertUtil.equals(iCrdAnfInfOferInqIn.getString("카드등급구분코드"), ta_GnrAliAnfInf.getString("카드등급구분코드"))) {
									if (DataConvertUtil.equals(iCrdAnfInfOferInqIn.getString("모바일단독발급상품여부") , UBD_CONST.STR_1)) {
										lBasAnf = TypeConvertUtil.parseTo_long(ta_GnrAliAnfInf.getString("모바일기본연회비"));
									}else {
										lBasAnf = TypeConvertUtil.parseTo_long(ta_GnrAliAnfInf.getString("기본연회비"));
									}
								}
							}else {
								
								LLog.debug.println("연회비적용구분코드[else]");
								if (DataConvertUtil.equals(iCrdAnfInfOferInqIn.getString("모바일단독발급상품여부") , UBD_CONST.STR_1)) {
									lBasAnf = TypeConvertUtil.parseTo_long(ta_GnrAliAnfInf.getString("모바일기본연회비"));
								}else {
									lBasAnf = TypeConvertUtil.parseTo_long(ta_GnrAliAnfInf.getString("기본연회비"));
								}
							}
							
						}else {
							if (UBD_CONST.STR_2.equals(ta_GnrAliAnfInf.getString("연회비구분코드"))) {
								lAliAnf = TypeConvertUtil.parseTo_long(ta_GnrAliAnfInf.getString("기본연회비"));
							}
						}
						
					}
				}
				
				/**■ 교통기능여부 ■ (교통카드) 대중교통의 육성 및 이용촉진에 관한 법률에따른 교통카드 기능 여부*/
				if (UBD_CONST.STR_1.equals(sRfCrdYn)) {
					if ("01".equals(rPrcPtRceBhCrdDtlImpaHisInfOut.getString("카드정보변경유형일련번호")) || "02".equals(rPrcPtRceBhCrdDtlImpaHisInfOut.getString("카드정보변경유형일련번호"))) {
						rMyDtApiTlgOut.setString("교통기능여부"		, "false");
					}else{
						rMyDtApiTlgOut.setString("교통기능여부"		, "true");
					}
				}else {
					rMyDtApiTlgOut.setString("교통기능여부"		, "false");
				}
								
				
				
				/**■ 현금카드기능여부 ■ (현금카드기능) ATM 등을 통해 현금을 입출금할 수 있는카드 기능 여부*/				 
				if (!StringUtil.trimNisEmpty(rCrdBasInfInqOut.getString("카드만료년월일"))) {
					rMyDtApiTlgOut.setString("현금카드기능여부"	,"false");
					
					if (!UBD_CONST.STR_3.equals(rCrdBasInfInqOut.getString("CD기기능구분코드"))) {
						if (rCrdBasInfInqOut.getString("현금카드발급번호").compareTo("00") > 0) {
							if (UBD_CONST.STR_1.equals(rCrdBasInfInqOut.getString("CD상태구분코드"))) {
								rMyDtApiTlgOut.setString("현금카드기능여부"	,"true");
							}
						}else {
							if (UBD_CONST.STR_1.equals(rCrdBasInfInqOut.getString("CD기기능구분코드")) || "2".equals(rCrdBasInfInqOut.getString("CD기기능구분코드"))) {
								rMyDtApiTlgOut.setString("현금카드기능여부"	,"true");	
							}
						}
					}
				}
				
				if ("Y".equals(s029Yn) && !"Y".equals(s314Yn) && !"Y".equals(s580Yn)) {
					rMyDtApiTlgOut.setString("현금카드기능여부"	,"false");
				}
				
				if ("Y".equals(s330Yn) && "050".equals(rCrdBasInfInqOut.getString("결제기관구분코드")) && "1".equals(rCrdBasInfInqOut.getString("CD기기능구분코드"))) {
					rMyDtApiTlgOut.setString("현금카드기능여부"	,"true");	
				}
				
				
				/**■ 결제은행 ■ (결제기관) 해당 카드대금이 결제되는 거래금융기관에 부여된 코드*/
				rMyDtApiTlgOut.setString("결제은행"		,rCrdBasInfInqOut.getString("결제기관구분코드")  ); //
				
				/**■ 카드브랜드구분코드 ■ (브랜드코드) 결제를 위한 카드사 브랜드 코드*/
				if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_1 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_D08 ); //로컬 
				}
				else if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_2 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_I02 ); //마스터 
				}
				else if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_3 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_I01 ); //비자 
				}
				else if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_4 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_I05 ); //JCB 
					if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드상세구분코드" ), UBD_CONST.STR_2 ) ) {
						rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_D06 ); //K-WORLD 
					}
				}
				else if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_5 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_I03 ); //UPI&글로벌 
					if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드상세구분코드" ), UBD_CONST.STR_2 ) ) {
						rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_D06 ); //K-WORLD 
					}
				}
				else if ( DataConvertUtil.equals( rCrdBasInfInqOut.getString( "카드브랜드구분코드" ), UBD_CONST.STR_6 ) ) {
					rMyDtApiTlgOut.setString("카드브랜드코드", UBD_CONST.CRD_BRD_DTCD_I04 ); //아멕스 
				}
				

				
				/**■ 상품연회비 ■ (연회비) 해당 카드상품을 사용하는데 지불하는 연간 비용*/
				rMyDtApiTlgOut.setString("상품연회비"			 ,  TypeConvertUtil.toString(lAliAnf + lBasAnf)); //
				
				/**■ 발급일자 ■ (발급일자) 카드 발급 시 해당 카드번호가 생성된 최근일자(최초 카드 발급 시 발급일자나 카드재발급 시 갱신되는 날짜를 의미)*/
				rMyDtApiTlgOut.setString("발급일자"				, rCrdBasInfInqOut.getString("등록년월일")); //
				
				setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
				
			}else {

				// =============================================================================
				// ######### ##마이데이터 API MCI처리계 인터페이스 호출 
				// =============================================================================
				iMciInput.setString("대체카드번호_V16", iMyDtApiTlgIn.getString("card_id"));
				rMciOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvCrdBasInf", iMciInput);
				
				if (!"N0000000".equals(rMciOutput.getString("오류메시지코드"))) {
					setRspCd(rMciOutput,UBD_CONST.CLLGL_SYS_DTCD_MCI);
					return rMyDtApiTlgOut;
				}
					
				if ("Y".equals(rMciOutput.getString("교통카드여부_V1"))) {
					rMyDtApiTlgOut.setString("교통기능여부" 		, "true"); 
				}else {
					rMyDtApiTlgOut.setString("교통기능여부" 		, "false");
				}
				
				if ("Y".equals(rMciOutput.getString("현금카드기능_V1"))) {
					rMyDtApiTlgOut.setString("현금카드기능여부" 		, "true"); 
				}else {
					rMyDtApiTlgOut.setString("현금카드기능여부" 		, "false");
				}
				
				rMyDtApiTlgOut.setString("결제은행"					, rMciOutput.getString("결제은행코드_V10"));
				rMyDtApiTlgOut.setString("카드브랜드코드"			, rMciOutput.getString("카드브랜드구분코드_V3"));	
				rMyDtApiTlgOut.setString("상품연회비"				, rMciOutput.getString("연회비_N15"));
				rMyDtApiTlgOut.setString("발급일자"					, rMciOutput.getString("카드발급일자_V8"));

				if("N0000000".equals(rMciOutput.getString("오류메시지코드"))) {
					setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
					
					if (LNullUtils.isNone(rMciOutput)) {
						setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403
								   , UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
					}
				}
			}
			
		}catch(LBizException lbe) {
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50002
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50002);
		}catch(LException le){
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50001, UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);			
		}finally {
			
			/**
			 * 마이데이터 요청내역관리/요청검증내역관리 비동기방식
			 * 1. 이용기관의 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청내역관리
			 * 2. KB포탈이 요청으로 데이터를 조회하는 경우
			 *  => 마이데이터 요청검증내역관리
			 */
			
			String sPrcMciInsGb 		= "N"; // 요청내역상세 - MCI insert 입력여부
			String sPrcEaiInsGb 		= "N"; // 요청내역상세 - EAI insert 입력여부
			String sdtLnkdOgtnGidNo  	= "";
			LLog.debug.println(" ========== 마이데이터 요청내역관리/요청검증내역관리 비동기방식 ========= ");
			LData linkResponseHeader 	= new LData();
			if(sCdcMciGb.equals(UBD_CONST.CLLGL_SYS_DTCD_CDC)) {
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
			
			lEncInf.setString("거래고유번호"			, sMydtTrUno								);
			lEncInf.setString("마이데이터이용기관코드"	, sMydtUtzInsCd								);
			lEncInf.setString("API구분코드"				, UBD_CONST.API_DTCD_CRD_BAS_INF_INQ			);
			lEncInf.setString("포탈분기구분코드"		, sPrtlDcCd									);
			lEncInf.setString("처리계시스템구분"		, sCdcMciGb									);
			lEncInf.setString("CI내용"					, ci_ctt									);
			lEncInf.setString("고객식별자"				, sCstIdf									);
			lEncInf.setString("고객관리번호"			, sCstMgNo									);
			lEncInf.setString("마이데이터정기전송여부"	, sRtvlTrsYN								);			
			lEncInf.setString("오픈API응답코드"			, rMyDtApiTlgOut.getString("세부응답코드")	);
			lEncInf.setString("오픈API응답메시지내용"	, rMyDtApiTlgOut.getString("세부응답메시지"));
			
			if (!StringUtil.trimNisEmpty(rErrCode)) {
				lEncInf.setString("오류메시지코드"		, rErrCode									); // MCI/EAI호출 코드
				lEncInf.setString("오류메시지출력내용"	, rErrMsg									); // MCI/EAI호출 메시지코드
			}else {
				lEncInf.setString("오류메시지코드"		, rMciOutput.getString("오류메시지코드")		); // MCI/EAI호출 코드
				lEncInf.setString("오류메시지출력내용"	, rMciOutput.getString("오류메시지")			); // MCI/EAI호출 메시지코드				
			}
			lEncInf.setString("MCI원거래GUID"			, sdtLnkdOgtnGidNo	);
			lEncInf.setString("EAI원거래GUID"			, ""										);
			lEncInf.setString("MCI인터페이스ID"			, "UBD_1_GAGS00002"							);
			lEncInf.setString("EAI인터페이스ID"			, ""										);
			lEncInf.setString("시스템최종갱신식별자"	, ContextHandler.getContextObject(ContextKey.TRAN_ID));
			lEncInf.setString("MCI요청상세입력여부"		, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"		, sPrcEaiInsGb					);
	        lEncInf.setString("마이데이터전송대상구분코드"  , sMydtTrsTgDtcd    );
	        lEncInf.setString("마이데이터클라이언트식별번호", sMydtClintIdiNo    );
			
			LData iEaiInput = new LData();
			LData rEaiInput = new LData();
			
			LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■Finally■■■■■■■■■■■■■■■■■■■■■" , lEncInf);			
			AsyncRunner.setLogParam(iMyDtApiTlgIn, rMyDtApiTlgOut, iMciInput, rMciOutput, iEaiInput, rEaiInput, lEncInf);
			AsyncRunner.start();	
			LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■Finally■■■■■■■■■■■■■■■■■■■■■");			
			
		}
		return rMyDtApiTlgOut;
	}

	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 */
	public void setRspReturn(String sErrCd , String sErrMsg) {
		rMyDtApiTlgOut.setString("세부응답코드"		 , sErrCd);
		rMyDtApiTlgOut.setString("세부응답메시지"	 , sErrMsg);
	}
	
	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 * @throws LException 
	 */
	public void setRspCd(LData input , String sDtcd) throws LException {
		
		LData iRspCdMap = new LData(); 		// 응답코드매핑조회(input)
		LData tRspCdMap = new LData(); 		// 응답코드매핑조회(output)
		
		// 응답코드매핑조회
		iRspCdMap.setString("오픈API언어구분코드"	, "KOR"		);
		iRspCdMap.setString("오픈API업무구분코드"	, "UBD"		); 
		iRspCdMap.setString("언어구분코드"			, "KOR"		);
		iRspCdMap.setString("메시지채널구분코드"	, "01"		);	// 01(단말)
		iRspCdMap.setString("오류메시지코드"		, input.getString("오류메시지코드"));		
		iRspCdMap.setString("오류메시지출력내용"	, input.getString("오류메시지")	);
		iRspCdMap.setString("처리계호출방식"		, sDtcd		); // 처리계호출방식(CDC, MCI, EAI)
		
		LLog.debug.println("tRspCdMap 확인해본다 " , iRspCdMap);
		tRspCdMap 	= (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.UbdMdulSptFntCpbc", "retvRspCdMapping", iRspCdMap);
		LLog.debug.println("tRspCdMap 확인해본다 " , tRspCdMap);
		setRspReturn(tRspCdMap.getString("오픈API응답코드")
				   , tRspCdMap.getString("오픈API응답메시지내용"));
		
	}			
}