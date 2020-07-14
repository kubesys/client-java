/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.utils.URLUtils;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClient {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());
	
	
	/**
	 * admin.conf
	 */
	public static final String ADMIN_CONF = "/etc/kubernetes/admin.conf";
	
	
	public static final String HTTP_HEADER_KEY            = "Sec-WebSocket-Protocol";
	
	public static final String HTTP_HEADER_VALUE          = "v4.channel.k8s.io";
	
	public static final String HTTP_ORIGIN                = "Origin";
	
	public static final MediaType HTTP_MEDIA_TYPE         = MediaType.parse("application/json");
	
	public static final String HTTP_GET                   = "GET";
	
	
	
	/**
	 * Kubernetes' leader API
	 */
	protected final String url;
	
	/**
	 * config
	 */
	protected final KubernetesConfig config;
	
	/**
	 * client
	 */
	protected final OkHttpClient client;
	

	
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
		this(new ConfigBuilder().withMasterUrl(url).build());
	}
	
	public KubernetesClient(Config kubeconfig) throws Exception {
		this.url = kubeconfig.getMasterUrl();
		this.client = HttpClientUtils.createHttpClient(kubeconfig);
		this.config = KubernetesAnalyzer
				.getParser(this).getConfig();
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static KubernetesClient getKubeClient(File token) throws Exception {

		Map<String, Object> map = new Yaml().load(new FileInputStream(token));
		

		Map<String, Map<String, Object>> clusdata = (Map<String, Map<String, Object>>) ((List) map.get("clusters"))
				.get(0);

		Map<String, Map<String, Object>> userdata = (Map<String, Map<String, Object>>) ((List) map.get("users")).get(0);

		Config config = new ConfigBuilder().withApiVersion("v1")
				.withCaCertData((String) clusdata.get("cluster").get("certificate-authority-data"))
				.withClientCertData((String) userdata.get("user").get("client-certificate-data"))
				.withClientKeyData((String) userdata.get("user").get("client-key-data"))
				.withMasterUrl((String) clusdata.get("cluster").get("server")).build();

		return new KubernetesClient(config);
	}
	
	/**
	 * create token: kubectl apply -f account.yaml ()
	 * get token: kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep kubernetes-client | awk '{print $1}') | grep "token:" | awk -F":" '{print$2}' | sed 's/ //g'
	 * 
	 * @param url                              url
	 * @param token                            token
	 * @throws Exception                       exception
	 */
//	public KubernetesClient(String url, String token) throws Exception {
//		super();
//		this.url = url;
//		this.token = token;
//		X509TrustManager initTrustManager = initTrustManager();
//		this.client = (token == null) 
//				?  new OkHttpClient.Builder()
//							.retryOnConnectionFailure(true)
//							.pingInterval(30000, TimeUnit.MILLISECONDS)
//							.connectTimeout(0, TimeUnit.MILLISECONDS)
//							.readTimeout(0, TimeUnit.MILLISECONDS)
//							.followRedirects(true)
//							.followSslRedirects(true)
//							.build()
//						: new OkHttpClient.Builder()
//								.sslSocketFactory(initSslSocketFactory(
//                    							initTrustManager), initTrustManager)
//								.hostnameVerifier(initHostnameVerifier())
//								.pingInterval(30000, TimeUnit.MILLISECONDS)
//								.connectTimeout(0, TimeUnit.MILLISECONDS)
//								.readTimeout(0, TimeUnit.MILLISECONDS)
//								.followRedirects(true)
//								.followSslRedirects(true)
//								.retryOnConnectionFailure(true)
//								.build();
//		this.config = KubernetesAnalyzer
//				.getParser(this).getConfig();
//	}

	/**
	 * @param trustManager                    trustManager
	 * @return                                SSLSocketFactory
	 * @throws NoSuchAlgorithmException       NoSuchAlgorithmException
	 * @throws KeyManagementException         KeyManagementException
	 */
	/*
	 * protected SSLSocketFactory initSslSocketFactory(X509TrustManager
	 * trustManager) throws NoSuchAlgorithmException, KeyManagementException {
	 * SSLContext sslContext = SSLContext.getInstance("TLS"); sslContext.init(null,
	 * new TrustManager[]{trustManager}, null); return
	 * sslContext.getSocketFactory(); }
	 */

	/**
	 * @return                                X509TrustManager  
	 */
	/*
	 * protected X509TrustManager initTrustManager() { return new X509TrustManager()
	 * {
	 * 
	 * @Override public void checkClientTrusted(X509Certificate[] chain, String
	 * authType) throws CertificateException { // Do nothing if (chain == null) {
	 * throw new CertificateException("Client is not using tls"); } }
	 * 
	 * @Override public void checkServerTrusted(X509Certificate[] chain, String
	 * authType) throws CertificateException { // Do nothing if (chain == null) {
	 * throw new CertificateException("Server is not using tls"); } }
	 * 
	 * @Override public X509Certificate[] getAcceptedIssuers() { return new
	 * X509Certificate[0]; } }; }
	 */

	/**
	 * @return                                hostnameVerifier   
	 */
	/*
	 * protected HostnameVerifier initHostnameVerifier() { return new
	 * HostnameVerifier() {
	 * 
	 * @Override public String toString() { return super.toString(); }
	 * 
	 * @Override public boolean verify(String hostname, SSLSession arg1) { return
	 * hostname != null; }
	 * 
	 * }; }
	 */
	

	/**********************************************************
	 * 
	 *               Core
	 * 
	 **********************************************************/
	
	public static final String URL = "url: ";
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode createResource(JsonNode json) throws Exception {

		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
							config.isNamespaced(kind), json), config.getName(kind));
		
		m_logger.info(URL + uri);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		Request request = createNormalRequest(KubernetesConstants
				.HTTP_REQUEST_POST, uri, requestBody);
		
		return getResponse(request);
	}
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
								config.isNamespaced(kind), json), 
								config.getName(kind), getName(json));
		
		m_logger.info(URL + uri);
		
		ObjectNode node = json.deepCopy();
		
		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			node.remove(KubernetesConstants.KUBE_STATUS);
		} 
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, node.toString());
		
		Request request = createNormalRequest(KubernetesConstants
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
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
				config.isNamespaced(kind), namespace), 
				config.getName(kind), name);
		
		m_logger.info(URL + uri);
		
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, 
				new ObjectMapper().writeValueAsString(map));
		
		Request request = createNormalRequest(KubernetesConstants
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
		
		m_logger.info(URL + uri);
		
		Request request = createNormalRequest(KubernetesConstants
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
	 * @param fieldSelector                   fieldSelector
	 * @param labelSelector                   labelSelector
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
	 * @return                                  json
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
		
		m_logger.info(URL + fullUri.toString());
		
		Request request = createNormalRequest(KubernetesConstants
				.HTTP_REQUEST_GET, fullUri.toString(), null);
		
		return getResponse(request);
	}
	
	/**
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {
		
		final String kind = getKind(json);
		
		final String uri = URLUtils.join(config.getApiPrefix(kind), getNamespace(
							config.isNamespaced(kind), json), config.getName(kind), 
							getName(json), KubernetesConstants.HTTP_RESPONSE_STATUS);
		
		m_logger.info(URL + uri);
		
		RequestBody requestBody = RequestBody.create(
				KubernetesConstants.HTTP_MEDIA_TYPE, json.toString());
		
		Request request = createNormalRequest(KubernetesConstants
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
		
		m_logger.info(URL + uri);
		
		OkHttpClient clone = client.newBuilder()
				.readTimeout(0, TimeUnit.MILLISECONDS)
//				.connectTimeout(10000, TimeUnit.MILLISECONDS)
//				.readTimeout(5000, TimeUnit.MILLISECONDS)
//				.pingInterval(30000, TimeUnit.MILLISECONDS)
//				.protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
//				.cache(null)
				.build();
		clone.newWebSocket(createWebSocket(uri), listener);
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
		
		m_logger.info(URL + uri);
		
		OkHttpClient clone = client.newBuilder()
				.readTimeout(0, TimeUnit.MILLISECONDS)
//				.connectTimeout(10000, TimeUnit.MILLISECONDS)
//				.readTimeout(5000, TimeUnit.MILLISECONDS)
//				.pingInterval(30000, TimeUnit.MILLISECONDS)
//				.protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
//				.cache(null)
				.build();
		clone.newWebSocket(createWebSocket(uri), listener);
		clone.dispatcher().executorService();
	}

	
	protected Request createWebSocket(String fullUrl) {
		return new Request.Builder()
				.header(HTTP_HEADER_KEY, HTTP_HEADER_VALUE)
				.addHeader(HTTP_ORIGIN, this.url)
				.method(HTTP_GET, null).url(fullUrl).build();
	}
	/**
	 * @return                                  builder
	 */
	/*
	 * protected Builder getBuilder() { if (token == null) { return new
	 * Request.Builder() .header(KubernetesConstants.HTTP_REQUEST_HEADER_KEY ,
	 * KubernetesConstants.HTTP_REQUEST_HEADER_VALUE)
	 * .addHeader(KubernetesConstants.HTTP_REQUEST_ORIGIN, url)
	 * .method(KubernetesConstants.HTTP_REQUEST_GET, null); } else { return new
	 * Request.Builder() .header(KubernetesConstants.HTTP_REQUEST_HEADER_KEY ,
	 * KubernetesConstants.HTTP_REQUEST_HEADER_VALUE)
	 * .addHeader(KubernetesConstants.HTTP_REQUEST_ORIGIN, url)
	 * .addHeader(KubernetesConstants.HTTP_REQUEST_AUTHORIZATION, "Bearer " + token)
	 * .method(KubernetesConstants.HTTP_REQUEST_GET, null); } }
	 */
	
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
	protected Request createNormalRequest(String type, final String uri, RequestBody requestBody) {
		if (HTTP_GET.equals(type)) {
			return new Request.Builder()
//				.header(HTTP_HEADER_KEY, HTTP_HEADER_VALUE)
//				.addHeader(HTTP_ORIGIN, this.url)
				.method(HTTP_GET, null).url(uri).build();
		} else {
			return new Request.Builder()
//					.header(HTTP_HEADER_KEY, HTTP_HEADER_VALUE)
//					.addHeader(HTTP_ORIGIN, this.url)
					.method(type, requestBody).url(uri).build();
		}
	}
	
	/**
	 * @param request                           request
	 * @return                                  response
	 * @throws Exception                        exception
	 */
	protected synchronized JsonNode getResponse(Request request) throws Exception {
		Response response = null;
		try {
			response = client.newCall(request).execute();
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
		return (namespaced && namespace.length() != 0) ? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
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
	 * @return                                  config
	 */
	public KubernetesConfig getConfig() {
		return config;
	}
	
}
