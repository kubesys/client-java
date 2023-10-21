/*
  Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.github.kubesys.client.annotations.Api;
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
 * Kubernetes客户端
 * 根据
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/的
 * URL规则生产URL
 * 
 * 对于JSON参数，可参见https://kubernetes.io/docs/reference/kubernetes-api/
 * 
 * @author wuheng@iscas.ac.cn
 * @since 1.0.0
 * 
 */
public class KubernetesClient {

	/**
	 * 日志
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());


	/**
	 * 用于连接Kubernetes api-server的配置文件，安装好Kubernetes后，
	 * 通常位于/etc/kubernetes/admin.conf 或者 /root/.kube/config
	 */
	protected KubernetesAdminConfig kubernetesAdminConfig;

	/**
	 * it is used for getting the metadata of all kinds in Kubernetes according to
	 * [Kubernetes API pattern]
	 * (https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/)
	 */
	protected KubernetesAnalyzer analyzer;
	
	/**
	 * client
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
	 * @throws KubernetesConnectionException
	 */
	public KubernetesClient() throws KubernetesConnectionException {
		this(new File(KubernetesConstants.KUBE_CONFIG));
	}

	/**
	 * 根据配置文件创建Kubernetes客户端
	 * 
	 * @param file 比如$HOME$/.kube/conf
	 * @throws KubernetesConnectionException
	 */
	public KubernetesClient(File file) throws KubernetesConnectionException {
		this(file, new KubernetesAnalyzer());
	}

	
	/**
	 * invoke Kubernetes using x509
	 * 
	 * @param file     file
	 * @param analyzer it is used for getting the metadata for each Kubernetes kind.
	 * @throws KubernetesConnectionException
	 */
	public KubernetesClient(File file, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = new KubernetesAdminConfig(new YAMLMapper().readTree(file));
			this.analyzer = analyzer.initIfNeed(this);
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
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
	 * invoke Kubernetes using token,see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url   default is https://IP:6443/
	 * @param token bearer token, you can create it using ServiceAccount and
	 *              ClusterRoleBinding
	 * @throws KubernetesConnectionException 
	 */
	public KubernetesClient(String url, String token) throws KubernetesConnectionException {
		this(url, token, new KubernetesAnalyzer());
	}

	/**
	 * invoke Kubernetes using token, see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      default is https://IP:6443/
	 * @param token    bearer token, you can create it using ServiceAccount and
	 *                 ClusterRoleBinding
	 * @param analyzer it is used for getting the metadata for each Kubernetes kind.
	 * @throws KubernetesConnectionException 
	 */
	public KubernetesClient(String url, String token, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = new KubernetesAdminConfig(url, token);
			this.analyzer = analyzer.initIfNeed(this);
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
		} catch (Exception ex) {
			throw new KubernetesConnectionException(ex.toString());
		} 
	}

	
	/***************************************************************************
	 * 
	 * using usename and password 
	 * 
	 ***************************************************************************/
	
	/**
	 * invoke Kubernetes using token,see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      default is https://IP:6443/
	 * @param username basic authing
	 * @param password basic authing
	 * @throws KubernetesConnectionException 
	 */
	public KubernetesClient(String url, String username, String password) throws KubernetesConnectionException {
		this(url, username, password, new KubernetesAnalyzer());
	}

	/**
	 * invoke Kubernetes using token, see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      default is https://IP:6443/
	 * @param username basic authing
	 * @param password basic authing
	 * @param analyzer it is used for getting the metadata for each Kubernetes kind.
	 * @throws KubernetesConnectionException 
	 */
	public KubernetesClient(String url, String username, String password, KubernetesAnalyzer analyzer) throws KubernetesConnectionException {
		try {
			this.kubernetesAdminConfig = new KubernetesAdminConfig(url, username, password);
			this.analyzer = analyzer.initIfNeed(this);
			this.httpClient = createDefaultHttpClient(kubernetesAdminConfig);
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
	// https://www.oreilly.com/library/view/managing-kubernetes/9781492033905/ch04.html
	static Map<Integer, String> statusDesc = new HashMap<>();

	static {
		statusDesc.put(400, "Bad Request. The server could not parse or understand the request.");
		statusDesc.put(401, "Unauthorized. A request was received without a known authentication scheme.");
		statusDesc.put(403,
				"Bad Request. Forbidden. The request was received and understood, but access is forbidden.");
		statusDesc.put(409,
				"Conflict. The request was received, but it was a request to update an older version of the object.");
		statusDesc.put(422,
				"Unprocessable entity. The request was parsed correctly but failed some sort of validation.");
	}
		
	/**
	 * @return httpClient
	 * @throws Exception
	 */
	protected CloseableHttpClient createDefaultHttpClient(KubernetesAdminConfig kac)
			throws Exception {

		@SuppressWarnings("deprecation")
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Timeout.DISABLED)
				.setConnectionKeepAlive(Timeout.DISABLED).setConnectionRequestTimeout(Timeout.DISABLED)
				.setResponseTimeout(Timeout.DISABLED).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setConnectionManager(
						new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
								.register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory())
								.register(URIScheme.HTTPS.id,
										SSLUtil.createSocketFactory(
												kac.keyManagers(), 
												kac.trustManagers()))
								.build()))
				.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy()).build();
	}
	
	/**
	 * 200 OK: 请求成功，服务器成功处理了请求并返回所请求的数据。 201 Created: 请求成功，服务器创建了新资源。 204 No
	 * Content: 请求成功，服务器处理成功，但没有返回数据。 400 Bad Request: 请求无效，服务器无法理解请求。 401
	 * Unauthorized: 未授权，需要进行身份验证或令牌无效。 403 Forbidden: 请求被拒绝，客户端没有访问资源的权限。 404 Not
	 * Found: 请求的资源不存在。 409 Conflict: 请求冲突，通常用于表示资源的当前状态与请求的条件不匹配。 500 Internal
	 * Server Error: 服务器内部错误，表示服务器在处理请求时遇到了问题。
	 * 
	 * @param response response
	 * @return json json
	 */
	protected synchronized JsonNode parseResponse(CloseableHttpResponse response) {

		switch (response.getCode()) {
		case 200:
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				return objectMapper.readTree(response.getEntity().getContent());
			} catch (Exception e) {
				throw new KubernetesUnknownException(e.toString());
			}
		case 201:
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				return objectMapper.readTree(response.getEntity().getContent());
			} catch (Exception e) {
				throw new KubernetesUnknownException(e.toString());
			}
		case 400:
			throw new KubernetesBadRequestException(response.toString());
		case 401:
			throw new KubernetesUnauthorizedTokenException(response.toString());
		case 403:
			throw new KubernetesForbiddenAccessException(response.toString());
		case 404:
			throw new KubernetesResourceNotFoundException(response.toString());
		case 409:
			throw new KubernetesConflictResourceException(response.toString());
		case 500:
			throw new KubernetesInternalServerErrorException(response.toString());
		default:
			throw new KubernetesUnknownException(response.toString());
		}

	}

	/**
	 * @param req req
	 * @return json json
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
	 * @param json json object, which must meet the Kubernetes' specification
	 * @return json Kubernetes may add come fields according to Kubernetes' context
	 * @throws Exception see link HttpCaller.getResponse
	 */
	@Api(description = "通过Json字符串创建Kubernetes资源", date = "2023/1007/21", exceptions = {})
	public JsonNode createResource(String json) throws Exception {
		return createResource(new ObjectMapper().readTree(json));
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
	 * @param yaml yaml
	 * @return String
	 * @throws Exception Exception
	 */
	@Api(description = "通过YAML文件创建Kubernetes资源", date = "2023/07/25", exceptions = {})
	public String createResourceUsingYaml(String yaml) throws Exception {
		JsonNode jsonNode = KubeUtil.yamlStringToJsonNode(yaml);
		return KubeUtil.jsonNodeToYamlString(createResource(jsonNode));
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
	public JsonNode deleteResource(String json) throws Exception {
		return deleteResource(new ObjectMapper().readTree(json));
	}

	/**
	 * @param yaml yaml
	 * @return string
	 * @throws Exception Exception
	 */
	public String deleteResourceUsingYaml(String yaml) throws Exception {
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

		return deleteResource(analyzer.getConvertor().fullkind(json), analyzer.getConvertor().namespace(json),
				analyzer.getConvertor().name(json));
	}

	/**
	 * delete a Kubernetes resource using kind and name
	 * 
	 * see https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param kind kind or fullKind
	 * @param name resource name
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(String kind, String name) throws Exception {
		return deleteResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * delete a Kubernetes resource using kind, namespace and name see
	 * https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param kind      kind or fullKind
	 * @param namespace resource namespace, and "" means all-namespaces
	 * @param name      resource name
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public JsonNode deleteResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().deleteUrl(kind, namespace, name);
		HttpDelete request = ReqUtil.delete(kubernetesAdminConfig, uri);
		return getResponse(request);
	}

	/**
	 * delete a Kubernetes resource using kind, namespace and name see
	 * https://kubernetes.io/docs/reference/kubectl/overview/
	 * 
	 * @param kind      kind or fullKind
	 * @param namespace resource namespace, and "" means all-namespaces
	 * @param name      resource name
	 * @return json the deleted object with json style
	 * @throws Exception see HttpCaller.getResponse
	 */
	public String deleteResourceUsingYaml(String kind, String namespace, String name) throws Exception {
		return new YAMLMapper().writeValueAsString(deleteResource(kind, namespace, name));
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
	public JsonNode updateResource(String json) throws Exception {
		return updateResource(new ObjectMapper().readTree(json));
	}

	/**
	 * @param yaml yaml
	 * @return string
	 * @throws Exception Exception
	 */
	public String updateResourceUsingYaml(String yaml) throws Exception {
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
	 * @param kind kind or fullkind
	 * @param name name
	 * @return json expected object with json style
	 * @throws Exception json object, which must meet the Kubernetes' specification
	 */
	public JsonNode getResource(String kind, String name) throws Exception {

		return getResource(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, name);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind kind or fullkind
	 * @param name name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String getResourceUsingYaml(String kind, String name) throws Exception {
		return new YAMLMapper().writeValueAsString(getResource(kind, name));
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind      kind or fullkind
	 * @param namespace namespace, if this kind unsupports namespace, it is ""
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode getResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);
		HttpGet request = ReqUtil.get(kubernetesAdminConfig, uri);
		return getResponse(request);
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind      kind or fullkind
	 * @param namespace namespace, if this kind unsupports namespace, it is ""
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String getResourceUsingYaml(String kind, String namespace, String name) throws Exception {
		return new YAMLMapper().writeValueAsString(getResource(kind, namespace, name));
	}

	/**
	 * get a Kubernetes resource using kind, namespace and name
	 * 
	 * @param kind      kind
	 * @param namespace namespace, if this kind unsupports namespace, it is null
	 * @param name      name
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public boolean hasResource(String kind, String namespace, String name) throws Exception {

		final String uri = analyzer.getConvertor().getUrl(kind, namespace, name);
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
	 * @param kind kind
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResources(String kind) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind kind
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesUsingYaml(String kind) throws Exception {
		return new YAMLMapper()
				.writeValueAsString(listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, null, 0, null));
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind   kind
	 * @param fields fields
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesWithField(String kind, Map<String, String> fields) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, URLUtil.fromMap(fields), null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind   kind
	 * @param fields fields
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesWithFieldUsingYaml(String kind, Map<String, String> fields) throws Exception {
		return new YAMLMapper().writeValueAsString(
				listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, URLUtil.fromMap(fields), null, 0, null));
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind   kind
	 * @param labels labels
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesWithLabel(String kind, Map<String, String> labels) throws Exception {
		return listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, URLUtil.fromMap(labels), 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind   kind
	 * @param labels labels
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesWithLabelUsingYaml(String kind, Map<String, String> labels) throws Exception {
		return new YAMLMapper().writeValueAsString(
				listResources(kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null, URLUtil.fromMap(labels), 0, null));
	}

	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResources(String kind, String namespace) throws Exception {
		return listResources(kind, namespace, null, null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesUsingYaml(String kind, String namespace) throws Exception {
		return new YAMLMapper().writeValueAsString(listResources(kind, namespace, null, null, 0, null));
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @return fields fields
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesWithField(String kind, String namespace, Map<String, String> fields) throws Exception {
		return listResources(kind, namespace, URLUtil.fromMap(fields), null, 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @param fields    fields
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesWithFieldUsingYaml(String kind, String namespace, Map<String, String> fields)
			throws Exception {
		return new YAMLMapper()
				.writeValueAsString(listResources(kind, namespace, URLUtil.fromMap(fields), null, 0, null));
	}

	/**
	 * list all Kubernetes resources using kind and namespace
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @return labels labels
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode listResourcesWithLabel(String kind, String namespace, Map<String, String> labels) throws Exception {
		return listResources(kind, namespace, null, URLUtil.fromMap(labels), 0, null);
	}

	/**
	 * list all Kubernetes resources using kind
	 * 
	 * @param kind      kind
	 * @param namespace namespace
	 * @param labels    labels
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesWithLabelUsingYaml(String kind, String namespace, Map<String, String> labels)
			throws Exception {
		return new YAMLMapper()
				.writeValueAsString(listResources(kind, namespace, null, URLUtil.fromMap(labels), 0, null));
	}

	/**
	 * list all Kubernetes resources using kind, namespace, fieldSelector and
	 * labelSelector
	 * 
	 * @param kind          kind
	 * @param namespace     namespace
	 * @param fieldSelector fieldSelector
	 * @param labelSelector labelSelector
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
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
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
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

		HttpGet request = ReqUtil.get(kubernetesAdminConfig, uri.toString());
		return getResponse(request);
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
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public String listResourcesUsingYaml(String kind, String namespace, String fieldSelector, String labelSelector,
			int limit, String nextId) throws Exception {
		return new YAMLMapper()
				.writeValueAsString(listResources(kind, namespace, fieldSelector, labelSelector, limit, nextId));
	}

	/**
	 * update a Kubernetes resource status using JSON
	 * 
	 * @param json json
	 * @return json json
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public JsonNode updateResourceStatus(String json) throws Exception {
		return updateResourceStatus(new ObjectMapper().readTree(json));
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
	 * @param kind    kind
	 * @param name    name
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
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
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResource(String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		String watchName = kind.toLowerCase() + "-"
				+ (namespace == null || "".equals(namespace) ? "all-namespaces" : namespace) + "-" + name;
		return watchResource(watchName, kind, namespace, name, watcher);
	}

	/**
	 * watch a Kubernetes resource using kind, namespace, name and WebSocketListener
	 * 
	 * @param watchName name
	 * @param kind      kind
	 * @param namespace namespace
	 * @param name      name
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResource(String watchName, String kind, String namespace, String name, KubernetesWatcher watcher)
			throws Exception {
		watcher.setRequest(ReqUtil.get(kubernetesAdminConfig, analyzer.getConvertor().watchOneUrl(kind, namespace, name)));
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param kind    kind
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
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
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResources(String kind, String namespace, KubernetesWatcher watcher) throws Exception {
		String watchName = kind.toLowerCase() + "-"
				+ (namespace == null || "".equals(namespace) ? "all-namespaces" : namespace);
		return watchResources(watchName, kind, namespace, watcher);
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param watchName name
	 * @param kind      kind
	 * @param namespace namespace
	 * @param watcher   watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchResources(String watchName, String kind, String namespace, KubernetesWatcher watcher)
			throws Exception {
		watcher.setRequest(ReqUtil.get(kubernetesAdminConfig, analyzer.getConvertor().watchAllUrl(kind, namespace)));
		Thread thread = new Thread(watcher, watchName);
		thread.start();
		return thread;
	}

	/**
	 * watch a Kubernetes resources using kind, namespace, and WebSocketListener
	 * 
	 * @param watcher watcher
	 * @return thread thread
	 * @throws Exception Kubernetes cannot parsing this jsonStr
	 */
	public Thread watchPodsOnLocalNode(KubernetesWatcher watcher) throws Exception {
		String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
		watcher.setRequest(ReqUtil.get(kubernetesAdminConfig,
				analyzer.getConvertor().watchAllUrlWithFieldSelector("Pod", "", "spec.nodeName=" + hostname)));
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
	public JsonNode bindingResource(String pod, String namespace, String host) throws Exception {
		return createResource(KubeUtil.toBinding(pod, namespace, host));
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
	public CloseableHttpClient copy() throws Exception {
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
