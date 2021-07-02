package com.kbcard.ubf.pbi.opnb.opnbPuse.cfreUtzRg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
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

/** 
 * opnbCfreUtzRgPbc
 * 
 * @logicalname  : 오픈뱅킹무료이용등록관리Pbc
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

public class OpnbCfreUtzRgPbc {

    /**
     * - 오픈뱅킹 무료거래내역등록
     * 
     * 1. 오픈뱅킹사용자고유번호와 거래정보로 거래내역 등록을 요청
     * 2. 무료이용대상에 해당하는지 확인 
     * 3. 현재거래일에 해당하는 유효한 오프뱅킹루료정책중 일련번호가 가장 작은것부터 조회하여 건수가 존재하는 것부터 차감
     * 4. 처리결과 리턴"
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * 
     * <INPUT>
     * 채널세부업무구분코드,오픈뱅킹사용자고유번호
     *
     * @serviceID UBF2020101
     * @logicalName 무료거래내역등록
     * @method regtTrHis
     * @method(한글명) 거래 내역 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtTrHis(LData input) throws LException {
    	LData result = new LData();		
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
         * */
    	
    	result = opnbCstMgCpbc.regtCfreTrHis(input);
		
        return result;
    }

    /**
     * - 오픈뱅킹무료거래내역 목록조회
     * 
     * 1. 오픈뱅킹사용자고유번호로 무료거래내역 목록조회 요청
     * 2. 무료거래내역 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역
     * UBF오픈뱅킹고객별무료거래내역
     * 
     * <INPUT>
     * 거래시작일자 , 거래종료일자 , 오픈뱅킹사용자고유번호,채널세부구분코드
     * <OUTPUT>
     * 오픈뱅킹거래년월,출금기관명,입금기관명,오픈뱅킹거래금액,무료정책일련번호,오픈뱅킹무료거래일련번호
     * 
     * @serviceID UBF2020102
     * @logicalName 무료거래내역목록조회
     * @method retvCfreTrHisCtg
     * @method(한글명) 무료거래 내역 목록 조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvCfreTrHisCtg(LData input) throws LException {
    	
    	LData tmpLData = new LData();
    	LData rstLData = new LData();
    	
    	LData result = new LData();
    	LMultiData rsltLMultiData = new LMultiData();
        
	    if(StringUtil.trimNisEmpty(input.getString("거래시작년월일_V8"))) {			
			LLog.debug.println("로그 " + input.getString("거래시작년월일_V8"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 거래시작년월일"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-거래시작년월일" ));//거래시작년월일가 존재하지 않습니다.
		}
      
        if(StringUtil.trimNisEmpty(input.getString("거래종료년월일_V8"))) {			
			LLog.debug.println("로그 " + input.getString("거래종료년월일_V8"));	
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 거래종료년월일"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-거래종료년월일" ));//거래종료년월일가 존재하지 않습니다.
		}
      
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {			
   			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
   			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
   			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
   		}
   		
   		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
   			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
   			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));//처리중 오류가 발생했습니다.
   			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
   		}
        
        LData iCfreTrHisCtg = new LData();
   	    LMultiData rCfreTrHisCtg = new LMultiData();
        
   	    iCfreTrHisCtg.setString("거래시작일시"       , StringUtil.mergeStr(input.getString("거래시작년월일_V8"),"000000"));//무료정책시작일자_V8
   	    iCfreTrHisCtg.setString("거래종료일시"       , StringUtil.mergeStr(input.getString("거래종료년월일_V8"),"235959"));//무료정책종료일자_V8
   	    iCfreTrHisCtg.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));//무료정책종료일자_V8
   
   	    rCfreTrHisCtg = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "selectCfreTrHisCtg", iCfreTrHisCtg);
       
  		for(int i = 0 ; i < rCfreTrHisCtg.getDataCount(); i++ ) { 
	    	   tmpLData = rCfreTrHisCtg.getLData(i);    	   
	    	   rstLData = new LData();
	    	   
	    	   rstLData.setString("오픈뱅킹사용자고유번호"       , tmpLData.getString("오픈뱅킹사용자고유번호"));//오픈뱅킹사용자고유번호
	    	   rstLData.setString("채널세부업무구분코드"        , tmpLData.getString("채널세부업무구분코드"));//채널세부업무구분코드
	    	   rstLData.setString("오픈뱅킹무료이용일시_V14"    , tmpLData.getString("오픈뱅킹무료건수갱신일시"));//오픈뱅킹무료이용일시_V14
	    	   rstLData.setInt("오픈뱅킹무료잔여건수_N10"       , tmpLData.getInt("오픈뱅킹무료거래건수"));//오픈뱅킹무료잔여건수_N10
	    	   rstLData.setInt("오픈뱅킹무료정책일련번호"        , tmpLData.getInt("오픈뱅킹무료정책일련번호"));//오픈뱅킹무료정책일련번호
	    	   rstLData.setInt("오픈뱅킹무료거래일련번호"        , tmpLData.getInt("오픈뱅킹무료거래일련번호"));//오픈뱅킹무료거래일련번호 
	    	   
	    	   rsltLMultiData.addLData(rstLData);
	     }
     
	     result.set("GRID",rsltLMultiData);

        return result;
    }

    /**
     * - 오픈뱅킹무료거래내역 상세조회
     * 
     * 1. 오픈뱅킹사용자고유번호와 거래고유번호로 무료거래내역 상세조회 요청
     * 2. 상세정보 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹무료거래내역   
     * UBF오픈뱅킹무료이용정책기본    
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,채널세부구분코드,무료정책일련번호,거래년월일
     * <OUTPUT>
     * 오픈뱅킹거래년월일,출금기관명,입금기관명,오픈뱅킹거래금액,오픈뱅킹무료정책명,오픈뱅킹무료이용구분코드,오픈뱅킹혜택구분코드,오픈뱅킹혜택대상내용
     * 
     * @serviceID UBF2020103
     * @logicalName 무료거래내역상세조회
     * @method retvCfreTrHisDtl
     * @method(한글명) 무료거래 내역 상세 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCfreTrHisDtl(LData input) throws LException {
    	
    	 LData result = new LData();
         
         if(input.getInt("오픈뱅킹무료정책일련번호") == 0) {			
 			LLog.debug.println("로그 " + input.getInt("오픈뱅킹무료정책일련번호"));
 			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료정책일련번호"));//처리중 오류가 발생했습니다.
 			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료정책일련번호" ));//오픈뱅킹무료정책일련번호가 존재하지 않습니다.
 		 }
         
         if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {			
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		 }
    		
		 if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		 }
		 
		 if(input.getInt("오픈뱅킹무료거래일련번호") == 0) {			
	 		LLog.debug.println("로그 " + input.getInt("오픈뱅킹무료거래일련번호"));
	 		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹무료거래일련번호"));//처리중 오류가 발생했습니다.
	 		//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹무료거래일련번호" ));//오픈뱅킹무료거래일련번호가 존재하지 않습니다.
	 	 }
         
         LData iCfreTrHisDtl = new LData();
         LData rCfreTrHisDtl = new LData();
    	    
         try {
         	
        	 iCfreTrHisDtl.setString("채널세부업무구분코드"       , input.getString("채널세부업무구분코드"));//채널세부업무구분코드   
        	 iCfreTrHisDtl.setString("오픈뱅킹사용자고유번호"     , input.getString("오픈뱅킹사용자고유번호"));//오픈뱅킹사용자고유번호   
        	 iCfreTrHisDtl.setInt("오픈뱅킹무료정책일련번호"       , input.getInt("오픈뱅킹무료정책일련번호"));//오픈뱅킹무료정책일련번호    
        	 iCfreTrHisDtl.setInt("오픈뱅킹무료거래일련번호"       , input.getInt("오픈뱅킹무료거래일련번호"));//오픈뱅킹무료거래일련번호
        	 
        	 rCfreTrHisDtl = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "selectCfreTrHisDtl", iCfreTrHisDtl);
    	    
         	 result.setString("오픈뱅킹무료정책명"         , rCfreTrHisDtl.getString("오픈뱅킹무료정책명"));//오픈뱅킹무료정책명
         	 result.setString("채널세부업무구분코드"        , rCfreTrHisDtl.getString("채널세부업무구분코드"));//채널세부업무구분코드
         	 result.setString("오픈뱅킹무료이용유형구분코드"  , rCfreTrHisDtl.getString("오픈뱅킹무료이용유형구분코드"));//오픈뱅킹무료이용유형구분코드
             result.setString("오픈뱅킹혜택구분코드"        , rCfreTrHisDtl.getString("오픈뱅킹혜택구분코드"));//오픈뱅킹혜택구분코드
             result.setString("오픈뱅킹혜택대상내용"        , rCfreTrHisDtl.getString("오픈뱅킹혜택대상내용"));//오픈뱅킹혜택대상내용
             result.setString("오픈뱅킹무료이용일시_V14"    , rCfreTrHisDtl.getString("오픈뱅킹무료건수갱신일시"));//오픈뱅킹무료이용일시_V14
             result.setInt("오픈뱅킹무료잔여건수_N10"       , rCfreTrHisDtl.getInt("오픈뱅킹무료거래건수"));//오픈뱅킹무료잔여건수_N10
             result.setString("오픈뱅킹무제한여부"         , rCfreTrHisDtl.getString("오픈뱅킹무제한여부"));//오픈뱅킹무제한여부
             result.setString("오픈뱅킹기간구분코드"        , rCfreTrHisDtl.getString("오픈뱅킹기간구분코드"));//오픈뱅킹기간구분코드
             
         	
         } catch (LNotFoundException nfe) {        
         	
         	throw new LException("오픈뱅킹무료거래내역상세조회이 존재하지 않습니다.", nfe);
         }

        return result;
    }

    /**
     * - 오픈뱅킹을 무료로 거래할 수 있는 잔여건수를 조회
     * 
     * 1. 오픈뱅킹사용자고유번호로 무료거래 잔여건수를 조회요청
     * 2. 잔여건수 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객별무료거래내역
     * 
     * <INPUT>
     * 오픈뱅킹사용자고유번호,채널세부구분코드
     * <OUTPUT>
     * 무료거래잔여건수
     * 
     * @serviceID UBF2020104
     * @logicalName 무료거래잔여건수조회 
     * @method retvCfreTrRmgNcn
     * @method(한글명) 무료거래 잔여 건수 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvCfreTrRmgNcn(LData input) throws LException {
        LData result = new LData();
        LMultiData rstMulData = new LMultiData();
        
        String aplStYms = "";//적용시작일시
        String aplEdYms = "";//적용종료일시 
        
        String curDateTm = DateUtil.getDateTimeStr();
        String trmDtcd = "";//오픈뱅킹기간구분코드
        
        if(StringUtil.trimNisEmpty(input.getString("채널세부업무구분코드"))) {			
			LLog.debug.println("로그 " + input.getString("채널세부업무구분코드"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 채널세부업무구분코드"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-채널세부업무구분코드" ));//채널세부업무구분코드가 존재하지 않습니다.
		}
		
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹사용자고유번호"))) {
			LLog.debug.println("로그 " + input.getString("오픈뱅킹사용자고유번호"));
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹사용자고유번호"));//처리중 오류가 발생했습니다.
			//throw new LBizException(ObsErrCode.ERR_9000.getCode(), StringUtil.mergeStr(ObsErrCode.ERR_9000.getName(),"-오픈뱅킹사용자고유번호" ));//오픈뱅킹사용자고유번호가 존재하지 않습니다.
		}
		    
		//무제한여부 정책이 존재하는지 조회		
		try {
			
			LData iNbRccTrTgPlcy = new LData();
			LData rNbRccTrTgPlcy = new LData();
			
			iNbRccTrTgPlcy.setString("채널세부업무구분코드", input.getString("채널세부업무구분코드"));
			iNbRccTrTgPlcy.setString("거래일시", curDateTm);
			rNbRccTrTgPlcy = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectOpnbNbRccTrTgPlcy" , iNbRccTrTgPlcy);
	
			result.setInt("오픈뱅킹무료잔여건수_N10"  , 99999);//오픈뱅킹무료잔여건수_N10
			//result.setInt("오픈뱅킹무료잔여건수_N10", rNbRccTrTgPlcy.getInt("오픈뱅킹무료제공건수"));//오픈뱅킹무료잔여건수_N10
			return result;
			
		}catch(LNotFoundException nfe) {
		
	        //현재 유효한 무료거래정책조회
			try {
				
				LData inpLData = new LData(); 
				inpLData.setString("거래일시"          , DateUtil.getDateTimeStr());
				inpLData.setString("채널세부업무구분코드" , input.getString("채널세부업무구분코드"));
				inpLData.setString("오픈뱅킹무제한여부"   , "N");
				
				rstMulData = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbCfreUtzPlcyMgEbc", "selectOpnbVldCfrePlcyCtg" , inpLData);
				
			} catch (LException e) {
				
				result.setInt("오픈뱅킹무료잔여건수_N10", 0);//오픈뱅킹무료잔여건수_N10
				return result;
			}
			
			if(rstMulData.getDataCount() == 0) {
			
				result.setInt("오픈뱅킹무료잔여건수_N10", 0);//오픈뱅킹무료잔여건수_N10
				return result;
			}
			
			LData rFreeCnt = new LData();
			LData rCfreTrRmgCnt = new LData();
			
			int sOpnbCfrePlcySno = 0;
			int sOpnbFreeCnt = 0; //총무료제공건수
			int sOpnbFreeUseCnt = 0;//무료정책별 이용건수
			int sOpnbFreeUseTotCnt = 0; //무료정책별 총이용건수
			
			for(int i = 0 ; i < rstMulData.getDataCount(); i++) {
				
				aplStYms = "";//적용시작일시
		        aplEdYms = "";//적용종료일시 
				
				rFreeCnt = rstMulData.getLData(i);
				
				trmDtcd = rFreeCnt.getString("오픈뱅킹기간구분코드");//오픈뱅킹기간구분코드	
				sOpnbCfrePlcySno = rFreeCnt.getInt("오픈뱅킹무료정책일련번호");//오픈뱅킹무료정책일련번호			
				sOpnbFreeCnt = sOpnbFreeCnt + rFreeCnt.getInt("오픈뱅킹무료제공건수");//오픈뱅킹무료제공건수
					
				LData inpLData = new LData();
				inpLData.setString("오픈뱅킹사용자고유번호", input.getString("오픈뱅킹사용자고유번호"));
				inpLData.setString("채널세부업무구분코드", rFreeCnt.getString("채널세부업무구분코드"));
				inpLData.setInt("오픈뱅킹무료정책일련번호", sOpnbCfrePlcySno);
				
				String yyyyMmDd = StringUtil.substring(curDateTm, 0, 8);
				String yyyyMm = StringUtil.substring(curDateTm, 0, 6);
				String yyyy = StringUtil.substring(curDateTm, 0, 4);
						
				if("Y".equals(trmDtcd)) {		
							
					aplStYms = StringUtil.mergeStr(yyyy,"0101000000");//적용시작일시
				    aplEdYms = StringUtil.mergeStr(yyyy,"1231235959");//적용종료일시 
							
				}else if("M".equals(trmDtcd)) {
							
					aplStYms = StringUtil.mergeStr(yyyyMm,"01000000");//적용시작일시
			        aplEdYms = StringUtil.mergeStr(yyyyMm,DateUtil.getLastDayOfMonth(yyyyMm),"235959");//적용종료일시
			     		    
				}else if("D".equals(trmDtcd)) {
							
					aplStYms = StringUtil.mergeStr(yyyyMmDd,"000000");//적용시작일시
			        aplEdYms = StringUtil.mergeStr(yyyyMmDd,"235959");//적용종료일시
		
				}
				
				inpLData.setString("거래시작일시"   , aplStYms);
				inpLData.setString("거래종료일시"   , aplEdYms);
				
				try {
				
					rCfreTrRmgCnt = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.cfreUtzRg.OpnbCfreUtzRgEbc", "selectCfreTrRmgNcn" , inpLData);
					sOpnbFreeUseCnt = rCfreTrRmgCnt.getInt("오픈뱅킹무료거래건수");
						
					sOpnbFreeUseTotCnt = sOpnbFreeUseTotCnt + sOpnbFreeUseCnt;
				
				}catch(LException e){
					
					result.setInt("오픈뱅킹무료잔여건수_N10", 0);//오픈뱅킹무료잔여건수_N10
					return result;
	
				}
				
			}
			
			LLog.debug.println("정책유효한무료총건수 " + sOpnbFreeCnt);
			LLog.debug.println("현재사용한무료거래건수 " + sOpnbFreeUseTotCnt);
	        
			result.setInt("오픈뱅킹무료잔여건수_N10", sOpnbFreeCnt - sOpnbFreeUseTotCnt);//오픈뱅킹무료잔여건수_N10
		
		}
		
        return result;
    }

}


