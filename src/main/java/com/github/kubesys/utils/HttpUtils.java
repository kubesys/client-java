/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.utils;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
 * 
 **/
public class HttpUtils {


	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @param body                        body
	 * @return                            request
	 */
	public static HttpPost post(String token, String uri, String body) {
		HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
		setBearerHeader(request, token);
		return request;
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @param body                        body
	 * @return                            request
	 */
	public static HttpPut put(String token, String uri, String body) {
		HttpPut request = new HttpPut(uri);
		request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
		setBearerHeader(request, token);
		return request;
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request
	 */
	public static HttpDelete delete(String token, String uri) {
		HttpDelete request = new HttpDelete(uri);
		setBearerHeader(request, token);
		return request;
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request
	 */
	public static HttpGet get(String token, String uri) {
		HttpGet request = new HttpGet(uri);
		setBearerHeader(request, token);
		return request;
	}
	
	/**
	 * @param request                     request
	 * @param token                       token
	 */
	public static void setBearerHeader(HttpRequestBase request, String token) {
		if (token != null) {
			request.addHeader("Authorization", "Bearer " + token);
		}
		
		request.addHeader("Connection", "keep-alive");
	}
}
