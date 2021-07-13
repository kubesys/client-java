/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient.HttpCaller;
import com.github.kubesys.utils.HttpUtil;
import com.github.kubesys.utils.URLUtil;

/**
 * @author wuheng09@gmail.com
 *
 */
public final class KubernetesAnalyzer {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesAnalyzer.class.getName());

	
	/**
	 * caller
	 */
	protected final HttpCaller caller;
	
	/*******************************************
	 * 
	 *            Core
	 * 
	
	 ********************************************/
	
	/**
	 * @param  caller              caller
	 * @throws Exception           exception 
	 */
	public KubernetesAnalyzer(HttpCaller caller) throws Exception {
		
		this.caller = caller;
		
		HttpGet request = HttpUtil.get(caller.getToken(), caller.getMasterUrl());
		
		JsonNode resp = caller.getResponse(request);
		
		if (!resp.has(KubernetesConstants.HTTP_RESPONSE_PATHS)) {
			throw new Exception("Fail to init HTTP(s) client, forbidden users or invalid token.");
		}
		
		Iterator<JsonNode> iterator = resp.get(
				KubernetesConstants.HTTP_RESPONSE_PATHS).iterator();
		
		// traverse all paths in key 'paths' 
		while (iterator.hasNext()) {
			
			String path = iterator.next().asText();
			
			// we just find and register Kubernetes native kinds
			// which cannot be undeployed
			if (path.startsWith(KubernetesConstants.KUBEAPI_CORE_PREFIX_PATTERN) && 
					(path.split(KubernetesConstants.KUBEAPI_PATHSEPARTOR_PATTERN).length == 4 
						|| path.equals(KubernetesConstants.KUBEAPI_CORE_PATTERN))) {

				// register it
				try {
					registerKinds(caller, path);
				} catch (Exception ex) {
					// warning
				}
			}
		}
	}

	/**
	 * @param caller              caller
	 * @param path                path
	 * @throws Exception          exception
	 */
	public void registerKinds(HttpCaller caller, String path) throws Exception {
		
		String uri = URLUtil.join(caller.getMasterUrl(), path);
		
		HttpGet request = HttpUtil.get(caller.getToken(), uri);
		
		JsonNode response  = caller.getResponse(request);
		
		JsonNode resources = response.get(KubernetesConstants.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			
			String shortKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			String apiVersion = response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText();
			String apiGroup   = apiVersion.indexOf("/") == -1 ? null : apiVersion.substring(0, apiVersion.indexOf("/"));
			String fullKind   = apiGroup == null ? shortKind : apiGroup + "." + shortKind;
			
			// we only support a version for each resources
			if (getNameMapping().containsKey(fullKind)) {
				continue;
			}

			addFullKind(shortKind, fullKind);
			addApiPrefix(fullKind, uri);
			addKind(fullKind, shortKind);
			addGroup(fullKind, getGroupByUrl(uri));
			addName(fullKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			addNamespaced(fullKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			addVersion(fullKind, apiVersion);
			addVerbs(fullKind, resource.get("verbs"));
			
			m_logger.info("register " + fullKind + ": <" + getGroupByUrl(uri) + "," 
					+ apiVersion + ","
					+ resource.get(KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asText() + ","
					+ uri + ">");
		}
	}

	/**
	 * @param url                url
	 * @return                   group
	 */
	public String getGroupByUrl(String url) {
		if (url.endsWith(KubernetesConstants.KUBEAPI_CORE_PATTERN)) {
			return "";
		}
		int etx = url.lastIndexOf('/');
		int stx = url.substring(0, etx).lastIndexOf("/");
		return  url.substring(stx + 1, etx);
	}
	
	
	/*******************************************
	 * 
	 * analysis-based Url
	 * 
	 ********************************************/
	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	public String createUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);
		String uri = (version.indexOf("/") == -1) 
				? "api/" + version : "apis/" + version;

		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 
				? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		return URLUtil.join(caller.getMasterUrl(), uri, 
				getNamespace(isNamespaced(fullKind), 
						getNamespace(json)),
						getName(fullKind));
	}

	/**
	 * @param json json
	 * @return Url
	 * @throws Exception exception
	 */
	public String bindingUrl(JsonNode json) throws Exception {

		String version = getApiVersion(json);
		String uri = (version.indexOf("/") == -1) 
				? "api/" + version : "apis/" + version;

		String kind = getKind(json);
		String fullKind = version.indexOf("/") == -1 
				? kind : version.substring(0, version.indexOf("/")) + "." + kind;
		return URLUtil.join(caller.getMasterUrl(), uri, 
				getNamespace(isNamespaced(fullKind), 
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
				? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), 
				getNamespace(isNamespaced(fullKind), ns),
				getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String updateUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), getNamespace(isNamespaced(fullKind), ns),
				getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String getUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), getNamespace(isNamespaced(fullKind), ns),
				getName(fullKind), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	public String listUrl(String kind, String ns) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), getNamespace(isNamespaced(fullKind), ns),
				getName(fullKind));
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String updateStatusUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), getNamespace(isNamespaced(fullKind), ns),
				getName(fullKind), name, KubernetesConstants.HTTP_RESPONSE_STATUS);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws Exception exception
	 */
	public String watchOneUrl(String kind, String ns, String name) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(isNamespaced(fullKind), ns), getName(fullKind), name,
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws Exception exception
	 */
	public String watchAllUrl(String kind, String ns) throws Exception {
		String fullKind = kind.indexOf(".") == -1 ? getFullKind(kind) : kind;
		return URLUtil.join(getApiPrefix(fullKind), KubernetesConstants.KUBEAPI_WATCHER_PATTERN,
				getNamespace(isNamespaced(fullKind), ns), getName(fullKind),
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
	
	/*******************************************
	 * 
	 * Getter and Putter
	 * 
	 ********************************************/
	
	/**
	 * mapper
	 */
	protected Map<String, List<String>>   kindToFullKindMapper        = new HashMap<>();
	
	/**
	 * kind
	 */
	protected final Map<String, String>   fullKindToKindMapper        = new HashMap<>();
	
	/**
	 * name
	 */
	protected final Map<String, String>   fullKindToNameMapper        = new HashMap<>();
	
	/**
	 * namespace
	 */
	protected final Map<String, Boolean>  fullKindToNamespacedMapper  = new HashMap<>();
	
	/**
	 * version
	 */
	protected final Map<String, String>   fullKindToVersionMapper     = new HashMap<>();
	
	/**
	 * group
	 */
	protected final Map<String, String>   fullKindToGroupMapper       = new HashMap<>();

	/**
	 * verbs
	 */
	protected final Map<String, JsonNode> fullKindToVerbsMapper       = new HashMap<>();


	/**
	 * apiPrefix
	 */
	protected final Map<String, String>   fullKindToApiPrefixMapper   = new HashMap<>();
	
	
	/**
	 * @return                    fullKinds
	 */
	public Map<String, List<String>> getFullKinds() {
		return kindToFullKindMapper;
	}
	
	/**
	 * @param kind                kind
	 * @return                    fullKinds
	 */
	public List<String> getFullKinds(String kind) {
		return kindToFullKindMapper.get(kind);
	}

	/**
	 * @param kind                kind
	 * @param fullKind            fullKind
	 */
	public void addFullKind(String kind, String fullKind) {
		List<String> values = kindToFullKindMapper.containsKey(kind) ? 
				kindToFullKindMapper.get(kind) : new ArrayList<>();
		values.add(fullKind);
		kindToFullKindMapper.put(kind, values);
	}

	/**
	 * @param kind                kind
	 * @return                    fullKinds
	 * @throws Exception          exception
	 */
	public String getFullKind(String kind) throws Exception {
		List<String> values = kindToFullKindMapper.get(kind);
		if (values != null && values.size() == 1) {
			return values.get(0);
		}
		throw new Exception("Please use fullKind, " + getFullKinds(kind));
	}
	
	/**
	 * @param kind                kind
	 * @param fullKind            fullKind
	 */
	public void removeFullKind(String kind, String fullKind) {
		List<String> values = kindToFullKindMapper.get(kind);
		if (values == null) {
			return;
		}
		
		values.remove(fullKind);
		
		if (values.size() == 0) {
			kindToFullKindMapper.remove(kind);
		} else {
			kindToFullKindMapper.put(kind, values);
		}
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       name
	 */
	public String getName(String fullKind) {
		return fullKindToNameMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param name                   name
	 */
	public void addName(String fullKind, String name) {
		this.fullKindToNameMapper.put(fullKind, name);
	}
	
	/**
	 * @param fullKind             fullKind
	 */
	public void removeNameBy(String fullKind) {
		this.fullKindToNameMapper.remove(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */     
	public String getVersion(String fullKind) {
		return fullKindToVersionMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param version                version
	 */
	public void addVersion(String fullKind, String version) {
		this.fullKindToVersionMapper.put(fullKind, version);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */     
	public String removeVersionBy(String fullKind) {
		return fullKindToVersionMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */
	public String getGroup(String fullKind) {
		return fullKindToGroupMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param group                  group
	 */
	public void addGroup(String fullKind, String group) {
		this.fullKindToGroupMapper.put(fullKind, group);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */
	public String removeGroupBy(String fullKind) {
		return fullKindToGroupMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       kind
	 */
	public String getKind(String fullKind) {
		return fullKindToKindMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param kind                   kind
	 */
	public void addKind(String fullKind, String kind) {
		this.fullKindToKindMapper.put(fullKind, kind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       kind
	 */
	public String removeKindBy(String fullKind) {
		return fullKindToKindMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       namespaced
	 */
	public Boolean isNamespaced(String fullKind) {
		return fullKindToNamespacedMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param namespaced             namespaced
	 */
	public void addNamespaced(String fullKind, boolean namespaced) {
		this.fullKindToNamespacedMapper.put(fullKind, namespaced);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       namespaced
	 */
	public Boolean removeNamespacedBy(String fullKind) {
		return fullKindToNamespacedMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind               fullKind
	 * @param verbs                   verbs
	 */
	public void addVerbs(String fullKind, JsonNode verbs) {
		this.fullKindToVerbsMapper.put(fullKind, verbs);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       apiPrefix
	 */
	public String getApiPrefix(String fullKind) {
		return fullKindToApiPrefixMapper.get(fullKind);
	}
	

	/**
	 * @param fullKind              fullKind
	 * @param apiPrefix              apiPrefix
	 */
	public void addApiPrefix(String fullKind, String apiPrefix) {
		this.fullKindToApiPrefixMapper.put(fullKind, apiPrefix);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       apiPrefix
	 */
	public String removeApiPrefixBy(String fullKind) {
		return fullKindToApiPrefixMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       verbs
	 */
	public JsonNode removeVerbsBy(String fullKind) {
		return fullKindToVerbsMapper.remove(fullKind);
	}
	
	/**
	 * @return                       nameMapping
	 */
	public Map<String, String> getNameMapping() {
		return fullKindToNameMapper;
	}

	/**
	 * @return                       versionMapping
	 */
	public Map<String, String> getVersionMapping() {
		return fullKindToVersionMapper;
	}

	/**
	 * @return                       groupMapping
	 */
	public Map<String, String> getGroupMapping() {
		return fullKindToGroupMapper;
	}

	/**
	 * @return                       namespacedMapping
	 */
	public Map<String, Boolean> getNamespacedMapping() {
		return fullKindToNamespacedMapper;
	}

	/**
	 * @return                       apiPrefixMapping
	 */
	public Map<String, String> getApiPrefixMapping() {
		return fullKindToApiPrefixMapper;
	}

	/**
	 * @return                       verbsMapping
	 */
	public Map<String, JsonNode> getVerbsMapping() {
		return fullKindToVerbsMapper;
	}

	/**
	 * @return                       kindMapping
	 */
	public Map<String, String> getKindMapping() {
		return fullKindToKindMapper;
	}
	
}
