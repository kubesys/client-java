/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import sun.security.x509.X509CertImpl;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClient {

	/**
	 * Kubernetes' leader API
	 */
	protected final String url;
	
	/**
	 * Kubernetes's secret
	 */
	protected final String token;
	
	/**
	 * http client
	 */
	protected final OkHttpClient client;
	
	/**
	 * config
	 */
	protected final kubernetesConfig config;
	

	
	/**********************************************************
	 * 
	 *               Constructors
	 * 
	 **********************************************************/
	
	/**
	 * @param url                              url
	 * @throws Exception                       exception
	 */
	public KubernetesClient(String url) throws Exception {
		this(url, null);
	}
	
	/**
	 * @param url                              url
	 * @param token                            token
	 * @throws Exception                       exception
	 */
	@SuppressWarnings("deprecation")
	public KubernetesClient(String url, String token) throws Exception {
		super();
		this.url = url;
		this.token = token;
		this.client = (token == null) ? 
				new OkHttpClient.Builder().build() 
				: new OkHttpClient.Builder()
						.hostnameVerifier(getHostnameVerifier())
						.sslSocketFactory(getSocketFactory())
						.build();
		this.config = KubernetesParser
				.getParser(this).getConfig();
	}
	
	/**
	 * @return                                  SocketFactory
	 * @throws Exception                        exception
	 */
	private static SSLSocketFactory getSocketFactory() throws Exception {
		TrustManager[] managers = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				X509CertImpl xc =  new X509CertImpl();
				return new X509Certificate[] {xc};
			}

			public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
			}
			
		} };

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, managers, new SecureRandom());
		return sc.getSocketFactory();
	}
	
	/**
	 * @return                                  HostnameVerifier
	 * @throws Exception                        exception
	 */
	private HostnameVerifier getHostnameVerifier() throws Exception {
		return new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}

		};
	}

	/**********************************************************
	 * 
	 *               Core
	 * 
	 **********************************************************/
	
	protected Request createRequest(String type, final String uri, RequestBody requestBody) {
		Builder builder = (token == null) ? new Builder() : 
			new Builder().header("Authorization", "Bearer " + token);
		return builder.method(type, requestBody).url(uri).build();
	}
	
	protected JsonNode getResponse(Request request) throws Exception {
		Response response = null;
		try {
			response = client.newCall(request).execute();
			return new ObjectMapper().readTree(response.body().byteStream());
		} catch (Exception ex) {
			System.out.println(ex);
			throw new Exception(ex);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	/**********************************************************
	 * 
	 *               Getter
	 * 
	 **********************************************************/
	
	public String getUrl() {
		return url;
	}

	public String getToken() {
		return token;
	}

	public OkHttpClient getClient() {
		return client;
	}

	public kubernetesConfig getConfig() {
		return config;
	}
	
}
