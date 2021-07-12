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

	
	
	
	/*******************************************
	 * 
	 *            Core
	 * 
	
	 ********************************************/
	
	/**
	 * @param  caller              caller
	 * @throws Exception          exception 
	 */
	public KubernetesAnalyzer(HttpCaller caller) throws Exception {
		
		HttpGet request = HttpUtil.get(caller.getTokenInfo(), caller.getMasterUrl());
		
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
	 * @param caller             caller
	 * @param path                path
	 * @throws Exception          exception
	 */
	public void registerKinds(HttpCaller caller, String path) throws Exception {
		
		String uri = URLUtil.join(caller.getMasterUrl(), path);
		
		HttpGet request = HttpUtil.get(caller.getTokenInfo(), uri);
		
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
	 *            singleton
	 * 
	 ********************************************/
	
	
	
	
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
