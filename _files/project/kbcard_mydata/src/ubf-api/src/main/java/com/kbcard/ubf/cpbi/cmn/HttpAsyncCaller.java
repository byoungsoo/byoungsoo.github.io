package com.kbcard.ubf.cpbi.cmn;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.AuthInfo;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devon.core.util.converter.LJsonMessageConverter;

public class HttpAsyncCaller implements Callable<LData>{
	private LData urlInfo;
	private LData httpHeader;
	private LData httpBody;
	private CloseableHttpClient httpClient = HttpClient.getHttpClient();
	
	public HttpAsyncCaller(LData urlInfo, LData header, LData body) {
		this.urlInfo = urlInfo;
		this.httpHeader = header;
		this.httpBody = body;
	}

	@Override
	public LData call() throws Exception {
		LLog.debug.println("HttpAsyncCaller [" + Thread.currentThread().getName() + "]-[" + Thread.currentThread().getId() +"] start");
		
		LData result = httpCall(httpHeader, httpBody);
		
		LLog.debug.println("HttpAsyncCaller [" + Thread.currentThread().getName() + "]-[" + Thread.currentThread().getId() +"] end \n" + result);
		
		return result; 
	}
	
	private LData httpCall(LData header, LData body) throws LException {
		LData result = new LData();
		String httpMethod = this.urlInfo.getString("오픈API타입구분");
		String contentType;
		String targetUrl = null;
		HttpGet httpGet = null;
		HttpPost httpPost = null;
		HttpResponse response = null;

		try {
			
			if("Y".equals(this.urlInfo.getString("오픈뱅킹API콘텐츠JSON유형"))) {
				contentType = AuthInfo.URL_CONTENT_TYPE_JSON.getCode();
				
				if("GET".equals(httpMethod)) {
					if(!LNullUtils.isNone(body)) {
						targetUrl = this.setTargetUrl(body);
					} else {
						targetUrl = this.urlInfo.getString("오픈뱅킹URL내용");
					}
				} else {
					targetUrl = this.urlInfo.getString("오픈뱅킹URL내용");
				}
			} else {
				contentType = AuthInfo.URL_CONTENT_TYPE_FORM.getCode();
				targetUrl = this.setTargetUrl(header);
				body = new LData();
			}

			if("GET".equals(httpMethod)) {
				httpGet = new HttpGet(targetUrl);
				httpGet.setHeader("Content-Type", contentType);
				httpGet.setHeader("Cache-Control", "no-cache");
				httpGet.setHeader("Accept", "application/json");

				if(AuthInfo.URL_CONTENT_TYPE_JSON.getCode().equals(contentType)) {
					// headerParam에 있는 값으로 http header 설정
					for(Object key : header.getKeys()) {
						httpGet.setHeader((String) key, header.getString(key));
					}
				} else {
					httpGet.setHeader("chn_dtls_bwk_dtcd", header.getString("chn_dtls_bwk_dtcd"));
				}

				response = this.httpClient.execute(httpGet);	
			} else if("POST".equals(httpMethod)) {
				httpPost = new HttpPost(targetUrl);
				httpPost.setHeader("Content-Type", contentType);
				httpPost.setHeader("Cache-Control", "no-cache");
				httpPost.setHeader("Accept", "application/json");
				
				if(AuthInfo.URL_CONTENT_TYPE_JSON.getCode().equals(contentType)) {
					// headerParam에 있는 값으로 http header 설정
					for(Object key : header.getKeys()) {
						httpPost.setHeader((String) key, header.getString(key));
					}
				} else {
					httpPost.setHeader("chn_dtls_bwk_dtcd", header.getString("chn_dtls_bwk_dtcd"));
				}

				httpPost.setEntity(new StringEntity(LJsonMessageConverter.convertToMessage(body)));
				
				response = this.httpClient.execute(httpPost);
			}
			
			int responseCode = response.getStatusLine().getStatusCode();
			String responseTxt = EntityUtils.toString(response.getEntity());
			
			if(LNullUtils.notNone(response.toString())) {
				result.set("response", LJsonMessageConverter.convertToLData(responseTxt));
			}
			
			result.setInt("response_code", responseCode);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			if("GET".equals(httpMethod)) {
				HttpClient.abort(httpGet);	
			} else {
				HttpClient.abort(httpPost);	
			}
		} catch (IOException e) {
			e.printStackTrace();			
			if("GET".equals(httpMethod)) {
				HttpClient.abort(httpGet);	
			} else {
				HttpClient.abort(httpPost);	
			}
		} finally {
			HttpClient.release(response);
		}

		return result;
	}
	
	private String setTargetUrl(LData header) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		
		for(Object key : header.getKeys()) {
			if(first) {
				first = false;
				result.append(this.urlInfo.getString("오픈뱅킹URL내용"));
				result.append("?");
			}
			
			if(!key.toString().equals("chn_dtls_bwk_dtcd")
				&& !key.toString().equals("ci_ctt")) {
				result.append("&");
				result.append((String) key);
				result.append("=");
				result.append(header.getString(key));
			}
		}			
		
		return result.toString();
	}
}
