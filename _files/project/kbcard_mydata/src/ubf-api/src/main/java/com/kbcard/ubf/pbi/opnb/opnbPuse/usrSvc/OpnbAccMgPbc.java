package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST;
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
 * opnbAccMgPbc
 * 
 * @logicalname  : 오픈뱅킹계좌관리Pbc
 * @author       : 정영훈
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       정영훈       최초 생성
 *
 */

public class OpnbAccMgPbc {

    /**
     * - UBF오픈뱅킹계좌기본,상세 테이블에 등록된 계좌를 채널별로 목록조회한다.
     * 
     * 1.오픈뱅킹계좌기본,상세 조회시 등록된 계좌 채널별로 목록조회
     * 2.응답값 리턴
     * * 금결원에 등록된 계좌목록은 등록계좌조회API를 이용하여 조회 
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * CI내용,해지계좌포함여부,정렬순서,채널세부구분코드
     * <OUTPUT>
     * 채널세부업무구분코드,오픈뱅킹계좌일련번호,오픈뱅킹계좌상세일련번호,고객계좌번호,핀테크이용번호,납부자번호,오픈뱅킹계좌종류구분코드,오픈뱅킹명의구분코드,오픈뱅킹계좌상품명,오픈뱅킹이메일주소,계좌조회동의여부,출금동의여부,대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,계좌숨김일시,즐겨찾기계좌여부
     * 
     * @serviceID UBF2030201
     * @logicalName 채널별 등록계좌 목록조회
     * @method retvChnPrRgAccCtg
     * @method(한글명) 채널별 등록계좌 목록조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvChnPrRgAccCtg(LData input) throws LException {
      
    	LData tmpData = new LData();
    	LData rstLData = new LData();
    	
    	LData result = new LData();
    	LMultiData rsltLMultiData = new LMultiData();
    	  
        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
        
		LData iretvRgAccAPICall = new LData();
		LData rRetvRgAccAPICall = new LData();
		
		LData iOpnbAccDtl = new LData();
		LData rOpnbAccDtl = new LData();
		
		String opnbUsrUno = "";
		String opnbUtzInsCd = UBF_CONST.AuthInfo.UTZ_INS_CD.getCode();//오픈뱅킹이용기관코드
        
	    if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {			
			LLog.debug.println("로그 " + input.getString("CI내용"));
			throw new LBizException( ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
		}
      
        if(StringUtil.trimNisEmpty(input.getString("해지계좌포함여부_V1"))) {			
			LLog.debug.println("로그 " + input.getString("해지계좌포함여부_V1"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 해지계좌포함여부"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-해지계좌포함여부" ));//해지계좌포함여부가 존재하지 않습니다.
		}
         
        if(StringUtil.trimNisEmpty(input.getString("조회정렬구분코드_V1"))) {			
			LLog.debug.println("로그 " + input.getString("조회정렬구분코드_V1"));	
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 조회정렬구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-정렬순서" ));//정렬순서가 존재하지 않습니다.
		}
       
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.			
		}
       
        //사용자고유번호 조회
		try {
			
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();		
	    
			LData iSelectUsrUno = new LData();
			LData rSelectUsrUno = new LData();
			
			iSelectUsrUno.setString("CI내용"            , input.getString("CI내용"));
			iSelectUsrUno.setString("채널세부업무구분코드"  , input.getString("채널세부업무구분코드"));
			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
			opnbUsrUno = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
			
         	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
       	    LLog.debug.println(input);
       		
 		} catch(LException e) {
 			
 			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
 		}
		
       //등록계좌조회 API호출
       iretvRgAccAPICall.setString("user_seq_no"       , opnbUsrUno); //사용자일련번호
       iretvRgAccAPICall.setString("include_cancel_yn" , input.getString("해지계좌포함여부_V1")); //해지계좌포함여부_V1 ‘Y’:해지계좌포함, ‘N’:해지계좌불포함      
       iretvRgAccAPICall.setString("sort_order"        , input.getString("조회정렬구분코드_V1")); //조회정렬구분코드_V1 ‘D’:Descending, ‘A’:Ascending
       iretvRgAccAPICall.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드")); //채널세부업무구분코드
       rRetvRgAccAPICall = opnbApi.retvRgAccAPICall(iretvRgAccAPICall); //등록계좌조회 API호출
       String rspCd = rRetvRgAccAPICall.getString("rsp_code");
       
       if("A0000".equals(rspCd)) { //정상이라면
       
	       int resListCnt = rRetvRgAccAPICall.getInt("res_cnt");
	       
	       result.setString("API거래고유번호_V40"   , rRetvRgAccAPICall.getString("api_tran_id"));
	       result.setString("API거래일시_V17"      , rRetvRgAccAPICall.getString("api_tran_dtm"));
	       result.setString("API응답코드_V5"       , rRetvRgAccAPICall.getString("rsp_code"));
	       result.setString("API응답메시지_V300"   , rRetvRgAccAPICall.getString("rsp_message"));
	       result.setString("고객명"              , rRetvRgAccAPICall.getString("user_name"));//고객명 50
	       
	       LMultiData tmpLMultiDat = (LMultiData)rRetvRgAccAPICall.get("res_list");
	       
	       for(int i = 0 ; i < resListCnt; i++ ) {    	   
	    	   tmpData = tmpLMultiDat.getLData(i);    	   
	    	   rstLData = new LData();
	    	  
	    	   rstLData.setString("핀테크이용번호"           , tmpData.getString("fintech_use_num"));//핀테크이용번호 24
	    	   rstLData.setString("계좌별명"               , tmpData.getString("account_alias"));//계좌별명(Alias) 100
	    	   rstLData.setString("계좌개설은행코드"         , tmpData.getString("bank_code_std"));//출금(개설)기관.대표코드 - 계좌개설은행코드
	    	   rstLData.setString("개별저축은행명"           , tmpData.getString("savings_bank_name"));//개별저축은행명 50
	    	   rstLData.setString("고객계좌번호"            , tmpData.getString("account_num"));//고객계좌번호 20
	    	   rstLData.setString("계좌납입회차번호"          , tmpData.getString("account_seq"));//계좌납입회차번호 3
	    	   rstLData.setString("오픈뱅킹계좌명의구분코드"    , tmpData.getString("account_holder_type"));//계좌구분(P:개인) 1
	    	   rstLData.setString("오픈뱅킹계좌종류구분코드"    , tmpData.getString("account_type"));//오픈뱅킹계좌종류구분코드 (‘1’:수시입출금, ‘2’:예적금, ‘6’:수익증권, ‘T’:종합계좌) 1
	    	   rstLData.setString("계좌조회동의여부"          , tmpData.getString("inquiry_agree_yn"));//계좌조회동의여부  1
	    	   rstLData.setString("계좌조회동의등록일시"       , tmpData.getString("inquiry_agree_dtime"));//계좌조회동의등록일시 14 
	    	   rstLData.setString("출금동의여부"             , tmpData.getString("transfer_agree_yn"));//출금동의여부  1
	    	   rstLData.setString("출금동의갱신일시"          , tmpData.getString("transfer_agree_dtime"));//출금동의갱신일시 14
	    	   rstLData.setString("계좌상태구분코드_V2"       , tmpData.getString("account_state"));//계좌상태 ‘01’:사용, ‘09’:해지 2
	    	   rstLData.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
	    	   rstLData.setString("CI내용"                 , input.getString("CI내용"));
	    	   rstLData.setString("오픈뱅킹개설기관명"        , tmpData.getString("bank_name"));//출금(개설)기관명 - 오픈뱅킹개설기관명 50
	    	   rstLData.setString("마스킹계좌번호_V20"       , tmpData.getString("account_num_masked"));//마스킹된 출력용 계좌번호 20
	    	   rstLData.setString("오픈뱅킹등록계좌예금주명"    , tmpData.getString("account_holder_name"));//계좌예금주명 50
	    	   rstLData.setString("계좌개설부점코드_V7"      , tmpData.getString("bank_code_sub"));//출금(개설)기관.점별코드 - 계좌개설부점코드
	    	       	   	    	   
	    	   //채널별등록계좌상세조회
	    	   iOpnbAccDtl = new LData();
			   //iOpnbAccDtl.setString("고객계좌번호"       , CryptoDataUtil.encryptKey( tmpLData.getString("account_num") , false, CryptoDataUtil.KB_BD_NORMAL_KEY));//계좌번호 암호화
			   iOpnbAccDtl.setString("고객계좌번호"         , tmpData.getString("account_num"));
			   iOpnbAccDtl.setString("계좌개설은행코드"      , tmpData.getString("bank_code_std"));
			   iOpnbAccDtl.setString("오픈뱅킹이용기관코드"   , opnbUtzInsCd);
			   iOpnbAccDtl.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
			   iOpnbAccDtl.setString("CI내용"             , input.getString("CI내용"));
				
			   try {
			   
				   rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectChnPrRgAccDtl" , iOpnbAccDtl);
			 	   //계좌기본
				   rstLData.setString("고객계좌번호"                 , tmpData.getString("account_num"));//고객계좌번호 20
		    	   rstLData.setString("계좌개설은행코드"              , tmpData.getString("bank_code_std"));//출금(개설)기관.대표코드 - 계좌개설은행코드
		    	   rstLData.setString("오픈뱅킹이용기관코드"           , rOpnbAccDtl.getString("오픈뱅킹이용기관코드"));
		    	   rstLData.setString("오픈뱅킹사용자고유번호"         , rOpnbAccDtl.getString("오픈뱅킹사용자고유번호"));
		    	   rstLData.setString("핀테크이용번호"               , tmpData.getString("fintech_use_num"));//핀테크이용번호 24
		    	   rstLData.setString("오픈뱅킹납부자번호"            , rOpnbAccDtl.getString("오픈뱅킹납부자번호"));
		    	   rstLData.setString("오픈뱅킹계좌종류구분코드"        , tmpData.getString("account_type"));//오픈뱅킹계좌종류구분코드 (‘1’:수시입출금, ‘2’:예적금, ‘6’:수익증권, ‘T’:종합계좌) 1
		    	   rstLData.setString("오픈뱅킹계좌명의구분코드"        , tmpData.getString("account_holder_type"));//계좌구분(P:개인) 1
		    	   rstLData.setString("오픈뱅킹계좌상품명"            , rOpnbAccDtl.getString("오픈뱅킹계좌상품명"));		    	   
		    	   rstLData.setString("오픈뱅킹이메일주소전문내용"      , rOpnbAccDtl.getString("오픈뱅킹이메일주소전문내용"));
		    	   //rstLData.setString("오픈뱅킹이메일주소"            , CryptoDataUtil.decryptKey(rOpnbAccDtl.getString("오픈뱅킹이메일주소"), CryptoDataUtil.KB_BD_NORMAL_KEY));//이메일 복호화
		    	   
		    	   rstLData.setString("계좌조회동의여부"             , tmpData.getString("inquiry_agree_yn"));//계좌조회동의여부  1
		    	   rstLData.setString("계좌조회동의등록일시"          , tmpData.getString("inquiry_agree_dtime"));//계좌조회동의등록일시 14 
		    	   rstLData.setString("조회등록채널세부업무구분코드"    , rOpnbAccDtl.getString("조회등록채널세부업무구분코드"));
		    	   rstLData.setString("계좌조회동의갱신일시"          , rOpnbAccDtl.getString("계좌조회동의갱신일시"));
		    	   rstLData.setString("조회갱신채널세부업무구분코드"    , rOpnbAccDtl.getString("조회갱신채널세부업무구분코드"));
		    	   rstLData.setString("계좌조회동의해제일시"          , rOpnbAccDtl.getString("계좌조회동의해제일시"));
		    	   rstLData.setString("조회해제채널세부업무구분코드"    , rOpnbAccDtl.getString("조회해제채널세부업무구분코드"));
		    	   rstLData.setString("출금동의여부"               , tmpData.getString("transfer_agree_yn"));//출금동의여부  1
		    	   rstLData.setString("출금등록채널세부업무구분코드"    , rOpnbAccDtl.getString("출금등록채널세부업무구분코드"));
		    	   rstLData.setString("출금동의갱신일시"             , tmpData.getString("transfer_agree_dtime"));//출금동의갱신일시 14
		    	   rstLData.setString("출금갱신채널세부업무구분코드"    , rOpnbAccDtl.getString("출금갱신채널세부업무구분코드"));
		    	   rstLData.setString("출금동의해제일시"             , rOpnbAccDtl.getString("출금동의해제일시"));
		    	   rstLData.setString("출금해제채널세부업무구분코드"    , rOpnbAccDtl.getString("출금해제채널세부업무구분코드"));
		    	   rstLData.setString("계좌등록일시"               , rOpnbAccDtl.getString("계좌등록일시"));
		    	   rstLData.setString("계좌사용여부"               , rOpnbAccDtl.getString("계좌사용여부"));
		    	   rstLData.setString("계좌출금동의등록일시"         , rOpnbAccDtl.getString("계좌출금동의등록일시"));
		    	   
		    	   rstLData.setString("오픈뱅킹개설기관명"          , tmpData.getString("bank_name"));//출금(개설)기관명 - 오픈뱅킹개설기관명 50
		    	   rstLData.setString("마스킹계좌번호_V20"         , tmpData.getString("account_num_masked"));//마스킹된 출력용 계좌번호 20
		    	   rstLData.setString("오픈뱅킹등록계좌예금주명"      , tmpData.getString("account_holder_name"));//계좌예금주명 50
		    	   rstLData.setString("계좌개설부점코드_V7"         , tmpData.getString("bank_code_sub"));//출금(개설)기관.점별코드 - 계좌개설부점코드
		    	   rstLData.setString("계좌상태구분코드_V2"         , tmpData.getString("account_state"));//계좌상태 ‘01’:사용, ‘09’:해지 2
		    		
   		    	   //계좌상세
    		       rstLData.setString("채널세부업무구분코드"           , rOpnbAccDtl.getString("채널세부업무구분코드"));
		    	   rstLData.setString("CI내용"                    , rOpnbAccDtl.getString("CI내용"));
		    	   rstLData.setString("준회원식별자"                , rOpnbAccDtl.getString("준회원식별자"));
		    	   rstLData.setString("고객식별자"                  , rOpnbAccDtl.getString("고객식별자"));
		    	   rstLData.setString("오픈뱅킹플랫폼식별번호"         , rOpnbAccDtl.getString("오픈뱅킹플랫폼식별번호"));
		    	   rstLData.setString("계좌납입회차번호"            , tmpData.getString("account_seq"));//계좌납입회차번호 3
		    	   rstLData.setString("개별저축은행명"             , tmpData.getString("savings_bank_name"));//개별저축은행명 50
		    	   rstLData.setString("대표계좌여부"              , rOpnbAccDtl.getString("대표계좌여부"));
		    	   rstLData.setString("계좌별명"                 , tmpData.getString("account_alias"));//계좌별명(Alias) 100
		    	   rstLData.setInt("계좌표시순서"                 , rOpnbAccDtl.getInt("계좌표시순서"));
		    	   rstLData.setString("계좌숨김여부"              , rOpnbAccDtl.getString("계좌숨김여부"));
		    	   rstLData.setString("계좌숨김일시"              , rOpnbAccDtl.getString("계좌숨김일시"));
		    	   rstLData.setString("즐겨찾기계좌여부"           , rOpnbAccDtl.getString("즐겨찾기계좌여부"));
		    	   rstLData.setString("화면노출버튼구분코드"        , rOpnbAccDtl.getString("화면노출버튼구분코드"));
		    	   rstLData.setString("계좌상세사용여부"           , rOpnbAccDtl.getString("계좌상세사용여부"));
		    	   rstLData.setString("개별저축은행코드"           , rOpnbAccDtl.getString("개별저축은행코드"));
		    	   rsltLMultiData.addLData(rstLData);
				   
			   }catch(LNotFoundException e){
				   
				   rsltLMultiData.addLData(rstLData);				  
			   }    	   
	    	   
	       }//end_for
        
	       result.setInt("GRID_cnt"    , resListCnt);//등록계좌수
	       result.set("GRID",rsltLMultiData);
	       
       }else {
    	   
    	   result.setString("API거래고유번호_V40"   , rRetvRgAccAPICall.getString("api_tran_id"));
	       result.setString("API거래일시_V17"      , rRetvRgAccAPICall.getString("api_tran_dtm"));
	       result.setString("API응답코드_V5"       , rRetvRgAccAPICall.getString("rsp_code"));
	       result.setString("API응답메시지_V300"   , rRetvRgAccAPICall.getString("rsp_message"));
	       result.setString("고객명"              , rRetvRgAccAPICall.getString("user_name"));//고객명 50
	       result.setInt("GRID_cnt"             , 0);
       }
        
       return result;
    }

    /**
     * - 등록된 계좌의 정보를 변경 
     * - 이메일변경시 오픈뱅킹계좌기본수정 및 계좌변경 API호출
     * 
     * 1. 거래고유번호, 오픈뱅킹사용자일련번호, 개설기관표준코드, 계좌번호, 회차번호, 서비스구분, 변경할 이메일주소로  API 호출
     *  - 오픈뱅킹사용자일련번호, 개설기관표준코드, 계좌번호, 회차번호, 서비스구분이 일치하는 계좌에 대해 이메일주소를 변경함
     * 2-1. 성공 응답
     * 2-2. 오류 응답
     *  - 오픈뱅킹센터로부터 응답을 받지 못했을 경우 계좌정보조회 API를 통해 처리 결과 확인 -> 정보변경 실패시 계좌정보변경 API 재호출
     * 3. 오픈뱅킹고객정보, 오픈뱅킹계좌기본 테이블의 이메일주소 변경
     * 4. 응답값 리턴
     *
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 고객계좌번호,계좌개설은행코드,오픈뱅킹금융기관코드,오픈뱅킹계좌일련번호,오픈뱅킹사용자고유번호,오픈뱅킹이메일주소
     * 
     * @serviceID UBF2030203
     * @logicalName 계좌정보 변경
     * @method chngAccInf
     * @method(한글명) 계좌정보 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData chngAccInf(LData input) throws LException {
	   LData result = new LData();
       
       LData iOpnbAccBasDtl = new LData();
       LData rOpnbAccBasDtl = new LData();
       
       LData iChngAccInfApiCall = new LData();
       LData rChngAccInfApiCall = new LData();
       
       String opnbUsrUno = ""; //사용자고유번호
       
       if(StringUtil.trimNisEmpty(input.getString("고객계좌번호"))) {
			LLog.debug.println("로그 " + input.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객계좌번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
	   }
      
       if(StringUtil.trimNisEmpty(input.getString("계좌개설은행코드"))) {
			LLog.debug.println("로그 " + input.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 계좌개설은행코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
	   }
      
       if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {			
			LLog.debug.println("로그 " + input.getString("CI내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
	   }
      
       if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹이메일주소전문내용"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹이메일주소전문내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹이메일주소전문내용"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹이메일주소전문내용" ));//오픈뱅킹이메일주소전문내용가 존재하지 않습니다.
	   }
       
       if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
	   }

       //사용자고유번호 조회
 		try {
 			
 			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
 	    
 			LData iSelectUsrUno = new LData();
 			LData rSelectUsrUno = new LData();
 			
 			iSelectUsrUno.setString("CI내용"            , input.getString("CI내용"));
 			iSelectUsrUno.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
 			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
 			opnbUsrUno = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
 			
          	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
        	LLog.debug.println(input);
        		
  		} catch(LException e) {
  			
  			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회" , ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
  		}
       
       //계좌기본상세조회
       try {
       	
       		iOpnbAccBasDtl.setString("고객계좌번호"        , input.getString("고객계좌번호"));
       		iOpnbAccBasDtl.setString("계좌개설은행코드"     , input.getString("계좌개설은행코드"));
       		iOpnbAccBasDtl.setString("오픈뱅킹이용기관코드"  , UBF_CONST.AuthInfo.UTZ_INS_CD.getCode());
       		iOpnbAccBasDtl.setString("오픈뱅킹사용자고유번호" , opnbUsrUno);
       
       		rOpnbAccBasDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectOpnbAccBasDtl" , iOpnbAccBasDtl);
       
       } catch (LNotFoundException nfe) {
			
    	    throw new LBizException(ObsErrCode.ERR_7000.getCode() , StringUtil.mergeStr(ObsErrCode.ERR_7000.getName(),"(" , ObsErrCode.ERR_7000.getCode(),")"));//계좌가 존재하지 않습니다.
	   }
       
       String accInqagYn = rOpnbAccBasDtl.getString("계좌조회동의여부"); //제3자제공동의(조회동의)가 Y일경우 만 API이메일변경
       
       String afEmailAdr = StringUtil.trim(input.getString("오픈뱅킹이메일주소전문내용"));
       //String bfEmailAdr = rOpnbAccBasDtl.getString("오픈뱅킹이메일주소");
       
	   if("Y".equals(accInqagYn)) {
   	    
           	OpnbApiCpbc opnbApi = new OpnbApiCpbc();
           	
       		// 전문거래고유번호 생성 Cpbc 호출
           	LData rCdMg = new LData();        	
           	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
           	
           	rCdMg = opnbCdMg.crtTrUno(new LData());
           	
			/** apiBody Set */
           	iChngAccInfApiCall.setString("bank_tran_id"     , rCdMg.getString("거래고유번호"));//거래고유번호
           	iChngAccInfApiCall.setString("user_seq_no"      , opnbUsrUno);//사용자고유번호
           	iChngAccInfApiCall.setString("bank_code_std"    , input.getString("계좌개설은행코드"));//개설기관표준코드
           	//iChngAccInfApiCall.setString("account_num"      , CryptoDataUtil.decryptKey(input.getString("고객계좌번호") , CryptoDataUtil.KB_BD_NORMAL_KEY));//고객계좌번호
           	iChngAccInfApiCall.setString("account_num"      , input.getString("고객계좌번호"));//고객계좌번호
           	
            if(!StringUtil.trimNisEmpty(input.getString("계좌납입회차번호"))) {    	   
            	iChngAccInfApiCall.setString("account_seq"  , input.getString("계좌납입회차번호"));//계좌납입회차번호
        	}

           	iChngAccInfApiCall.setString("scope"            , "inquiry");//서비스구분 - 조회서비스로 등록(제3자정보제공동의여부 = ‘Y’)된 계좌에 대해 이메일 주소를 변경할 수 있음
           	//iChngAccInfApiCall.setString("update_user_email",  CryptoDataUtil.decryptKey(afEmailAdr, CryptoDataUtil.KB_BD_NORMAL_KEY));//이메일 복호화
           	iChngAccInfApiCall.setString("update_user_email", afEmailAdr);//이메일주소
           	iChngAccInfApiCall.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
           	
           	LLog.debug.println("계좌변경 API호출 ================ " + iChngAccInfApiCall);
           	//계좌정보변경 API호출
            rChngAccInfApiCall = opnbApi.chngAccInfAPICall(iChngAccInfApiCall); //계좌변경 API호출
           	
           	if("A0000".equals(rChngAccInfApiCall.getString("rsp_code")) 
           			&& "000".equals(rChngAccInfApiCall.getString("bank_rsp_code"))) {

             	  //계좌기본정보수정 (이메일주소)
                   try {
                   	   
                   		iOpnbAccBasDtl.setString("오픈뱅킹이메일주소전문내용"   , afEmailAdr);
                   		iOpnbAccBasDtl.setString("시스템최종갱신식별자"       , "UBF2030203");
                   	
                   		BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccInf" , iOpnbAccBasDtl);
                   
                   } catch (LBizException e) {
           			
                	    throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "계좌기본정보수정(updateAccInf) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
           				
                   }
                   
           	}
           	
           	result.setString("API거래고유번호_V40"    , rChngAccInfApiCall.getString("api_tran_id"));
           	result.setString("API거래일시_V17"       , rChngAccInfApiCall.getString("api_tran_dtm"));
           	result.setString("API응답코드_V5"        , rChngAccInfApiCall.getString("rsp_code"));
           	result.setString("API응답메시지_V300"     , rChngAccInfApiCall.getString("rsp_message"));
           	result.setString("참가기관거래고유번호_V20" , rChngAccInfApiCall.getString("bank_tran_id"));
           	result.setString("참가기관거래일자_V8"     , rChngAccInfApiCall.getString("bank_tran_date"));
           	result.setString("참가기관표준코드_V3"     , rChngAccInfApiCall.getString("bank_code_tran"));
           	result.setString("참가기관응답코드_V3"     , rChngAccInfApiCall.getString("bank_rsp_code"));
           	result.setString("참가기관응답메시지_V100"  , rChngAccInfApiCall.getString("bank_rsp_message"));
           	result.setString("참가기관명_V20"         , rChngAccInfApiCall.getString("bank_name"));
           	result.setString("개별저축은행명_V20"      , rChngAccInfApiCall.getString("savings_bank_name"));
           	result.setString("오픈뱅킹사용자고유번호"    , rChngAccInfApiCall.getString("user_seq_no"));
           	
           	result.setString("고객계좌번호"           , rChngAccInfApiCall.getString("account_num"));
           	//result.setString("고객계좌번호"           , CryptoDataUtil.encryptKey(rChngAccInfApiCall.getString("account_num") , false , CryptoDataUtil.KB_BD_NORMAL_KEY));
           	result.setString("회차번호_V3"           , rChngAccInfApiCall.getString("account_seq"));
           	result.setString("오픈뱅킹계좌종류구분코드"   , rChngAccInfApiCall.getString("account_type"));
           	result.setString("서비스구분_V8"          , rChngAccInfApiCall.getString("scope"));
           	result.setString("오픈뱅킹이메일주소전문내용" , rChngAccInfApiCall.getString("update_user_email"));
           	//result.setString("오픈뱅킹이메일주소전문내용" , CryptoDataUtil.encryptKey(rChngAccInfApiCall.getString("update_user_email") , false , CryptoDataUtil.KB_BD_NORMAL_KEY));
           	
           	return result;
       }
     
	   LLog.debug.println("출력 ☆★☆☆★☆☆★☆ : " +  result);
 
       return result;
    }

    /**
     * - 채널별 등록된 상세계좌의 정보를 변경
     * - 이메일이외 변경사항은 오픈뱅킹계좌상세 변경
     * 
     * 1. 대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,즐겨찾기계좌여부 수정
     * 2. 결과값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 거래고유번호,계좌개설은행코드,계좌납입회자,
     * 오픈뱅킹사용자고유번호,채널세부업무구분코드,고객계좌번호, 이메일주소, 대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,즐겨찾기계좌여부
     * 
     * @serviceID UBF2030202
     * @logicalName 계좌상세정보변경
     * @method chngAccDtlInf
     * @method(한글명) 계좌상세정보 변경
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData chngAccDtlInf(LData input) throws LException {
	   LData result = new LData();
	   int updTotCnt = 0;

       //계좌상세정보수정 (대표계좌여부,계좌별명,계좌표시순서,계좌숨김여부,즐겨찾기계좌여부,화면노출버튼구분코드)	   
	   LMultiData accDtlInfo = (LMultiData)input.get("GRID");
	   
	   for(int i =0; i < accDtlInfo.getDataCount(); i++) {		
		   
		   LData iInputData = accDtlInfo.getLData(i);
		   
		   if(StringUtil.trimNisEmpty(iInputData.getString("고객계좌번호"))) {
				LLog.debug.println("로그 " + iInputData.getString("고객계좌번호"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객계좌번호"));//처리중 오류가 발생했습니다.
				//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
			}
		   
	       if(StringUtil.trimNisEmpty(iInputData.getString("채널세부업무구분코드"))) {
				LLog.debug.println("로그 " + iInputData.getString("채널세부업무구분코드"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
				//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
			}
	      
	       if(StringUtil.trimNisEmpty(iInputData.getString("계좌개설은행코드"))) {
				LLog.debug.println("로그 " + iInputData.getString("계좌개설은행코드"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 계좌개설은행코드"));//처리중 오류가 발생했습니다.
				//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
			}
	      
	       if(StringUtil.trimNisEmpty(iInputData.getString("CI내용"))) {			
				LLog.debug.println("로그 " + iInputData.getString("CI내용"));
				throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
				//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
			}
		   
	       try {
	       	
		       	LData iUpdOpnbAccDtl = new LData();
		       	iUpdOpnbAccDtl.setString("채널세부업무구분코드"   , iInputData.getString("채널세부업무구분코드"));
		       	iUpdOpnbAccDtl.setString("고객계좌번호"         , iInputData.getString("고객계좌번호"));
		       	iUpdOpnbAccDtl.setString("계좌개설은행코드"      , iInputData.getString("계좌개설은행코드"));
		       	iUpdOpnbAccDtl.setString("오픈뱅킹이용기관코드"   , UBF_CONST.AuthInfo.UTZ_INS_CD.getCode());
		       	iUpdOpnbAccDtl.setString("CI내용"            , iInputData.getString("CI내용"));
		       	
		       	iUpdOpnbAccDtl.setString("대표계좌여부"        , iInputData.getString("대표계좌여부"));
		       	iUpdOpnbAccDtl.setString("계좌별명"           , iInputData.getString("계좌별명"));
		       	iUpdOpnbAccDtl.setString("계좌표시순서"        , iInputData.getString("계좌표시순서"));
		       	iUpdOpnbAccDtl.setString("계좌숨김여부"        , iInputData.getString("계좌숨김여부"));
		       	iUpdOpnbAccDtl.setString("즐겨찾기계좌여부"     , iInputData.getString("즐겨찾기계좌여부"));
		       	iUpdOpnbAccDtl.setString("화면노출버튼구분코드"  , iInputData.getString("화면노출버튼구분코드"));
		       	iUpdOpnbAccDtl.setString("시스템최종갱신식별자"   , "UBF2030202");
		       	
		       	int updCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtl" , iUpdOpnbAccDtl);
		       	updTotCnt = updTotCnt + updCnt;
	       
	        } catch (LException e) {
			
	           throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "계좌기본상세정보수정(updateAccDtl) ", ObsErrCode.ERR_7777.getName(), "(", ObsErrCode.ERR_7777.getCode(), ")" ));//처리중 오류가 발생했습니다.
	   		} 
		   
	   }
	   
		if(updTotCnt == 0 ) {
			result.setString("처리결과_V1", "N");
		}else {
			result.setString("처리결과_V1", "Y");
		}
	   
       return result;
    }    
    
    
    /**
     * - 사용자가 등록한 계좌의 정보를 조회
     * 
     * 1. 오픈뱅킹사용자고유번호, 고객계좌번호, 계좌개설은행코드 입력
     * 2-1. 성공 응답
     *  - 오픈뱅킹계좌종류구분코드, 오픈뱅킹계좌상품명 리턴
     * 2-2. 오류 응답
     *  - 사용자탈퇴 API 요청 후 계좌 해지 처리전 계좌정보조회 요청하게 되면, '사용자탈퇴 처리중인 계좌(A0019)'로 조회 거부 응답
     *  - 응답코드(참가기관) 000 이면 동의, 551,555,556이면 동의거부, 이외코드 조회 불능
     * 3. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 고객계좌번호, 계좌개설은행코드
     * <OUTPUT>
     * LIST
     *   - 오픈뱅킹계좌일련번호, 핀테크이용번호, 납부자번호, 오픈뱅킹계좌종류구분코드, 오픈뱅킹계좌명의구분코드, 오픈뱅킹계좌상품명, 계좌조회동의여부, 계좌조회동의등록일시, 조회등록채널세부업무구분코드, 계좌조회동의갱신일시, 조회갱신채널세부업무구분코드, 계좌조회동의해제일시, 조회해제채널세부업무구분코드, 출금동의여부, 출금등록채널세부업무구분코드, 출금동의갱신일시, 출금갱신채널세부업무구분코드, 출금동의해제일시, 출금해제채널세부업무구분코드, 계좌등록일시, 계좌사용여부, 권유자부점코드, 권유자부점명, 권유직원번호
     * 
     * @serviceID UBF2030204
     * @method retvAccInf
     * @method(한글명) 계좌정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAccInf(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("계좌정보조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}

    	LData iRetvAccInfP = input; //i계좌정보조회입력
        LData rRetvAccInfP = new LData(); //r계좌정보조회출력
        LData iRetvAccInfAPICall = new LData(); // 계좌정보조회입력
  		LData rRetvAccInfAPICall = new LData(); // 계좌정보조회출력
  		
  		try {
  			
  			//Validation Check
  	    	if(StringUtil.trimNisEmpty(iRetvAccInfP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
  	 			throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(), " - 채널세부업무구분코드"));
  			}
  	    	if(StringUtil.trimNisEmpty(iRetvAccInfP.getString("오픈뱅킹사용자고유번호"))) {			
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(), " - 오픈뱅킹사용자고유번호"));
  			}
  	    	if(StringUtil.trimNisEmpty(iRetvAccInfP.getString("계좌개설은행코드"))) {			
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(), " - 계좌개설은행코드"));
  			}
  	    	if(StringUtil.trimNisEmpty(iRetvAccInfP.getString("고객계좌번호"))) {			
  				throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(), " - 고객계좌번호"));
  			}
  	    	if(StringUtil.trimNisEmpty(iRetvAccInfP.getString("서비스구분_V8"))) {			
  	    		throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(), " - 서비스구분"));
  	    	}
  	    	
  	    	//API 호출
  	  		OpnbApiCpbc opnbApi = new OpnbApiCpbc();
  	  		OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc();
  	  		
  	  		iRetvAccInfAPICall.setString("채널세부업무구분코드",		iRetvAccInfP.getString("채널세부업무구분코드")); // 채널세부업무구분코드
  	  		iRetvAccInfAPICall.setString("bank_tran_id",		opnbCdMgCpbc.crtTrUno(input).getString("거래고유번호")); // 참가기관 거래고유번호
  	  		iRetvAccInfAPICall.setString("user_seq_no",			iRetvAccInfP.getString("오픈뱅킹사용자고유번호")); // 사용자고유번호
  	  		iRetvAccInfAPICall.setString("bank_code_std",		iRetvAccInfP.getString("계좌개설은행코드")); // 개설기관 표준코드 
  	  		iRetvAccInfAPICall.setString("account_num",			iRetvAccInfP.getString("고객계좌번호")); // 계좌번호
  	  		
  	  		if(!StringUtil.trimNisEmpty(iRetvAccInfP.getString("계좌납입회차번호"))) {
  	  			iRetvAccInfAPICall.setString("account_seq",		iRetvAccInfP.getString("계좌납입회차번호")); // 회차번호
  	  		}
  	  		
  	  		iRetvAccInfAPICall.setString("scope",				iRetvAccInfP.getString("서비스구분_V8")); // 서비스구분 / inquiry:조회서비스 / transfer:출금서비스
  	        
  			rRetvAccInfAPICall = opnbApi.retvAccInfAPICall(iRetvAccInfAPICall);			
  	  		
  	  		if("A0000".equals(rRetvAccInfAPICall.getString("rsp_code"))) {
  				rRetvAccInfP.setString("API거래고유번호_V40",		rRetvAccInfAPICall.getString("api_tran_id"));
  				rRetvAccInfP.setString("API거래일시_V17",			rRetvAccInfAPICall.getString("api_tran_dtm"));
  				rRetvAccInfP.setString("API응답코드_V5",			rRetvAccInfAPICall.getString("rsp_code"));
  				rRetvAccInfP.setString("API응답메시지_V300",		rRetvAccInfAPICall.getString("rsp_message"));
  				rRetvAccInfP.setString("참가기관거래고유번호_V20",	rRetvAccInfAPICall.getString("bank_tran_id"));
  				rRetvAccInfP.setString("참가기관거래일자_V8",		rRetvAccInfAPICall.getString("bank_tran_date"));
  				rRetvAccInfP.setString("계좌개설은행코드",			rRetvAccInfAPICall.getString("bank_code_tran"));
  				rRetvAccInfP.setString("참가기관응답코드_V3",		rRetvAccInfAPICall.getString("bank_rsp_code"));
  				rRetvAccInfP.setString("참가기관응답메시지_V100",		rRetvAccInfAPICall.getString("bank_rsp_message"));
  				rRetvAccInfP.setString("오픈뱅킹개설기관명",			rRetvAccInfAPICall.getString("bank_name"));
  				rRetvAccInfP.setString("개별저축은행명",			rRetvAccInfAPICall.getString("savings_bank_name"));
  				rRetvAccInfP.setString("오픈뱅킹사용자고유번호",		rRetvAccInfAPICall.getString("user_seq_no"));
  				rRetvAccInfP.setString("고객계좌번호",				rRetvAccInfAPICall.getString("account_num"));
  				rRetvAccInfP.setString("계좌납입회차번호",			rRetvAccInfAPICall.getString("account_seq"));
  				rRetvAccInfP.setString("오픈뱅킹계좌종류구분코드",		rRetvAccInfAPICall.getString("account_type"));
  				rRetvAccInfP.setString("서비스구분_V8",			rRetvAccInfAPICall.getString("scope"));
  				rRetvAccInfP.setString("핀테크이용번호",			rRetvAccInfAPICall.getString("fintech_use_num"));
  				rRetvAccInfP.setString("마스킹계좌번호_V20",		rRetvAccInfAPICall.getString("account_num_masked"));
  				rRetvAccInfP.setString("오픈뱅킹납부자번호",			rRetvAccInfAPICall.getString("payer_num"));
  				rRetvAccInfP.setString("제3자정보제공동의여부",		rRetvAccInfAPICall.getString("inquiry_agree_yn"));
  				rRetvAccInfP.setString("출금동의여부",				rRetvAccInfAPICall.getString("transfer_agree_yn"));
  				rRetvAccInfP.setString("오픈뱅킹이메일주소전문내용",	rRetvAccInfAPICall.getString("user_email"));
  	  		}
  	  		else {
  	  			// 참가기관 응답 오류
	  	  		if("A0002".equals(rRetvAccInfAPICall.getString("rsp_code"))) {
	  	  			throw new LBizException(rRetvAccInfAPICall.getString("bank_rsp_code"), StringUtil.mergeStr(rRetvAccInfAPICall.getString("rsp_message"), " [", rRetvAccInfAPICall.getString("bank_rsp_message"), "]"));
	 	  		}
  	  			// API 응답 오류
  	  			throw new LBizException(rRetvAccInfAPICall.getString("rsp_code"), rRetvAccInfAPICall.getString("rsp_message"));
  	  		}
  	  		
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
    	
  		if(LLog.debug.isEnabled()) {
  			LLog.debug.println("----------[rRetvAccInfP]----------");
  			LLog.debug.println(rRetvAccInfP);
  			LLog.debug.println("계좌정보조회 END ☆★☆☆★☆☆★☆" );
  		}
		
        return rRetvAccInfP;
    }

    /**
     * - 해당 채널에 등록돼있는 오픈뱅킹 계좌 목록 상세조회
     * 
     * 1. 채널세부업무구분코드, 고객계좌번호, 계좌개설은행코드, 오픈뱅킹금융기관코드, 오픈뱅킹계좌일련번호로 조회 요청
     * 2. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호, 채널별세부업무구분코드
     * <OUTPUT>
     * LIST
     *   -고객계좌번호, 계좌개설은행코드, 오픈뱅킹금융기관코드, CI내용, 오픈뱅킹계좌상세일련번호, 준회원식별자, 고객식별자, 오픈뱅킹플랫폼식별번호, 계좌납입회차번호, 개별저축은행명, 출금동의여부, 출금동의갱신일시, 
     *    출금동의해제일시, 대표계좌여부, 계좌별명, 계좌표시순서, 계좌숨김여부, 계좌숨김일시, 즐겨찾기계좌여부, 화면노출버튼구분코드, 계좌상세사용여부, 개별저축은행코드
     * 
     * @serviceID UBF2030205
     * @method retvChnPrAccInfCtg
     * @method(한글명) 채널별 계좌정보 목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvChnPrAccInfCtg(LData input) throws LException {
        
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("채널별계좌정보목록조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
    	LData iRetvChnPrAccInfCtgP = input; // i채널별계좌정보목록조회입력
        LData rRetvChnPrAccInfCtgP = new LData(); // r채널별계좌정보목록조회출력
        
        try {
        	
        	// Validation Check
        	if(StringUtil.trimNisEmpty(iRetvChnPrAccInfCtgP.getString("조회채널세부업무구분코드"))) { // Pbc를 호출한 채널의 세부업무구분코드
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	if(StringUtil.trimNisEmpty(iRetvChnPrAccInfCtgP.getString("오픈뱅킹사용자고유번호"))) {
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	if(StringUtil.trimNisEmpty(iRetvChnPrAccInfCtgP.getString("채널세부업무구분코드"))) { // 계좌정보목록을 조회하고자 하는 채널의 세부업무구분코드
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	
        	// Ebc 호출
        	LData iSelectChnPrAccInfCtgIn = new LData(); // 채널별계좌정보목록조회입력
        	LMultiData rSelectChnPrAccInfCtgOut = new LMultiData(); // 채널별계좌정보목록조회출력
        	
        	iSelectChnPrAccInfCtgIn.setString("채널세부업무구분코드",		iRetvChnPrAccInfCtgP.getString("채널세부업무구분코드"));
        	iSelectChnPrAccInfCtgIn.setString("오픈뱅킹사용자고유번호",	iRetvChnPrAccInfCtgP.getString("오픈뱅킹사용자고유번호"));
        	
        	try {
        		rSelectChnPrAccInfCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectChnPrAccInfCtg", iSelectChnPrAccInfCtgIn);
			} catch (LException e) {
				throw new LBizException(ObsErrCode.ERR_2002.getCode(), ObsErrCode.ERR_2002.getName());
			}
        	
        	if(rSelectChnPrAccInfCtgOut.getDataCount() == 0) {
        		throw new LBizException(ObsErrCode.ERR_7000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_7000.getName())); // 계좌가 존재하지 않습니다.
        	}
        	
        	rRetvChnPrAccInfCtgP.setInt("그리드_cnt", rSelectChnPrAccInfCtgOut.getDataCount());
        	
        	LMultiData tmprSelectChnPrRgAccCtgOut = new LMultiData();
        	
        	for(int j=0; j<rSelectChnPrAccInfCtgOut.getDataCount(); j++) {
        		
        		LData tmpLData = new LData();
        		
        		tmpLData.setString("고객계좌번호",				rSelectChnPrAccInfCtgOut.getLData(j).getString("고객계좌번호"));
        		tmpLData.setString("계좌개설은행코드",			rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌개설은행코드"));
        		tmpLData.setString("채널세부업무구분코드",		rSelectChnPrAccInfCtgOut.getLData(j).getString("채널세부업무구분코드"));
        		tmpLData.setString("오픈뱅킹이용기관코드",		rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹이용기관코드"));
        		tmpLData.setString("오픈뱅킹사용자고유번호",		rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹사용자고유번호"));
        		tmpLData.setString("핀테크이용번호",			rSelectChnPrAccInfCtgOut.getLData(j).getString("핀테크이용번호"));
        		tmpLData.setString("오픈뱅킹납부자번호",			rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹납부자번호"));
        		tmpLData.setString("오픈뱅킹계좌종류구분코드",		rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹계좌종류구분코드"));
        		tmpLData.setString("오픈뱅킹계좌명의구분코드",		rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹계좌명의구분코드"));
        		tmpLData.setString("오픈뱅킹계좌상품명",			rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹계좌상품명"));
        		tmpLData.setString("오픈뱅킹이메일주소전문내용",	rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹이메일주소전문내용"));
        		tmpLData.setString("계좌조회동의여부",			rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌조회동의여부"));
        		tmpLData.setString("계좌조회동의등록일시",		rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌조회동의등록일시"));
        		tmpLData.setString("조회등록채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("조회등록채널세부업무구분코드"));
        		tmpLData.setString("계좌조회동의갱신일시",		rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌조회동의갱신일시"));
        		tmpLData.setString("조회갱신채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("조회갱신채널세부업무구분코드"));
        		tmpLData.setString("계좌조회동의해제일시",		rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌조회동의해제일시"));
        		tmpLData.setString("조회해제채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("조회해제채널세부업무구분코드"));
        		tmpLData.setString("출금동의여부",				rSelectChnPrAccInfCtgOut.getLData(j).getString("출금동의여부"));
        		tmpLData.setString("출금등록채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("출금등록채널세부업무구분코드"));
        		tmpLData.setString("출금동의갱신일시",			rSelectChnPrAccInfCtgOut.getLData(j).getString("출금동의갱신일시"));
        		tmpLData.setString("출금갱신채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("출금갱신채널세부업무구분코드"));
        		tmpLData.setString("출금동의해제일시",			rSelectChnPrAccInfCtgOut.getLData(j).getString("출금동의해제일시"));
        		tmpLData.setString("출금해제채널세부업무구분코드",	rSelectChnPrAccInfCtgOut.getLData(j).getString("출금해제채널세부업무구분코드"));
        		tmpLData.setString("계좌등록일시",				rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌등록일시"));
        		tmpLData.setString("계좌사용여부",				rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌사용여부"));
        		tmpLData.setString("계좌출금동의등록일시",		rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌출금동의등록일시"));
        		tmpLData.setString("계좌납입회차번호",			rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌납입회차번호"));
        		tmpLData.setString("CI내용",					rSelectChnPrAccInfCtgOut.getLData(j).getString("CI내용"));
        		tmpLData.setString("준회원식별자",				rSelectChnPrAccInfCtgOut.getLData(j).getString("준회원식별자"));
        		tmpLData.setString("고객식별자",				rSelectChnPrAccInfCtgOut.getLData(j).getString("고객식별자"));
        		tmpLData.setString("오픈뱅킹플랫폼식별번호",		rSelectChnPrAccInfCtgOut.getLData(j).getString("오픈뱅킹플랫폼식별번호"));
        		tmpLData.setString("개별저축은행명",			rSelectChnPrAccInfCtgOut.getLData(j).getString("개별저축은행명"));
        		tmpLData.setString("대표계좌여부",				rSelectChnPrAccInfCtgOut.getLData(j).getString("대표계좌여부"));
        		tmpLData.setString("계좌별명",					rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌별명"));
        		tmpLData.setInt("계좌표시순서",					rSelectChnPrAccInfCtgOut.getLData(j).getInt("계좌표시순서"));
        		tmpLData.setString("계좌숨김여부",				rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌숨김여부"));
        		tmpLData.setString("계좌숨김일시",				rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌숨김일시"));
        		tmpLData.setString("즐겨찾기계좌여부",			rSelectChnPrAccInfCtgOut.getLData(j).getString("즐겨찾기계좌여부"));
        		tmpLData.setString("화면노출버튼구분코드",		rSelectChnPrAccInfCtgOut.getLData(j).getString("화면노출버튼구분코드"));
        		tmpLData.setString("계좌상세사용여부",			rSelectChnPrAccInfCtgOut.getLData(j).getString("계좌상세사용여부"));
        		tmpLData.setString("개별저축은행코드",			rSelectChnPrAccInfCtgOut.getLData(j).getString("개별저축은행코드"));
        		
        		tmprSelectChnPrRgAccCtgOut.addLData(tmpLData);
        		
        	}
        	
        	rRetvChnPrAccInfCtgP.set("그리드", tmprSelectChnPrRgAccCtgOut);
			
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
        
        if(LLog.debug.isEnabled()) {
        	LLog.debug.println("----------[rRetvChnPrAccInfCtgP]----------");
        	LLog.debug.println(rRetvChnPrAccInfCtgP);
        	LLog.debug.println("채널별계좌정보목록조회 END ☆★☆☆★☆☆★☆" );
        }
		
        return rRetvChnPrAccInfCtgP;
        
    }

    /**
     * - 오픈뱅킹계좌상세테이블의 모든채널에 계좌가 사용하지 않을경우 센터에 등록 된 사용자의 계좌를 해지(등록 삭제 및 동의해지)
     * 
     * 1. 오픈뱅킹계좌상세 테이블의 계좌사용여부를 Y로 변경
     * 2. 오픈뱅킹계좌상세원장에 계좌사용여부 Y인 건수가 0 인지 체크 0일경우 계좌 API호출  
     * 3. 계좌 API호출 후 계좌 기본 N으로 변경 
     * 4. 계좌 및 카드 계좌수 카드수 체크하여 없으면 고객원장에 삭제   
     * 5-1. 계좌정보로  API 호출 
     *  - 계좌개설기관코드, 계좌번호, 회차번호, 오픈뱅킹사용자일련번호
     * 5-2. 핀테크이용번호로 API 호출
     * 6. 응답정보 확인
     *  - 기 해지 사용자(551), 해당 사용자 없음(555), 사용자 미등록(556)인 경우 해지 완료로 처리
     * 7. 오픈뱅킹 고객정보 테이블, 오픈뱅킹계좌상세 정보 업데이트(해지)
     * 8. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹계좌기본,UBF오픈뱅킹계좌상세
     * <INPUT>
     * 채널세부업무구분코드,고객계좌번호,오픈뱅킹계좌일련번호,오픈뱅킹플랫폼식별번호,오픈뱅킹사용자고유번호,거래고유번호,핀테크이용번호,계좌개설은행코드,계좌납입회차
     * 
     * @serviceID UBF2030206
     * @logicalName 계좌 해지
     * @method closAcc
     * @method(한글명) 계좌 해지
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData closAcc(LData iClosData) throws LException {
        LData result = new LData();
        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
        
		LData iClosAccAPICall = null;
		LData rClosAccAPICall = null;//조회서비스
		LData rOdwClosAccAPICall = null;//출금서비스
		
		String opnbUsrUno = ""; //오픈뱅킹사용자 고유번호
		String ciCtt = ""; //CI내용
		String chnDtlsBwkDtcd = ""; //채널세부구분코드
		int updTotCnt = 0;
	 
	    if(StringUtil.trimNisEmpty(iClosData.getString("고객계좌번호"))) {				
			LLog.debug.println("로그 " + iClosData.getString("고객계좌번호"));	
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객계좌번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
		}
	    
        if(StringUtil.trimNisEmpty(iClosData.getString("계좌개설은행코드"))) {				
			LLog.debug.println("로그 " + iClosData.getString("계좌개설은행코드"));			
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 계좌개설은행코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
		
    	if(StringUtil.trimNisEmpty(iClosData.getString("채널세부업무구분코드"))) {				
			LLog.debug.println("로그 " + iClosData.getString("채널세부업무구분코드"));	
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(iClosData.getString("CI내용"))) {				
			LLog.debug.println("로그 " + iClosData.getString("CI내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
		}

    	String opnbUtzInsCd = UBF_CONST.AuthInfo.UTZ_INS_CD.getCode();//오픈뱅킹이용기관코드
    	
    	//값이 없을경우 move
 		if(StringUtil.trimNisEmpty(opnbUsrUno)) {	
 			
 			//사용자고유번호 조회
 	 		try {
 	 			
 	 			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
 	 	    
 	 			LData iSelectUsrUno = new LData();
 	 			LData rSelectUsrUno = new LData();
 	 			
 	 			iSelectUsrUno.setString("CI내용"           , iClosData.getString("CI내용"));
 	 			iSelectUsrUno.setString("채널세부업무구분코드"  , iClosData.getString("채널세부업무구분코드"));
 	 			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
 	 			opnbUsrUno = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
 	 			
 	          	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
 	        	LLog.debug.println(iClosData);
 	        		
 	  		} catch(LException e) {
 	  			
 	  			throw new LBizException( ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
 	  		}
			
 		}
 			
 		ciCtt = iClosData.getString("CI내용");//CI내용
 		chnDtlsBwkDtcd = iClosData.getString("채널세부업무구분코드");//CI내용
 		
    	LData iUpdAccDtl = new LData();
    	iUpdAccDtl.setString("고객계좌번호"          , iClosData.getString("고객계좌번호"));
    	iUpdAccDtl.setString("계좌개설은행코드"       , iClosData.getString("계좌개설은행코드"));
    	iUpdAccDtl.setString("채널세부업무구분코드"    , iClosData.getString("채널세부업무구분코드"));
    	iUpdAccDtl.setString("오픈뱅킹이용기관코드"    , opnbUtzInsCd);
    	iUpdAccDtl.setString("CI내용"              , iClosData.getString("CI내용"));
    	//계좌상세사용 N으로 UPDATE
    	iUpdAccDtl.setString("계좌상세사용여부"      , "N");
    	iUpdAccDtl.setString("출금동의여부"         , "N");
    	iUpdAccDtl.set("출금동의갱신일시"            , DateUtil.getDateTimeStr());
    	iUpdAccDtl.setString("시스템최초생성식별자"   , "UBF2030206");
    	iUpdAccDtl.setString("시스템최종갱신식별자"   , "UBF2030206");
    	        
    	LNestedTransactionManager ntxManager = new LNestedTransactionManager();
    	ntxManager.nestedBegin();    // 트랜잭션 시작
    	int rtnUpd = 0;
    	
    	try {
    	
    		rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
    		updTotCnt = updTotCnt + rtnUpd;
        
		    if(rtnUpd == 0) {		 
		    	
		    	throw new LBizException(ObsErrCode.ERR_7000.getCode() , StringUtil.mergeStr(ObsErrCode.ERR_7000.getName()," (" , ObsErrCode.ERR_7000.getCode(),")"));//계좌가 존재하지 않습니다.
		    	//throw new LBizException(ObsErrCode.ERR_7000.getCode(), ObsErrCode.ERR_7000.getName());//계좌가 존재하지 않습니다.
	
		    }else {
		    	
		    	int rgAccCnt = 0;
		    	
		    	//오픈뱅킹상세 원장에 계좌에대해 채널구분없이 사용여부 Y이 건이 몇건존재하는지 조회			         
		        LData iAlAccData = new LData();  
		        LData rAlAccData = new LData();
		        
		        iAlAccData.setString("고객계좌번호"        , iClosData.getString("고객계좌번호"));   
		        iAlAccData.setString("계좌개설은행코드"     , iClosData.getString("계좌개설은행코드"));   
		        iAlAccData.setString("오픈뱅킹이용기관코드"  , opnbUtzInsCd);
		        iAlAccData.setString("CI내용"           , iClosData.getString("CI내용"));
		        iAlAccData.setString("계좌상세사용여부"     ,"Y");
		        
		        rAlAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtlRgAccnt" , iAlAccData);
		        rgAccCnt = rAlAccData.getInt("등록계좌수");
	
		        LLog.debug.println("등록계좌수 ☆★☆☆★☆☆★☆ : " +  rgAccCnt);
		        
		        if(rgAccCnt == 0) { //등록계좌수가 0일경우
		        	
		       	  // 전문거래고유번호 생성 Cpbc 호출
			       	LData rCdMg = new LData();        	
			       	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
			       	
			       	String rspCd = ""; //응답코드
			       	String bankRspCd = ""; //참가기관응답코드
			    
			       	iClosAccAPICall = new LData();
			       	iClosAccAPICall.setString("user_seq_no"    , opnbUsrUno);//오픈뱅킹사용자고유번호 
			 		iClosAccAPICall.setString("bank_code_std"  , iClosData.getString("계좌개설은행코드"));//계좌개설기관.표준코드
			 		iClosAccAPICall.setString("account_num"    , iClosData.getString("고객계좌번호"));//고객계좌번호
					//iClosAccAPICall.setString("account_num"    , CryptoDataUtil.decryptKey( iClosData.getString("고객계좌번호") , CryptoDataUtil.KB_BD_NORMAL_KEY));//계좌번호 복호화
			 		
			 		if(!StringUtil.trimNisEmpty(iClosData.getString("계좌납입회차번호"))) {    	   
			 			iClosAccAPICall.setString("account_seq"    , iClosData.getString("계좌납입회차번호"));//계좌납입회차번호
					}   
			 		
			 		iClosAccAPICall.setString("채널세부업무구분코드" , iClosData.getString("채널세부업무구분코드"));//채널세부업무구분코드
			       	
			    	//계좌조회동의여부
			 		rCdMg = opnbCdMg.crtTrUno(new LData());
			       	//사용자계좌등록 필수값
			 		iClosAccAPICall.setString("bank_tran_id"  , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
			 		iClosAccAPICall.setString("scope"         , "inquiry"); //조회서비스		 		
			 		rClosAccAPICall = opnbApi.closAccAPICall(iClosAccAPICall); //계좌헤지 API호출
			 		rspCd = rClosAccAPICall.getString("rsp_code");
			 		bankRspCd = rClosAccAPICall.getString("bank_rsp_code");
			 		
			 		if(("A0000".equals(rspCd) && "000".equals(bankRspCd))
			 				||("A0002".equals(rspCd) && ("551".equals(bankRspCd) || "555".equals(bankRspCd) || "556".equals(bankRspCd))) ){ //000.정상 551.해지사용자 555.해당사용자없음 556.사용자미등록
			 				
			 				try {
			 				
				 		    	LData iUpdOpnbAccBas = new LData();	
				 		    	iUpdOpnbAccBas.setString("계좌사용여부"            , "N");	    
				 		    	
				 		    	iUpdOpnbAccBas.setString("계좌조회동의여부"          , "N");
				 		    	iUpdOpnbAccBas.setString("조회해제채널세부업무구분코드" , iClosData.getString("채널세부업무구분코드"));
				 		    	iUpdOpnbAccBas.set("계좌조회동의해제일시"             , DateUtil.getDateTimeStr());
				 		    	
				 		    	iUpdOpnbAccBas.setString("고객계좌번호"             , iClosData.getString("고객계좌번호"));
				 		    	iUpdOpnbAccBas.setString("계좌개설은행코드"          , iClosData.getString("계좌개설은행코드"));
				 		    	iUpdOpnbAccBas.setString("오픈뱅킹이용기관코드"       , opnbUtzInsCd);				 		    	
				 		    	iUpdOpnbAccBas.setString("시스템최초생성식별자"       , "UBF2030206");
				 		    	iUpdOpnbAccBas.setString("시스템최종갱신식별자"       , "UBF2030206");
				 		    		    	
				 		    	int updCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccBasUseYn" , iUpdOpnbAccBas);
				 		    	updTotCnt = updTotCnt + updCnt;
			 		    	
			 				}catch(LException e) {
			 				
			 					throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "계좌기본사용여부수정(updateAccBasUseYn1) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
			 					//throw new LException("기관거래 처리중 오류가 발생했습니다.");//기관거래 처리중 오류
			 				}
			 				
			 		}else {
			 			
			 			throw new LBizException(rClosAccAPICall.getString("rsp_code") , StringUtil.mergeStr( rClosAccAPICall.getString("rsp_message") ,"(", rClosAccAPICall.getString("rsp_code"),")"));//처리중 오류가 발생했습니다.
			 		}
			 		
			 		//조회계좌해지 API응답
			     	result.setString("API거래고유번호_V40"     , rClosAccAPICall.getString("api_tran_id"));
		           	result.setString("API거래일시_V17"        , rClosAccAPICall.getString("api_tran_dtm"));
		           	result.setString("API응답코드_V5"         , rClosAccAPICall.getString("rsp_code"));
		           	result.setString("API응답메시지_V300"     , rClosAccAPICall.getString("rsp_message"));
		           	result.setString("참가기관거래고유번호_V20"  , rClosAccAPICall.getString("bank_tran_id"));
		           	result.setString("참가기관거래일자_V8"      , rClosAccAPICall.getString("bank_tran_date"));
		           	result.setString("참가기관표준코드_V3"      , rClosAccAPICall.getString("bank_code_tran"));
		           	result.setString("참가기관응답코드_V3"      , rClosAccAPICall.getString("bank_rsp_code"));
		           	result.setString("참가기관응답메시지_V100"   , rClosAccAPICall.getString("bank_rsp_message"));
		       		
			 		//출금계좌해지
			    	if(StringUtil.trimNisEmpty(iClosData.getString("계좌납입회차번호"))) {//계좌납입회차가 없는것만 출금계좌해지
			    			
				 		rCdMg = opnbCdMg.crtTrUno(new LData());
				       	//사용자계좌등록 필수값
				 		iClosAccAPICall.setString("bank_tran_id"  , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
				 		iClosAccAPICall.setString("scope"         , "transfer"); //출금서비스 		
				 		rOdwClosAccAPICall = opnbApi.closAccAPICall(iClosAccAPICall); //계좌헤지 API호출
				 		rspCd = rOdwClosAccAPICall.getString("rsp_code");
				 		bankRspCd = rOdwClosAccAPICall.getString("bank_rsp_code");
				 		
				 		if(("A0000".equals(rspCd) && "000".equals(bankRspCd))
				 				||("A0002".equals(rspCd) && ("551".equals(bankRspCd) || "555".equals(bankRspCd) || "556".equals(bankRspCd))) ){ //000.정상 551.해지사용자 555.해당사용자없음 556.사용자미등록
				 				
				 				try {
				 					
				 					LData iUpdOpnbAccBas = new LData();	
					 		    	iUpdOpnbAccBas.setString("계좌사용여부"            , "N");	    
					 		    	
					 		    	iUpdOpnbAccBas.setString("출금동의여부"            , "N");
					 		    	iUpdOpnbAccBas.setString("출금해제채널세부업무구분코드" , iClosData.getString("채널세부업무구분코드"));
					 		    	iUpdOpnbAccBas.set("출금동의해제일시"                , DateUtil.getDateTimeStr());
					 		    	
					 		    	iUpdOpnbAccBas.setString("고객계좌번호"             , iClosData.getString("고객계좌번호"));
					 		    	iUpdOpnbAccBas.setString("계좌개설은행코드"          , iClosData.getString("계좌개설은행코드"));
					 		    	iUpdOpnbAccBas.setString("오픈뱅킹이용기관코드"       , opnbUtzInsCd);
					 		    	iUpdOpnbAccBas.setString("시스템최초생성식별자"       , "UBF2030206");
					 		    	iUpdOpnbAccBas.setString("시스템최종갱신식별자"       , "UBF2030206");
					 		    	
					 		    	int updCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccBasUseYn" , iUpdOpnbAccBas);
					 		    	updTotCnt = updTotCnt + updCnt;
					 		    	
				 				}catch(LException e) {
				 					
				 					throw new LBizException(ObsErrCode.ERR_7777.getCode() ,StringUtil.mergeStr( "계좌기본사용여부수정(updateAccBasUseYn2) " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
				 					//throw new LException("기관거래 처리중 오류가 발생했습니다.");//기관거래 처리중 오류
				 				}
				 			
				 		}else {
				 			
				 			LLog.debug.println("롤백 ☆★☆☆★☆☆★☆" );
				 			ntxManager.nestedRollback();  // 에러 발생 시 트랜잭션 rollbacks
				 		}
				 		
				 		result.setString("API거래고유번호_V40"     , rOdwClosAccAPICall.getString("api_tran_id"));
			           	result.setString("API거래일시_V17"        , rOdwClosAccAPICall.getString("api_tran_dtm"));
			           	result.setString("API응답코드_V5"         , rOdwClosAccAPICall.getString("rsp_code"));
			           	result.setString("API응답메시지_V300"     , rOdwClosAccAPICall.getString("rsp_message"));
			           	result.setString("참가기관거래고유번호_V20"  , rOdwClosAccAPICall.getString("bank_tran_id"));
			           	result.setString("참가기관거래일자_V8"      , rOdwClosAccAPICall.getString("bank_tran_date"));
			           	result.setString("참가기관표준코드_V3"      , rOdwClosAccAPICall.getString("bank_code_tran"));
			           	result.setString("참가기관응답코드_V3"      , rOdwClosAccAPICall.getString("bank_rsp_code"));
			           	result.setString("참가기관응답메시지_V100"   , rOdwClosAccAPICall.getString("bank_rsp_message"));			 		
			    	}
			    	
		        }else {//등록계좌수가 0보다 크다면
		        	
		        	LLog.debug.println("계좌상세 계좌존재 로그 :" + iClosData);
		        	result.setString("API응답코드_V5"         , "ZZZZZ");
		           	result.setString("API응답메시지_V300"     , "해당계좌 사용여부 N으로 변경");
		        }
		        
		    }//수정한 계좌 건수가 존재한다면
	    
    	}catch(LException e) {
    		
    		ntxManager.nestedRollback();  // 에러 발생 시 트랜잭션 rollback
    		throw new LBizException(e.getMessage(), e.getOptionalInfo().getMessageAddContent());//처리중 오류가 발생했습니다.
    		
    	} finally {
    		
    		ntxManager.nestedCommit();  // 트랜잭션 commit 
    		ntxManager.nestedRelease();   // 트랜잭션 release
    	}
		
    	//채널별계좌해지 이력추가
    	LData iRgAccData = new LData();  
	    LData rRgAccData = new LData();
	        
	    iRgAccData.setString("고객계좌번호"         , iClosData.getString("고객계좌번호"));   
	    iRgAccData.setString("계좌개설은행코드"      , iClosData.getString("계좌개설은행코드"));   
	    iRgAccData.setString("오픈뱅킹이용기관코드"   , opnbUtzInsCd);
	    iRgAccData.setString("CI내용"            , ciCtt);
	    iRgAccData.setString("계좌상세사용여부"      , "Y");
	    iRgAccData.setString("채널세부업무구분코드"   , chnDtlsBwkDtcd);
	        
	    rRgAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtlRgAccnt" , iRgAccData);
	    int rgAccDtlCnt = rRgAccData.getInt("등록계좌수");
	    LLog.debug.println("등록계좌수 ☆★☆☆★☆☆★☆ : " +  rgAccDtlCnt);
	    
	    if(rgAccDtlCnt == 0) { //해당채널에 등록계좌가 없을경우해지이력 
	    	
	    	 //동의이력적재
			LData iOpnbChnPrStplCnsYn = new LData();
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
	    	
	    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"      , opnbUsrUno);
	    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , chnDtlsBwkDtcd);
	    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , "3");//통합계좌조회
	    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의연장구분코드"    , "3");//3.해지
	    	opnbCstMgCpbc.opnbCstCnsPhsRg(iOpnbChnPrStplCnsYn);
	    	
	    }//end_if
    	
    	//계좌기본테이블 및 카드테이블 조회하여 사용여부가 N인 건수를 조회 
    	LData iUseAccData = new LData();  
        LData rUseAccData = new LData();
        iUseAccData.setString("오픈뱅킹사용자고유번호"     , opnbUsrUno); //오픈뱅킹사용자고유번호  
        
        rUseAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectOpnbAccCrdUseAblNcn" , iUseAccData);
        int useAccCnt = rUseAccData.getInt("사용가능건수");
        
        LLog.debug.println("사용가능건수 ☆★☆☆★☆☆★☆ : " +  useAccCnt);
        
        if(useAccCnt == 0) { //사용계좌가 존재하지 않는다면
        	
//        	LData iPrcUsrScsnApiCall = new LData();
//        	LData rPrcUsrScsnApiCall = new LData();
//        	
//        	iPrcUsrScsnApiCall.setString("client_use_code",  AuthInfo.UTZ_INS_CD.getCode());//이용기관코드
//        	iPrcUsrScsnApiCall.setString("user_seq_no"    ,  opnbUsrUno);//사용자일련번호
//        	rPrcUsrScsnApiCall = opnbApi.prcUsrScsnAPICall(iPrcUsrScsnApiCall); //계좌탈퇴 API호출
//        	
//	 		if("A0000".equals(rPrcUsrScsnApiCall.getString("rsp_code"))){ //정상

        	LData iDelAccDtl = new LData();
        	iDelAccDtl.setString("오픈뱅킹사용자고유번호", opnbUsrUno);
        	iDelAccDtl.setString("CI내용"           , ciCtt);
	    
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
        	opnbCstMgCpbc.delOpnbCstPsnInf(iDelAccDtl);
        	  	
//	 		}else {
//	 			
//	 			throw new LBizException(rPrcUsrScsnApiCall.getString("rsp_code"), rPrcUsrScsnApiCall.getString("rsp_message"));//기관오류처리
//	 		}
        	
        }
       
        LLog.debug.println("출력 ☆★☆☆★☆☆★☆ : " +  result);
        
        return result;
    }

    /**
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌상세 테이블에 대상 데이터 존재하지 않는 경우
     * 
     * 1. 거래고유번호, 등록계좌개설기관표준코드, 등록계좌번호, 회차번호, 생년월일, 사용자명, CI, 이메일주소, 서비스구분, 동의여부 및 동의 자료구분으로 API 호출
     * 2-1. 성공 응답
     *  - 출금서비스 사용자 등록시에만 출금등록거래고유번호 및 출금등록거래일자 세팅
     * 2-2. 오류 응답
     *  - 응답을 받지 못한 경우 API를 재호출하여 사용자계좌등록 상태 확인 -> 기등록된 경우 '기등록된 조회서비스용 사용자 계좌(A0324)' 또는 '기등록된 출금서비스용 사용자 계좌(A0325)'로 응답
     *  - 사용자 탈퇴 API 호출하여 계좌 해지 시 계좌 해지처리전에 동일한 계좌에 대해서 계좌 등록 요청하면 '사용자탈퇴 처리중인 서비스(A0019)'로 거부 응답. 계좌 해지 처리 후 정상 계좌 등록 가능
     * 3. 계좌정보조회 API호출
     * 4. 오픈뱅킹고객정보, 오픈뱅킹계좌기본, 오픈뱅킹 계좌 상세 테이블 적재
     * 5. 응답값 리턴
     * 
     * - UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌상세 테이블에 대상 데이터 존재하는 경우
     * - UBF오픈뱅킹계좌상세 테이블에 채널변경하여 재적재
     * - 오픈뱅킹 가져오기 기능에서 타 채널에 등록돼있는 사용자의 오픈뱅킹 계좌 정보를 현재 이용채널에 가져오기 위함
     * (ex.리브메이트 오픈뱅킹 등록계좌 없을시 KB PAY 계좌출력 , 반대의경우(리브메이터오픈뱅킹신규) 마스터원장에 Y일경우 약관동의 계좌등록절차없이 마스터원장 내용가져와서 계좌상세 반영 )
     * - 리브메이트 등록계좌 있는상태 가져오기 모두삭제하고 KBpay 기준의 등록계좌로 등록하고 출력 
     * 
     * 1. 채널세부업무구분코드, 고객계좌번호, 계좌개설은행코드, 오픈뱅킹금융기관코드, 오픈뱅킹계좌일련번호 입력
     * 2. 이용중인 채널의 채널구분코드 입력
     * 3. UBF오픈뱅킹계좌상세 테이블에서 입력값에 해당하는 데이터의 채널세부업무구분코드를 이용중인 채널구분코드로 변경하여 원장에 적재
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 필수 : 계좌개설은행코드,고객계좌번호,고객출생년월일,고객명,CI내용,오픈뱅킹금융기관코드,채널세부업무구분코드
     * 계좌납입회차번호,오픈뱅킹이메일주소,계좌조회동의여부,출금동의여부,동의자료구분코드,
     * 오픈뱅킹금융기관코드,고객식별자,준회원식별자,오픈뱅킹주민등록번호,오픈뱅킹사용자휴대폰번호,성별구분코드,
     * 채널세부업무구분코드,오픈뱅킹계좌명의구분코드,오픈뱅킹계좌상품명,권유자부점코드,권유자부점명,권유직원번호,오픈뱅킹플랫폼식별번호,대표계좌여부
     * 계좌별명,계좌표시순서,즐겨찾기계좌여부,화면노출버튼구분코드
     * 
     * <OUTPUT>
     * 처리결과구분코드
     * 
     * @serviceID UBF2030207
     * @logicalName 사용자계좌 등록
     * @method regtUsrAcc
     * @method(한글명) 사용자계좌 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtUsrAcc(LData iRegData) throws LException {
        LData result = new LData();
        LData iRegtUsrAccAPICall = null;
        LData iRetvAccInfAPICall = null;
        
        String accTbExt = "";
        int updTotCnt = 0;
        
        String inqRspCd = ""; //조회응답코드
		String tranRspCd = "";//출금응답코드
		
		String userSeqNo = ""; //오픈뱅킹사용자고유번호
		String opnbRegProcYn = "N";//오픈뱅킹등록처리여부
		
		String opnbUtzInsCd = UBF_CONST.AuthInfo.UTZ_INS_CD.getCode();//오픈뱅킹이용기관코드
	        
        if(StringUtil.trimNisEmpty(iRegData.getString("고객계좌번호"))) {				
			LLog.debug.println("로그 " + iRegData.getString("고객계좌번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객계좌번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객계좌번호" ));//고객계좌번호가 존재하지 않습니다.
		}
       
        if(StringUtil.trimNisEmpty(iRegData.getString("계좌개설은행코드"))) {				
			LLog.debug.println("로그 " + iRegData.getString("계좌개설은행코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 계좌개설은행코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌개설은행코드" ));//계좌개설은행코드가 존재하지 않습니다.
		}
       
        if(StringUtil.trimNisEmpty(iRegData.getString("고객출생년월일"))) {
			LLog.debug.println("로그 " + iRegData.getString("고객출생년월일"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객출생년월일"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객출생년월일" ));//고객출생년월일이 존재하지 않습니다.
		}
       
        if(StringUtil.trimNisEmpty(iRegData.getString("고객명"))) {
			LLog.debug.println("로그 " + iRegData.getString("고객명"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 고객명"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-고객명" ));//고객명이 존재하지 않습니다.
		}
       
        if(StringUtil.trimNisEmpty(iRegData.getString("CI내용"))) {
			LLog.debug.println("로그 " + iRegData.getString("CI내용"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
		}
        
        if(StringUtil.trimNisEmpty(iRegData.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + iRegData.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
        
        if(StringUtil.trimNisEmpty(iRegData.getString("계좌조회동의여부"))) {
			LLog.debug.println("로그 " + iRegData.getString("계좌조회동의여부"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 계좌조회동의여부"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-계좌조회동의여부" ));//계좌조회동의여부이 존재하지 않습니다.
		}
        
        if("Y".equals(iRegData.getString("계좌조회동의여부"))) {
        	if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹이메일주소전문내용"))) {
    			LLog.debug.println("로그 " + iRegData.getString("오픈뱅킹이메일주소전문내용"));
    			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹이메일주소전문내용"));//처리중 오류가 발생했습니다.
    			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹이메일주소전문내용" ));//오픈뱅킹이메일주소전문내용 존재하지 않습니다.
    		}
        }
        
        if(StringUtil.trimNisEmpty(iRegData.getString("출금동의여부"))) {
			LLog.debug.println("로그 " + iRegData.getString("출금동의여부"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 출금동의여부"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-출금동의여부" ));//출금동의여부가 존재하지 않습니다.
		}
        
        if(StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹통합계좌조회동의여부"))) {
			LLog.debug.println("로그 " + iRegData.getString("오픈뱅킹통합계좌조회동의여부"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹통합계좌조회동의여부"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹통합계좌조회동의여부" ));//오픈뱅킹통합계좌조회동의여부가 존재하지 않습니다.
		}
        
        try {
        
	    	//오픈뱅킹 계좌기본테이블에 사용여부 Y인상태의 등록된 계좌인지 체크 
	        LData iAlAccData = new LData();  
	        iAlAccData.setString("고객계좌번호"        , iRegData.getString("고객계좌번호"));   
	        iAlAccData.setString("계좌개설은행코드"     , iRegData.getString("계좌개설은행코드"));   
	        iAlAccData.setString("오픈뱅킹이용기관코드"  , opnbUtzInsCd);
	        iAlAccData.setString("계좌사용여부"        ,"Y");
	        
	        LData rAlAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectOpnbAccUseYnInq" , iAlAccData);	              
	        
	        accTbExt = "Y";
	        
	        if(!iRegData.getString("계좌조회동의여부").equals(rAlAccData.getString("계좌조회동의여부")) 
	        		|| !iRegData.getString("출금동의여부").equals(rAlAccData.getString("출금동의여부"))){	        	
	        	accTbExt = "N";
	        }
	        
        } catch (LNotFoundException nfe) {
        	
        	accTbExt = "N";
        }	
        
        //계좌존재여부
        if("N".equals(accTbExt)) { //오픈뱅킹계좌기본테이블에 계좌가 존재하지 않는다면 상세에도 모두 N임
        	
            // 전문거래고유번호 생성 Cpbc 호출
	       	LData rCdMg = new LData();        	
	       	OpnbCdMgCpbc opnbCdMg = new OpnbCdMgCpbc();
	            	
	       	iRegtUsrAccAPICall = new LData();
	       	iRegtUsrAccAPICall.setString("bank_code_std"         , iRegData.getString("계좌개설은행코드"));//계좌개설은행코드	       	
	        //iRegtUsrAccAPICall.setString("register_account_num"  , CryptoDataUtil.decryptKey(iRegData.getString("고객계좌번호") , CryptoDataUtil.KB_BD_NORMAL_KEY));//계좌번호 암호화
	       	iRegtUsrAccAPICall.setString("register_account_num"  , iRegData.getString("고객계좌번호"));//고객계좌번호
	       	iRegtUsrAccAPICall.setString("user_info"             , iRegData.getString("고객출생년월일"));//고객출생년월일
	       	
  	        //iRegtUsrAccAPICall.setString("user_name"           , CryptoDataUtil.decryptKey(iRegData.getString("고객명") , CryptoDataUtil.KB_BD_NORMAL_KEY));//계좌번호 암호화
	       	iRegtUsrAccAPICall.setString("user_name"             , iRegData.getString("고객명"));//고객명
	       	iRegtUsrAccAPICall.setString("user_ci"               , iRegData.getString("CI내용"));//CI내용
	       	
	        if(!StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹동의자료구분코드"))) {    	   
	        	iRegtUsrAccAPICall.setString("agmt_data_type"   , iRegData.getString("오픈뱅킹동의자료구분코드"));//동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
		    }
	       	
  	        //iRegtUsrAccAPICall.setString("user_email"           , CryptoDataUtil.decryptKey(iRegData.getString("오픈뱅킹이메일주소") , CryptoDataUtil.KB_BD_NORMAL_KEY));//계좌번호 암호화
	       	
	        if(!StringUtil.trimNisEmpty(iRegData.getString("오픈뱅킹이메일주소전문내용"))) {    	   
	        	iRegtUsrAccAPICall.setString("user_email"   , iRegData.getString("오픈뱅킹이메일주소전문내용"));//오픈뱅킹이메일주소전문내용
		    }
	       	
	       	iRegtUsrAccAPICall.setString("채널세부업무구분코드"        , iRegData.getString("채널세부업무구분코드"));//채널세부업무구분코드
	       	
	        if(!StringUtil.trimNisEmpty(iRegData.getString("계좌납입회차번호"))) {    	   
	        	iRegtUsrAccAPICall.setString("register_account_seq"  , iRegData.getString("계좌납입회차번호"));//계좌납입회차번호
		    }
	       	
			OpnbApiCpbc opnbApi = new OpnbApiCpbc();
			
			LData rRegAccInqApiCall = new LData();//조회서비스 사용자계좌등록
			LData rRegOdwCnsApiCall = new LData();//출금서비스 사용자계좌등록
			LData rRetvAccInfAPICall = new LData();//계좌정보조회
			
			inqRspCd = ""; //조회응답코드
			tranRspCd = "";//출금응답코드
			
			userSeqNo = ""; //사용자일련번호	
			String payerNum = ""; //납부자번호 
			String bankRspCd = ""; // 은행응답코드
			String accoutTp = ""; // 계좌종류구분코드
			
			LData rData = new LData();
			
			if("Y".equals(iRegData.getString("계좌조회동의여부"))) {//계좌조회동의여부
				
		       	rCdMg = opnbCdMg.crtTrUno(new LData());
		       	//사용자계좌등록 필수값
		     	iRegtUsrAccAPICall.setString("bank_tran_id"          , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
				iRegtUsrAccAPICall.setString("scope"                 , "inquiry");//서비스구분 inquiry : 조회서비스 / transfer : 출금서비스
				iRegtUsrAccAPICall.setString("info_prvd_agmt_yn"     , iRegData.getString("계좌조회동의여부"));//계좌조회동의여부
				iRegtUsrAccAPICall.setString("wd_agmt_yn"            , " ");//출금동의여부
				
			 	//사용자계좌등록 API호출
				rRegAccInqApiCall = opnbApi.regtUsrAccAPICall(iRegtUsrAccAPICall); //계좌변경 API호출
		        inqRspCd = rRegAccInqApiCall.getString("rsp_code");//응답코드(API)
		        userSeqNo = rRegAccInqApiCall.getString("user_seq_no");//사용자 일련번호
		        bankRspCd = rRegAccInqApiCall.getString("bank_rsp_code");//참가기관응답코드
		        accoutTp = rRegAccInqApiCall.getString("account_type");//계좌종류구분코드
		        
		        if(("A0000".equals(inqRspCd) && "000".equals(bankRspCd)) || "A0324".equals(inqRspCd) ) {//정상등록   / 우리원장에는 사용안함이나 존재하지 않고 금결원에는 기등록되었다고 했을때 금결원기준으로 변경
			    	
			        	LData inputData = new LData();
				    	
				    	inputData.putAll(iRegData);
				    	inputData.setString("오픈뱅킹이용기관코드"         , opnbUtzInsCd);
				    	inputData.setString("오픈뱅킹사용자고유번호"        , userSeqNo);
				    	inputData.setString("핀테크이용번호"              , rRegAccInqApiCall.getString("fintech_use_num"));
				    	inputData.setString("오픈뱅킹납부자번호"           , rRegAccInqApiCall.getString("payer_num"));
				    	inputData.setString("오픈뱅킹계좌종류구분코드"       , accoutTp);//계좌종류 1:수시입출금, 2:예적금, 6:수익증권, T:종합계좌
				    	inputData.setString("개별저축은행명"              , rRegAccInqApiCall.getString("savings_bank_name"));
				    	inputData.setString("출금동의여부"                , "N");//출금동의여부	
		        	
		        		if( "A0324".equals(inqRspCd) ) { //기등록일경우 계좌정보조회 API호출
		        			
		        			rCdMg = opnbCdMg.crtTrUno(new LData());		        			
		        			iRetvAccInfAPICall = new LData();
		        			iRetvAccInfAPICall.setString("채널세부업무구분코드"       , iRegData.getString("채널세부업무구분코드"));//채널세부업무구분코드
		        			iRetvAccInfAPICall.setString("bank_tran_id"          , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
		        			iRetvAccInfAPICall.setString("user_seq_no"           , userSeqNo);//사용자일련번호
		        			iRetvAccInfAPICall.setString("bank_code_std"         , iRegData.getString("계좌개설은행코드"));//계좌개설은행코드
		        			iRetvAccInfAPICall.setString("account_num"           , iRegData.getString("고객계좌번호"));//고객계좌번호
		        			if(!StringUtil.trimNisEmpty(iRegData.getString("계좌납입회차번호"))) {    	   
		        				iRetvAccInfAPICall.setString("account_seq"       , iRegData.getString("계좌납입회차번호"));//계좌납입회차번호
		        			 }
		        			iRetvAccInfAPICall.setString("scope"                 , "inquiry");//서비스구분 inquiry : 조회서비스 / transfer : 출금서비스
		        			
		        			rRetvAccInfAPICall = opnbApi.retvAccInfAPICall(iRetvAccInfAPICall); //계좌정보조회 API호출
		        			accoutTp = rRetvAccInfAPICall.getString("account_type");
		        			
		        			if("A0000".equals(rRetvAccInfAPICall.getString("rsp_code")) && "000".equals(rRetvAccInfAPICall.getString("bank_rsp_code"))){
		        			
						    	inputData.setString("핀테크이용번호"              , rRetvAccInfAPICall.getString("fintech_use_num"));
						    	inputData.setString("오픈뱅킹납부자번호"           , rRetvAccInfAPICall.getString("payer_num"));
						    	inputData.setString("오픈뱅킹계좌종류구분코드"       , accoutTp);//계좌종류 1:수시입출금, 2:예적금, 6:수익증권, T:종합계좌
						    	inputData.setString("개별저축은행명"              , rRetvAccInfAPICall.getString("savings_bank_name"));
		        			} 	
		        			
		        		}
		        						    	
				    	rData = this.regUsrCstAccPrc(inputData);//사용자고객등록 처리
				    	updTotCnt = updTotCnt + rData.getInt("처리건수");
				    	opnbRegProcYn = "Y";//등록처리 Y
			    			    
			    }else {
			    	
			    		throw new LBizException(rRegAccInqApiCall.getString("rsp_code") , StringUtil.mergeStr( rRegAccInqApiCall.getString("rsp_message") ,"(", rRegAccInqApiCall.getString("rsp_code"),")"));//처리중 오류가 발생했습니다.
			    }
		        
		    	result.setString("API거래고유번호_V40"     , rRegAccInqApiCall.getString("api_tran_id"));
	           	result.setString("API거래일시_V17"        , rRegAccInqApiCall.getString("api_tran_dtm"));
	           	result.setString("API응답코드_V5"         , rRegAccInqApiCall.getString("rsp_code"));
	           	result.setString("API응답메시지_V300"     , rRegAccInqApiCall.getString("rsp_message"));
	           	result.setString("참가기관거래고유번호_V20"  , rRegAccInqApiCall.getString("bank_tran_id"));
	           	result.setString("참가기관거래일자_V8"      , rRegAccInqApiCall.getString("bank_tran_date"));
	           	result.setString("참가기관표준코드_V3"      , rRegAccInqApiCall.getString("bank_code_tran"));
	           	result.setString("참가기관응답코드_V3"      , rRegAccInqApiCall.getString("bank_rsp_code"));
	           	result.setString("참가기관응답메시지_V100"   , rRegAccInqApiCall.getString("bank_rsp_message"));
	           	result.setString("참가기관명_V20"          , rRegAccInqApiCall.getString("bank_name"));
	           	result.setString("개별저축은행명_V20"       , rRegAccInqApiCall.getString("savings_bank_name"));
	           	result.setString("오픈뱅킹사용자고유번호"     , rRegAccInqApiCall.getString("user_seq_no"));
	           	result.setString("핀테크이용번호"           , rRegAccInqApiCall.getString("fintech_use_num"));
	           	result.setString("오픈뱅킹납부자번호"        , rRegAccInqApiCall.getString("payer_num"));
	           	result.setString("오픈뱅킹계좌종류구분코드"    , accoutTp);
	           	result.setString("참가기관출금거래고유번호"    , rRegAccInqApiCall.getString("transfer_bank_tran_id"));
	           	result.setString("참가기관출금등록거래일자_V8" , rRegAccInqApiCall.getString("transfer_bank_tran_date"));
	           	
			}   	
			
			if("Y".equals(iRegData.getString("출금동의여부")) //출금동의여부 Y
					&& StringUtil.trimNisEmpty(iRegData.getString("계좌납입회차번호"))) { //계좌납입회차가 존재하지 않음
				
				rCdMg = opnbCdMg.crtTrUno(new LData());
		       	//사용자계좌등록 필수값
		       	iRegtUsrAccAPICall.setString("bank_tran_id"          , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
				iRegtUsrAccAPICall.setString("scope"                 , "transfer");//서비스구분 inquiry : 조회서비스 / transfer : 출금서비스
				iRegtUsrAccAPICall.setString("wd_agmt_yn"            , "Y");//출금동의여부
				iRegtUsrAccAPICall.setString("info_prvd_agmt_yn"     , " ");//계좌조회동의여부
				
				//사용자계좌등록 API호출
				rRegOdwCnsApiCall = opnbApi.regtUsrAccAPICall(iRegtUsrAccAPICall); //계좌변경 API호출
		        tranRspCd = rRegOdwCnsApiCall.getString("rsp_code");//응답코드(API)
		        userSeqNo = rRegOdwCnsApiCall.getString("user_seq_no");//사용자 일련번호
		        payerNum = rRegOdwCnsApiCall.getString("payer_num");//납부자번호
		        bankRspCd = rRegOdwCnsApiCall.getString("bank_rsp_code");//참가기관응답코드
		        accoutTp = rRegOdwCnsApiCall.getString("account_type");//계좌종류구분코드
		        
				if(("A0000".equals(tranRspCd) && "000".equals(bankRspCd))|| "A0325".equals(tranRspCd)) {// 정상등록 ||기등록된 출금서비스용 사용자 서비스(A0325) <- 우리원장에 사용여부 Y인 계좌가 아님 신규등록과 같이 채번해서 등록 처리
					
					if("N".equals(opnbRegProcYn)) {//등록처리가 N일경우
						
						LData inputData = new LData();
						inputData.putAll(iRegData);
						
						inputData.setString("오픈뱅킹이용기관코드"          , opnbUtzInsCd);
				    	inputData.setString("오픈뱅킹사용자고유번호"        , userSeqNo);
				    	inputData.setString("핀테크이용번호"              , rRegOdwCnsApiCall.getString("fintech_use_num"));
				    	inputData.setString("오픈뱅킹납부자번호"           , payerNum);
				    	inputData.setString("오픈뱅킹계좌종류구분코드"       , accoutTp);
				    	inputData.setString("개별저축은행명"              , rRegOdwCnsApiCall.getString("savings_bank_name"));
				    	inputData.setString("출금동의여부"               , "Y");//출금동의여부	
						
						if( "A0325".equals(tranRspCd) ) { //기등록일경우 계좌정보조회 API호출
		        			
		        			rCdMg = opnbCdMg.crtTrUno(new LData());		        			
		        			iRetvAccInfAPICall = new LData();
		        			iRetvAccInfAPICall.setString("채널세부업무구분코드"        , iRegData.getString("채널세부업무구분코드"));//채널세부업무구분코드
		        			iRetvAccInfAPICall.setString("bank_tran_id"          , rCdMg.getString("거래고유번호"));//참가기관거래고유번호
		        			iRetvAccInfAPICall.setString("user_seq_no"           , userSeqNo);//사용자일련번호
		        			iRetvAccInfAPICall.setString("bank_code_std"         , iRegData.getString("계좌개설은행코드"));//계좌개설은행코드
		        			iRetvAccInfAPICall.setString("account_num"           , iRegData.getString("고객계좌번호"));//고객계좌번호
		        			if(!StringUtil.trimNisEmpty(iRegData.getString("계좌납입회차번호"))) {    	   
		        				iRetvAccInfAPICall.setString("account_seq"       , iRegData.getString("계좌납입회차번호"));//계좌납입회차번호
		        			 }
		        			iRetvAccInfAPICall.setString("scope"                 , "transfer");//서비스구분 inquiry : 조회서비스 / transfer : 출금서비스
		        			
		        			rRetvAccInfAPICall = opnbApi.retvAccInfAPICall(iRetvAccInfAPICall); //계좌정보조회 API호출
		        			accoutTp = rRetvAccInfAPICall.getString("account_type");
		        			
		        			if("A0000".equals(rRetvAccInfAPICall.getString("rsp_code")) && "000".equals(rRetvAccInfAPICall.getString("bank_rsp_code"))){
		        			
						    	inputData.setString("핀테크이용번호"              , rRetvAccInfAPICall.getString("fintech_use_num"));
						    	inputData.setString("오픈뱅킹납부자번호"           , rRetvAccInfAPICall.getString("payer_num"));
						    	inputData.setString("오픈뱅킹계좌종류구분코드"       , accoutTp);//계좌종류 1:수시입출금, 2:예적금, 6:수익증권, T:종합계좌
						    	inputData.setString("개별저축은행명"              , rRetvAccInfAPICall.getString("savings_bank_name"));
		        			} 	
		        			
		        		}
						
				    	this.regUsrCstAccPrc(inputData);//사용자고객등록 처리
						
					}else { //납부자번호 및 출금동의 update
						
						//고객기본 
						//고객존재할경우 출금 및 계좌 동의 여부 Y로 업데이트
				    	LLog.debug.println("기등록 정상고객 로그 :" + iRegData);
				    	//계좌기본
						LData iUpdOpnbAccBas = new LData();	
				    	iUpdOpnbAccBas.setString("오픈뱅킹납부자번호"        , payerNum);					    	
				    	iUpdOpnbAccBas.setString("고객계좌번호"             , iRegData.getString("고객계좌번호"));
				    	iUpdOpnbAccBas.setString("출금동의여부"             , "Y");					    	
				    	iUpdOpnbAccBas.setString("계좌개설은행코드"          , iRegData.getString("계좌개설은행코드"));
				    	iUpdOpnbAccBas.setString("오픈뱅킹이용기관코드"       , opnbUtzInsCd);
				    	iUpdOpnbAccBas.setString("시스템최초생성식별자"       , "UBF2030207");
				    	iUpdOpnbAccBas.setString("시스템최종갱신식별자"       , "UBF2030207");
				    		    	
				    	int rtnAccBasUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccInf" , iUpdOpnbAccBas);
				    	updTotCnt = updTotCnt + rtnAccBasUpd;
				    	
				    	//계좌상세
				    	LData iUpdAccDtl = new LData();
				    	iUpdAccDtl.setString("고객계좌번호"          , iRegData.getString("고객계좌번호"));
				    	iUpdAccDtl.setString("계좌개설은행코드"       , iRegData.getString("계좌개설은행코드"));
				    	iUpdAccDtl.setString("채널세부업무구분코드"    , iRegData.getString("채널세부업무구분코드"));
				    	iUpdAccDtl.setString("오픈뱅킹이용기관코드"    , opnbUtzInsCd);
				    	iUpdAccDtl.setString("CI내용"              , iRegData.getString("CI내용"));
				    	//계좌상세사용 N으로 UPDATE
				    	iUpdAccDtl.setString("출금동의여부"          , "Y");
				    	iUpdAccDtl.setString("출금동의갱신일시"       , DateUtil.getDateTimeStr());
				    	iUpdAccDtl.setString("시스템최종갱신식별자"    , "UBF2030207");
				    	        
					    int rtnAccDtlUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
					    updTotCnt = updTotCnt + rtnAccDtlUpd;
					}
					
				}	
				  	
		      	result.setString("API거래고유번호_V40"     , rRegOdwCnsApiCall.getString("api_tran_id"));
	           	result.setString("API거래일시_V17"        , rRegOdwCnsApiCall.getString("api_tran_dtm"));
	           	result.setString("API응답코드_V5"         , rRegOdwCnsApiCall.getString("rsp_code"));
	           	result.setString("API응답메시지_V300"     , rRegOdwCnsApiCall.getString("rsp_message"));
	           	result.setString("참가기관거래고유번호_V20"  , rRegOdwCnsApiCall.getString("bank_tran_id"));
	           	result.setString("참가기관거래일자_V8"      , rRegOdwCnsApiCall.getString("bank_tran_date"));
	           	result.setString("참가기관표준코드_V3"      , rRegOdwCnsApiCall.getString("bank_code_tran"));
	           	result.setString("참가기관응답코드_V3"      , rRegOdwCnsApiCall.getString("bank_rsp_code"));
	           	result.setString("참가기관응답메시지_V100"   , rRegOdwCnsApiCall.getString("bank_rsp_message"));
	           	result.setString("참가기관명_V20"          , rRegOdwCnsApiCall.getString("bank_name"));
	           	result.setString("개별저축은행명_V20"       , rRegOdwCnsApiCall.getString("savings_bank_name"));
	           	result.setString("오픈뱅킹사용자고유번호"     , rRegOdwCnsApiCall.getString("user_seq_no"));
	           	result.setString("핀테크이용번호"           , rRegOdwCnsApiCall.getString("fintech_use_num"));
	           	result.setString("오픈뱅킹납부자번호"        , rRegOdwCnsApiCall.getString("payer_num"));
	           	result.setString("오픈뱅킹계좌종류구분코드"    , accoutTp);
	           	result.setString("참가기관출금거래고유번호"    , rRegOdwCnsApiCall.getString("transfer_bank_tran_id"));
	           	result.setString("참가기관출금등록거래일자_V8" , rRegOdwCnsApiCall.getString("transfer_bank_tran_date"));
		       	
			}
			
	    }else {//오픈뱅킹계좌기본테이블에 계좌가 존재한다면
	    	
	    	//계좌상세테이블에 해당채널의 계좌가 존재하는지 체크 
	    	//없다면 채널만 변경해서 SELECT INSERT 
	    	//있다면 계좌상세사용여부가 N일경우 계좌상세사용여부 Y로 변경
	        String accDtlUseYn = "N";

		    try {
		        
		    	LData iOpnbAccDtl = new LData();
		    	LData rOpnbAccDtl = new LData();
		    	
		    	iOpnbAccDtl.setString("고객계좌번호"       , iRegData.getString("고객계좌번호"));        
		    	iOpnbAccDtl.setString("계좌개설은행코드"    , iRegData.getString("계좌개설은행코드"));
		    	iOpnbAccDtl.setString("오픈뱅킹이용기관코드" , opnbUtzInsCd);
		    	iOpnbAccDtl.setString("채널세부업무구분코드" , iRegData.getString("채널세부업무구분코드"));
		    	iOpnbAccDtl.setString("CI내용"          , iRegData.getString("CI내용"));
		    	
		    	rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
		    	accDtlUseYn = rOpnbAccDtl.getString("계좌상세사용여부");
	       			        	
	        } catch (LNotFoundException nfe) {
	        	
	        	accDtlUseYn = "NFE";
	        }	
	    	
	    	if("NFE".equals(accDtlUseYn)) {//존재하지 않는다면 채널변경하여 SELECT INSERT
			    
	    	   	LData iInsAccDtl = new LData();
		    	
		    	iInsAccDtl.setString("고객계좌번호"          , iRegData.getString("고객계좌번호"));
		    	iInsAccDtl.setString("계좌개설은행코드"       , iRegData.getString("계좌개설은행코드"));			    	
		    	iInsAccDtl.setString("오픈뱅킹이용기관코드"    , opnbUtzInsCd);
		    	iInsAccDtl.setString("CI내용"              , iRegData.getString("CI내용"));	    
		    	
		    	iInsAccDtl.setString("채널세부업무구분코드"    , iRegData.getString("채널세부업무구분코드"));
		    	iInsAccDtl.setString("오픈뱅킹플랫폼식별번호"  , iRegData.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsAccDtl.setString("계좌납입회차번호"       , iRegData.getString("계좌납입회차번호"));
		    	
		    	iInsAccDtl.setString("대표계좌여부"         , iRegData.getString("대표계좌여부"));
		    	iInsAccDtl.setString("계좌별명"            , iRegData.getString("계좌별명"));
		    	iInsAccDtl.setInt("계좌표시순서"            , iRegData.getInt("계좌표시순서"));
		    	iInsAccDtl.setString("계좌숨김여부"          , "N");	    	
		    	iInsAccDtl.setString("즐겨찾기계좌여부"       , iRegData.getString("즐겨찾기계좌여부"));
		    	iInsAccDtl.setString("화면노출버튼구분코드"    , iRegData.getString("화면노출버튼구분코드"));
		    	iInsAccDtl.setString("계좌상세사용여부"      , "Y");
		    	iInsAccDtl.setString("시스템최초생성식별자"   , "UBF2030207");
		    	iInsAccDtl.setString("시스템최종갱신식별자"   , "UBF2030207");
		    	        
			    int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccDtlChnPr" , iInsAccDtl);
			    updTotCnt = updTotCnt + rtnReg;
			    
			    if(rtnReg == 0) {//상세원장에 해당계좌정보가 아예없을경우
			    	
			    	iInsAccDtl.clear();
			    	iInsAccDtl.setString("고객계좌번호"          , iRegData.getString("고객계좌번호"));
			    	iInsAccDtl.setString("계좌개설은행코드"       , iRegData.getString("계좌개설은행코드"));
			    	iInsAccDtl.setString("채널세부업무구분코드"    , iRegData.getString("채널세부업무구분코드"));
			    	iInsAccDtl.setString("오픈뱅킹이용기관코드"    , opnbUtzInsCd);
			    	iInsAccDtl.setString("CI내용"              , iRegData.getString("CI내용"));	    	
			    	iInsAccDtl.setString("준회원식별자"          , iRegData.getString("준회원식별자"));
			    	iInsAccDtl.setString("고객식별자"           , iRegData.getString("고객식별자"));
			    	iInsAccDtl.setString("오픈뱅킹플랫폼식별번호"  , iRegData.getString("오픈뱅킹플랫폼식별번호"));
			    	iInsAccDtl.setString("계좌납입회차번호"       , iRegData.getString("계좌납입회차번호"));
			    	//iInsAccDtl.setString("개별저축은행명"        , input.getString("개별저축은행명"));
			    	iInsAccDtl.setString("출금동의여부"          , iRegData.getString("출금동의여부"));
			    	iInsAccDtl.setString("출금동의갱신일시"       , DateUtil.getDateTimeStr());
			    	iInsAccDtl.setString("대표계좌여부"         , iRegData.getString("대표계좌여부"));
			    	iInsAccDtl.setString("계좌별명"            , iRegData.getString("계좌별명"));
			    	iInsAccDtl.setInt("계좌표시순서"             , iRegData.getInt("계좌표시순서"));
			    	iInsAccDtl.setString("계좌숨김여부"          , "N");	    	
			    	iInsAccDtl.setString("즐겨찾기계좌여부"       , iRegData.getString("즐겨찾기계좌여부"));
			    	iInsAccDtl.setString("화면노출버튼구분코드"    , iRegData.getString("화면노출버튼구분코드"));
			    	iInsAccDtl.setString("계좌상세사용여부"       , "Y");
			    	iInsAccDtl.setString("시스템최초생성식별자"    , "UBF2030207");
			    	iInsAccDtl.setString("시스템최종갱신식별자"    , "UBF2030207");
			    	        
				    int insCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccDtl" , iInsAccDtl);
				    updTotCnt = updTotCnt + insCnt;
			    	
				    result.setString("API응답메시지_V300"      , "오픈뱅킹계좌기본테이블에 계좌가 존재. 계좌상세 해당채널에 계좌추가");
			    }
			    			    	
		    }else if("N".equals(accDtlUseYn)){ //계좌상세사용 N일경우
		    	
		    	LData iUpdAccDtl = new LData();
		    	iUpdAccDtl.setString("고객계좌번호"          , iRegData.getString("고객계좌번호"));
		    	iUpdAccDtl.setString("계좌개설은행코드"       , iRegData.getString("계좌개설은행코드"));
		    	iUpdAccDtl.setString("채널세부업무구분코드"    , iRegData.getString("채널세부업무구분코드"));
		    	iUpdAccDtl.setString("오픈뱅킹이용기관코드"    , opnbUtzInsCd);
		    	iUpdAccDtl.setString("CI내용"              , iRegData.getString("CI내용"));
		    	//계좌상세사용 N으로 UPDATE
		    	iUpdAccDtl.setString("계좌상세사용여부"       , "Y");
		    	iUpdAccDtl.setString("시스템최초생성식별자"   , "UBF2030207");
		    	iUpdAccDtl.setString("시스템최종갱신식별자"   , "UBF2030207");
		    	        
			    int rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
			    updTotCnt = updTotCnt + rtnUpd;
			    
			    result.setString("API응답메시지_V300"      , "오픈뱅킹계좌기본테이블에 계좌가 존재. 계좌상세 사용여부 Y로 변경");
	    		
	    	}else{//게좌상세에 존재한다면 사용여부 Y
	    		//처리안함 고객존재
		    	LLog.debug.println("계좌상세 기등록 정상고객 로그 :" + iRegData);
		    	
		    	result.setString("API응답메시지_V300"      , "오픈뱅킹계좌기본테이블에 계좌기본및 상세에 이미 존재");
	    	}
	    	
	      	result.setString("API응답코드_V5"         , "ZZZZZ");
           	
	    }
    
        LLog.debug.println("출력 ☆★☆☆★☆☆★☆ : " +  result);

        return result;
    }
    
    /**
     * - UBF오픈뱅킹고객정보기본,UBF오픈뱅킹계좌기본 ,UBF오픈뱅킹계좌상세 테이블데이터 등록
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * UBF오픈뱅킹계좌기본
     * UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 거래래고유번호,계좌개설은행코드,고객계좌번호,계좌납인회차,생년월일,사용자명,이메일주소, 제3자제공 동의여부, 출금동의여부, 동의자료구분
     * <OUTPUT>
     * 개설기관명,개별저축은행명, 사용자고유번호,핀테크이용번호,계좌종류,출금등록
     *
     * @method regUsrCstAccPrc
     * @method(한글명) 사용자고객계좌등록처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regUsrCstAccPrc(LData input) throws LException {
    	
    	LData rData = new LData(); 
    	int updTotCnt = 0;
    	
    	//오픈뱅킹 고객원장 등록
        String userSeqNo = input.getString("오픈뱅킹사용자고유번호");
        String custTbExt = "N";
        String accTbExt = "N";
        
        //오픈뱅킹고객이 존재하는 지 조회
        //고객원장에 존재하는 체크하여 등록
        
        try {
        	
        	LLog.debug.println("==========================selectRgYn 쿼리호출 start");
        	
        	LData iAlCustData = new LData();  
        	iAlCustData.setString("오픈뱅킹사용자고유번호" , userSeqNo);        
        	LData rAlCustData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectRgYn" , iAlCustData);
        	custTbExt = "Y";	       
        	
        	LLog.debug.println("==========================selectRgYn 쿼리호출 End");
        	
        } catch (LNotFoundException nfe) {
        	
        	custTbExt = "NFE";
        }	
        
	    if("NFE".equals(custTbExt)) {//고객신규등록

	    	LData iAlCustRegData = new LData();  
	    	 //고객원장에 존재하는 체크하여 등록
            iAlCustRegData.setString("오픈뱅킹사용자고유번호"        , userSeqNo);
            iAlCustRegData.setString("고객식별자"                , input.getString("고객식별자"));
            iAlCustRegData.setString("준회원식별자"               , input.getString("준회원식별자"));
            iAlCustRegData.setString("고객명"                   , input.getString("고객명"));
            iAlCustRegData.setString("CI내용"                  , input.getString("CI내용"));
            iAlCustRegData.setString("오픈뱅킹주민등록번호"         , input.getString("오픈뱅킹주민등록번호"));
            iAlCustRegData.setString("오픈뱅킹사용자휴대폰번호"      , input.getString("오픈뱅킹사용자휴대폰번호"));
            iAlCustRegData.setString("성별구분코드"               , input.getString("성별구분코드"));//1.남자 2.여자 9.기타
            iAlCustRegData.setString("고객출생년월일"             , input.getString("고객출생년월일"));
            iAlCustRegData.setString("오픈뱅킹통합계좌조회동의여부"   , input.getString("오픈뱅킹통합계좌조회동의여부"));
            iAlCustRegData.setString("오픈뱅킹서비스해지여부"       , "N");
            iAlCustRegData.setString("시스템최초생성식별자"   , "UBF2030207");
            iAlCustRegData.setString("시스템최종갱신식별자"   , "UBF2030207");
            
		    int insCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "insertOpnbCstInfBas" , iAlCustRegData);
		    updTotCnt = updTotCnt + insCnt;
		  	
	    }else if("Y".equals(custTbExt)){ //기고객일경우
	    	
	    	//고객존재할경우 출금 및 계좌 동의 여부 Y로 업데이트
	    	LLog.debug.println("기등록 정상고객 로그 :" + input);
	    	
	    	LData iUptOpnbCstCnsYn = new LData();	
	    	iUptOpnbCstCnsYn.setString("오픈뱅킹통합계좌조회동의여부" , input.getString("오픈뱅킹통합계좌조회동의여부"));	    	
	    	iUptOpnbCstCnsYn.setString("오픈뱅킹사용자고유번호"      , userSeqNo);
	    	iUptOpnbCstCnsYn.setString("시스템최종갱신식별자"        , "UBF2030207");
	    	
	    	int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "uptOpnbCstCnsYn" , iUptOpnbCstCnsYn);
	    	updTotCnt = updTotCnt + rtnUpd;
	    }
    	
	    //오픈뱅킹 계좌기본원장에 계좌가 존재하는지 조회
	    try {
	         
	        LData iAlAccData = new LData();  
	        LData rAlAccData = new LData();
	        
	        iAlAccData.setString("고객계좌번호"        , input.getString("고객계좌번호"));   
	        iAlAccData.setString("계좌개설은행코드"     , input.getString("계좌개설은행코드"));   
	        iAlAccData.setString("오픈뱅킹이용기관코드"  , input.getString("오픈뱅킹이용기관코드"));
	        iAlAccData.setString("오픈뱅킹사용자고유번호" , userSeqNo);
	        
	        rAlAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectOpnbAccBasDtl" , iAlAccData);
	        accTbExt = "N";
	  	
        } catch (LNotFoundException nfe) {
        	
        	accTbExt = "NFE";
        }
	    
	    if("NFE".equals(accTbExt)) { //계좌원장에 아예 계좌가 존재하지 않는다면
	    	
	    	LData iOpnbAccBas = new LData();				    	
	    	iOpnbAccBas.setString("고객계좌번호"             , input.getString("고객계좌번호"));
	    	iOpnbAccBas.setString("계좌개설은행코드"          , input.getString("계좌개설은행코드"));
	    	iOpnbAccBas.setString("오픈뱅킹이용기관코드"       , input.getString("오픈뱅킹이용기관코드"));
	    	iOpnbAccBas.setString("오픈뱅킹사용자고유번호"      , userSeqNo);
	    	iOpnbAccBas.setString("핀테크이용번호"            , input.getString("핀테크이용번호"));
	    	iOpnbAccBas.setString("오픈뱅킹납부자번호 "         , input.getString("오픈뱅킹납부자번호 "));
	    	iOpnbAccBas.setString("오픈뱅킹계좌종류구분코드"     , input.getString("오픈뱅킹계좌종류구분코드"));
	    	iOpnbAccBas.setString("오픈뱅킹계좌명의구분코드"     , input.getString("오픈뱅킹계좌명의구분코드"));
	    	iOpnbAccBas.setString("오픈뱅킹계좌상품명"         , input.getString("오픈뱅킹계좌상품명"));
	    	iOpnbAccBas.setString("오픈뱅킹이메일주소전문내용"   , input.getString("오픈뱅킹이메일주소전문내용"));
	    	iOpnbAccBas.setString("계좌조회동의여부"          , input.getString("계좌조회동의여부"));
	    	iOpnbAccBas.set("계좌조회동의등록일시"             , DateUtil.getDateTimeStr());
	    	iOpnbAccBas.setString("조회등록채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
	    	iOpnbAccBas.setString("출금동의여부"             , input.getString("출금동의여부"));
	    	iOpnbAccBas.setString("출금등록채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
	    	iOpnbAccBas.set("계좌등록일시"                  , DateUtil.getDateTimeStr());
	    	iOpnbAccBas.setString("계좌사용여부"            , "Y");
	    	iOpnbAccBas.set("계좌출금동의등록일시"            , DateUtil.getDateTimeStr());
	    	iOpnbAccBas.setString("계좌납입회차번호"         , input.getString("계좌납입회차번호"));
	    	iOpnbAccBas.setString("시스템최초생성식별자"   , "UBF2030207");
	    	iOpnbAccBas.setString("시스템최종갱신식별자"   , "UBF2030207");
	    	
	    	//--------여기부터				    	
	    	int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccBas" , iOpnbAccBas);
	    	updTotCnt = updTotCnt + rtnReg;
	    	
	    } else if("N".equals(accTbExt)) { //계좌원장에 사용여부 N인계좌가 존재한다면 Y로 업데이트
	    	
	    	LData iUpdOpnbAccBas = new LData();	
	    	iUpdOpnbAccBas.setString("계좌사용여부"            , "Y");	    	
	    	iUpdOpnbAccBas.setString("계좌조회동의여부"          , input.getString("계좌조회동의여부"));
	    	//iUpdOpnbAccBas.setString("조회갱신채널세부업무구분코드" , input.getString("CHN_DTLS_BWK_DTCD"));
	    	//iUpdOpnbAccBas.setString("계좌조회동의갱신일시"       , DateUtil.getDateTimeStr());
	    	
	    	iUpdOpnbAccBas.setString("출금동의여부"             , input.getString("출금동의여부"));
	    	//iUpdOpnbAccBas.setString("출금갱신채널세부업무구분코드" , input.getString("CHN_DTLS_BWK_DTCD"));
	    	//iUpdOpnbAccBas.setString("출금동의갱신일시"          , DateUtil.getDateTimeStr());
	    	
	    	iUpdOpnbAccBas.setString("고객계좌번호"             , input.getString("고객계좌번호"));
	    	iUpdOpnbAccBas.setString("계좌개설은행코드"          , input.getString("계좌개설은행코드"));
	    	iUpdOpnbAccBas.setString("오픈뱅킹이용기관코드"       , input.getString("오픈뱅킹이용기관코드"));
	    	iUpdOpnbAccBas.setString("시스템최종갱신식별자"       , "UBF2030207");
	    		    	
	    	int rtnUpd = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccBasUseYn" , iUpdOpnbAccBas);
	    	updTotCnt = updTotCnt + rtnUpd;
	    	
	    }
	    
	    String accDtlUseYn = "N";
	    //오픈뱅킹 계좌상세원장 존재하는지 체크하여 존재하면 사용여부 Y로 업데이트 존재안하면 INSERT
	    try {
	        
	    	LData iOpnbAccDtl = new LData();
	    	LData rOpnbAccDtl = new LData();
	    	
	    	iOpnbAccDtl.setString("고객계좌번호"       , input.getString("고객계좌번호"));        
	    	iOpnbAccDtl.setString("계좌개설은행코드"    , input.getString("계좌개설은행코드"));
	    	iOpnbAccDtl.setString("오픈뱅킹이용기관코드" , input.getString("오픈뱅킹이용기관코드"));
	    	iOpnbAccDtl.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
	    	iOpnbAccDtl.setString("CI내용"          , input.getString("CI내용"));
	    	
	    	rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
	    	accDtlUseYn = rOpnbAccDtl.getString("계좌상세사용여부");
       			        	
        } catch (LNotFoundException nfe) {
        	
        	accDtlUseYn = "NFE";
        }	
	    
	    if("NFE".equals(accDtlUseYn)) {//채널신규등록

	    	LData iInsAccDtl = new LData();
	    	
	    	iInsAccDtl.setString("고객계좌번호"          , input.getString("고객계좌번호"));
	    	iInsAccDtl.setString("계좌개설은행코드"       , input.getString("계좌개설은행코드"));
	    	iInsAccDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
	    	iInsAccDtl.setString("오픈뱅킹이용기관코드"    , input.getString("오픈뱅킹이용기관코드"));
	    	iInsAccDtl.setString("CI내용"              , input.getString("CI내용"));	    	
	    	iInsAccDtl.setString("준회원식별자"          , input.getString("준회원식별자"));
	    	iInsAccDtl.setString("고객식별자"           , input.getString("고객식별자"));
	    	iInsAccDtl.setString("오픈뱅킹플랫폼식별번호"  , input.getString("오픈뱅킹플랫폼식별번호"));
	    	iInsAccDtl.setString("계좌납입회차번호"       , input.getString("계좌납입회차번호"));
	    	iInsAccDtl.setString("개별저축은행명"        , input.getString("개별저축은행명"));
	    	iInsAccDtl.setString("출금동의여부"          , input.getString("출금동의여부"));
	    	//iInsAccDtl.setString("출금동의갱신일시"       , DateUtil.getDateTimeStr());
	    	iInsAccDtl.setString("대표계좌여부"         , input.getString("대표계좌여부"));
	    	iInsAccDtl.setString("계좌별명"            , input.getString("계좌별명"));
	    	iInsAccDtl.setInt("계좌표시순서"             , input.getInt("계좌표시순서"));
	    	iInsAccDtl.setString("계좌숨김여부"          , "N");	    	
	    	iInsAccDtl.setString("즐겨찾기계좌여부"       , input.getString("즐겨찾기계좌여부"));
	    	iInsAccDtl.setString("화면노출버튼구분코드"    , input.getString("화면노출버튼구분코드"));
	    	iInsAccDtl.setString("계좌상세사용여부"       , "Y");
	    	iInsAccDtl.setString("시스템최초생성식별자"    , "UBF2030207");
	    	iInsAccDtl.setString("시스템최종갱신식별자"    , "UBF2030207");
	    	        
		    int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccDtl" , iInsAccDtl);
		    updTotCnt = updTotCnt + rtnReg;
		    
		    if(rtnReg > 0) {
		    	//동의이력적재
				LData iOpnbChnPrStplCnsYn = new LData();
				OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
		    	
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
		    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , "3");//통합계좌조회
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의연장구분코드"    , "1");//1.약관동의
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의자료구분코드 "   , input.getString("오픈뱅킹동의자료구분코드"));//동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
		    	opnbCstMgCpbc.opnbCstCnsPhsRg(iOpnbChnPrStplCnsYn);
		    }
		    
	    }else if("N".equals(accDtlUseYn)){ //계좌상세사용 N일경우
	    	
	    	LData iUpdAccDtl = new LData();
	    	iUpdAccDtl.setString("고객계좌번호"          , input.getString("고객계좌번호"));
	    	iUpdAccDtl.setString("계좌개설은행코드"       , input.getString("계좌개설은행코드"));
	    	iUpdAccDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
	    	iUpdAccDtl.setString("오픈뱅킹이용기관코드"    , input.getString("오픈뱅킹이용기관코드"));
	    	iUpdAccDtl.setString("CI내용"              , input.getString("CI내용"));
	    	//계좌상세사용 N으로 UPDATE
	    	iUpdAccDtl.setString("계좌상세사용여부"       , "Y");
	    	iUpdAccDtl.setString("시스템최종갱신식별자"    , "UBF2030207");
	    	        
		    int rtnUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
		    updTotCnt = updTotCnt + rtnUpd;
		    
		    if(rtnUpd > 0) {
		    	 //동의이력적재
				LData iOpnbChnPrStplCnsYn = new LData();
				OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
		    	
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
		    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , "3");//통합계좌조회
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의연장구분코드"    , "1");//1.약관동의
		    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의자료구분코드 "   , input.getString("오픈뱅킹동의자료구분코드"));//동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
		    	opnbCstMgCpbc.opnbCstCnsPhsRg(iOpnbChnPrStplCnsYn);
		    }
		     
	    }else if("Y".equals(accDtlUseYn)){ //계좌상세사용 Y일경우
	    	
	    	//처리안함 고객존재
	    	LLog.debug.println("계좌상세 기등록 정상고객 로그 :" + input);
	    }
	    
	    rData.setInt("처리건수", updTotCnt);
	    
	    return rData;
    }
    
    /**
     * - UBF오픈뱅킹계좌기본에 사용여부가 Y인 계좌를 입력받은 채널로 계좌상세에 INSERT한다.
     * 
     * 1. CI번호로 사용자고객번호 조회
     * 2. 사용자번호로 현재 사용여부가 Y인계좌번호 조회
     * 3. 입력받은 채널로 상세테이블조회하여 SELECT INSERT 
     * 4. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 필수 : CI내용,채널세부업무구분코드
     * 
     * <OUTPUT>
     * 처리결과구분코드
     * 
     * @serviceID UBF2030209
     * @logicalName 채널별통합계좌정보목록조회
     * @method retvChnPrIngAccInfCtg
     * @method(한글명) 채널별통합계좌정보목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvChnPrIngAccInfCtg(LData input) throws LException {
    	LData result = new LData();
    	LData iOpnbAccDtl = null;
		LData rOpnbAccDtl = null;
    	
    	LData rstLData = null;
    	LMultiData rsltLMultiData = new LMultiData();
    	
    	String userSeqNo = "";
    	boolean regYn = true;
    	
    	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
    	
    	if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {			
  			LLog.debug.println("로그 " + input.getString("CI내용"));
  			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
  			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
  		}
        
    	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹플랫폼식별번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹플랫폼식별번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹플랫폼식별번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹플랫폼식별번호" ));//오픈뱅킹플랫폼식별번호가 존재하지 않습니다.
		}
    	
    	//사용자고유번호 조회
 		try {
 	    
 			LData iSelectUsrUno = new LData();
 			LData rSelectUsrUno = new LData();
 			
 			iSelectUsrUno.setString("CI내용"            , input.getString("CI내용"));
 			iSelectUsrUno.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
 			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
 			userSeqNo = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
 			
          	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
        	LLog.debug.println(input);
        		
  		} catch(LException e) {
  			
  			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
  			//throw new LBizException("CI로 오픈뱅킹사용자고유번호 조회 오류", e);
  		}
    	
 		//사용자고유번호 사용여부가 Y인계좌조회
 		//오픈뱅킹사용가능계좌조회
 		LData iSelectUseAcc = new LData();
		LMultiData rSelectUseAcc = new LMultiData();
 		
		iSelectUseAcc.setString("오픈뱅킹사용자고유번호", userSeqNo);//오픈뱅킹사용자고유번호
		iSelectUseAcc.setString("계좌사용여부"       , "Y");//계좌사용여부
		rSelectUseAcc = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectOpnbUseAblAcc" , iSelectUseAcc);
		
		for(int i = 0; i < rSelectUseAcc.getDataCount(); i++) {			
			LData tmpData = rSelectUseAcc.getLData(i);
			
			iOpnbAccDtl = new LData();
		    iOpnbAccDtl.setString("고객계좌번호"       , tmpData.getString("고객계좌번호"));        
		    iOpnbAccDtl.setString("계좌개설은행코드"    , tmpData.getString("계좌개설은행코드"));
		    iOpnbAccDtl.setString("오픈뱅킹이용기관코드" , tmpData.getString("오픈뱅킹이용기관코드"));
		    iOpnbAccDtl.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
		    iOpnbAccDtl.setString("CI내용"          , input.getString("CI내용"));
			
		    try {
		    
		    	rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
		    	
		    	rstLData = new LData();
		    	rstLData.putAll(tmpData);
		    	rstLData.putAll(rOpnbAccDtl);
		    	
		    	/*
			    rstLData.setString("CST_ACNO"                 , tmpData.getString("고객계좌번호"));
	    		rstLData.setString("ACC_OPE_BK_CD"            , tmpData.getString("계좌개설은행코드"));
	    		rstLData.setString("CHN_DTLS_BWK_DTCD"        , tmpData.getString("채널세부업무구분코드"));
	    		rstLData.setString("OPNB_UTZ_INS_CD"          , tmpData.getString("오픈뱅킹이용기관코드"));
	    		rstLData.setString("OPNB_USR_UNO"             , tmpData.getString("오픈뱅킹사용자고유번호"));
	    		rstLData.setString("FNTC_UTZ_NO"              , tmpData.getString("핀테크이용번호"));
	    		rstLData.setString("PYR_NO"                   , tmpData.getString("납부자번호"));
	    		rstLData.setString("OPNB_ACC_KD_DTCD"         , tmpData.getString("오픈뱅킹계좌종류구분코드"));
	    		rstLData.setString("OPNB_ACC_NMNL_DTCD"       , tmpData.getString("오픈뱅킹계좌명의구분코드"));
	    		rstLData.setString("OPNB_ACC_PD_NM"           , tmpData.getString("오픈뱅킹계좌상품명"));
	    		rstLData.setString("OPNB_EMAD"                , tmpData.getString("오픈뱅킹이메일주소"));
	    		rstLData.setString("ACC_INQ_CNS_YN"           , tmpData.getString("계좌조회동의여부"));
	    		rstLData.setString("ACC_INQ_CNS_RG_YMS"       , tmpData.getString("계좌조회동의등록일시"));
	    		rstLData.setString("INQ_RG_CHN_DTLS_BWK_DTCD" , tmpData.getString("조회등록채널세부업무구분코드"));
	    		rstLData.setString("ACC_INQ_CNS_UPD_YMS"      , tmpData.getString("계좌조회동의갱신일시"));
	    		rstLData.setString("INQ_UPD_CHN_DTLS_BWK_DTCD", tmpData.getString("조회갱신채널세부업무구분코드"));
	    		rstLData.setString("ACC_INQ_CNS_RVC_YMS"      , tmpData.getString("계좌조회동의해제일시"));
	    		rstLData.setString("INQ_RVC_CHN_DTLS_BWK_DTCD", tmpData.getString("조회해제채널세부업무구분코드"));
	    		rstLData.setString("ODW_CNS_YN"               , tmpData.getString("출금동의여부"));
	    		rstLData.setString("ODW_RG_CHN_DTLS_BWK_DTCD" , tmpData.getString("출금등록채널세부업무구분코드"));
	    		rstLData.setString("ODW_CNS_UPD_YMS"          , tmpData.getString("출금동의갱신일시"));
	    		rstLData.setString("ODW_UPD_CHN_DTLS_BWK_DTCD", tmpData.getString("출금갱신채널세부업무구분코드"));
	    		rstLData.setString("ODW_CNS_RVC_YMS"          , tmpData.getString("출금동의해제일시"));
	    		rstLData.setString("ODW_RVC_CHN_DTLS_BWK_DTCD", tmpData.getString("출금해제채널세부업무구분코드"));
	    		rstLData.setString("ACC_RG_YMS"               , tmpData.getString("계좌등록일시"));
	    		rstLData.setString("ACC_USE_YN"               , tmpData.getString("계좌사용여부"));
	    		rstLData.setString("ACC_ODW_CNS_RG_YMS"       , tmpData.getString("계좌출금동의등록일시"));
	    		
	    		rstLData.setString("ACC_PAYT_NTH_NO"          , rOpnbAccDtl.getString("계좌납입회차번호"));
	    		rstLData.setString("CI_CTT"                   , rOpnbAccDtl.getString("CI내용"));
	    		rstLData.setString("SEMI_MBR_IDF"             , rOpnbAccDtl.getString("준회원식별자"));
	    		rstLData.setString("CST_IDF"                  , rOpnbAccDtl.getString("고객식별자"));
	    		rstLData.setString("OPNB_PLFM_IDI_NO"         , rOpnbAccDtl.getString("오픈뱅킹플랫폼식별번호"));
	    		rstLData.setString("IDV_SVN_BK_NM"            , rOpnbAccDtl.getString("개별저축은행명"));
	    		rstLData.setString("RPS_ACC_YN"               , rOpnbAccDtl.getString("대표계좌여부"));
	    		rstLData.setString("ACC_ALS"                  , rOpnbAccDtl.getString("계좌별명"));
	    		rstLData.setInt("ACC_RPN_SEQ"                 , rOpnbAccDtl.getInt("계좌표시순서"));
	    		rstLData.setString("ACC_HIDE_YN"              , rOpnbAccDtl.getString("계좌숨김여부"));
	    		rstLData.setString("ACC_HIDE_YMS"             , rOpnbAccDtl.getString("계좌숨김일시"));
	    		rstLData.setString("BKMR_ACC_YN"              , rOpnbAccDtl.getString("즐겨찾기계좌여부"));
	    		rstLData.setString("SCE_EXPS_BTTN_DTCD"       , rOpnbAccDtl.getString("화면노출버튼구분코드"));
	    		rstLData.setString("ACC_DTL_USE_YN"           , rOpnbAccDtl.getString("계좌상세사용여부"));
	    		rstLData.setString("IDV_SVN_BK_CD"            , rOpnbAccDtl.getString("개별저축은행코드"));*/
		    	
		    	if("Y".equals(rOpnbAccDtl.getString("계좌상세사용여부"))) {
		    		rsltLMultiData.addLData(rstLData);	
		    	}
		    	
		    }catch(LNotFoundException nfe) { //계좌가 존재하지 않는다면
		    	
		    	LData iInsAccDtl = new LData();
			    	
		    	iInsAccDtl.setString("고객계좌번호"          , tmpData.getString("고객계좌번호"));
		    	iInsAccDtl.setString("계좌개설은행코드"       , tmpData.getString("계좌개설은행코드"));			    	
		    	iInsAccDtl.setString("오픈뱅킹이용기관코드"    , tmpData.getString("오픈뱅킹이용기관코드"));
		    	iInsAccDtl.setString("CI내용"              , input.getString("CI내용"));	    
		    	iInsAccDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
		    	iInsAccDtl.setString("오픈뱅킹플랫폼식별번호"   , input.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsAccDtl.setString("대표계좌여부"          , "N");
		    	iInsAccDtl.setString("계좌별명"             , "");
		    	iInsAccDtl.setString("계좌숨김여부"          , "N");	    	
		    	iInsAccDtl.setString("즐겨찾기계좌여부"       , "N");
		    	iInsAccDtl.setString("화면노출버튼구분코드"    , "");
		    	iInsAccDtl.setString("계좌상세사용여부"      , "Y");
		    	iInsAccDtl.setString("시스템최초생성식별자"   , "UBF2030209");
		    	iInsAccDtl.setString("시스템최종갱신식별자"   , "UBF2030209");
			    	        
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccDtlChnPr" , iInsAccDtl);
				
				if(rtnReg > 0) {
				
					rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
					
					rstLData = new LData();					
					rstLData.putAll(tmpData);					
			    	rstLData.putAll(rOpnbAccDtl);
			    	rsltLMultiData.addLData(rstLData);		
			    	
			    	if(regYn) {
			    	
			    		//동의이력적재
						LData iOpnbChnPrStplCnsYn = new LData();
				    	
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
				    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , "3");//통합계좌조회
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의연장구분코드"    , "1");//1.약관동의
						opnbCstMgCpbc.opnbCstCnsPhsRg(iOpnbChnPrStplCnsYn);
				    
					    regYn = false;
			    	}//if_end			    	
				}
		    }//catch_end
		   
		}//end_for
		
		result.set("GRID_cnt",rsltLMultiData.getDataCount());
		result.set("GRID",rsltLMultiData);
		
    	return result;
    }
    
    /**
     * - UBF오픈뱅킹계좌기본에 사용여부가 Y인 계좌를 입력받은 채널로 계좌상세에 INSERT한다.
     * 
     * 1. CI번호로 사용자고객번호 조회
     * 2. 사용자번호로 현재 사용여부가 Y인계좌번호 조회
     * 3. 입력받은 채널로 상세테이블조회하여 SELECT INSERT 
     * 4. 응답값 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세
     * 
     * <INPUT>
     * 필수 : CI내용,채널세부업무구분코드
     * 
     * <OUTPUT>
     * 처리결과구분코드
     * 
     * @serviceID UBF2030208
     * @logicalName 등록계좌정보채널복사처리 
     * @method prcRgAccInfChnCpy
     * @method(한글명) 등록계좌정보채널복사처리 
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcRgAccInfChnCpy(LData input) throws LException {
    	LData result = new LData();
    	LData iOpnbAccDtl = null;
		LData rOpnbAccDtl = null;
    	
    	LData rstLData = null;
    	LMultiData rsltLMultiData = new LMultiData();
    	
    	String userSeqNo = "";
    	boolean regYn = true;
    	
		OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
    	
    	if(StringUtil.trimNisEmpty(input.getString("CI내용"))) {			
  			LLog.debug.println("로그 " + input.getString("CI내용"));
  			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - CI내용"));//처리중 오류가 발생했습니다.
  			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-CI내용" ));//CI내용이 존재하지 않습니다.
  		}
        
    	if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹플랫폼식별번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹플랫폼식별번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹플랫폼식별번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹플랫폼식별번호" ));//오픈뱅킹플랫폼식별번호가 존재하지 않습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("해지계좌포함여부_V1"))) {
    		input.setString("해지계좌포함여부_V1" , "N" );
    	}
    	
    	//사용자고유번호 조회
 		try {
 	    
 			LData iSelectUsrUno = new LData();
 			LData rSelectUsrUno = new LData();
 			
 			iSelectUsrUno.setString("CI내용"            , input.getString("CI내용"));
 			iSelectUsrUno.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
 			rSelectUsrUno = opnbCstMgCpbc.retvUsrUno(iSelectUsrUno);
 			userSeqNo = rSelectUsrUno.getString("오픈뱅킹사용자고유번호");
 			
          	LLog.debug.println("CI로 오픈뱅킹사용자고유번호 조회::input::");
        	LLog.debug.println(input);
        		
  		} catch(LException e) {
  			
  			throw new LBizException(ObsErrCode.ERR_7777.getCode() , StringUtil.mergeStr( "오픈뱅킹사용자고유번호 조회 " , ObsErrCode.ERR_7777.getName() ,"(", ObsErrCode.ERR_7777.getCode(),")"));//처리중 오류가 발생했습니다.
  			//throw new LBizException("CI로 오픈뱅킹사용자고유번호 조회 오류", e);
  		}
    	
 		//사용자고유번호 사용여부가 Y인계좌조회
 		//오픈뱅킹사용가능계좌조회
 		LData iSelectUseAcc = new LData();
		LMultiData rSelectUseAcc = new LMultiData();
 		
		iSelectUseAcc.setString("오픈뱅킹사용자고유번호", userSeqNo);//오픈뱅킹사용자고유번호
		iSelectUseAcc.setString("계좌사용여부"       , "Y");//계좌사용여부
		rSelectUseAcc = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccInqEbc", "selectOpnbUseAblAcc" , iSelectUseAcc);
		
		for(int i = 0; i < rSelectUseAcc.getDataCount(); i++) {			
			LData tmpData = rSelectUseAcc.getLData(i);
			
			iOpnbAccDtl = new LData();
		    iOpnbAccDtl.setString("고객계좌번호"       , tmpData.getString("고객계좌번호"));        
		    iOpnbAccDtl.setString("계좌개설은행코드"    , tmpData.getString("계좌개설은행코드"));
		    iOpnbAccDtl.setString("오픈뱅킹이용기관코드" , tmpData.getString("오픈뱅킹이용기관코드"));
		    iOpnbAccDtl.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
		    iOpnbAccDtl.setString("CI내용"          , input.getString("CI내용"));
			
		    try {
		    
		    	rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
		    	
		    	rstLData = new LData();
		    	rstLData.putAll(tmpData);
		    	rstLData.putAll(rOpnbAccDtl);
		    	
		    	if("Y".equals(input.getString("해지계좌포함여부_V1"))) {
		    		
		    		if("N".equals(rOpnbAccDtl.getString("계좌상세사용여부"))) {
		    		
			    		LData iUpdAccDtl = new LData();
				    	iUpdAccDtl.setString("고객계좌번호"          , tmpData.getString("고객계좌번호"));
				    	iUpdAccDtl.setString("계좌개설은행코드"       , tmpData.getString("계좌개설은행코드"));
				    	iUpdAccDtl.setString("오픈뱅킹이용기관코드"    , tmpData.getString("오픈뱅킹이용기관코드"));
				    	iUpdAccDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
				    	iUpdAccDtl.setString("CI내용"              , input.getString("CI내용"));
				    	//계좌상세사용 N으로 UPDATE
				    	iUpdAccDtl.setString("계좌상세사용여부"      , "Y");
				    	iUpdAccDtl.setString("시스템최초생성식별자"   , "UBF2030209");
				    	iUpdAccDtl.setString("시스템최종갱신식별자"   , "UBF2030209");
				    	        
					    BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
		    		}
		    	
		    		rsltLMultiData.addLData(rstLData);
		    		
		    	}else {
		    		
		    		if("Y".equals(rOpnbAccDtl.getString("계좌상세사용여부"))) {
			    		rsltLMultiData.addLData(rstLData);	
			    	}
		    	}	
		    	
		    }catch(LNotFoundException nfe) { //계좌가 존재하지 않는다면
		    	
		    	LData iInsAccDtl = new LData();
			    	
		    	iInsAccDtl.setString("고객계좌번호"          , tmpData.getString("고객계좌번호"));
		    	iInsAccDtl.setString("계좌개설은행코드"       , tmpData.getString("계좌개설은행코드"));			    	
		    	iInsAccDtl.setString("오픈뱅킹이용기관코드"    , tmpData.getString("오픈뱅킹이용기관코드"));
		    	iInsAccDtl.setString("CI내용"              , input.getString("CI내용"));	    
		    	iInsAccDtl.setString("채널세부업무구분코드"    , input.getString("채널세부업무구분코드"));
		    	iInsAccDtl.setString("오픈뱅킹플랫폼식별번호"   , input.getString("오픈뱅킹플랫폼식별번호"));
		    	iInsAccDtl.setString("대표계좌여부"          , "N");
		    	iInsAccDtl.setString("계좌별명"             , "");
		    	iInsAccDtl.setString("계좌숨김여부"          , "N");	    	
		    	iInsAccDtl.setString("즐겨찾기계좌여부"       , "N");
		    	iInsAccDtl.setString("화면노출버튼구분코드"    , "");
		    	iInsAccDtl.setString("계좌상세사용여부"      , "Y");
		    	iInsAccDtl.setString("시스템최초생성식별자"   , "UBF2030208");
		    	iInsAccDtl.setString("시스템최종갱신식별자"   , "UBF2030208");
			    	        
				int rtnReg = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "insertOpnbAccDtlChnPr" , iInsAccDtl);
				
				if(rtnReg > 0) {
					
					rOpnbAccDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbAccMgEbc", "selectOpnbAccDtl" , iOpnbAccDtl);
					
					rstLData = new LData();
					
					rstLData.putAll(tmpData);					
			    	rstLData.putAll(rOpnbAccDtl);
					rsltLMultiData.addLData(rstLData);
					
					if(regYn) {
					
						//동의이력적재
						LData iOpnbChnPrStplCnsYn = new LData();
				    	
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹사용자고유번호"     , userSeqNo);
				    	iOpnbChnPrStplCnsYn.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹약관동의구분코드"    , "3");//통합계좌조회
				    	iOpnbChnPrStplCnsYn.setString("오픈뱅킹동의연장구분코드"    , "1");//1.약관동의
						opnbCstMgCpbc.opnbCstCnsPhsRg(iOpnbChnPrStplCnsYn);
						  
					    regYn = false;
				    
					}//if_end
					
				}
		    }//catch_end
		   
		}//end_for
		
		result.set("GRID_cnt",rsltLMultiData.getDataCount());
		result.set("GRID",rsltLMultiData);
		
    	return result;
    }
    
    
}

