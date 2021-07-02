package com.kbcard.ubd.pbi.capital;

import com.kbcard.ubd.cpbi.cmn.UBD_CONST;
import com.kbcard.ubd.cpbi.cmn.UbdCommon;
import com.kbcard.ubd.cpbi.cmn.UbdDmdRspPhsLdinCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.TypeConvertUtil;
import devonenterprise.util.StringUtil;

public class MyDtAccCtgInfPbc {

	/**
	 * @serviceID 
	 * @logicalName 
	 * @param 
	 * @return 
	 * @exception 
	 */
	
	/*마이데이터API 계좌목록조회 INPUT*/
	LData rMyDtAccCtgInfOut = new LData();
	/*마이데이터API 계좌목록조회 OUTPUT*/
	LData iMyDtAccCtgInfPbcIn = new LData();
	
	/*EAI호출 INPUT*/
	LData iEaiInput = new LData();
	/*EAI호출 OUTPUT*/
	LData rEaiOutput = new LData();
	/*MCI호출 INPUT*/
	LData iMciInput = new LData();
	/*MCI호출 OUTPUT*/
	LData rMciOutput = new LData();
	/*요청제공상세등록 테이블 기타 LData*/
	LData lEncInf = new LData();
	
	public LData retvLstMyDtApiAccForPaging(LData input) throws LException {
		iMyDtAccCtgInfPbcIn = (LData) input;
		
		LData iTrsRqstYnCdnoInqIn = new LData();
		LData rTrsRqstYnCdnoInqOut = new LData();

		LData iRspCdMap = new LData(); 		// 음답코드매핑조회(input)
		LData tRspCdMap = new LData(); 		// 음답코드매핑조회(output)
		
		LData cust_info = new LData();		//고객정보
		
		/*EAI에러코드*/
		String rEaiErrCode  = "";
		/*EAI에러메시지코드*/
		String rEaiErrMsg   = "";
		/*MCI에러코드*/
		String rMciErrCode  = "";
		/*MCI에러메시지코드*/
		String rMciErrMsg   = "";
		/*고객식별자*/
		String sCstIdf      = "";
		/*고객관리번호*/
		String sCstMgNo 	= "00000";
		
		/*마이데이터전송대상구분코드*/
		String sMydtTrsTgDtcd = "";
		/*마이데이터클라이언트식별번호*/
		String sMydtClintIdiNo ="";
		
		/*처리계시스템구분(CDC, MCI, EAI)*/
		String sDmdAccDtcd  = UBD_CONST.CLLGL_SYS_DTCD_MCI;
		/*NextKey*/
		String sNextKey 	= ""; 
 
		// =============================================================================
		// ######### ##마이데이터 API 헤더값 셋팅 
		// =============================================================================
		/** SELECT * FROM INSTC.TBUBDS301  UBD오픈API별N서비스상태관리기본 
		 *  오픈API서비스정상여부 ='Y' CDC
		 *  오픈API서비스정상여부 ='N' MCI 
		 * */
		LData apiInput = new LData();
		apiInput.setString("apiDtcd", UBD_CONST.API_DTCD_CAPITAL_LST_INQ);
		
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
		sMydtTrsTgDtcd          = tdHeader.getString("x-client-type"	); // 마이데이터전송대상구분코드
		
		ContextUtil.setHttpResponseHeaderParam("x-api-tran-id", sMydtTrUno);	// 마이데이터거래고유번호
		
		LLog.debug.println( "************* [ 헤더값 ] *****************");
		LLog.debug.println( "tdHeader : " , tdHeader);
		
		// =============================================================================
		// ######### ##마이데이터 API 유효성검증 (접근토큰,마이데이터 거래고유번호)
		// =============================================================================
		if (!UBD_CONST.PRTL_DTCD_PRTL.equals(sPrtlDcCd)) {
			if(StringUtil.trimNisEmpty(sAccsTken) || StringUtil.trimNisEmpty(sMydtTrUno)) {
				setRspReturn(UBD_CONST.REP_CD_BAD_REQUEST_40002
						   , UBD_CONST.REP_CD_MSG_BAD_REQUEST_40002 );
				return rMyDtAccCtgInfOut;			
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
				return rMyDtAccCtgInfOut;
			}
			
			sCstIdf   = cust_info.getString("고객식별자");
			ci_ctt 	  = cust_info.getString("CI내용");
			sMydtClintIdiNo  = cust_info.getString("클라이언트식별번호");
		}else {
			
			//포탈일경우에는 고객최초등록년월일값이 들어오지 않는다.
			cust_info.setString("고객최초등록년월일","00000000");
			
			if(StringUtil.trimNisEmpty(cst_idf) || StringUtil.trimNisEmpty(ci_ctt)) {
				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40403, UBD_CONST.REP_CD_MSG_NOTFOUND_40403);
				return rMyDtAccCtgInfOut;
			}
			sCstIdf   = cst_idf;
		}
		// =============================================================================
		// ######### ##마이데이터 API 페이지사이즈 : 최대500건 초과시 RETURN 
		// =============================================================================
		// 2.5 페이지사이즈 : 최대500 건 
		
		if (iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") <= 0) {
			setRspReturn(UBD_CONST.REP_CD_TOO_MANY_42901
					   , UBD_CONST.REP_CD_MSG_TOO_MANY_42901 );
			return rMyDtAccCtgInfOut;
		}
		
		if(iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") > 500) {
			setRspReturn(UBD_CONST.REP_CD_TOO_MANY_42901
					   , UBD_CONST.REP_CD_MSG_TOO_MANY_42901 );
			return rMyDtAccCtgInfOut;			
		}
		
		if (StringUtil.trimNisEmpty(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체"))) {
			iMciInput.setString("고객식별자"				, sCstIdf);
			iMciInput.setString("고객관리번호"				, sCstMgNo);
			iMciInput.setString("다음조회키_V1000"			, "SQ_카드론번호=0|NK_카드론번호=");
			iMciInput.setString("페이지사이즈_N5"			, iMyDtAccCtgInfPbcIn.getString("최대조회갯수"));
		} else {

			/*다음페이지기준개체 NULL일 경우 MCI거래 */
			if (StringUtil.length(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체")) > 2) {
				sNextKey = StringUtil.substring(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체"), StringUtil.length(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체")) - 3 ,StringUtil.length(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체")));
				/*다음페이지기준개체에 Legnth가 3이상이라면 [EAI] 문자열이 있는지 확인 */
				if (UBD_CONST.CLLGL_SYS_DTCD_EAI.equals(sNextKey)) {
					sDmdAccDtcd = UBD_CONST.CLLGL_SYS_DTCD_EAI;
					iEaiInput.setString("고객식별자"				, sCstIdf);
					iEaiInput.setString("고객관리번호"				, sCstMgNo);
					iEaiInput.setString("다음조회키_V1000"			, StringUtil.replaceAll(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체"), UBD_CONST.CLLGL_SYS_DTCD_EAI, ""));
					iEaiInput.setString("페이지사이즈_N5"			, iMyDtAccCtgInfPbcIn.getString("최대조회갯수"));
					iMciInput.clear();
				/*다음페이지기준개체에 Legnth가 3이상이라면 [EAI] 문자열이 아니라면 MCI 다음조회거래 */					
				}else {
					sDmdAccDtcd = UBD_CONST.CLLGL_SYS_DTCD_MCI;
					iMciInput.setString("고객식별자"				, sCstIdf);
					iMciInput.setString("고객관리번호"				, sCstMgNo);
					iMciInput.setString("다음조회키_V1000"			, StringUtil.mergeStr("SQ_카드론번호=0|NK_카드론번호=",StringUtil.replaceAll(iMyDtAccCtgInfPbcIn.getString("다음페이지기준개체"), UBD_CONST.CLLGL_SYS_DTCD_MCI, "")));
					iMciInput.setString("페이지사이즈_N5"			, iMyDtAccCtgInfPbcIn.getString("최대조회갯수"));

				}
			}
		}
		
		try {
			// =============================================================================
			// ######### ##마이데이터API MCI 처리계 인터페이스 호출 
			// =============================================================================
			LMultiData  rMultiEaiOutput 	= new LMultiData();
			LMultiData  rMultiMciOutput 	= new LMultiData();
			LMultiData 	tAccCtgInput 		= new LMultiData();
			
			if (UBD_CONST.CLLGL_SYS_DTCD_MCI.equals(sDmdAccDtcd)) {
				
				lEncInf.setString("MCI인터페이스ID"			, "UBD_1_GEAS00002"								);//할부금융계좌목록조회
				rMciOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciLstLnPdAccInf", iMciInput);
				
				if (!"N0000000".equals(rMciOutput.getString("오류메시지코드"))) {
					setRspCd(rMciOutput,UBD_CONST.CLLGL_SYS_DTCD_MCI);
					return rMyDtAccCtgInfOut;
				}else{
					setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
				}

				rMyDtAccCtgInfOut.setString("최초고객DB생성일"					, cust_info.getString("고객최초등록년월일"));
				
				rMultiMciOutput = rMciOutput.getLMultiData("GEA0156442_그리드");

				if (DataConvertUtil.equals(rMciOutput.getString("다음존재여부_V1"), "Y")) {
					rMyDtAccCtgInfOut.setString("다음페이지기준개체"			, StringUtil.replaceAll(rMciOutput.getString("다음조회키_V1000"),"SQ_카드론번호=0|NK_카드론번호=",""));
					LLog.debug.println("다음페이지확인" , rMyDtAccCtgInfOut.getString("다음페이지기준개체"));
				} else {
					/*최대조회건수와 MCI건수가 같다면 EAI데이터가 존재하는지 확인한 후에 
					  데이터 존재시 다음페이지기준개체에 [EAI] SET 없다면 "" */
					if (iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") == rMultiMciOutput.getDataCount()) {

						iEaiInput.setString("고객식별자"				, iMciInput.getString("고객식별자"));
						iEaiInput.setString("고객관리번호"				, iMciInput.getString("고객관리번호"));
						iEaiInput.setString("다음조회키_V1000"			, "");
						iEaiInput.setString("페이지사이즈_N5"			, iMyDtAccCtgInfPbcIn.getString("최대조회갯수"));
						lEncInf.setString("EAI인터페이스ID"				, "UBD_2_KIWS00001");//할부금융기준
						
						rEaiOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.EaiSptFntCpbc", "retvEaiLstLnPdAccInf", iEaiInput);
						
						/*건수가 없어도 RETURN 정상으로 수신*/
						if (!"N0000000".equals(rEaiOutput.getString("오류메시지코드"))) {
							setRspCd(rMciOutput,UBD_CONST.CLLGL_SYS_DTCD_EAI);
							return rMyDtAccCtgInfOut;
						}else{
							setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
						}
						
						rMultiEaiOutput = rEaiOutput.getLMultiData("출력데이터");
						if (rMultiEaiOutput.getDataCount() > 0) {
							rMyDtAccCtgInfOut.setString("다음페이지기준개체"			, UBD_CONST.CLLGL_SYS_DTCD_EAI);
						}else {
							rMyDtAccCtgInfOut.setString("다음페이지기준개체"			, "");
						}
					}
				}
				
				rMyDtAccCtgInfOut.setString("최초고객DB생성일"					, cust_info.getString("고객최초등록년월일"));
				rMyDtAccCtgInfOut.setInt("보유계좌목록_cnt"						, rMultiMciOutput.getDataCount());
				
				for (int anx = 0; anx < rMultiMciOutput.getDataCount(); anx++) {
					LData tAccCtg = new LData();
					LData tAccCtgInfOut = rMultiMciOutput.getLData(anx);
					tAccCtg.setString("계좌번호"					, tAccCtgInfOut.getString("대출번호")); //대출을 식별하기위해 부여한 코드
					
					
					iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tAccCtgInfOut.getString("대출번호"));
					iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "capital");
					iTrsRqstYnCdnoInqIn.setString("고객식별자"					, sCstIdf);
					rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);
					
					if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
						tAccCtg.setString("전송요구여부"			, "false");	
					}else {
						tAccCtg.setString("전송요구여부"			, "true");
					}				
					tAccCtg.setString("회차번호"					, tAccCtgInfOut.getString("대출회차")); 
					tAccCtg.setString("상품명"						, tAccCtgInfOut.getString("대출상품명")); //해당 계좌의 상품명
					tAccCtg.setString("계좌구분코드"				, tAccCtgInfOut.getString("계좌구분코드_V4")); //계좌번호 별 구분 코드
					tAccCtg.setString("계좌상태코드"				, tAccCtgInfOut.getString("계좌상태코드_V2")); //계좌번호 별 상태 코드 ? <코드표> 01 : 활동(사고 포함)
					tAccCtgInput.addLData(tAccCtg);
				}
				rMyDtAccCtgInfOut.set("보유계좌목록"					, tAccCtgInput);
				
				/**
				 * 최대조회갯수보다 MCI조회건수가 부족할경우 EAI 호출
				 * 요청제공상세 등록에 필요한 값 셋팅
				 * */
				if (iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") > rMultiMciOutput.getDataCount()) {
					iEaiInput.setString("고객식별자"				, iMciInput.getString("고객식별자"));
					iEaiInput.setString("고객관리번호"				, iMciInput.getString("고객관리번호"));
					iEaiInput.setString("다음조회키_V1000"			, iMciInput.getString("다음페이지기준개체"));
					iEaiInput.setString("페이지사이즈_N5"			, TypeConvertUtil.toString((iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") - rMultiMciOutput.getDataCount())));
				}
				
			}

			/** 1. 요청건수가 많을경우 EAI호출
		     *  2. 다음조회 요청이 EAI일 경우 
			 */
			if (iMyDtAccCtgInfPbcIn.getInt("최대조회갯수") > rMultiMciOutput.getDataCount() || UBD_CONST.CLLGL_SYS_DTCD_EAI.equals(sDmdAccDtcd)) {

				LLog.debug.println("바로여기서 시작한다규");
				// =============================================================================
				// ######### ##마이데이터API EAI 처리계 인터페이스 호출 
				// =============================================================================
				LLog.debug.println("마이데이터API EAI호출 " , iEaiInput);
				lEncInf.setString("EAI인터페이스ID"			, "UBD_2_KIWS00001"								);//할부금융기준
				rEaiOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.EaiSptFntCpbc", "retvEaiLstLnPdAccInf", iEaiInput);
				rMultiEaiOutput = rEaiOutput.getLMultiData("출력데이터");
				
				if (!"N0000000".equals(rEaiOutput.getString("오류메시지코드"))) {
					//EAI거래발생시 output초기화후 return
					rMyDtAccCtgInfOut.clear(); 
					setRspCd(rEaiOutput,UBD_CONST.CLLGL_SYS_DTCD_EAI);
					return rMyDtAccCtgInfOut;
				}else {
					if (!StringUtil.trimNisEmpty(rMciOutput.getString("연계원거래GUID"))) {
						if (rMciOutput.getString("연계원거래GUID").equals(rEaiOutput.getString("연계원거래GUID"))) {
							//EAI거래발생시 output초기화후 return
							rMyDtAccCtgInfOut.clear(); 
							setRspCd(rEaiOutput,UBD_CONST.CLLGL_SYS_DTCD_MCI);
							return rMyDtAccCtgInfOut;							
						}else {
							setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
						}
					}else {
						setRspReturn(UBD_CONST.REP_CD_SUCCESS, UBD_CONST.REP_CD_MSG_SUCCESS);
					}
				}
				rMyDtAccCtgInfOut.setString("최초고객DB생성일"					, cust_info.getString("고객최초등록년월일")); 
				
				if (DataConvertUtil.equals(rEaiOutput.getString("다음존재여부_V1"), "Y")) {
					rMyDtAccCtgInfOut.setString("다음페이지기준개체"			, StringUtil.mergeStr(rEaiOutput.getString("다음조회키_V1000"),"EAI"));
				} else {
					rMyDtAccCtgInfOut.setString("다음페이지기준개체"			, "");
				}
				
				rMyDtAccCtgInfOut.setInt("보유계좌목록_cnt"						, rMultiMciOutput.getDataCount() + rMultiEaiOutput.getDataCount());
				
				for (int anx = 0; anx < rMultiEaiOutput.getDataCount(); anx++) {
					LData tAccCtg = new LData();
					LData tAccCtgInfOut = rMultiEaiOutput.getLData(anx);
					tAccCtg.setString("계좌번호"								, tAccCtgInfOut.getString("대출번호_V14")); //대출을 식별하기위해 부여한 코드
					
					iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tAccCtgInfOut.getString("대출번호_V14"));
					iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "capital");
					iTrsRqstYnCdnoInqIn.setString("고객식별자"					, sCstIdf);
					rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);

					if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
						tAccCtg.setString("전송요구여부"			, "false");	
					}else {
						tAccCtg.setString("전송요구여부"			, "true");
					}				
					tAccCtg.setString("회차번호"					, tAccCtgInfOut.getString("대출회차_V3")); 
					tAccCtg.setString("상품명"						, tAccCtgInfOut.getString("대출상품명")); //해당 계좌의 상품명
					tAccCtg.setString("계좌구분코드"				, tAccCtgInfOut.getString("계좌구분코드_V4")); //계좌번호 별 구분 코드
					tAccCtg.setString("계좌상태코드"				, tAccCtgInfOut.getString("계좌상태구분코드_V2")); //계좌번호 별 상태 코드 ? <코드표> 01 : 활동(사고 포함)
					tAccCtgInput.addLData(tAccCtg);
				}
				rMyDtAccCtgInfOut.set("보유계좌목록"					, tAccCtgInput);
			}
			
			if(rMyDtAccCtgInfOut.getInt("보유계좌목록_cnt") == 0) {
				setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40402
						   , UBD_CONST.REP_CD_MSG_NOTFOUND_40402);
			}
			
			return rMyDtAccCtgInfOut;
			
		}catch(LBizException lbe) {
			rMyDtAccCtgInfOut.clear(); 
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50002
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50002);
		}catch(LException le){
			rMyDtAccCtgInfOut.clear(); 
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50001
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50001);			
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
			
			if (!StringUtil.trimNisEmpty(rMciOutput.getString("연계원거래GUID"))) {
				sPrcMciInsGb = "Y"; //NULL이 아닐경우 MCI호출함
			}else {
				if (!StringUtil.trimNisEmpty(rMciErrCode)) {
					sPrcMciInsGb = "Y"; 
				}
			}
			
			if (!StringUtil.trimNisEmpty(rEaiOutput.getString("연계원거래GUID"))) {
				sPrcEaiInsGb = "Y"; //NULL이 아닐경우 EAI호출함
			}else {
				if (!StringUtil.trimNisEmpty(rEaiErrCode)) {
					sPrcMciInsGb = "Y"; 
				}
			}
			
			LLog.debug.println(" ========== 마이데이터 요청내역관리/요청검증내역관리 비동기방식 ========= ");
			UbdDmdRspPhsLdinCpbc AsyncRunner = new UbdDmdRspPhsLdinCpbc();
			
			lEncInf.setString("거래고유번호"				, sMydtTrUno								);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd								);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_CAPITAL_LST_INQ		);
			lEncInf.setString("포탈분기구분코드"			, sPrtlDcCd									);
			lEncInf.setString("처리계시스템구분"			, sDmdAccDtcd								);
			lEncInf.setString("CI내용"						, ci_ctt									);
			lEncInf.setString("고객식별자"					, sCstIdf									);
			lEncInf.setString("고객관리번호"				, sCstMgNo									);
			lEncInf.setString("마이데이터정기전송여부"		, sRtvlTrsYN								);			
			lEncInf.setString("오픈API응답코드"				, rMyDtAccCtgInfOut.getString("세부응답코드")	);
			lEncInf.setString("오픈API응답메시지내용"		, rMyDtAccCtgInfOut.getString("세부응답메시지"));
			
			/*EAI에러코드*/
			if (!StringUtil.trimNisEmpty(rEaiErrCode)) {
				lEncInf.setString("EAI오류메시지코드"		, rEaiErrCode									); 
				lEncInf.setString("EAI오류메시지출력내용"	, rEaiErrMsg									); 
			}else {
				lEncInf.setString("EAI오류메시지코드"		, rEaiOutput.getString("오류메시지코드")		); 
				lEncInf.setString("EAI오류메시지출력내용"	, rEaiOutput.getString("오류메시지")			); 				  
			}
			
			/*MCI에러코드*/
			if (!StringUtil.trimNisEmpty(rMciErrCode)) {
				lEncInf.setString("MCI오류메시지코드"		, rMciErrCode									); 
				lEncInf.setString("MCI오류메시지출력내용"	, rMciErrMsg									); 
			}else {
				lEncInf.setString("MCI오류메시지코드"		, rMciOutput.getString("오류메시지코드")		); 
				lEncInf.setString("MCI오류메시지출력내용"	, rMciOutput.getString("오류메시지")			); 		
			}
			
			lEncInf.setString("MCI원거래GUID"			, rMciOutput.getString("연계원거래GUID")		);
			lEncInf.setString("EAI원거래GUID"			, rEaiOutput.getString("연계원거래GUID")		);
			lEncInf.setString("MCI요청상세입력여부"		, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"		, sPrcEaiInsGb					);
			lEncInf.setString("시스템최종갱신식별자"	, ContextHandler.getContextObject(ContextKey.TRAN_ID));
	        lEncInf.setString("마이데이터전송대상구분코드"  , sMydtTrsTgDtcd    );
	        lEncInf.setString("마이데이터클라이언트식별번호", sMydtClintIdiNo    );
						
			
			
			LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■Finally■■■■■■■■■■■■■■■■■■■■■" ,lEncInf);		
			AsyncRunner.setLogParam(iMyDtAccCtgInfPbcIn, rMyDtAccCtgInfOut, iMciInput, rMciOutput, iEaiInput, rEaiOutput, lEncInf);
			AsyncRunner.start();	
			LLog.debug.println("■■■■■■■■■■■■■■■■■■■■■Finally■■■■■■■■■■■■■■■■■■■■■");
			
		}
		return rMyDtAccCtgInfOut;
	}
	
	/**
	 * @serviceID 포탈요청 계좌목록조회
	 * @logicalName 
	 * @param  LData input
	 * @return LData rMyDtAccCtgInfOut
	 * @exception  LException
	 */
	public LData retvLstMyDtApiAccListVln(LData input) throws LException {
		
		LData iTrsRqstYnCdnoInqIn = new LData();
		LData rTrsRqstYnCdnoInqOut = new LData();
		
		LMultiData  rMultiEaiOutput 	= new LMultiData();
		LMultiData  rMultiMciOutput 	= new LMultiData();
		LMultiData 	tAccCtgInput 		= new LMultiData();
		
		iMciInput.setString("고객식별자"				, input.getString("고객식별자"));
		iMciInput.setString("고객관리번호"				, "00000");
		iMciInput.setString("다음조회키_V1000"			, "SQ_카드론번호=0|NK_카드론번호=");
		iMciInput.setString("페이지사이즈_N5"			, "500");
		rMciOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvMciLstLnPdAccInf", iMciInput);
		
		rMultiMciOutput = rMciOutput.getLMultiData("GEA0156442_그리드");
		
		for (int anx = 0; anx < rMultiMciOutput.getDataCount(); anx++) {
			LData tAccCtg = new LData();
			LData tAccCtgInfOut = rMultiMciOutput.getLData(anx);
			tAccCtg.setString("계좌번호"					, tAccCtgInfOut.getString("대출번호")); //대출을 식별하기위해 부여한 코드
			
			
			iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tAccCtgInfOut.getString("대출번호"));
			iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "capital");
			iTrsRqstYnCdnoInqIn.setString("고객식별자"					, input.getString("고객식별자"));
			rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);
			
			if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
				tAccCtg.setString("전송요구여부"			, "N");	
			}else {
				tAccCtg.setString("전송요구여부"			, "Y");
			}				
			tAccCtg.setString("회차번호"					, tAccCtgInfOut.getString("대출회차")); 
			tAccCtg.setString("상품명"						, tAccCtgInfOut.getString("대출상품명")); //해당 계좌의 상품명
			tAccCtg.setString("계좌구분코드"				, tAccCtgInfOut.getString("계좌구분코드_V4")); //계좌번호 별 구분 코드
			tAccCtg.setString("계좌상태코드"				, tAccCtgInfOut.getString("계좌상태코드_V2")); //계좌번호 별 상태 코드 ? <코드표> 01 : 활동(사고 포함)
			tAccCtgInput.addLData(tAccCtg);
		}
		rMyDtAccCtgInfOut.set("보유계좌목록"					, tAccCtgInput);
		
		iEaiInput.setString("고객식별자"				, iMciInput.getString("고객식별자"));
		iEaiInput.setString("고객관리번호"				, iMciInput.getString("고객관리번호"));
		iEaiInput.setString("다음조회키_V1000"			, "");
		iEaiInput.setString("페이지사이즈_N5"			, "500");
		rEaiOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.EaiSptFntCpbc", "retvEaiLstLnPdAccInf", iEaiInput);
		
		
		rMultiEaiOutput = rEaiOutput.getLMultiData("출력데이터");
		rMyDtAccCtgInfOut.setInt("보유계좌목록_cnt"						, rMultiMciOutput.getDataCount() + rMultiEaiOutput.getDataCount());
		
		for (int anx = 0; anx < rMultiEaiOutput.getDataCount(); anx++) {
			LData tAccCtg = new LData();
			LData tAccCtgInfOut = rMultiEaiOutput.getLData(anx);
			tAccCtg.setString("계좌번호"								, tAccCtgInfOut.getString("대출번호_V14")); //대출을 식별하기위해 부여한 코드
			
			iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tAccCtgInfOut.getString("대출번호_V14"));
			iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "capital");
			iTrsRqstYnCdnoInqIn.setString("고객식별자"					, input.getString("고객식별자"));
			rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);

			if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
				tAccCtg.setString("전송요구여부"			, "false");	
			}else {
				tAccCtg.setString("전송요구여부"			, "true");
			}				
			tAccCtg.setString("회차번호"					, tAccCtgInfOut.getString("대출회차_V3")); 
			tAccCtg.setString("상품명"						, tAccCtgInfOut.getString("대출상품명")); //해당 계좌의 상품명
			tAccCtg.setString("계좌구분코드"				, tAccCtgInfOut.getString("계좌구분코드_V4")); //계좌번호 별 구분 코드
			tAccCtg.setString("계좌상태코드"				, tAccCtgInfOut.getString("계좌상태구분코드_V2")); //계좌번호 별 상태 코드 ? <코드표> 01 : 활동(사고 포함)
			tAccCtgInput.addLData(tAccCtg);
		}
		rMyDtAccCtgInfOut.set("보유계좌목록"					, tAccCtgInput);
		
		return rMyDtAccCtgInfOut;
	}
	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 */
	public void setRspReturn(String sErrCd , String sErrMsg) {
		rMyDtAccCtgInfOut.setString("세부응답코드"	 	 , sErrCd);
		rMyDtAccCtgInfOut.setString("세부응답메시지"	 , sErrMsg);
	}
	
	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 * @throws LException 
	 */
	public void setRspCd(LData input , String sDtcd) throws LException { 
		
		LData iRspCdMap = new LData(); 		// 음답코드매핑조회(input)
		LData tRspCdMap = new LData(); 		// 음답코드매핑조회(output)
		
		// 응답코드매핑조회
		iRspCdMap.setString("오픈API언어구분코드"	, "KOR"		);
		iRspCdMap.setString("오픈API업무구분코드"	, "UBD"		); 
		iRspCdMap.setString("언어구분코드"			, "KOR"		);
		iRspCdMap.setString("메시지채널구분코드"	, "01"		);	// 01(단말)
		iRspCdMap.setString("오류메시지코드"		, input.getString("오류메시지코드"));		
		iRspCdMap.setString("오류메시지출력내용"	, input.getString("오류메시지")	);
		iRspCdMap.setString("처리계호출방식"		, sDtcd		); // 처리계호출방식(CDC, MCI, EAI)
		
		tRspCdMap 	= (LData) BizCommand.execute("com.kbcard.ubd.cpbi.cmn.UbdMdulSptFntCpbc", "retvRspCdMapping", iRspCdMap);  
		setRspReturn(tRspCdMap.getString("오픈API응답코드")
				   , tRspCdMap.getString("오픈API응답메시지내용"));
		
	}	
}


