package com.kbcard.frw.pbi;

import java.util.Map;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.context.ContextHandler;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.ext.core.context.ExtensionContextKey.HEADER_SPEC;
import devonenterprise.ext.core.context.ExtensionKeyDefs.InfContextKey;

public class OutboundTestPbc {
	@SuppressWarnings("unchecked")
	public LData sendOutboundRequest(LData input) throws LException {
		LData result = new LData();
		try {
			LinkHttpAdaptor httpClient = new LinkHttpAdaptor();
			result = httpClient.sendOutboundMessage(TargetTypeConst.EAI, "UBE_3KFTCS00003", input);
		} catch (Exception e) {
			e.printStackTrace(LLog.err);
			Map<HEADER_SPEC,LData> headerMap = (Map<HEADER_SPEC,LData>) ContextHandler.getContextObject(InfContextKey.INTERFACE_RESPONSE_HEADER);
			for(HEADER_SPEC spec : headerMap.keySet()) {
				LLog.debug.println("========" + spec.name() + "========");
				LLog.debug.println(headerMap.get(spec));
				LLog.debug.println("========" + spec.name() + "========");
			}
		
		}
		
		return result;
	}
}
