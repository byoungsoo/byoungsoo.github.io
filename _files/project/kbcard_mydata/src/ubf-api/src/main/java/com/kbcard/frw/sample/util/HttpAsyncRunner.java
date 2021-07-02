package com.kbcard.frw.sample.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.service.thread.ThreadPoolManager;

public class HttpAsyncRunner {
	public static LData execute(String targetUrl, LData inBodyLData) throws LException {
		int timeout = 20;
		
		HttpAsyncCaller asyncCaller = new HttpAsyncCaller(targetUrl, inBodyLData);
		Future<LData> future = ThreadPoolManager.getInstance().execute("asyncAdaptor", asyncCaller);
		
		LData result = new LData();
		try {
			if(timeout > 0) {
				result = future.get(timeout + 5000, TimeUnit.MILLISECONDS);
			} else {
				result = future.get();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(LLog.err);
			// new LBizException
		}
		
		/*
		// 온라인 수행 결과에 따라 정상 리턴 또는 Exception을 발생 시킨다.
		if (result instanceof LData) {
			return (LData) result;
		} else if (result instanceof EnterpriseException) {
			throw (EnterpriseException) result;
		} else if (result instanceof DevonException) {
			throw EnterpriseExceptionPitcher.createEnterpriseException((DevonException) result);
		} else if (result instanceof Throwable) {
			// 서비스(@) 호출 중 오류가 발생하였습니다.
			throw EnterpriseExceptionPitcher.createEnterpriseException("SVC_ONLC_001", new String[] { tranId },
					this.getClass(), "execute(LData param)", (Throwable) result);
		} else {
			return null;
		}
		*/
		return result;
	}
	
}
