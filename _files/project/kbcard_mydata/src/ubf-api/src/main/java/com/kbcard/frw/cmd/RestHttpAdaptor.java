package com.kbcard.frw.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.kbcard.ubf.cpbi.cmn.UBF_CONST.AuthInfo;
import com.kbcard.ubf.cpbi.cmn.UBF_CONST.ObsErrCode;

import devon.core.collection.LData;
import devon.core.exception.LBizException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LNullUtils;
import devon.core.util.converter.LJsonMessageConverter;

public class RestHttpAdaptor {
		private URL targetUrl;
		private String httpMethod;
		private String contentType = AuthInfo.URL_CONTENT_TYPE_JSON.getCode();
		
		public RestHttpAdaptor(LData urlInfo) throws LBizException {			
			try {
				this.targetUrl = new URL(urlInfo.getString("오픈뱅킹URL내용"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9001.getCode(), "targetUrl");
			}
			
			this.httpMethod = urlInfo.getString("오픈API타입구분");
			
			if("Y".equals(urlInfo.getString("오픈뱅킹API콘텐츠JSON유형"))) {
				this.contentType = AuthInfo.URL_CONTENT_TYPE_JSON.getCode();
			} else {
				this.contentType = AuthInfo.URL_CONTENT_TYPE_FORM.getCode();
			}
		}
		
		public RestHttpAdaptor(String httpMethod, String targetUrl) throws LBizException {
			try {
				this.targetUrl = new URL(targetUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9001.getCode(), "targetUrl");
			}
			
			this.httpMethod = httpMethod;
		}
		
		public RestHttpAdaptor(String httpMethod, URL targetUrl) {
			this.targetUrl = targetUrl;
			this.httpMethod = httpMethod;
		}
		
		public RestHttpAdaptor(String httpMethod, String targetUrl, String contentType) throws LBizException {
			try {
				this.targetUrl = new URL(targetUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new LBizException(ObsErrCode.ERR_9001.getCode(), "targetUrl");
			}
			
			this.httpMethod = httpMethod;
			this.contentType = contentType;
		}
		
		public RestHttpAdaptor(String httpMethod, URL targetUrl, String contentType) {
			this.targetUrl = targetUrl;
			this.httpMethod = httpMethod;
			this.contentType = contentType;
		}
		
		public LData sendMessage(LData headerParam, LData message) throws LException {
			return process(headerParam, message);
		}
		
		private String setTargetUrl(LData headerParam) {
			StringBuilder result = new StringBuilder();
			boolean first = true;
			
			for(Object key : headerParam.getKeys()) {
				if(first) {
					first = false;
					result.append(this.targetUrl.toString());
					result.append("?");
				}
				
				if(!key.toString().equals("chn_dtls_bwk_dtcd")
					&& !key.toString().equals("ci_ctt")) {
					result.append("&");
					result.append((String) key);
					result.append("=");
					result.append(headerParam.getString(key));
				}
			}			
			
			return result.toString();
		}
		
		private LData process(LData header, LData body) throws LException {
			LData result = new LData();
			HttpURLConnection conn = null;
			String jsonMessage = "";
			int connectTimeout = 15000;
			int readTimeout = 15000;
			
			try {
				if(AuthInfo.URL_CONTENT_TYPE_FORM.getCode().equals(this.contentType)) {
					this.targetUrl = new URL(this.setTargetUrl(header));
					body = new LData();
				}
				
				if("GET".equals(this.httpMethod)) {
					if(!LNullUtils.isNone(body)) {
						this.targetUrl = new URL(this.setTargetUrl(body));
					}
				}				
				
				// HTTP Post Request		
				conn = (HttpURLConnection) targetUrl.openConnection();
				// method 설정
				conn.setRequestMethod(this.httpMethod);
				// timeout 설정
	            conn.setConnectTimeout(connectTimeout);
	            conn.setReadTimeout(readTimeout);

				// http header 설정
				conn.setRequestProperty("Content-Type", this.contentType);
				conn.setRequestProperty("Accept", "application/json");

				// Connection set
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				
				if(AuthInfo.URL_CONTENT_TYPE_JSON.getCode().equals(this.contentType)) {
					// headerParam에 있는 값으로 http header 설정
					for(Object key : header.getKeys()) {
						conn.setRequestProperty((String) key, header.getString(key));
					}
				} else {
					conn.setRequestProperty("chn_dtls_bwk_dtcd", header.getString("chn_dtls_bwk_dtcd"));
				}
				
				LLog.debug.println("RestHttpAdaptor::process >> targetUri : [" + this.targetUrl.toString() + "]");
				LLog.debug.println("RestHttpAdaptor::process >> header : \n" + conn.getRequestProperties());
				LLog.debug.println("RestHttpAdaptor::process >> body : \n" + body);
				LLog.debug.println("LinkHttpAdaptor::process >> jsonMessage : [" + jsonMessage + "]");
				
				if(!"GET".equals(this.httpMethod)) {
					OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
					PrintWriter writer = new PrintWriter(out);
					writer.write(LJsonMessageConverter.convertToMessage(body));
					writer.flush();
					writer.close();
				}
				
				int responseCode = conn.getResponseCode();
				BufferedReader br;
				String inputLine;
				StringBuilder response = new StringBuilder();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					LLog.debug.println("response : " + response.toString());
				} else {
					br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				}
				
				while ((inputLine = br.readLine()) != null) {
					response.append(inputLine);
				}

				result = LJsonMessageConverter.convertToLData(response.toString());
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
