/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.utils.URLUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import sun.security.x509.X509CertImpl;

/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * by using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.18/) 
 * 
 */
public class KubernetesClient {

	public static final Logger m_logger                   = Logger.getLogger(KubernetesClient.class.getName());
	
	public static final String ADMIN_CONF                 = "/etc/kubernetes/admin.conf";
	
	public static final String HTTP_HEADER_KEY            = "Sec-WebSocket-Protocol";
	
	public static final String HTTP_HEADER_VALUE          = "v4.channel.k8s.io";
	
	public static final String HTTP_ORIGIN                = "Origin";
	
	public static final String HTTP_GET                   = "GET";
	
	/**
	 * master IP
	 */
	protected final String masterUrl;
	
	/**
	 * token
	 */
	protected final String token;
	
	/**
	 * config
	 */
	protected final KubernetesConfig kubeConfig;
	
	/**
	 * client
	 */
	protected final OkHttpClient httpClient;
	
	/**
	 * @param masterUrl                             
	 * @throws Exception
	 */
	public KubernetesClient(String masterUrl) throws Exception {
		this(masterUrl, null);
	}
	
	@SuppressWarnings("deprecation")
	public KubernetesClient(String masterUrl, String token) throws Exception {
		super();
		this.masterUrl = masterUrl;
		this.token = token;
		this.httpClient = (token == null) ? 
				new OkHttpClient.Builder()
						.readTimeout(0, TimeUnit.SECONDS)
						.pingInterval(30, TimeUnit.SECONDS)
						.build() 
				: new OkHttpClient.Builder()
						.readTimeout(0, TimeUnit.SECONDS)
						.pingInterval(30, TimeUnit.SECONDS)
						.hostnameVerifier(getHostnameVerifier())
						.sslSocketFactory(getSocketFactory())
						.build();
		this.kubeConfig = KubernetesAnalyzer.getParser(this).getConfig();
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
	 * create a Kubernetes resource using JSON
	 * 
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode createResource(JsonNode json) throws Exception {

		final String kind = getKind(json);
		
		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), 
							getNamespace(kubeConfig.isNamespaced(kind), json), 
							kubeConfig.getName(kind));
		
		RequestBody requestBody = RequestBody.create(
						KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_POST, uri, requestBody));
	}
	
	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode deleteResource(JsonNode json) throws Exception {

		final String kind = getKind(json);
		
		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), 
							getNamespace(kubeConfig.isNamespaced(kind), json), 
							kubeConfig.getName(kind), getName(json));
		
		RequestBody requestBody = RequestBody.create(
						KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_DELETE, uri, requestBody));
	}
	
	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), 
							getNamespace(kubeConfig.isNamespaced(kind), json), 
							kubeConfig.getName(kind), getName(json));
		
		ObjectNode node = json.deepCopy();
		
		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			node.remove(KubernetesConstants.KUBE_STATUS);
		} 
		
		RequestBody requestBody = RequestBody.create(
						KubernetesConstants.HTTP_MEDIA_TYPE, node.toString());
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_PUT, uri, requestBody));
	}
	
	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace, if this kind unsupports namespace, it is null
	 * @param name                              name
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {
		
		final String uri = URLUtils.join(
								kubeConfig.getApiPrefix(kind), 
								getNamespace(kubeConfig.isNamespaced(kind), namespace), 
								kubeConfig.getName(kind), name);
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_GET, uri, null));
	}
	
	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind                            kind
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}
	
	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind, String namespace) throws Exception {
		return listResources(kind, namespace, null, null, 0, null);
	}
	
	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector and labelSelector
	 * 
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @param fieldSelector                   fieldSelector
	 * @param labelSelector                   labelSelector
	 * @return                                json
	 * @throws Exception                      exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector) throws Exception {
		return listResources(kind, namespace, fieldSelector, labelSelector, 0, null);
	}
	
	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector, labelSelector, limit and nextId
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param fieldSelector                     fieldSelector
	 * @param labelSelector                     labelSelector
	 * @param limit                             limit
	 * @param nextId                            nextId
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector, int limit, String nextId) throws Exception {
		StringBuilder uri = new StringBuilder();
		
		uri.append(URLUtils.join(
						kubeConfig.getApiPrefix(kind), 
						getNamespace(kubeConfig.isNamespaced(kind), namespace), 
						kubeConfig.getName(kind)));
		
		uri.append(KubernetesConstants.HTTP_QUERY_KIND + kind);
		
		if (limit > 0) {
			uri.append(KubernetesConstants.HTTP_QUERY_PAGELIMIT).append(limit);
		}
		
		if (nextId != null) {
			uri.append(KubernetesConstants.HTTP_QUERY_NEXTID).append(nextId);
		}
		
		if (fieldSelector != null) {
			uri.append(KubernetesConstants.HTTP_QUERY_FIELDSELECTOR).append(fieldSelector);
		}
		
		if (labelSelector != null) {
			uri.append(KubernetesConstants.HTTP_QUERY_LABELSELECTOR).append(labelSelector);
		}
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_GET, uri.toString(), null));
	}
	
	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), getNamespace(
							kubeConfig.isNamespaced(kind), json), kubeConfig.getName(kind), 
							getName(json), KubernetesConstants.HTTP_RESPONSE_STATUS);
		
		RequestBody requestBody = RequestBody.create(
						KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		return getResponse(createRequest(KubernetesConstants.HTTP_REQUEST_PUT, uri, requestBody));
	}
	
	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @param listener                          listenerm
	 */
	public void watchResource(String kind, String namespace, String name, WebSocketListener listener) throws Exception {
		final String uri = URLUtils.join(kubeConfig.getApiPrefix(kind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
											getNamespace(kubeConfig.isNamespaced(kind), namespace), kubeConfig.getName(kind), name, 
											KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
		
		OkHttpClient clone = httpClient.newBuilder()
				.readTimeout(0, TimeUnit.MILLISECONDS)
				.build();
		clone.newWebSocket(createWebSocketRequest(uri), listener);
		clone.dispatcher().executorService().shutdown();
	}
	
	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param listener                          listenerm
	 */
	public void watchResources(String kind, String namespace, WebSocketListener listener) throws Exception {
		final String uri = URLUtils.join(kubeConfig.getApiPrefix(kind),  
											getNamespace(kubeConfig.isNamespaced(kind), namespace), kubeConfig.getName(kind),  
											KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
		
		OkHttpClient clone = httpClient.newBuilder()
				.readTimeout(0, TimeUnit.SECONDS)
				.build();
		clone.newWebSocket(createWebSocketRequest(uri), listener);
		clone.dispatcher().executorService().shutdown();
	}

	/**
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	@Deprecated
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), 
							getNamespace(kubeConfig.isNamespaced(kind), namespace), 
							kubeConfig.getName(kind), name);
		
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, 
				new ObjectMapper().writeValueAsString(map));
		
		Request request = createRequest(KubernetesConstants
				.HTTP_REQUEST_DELETE, uri, requestBody);
		
		return getResponse(request);
	}
	
	
	/**********************************************************
	 * 
	 *               Request and Response
	 * 
	 **********************************************************/
	
	/**
	 * @param uri
	 * @return
	 */
	protected Request createWebSocketRequest(String uri) {
		return new Request.Builder()
				.get()
				.url(uri)
				.addHeader("connection", "keep-alive")
				.addHeader(HTTP_HEADER_KEY, HTTP_HEADER_VALUE)
				.addHeader(HTTP_ORIGIN, this.masterUrl)
				.build();
	}
	
	/**
	 * @param type                             type
	 * @param uri                              uri
	 * @param requestBody                      body
	 * @return                                 request
	 */
	protected Request createRequest(String type, final String uri, RequestBody requestBody) {
		return (HTTP_GET.equals(type)) 
							? new Request.Builder().method(HTTP_GET, null).url(uri).build()
							: new Request.Builder().method(type, requestBody).url(uri).build();
	}
	
	/**
	 * @param request                           request
	 * @return                                  response
	 * @throws Exception                        exception
	 */
	protected synchronized JsonNode getResponse(Request request) throws Exception {
		
		Response response = null;
		try {
			response = httpClient.newCall(request).execute();
			return new ObjectMapper().readTree(response.body().byteStream());
		} catch (Exception ex) {
			m_logger.severe(ex.toString());
			throw new KubernetesException(ex);
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
	public String getKind(JsonNode json) throws Exception {
		return json.get(KubernetesConstants.KUBE_KIND).asText();
	}
	
	/**
	 * @param json                             json
	 * @return                                 name
	 */
	public String getName(JsonNode json) throws Exception {
		return json.get(KubernetesConstants.KUBE_METADATA)
					.get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}
	
	
	/**
	 * @param namespaced                       bool
	 * @param namespace                        ns
	 * @return                                 full path
	 */
	public String getNamespace(boolean namespaced, String namespace) throws Exception {
		return (namespaced && namespace.length() != 0) 
					? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
						: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}
	
	/**
	 * @param namespaced                       bool
	 * @param json                             json
	 * @return                                 full path
	 */
	public String getNamespace(boolean namespaced, JsonNode json) throws Exception {
		JsonNode meta = json.get(KubernetesConstants.KUBE_METADATA);
		String ns = meta.has(KubernetesConstants.KUBE_METADATA_NAMESPACE) 
					? meta.get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText()
						: KubernetesConstants.VALUE_DEFAULT_NAMESPACE;
					
		return getNamespace(namespaced, ns);
	}
	
	/**
	 * @return                                  url
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * @return                                  config
	 */
	public KubernetesConfig getConfig() {
		return kubeConfig;
	}
	
}
