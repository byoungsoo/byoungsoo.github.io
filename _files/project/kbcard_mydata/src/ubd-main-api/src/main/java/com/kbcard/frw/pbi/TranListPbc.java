package com.kbcard.frw.pbi;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.persistent.page.PageConstants;

public class TranListPbc {
	public LData retrieveTranList(LData input) throws LException {
		LLog.debug.println("retrieveTranList input : " + input );
		
		
		LMultiData tranList = new LMultiData();
		tranList = (LMultiData) BizCommand.execute("com.kbcard.frw.ebi.TranListEbc", "retrieveTranList", input); 
		
		LData result = new LData();
		result.set("거래목록_cnt", tranList.getDataCount());
		result.set("거래목록",tranList);
		return result;
	}
	
	public LData retrieveTranListForPaging(LData input) throws LException {
		input.setString(PageConstants.NEXT_INQ_KY, input.getString(PageConstants.NEXT_INQ_KY));
		input.setString(PageConstants.PGE_SIZE, input.getString(PageConstants.PGE_SIZE));
		
		
		LMultiData tranList = new LMultiData();
		tranList = (LMultiData) BizCommand.execute("com.kbcard.frw.ebi.TranListEbc", "retrieveTranListForPaging", input); 
		
		LData result = new LData();
		result.set("tranList",tranList);
		return result;
	}
}
