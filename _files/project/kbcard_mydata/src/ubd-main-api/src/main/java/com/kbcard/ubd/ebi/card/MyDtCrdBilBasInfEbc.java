package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;

/**
 * 프로그램명 	: 마이데이터 API 제공 청구기본정보조회 query
 * 작성자 		: 임용택
 * 작성일자 	: 2021-04-07
 * 설      명 	: 마이데이터 API 제공 청구기본정보조회 ebi
 */
public class MyDtCrdBilBasInfEbc {
	
	// 청구기본정보조회 paging
	public LMultiData selectMyDtCrdBilBasInfForPaging(LData iCrdBilBasInf) throws LException {
		LPagingCommonDao dao = new LPagingCommonDao("card/MyDtCrdBilBasInfEbc/selectMyDtApiCrdBilBasInf", iCrdBilBasInf);
		return dao.executeQueryForScrollPage();
	}
	
	// 청구추가정보조회 paging
	public LMultiData selectMyDtCrdBilDtlInfForPaging(LData iCrdBilDtlInf) throws LException {
		LPagingCommonDao dao = new LPagingCommonDao("card/MyDtCrdBilBasInfEbc/selectMyDtApiCrdBilDtlInf", iCrdBilDtlInf);
		return dao.executeQueryForScrollPage();
	}
	
	
}
