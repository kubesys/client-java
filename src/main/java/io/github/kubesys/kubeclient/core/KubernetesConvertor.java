/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.core;


import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.kubeclient.KubernetesConstants;
import io.github.kubesys.kubeclient.utils.URLUtil;


/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources]
 * (https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
 * 
 */
public class KubernetesConvertor {

	
	/**
	 * ruleBase
	 */
	protected final KubernetesRuleBase ruleBase;
	
	
	public KubernetesConvertor(KubernetesRuleBase ruleBase) {
		super();
		this.ruleBase = ruleBase;
	}

	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	public String createUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);
		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 
				? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind),
							getNamespace(ruleBase.isNamespaced(fullKind), 
							getNamespace(json)),
							ruleBase.getName(fullKind));
	}

	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	public String bindingUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);

		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 
				? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), 
							getNamespace(ruleBase.isNamespaced(fullKind), 
							getNamespace(json)),"pods", 
							json.get("metadata").get("name").asText(), "binding");
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String deleteUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 
				? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), 
				            getNamespace(ruleBase.isNamespaced(fullKind), ns),
				            ruleBase.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String updateUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), 
				getNamespace(ruleBase.isNamespaced(fullKind), ns),
				ruleBase.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String getUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), 
				getNamespace(ruleBase.isNamespaced(fullKind), ns),
				ruleBase.getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	public String listUrl(String kind, String ns) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), getNamespace(ruleBase.isNamespaced(fullKind), ns),
				ruleBase.getName(fullKind));
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String updateStatusUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), 
				getNamespace(ruleBase.isNamespaced(fullKind), ns),
				ruleBase.getName(fullKind), name, KubernetesConstants.HTTP_RESPONSE_STATUS);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String watchOneUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(ruleBase.isNamespaced(fullKind), ns), 
				ruleBase.getName(fullKind), name,
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	public String watchAllUrl(String kind, String ns) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
		return URLUtil.join(ruleBase.getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(ruleBase.isNamespaced(fullKind), ns), 
				ruleBase.getName(fullKind),
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}
	
	
	/**
	 * @param json json
	 * @return full path
	 */
	protected String getApiVersion(JsonNode json) {
		return json.get("apiVersion").asText();

	}
	
	public String getName(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}

	public String getNamespace(JsonNode json) {
		JsonNode meta = json.get(KubernetesConstants.KUBE_METADATA);
		return meta.has(KubernetesConstants.KUBE_METADATA_NAMESPACE)
				? meta.get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText()
				: KubernetesConstants.VALUE_DEFAULT_NAMESPACE;

	}
	
	protected String getNamespace(boolean namespaced, String namespace) {
		return (namespaced && namespace != null && namespace.length() != 0)
				? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
				: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}
	
	public String getKind(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_KIND).asText();
	}

	public String getFullKind(JsonNode json) {
		String apiVersion = json.get(KubernetesConstants.KUBE_APIVERSION).asText();
		String kind = json.get(KubernetesConstants.KUBE_KIND).asText();
		if(apiVersion.indexOf("/") > 0) {
			return apiVersion.substring(0, apiVersion.indexOf("/"))+ "." + kind;
		}
		return kind;
	}

	public KubernetesRuleBase getRuleBase() {
		return ruleBase;
	}
	
}
