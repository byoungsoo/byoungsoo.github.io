package com.kbcard.frw.ebi;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

public class TranListEbc {
	public LMultiData retrieveTranList(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("frw/tranList/retrieveTranList",input);
		return dao.executeQuery();
	}
	
	public LMultiData retrieveTranListForPaging(LData input) throws DevonException {
		LPagingCommonDao dao = new LPagingCommonDao("frw/tranList/retrieveTranList",input);
		return dao.executeQueryForScrollPage();
	}
	
}
