package com.kbcard.ubf.pbi.opnb.opnbPuse.usrSvc;

import org.apache.commons.lang.StringUtils;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.AuthInfo;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;
import com.kbcard.ubf.cpbi.opnb.apiCllg.kftcApi.OpnbApiCpbc;
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
 * opnbUsrMgPbc
 * 
 * @logicalname  : 오픈뱅킹사용자관리Pbc
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

public class OpnbUsrMgPbc {

    /**
     * - 오픈뱅킹등록여부조회
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용] 항목으로 요청
     * 2. UBF오픈뱅킹고객정보기본 테이블에 존재하는지 확인
     * 3-1. 존재시 
     *  - 등록여부 TRUE 리턴
     * 3-2. 미존재시
     *  - 등록여부 FALSE 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 등록여부(T/F)
     * 
     * @serviceId UBF2030101
     * @method retvRgYn
     * @method(한글명) 등록여부 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvRgYn(LData input) throws LException {
		LLog.debug.println("OpnbUsrMgPbc.retvRgYn[오픈뱅킹사용자관리Pbc.등록여부 조회] START ☆★☆☆★☆☆★☆" + input );

        LData iRetvRgYnP = input; //i등록여부조회입력
        LData rRetvRgYnP = new LData(); //r등록여부조회결과
		
        //Validation Check
 		if(StringUtils.isEmpty(iRetvRgYnP.getString("채널세부업무구분코드"))) { 
 			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
		}
 		if(StringUtil.trimNisEmpty(iRetvRgYnP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iRetvRgYnP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iRetvRgYnP.getString("준회원식별자")) &&
 				StringUtil.trimNisEmpty(iRetvRgYnP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 		
 		try {
			LData iSelectRgYnIn = new LData(); // 등록여부조회입력
			LData rSelectRgYnOut = new LData(); // 등록여부조회출력
			iSelectRgYnIn.setString("오픈뱅킹사용자고유번호", iRetvRgYnP.getString("오픈뱅킹사용자고유번호"));
			
	        if(StringUtil.trimNisEmpty(iRetvRgYnP.getString("오픈뱅킹사용자고유번호"))) { //사용자고유번호가 없으면
		        //사용자고유번호조회
	        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 			
				LData iRetvUsrNo = iRetvRgYnP; 
				LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
				iSelectRgYnIn.setString("오픈뱅킹사용자고유번호", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
	        }
	        rSelectRgYnOut = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectRgYn", iSelectRgYnIn);
	
	        if(StringUtil.trimNisEmpty(rSelectRgYnOut.getString("오픈뱅킹사용자고유번호")) == false) {
	        	rRetvRgYnP.setString("오픈뱅킹등록여부_V1", "Y");
	        }
 		}catch(LNotFoundException lnfe) { 		//오픈뱅킹고객정보기본 테이블에 해당 사용자 미존재
 			lnfe.printStackTrace();
 			rRetvRgYnP.setString("오픈뱅킹등록여부_V1", "N");
	
 		}catch(LBizException le) {
 			le.printStackTrace();
 			if("1000".equals(le.getCode())){
 				rRetvRgYnP.setString("오픈뱅킹등록여부_V1", "N");
 			}else {
 				throw new LBizException(ObsErrCode.ERR_1001.getCode(), ObsErrCode.ERR_1001.getName());
 			}
 		}
 	
 		
		LLog.debug.println("OpnbUsrMgPbc.retvRgYn[오픈뱅킹사용자관리Pbc.등록여부 조회] END ☆★☆☆★☆☆★☆" + rRetvRgYnP );

        return rRetvRgYnP;
    }

    /**
     * 1. 고객식별자/준회원식별자/CI내용 등으로 API 호출
     * 2. 오픈뱅킹사용자고유번호 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * 오픈뱅킹사용자고유번호
     * 
     * @serviceId UBF2030102
     * @method retvUsrUno
     * @method(한글명) 사용자고유번호조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvUsrUno(LData input) throws LException {
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("사용자고유번호조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
    	LData iRetvUsrUnoP = input; // i사용자고유번호조회입력
    	LData rRetvUsrUnoP = new LData(); // r사용자고유번호조회출력
        
    	try {
    		
    		// Validation Check
        	if(StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
     			throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
    		}
        	if(StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("CI내용"))
        			&& StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("고객식별자"))
        			&& StringUtil.trimNisEmpty(iRetvUsrUnoP.getString("준회원식별자"))) {
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	
        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc();
        	
        	rRetvUsrUnoP = opnbCstMgCpbc.retvUsrUno(iRetvUsrUnoP);
        	
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
    	
        if(LLog.debug.isEnabled()) {
        	LLog.debug.println("----------[rRetvUsrUnoP]----------");
        	LLog.debug.println(rRetvUsrUnoP);
        	LLog.debug.println("사용자고유번호조회 END ☆★☆☆★☆☆★☆" );
        }
		
        return rRetvUsrUnoP;
    }

    /**
     * - 사용자의 오픈뱅킹등록여부(등록여부조회 API)가 Y일 때 등록한 채널구분코드 리턴
     * - 오픈뱅킹을 타 채널에서 등록한 사용자에게 채널명을 화면에 표시하기 위함
     * ex) 고객님은 현재 'KBPay'에서 오픈뱅킹을 사용 중입니다. 사용중이던 오픈뱅킹 정보를 가져올까요?
     * 
     * 1. 오픈뱅킹사용자고유번호로 API 호출
     * 2. 채널구분코드 목록 리턴
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호
     * <OUTPUT>
     * LIST
     *   - 채널구분코드
     * 
     * @serviceId UBF2030103
     * @method retvRgChnCtg
     * @method(한글명) 등록채널목록조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvRgChnCtg(LData input) throws LException {
        
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("등록채널목록조회 START ☆★☆☆★☆☆★☆");
    		LLog.debug.println("----------[input]----------");
    		LLog.debug.println(input);
    	}
    	
    	LData iRetvRgChnCtgP = input; // i등록채널목록조회입력
        LData rRetvRgChnCtgP = new LData(); // r등록채널목록조회출력

        try {
			
        	// Validation Check
        	if(StringUtil.trimNisEmpty(iRetvRgChnCtgP.getString("조회채널세부업무구분코드"))) {
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	if(StringUtil.trimNisEmpty(iRetvRgChnCtgP.getString("오픈뱅킹사용자고유번호"))) {
        		throw new LBizException(ObsErrCode.ERR_9000.getCode(), ObsErrCode.ERR_9000.getName());
        	}
        	
        	// Ebc 호출
    		LData iSelectRgChnCtgIn = new LData(); // 등록채널목록조회입력
    		LMultiData rSelectRgChnCtgOut = new LMultiData(); // 등록채널목록조회출력
    		iSelectRgChnCtgIn.setString("오픈뱅킹사용자고유번호", iRetvRgChnCtgP.getString("오픈뱅킹사용자고유번호"));
    		
    		try {
    			rSelectRgChnCtgOut = (LMultiData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectRgChnCtg", iSelectRgChnCtgIn);
    		} catch (LException e) {
    			throw new LBizException(ObsErrCode.ERR_1008.getCode(), ObsErrCode.ERR_1008.getName());
    		}
    		
    		LMultiData tmprSelectRgChnCtgOut = new LMultiData();
    		
    		for(int i=0; i<rSelectRgChnCtgOut.getDataCount(); i++) {
    			
    			LData tmpSelectRgChnCtg = new LData();
    			
    			tmpSelectRgChnCtg.setString("채널세부업무구분코드", rSelectRgChnCtgOut.getLData(i).getString("채널세부업무구분코드"));
    			
    			tmprSelectRgChnCtgOut.addLData(tmpSelectRgChnCtg);
    		}
    		
    		rRetvRgChnCtgP.setInt("그리드_cnt", rSelectRgChnCtgOut.getDataCount());
    		rRetvRgChnCtgP.set("그리드", tmprSelectRgChnCtgOut);
    		
		} catch (LException e) {
			throw new LBizException(e.getMessage(), "", e.getOptionalInfo().getMessageAddContent());
		}
    	
    	if(LLog.debug.isEnabled()) {
    		LLog.debug.println("----------[rRetvRgChnCtgP]----------");
    		LLog.debug.println(rRetvRgChnCtgP);
    		LLog.debug.println("등록채널목록조회 END ☆★☆☆★☆☆★☆" );
    	}
		
        return rRetvRgChnCtgP;
        
    }

    /**
     * - 오픈뱅킹센터에 등록된 사용자의 고객정보 및 등록한 서비스목록을 조회. 해지된 서비스는 출력하지 않으며 고객정보의 제공 범위는 이용기관에 따라서 선별적으로 제공됨. 
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용] 항목으로 요청
     * 2. 사용자정보조회 금결원API 호출
     *
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용
     * <OUTPUT>
     * [UBF오픈뱅킹고객정보기본] 
     * 
     * @serviceId UBF2030104
     * @method retvUsrInf
     * @method(한글명) 사용자정보 조회
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData retvUsrInf(LData input) throws LException {

    	LLog.debug.println("OpnbUsrMgPbc.retvUsrInf[오픈뱅킹사용자관리Pbc.사용자정보 조회] START ☆★☆☆★☆☆★☆" +  input);
        LData iRetvUsrInfP = input; //i사용자정보조회입력
        LData rRetvUsrInfP = new LData(); //r사용자정보조회결과

	       
    	//Validation Check
 		if(StringUtils.isEmpty(iRetvUsrInfP.getString("채널세부업무구분코드"))) { 
 			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
		}
 		if(StringUtil.trimNisEmpty(iRetvUsrInfP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iRetvUsrInfP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iRetvUsrInfP.getString("준회원식별자"))&&
 				StringUtil.trimNisEmpty(iRetvUsrInfP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 		

 	    //사용자정보조회 금결원API 호출
		LData iRetvUsrInfAPICall = new LData(); //i사용자정보조회API호출입력
        LData rRetvUsrInfAPICall = new LData(); //r사용자정보조회API호출결과
        
        iRetvUsrInfAPICall.setString("user_seq_no", iRetvUsrInfP.getString("오픈뱅킹사용자고유번호")); //사용자일련번호
        iRetvUsrInfAPICall.setString("채널세부업무구분코드", iRetvUsrInfP.getString("채널세부업무구분코드")); //채널세부업무구분코드
        
        if(StringUtil.trimNisEmpty(iRetvUsrInfP.getString("오픈뱅킹사용자고유번호"))) { //사용자고유번호가 없으면
	        //사용자고유번호조회
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iRetvUsrInfP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			iRetvUsrInfAPICall.setString("user_seq_no", rRetvUsrNo.getString("오픈뱅킹사용자고유번호"));
        }
        

        OpnbApiCpbc opnbApi = new OpnbApiCpbc();
        rRetvUsrInfAPICall = opnbApi.retvUsrInfAPICall(iRetvUsrInfAPICall); //사용자정보조회 API호출
        LLog.debug.println("OpnbApiCpbc.issueTkenAPICall RESULT ☆★☆☆★☆☆★☆" + rRetvUsrInfAPICall);
        
        /**응답셋팅 START **/
        rRetvUsrInfP.setString("API거래고유번호_V40", rRetvUsrInfAPICall.getString("api_tran_id")); //오픈뱅킹API거래고유번호
        rRetvUsrInfP.setString("API거래일시_N17", rRetvUsrInfAPICall.getString("api_tran_dtm")); //API거래일시_N17
        rRetvUsrInfP.setString("API응답코드_V5", rRetvUsrInfAPICall.getString("rsp_code")); //오픈뱅킹API응답구분코드
        rRetvUsrInfP.setString("API응답메시지_V300", rRetvUsrInfAPICall.getString("rsp_message")); //오픈뱅킹API응답메시지내용 
        rRetvUsrInfP.setString("오픈뱅킹사용자고유번호", rRetvUsrInfAPICall.getString("user_seq_no")); //오픈뱅킹사용자고유번호
        rRetvUsrInfP.setString("CI내용", rRetvUsrInfAPICall.getString("user_ci")); //CI내용
        rRetvUsrInfP.setString("고객명", rRetvUsrInfAPICall.getString("user_name")); //고객명
        rRetvUsrInfP.setString("고객출생년월일", rRetvUsrInfAPICall.getString("user_info")); //고객출생년월일
        rRetvUsrInfP.setString("성별구분코드", rRetvUsrInfAPICall.getString("user_gender")); //성별구분코드
        rRetvUsrInfP.setString("오픈뱅킹사용자휴대폰번호", rRetvUsrInfAPICall.getString("user_cell_no")); //오픈뱅킹사용자휴대폰번호
        rRetvUsrInfP.setString("오픈뱅킹이메일주소전문내용", rRetvUsrInfAPICall.getString("user_email")); //오픈뱅킹이메일주소
        rRetvUsrInfP.setInt("그리드1_cnt", rRetvUsrInfAPICall.getInt("res_cnt")); //오픈뱅킹등록계좌수
        LMultiData resList = (LMultiData) rRetvUsrInfAPICall.get("res_list");
        LMultiData newResList = new LMultiData();
        for(int i =0; i < resList.size(); i++) {
        	LData temp = resList.getLData(i); 
        	LData newTemp = new LData();
   
        	newTemp.setString("핀테크이용번호", temp.getString("fintech_use_num")); //핀테크이용번호
        	newTemp.setString("계좌별명", temp.getString("account_alias")); //계좌별명
        	newTemp.setString("개설기관대표코드_V3", temp.getString("bank_code_std")); //오픈뱅킹개설기관대표코드
        	newTemp.setString("개설기관점별코드_V7", temp.getString("bank_code_sub")); //오픈뱅킹개설기관지점별코드
        	newTemp.setString("오픈뱅킹개설기관명", temp.getString("bank_name")); //오픈뱅킹개설기관명
            newTemp.setString("개별저축은행명", temp.getString("savings_bank_name")); //개별저축은행명
            newTemp.setString("계좌번호", temp.getString("account_num")); //계좌번호
            newTemp.setString("마스킹계좌번호_V20", temp.getString("account_num_masked")); //마스킹계좌번호
            newTemp.setString("예금회차번호", temp.getString("account_seq")); //예금회차번호
            newTemp.setString("오픈뱅킹등록계좌예금주명", temp.getString("account_holder_name")); //오픈뱅킹등록계좌예금주명
            newTemp.setString("오픈뱅킹계좌명의구분코드", temp.getString("account_holder_type")); //오픈뱅킹계좌명의구분코드
            newTemp.setString("오픈뱅킹계좌종류구분코드", temp.getString("account_type")); //오픈뱅킹계좌종류구분코드
            newTemp.setString("계좌조회동의여부", temp.getString("inquiry_agree_yn")); //계좌조회동의여부
            newTemp.setString("계좌조회동의등록일시 ", temp.getString("inquiry_agree_dtime")); //계좌조회동의등록일시 
            newTemp.setString("출금동의여부", temp.getString("transfer_agree_yn")); //출금동의여부
            newTemp.setString("계좌출금동의등록일시", temp.getString("transfer_agree_dtime")); //계좌출금동의등록일시
            newTemp.setString("오픈뱅킹납부자번호", temp.getString("payer_num")); //오픈뱅킹납부자번호
            newResList.add(newTemp);
        }
        rRetvUsrInfP.set("그리드1", newResList); //오픈뱅킹등록계좌목록
        rRetvUsrInfP.setInt("그리드2_cnt", rRetvUsrInfAPICall.getInt("inquiry_card_cnt")); //카드정보조회동의건수
        LMultiData cardList = (LMultiData) rRetvUsrInfAPICall.get("inquiry_card_list");
        LMultiData newCardList = new LMultiData();
        for(int i =0; i < cardList.size(); i++) {
        	LData temp = cardList.getLData(i); 
        	LData newTemp = new LData();

        	newTemp.setString("기관대표코드", temp.getString("bank_code_std")); //기관대표코드
        	newTemp.setString("오픈뱅킹회원금융회사코드", temp.getString("member_bank_code")); //오픈뱅킹회원금융회사코드
        	newTemp.setString("카드정보조회동의등록일시  ", temp.getString("inquiry_agree_dtime")); // 카드정보조회동의등록일시  
            newCardList.add(newTemp);
        }
        rRetvUsrInfP.set("그리드2", newCardList); //카드정보조회동의목록
        /**응답셋팅 END **/

		LLog.debug.println("OpnbUsrMgPbc.retvUsrInf[오픈뱅킹사용자관리Pbc.사용자정보 조회] END ☆★☆☆★☆☆★☆" + rRetvUsrInfP );
      
		return rRetvUsrInfP;

    }

    /**
     * - 이용기관에 등록된 모든 서비스를 해지하고 오픈뱅킹에 등록된 사용자를 탈퇴. 이용기관은 장기 미사용 등을 이유로 고객정보 파기 시에는 사용자 탈퇴를 해야함. 
     * - 채널 별로 UBF테이블 상태 업데이트
     * - 모든 채널 탈퇴 시 UBF오픈뱅킹고객정보기본 업데이트 및 금결원API 호출 처리
     * 
     * 1. [오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용, 채널세부업무구분코드] 항목으로 요청
     * 2. 채널세부업무구분코드로 UBF오픈뱅킹계좌상세 테이블 및 UBF오픈뱅킹카드고객상세 사용여부 N으로 업데이트
     * 3. UBF오픈뱅킹계좌상세 테이블 및 UBF오픈뱅킹카드고객상세 테이블에 모든 채널에서 미사용이면 기본테이블 N으로 업데이트 
     * 4. UBF오픈뱅킹계좌기본 테이블 및 UBF오픈뱅킹카드고객기본 테이블에 모두 미사용이면 N으로 업데이트 
     * 5. 사용자탈퇴처리 금결원API 호출
     * 6. 관련테이블 전체 삭제
     * 
     * <관련 테이블>
     * UBF오픈뱅킹고객정보기본, UBF오픈뱅킹계좌기본, UBF오픈뱅킹계좌상세, UBF오픈뱅킹카드고객기본, UBF오픈뱅킹카드고객상세
     * <INPUT>
     * 오픈뱅킹사용자고유번호/고객식별자/준회원식별자/CI내용, 채널세부업무구분코드
     * <OUTPUT>
     * 결과(T/F)
     * 
     * @serviceId UBF2030105
     * @method prcUsrScsn
     * @method(한글명) 사용자탈퇴 처리
     * @param LData
     * @return LData
     * @throws LException 
     */ 
    public LData prcUsrScsn(LData input) throws LException {
    	LLog.debug.println("OpnbUsrMgPbc.prcUsrScsn[오픈뱅킹사용자관리Pbc.사용자탈퇴 처리] START ☆★☆☆★☆☆★☆" +  input);
        LData iPrcUsrScsnP = input; //i사용자탈퇴처리입력
        LData rPrcUsrScsnP = new LData(); //r사용자탈퇴처리결과
		String opnbUsrUno = ""; //오픈뱅킹사용자 고유번호
		String ciCtt = ""; //CI내용
		String chnDtlsBwkDtcd = "";
	       
    	//Validation Check
		if(StringUtils.isEmpty(iPrcUsrScsnP.getString("채널세부업무구분코드"))) { //채널세부업무구분코드
			throw new LBizException(ObsErrCode.ERR_9001.getCode(), new String[] {"채널세부업무구분코드"}, ObsErrCode.ERR_9001.getName());
		}
 		if(StringUtil.trimNisEmpty(iPrcUsrScsnP.getString("오픈뱅킹사용자고유번호")) && 
 				StringUtil.trimNisEmpty(iPrcUsrScsnP.getString("고객식별자")) && 
 				StringUtil.trimNisEmpty(iPrcUsrScsnP.getString("준회원식별자"))&&
 				StringUtil.trimNisEmpty(iPrcUsrScsnP.getString("CI내용"))) {
 			  throw new LBizException(ObsErrCode.ERR_9001.getCode(), ObsErrCode.ERR_9001.getName());
 		}
 		chnDtlsBwkDtcd = iPrcUsrScsnP.getString("채널세부업무구분코드");

        opnbUsrUno = iPrcUsrScsnP.getString("오픈뱅킹사용자고유번호"); //사용자일련번호
        if(StringUtil.trimNisEmpty(opnbUsrUno)) { //사용자고유번호가 없으면
	        //사용자고유번호조회
			OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
			LData iRetvUsrNo = iPrcUsrScsnP; 
			LData rRetvUsrNo = opnbCstMgCpbc.retvUsrUno(iRetvUsrNo);
			opnbUsrUno = rRetvUsrNo.getString("오픈뱅킹사용자고유번호");
        }
        
        try {
        	//CI내용조회
        	LData iSelectCiCtt = new LData(); 
    		LData rSelectCiCtt = new LData(); 
    		iSelectCiCtt.setString("오픈뱅킹사용자고유번호", opnbUsrUno);
    		rSelectCiCtt = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectCiCtt" , iSelectCiCtt);
            ciCtt =  rSelectCiCtt.getString("CI내용");
            
        }catch(LNotFoundException lnfe) {
        	lnfe.printStackTrace();
        	throw new LBizException(ObsErrCode.ERR_1002.getCode(), ObsErrCode.ERR_1002.getName());
        }
     
        try {
	        //계좌상세테이블 N 업데이트
	    	LData iUpdAccDtl = new LData();
	    	iUpdAccDtl.setString("채널세부업무구분코드", chnDtlsBwkDtcd); //채널세부업무구분코드
	    	iUpdAccDtl.setString("CI내용", ciCtt);
	    	iUpdAccDtl.setString("계좌상세사용여부", "N"); //계좌상세사용 N으로 UPDATE
	    	iUpdAccDtl.setString("출금동의여부", "N"); //계좌상세사용 N으로 UPDATE
	    	iUpdAccDtl.setString("시스템최종갱신식별자", chnDtlsBwkDtcd); //채널세부업무구분코드
	    	        
		    int rtnAccUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "updateAccDtlUseYn" , iUpdAccDtl);
		
	        //카드상세테이블 N 업데이트
	     	LData iUpdCrdDtl = new LData();
	     	iUpdCrdDtl.setString("채널세부업무구분코드", chnDtlsBwkDtcd); //채널세부업무구분코드
	     	iUpdCrdDtl.setString("CI내용", ciCtt);
	     	iUpdCrdDtl.setString("카드정보조회동의여부", "N"); // N으로 UPDATE
	     	iUpdCrdDtl.setString("시스템최종갱신식별자", chnDtlsBwkDtcd); //채널세부업무구분코드
	     	        
	 	    int rtnCrdUpd  = BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "updateCrdDtlUseYn" , iUpdCrdDtl);
	 	    
		    if(rtnAccUpd == 0 && rtnCrdUpd == 0) {		
		    	throw new LBizException(ObsErrCode.ERR_1004.getCode(), ObsErrCode.ERR_1004.getName());
		    }
		   
		    //채널별동의이력등록
		    LData iRegOpnbCstCnsPhsIn = new LData(); // 오픈뱅킹고객동의이력등록입력
		    
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹사용자고유번호"       , opnbUsrUno);
		    iRegOpnbCstCnsPhsIn.setString("채널세부업무구분코드"        , chnDtlsBwkDtcd);
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드"     , "3"); 
		    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의일시"        , DateUtil.getDateTimeStr());
			iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의연장구분코드"     , "3");//3.해지
		  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹동의자료구분코드"     , "6"); //동의자료구분코드  1:서면, 2:공인인증, 3: 일반인증, 4:녹취, 5:ARS, 6:기타
			iRegOpnbCstCnsPhsIn.setString("시스템최초생성식별자"        , chnDtlsBwkDtcd);
	  	  	iRegOpnbCstCnsPhsIn.setString("시스템최종갱신식별자"        , chnDtlsBwkDtcd);
	  	  	
	  	  	iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드", "3"); //통합계좌조회 (오픈뱅킹계좌서비스)
	  	  	BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "regOpnbCstCnsPhs", iRegOpnbCstCnsPhsIn);	
	  	  	
			
	  	    iRegOpnbCstCnsPhsIn.setString("오픈뱅킹약관동의구분코드", "4"); //카드정보조회 (오픈뱅킹카드서비스)
	  	  	BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.utzRg.OpnbUtzCnsMgEbc", "regOpnbCstCnsPhs", iRegOpnbCstCnsPhsIn);	
	  	  	
		    int rgAccCnt = 0;
	    	
	    	//오픈뱅킹상세 원장에 계좌에대해 채널구분없이 사용여부 N이 건이 몇건존재하는지 조회			         
	        LData iAlAccData = new LData();  
	        LData rAlAccData = new LData();
	        iAlAccData.setString("CI내용", ciCtt);
	        iAlAccData.setString("계좌상세사용여부", "Y");
	        
	        rAlAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectOpnbAccDtlRgAccnt" , iAlAccData);
	        rgAccCnt = rAlAccData.getInt("등록계좌수");
	
	        if(rgAccCnt == 0) { //등록계좌수가 0일경우
	        	
	        	//계좌기본테이블 N 업데이트
		    	LData iUpdOpnbAccBas = new LData();	
		    	iUpdOpnbAccBas.setString("오픈뱅킹사용자고유번호", opnbUsrUno);
		    	iUpdOpnbAccBas.setString("계좌사용여부", "N");	    
		    	iUpdOpnbAccBas.setString("계좌조회동의여부", "N");
		    	iUpdOpnbAccBas.setString("출금동의여부", "N");
		    	iUpdOpnbAccBas.setString("해제채널세부업무구분코드", chnDtlsBwkDtcd); //채널세부업무구분코드
		    	iUpdOpnbAccBas.setString("시스템최종갱신식별자", chnDtlsBwkDtcd); //채널세부업무구분코드
		    		    	
		    	BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "updateAccBasUseYn" , iUpdOpnbAccBas);
	    	
		    }
	
	 	    int rgCrdCnt = 0;
	     	
	     	//오픈뱅킹상세 원장에 카드대해 채널구분없이 사용여부 N이 건이 몇건존재하는지 조회			         
	         LData iAlCrdData = new LData();  
	         LData rAlCrdData = new LData();
	         iAlCrdData.setString("CI내용", ciCtt);
	         iAlCrdData.setString("카드정보조회동의여부", "Y");
	         
	         rAlCrdData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectOpnbCrdCstDtlRgCrdCstCnt" , iAlCrdData);
	         rgCrdCnt = rAlCrdData.getInt("등록카드고객수");
	
	         if(rgCrdCnt == 0) { //등록카드고객수가 0일경우
	         	
	         	//카드기본테이블 N 업데이트
	 	    	LData iUpdOpnbCrdBas = new LData();	
	 	    	iUpdOpnbCrdBas.setString("오픈뱅킹사용자고유번호", opnbUsrUno);
	 	    	iUpdOpnbCrdBas.setString("카드정보조회동의여부", "N");	    
	 	    	iUpdOpnbCrdBas.setString("해제채널세부업무구분코드",  chnDtlsBwkDtcd); //채널세부업무구분코드
	 	    	iUpdOpnbCrdBas.setString("시스템최종갱신식별자", chnDtlsBwkDtcd); //채널세부업무구분코드
	 	    		    	
	 	    	BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "updateCrdCstBasUseYn" , iUpdOpnbCrdBas);
	     	
	 		 		
	 	    }
        }catch(LException e) {
        	e.printStackTrace();
      	  	throw new LBizException(ObsErrCode.ERR_1003.getCode(), ObsErrCode.ERR_1003.getName());
        }
         
        //계좌기본테이블 및 카드테이블 조회하여 사용여부가 N인 건수를 조회 
    	LData iUseAccData = new LData();  
        LData rUseAccData = new LData();
        iUseAccData.setString("오픈뱅킹사용자고유번호", opnbUsrUno); //오픈뱅킹사용자고유번호  
        
        rUseAccData = (LData) BizCommand.execute("com.kbcard.ubf.ebi.opnb.opnbPuse.usrSvc.OpnbUsrMgEbc", "selectOpnbAccCrdUseAblNcn" , iUseAccData);
        int useAccCnt = rUseAccData.getInt("사용가능건수");
        
        if(useAccCnt == 0) { //사용계좌/카드고객가 존재하지 않는다면
        	
     	    //사용자탈퇴처리 금결원API 호출
    		LData iPrcUsrScsnAPICall = new LData(); //i사용자탈퇴처리API호출입력
            LData rPrcUsrScsnAPICall = new LData(); //r사용자탈퇴처리API호출결과
        	
        	iPrcUsrScsnAPICall.setString("client_use_code",  AuthInfo.UTZ_INS_CD.getCode());//이용기관코드
        	iPrcUsrScsnAPICall.setString("user_seq_no",  opnbUsrUno);//사용자일련번호
        	iPrcUsrScsnAPICall.setString("채널세부업무구분코드", chnDtlsBwkDtcd); //채널세부업무구분코드
        	
        	OpnbApiCpbc opnbApi = new OpnbApiCpbc();
            rPrcUsrScsnAPICall = opnbApi.prcUsrScsnAPICall(iPrcUsrScsnAPICall); //사용자탈퇴 API호출
            LLog.debug.println("OpnbApiCpbc.prcUsrScsnAPICall RESULT ☆★☆☆★☆☆★☆" + rPrcUsrScsnAPICall);
             
             
        	
	 		if("A0000".equals(rPrcUsrScsnAPICall.getString("rsp_code"))){ //정상
	 			try {
		        	LData iDelAccDtl = new LData();
		        	iDelAccDtl.setString("오픈뱅킹사용자고유번호", opnbUsrUno);
		        	iDelAccDtl.setString("CI내용", ciCtt);
			    
		        	OpnbCstMgCpbc opnbCstMgCpbc = new OpnbCstMgCpbc(); 
		        	opnbCstMgCpbc.delOpnbCstPsnInf(iDelAccDtl);
        	        	
	 			}catch(LException e) {
	 	        	e.printStackTrace();
	 	      	  	throw new LBizException(ObsErrCode.ERR_1003.getCode(), ObsErrCode.ERR_1003.getName());
	 	        }
	 		}else {
	 			throw new LBizException(rPrcUsrScsnAPICall.getString("rsp_code"), rPrcUsrScsnAPICall.getString("rsp_message"));//기관거래 처리중 오류
	 		}       	
	 		
	 	    /**응답셋팅 START **/
	 		rPrcUsrScsnP.setString("API거래고유번호_V40", rPrcUsrScsnAPICall.getString("api_tran_id")); //오픈뱅킹API거래고유번호
		    rPrcUsrScsnP.setString("API거래일시_N17", rPrcUsrScsnAPICall.getString("api_tran_dtm")); //API거래일시_N17
		    rPrcUsrScsnP.setString("API응답코드_V5", rPrcUsrScsnAPICall.getString("rsp_code")); //오픈뱅킹API응답구분코드
		    rPrcUsrScsnP.setString("API응답메시지_V300", rPrcUsrScsnAPICall.getString("rsp_message")); //오픈뱅킹API응답메시지내용
	        /**응답셋팅 END **/
        }  	
	    	
		LLog.debug.println("OpnbUsrMgPbc.prcUsrScsn[오픈뱅킹사용자관리Pbc.사용자탈퇴 처리] END ☆★☆☆★☆☆★☆" + rPrcUsrScsnP );
      
		return rPrcUsrScsnP;

    }

}

