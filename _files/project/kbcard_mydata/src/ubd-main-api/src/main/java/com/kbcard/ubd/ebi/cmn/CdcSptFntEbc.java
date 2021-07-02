package com.kbcard.ubd.ebi.cmn;

import devon.core.collection.LData;
import devon.core.exception.DevonException;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 프로그램명 	: 오픈뱅킹 CDC지원함수
 * 작성자 		: 임용택
 * 작성일자 		: 2021-04-07
 * 설      명 		: 오픈뱅킹 CDC지원함수 ebi
 */
public class CdcSptFntEbc {
	
	// 고객정보조회
	public LData selectCdcCstInf(LData iCdcCstInf) throws DevonException {
		LCommonDao dao = new LCommonDao("cmn/CdcSptFntEbc/selectCdcCstInf",iCdcCstInf);
		return dao.executeQueryOnlySingle();
	}
	
	// 카드식별자조회
	public LData selectCdcCrdIdf(LData iCdcCrdIdf) throws DevonException {
		LCommonDao dao = new LCommonDao("cmn/CdcSptFntEbc/selectCdcCrdIdf",iCdcCrdIdf);
		return dao.executeQueryForSingle();
	}
	
	// 카드목록존재여부
	public LData selectCdcCrdLstExstYnInf(LData iCdcCrdIdf) throws DevonException {
		LCommonDao dao = new LCommonDao("cmn/CdcSptFntEbc/chkCdcCrdLstExstYn",iCdcCrdIdf);
		return dao.executeQueryForSingle();
	}

	// 오픈뱅킹이용기관검증
	public LData verifyOpnbUtzIns(LData iCdcCrdIdf) throws DevonException {
		LCommonDao dao = new LCommonDao("ins/insBas/selectOpnbApiInsVln", iCdcCrdIdf);
		return dao.executeQueryOnlySingle();
	}
}
