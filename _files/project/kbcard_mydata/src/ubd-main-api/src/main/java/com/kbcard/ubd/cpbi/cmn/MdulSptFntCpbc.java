package com.kbcard.ubd.cpbi.cmn;

import com.kbcard.ubd.cpbi.cmn.UBD_CONST;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.util.StringUtil;

/*
 * 프로그램명 	: 마이데이터 API 모듈지원함수
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-03
 * 설      명 	: 마이데이터 API 모듈지원함수 cpbi
 */
public class MdulSptFntCpbc {

	public static String DEFAULT_CHARSET = "EUC-KR";
	private LData rRtnErr;

	//
	/**
	 * 응답코드 매핑조회 오픈API응답코드와 처리계응답코드간 매핑된 응답코드를 반환한다.
	 * 
	 * 테이블명 : UBD오픈API매핑응답코드 ( TBUBDS902 )
	 * 
	 * @param LData iErrCode
	 *              오픈API언어구분코드 = KOR
	 *              오픈API업무구분코드 = UBE 
	 *              언어구분코드 		= KOR
	 *              메시지채널구분코드	= 01 (단말)
	 *              오류메시지코드 		=
	 *              오류메시지출력내용 	=
	 *              처리계호출방식      = CDC, MCI, EAI
	 * @return LData rRtnErr 오픈API응답코드/응답내용
	 * @author 2021.04.13 / 임용택  
	 * 
	 * @변경 내역
	 *  	20210526 MS05722 장민석 
	 *  		에러메세지 조회가 안되는경우 입력 iErrCode 오류메시지출력내용 에 값이 존재하면 (오류코드) - 오류메세지 
	 *  																			  아니라면 ( 시스템 ) - 기타처리불가 로 세팅처리
	 */
	public LData retvRspCdMapping(LData iErrCode) throws LException {
		rRtnErr = new LData();
		String sInsMsg = "";
		
		rRtnErr = (LData) BizCommand.execute("com.kbcard.ubd.ebi.cmn.MdulSptFntEbc", "selectMydtApiRspCdMapping", iErrCode);
		
		
		//에러 메세지 조회가 안될때
		if (rRtnErr.isEmpty() ) {
			//응답메세지가 존재하지 않는경우
			if( StringUtil.trimNisEmpty( iErrCode.getString("오류메시지출력내용") ) ) {
				if(iErrCode.getString("처리계호출방식").equals("CDC")) {
					rRtnErr.setString("오픈API응답코드"			, UBD_CONST.REP_CD_SERVER_ERR_50002			); // 응답코드(50002)
					rRtnErr.setString("오픈API응답메시지내용"	, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002		); // 응답내용 : API 요청 처리 실패
				} else if(iErrCode.getString("처리계호출방식").equals("MCI")) {
					rRtnErr.setString("오픈API응답코드"			, UBD_CONST.REP_CD_SERVER_ERR_50002			); // 응답코드(50002)
					rRtnErr.setString("오픈API응답메시지내용"	, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002		); // 응답내용 : API 요청 처리 실패
				} else if(iErrCode.getString("처리계호출방식").equals("EAI")) {
					rRtnErr.setString("오픈API응답코드"			, UBD_CONST.REP_CD_SERVER_ERR_50002			); // 응답코드(50002)
					rRtnErr.setString("오픈API응답메시지내용"	, UBD_CONST.REP_CD_MSG_SERVER_ERR_50002		); // 응답내용 : API 요청 처리 실패
				}
			}
			else{
				sInsMsg = StringUtil.mergeStr("(",iErrCode.getString("오류메시지코드"),")" , "-" , iErrCode.getString("오류메시지출력내용") );
				rRtnErr.setString("오픈API응답코드"			, UBD_CONST.REP_CD_SERVER_ERR_50005		); // 응답코드(50005)
				rRtnErr.setString("오픈API응답메시지내용"	, sInsMsg								);
			}
		}
		
		return rRtnErr;
	}

	

}
