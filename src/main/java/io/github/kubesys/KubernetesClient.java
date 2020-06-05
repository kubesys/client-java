/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.utils.URLUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;
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
	protected final KubernetesConfig config;
	

	
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
		TrustManager[] managers = new TrustManager[] {
								new TrustAllManager()};
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, managers, new SecureRandom());
		return sc.getSocketFactory();
	}
	
	
	/**
	 * 
	 * @author wuheng09@gmail.com
	 *
	 */
	private static class TrustAllManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// ignore here
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// ignore here
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			X509CertImpl xc =  new X509CertImpl();
			return new X509Certificate[] {xc};
		}
				
	}
	
	/**
	 * @return                                  HostnameVerifier
	 */
	private HostnameVerifier getHostnameVerifier() {
		return new HostnameVerifier() {
			
			@Override
			public String toString() {
				return super.toString();
			}

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return (hostname != null);
			}

		};
	}

	/**********************************************************
	 * 
	 *               Core
	 * 
	 **********************************************************/
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	@SuppressWarnings("deprecation")
	public JsonNode createResource(JsonNode json) throws Exception {

		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
							config.isNamespaced(kind), json), config.getName(kind));
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_POST, uri, requestBody);
		
		return getResponse(request);
	}
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	@SuppressWarnings("deprecation")
	public JsonNode updateResource(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
								config.isNamespaced(kind), json), 
								config.getName(kind), getName(json));
		
		ObjectNode node = json.deepCopy();
		
		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			node.remove(KubernetesConstants.KUBE_STATUS);
		} 
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, node.toString());
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_PUT, uri, requestBody);
		
		return getResponse(request);
	}
	
	
	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	@SuppressWarnings("deprecation")
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
				config.isNamespaced(kind), namespace), 
				config.getName(kind), name);
		
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, 
				new ObjectMapper().writeValueAsString(map));
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_DELETE, uri, requestBody);
		
		return getResponse(request);
	}
	
	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
											config.isNamespaced(kind), namespace), 
											config.getName(kind), name);
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_GET, uri, null);
		
		return getResponse(request);
	}
	
	/**
	 * @param kind                            kind
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}
	
	/**
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind, String namespace) throws Exception {
		return listResources(kind, namespace, null, null, 0, null);
	}
	
	/**
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector) throws Exception {
		return listResources(kind, namespace, fieldSelector, labelSelector, 0, null);
	}
	
	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param fieldSelector                     fieldSelector
	 * @param labelSelector                     labelSelector
	 * @param limit                             limit
	 * @param nextId                            nextId
	 * @return
	 * @throws Exception                        exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector, int limit, String nextId) throws Exception {
		StringBuilder fullUri = new StringBuilder();
		
		fullUri.append(URLUtils.join(config.getApiPrefix(kind), getNamespace(
										config.isNamespaced(kind), namespace), 
										config.getName(kind)));
		fullUri.append(KubernetesConstants.HTTP_QUERY_KIND + kind);
		
		if (limit > 0) {
			fullUri.append(KubernetesConstants.HTTP_QUERY_PAGELIMIT).append(limit);
		}
		
		if (nextId != null) {
			fullUri.append(KubernetesConstants.HTTP_QUERY_NEXTID).append(nextId);
		}
		
		if (fieldSelector != null) {
			fullUri.append(KubernetesConstants.HTTP_QUERY_FIELDSELECTOR).append(fieldSelector);
		}
		
		if (labelSelector != null) {
			fullUri.append(KubernetesConstants.HTTP_QUERY_LABELSELECTOR).append(labelSelector);
		}
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_GET, fullUri.toString(), null);
		
		return getResponse(request);
	}
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	@SuppressWarnings("deprecation")
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
							config.isNamespaced(kind), json), config.getName(kind), 
							getName(json), KubernetesConstants.HTTP_RESPONSE_STATUS);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_PUT, uri, requestBody);
		
		return getResponse(request);
	}
	
	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @param listener                          listenerm
	 */
	public void watchResource(String kind, String namespace, String name, WebSocketListener listener) {
		final String uri = URLUtils.join(config.getApiPrefix(kind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
											getNamespace(config.isNamespaced(kind), namespace), config.getName(kind), name, 
											KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
		
		OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
		Builder builder = new Request.Builder()
				.header(KubernetesConstants.HTTP_HEADER_KEY
						, KubernetesConstants.HTTP_HEADER_VALUE)
				.addHeader(KubernetesConstants.HTTP_ORIGIN, url)
				.method(KubernetesConstants.HTTP_REQUEST_GET, null);
		clone.newWebSocket(builder.url(uri).build(), listener);
		clone.dispatcher().executorService();
	}
	
	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param listener                          listenerm
	 */
	public void watchResources(String kind, String namespace, WebSocketListener listener) {
		final String uri = URLUtils.join(config.getApiPrefix(kind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
											getNamespace(config.isNamespaced(kind), namespace), config.getName(kind),  
											KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
		
		OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
		Builder builder = new Request.Builder()
				.header(KubernetesConstants.HTTP_HEADER_KEY
						, KubernetesConstants.HTTP_HEADER_VALUE)
				.addHeader(KubernetesConstants.HTTP_ORIGIN, url)
				.method(KubernetesConstants.HTTP_REQUEST_GET, null);
		clone.newWebSocket(builder.url(uri).build(), listener);
		clone.dispatcher().executorService();
	}
	
	/**********************************************************
	 * 
	 *               Request and Response
	 * 
	 **********************************************************/
	
	/**
	 * @param type                             type
	 * @param uri                              uri
	 * @param requestBody                      body
	 * @return                                 request
	 */
	protected Request createRequest(String type, final String uri, RequestBody requestBody) {
		Builder builder = (token == null) ? new Builder() : 
			new Builder().header("Authorization", "Bearer " + token);
		return builder.method(type, requestBody).url(uri).build();
	}
	
	/**
	 * @param request                           request
	 * @return                                  response
	 * @throws Exception                        exception
	 */
	protected JsonNode getResponse(Request request) throws Exception {
		Response response = null;
		try {
			response = client.newCall(request).execute();
			return new ObjectMapper().readTree(response.body().byteStream());
		} catch (Exception ex) {
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
	
	/**
	 * @param json                             json
	 * @return                                 kind
	 */
	public String getKind(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_KIND).asText();
	}
	
	/**
	 * @param json                             json
	 * @return                                 name
	 */
	public String getName(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_METADATA)
					.get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}
	
	/**
	 * @param namespaced                       bool
	 * @param namespace                        ns
	 * @return                                 full path
	 */
	public String getNamespace(boolean namespaced, String namespace) {
		return (namespaced && namespace.length() != 0) ? KubernetesConstants.KUBE_NAMESPACES_PATTERN + namespace
						: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}
	
	/**
	 * @param namespaced                       bool
	 * @param json                             json
	 * @return                                 full path
	 */
	public String getNamespace(boolean namespaced, JsonNode json) {
		JsonNode meta = json.get(KubernetesConstants.KUBE_METADATA);
		String ns = meta.has(KubernetesConstants.KUBE_METADATA_NAMESPACE) 
					? meta.get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText()
						: KubernetesConstants.VALUE_DEFAULT_NAMESPACE;
					
		return getNamespace(namespaced, ns);
	}
	
	/**
	 * @return                                  url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return                                  token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return                                  client
	 */
	public OkHttpClient getClient() {
		return client;
	}

	/**
	 * @return                                  config
	 */
	public KubernetesConfig getConfig() {
		return config;
	}
	
}
