package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 프로그램명 	: 마이데이터 API 제공 단기대출정보조회 query
 * 작성자 		: 김형구
 * 작성일자 	: 2021-04-07
 * 설      명 	: 마이데이터 API 제공 단기대출정보조회 ebi
 */
public class MyDtShtmLnInfEbc {
	
	// 단기대출정보조회
	public LMultiData selectMyDtShtmLnInfForPaging(LData iShtmLnInf) throws LException {
		LCommonDao dao = new LCommonDao("card/MyDtShtmLnInfEbc/selectMyDtApiShtmLnInf", iShtmLnInf);
		return dao.executeQueryForPage();
	}
	
}
