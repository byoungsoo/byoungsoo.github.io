package com.kbcard.frw.sample.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devon.core.util.converter.LJsonMessageConverter;

public class HttpAsyncCaller implements Callable<LData>{
		
	private String targetUrl;
	private LData  inBodyLData;
	
	public HttpAsyncCaller(String targetUrl, LData inBodyLData) {
		this.targetUrl = targetUrl;
		this.inBodyLData = inBodyLData;
	}
	

	@Override
	public LData call() throws Exception {
		LLog.debug.println("HttpAsyncCaller [" + Thread.currentThread().getName() + "]-[" + Thread.currentThread().getId() +"] start");
		
		LData result = process(targetUrl, inBodyLData);
		
		LLog.debug.println("HttpAsyncCaller [" + Thread.currentThread().getName() + "]-[" + Thread.currentThread().getId() +"] end \n" + result);
		
		return result; 
	}
		
	private LData process(String targetUrl, LData body) throws LException {
		LData result = new LData();
		HttpURLConnection conn = null;
		String jsonMessage = "";
		int connectTimeout = 15000;
		int readTimeout = 15000;
		
		try {
			conn = (HttpURLConnection) new URL(targetUrl).openConnection();
			
//			// method 설정
			conn.setRequestMethod("GET");
//			// timeout 설정
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
//
//			// http header 설정
//			conn.setRequestProperty("Content-Type", this.contentType);
			conn.setRequestProperty("Accept", "application/json");

			// Connection set
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			LLog.debug.println("RestHttpAdaptor::process >> targetUri : [" + this.targetUrl.toString() + "]");
			
			int responseCode = conn.getResponseCode();
			BufferedReader br;
			String inputLine;
			StringBuilder response = new StringBuilder();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
			
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			
			LLog.debug.println("response.toString() : " + response);
			
			if(LNullUtils.notNone(response.toString())) {
				result = LJsonMessageConverter.convertToLData(response.toString());
			}

			result.setInt("response_code", responseCode);
			
			LLog.debug.println("resultLData : " + result);
			br.close();				
		} catch (MalformedURLException mfue) {
			throw new LException("부정확한 url입니다." ,mfue);
		} catch (Exception e) {
			throw new LException("PER_LNKP_080",e);
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
		}
		return result;
	}
}
