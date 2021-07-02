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
import devonenterprise.ext.persistent.page.PageConstants;
import devonenterprise.ext.service.cache.service.ErrorCodeMessageService;
import devonenterprise.ext.util.ContextUtil;
import devonenterprise.ext.util.DataConvertUtil;
import devonenterprise.ext.util.DataCryptUtil;
import devonenterprise.ext.util.LDataUtil;
import devonenterprise.util.DateUtil;
import devonenterprise.util.StringUtil;

public class MyDtCrdCtgPbc {
	
	/**
	 * @serviceID 마이데이터API 카드목록조회
	 * @logicalName 
	 * @param  LData iMyDtApiTlgIn
	 * @return LData rMyDtApiTlgOut
	 * @exception  LException
	 */
	
	/*마이데이터API 카드목록조회 INPUT*/
	LData iMyDtApiTlgIn = new LData();
	/*마이데이터API 카드목록조회 OUTPUT*/
	LData rMyDtApiTlgOut = new LData();
	/*MCI호출 INPUT*/
	LData iMciInput = new LData();
	/*MCI호출 OUTPUT*/
	LData rMciOutput = new LData();
	/*요청제공상세등록 테이블 기타 LData*/ 
	LData lEncInf = new LData();
	
	public LData retvLstMyDtApiCrdForPaging(LData input) throws LException {
		
		iMyDtApiTlgIn = (LData) input;

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
		
		/*CDC카드목록조회 INPUT*/
		LData iCrdCtgInqIn = new LData();
		/*CDC카드목록조회 OUTPUT*/
		LMultiData rCrdCtgInqOut = new LMultiData();
		/*처리계시스템구분(CDC, MCI, EAI)*/
		String sCdcMciGb	= ""; 			
		/*마이데이터전송대상구분코드*/
		String sMydtTrsTgDtcd = "";
		/*마이데이터클라이언트식별번호*/
		String sMydtClintIdiNo ="";
		
		
		/** SELECT * FROM INSTC.TBUBDS301  UBD오픈API별N서비스상태관리기본 
		 *  오픈API서비스정상여부 ='Y' CDC
		 *  오픈API서비스정상여부 ='N' MCI 
		 * */
		LData apiInput = new LData();
		apiInput.setString("apiDtcd", UBD_CONST.API_DTCD_CRD_LST_INQ);
		
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
		sCdcMciGb 				= tdHeader.getString("tran_dv_cd"	); // CDC와 MCI거래를 구분하기위한 헤더값 확인
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
		// ######### ##마이데이터 API 페이지사이즈 : 최대500건 초과시 RETURN 
		// =============================================================================
		// 2.5 페이지사이즈 : 최대500 건 
		int iPgeSize 	= iMyDtApiTlgIn.getInt("최대조회갯수");
		
		if (iPgeSize == 0) {
			setRspReturn(UBD_CONST.REP_CD_TOO_MANY_42901
					   , UBD_CONST.REP_CD_MSG_TOO_MANY_42901 );
			return rMyDtApiTlgOut;
		}
		if(iPgeSize > 500) {
			setRspReturn(UBD_CONST.REP_CD_TOO_MANY_42901
					   , UBD_CONST.REP_CD_MSG_TOO_MANY_42901 );
			return rMyDtApiTlgOut;			
		}
		
		iCrdCtgInqIn.setString(PageConstants.PGE_SIZE	, iMyDtApiTlgIn.getString("최대조회갯수"));
		
		if (StringUtil.trimNisEmpty(iMyDtApiTlgIn.getString("다음페이지기준개체"))) {
			iCrdCtgInqIn.setString(PageConstants.NEXT_INQ_KY, "SQ_카드대체번호=0|NK_카드대체번호=");
		} else {
			iCrdCtgInqIn.setString(PageConstants.NEXT_INQ_KY,StringUtil.mergeStr("SQ_카드대체번호=0|NK_카드대체번호=", iMyDtApiTlgIn.getString("다음페이지기준개체")));
		}
		
		try {
			// 호출시스템분기(CDC, MCI) 분기 로직
			if(sCdcMciGb.equals(UBD_CONST.CLLGL_SYS_DTCD_CDC)) {
				// =============================================================================
				// ######### ##마이데이터 API 고객식별자 => 카드목록조회 
				// =============================================================================
				iCrdCtgInqIn.setString("기준년월일"				, DateUtil.getCurrentDate());
				iCrdCtgInqIn.setString("고객식별자"				, sCstIdf);
				rCrdCtgInqOut = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvLstMyDtApiCrdForPaging", iCrdCtgInqIn);
				
				setRspReturn(UBD_CONST.REP_CD_SUCCESS
						   , UBD_CONST.REP_CD_MSG_SUCCESS);
				
				if (rCrdCtgInqOut.getDataCount() == 0) { 
					setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40402
							   , UBD_CONST.REP_CD_MSG_NOTFOUND_40402);	
				}
				
				if (DataConvertUtil.equals(LDataUtil.getNextYn(), "Y")) {
					rMyDtApiTlgOut.setString("다음페이지기준개체"			, StringUtil.trim(StringUtil.replaceAll(LDataUtil.getNextKey(), "SQ_카드대체번호=0|NK_카드대체번호=", ""))); //MDD 34,16 설정
				} else {
					rMyDtApiTlgOut.setString("다음페이지기준개체"			, "");
				}

				LMultiData 	tCardList 		= new LMultiData();
				
				for (int anx = 0;  anx < rCrdCtgInqOut.getDataCount(); anx++) {			
					LData tCrdCtg = new LData();
					LData iCrdDcInqIn = new LData();
					LData rCrdDcInqOut = new LData();
					
					LData tCrdCtgInqOut = rCrdCtgInqOut.getLData(anx); 
					tCrdCtg.setString("카드식별자"					, tCrdCtgInqOut.getString("카드대체번호")); 
					
					if (!StringUtil.trimNisEmpty(tCrdCtgInqOut.getString("카드번호"))) {
						String sTemp ="";
						if (StringUtil.length(tCrdCtgInqOut.getString("카드번호")) > 19) {
							sTemp =   DataCryptUtil.decryptCardNo(tCrdCtgInqOut.getString("카드번호"));	
						}else {
							sTemp =   tCrdCtgInqOut.getString("카드번호");
						}
						String sMaskCardNo = StringUtil.mergeStr(StringUtil.substring(sTemp, 0, 6), "******" ,StringUtil.substring(sTemp, 12)    );
						tCrdCtg.setString("카드번호"			, sMaskCardNo);
					}else {
						tCrdCtg.setString("카드번호"			, " ");
					}
					// =============================================================================
					// ######### ##마이데이터 API 카드 전송요구여부 확인  
					// =============================================================================
					iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tCrdCtgInqOut.getString("카드대체번호"));
					iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "card");
					iTrsRqstYnCdnoInqIn.setString("고객식별자"					, sCstIdf);
					rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);

					if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
						tCrdCtg.setString("전송요구여부"			, "false");	
					}else {
						tCrdCtg.setString("전송요구여부"			, "true");
					}
					
					if (StringUtil.byteLength(tCrdCtgInqOut.getString("상품명")) > 300) {
						StringUtil.substring(tCrdCtgInqOut.getString("상품명"), 300);
					}else {
						tCrdCtg.setString("카드상품명"				, tCrdCtgInqOut.getString("상품명"));
					}
					tCrdCtg.setString("소지자구분"				, tCrdCtgInqOut.getString("카드소유자구분코드"));
					
					/* 상품중분코드 CP04(체크/체크신용) 구분을 위한 회원중요변경이력 조회 */
					if (DataConvertUtil.equals(tCrdCtgInqOut.getString("상품중분류구분코드"), "CP04")) {
						iCrdDcInqIn.setString("회원일련번호", tCrdCtgInqOut.getString("회원일련번호"));
						rCrdDcInqOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc", "retvMbrBasImpaMdHisLstHis",iCrdDcInqIn);
						
						if ((!StringUtil.trimNisEmpty(rCrdDcInqOut.getString("회원일련번호")) && StringUtil.trimNisEmpty(rCrdDcInqOut.getString("해제년월일")))) {
							tCrdCtg.setString("카드구분", UBD_CONST.STR_03); //소액체크신용
						} else {
							tCrdCtg.setString("카드구분", UBD_CONST.STR_02); //체크
						}
					} else {
						tCrdCtg.setString("카드구분"	 , UBD_CONST.STR_01); //신용
					} 
					tCardList.addLData(tCrdCtg);		
				}
				
				rMyDtApiTlgOut.setInt("보유카드목록_cnt", rCrdCtgInqOut.getDataCount() );
				rMyDtApiTlgOut.set("보유카드목록", tCardList);
				
			}else {
				// =============================================================================
				// ######### ##마이데이터API MCI처리계 인터페이스 호출 UBD_1_GAGS00001
				// =============================================================================
				LMultiData  rMultiMciOutPut = new LMultiData();
				LMultiData 	tCrdCtgInput 		= new LMultiData();
				LData   	iRspCdMap			= new LData(); 		// 음답코드매핑조회(input)
				LData   	tRspCdMap			= new LData(); 		// 음답코드매핑조회(output)
				
				iMciInput.setString("CI번호_V88" 		 , ci_ctt);
				iMciInput.setString("다음조회키1_V1000"  , iMyDtApiTlgIn.getString("다음페이지기준개체"));
				iMciInput.setString("요청건수_N3" 	     , iMyDtApiTlgIn.getString("최대조회갯수"));
				rMciOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvCrdCtgInq", iMciInput);
				
				if (!"N0000000".equals(rMciOutput.getString("오류메시지코드"))) {
					setRspCd(rMciOutput,UBD_CONST.CLLGL_SYS_DTCD_MCI);
					return rMyDtApiTlgOut;
				}
				
				rMultiMciOutPut = rMciOutput.getLMultiData("그리드");
				
				for (int anx = 0; anx < rMultiMciOutPut.getDataCount(); anx++) {
					LData tCrdCtg = new LData();
					LData tCrdCtgInqOut = rMultiMciOutPut.getLData(anx);
					
					tCrdCtg.setString("카드식별자"					, tCrdCtgInqOut.getString("대체카드번호_V16"));
					
					if (!StringUtil.trimNisEmpty(tCrdCtgInqOut.getString("카드번호_V19"))) {
						String sTemp =   DataCryptUtil.decryptCardNo(tCrdCtgInqOut.getString("카드번호_V19"));
						String sMaskCardNo = StringUtil.mergeStr(StringUtil.substring(sTemp, 0, 6), "******" ,StringUtil.substring(sTemp, 12)    );
						tCrdCtg.setString("카드번호"			, sMaskCardNo);
					}else {
						tCrdCtg.setString("카드번호"			, " ");
					}
					
					// =============================================================================
					// ######### ##마이데이터 API 카드 전송요구여부 확인  
					// =============================================================================
					iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tCrdCtgInqOut.getString("대체카드번호_V16"));
					iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "card");
					iTrsRqstYnCdnoInqIn.setString("고객식별자"					, sCstIdf);
					rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);
					
					if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
						tCrdCtg.setString("전송요구여부"			, "false");	
					}else {
						tCrdCtg.setString("전송요구여부"			, "true");
					}
					
					tCrdCtg.setString("카드상품명" 				, tCrdCtgInqOut.getString("상품명_V300"));
					tCrdCtg.setString("소지자구분"				, tCrdCtgInqOut.getString("카드소지자구분코드_V1"));
					tCrdCtg.setString("카드구분"				, tCrdCtgInqOut.getString("카드구분_V2"));
					tCrdCtgInput.addLData(tCrdCtg);
				}
				
				
				if (DataConvertUtil.equals(rMciOutput.getString("다음페이지존재여부_V1"), "Y")) {
					rMyDtApiTlgOut.setString("다음페이지기준개체"		, rMciOutput.getString("다음조회키1_V1000"));
				} else {
					rMyDtApiTlgOut.setString("다음페이지기준개체"		, "");
				}
				rMyDtApiTlgOut.setInt("보유카드목록_cnt"				, rMultiMciOutPut.getDataCount());
				rMyDtApiTlgOut.set("보유카드목록"					, tCrdCtgInput);
				
				if ("N0000000".equals(rMciOutput.getString("오류메시지코드"))) {
					setRspReturn(UBD_CONST.REP_CD_SUCCESS
							   , UBD_CONST.REP_CD_MSG_SUCCESS); 
					
					if (rMyDtApiTlgOut.getInt("보유카드목록_cnt") == 0) {
						setRspReturn(UBD_CONST.REP_CD_NOTFOUND_40402
								   , UBD_CONST.REP_CD_MSG_NOTFOUND_40402);
					}
				}
			}
			return rMyDtApiTlgOut;
			
		}catch(LBizException lbe) {
			setRspReturn(UBD_CONST.REP_CD_SERVER_ERR_50002
					   , UBD_CONST.REP_CD_MSG_SERVER_ERR_50002);
		}catch(LException le){
			le.printStackTrace();
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
			
			// =============================================================================
			// ######### ##트랜젝션 분리
			//             요청,제공,상세 테이블 INSERT
			// =============================================================================
			
			UbdDmdRspPhsLdinCpbc AsyncRunner = new UbdDmdRspPhsLdinCpbc();
			 
			lEncInf.setString("거래고유번호"				, sMydtTrUno								);
			lEncInf.setString("마이데이터이용기관코드"		, sMydtUtzInsCd								);
			lEncInf.setString("API구분코드"					, UBD_CONST.API_DTCD_CRD_LST_INQ			);
			lEncInf.setString("포탈분기구분코드"			, sPrtlDcCd									);
			lEncInf.setString("처리계시스템구분"			, sCdcMciGb									);
			lEncInf.setString("CI내용"						, ci_ctt									);
			lEncInf.setString("고객식별자"					, sCstIdf									);
			lEncInf.setString("고객관리번호"				, sCstMgNo									);
			lEncInf.setString("마이데이터정기전송여부"		, sRtvlTrsYN								);			
			lEncInf.setString("오픈API응답코드"				, rMyDtApiTlgOut.getString("세부응답코드")	);
			lEncInf.setString("오픈API응답메시지내용"		, rMyDtApiTlgOut.getString("세부응답메시지"));
			
			if (!StringUtil.trimNisEmpty(rErrCode)) {
				lEncInf.setString("오류메시지코드"			, rErrCode									); // MCI/EAI호출 코드
				lEncInf.setString("오류메시지출력내용"		, rErrMsg									); // MCI/EAI호출 메시지코드
			}else {
				lEncInf.setString("오류메시지코드"			, rMciOutput.getString("오류메시지코드")		); // MCI/EAI호출 코드
				lEncInf.setString("오류메시지출력내용"		, rMciOutput.getString("오류메시지")			); // MCI/EAI호출 메시지코드				
			}
			lEncInf.setString("MCI원거래GUID"				, sdtLnkdOgtnGidNo	);
			lEncInf.setString("EAI원거래GUID"				, ""										);
			lEncInf.setString("MCI인터페이스ID"				, "UBD_1_GAGS00001"							);
			lEncInf.setString("EAI인터페이스ID"				, ""										);
			lEncInf.setString("시스템최종갱신식별자"		, ContextHandler.getContextObject(ContextKey.TRAN_ID));
			lEncInf.setString("MCI요청상세입력여부"			, sPrcMciInsGb					);
			lEncInf.setString("EAI요청상세입력여부"			, sPrcEaiInsGb					);
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
	 * @serviceID 포탈요청 카드목록조회
	 * @logicalName 
	 * @param  LData input
	 * @return LData rMyDtApiTlgOut
	 * @exception  LException
	 */
	public LData retvCardListVln(LData input) throws LException {
		
		LMultiData rCrdCtgInqOut = new LMultiData();
		LData iCrdCtgInqIn = new LData();
		LData iCrdDcInqIn = new LData();
		LData rCrdDcInqOut = new LData();
		LData iTrsRqstYnCdnoInqIn = new LData();
		LData rTrsRqstYnCdnoInqOut = new LData();
		LMultiData  rMultiMciOutPut = new LMultiData();
		LMultiData 	tCrdCtgInput 		= new LMultiData();
		
		
		if (UBD_CONST.CLLGL_SYS_DTCD_CDC.equals(input.getString("호출시스템분기"))) {
			
			iCrdCtgInqIn.setString("기준년월일"				, DateUtil.getCurrentDate());
			iCrdCtgInqIn.setString("고객식별자"				, input.getString("고객식별자"));
			iCrdCtgInqIn.setString("최대조회갯수"			, "500");
			iCrdCtgInqIn.setString(PageConstants.NEXT_INQ_KY, "SQ_카드식별자=0|NK_카드식별자=");
			
			rCrdCtgInqOut = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvLstMyDtApiCrdForPaging", iCrdCtgInqIn);
			
			LMultiData 	tCardList 		= new LMultiData();
			
			for (int anx = 0;  anx < rCrdCtgInqOut.getDataCount(); anx++) {			
				LData tCrdCtg = new LData();
				LData tCrdCtgInqOut = rCrdCtgInqOut.getLData(anx); 
				tCrdCtg.setString("카드식별자"					, tCrdCtgInqOut.getString("카드대체번호")); 
				/*
				if (!StringUtil.trimNisEmpty(tCrdCtgInqOut.getString("카드번호"))) {
					String sTemp ="";
					if (StringUtil.length(tCrdCtgInqOut.getString("카드번호")) > 19) {
						sTemp =   DataCryptUtil.decryptCardNo(tCrdCtgInqOut.getString("카드번호"));	
					}else {
						sTemp =   tCrdCtgInqOut.getString("카드번호");
					}
					String sMaskCardNo = StringUtil.mergeStr(StringUtil.substring(sTemp, 0, 6), "******" ,StringUtil.substring(sTemp, 12)    );
					tCrdCtg.setString("카드번호"			, sMaskCardNo);
				}else {
					tCrdCtg.setString("카드번호"			, " ");
				}
				*/
				iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tCrdCtgInqOut.getString("카드대체번호"));
				iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "card");
				iTrsRqstYnCdnoInqIn.setString("고객식별자"					, input.getString("고객식별자"));
				rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);

				if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
					tCrdCtg.setString("전송요구여부"			, "N");	
				}else {
					tCrdCtg.setString("전송요구여부"			, "Y");
				}
				
				if (StringUtil.byteLength(tCrdCtgInqOut.getString("상품명")) > 300) {
					StringUtil.substring(tCrdCtgInqOut.getString("상품명"), 300);
				}else {
					tCrdCtg.setString("카드상품명"				, tCrdCtgInqOut.getString("상품명"));
				}
				tCrdCtg.setString("소지자구분"				, tCrdCtgInqOut.getString("카드소유자구분코드"));
				
				/* 상품중분코드 CP04(체크/체크신용) 구분을 위한 회원중요변경이력 조회 */
				if (DataConvertUtil.equals(tCrdCtgInqOut.getString("상품중분류구분코드"), "CP04")) {
					iCrdDcInqIn.setString("회원일련번호", tCrdCtgInqOut.getString("회원일련번호"));
					rCrdDcInqOut = BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc", "retvMbrBasImpaMdHisLstHis",iCrdDcInqIn);
					
					if ((!StringUtil.trimNisEmpty(rCrdDcInqOut.getString("회원일련번호")) && StringUtil.trimNisEmpty(rCrdDcInqOut.getString("해제년월일")))) {
						tCrdCtg.setString("카드구분", UBD_CONST.STR_03); //소액체크신용
					} else {
						tCrdCtg.setString("카드구분", UBD_CONST.STR_02); //체크
					}
				} else {
					tCrdCtg.setString("카드구분"	 , UBD_CONST.STR_01); //신용
				} 
				tCardList.addLData(tCrdCtg);	
			}
			
			rMyDtApiTlgOut.setInt("보유카드목록_cnt", rCrdCtgInqOut.getDataCount() );
			rMyDtApiTlgOut.set("보유카드목록", tCardList);
			
		}else {
			
			iMciInput.setString("CI번호_V88" 		 , input.getString("CI"));
			iMciInput.setString("다음조회키1_V1000"  , "");
			iMciInput.setString("요청건수_N3" 	     , "500");
			rMciOutput = BizCommand.execute("com.kbcard.ubd.cpbi.cmn.MciSptFntCpbc", "retvCrdCtgInq", iMciInput);
			
			rMultiMciOutPut = rMciOutput.getLMultiData("그리드");
			
			for (int anx = 0; anx < rMultiMciOutPut.getDataCount(); anx++) {
				LData tCrdCtg = new LData();
				LData tCrdCtgInqOut = rMultiMciOutPut.getLData(anx);
				
				tCrdCtg.setString("카드식별자"					, tCrdCtgInqOut.getString("대체카드번호_V16"));
				
				if (!StringUtil.trimNisEmpty(tCrdCtgInqOut.getString("카드번호_V19"))) {
					String sTemp =   DataCryptUtil.decryptCardNo(tCrdCtgInqOut.getString("카드번호_V19"));
					String sMaskCardNo = StringUtil.mergeStr(StringUtil.substring(sTemp, 0, 6), "******" ,StringUtil.substring(sTemp, 12)    );
					tCrdCtg.setString("카드번호"			, sMaskCardNo);
				}else {
					tCrdCtg.setString("카드번호"			, " ");
				}
				
				iTrsRqstYnCdnoInqIn.setString("마이데이터자산내용"			, tCrdCtgInqOut.getString("대체카드번호_V16"));
				iTrsRqstYnCdnoInqIn.setString("마이데이터업권구분코드"		, "card");
				iTrsRqstYnCdnoInqIn.setString("CI내용"						, input.getString("CI"));
				rTrsRqstYnCdnoInqOut =  BizCommand.execute("com.kbcard.ubd.ebi.card.MyDtCrdCtgEbc","retvTrsRqstYn", iTrsRqstYnCdnoInqIn);
				
				if (StringUtil.trimNisEmpty(rTrsRqstYnCdnoInqOut.getString("존재여부"))) {
					tCrdCtg.setString("전송요구여부"			, "N");	
				}else {
					tCrdCtg.setString("전송요구여부"			, "Y");
				}
				
				tCrdCtg.setString("카드상품명" 				, tCrdCtgInqOut.getString("상품명_V300"));
				tCrdCtg.setString("소지자구분"				, tCrdCtgInqOut.getString("카드소지자구분코드_V1"));
				tCrdCtg.setString("카드구분"				, tCrdCtgInqOut.getString("카드구분_V2"));
				tCrdCtgInput.addLData(tCrdCtg);
			}
			rMyDtApiTlgOut.setInt("보유카드목록_cnt"				, rMultiMciOutPut.getDataCount());
			rMyDtApiTlgOut.set("보유카드목록"					, tCrdCtgInput);
		}
		
		return rMyDtApiTlgOut;
	}

	
	/**
	 * @serviceID setRspReturn
	 * @logicalName 
	 * @param LData String sErrCd, String sErrMsg 
	 */
	public void setRspReturn(String sErrCd , String sErrMsg) {
		rMyDtApiTlgOut.setString("세부응답코드"	 	 , sErrCd);
		rMyDtApiTlgOut.setString("세부응답메시지"	 , sErrMsg);
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


