/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since 2.0.5
 **/
public class ReqUtil {

	/**********************************************************
	 * 
	 * Commons
	 * 
	 **********************************************************/

	private ReqUtil() {
		super();
	}

	/**
	 * @param req   request
	 * @param token token
	 * @param body  body
	 * @return request
	 */
	private static HttpUriRequestBase createRequest(HttpUriRequestBase req, String token, String body) {
		try {
			req.setEntity(new StringEntity(body == null ? "" : body, ContentType.APPLICATION_JSON));
		} catch (Exception ex) {
			// ignore here
		}
		setBearerHeader(req, token);
		return req;
	}

	/**
	 * @param request request
	 * @param token   token
	 */
	private static void setBearerHeader(HttpUriRequestBase request, String token) {
		if (token != null) {
			request.addHeader("Authorization", "Bearer " + token);
		}
		request.addHeader("Connection", "keep-alive");
	}

	/**********************************************************
	 * 
	 * Core
	 * 
	 **********************************************************/
	/**
	 * @param token token
	 * @param uri   uri
	 * @param body  body
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpPost post(String token, String uri, String body) throws MalformedURLException {
		return (HttpPost) createRequest(new HttpPost(new URL(uri).toString()), token, body);
	}

	/**
	 * @param token token
	 * @param uri   uri
	 * @param body  body
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpPut put(String token, String uri, String body) throws MalformedURLException {
		return (HttpPut) createRequest(new HttpPut(new URL(uri).toString()), token, body);
	}

	/**
	 * @param token token
	 * @param uri   uri
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpDelete delete(String token, String uri) throws MalformedURLException {
		return (HttpDelete) createRequest(new HttpDelete(new URL(uri).toString()), token, null);
	}

	/**
	 * @param token token
	 * @param uri   uri
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpGet get(String token, String uri) throws MalformedURLException {
		return (HttpGet) createRequest(new HttpGet(new URL(uri).toString()), token, null);
	}

}
