package com.kbcard.frw.ebi;

import com.kbcard.ubd.constants.DatasourceConst.DSCODE;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devon.core.log.LLog;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;

public class TranListEbc {
	public LMultiData retrieveTranList(LData input) throws DevonException {
		LLog.debug.println("base    : " + DSCODE.BASE.getCode());
		LLog.debug.println("default : " + DSCODE.DEFAULT.getCode());
		LLog.debug.println("appr    : " + DSCODE.APPR.getCode());
		LLog.debug.println("bill    : " + DSCODE.BILL.getCode());
		
		LCommonDao dao = new LCommonDao("frw/tranList/retrieveTranList", input, DSCODE.DEFAULT.getCode());
		return dao.executeQuery();
	}

	public LMultiData retrieveTranListForPaging(LData input)
			throws DevonException {
		LPagingCommonDao dao = new LPagingCommonDao(
				"frw/tranList/retrieveTranList", input);
		return dao.executeQueryForScrollPage();
	}

}
