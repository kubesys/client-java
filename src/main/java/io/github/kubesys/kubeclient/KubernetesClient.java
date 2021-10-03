/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import io.github.kubesys.kubeclient.utils.ReqUtil;
import io.github.kubesys.kubeclient.utils.SSLUtil;

/**
 * <p>
 * Providing a unified API to create, update, delete, get, list and watch Kubernetes' kinds
 * according to Kubernetes' APIs.
 * <ul>
 * <li><p>
 *     Kubernetes kinds: https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/
 * <li><p>
 *     Kubernetes APIs: https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/
 * </ul>
 * fullKind = "group" + "." + kind
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 */
public class KubernetesClient {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());
	
	
	/**
	 * all-namespaces
	 */
	public static final String ALL_NAMESPACES = "";
	
	
	/**
	 * caller: it is used for sending request to Kuberenetes and receiving response from Kubernetes. 
	 */
	protected final HttpCaller httpCaller;
	
	/**
	 * analyzer: it is used for getting the metadata for each Kubernetes kind.
	 * With the metadata, we can create, update, delete, get, list and watch it 
	 * according to the description of [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)    
	 */
	protected final KubernetesAnalyzer analyzer;


	/**
	 * invoke Kubernetes without token, which has been deprecated after Kubernetes 1.18
	 * 
	 * @param  url                 default is https://IP:6443/
	 * @throws Exception           exception
	 */
	public KubernetesClient(String url) throws Exception {
		this(url, null);
	}

	/**
	 * invoke Kubernetes using token,see https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url                   default is https://IP:6443/
	 * @param token                 bearer token, you can create it using ServiceAccount and ClusterRoleBinding
	 * @throws Exception            exception
	 */
	public KubernetesClient(String url, String token) throws Exception {
		this(url, token, new KubernetesAnalyzer());
		this.analyzer.analyseServerBy(this);
	}

	/**
	 * invoke Kubernetes using token, see https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * 
	 * @param url                   default is https://IP:6443/
	 * @param token                 bearer token, you can create it using ServiceAccount and ClusterRoleBinding
	 * @param analyzer              it is used for getting the metadata for each Kubernetes kind.
	 */
	public KubernetesClient(String url, String token, KubernetesAnalyzer analyzer) {
		this.httpCaller = new HttpCaller(url, token);
		this.analyzer = analyzer;
	}


	/**********************************************************
	 * 
	 *                     APIs
	 * Create, Update, List, Get, Delete And Watch
	 * 
	 **********************************************************/

	/**
	 * create a Kubernetes resource using JSON. <br>
	 * 
	 * for example, a json can be                        <br>
	 * {                                       <br>
	 *   "apiVersion": "v1",                   <br>
	 *   "kind": "Pod",                        <br>
	 *   "metadata": {                         <br>
	 *      "name": "busybox",                 <br>
	 *      "namespace": "default",            <br>
	 *      "labels": {                        <br>
	 *        "test": "test"                   <br>
	 *     }                                   <br>
	 *   }                                     <br>
	 * }                                       <br>
	 * 
	 * @param json                   json object, which must meet the Kubernetes' specification
	 * @return json                  Kubernetes may add come fields according to Kubernetes' context 
	 * @throws Exception             see link HttpCaller.getResponse
	 */
	public JsonNode createResource(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().createUrl(json);
		
		HttpPost request = ReqUtil.post(
						httpCaller.getToken(), 
						uri, json.toString());
		
		return httpCaller.getResponse(request);
	}

	/**
	 * delete a Kubernetes resource using JSON <br>
	 * 
	 * for example, a json can be                        <br>
	 * {                                       <br>
	 *   "apiVersion": "v1",                   <br>
	 *   "kind": "Pod",                        <br>
	 *   "metadata": {                         <br>
	 *      "name": "busybox",                 <br>
	 *      "namespace": "default",            <br>
	 *      "labels": {                        <br>
	 *        "test": "test"                   <br>
	 *     }                                   <br>
	 *   }                                     <br>
	 * }                                       <br>
	 * 
	 * @param json                   json object, which must meet the Kubernetes' specification
	 * @return json                  the deleted object with json style
	 * @throws Exception             see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(JsonNode json) throws Exception {
		
		return deleteResource(analyzer.getConvertor().fullkind(json), 
							  analyzer.getConvertor().namespace(json), 
							  analyzer.getConvertor().name(json));
	}
	
	/**
	 * delete a Kubernetes resource using kind and name
	 * 
	 * see https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param kind                    kind or fullKind
	 * @param name                    resource name
	 * @return json                   the deleted object with json style
	 * @throws Exception              see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(String kind, String name) throws Exception {
		return deleteResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}


	/**
	 * delete a Kubernetes resource using kind, namespace and name
	 * see https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param kind                    kind or fullKind
	 * @param namespace               resource namespace, and "" means all-namespaces
	 * @param name                    resource name
	 * @return json                   the deleted object with json style
	 * @throws Exception              see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().deleteUrl(kind, namespace, name);

		HttpDelete request = ReqUtil.delete(httpCaller.token, uri);
		
		return httpCaller.getResponse(request);
	}

	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * for example, a json can be                        <br>
	 * {                                       <br>
	 *   "apiVersion": "v1",                   <br>
	 *   "kind": "Pod",                        <br>
	 *   "metadata": {                         <br>
	 *      "name": "busybox",                 <br>
	 *      "namespace": "default",            <br>
	 *      "labels": {                        <br>
	 *        "test": "test"                   <br>
	 *     }                                   <br>
	 *   }                                     <br>
	 * }                                       <br>
	 * 
	 * @param json                   json object, which must meet the Kubernetes' specification
	 * @return json                  updated object with json style
	 * @throws Exception             see HttpCaller.getResponse
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateUrl(
							 	analyzer.getConvertor().fullkind(json), 
							 	analyzer.getConvertor().namespace(json), 
							 	analyzer.getConvertor().name(json));

		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			((ObjectNode) json).remove(KubernetesConstants.KUBE_STATUS);
		}

		HttpPut request = ReqUtil.put(
						httpCaller.getToken(), 
						uri, json.toString());
		
		return httpCaller.getResponse(request);
	}


	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                        kind or fullkind
	 * @param name                        name
	 * @return json                       expected object with json style
	 * @throws Exception                  json object, which must meet the Kubernetes' specification 
	 */
	public JsonNode getResource(String kind, String name) throws Exception {

		return getResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                        kind or fullkind
	 * @param namespace                   namespace, if this kind unsupports namespace, it is ""
	 * @param name                        name
	 * @return json                       json
	 * @throws Exception                  Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);

		HttpGet request = ReqUtil.get(httpCaller.getToken(), uri);
		
		return httpCaller.getResponse(request);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind                         kind
	 * @param namespace                    namespace, if this kind unsupports namespace, it is null
	 * @param name                         name
	 * @return json                        json
	 * @throws Exception                   Kubernetes cannot parsing this jsonStr
	 */
	public boolean hasResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);

		try {
			HttpGet request = ReqUtil.get(httpCaller.getToken(), uri);
			httpCaller.getResponse(request);
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
	 * @throws Exception                    Kubernetes cannot parsing this jsonStr 
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
	 * @throws Exception                    Kubernetes cannot parsing this jsonStr
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
	 * @throws Exception                    Kubernetes cannot parsing this jsonStr
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
	 * @throws Exception                     Kubernetes cannot parsing this jsonStr
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

		HttpGet request = ReqUtil.get(httpCaller.getToken(), uri.toString());
		
		return httpCaller.getResponse(request);
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json                          json
	 * @return json                         json
	 * @throws Exception                    Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateStatusUrl(analyzer.getConvertor().kind(json), 
							analyzer.getConvertor().namespace(json), 
							analyzer.getConvertor().name(json));

		HttpPut request = ReqUtil.put(
				httpCaller.getToken(), 
				uri, json.toString());
		
		return httpCaller.getResponse(request);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param kind                           kind
	 * @param name                           name
	 * @param watcher                        watcher
	 * @return thread                        thread
	 * @throws Exception                     Kubernetes cannot parsing this jsonStr 
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
	 * @throws Exception                      Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResource(String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		String watchName = kind.toLowerCase() + "-" + (namespace == null || "".equals(namespace) 
							? "all-namespaces" : namespace) + "-" + name;
		return watchResource(watchName, kind, namespace, name, watcher);
	}
	
	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param watchName                       name
	 * @param kind                            kind
	 * @param namespace                       namespace
	 * @param name                            name
	 * @param watcher                         watcher
	 * @return thread                         thread
	 * @throws Exception                      Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResource(String watchName, String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		watcher.setRequest(ReqUtil.get(httpCaller.getToken(), analyzer.getConvertor().watchOneUrl(kind, namespace, name)));
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind                            kind
	 * @param watcher                         watcher
	 * @return thread                         thread
	 * @throws Exception                      Kubernetes cannot parsing this jsonStr
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
	 * @throws Exception                       Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResources(String kind, String namespace, KubernetesWatcher watcher) throws Exception {
		String watchName = kind.toLowerCase() + "-" 
								+ (namespace == null || "".equals(namespace) 
								? "all-namespaces" : namespace);
		return watchResources(watchName, kind, namespace, watcher);
	}
	
	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param watchName                        name
	 * @param kind                             kind
	 * @param namespace                        namespace
	 * @param watcher                          watcher
	 * @return thread                          thread
	 * @throws Exception                       Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResources(String watchName, String kind, String namespace, KubernetesWatcher watcher) throws Exception {
		watcher.setRequest(ReqUtil.get(httpCaller.getToken(), analyzer.getConvertor().watchAllUrl(kind, namespace)));
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * for Scheduler
	 * 
	 * { "apiVersion": "v1", "kind": "Binding", "metadata": { "name": "podName" },
	 * "target": { "apiVersion": "v1", "kind": "Node", "name": "hostName" } }
	 * 
	 * @param pod                        pod json
	 * @param host                       hostname
	 * @return json                      json from Kubernetes
	 * @throws Exception                 Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode bindingResource(JsonNode pod, String host) throws Exception {

		ObjectNode binding = new ObjectMapper().createObjectNode();
		binding.put(KubernetesConstants.KUBE_APIVERSION, "v1");
		binding.put(KubernetesConstants.KUBE_KIND, "Binding");
		
		ObjectNode metadata = new ObjectMapper().createObjectNode();
		metadata.put(KubernetesConstants.KUBE_METADATA_NAME, pod.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAME).asText());
		metadata.put(KubernetesConstants.KUBE_METADATA_NAMESPACE, pod.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText());
		binding.set(KubernetesConstants.KUBE_METADATA, metadata);
		
		ObjectNode target = new ObjectMapper().createObjectNode();
		target.put(KubernetesConstants.KUBE_APIVERSION, "v1");
		target.put(KubernetesConstants.KUBE_KIND, "Node");
		target.put(KubernetesConstants.KUBE_METADATA_NAME, host);
		binding.set("target", target);
			
		return createResource(binding);
	}
	
	/**
	 * @return kinds           kind, see Kubernetes kind
	 * @throws Exception       Kubernetes unavailability
	 */
	public JsonNode getKinds() throws Exception {
		return new ObjectMapper().readTree(
				new ObjectMapper().writeValueAsString(
						getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.values()));
	}
	
	/**
	 * @return fullkinds       fullkind = apiversion + "." + kind
	 * @throws Exception       Kubernetes unavailability
	 */
	public JsonNode getFullKinds() throws Exception {
		return new ObjectMapper().readTree(
				new ObjectMapper().writeValueAsString(
						getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.keySet()));
	}


	/**
	 * @return json, which includes kind, apiversion, supported operators
	 */
	public JsonNode getKindDesc() {

		ObjectNode map = new ObjectMapper().createObjectNode();
		KubernetesRuleBase ruleBase = analyzer.getConvertor().getRuleBase();
		
		for (String kind : ruleBase.fullKindToNamespacedMapper.keySet()) {
			ObjectNode node = new ObjectMapper().createObjectNode();
			node.put(KubernetesConstants.KUBE_APIVERSION, ruleBase.fullKindToVersionMapper.get(kind));
			node.put(KubernetesConstants.KUBE_KIND, ruleBase.fullKindToKindMapper.get(kind));
			node.put(KubernetesConstants.KUBE_SPEC_NAMES_PLURAL, ruleBase.fullKindToNameMapper.get(kind));
			node.set(KubernetesConstants.KUBE_SPEC_NAMES_VERBS, ruleBase.fullKindToVerbsMapper.get(kind));

			map.set(kind, node);
		}

		return map;
	}
	

	/**********************************************************
	 * 
	 * Getter
	 * 
	 **********************************************************/
	
	/**
	 * 
	 * @return analyzer. it is used for getting the metadata for each Kubernetes kind.
	 * With the metadata, we can create, update, delete, get, list and watch it 
	 * according to the description of [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)    
	 */
	public KubernetesAnalyzer getAnalyzer() {
		return analyzer;
	}


	/**
	 * @return httpCaller, it is used for sending request to Kuberenetes and receiving response from Kubernetes.
	 */
	public HttpCaller getHttpCaller() {
		return httpCaller;
	}
	
	/**
	 * create a new HttpCaller for each WatchResource or WatchResources API
	 * 
	 * @return               httpCaller
	 */
	public HttpCaller copy() {
		return new HttpCaller(httpCaller);
	}
	

	/**
	 * @author wuheng@iscas.ac.cn
	 *
	 */
	public static class HttpCaller {
		
		// https://www.oreilly.com/library/view/managing-kubernetes/9781492033905/ch04.html
		static Map<Integer, String> statusDesc = new HashMap<>();
		
		static  {
			statusDesc.put(400, "Bad Request. The server could not parse or understand the request.");
			statusDesc.put(401, "Unauthorized. A request was received without a known authentication scheme.");
			statusDesc.put(403, "Bad Request. Forbidden. The request was received and understood, but access is forbidden.");
			statusDesc.put(409, "Conflict. The request was received, but it was a request to update an older version of the object.");
			statusDesc.put(422, "Unprocessable entity. The request was parsed correctly but failed some sort of validation.");
		}
		
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
				builder.setSSLHostnameVerifier(SSLUtil.createDefaultHostnameVerifier())
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
					int code = result.get("code").asInt();
					String cause = statusDesc.get(code);
					throw new Exception(cause != null ? cause : result.toPrettyString());
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
					m_logger.warning(e.toString());
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
