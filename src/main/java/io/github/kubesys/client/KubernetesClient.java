/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
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
 *limitations under the License.
 */
package io.github.kubesys.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.github.kubesys.client.beans.KubernetesAdminConfig;
import io.github.kubesys.client.cores.KubernetesRuleBase;
import io.github.kubesys.client.exceptions.KubernetesBadRequestException;
import io.github.kubesys.client.exceptions.KubernetesConflictResourceException;
import io.github.kubesys.client.exceptions.KubernetesConnectionException;
import io.github.kubesys.client.exceptions.KubernetesForbiddenAccessException;
import io.github.kubesys.client.exceptions.KubernetesInternalServerErrorException;
import io.github.kubesys.client.exceptions.KubernetesResourceNotFoundException;
import io.github.kubesys.client.exceptions.KubernetesUnauthorizedTokenException;
import io.github.kubesys.client.exceptions.KubernetesUnknownException;
import io.github.kubesys.client.utils.KubeUtil;
import io.github.kubesys.client.utils.ReqUtil;
import io.github.kubesys.client.utils.SSLUtil;
import io.github.kubesys.client.utils.URLUtil;

/**
 * Kubernetes客户端，用于建立与Kubernetes的连接，随后可以对Kubernetes的Kind资源进行生命周期管理。
 * 其中，kind的定义参见https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/
 * <br>
 * <br>
 * Kubernetes客户端主要支持create、update、delete、get、list和watch5种语义，其设计参见
 * https://g-ubjg5602.coding.net/p/iscas-system/km/spaces/1326202/pages/K-28
 * <br>
 * <br>
 * 
 * @author wuheng@iscas.ac.cn
 * @since 1.0.0
 * 
 */
public class KubernetesClient {

	/**
	 * 日志对象
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());


	/**
	 * 用于连接api-server的配置文件，安装好Kubernetes后，
	 * 通常位于/etc/kubernetes/admin.conf 或者 /root/.kube/config
	 */
	protected KubernetesAdminConfig kubernetesAdminConfig;

	/**
	 * 用于自动分析Kubernetes中所有kind资源，以及该资源所对应的Url，
	 * 参见https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27 
	 * <br>
	 * <br>
	 * 如对于Pod，会至少学习以下的url<br>
	 * - create: POST /api/v1/namespaces/{namespace}/pods <br>
	 * - update: PUT /api/v1/namespaces/{namespace}/pods/{name} <br>
	 * - delete: DELETE /api/v1/namespaces/{namespace}/pods <br>
	 * - get: GET /api/v1/namespaces/{namespace}/pods /{name}<br>
	 * - list: GET /api/v1/pods <br>
	 * - watch: GET /api/v1/watch/pods <br>
	 */
	protected KubernetesAnalyzer analyzer;
	
	/**
	 * 连接Kubernetes的http客户端
	 */
	protected final CloseableHttpClient httpClient;

	/***************************************************************************
	 * 
	 * using config 
	 * 
	 ***************************************************************************/
	
	/**
	 * 根据配置文件创建Kubernetes客户端
	 * 
	 * @throws KubernetesConnectionException Kubernetes连接异常
	 */
	public KubernetesClient() throws KubernetesConnectionException {
		this(new File(KubernetesConstants.KUBE_CONFIG));
	}

	/**
	 * 根据配置文件创建Kubernetes客户端
	 * 
	 * @param file 比如$HOME$/.kube/conf
	 * @throws KubernetesConnectionException Kubernetes连接异常
	 */
	public KubernetesClient(File file) throws KubernetesConnectionException {
		this(file, new KubernetesAnalyzer());
	}

	
	/**
	 * 根据配置文件创建Kubernetes客户端
	 *  
	 * @param file     比如$HOME$/.kube/conf
	 * @param analyzer 用于自动分析Kubernetes中所有kind资源，以及该资源对应的所有Url
	 * @throws KubernetesConnectionException Kubernetes连接异常
	 * 
	 */
	public KubernetesClient(File file, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = KubernetesAdminConfig.from(new YAMLMapper().readTree(file));
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
			this.analyzer = analyzer.initIfNeed(this);
		} catch (Exception ex) {
			throw new KubernetesConnectionException(ex.toString());
		}
	}

	/***************************************************************************
	 * 
	 * using bearer token 
	 * 
	 ***************************************************************************/
	
	/**
	 * 根据token访问kubernetes
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url   如https://IP:6443/
	 * @param token bearer token, 通过ServiceAccount和ClusterRoleBinding进行创建
	 * @throws KubernetesConnectionException  KubernetesConnectionException
	 */
	public KubernetesClient(String url, String token) throws KubernetesConnectionException {
		this(url, token, new KubernetesAnalyzer());
	}

	/**
	 * 根据token访问kubernetes
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      如https://IP:6443/
	 * @param token    bearer token, 通过ServiceAccount和ClusterRoleBinding进行创建
	 * @param analyzer 用于自动分析Kubernetes中所有kind资源，以及该资源对应的Url
	 * @throws KubernetesConnectionException KubernetesConnectionException
	 */
	public KubernetesClient(String url, String token, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = new KubernetesAdminConfig(url, token);
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
			this.analyzer = analyzer.initIfNeed(this);
		} catch (Exception ex) {
			if (url == null || token == null) {
				throw new KubernetesConnectionException("missing parameters/environment variables  'url' or 'token'.");
			}
			throw new KubernetesConnectionException(ex.toString());
		} 
	}

	
	/***************************************************************************
	 * 
	 * using usename and password 
	 * 
	 ***************************************************************************/
	
	/**
	 * 根据用户名密码创建Kubernetes连接
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      如https://IP:6443/
	 * @param username basic authing
	 * @param password basic authing
	 * @throws KubernetesConnectionException KubernetesConnectionException
	 */
	public KubernetesClient(String url, String username, String password) throws KubernetesConnectionException {
		this(url, username, password, new KubernetesAnalyzer());
	}

	/**
	 * 根据用户名密码创建Kubernetes连接
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      default is https://IP:6443/
	 * @param username basic authing
	 * @param password basic authing
	 * @param analyzer 用于自动分析Kubernetes中所有kind资源，以及该资源对应的Url
	 * @throws KubernetesConnectionException KubernetesConnectionException
	 */
	public KubernetesClient(String url, String username, String password, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = new KubernetesAdminConfig(url, username, password);
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
			this.analyzer = analyzer.initIfNeed(this);
		} catch (Exception ex) {
			throw new KubernetesConnectionException(ex.toString());
		}
	}

	/**********************************************************
	 * 
	 * 
	 * 
	 * HttpClient
	 * 
	 * 
	 * 
	 **********************************************************/
	
	/**
	 * @param kac kac
	 * @return CloseableHttpClient
	 * @throws Exception Exception
	 */
	protected CloseableHttpClient createDefaultHttpClient(KubernetesAdminConfig kac) throws Exception {

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
				RegistryBuilder.<ConnectionSocketFactory>create()
				.register(URIScheme.HTTP.id, 
						PlainConnectionSocketFactory.getSocketFactory())
				.register(URIScheme.HTTPS.id,
						SSLUtil.createSocketFactory(
								kac.keyManagers(), 
								kac.trustManagers()))
				.build());
		
		connManager.setDefaultMaxPerRoute(10);
		connManager.setMaxTotal(20);
		
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ZERO_MILLISECONDS)
                .setSocketTimeout(Timeout.ZERO_MILLISECONDS)
                .setTimeToLive(TimeValue.ofDays(36500L))
                .build();
		
		connManager.setDefaultConnectionConfig(connectionConfig);
		
		@SuppressWarnings("deprecation")
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(Timeout.ZERO_MILLISECONDS)
				.setConnectionRequestTimeout(Timeout.ZERO_MILLISECONDS)
				.setResponseTimeout(Timeout.ZERO_MILLISECONDS)
				.setConnectionKeepAlive(TimeValue.ZERO_MILLISECONDS)
				.build();
		
		return HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connManager)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(
						10, TimeValue.ofSeconds(10)))
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {

					@Override
					public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
					    return TimeValue.ZERO_MILLISECONDS;
					}
					
				})
				.build();
	}
	
	
	/**
	 * https://appwrite.io/docs/advanced/platform/response-codes
	 * 
	 * 200 OK: 请求成功，服务器成功处理了请求并返回所请求的数据。 <br>
	 * 201 Created: 请求成功，服务器创建了新资源。 <br>
	 * 204 No Content: 请求成功，服务器处理成功，但没有返回数据。<br> 
	 * 400 Bad Request: 请求无效，服务器无法理解请求。 <br>
	 * 401 Unauthorized: 未授权，需要进行身份验证或令牌无效。 <br> 
	 * 403 Forbidden: 请求被拒绝，客户端没有访问资源的权限。 <br>
	 * 404 Not Found: 请求的资源不存在。 <br>
	 * 409 Conflict: 请求冲突，通常用于表示资源的当前状态与请求的条件不匹配。 <br> 
	 * 500 Internal Server Error: 服务器内部错误，表示服务器在处理请求时遇到了问题。 <br>
	 * 
	 * @param response response
	 * @return json json
	 */
	protected synchronized JsonNode parseResponse(CloseableHttpResponse response) {

		switch (response.getCode()) {
		case 200:
		case 201:
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				return objectMapper.readTree(response.getEntity().getContent());
			} catch (Exception e) {
				throw new KubernetesUnknownException(e.toString());
			}
		case 400:
			throw new KubernetesBadRequestException(convertToString(response));
		case 401:
			throw new KubernetesUnauthorizedTokenException(convertToString(response));
		case 403:
			throw new KubernetesForbiddenAccessException(convertToString(response));
		case 404:
			throw new KubernetesResourceNotFoundException(convertToString(response));
		case 409:
			throw new KubernetesConflictResourceException(convertToString(response));
		case 500:
			throw new KubernetesInternalServerErrorException(convertToString(response));
		default:
			throw new KubernetesUnknownException(convertToString(response));
		}

	}

	
	/**
	 * @param resp response
	 * @return string
	 */
	public static String convertToString(CloseableHttpResponse resp) {
		try {
	        ByteArrayOutputStream result = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = resp.getEntity().getContent().read(buffer)) != -1) {
	            result.write(buffer, 0, length);
	        }
	        return result.toString("UTF-8");
		} catch (IOException ex) {
			return ex.toString();
		}
    }
	
	/**
	 * @param req req
	 * @return json 
	 * @throws Exception exception
	 */
	@SuppressWarnings("deprecation")
	public synchronized JsonNode getResponse(HttpUriRequestBase req) throws Exception {
		return parseResponse(httpClient.execute(req));
	}

	/**
	 * @return httpClient
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
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
	
	/**********************************************************
	 * 
	 * 
	 * 
	 * Core APIs Create, Update, List, Get, Delete And Watch
	 * 
	 * 
	 * 
	 **********************************************************/

	/**
	 * create a Kubernetes resource using JSON. <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param obj json object, which must meet the Kubernetes' specification
	 * @return json Kubernetes may add come fields according to Kubernetes' context
	 * @throws Exception see link HttpCaller.getResponse
	 */
	public JsonNode createResourceByObject(Object obj) throws Exception {
		return createResource(new ObjectMapper().valueToTree(obj));
	}
	
	/**
	 * create a Kubernetes resource using JSON. <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json Kubernetes may add come fields according to Kubernetes' context
	 * @throws Exception see link HttpCaller.getResponse
	 */
	public JsonNode createResourceByJson(String json) throws Exception {
		return createResource(new ObjectMapper().readTree(json));
	}

	/**
	 * @param yaml yaml
	 * @return String
	 * @throws Exception Exception
	 */
	public String createResourceByYaml(String yaml) throws Exception {
		JsonNode jsonNode = KubeUtil.yamlStringToJsonNode(yaml);
		return KubeUtil.jsonNodeToYamlString(createResource(jsonNode));
	}
	
	/**
	 * create a Kubernetes resource using JSON. <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json Kubernetes may add come fields according to Kubernetes' context
	 * @throws Exception see link HttpCaller.getResponse
	 */
	public JsonNode createResource(JsonNode json) throws Exception {
		final String uri = analyzer.getConvertor().createUrl(json);
		HttpPost request = ReqUtil.post(kubernetesAdminConfig, uri, json.toString());
		return getResponse(request);
	}

	/**
	 * delete a Kubernetes resource using JSON <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param obj json object, which must meet the Kubernetes' specification
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResourceByObject(Object obj) throws Exception {
		return deleteResource(new ObjectMapper().valueToTree(obj));
	}
	
	/**
	 * delete a Kubernetes resource using JSON <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResourceByJson(String json) throws Exception {
		return deleteResource(new ObjectMapper().readTree(json));
	}

	/**
	 * @param yaml yaml
	 * @return string
	 * @throws Exception Exception
	 */
	public String deleteResourceByYaml(String yaml) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode jsonNode = mapper.readTree(yaml);
		return new YAMLMapper().writeValueAsString(deleteResource(jsonNode));
	}

	/**
	 * delete a Kubernetes resource using JSON <br>
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(JsonNode json) throws Exception {

		return deleteResourceByNamespaceAndName(
				analyzer.getConvertor().fullkind(json), 
				analyzer.getConvertor().namespace(json),
				analyzer.getConvertor().name(json));
	}

	/**
	 * delete a Kubernetes resource using kind and name
	 * 
	 * see https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param fullkind kind or fullKind
	 * @param name resource name
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResourceByName(String fullkind, String name) throws Exception {
		return deleteResourceByNamespaceAndName(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * delete a Kubernetes resource using kind, namespace and name see
	 * https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param fullkind      kind or fullKind
	 * @param namespace resource namespace, and "" means all-namespaces
	 * @param name      resource name
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResourceByNamespaceAndName(String fullkind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().deleteUrl(fullkind, namespace, name);
		HttpDelete request = ReqUtil.delete(kubernetesAdminConfig, uri);
		return getResponse(request);
	}


	
	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param obj json object, which must meet the Kubernetes' specification
	 * @return json updated object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode updateResourceByObject(Object obj) throws Exception {
		return updateResource(new ObjectMapper().valueToTree(obj));
	}
	
	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json updated object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode updateResourceByJson(String json) throws Exception {
		return updateResource(new ObjectMapper().readTree(json));
	}

	/**
	 * @param yaml yaml
	 * @return string
	 * @throws Exception Exception
	 */
	public String updateResourceByYaml(String yaml) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode jsonNode = mapper.readTree(yaml);
		return new YAMLMapper().writeValueAsString(updateResource(jsonNode));
	}

	/**
	 * update a Kubernetes resource using JSON
	 * 
	 * for example, a json can be <br>
	 * { <br>
	 * "apiVersion": "v1", <br>
	 * "kind": "Pod", <br>
	 * "metadata": { <br>
	 * "name": "busybox", <br>
	 * "namespace": "default", <br>
	 * "labels": { <br>
	 * "test": "test" <br>
	 * } <br>
	 * } <br>
	 * } <br>
	 * 
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json updated object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode updateResource(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateUrl(analyzer.getConvertor().fullkind(json),
				analyzer.getConvertor().namespace(json), analyzer.getConvertor().name(json));

		if (json.has(KubernetesConstants.KUBE_STATUS)) {
			((ObjectNode) json).remove(KubernetesConstants.KUBE_STATUS);
		}

		HttpPut request = ReqUtil.put(kubernetesAdminConfig, uri, json.toString());
		return getResponse(request);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param fullkind kind or fullkind
	 * @param name name
	 * @return json expected object with json style
	 * @throws Exception json object, which must meet the Kubernetes' specification
	 */
	public JsonNode getResourceByName(String fullkind, String name) throws Exception {

		return getResourceByNamespaceAndName(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}


	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param fullkind      kind or fullkind
	 * @param namespace namespace, if this kind unsupports namespace, it is ""
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode getResourceByNamespaceAndName(String fullkind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(fullkind, namespace, name);
		HttpGet request = ReqUtil.get(kubernetesAdminConfig, uri);
		return getResponse(request);
	}


	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param fullkind      kind
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public boolean hasResourceByName(String fullkind, String name) throws Exception {
		return hasResourceByNamespaceAndName(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}
	
	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace, if this kind unsupports namespace, it is null
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public boolean hasResourceByNamespaceAndName(String fullkind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(fullkind, namespace, name);
		try {
			HttpGet request = ReqUtil.get(kubernetesAdminConfig, uri);
			getResponse(request);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param fullkind kind
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResources(String fullkind) throws Exception {
		return listResources(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}


	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param fullkind   kind
	 * @param fields fields
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesByField(String fullkind, Map<String, String> fields) throws Exception {
		return listResources(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, URLUtil.fromMap(fields), null, 0, null);
	}


	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param fullkind   kind
	 * @param labels labels
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesByLabel(String fullkind, Map<String, String> labels) throws Exception {
		return listResources(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, URLUtil.fromMap(labels), 0, null);
	}

	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesByNamespace(String fullkind, String namespace) throws Exception {
		return listResources(fullkind, namespace, null, null, 0, null);
	}


	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param fields fields
	 * @return json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesByNamespaceAndField(String fullkind, String namespace, Map<String, String> fields) throws Exception {
		return listResources(fullkind, namespace, URLUtil.fromMap(fields), null, 0, null);
	}


	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param labels labels
	 * @return json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesByNamespaceAndLabel(String fullkind, String namespace, Map<String, String> labels) throws Exception {
		return listResources(fullkind, namespace, null, URLUtil.fromMap(labels), 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector and
	 * labelSelector
	 * 
	 * @param fullkind          kind
	 * @param namespace     namespace
	 * @param fields fieldSelector
	 * @param labels labelSelector
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResources(String fullkind, String namespace, Map<String, String> fields, Map<String, String> labels)
			throws Exception {

		return listResources(fullkind, namespace, URLUtil.fromMap(fields), URLUtil.fromMap(labels), 0, null);
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector,
	 * labelSelector, limit and nextId
	 * 
	 * @param fullkind          kind
	 * @param namespace     namespace
	 * @param fieldSelector fieldSelector
	 * @param labelSelector labelSelector
	 * @param limit         limit
	 * @param nextId        nextId
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	JsonNode listResources(String fullkind, String namespace, String fieldSelector, String labelSelector, 
			int limit, String nextId) throws Exception {

		StringBuilder uri = new StringBuilder();
		uri.append(analyzer.getConvertor().listUrl(fullkind, namespace));
		uri.append(KubernetesConstants.HTTP_QUERY_KIND + fullkind);

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

		HttpGet request = ReqUtil.get(kubernetesAdminConfig, uri.toString());
		return getResponse(request);
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param obj json
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode updateResourceStatusByObject(String obj) throws Exception {
		return updateResourceStatus(new ObjectMapper().readTree(obj));
	}
	
	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param obj json
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode updateResourceStatusByJson(String obj) throws Exception {
		return updateResourceStatus(new ObjectMapper().valueToTree(obj));
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param yaml yaml
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String updateResourceStatusByYaml(String yaml) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode jsonNode = mapper.readTree(yaml);
		return new YAMLMapper().writeValueAsString(updateResourceStatus(jsonNode));
	}
	
	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json json
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode updateResourceStatus(JsonNode json) throws Exception {

		final String uri = analyzer.getConvertor().updateStatusUrl(analyzer.getConvertor().kind(json),
				analyzer.getConvertor().namespace(json), analyzer.getConvertor().name(json));

		HttpPut request = ReqUtil.put(kubernetesAdminConfig, uri, json.toString());

		return getResponse(request);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param fullkind    kind
	 * @param name    name
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResourceByName(String fullkind, String name, KubernetesWatcher watcher) throws Exception {
		return watchResourceByNamespaceAndName(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, name, watcher);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param name      name
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResourceByNamespaceAndName(String fullkind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		String watchName = fullkind.toLowerCase() + "-"
				+ (namespace == null || "".equals(namespace) ? "all-namespaces" : namespace) + "-" + name;
		return watchResource(watchName, fullkind, namespace, name, watcher);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param watchName name
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param name      name
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	Thread watchResource(String watchName, String fullkind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		HttpGet request = ReqUtil.get(kubernetesAdminConfig, analyzer.getConvertor().watchOneUrl(fullkind, namespace, name));
		request.setHeader("Connection", "keep-alive");
		watcher.setRequest(request);
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param fullkind    kind
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResources(String fullkind, KubernetesWatcher watcher) throws Exception {
		return watchResourcesByFullkindAndNamespace(fullkind, KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResourcesByFullkindAndNamespace(String fullkind, String namespace, KubernetesWatcher watcher) throws Exception {
		String watchName = fullkind.toLowerCase() + "-"
				+ (namespace == null || "".equals(namespace) ? "all-namespaces" : namespace);
		return watchResources(watchName, fullkind, namespace, watcher);
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param watchName name
	 * @param fullkind      kind
	 * @param namespace namespace
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	Thread watchResources(String watchName, String fullkind, String namespace, KubernetesWatcher watcher)
			throws Exception {
		HttpGet request = ReqUtil.get(kubernetesAdminConfig, analyzer.getConvertor().watchAllUrl(fullkind, namespace));
		request.setHeader("Connection", "keep-alive");
		watcher.setRequest(request);
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * see https://kubernetes.io/docs/concepts/overview/working-with-objects/field-selectors/
	 * 
	 * e.g., kubectl get pods --field-selector=status.phase!=Running,spec.restartPolicy=Always
	 * 
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchPodsOnLocalNode(KubernetesWatcher watcher) throws Exception {
		String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
		HttpGet request = ReqUtil.get(kubernetesAdminConfig,
				analyzer.getConvertor().watchAllUrlWithFieldSelector("Pod", "", "spec.nodeName=" + hostname));
		request.setHeader("Connection", "keep-alive");
		watcher.setRequest(request);
		Thread thread = new Thread(watcher, "watch-pods-by-hostname");
		thread.start();
		return thread;
	}

	
	/**
	 * for Scheduler
	 * 
	 * { "apiVersion": "v1", "kind": "Binding", "metadata": { "name": "podName" },
	 * "target": { "apiVersion": "v1", "kind": "Node", "name": "hostName" } }
	 * 
	 * @param pod  pod
	 * @param namespace namespace
	 * @param host hostname
	 * @return json json from Kubernetes
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode bindingResource(String namespace, String pod,  String host) throws Exception {
		return createResource(KubeUtil.toBinding(namespace, pod, host));
	}
	
	
	/**
	 * {
     * "apiVersion": "apiextensions.k8s.io/v1",
     *  "kind": "CustomResourceDefinition",
     *  "metadata": {
     *   "name": "frontends.doslab.io"
     *},
     *"spec": {
     * "group": "doslab.io",
     *"names": {
     * "kind": "Frontend",
     * "plural": "frontends",
     * "shortNames": [
     *   "frontend"
     * ],
     * "singular": "frontend"
     *},
     *"scope": "Namespaced",
     *"versions": [
     *  {
     *   "name": "v1",
     *  "served": true,
     *  "storage": true,
     *  "schema": {
     *    "openAPIV3Schema": {
     *      "type": "object",
     *      "properties": {
     *        "spec": {
     *          "type": "object",
     *          "x-kubernetes-preserve-unknown-fields": true
     *        }
     *      }
     *    }
     *  }
     *}
     *]
     *}
     * }
	 * 
	 * @param group group
	 * @param kind  kind
	 * @param plural plural
	 * @return JSON
	 * @throws Exception Exception
	 */
	public JsonNode registerResource(String group, String kind, String plural) throws Exception {
		String json = KubernetesConstants.TEMP_CRD_KIND.replaceAll("#GROUP#", group)
												.replaceAll("#PLURAL#", plural).replaceAll("#KIND#", kind)
												.replaceAll("#LOWCASE_KIND#", kind.toLowerCase());
		return createResourceByJson(json);
	}
	
	/**
	 * @return json
	 * @throws Exception Exception
	 */
	public JsonNode createWebHook() throws Exception {
		return null;
	}
	
	/**********************************************************
	 * 
	 * 
	 * 
	 * Get
	 * 
	 * 
	 * 
	 **********************************************************/

	/**
	 * {
	"kind": "CustomResourceDefinition",
	"apiVersion": "apiextensions.k8s.io/v1",
	"metadata": {
		"name": "virtualmachines.doslab.io",
		"uid": "eac76c1c-254d-4895-b815-381755805da7",
		"resourceVersion": "10577917",
		"generation": 1,
		"creationTimestamp": "2023-10-21T08:23:57Z",
		"labels": {
			"kubevirt.io": ""
		},
		"annotations": {
			"kubectl.kubernetes.io/last-applied-configuration": "{\"apiVersion\":\"apiextensions.k8s.io/v1\",\"kind\":\"CustomResourceDefinition\",\"metadata\":{\"annotations\":{},\"labels\":{\"kubevirt.io\":\"\"},\"name\":\"virtualmachines.doslab.io\"},\"spec\":{\"group\":\"doslab.io\",\"names\":{\"kind\":\"VirtualMachine\",\"plural\":\"virtualmachines\",\"shortNames\":[\"vm\",\"vms\"],\"singular\":\"virtualmachine\"},\"scope\":\"Namespaced\",\"versions\":[{\"additionalPrinterColumns\":[{\"jsonPath\":\".spec.domain.uuid.text\",\"name\":\"UUID\",\"type\":\"string\"},{\"jsonPath\":\".spec.domain._type\",\"name\":\"TYPE\",\"type\":\"string\"},{\"jsonPath\":\".spec.domain.vcpu._current\",\"name\":\"CPU\",\"type\":\"string\"},{\"jsonPath\":\".spec.domain.currentMemory.text\",\"name\":\"RAM(KB)\",\"type\":\"string\"},{\"jsonPath\":\".spec.powerstate\",\"name\":\"STATUS\",\"type\":\"string\"},{\"jsonPath\":\".metadata.creationTimestamp\",\"name\":\"AGE\",\"type\":\"date\"},{\"jsonPath\":\".spec.nodeName\",\"name\":\"NODE\",\"type\":\"string\"},{\"jsonPath\":\".spec.status.conditions.state.waiting.reason\",\"name\":\"MESSAGE\",\"type\":\"string\"},{\"jsonPath\":\".spec.image\",\"name\":\"IMAGE\",\"type\":\"string\"}],\"name\":\"v1\",\"schema\":{\"openAPIV3Schema\":{\"properties\":{\"spec\":{\"type\":\"object\",\"x-kubernetes-preserve-unknown-fields\":true}},\"type\":\"object\"}},\"served\":true,\"storage\":true}]}}\n"
		},
		"spec": {
			"group": "doslab.io",
			"names": {
				"plural": "virtualmachines",
				"singular": "virtualmachine",
				"shortNames": ["vm", "vms"],
				"kind": "VirtualMachine",
				"listKind": "VirtualMachineList"
			},
			"scope": "Namespaced",
			"versions": [{
				"name": "v1",
				"served": true,
				"storage": true,
				"schema": {
					"openAPIV3Schema": {
						"type": "object",
						"properties": {
							"spec": {
								"type": "object",
								"x-kubernetes-preserve-unknown-fields": true
							}
						}
					}
				},
				"additionalPrinterColumns": [{
					"name": "UUID",
					"type": "string",
					"jsonPath": ".spec.domain.uuid.text"
				}, {
					"name": "TYPE",
					"type": "string",
					"jsonPath": ".spec.domain._type"
				}, {
					"name": "CPU",
					"type": "string",
					"jsonPath": ".spec.domain.vcpu._current"
				}, {
					"name": "RAM(KB)",
					"type": "string",
					"jsonPath": ".spec.domain.currentMemory.text"
				}, {
					"name": "STATUS",
					"type": "string",
					"jsonPath": ".spec.powerstate"
				}, {
					"name": "AGE",
					"type": "date",
					"jsonPath": ".metadata.creationTimestamp"
				}, {
					"name": "NODE",
					"type": "string",
					"jsonPath": ".spec.nodeName"
				}, {
					"name": "MESSAGE",
					"type": "string",
					"jsonPath": ".spec.status.conditions.state.waiting.reason"
				}, {
					"name": "IMAGE",
					"type": "string",
					"jsonPath": ".spec.image"
				}]
			}],
			"conversion": {
				"strategy": "None"
			}
		},
		"status": {
			"conditions": null,
			"acceptedNames": {
				"plural": "",
				"kind": ""
			},
			"storedVersions": ["v1"]
		}
	}
	 * 
	 * @param crd 上面就是一个CRD例子
	 * @return /apis/doslab.io/v1
	 * @throws Exception  Exception
	 */
	public JsonNode extractResource(JsonNode crd) throws Exception {
		if (crd.has("kind") && crd.get("kind").asText().equals("CustomResourceDefinition")) {
			StringBuilder sb = new StringBuilder();
			sb.append("/apis/").append(crd.get("spec").get("group").asText()).append("/")
				.append(crd.get("spec").get("versions").get(0).get("name").asText());
			getAnalyzer().getRegistry().registerKinds(this, sb.toString());
			return crd;
		}
		
		throw new KubernetesInternalServerErrorException("it is not a valid crd.");
	}
	
	/**
	 * @return kinds kind, see Kubernetes kind
	 * @throws Exception Kubernetes unavailability
	 */
	public JsonNode getKinds() throws Exception {
		return new ObjectMapper().readTree(new ObjectMapper()
				.writeValueAsString(getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.values()));
	}

	/**
	 * @return fullkinds fullkind = apiversion + "." + kind
	 * @throws Exception Kubernetes unavailability
	 */
	public JsonNode getFullKinds() throws Exception {
		return new ObjectMapper().readTree(new ObjectMapper()
				.writeValueAsString(getAnalyzer().getConvertor().getRuleBase().fullKindToKindMapper.keySet()));
	}
	
	/**
	 * @param kind kind
	 * @return fullkinds fullkind = apiversion + "." + kind
	 * @throws Exception Kubernetes unavailability
	 */
	public JsonNode getFullKind(String kind) throws Exception {
		return new ObjectMapper().readTree(new ObjectMapper()
				.writeValueAsString(getAnalyzer().getConvertor().getRuleBase().kindToFullKindMapper.get(kind)));
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

	/**
	 * 
	 * @return analyzer. it is used for getting the metadata for each Kubernetes
	 *         kind. With the metadata, we can create, update, delete, get, list and
	 *         watch it according to the description of [Kubernetes native
	 *         API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
	 */
	public KubernetesAnalyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @return httpCaller, it is used for sending request to Kuberenetes and
	 *         receiving response from Kubernetes.
	 */
	public KubernetesAdminConfig getKubernetesAdminConfig() {
		return kubernetesAdminConfig;
	}

	/**
	 * create a new HttpCaller for each WatchResource or WatchResources API
	 * 
	 * @return httpCaller
	 * @throws Exception exception
	 */
	CloseableHttpClient copy() throws Exception {
		KubernetesAdminConfig kac = (kubernetesAdminConfig.getToken() != null) 
				? new KubernetesAdminConfig(
						kubernetesAdminConfig.getMasterUrl(), 
						kubernetesAdminConfig.getToken()) 
				: new KubernetesAdminConfig(
						kubernetesAdminConfig.getMasterUrl(), 
						kubernetesAdminConfig.getCaCertData(), 
						kubernetesAdminConfig.getClientCertData(),
						kubernetesAdminConfig.getClientKeyData());
		return createDefaultHttpClient(kac);
	}

}
