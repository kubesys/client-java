/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.utils.URLUtils;

import sun.security.x509.X509CertImpl;

/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * by using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.18/) 
 * 
 */
@SuppressWarnings("deprecation")
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
	protected final CloseableHttpClient httpClient;
	
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
		HttpClientBuilder builder = HttpClients.custom();
		if (token != null) {
			builder.setSSLHostnameVerifier(getHostnameVerifier())
					.setSSLSocketFactory(new org.apache.http.conn.ssl.SSLSocketFactory(
							getSocketFactory(), new AllowAllHostnameVerifier()));
		}
		this.httpClient = builder.build(); 
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
		
		HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(json.toString(),
						ContentType.APPLICATION_JSON));
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
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
		
		HttpDelete request = new HttpDelete(uri);
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
	}
	
	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param json                              json
	 * @return                                  json
	 * @throws Exception                        exception
	 */
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = URLUtils.join(
							kubeConfig.getApiPrefix(kind), 
							getNamespace(kubeConfig.isNamespaced(kind), namespace), 
							kubeConfig.getName(kind), name);
		
		HttpDelete request = new HttpDelete(uri);
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
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
		
		HttpPut request = new HttpPut(uri);
		request.setEntity(new StringEntity(json.toString(),
						ContentType.APPLICATION_JSON));
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
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
		
		HttpGet request = new HttpGet(uri);
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
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
		
		HttpGet request = new HttpGet(uri.toString());
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
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
		
		HttpPut request = new HttpPut(uri);
		request.setEntity(new StringEntity(json.toString(),
						ContentType.APPLICATION_JSON));
		if (token != null) {
			request.setHeader("Authorization", "Bearer " + token);
		}
		
		return getResponse(httpClient.execute(request));
	}
	
	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param name                              name
	 * @param listener                          listenerm
	 */
	public void watchResource(String kind, String namespace, String name, KubernetesWatcher watcher) throws Exception {
		
	}
	
	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind                              kind
	 * @param namespace                         namespace
	 * @param listener                          listenerm
	 */
	public void watchResources(String kind, String namespace, KubernetesWatcher watcher) throws Exception {
	}

	/**********************************************************
	 * 
	 *               Request and Response
	 * 
	 **********************************************************/
	
	
	/**
	 * @param request                           request
	 * @return                                  response
	 * @throws Exception                        exception
	 */
	protected synchronized JsonNode getResponse(CloseableHttpResponse response) throws Exception {
		
		try {
			return new ObjectMapper().readTree(response.getEntity().getContent());
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
