/*
  Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.github.kubesys.client.annotations.Api;
import io.github.kubesys.client.cores.KubernetesRuleBase;
import io.github.kubesys.client.exceptions.KubernetesBadRequestException;
import io.github.kubesys.client.exceptions.KubernetesConflictResourceException;
import io.github.kubesys.client.exceptions.KubernetesForbiddenAccessException;
import io.github.kubesys.client.exceptions.KubernetesInternalServerErrorException;
import io.github.kubesys.client.exceptions.KubernetesResourceNotFoundException;
import io.github.kubesys.client.exceptions.KubernetesUnauthorizedTokenException;
import io.github.kubesys.client.exceptions.KubernetesUnknownException;
import io.github.kubesys.client.exceptions.KubernetesUnknownUrlException;
import io.github.kubesys.client.utils.ReqUtil;
import io.github.kubesys.client.utils.SSLUtil;
import io.github.kubesys.client.utils.URLUtil;

/**
 * Kubernetes的客户端，根据https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/的
 * URL规则生产URL
 * 
 * 对于JSON参数，可参见https://kubernetes.io/docs/reference/kubernetes-api/
 * 
 * @author wuheng@iscas.ac.cn
 * @since 2.0.0
 * 
 */
public class KubernetesClient {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesClient.class.getName());

	/**
	 * exceptions
	 */
	public static final String KUBE_CONNECTION_ERROR = "unable to connect to Kubernetes, ";

	/**
	 * it is used for sending requests to Kuberenetes kube-apiserver, and then
	 * receiving response from it.
	 */
	protected BaseRequestConfig requester;

	/**
	 * it is used for getting the metadata of all kinds in Kubernetes according to
	 * [Kubernetes API pattern]
	 * (https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/)
	 */
	protected KubernetesAnalyzer analyzer;

	/**
	 * for the Pods running on the leader nodes
	 * 
	 */
	public KubernetesClient() {
		this(new File(KubernetesConstants.KUBE_CONFIG));
	}

	/**
	 * for the Pods running on all nodes
	 * 
	 * @param file such as $HOME$/.kube/conf
	 */
	public KubernetesClient(File file) {
		this(file, new KubernetesAnalyzer());
	}

	/**
	 * invoke Kubernetes using x509
	 * 
	 * @param file     file
	 * @param analyzer it is used for getting the metadata for each Kubernetes kind.
	 * 
	 *                 TODO 各种异常被准确描述并抛出
	 */
	public KubernetesClient(File file, KubernetesAnalyzer analyzer) {
		try {
			this.requester = new BaseRequestConfig(new YAMLMapper().readTree(file));
			this.analyzer = analyzer.initIfNeed(this);
		} catch (Exception e) {
			m_logger.severe(KUBE_CONNECTION_ERROR + e);
			System.exit(1);
		}
	}

	/**
	 * invoke Kubernetes using token,see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url   default is https://IP:6443/
	 * @param token bearer token, you can create it using ServiceAccount and
	 *              ClusterRoleBinding
	 * @throws Exception 
	 */
	public KubernetesClient(String url, String token) throws Exception {
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
	 * @throws Exception 
	 */
	public KubernetesClient(String url, String token, KubernetesAnalyzer analyzer) throws Exception {
		try {
			this.requester = new BaseRequestConfig(url, token);
			this.analyzer = analyzer.initIfNeed(this);
		} catch (MalformedURLException ex) {
			throw new KubernetesUnknownUrlException(KUBE_CONNECTION_ERROR + ex);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * invoke Kubernetes using token,see
	 * https://kubernetes.io/docs/reference/access-authn-authz/authentication/
	 * 
	 * @param url      default is https://IP:6443/
	 * @param username basic authing
	 * @param password basic authing
	 */
	public KubernetesClient(String url, String username, String password) {
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
	 * 
	 *                 TODO 各种异常被准确描述并抛出
	 */
	public KubernetesClient(String url, String username, String password, KubernetesAnalyzer analyzer) {
		try {
			this.requester = new BaseRequestConfig(url, username, password);
			this.analyzer = analyzer.initIfNeed(this);
		} catch (Exception e) {
			m_logger.severe(KUBE_CONNECTION_ERROR + e);
			System.exit(1);
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
	 * @param yaml yaml
	 * @return String
	 * @throws Exception Exception
	 */
	@Api(description = "通过YAML文件创建Kubernetes资源", date = "2023/07/25", exceptions = {})
	public String createResourceUsingYaml(String yaml) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode jsonNode = mapper.readTree(yaml);
		return new YAMLMapper().writeValueAsString(createResource(jsonNode));
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
		HttpPost request = ReqUtil.post(requester, uri, json.toString());
		return requester.getResponse(request);
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
		HttpDelete request = ReqUtil.delete(requester, uri);
		return requester.getResponse(request);
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

		HttpPut request = ReqUtil.put(requester, uri, json.toString());
		return requester.getResponse(request);
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
		HttpGet request = ReqUtil.get(requester, uri);
		return requester.getResponse(request);
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
			HttpGet request = ReqUtil.get(requester, uri);
			requester.getResponse(request);
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
	public String listResourcesUsingYamml(String kind) throws Exception {
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

		HttpGet request = ReqUtil.get(requester, uri.toString());
		return requester.getResponse(request);
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

		HttpPut request = ReqUtil.put(requester, uri, json.toString());

		return requester.getResponse(request);
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
		watcher.setRequest(ReqUtil.get(requester, analyzer.getConvertor().watchOneUrl(kind, namespace, name)));
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
		watcher.setRequest(ReqUtil.get(requester, analyzer.getConvertor().watchAllUrl(kind, namespace)));
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
		watcher.setRequest(ReqUtil.get(requester,
				analyzer.getConvertor().watchAllUrlWithFieldSelector("Pod", "", "spec.nodeName=" + hostname)));
		Thread thread = new Thread(watcher, "watch-pods-by-hostname");
		thread.start();
		return thread;
	}

	/**********************************************************
	 * 
	 * 
	 * 
	 * Deprecated APIs
	 * 
	 * 
	 * 
	 **********************************************************/
	/**
	 * see webhook-service.yaml
	 * 
	 * @param hookName hookName
	 * @param path     path
	 * @param servName servName
	 * @param ns       ns
	 * @param labels   labels
	 * @param rules    rules
	 * @return MutatingWebhookConfiguration
	 * @throws Exception exception
	 */
	@Deprecated
	public JsonNode createWebhook(String hookName, String path, String servName, String ns, Map<String, String> labels,
			KubeRule[] rules) throws Exception {
		ObjectNode webhookConfig = createWebHookConfig(hookName, path, servName, ns, null, labels, rules);
		return createResource(webhookConfig);
	}

	/**
	 * see webhook-url.yaml
	 * 
	 * @param hookName hookName
	 * @param path     path
	 * @param url      url
	 * @param labels   labels
	 * @param rules    rules
	 * @return MutatingWebhookConfiguration
	 * @throws Exception exception
	 */
	@Deprecated
	public JsonNode createWebhook(String hookName, String path, String url, Map<String, String> labels,
			KubeRule[] rules) throws Exception {
		ObjectNode webhookConfig = createWebHookConfig(hookName, path, null, null, url, labels, rules);
		return createResource(webhookConfig);
	}

	/**
	 * @param hookName hookName
	 * @param path     path
	 * @param servName name
	 * @param ns       namespace
	 * @param url      url
	 * @param labels   labels
	 * @param rules    rules
	 * @return JsonNode
	 * @throws Exception exception
	 */
	@Deprecated
	protected ObjectNode createWebHookConfig(String hookName, String path, String servName, String ns, String url,
			Map<String, String> labels, KubeRule[] rules) throws Exception {
		ObjectNode webhookConfig = new ObjectMapper().createObjectNode();
		webhookConfig.put("apiVersion", "admissionregistration.k8s.io/v1");
		webhookConfig.put("kind", "MutatingWebhookConfiguration");
		webhookConfig.set("metadata", createMetadata(hookName));

		ArrayNode webhooks = new ObjectMapper().createArrayNode();

		ObjectNode webhook = new ObjectMapper().createObjectNode();
		webhook.put("name", hookName);
		webhook.set("objectSelector", createExpressions(labels));
		webhook.set("rules", new ObjectMapper().readTree(new ObjectMapper().writeValueAsBytes(rules)));
		webhook.set("clientConfig", createClientConfig(path, servName, ns, url));
		webhook.set("admissionReviewVersions", createReviewVersions());
		webhook.put("sideEffects", "None");
		webhook.put("timeoutSeconds", 10);

		webhooks.add(webhook);
		webhookConfig.set("webhooks", webhooks);
		return webhookConfig;
	}

	/**
	 * @return arrayNode
	 */
	@Deprecated
	protected ArrayNode createReviewVersions() {
		ArrayNode versions = new ObjectMapper().createArrayNode();
		versions.add("v1");
		versions.add("v1beta1");
		return versions;
	}

	/**
	 * @param path    path
	 * @param serName name
	 * @param ns      ns
	 * @param url     url
	 * @return JsonNode
	 * @throws Exception exception
	 */
	@Deprecated
	protected ObjectNode createClientConfig(String path, String serName, String ns, String url) throws Exception {
		ObjectNode config = new ObjectMapper().createObjectNode();
		JsonNode json = getResource("ConfigMap", "kube-system", "extension-apiserver-authentication");
		String cert = json.get("data").get("client-ca-file").asText();
		config.put("caBundle", Base64.getEncoder().encodeToString(cert.getBytes()));
		if (serName != null) {
			ObjectNode serv = new ObjectMapper().createObjectNode();
			serv.put("namespace", ns);
			serv.put("name", serName);
			serv.put("path", path);
			config.set("service", serv);
		} else {
			config.put("url", url + "/" + path);
		}
		return config;
	}

	/**
	 * @param name name
	 * @return json
	 */
	@Deprecated
	protected ObjectNode createMetadata(String name) {
		ObjectNode meta = new ObjectMapper().createObjectNode();
		meta.put("name", name);
		return meta;
	}

	/**
	 * @param labels labels
	 * @return JSON
	 */
	@Deprecated
	protected ObjectNode createExpressions(Map<String, String> labels) {
		ObjectNode match = new ObjectMapper().createObjectNode();

		ArrayNode exps = new ObjectMapper().createArrayNode();
		for (String key : labels.keySet()) {
			ObjectNode exp = new ObjectMapper().createObjectNode();

			exp.put("key", key);
			exp.put("operator", "In");

			ArrayNode values = new ObjectMapper().createArrayNode();
			values.add(labels.get(key));

			exp.set("values", values);
			exps.add(exp);
		}
		match.set("matchExpressions", exps);

		return match;
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
	public BaseRequestConfig getRequester() {
		return requester;
	}

	/**
	 * create a new HttpCaller for each WatchResource or WatchResources API
	 * 
	 * @return httpCaller
	 * @throws Exception exception
	 */
	public BaseRequestConfig copy() throws Exception {
		if (requester.getToken() != null) {
			return new BaseRequestConfig(requester.getMasterUrl(), requester.getToken());
		}
		return new BaseRequestConfig(requester.getMasterUrl(), requester.getCaCertData(), requester.getClientCertData(),
				requester.getClientKeyData());
	}

	/**********************************************************
	 * 
	 * 
	 * 
	 * Models
	 * 
	 * 
	 * 
	 **********************************************************/

	public static class KubeRule {

		protected String[] apiGroups;

		protected String[] apiVersions;

		protected String[] operations;

		protected String[] resources;

		protected String scope;

		public String[] getApiGroups() {
			return apiGroups;
		}

		public void setApiGroups(String[] apiGroups) {
			this.apiGroups = apiGroups;
		}

		public String[] getApiVersions() {
			return apiVersions;
		}

		public void setApiVersions(String[] apiVersions) {
			this.apiVersions = apiVersions;
		}

		public String[] getOperations() {
			return operations;
		}

		public void setOperations(String[] operations) {
			this.operations = operations;
		}

		public String[] getResources() {
			return resources;
		}

		public void setResources(String[] resources) {
			this.resources = resources;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

	}

	/**
	 * Http Requester
	 * 
	 * @author wuheng@iscas.ac.cn
	 * @since 2.0.0
	 *
	 */
	public static class BaseRequestConfig {

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
		 * master IP
		 */
		protected String masterUrl;

		/**
		 * token
		 */
		protected String token;

		/**
		 * username
		 */
		protected String username;

		/**
		 * password
		 */
		protected String password;

		/**
		 * caCertData
		 */
		protected String caCertData;

		/**
		 * clientCertData
		 */
		protected String clientCertData;

		/**
		 * clientKeyData
		 */
		protected String clientKeyData;

		/**
		 * client
		 */
		protected final CloseableHttpClient httpClient;

		/**
		 * @param requester requester
		 * @throws Exception exception
		 */
		public BaseRequestConfig(BaseRequestConfig requester) throws Exception {
			this(requester.getMasterUrl(), requester.getToken());
		}

		/**
		 * @param masterUrl masterUrl
		 * @param token     token
		 * @throws Exception exception
		 */
		public BaseRequestConfig(String masterUrl, String token) throws Exception {
			super();
			this.masterUrl = masterUrl;
			this.token = token;
			this.httpClient = createDefaultHttpClient();
		}

		/**
		 * @param masterUrl masterUrl
		 * @param username  username
		 * @param password  password
		 * @throws Exception exception
		 */
		public BaseRequestConfig(String masterUrl, String username, String password) throws Exception {
			super();
			this.masterUrl = masterUrl;
			this.username = username;
			this.password = password;
			this.httpClient = createDefaultHttpClient();
		}

		/**
		 * @param masterUrl      masterUrl
		 * @param caCertData     caCertData
		 * @param clientCertData clientCertData
		 * @param clientKeyData  clientKeyData
		 * @throws Exception
		 */
		public BaseRequestConfig(String masterUrl, String caCertData, String clientCertData, String clientKeyData)
				throws Exception {
			this.masterUrl = masterUrl;
			this.caCertData = caCertData;
			this.clientCertData = clientCertData;
			this.clientKeyData = clientKeyData;
			this.httpClient = createDefaultHttpClient();
		}

		/**
		 * @param json json
		 * @throws Exception
		 */
		public BaseRequestConfig(JsonNode json) throws Exception {
			JsonNode cluster = json.get("clusters").get(0).get("cluster");
			this.masterUrl = cluster.get("server").asText();
			this.caCertData = cluster.get("certificate-authority-data").asText();
			JsonNode user = json.get("users").get(0).get("user");
			this.clientCertData = user.get("client-certificate-data").asText();
			this.clientKeyData = user.get("client-key-data").asText();
			this.httpClient = createDefaultHttpClient();
		}

		/**
		 * @return httpClient
		 * @throws Exception
		 */
		protected CloseableHttpClient createDefaultHttpClient() throws Exception {

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
											SSLUtil.createSocketFactory(keyManagers(), trustManagers()))
									.build()))
					.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy()).build();
		}

		public String getCaCertData() {
			return caCertData;
		}

		public void setCaCertData(String caCertData) {
			this.caCertData = caCertData;
		}

		public String getClientCertData() {
			return clientCertData;
		}

		public void setClientCertData(String clientCertData) {
			this.clientCertData = clientCertData;
		}

		public String getClientKeyData() {
			return clientKeyData;
		}

		public void setClientKeyData(String clientKeyData) {
			this.clientKeyData = clientKeyData;
		}

		public void setMasterUrl(String masterUrl) {
			this.masterUrl = masterUrl;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public static Map<Integer, String> getStatusDesc() {
			return statusDesc;
		}

		public static void setStatusDesc(Map<Integer, String> statusDesc) {
			BaseRequestConfig.statusDesc = statusDesc;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		/**********************************************************************
		 * 
		 * I do not known why, just copy from fabric8
		 * 
		 **********************************************************************/

		public KeyManager[] keyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException,
				CertificateException, InvalidKeySpecException, IOException {
			if (this.clientCertData == null || this.clientKeyData == null) {
				return null;
			}
			KeyManager[] keyManagers = null;
			char[] passphrase = "changeit".toCharArray();
			KeyStore keyStore = createKeyStore(createInputStreamFromBase64EncodedString(this.clientCertData),
					createInputStreamFromBase64EncodedString(this.clientKeyData), passphrase);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, passphrase);
			keyManagers = kmf.getKeyManagers();
			return keyManagers;
		}

		public KeyStore createKeyStore(InputStream certInputStream, InputStream keyInputStream,
				char[] clientKeyPassphrase) throws IOException, CertificateException, NoSuchAlgorithmException,
				InvalidKeySpecException, KeyStoreException {
			CertificateFactory certFactory = CertificateFactory.getInstance("X509");
			Collection<? extends Certificate> certificates = certFactory.generateCertificates(certInputStream);
			PrivateKey privateKey = handleOtherKeys(keyInputStream, "RSA");

			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

			keyStore.load(null);
			String alias = certificates.stream()
					.map(cert -> ((X509Certificate) cert).getIssuerX500Principal().getName())
					.collect(Collectors.joining("_"));
			keyStore.setKeyEntry(alias, privateKey, clientKeyPassphrase, certificates.toArray(new Certificate[0]));

			return keyStore;
		}

		protected byte[] decodePem(InputStream keyInputStream) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(keyInputStream));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("-----BEGIN ")) {
						return readBytes(reader, line.trim().replace("BEGIN", "END"));
					}
				}
				throw new IOException("PEM is invalid: no begin marker");
			} finally {
				reader.close();
			}
		}

		protected byte[] readBytes(BufferedReader reader, String endMarker) throws IOException {
			String line;
			StringBuffer buf = new StringBuffer();

			while ((line = reader.readLine()) != null) {
				if (line.indexOf(endMarker) != -1) {
					return Base64.getDecoder().decode(buf.toString());
				}
				buf.append(line.trim());
			}
			throw new IOException("PEM is invalid : No end marker");
		}

		protected PrivateKey handleOtherKeys(InputStream keyInputStream, String clientKeyAlgo)
				throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
			byte[] keyBytes = decodePem(keyInputStream);
			KeyFactory keyFactory = KeyFactory.getInstance(clientKeyAlgo);
			try {
				// First let's try PKCS8
				return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
			} catch (InvalidKeySpecException e) {
				// Otherwise try PKCS8
				RSAPrivateCrtKeySpec keySpec = decodePKCS1(keyBytes);
				return keyFactory.generatePrivate(keySpec);
			}
		}

		public RSAPrivateCrtKeySpec decodePKCS1(byte[] keyBytes) throws IOException {
			DerParser parser = new DerParser(keyBytes);
			Asn1Object sequence = parser.read();
			sequence.validateSequence();
			parser = new DerParser(sequence.getValue());
			parser.read();

			return new RSAPrivateCrtKeySpec(next(parser), next(parser), next(parser), next(parser), next(parser),
					next(parser), next(parser), next(parser));
		}

		protected BigInteger next(DerParser parser) throws IOException {
			return parser.read().getInteger();
		}

		protected TrustManager[] trustManagers()
				throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
			if (this.caCertData == null) {
				return null;
			}
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore trustStore = createTrustStore(createInputStreamFromBase64EncodedString(this.caCertData));
			tmf.init(trustStore);
			return tmf.getTrustManagers();
		}

		protected KeyStore createTrustStore(InputStream pemInputStream)
				throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			loadDefaultTrustStoreFile(trustStore, "changeit".toCharArray());
			while (pemInputStream.available() > 0) {
				CertificateFactory certFactory = CertificateFactory.getInstance("X509");
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(pemInputStream);
				String alias = cert.getSubjectX500Principal().getName() + "_" + cert.getSerialNumber().toString(16);
				trustStore.setCertificateEntry(alias, cert);
			}
			return trustStore;
		}

		protected void loadDefaultTrustStoreFile(KeyStore keyStore, char[] trustStorePassphrase)
				throws CertificateException, NoSuchAlgorithmException, IOException {

			File trustStoreFile = getDefaultTrustStoreFile();

			if (!loadDefaultStoreFile(keyStore, trustStoreFile, trustStorePassphrase)) {
				keyStore.load(null);
			}
		}

		protected boolean loadDefaultStoreFile(KeyStore keyStore, File fileToLoad, char[] passphrase)
				throws CertificateException, NoSuchAlgorithmException, IOException {

			if (fileToLoad.exists() && fileToLoad.isFile() && fileToLoad.length() > 0) {
				try {
					try (FileInputStream fis = new FileInputStream(fileToLoad)) {
						keyStore.load(fis, passphrase);
					}
					return true;
				} catch (Exception e) {
				}
			}
			return false;
		}

		protected File getDefaultTrustStoreFile() {
			String securityDirectory = System.getProperty("java.home") + File.separator + "lib" + File.separator
					+ "security" + File.separator;

			String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
			if (trustStorePath != null) {
				return new File(trustStorePath);
			}

			File jssecacertsFile = new File(securityDirectory + "jssecacerts");
			if (jssecacertsFile.exists() && jssecacertsFile.isFile()) {
				return jssecacertsFile;
			}

			return new File(securityDirectory + "cacerts");
		}

		protected ByteArrayInputStream createInputStreamFromBase64EncodedString(String data) {
			byte[] bytes;
			try {
				bytes = Base64.getDecoder().decode(data);
			} catch (IllegalArgumentException illegalArgumentException) {
				bytes = data.getBytes();
			}

			return new ByteArrayInputStream(bytes);
		}

		/**
		 * 200 OK: 请求成功，服务器成功处理了请求并返回所请求的数据。 
		 * 201 Created: 请求成功，服务器创建了新资源。 
		 * 204 No Content: 请求成功，服务器处理成功，但没有返回数据。 
		 * 400 Bad Request: 请求无效，服务器无法理解请求。 
		 * 401 Unauthorized: 未授权，需要进行身份验证或令牌无效。 
		 * 403 Forbidden: 请求被拒绝，客户端没有访问资源的权限。 
		 * 404 Not Found: 请求的资源不存在。 
		 * 409 Conflict: 请求冲突，通常用于表示资源的当前状态与请求的条件不匹配。 
		 * 500 Internal Server Error: 服务器内部错误，表示服务器在处理请求时遇到了问题。
		 * 
		 * @param response response
		 * @return json json
		 */
		protected synchronized JsonNode parseResponse(CloseableHttpResponse response) {

			switch (response.getCode()) {
				case 200:
				try {
					return new ObjectMapper().readTree(response.getEntity().getContent());
				} catch (Exception e) {
					throw new KubernetesUnknownException(e.toString());
				} 
//					if (result.has("status") && result.get("status").asText().equals("Failure")) {
//						int code = result.get("code").asInt();
//						String cause = statusDesc.get(code);
//						throw new Exception(cause != null ? cause : result.toPrettyString());
//					}
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
		 * @return masterUrl
		 */
		public String getMasterUrl() {
			return masterUrl;
		}

		/**
		 * @return token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * @return httpClient
		 */
		public CloseableHttpClient getHttpClient() {
			return httpClient;
		}

		static class DerParser {

			private InputStream in;

			DerParser(byte[] bytes) throws IOException {
				this.in = new ByteArrayInputStream(bytes);
			}

			Asn1Object read() throws IOException {
				int tag = in.read();

				if (tag == -1) {
					throw new IOException("Invalid DER: stream too short, missing tag");
				}

				int length = getLength();
				byte[] value = new byte[length];
				if (in.read(value) < length) {
					throw new IOException("Invalid DER: stream too short, missing value");
				}

				return new Asn1Object(tag, value);
			}

			private int getLength() throws IOException {
				int i = in.read();
				if (i == -1) {
					throw new IOException("Invalid DER: length missing");
				}

				if ((i & ~0x7F) == 0) {
					return i;
				}

				int num = i & 0x7F;
				if (i >= 0xFF || num > 4) {
					throw new IOException("Invalid DER: length field too big (" + i + ")");
				}

				byte[] bytes = new byte[num];
				if (in.read(bytes) < num) {
					throw new IOException("Invalid DER: length too short");
				}

				return new BigInteger(1, bytes).intValue();
			}
		}

		static class Asn1Object {

			private final int type;
			private final byte[] value;
			private final int tag;

			public Asn1Object(int tag, byte[] value) {
				this.tag = tag;
				this.type = tag & 0x1F;
				this.value = value;
			}

			public byte[] getValue() {
				return value;
			}

			BigInteger getInteger() throws IOException {
				if (type != 0x02) {
					throw new IOException("Invalid DER: object is not integer"); //$NON-NLS-1$
				}
				return new BigInteger(value);
			}

			void validateSequence() throws IOException {
				if (type != 0x10) {
					throw new IOException("Invalid DER: not a sequence");
				}
				if ((tag & 0x20) != 0x20) {
					throw new IOException("Invalid DER: can't parse primitive entity");
				}
			}
		}

	}

}
