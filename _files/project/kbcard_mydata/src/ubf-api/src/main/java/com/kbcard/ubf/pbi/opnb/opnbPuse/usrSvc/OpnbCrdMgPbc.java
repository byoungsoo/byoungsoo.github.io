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
import devonenterprise.ext.util.DateUtil;
import devonenterprise.util.StringUtil;
import devonframework.business.transaction.nested.LNestedTransactionManager;

/** 
 * opnbCrdMgPbc
 * 
 * @logicalname  : 오픈뱅킹카드관리Pbc
 * @author       : 박건우
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       박건우       최초 생성
 *
 */

public class OpnbCrdMgPbc {

	/**
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹발급기관기본, UBF오픈뱅킹카드발급기관상세 테이블에 대상 데이터 존재하지 않는 경우
     * - 카드정보조회(제3자정보제공동의)를 요청한 사용자를 등록
     * 
     * 1. 카드사용자등록 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 응답받지 못한 경우 API재호출하여 사용자 등록상태 확인 -> 기등록된 경우, '기등록된 조회서비스용 사용자 서비스(A0324)'로 응답
     *  - 사용자 탈퇴 API 호출하여 카드사용자등록 해지 시 카드 사용자 등록 해지처리전에 동일한 계좌에 대해서 계좌 등록 요청하면 '사용자탈퇴 처리중인 서비스(A0019)'로 거부 응답. 사용자 등록 해지 처리 후 정상 계좌 등록 가능
     * 3. 오픈뱅킹고객정보기본 원장에 응답받은 사용자일련번호와 동일한 오픈뱅킹사용자고유번호가 존재하는지 식별 후 존재하지 않는다면 응답받은 사용자일련번호를 이용하여 오픈뱅킹 고객정보 적재
     * 4. 오픈뱅킹카드발급기관기본, 오픈뱅킹카드발급기관 원장에 동의정보 적재
     * 5. 응답값 리턴
     * 
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹발급기관기본, UBF오픈뱅킹카드발급기관상세 테이블에 대상 데이터 존재하는 경우
     * - 오픈뱅킹 가져오기 기능에서 타 채널에 등록돼있는 사용자의 오픈뱅킹 카드 조회 동의 정보를 현재 이용채널에 가져오기 위함
     * 
     * 1. 이용중인 채널의 채널구분코드 입력
     * 2. 오픈뱅킹발급기관상세 원장에서 입력값에 해당하는 데이터의 채널세부업무구분코드를 이용중인 채널구분코드로 변경하여 원장에 적재
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드발급기관기본, UBF오픈뱅킹카드발급기관상세
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 회원금융회사코드, 사용자명, CI, 이메일주소, 서비스구분(cardinfo), 제3자정보제공동의여부, 채널구분코드, 오픈뱅킹플랫폼식별번호
     * <OUTPUT>
     * 
     * @serviceID UBF2030301
     * @method regtCrdUsr
     * @method(한글명) 카드사용자 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtCrdUsr(LData iRegData) throws LException {
    	LData result = new LData();
        LData apiBody = new LData();
        LData callOutput = new LData();
         
        String crdTbExt = ""; //카드원장존재여부
        
	 	/** INPUT VALIDATION */
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹개설기관코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹회원금융회사코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("고객명"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객명"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
		}
	    
	    if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹이메일주소전문내용"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹이메일주소전문내용"));
	    }

	    if(StringUtil.trimNisEmpty(iRegData.getString("채널세부업무구분코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
	        
        /** [오픈뱅킹카드고객기본] 원장에 조회동의여부/사용여부 Y인상태의 등록된 정보인지 체크  */
        
	    try {
		   	
        	LData iAlCrdData = new LData();  
    	    LData rAlCrdData = new LData();
    	    
    	    iAlCrdData.setString("CI내용"              , iRegData.getString("CI내용"));
	       	iAlCrdData.setString("오픈뱅킹개설기관코드"     , iRegData.getString("오픈뱅킹개설기관코드"));   
		    iAlCrdData.setString("오픈뱅킹회원금융회사코드"  , iRegData.getString("오픈뱅킹회원금융회사코드"));
		    iAlCrdData.setString("카드정보조회동의여부"     , "Y");
		    
		    rAlCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdUseYnInq" , iAlCrdData);	              
		    
		    crdTbExt = "Y";
		    
		} catch (LNotFoundException nfe) {
			crdTbExt = "N";
		}
        	
	    /** [오픈뱅킹카드고객기본] 원장에 조회동의여부/사용여부 Y인상태의 등록된 정보 여부에 따라서 등록 OR 변경  */
	        
	    if("N".equals(crdTbExt)) { // [오픈뱅킹고객정보기본]에 고객정보가 없거나 [오픈뱅킹카드고객기본]에 카드사별 조회 동의 정보 없음.
	        	
	        LData rData = new LData();
	        	
	        /** 거래고유번호 채번  */
	        
		    LData rCdMg = new LData();        	
		    OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
		       	
		    rCdMg = opnbCdMg.crtTrUno(new LData());
			
		    /** API INPUT SETTING */
		    
			apiBody.setString("채널세부업무구분코드"   , iRegData.getString("채널세부업무구분코드"));
		    apiBody.setString("bank_tran_id"     , rCdMg.getString("거래고유번호")); // 참가기관 거래고유번호
		    apiBody.setString("bank_code_std"    , iRegData.getString("오픈뱅킹개설기관코드")); // 카드사 대표코드(오픈뱅킹개설기관코드)
		    apiBody.setString("member_bank_code" , iRegData.getString("오픈뱅킹회원금융회사코드")); // 회원 금융회사 코드
		    apiBody.setString("user_name"        , iRegData.getString("고객명")); // 고객명
		    //apiBody.setString("user_name"      , CryptoDataUtil.decryptKey(iRegData.getString("고객명") , CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
		    apiBody.setString("user_ci"          , iRegData.getString("CI내용")); // CI
		    apiBody.setString("user_email"       , iRegData.getString("오픈뱅킹이메일주소전문내용")); // 이메일주소
		    //apiBody.setString("user_email"     , CryptoDataUtil.decryptKey(iRegData.getString("오픈뱅킹이메일주소전문내용") , CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
		    
		    apiBody.setString("info_prvd_agmt_yn", "Y"); // 제3자정보제공동의여부(카드정보조회동의여부), 카드사용자 등록 호출 = 카드정보조회동의여부(Y)를 의미
		    apiBody.setString("scope"            , "cardinfo"); // 제3자정보제공동의여부(카드정보조회동의여부)
	        	
		    /** API CALL */
			
		    OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
		    
		    callOutput = opnbApiCpbc.regtCrdUsrAPICall(apiBody);
		    
			/** API CALL 후처리 */
		    
		    result.setString("API거래고유번호_V40" , callOutput.getString("api_tran_id"));
		    result.setString("API거래일시_V17"    , callOutput.getString("api_tran_dtm"));
		    result.setString("API응답코드_V5"     , callOutput.getString("rsp_code"));
			result.setString("API응답메시지_V300" , callOutput.getString("rsp_message"));
		        
			if("A0000".equals(callOutput.getString("rsp_code")) || "A0324".equals(callOutput.getString("rsp_code"))) { // 정상등록 / [오픈뱅킹카드고객기본] 원장에는 사용안함이나 존재하지 않고 금결원에는 기등록되었다고 했을 때 금결원 기준으로 변경
					
				LData inputData = new LData();
				
				inputData.putAll(iRegData);
				
				inputData.setString("오픈뱅킹사용자고유번호" , callOutput.getString("user_seq_no")); // 사용자일련번호
				inputData.setString("오픈뱅킹카드개설기관명", callOutput.getString("bank_name")); // 카드개설기관명
				
				if("A0324".equals(callOutput.getString("rsp_code"))) { // A0324 기등록된 사용자일경우, 금결원에서 Return 해주지 않는 정보 채워넣기 (카드개설기관명)
					
					try {
						
						LData iCrdOpeInsNm = new LData();
						LData rCrdOpeInsNm = new LData();
						
						iCrdOpeInsNm.setString("오픈뱅킹금융기관코드", iRegData.getString("오픈뱅킹회원금융회사코드"));
						
						rCrdOpeInsNm = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectCrdOpeInsNm" , iCrdOpeInsNm);
						
						inputData.setString("오픈뱅킹카드개설기관명", rCrdOpeInsNm.getString("오픈뱅킹기관명"));
						
					} catch(LNotFoundException nfe) { // NotFoundException 발생하여도 에러처리 안함.
						
						inputData.setString("오픈뱅킹카드개설기관명", ""); 
						
					}
					
				}
				
				rData = this.regCrdCstPrc(inputData);//사용자고객등록 처리
			    
			    result.setString("참가기관거래고유번호_V20" , callOutput.getString("bank_tran_id"));
				result.setString("참가기관거래일자_V8"     , callOutput.getString("bank_tran_date"));
				result.setString("참가기관표준코드_V3"     , callOutput.getString("bank_code_tran"));
				result.setString("참가기관응답코드_V3"     , callOutput.getString("bank_rsp_code"));
				result.setString("참가기관응답메시지_V100" , callOutput.getString("bank_rsp_message"));
				
				result.setString("오픈뱅킹카드개설기관명"   , callOutput.getString("bank_name")); // 오픈뱅킹카드개설기관명
				result.setLong("오픈뱅킹사용자고유번호"     , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
			    	
			} else {
				
				if("A0002".equals(callOutput.getString("rsp_code"))) { //참가기관에러
					
					result.setString("참가기관거래고유번호_V20" , callOutput.getString("bank_tran_id")); // 응답코드(참가기관) // 참가기관거래고유번호
					result.setString("참가기관거래일자_V8"     , callOutput.getString("bank_tran_date")); // 거래일자(참가기관) // 오픈뱅킹전문거래년월일
					result.setString("참가기관표준코드_V3"     , callOutput.getString("bank_code_tran")); // 참가기관대표코드 // 오픈뱅킹응답금융기관코드
					result.setString("참가기관응답코드_V3"     , callOutput.getString("bank_rsp_code")); // 응답코드(참가기관) // 오픈뱅킹참가기관응답구분코드
					result.setString("참가기관응답메시지_V100" , callOutput.getString("bank_rsp_message")); // 응답메시지(참가기관) // 오픈뱅킹참가기관응답메시지내용
					
					result.setString("오픈뱅킹카드개설기관명"   , callOutput.getString("bank_name")); // 오픈뱅킹카드개설기관명
					result.setLong("오픈뱅킹사용자고유번호"     , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
					
				}
				
				return result;
					
			}
					
		} else { // [오픈뱅킹고객정보기본] 와 [오픈뱅킹카드고객기본]에 카드사별 조회 동의 정보 있음.
			
			/** [오픈뱅킹카드고객상세] 원장 해당 채널로 등록된 정보 있는지 체크 -> 없다면 채널 변경해서 INSERT -> 있다면 계좌상세사용여부 Y로 UPDATE  */
			
		    String crdDtlUseYn = "N";

			try {
			    
			    LData iOpnbCrdDtl = new LData();
			    LData rOpnbCrdDtl = new LData();
			    
			    iOpnbCrdDtl.setString("오픈뱅킹회원금융회사코드"    , iRegData.getString("오픈뱅킹회원금융회사코드"));
			    iOpnbCrdDtl.setString("채널세부업무구분코드"       , iRegData.getString("채널세부업무구분코드"));
			    iOpnbCrdDtl.setString("CI내용"                 , iRegData.getString("CI내용"));
			    
			    rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl);
			    crdDtlUseYn = rOpnbCrdDtl.getString("카드정보조회동의여부");
		       			        	
		    } catch (LNotFoundException nfe) {
		    	
		        crdDtlUseYn = "NFE";
		        
		    }
		    	
		    if("NFE".equals(crdDtlUseYn)) { // 존재하지 않는다면 채널변경하여 SELECT INSERT
				
		    	LData iInsCrdDtl = new LData();
			    
		    	iInsCrdDtl.setString("채널세부업무구분코드"     , iRegData.getString("채널세부업무구분코드"));
		    	iInsCrdDtl.setString("오픈뱅킹회원금융회사코드"  , iRegData.getString("오픈뱅킹회원금융회사코드"));
		    	iInsCrdDtl.setString("CI내용"              , iRegData.getString("CI내용"));
		    	iInsCrdDtl.setString("오픈뱅킹플랫폼식별번호"   , iRegData.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsCrdDtl.setString("카드정보조회동의여부"     , "Y");
		    	iInsCrdDtl.setString("카드정보조회동의갱신일시"  , DateUtil.getDateTimeStr());
			    
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertOpnbCrdDtlChnPr" , iInsCrdDtl);
				
				if(rtnReg == 0) { // 만약 에러로 인해 타 채널에서 등록한 정보도 없는 경우 - SELECT INSERT 안되는 경우
					
					iInsCrdDtl.setString("고객식별자"                , iRegData.getString("고객식별자"));
					iInsCrdDtl.setString("준회원식별자"               , iRegData.getString("준회원식별자"));
					
					rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertOpnbCrdDtl" , iInsCrdDtl);
					
				}
				
				/** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
				
				LData iSelectUsrUno = new LData();
				
				iSelectUsrUno.setString("CI내용", iRegData.getString("CI내용"));
	     		
	     		try {
	     			
	     			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	     			iRegData.setString("오픈뱅킹사용자고유번호", opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호"));
	           
	      		} catch(LException e) {
	      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	      		}
				
				LData iRegOpnbCstCnsPhsIn = new LData(); 
			    
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , iRegData.getString("오픈뱅킹사용자고유번호"));
			    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , iRegData.getString("채널세부업무구분코드"));
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
				iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
			  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(iRegData.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
				
			  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
			  	
			  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
				
				result.setString("API응답코드_V5"     , "ZZZZZ");
				result.setString("API응답메시지_V300" , "[오픈뱅킹고객정보기본], [오픈뱅킹카드고객기본]에 카드사별 조회 동의 정보 존재/ [오픈뱅킹카드고객상세] 원장 채널 동의 정보 추가");
				
				return result;
			    	
			} else if("N".equals(crdDtlUseYn)){ // 등록 정보 있을 경우 - 카드정보조회동의여부 = "N"
				
			    LData iUpdCrdDtl = new LData();
			    
			    iUpdCrdDtl.setString("채널세부업무구분코드"      , iRegData.getString("채널세부업무구분코드"));
			    iUpdCrdDtl.setString("오픈뱅킹회원금융회사코드"   , iRegData.getString("오픈뱅킹회원금융회사코드"));
			    iUpdCrdDtl.setString("CI내용"               , iRegData.getString("CI내용"));
			    iUpdCrdDtl.setString("카드정보조회동의여부"      , "Y");
			    iUpdCrdDtl.setString("카드정보조회동의갱신일시"   , DateUtil.getDateTimeStr());

				int rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdDtlUseYn" , iUpdCrdDtl);
				
				/** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
				
				LData iSelectUsrUno = new LData();
				
				iSelectUsrUno.setString("CI내용", iRegData.getString("CI내용"));
	     		
	     		try {
	     			
	     			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	     			iRegData.setString("오픈뱅킹사용자고유번호", opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호"));
	              	
	      		} catch(LException e) {
	      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	      		}
				
				LData iRegOpnbCstCnsPhsIn = new LData(); 
			    
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , iRegData.getString("오픈뱅킹사용자고유번호"));
			    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , iRegData.getString("채널세부업무구분코드"));
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
			    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
				iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
			  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(iRegData.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
				
			  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
			  	
			  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
			  	
				result.setString("API응답코드_V5"     , "ZZZZZ");
				result.setString("API응답메시지_V300" , "[오픈뱅킹고객정보기본], [오픈뱅킹카드고객기본], [오픈뱅킹카드고객상세] 원장에 카드사별 조회 동의 정보 존재/ [오픈뱅킹카드고객상세] 원장 조회동의여부 변경(Y)");
				
				return result;
		    		
		    } else{ // 등록 정보 있을 경우 - 카드정보조회동의여부 = "Y"
			    LLog.debug.println("카드 상세 기등록 정상고객 로그 :" + iRegData);
			    
			    result.setString("API응답코드_V5"     , "ZZZZZ");
				result.setString("API응답메시지_V300" , "[오픈뱅킹고객정보기본], [오픈뱅킹카드고객기본], [오픈뱅킹카드고객상세] 원장에 카드사별 조회 동의 정보 존재/ 변경사항 없음");
				
				return result;
		    }
		    	
		}
 	   	
        return result;
    }
    
    
    /**
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세 테이블데이터 등록
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * UBF오픈뱅킹카드고객기본
     * UBF오픈뱅킹카드고객상세
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호 
     * 고객식별자
     * 준회원식별자
     * CI내용
     * 오픈뱅킹주민등록번호
     * 오픈뱅킹사용자휴대폰번호
     * 성별구분코드
     * 고객출생년월일
     * 오픈뱅킹통합계좌동의여부
     * 카드정보조회동의여부
     * 오픈뱅킹회원금융회사코드
     * 고객명
     * 오픈뱅킹개설기관코드
     * 오픈뱅킹이메일주소전문내용
     * 
     * <OUTPUT>
     * 
     *
     * @method regCrdCstPrc
     * @method(한글명) 카드고객등록처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regCrdCstPrc(LData input) throws LException {
    	
    	LData rData = new LData(); 
    	
        String userSeqNo = input.getString("오픈뱅킹사용자고유번호");
        String custTbExt = "N"; // 오픈뱅킹 고객 존재 여부
        String crdTbExt = "N"; // 오픈뱅킹카드고객기본 존재 여부  
        
        /** [오픈뱅킹고객정보기본] 원장 고객정보 존재하는지 조회 */

        try {
        	
        	LData iAlCustData = new LData();  
            LData rAlCustData = new LData();
            
        	iAlCustData.setString("오픈뱅킹사용자고유번호" , userSeqNo);        
        	rAlCustData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectRgYn" , iAlCustData);
        	custTbExt = "Y";	       
        	
        } catch (LNotFoundException nfe) {
        	custTbExt = "N";
        }	
        
        /** [오픈뱅킹고객정보기본] 원장 고객정보 존재 여부에 따라서 등록 OR 변경 */
        
	    if("N".equals(custTbExt)) {//고객신규등록

	    	LData iAlCustRegData = new LData();  

	    	iAlCustRegData.setString("오픈뱅킹사용자고유번호"        , userSeqNo);
            iAlCustRegData.setString("고객식별자"                , input.getString("고객식별자"));
            iAlCustRegData.setString("준회원식별자"               , input.getString("준회원식별자"));
            iAlCustRegData.setString("고객명"                   , input.getString("고객명"));
            //iAlCustRegData.setString("고객명"                   , CryptoDataUtil.encryptKey( input.getString("고객명") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
            iAlCustRegData.setString("CI내용"                  , input.getString("CI내용"));
            iAlCustRegData.setString("오픈뱅킹주민등록번호"         , input.getString("오픈뱅킹주민등록번호"));
            //iAlCustRegData.setString("오픈뱅킹주민등록번호"         , CryptoDataUtil.encryptKey( input.getString("오픈뱅킹주민등록번호") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
            iAlCustRegData.setString("오픈뱅킹사용자휴대폰번호"      , input.getString("오픈뱅킹사용자휴대폰번호"));
            //iAlCustRegData.setString("오픈뱅킹사용자휴대폰번호"      , CryptoDataUtil.encryptKey( input.getString("오픈뱅킹사용자휴대폰번호") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
            iAlCustRegData.setString("성별구분코드"               , input.getString("성별구분코드"));//1.남자 2.여자 9.기타
            iAlCustRegData.setString("고객출생년월일"             , input.getString("고객출생년월일"));
            iAlCustRegData.setString("오픈뱅킹통합계좌조회동의여부"   , input.getString("오픈뱅킹통합계좌조회동의여부"));
            iAlCustRegData.setString("오픈뱅킹서비스해지여부"       , "N");
            
            BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "insertOpnbCstInfBas" , iAlCustRegData);
	    	
	    } else if("Y".equals(custTbExt)){ // 기 등록 오픈뱅킹 고객
	    	
	    	//고객존재할경우 출금 및 계좌 동의 여부 Y로 업데이트
	    	LLog.debug.println("기등록 정상고객 로그 :" + input);
	    	
	    	LData iUptOpnbCstCnsYn = new LData();	
	    	iUptOpnbCstCnsYn.setString("오픈뱅킹통합계좌조회동의여부" , input.getString("오픈뱅킹통합계좌조회동의여부"));	    	
	    	iUptOpnbCstCnsYn.setString("카드정보조회동의여부"       , "Y");
	    	iUptOpnbCstCnsYn.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
	    	iUptOpnbCstCnsYn.setString("시스템최종갱신일시"         , DateUtil.getDateTimeStr());
	    	iUptOpnbCstCnsYn.setString("시스템최종거래일시"        , DateUtil.getDateTimeStr());
	    	
	    	int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "uptOpnbCstCnsYn" , iUptOpnbCstCnsYn);
	    	
	    }
    	
	    /** [오픈뱅킹카드고객기본] 원장 카드정보사용여부 = N 처리되어 있는 기 등록 된 정보 있는지 조회  */
	    
	    try {
	         
	        LData iAlCrdData = new LData();  
	        LData rAlCrdData = new LData();
	        
	        iAlCrdData.setString("오픈뱅킹사용자고유번호"    , userSeqNo);
           	iAlCrdData.setString("오픈뱅킹개설기관코드"     , input.getString("오픈뱅킹개설기관코드"));   
		    iAlCrdData.setString("오픈뱅킹회원금융회사코드"  , input.getString("오픈뱅킹회원금융회사코드"));
		    iAlCrdData.setString("카드정보조회동의여부"     , "N");
	        
		    rAlCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdBas" , iAlCrdData);
	        
	        crdTbExt = "Y";
	  	
        } catch (LNotFoundException nfe) {
        	
        	crdTbExt = "N";
        }
	    
	    /** [오픈뱅킹카드고객기본] 원장 등록 된 정보 존재 여부에 따라서 등록 OR 변경(사용여부 Y) */
	    
	    if("N".equals(crdTbExt)) { // 등록 정보 없을 경우
	
	    	LData iOpnbCrdBas = new LData();
	    	
	    	iOpnbCrdBas.setString("오픈뱅킹사용자고유번호"      , userSeqNo);
	    	iOpnbCrdBas.setString("오픈뱅킹회원금융회사코드"    , input.getString("오픈뱅킹회원금융회사코드"));
	    	iOpnbCrdBas.setString("고객명"                 , input.getString("고객명"));
	    	//iOpnbCrdBas.setString("고객명"               , CryptoDataUtil.encryptKey( input.getString("고객명") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
	    	iOpnbCrdBas.setString("오픈뱅킹개설기관코드"       , input.getString("오픈뱅킹개설기관코드"));
	    	iOpnbCrdBas.setString("오픈뱅킹카드개설기관명"      , input.getString("오픈뱅킹카드개설기관명"));
	    	iOpnbCrdBas.setString("오픈뱅킹이메일주소전문내용"         , input.getString("오픈뱅킹이메일주소전문내용"));
	    	//iOpnbCrdBas.setString("오픈뱅킹이메일주소전문내용"         , CryptoDataUtil.encryptKey( input.getString("오픈뱅킹이메일주소전문내용") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
	    	
	    	iOpnbCrdBas.setString("카드정보조회동의여부"       , "Y");
	    	iOpnbCrdBas.setString("카드정보조회동의등록일시"    , DateUtil.getDateTimeStr());
	    	iOpnbCrdBas.setString("조회등록채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
	    	
	    	int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertCrdUsr" , iOpnbCrdBas);
	    	
	    } else if("Y".equals(crdTbExt)) { // 등록 정보 있을 경우(카드조회사용여부 = 'N')
	    	
	    	LData iUpdOpnbCrdBas = new LData();
	    	
	    	iUpdOpnbCrdBas.setString("오픈뱅킹사용자고유번호"      , userSeqNo);
	    	iUpdOpnbCrdBas.setString("오픈뱅킹회원금융회사코드"    , input.getString("오픈뱅킹회원금융회사코드"));
	    	iUpdOpnbCrdBas.setString("카드정보조회동의여부"       , "Y");
	    	
	    	int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdBasUseYn" , iUpdOpnbCrdBas);
	    	
	    }
	    
	    /** [오픈뱅킹카드고객상세] 원장에 등록 된 정보 있는지 조회  */
	    
	    String crdDtlUseYn = "N";
	    
	    try {
	        
	    	LData iOpnbCrdDtl = new LData();
	    	LData rOpnbCrdDtl = new LData();
	    	
	    	iOpnbCrdDtl.setString("오픈뱅킹회원금융회사코드"    , input.getString("오픈뱅킹회원금융회사코드"));
	    	iOpnbCrdDtl.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
	    	iOpnbCrdDtl.setString("CI내용"                , input.getString("CI내용"));
	    	
	    	rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl);
	    	crdDtlUseYn = rOpnbCrdDtl.getString("카드정보조회동의여부");
       			        	
        } catch (LNotFoundException nfe) {
        	
        	crdDtlUseYn = "NFE";
        }	
	    
	    /** [오픈뱅킹카드고객상세] 원장 등록 된 정보 존재 여부에 따라서 등록 OR 변경(사용여부 Y) */
	    
	    if("NFE".equals(crdDtlUseYn)) { // 등록 정보 없을 경우 - 등록 채널 신규 등록

	    	LData iInsCrdDtl = new LData();
	    	
	    	iInsCrdDtl.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드"));
	    	iInsCrdDtl.setString("오픈뱅킹회원금융회사코드"   , input.getString("오픈뱅킹회원금융회사코드"));
	    	iInsCrdDtl.setString("CI내용"               , input.getString("CI내용"));	    	
	    	iInsCrdDtl.setString("오픈뱅킹플랫폼식별번호"    , input.getString("오픈뱅킹플랫폼식별번호"));
	    	iInsCrdDtl.setString("준회원식별자"           , input.getString("준회원식별자"));
	    	iInsCrdDtl.setString("고객식별자"            , input.getString("고객식별자"));
	    	iInsCrdDtl.setString("카드정보조회동의여부"     , "Y");
	    	iInsCrdDtl.setString("카드정보조회동의갱신일시"  , DateUtil.getDateTimeStr());
	    	
		    int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertOpnbCrdDtl" , iInsCrdDtl);
		    
		    /** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
		  
		    LData iRegOpnbCstCnsPhsIn = new LData(); 
		    
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , input.getString("오픈뱅킹사용자고유번호"));
		    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
			iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
		  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
			
		  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
		  	
		  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
		    
	    }else if("N".equals(crdDtlUseYn)){ // 등록 정보 있을 경우 - 카드정보조회동의여부 = "N"
	    	
	    	LData iUpdCrdDtl = new LData();
	    	
	    	iUpdCrdDtl.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
	    	iUpdCrdDtl.setString("오픈뱅킹회원금융회사코드"    , input.getString("오픈뱅킹회원금융회사코드"));
	    	iUpdCrdDtl.setString("CI내용"                , input.getString("CI내용"));
	    	iUpdCrdDtl.setString("카드정보조회동의여부"      , "Y");
	    	iUpdCrdDtl.setString("카드정보조회동의갱신일시"   , DateUtil.getDateTimeStr());

		    int rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdDtlUseYn" , iUpdCrdDtl);
		    
		    /** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
			  
		    LData iRegOpnbCstCnsPhsIn = new LData(); 
		    
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , input.getString("오픈뱅킹사용자고유번호"));
		    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
			iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
		  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
			
		  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
		  	
		  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
	    	
	    }else if("Y".equals(crdDtlUseYn)){ // 등록 정보 있을 경우 - 카드정보조회동의여부 = "Y"
	    	
	    	// 처리 X 

	    	LLog.debug.println("카드고객상세 기등록 정상고객 로그 :" + input);
	    }
	    
	    return rData;
    }

    /**
     * - 등록된 사용자의 카드정보(이메일주소)를 변경
     * 
     * 1. 카드정보변경 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 오픈뱅킹센터로부터 응답을 받지 못했을 경우 카드정보조회 API를 통해 처리 결과 확인 -> 카드정보변경 API 재호출
     * 3. 오픈뱅킹고객정보기본, 오픈뱅킹발급기관기본 원장의 오픈뱅킹이메일주소전문내용 변경
     * 4. 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드발급기관기본
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 오픈뱅킹회원금융회사코드, 오픈뱅킹사용자고유번호, 서비스구분(cardinfo), 변경할 이메일 주소
     * <OUTPUT>
     * 
     * @ServiceID UBF2030302
     * @method chngCrdInf
     * @method(한글명) 카드정보 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData chngCrdInf(LData input) throws LException {
    	
    	LData result = new LData();
    	LData apiBody = new LData();
    	LData callOutput = new LData();
        
        /** CI내용으로 INPUT */
	    
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
  		}
     	
     	LData iSelectUsrUno = new LData();
     	
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
     		
     		iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
     		
     		try {
     			
     			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
     	    	
             	input.setString("오픈뱅킹사용자고유번호", opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호"));
              	
              	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
            	LLog.debug.println(input);
            		
      		} catch(LException e) {
      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
      		}
     	}
         
        /** 거래고유번호 채번 */
     	
     	OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc();
     	
     	try {
     		 			
     		LData rCdMg = new LData();
         	rCdMg = opnbCdMgCpbc.crtTrUno(new LData());
         	
         	input.setString("참가기관거래고유번호", rCdMg.getString("거래고유번호"));
        		
  		} catch(LException e) {
  			throw new LBizException(ObsErrCode.ERR_9003.getCode(), ObsErrCode.ERR_9003.getName());
  		}
     	
       	/** INPUT VALIDATION */
	    
     	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
	    }
     	
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹개설기관코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
	    }
	    
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
	    }
	    
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
	    }
	    
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹이메일주소전문내용"))) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹이메일주소전문내용"));
		}

		/** API INPUT SETTING */
    	
		apiBody.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드"));
		apiBody.setString("bank_tran_id"        , input.getString("참가기관거래고유번호")); 
		apiBody.setString("bank_code_std"       , input.getString("오픈뱅킹개설기관코드")); 
		apiBody.setString("member_bank_code"    , input.getString("오픈뱅킹회원금융회사코드")); 
		apiBody.setString("user_seq_no"         , input.getString("오픈뱅킹사용자고유번호"));
		apiBody.setString("update_user_email"   , input.getString("오픈뱅킹이메일주소전문내용"));
		//apiBody.setString("update_user_email" , CryptoDataUtil.decryptKey(input.getString("오픈뱅킹이메일주소전문내용") , CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
		apiBody.setString("scope", "cardinfo");
        
        /** API CALL */
		
        OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
        
        callOutput = opnbApiCpbc.chngCrdInfAPICall(apiBody);
     	
        /** API OUTPUT SETTING */
        
        result.setString("API거래고유번호_V40" , callOutput.getString("api_tran_id"));
        result.setString("API거래일시_V17"    , callOutput.getString("api_tran_dtm"));
        result.setString("API응답코드_V5"     , callOutput.getString("rsp_code"));
		result.setString("API응답메시지_V300" , callOutput.getString("rsp_message"));
		
		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 카드정보조회 API 정상 조회 시
			result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id")); // 응답코드(참가기관) // 참가기관거래고유번호
			result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date")); // 거래일자(참가기관) // 오픈뱅킹전문거래년월일
			result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran")); // 참가기관대표코드 // 오픈뱅킹응답금융기관코드
			result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code")); // 응답코드(참가기관) // 오픈뱅킹참가기관응답구분코드
			result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message")); // 응답메시지(참가기관) // 오픈뱅킹참가기관응답메시지내용
			
			result.setString("오픈뱅킹카드개설기관명"       , callOutput.getString("bank_name")); // 오픈뱅킹카드개설기관명
			result.setLong("오픈뱅킹사용자고유번호"         , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
			result.setString("오픈뱅킹이메일주소전문내용"    , callOutput.getString("update_user_email")); // 이메일주소 // 오픈뱅킹이메일주소
			//result.setString("오픈뱅킹이메일주소전문내용"       , CryptoDataUtil.encryptKey( callOutput.getString("update_user_email"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //암호화처리_주석
			
			/** [오픈뱅킹카드고객기본] 원장 오픈뱅킹이메일주소 변경 */
	        
	        LData chgData = new LData();
	        
	        chgData.setString("오픈뱅킹회원금융회사코드"   , input.getString("오픈뱅킹회원금융회사코드"));
	        chgData.setString("오픈뱅킹사용자고유번호"     , result.getString("오픈뱅킹사용자고유번호"));
	        chgData.setString("오픈뱅킹이메일주소전문내용"  , result.getString("오픈뱅킹이메일주소전문내용")); //금결원 변경 이메일 주소 (result set -> 기 암호화 처리)
	        
	        try {
	        	int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdInf", chgData); // 카드정보변경_원장 변경
	        } catch(LException e) {
	        	throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "카드고객기본변경(updateCrdInf) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	        }
	        
		} else {
			
			if("A0002".equals(callOutput.getString("rsp_code"))) { // 참가기관 오류
				 
				result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id")); // 응답코드(참가기관) // 참가기관거래고유번호
				result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date")); // 거래일자(참가기관) // 오픈뱅킹전문거래년월일
				result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran")); // 참가기관대표코드 // 오픈뱅킹응답금융기관코드
				result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code")); // 응답코드(참가기관) // 오픈뱅킹참가기관응답구분코드
				result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message")); // 응답메시지(참가기관) // 오픈뱅킹참가기관응답메시지내용
				
				result.setString("오픈뱅킹카드개설기관명"       , callOutput.getString("bank_name")); // 오픈뱅킹카드개설기관명
				result.setLong("오픈뱅킹사용자고유번호"         , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
				result.setString("오픈뱅킹이메일주소전문내용"    , callOutput.getString("user_email")); // 이메일주소 // 오픈뱅킹이메일주소
				//result.setString("오픈뱅킹이메일주소전문내용"      , CryptoDataUtil.encryptKey( callOutput.getString("user_email"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); //암호화처리_주석
				
			}
			
			return result;
			
		}
        
        return result;
    }

    /**
     * - 등록된 사용자의 카드정보를 조회
     * 
     * 1. 카드정보조회 API(금결원) 호출
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 사용자탈퇴 API 요청 후 카드사용자 해지 처리전 카드정보조회 요청하게 되면, '사용자탈퇴 처리중인 계좌(A0019)'로 조회 거부 응답
     *  - 응답코드 000 이면 동의, 551,555,556이면 동의거부, 이외코드 조회 불능
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     * <INPUT>
     *  거래고유번호, 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 서비스구분(cardinfo)
     * <OUTPUT>
     *  카드개설기관명, 사용자일련번호, 변경된 이메일주소
     * 
     * @ServiceID UBF2030303
     * @method retvCrdInf
     * @method(한글명) 카드정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCrdInf(LData input) throws LException {

    	LData result = new LData();
    	LData apiBody = new LData();
    	LData callOutput = new LData();
    	
        /** CI내용으로 INPUT */
       
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(input.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
 		}
    	
    	LData iSelectUsrUno = new LData();
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회
    		
    		iSelectUsrUno.setString("CI내용", input.getString("CI내용"));

    		try {
    			
    			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
    	    	
            	input.setString("오픈뱅킹사용자고유번호", opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호"));
             	
             	LLog.debug.println("CI_사용자 고유번호 조회::input::");
           		LLog.debug.println(input);
           		
     		} catch(LException e) {
     			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
     		}
    	}
    	
    	/** 거래고유번호 채번 */
    	
    	OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc();
    	
    	try {
			
    		LData rCdMg = new LData();
        	rCdMg = opnbCdMgCpbc.crtTrUno(new LData());
        	
        	input.setString("참가기관거래고유번호", rCdMg.getString("거래고유번호"));
       		
 		} catch(LException e) {
 			throw new LBizException(ObsErrCode.ERR_9003.getCode(), ObsErrCode.ERR_9003.getName());
 		}
    	
    	/** INPUT VALIDATION */
		
    	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
    	
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹개설기관코드"))) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹회원금융회사코드"))) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
		}
		
		/** API INPUT SETTING */
    	
		apiBody.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
		apiBody.setString("bank_tran_id"     , input.getString("참가기관거래고유번호"));
		apiBody.setString("bank_code_std"    , input.getString("오픈뱅킹개설기관코드")); 
		apiBody.setString("member_bank_code" , input.getString("오픈뱅킹회원금융회사코드"));
		apiBody.setString("user_seq_no"      , input.getString("오픈뱅킹사용자고유번호")); 
		apiBody.setString("scope"            , "cardinfo");
        
        /** API CALL */
		
        OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
        
        callOutput = opnbApiCpbc.retvCrdInfAPICall(apiBody);
        
        /** API OUTPUT SETTING */
        
        result.setString("API거래고유번호_V40"    , callOutput.getString("api_tran_id")); // 응답코드(API) // 오픈뱅킹API거래고유번호
        result.setString("API거래일시_V17"       , callOutput.getString("api_tran_dtm"));
		result.setString("API응답코드_V5"        , callOutput.getString("rsp_code")); // 응답메시지(API) // 오픈뱅킹API응답구분코드
		result.setString("API응답메시지_V300"     , callOutput.getString("rsp_message")); // 응답메시지(API) // 오픈뱅킹API응답구분코드
		
		if("A0000".equals(callOutput.getString("rsp_code"))) { // 금융결제원 카드정보조회 API 정상 조회 시
			
			result.setString("참가기관거래고유번호_V20"    , callOutput.getString("bank_tran_id")); // 응답코드(참가기관) // 참가기관거래고유번호
			result.setString("참가기관거래일자_V8"        , callOutput.getString("bank_tran_date")); // 거래일자(참가기관) // 오픈뱅킹전문거래년월일
			result.setString("참가기관표준코드_V3"        , callOutput.getString("bank_code_tran")); // 참가기관대표코드 // 오픈뱅킹응답금융기관코드
			result.setString("참가기관응답코드_V3"        , callOutput.getString("bank_rsp_code")); // 응답코드(참가기관) // 오픈뱅킹참가기관응답구분코드
			result.setString("참가기관응답메시지_V100"     , callOutput.getString("bank_rsp_message")); // 응답메시지(참가기관) // 오픈뱅킹참가기관응답메시지내용
			
			result.setString("오픈뱅킹카드개설기관명"       , callOutput.getString("bank_name")); // 오픈뱅킹카드개설기관명
			result.setLong("오픈뱅킹사용자고유번호"         , callOutput.getLong("user_seq_no")); // 사용자일련번호 // 오픈뱅킹사용자고유번호
			result.setString("카드정보조회동의여부"        , callOutput.getString("inquiry_agree_yn")); // 제3자정보제공동의여부 // 카드정보조회동의여부
			result.setString("오픈뱅킹이메일주소전문내용"    , callOutput.getString("user_email")); // 이메일주소 // 오픈뱅킹이메일주소
			//result.setString("오픈뱅킹이메일주소전문내용"  , CryptoDataUtil.encryptKey( callOutput.getString("user_email"), false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
			
		} else { //기관오류
			
			throw new LBizException(callOutput.getString("rsp_code") , callOutput.getString("rsp_message"));
			
		}
        
        /** RETURN */
        
        return result;
    }

    /**
     * - 요청자의 카드사별 카드 목록 정보 등록(이용동의 상태의 카드사)
     * 
     * 1. 오픈뱅킹카드사별이용동의 조회
     * 2. 카드목록조회(금결원) API 호출하여 요청받은 카드사의 카드 목록 정보를 오픈뱅킹보유카드정보기본 원장, 오픈뱅킹카드정보기본 원장에 적재
     * 3. 조회된 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드발급기관기본, UBF오픈뱅킹카드발급기관상세, UBF오픈뱅킹카드정보기본
     * <INPUT>
     *  거래고유번호, 카드사대표코드, 회원금융회사코드, 사용자명, CI, 이메일주소, 서비스구분(cardinfo), 제3자정보제공동의여부, 채널구분코드, 오픈뱅킹플랫폼식별번호
     * <OUTPUT>
     * 
     * @ServiceID UBF2030304
     * @method regtChnPrCrdCtg
     * @method(한글명) 채널별 카드 목록 등록
     * @param LMultiData
     * @return LData
     * @throws LException 
     */ 
    public LData regtChnPrCrdCtg(LData input) throws LException {
        LData result = new LData();
        
        int cRegData = 0;
         
		String userSeqNo = ""; //오픈뱅킹사용자고유번호
		
        LMultiData iCrdDtlInfo = (LMultiData)input.get("그리드");
        
     	/** 다건 입력 처리 */
 	   
 	   	for(int i =0; i < iCrdDtlInfo.getDataCount(); i++) {	
        
	 	   	LData iRegData = iCrdDtlInfo.getLData(i);
	 	   	
	 	    /** CI내용으로 조회 요청 */
	        
	     	if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
	     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
	  		}
	     	
	     	LData iSelectUsrUno = new LData();
	     	
	     	if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
	     		
	     		iSelectUsrUno.setString("CI내용", iRegData.getString("CI내용"));
	     		
	     		try {
	     			
	     	    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	     	    	
	             	userSeqNo = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
	             			
	              	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
	            	LLog.debug.println(userSeqNo);
	            		
	      		} catch(LException e) {
	      			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	      		}
	     		
	     	} else {
	     		userSeqNo = iRegData.getString("오픈뱅킹사용자고유번호");
	     	}
	     	
	     	/** INPUT VALIDATION */
	     	
	 	   	if(StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) {
	 	   		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
			}
	 	   	
	 	    if(StringUtil.trimNisEmpty(iRegData.getString("채널세부업무구분코드"))) {
	 	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
			}
	 	   
	        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹개설기관코드"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
			}
	       
	        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹회원금융회사코드"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
			}
	        
	        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹카드식별자"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹카드식별자"));
		    }
	        
	        if(StringUtil.trimNisEmpty(userSeqNo)) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));
			}
	        
	        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹마스킹카드번호"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹마스킹카드번호"));
		    }
	        
	        if(StringUtil.trimNisEmpty(iRegData.getString("카드상품명"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 카드상품명"));
		    }
	        
	        if(StringUtil.trimNisEmpty(iRegData.getString("가족카드여부"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 가족카드여부"));
		    }
	        
	        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹카드구분코드"))) {
	        	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹카드구분코드"));
		    }
	       
	    	/** [오픈뱅킹카드정보기본] 원장 INPUT SETTING */
	       	
			LData iCrdData = new LData();
			
			iCrdData.setString("채널세부업무구분코드"    , iRegData.getString("채널세부업무구분코드"));
			
			iCrdData.setString("오픈뱅킹회원금융회사코드" , iRegData.getString("오픈뱅킹회원금융회사코드"));
			iCrdData.setString("오픈뱅킹사용자고유번호"  , userSeqNo);
			iCrdData.setString("오픈뱅킹카드식별자"     , iRegData.getString("오픈뱅킹카드식별자"));
			
		    iCrdData.setString("오픈뱅킹개설기관코드"    , iRegData.getString("오픈뱅킹개설기관코드"));
		    iCrdData.setString("오픈뱅킹마스킹카드번호"   , iRegData.getString("오픈뱅킹마스킹카드번호"));
		    iCrdData.setString("카드상품명"           , iRegData.getString("카드상품명"));
		    iCrdData.setString("가족카드여부"          , iRegData.getString("가족카드여부"));
		    
		    iCrdData.setString("오픈뱅킹카드구분코드"    , iRegData.getString("오픈뱅킹카드구분코드"));
		    iCrdData.setString("결제은행코드"          , iRegData.getString("결제은행코드"));
		    
		    if(StringUtil.trimNisEmpty(iRegData.getString("카드결제계좌번호"))) { //카드결제계좌번호 NULL값일경우 암호화 처리 X
		    	iCrdData.setString("카드결제계좌번호"   , iRegData.getString("카드결제계좌번호"));
		    } else {
		    	//iCrdData.setString("카드결제계좌번호"         , CryptoDataUtil.encryptKey( iRegData.getString("카드결제계좌번호") , false, CryptoDataUtil.KB_BD_NORMAL_KEY)); 암호화처리_주석
		    	iCrdData.setString("카드결제계좌번호"   , iRegData.getString("카드결제계좌번호"));
		    }
		    
		    iCrdData.setString("마스킹카드결제계좌번호"   , iRegData.getString("마스킹카드결제계좌번호"));
		    iCrdData.setString("카드발급년월일"         , iRegData.getString("카드발급년월일"));
	       	
			try {
				cRegData = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertChnPrCrdCtg" , iCrdData);
			} catch(LException e) {
				throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "카드정보기본등록(insertChnPrCrdCtg) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
			}
	       	
 	   	} // END_FOR
 	   	
 	   	/** OUTPUT SETTING */
 	   	
 	   	if(cRegData > 0) {
 	   		result.setString("처리결과_V1", "Y");
 	   	} else {
 	   		result.setString("처리결과_V1", "N");
 	   	}

        return result;
    }

    /**
     * - 오픈뱅킹에 타 채널에서 기 등록한 카드정보조회 동의 정보를 복사하여 요청 채널 정보로 등록한다.
     * 
     * 1. 기 등록된 타 채널 동의 정보 조회하여 요청이 들어온 채널세부업무구분코드로 변경하여 오픈뱅킹카드고객상세 원장 적재
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세
     * <INPUT>
     *  CI내용, 채널세부업무구분코드, 오픈뱅킹플랫폼식별번호
     * <OUTPUT>
     *  채널세부업무구분코드, 오픈뱅킹회원금융회사코드, CI내용, 오픈뱅킹플랫폼식별번호, 준회원식별자, 고객식별자, 카드정보조회동의여부, 카드정보조회동의갱신일시, 카드정보조회동의해제일시
     * 
     * @ServiceID UBF0100542
     * @method retvChnPrIngCrdInfCtg
     * @method(한글명) 채널별통합카드정보목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvChnPrIngCrdInfCtg(LData input) throws LException {
    	LData result = new LData();
    	LData iOpnbCrdDtl = new LData();
		LData rOpnbCrdDtl = new LData();
		
		LData rstLData = null;
		
   	 	String userSeqNo = "";
   	
   	 	if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {	
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
 		}
       
   	 	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
   	 	
   	 	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹플랫폼식별번호"))) {
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹플랫폼식별번호"));
		}
   	
        /** CI내용으로 조회 요청 */
   	 	
		try {
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
			
			LData iSelectUsrUno = new LData();
			LData rSelectUsrUno = new LData();
			
			iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
			
			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
			userSeqNo = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
			
         	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
         	LLog.debug.println(input);
       		
 		} catch(LException e) {
 			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
 		}
		
		/** 요청 사용자의 카드정보조회동의 = Y 인 카드사 정보 조회 */
		
		LData iSelectUseCrd = new LData();
		LMultiData rSelectUseCrd = new LMultiData();
		
		iSelectUseCrd.setString("오픈뱅킹사용자고유번호" , userSeqNo);
		iSelectUseCrd.setString("카드정보조회동의여부"  , "Y");
		
		rSelectUseCrd = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdCstBasWhlInq" , iSelectUseCrd);
		
		if(rSelectUseCrd.getMaxDataCount() == 0) {
			throw new LBizException(ObsErrCode.ERR_7001.getCode() , StringUtil.mergeStr( "오픈뱅킹에 등록된 " , ObsErrCode.ERR_7001.getName() ,"(", ObsErrCode.ERR_7001.getCode(),")"));
		}
		
		LMultiData rInsCrdDtlGrid = new LMultiData();
		
		for(int i = 0; i < rSelectUseCrd.getDataCount(); i++) {	
			
			/** 조회된 카드사 정보 기준으로 [오픈뱅킹카드고객상세] SELECT INSERT */
			
			LData tmpData = rSelectUseCrd.getLData(i);
			
			iOpnbCrdDtl = new LData();

			iOpnbCrdDtl.setString("오픈뱅킹회원금융회사코드" , tmpData.getString("오픈뱅킹회원금융회사코드"));
	    	iOpnbCrdDtl.setString("CI내용"              , input.getString("CI내용"));	    
	    	iOpnbCrdDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
	    	
	    	try {
	    		
	    		rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl); // [오픈뱅킹카드고객상세] 원장 SELECT
		        
		        rstLData = new LData();
		        
		        rstLData.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
		        rstLData.setString("오픈뱅킹회원금융회사코드"     , tmpData.getString("오픈뱅킹회원금융회사코드"));
		        rstLData.setString("CI내용"                 , input.getString("CI내용"));
		        rstLData.setString("오픈뱅킹플랫폼식별번호"      , input.getString("오픈뱅킹플랫폼식별번호"));
		        rstLData.setString("준회원식별자"             , rOpnbCrdDtl.getString("준회원식별자"));
		        rstLData.setString("고객식별자"              , rOpnbCrdDtl.getString("고객식별자"));
		        rstLData.setString("카드정보조회동의여부"       , rOpnbCrdDtl.getString("카드정보조회동의여부"));
		        rstLData.setString("카드정보조회동의갱신일시"    , rOpnbCrdDtl.getString("카드정보조회동의갱신일시"));
		        rstLData.setString("카드정보조회동의해제일시"    , rOpnbCrdDtl.getString("카드정보조회동의해제일시"));
		        
		        if("Y".equals(rOpnbCrdDtl.getString("카드정보조회동의여부"))) {
		        	rInsCrdDtlGrid.addLData(rstLData);
		        }
		        
	    	} catch (LNotFoundException nfe) { // 카드사 동의정보가 존재하지 않는다면
	    		
	    		LData iInsCrdDtl = new LData();
		    	iInsCrdDtl.setString("오픈뱅킹회원금융회사코드" , tmpData.getString("오픈뱅킹회원금융회사코드"));
		    	iInsCrdDtl.setString("CI내용"              , input.getString("CI내용"));	    
		    	iInsCrdDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
		    	iInsCrdDtl.setString("오픈뱅킹플랫폼식별번호"   , input.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsCrdDtl.setString("카드정보조회동의갱신일시" , DateUtil.getDateTimeStr());
		    	iInsCrdDtl.setString("카드정보조회동의여부"    , "Y");
		    	
		    	// 타 채널에서 기 등록한  SELECT INSERT
		        int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertOpnbCrdDtlChnPr" , iInsCrdDtl);
		        
		        if(rtnReg > 0) {
		        	
		        	rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl);
			        
			        rstLData = new LData();
			        
			        rstLData.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
			        rstLData.setString("오픈뱅킹회원금융회사코드"     , tmpData.getString("오픈뱅킹회원금융회사코드"));
			        rstLData.setString("CI내용"                 , input.getString("CI내용"));
			        rstLData.setString("오픈뱅킹플랫폼식별번호"      , input.getString("오픈뱅킹플랫폼식별번호"));
			        rstLData.setString("준회원식별자"             , rOpnbCrdDtl.getString("준회원식별자"));
			        rstLData.setString("고객식별자"              , rOpnbCrdDtl.getString("고객식별자"));
			        rstLData.setString("카드정보조회동의여부"       , rOpnbCrdDtl.getString("카드정보조회동의여부"));
			        rstLData.setString("카드정보조회동의갱신일시"    , rOpnbCrdDtl.getString("카드정보조회동의갱신일시"));
			        rstLData.setString("카드정보조회동의해제일시"    , rOpnbCrdDtl.getString("카드정보조회동의해제일시"));
			        
			        rInsCrdDtlGrid.addLData(rstLData);
			        
			        /** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
					  
				    LData iRegOpnbCstCnsPhsIn = new LData(); 
				    
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , userSeqNo);
				    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
					iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
				  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
					
				  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
				  	
				  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
			        
		        }
	    	}
		}
		
		/** OUTPUT SETTING (타 채널에서 복사 된  정보) */
		
		result.setInt("그리드_cnt", rInsCrdDtlGrid.getDataCount());
		result.set("그리드", rInsCrdDtlGrid);
		
		return result;
       
    }
    
    /**
     * - 오픈뱅킹에 타 채널에서 기 등록한 카드정보조회 동의 정보를 복사하여 요청 채널 정보로 등록한다.
     * 
     * 1. 기 등록된 타 채널 동의 정보 조회하여 요청이 들어온 채널세부업무구분코드로 변경하여 오픈뱅킹카드고객상세 원장 적재
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹고객정보기본, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세
     * <INPUT>
     *  CI내용, 채널세부업무구분코드, 오픈뱅킹플랫폼식별번호, 해지카드포함여부
     * <OUTPUT>
     *  채널세부업무구분코드, 오픈뱅킹회원금융회사코드, CI내용, 오픈뱅킹플랫폼식별번호, 준회원식별자, 고객식별자, 카드정보조회동의여부, 카드정보조회동의갱신일시, 카드정보조회동의해제일시
     * 
     * @ServiceID UBF2030309
     * @method prcRgCrdInfChnCpy
     * @method(한글명) 등록카드정보채널복사처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcRgCrdInfChnCpy(LData input) throws LException {
    	LData result = new LData();
    	LData iOpnbCrdDtl = new LData();
		LData rOpnbCrdDtl = new LData();
		
		LData rstLData = null;
		
   	 	String userSeqNo = "";
   	
   	 	if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {	
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
 		}
       
   	 	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
   	 	
   	 	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹플랫폼식별번호"))) {
   	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹플랫폼식별번호"));
		}
   	 	
   	 	if(StringUtil.trimNisEmpty(input.getString("해지카드포함여부"))) {
	 		input.setString("해지카드포함여부", "N");
		}
   	
        /** CI내용으로 조회 요청 */
   	 	
		try {
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
			
			LData iSelectUsrUno = new LData();
			LData rSelectUsrUno = new LData();
			
			iSelectUsrUno.setString("CI내용", input.getString("CI내용"));
			
			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
			userSeqNo = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
			
         	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
         	LLog.debug.println(input);
       		
 		} catch(LException e) {
 			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
 		}
		
		/** 요청 사용자의 카드정보조회동의 = Y 인 카드사 정보 조회 */
		
		LData iSelectUseCrd = new LData();
		LMultiData rSelectUseCrd = new LMultiData();
		
		iSelectUseCrd.setString("오픈뱅킹사용자고유번호" , userSeqNo);
		iSelectUseCrd.setString("카드정보조회동의여부"  , "Y");
		
		rSelectUseCrd = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdCstBasWhlInq" , iSelectUseCrd);
		
		if(rSelectUseCrd.getMaxDataCount() == 0) {
			throw new LBizException(ObsErrCode.ERR_7001.getCode() , StringUtil.mergeStr( "오픈뱅킹에 등록된 " , ObsErrCode.ERR_7001.getName() ,"(", ObsErrCode.ERR_7001.getCode(),")"));
		}
		
		LMultiData rInsCrdDtlGrid = new LMultiData();
		
		for(int i = 0; i < rSelectUseCrd.getDataCount(); i++) {	
			
			/** 조회된 카드사 정보 기준으로 [오픈뱅킹카드고객상세] SELECT INSERT */
			
			LData tmpData = rSelectUseCrd.getLData(i);
			
			iOpnbCrdDtl = new LData();

			iOpnbCrdDtl.setString("오픈뱅킹회원금융회사코드" , tmpData.getString("오픈뱅킹회원금융회사코드"));
	    	iOpnbCrdDtl.setString("CI내용"              , input.getString("CI내용"));	    
	    	iOpnbCrdDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
	    	
	    	try {
	    		
	    		rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl); // [오픈뱅킹카드고객상세] 원장 SELECT
		        
		        rstLData = new LData();
		        
		        rstLData.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
		        rstLData.setString("오픈뱅킹회원금융회사코드"     , tmpData.getString("오픈뱅킹회원금융회사코드"));
		        rstLData.setString("CI내용"                 , input.getString("CI내용"));
		        rstLData.setString("오픈뱅킹플랫폼식별번호"      , input.getString("오픈뱅킹플랫폼식별번호"));
		        rstLData.setString("준회원식별자"             , rOpnbCrdDtl.getString("준회원식별자"));
		        rstLData.setString("고객식별자"              , rOpnbCrdDtl.getString("고객식별자"));
		        rstLData.setString("카드정보조회동의여부"       , rOpnbCrdDtl.getString("카드정보조회동의여부"));
		        rstLData.setString("카드정보조회동의갱신일시"    , rOpnbCrdDtl.getString("카드정보조회동의갱신일시"));
		        rstLData.setString("카드정보조회동의해제일시"    , rOpnbCrdDtl.getString("카드정보조회동의해제일시"));
		        
		        if("Y".equals(input.getString("해지카드포함여부"))) {
		        	
		        	if("N".equals(rOpnbCrdDtl.getString("카드정보조회동의여부"))) {
		        		
		        		LData iUpdCrdDtl = new LData();
				    	iUpdCrdDtl.setString("채널세부업무구분코드"      , input.getString("채널세부업무구분코드"));
				    	iUpdCrdDtl.setString("오픈뱅킹회원금융회사코드"   , tmpData.getString("오픈뱅킹회원금융회사코드"));
				    	iUpdCrdDtl.setString("CI내용"               , input.getString("CI내용"));
				    	iUpdCrdDtl.setString("카드정보조회동의여부"      , "Y"); // 해지되어있는 정보 Y 처리
				    	iUpdCrdDtl.setString("카드정보조회동의갱신일시"   , DateUtil.getDateTimeStr());
				    	
				    	try {
				    		
				    		int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdDtlUseYn" , iUpdCrdDtl);
				    		
				    		rstLData.setString("카드정보조회동의여부"       , "Y"); // 업데이트 되었으므로
				    		rstLData.setString("카드정보조회동의갱신일시"    , DateUtil.getDateTimeStr());
				    		
				    	} catch (LException e) {
				    		throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "카드고객상세변경(updateCrdDtlUseYn) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
				    	}
				    	
				    	/** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
						  
					    LData iRegOpnbCstCnsPhsIn = new LData(); 
					    
					    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , userSeqNo);
					    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
					    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
					    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
						iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
					  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
						
					  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
					  	
					  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
		        		
		        	}
		        	
		        	rInsCrdDtlGrid.addLData(rstLData);
		        	
		        } else {
		        	
		        	if("Y".equals(rOpnbCrdDtl.getString("카드정보조회동의여부"))) {
			        	rInsCrdDtlGrid.addLData(rstLData);
			        }
		        	
		        }
		        
	    	} catch (LNotFoundException nfe) { // 카드사 동의정보가 존재하지 않는다면
	    		
	    		LData iInsCrdDtl = new LData();
		    	iInsCrdDtl.setString("오픈뱅킹회원금융회사코드" , tmpData.getString("오픈뱅킹회원금융회사코드"));
		    	iInsCrdDtl.setString("CI내용"              , input.getString("CI내용"));	    
		    	iInsCrdDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
		    	iInsCrdDtl.setString("오픈뱅킹플랫폼식별번호"   , input.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsCrdDtl.setString("카드정보조회동의갱신일시" , DateUtil.getDateTimeStr());
		    	iInsCrdDtl.setString("카드정보조회동의여부"    , "Y");
		    	
		        int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "insertOpnbCrdDtlChnPr" , iInsCrdDtl); // 타 채널에서 기 등록한  SELECT INSERT
		        
		        if(rtnReg > 0) {
		        	
		        	rOpnbCrdDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtl" , iOpnbCrdDtl);
			        
			        rstLData = new LData();
			        
			        rstLData.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
			        rstLData.setString("오픈뱅킹회원금융회사코드"     , tmpData.getString("오픈뱅킹회원금융회사코드"));
			        rstLData.setString("CI내용"                 , input.getString("CI내용"));
			        rstLData.setString("오픈뱅킹플랫폼식별번호"      , input.getString("오픈뱅킹플랫폼식별번호"));
			        rstLData.setString("준회원식별자"             , rOpnbCrdDtl.getString("준회원식별자"));
			        rstLData.setString("고객식별자"              , rOpnbCrdDtl.getString("고객식별자"));
			        rstLData.setString("카드정보조회동의여부"       , rOpnbCrdDtl.getString("카드정보조회동의여부"));
			        rstLData.setString("카드정보조회동의갱신일시"    , rOpnbCrdDtl.getString("카드정보조회동의갱신일시"));
			        rstLData.setString("카드정보조회동의해제일시"    , rOpnbCrdDtl.getString("카드정보조회동의해제일시"));
			        
			        rInsCrdDtlGrid.addLData(rstLData);
			        
			        /** [오픈뱅킹고객동의이력] 원장 동의 이력 적재 */
					  
				    LData iRegOpnbCstCnsPhsIn = new LData(); 
				    
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , userSeqNo);
				    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , input.getString("채널세부업무구분코드"));
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
				    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
					iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "1"); //1.약관동의, 3: 약관해지
				  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(input.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
					
				  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
				  	
				  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
			        
		        }
	    	}
		}
		
		/** OUTPUT SETTING (타 채널에서 복사 된  정보) */
		
		result.setInt("그리드_cnt", rInsCrdDtlGrid.getDataCount());
		result.set("그리드", rInsCrdDtlGrid);
		
		return result;
       
    }
    
    /**
     * - 해당 채널에 등록돼있는 오픈뱅킹 카드 목록 상세조회
     * 
     * 1. 채널세부업무구분코드, 오픈뱅킹카드식별자, 오픈뱅킹이용기관코드, 오픈뱅킹카드일련번호로 조회 요청
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹카드발급기관상세, UBF오픈뱅킹보유카드정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 채널세부업무구분코드
     * <OUTPUT>
     * 오픈뱅킹카드일련번호, 오픈뱅킹개설기관코드, 오픈뱅킹카드식별자, 오픈뱅킹마스킹카드번호, 카드상품명, 가족카드여부, 오픈뱅킹카드구분코드, 결제은행코드, 오픈뱅킹마스킹계좌번호, 카드발급년월일, 오픈뱅킹카드결제일
     * 
     * @ServiceID UBF2030305
     * @method retvChnPrCrdCtg
     * @method(한글명) 채널별 카드 목록 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvChnPrCrdCtg(LData input) throws LException {
        
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("채널별카드목록조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
    	LData iRetvChnPrCrdCtgP = input; // i채널별카드목록조회입력
        LData rRetvChnPrCrdCtgP = new LData(); // r채널별카드목록조회출력
    	
        try {
			
        	// Validation Check
    		if(StringUtil.trimNisEmpty(iRetvChnPrCrdCtgP.getString("조회채널세부업무구분코드"))) {
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvChnPrCrdCtgP.getString("오픈뱅킹사용자고유번호"))) {
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
    		if(StringUtil.trimNisEmpty(iRetvChnPrCrdCtgP.getString("채널세부업무구분코드"))) {
    			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
        	
        	// Ebc 호출
        	LData iSelectCrdCstCtgIn = new LData(); // 카드고객목록조회입력
        	LMultiData rSelectCrdCstCtgOut = new LMultiData(); // 카드고객목록조회출력
        	iSelectCrdCstCtgIn.setString("오픈뱅킹사용자고유번호",		iRetvChnPrCrdCtgP.getString("오픈뱅킹사용자고유번호"));
        	iSelectCrdCstCtgIn.setString("채널세부업무구분코드",		iRetvChnPrCrdCtgP.getString("채널세부업무구분코드"));
        	
        	try {
        		rSelectCrdCstCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectCrdCstCtg", iSelectCrdCstCtgIn);
    		} catch (LException e) {
    			throw new LBizException(ObsErrCode.ERR_3001.getCode(), ObsErrCode.ERR_3001.getName());
    		}
    		
    		if(rSelectCrdCstCtgOut.getDataCount() == 0) {
        		throw new LBizException(ObsErrCode.ERR_7001.getCode() , StringUtil.mergeStr(ObsErrCode.ERR_7001.getName())); // 카드사 정보가 존재하지 않습니다.
    		}
    		
    		rRetvChnPrCrdCtgP.setInt("그리드1_cnt", rSelectCrdCstCtgOut.getDataCount()); // 카드고객 개수
    		
        	LMultiData tmpLMultiData = new LMultiData();
        	
        	for(int i=0; i<rSelectCrdCstCtgOut.getDataCount(); i++) {
        		
        		// Ebc 호출
        		LData iSelectChnPrCrdCtgIn = new LData(); // 채널별카드목록조회입력
            	LMultiData rSelectChnPrCrdCtgOut = new LMultiData(); // 채널별카드목록조회출력
            	iSelectChnPrCrdCtgIn.setString("오픈뱅킹회원금융회사코드",		rSelectCrdCstCtgOut.getLData(i).getString("오픈뱅킹회원금융회사코드"));
            	iSelectChnPrCrdCtgIn.setString("오픈뱅킹사용자고유번호",		iRetvChnPrCrdCtgP.getString("오픈뱅킹사용자고유번호"));
            	iSelectChnPrCrdCtgIn.setString("채널세부업무구분코드",		iRetvChnPrCrdCtgP.getString("채널세부업무구분코드"));
            	
            	try {
            		rSelectChnPrCrdCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectChnPrCrdCtg", iSelectChnPrCrdCtgIn);
    			} catch (LException e) {
    				throw new LBizException(ObsErrCode.ERR_3002.getCode(), ObsErrCode.ERR_3002.getName());
    			}
            	
            	LData tmprRetvChnPrCrdCtgP = new LData();
            	tmprRetvChnPrCrdCtgP.setString("오픈뱅킹회원금융회사코드", rSelectCrdCstCtgOut.getLData(i).getString("오픈뱅킹회원금융회사코드"));
            	tmprRetvChnPrCrdCtgP.setInt("그리드2_cnt", rSelectChnPrCrdCtgOut.getDataCount());
            	
//            	if(rSelectChnPrCrdCtgOut.getDataCount() == 0) {
//            		continue;
//            	}
            	
            	LMultiData tmprSelectChnPrCrdCtgOut = new LMultiData();
            	
            	for(int j=0; j<rSelectChnPrCrdCtgOut.getDataCount(); j++) {
            		
            		LData tmpLData = new LData();
            		
            		tmpLData.setString("오픈뱅킹회원금융회사코드",		rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹회원금융회사코드"));
            		tmpLData.setString("오픈뱅킹사용자고유번호",		rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹사용자고유번호"));
            		tmpLData.setString("오픈뱅킹카드식별자",			rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹카드식별자"));
            		tmpLData.setString("오픈뱅킹개설기관코드",		rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹개설기관코드"));
            		tmpLData.setString("오픈뱅킹마스킹카드번호",		rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹마스킹카드번호"));
            		tmpLData.setString("카드상품명",				rSelectChnPrCrdCtgOut.getLData(j).getString("카드상품명"));
            		tmpLData.setString("가족카드여부",				rSelectChnPrCrdCtgOut.getLData(j).getString("가족카드여부"));
            		tmpLData.setString("오픈뱅킹카드구분코드",		rSelectChnPrCrdCtgOut.getLData(j).getString("오픈뱅킹카드구분코드"));
            		tmpLData.setString("결제은행코드",				rSelectChnPrCrdCtgOut.getLData(j).getString("결제은행코드"));
            		tmpLData.setString("마스킹카드결제계좌번호",		rSelectChnPrCrdCtgOut.getLData(j).getString("마스킹카드결제계좌번호"));
            		tmpLData.setString("카드발급년월일",			rSelectChnPrCrdCtgOut.getLData(j).getString("카드발급년월일"));
            		
            		tmprSelectChnPrCrdCtgOut.addLData(tmpLData);
            		
            	}
            	
            	tmprRetvChnPrCrdCtgP.set("그리드2", tmprSelectChnPrCrdCtgOut);
            	
            	tmpLMultiData.addLData(tmprRetvChnPrCrdCtgP);
        	}
        	
        	rRetvChnPrCrdCtgP.set("그리드1", tmpLMultiData);
        	
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}

    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("----------[rRetvChnPrCrdCtgP]----------");
    		LLog.debug.println(rRetvChnPrCrdCtgP);
    		LLog.debug.println("채널별카드목록조회 END ☆★☆☆★☆☆★☆" );
    	}
		
        return rRetvChnPrCrdCtgP;
    }

    /**
     * - 요청된 카드사 정보 조회 동의 여부(제3자정보제공동의)를 해지하며, 모든 채널의 카드 정보 조회 동의 여부가 해지되었을 경우 해당 카드사에 대해서 오픈뱅킹센터에 등록했던 사용자등록(제3자정보제공동의)을 해지.
     * 
     * 1. 오픈뱅킹카드발급기관상세 원장의 채널별 동의 여부 정보 수정
     * 1-1. 특정 채널만 해지되었을 경우
     *  - 오픈뱅킹카드발급기관상세 원장 동의 정보 변경
     * 1-2. 전체 채널이 해지되었을 경우
     *  - 카드조회해지API 호출 후 오픈뱅킹카드발급기관상세, 오픈뱅킹카드발급기관기본 원장 동의 정보 변경(해지)
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹카드발급기관기본, UBF오픈뱅킹카드발급기관상세
     * <INPUT>
     *  채널세부업무구분코드, 오픈뱅킹사용자고유번호, 제3자정보제공동의여부(해지), 오픈뱅킹개설기관코드, 오픈뱅킹회원금융회사코드, 거래고유번호
     * <OUTPUT>
     *  
     * @ServiceID UBF2030306
     * @method prcTmnCrdInq
     * @method(한글명) 카드조회 해지처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcTmnCrdInq(LData iClosData) throws LException {
    	LData result = new LData();
        LData apiBody = new LData();
        LData callOutput = new LData();
        
	    String userSeqNo = ""; //오픈뱅킹사용자고유번호
	    String ciCtt = ""; //CI내용
	    String chnDtlsBwkDtcd = ""; //채널세부구분코드
        
	 	/** INPUT VALIDATION */
	
	    
		if(StringUtil.trimNisEmpty(iClosData.getString("채널세부업무구분코드"))) {
		 	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));
		}
	 		
		if(StringUtil.trimNisEmpty(iClosData.getString("CI내용"))) {
		 	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));
		}
	 		
	    if(StringUtil.trimNisEmpty(iClosData.getString("오픈뱅킹개설기관코드"))) {
	        throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹개설기관코드"));
		}
	        
		if(StringUtil.trimNisEmpty(iClosData.getString("오픈뱅킹회원금융회사코드"))) {
		 	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹회원금융회사코드"));
		}
			
		if(StringUtil.trimNisEmpty(ciCtt)) {			
			ciCtt = iClosData.getString("CI내용");
		}
	 		
		if(StringUtil.trimNisEmpty(chnDtlsBwkDtcd)) {			
		 	chnDtlsBwkDtcd = iClosData.getString("채널세부업무구분코드");
		}
	 		
		/** CI내용으로 조회 요청 */
			
		if(StringUtil.trimNisEmpty(iClosData.getString("오픈뱅킹사용자고유번호")) && StringUtil.trimNisEmpty(iClosData.getString("CI내용"))) { //입력값에 사용자 고유번호와 CI내용 둘다 없을 경우
	    	throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호, CI내용"));
	    }
	     	
	    LData iSelectUsrUno = new LData();
	    
	    if(StringUtil.trimNisEmpty(iClosData.getString("오픈뱅킹사용자고유번호"))) { //입력값에 CI내용만 있을 경우 사용자고유번호 조회 
	    	
	     	iSelectUsrUno.setString("CI내용", iClosData.getString("CI내용"));
	     		
	     	try {
	     		
	     	    OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	            userSeqNo = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno).getString("오픈뱅킹사용자고유번호");
	             			
	            LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
	            LLog.debug.println(userSeqNo);
	            		
	      	} catch(LException e) {
	      		throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
	      	}
	     	
	    } else {
	     	userSeqNo = iClosData.getString("오픈뱅킹사용자고유번호");
	    }
	     	
	 	/** [오픈뱅킹카드고객상세] 원장 동의여부 N 처리 */
	 		
	 	LData iUpdCrdDtl = new LData();
	    	
	    iUpdCrdDtl.setString("채널세부업무구분코드"      , iClosData.getString("채널세부업무구분코드"));
	    iUpdCrdDtl.setString("오픈뱅킹회원금융회사코드"   , iClosData.getString("오픈뱅킹회원금융회사코드"));
	    iUpdCrdDtl.setString("CI내용"               , iClosData.getString("CI내용"));
	    iUpdCrdDtl.setString("카드정보조회동의여부"      , "N"); // 카드해지처리는 카드정보조회동의여부 = N 이라는 묵시적 동의
	    iUpdCrdDtl.setString("카드정보조회동의해제일시"   , DateUtil.getDateTimeStr());
	    
	    /** 금결원 오류 시 Exception 발생하지 않더라도 원장 롤백 처리 위해서 */
	    
	    LNestedTransactionManager ntxManager = new LNestedTransactionManager();
    	ntxManager.nestedBegin();    // 트랜잭션 시작

	    int rtnUpd  = 0;
	    
	    try {
	   
	    	rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdDtlUseYn" , iUpdCrdDtl);
		 		
			if(rtnUpd == 0) {		 

		    	result.setString("API응답코드_V5"     , "ZZZZZ");
				result.setString("API응답메시지_V300" , "[오픈뱅킹카드고객상세] 원장 해당 채널 동의 정보 없음");
				
			} else {
				
			    int rgCrdCnt = 0;
			    
			    /** [오픈뱅킹카드고객상세] 원장 동의여부 Y인 카드사수 COUNT */	
			    	
			    LData iAlCrdData = new LData();  
			    LData rAlCrdData = new LData();
			    
			    iAlCrdData.setString("오픈뱅킹회원금융회사코드"  , iClosData.getString("오픈뱅킹회원금융회사코드"));
			    iAlCrdData.setString("CI내용"              , iClosData.getString("CI내용"));
			    iAlCrdData.setString("카드정보조회동의여부"     , "Y");
			    
			    rAlCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtlRgAccnt" , iAlCrdData);
			    rgCrdCnt = rAlCrdData.getInt("등록카드사수");
	
			    if(rgCrdCnt == 0) { // 등록카드사수가 0일경우
			    	
			        /** 거래고유번호 채번  */
			        
				    LData rCdMg = new LData();        	
				    OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
				    
				    rCdMg = opnbCdMg.crtTrUno(new LData());
				    
				    String rspCd = ""; //응답코드
				    String bankRspCd = ""; //참가기관응답코드
				    
				    apiBody.setString("채널세부업무구분코드"      , iClosData.getString("채널세부업무구분코드"));// 채널세부업무구분코드
				    
				    apiBody.setString("bank_tran_id"        , rCdMg.getString("거래고유번호"));// 참가기관거래고유번호
				    apiBody.setString("user_seq_no"         , userSeqNo);// 오픈뱅킹사용자고유번호 
				 	apiBody.setString("bank_code_std"       , iClosData.getString("오픈뱅킹개설기관코드"));// 오픈뱅킹개설기관코드
				 	apiBody.setString("member_bank_code"    , iClosData.getString("오픈뱅킹회원금융회사코드"));// 오픈뱅킹회원금융회사코드
				 	
				 	/** API CALL */
					
				    OpnbApiCpbc opnbApiCpbc = new OpnbApiCpbc();
				    
				    callOutput = opnbApiCpbc.closCrdInqAPICall(apiBody);
				    
				    /** [오픈뱅킹카드고객기본] 원장 카드정보조회동의여부 = N 처리 */
				    
				    rspCd = callOutput.getString("rsp_code");
				    bankRspCd = callOutput.getString("bank_rsp_code");
				    
				    result.setString("API거래고유번호_V40" , callOutput.getString("api_tran_id"));
				    result.setString("API거래일시_V17"    , callOutput.getString("api_tran_dtm"));
				    result.setString("API응답코드_V5"     , callOutput.getString("rsp_code"));
					result.setString("API응답메시지_V300" , callOutput.getString("rsp_message"));
	
				 	if(("A0000".equals(rspCd) && "000".equals(bankRspCd)) || 
				 			("A0002".equals(rspCd) && ("551".equals(bankRspCd) || "555".equals(bankRspCd) || "556".equals(bankRspCd))) ){ //정상
				 	
				 		try {
				 				
					 		LData iUpdOpnbCrdBas = new LData();	
	
					 		iUpdOpnbCrdBas.setString("오픈뱅킹회원금융회사코드"    , iClosData.getString("오픈뱅킹회원금융회사코드"));
					 		iUpdOpnbCrdBas.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
					 		iUpdOpnbCrdBas.setString("카드정보조회동의여부"       , "N");
					 		iUpdOpnbCrdBas.setString("카드정보조회동의해제일시"    , DateUtil.getDateTimeStr());
					 		iUpdOpnbCrdBas.setString("조회해제채널세부업무구분코드" , iClosData.getString("채널세부업무구분코드"));
					 			
					 		int rtnUpdUseYn = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "updateCrdBasUseYn" , iUpdOpnbCrdBas);
				 		 	
				 		} catch(LException e) {
				 			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "카드고객기본변경(updateCrdBasUseYn) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));
				 		}
				 		
				 		result.setString("참가기관거래고유번호_V20" , callOutput.getString("bank_tran_id"));
						result.setString("참가기관거래일자_V8"     , callOutput.getString("bank_tran_date"));
						result.setString("참가기관표준코드_V3"     , callOutput.getString("bank_code_tran"));
						result.setString("참가기관응답코드_V3"     , callOutput.getString("bank_rsp_code"));
						result.setString("참가기관응답메시지_V100" , callOutput.getString("bank_rsp_message"));
				 				
				 	} else {
				 		ntxManager.nestedRollback();  // 에러 발생 시 트랜잭션 rollback
				 	}
				 	
			    } else {// 등록카드사수(상세)가 0보다 크다면
			    	LLog.debug.println("카드 상세 카드 존재 로그 :" + iClosData);
			    	result.setString("API응답코드_V5"     , "ZZZZZ");
					result.setString("API응답메시지_V300" , "[오픈뱅킹카드고객상세] 원장 채널 동의 정보 변경(N)");
			    }
			        
			} //수정한 카드 건수가 존재한다면
		
	    } catch(LException e) {
	    	
	    	ntxManager.nestedRollback();  // 에러 발생 시 트랜잭션 rollback
	    	
	    } finally {
	    	
	    	ntxManager.nestedCommit();  // 트랜잭션 commit 
    		ntxManager.nestedRelease();   // 트랜잭션 release
    		
	    }
        
	 	/** 계좌/카드 전체 해지 시 자동으로 오픈뱅킹 사용자 탈퇴 절차가 이루어지는 것으로 보아 사용자 탈퇴 API 별도 호출 X */
	    
	    /** [오픈뱅킹카드고객상세] 원장 조회하여 채널별 사용여부 Y인 건수 조회 */
	 	
    	LData iUseChnCrdData = new LData();  
        LData rUseChnCrdData = new LData();
        
        iUseChnCrdData.setString("채널세부업무구분코드"  , iClosData.getString("채널세부업무구분코드"));
        iUseChnCrdData.setString("CI내용"           , iClosData.getString("CI내용"));
        iUseChnCrdData.setString("카드정보조회동의여부"  , "Y");
        
        rUseChnCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbCrdMgEbc", "selectOpnbCrdDtlChnRgAccnt" , iUseChnCrdData);
        int useChnCrdCnt = rUseChnCrdData.getInt("채널사용가능건수");
	    
        if(useChnCrdCnt == 0) { // 해당 채널에서 사용 가능한 카드사가 존재하지 않는다면

        	/** [오픈뱅킹고객동의이력] 원장 동의 이력 적재(해지) */
			  
		    LData iRegOpnbCstCnsPhsIn = new LData(); 
		    
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , userSeqNo);
		    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , iClosData.getString("채널세부업무구분코드"));
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "4"); // 3: 계좌사용동의, 4:카드조회동의
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
			iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "3"); //1.약관동의, 3: 약관해지
		  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , StringUtil.initEmptyValue(iClosData.getString("오픈뱅킹동의자료구분코드"),"6") ); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
			
		  	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
		  	
		  	opnbCstMgCpbc.opnbCstCnsPhsRg(iRegOpnbCstCnsPhsIn);
		  	
        }
        
	 	/** [오픈뱅킹계좌기본], [오픈뱅킹카드고객기본] 원장 조회하여 사용여부 Y인 건수 조회 */
	 	
    	LData iUseCrdData = new LData();  
        LData rUseCrdData = new LData();
        iUseCrdData.setString("오픈뱅킹사용자고유번호" , userSeqNo);  
        
        rUseCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectOpnbAccCrdUseAblNcn" , iUseCrdData);
        int useCrdCnt = rUseCrdData.getInt("사용가능건수");
        
        /** 오픈뱅킹 고객정보 파기 */
        
        if(useCrdCnt == 0) { //사용 가능한 카드사가 존재하지 않는다면
        	
        	LData iDelCrdDtl = new LData();
        	iDelCrdDtl.setString("오픈뱅킹사용자고유번호", userSeqNo);
        	iDelCrdDtl.setString("CI내용"           , ciCtt);
	    
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
        	opnbCstMgCpbc.delOpnbCstPsnInf(iDelCrdDtl);
	  	  	  	  		        	
        }
        
        /** OUTPUT SETTING */
       
        return result;
    }
   
}

