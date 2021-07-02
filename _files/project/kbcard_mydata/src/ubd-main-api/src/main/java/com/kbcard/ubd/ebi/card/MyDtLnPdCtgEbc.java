package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.exception.DevonException;
import devonframework.persistent.autodao.LCommonDao;

public class MyDtLnPdCtgEbc {
	
	/** 대출상품목록조회 */
	public LData selectLnPdCtg(LData iDao) throws DevonException {
		LCommonDao rDao = new LCommonDao("card/MyDtLnPdCtgEbc/selectLnPdCtg", iDao);
		return rDao.executeQueryForSingle();
	}

}
