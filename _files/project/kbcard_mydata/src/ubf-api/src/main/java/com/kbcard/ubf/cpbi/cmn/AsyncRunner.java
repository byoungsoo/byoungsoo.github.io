package com.kbcard.ubf.cpbi.cmn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;

public class AsyncRunner {
	static int timeout = 35000;
	
	public static LData httpAsyncExecute(LData urlInfo, LData header, LData body) throws LException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();		
		HttpAsyncCaller asyncCaller = new HttpAsyncCaller(urlInfo, header, body);		
		//Future<LData> future = ThreadPoolManager.getInstance().execute("asyncAdaptor", asyncCaller);
		Future<LData> future = executorService.submit(asyncCaller);
		
		LData result = new LData();

		try {
			if(timeout > 0) {
				result = future.get(timeout, TimeUnit.MILLISECONDS);
			} else {
				result = future.get();
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace(LLog.err);
			if(Thread.currentThread().isInterrupted()) {
				Thread.currentThread().interrupt();
			}
			// throw new LBizException(ObsErrCode.ERR_9408.getCode(), ObsErrCode.ERR_9408.getName());
		} catch (ExecutionException ee) {
			ee.printStackTrace(LLog.err);
			throw new LBizException(ObsErrCode.ERR_9407.getCode(), ObsErrCode.ERR_9407.getName());
		} catch (TimeoutException te) {
			te.printStackTrace(LLog.err);
			throw new LBizException(ObsErrCode.ERR_9408.getCode(), ObsErrCode.ERR_9408.getName());
		} finally {
			executorService.shutdown();
		}
		
		
		return result;
	}
	
	public static LMultiData httpAsyncExecute(LMultiData apiCalls) throws LException {		
		ExecutorService executorService = Executors.newFixedThreadPool(apiCalls.getDataCount());
		//Future<LData> future = ThreadPoolManager.getInstance().execute("asyncAdaptor", asyncCaller);
		List<HttpAsyncCaller> apiCallList = new ArrayList<HttpAsyncCaller>();
		LMultiData result = new LMultiData();		
		
        if(!LNullUtils.isNone(apiCalls) ){
            for(int idx=0; idx < apiCalls.getDataCount(); idx++){
                LData apiCall = apiCalls.getLData(idx);
                apiCallList.add(new HttpAsyncCaller(apiCall.getLData("apiInf"), apiCall.getLData("apiHeader"), apiCall.getLData("apiBody")));
            }		
        }

		try {			
			List<Future<LData>> futureList =  executorService.invokeAll((Collection<? extends Callable<LData>>) apiCallList);
			
			for(Future<LData> future : futureList) {
				if(timeout > 0) {
					result.addLData(future.get(timeout, TimeUnit.MILLISECONDS));
				} else {
					result.addLData(future.get());
				}
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace(LLog.err);
			if(Thread.currentThread().isInterrupted()) {
				Thread.currentThread().interrupt();
			}
			// throw new LBizException(ObsErrCode.ERR_9408.getCode(), ObsErrCode.ERR_9408.getName());
		} catch (ExecutionException ee) {
			ee.printStackTrace(LLog.err);
			throw new LBizException(ObsErrCode.ERR_9407.getCode(), ObsErrCode.ERR_9407.getName());
		} catch (TimeoutException te) {
			te.printStackTrace(LLog.err);
			throw new LBizException(ObsErrCode.ERR_9408.getCode(), ObsErrCode.ERR_9408.getName());
		} finally {
			executorService.shutdown();
		}
		
		return result;
	}
}