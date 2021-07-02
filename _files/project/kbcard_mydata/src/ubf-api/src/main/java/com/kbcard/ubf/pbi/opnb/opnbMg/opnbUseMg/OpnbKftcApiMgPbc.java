package com.kbcard.ubf.pbi.opnb.opnbMg.opnbUseMg;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LBizException;
import devonenterprise.util.StringUtil;

/** 
 * opnbKftcApiMgPbc
 * 
 * @logicalname  : 오픈뱅킹금결원API관리Pbc
 * @author       : 김정화
 * @since        : 2021-04-30
 * @version      : 1.0 
 * @see          : 
 * 
 * << 개정이력(Modification Information) >>
 *
 *       수정일         수정자        수정내용   
 *  ---------------    ---------    ---------------------------
 *   2021-04-30       김정화       최초 생성
 *
 */

public class OpnbKftcApiMgPbc {

    /**
     * - 금결원에 등록된 API와 수수료 금액을 조회. 
     * 
     * 1. [오픈뱅킹URL일련번호] 항목으로 UBF오픈뱅킹금융결제원API기본 테이블 조회
     * 2. 오픈뱅킹API관리내용 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹금융결제원API기본
     * <INPUT>
     * 1. 오픈뱅킹URL일련번호 존재
     * 2. 오픈뱅킹URL일련번호 미존재
     * <OUTPUT>
     * LIST
     *  - 오픈뱅킹URL일련번호, 오픈뱅킹API관리구분코드, 오픈뱅킹이용기관수수료, 오픈뱅킹수수료대상여부, 오픈뱅킹API명, 오픈뱅킹URL내용
     * 
     * @serviceId UBF1010601
     * @method retvOpnbKftcApiInf
     * @method(한글명) 오픈뱅킹금결원API정보조회
     * @param LData
     * @return LMultiData
     * @throws LException 
     */ 
    public LData retvOpnbKftcApiInf(LData input) throws LException {
        
        LLog.debug.println("OpnbKftcApiMgPbc.retvOpnbKftcApiInf[오픈뱅킹금결원API관리Pbc.오픈뱅킹금결원API정보조회] START ☆★☆☆★☆☆★☆" +  input);
        LData iRetvOpnbKftcApiInfP = input; //i오픈뱅킹금결원API정보조회입력
        LData rRetvOpnbKftcApiInfP = new LData(); //r오픈뱅킹금결원API정보조회결과

        //Validation Check
        if(StringUtil.trimNisEmpty(iRetvOpnbKftcApiInfP.getString("채널세부업무구분코드"))) {//채널세부업무구분코드
        	 throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
        }
        
		//Ebc 호출
		LData iRetvOpnbKftcApiInfIn = new LData(); // 오픈뱅킹금결원API정보조회입력
		LMultiData rRetvOpnbKftcApiInfOut = new LMultiData(); // 오픈뱅킹금결원API정보조회출력
		 
	    try {
			iRetvOpnbKftcApiInfIn.setString("오픈뱅킹URL일련번호", iRetvOpnbKftcApiInfP.getString("오픈뱅킹URL일련번호"));
			rRetvOpnbKftcApiInfOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbMg.opnbUseMg.OpnbKftcApiMgEbc", "selectOpnbKftcApiInf", iRetvOpnbKftcApiInfIn);
	    }catch(LException le) {
			le.printStackTrace();
			throw new LBizException(ObsErrCode.ERR_F002.getCode(), ObsErrCode.ERR_F002.getName());	
		}
    	
		//오픈뱅킹URL일련번호가 존재하는데, 목록이 없으면 에러 
		if((StringUtil.trimNisEmpty(iRetvOpnbKftcApiInfP.getString("오픈뱅킹URL일련번호")) == false) &&  rRetvOpnbKftcApiInfOut.getDataCount() == 0) {
		 	 	throw new LBizException(ObsErrCode.ERR_F001.getCode(), ObsErrCode.ERR_F001.getName());	
		}
		
		rRetvOpnbKftcApiInfP.setInt("그리드_cnt", rRetvOpnbKftcApiInfOut.getDataCount());
		rRetvOpnbKftcApiInfP.set("그리드", rRetvOpnbKftcApiInfOut);
		LLog.debug.println("OpnbKftcApiMgPbc.retvOpnbKftcApiInf[오픈뱅킹금결원API관리Pbc.오픈뱅킹금결원API정보조회] END ☆★☆☆★☆☆★☆" + rRetvOpnbKftcApiInfP );
      
		

        return rRetvOpnbKftcApiInfP;
    }

}

