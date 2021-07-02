package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCdMgCpbc;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCstMgCpbc;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.core.exception.LTooManyRowException;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;
import devonenterprise.util.TypeConvertUtil;

/** 
 * opnbTracSvcPbc
 * 
 * @logicalname  : 오픈뱅킹이체서비스Pbc
 * @author       : 임헌택
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       임헌택       최초 생성
 *
 */

public class OpnbTracSvcPbc {

    /**
     * - 이용기관이 출금이체 혹은 입금이체 후 이체결과를 다시 확인
     * - 이체 시 비정상적인 응답코드를 받았을 경우나 응답을 받지 못했을 경우 등 이체결과 확인이 필요한 경우 사용하는 용도
     * 
     * 1. 오픈뱅킹이체내역 정보 확인
     *     - NOT FOUND일경우 오류
     * 2. 이체결과조회 API 호출
     * 3. 오픈뱅킹이체결과조회요청내역에 등록처리.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹이체내역, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 입출금구분코드, 요청일시
     * LIST
     * - 원거래고유번호, 원거래거래일시, 원거래금액, 
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * LIST
     * - 거래고유번호(참가기관), 거래일자, 참가기관응답코드, 참가기관응답메시지, 출금기관코드, 출금기관점별코드, 출금기관명, 개별저축은행명,출금계좌핀테크번호, 출금계좌번호(출력용), 출금계좌인자내역, 송금인성명, 입금기관코드, 입금기관점별코드, 입금기관명, 입금개별저축은행명, 입금계좌핀테크번호, 입금계좌번호(출력용), 입금계좌인자내역, 수취인성명, 거래금액 
     * 
     * @serviceID UBF0100840
     * @method retvTracRst
     * @method(한글명) 이체결과조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvTracRst(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.retvTracRst START ☆★☆☆★☆☆★☆");
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRetvTracRst = new LData(); //이체결과조회 결과값 리턴
        
	    //Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		//오픈뱅킹 입출금구분코드 (1:출금이체,2:입금이체)
		if(input.getString("오픈뱅킹입출금구분코드").compareTo("1") != 0 &&
			input.getString("오픈뱅킹입출금구분코드").compareTo("2") != 0) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹입출금구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹입출금구분코드" ));//오픈뱅킹입출금구분코드가 존재하지 않습니다.
		}
		
		if (TypeConvertUtil.to_int(input.getLong("요청건수_N3")) != input.getLMultiData("그리드").getDataCount()) {
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체목록 건수와 요청건수가 불일치 합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
		}
		
		LData tmpGInput = new LData();
		
		for (int i=0; i < input.getLMultiData("그리드").getDataCount(); i++) {
			
			tmpGInput = input.getLMultiData("그리드").getLData(i);
			
			if(StringUtil.trimNisEmpty(tmpGInput.getString("오픈뱅킹이체거래고유번호"))) {
				if(StringUtil.trimNisEmpty(tmpGInput.getString("오픈뱅킹원거래고유번호"))) {
					throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹원거래고유번호" ));//오픈뱅킹원거래고유번호가 존재하지 않습니다.
				}
			}
			
			if(StringUtil.trimNisEmpty(tmpGInput.getString("오픈뱅킹원거래년월일"))) {
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹원거래년월일" ));//오픈뱅킹원거래년월일이 존재하지 않습니다.
			}
			
			if(tmpGInput.getLong("오픈뱅킹원거래금액") == 0) {
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹원거래금액" ));//오픈뱅킹원거래금액이 존재하지 않습니다.
			}
		}

		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc(); //공통코드조회 cpbc 
		
		// 이체결과조회 API호출
		LData iRetvTracRstAPICall = new LData(); //i이체결과조회API호출입력
        LData rRetvTracRstAPICall = new LData(); //r이체결과조회API호출결과
        
        LMultiData tmpInputInf = new LMultiData(); //입력값 Group
        LMultiData tmpRmtrInf = new LMultiData(); //결과값 출력 Group
        
		try {

			iRetvTracRstAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //거래고유번호(참가기관)
			iRetvTracRstAPICall.setString("check_type"           , input.getString("오픈뱅킹입출금구분코드")); //1:출금이체, 2:입금이체
			iRetvTracRstAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시
			
			for (int i=0; i < input.getLMultiData("그리드").getDataCount(); i++) {
				
				tmpGInput = input.getLMultiData("그리드").getLData(i);
				
				if(StringUtil.trimNisEmpty(tmpGInput.getString("오픈뱅킹원거래고유번호"))) {
					//이체거래고유번호로 원거래정보 조회
					LData iTracTrUno = new LData(); //이체내역조회 input
					LData rTracTrUno = new LData(); //이체내역조회 output
					iTracTrUno.setString("오픈뱅킹이체거래고유번호", tmpGInput.getString("오픈뱅킹이체거래고유번호"));
					try {
						rTracTrUno = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvTracTrUno", iTracTrUno); //이체내역조회
					} catch (LTooManyRowException e) {
						LLog.err.println("오픈뱅킹이체거래고유번호 중복건 발생 오류! "+tmpGInput.getString("오픈뱅킹이체거래고유번호"));
						throw new LBizException("계좌정보조회 TooManyRow오류",e);
					} catch (LNotFoundException nfe) {
						LLog.err.println("오픈뱅킹이체거래고유번호 not found 오류! "+tmpGInput.getString("오픈뱅킹이체거래고유번호"));
						throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체거래고유번호(", tmpGInput.getString("오픈뱅킹이체거래고유번호"), ") 에 해당하는 이체내역이 없습니다.", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
						//tmpGInput.setString("오픈뱅킹원거래고유번호" , opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"));
						//rTracTrUno.setString("오픈뱅킹전문거래년월일", "99999999");
					}
					if (rTracTrUno.getString("오픈뱅킹전문거래년월일").compareTo(tmpGInput.getString("오픈뱅킹원거래년월일")) != 0) {
						LLog.err.println("오픈뱅킹원거래년월일 불일치 발생! "+tmpGInput.getString("오픈뱅킹이체거래고유번호"));
						throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체거래고유번호(", tmpGInput.getString("오픈뱅킹이체거래고유번호"), ") 에 해당하는 원거래년월일이 불일치합니다.", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
					} else {
						tmpGInput.setString("오픈뱅킹원거래고유번호", rTracTrUno.getString("참가기관거래고유번호"));
					}
					tmpGInput.setString("오픈뱅킹사용자고유번호", rTracTrUno.getString("오픈뱅킹사용자고유번호"));
				}
				
				LData tmpInput = new LData();
				
				tmpInput.setLong("tran_no"               , i+1); //거래순번_N2           
				tmpInput.setString("org_bank_tran_id"    , tmpGInput.getString("오픈뱅킹원거래고유번호")); //원거래거래고유번호
				tmpInput.setString("org_bank_tran_date"  , tmpGInput.getString("오픈뱅킹원거래년월일")); //원거래거래일자     
				tmpInput.setLong("org_tran_amt"          , tmpGInput.getLong("오픈뱅킹원거래금액")); //원거래금액_N12
				
		        // 기관거래내역테이블 추가 정보
		        tmpInput.setString("오픈뱅킹사용자고유번호", tmpGInput.getString("오픈뱅킹사용자고유번호"));

				tmpInputInf.addLData(tmpInput); 
			}
			
			iRetvTracRstAPICall.setInt("req_cnt", input.getLMultiData("그리드").getDataCount()); //요청건수
			
			iRetvTracRstAPICall.set("req_list", tmpInputInf); //요청목록
			
	        LLog.debug.println("--- 이체결과조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvTracRstAPICall);
			
	        OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
	        
		    rRetvTracRstAPICall = opnbApi.retvTracRstAPICall(iRetvTracRstAPICall); //이체결과조회 API호출
			
	        //API 호출이후 처리
	        rRetvTracRst.setString("API거래고유번호_V40"    , rRetvTracRstAPICall.getString("api_tran_id"));//API거래고유번호       
	        rRetvTracRst.setString("API거래일시_V17"        , rRetvTracRstAPICall.getString("api_tran_dtm"));//API거래일시       
	        rRetvTracRst.setString("API응답코드_V5"         , rRetvTracRstAPICall.getString("rsp_code"));//API응답코드       
	        rRetvTracRst.setString("API응답메시지_V300"     , rRetvTracRstAPICall.getString("rsp_message"));//API응답메시지        
	        rRetvTracRst.setLong("이체건수_N3"              , rRetvTracRstAPICall.getLong("res_cnt"));//이체건수_N9                  

	        for(int i=0; i < rRetvTracRstAPICall.getLMultiData("res_list").getDataCount(); i++) {
	        	
				// 이체내역변경처리
				// 이체내역의 참가기관응답코드가 정상이 아니면 이체결과조회 내역으로 변경처리.
				LData iRetvTracHis = new LData(); //이체내역조회 입력.
				LData rRetvTracHis = new LData(); //이체내역조회 출력.

				LData rAPIData = new LData(); //단건 처리.
				rAPIData = rRetvTracRstAPICall.getLMultiData("res_list").getLData(i);
				
				iRetvTracHis.setString("오픈뱅킹전문거래년월일"    , rAPIData.getString("bank_tran_date"));// 거래일자(참가기관)
				iRetvTracHis.setString("참가기관거래고유번호"      , rAPIData.getString("bank_tran_id"));// 거래고유번호(참가기관)
				
				try {
					rRetvTracHis = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvOpnbTracHis", iRetvTracHis); //이체내역조회
				} catch (LNotFoundException nfe) {
					rRetvTracHis.setString("오픈뱅킹참가기관응답구분코드", "   ");
					rRetvTracHis.setString("오픈뱅킹사용자고유번호", "UNKNOWN");
					if (input.getString("오픈뱅킹입출금구분코드").compareTo("1") == 0) {
						rRetvTracHis.setString("입출금구분코드", "2"); //출금
					} else {
						rRetvTracHis.setString("입출금구분코드", "1"); //입금
					}
				}
				
				if (rRetvTracHis.getString("오픈뱅킹참가기관응답구분코드").compareTo("000") > 0 && 
					rAPIData.getString("bank_rsp_code").compareTo(rRetvTracHis.getString("오픈뱅킹참가기관응답구분코드")) != 0) {
					//이체내역변경처리
					LData iUpdTracHis = new LData();

					iUpdTracHis.setString("오픈뱅킹전문거래년월일"         , rAPIData.getString("bank_tran_date"));// 오픈뱅킹전문거래년월일
					iUpdTracHis.setString("참가기관거래고유번호"           , rAPIData.getString("bank_tran_id"));// 참가기관거래고유번호
					iUpdTracHis.setString("오픈뱅킹참가기관응답구분코드"   , rAPIData.getString("bank_rsp_code"));// 오픈뱅킹참가기관응답구분코드
					iUpdTracHis.setString("오픈뱅킹참가기관응답메시지내용" , rAPIData.getString("bank_rsp_message"));// 오픈뱅킹참가기관응답메시지내용
					iUpdTracHis.setString("오픈뱅킹API응답구분코드"        , rRetvTracRstAPICall.getString("rsp_code"));// 오픈뱅킹API응답구분코드
					iUpdTracHis.setString("오픈뱅킹API응답메시지내용"      , rRetvTracRstAPICall.getString("rsp_message"));// 오픈뱅킹API응답메시지내용
//					iUpdTracHis.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
					iUpdTracHis.setString("시스템최종갱신식별자"           , "UBF2030603");// 시스템최종갱신식별자
//					iUpdTracHis.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

					LLog.debug.println("이체내역결과 수정 입력값 출력 ----"); 
					LLog.debug.println(iUpdTracHis);
					
					try {
						BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "uptOpnbTracHisRst", iUpdTracHis); //
					} catch (LException e) {
					   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체내역결과수정(uptOpnbTracHisRst) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
					} 
				}
				
				// 이체결과조회요청회차조회처리
				LData iRtrvTracRstNth = new LData();
				LData rRtrvTracRstNth = new LData();

				iRtrvTracRstNth.setString("오픈뱅킹전문거래년월일"    , rAPIData.getString("bank_tran_date"));// 거래일자(참가기관)
				iRtrvTracRstNth.setString("참가기관거래고유번호"      , rAPIData.getString("bank_tran_id"));// 거래고유번호(참가기관)

				rRtrvTracRstNth = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvOpnbTracRstNth", iRtrvTracRstNth); //결과조회 회차조회
				
				// 이체결과조회요청등록처리
				LData iRegTracRst = new LData();

				iRegTracRst.setString("오픈뱅킹전문거래년월일"        , rAPIData.getString("bank_tran_date"));// 오픈뱅킹전문거래년월일
				iRegTracRst.setLong("오픈뱅킹재전송요청회차"          , rRtrvTracRstNth.getLong("오픈뱅킹재전송요청회차")); //오픈뱅킹재전송요청회차
				iRegTracRst.setString("참가기관거래고유번호"          , rAPIData.getString("bank_tran_id"));// 참가기관거래고유번호
                iRegTracRst.setString("채널세부업무구분코드"          , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
                iRegTracRst.setString("오픈뱅킹API거래고유번호"       , rRetvTracRstAPICall.getString("api_tran_id")); //오픈뱅킹API거래고유번호
                iRegTracRst.setString("입출금구분코드"                , rRetvTracHis.getString("입출금구분코드")); //입출금구분코드
                iRegTracRst.setString("입출금금액"                    , rAPIData.getString("tran_amt")); //거래금액
                iRegTracRst.setString("오픈뱅킹사용자고유번호"        , rRetvTracHis.getString("오픈뱅킹사용자고유번호")); //사용자고유번호
//                iRegTracRst.setString("오픈뱅킹이체결과조회요청일시"  , DateUtil.getCurrentTime("yyyyMMddHHmmss")); //요청일시
                iRegTracRst.setString("오픈뱅킹이체결과조회요청일시"  , DateUtil.getCurrentDate()); //요청일시
                iRegTracRst.setString("오픈뱅킹API응답구분코드"       , rRetvTracRstAPICall.getString("rsp_code")); //응답코드(API)
                iRegTracRst.setString("오픈뱅킹API응답메시지내용"     , rRetvTracRstAPICall.getString("rsp_message")); //응답메시지(API)
                iRegTracRst.setString("오픈뱅킹응답금융기관코드"      , rAPIData.getString("bank_code_tran")); //응답코드를 부여한 참가기관.표준코드
                iRegTracRst.setString("오픈뱅킹참가기관응답구분코드"  , rAPIData.getString("bank_rsp_code")); //응답코드(참가기관)
                iRegTracRst.setString("오픈뱅킹참가기관응답메시지내용", rAPIData.getString("bank_rsp_message")); //응답메시지(참가기관)
                iRegTracRst.setString("출금금융기관코드"              , rAPIData.getString("wd_bank_code_std")); //출금기관.표준코드
                iRegTracRst.setString("출금금융기관지점별코드"        , rAPIData.getString("wd_bank_code_sub")); //출금기관.점별코드
                iRegTracRst.setString("출금개별저축은행명"            , rAPIData.getString("wd_savings_bank_name")); //개별(출금)저축은행명
                iRegTracRst.setString("출금계좌핀테크이용번호"        , rAPIData.getString("wd_fintech_use_num")); //출금계좌 핀테크이용번호
                iRegTracRst.setString("출금출력계좌번호"              , rAPIData.getString("wd_account_num_masked")); //출금계좌번호(출력용)
                iRegTracRst.setString("출금계좌인자내용"              , rAPIData.getString("wd_print_content")); //출금계좌인자내역
                iRegTracRst.setString("송금의뢰인명"                  , rAPIData.getString("wd_account_holder_name")); //송금인성명
                //iRegTracRst.setString("송금의뢰인명"                  , CryptoDataUtil.encryptKey(rAPIData.getString("wd_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //송금인성명
                iRegTracRst.setString("입금금융기관코드"              , rAPIData.getString("dps_bank_code_std")); //입금(개설)기관.표준코드
                iRegTracRst.setString("입금금융기관지점별코드"        , rAPIData.getString("dps_bank_code_sub")); //입금(개설)기관.점별코드
                iRegTracRst.setString("입금개별저축은행명"            , rAPIData.getString("dps_savings_bank_name")); //개별(입금)저축은행명
                iRegTracRst.setString("입금계좌핀테크이용번호"        , rAPIData.getString("dps_fintech_use_num")); //입금계좌 핀테크이용번호
                iRegTracRst.setString("입금출력계좌번호"              , rAPIData.getString("dps_account_num_masked")); //입금계좌번호(출력용)
                iRegTracRst.setString("입금계좌인자내용"              , rAPIData.getString("dps_print_content")); //입금계좌인자내역
                iRegTracRst.setString("수취인명"                      , rAPIData.getString("dps_account_holder_name")); //수취인성명
                //iRegTracRst.setString("수취인명"                      , CryptoDataUtil.encryptKey(rAPIData.getString("dps_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //수취인성명
				
				//iRegTracRst.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
				iRegTracRst.setString("시스템최초생성식별자", "UBF2030603");// 시스템최초생성식별자
				//iRegTracRst.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
				iRegTracRst.setString("시스템최종갱신식별자", "UBF2030603");// 시스템최종갱신식별자
				//iRegTracRst.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

				LLog.debug.println("이체결과조회요청등록 EBC 입력값 출력 ----"); 
				LLog.debug.println(iRegTracRst);
				
				try {
					BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "regOpnbTracRst", iRegTracRst); //
				} catch (LException e) {
				   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체결과조회요청등록(regOpnbTracRst) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
				}				
	        	LData tmpSelRec = new LData();
	        	
	        	tmpSelRec.setString("거래순번_N2"              , rAPIData.getString("tran_no")); //거래순번
	        	tmpSelRec.setString("참가기관거래고유번호_V20" , rAPIData.getString("bank_tran_id")); //참가기관거래고유번호
	        	tmpSelRec.setString("참가기관거래일자_V8"      , rAPIData.getString("bank_tran_date")); //참가기관거래일자
	        	tmpSelRec.setString("참가기관표준코드_V3"      , rAPIData.getString("bank_code_tran")); //참가기관표준코드_V3
	        	tmpSelRec.setString("참가기관응답코드_V3"      , rAPIData.getString("bank_rsp_code")); //참가기관응답코드
	        	tmpSelRec.setString("참가기관응답메시지_V100"  , rAPIData.getString("bank_rsp_message")); //참가기관응답메시지
	        	if(StringUtil.trimNisEmpty(tmpSelRec.getString("참가기관응답메시지_V100")) &&
	        		!StringUtil.trimNisEmpty(rAPIData.getString("bank_rsp_code"))) {
	        		tmpSelRec.setString("참가기관응답메시지_V100", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드", tmpSelRec.getString("참가기관응답코드_V3")).getString("통합코드내용"));
	        	}
	        	tmpSelRec.setString("출금기관대표코드_V3"      , rAPIData.getString("wd_bank_code_std")); //출금기관대표코드_V3
	        	tmpSelRec.setString("출금기관지점별코드"       , rAPIData.getString("wd_bank_code_sub")); //출금기관지점별코드
	        	tmpSelRec.setString("출금기관명_V20"           , rAPIData.getString("wd_bank_name")); //출금기관명_V20
	        	tmpSelRec.setString("출금개별저축은행명"       , rAPIData.getString("wd_savings_bank_name")); //출금개별저축은행명
	        	tmpSelRec.setString("출금계좌핀테크이용번호"   , rAPIData.getString("wd_account_num_masked")); //출금계좌핀테크이용번호
	        	tmpSelRec.setString("출금출력계좌번호_V20"     , rAPIData.getString("wd_account_num_masked")); //출금출력계좌번호
	        	tmpSelRec.setString("출금계좌인자내용"         , rAPIData.getString("wd_print_content")); //출금계좌인자내용
	        	tmpSelRec.setString("송금의뢰인명"            , rAPIData.getString("wd_account_holder_name")); //송금인성명_V22
	        	//tmpSelRec.setString("송금인성명_V50"           , CryptoDataUtil.encryptKey(rAPIData.getString("wd_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //송금인성명_V22
	        	tmpSelRec.setString("입금기관대표코드_V3"      , rAPIData.getString("dps_bank_code_std")); //입금기관대표코드_V3
	        	tmpSelRec.setString("입금기관지점별코드_V7"    , rAPIData.getString("dps_bank_code_sub")); //입금기관지점별코드_V7
	        	tmpSelRec.setString("입금기관명_V20"           , rAPIData.getString("dps_bank_name")); //입금기관명_V20
	        	tmpSelRec.setString("입금개별저축은행명"       , rAPIData.getString("dps_savings_bank_name")); //입금개별저축은행명
	        	tmpSelRec.setString("입금계좌핀테크이용번호"   , rAPIData.getString("dps_fintech_use_num")); //입금계좌핀테크이용번호
	        	tmpSelRec.setString("입금출력계좌번호_V20"     , rAPIData.getString("dps_account_num_masked")); //입금출력계좌번호
	        	tmpSelRec.setString("입금계좌인자내용"         , rAPIData.getString("dps_print_content")); //입금계좌인자내용
	        	tmpSelRec.setString("수취인명"               , rAPIData.getString("dps_account_holder_name")); //수취인명_V20
	        	//tmpSelRec.setString("수취인명"             , CryptoDataUtil.encryptKey(rAPIData.getString("dps_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //수취인명_V20
	        	tmpSelRec.setLong("오픈뱅킹거래금액"           , rAPIData.getLong("tran_amt")); //오픈뱅킹거래금액

	        	tmpRmtrInf.addLData(tmpSelRec); //출력 Group 추가
	        }
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		rRetvTracRst.setLong("그리드_cnt", rRetvTracRstAPICall.getLong("res_cnt")); //그리드count
		//rRetvTracRst.setLong("GRID_cnt", tmpRmtrInf.getDataCount()); //그리드count
		rRetvTracRst.set("그리드", tmpRmtrInf); //그리드셋
        
		LLog.debug.println("OpnbTracSvcPbc.retvTracRst END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvTracRst);
        
        return rRetvTracRst;
    }

    /**
     * - 거래가 완료된 입금이체에 대해서 사용자의 착오송금, 이용기관의 장애 등으로 자금반환이 필요한 경우 이용기관은 자금반환을 청구
     * 
     * 1. 입금이체 원장 정보 확인.
     *     - NOT FOUND일경우 오류
     * 2. 자금반환 요청 원장에 등록처리.
     * 3. 자금반환청구 API 호출
     * 4. 거래고유번호등 정상처리 결과 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹이체내역, UBF오픈뱅킹자금청구접수내역, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 거래고유번호(참가기관), 전액반환여부, 반환금액, 자금반환청구내용, 자금반환청구사유구분코드, 청구사유, 반환금액입금계좌, 이용기관담당자연락처, 이용기관담당자이메일주소
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 응답코드,응답메시지, 정상등록여부
     * 
     * @serviceID UBF0300800
     * @method regtFndsRtunBil
     * @method(한글명) 자금반환청구등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtFndsRtunBil(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.regtFndsRtunBil START ☆★☆☆★☆☆★☆");
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRegFndsRtun = new LData(); //자금반환청구 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹원거래년월일"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹원거래년월일"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹원거래년월일" ));//오픈뱅킹원거래년월일이 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹원거래고유번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹원거래고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹원거래고유번호" ));//오픈뱅킹원거래고유번호가 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("입금계좌핀테크이용번호"))) {
			if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹원거래입금기관코드")) ||
				StringUtil.trimNisEmpty(input.getString("오픈뱅킹원거래입금계좌번호_V48"))) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입금계좌핀테크번호 또는 기관코드와 계좌번호를  입력하세요.-", ObsErrCode.ERR_7011.getName()));//입력값을 확인하세요.
			}
		}

		if(StringUtil.trimNisEmpty(input.getString("자금반환청구사유구분코드"))) {
			LLog.debug.println("로그 " + input.getString("자금반환청구사유구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-자금반환청구사유구분코드" ));//자금반환청구사유구분코드가 존재하지 않습니다.
		}

		if(input.getString("자금반환청구사유구분코드").compareTo("02") != 0 &&
			input.getString("자금반환청구사유구분코드").compareTo("03") != 0 &&
			input.getString("자금반환청구사유구분코드").compareTo("04") != 0 &&
			input.getString("자금반환청구사유구분코드").compareTo("05") != 0 &&
			input.getString("자금반환청구사유구분코드").compareTo("08") != 0) {
			LLog.debug.println("로그 " + input.getString("자금반환청구사유구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-자금반환청구사유구분코드" ));//자금반환청구사유구분코드가 존재하지 않습니다.
		}
				
		if(input.getString("자금반환청구사유구분코드").compareTo("05") != 0) {
			if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹자금반환청구사유"))) {
				LLog.debug.println("로그 " + input.getString("오픈뱅킹자금반환청구사유"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹자금반환청구사유" ));//오픈뱅킹자금반환청구사유가 존재하지 않습니다.
			}
		}
					
		if(input.getString("오픈뱅킹전액반환여부").compareTo("Y") != 0 &&
			input.getString("오픈뱅킹전액반환여부").compareTo("N") != 0) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹전액반환여부"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹전액반환여부" ));//오픈뱅킹전액반환여부가 존재하지 않습니다.
		}
				
		if(input.getLong("오픈뱅킹반환금액") <= 0) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹반환금액"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹반환금액" ));//오픈뱅킹반환금액이 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹반환입금계좌번호_V48"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹반환입금계좌번호_V48"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹반환입금계좌번호_V48" ));//오픈뱅킹반환입금계좌번호_V48이 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("자금반환이용기관담당자전화번호"))) {
			LLog.debug.println("로그 " + input.getString("자금반환이용기관담당자전화번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-자금반환이용기관담당자전화번호" ));//자금반환이용기관담당자전화번호가 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("자금반환기관담당자이메일주소_V152"))) {
			LLog.debug.println("로그 " + input.getString("자금반환기관담당자이메일주소_V152"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-자금반환기관담당자이메일주소_V152" ));//자금반환기관담당자이메일주소_V152가 존재하지 않습니다.
		}

		// 자금반환 정상접수여부조회
		LData iOgtnRcpYn = new LData(); //자금반환접수여부조회 입력.
		LData rOgtnRcpYn = new LData(); //자금반환접수여부조회 출력.
		
		iOgtnRcpYn.setString("오픈뱅킹원거래년월일"    , input.getString("오픈뱅킹원거래년월일"));// 거래일자(참가기관)
		iOgtnRcpYn.setString("오픈뱅킹원거래고유번호"  , input.getString("오픈뱅킹원거래고유번호"));// 거래고유번호(참가기관)
		
		try {
			rOgtnRcpYn = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvOgtnRcpYn", iOgtnRcpYn); //이체내역조회
		} catch (LNotFoundException nfe) {
		}
		
		if(!StringUtil.trimNisEmpty(rOgtnRcpYn.getString("오픈뱅킹전문거래년월일")) ||
			!StringUtil.trimNisEmpty(rOgtnRcpYn.getString("참가기관거래고유번호"))) {
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("정상접수 자금반환 등록내역이 존재합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
		}
		
		// 이체내역조회
		LData iRetvTracHis = new LData(); //이체내역조회 입력.
		LData rRetvTracHis = new LData(); //이체내역조회 출력.

		iRetvTracHis.setString("오픈뱅킹전문거래년월일"    , input.getString("오픈뱅킹원거래년월일"));// 거래일자(참가기관)
		iRetvTracHis.setString("참가기관거래고유번호"      , input.getString("오픈뱅킹원거래고유번호"));// 거래고유번호(참가기관)
		
		try {
			rRetvTracHis = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvOpnbTracHis", iRetvTracHis); //이체내역조회
		} catch (LNotFoundException nfe) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), "오픈뱅킹원거래정보가  존재하지 않습니다.-", StringUtil.mergeStr(ObsErrCode.ERR_7777.getName())); //DB처리중 오류가 발생하였습니다.
		}
		
		if(input.getString("오픈뱅킹전액반환여부").compareTo("Y") == 0) {
			if(input.getLong("오픈뱅킹반환금액") != rRetvTracHis.getLong("입출금금액")) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입출금금액이 불일치 합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
			}
		} else {
			if(input.getLong("오픈뱅킹반환금액") > rRetvTracHis.getLong("입출금금액")) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("요청금액이 원금액보다 큽니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
			}
		}
		
		//입금핀테크 이용번호 혹은 입금계좌정보(입금계좌번호 및 입금기관.표준코드)를 세팅하여야 하며, 둘 다 세팅할 경우 포맷오류로 응답함.
		if(!StringUtil.trimNisEmpty(input.getString("입금계좌핀테크이용번호"))) {
			if(input.getString("입금계좌핀테크이용번호").compareTo(rRetvTracHis.getString("핀테크이용번호")) != 0) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입금계좌핀테크번호가 이체내역의 핀테크번호와 불일치 합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
			}
			input.setString("오픈뱅킹원거래입금기관코드","");
			input.setString("오픈뱅킹원거래입금계좌번호_V48","");
		} else {
			if(input.getString("오픈뱅킹원거래입금기관코드").compareTo(rRetvTracHis.getString("입출금금융기관코드")) != 0) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입금기관코드가 이체내역의 기관코드와 불일치 합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
			}
			if(input.getString("오픈뱅킹원거래입금계좌번호_V48").compareTo(rRetvTracHis.getString("입출금계좌번호")) != 0) {
				throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("입금계좌번호가 이체내역의 계좌번호와 불일치 합니다.-", ObsErrCode.ERR_7011.getName())); //입력값을 확인바랍니다.
			}
			input.setString("입금계좌핀테크이용번호","");
		}

		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		
        OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
        
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc(); //거래고유번호 채번 호출
		
		String sFndsRtun = opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"); //자금반환청구 거래고유번호
		
		LLog.debug.println("lht 자금반환청구 거래고유번호 = " + sFndsRtun);
		
		// 자금반환청구 API호출
		LData iRegFndsRtunAPICall = new LData(); //i자금반환청구API호출입력
        LData rRegFndsRtunAPICall = new LData(); //r자금반환청구API호출결과
        
		try {

			iRegFndsRtunAPICall.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			iRegFndsRtunAPICall.setString("bank_tran_id"            , sFndsRtun); //거래고유번호(참가기관)
			iRegFndsRtunAPICall.setString("org_bank_tran_date"      , input.getString("오픈뱅킹원거래년월일")); //원거래 거래일자
			iRegFndsRtunAPICall.setString("org_bank_tran_id"        , input.getString("오픈뱅킹원거래고유번호")); //원거래 거래고유번호(참가기관)
			iRegFndsRtunAPICall.setString("org_dps_bank_code_std"   , input.getString("오픈뱅킹원거래입금기관코드")); //원거래 입금기관.표준코드
			iRegFndsRtunAPICall.setString("org_dps_account_num"     , input.getString("오픈뱅킹원거래입금계좌번호_V48")); //원거래 입금계좌번호
			//iRegFndsRtunAPICall.setString("org_dps_account_num"     , CryptoDataUtil.decryptKey(input.getString("오픈뱅킹원거래입금계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //원거래 입금계좌번호
			iRegFndsRtunAPICall.setString("org_dps_fintech_use_num" , input.getString("입금계좌핀테크이용번호")); //원거래 입금핀테크이용번호
			iRegFndsRtunAPICall.setLong("org_tran_amt"              , input.getLong("오픈뱅킹반환금액")); //거래금액(청구금액)
			iRegFndsRtunAPICall.setString("org_wd_bank_code_std"    , input.getString("오픈뱅킹원거래출금기관코드")); //원거래 출금기관.표준코드
			iRegFndsRtunAPICall.setString("claim_code"              , input.getString("자금반환청구사유구분코드")); //청구사유코드
			iRegFndsRtunAPICall.setString("claim_message"           , input.getString("오픈뱅킹자금반환청구사유")); //청구사유
			iRegFndsRtunAPICall.setString("total_return_yn"         , input.getString("오픈뱅킹전액반환여부")); //전액반환여부
			iRegFndsRtunAPICall.setString("return_account_num"      , input.getString("오픈뱅킹반환입금계좌번호_V48")); //반환금액 입금계좌
			//iRegFndsRtunAPICall.setString("return_account_num"      , CryptoDataUtil.decryptKey(input.getString("오픈뱅킹반환입금계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //반환금액 입금계좌
			iRegFndsRtunAPICall.setString("use_org_contact"         , input.getString("자금반환이용기관담당자전화번호")); //이용기관 담당자 연락처
			iRegFndsRtunAPICall.setString("use_org_email"           , input.getString("자금반환기관담당자이메일주소_V152")); //이용기관 담당자 이메일주소
			//iRegFndsRtunAPICall.setString("use_org_email"           , CryptoDataUtil.decryptKey(input.getString("자금반환기관담당자이메일주소_V152"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //이용기관 담당자 이메일주소
			
	        // 기관거래내역테이블 추가 정보
			iRegFndsRtunAPICall.setString("오픈뱅킹사용자고유번호", rRetvTracHis.getString("오픈뱅킹사용자고유번호"));
			
	        LLog.debug.println("--- 자금반환청구 API호출 입력값 ----"); 
			LLog.debug.println(iRegFndsRtunAPICall);
			
	        rRegFndsRtunAPICall = opnbApi.dmndFndsRtunAPICall(iRegFndsRtunAPICall); //자금반환청구 API호출
			
	        //API 호출이후 처리
	        rRegFndsRtun.setString("API거래고유번호_V40"            , rRegFndsRtunAPICall.getString("api_tran_id"));//API거래고유번호       
	        rRegFndsRtun.setString("API거래일시_V17"                , rRegFndsRtunAPICall.getString("api_tran_dtm"));//API거래일시       
	        rRegFndsRtun.setString("API응답코드_V5"                 , rRegFndsRtunAPICall.getString("rsp_code"));//API응답코드       
	        rRegFndsRtun.setString("API응답메시지_V300"             , rRegFndsRtunAPICall.getString("rsp_message"));//API응답메시지        
	        rRegFndsRtun.setString("오픈뱅킹입금기관대표코드"       , rRegFndsRtunAPICall.getString("dps_bank_code_std"));//오픈뱅킹입금기관대표코드           
	        rRegFndsRtun.setString("참가기관거래고유번호_V20"       , rRegFndsRtunAPICall.getString("bank_tran_id")); //참가기관거래고유번호
	        rRegFndsRtun.setString("참가기관거래일자_V8"            , rRegFndsRtunAPICall.getString("bank_tran_date")); //참가기관거래일자
	        rRegFndsRtun.setString("참가기관표준코드_V3"            , rRegFndsRtunAPICall.getString("bank_code_tran")); //참가기관표준코드_V3
	        rRegFndsRtun.setString("참가기관응답코드_V3"            , rRegFndsRtunAPICall.getString("bank_rsp_code")); //참가기관응답코드
	        rRegFndsRtun.setString("참가기관응답메시지_V100"        , rRegFndsRtunAPICall.getString("bank_rsp_message")); //참가기관응답메시지
	        rRegFndsRtun.setString("출금기관대표코드_V3"            , rRegFndsRtunAPICall.getString("wd_bank_code_std")); //출금기관대표코드_V3
	        rRegFndsRtun.setString("출금기관지점별코드_V7"          , rRegFndsRtunAPICall.getString("wd_bank_code_sub")); //출금기관지점별코드
	        rRegFndsRtun.setString("출금기관명_V20"                 , rRegFndsRtunAPICall.getString("wd_bank_name")); //출금기관명_V20
	        rRegFndsRtun.setString("오픈뱅킹원거래년월일"           , rRegFndsRtunAPICall.getString("org_bank_tran_date")); //오픈뱅킹원거래년월일
	        rRegFndsRtun.setString("오픈뱅킹원거래고유번호"         , rRegFndsRtunAPICall.getString("org_bank_tran_id")); //오픈뱅킹원거래고유번호
	        rRegFndsRtun.setString("오픈뱅킹원거래입금기관코드"     , rRegFndsRtunAPICall.getString("org_dps_bank_code_std")); //오픈뱅킹원거래입금기관코드
	        rRegFndsRtun.setString("오픈뱅킹원거래입금계좌번호_V48" , rRegFndsRtunAPICall.getString("org_dps_account_num")); //오픈뱅킹원거래입금계좌번호
	        //rRegFndsRtun.setString("오픈뱅킹원거래입금계좌번호_V48" , CryptoDataUtil.encryptKey(rRegFndsRtunAPICall.getString("org_dps_account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //오픈뱅킹원거래입금계좌번호
	        rRegFndsRtun.setString("입금계좌핀테크이용번호"         , rRegFndsRtunAPICall.getString("org_dps_fintech_use_num")); //입금계좌핀테크이용번호
	        rRegFndsRtun.setLong("오픈뱅킹원거래금액"              , rRegFndsRtunAPICall.getLong("org_tran_amt")); //원거래금액_N12
	        rRegFndsRtun.setString("오픈뱅킹반환입금계좌번호_V48"   , rRegFndsRtunAPICall.getString("return_account_num")); //오픈뱅킹반환입금계좌번호
	        //rRegFndsRtun.setString("오픈뱅킹반환입금계좌번호_V48"   , CryptoDataUtil.encryptKey(rRegFndsRtunAPICall.getString("return_account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //오픈뱅킹반환입금계좌번호

	        //참가기관 정상 혹은 처리중이거나 내부오류인경우는 등록내역을 남겨, 자금반환 결과조회를 할수 있도록 한다.
	        if(rRegFndsRtunAPICall.getString("bank_rsp_code").compareTo("000") == 0 ||
	        	rRegFndsRtunAPICall.getString("bank_rsp_code").compareTo("900") == 0 ||
	        	rRegFndsRtunAPICall.getString("rsp_code").compareTo("A0003") == 0 ||
	        	rRegFndsRtunAPICall.getString("rsp_code").compareTo("A0007") == 0 ||
	        	StringUtil.trimNisEmpty(rRegFndsRtunAPICall.getString("rsp_code"))) {
				// 자금청구요청내역 등록처리
				LData iRegFndsRtun = new LData();
	
				iRegFndsRtun.setString("오픈뱅킹전문거래년월일"          , rRegFndsRtunAPICall.getString("bank_tran_date"));// 오픈뱅킹전문거래년월일
				iRegFndsRtun.setString("참가기관거래고유번호"            , rRegFndsRtunAPICall.getString("bank_tran_id"));// 참가기관거래고유번호
	            iRegFndsRtun.setString("채널세부업무구분코드"            , input.getString("채널세부업무구분코드"));// 채널세부업무구분코드
	            iRegFndsRtun.setString("오픈뱅킹사용자고유번호"          , rRetvTracHis.getString("오픈뱅킹사용자고유번호"));// 오픈뱅킹사용자고유번호
	            iRegFndsRtun.setString("오픈뱅킹API거래고유번호"         , rRegFndsRtunAPICall.getString("api_tran_id"));// 오픈뱅킹API거래고유번호
	            iRegFndsRtun.setString("오픈뱅킹원거래고유번호"          , rRegFndsRtunAPICall.getString("org_bank_tran_id"));// 오픈뱅킹원거래고유번호
	            iRegFndsRtun.setLong("오픈뱅킹원거래금액"                , rRegFndsRtunAPICall.getLong("org_tran_amt"));// 오픈뱅킹원거래금액
	            iRegFndsRtun.setString("오픈뱅킹원거래년월일"            , rRegFndsRtunAPICall.getString("org_bank_tran_date"));// 오픈뱅킹원거래년월일
	            iRegFndsRtun.setString("오픈뱅킹원거래입금기관대표코드"  , rRegFndsRtunAPICall.getString("org_dps_bank_code_std"));// 오픈뱅킹원거래입금기관대표코드
	            iRegFndsRtun.setString("오픈뱅킹원거래입금계좌번호"      , rRegFndsRtunAPICall.getString("org_dps_account_num"));// 오픈뱅킹원거래입금계좌번호
	            //iRegFndsRtun.setString("오픈뱅킹원거래입금계좌번호"      , CryptoDataUtil.encryptKey(rRegFndsRtunAPICall.getString("org_dps_account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));// 오픈뱅킹원거래입금계좌번호
	            iRegFndsRtun.setString("핀테크이용번호"                  , rRegFndsRtunAPICall.getString("org_dps_fintech_use_num"));// 입금계좌핀테크이용번호
	            iRegFndsRtun.setString("오픈뱅킹원거래출금기관대표코드"  , rRegFndsRtunAPICall.getString("wd_bank_code_std"));// 오픈뱅킹원거래출금기관대표코드
	            iRegFndsRtun.setString("오픈뱅킹반환입금계좌번호"        , rRegFndsRtunAPICall.getString("wd_bank_name"));// 오픈뱅킹반환입금계좌번호
	            //iRegFndsRtun.setString("오픈뱅킹반환입금계좌번호"        , CryptoDataUtil.encryptKey(rRegFndsRtunAPICall.getString("wd_bank_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));// 오픈뱅킹반환입금계좌번호
	            iRegFndsRtun.setString("담당자휴대폰번호"                , input.getString("자금반환이용기관담당자전화번호"));// 담당자휴대폰번호
	            iRegFndsRtun.setString("자금반환청구사유구분코드"        , input.getString("자금반환청구사유구분코드"));// 자금반환청구사유구분코드
	            iRegFndsRtun.setString("오픈뱅킹자금반환청구사유"        , input.getString("오픈뱅킹자금반환청구사유"));// 오픈뱅킹자금반환청구사유
	            iRegFndsRtun.setString("오픈뱅킹전액반환여부"            , input.getString("오픈뱅킹전액반환여부"));// 오픈뱅킹전액반환여부
	            iRegFndsRtun.setString("오픈뱅킹API응답구분코드"         , rRegFndsRtunAPICall.getString("rsp_code"));//API응답코드       
	            iRegFndsRtun.setString("오픈뱅킹API응답메시지내용"       , rRegFndsRtunAPICall.getString("rsp_message"));//API응답메시지        
	            iRegFndsRtun.setString("오픈뱅킹참가기관응답구분코드"    , rRegFndsRtunAPICall.getString("bank_rsp_code")); //참가기관응답코드
	            iRegFndsRtun.setString("오픈뱅킹참가기관응답메시지내용"  , rRegFndsRtunAPICall.getString("bank_rsp_message")); //참가기관응답메시지
	//            , 자금반환결과구분코드
	//            , 자금반환불가사유구분코드
	//            , 오픈뱅킹자금반환불가사유
	            iRegFndsRtun.setLong("오픈뱅킹반환금액"                  , input.getLong("오픈뱅킹반환금액")); //거래금액(청구금액)
	//            , 오픈뱅킹자금반환년월일
				//iRegFndsRtun.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
				iRegFndsRtun.setString("시스템최초생성식별자", "UBF2030604");// 시스템최초생성식별자
				//iRegFndsRtun.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
				iRegFndsRtun.setString("시스템최종갱신식별자", "UBF2030604");// 시스템최종갱신식별자
				//iRegFndsRtun.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시
	
				LLog.debug.println("자금청구요청등록 EBC 입력값 출력 ----"); 
				LLog.debug.println(iRegFndsRtun);
				
				try {
					BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "regOpnbFndsRtun", iRegFndsRtun); //
				} catch (LException e) {
				   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "자금청구요청등록(regOpnbFndsRtun) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
				}				
	        }
				
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbTracSvcPbc.regtFndsRtunBil END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRegFndsRtun);
        
        return rRegFndsRtun;
    }

    /**
     * - 이용기관이 자금반환 청구 요청 후 자금반환 결과를 확인
     * - 자금반환 청구 요청결과 확인이 필요한  경우나 자금반환 청구가 정상적으로 요청된 후 반환 결과를 확인하는 용도
     * 
     * 1. 조회조건(청구요청거래일자/청구요청거래고유번호)에 따른 자금반환요청원장 목록 조회
     *     - not found일 경우 오류 리턴
     * 2. 요청원장의 정보로 자금반환결과 조회 API 호출하여 조회목록에 결과값 셋팅
     * 3. 신청결과 목록조회 리턴
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹자금청구접수내역, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI값, 거래기간
     * 
     * <OUTPUT>
     * LIST
     * - 거래고유번호(참가기관), 금융기관코드, 계좌번호, 전액반환여부, 자금반환결과구분코드, 자금반환청구내용, 자금반환불가사유
     * 
     * @serviceID UBF0100841
     * @method retvFndsRtunRst
     * @method(한글명) 자금반환결과조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvFndsRtunRst(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.retvFndsRtunRst START ☆★☆☆★☆☆★☆");
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRetvFndsRtunRst = new LData(); //자금반환청구결과조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹전문거래년월일"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹전문거래년월일"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹전문거래년월일" ));//오픈뱅킹전문거래년월일이 존재하지 않습니다.
		}

		if(StringUtil.trimNisEmpty(input.getString("참가기관거래고유번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("참가기관거래고유번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-참가기관거래고유번호_V20" ));//참가기관거래고유번호_V20이 존재하지 않습니다.
		}

		// 자금청구내역조회
		LData iRetvFndsRtun = new LData(); //자금청구내역조회 입력.
		LData rRetvFndsRtun = new LData(); //자금내역조회 출력.

		iRetvFndsRtun.setString("오픈뱅킹전문거래년월일"    , input.getString("오픈뱅킹전문거래년월일"));// 거래일자(참가기관)
		iRetvFndsRtun.setString("참가기관거래고유번호"      , input.getString("참가기관거래고유번호_V20"));// 거래고유번호(참가기관)
		
		try {
			rRetvFndsRtun = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvOpnbFndsRtun", iRetvFndsRtun); //이체내역조회
		} catch (LNotFoundException nfe) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-자금반환청구내역" ));//자금반환청구내역이 존재하지 않습니다.
		}
		
        OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
        
		// 자금반환청구 API호출
		LData iRetvFndsRtunAPICall = new LData(); //i자금반환청구API호출입력
        LData rRetvFndsRtunAPICall = new LData(); //r자금반환청구API호출결과
        
		try {

			iRetvFndsRtunAPICall.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			iRetvFndsRtunAPICall.setString("claim_bank_tran_date"    , input.getString("오픈뱅킹전문거래년월일")); //청구 요청 거래일자
			iRetvFndsRtunAPICall.setString("claim_bank_tran_id"      , input.getString("참가기관거래고유번호_V20")); //청구 요청 거래고유번호
			
	        // 기관거래내역테이블 추가 정보
			iRetvFndsRtunAPICall.setString("오픈뱅킹사용자고유번호", rRetvFndsRtun.getString("오픈뱅킹사용자고유번호"));
			
	        LLog.debug.println("--- 자금반환결과조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvFndsRtunAPICall);
			
	        rRetvFndsRtunAPICall = opnbApi.retvFndsRtunRstAPICall(iRetvFndsRtunAPICall); //자금반환결과조회 API호출
			
	        //API 호출이후 처리
	        rRetvFndsRtunRst.setString("API거래고유번호_V40"      , rRetvFndsRtunAPICall.getString("api_tran_id"));//API거래고유번호       
	        rRetvFndsRtunRst.setString("API거래일시_V17"          , rRetvFndsRtunAPICall.getString("api_tran_dtm"));//API거래일시       
	        rRetvFndsRtunRst.setString("API응답코드_V5"           , rRetvFndsRtunAPICall.getString("rsp_code"));//API응답코드       
	        rRetvFndsRtunRst.setString("API응답메시지_V300"       , rRetvFndsRtunAPICall.getString("rsp_message"));//API응답메시지        
	        rRetvFndsRtunRst.setString("참가기관거래일자_V8"      , rRetvFndsRtunAPICall.getString("claim_bank_tran_date"));//청구 요청 거래일자        
	        rRetvFndsRtunRst.setString("참가기관거래고유번호_V20" , rRetvFndsRtunAPICall.getString("claim_bank_tran_id")); //청구 요청 거래고유번호
	        rRetvFndsRtunRst.setString("청구요청정상여부_V1"      , rRetvFndsRtunAPICall.getString("claim_normal_yn")); //청구 요청 정상여부
	        rRetvFndsRtunRst.setString("참가기관응답코드_V3"      , rRetvFndsRtunAPICall.getString("claim_bank_rsp_code")); //청구 요청 응답코드(참가기관)
	        rRetvFndsRtunRst.setString("참가기관응답메시지_V100"  , rRetvFndsRtunAPICall.getString("claim_bank_rsp_message")); //청구 요청 응답메시지(참가기관)
	        rRetvFndsRtunRst.setString("자금반환결과구분코드"     , rRetvFndsRtunAPICall.getString("return_yn")); //자금반환여부
	        rRetvFndsRtunRst.setString("자금반환불가사유구분코드" , rRetvFndsRtunAPICall.getString("return_fail_code")); //자금반환불가사유코드
	        rRetvFndsRtunRst.setString("오픈뱅킹자금반환불가사유" , rRetvFndsRtunAPICall.getString("return_fail_message")); //자금반환불가사유
	        rRetvFndsRtunRst.setString("오픈뱅킹전액반환여부"     , rRetvFndsRtunAPICall.getString("total_return_yn")); //전액반환여부
	        
	        if (StringUtil.isEmpty(rRetvFndsRtunAPICall.getString("return_amt"))) {
	        	rRetvFndsRtunAPICall.setLong("return_amt"       , 0);
	        }
	        
        	rRetvFndsRtunRst.setLong("오픈뱅킹반환금액"           , TypeConvertUtil.parseTo_long(rRetvFndsRtunAPICall.getString("return_amt"))); //반환금액
	        rRetvFndsRtunRst.setString("오픈뱅킹자금반환년월일"   , rRetvFndsRtunAPICall.getString("return_date")); //반환일자

			// 자금청구요청내역 결과반영처리
			LData iUptFndsRtun = new LData();

			iUptFndsRtun.setString("오픈뱅킹전문거래년월일"       , input.getString("오픈뱅킹전문거래년월일"));// 오픈뱅킹전문거래년월일
			iUptFndsRtun.setString("참가기관거래고유번호"         , input.getString("참가기관거래고유번호_V20"));// 참가기관거래고유번호
			iUptFndsRtun.setString("오픈뱅킹자금반환청구정상여부" , rRetvFndsRtunAPICall.getString("claim_normal_yn")); //청구 요청 정상여부
			iUptFndsRtun.setString("자금반환결과구분코드"         , rRetvFndsRtunAPICall.getString("return_yn")); //자금반환여부
			iUptFndsRtun.setString("자금반환불가사유구분코드"     , rRetvFndsRtunAPICall.getString("return_fail_code")); //자금반환불가사유코드
			iUptFndsRtun.setString("오픈뱅킹자금반환불가사유"     , rRetvFndsRtunAPICall.getString("return_fail_message")); //자금반환불가사유
			iUptFndsRtun.setLong("오픈뱅킹반환금액"               , rRetvFndsRtunAPICall.getLong("return_amt")); //반환금액
			iUptFndsRtun.setString("오픈뱅킹자금반환년월일"       , rRetvFndsRtunAPICall.getString("return_date")); //반환일자
			//iRegFndsRtun.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
			iUptFndsRtun.setString("시스템최종갱신식별자"         , "UBF2030605");// 시스템최종갱신식별자
			//iRegFndsRtun.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

			LLog.debug.println("자금청구요청수정 EBC 입력값 출력 ----"); 
			LLog.debug.println(iUptFndsRtun);
			
			try {
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "uptOpnbFndsRtun", iUptFndsRtun); //
			} catch (LException e) {
			   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "자금청구요청수정(uptOpnbFndsRtun) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
			}				
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbTracSvcPbc.retvFndsRtunRst END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvFndsRtunRst);
        
        return rRetvFndsRtunRst;
    }

    /**
     * - 이용자의 최근 입금이체이력을 조회
     * - 최근입금이체 이력을 바탕으로 간편 송금이체 처리하기 위함.
     * 
     * 1. 최근 입금이체이력 n건 조회처리.
     * 2. 이체이력 원장 조회.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹이체내역
     * 
     * <INPUT>
     * - 오픈뱅킹사용자고유번호 또는 CI값.
     * 
     * <OUTPUT>
     * LIST
     * - 거래고유번호(참가기관), 금융기관코드, 계좌번호, 입금금액, 입금일자
     * 
     * @serviceID UBF0100842
     * @method retvLaMrvTracPhs
     * @method(한글명) 최근입금이체이력조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvLaMrvTracPhs(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.retvLaMrvTracPhs START ☆★☆☆★☆☆★☆");
		
        LData rLaMrvTracPhs = new LData(); //최근입금이체이력조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {
			if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
				LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
			}
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) &&
			!StringUtil.trimNisEmpty(input.getString("CI내용"))) {
	    	LData iRetvUsrUnoP = new LData(); // i사용자고유번호조회입력
	        LData rRetvUsrUnoP = new LData(); // r사용자고유번호조회출력
	    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	    	iRetvUsrUnoP.setString("CI내용", input.getString("CI내용"));
	    	rRetvUsrUnoP = opnbCstMgCpbc.retvUsrUno(iRetvUsrUnoP);
	    	input.setString("오픈뱅킹사용자고유번호",rRetvUsrUnoP.getString("오픈뱅킹사용자고유번호"));
		}
			 
		// 최근입금이체이력조회 Ebc호출 
		LData iRetvTracHis = new LData();
		LMultiData rRetvTracHis = new LMultiData();
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
		iRetvTracHis.setString("조회시작년월일", DateUtil.getDateMonthsBefore(3,DateUtil.getCurrentDate())); //조회시작년월일 (최근3개월 이체내역)
		iRetvTracHis.setString("조회종료년월일", DateUtil.getCurrentDate()); //조회종료년월일
		iRetvTracHis.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호")); //사용자고유번호
		iRetvTracHis.setString("입출금구분코드", "1"); //입출금구분코드 '1':입금, '2':출금
		rRetvTracHis = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "retvLstOpnbTracHis", iRetvTracHis); //
		
		LMultiData 	tmTracHis 	= new LMultiData(); //tm이체이력조회(output)
        
        for (int i=0; i < rRetvTracHis.getDataCount(); i++) {
			LData tmpTracHis = new LData();
			
			tmpTracHis.setString("오픈뱅킹전문거래년월일"     , rRetvTracHis.getLData(i).getString("오픈뱅킹전문거래년월일"));          		//오픈뱅킹전문거래년월일            
			tmpTracHis.setString("참가기관거래고유번호_V20"   , rRetvTracHis.getLData(i).getString("참가기관거래고유번호"));          		//참가기관거래고유번호            
			tmpTracHis.setString("입출금금융기관코드"         , rRetvTracHis.getLData(i).getString("입출금금융기관코드"));          		//입출금금융기관코드            
			tmpTracHis.setString("입출금계좌번호"             , rRetvTracHis.getLData(i).getString("입출금계좌번호"));          		//입출금계좌번호            
			tmpTracHis.setString("입출금금액"                 , rRetvTracHis.getLData(i).getString("입출금금액"));          		//입출금금액            

			tmTracHis.addLData(tmpTracHis);
        }
        
        rLaMrvTracPhs.setLong("이체건수_N3", rRetvTracHis.getDataCount()); //이체건수
        rLaMrvTracPhs.setLong("그리드_cnt", rRetvTracHis.getDataCount()); //그리드count
        rLaMrvTracPhs.set("그리드", tmTracHis); //그리드셋
        
		LLog.debug.println("OpnbTracSvcPbc.retvLaMrvTracPhs END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rLaMrvTracPhs);
        
        
        return rLaMrvTracPhs; 
    }

    /**
     * - 입금계좌의 유효성 검증
     * - 이체처리 전 사용자가 입력한 계좌의 입출금 거래 가능여부를 체크하기 위함.
     * 
     * 1. 기관코드와 은행계좌번호로 계좌실명조회 API 호출.
     * 2. 입출력계좌 정상 여부를 확인하여 리턴한다.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 개설기관코드, 계좌번호, 예금주실명번호구분코드, 실명번호
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 계좌종류, 개별저축은행명,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지, 입금가능여부
     * 
     * @serviceID UBF0100843
     * @logicalName 입금계좌유효성조회
     * @method retvMrvAccVld
     * @method(한글명) 입금계좌유효성조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvMrvAccVld(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.retvMrvAccVld START ☆★☆☆★☆☆★☆");
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRetvAccRlnmP = new LData(); //계좌실명조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("계좌개설은행코드"))) {
			LLog.debug.println("로그 " + input.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
		 
		if(StringUtil.trimNisEmpty(input.getString("고객계좌번호"))) {
			LLog.debug.println("로그 " + input.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("실명번호구분코드_V1"))) {
			LLog.debug.println("로그 " + input.getString("실명번호구분코드_V1"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-실명번호구분코드_V1" ));//실명번호구분코드_V1가 존재하지 않습니다.
		}
			 
		if(input.getString("실명번호구분코드_V1").compareTo("N") != 0) {
			if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹예금주실명번호_V13"))) {
				LLog.debug.println("로그 " + input.getString("오픈뱅킹예금주실명번호_V13"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹예금주실명번호_V13" ));//오픈뱅킹예금주실명번호_V13가 존재하지 않습니다.
			}
		}
			 
		 
		// 계좌실명조회 API호출
  		LData iRetvAccRlnmAPICall = new LData();; // 계좌실명조회입력
  		LData rRetvAccRlnmAPICall = new LData(); // 계좌실명조회출력
        
  		OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
  		
  		OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc(); //거래고유번호 채번모듈 호출
  		
  		iRetvAccRlnmAPICall.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
  		iRetvAccRlnmAPICall.setString("bank_tran_id"            , opnbCdMgCpbc.crtTrUno(input).getString("거래고유번호")); //참가기관거래고유번호ㅗ
  		iRetvAccRlnmAPICall.setString("bank_code_std"           , input.getString("계좌개설은행코드")); //계좌개설은행코드
  		iRetvAccRlnmAPICall.setString("account_num"             , input.getString("고객계좌번호")); //고객개좌번호
  		//iRetvAccRlnmAPICall.setString("account_num"             , CryptoDataUtil.decryptKey(input.getString("고객계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객개좌번호
  		//iRetvAccRlnmAPICall.setString("account_seq"           , iRetvAccRlnmP.getString("ACCOUNT_SEQ"));
  		iRetvAccRlnmAPICall.setString("account_holder_info_type", input.getString("실명번호구분코드_V1")); //오픈뱅킹계좌명의구분코드
  		if(!StringUtil.trimNisEmpty(input.getString("오픈뱅킹예금주실명번호_V13"))) {
  			iRetvAccRlnmAPICall.setString("account_holder_info"     , input.getString("오픈뱅킹예금주실명번호_V13")); //오픈뱅킹예금주실명번호
  		}
  		iRetvAccRlnmAPICall.setString("tran_dtime"              , DateUtil.getCurrentTime("yyyyMMddHHmmss")); //요청일시
		
		try {

		    LLog.debug.println("--- 계좌실명조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvAccRlnmAPICall);
			
      		rRetvAccRlnmAPICall = opnbApi.retvAccRlnmAPICall(iRetvAccRlnmAPICall);
      		
      		//API 호출이후 처리
      		rRetvAccRlnmP.setString("API거래고유번호_V40"        , rRetvAccRlnmAPICall.getString("api_tran_id"));              //거래고유번호(API)
      		rRetvAccRlnmP.setString("API거래일시_V17"            , rRetvAccRlnmAPICall.getString("api_tran_dtm"));             //거래일시(밀리세컨드)
      		rRetvAccRlnmP.setString("API응답코드_V5"             , rRetvAccRlnmAPICall.getString("rsp_code"));                 //API응답코드
      		rRetvAccRlnmP.setString("API응답메시지_V300"         , rRetvAccRlnmAPICall.getString("rsp_message"));              //API응답메시지
      		rRetvAccRlnmP.setString("참가기관거래고유번호_V20"   , rRetvAccRlnmAPICall.getString("bank_tran_id"));             //참가기관거래고유번호
      		rRetvAccRlnmP.setString("참가기관거래일자_V8"        , rRetvAccRlnmAPICall.getString("bank_tran_date"));           //거래일자
      		rRetvAccRlnmP.setString("참가기관표준코드_V3"        , rRetvAccRlnmAPICall.getString("bank_code_tran"));           //은행표준코드
      		rRetvAccRlnmP.setString("참가기관응답코드_V3"        , rRetvAccRlnmAPICall.getString("bank_rsp_code"));            //오픈뱅킹참가기관응답구분코드  
      		rRetvAccRlnmP.setString("참가기관응답메시지_V100"    , rRetvAccRlnmAPICall.getString("bank_rsp_message"));         //오픈뱅킹참가기관응답메시지내용
      		rRetvAccRlnmP.setString("개설기관대표코드_V3"        , rRetvAccRlnmAPICall.getString("bank_code_std"));            //참가기관표준코드
      		rRetvAccRlnmP.setString("개설기관점별코드_V7"        , rRetvAccRlnmAPICall.getString("bank_code_sub"));            //개설기관점별코드
      		rRetvAccRlnmP.setString("참가기관명_V20"             , rRetvAccRlnmAPICall.getString("bank_name"));                //참가기관명
      		rRetvAccRlnmP.setString("개별저축은행명_V20"         , rRetvAccRlnmAPICall.getString("savings_bank_name"));        //개별저축은행명
      		rRetvAccRlnmP.setString("고객계좌번호"           , rRetvAccRlnmAPICall.getString("account_num"));              //계좌번호
      		//rRetvAccRlnmP.setString("고객계좌번호"           , CryptoDataUtil.encryptKey(rRetvAccRlnmAPICall.getString("account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));              //계좌번호
      		rRetvAccRlnmP.setString("회차번호_V3"                , rRetvAccRlnmAPICall.getString("account_seq"));              //회차번호
      		rRetvAccRlnmP.setString("실명번호구분코드_V1"        , rRetvAccRlnmAPICall.getString("account_holder_info_type")); //실명번호구분코드
      		rRetvAccRlnmP.setString("오픈뱅킹예금주실명번호_V13" , rRetvAccRlnmAPICall.getString("account_holder_info"));      //예금주실명번호
      		rRetvAccRlnmP.setString("예금주명"             , rRetvAccRlnmAPICall.getString("account_holder_name"));      //예금주성명
      		//rRetvAccRlnmP.setString("예금주명"             , CryptoDataUtil.encryptKey(rRetvAccRlnmAPICall.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));      //예금주성명
      		rRetvAccRlnmP.setString("오픈뱅킹계좌종류구분코드"                , rRetvAccRlnmAPICall.getString("account_type"));             //계좌종류

      		if (rRetvAccRlnmAPICall.getString("account_type").compareTo("1") == 0) {
      			rRetvAccRlnmP.setString("입출금가능여부_V1", "Y"); //입출금가능여부_V1
      		} else {
      			rRetvAccRlnmP.setString("입출금가능여부_V1", "N"); //입출금가능여부_V1
      		}
	        
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbTracSvcPbc.retvMrvAccVld END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvAccRlnmP);
        
        
        return rRetvAccRlnmP;

    }

    /**
     * - 출금계좌의 유효성 검증.
     * - 송금처리전 출금계좌의 유효성을 검증한다.
     * 
     * 1. 출금계좌 등록여부 확인.
     * 2. 잔액조회 API를 호출하여, 출금한도 조회.
     * 3. 일누적출금금액을 체크하여 일 이체한도 조회 체크.
     * 4. 출금 가능여부 리턴.
     * 
     * <관련테이블>
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관), 계좌잔액, 출금계좌핀테크이용번호,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지, 출금가능여부
     * 
     * @serviceID UBF0100844
     * @logicalName 출금계좌유효성조회
     * @method retvOdwAccVld
     * @method(한글명) 출금계좌유효성조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvOdwAccVld(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.retvOdwAccVld START ☆★☆☆★☆☆★☆");
		
	    LLog.debug.println("입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rRetvOdwAccVld = new LData(); //계좌잔액조회 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("계좌개설은행코드"))) {
			LLog.debug.println("로그 " + input.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("고객계좌번호"))) {
			LLog.debug.println("로그 " + input.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호_V48가 존재하지 않습니다.
		}

		
		// 계좌기본조회 Ebc호출 
		LData iRetvAccInf = new LData();
		LData rRetvAccInf = new LData();
		
		try {
			iRetvAccInf.setString("계좌개설은행코드", input.getString("계좌개설은행코드"));
			iRetvAccInf.setString("고객계좌번호"    , input.getString("고객계좌번호"));
			//iRetvAccInf.setString("고객계좌번호"    , CryptoDataUtil.decryptKey(input.getString("고객계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY));
			rRetvAccInf = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectAcnoPrBl", iRetvAccInf); //
		} catch (LTooManyRowException e) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr("고객계좌정보 조회 ", ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		} catch (LNotFoundException nfe) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr("고객계좌정보 조회 ", ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		}
		
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		LData rCdMg = new LData();
		
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		
		rCdMg = opnbCdMg.crtTrUno(iCdMg);
		
		// 계좌잔액조회 API호출
		LData iRetvBlByAcnoAPICall = new LData(); //i계좌잔액조회API호출입력
        LData rRetvBlByAcnoAPICall = new LData(); //r계좌잔액조회API호출결과
        
		try {

			iRetvBlByAcnoAPICall.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
	        iRetvBlByAcnoAPICall.setString("bank_tran_id"         , rCdMg.getString("거래고유번호")); //거래고유번호(참가기관)
	        iRetvBlByAcnoAPICall.setString("bank_code_std"        , input.getString("계좌개설은행코드")); //계좌개설은행코드
	        iRetvBlByAcnoAPICall.setString("account_num"          , input.getString("고객계좌번호")); //고객계좌번호
	        //iRetvBlByAcnoAPICall.setString("account_num"          , CryptoDataUtil.decryptKey(rRetvAccInf.getString("고객계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //고객계좌번호
	        iRetvBlByAcnoAPICall.setString("account_seq"          , rRetvAccInf.getString("계좌납입회차")); //계좌납입회차
	        iRetvBlByAcnoAPICall.setString("user_seq_no"          , rRetvAccInf.getString("오픈뱅킹사용자고유번호")); //오픈뱅킹사용자고유번호
	        iRetvBlByAcnoAPICall.setString("tran_dtime"           , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시

			LLog.debug.println("--- 계좌잔액조회 API호출 입력값 ----"); 
			LLog.debug.println(iRetvBlByAcnoAPICall);
			
	        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
	        
	        rRetvBlByAcnoAPICall = opnbApi.retvBlByAcnoAPICall(iRetvBlByAcnoAPICall); //계좌번호로 잔액조회
			
	        //API 호출이후 처리
			rRetvOdwAccVld.setString("API거래고유번호_V40"     , rRetvBlByAcnoAPICall.getString("api_tran_id")); // 거래고유번호(API)
			rRetvOdwAccVld.setString("API거래일시_V17"         , rRetvBlByAcnoAPICall.getString("api_tran_dtm")); // 거래일시(밀리세컨드)
			rRetvOdwAccVld.setString("API응답코드_V5"          , rRetvBlByAcnoAPICall.getString("rsp_code")); // API응답코드
			rRetvOdwAccVld.setString("API응답메시지_V300"      , rRetvBlByAcnoAPICall.getString("rsp_message")); // API응답메시지
			rRetvOdwAccVld.setString("참가기관거래고유번호_V20", rRetvBlByAcnoAPICall.getString("bank_tran_id")); // 참가기관거래고유번호
			rRetvOdwAccVld.setString("참가기관거래일자_V8"     , rRetvBlByAcnoAPICall.getString("bank_tran_date")); // 거래일자
			rRetvOdwAccVld.setString("참가기관표준코드_V3"     , rRetvBlByAcnoAPICall.getString("bank_code_tran")); // 응답코드를 부여한 참가기관.표준코드
			rRetvOdwAccVld.setString("참가기관응답코드_V3"     , rRetvBlByAcnoAPICall.getString("bank_rsp_code")); // 오픈뱅킹참가기관응답구분코드
			rRetvOdwAccVld.setString("참가기관응답메시지_V100" , rRetvBlByAcnoAPICall.getString("bank_rsp_message")); // 오픈뱅킹참가기관응답메시지내용
			rRetvOdwAccVld.setString("오픈뱅킹개설기관명"      , rRetvBlByAcnoAPICall.getString("bank_name")); // 오픈뱅킹개설기관명
			rRetvOdwAccVld.setString("개별저축은행명"          , rRetvBlByAcnoAPICall.getString("savings_bank_name")); // 개별저축은행명
			rRetvOdwAccVld.setLong("계좌잔액_N15"              , rRetvBlByAcnoAPICall.getLong("balance_amt")); // 계좌잔액_N15
			rRetvOdwAccVld.setLong("출금가능금액"              , rRetvBlByAcnoAPICall.getLong("available_amt")); // 출금가능금액
			rRetvOdwAccVld.setString("오픈뱅킹계좌종류구분코드", rRetvBlByAcnoAPICall.getString("account_type")); // 오픈뱅킹계좌종류구분코드
			rRetvOdwAccVld.setString("오픈뱅킹계좌상품명"      , rRetvBlByAcnoAPICall.getString("product_name")); // 오픈뱅킹계좌상품명
			rRetvOdwAccVld.setString("계좌개설년월일"          , rRetvBlByAcnoAPICall.getString("account_issue_date")); // 계좌개설년월일
			rRetvOdwAccVld.setString("계좌만기년월일_V8"       , rRetvBlByAcnoAPICall.getString("maturity_date")); // 계좌만기년월일_V8
			rRetvOdwAccVld.setString("최종거래년월일"          , rRetvBlByAcnoAPICall.getString("last_tran_date")); // 최종거래년월일

      		if (rRetvBlByAcnoAPICall.getString("account_type").compareTo("1") == 0) {
      			rRetvOdwAccVld.setString("입출금가능여부_V1", "Y"); //입출금가능여부_V1
      		} else {
      			rRetvOdwAccVld.setString("입출금가능여부_V1", "N"); //입출금가능여부_V1
      		}
      		
      		if (rRetvBlByAcnoAPICall.getLong("available_amt") == 0) {
      			rRetvOdwAccVld.setString("입출금가능여부_V1", "N"); //입출금가능여부_V1
      		}
	        
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbTracSvcPbc.retvOdwAccVld END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rRetvOdwAccVld);
        
        
        return rRetvOdwAccVld;
    }

    /**
     * - 입금계좌정보와 당사출금정보를 입력받아 출금처리.
     * 
     * 1. 입금요청 필수입력값 체크
     *     - 입금계좌번호 및 입금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 2. 계좌실명조회 API 호출
     *     - 입금계좌의 입금가능여부 및 계좌주실명 사전 조회
     * 3. 입금이체 API 호출
     * 4. 오픈뱅킹이체내역 LIST 생성
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역, UBF오픈뱅킹고객정보기본, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 출금계좌인자내역, 입금이체용 암호문구, 수취인성명검증여부
     * LIST
     * - 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도, 입금계좌인자내역
     * 
     * <OUTPUT>
     * - 응답코드, 응답메시지
     * LIST
     * - 거래고유번호(참가기관),핀테크이용번호, 참가기관응답코드, 참가기관응답메시지
     * 
     * @serviceID UBF0600800
     * @logicalName 입금요청처리
     * @method prcMrvDmd
     * @method(한글명) 입금요청처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcMrvDmd(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.prcMrvDmd START ☆★☆☆★☆☆★☆");
		
		LLog.debug.println("입금요청처리 입력값 출력 ----"); 
		LLog.debug.println(input);
		
        LData rPrcMrvDmd = new LData(); //입금요청처리 결과값 리턴
        
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹계좌계정구분코드"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹계좌계정구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹계좌계정구분코드" ));//오픈뱅킹계좌계정구분코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("집금모계좌번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("집금모계좌번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-집금모계좌번호_V20" ));//집금모계좌번호_V20가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("입금이체용암호내용_V128"))) {
			LLog.debug.println("로그 " + input.getString("입금이체용암호내용_V128"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입금이체용암호내용_V128" ));//입금이체용암호내용_V128가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("출금계좌인자내용"))) {
			LLog.debug.println("로그 " + input.getString("출금계좌인자내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-출금계좌인자내용" ));//출금계좌인자내용이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("수취인성명검증내용_V3"))) {
			input.setString("수취인성명검증내용_V3", "on"); //기본값 셋팅
		}
			
		if(input.getString("수취인성명검증내용_V3").compareTo("on") != 0 &&
			input.getString("수취인성명검증내용_V3").compareTo("off") != 0) {
			LLog.debug.println("로그 " + input.getString("수취인성명검증내용_V3"));
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("수취인성명검증내용_V3 ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
		}
		
		if(input.getLong("입금요청건수_N7") != 1) {
			throw new LException("입금요청건수는  한건만 가능합니다."); //입금요청건수_N7
		}
		
		//LMulti data to LData
		LData myInput = input.getLMultiData("그리드").getLData(0);
		
        if (StringUtil.trimNisEmpty(myInput.getString("입금계좌핀테크이용번호"))) {
        	if (StringUtil.trimNisEmpty(myInput.getString("오픈뱅킹입금기관대표코드")) ||
        		StringUtil.trimNisEmpty(myInput.getString("오픈뱅킹입금계좌번호"))) {
        		throw new LException("입금계좌번호 및 입금계좌기관코드를 입력하세요."); 
        	}
        	
        }
		
        if (!StringUtil.trimNisEmpty(myInput.getString("입금계좌핀테크이용번호"))) {
        	if (!StringUtil.trimNisEmpty(myInput.getString("오픈뱅킹입금기관대표코드")) ||
        		!StringUtil.trimNisEmpty(myInput.getString("오픈뱅킹입금계좌번호"))) {
        		throw new LException("입금계좌핀테크번호 입력시에는 입금계좌번호 및 입금계좌기관코드 중복입력은 불가합니다."); 
        	}
        	
        }
		
        if (StringUtil.trimNisEmpty(myInput.getString("요청자핀테크이용번호_V24"))) {
        	if (StringUtil.trimNisEmpty(myInput.getString("요청고객계좌기관코드_V3")) ||
        		StringUtil.trimNisEmpty(myInput.getString("요청고객계좌번호_V48"))) {
        		throw new LException("요청고객계좌번호 및 요청고객계좌기관코드를 입력하세요."); 
        	}
        	
        }

        if (!StringUtil.trimNisEmpty(myInput.getString("요청자핀테크이용번호_V24"))) {
        	if (!StringUtil.trimNisEmpty(myInput.getString("요청고객계좌기관코드_V3")) ||
        		!StringUtil.trimNisEmpty(myInput.getString("요청고객계좌번호_V48"))) {
        		throw new LException("요청고객핀테크번호 입력시에는 요청고객계좌번호 및 계좌개설기관코드 중복입력은 불가합니다."); 
        	}
        	
        }

        if(StringUtil.trimNisEmpty(myInput.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("로그 " + myInput.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(myInput.getString("요청고객회원번호_V20"))) {
			myInput.setString("요청고객회원번호_V20", myInput.getString("오픈뱅킹사용자고유번호"));
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객회원번호_V20" ));//요청고객회원번호_V20가 존재하지 않습니다.
		}
			 
        if(StringUtil.trimNisEmpty(myInput.getString("요청고객회원번호_V20"))) {
			LLog.debug.println("로그 " + myInput.getString("요청고객회원번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객회원번호_V20" ));//요청고객회원번호_V20가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(myInput.getString("입금계좌예금주명_V30"))) {
			LLog.debug.println("로그 " + myInput.getString("입금계좌예금주명_V30"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입금계좌예금주명_V30" ));//입금계좌예금주명_V30이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(myInput.getString("입금계좌인자내용"))) {
			LLog.debug.println("로그 " + myInput.getString("입금계좌인자내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입금계좌인자내용" ));//입금계좌인자내용이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(myInput.getString("입출금요청고객명_V50"))) {
			LLog.debug.println("로그 " + myInput.getString("입출금요청고객명_V50"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입출금요청고객명_V50" ));//입출금요청고객명_V50이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(myInput.getString("이체용도구분코드_V2"))) {
			LLog.debug.println("로그 " + myInput.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-이체용도구분코드_V2" ));//이체용도구분코드_V2이 존재하지 않습니다.
		}

		if(myInput.getString("이체용도구분코드_V2").compareTo("TR") != 0 &&
			myInput.getString("이체용도구분코드_V2").compareTo("ST") != 0 &&
			myInput.getString("이체용도구분코드_V2").compareTo("AU") != 0) {
			LLog.debug.println("로그 " + myInput.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체용도구분코드_V2 ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
		}
			
		//무료이용정책 반영관련 참가기관출금거래고유번호 필수입력 체크
//		if(myInput.getString("이체용도구분코드_V2").compareTo("TR") == 0) {
//			if(StringUtil.trimNisEmpty(myInput.getString("참가기관출금거래고유번호"))) {
//				LLog.debug.println("로그 " + myInput.getString("참가기관출금거래고유번호"));
//				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-참가기관출금거래고유번호" ));//참가기관출금거래고유번호가 존재하지 않습니다.
//			}
//		}

//		if(StringUtil.trimNisEmpty(myInput.getString("수취조회거래고유번호_V20"))) {
//			LLog.debug.println("로그 " + myInput.getString("수취조회거래고유번호_V20"));
//			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-수취조회거래고유번호_V20" ));//수취조회거래고유번호_V20가 존재하지 않습니다.
//		}

        // 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		
        OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
        
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc(); //거래고유번호 채번 호출
		
//		String sPrcRcev = opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"); //수취조회 거래고유번호
//		
//		LLog.debug.println("lht 수취조회 거래고유번호 = " + sPrcRcev); 
//		
//		// 수취조회 API호출
//		LData iPrcRcevAPICall = new LData(); //i수취조회API호출입력
//        LData rPrcRcevAPICall = new LData(); //r수취조회API호출결과
//        
//		try {
//			iPrcRcevAPICall.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
//			iPrcRcevAPICall.setString("bank_tran_id"              , sPrcRcev); //참가기관거래고유번호  
//			iPrcRcevAPICall.setString("cntr_account_type"         , input.getString("오픈뱅킹계좌계정구분코드")); //오픈뱅킹계좌계정구분코드
//			iPrcRcevAPICall.setString("cntr_account_num"          , input.getString("집금모계좌번호_V20")); //집금모계좌번호
//			iPrcRcevAPICall.setString("bank_code_std"             , myInput.getString("오픈뱅킹입금기관대표코드")); //오픈뱅킹입금기관대표코드     
//			iPrcRcevAPICall.setString("account_num"               , myInput.getString("오픈뱅킹입금계좌번호")); //입금계좌번호_V16      
//	        if(!StringUtil.trimNisEmpty(myInput.getString("회차번호_V3"))) {
//	        	iPrcRcevAPICall.setString("account_seq"               , myInput.getString("회차번호_V3")); //회차번호
//	        }
//	        if(!StringUtil.trimNisEmpty(myInput.getString("입금계좌핀테크이용번호"))) {
//	        	iPrcRcevAPICall.setString("fintech_use_num"           , myInput.getString("입금계좌핀테크이용번호")); //입금계좌핀테크이용번호
//	        }
//			iPrcRcevAPICall.setString("print_content"             , myInput.getString("입금계좌인자내용")); //입금계좌인자내용      
//			iPrcRcevAPICall.setLong("tran_amt"                    , myInput.getLong("오픈뱅킹거래금액")); //거래금액_N12          
//			iPrcRcevAPICall.setString("req_client_name"           , myInput.getString("입출금요청고객명_V50")); //입출금요청고객명      
//			iPrcRcevAPICall.setString("req_client_bank_code"      , myInput.getString("요청고객계좌기관코드_V3")); //계좌개설은행코드      
//			iPrcRcevAPICall.setString("req_client_account_num"    , myInput.getString("요청고객계좌번호_V48")); //고객계좌번호_V16      
//	        if(!StringUtil.trimNisEmpty(myInput.getString("요청자핀테크이용번호_V24"))) {
//	        	iPrcRcevAPICall.setString("req_client_fintech_use_num", myInput.getString("요청자핀테크이용번호_V24")); //핀테크이용번호
//	        }
//			iPrcRcevAPICall.setString("req_client_num"            , myInput.getString("요청고객회원번호_V20")); //요청고객회원번호_V20
//			iPrcRcevAPICall.setString("transfer_purpose"          , myInput.getString("이체용도구분코드_V2")); //이체용도구분코드      
//			iPrcRcevAPICall.setString("sub_frnc_name"             , input.getString("하위가맹점명_V100")); //하위가맹점명      
//	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점번호_V15"))) {
//	        	iPrcRcevAPICall.setString("sub_frnc_num"              , input.getString("하위가맹점번호_V15")); //하위가맹점번호
//	        }
//	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점사업자등록번호"))) {
//	        	iPrcRcevAPICall.setString("sub_frnc_business_num"     , input.getString("하위가맹점사업자등록번호")); //하위가맹점 사업자등록번호
//	        }
//	        if(!StringUtil.trimNisEmpty(myInput.getString("CMS번호_V32"))) {
//	        	iPrcRcevAPICall.setString("cms_num"                   , myInput.getString("CMS번호_V32")); //CMS번호_V32
//	        }
//			
//			rPrcRcevAPICall = opnbApi.retvRcevAPICall(iPrcRcevAPICall); //수취조회 API호출
//			
//		} catch ( LException e) {
//			e.printStackTrace(LLog.err); 
//			
////			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
////			
////			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
////			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
//		}
//
//		//수취조회 리턴값에 따라 입금이체 처리 진행.
//		if (rPrcRcevAPICall.getString("rsp_code").compareTo("A0000") != 0 ||
//			rPrcRcevAPICall.getString("bank_rsp_code").compareTo("000") != 0) {
//			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr(rPrcRcevAPICall.getString("rsp_message") , ObsErrCode.ERR_7011.getName()));//수취조회 거래시 오류가 발생하였습니다.
//		}
		
		// 이체요청등록처리(require new처리 필요)
		LData iRegTracHis = new LData();

		String sPrcMrvDmd = opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"); //입금이체 거래고유번호
		
		LLog.debug.println("lht 입금이체 거래고유번호 = " + sPrcMrvDmd); 
		
		iRegTracHis.setString("오픈뱅킹전문거래년월일"        , DateUtil.getCurrentDate());// 오픈뱅킹전문거래년월일
		iRegTracHis.setString("참가기관거래고유번호"          , sPrcMrvDmd);// 참가기관거래고유번호
		iRegTracHis.setString("채널세부업무구분코드"          , input.getString("채널세부업무구분코드"));// 채널세부업무구분코드
		//iRegTracHis.setString("오픈뱅킹API거래고유번호"       , "");// 오픈뱅킹API거래고유번호
		iRegTracHis.setString("입출금구분코드"                , "1");                                  // 입출금구분코드 '1':입금, '2':출금
		iRegTracHis.setString("참가기관출금거래고유번호"      , myInput.getString("참가기관출금거래고유번호"));// 참가기관출금거래고유번호
		iRegTracHis.setString("오픈뱅킹사용자고유번호"        , myInput.getString("오픈뱅킹사용자고유번호"));// 오픈뱅킹사용자고유번호
		iRegTracHis.setString("오픈뱅킹이용기관계좌번호"      , input.getString("집금모계좌번호_V20"));// 집금모계좌번호
		iRegTracHis.setString("입출금금융기관코드"            , myInput.getString("오픈뱅킹입금기관대표코드"));// 입출금금융기관코드
		iRegTracHis.setString("입출금계좌번호"                , myInput.getString("오픈뱅킹입금계좌번호"));// 입출금계좌번호
		iRegTracHis.setString("계좌납입회차"                  , myInput.getString("회차번호_V3"));// 계좌납입회차
		iRegTracHis.setString("핀테크이용번호"                , myInput.getString("입금계좌핀테크이용번호"));// 핀테크이용번호
		iRegTracHis.setLong("입출금금액"                      , myInput.getLong("오픈뱅킹거래금액"));// 입출금금액
		iRegTracHis.setLong("이체수수료"                      , 0);// 이체수수료
		iRegTracHis.setString("입출금계좌인자내용"            , myInput.getString("입금계좌인자내용"));// 입출금계좌인자내용
		iRegTracHis.setString("입출금요청고객명"              , myInput.getString("입출금요청고객명_V50"));// 입출금요청고객명
		iRegTracHis.setString("입출금거래일시"                , DateUtil.getCurrentTime("yyyyMMddHHmmss"));// 입출금거래일시
//		iRegTracHis.setString("입출금결과금융기관코드"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과금융기관코드
//		iRegTracHis.setString("입출금결과금융기관지점별코드"  , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과금융기관지점별코드
//		iRegTracHis.setString("입출금결과출력계좌번호"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과출력계좌번호
//		iRegTracHis.setString("입출금결과기관명"              , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과기관명
//		iRegTracHis.setString("입출금결과계좌인자내용"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과계좌인자내용
//		iRegTracHis.setString("입출금결과계좌수취인성명"      , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과계좌수취인성명
//		iRegTracHis.setString("입출금결과개별저축은행명"      , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과개별저축은행명
//		iRegTracHis.setString("오픈뱅킹응답금융기관코드"      , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹응답금융기관코드
//		iRegTracHis.setString("오픈뱅킹참가기관응답구분코드"  , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹참가기관응답구분코드
//		iRegTracHis.setString("오픈뱅킹참가기관응답메시지내용", iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹참가기관응답메시지내용
//		iRegTracHis.setString("오픈뱅킹API응답구분코드"       , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹API응답구분코드
//		iRegTracHis.setString("오픈뱅킹API응답메시지내용"     , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹API응답메시지내용
		//iRegTracHis.setString("입출금이체응답거래일시"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금이체응답거래일시
		//iRegTracHis.setString("카드번호"                      , iRegTracHis.getString("OPNB_USR_UNO"));// 카드번호
		//iRegTracHis.setString("카드식별자"                    , iRegTracHis.getString("OPNB_USR_UNO"));// 카드식별자
		//iRegTracHis.setString("KB카드승인번호"                , iRegTracHis.getString("OPNB_USR_UNO"));// KB카드승인번호
		//iRegTracHis.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
		iRegTracHis.setString("시스템최초생성식별자", "UBF2030609");// 시스템최초생성식별자
		//iRegTracHis.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
		iRegTracHis.setString("시스템최종갱신식별자", "UBF2030609");// 시스템최종갱신식별자
		//iRegTracHis.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

		LLog.debug.println("EBC 입력값 출력 ----"); 
		LLog.debug.println(iRegTracHis);
		
		try {
			BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "regOpnbTracHis", iRegTracHis); //
		} catch (LException e) {
		   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체내역등록(regOpnbTracHis) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
		} 		
		// 입금이체 API호출
		LData iPrcMrvDmdAPICall = new LData(); //i입금이체API호출입력
        LData rPrcMrvDmdAPICall = new LData(); //r입금이체API호출결과
        
        LMultiData tmpInputInf = new LMultiData(); //입력값 Group
        LMultiData tmpRmtrInf = new LMultiData(); //결과값 출력 Group
        
		try {

			iPrcMrvDmdAPICall.setString("채널세부업무구분코드"  , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			iPrcMrvDmdAPICall.setString("cntr_account_type"     , input.getString("오픈뱅킹계좌계정구분코드")); //오픈뱅킹계좌계정구분코드
			iPrcMrvDmdAPICall.setString("cntr_account_num"      , input.getString("집금모계좌번호_V20")); //집금모계좌번호
			iPrcMrvDmdAPICall.setString("wd_pass_phrase"        , input.getString("입금이체용암호내용_V128")); //입금이체용암호내용
			iPrcMrvDmdAPICall.setString("wd_print_content"      , input.getString("출금계좌인자내용")); //출금계좌인자내용
			iPrcMrvDmdAPICall.setString("name_check_option"     , input.getString("수취인성명검증내용_V3")); //수취인성명검증내용
			iPrcMrvDmdAPICall.setString("sub_frnc_name"         , input.getString("하위가맹점명_V100")); //하위가맹점명      
	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점번호_V15"))) {
	        	iPrcMrvDmdAPICall.setString("sub_frnc_num"          , input.getString("하위가맹점번호_V15")); //하위가맹점번호
	        }
	        if(!StringUtil.trimNisEmpty(input.getString("하위가맹점사업자등록번호"))) {
	        	iPrcMrvDmdAPICall.setString("sub_frnc_business_num" , input.getString("하위가맹점사업자등록번호")); //하위가맹점 사업자등록번호
	        }
			iPrcMrvDmdAPICall.setString("tran_dtime"            , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시
			iPrcMrvDmdAPICall.setLong("req_cnt"                 , 1); //입금요청건수_N7
			
			LData tmpInput = new LData();
			
			tmpInput.setLong("tran_no"                      , myInput.getLong("거래순번_N2")); //거래순번_N2           
			tmpInput.setString("bank_tran_id"               , sPrcMrvDmd); //참가기관거래고유번호  
	        if(!StringUtil.trimNisEmpty(myInput.getString("입금계좌핀테크이용번호"))) {
	        	tmpInput.setString("fintech_use_num"            , myInput.getString("입금계좌핀테크이용번호")); //입금계좌핀테크이용번호
	        }
			tmpInput.setString("bank_code_std"              , myInput.getString("오픈뱅킹입금기관대표코드")); //오픈뱅킹입금기관대표코드     
			tmpInput.setString("account_num"                , myInput.getString("오픈뱅킹입금계좌번호")); //입금계좌번호_V16      
			//tmpInput.setString("account_num"                , CryptoDataUtil.decryptKey(myInput.getString("오픈뱅킹입금계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //입금계좌번호_V16      
	        if(!StringUtil.trimNisEmpty(myInput.getString("회차번호_V3"))) {
	        	tmpInput.setString("account_seq"                , myInput.getString("회차번호_V3")); //회차번호
	        }
			tmpInput.setString("account_holder_name"        , myInput.getString("입금계좌예금주명_V30")); //입금계좌예금주명_V30             
			tmpInput.setString("print_content"              , myInput.getString("입금계좌인자내용")); //입금계좌인자내용      
			tmpInput.setLong("tran_amt"                     , myInput.getLong("오픈뱅킹거래금액")); //거래금액_N12          
			tmpInput.setString("req_client_name"            , myInput.getString("입출금요청고객명_V50")); //입출금요청고객명      
			//tmpInput.setString("req_client_name"            , CryptoDataUtil.decryptKey(myInput.getString("입출금요청고객명_V50"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //입출금요청고객명      
			tmpInput.setString("req_client_bank_code"       , myInput.getString("요청고객계좌기관코드_V3")); //DMD_CST_ACC_INS_CD       
			tmpInput.setString("req_client_account_num"     , myInput.getString("요청고객계좌번호_V48")); //DMD_CST_ACNO      
			//tmpInput.setString("req_client_account_num"     , CryptoDataUtil.decryptKey(myInput.getString("요청고객계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //DMD_CST_ACNO      
	        if(!StringUtil.trimNisEmpty(myInput.getString("요청자핀테크이용번호_V24"))) {
	        	tmpInput.setString("req_client_fintech_use_num" , myInput.getString("요청자핀테크이용번호_V24")); //REQ_FINTECH_USE_NUM
	        }
			tmpInput.setString("req_client_num"             , myInput.getString("요청고객회원번호_V20")); //요청고객회원번호_V20
			tmpInput.setString("transfer_purpose"           , myInput.getString("이체용도구분코드_V2")); //이체용도구분코드      
	        if(!StringUtil.trimNisEmpty(myInput.getString("수취조회거래고유번호_V20"))) {
	        	tmpInput.setString("recv_bank_tran_id"          , myInput.getString("수취조회거래고유번호_V20")); //수취조회거래고유번호
	        }
			
	        if(!StringUtil.trimNisEmpty(myInput.getString("CMS번호_V32"))) {
	        	tmpInput.setString("cms_num"                    , myInput.getString("CMS번호_V32")); //CMS번호_V32
	        }
	        
	        // 기관거래내역테이블 추가 정보
	        tmpInput.setString("오픈뱅킹사용자고유번호", myInput.getString("오픈뱅킹사용자고유번호"));

			tmpInputInf.addLData(tmpInput); 
			
			iPrcMrvDmdAPICall.set("req_list", tmpInputInf);
			
	        LLog.debug.println("--- 입금이체 API호출 입력값 ----"); 
			LLog.debug.println(iPrcMrvDmdAPICall);
			
	        if (StringUtil.trimNisEmpty(tmpInput.getString("fintech_use_num"))) {
		        rPrcMrvDmdAPICall = opnbApi.tnsfMrvByAcnoAPICall(iPrcMrvDmdAPICall); //계좌번호 별 입금이체 API호출
	        } else {
		        rPrcMrvDmdAPICall = opnbApi.tnsfMrvByFntcUtzNoAPICall(iPrcMrvDmdAPICall); //핀테크이용번호 별 입금이체 API호출
	        }
			
			//오픈뱅킹으로부터 수신한 "응답코드(참가기관)"가 정상(000)이 아닌 경우에는 ‘이체 불능’으로 간주하되
			//요청거래가 이체인 경우 송금/결제/인증 용도의 경우 이체결과 체크 해야 함.
//			if (rPrcMrvDmdAPICall.getLMultiData("res_list").getLData(0).getString("bank_rsp_code").compareTo("000") != 0 &&
//				(input.getLMultiData("GRID").getLData(0).getString("TRAC_USG_DTCD").compareTo("TR") == 0 ||
//				input.getLMultiData("GRID").getLData(0).getString("TRAC_USG_DTCD").compareTo("ST") == 0 ||
//				input.getLMultiData("GRID").getLData(0).getString("TRAC_USG_DTCD").compareTo("AU") == 0)) {
//				LLog.debug.println("참가기관응답코드" + rPrcMrvDmdAPICall.getLMultiData("res_list").getLData(0).getString("bank_rsp_code"));
//				LLog.debug.println("이체용도구분코드" + input.getLMultiData("GRID").getLData(0).getString("TRAC_USG_DTCD"));
//
//				// 이체결과조회 API호출
//				LData iPrcTracRstAPICall = new LData(); //i이체결과조회API호출입력
//		        LData rPrcTracRstAPICall = new LData(); //r이체결과조회API호출결과
//		        
//		        LMultiData tracInputInf = new LMultiData(); //이체결과조회입력값 Group
//		        LMultiData tracRmtrInf = new LMultiData(); //이체결과조회결과값 출력 Group
//		        
//		        iPrcTracRstAPICall.setString("check_type"   , "2"); //1:출금이체, 2:입금이체
//		        iPrcTracRstAPICall.setString("tran_dtime"   , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시
//		        iPrcTracRstAPICall.setLong("req_cnt"        , 1); //요청건수_N7
//				
//				LData tracInput = new LData(); //이체결과조리 입력값 group Ldata
//				
//				tracInput.setLong("tran_no"               , input.getLMultiData("GRID").getLData(0).getLong("TNHS_SEQ")); //거래순번_N2           
//				tracInput.setString("org_bank_tran_id"    , sPrcMrvDmd); //참가기관거래고유번호  
//				tracInput.setString("org_bank_tran_date"  , DateUtil.getCurrentDate()); //입금계좌핀테크이용번호
//				tracInput.setLong("org_tran_amt"          , input.getLMultiData("GRID").getLData(0).getLong("OPNB_TR_AMT")); //거래금액_N12          
//
//				tracInputInf.addLData(tracInput); 
//				
//				iPrcTracRstAPICall.set("req_list", tracInputInf);
//				
//		        LLog.debug.println("--- 이체결과조회 API호출 입력값 ----"); 
//				LLog.debug.println(iPrcTracRstAPICall);
//				
//				rPrcTracRstAPICall = opnbApi.retvTracRstAPICall(iPrcTracRstAPICall); //이체결과조회 API호출
//				
//				if (rPrcTracRstAPICall.getLMultiData("res_list").getLData(0).getString("bank_rsp_code").compareTo("000") == 0) {
//					rPrcMrvDmdAPICall.clear();
//				}
//			}
	        
	        LData rOutPut = new LData();
	        
	        if (rPrcMrvDmdAPICall.getLMultiData("res_list").getDataCount() > 0) {
	        	rOutPut = rPrcMrvDmdAPICall.getLMultiData("res_list").getLData(0);
	        }
	        
	        
			// 이체요청 반영처리
			LData iUpdTracHis = new LData();

			iUpdTracHis.setString("오픈뱅킹전문거래년월일"        , DateUtil.getCurrentDate());// 오픈뱅킹전문거래년월일
			iUpdTracHis.setString("참가기관거래고유번호"          , sPrcMrvDmd);// 참가기관거래고유번호
//			iUpdTracHis.setString("채널세부업무구분코드"          , input.getString("CHN_DTLS_BWK_DTCD"));// 채널세부업무구분코드
			iUpdTracHis.setString("오픈뱅킹API거래고유번호"       , rPrcMrvDmdAPICall.getString("api_tran_id"));// 오픈뱅킹API거래고유번호
//			iUpdTracHis.setString("입출금구분코드"                , "1");                                  // 입출금구분코드 '1':입금, '2':출금
//			iUpdTracHis.setString("오픈뱅킹사용자고유번호"        , input.getLMultiData("GRID").getLData(0).getString("OPNB_USR_UNO"));// 오픈뱅킹사용자고유번호
//			iUpdTracHis.setString("오픈뱅킹이용기관계좌번호"      , input.getString("BPC_MTHR_ACNO"));// 집금모계좌번호
//			iUpdTracHis.setString("입출금금융기관코드"            , input.getLMultiData("GRID").getLData(0).getString("OPNB_MRV_INS_RPS_CD"));// 입출금금융기관코드
//			iUpdTracHis.setString("입출금계좌번호"                , input.getLMultiData("GRID").getLData(0).getString("MNRCVACNO"));// 고객계좌번호
//			iUpdTracHis.setString("계좌납입회차"                  , input.getLMultiData("GRID").getLData(0).getString("ACCOUNT_SEQ"));// 계좌납입회차
//			iUpdTracHis.setString("핀테크이용번호"                , input.getLMultiData("GRID").getLData(0).getString("MRV_ACC_FNTC_UTZ_NO"));// 핀테크이용번호
//			iUpdTracHis.setLong("입출금금액"                      , input.getLMultiData("GRID").getLData(0).getLong("OPNB_TR_AMT"));// 입출금금액
//			//iUpdTracHis.setString("이체수수료"                    , "");// 이체수수료
//			iUpdTracHis.setString("입출금계좌인자내용"            , input.getLMultiData("GRID").getLData(0).getString("MRV_ACC_PRT_CTT"));// 입출금계좌인자내용
//			iUpdTracHis.setString("입출금요청고객명"              , input.getLMultiData("GRID").getLData(0).getString("RCDW_DMD_CSNM"));// 입출금요청고객명
//			iUpdTracHis.setString("입출금거래일시"                , DateUtil.getCurrentTime("yyyyMMddHHmmss"));// 입출금거래일시
			iUpdTracHis.setString("입출금결과금융기관코드"        , rOutPut.getString("bank_code_std"));// 입출금결과금융기관코드
			iUpdTracHis.setString("입출금결과금융기관지점별코드"  , rOutPut.getString("bank_code_sub"));// 입출금결과금융기관지점별코드
			iUpdTracHis.setString("입출금결과출력계좌번호"        , rOutPut.getString("account_num_masked"));// 입출금결과출력계좌번호
			iUpdTracHis.setString("입출금결과기관명"              , rOutPut.getString("bank_name"));// 입출금결과기관명
			iUpdTracHis.setString("입출금결과계좌인자내용"        , rPrcMrvDmdAPICall.getString("wd_print_content"));// 입출금결과계좌인자내용
			iUpdTracHis.setString("입출금결과계좌수취인성명"      , rOutPut.getString("account_holder_name"));// 입출금결과계좌수취인성명
			//iUpdTracHis.setString("입출금결과계좌수취인성명"      , CryptoDataUtil.encryptKey(rOutPut.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));// 입출금결과계좌수취인성명
			iUpdTracHis.setString("입출금결과개별저축은행명"      , rOutPut.getString("savings_bank_name"));// 입출금결과개별저축은행명
			iUpdTracHis.setString("오픈뱅킹응답금융기관코드"      , rOutPut.getString("bank_code_tran"));// 오픈뱅킹응답금융기관코드
			iUpdTracHis.setString("오픈뱅킹참가기관응답구분코드"  , rOutPut.getString("bank_rsp_code"));// 오픈뱅킹참가기관응답구분코드
			iUpdTracHis.setString("오픈뱅킹참가기관응답메시지내용", rOutPut.getString("bank_rsp_message"));// 오픈뱅킹참가기관응답메시지내용
        	if(StringUtil.trimNisEmpty(rOutPut.getString("bank_rsp_message")) &&
        		!StringUtil.trimNisEmpty(rOutPut.getString("bank_rsp_code"))) {
        		iUpdTracHis.setString("오픈뱅킹참가기관응답메시지내용", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드",rOutPut.getString("bank_rsp_code")).getString("통합코드내용"));
        	}
			iUpdTracHis.setString("오픈뱅킹API응답구분코드"       , rPrcMrvDmdAPICall.getString("rsp_code"));// 오픈뱅킹API응답구분코드
			iUpdTracHis.setString("오픈뱅킹API응답메시지내용"     , rPrcMrvDmdAPICall.getString("rsp_message"));// 오픈뱅킹API응답메시지내용
			iUpdTracHis.setString("입출금이체응답거래일시"        , StringUtil.substring(rPrcMrvDmdAPICall.getString("api_tran_dtm"),0,14));// 입출금이체응답거래일시
//			iUpdTracHis.setString("카드번호"                      , iUpdTracHis.getString("OPNB_USR_UNO"));// 카드번호
//			iUpdTracHis.setString("카드식별자"                    , iUpdTracHis.getString("OPNB_USR_UNO"));// 카드식별자
//			iUpdTracHis.setString("KB카드승인번호"                , iUpdTracHis.getString("OPNB_USR_UNO"));// KB카드승인번호
//			iUpdTracHis.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
//			iUpdTracHis.setString("시스템최초생성식별자", "");// 시스템최초생성식별자
//			iUpdTracHis.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
			iUpdTracHis.setString("시스템최종갱신식별자"          , "UBF2030609");// 시스템최종갱신식별자
//			iUpdTracHis.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

			LLog.debug.println("이체내역 결과 수정 입력값 출력 ----"); 
			LLog.debug.println(iUpdTracHis);
			
			try {
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "uptOpnbTracHis", iUpdTracHis); //
			} catch (LException e) {
			   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체내역수정(uptOpnbTracHis) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
			} 		
			
			//무료이용정책 반영
			if(myInput.getString("이체용도구분코드_V2").compareTo("TR") == 0 &&
				!StringUtil.trimNisEmpty(myInput.getString("참가기관출금거래고유번호"))) {
				OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			    /** 무료거래등록 cpbi호출
		         * 입력
		         * 채널세부업무구분코드(필수)
		         * 오픈뱅킹사용자고유번호(필수)
		         * 오픈뱅킹전문거래년월일
		         * 참가기관출금거래고유번호
		         * 참가기관입금거래고유번호
		         * 
		         * 출력
		         * 처리결과_V1(Y.무료적용 N.미적용)  
		         **/
			    LData iCfreTrHis = new LData(); 
			    LData rCfreTrHis = new LData();
			    iCfreTrHis.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			    iCfreTrHis.setString("오픈뱅킹사용자고유번호"    , myInput.getString("오픈뱅킹사용자고유번호"));// 오픈뱅킹사용자고유번호
			    iCfreTrHis.setString("오픈뱅킹전문거래년월일"    , DateUtil.getCurrentDate());// 오픈뱅킹전문거래년월일
			    iCfreTrHis.setString("참가기관출금거래고유번호"  , myInput.getString("참가기관출금거래고유번호"));// 참가기관출금거래고유번호
			    iCfreTrHis.setString("참가기관입금거래고유번호"  , sPrcMrvDmd);// 참가기관입금거래고유번호
			    
			    rCfreTrHis = opnbCstMgCpbc.regtCfreTrHis(iCfreTrHis);
			    
			    LLog.debug.println("lht 무료이용정책적용여부"+ rCfreTrHis.getString("처리결과_V1")); 
			}
		    
	        //API 호출이후 처리
	        rPrcMrvDmd.setString("API거래고유번호_V40"  , rPrcMrvDmdAPICall.getString("api_tran_id"));//API거래고유번호       
	        rPrcMrvDmd.setString("API거래일시_V17"      , rPrcMrvDmdAPICall.getString("api_tran_dtm"));//API거래일시       
	        rPrcMrvDmd.setString("API응답코드_V5"       , rPrcMrvDmdAPICall.getString("rsp_code"));//API응답코드       
	        rPrcMrvDmd.setString("API응답메시지_V300"   , rPrcMrvDmdAPICall.getString("rsp_message"));//API응답메시지        
	        rPrcMrvDmd.setString("출금기관대표코드_V3"  , rPrcMrvDmdAPICall.getString("wd_bank_code_std"));//출금기관대표코드_V3  
	        rPrcMrvDmd.setString("출금기관지점별코드"   , rPrcMrvDmdAPICall.getString("wd_bank_code_sub"));//출금기관지점별코드
	        rPrcMrvDmd.setString("출금기관명_V20"       , rPrcMrvDmdAPICall.getString("wd_bank_name"));//출금기관명_V20            
	        rPrcMrvDmd.setString("출금출력계좌번호_V20"     , rPrcMrvDmdAPICall.getString("wd_account_num_masked"));//오픈뱅킹출금계좌번호                
	        //rPrcMrvDmd.setString("출금출력계좌번호_V20"     , CryptoDataUtil.encryptKey(rPrcMrvDmdAPICall.getString("wd_account_num_masked"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));//오픈뱅킹출금계좌번호                
	        rPrcMrvDmd.setString("출금계좌인자내용"     , rPrcMrvDmdAPICall.getString("wd_print_content"));//출금계좌인자내용                  
	        rPrcMrvDmd.setString("송금의뢰인명"       , rPrcMrvDmdAPICall.getString("wd_account_holder_name"));//송금인성명_V22              
	        //rPrcMrvDmd.setString("송금의뢰인명"       , CryptoDataUtil.encryptKey(rPrcMrvDmdAPICall.getString("wd_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));//송금인성명_V22              
	        rPrcMrvDmd.setString("입금건수_N9"          , rPrcMrvDmdAPICall.getString("res_cnt"));//입금건수_N9                  

        	LData tmpSelRec = new LData(); //출력 Group Buffer
        	
        	tmpSelRec.setString("거래순번_N2"              , rOutPut.getString("tran_no")); //거래순번
        	tmpSelRec.setString("참가기관거래고유번호_V20" , rOutPut.getString("bank_tran_id")); //참가기관거래고유번호
        	tmpSelRec.setString("참가기관거래일자_V8"      , rOutPut.getString("bank_tran_date")); //참가기관거래일자
        	tmpSelRec.setString("참가기관표준코드_V3"      , rOutPut.getString("bank_code_tran")); //참가기관표준코드_V3
        	tmpSelRec.setString("참가기관응답코드_V3"      , rOutPut.getString("bank_rsp_code")); //참가기관응답코드
        	tmpSelRec.setString("참가기관응답메시지_V100"  , rOutPut.getString("bank_rsp_message")); //참가기관응답메시지
        	if(StringUtil.trimNisEmpty(rOutPut.getString("bank_rsp_message")) &&
        		!StringUtil.trimNisEmpty(rOutPut.getString("bank_rsp_code"))) {
        		tmpSelRec.setString("참가기관응답메시지_V100", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드", rOutPut.getString("bank_rsp_code")).getString("통합코드내용"));
        	}
        	tmpSelRec.setString("입금계좌핀테크이용번호"   , rOutPut.getString("fintech_use_num")); //입금계좌핀테크이용번호(센터인증)
        	tmpSelRec.setString("계좌별명"                 , rOutPut.getString("account_alias")); //계좌별명(센터인증)
        	tmpSelRec.setString("입금기관대표코드_V3"      , rOutPut.getString("bank_code_std")); //입금기관대표코드_V3
        	tmpSelRec.setString("입금기관지점별코드_V7"    , rOutPut.getString("bank_code_sub")); //입금기관지점별코드_V7
        	tmpSelRec.setString("입금기관명_V20"           , rOutPut.getString("bank_name")); //입금기관명_V20
        	tmpSelRec.setString("입금개별저축은행명"       , rOutPut.getString("savings_bank_name")); //입금개별저축은행명
        	tmpSelRec.setString("오픈뱅킹입금계좌번호"       , rOutPut.getString("account_num")); //입금계좌번호IF_V48(자체인증)
        	//tmpSelRec.setString("오픈뱅킹입금계좌번호"       , CryptoDataUtil.encryptKey(rOutPut.getString("account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //입금계좌번호IF_V48(자체인증)
        	tmpSelRec.setString("회차번호_V3"              , rOutPut.getString("account_seq")); //회차번호(자체인증)
        	tmpSelRec.setString("입금출력계좌번호_V20"     , rOutPut.getString("account_num_masked")); //입금출력계좌번호_V20
        	tmpSelRec.setString("입금계좌인자내용"         , rOutPut.getString("print_content")); //입금계좌인자내용
        	tmpSelRec.setString("수취인명"             , rOutPut.getString("account_holder_name")); //수취인명_V48
        	//tmpSelRec.setString("수취인명"             , CryptoDataUtil.encryptKey(rOutPut.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //수취인명_V20
	        if (!StringUtil.isEmpty(rOutPut.getString("tran_amt"))) {
	        	tmpSelRec.setLong("오픈뱅킹거래금액"           , rOutPut.getLong("tran_amt")); //오픈뱅킹거래금액
	        } else {
	        	tmpSelRec.setLong("오픈뱅킹거래금액"           , 0); //오픈뱅킹거래금액
	        }
        	tmpSelRec.setString("CMS번호_V32"              , rOutPut.getString("cms_num")); //CMS번호_V32

        	tmpRmtrInf.addLData(tmpSelRec); //출력 Group 추가
		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		rPrcMrvDmd.setInt("그리드_cnt", rPrcMrvDmdAPICall.getLMultiData("res_list").getDataCount()); //그리드count
		//rPrcMrvDmd.setLong("GRID_cnt", tmpRmtrInf.getDataCount()); //그리드count
		rPrcMrvDmd.set("그리드", tmpRmtrInf); //그리드셋
        
		LLog.debug.println("OpnbTracSvcPbc.prcMrvDmd END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rPrcMrvDmd);
        
        return rPrcMrvDmd;
    }

    /**
     * - 출금계좌정보와 당사입금정보를 입력받아 출금처리.
     * 
     * 1. 출금요청 필수입력값 체크
     *     - 출금계좌번호 및 출금계좌인자내역, 요청고객계좌번호 또는 핀테크이용번호 등
     * 2. 출금계좌 등록여부 확인 및 수취조회 API 호출
     *     - 수취계좌의 입금가능여부 및 수취인성명 사전 조회
     * 3. 출금이체 API 호출
     * 4. 오픈뱅킹이체내역 생성
     * 
     * <관련테이블>
     * - UBF오픈뱅킹이체내역, UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세, UBF오픈뱅킹기관거래내역
     * 
     * <INPUT>
     * - 약정계정/계좌구분, 약정계정/계좌번호, 입금계좌인자내역, 거래금액, 요청일시, 오픈뱅킹사용자고유번호,  이체용도
     * 
     * <OUTPUT>
     * - 거래고유번호(참가기관),출금한도잔여금액,출금계좌핀테크이용번호,응답코드, 응답메시지, 참가기관응답코드, 참가기관응답메시지
     * 
     * @serviceID UBF0700800
     * @logicalName 출금요청처리
     * @method prcOdwDmd
     * @method(한글명) 출금요청처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcOdwDmd(LData input) throws LException {
        LLog.debug.println("OpnbTracSvcPbc.prcOdwDmd START ☆★☆☆★☆☆★☆");
		
        LData rPrcOdwDmd = new LData(); //출금요청처리 결과값 리턴
        
		LLog.debug.println("출금요청처리 입력값 출력 ----"); 
		LLog.debug.println(input);
		
		//Validation Check
		if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹계좌계정구분코드"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹계좌계정구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹계좌계정구분코드" ));//오픈뱅킹계좌계정구분코드가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("집금모계좌번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("집금모계좌번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-집금모계좌번호" ));//집금모계좌번호가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("입금계좌인자내용"))) {
			LLog.debug.println("로그 " + input.getString("입금계좌인자내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입금계좌인자내용" ));//입금계좌인자내용이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("출금계좌핀테크이용번호"))) {      //출금계좌핀테크이용번호
			if(StringUtil.trimNisEmpty(input.getString("출금기관대표코드_V3")) ||       //출금기관대표코드
				StringUtil.trimNisEmpty(input.getString("오픈뱅킹출금계좌번호"))) {            //출금계좌번호
				throw new LException("출금계좌핀테크번호 또는 출금기관코드와 계좌번호를 입력하세요."); 
			}
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹거래금액"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹거래금액"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹거래금액" ));//오픈뱅킹거래금액이 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("입출금요청고객명_V50"))) {
			LLog.debug.println("로그 " + input.getString("입출금요청고객명_V50"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-입출금요청고객명_V50" ));//입출금요청고객명_V50이 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) &&
			!StringUtil.trimNisEmpty(input.getString("CI내용"))) {
	    	LData iRetvUsrUnoP = new LData(); // i사용자고유번호조회입력
	        LData rRetvUsrUnoP = new LData(); // r사용자고유번호조회출력
	    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	    	iRetvUsrUnoP.setString("CI내용", input.getString("CI내용"));
	    	rRetvUsrUnoP = opnbCstMgCpbc.retvUsrUno(iRetvUsrUnoP);
	    	input.setString("오픈뱅킹사용자고유번호",rRetvUsrUnoP.getString("오픈뱅킹사용자고유번호"));
		}
				 
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("요청고객회원번호_V20"))) {
			input.setString("요청고객회원번호_V20", input.getString("오픈뱅킹사용자고유번호"));
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("요청고객회원번호_V20"))) {
			LLog.debug.println("로그 " + input.getString("요청고객회원번호_V20"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-요청고객회원번호_V20" ));//요청고객회원번호_V20가 존재하지 않습니다.
		}
			 
		if(StringUtil.trimNisEmpty(input.getString("이체용도구분코드_V2"))) {
			LLog.debug.println("로그 " + input.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-이체용도구분코드_V2" ));//이체용도구분코드_V2가 존재하지 않습니다.
		}
		
//		카드 바로출금	    01	신용카드 바로출금 시	                                    결제	ST
//		카드론 바로출금	    02	카드론 바로출금 시	                                        결제	ST
//		할부금융 바로출금	03	할부금융 바로출금 시	                                    결제	ST
//		송금	            04	계좌간 송금을 오픈뱅킹 출금/입금 API 활용	                송금	TR
//		연락처 송금	        05	연락처 송금을 활용 한 오픈뱅킹 출금/입금 API 활용	        송금	TR
//		계좌 결제	        06	KB Pay 계좌 결제에서 오픈뱅킹 출금API 활용	                결제	ST
//		포인트 충전	        07	포인트 충전 시 오픈뱅킹 출금API 활용	                    충전	RC
//		바코드 결제	        08	바코드 결제  중 자동 충전 시 오픈뱅킹 출금API 활용	        결제	ST
//		자동충전 결제	    09	리브메이트 內 결제 시 자동 충전 발생을 오픈뱅킹 출금API 활용	결제	ST
//		금융상품	        10	금융상품과 연계된 오픈뱅킹 출금API 활용 	                출금	WD
		
		if(input.getString("이체용도구분코드_V2").compareTo("TR") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("ST") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("RC") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("WD") != 0 &&
			input.getString("이체용도구분코드_V2").compareTo("EX") != 0) {
			LLog.debug.println("로그 " + input.getString("이체용도구분코드_V2"));
			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("이체용도구분코드_V2 ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
		}
		
		//이체용도 필드값이 송금(“TR”) 및 결제(“ST”)인 경우 해당 필드 값을 설정
		if(input.getString("이체용도구분코드_V2").compareTo("TR") == 0 ||
			input.getString("이체용도구분코드_V2").compareTo("ST") == 0) {
			if(StringUtil.trimNisEmpty(input.getString("최종수취고객성명_V20"))) {
				LLog.debug.println("로그 " + input.getString("최종수취고객성명_V20"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-최종수취고객성명_V20" ));//최종수취고객성명_V20가 존재하지 않습니다.
			}
			if(StringUtil.trimNisEmpty(input.getString("최종수취고객계좌기관코드_V3"))) {
				LLog.debug.println("로그 " + input.getString("최종수취고객계좌기관코드_V3"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-최종수취고객계좌기관코드_V3" ));//최종수취고객계좌기관코드_V3가 존재하지 않습니다.
			}
			if(StringUtil.trimNisEmpty(input.getString("최종수취고객계좌번호_V48"))) {
				LLog.debug.println("로그 " + input.getString("최종수취고객계좌번호_V48"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-최종수취고객계좌번호_V48" ));//최종수취고객계좌번호_V48가 존재하지 않습니다.
			}
		}
			
		//자금세탁방지 정보 수집을 위해 해당 거래를 요청한 고객의 정보를 세팅
        if (StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
        	if (StringUtil.trimNisEmpty(input.getString("요청고객계좌기관코드_V3")) ||
        		StringUtil.trimNisEmpty(input.getString("요청고객계좌번호_V48"))) {
    			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("요청고객계좌번호 및 요청고객계좌기관코드를 입력하세요. ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
        	}
        }
        
        //둘 다 세팅할 경우 포맷오류로 응답
        if (!StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
        	if (!StringUtil.trimNisEmpty(input.getString("요청고객계좌기관코드_V3")) ||
        		!StringUtil.trimNisEmpty(input.getString("요청고객계좌번호_V48"))) {
    			throw new LBizException(ObsErrCode.ERR_7011.getCode(), StringUtil.mergeStr("요청고객핀테크번호 입력시에는 요청고객계좌번호 및 계좌개설기관코드 중복입력은 불가합니다. ", ObsErrCode.ERR_7011.getName()));//입력값을 확인바랍니다.
        	}
        }
		// 전문거래고유번호 생성 Cpbc 호출
		LData iCdMg = new LData();
		
        OpnbApiCpbc opnbApi = new OpnbApiCpbc(); //공통API호출
        
		OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc(); //거래고유번호 채번 호출
		
		// 이체요청등록처리(require new처리 필요)
		LData iRegTracHis = new LData();

		String sPrcOdwDmd = opnbCdMg.crtTrUno(iCdMg).getString("거래고유번호"); //출금이체 거래고유번호
		
		LLog.debug.println("lht 출금이체 거래고유번호 = " + sPrcOdwDmd); 
		
		iRegTracHis.setString("오픈뱅킹전문거래년월일"        , DateUtil.getCurrentDate());// 오픈뱅킹전문거래년월일
		iRegTracHis.setString("참가기관거래고유번호"          , sPrcOdwDmd);// 참가기관거래고유번호
		iRegTracHis.setString("채널세부업무구분코드"          , input.getString("채널세부업무구분코드"));// 채널세부업무구분코드
		iRegTracHis.setString("오픈뱅킹이체거래고유번호"      , input.getString("오픈뱅킹이체거래고유번호"));// 오픈뱅킹이체거래고유번호
		//iRegTracHis.setString("오픈뱅킹API거래고유번호"       , "");// 오픈뱅킹API거래고유번호
		iRegTracHis.setString("입출금구분코드"                , "2");                                  // 입출금구분코드 '1':입금, '2':출금
		iRegTracHis.setString("오픈뱅킹이체용도업무구분코드"  , input.getString("오픈뱅킹이체용도업무구분코드"));// 오픈뱅킹이체용도업무구분코드
		iRegTracHis.setString("오픈뱅킹사용자고유번호"        , input.getString("오픈뱅킹사용자고유번호"));// 오픈뱅킹사용자고유번호
		iRegTracHis.setString("오픈뱅킹이용기관계좌번호"      , input.getString("집금모계좌번호_V20"));// 집금모계좌번호
		iRegTracHis.setString("입출금금융기관코드"            , input.getString("출금기관대표코드_V3"));// 입출금금융기관코드
		iRegTracHis.setString("입출금계좌번호"                , input.getString("오픈뱅킹출금계좌번호"));// 출금계좌번호
//		iRegTracHis.setString("계좌납입회차"                  , input.getString("ACCOUNT_SEQ"));// 계좌납입회차
		iRegTracHis.setString("핀테크이용번호"                , input.getString("출금계좌핀테크이용번호"));// 핀테크이용번호
		iRegTracHis.setLong("입출금금액"                      , input.getLong("오픈뱅킹거래금액"));// 오픈뱅킹거래금액
		iRegTracHis.setLong("이체수수료"                      , 0);// 이체수수료
		iRegTracHis.setString("입출금계좌인자내용"            , input.getString("출금계좌인자내용"));// 입출금계좌인자내용
		iRegTracHis.setString("입출금요청고객명"              , input.getString("입출금요청고객명_V50"));// 입출금요청고객명
		iRegTracHis.setString("입출금거래일시"                , DateUtil.getCurrentTime("yyyyMMddHHmmss"));// 입출금거래일시
//		iRegTracHis.setString("입출금결과금융기관코드"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과금융기관코드
//		iRegTracHis.setString("입출금결과금융기관지점별코드"  , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과금융기관지점별코드
//		iRegTracHis.setString("입출금결과출력계좌번호"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과출력계좌번호
//		iRegTracHis.setString("입출금결과기관명"              , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과기관명
//		iRegTracHis.setString("입출금결과계좌인자내용"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과계좌인자내용
		iRegTracHis.setString("입출금결과계좌수취인성명"      , input.getString("최종수취고객성명_V20"));// 입출금결과계좌수취인성명
//		iRegTracHis.setString("입출금결과개별저축은행명"      , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금결과개별저축은행명
//		iRegTracHis.setString("오픈뱅킹응답금융기관코드"      , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹응답금융기관코드
//		iRegTracHis.setString("오픈뱅킹참가기관응답구분코드"  , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹참가기관응답구분코드
//		iRegTracHis.setString("오픈뱅킹참가기관응답메시지내용", iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹참가기관응답메시지내용
//		iRegTracHis.setString("오픈뱅킹API응답구분코드"       , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹API응답구분코드
//		iRegTracHis.setString("오픈뱅킹API응답메시지내용"     , iRegTracHis.getString("OPNB_USR_UNO"));// 오픈뱅킹API응답메시지내용
		//iRegTracHis.setString("입출금이체응답거래일시"        , iRegTracHis.getString("OPNB_USR_UNO"));// 입출금이체응답거래일시
		//iRegTracHis.setString("카드번호"                      , iRegTracHis.getString("OPNB_USR_UNO"));// 카드번호
		//iRegTracHis.setString("카드식별자"                    , iRegTracHis.getString("OPNB_USR_UNO"));// 카드식별자
		//iRegTracHis.setString("KB카드승인번호"                , iRegTracHis.getString("OPNB_USR_UNO"));// KB카드승인번호
		//iRegTracHis.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
		iRegTracHis.setString("시스템최초생성식별자", "UBF2030610");// 시스템최초생성식별자
		//iRegTracHis.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
		iRegTracHis.setString("시스템최종갱신식별자", "UBF2030610");// 시스템최종갱신식별자
		//iRegTracHis.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

		LLog.debug.println("이체내역등록EBC 입력값 출력 ----"); 
		LLog.debug.println(iRegTracHis);
		
		try {
			BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "regOpnbTracHis", iRegTracHis); //
		} catch (LException e) {
		   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체내역등록(regOpnbTracHis) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
		} 		
		
		// 출금이체 API호출
		LData iPrcOdwDmdAPICall = new LData(); //i출금이체API호출입력
        LData rPrcOdwDmdAPICall = new LData(); //r출금이체API호출결과
        
		try {

			iPrcOdwDmdAPICall.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
			iPrcOdwDmdAPICall.setString("bank_tran_id"               , sPrcOdwDmd); //참가기관거래고유번호  
			iPrcOdwDmdAPICall.setString("cntr_account_type"          , input.getString("오픈뱅킹계좌계정구분코드")); //오픈뱅킹계좌계정구분코드
			iPrcOdwDmdAPICall.setString("cntr_account_num"           , input.getString("집금모계좌번호_V20")); //집금모계좌번호_V20
			iPrcOdwDmdAPICall.setString("dps_print_content"          , input.getString("입금계좌인자내용")); //입금계좌인자내용      
			iPrcOdwDmdAPICall.setString("fintech_use_num"            , input.getString("출금계좌핀테크이용번호")); //출금계좌핀테크이용번호(센터인증)
			iPrcOdwDmdAPICall.setString("wd_print_content"           , input.getString("출금계좌인자내용")); //출금계좌인자내용
			iPrcOdwDmdAPICall.setString("wd_bank_code_std"           , input.getString("출금기관대표코드_V3")); //출금기관대표코드_V3(자체인증)
			iPrcOdwDmdAPICall.setString("wd_account_num"             , input.getString("오픈뱅킹출금계좌번호")); //출금계좌번호_V48(자체인증)
			//iPrcOdwDmdAPICall.setString("wd_account_num"             , CryptoDataUtil.decryptKey(input.getString("오픈뱅킹출금계좌번호"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //출금계좌번호_V48(자체인증)
			iPrcOdwDmdAPICall.setLong("tran_amt"                     , input.getLong("오픈뱅킹거래금액")); //거래금액_N12          
			iPrcOdwDmdAPICall.setString("user_seq_no"                , input.getString("오픈뱅킹사용자고유번호")); //이용자고유번호
			iPrcOdwDmdAPICall.setString("tran_dtime"                 , DateUtil.getCurrentTime("yyyyMMddHHmmss"));//요청일시
			iPrcOdwDmdAPICall.setString("req_client_name"            , input.getString("입출금요청고객명_V50")); //입출금요청고객명      
			//iPrcOdwDmdAPICall.setString("req_client_name"            , CryptoDataUtil.decryptKey(input.getString("입출금요청고객명_V50"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //입출금요청고객명      
			iPrcOdwDmdAPICall.setString("req_client_bank_code"       , input.getString("요청고객계좌기관코드_V3")); //요청고객계좌개설은행코드      
			iPrcOdwDmdAPICall.setString("req_client_account_num"     , input.getString("요청고객계좌번호_V48")); //요청고객고객계좌번호_V48      
			//iPrcOdwDmdAPICall.setString("req_client_account_num"     , CryptoDataUtil.decryptKey(input.getString("요청고객계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //요청고객고객계좌번호_V48      
			if (!StringUtil.trimNisEmpty(input.getString("요청자핀테크이용번호_V24"))) {
				iPrcOdwDmdAPICall.setString("req_client_fintech_use_num" , input.getString("요청자핀테크이용번호_V24")); //요청고객핀테크이용번호
			}
			iPrcOdwDmdAPICall.setString("req_client_num"             , input.getString("요청고객회원번호_V20")); //요청고객회원번호
			iPrcOdwDmdAPICall.setString("transfer_purpose"           , input.getString("이체용도구분코드_V2")); //이체용도구분코드      
			iPrcOdwDmdAPICall.setString("sub_frnc_name"              , input.getString("하위가맹점명_V100")); //하위가맹점명      
			if (!StringUtil.trimNisEmpty(input.getString("하위가맹점번호_V15"))) {
				iPrcOdwDmdAPICall.setString("sub_frnc_num"               , input.getString("하위가맹점번호_V15")); //하위가맹점번호
			}
			if (!StringUtil.trimNisEmpty(input.getString("하위가맹점사업자등록번호"))) {
				iPrcOdwDmdAPICall.setString("sub_frnc_business_num"      , input.getString("하위가맹점사업자등록번호")); //하위가맹점 사업자등록번호
			}
			iPrcOdwDmdAPICall.setString("recv_client_name"           , input.getString("최종수취고객성명_V20")); //최종수취고객명
			if (!StringUtil.trimNisEmpty(input.getString("최종수취고객계좌기관코드_V3"))) {
				iPrcOdwDmdAPICall.setString("recv_client_bank_code"      , input.getString("최종수취고객계좌기관코드_V3")); //최종수취계좌개설기관코드
			}
			if (!StringUtil.trimNisEmpty(input.getString("최종수취고객계좌번호_V48"))) {
				iPrcOdwDmdAPICall.setString("recv_client_account_num"    , input.getString("최종수취고객계좌번호_V48")); //최종수취계좌번호
				//iPrcOdwDmdAPICall.setString("recv_client_account_num"    , CryptoDataUtil.decryptKey(input.getString("최종수취고객계좌번호_V48"), CryptoDataUtil.KB_BD_NORMAL_KEY)); //최종수취계좌번호
			}

	        LLog.debug.println("--- 출금이체 API호출 입력값 ----"); 
			LLog.debug.println(iPrcOdwDmdAPICall);
			
	        if (StringUtil.trimNisEmpty(iPrcOdwDmdAPICall.getString("fintech_use_num"))) { 
		        rPrcOdwDmdAPICall = opnbApi.tnsfOdwByAcnoAPICall(iPrcOdwDmdAPICall); //계좌번호 별 입금이체 API호출
	        } else {
		        rPrcOdwDmdAPICall = opnbApi.tnsfOdwByFntcUtzNoAPICall(iPrcOdwDmdAPICall); //핀테크이용번호 별 입금이체 API호출
	        }
			
			//오픈뱅킹으로부터 수신한 "응답코드(참가기관)"가 정상(000)이 아닌 경우에는 ‘이체 불능’으로 간주하되
			//요청거래가 이체인 경우 송금/결제/충전/출금/환전 용도의 경우 이체결과 체크 해야 함.
//			if (rPrcOdwDmdAPICall.getString("bank_rsp_code").compareTo("000") != 0 &&
//				(input.getString("TRAC_USG_DTCD").compareTo("TR") == 0 ||
//				input.getString("TRAC_USG_DTCD").compareTo("ST") == 0 ||
//				input.getString("TRAC_USG_DTCD").compareTo("RC") == 0 ||
//				input.getString("TRAC_USG_DTCD").compareTo("WD") == 0 ||
//				input.getString("TRAC_USG_DTCD").compareTo("EX") == 0)) {
//				//이체결과조회 후 리턴값 변경 필요.
//				LLog.debug.println("참가기관응답코드" + rPrcOdwDmdAPICall.getString("bank_rsp_code"));
//				LLog.debug.println("이체용도구분코드" + input.getString("TRAC_USG_DTCD"));
//			}
	        
			// 이체요청 반영처리 정상여부와 관계없이 변경처리.
			LData iUpdTracHis = new LData();

			iUpdTracHis.setString("오픈뱅킹전문거래년월일"        , DateUtil.getCurrentDate());// 오픈뱅킹전문거래년월일
			iUpdTracHis.setString("참가기관거래고유번호"          , sPrcOdwDmd);// 참가기관거래고유번호
//			iUpdTracHis.setString("채널세부업무구분코드"          , input.getString("CHN_DTLS_BWK_DTCD"));// 채널세부업무구분코드
			iUpdTracHis.setString("오픈뱅킹API거래고유번호"       , rPrcOdwDmdAPICall.getString("api_tran_id"));// 오픈뱅킹API거래고유번호
//			iUpdTracHis.setString("입출금구분코드"                , "2");                                  // 입출금구분코드 '1':입금, '2':출금
//			iUpdTracHis.setString("오픈뱅킹사용자고유번호"        , input.getString("OPNB_USR_UNO"));// 오픈뱅킹사용자고유번호
//			iUpdTracHis.setString("오픈뱅킹이용기관계좌번호"      , input.getString("BPC_MTHR_ACNO"));// 집금모계좌번호
//			iUpdTracHis.setString("입출금금융기관코드"            , input.getString("ODW_INS_RPS_CD"));// 입출금금융기관코드(자체인증)
//			iUpdTracHis.setString("입출금계좌번호"                , input.getString("ODW_ACNO"));// 출금계좌번호(자체인증)
//			iUpdTracHis.setString("계좌납입회차"                  , input.getString("ACCOUNT_SEQ"));// 계좌납입회차(출금이체시에는 값없음)
//			iUpdTracHis.setString("핀테크이용번호"                , input.getString("ODW_ACC_FNTC_UTZ_NO"));// 핀테크이용번호(센터인증)
//			iUpdTracHis.setLong("입출금금액"                      , input.getLong("OPNB_TR_AMT"));// 입출금금액
//			//iUpdTracHis.setString("이체수수료"                    , "");// 이체수수료
//			iUpdTracHis.setString("입출금계좌인자내용"            , input.getString("ODW_ACC_PRT_CTT"));// 입출금계좌인자내용
//			iUpdTracHis.setString("입출금요청고객명"              , input.getString("RCDW_DMD_CSNM"));// 입출금요청고객명
//			iUpdTracHis.setString("입출금거래일시"                , DateUtil.getCurrentTime("yyyyMMddHHmmss"));// 입출금거래일시
			iUpdTracHis.setString("입출금결과금융기관코드"        , rPrcOdwDmdAPICall.getString("bank_code_std"));// 입출금결과금융기관코드
			iUpdTracHis.setString("입출금결과금융기관지점별코드"  , rPrcOdwDmdAPICall.getString("bank_code_sub"));// 입출금결과금융기관지점별코드
			iUpdTracHis.setString("입출금결과출력계좌번호"        , rPrcOdwDmdAPICall.getString("account_num_masked"));// 입출금결과출력계좌번호
			iUpdTracHis.setString("입출금결과기관명"              , rPrcOdwDmdAPICall.getString("bank_name"));// 입출금결과기관명
			iUpdTracHis.setString("입출금결과계좌인자내용"        , rPrcOdwDmdAPICall.getString("wd_print_content"));// 입출금결과계좌인자내용
			iUpdTracHis.setString("입출금결과계좌수취인성명"      , rPrcOdwDmdAPICall.getString("account_holder_name"));// 입출금결과계좌수취인성명
			//iUpdTracHis.setString("입출금결과계좌수취인성명"      , CryptoDataUtil.encryptKey(rPrcOdwDmdAPICall.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));// 입출금결과계좌수취인성명
			iUpdTracHis.setString("입출금결과개별저축은행명"      , rPrcOdwDmdAPICall.getString("savings_bank_name"));// 입출금결과개별저축은행명
			iUpdTracHis.setString("오픈뱅킹응답금융기관코드"      , rPrcOdwDmdAPICall.getString("bank_code_tran"));// 오픈뱅킹응답금융기관코드
			iUpdTracHis.setString("오픈뱅킹참가기관응답구분코드"  , rPrcOdwDmdAPICall.getString("bank_rsp_code"));// 오픈뱅킹참가기관응답구분코드
			iUpdTracHis.setString("오픈뱅킹참가기관응답메시지내용", rPrcOdwDmdAPICall.getString("bank_rsp_message"));// 오픈뱅킹참가기관응답메시지내용
        	if(StringUtil.trimNisEmpty(rPrcOdwDmdAPICall.getString("bank_rsp_message")) &&
        		!StringUtil.trimNisEmpty(rPrcOdwDmdAPICall.getString("bank_rsp_code"))) {
        		iUpdTracHis.setString("오픈뱅킹참가기관응답메시지내용", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드",rPrcOdwDmdAPICall.getString("bank_rsp_code")).getString("통합코드내용"));
        	}
			iUpdTracHis.setString("오픈뱅킹API응답구분코드"       , rPrcOdwDmdAPICall.getString("rsp_code"));// 오픈뱅킹API응답구분코드
			iUpdTracHis.setString("오픈뱅킹API응답메시지내용"     , rPrcOdwDmdAPICall.getString("rsp_message"));// 오픈뱅킹API응답메시지내용
			iUpdTracHis.setString("입출금이체응답거래일시"        , StringUtil.substring(rPrcOdwDmdAPICall.getString("api_tran_dtm"),0,14));// 입출금이체응답거래일시
//			iUpdTracHis.setString("카드번호"                      , iUpdTracHis.getString("OPNB_USR_UNO"));// 카드번호
//			iUpdTracHis.setString("카드식별자"                    , iUpdTracHis.getString("OPNB_USR_UNO"));// 카드식별자
//			iUpdTracHis.setString("KB카드승인번호"                , iUpdTracHis.getString("OPNB_USR_UNO"));// KB카드승인번호
//			iUpdTracHis.setString("시스템최초생성일시", TIMESTAMP));// 시스템최초생성일시
//			iUpdTracHis.setString("시스템최초생성식별자", "");// 시스템최초생성식별자
//			iUpdTracHis.setString("시스템최종갱신일시", TIMESTAMP);// 시스템최종갱신일시
			iUpdTracHis.setString("시스템최종갱신식별자", "UBF2030610");// 시스템최종갱신식별자
//			iUpdTracHis.setString("시스템최종거래일시", TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'));// 시스템최종거래일시

			LLog.debug.println("이체내역 결과 수정 입력값 출력 ----"); 
			LLog.debug.println(iUpdTracHis);
			
			try {
				BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbTracSvcEbc", "uptOpnbTracHis", iUpdTracHis); //
			} catch (LException e) {
			   throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "이체내역수정(uptOpnbTracHis) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
			} 		
			
	        //API 호출이후 처리
	        rPrcOdwDmd.setString("API거래고유번호_V40"      , rPrcOdwDmdAPICall.getString("api_tran_id"));//API거래고유번호       
	        rPrcOdwDmd.setString("API거래일시_V17"          , rPrcOdwDmdAPICall.getString("api_tran_dtm"));//API거래일시       
	        rPrcOdwDmd.setString("API응답코드_V5"           , rPrcOdwDmdAPICall.getString("rsp_code"));//API응답코드       
	        rPrcOdwDmd.setString("API응답메시지_V300"       , rPrcOdwDmdAPICall.getString("rsp_message"));//API응답메시지        
	        rPrcOdwDmd.setString("입금기관대표코드_V3"      , rPrcOdwDmdAPICall.getString("dps_bank_code_std"));//입금기관대표코드_V3  
	        rPrcOdwDmd.setString("입금기관지점별코드_V7"    , rPrcOdwDmdAPICall.getString("dps_bank_code_sub"));//입금기관지점별코드
	        rPrcOdwDmd.setString("입금기관명_V20"           , rPrcOdwDmdAPICall.getString("dps_bank_name"));//입금기관명_V20            
	        rPrcOdwDmd.setString("입금출력계좌번호_V20"     , rPrcOdwDmdAPICall.getString("dps_account_num_masked"));//입금계좌번호(출력용)                
	        rPrcOdwDmd.setString("입금계좌인자내용"         , rPrcOdwDmdAPICall.getString("dps_print_content"));//입금계좌인자내용                  
	        rPrcOdwDmd.setString("수취인명"             , rPrcOdwDmdAPICall.getString("dps_account_holder_name"));//수취인성명_V48                 
	        //rPrcOdwDmd.setString("수취인명"             , CryptoDataUtil.encryptKey(rPrcOdwDmdAPICall.getString("dps_account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY));//수취인성명_V48                 
	        rPrcOdwDmd.setString("참가기관거래고유번호_V20" , rPrcOdwDmdAPICall.getString("bank_tran_id")); //참가기관거래고유번호
	        rPrcOdwDmd.setString("참가기관거래일자_V8"      , rPrcOdwDmdAPICall.getString("bank_tran_date")); //참가기관거래일자
	        rPrcOdwDmd.setString("참가기관표준코드_V3"      , rPrcOdwDmdAPICall.getString("bank_code_tran")); //참가기관표준코드_V3
	        rPrcOdwDmd.setString("참가기관응답코드_V3"      , rPrcOdwDmdAPICall.getString("bank_rsp_code")); //참가기관응답코드
	        rPrcOdwDmd.setString("참가기관응답메시지_V100"  , rPrcOdwDmdAPICall.getString("bank_rsp_message")); //참가기관응답메시지
        	if(StringUtil.trimNisEmpty(rPrcOdwDmdAPICall.getString("bank_rsp_message")) &&
        		!StringUtil.trimNisEmpty(rPrcOdwDmdAPICall.getString("bank_rsp_code"))) {
        		rPrcOdwDmd.setString("참가기관응답메시지_V100", opnbCdMg.retvCdInf("오픈뱅킹참가기관응답구분코드",rPrcOdwDmdAPICall.getString("bank_rsp_code")).getString("통합코드내용"));
        	}
	        rPrcOdwDmd.setString("출금계좌핀테크이용번호"   , rPrcOdwDmdAPICall.getString("fintech_use_num")); //출금계좌핀테크이용번호(센터인증)
	        rPrcOdwDmd.setString("오픈뱅킹출금계좌번호"         , rPrcOdwDmdAPICall.getString("account_num")); //출금계좌번호(자체인증)
	        //rPrcOdwDmd.setString("오픈뱅킹출금계좌번호"         , CryptoDataUtil.encryptKey(rPrcOdwDmdAPICall.getString("account_num"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //출금계좌번호(자체인증)
	        rPrcOdwDmd.setString("계좌별명"                 , rPrcOdwDmdAPICall.getString("account_alias")); //계좌별명
	        rPrcOdwDmd.setString("출금기관대표코드_V3"      , rPrcOdwDmdAPICall.getString("bank_code_std")); //입출금기관대표코드
	        rPrcOdwDmd.setString("출금기관지점별코드"       , rPrcOdwDmdAPICall.getString("bank_code_sub")); //입출금기관지점별코드_V7
	        rPrcOdwDmd.setString("출금기관명_V20"           , rPrcOdwDmdAPICall.getString("bank_name")); //입출금기관명_V20
	        rPrcOdwDmd.setString("출금개별저축은행명"       , rPrcOdwDmdAPICall.getString("savings_bank_name")); //출금개별저축은행명
	        rPrcOdwDmd.setString("출금출력계좌번호_V20"     , rPrcOdwDmdAPICall.getString("account_num_masked")); //출금계좌번호(출력용)
	        rPrcOdwDmd.setString("출금계좌인자내용"         , rPrcOdwDmdAPICall.getString("print_content")); //출금계좌인자내용
	        rPrcOdwDmd.setString("송금의뢰인명"           , rPrcOdwDmdAPICall.getString("account_holder_name")); //송금인명_V20
	        //rPrcOdwDmd.setString("송금의뢰인명"           , CryptoDataUtil.encryptKey(rPrcOdwDmdAPICall.getString("account_holder_name"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //송금인명_V20
	        if (!StringUtil.isEmpty(rPrcOdwDmdAPICall.getString("tran_amt"))) {
		        rPrcOdwDmd.setLong("오픈뱅킹거래금액"           , rPrcOdwDmdAPICall.getLong("tran_amt")); //오픈뱅킹거래금액
	        } else {
	        	rPrcOdwDmd.setLong("오픈뱅킹거래금액"           , 0); //오픈뱅킹거래금액
	        }
	        if (!StringUtil.isEmpty(rPrcOdwDmdAPICall.getString("wd_limit_remain_amt"))) {
		        rPrcOdwDmd.setLong("출금잔여금액_N15"           , rPrcOdwDmdAPICall.getLong("wd_limit_remain_amt")); //출금한도잔여금액
	        } else {
		        rPrcOdwDmd.setLong("출금잔여금액_N15"           , 0); //출금한도잔여금액
	        }

		} catch ( LException e) {
			e.printStackTrace(LLog.err); 
			
//			rIssueTkenP = param_result( rRegtOpnbApiUsrP );
//			
//			rIssueTkenP.setString("응답코드", UBE_CONST.REP_CD_ETC_PRC_ERR);//656: 기타처리불가
//			rIssueTkenP.setString("에러메시지", UBE_CONST.REP_CD_MSG_ETC_PRC_ERR); //기타 처리불가(해당 지점 연락요망)
		}

		LLog.debug.println("OpnbTracSvcPbc.prcOdwDmd END ☆★☆☆★☆☆★☆");
		LLog.debug.println(rPrcOdwDmd);
        
        return rPrcOdwDmd;
    }

}

