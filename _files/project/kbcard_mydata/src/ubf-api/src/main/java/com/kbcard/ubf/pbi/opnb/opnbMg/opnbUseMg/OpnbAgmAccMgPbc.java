package com.kbcard.ubf.pbi.opnb.opnbMg.opnbUseMg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.opnbMg.opnbUseMg.OpnbCdMgCpbc;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.util.StringUtil;

/** 
 * opnbAgmAccMgPbc
 * 
 * @logicalname  : 오픈뱅킹약정계좌관리Pbc
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

public class OpnbAgmAccMgPbc {

	
	OpnbCdMgCpbc opnbCdMgCpbc = new OpnbCdMgCpbc();
	
    /**
     * - 오픈뱅킹 모계좌 정보 등록
     * 
     * 1. 오픈뱅킹약정계좌기본 원장에 약정계좌 정보 등록
     * 2. 오픈뱅킹업무구분기본 원장에 해당 약정계좌에 대한 업무구분 정보 등록
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹약정계좌기관코드<필수>
     *  오픈뱅킹계좌계정구분코드<필수> 
     *  오픈뱅킹약정계정계좌번호 <필수>
     *  예금주명<필수>
     *  오픈뱅킹모계좌용도구분코드<필수> 
     *  오픈뱅킹업무구분명<필수>
     *  오픈뱅킹약정계좌설명
     * <OUTPUT>
     * 
     * @serviceID UBF1010401
     * @method regtAgmAcc
     * @method(한글명) 약정계좌 등록
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData regtAgmAcc(LData input) throws LException {
        LData result = new LData();
        
        LData iRegData = new LData();
        
        int agmAccSno = 0;
        
        LLog.debug.println("약정계좌 등록::Input::");
		LLog.debug.println(input);
    	
		if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹약정계좌기관코드"))) {
			throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹약정계좌기관코드"));//처리중 오류가 발생했습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹계좌계정구분코드"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹계좌계정구분코드"));//처리중 오류가 발생했습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹약정계정계좌번호"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹약정계정계좌번호"));//처리중 오류가 발생했습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("예금주명"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 예금주명"));//처리중 오류가 발생했습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹모계좌용도구분코드"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹모계좌용도구분코드"));//처리중 오류가 발생했습니다.
		}
    	
    	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹업무구분명"))) {
    		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹업무구분명"));//처리중 오류가 발생했습니다.
		}
		
    	try {
    		
    		LData rAgmAccSnoSeq = new LData();
    		rAgmAccSnoSeq = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbAgmAccMgEbc", "selectAgmAccSnoSeq"); // 오픈뱅킹API사용자관리EBC.오픈뱅킹API사용자등록
				
			agmAccSno = rAgmAccSnoSeq.getInt("오픈뱅킹약정계좌일련번호");
			
			LLog.debug.println("약정계좌 일련번호 채번::");
			LLog.debug.println(agmAccSno);
			
    	} catch (LException e) {
    		throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr( "오픈뱅킹약정계좌일련번호 채번 " , ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		}
    	
    	iRegData.setInt("오픈뱅킹약정계좌일련번호"      , agmAccSno);
    	iRegData.setString("오픈뱅킹약정계좌설명"      , input.getString("오픈뱅킹약정계좌설명"));
    	iRegData.setString("오픈뱅킹약정계좌기관코드"   , input.getString("오픈뱅킹약정계좌기관코드"));
    	iRegData.setString("오픈뱅킹계좌계정구분코드"   , input.getString("오픈뱅킹계좌계정구분코드"));
    	iRegData.setString("오픈뱅킹약정계정계좌번호"   , input.getString("오픈뱅킹약정계정계좌번호"));
    	iRegData.setString("예금주명"              , input.getString("예금주명"));
    	iRegData.setString("오픈뱅킹업무구분명"       , input.getString("오픈뱅킹업무구분명"));
    	iRegData.setString("오픈뱅킹모계좌용도구분코드" , input.getString("오픈뱅킹모계좌용도구분코드"));
        
    	LLog.debug.println("오픈뱅킹약정계좌정보::");
		LLog.debug.println(iRegData);
		
		int prcRst = 0;
		
        try {
        	
        	prcRst = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbAgmAccMgEbc", "regOpnbAgmAccBas", iRegData); // 약정계좌 등록(UBF오픈뱅킹약정계좌기본)
        	
        	prcRst = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbAgmAccMgEbc", "regOpnbBwkDcBas", iRegData); // 약정계좌 등록(UBF오픈뱅킹업무구분기본)
            
		} catch (LException e) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr( "오픈뱅킹약정계좌 등록 " , ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		}
        
        if(prcRst > 0) {
        	result.setString("처리결과_V1", "Y");
        } else {
        	result.setString("처리결과_V1", "N");
        }
        
        return result;
    }

    /**
     * - 오픈뱅킹 모계좌 정보 조회
     * 
     * 1. 오픈뱅킹약정계좌기본, 오픈뱅킹업무구분기본 원장에서 약정계좌 정보 조회
     * 
     * <관련 테이블>
     *  UBF오픈뱅킹약정계좌기본
     *  UBF오픈뱅킹업무구분기본
     * <INPUT>
     *  오픈뱅킹모계좌용도구분코드<필수>
     * <OUTPUT>
     *  오픈뱅킹약정계좌일련번호
     *  오픈뱅킹약정계좌기관코드
     *  오픈뱅킹계좌계정구분코드
     *  오픈뱅킹약정계정계좌번호
     *  예금주명
     *  오픈뱅킹모계좌용도구분코드
     *  오픈뱅킹업무구분명
     *  오픈뱅킹약정계좌설명
     *   
     * @serviceID UBF1010402
     * @method retvAgmAcc
     * @method(한글명) 약정계좌 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvAgmAcc(LData input) throws LException {

    	LData result = new LData();
    	
    	LData iRegData = new LData();
    	LData rResData = new LData();
    	
        LLog.debug.println("약정계좌 조회::Input::");
 		LLog.debug.println(input);
     	
     	if(StringUtil.trimNisEmpty(input.getString("오픈뱅킹모계좌용도구분코드"))) {
     		throw new LBizException(ObsErrCode.ERR_9000.getCode() , StringUtil.mergeStr( ObsErrCode.ERR_9000.getName() ,"(", ObsErrCode.ERR_9000.getCode(),")"," - 오픈뱅킹모계좌용도구분코드"));//처리중 오류가 발생했습니다.
 		}

     	iRegData.setString("오픈뱅킹모계좌용도구분코드", input.getString("오픈뱅킹모계좌용도구분코드"));
     	
     	try {
        	
     		result = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbAgmAccMgEbc", "retvLstOpnbAgmAccInq", iRegData); // 약정계좌 조회
            
		} catch (LException e) {
			throw new LBizException(ObsErrCode.ERR_7777.getCode(), StringUtil.mergeStr( "오픈뱅킹약정계좌 조회 " , ObsErrCode.ERR_7777.getName()));//처리중 오류가 발생했습니다.
		}
     	
       	rResData.setInt("오픈뱅킹약정계좌일련번호"     , result.getInt("오픈뱅킹약정계좌일련번호"));
       	rResData.setString("오픈뱅킹약정계좌기관코드"  , result.getString("오픈뱅킹약정계좌기관코드"));
       	rResData.setString("오픈뱅킹계좌계정구분코드"  , result.getString("오픈뱅킹계좌계정구분코드"));
       	rResData.setString("오픈뱅킹약정계정계좌번호"  , result.getString("오픈뱅킹약정계정계좌번호"));
       	rResData.setString("예금주명"              , result.getString("예금주명"));
       	rResData.setString("오픈뱅킹모계좌용도구분코드" , result.getString("오픈뱅킹모계좌용도구분코드"));
       	rResData.setString("오픈뱅킹업무구분명"      , result.getString("오픈뱅킹업무구분명"));
    	rResData.setString("오픈뱅킹약정계좌설명"     , result.getString("오픈뱅킹약정계좌설명"));
    	
    	LLog.debug.println("약정계좌 조회::result::");
  		LLog.debug.println(rResData);

        return rResData;
    }

}

