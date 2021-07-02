package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;
/**
 * 프로그램명 		: 마이데이터 카드목록조회 query
 * 작성자 			: 박재성
 * 작성일자 		: 2021-05-31 
 * 설      명 		: 마이데이터 카드목록조회 Ebc 
 */
 
public class MyDtCrdCtgEbc {

	/** 카드목록조회 **/
	public LMultiData retvLstMyDtApiCrdForPaging(LData input) throws DevonException {
		LPagingCommonDao dao = new LPagingCommonDao("card/MyDtCrdCtgEbc/selectLstApiCrd", input);
		return dao.executeQueryForScrollPage();
	}
	
	/** 회원기본중요변경내역최종내역조회 **/
	public LData retvMbrBasImpaMdHisLstHis(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdCtgEbc/selectMbrBasImpaMdHisInf", input);
		return dao.executeQueryForSingle();
	}

	/** 전송요구여부조회 **/
	public LData retvTrsRqstYn(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdCtgEbc/selectTrsRqstYnInf", input);
		return dao.executeQueryForSingle();
	}
}

