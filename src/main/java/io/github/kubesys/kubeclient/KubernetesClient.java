/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
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

import io.github.kubesys.kubeclient.core.KubernetesRuleBase;
import io.github.kubesys.kubeclient.utils.HttpUtil;
import io.github.kubesys.kubeclient.utils.SSLUtil;

/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources]
 * (https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
 * 
 */
public class KubernetesClient {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());

	
	/**
	 * caller
	 */
	protected final HttpCaller caller;
	
	/**
	 * analyzer
	 */
	protected final KubernetesAnalyzer analyzer;


	/**
	 * @param  url                 url
	 * @throws Exception           exception 
	 */
	public KubernetesClient(String url) throws Exception {
		this(url, null);
	}

	/**
	 * @param url                   url
	 * @param token                 token
	 * @throws Exception            exception
	 */
	public KubernetesClient(String url, String token) throws Exception {
		this.caller = new HttpCaller(url, token);
		this.analyzer = new KubernetesAnalyzer();
		this.analyzer.doStart(this);
	}

	/**
	 * @param url                   url
	 * @param token                 token
	 * @param analyzer              analyzer
	 * @throws Exception            exception
	 */
	public KubernetesClient(String url, String token, KubernetesAnalyzer analyzer) throws Exception {
		super();
		this.caller = new HttpCaller(url, token);
		this.analyzer = analyzer;
		this.analyzer.doStart(this);
	}


	/**********************************************************
	 * 
	 * Core
	 * 
	 **********************************************************/

	/**
	 * create a Kubernetes resource using JSON
	 * 
	 * @param jsonStr               jsonStr
	 * @return json                 json
	 * @throws Exception            exception
	 */
	public JsonNode createResource(String jsonStr) throws Exception {
		return createResource(new ObjectMapper().readTree(jsonStr));
	}

	/**
	 * create a Kubernetes resource using JSON
	 * 
	 * @param json                   json
	 * @return json                  json
	 * @throws Exception             exception
	 */
	public JsonNode createResource(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().createUrl(json);
		
		HttpPost request = HttpUtil.post(
						caller.getToken(), 
						uri, json.toString());
		
		return caller.getResponse(request);
	}


	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param json                   json
	 * @return json                  json
	 * @throws Exception             exception
	 */
	public JsonNode deleteResource(JsonNode json) throws Exception {
		
		return deleteResource(analyzer.getConvertor().getFullKind(json), 
							  analyzer.getConvertor().getNamespace(json), 
							  analyzer.getConvertor().getName(json));
	}
	
	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param kind                    kind
	 * @param name                    name
	 * @return json                   json
	 * @throws Exception              exception
	 */
	public JsonNode deleteResource(String kind, String name) throws Exception {
		return deleteResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}


	/**
	 * delete a Kubernetes resource using JSON
	 * 
	 * @param kind                    kind
	 * @param namespace               namespace
	 * @param name                    name
	 * @return json                   json
	 * @throws Exception              exception
	 */
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().deleteUrl(kind, namespace, name);

		HttpDelete request = HttpUtil.delete(caller.token, uri);
		
		return caller.getResponse(request);
	}

	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * @param json                     json
	 * @return                         json
	 * @throws Exception               exception
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateUrl(analyzer.getConvertor().getFullKind(json), 
										      analyzer.getConvertor().getNamespace(json), 
										      analyzer.getConvertor().getName(json));

		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			((ObjectNode) json).remove(KubernetesConstants.KUBE_STATUS);
		}

		HttpPut request = HttpUtil.put(
						caller.getToken(), 
						uri, json.toString());
		
		return caller.getResponse(request);
	}

	/**
	 * binding a Kubernetes resource using JSON
	 * 
	 * { "apiVersion": "v1", "kind": "Binding", "metadata": { "name": "podName" },
	 * "target": { "apiVersion": "v1", "kind": "Node", "name": "hostName" } }
	 * 
	 * @param pod                        pod
	 * @param host                       host
	 * @return json                      json
	 * @throws Exception                 exception
	 */
	public JsonNode bindingResource(JsonNode pod, String host) throws Exception {

		ObjectNode binding = new ObjectMapper().createObjectNode();
		binding.put("apiVersion", "v1");
		binding.put("kind", "Binding");
		
		ObjectNode metadata = new ObjectMapper().createObjectNode();
		metadata.put("name", pod.get("metadata").get("name").asText());
		metadata.put("namespace", pod.get("metadata").get("namespace").asText());
		binding.set("metadata", metadata);
		
		ObjectNode target = new ObjectMapper().createObjectNode();
		target.put("apiVersion", "v1");
		target.put("kind", "Node");
		target.put("name", host);
		binding.set("target", target);
			
		return createResource(binding);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                        kind
	 * @param name                        name
	 * @return json                       json
	 * @throws Exception                  exception 
	 */
	public JsonNode getResource(String kind, String name) throws Exception {

		return getResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                        kind
	 * @param namespace                   namespace, if this kind unsupports namespace, it is null
	 * @param name                        name
	 * @return json                       json
	 * @throws Exception                  exception
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);

		HttpGet request = HttpUtil.get(caller.getToken(), uri);
		
		return caller.getResponse(request);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                         kind
	 * @param namespace                    namespace, if this kind unsupports namespace, it is null
	 * @param name                         name
	 * @return json                        json
	 * @throws Exception                   exception
	 */
	public boolean hasResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);

		try {
			HttpGet request = HttpUtil.get(caller.getToken(), uri);
			caller.getResponse(request);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind                          kind
	 * @return json                         json
	 * @throws Exception                    exception 
	 */
	public JsonNode listResources(String kind) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param kind                          kind
	 * @param namespace                     namespace
	 * @return json                         json
	 * @throws Exception                    exception
	 */
	public JsonNode listResources(String kind, String namespace) throws Exception {
		return listResources(kind, namespace, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector and
	 * labelSelector
	 * 
	 * @param kind                          kind
	 * @param namespace                     namespace
	 * @param fieldSelector                 fieldSelector
	 * @param labelSelector                 labelSelector
	 * @return json                         json
	 * @throws Exception                    exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector)
			throws Exception {
		return listResources(kind, namespace, fieldSelector, labelSelector, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector,
	 * labelSelector, limit and nextId
	 * 
	 * @param kind                           kind
	 * @param namespace                      namespace
	 * @param fieldSelector                  fieldSelector
	 * @param labelSelector                  labelSelector
	 * @param limit                          limit
	 * @param nextId                         nextId
	 * @return json                          json
	 * @throws Exception                     exception
	 */
	public JsonNode listResources(String kind, String namespace, String fieldSelector, String labelSelector, int limit,
			String nextId) throws Exception {
		StringBuilder uri = new StringBuilder();

		uri.append(analyzer.getConvertor().listUrl(kind, namespace));

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

		HttpGet request = HttpUtil.get(caller.getToken(), uri.toString());
		
		return caller.getResponse(request);
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json                          json
	 * @return json                         json
	 * @throws Exception                    exception
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateStatusUrl(analyzer.getConvertor().getKind(json), 
							analyzer.getConvertor().getNamespace(json), 
							analyzer.getConvertor().getName(json));

		HttpPut request = HttpUtil.put(
				caller.getToken(), 
				uri, json.toString());
		
		return caller.getResponse(request);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind                           kind
	 * @param name                           name
	 * @param watcher                        watcher
	 * @return thread                        thread
	 * @throws Exception                     exception 
	 */
	public Thread watchResource(String kind, String name, KubernetesWatcher watcher) throws Exception {
		return watchResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name, watcher);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @param name                            name
	 * @param watcher                         watcher
	 * @return thread                         thread
	 * @throws Exception                      exception
	 */
	public Thread watchResource(String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		watcher.setRequest(HttpUtil.get(caller.getToken(), analyzer.getConvertor().watchOneUrl(kind, namespace, name)));
		Thread thread = new Thread(watcher, kind.toLowerCase() + "-" + (namespace == null || "".equals("") 
							? "all-namespaces" : namespace) + "-" + name);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind                            kind
	 * @param watcher                         watcher
	 * @return thread                         thread
	 * @throws Exception                      exception
	 */
	public Thread watchResources(String kind, KubernetesWatcher watcher) throws Exception {
		return watchResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind                             kind
	 * @param namespace                        namespace
	 * @param watcher                          watcher
	 * @return thread                          thread
	 * @throws Exception                       exception
	 */
	public Thread watchResources(String kind, String namespace, KubernetesWatcher watcher) throws Exception {
		watcher.setRequest(HttpUtil.get(caller.getToken(), analyzer.getConvertor().watchAllUrl(kind, namespace)));
		Thread thread = new Thread(watcher, kind.toLowerCase() + "-" 
								+ (namespace == null || "".equals("") 
								? "all-namespaces" : namespace));
		thread.start();
		return thread;
	}


	/**
	 * @return analyzer
	 */
	public KubernetesAnalyzer getAnalyzer() {
		return analyzer;
	}


	/**
	 * @return               httpCaller
	 */
	public HttpCaller getHttpCaller() {
		return caller;
	}
	
	/**
	 * @return               httpCaller
	 */
	public HttpCaller clone() {
		return new HttpCaller(caller);
	}
	
	/**
	 * @return kinds           kinds
	 * @throws Exception       exception
	 */
	public JsonNode getKinds() throws Exception {
		return new ObjectMapper().readTree(
				new ObjectMapper().writeValueAsString(
						getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.values()));
	}
	
	/**
	 * @return fullkinds       fullkinds
	 * @throws Exception       execption
	 */
	public JsonNode getFullKinds() throws Exception {
		return new ObjectMapper().readTree(
				new ObjectMapper().writeValueAsString(
						getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.keySet()));
	}


	/**
	 * @return json
	 */
	public JsonNode getKindDesc() {

		ObjectNode map = new ObjectMapper().createObjectNode();
		KubernetesRuleBase ruleBase = analyzer.getConvertor().getRuleBase();
		
		for (String kind : ruleBase.fullKindToNamespacedMapper.keySet()) {
			ObjectNode node = new ObjectMapper().createObjectNode();
			node.put("apiVersion", ruleBase.fullKindToVersionMapper.get(kind));
			node.put("kind", ruleBase.fullKindToKindMapper.get(kind));
			node.put("plural", ruleBase.fullKindToNameMapper.get(kind));
			node.set("verbs", ruleBase.fullKindToVerbsMapper.get(kind));

			map.set(kind, node);
		}

		return map;
	}
	
	

	/**
	 * @author wuheng@iscas.ac.cn
	 *
	 */
	public static class HttpCaller {
		
		/**
		 * master IP
		 */
		protected String masterUrl;

		/**
		 * token
		 */
		protected String token;

		/**
		 * client
		 */
		protected final CloseableHttpClient httpClient;

		
		/**
		 * @param caller       caller
		 */
		public HttpCaller(HttpCaller caller) {
			this(caller.getMasterUrl(), caller.getToken());
		}
		/**
		 * @param masterUrl    masterUrl    
		 * @param token        token
		 */
		public HttpCaller(String masterUrl, String token) {
			super();
			this.masterUrl = masterUrl;
			this.token = token;
			this.httpClient = createDefaultHttpClient();
		}
		
		/**
		 * @return httpClient
		 */
		protected CloseableHttpClient createDefaultHttpClient() {

			SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true)
							.setSoTimeout(0).setSoReuseAddress(true).build();

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(0)
							.setConnectionRequestTimeout(0).setSocketTimeout(0).build();

			return createDefaultHttpClientBuilder()
					.setConnectionTimeToLive(0, TimeUnit.SECONDS)
					.setDefaultSocketConfig(socketConfig)
					.setDefaultRequestConfig(requestConfig)
					.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
					.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
					.setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy()).build();
		}

		/**
		 * @return builder
		 */
		protected HttpClientBuilder createDefaultHttpClientBuilder() {
			HttpClientBuilder builder = HttpClients.custom();

			if (this.token != null) {
				builder.setSSLHostnameVerifier(SSLUtil.createHostnameVerifier())
						.setSSLSocketFactory(SSLUtil.createSocketFactory());
			}

			return builder;
		}

		/**
		 * @param response        response
		 * @return json           json 
		 */
		protected synchronized JsonNode parseResponse(CloseableHttpResponse response) {

			try {
				JsonNode result = new ObjectMapper().readTree(response.getEntity().getContent());
				if (result.has("status") && result.get("status").asText().equals("Failure")) {
					throw new Exception(result.toPrettyString());
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
		 * @param  req             req
		 * @return json            json 
		 * @throws Exception       exception
		 */
		public synchronized JsonNode getResponse(HttpRequestBase req) throws Exception {
			return parseResponse(httpClient.execute(req)); 
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
		 * @return   masterUrl
		 */
		public String getMasterUrl() {
			return masterUrl;
		}

		/**
		 * @return   token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * @return   httpClient
		 */
		public CloseableHttpClient getHttpClient() {
			return httpClient;
		}
		
	}
}
