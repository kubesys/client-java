/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.utils.HttpUtils;
import com.github.kubesys.utils.SSLUtils;
import com.github.kubesys.utils.URLUtils;

/**
 * @author wuheng@iscas.ac.cn
 *
 *         Support create, update, delete, get and list [Kubernetes
 *         resources](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 *         by using [Kubernetes native
 *         API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.18/)
 * 
 */
public class KubernetesClient {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());

	/**
	 * master IP
	 */
	protected final String masterUrl;

	/**
	 * token
	 */
	protected final String tokenInfo;

	/**
	 * analyzer
	 */
	protected final KubernetesAnalyzer kubeAnalyzer;

	/**
	 * client
	 */
	protected final CloseableHttpClient httpClient;

	/**
	 * @param masterUrl masterUrl
	 */
	public KubernetesClient(String masterUrl) {
		this(masterUrl, null);
	}

	/**
	 * @param masterUrl masterUrl
	 * @param tokenInfo token
	 */
	public KubernetesClient(String masterUrl, String tokenInfo) {
		this.masterUrl = masterUrl;
		this.tokenInfo = tokenInfo;
		this.httpClient = createDefaultHttpClient();
		this.kubeAnalyzer = KubernetesAnalyzer.getParser(this);
	}

	/**
	 * @param masterUrl    masterUrl
	 * @param tokenInfo    token
	 * @param kubeAnalyzer analyzer
	 */
	public KubernetesClient(String masterUrl, String tokenInfo, KubernetesAnalyzer kubeAnalyzer) {
		super();
		this.masterUrl = masterUrl;
		this.tokenInfo = tokenInfo;
		this.httpClient = createDefaultHttpClient();
		this.kubeAnalyzer = kubeAnalyzer;
	}

	/**
	 * @return httpClient
	 */
	protected CloseableHttpClient createDefaultHttpClient() {

		SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(0).setSoReuseAddress(true)
				.build();

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(0).setConnectionRequestTimeout(0)
				.setSocketTimeout(0).build();

		return createDefaultHttpClientBuilder().setConnectionTimeToLive(0, TimeUnit.SECONDS)
				.setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
				.setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy()).build();
	}

	/**
	 * @return builder
	 */
	protected HttpClientBuilder createDefaultHttpClientBuilder() {
		HttpClientBuilder builder = HttpClients.custom();

		if (this.tokenInfo != null) {
			builder.setSSLHostnameVerifier(SSLUtils.createHostnameVerifier())
					.setSSLSocketFactory(SSLUtils.createSocketFactory());
		}

		return builder;
	}

	/**********************************************************
	 * 
	 * Core
	 * 
	 **********************************************************/

	/**
	 * create a Kubernetes resource using JSON
	 * 
	 * @param jsonStr json
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode createResource(String jsonStr) throws Exception {
		return createResource(new ObjectMapper().readTree(jsonStr));
	}

	/**
	 * create a Kubernetes resource using JSON
	 * 
	 * @param json json
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode createResource(JsonNode json) throws Exception {

		final String uri = createUrl(json);

		return getResponse(httpClient.execute(HttpUtils.post(tokenInfo, uri, json.toString())));
	}

	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param kind kind
	 * @param name name
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode deleteResource(String kind, String name) throws Exception {
		return deleteResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param json json
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode deleteResource(JsonNode json) throws Exception {
		return deleteResource(getKind(json), getNamespace(json), getName(json));
	}

	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @param name      name
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = deleteUrl(kind, namespace, name);

		return getResponse(httpClient.execute(HttpUtils.delete(tokenInfo, uri)));
	}

	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * @param json json
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {

		final String uri = updateUrl(getKind(json), getNamespace(json), getName(json));

		ObjectNode node = json.deepCopy();

		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			node.remove(KubernetesConstants.KUBE_STATUS);
		}

		return getResponse(httpClient.execute(HttpUtils.put(tokenInfo, uri, json.toString())));
	}

	/**
	 * binding a Kubernetes resource using JSON
	 * 
	 * { "apiVersion": "v1", "kind": "Binding", "metadata": { "name": "podName" },
	 * "target": { "apiVersion": "v1", "kind": "Node", "name": "hostName" } }
	 * 
	 * @param pod pod
	 * @param host host
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode bindingResource(JsonNode pod, String host) throws Exception {

		ObjectNode binding = new ObjectMapper().createObjectNode();
		binding.put("apiVersion", "v1");
		binding.put("kind", "Binding");
		
		ObjectNode metadata = new ObjectMapper().createObjectNode();
		metadata.put("name", pod.get("metadata").get("name").asText());
		binding.set("metadata", metadata);
		
		ObjectNode target = new ObjectMapper().createObjectNode();
		target.put("apiVersion", "v1");
		target.put("kind", "Node");
		target.put("name", host);
		binding.set("target", target);
			
		final String uri = bindingUrl(binding);
		return getResponse(httpClient.execute(HttpUtils.post(tokenInfo, uri, binding.toString())));
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind kind
	 * @param name name
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode getResource(String kind, String name) throws Exception {

		return getResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind      kind
	 * @param namespace namespace, if this kind unsupports namespace, it is null
	 * @param name      name
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {

		final String uri = getUrl(kind, namespace, name);

		return getResponse(httpClient.execute(HttpUtils.get(tokenInfo, uri)));
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind      kind
	 * @param namespace namespace, if this kind unsupports namespace, it is null
	 * @param name      name
	 * @return json
	 * @throws Exception exception
	 */
	public boolean hasResource(String kind, String namespace, String name) throws Exception {

		final String uri = getUrl(kind, namespace, name);

		try {
			getResponse(httpClient.execute(HttpUtils.get(tokenInfo, uri)));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind kind
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode listResources(String kind) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode listResources(String kind, String namespace) throws Exception {
		return listResources(kind, namespace, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector and
	 * labelSelector
	 * 
	 * @param kind          kind
	 * @param namespace     namespace
	 * @param fieldSelector fieldSelector
	 * @param labelSelector labelSelector
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector)
			throws Exception {
		return listResources(kind, namespace, fieldSelector, labelSelector, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector,
	 * labelSelector, limit and nextId
	 * 
	 * @param kind          kind
	 * @param namespace     namespace
	 * @param fieldSelector fieldSelector
	 * @param labelSelector labelSelector
	 * @param limit         limit
	 * @param nextId        nextId
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector, int limit,
			String nextId) throws Exception {
		StringBuilder uri = new StringBuilder();

		uri.append(listUrl(kind, namespace));

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

		return getResponse(httpClient.execute(HttpUtils.get(tokenInfo, uri.toString())));
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json json
	 * @return json
	 * @throws Exception exception
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {

		final String uri = updateStatusUrl(getKind(json), getNamespace(json), getName(json));

		return getResponse(httpClient.execute(HttpUtils.put(tokenInfo, uri, json.toString())));
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind    kind
	 * @param name    name
	 * @param watcher watcher
	 * @return thread
	 * @throws Exception exception
	 */
	public Thread watchResource(String kind, String name, KubernetesWatcher watcher) throws Exception {
		return watchResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name, watcher);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @param name      name
	 * @param watcher   watcher
	 * @return thread
	 * @throws Exception exception
	 */
	public Thread watchResource(String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {

		CloseableHttpClient cloneHttpClient = createDefaultHttpClient();
		watcher.setHttpClient(cloneHttpClient);
		watcher.setRequest(HttpUtils.get(tokenInfo, watchOneUrl(kind, namespace, name)));
		Thread thread = new Thread(watcher, kind.toLowerCase() + "-" + namespace + "-" + name);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind    kind
	 * @param watcher watcher
	 * @return thread
	 * @throws Exception exception
	 */
	public Thread watchResources(String kind, KubernetesWatcher watcher) throws Exception {
		return watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @param watcher   watcher
	 * @return thread
	 * @throws Exception exception
	 */
	public Thread watchResources(String kind, String namespace, KubernetesWatcher watcher) throws Exception {
		CloseableHttpClient cloneHttpClient = createDefaultHttpClient();
		watcher.setHttpClient(cloneHttpClient);
		watcher.setRequest(HttpUtils.get(tokenInfo, watchAllUrl(kind, namespace)));
		Thread thread = new Thread(watcher, kind.toLowerCase() + "-" + namespace);
		thread.start();
		return thread;
	}

	/**********************************************************
	 * 
	 * Request and Response
	 * 
	 **********************************************************/

	/**
	 * @param response response
	 * @return response
	 */
	protected synchronized JsonNode getResponse(CloseableHttpResponse response) {

		try {
			JsonNode result = new ObjectMapper().readTree(response.getEntity().getContent());
			if (result.has("status") && result.get("status").asText().equals("Failure")) {
				throw new Exception(result.get("status").get("message").asText());
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					m_logger.severe(e.toString());
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void close() {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @return httpClient
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * @return url
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * @return analyzer
	 */
	public KubernetesAnalyzer getAnalyzer() {
		return kubeAnalyzer;
	}

	/**********************************************************
	 * 
	 * Getter
	 * 
	 **********************************************************/

	/**
	 * @param json json
	 * @return kind
	 */
	public String getKind(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_KIND).asText();
	}

	/**
	 * @param json json
	 * @return name
	 */
	public String getName(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}

	/**
	 * @param json json
	 * @return full path
	 */
	public String getNamespace(JsonNode json) {
		JsonNode meta = json.get(KubernetesConstants.KUBE_METADATA);
		return meta.has(KubernetesConstants.KUBE_METADATA_NAMESPACE)
				? meta.get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText()
				: KubernetesConstants.VALUE_DEFAULT_NAMESPACE;

	}

	/**
	 * @param json json
	 * @return full path
	 */
	public String getApiVersion(JsonNode json) {
		return json.get("apiVersion").asText();

	}

	/**
	 * @return kinds
	 */
	public Collection<String> getKinds() {
		return getAnalyzer().getConfig().kindMapping.values();
	}

	/**
	 * @param key key [group.kind]
	 * @return kind
	 */
	public String getKind(String key) {
		return getMetaData().get(key).get("kind").asText();
	}

	/**
	 * @param key key
	 * @return apiVersion
	 */
	public String getLatestApiVersion(String key) {
		return getMetaData().get(key).get("apiVersion").asText();
	}

	/**
	 * @param key key
	 * @return plural
	 */
	public String getPlural(String key) {
		return getMetaData().get(key).get("plural").asText();
	}

	/*******************************************
	 * 
	 * knowledge-based Url
	 * 
	 ********************************************/
	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	protected String createUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);
		String uri = (version.indexOf("/") == -1) ? "api/" + version : "apis/" + version;

		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 ? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		return URLUtils.join(getMasterUrl(), uri, getNamespace(kubeConfig.isNamespaced(fullKind), getNamespace(json)),
				kubeConfig.getName(fullKind));
	}

	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	protected String bindingUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);
		String uri = (version.indexOf("/") == -1) ? "api/" + version : "apis/" + version;

		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 ? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		return URLUtils.join(getMasterUrl(), uri, getNamespace(kubeConfig.isNamespaced(fullKind), getNamespace(json)),
				"pods", json.get("metadata").get("name").asText(), "binding");
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	protected String deleteUrl(String kind, String ns, String name) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), getNamespace(kubeConfig.isNamespaced(fullKind), ns),
				kubeConfig.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	protected String updateUrl(String kind, String ns, String name) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), getNamespace(kubeConfig.isNamespaced(fullKind), ns),
				kubeConfig.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	protected String getUrl(String kind, String ns, String name) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), getNamespace(kubeConfig.isNamespaced(fullKind), ns),
				kubeConfig.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	protected String listUrl(String kind, String ns) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), getNamespace(kubeConfig.isNamespaced(fullKind), ns),
				kubeConfig.getName(fullKind));
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	protected String updateStatusUrl(String kind, String ns, String name) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), getNamespace(kubeConfig.isNamespaced(fullKind), ns),
				kubeConfig.getName(fullKind), name, KubernetesConstants.HTTP_RESPONSE_STATUS);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	protected String watchOneUrl(String kind, String ns, String name) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(kubeConfig.isNamespaced(fullKind), ns), kubeConfig.getName(fullKind), name,
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	protected String watchAllUrl(String kind, String ns) throws Exception {
		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();
		String fullKind = kind.indexOf(".") == -1 ? kubeAnalyzer.getFullKind(kind) : kind;
		return URLUtils.join(kubeConfig.getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(kubeConfig.isNamespaced(fullKind), ns), kubeConfig.getName(fullKind),
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}

	/**
	 * @param namespaced bool
	 * @param namespace  ns
	 * @return full path
	 */
	protected String getNamespace(boolean namespaced, String namespace) {
		return (namespaced && namespace != null && namespace.length() != 0)
				? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
				: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}

	/*******************************************
	 * 
	 * Utils
	 * 
	 ********************************************/

	/**
	 * @return json
	 */
	public JsonNode getMetaData() {

		ObjectNode map = new ObjectMapper().createObjectNode();

		KubernetesConfig kubeConfig = kubeAnalyzer.getConfig();

		for (String kind : kubeConfig.getNamespacedMapping().keySet()) {
			ObjectNode node = new ObjectMapper().createObjectNode();
			node.put("apiVersion", kubeConfig.versionMapping.get(kind));
			node.put("kind", kubeConfig.kindMapping.get(kind));
			node.put("plural", kubeConfig.nameMapping.get(kind));
			node.set("verbs", kubeConfig.verbsMapping.get(kind));

			map.set(kind, node);
		}

		return map;
	}
}
