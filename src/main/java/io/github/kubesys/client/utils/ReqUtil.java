/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.kubesys.client.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import io.github.kubesys.client.beans.KubernetesAdminConfig;

/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since 1.0.5
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
	 * @param url   request
	 * @param config config
	 * @param body  body
	 * @return request
	 */
	private static HttpUriRequestBase createRequest(HttpUriRequestBase url, KubernetesAdminConfig config, String body) {
		try {
			url.setEntity(new StringEntity(body == null ? "" : body, ContentType.APPLICATION_JSON));
		} catch (Exception ex) {
			// ignore here
		}
		
		if (config.getToken() != null) {
			setBearerHeader(url, config.getToken());
		} else if (config.getUsername() != null) {
			setBasicHeader(url, config.getUsername(), config.getPassword());
		}
		return url;
	}
	
	/**
	 * @param request request
	 * @param token   token
	 */
	private static void setBearerHeader(HttpUriRequestBase request, String token) {
		request.addHeader("Authorization", "Bearer " + token);
		request.addHeader("Connection", "keep-alive");
	}
	
	/**
	 * @param request request
	 * @param token   token
	 */
	private static void setBasicHeader(HttpUriRequestBase request, String username, String password) {
		request.addHeader("Authorization", "Basic " 
							+ Base64.getEncoder().encodeToString(
								(username + ":" + password).getBytes()));
		request.addHeader("Connection", "keep-alive");
	}
	

	/**********************************************************
	 * 
	 * Core
	 * 
	 **********************************************************/
	/**
	 * @param config config
	 * @param uri   uri
	 * @param body  body
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpPost post(KubernetesAdminConfig config, String uri, String body) throws MalformedURLException {
		return (HttpPost) createRequest(new HttpPost(new URL(uri).toString()), config, body);
	}

	/**
	 * @param config config
	 * @param uri   uri
	 * @param body  body
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpPut put(KubernetesAdminConfig config, String uri, String body) throws MalformedURLException {
		return (HttpPut) createRequest(new HttpPut(new URL(uri).toString()), config, body);
	}

	/**
	 * @param config config
	 * @param uri   uri
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpDelete delete(KubernetesAdminConfig config, String uri) throws MalformedURLException {
		return (HttpDelete) createRequest(new HttpDelete(new URL(uri).toString()), config, null);
	}

	/**
	 * @param config config
	 * @param uri   uri
	 * @return request or null
	 * @throws MalformedURLException MalformedURLException
	 */
	public static HttpGet get(KubernetesAdminConfig config, String uri) throws MalformedURLException {
		return (HttpGet) createRequest(new HttpGet(new URL(uri).toString()), config, null);
	}

}
