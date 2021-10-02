/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * 
 * it is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
public class ReqUtil {

	/**********************************************************
	 * 
	 *                     Commons
	 * 
	 **********************************************************/
	
	/**
	 * @param req                      request
	 * @param token                    token
	 * @param body                     body
	 * @return                         request
	 */
	static HttpRequestBase createRequest(HttpRequestBase req, String token, String body) {
		if (req instanceof HttpEntityEnclosingRequestBase) {
			setHttpEntity((HttpEntityEnclosingRequestBase) req, body);
		}
		setBearerHeader(req, token);
		return req;
	}
	
	/**
	 * @param req                        request
	 * @param body                       body
	 */
	static void setHttpEntity(HttpEntityEnclosingRequestBase req, String body) {
		req.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
	}
	
	/**
	 * @param request                     request
	 * @param token                       token
	 */
	static void setBearerHeader(HttpRequestBase request, String token) {
		if (token != null) {
			request.addHeader("Authorization", "Bearer " + token);
		}
		request.addHeader("Connection", "keep-alive");
	}
	
	/**********************************************************
	 * 
	 *                     Core
	 * 
	 **********************************************************/
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @param body                        body
	 * @return                            request
	 */
	public static HttpPost post(String token, String uri, String body) {
		return (HttpPost) createRequest(new HttpPost(uri), token, body);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @param body                        body
	 * @return                            request
	 */
	public static HttpPut put(String token, String uri, String body) {
		return (HttpPut) createRequest(new HttpPut(uri), token, body);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request
	 */
	public static HttpDelete delete(String token, String uri) {
		return (HttpDelete) createRequest(new HttpDelete(uri), token, null);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request
	 */
	public static HttpGet get(String token, String uri) {
		return (HttpGet) createRequest(new HttpGet(uri), token, null);
	}
	
}
