package com.kbcard.ubf.pbi.opnb.opnbMg.opnbUseMg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.ext.core.exception.LDuplicateException;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.ext.util.DateUtil;
import devonenterprise.ext.util.TypeConvertUtil;
import devonenterprise.util.StringUtil;

/** 
 * opnbCfreUtzPlcyMgPbc
 * 
 * @logicalname  : 오픈뱅킹무료이용정책관리Pbc
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

public class OpnbCfreUtzPlcyMgPbc {

    /**
     * - 오픈뱅킹무료이용 정책 등록
     * 1. 무료정책정보를 등록 요청
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본 - 
     * 
     * <INPUT>
     *  채널세부업무구분코드<필수> - T1.KBPay T8.리브메이트 ...
     *  오픈뱅킹무료정책명<필수>
     *  오픈뱅킹무료이용유형구분코드 - 1.기본제한 2.프로모션 3.고객
     *  오픈뱅킹무료정책시작일시 - yyyyMMddHHmmss
     *  오픈뱅킹무료정책종료일시 - yyyyMMddHHmmss
     *  오픈뱅킹혜택구분코드 - 01.채널 02.이벤트  03.예약 04.신규 05.카드 06.대출
     *  오픈뱅킹혜택대상내용 
     *  오픈뱅킹무제한여부 - 아무값도 없을시 Defalut 'N' 
     *  오픈뱅킹기간구분코드 - D.일  M.월 Y.년
     *  오픈뱅킹무료제공건수
     *  오픈뱅킹타스템연계여부 - 아무값도 없을시 Defalut 'N'
     *  
     * @serviceID UBF1010501
     * @logicalName 오픈뱅킹무료이용 정책 등록
     * @method regtCfreUtzPlcy
     * @method(한글명) 무료이용정책 등록
     * @param LData
     * @return LData
     * @throws LException
     *  
     */ 
	 public LData regtCfreUtzPlcy(LData input) throws LException {    	
    	LData result = new LData();		
    	
        LData iRegData = new LData();
        int sOpnbCfrePlcySno = 0;
        
	    LLog.debug.println("오픈뱅킹 무료이용정책기본");	    
		LLog.debug.println(input);
		
		LLog.debug.println("로그☆☆☆☆☆☆☆☆☆☆☆☆" + input.getString("채널세부업무구분코드"));
		
	    if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
	    
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책명"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책명"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책명"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책명" ));//오픈뱅킹무료정책명이 존재하지 않습니다.
		}
	    
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹기간구분코드"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹기간구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹기간구분코드"));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹기간구분코드" ));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
		}
	    
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책시작일시"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책시작일시"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책시작일시"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책시작일시" ));//오픈뱅킹무료정책시작일시가 존재하지 않습니다.
		}
		    
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책종료일시"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책종료일시"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책종료일시"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책종료일시" ));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
		}
		
		long stYmd = TypeConvertUtil.parseTo_long(input.getString("오픈뱅킹무료정책시작일시"));
		long edYmd = TypeConvertUtil.parseTo_long(input.getString("오픈뱅킹무료정책종료일시"));
		
		if(stYmd > edYmd ) {
			throw new LBizException(ObsErrCode.ERR_9002.getCode() , ObsErrCode.ERR_9002.getName());//시작일시가 종료일시보다 클수 없습니다.
		};
			
		//오픈뱅킹무료정책일련번호 채번조회		
		try {
			
			LData iInputData = new LData();
			LData rCdcCrdLst = new LData();
			                                         
			rCdcCrdLst = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectOpnbCfrePlcySnoNbr",iInputData); // 오픈뱅킹API사용자관리EBC.오픈뱅킹API사용자등록
			                                           
			sOpnbCfrePlcySno = rCdcCrdLst.getInt("오픈뱅킹무료정책일련번호");
			
		} catch (LNotFoundException nfe) {
			
			throw new LBizException("오픈뱅킹무료정책일련번호 NotFound오류", nfe);
		}
		
		iRegData.setInt("오픈뱅킹무료정책일련번호"       , sOpnbCfrePlcySno);
		iRegData.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
		iRegData.setString("오픈뱅킹무료정책명"        , input.getString("오픈뱅킹무료정책명"));
		iRegData.setString("오픈뱅킹무료이용유형구분코드" , input.getString("오픈뱅킹무료이용유형구분코드"));
		iRegData.setString("오픈뱅킹무료정책시작일시"    , input.getString("오픈뱅킹무료정책시작일시"));
		iRegData.setString("오픈뱅킹무료정책종료일시"    , input.getString("오픈뱅킹무료정책종료일시"));
		iRegData.setString("오픈뱅킹혜택구분코드"       , input.getString("오픈뱅킹혜택구분코드"));
		iRegData.setString("오픈뱅킹혜택대상내용"       , input.getString("오픈뱅킹혜택대상내용"));
		
		iRegData.setString("오픈뱅킹무제한여부"       , input.getString("오픈뱅킹무제한여부"));
		
		if(StringUtil.isEmpty(input.getString("오픈뱅킹무제한여부"))) {
			iRegData.setString("오픈뱅킹무제한여부"         , "N");	
		}
		
		iRegData.setString("오픈뱅킹기간구분코드"       , input.getString("오픈뱅킹기간구분코드"));
		iRegData.setInt("오픈뱅킹무료제공건수"          , input.getInt("오픈뱅킹무료제공건수"));
		iRegData.setString("오픈뱅킹타시스템연계여부"    , input.getString("오픈뱅킹타시스템연계여부"));
		
		if(StringUtil.isEmpty(input.getString("오픈뱅킹타시스템연계여부"))) {
			iRegData.setString("오픈뱅킹타시스템연계여부"         , "N");	
		}
		
		iRegData.setString("시스템최초생성식별자"       , "UBF1010501");
		iRegData.setString("시스템최종갱신식별자"       , "UBF1010501");
		
		LLog.debug.println("오픈뱅킹무료이용정책기본등록 Ldata");
		LLog.debug.println(iRegData);
		
		try {
				
			int insCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "insertCfreUtzPlcy", iRegData); // 오픈뱅킹API사용자관리EBC.오픈뱅킹API사용자등록
		
			if(insCnt == 0 ) {
				result.setString("처리결과_V1", "N");
			}else {
				result.setString("처리결과_V1", "Y");
			}
			
		} catch (LDuplicateException e) {
				
			throw new LBizException("오픈뱅킹무료이용정책기본등록 Dup오류",e);
		}		
        
        return result;
    }

    /**
     *  - 오픈뱅킹무료이용 정책 목록조회
     * 
     * 1. 무료정책 목록조회를 요청
     * 2. 정책정보 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     *  채널세부업무구분코드 ,유효여부_V1(정책유효여부)
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹무료정책일련번호,채널세부업무구분코드,오픈뱅킹무료정책명,오픈뱅킹무료제공건수,무료정책시작일시,무료정책종료일시
     * 
     * @serviceID UBF1010502
     * @logicalName 무료이용정책 목록 조회
     * @method retvCfreUtzPlcyCtg
     * @method(한글명) 무료이용정책 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvCfreUtzPlcyCtg(LData input) throws LException {
    	LData tmpLData = new LData();
    	LData rstLData = new LData();
    	
    	LData result = new LData();
    	LMultiData rsltLMultiData = new LMultiData();
      
        LData iCfreUtzPlcyCtg = new LData();
   	    LMultiData rCfreUtzPlcyCtg = new LMultiData();
        
   	    iCfreUtzPlcyCtg.setString("채널세부업무구분코드"   , input.getString("채널세부업무구분코드"));
   	    
   	    //정책유효여부
   	    if("Y".equals(input.getString("유효여부_V1"))) { // 정책유효여부(유효여부_V1)
   	    
   	    	iCfreUtzPlcyCtg.setString("COND"              , "CURYN");
   	    	iCfreUtzPlcyCtg.setString("거래일시"            , DateUtil.getDateTimeStr());
   	    }
   	    
  		rCfreUtzPlcyCtg = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectCfreUtzPlcyCtg", iCfreUtzPlcyCtg);
       
  		for(int i = 0 ; i < rCfreUtzPlcyCtg.getDataCount(); i++ ) { 
	    	   tmpLData = rCfreUtzPlcyCtg.getLData(i);    	   
	    	   rstLData = new LData();
	    	   
	    	   rstLData.setInt("오픈뱅킹무료정책일련번호"         , tmpLData.getInt("오픈뱅킹무료정책일련번호"));//오픈뱅킹무료정책일련번호
	    	   rstLData.setString("채널세부업무구분코드"         , tmpLData.getString("채널세부업무구분코드"));//채널세부업무구분코드
	    	   rstLData.setString("오픈뱅킹무료정책명"          , tmpLData.getString("오픈뱅킹무료정책명"));//오픈뱅킹무료정책명
	    	   rstLData.setString("오픈뱅킹무료제공건수"         , tmpLData.getString("오픈뱅킹무료제공건수"));//오픈뱅킹무료제공건수
	    	   rstLData.setString("오픈뱅킹무료정책시작일시"      , tmpLData.getString("오픈뱅킹무료정책시작일시"));//오픈뱅킹무료정책시작일시
	    	   rstLData.setString("오픈뱅킹무료정책종료일시"      , tmpLData.getString("오픈뱅킹무료정책종료일시"));//오픈뱅킹무료정책종료일시
	    	   
	    	   rsltLMultiData.addLData(rstLData);
	     }
     
	     result.set("GRID",rsltLMultiData);
  		
        return result;
    }

    /**
     * - 무료이용정책 상세조회
     * 
     * 1. 오픈뱅킹무료정책일련번호로 상세조회 요청
     * 2. 무료정책 상세정보 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본
     * 
     * <INPUT>
     * 오픈뱅킹무료정책일련번호
     * <OUTPUT>
     * 오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹 무료정책시작일시,오픈뱅킹무료정책종료일시,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용,오픈뱅킹무제한여부,오픈뱅킹기간구분코드,오픈뱅킹무료제공건수,오픈뱅킹타스템연계여부
     * 
     * @serviceID UBF1010503
     * @logicalName 무료이용정책 상세 조회
     * @method retvCfreUtzPlcyDtl
     * @method(한글명) 무료이용정책 상세 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCfreUtzPlcyDtl(LData input) throws LException {
        LData result = new LData();
        
        if(input.getInt("오픈뱅킹무료정책일련번호") == 0) {			
			LLog.debug.println("로그 " + input.getInt("오픈뱅킹무료정책일련번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책일련번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책일련번호" ));//오픈뱅킹무료정책일련번호가 존재하지 않습니다.
		}
        
        LData iCfreUtzPlcyDtl = new LData();
        LData rCfreUtzPlcyDtl = new LData();
   	    
        try {
        	
        	iCfreUtzPlcyDtl.setInt("오픈뱅킹무료정책일련번호"     , input.getInt("오픈뱅킹무료정책일련번호"));//오픈뱅킹무료정책일련번호        
        	rCfreUtzPlcyDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectCfreUtzPlcyDtl", iCfreUtzPlcyDtl);
   	    
        	result.setString("채널세부업무구분코드"       , rCfreUtzPlcyDtl.getString("채널세부업무구분코드"));//채널세부업무구분코드
            result.setString("오픈뱅킹무료정책명"        , rCfreUtzPlcyDtl.getString("오픈뱅킹무료정책명"));//오픈뱅킹무료정책명
            result.setString("오픈뱅킹무료이용유형구분코드" , rCfreUtzPlcyDtl.getString("오픈뱅킹무료이용유형구분코드"));//오픈뱅킹무료이용유형구분코드
            result.setString("오픈뱅킹무료정책시작일시"    , rCfreUtzPlcyDtl.getString("오픈뱅킹무료정책시작일시"));//오픈뱅킹무료정책시작일시
            result.setString("오픈뱅킹무료정책종료일시"    , rCfreUtzPlcyDtl.getString("오픈뱅킹무료정책종료일시"));//오픈뱅킹무료정책종료일시
            result.setString("오픈뱅킹혜택구분코드"       , rCfreUtzPlcyDtl.getString("오픈뱅킹혜택구분코드"));//오픈뱅킹혜택구분코드
            result.setString("오픈뱅킹혜택대상내용"       , rCfreUtzPlcyDtl.getString("오픈뱅킹혜택대상내용"));//오픈뱅킹혜택대상내용
            result.setString("오픈뱅킹무제한여부"        , rCfreUtzPlcyDtl.getString("오픈뱅킹무제한여부"));//오픈뱅킹무제한여부
            result.setString("오픈뱅킹기간구분코드"       , rCfreUtzPlcyDtl.getString("오픈뱅킹기간구분코드"));//오픈뱅킹기간구분코드
            result.setString("오픈뱅킹무료제공건수"       , rCfreUtzPlcyDtl.getString("오픈뱅킹무료제공건수"));//오픈뱅킹무료제공건수
            result.setString("오픈뱅킹타시스템연계여부"    , rCfreUtzPlcyDtl.getString("오픈뱅킹타시스템연계여부"));//오픈뱅킹타시스템연계여부
        	
        } catch (LNotFoundException nfe) {        
        	
        	LLog.debug.println("오픈뱅킹무료이용정책상세내역이 존재하지 않습니다");
        	//throw new LBizException("오픈뱅킹무료이용정책상세내역이 존재하지 않습니다.", nfe);        	
        }
   	     
        return result;
    }
    
    
    /**
     * - 오픈뱅킹무료이용 정책 등록
     * 1. 무료정책정보를 등록 요청
     * 2. 처리결과 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료이용정책기본 - 
     * 
     * <INPUT>
     *  채널세부업무구분코드<필수> - T1.KBPay T8.리브메이트 ...
     *  오픈뱅킹무료정책명<필수>
     *  오픈뱅킹무료이용유형구분코드 - 1.기본제한 2.프로모션 3.고객
     *  오픈뱅킹무료정책시작일시 - yyyyMMddHHmmss
     *  오픈뱅킹무료정책종료일시 - yyyyMMddHHmmss
     *  오픈뱅킹혜택구분코드 - 01.채널 02.이벤트  03.예약 04.신규 05.카드 06.대출
     *  오픈뱅킹혜택대상내용 
     *  오픈뱅킹무제한여부 - 아무값도 없을시 Defalut 'N' 
     *  오픈뱅킹기간구분코드 - D.일  M.월 Y.년
     *  오픈뱅킹무료제공건수
     *  오픈뱅킹타스템연계여부 - 아무값도 없을시 Defalut 'N'
     *  
     * @serviceID UBF1010504
     * @logicalName 오픈뱅킹무료이용정책변경
     * @method chngCfreUtzPlcy
     * @method(한글명) 무료이용정책변경
     * @param LData
     * @return LData
     * @throws LException
     *  
     */ 
	 public LData chngCfreUtzPlcy(LData input) throws LException {    	
    	LData result = new LData();		
    
	    LLog.debug.println("오픈뱅킹 무료이용정책변경");	    
		LLog.debug.println(input);
		
		if(input.getInt("오픈뱅킹무료정책일련번호") == 0) {			
			LLog.debug.println("로그 " + input.getInt("오픈뱅킹무료정책일련번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책일련번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책일련번호" ));//오픈뱅킹무료정책일련번호가 존재하지 않습니다.
		}
		
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책명"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책명"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책명"));//오픈뱅킹무료정책명이 존재하지 않습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책명" ));//오픈뱅킹무료정책명이 존재하지 않습니다.
		}
		    
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹기간구분코드"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹기간구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹기간구분코드"));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹기간구분코드" ));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
		}
		
	    if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책시작일시"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책시작일시"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책시작일시"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책시작일시" ));//오픈뱅킹무료정책시작일시가 존재하지 않습니다.
		}
		  
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹무료정책종료일시"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹무료정책종료일시"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책종료일시"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책종료일시" ));//오픈뱅킹무료정책종료일시가 존재하지 않습니다.
		}
		
		long stYmd = TypeConvertUtil.parseTo_long(input.getString("오픈뱅킹무료정책시작일시"));
		long edYmd = TypeConvertUtil.parseTo_long(input.getString("오픈뱅킹무료정책종료일시"));
		
		if(stYmd > edYmd ) {
			throw new LBizException(ObsErrCode.ERR_9002.getCode() , ObsErrCode.ERR_9002.getName());//시작일시가 종료일시보다 클수 없습니다.
		};
		
		//고객무료거래내역이 있을경우 변경불가
	    try {
         	
	    	 LData iCfreTrHisDtl = new LData();
	    	 LData rCfreTrHisDtl = new LData();
	    	    
	    	 iCfreTrHisDtl.setInt("오픈뱅킹무료정책일련번호"       , input.getInt("오픈뱅킹무료정책일련번호"));//오픈뱅킹무료정책일련번호       	 
       	     rCfreTrHisDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "selectCfreTnhsExstYn", iCfreTrHisDtl);
       	     
       	     if("Y".equals(rCfreTrHisDtl.getString("존재여부"))) {       	    	 
       	    	
       	    	 throw new LBizException(ObsErrCode.ERR_7700.getCode() , StringUtil.mergeStr( "무료거래내역이 존재하여 무료정책을 " , ObsErrCode.ERR_7700.getName()));//수정할수 없습니다.
       	     }
        	
        } catch (LNotFoundException nfe) {        
        	
        	LLog.debug.println("거래내역없음 통과==================================");
        }
		
		//오픈뱅킹무료정책일련번호 채번조회		
		try {
			
			LData iInputData = new LData();
			
			iInputData.setInt("오픈뱅킹무료정책일련번호"       , input.getInt("오픈뱅킹무료정책일련번호"));
			iInputData.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));
			iInputData.setString("오픈뱅킹무료정책명"        , input.getString("오픈뱅킹무료정책명"));
			iInputData.setString("오픈뱅킹무료이용유형구분코드" , input.getString("오픈뱅킹무료이용유형구분코드"));
			iInputData.setString("오픈뱅킹무료정책시작일시"    , input.getString("오픈뱅킹무료정책시작일시"));
			iInputData.setString("오픈뱅킹무료정책종료일시"    , input.getString("오픈뱅킹무료정책종료일시"));
			iInputData.setString("오픈뱅킹혜택구분코드"       , input.getString("오픈뱅킹혜택구분코드"));
			iInputData.setString("오픈뱅킹혜택대상내용"       , input.getString("오픈뱅킹혜택대상내용"));
			iInputData.setString("오픈뱅킹무제한여부"        , input.getString("오픈뱅킹무제한여부"));
			iInputData.setString("오픈뱅킹기간구분코드"       , input.getString("오픈뱅킹기간구분코드"));
			
			if(!StringUtil.trimNisEmpty(input.getString("오픈뱅킹무제한여부"))) {
				iInputData.setString("오픈뱅킹무제한여부"        , input.getString("오픈뱅킹무제한여부"));	 
			}
			
			if(input.getInt("오픈뱅킹무료제공건수") > 0) {
				iInputData.setInt("오픈뱅킹무료제공건수"          , input.getInt("오픈뱅킹무료제공건수"));	
			}
			
			iInputData.setString("오픈뱅킹타시스템연계여부"    , input.getString("오픈뱅킹타시스템연계여부"));
			
			int updCnt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "updateCfreUtzPlcy",iInputData); // 오픈뱅킹API사용자관리EBC.오픈뱅킹API사용자등록
			
			if(updCnt == 0 ) {
				LLog.debug.println("==============정책이 존재하지 않습니다.====================");
				result.setString("처리결과_V1", "N");
			}else {
				result.setString("처리결과_V1", "Y");
			}
			
		} catch (LException e) {
			
			throw new LBizException("오픈뱅킹무료정책변경 처리 중 오류", e);
		}
		
        return result;
    }

}

