package com.kbcard.ubf.cpbi.cmn;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class HttpClient {
	private static PoolingHttpClientConnectionManager cm = null;
	
	public static synchronized CloseableHttpClient getHttpClient() {
		if(cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(50);
			cm.setDefaultMaxPerRoute(20);
		}
		
		CloseableHttpClient httpClient = HttpClients.createMinimal(cm);		
		return httpClient;
	}

	public static void abort(HttpRequestBase httpRequest) {
		if(httpRequest != null) {
			httpRequest.abort();
		}
	}
	
	public static void release(HttpResponse response) {
		if(response != null && response.getEntity() != null) {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}
}
