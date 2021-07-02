package com.kbcard.ubd.ebi.cmn;

import devon.core.collection.LData;
import devon.core.exception.DevonException;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 프로그램명 	: 오픈뱅킹 모듈지원함수
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-07
 * 설      명 	: 마이데이터 모듈지원함수 ebi
 */
public class MdulSptFntEbc {
	
	/**
	 * 응답코드매핑조회
	 * 처리계 오류메시지를 마이데이터 응답코드로 변환하여 반환한다.
	 * 
	 * @param iCdcCstInf
	 * 				오픈API언어구분코드 = KOR
	 * 				오픈API업무구분코드 = UBD
	 * 				언어구분코드     	= KOR
	 * 				메시지채널구분코드 	= 01(단말)
	 * 				오류메시지코드
	 * @return 
	 * 				오픈API응답코드
     * 				오픈API응답메시지내용
	 * @author 2021.04.13 / 임용택
	 */
	public LData selectMydtApiRspCdMapping(LData iCdcCstInf) throws DevonException {
		LCommonDao dao = new LCommonDao("cmn/MdulSptFntEbc/selectMydtApiRspCdMapping",iCdcCstInf);
		return dao.executeQueryForSingle();
	}
	
	
}
