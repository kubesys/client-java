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
 * It is used for creating various HttpRequest
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
	private static HttpRequestBase createRequest(HttpRequestBase req, String token, String body) {
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
	private static void setHttpEntity(HttpEntityEnclosingRequestBase req, String body) {
		req.setEntity(new StringEntity(
					body == null ? "" : body, 
					ContentType.APPLICATION_JSON));
	}
	
	/**
	 * @param request                     request
	 * @param token                       token
	 */
	private static void setBearerHeader(HttpRequestBase request, String token) {
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
	 * @return                            request or null
	 * @throws NullPointerException       null
	 * @throws RuntimeException           runtime
	 */
	public static HttpPost post(String token, String uri, String body) throws NullPointerException, RuntimeException {
		return (HttpPost) createRequest(new HttpPost(uri), token, body);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @param body                        body
	 * @return                            request or null
	 * @throws NullPointerException       null
	 * @throws RuntimeException           runtime
	 */
	public static HttpPut put(String token, String uri, String body) throws NullPointerException, RuntimeException {
		return (HttpPut) createRequest(new HttpPut(uri), token, body);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request or null
	 * @throws NullPointerException       null
	 * @throws RuntimeException           runtime
	 */
	public static HttpDelete delete(String token, String uri) throws NullPointerException, RuntimeException {
		return (HttpDelete) createRequest(new HttpDelete(uri), token, null);
	}
	
	/**
	 * @param token                       token
	 * @param uri                         uri
	 * @return                            request or null
	 * @throws NullPointerException       null
	 * @throws RuntimeException           runtime
	 */
	public static HttpGet get(String token, String uri) throws NullPointerException, RuntimeException {
		return (HttpGet) createRequest(new HttpGet(uri), token, null);
	}
	
}
