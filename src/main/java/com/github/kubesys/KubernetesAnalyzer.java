/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.utils.HttpUtils;
import com.github.kubesys.utils.URLUtils;

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
	 * config
	 */
	protected KubernetesConfig kubeConfig = new KubernetesConfig();
	
	/*******************************************
	 * 
	 *            Core
	 * 
	
	 ********************************************/
	
	/**
	 * @param kubeConfig         config
	 */
	public KubernetesAnalyzer(KubernetesConfig kubeConfig) {
		super();
		this.kubeConfig = kubeConfig;
	}
	
	/**
	 * @param client              client
	 * @throws Exception          exception 
	 */
	protected KubernetesAnalyzer(KubernetesClient client) throws Exception {
		
		HttpGet request = HttpUtils.get(client.tokenInfo, client.masterUrl);
		
		JsonNode resp = client.getResponse(client.httpClient.execute(request));
		
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
				registerKinds(client, URLUtils.join(client.getMasterUrl(), path));
			}
		}
	}


	/**
	 * @param client              client
	 * @param uri                 uri
	 * @throws Exception          exception
	 */
	public void registerKinds(KubernetesClient client, String uri) throws Exception {
		
		HttpGet request = HttpUtils.get(client.tokenInfo, uri);
		
		JsonNode response  = client.getResponse(client.httpClient.execute(request));
		
		JsonNode resources = response.get(KubernetesConstants.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			
			String  thisKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			
			// we only support a version for each resources
			if (kubeConfig.getKind2NameMapping().containsKey(thisKind)) {
				continue;
			}
			
			kubeConfig.getKind2ApiPrefixMapping().put(thisKind, uri);
			kubeConfig.getKind2GroupMapping().put(thisKind, getGroup(uri));
			kubeConfig.getKind2NameMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			kubeConfig.getKind2NamespacedMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			kubeConfig.getKind2VersionMapping().put(thisKind, response.get(
							KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText());
			kubeConfig.getKind2VerbsMapping().put(thisKind, resource.get("verbs"));
			
			m_logger.info("register " + thisKind + ": <" + getGroup(uri) + "," 
					+ response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText() + ","
					+ resource.get(KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asText() + ","
					+ uri + ">");
		}
	}

	/**
	 * @param url                url
	 * @return                   group
	 */
	public String getGroup(String url) {
		if (url.endsWith(KubernetesConstants.KUBEAPI_CORE_PATTERN)) {
			return "";
		}
		int etx = url.lastIndexOf('/');
		int stx = url.substring(0, etx).lastIndexOf("/");
		return  url.substring(stx + 1, etx);
	}
	
	/*******************************************
	 * 
	 *            knowledge-based Url
	 * 
	 ********************************************/
	/**
	 * @param kind                 kind
	 * @param ns                   ns
	 * @return                     Url
	 */
	public String createUrl(String kind, String ns) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), 
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind));
	}
	
	/** 
	 * @param kind                 kind
	 * @param ns                   ns
	 * @param name                 name
	 * @return                     Url
	 */
	public String deleteUrl(String kind, String ns, String name) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), 
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind), name);
	}
	
	/** 
	 * @param kind                 kind
	 * @param ns                   ns
	 * @param name                 name
	 * @return                     Url
	 */
	public String updateUrl(String kind, String ns, String name) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), 
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind), name);
	}
	
	/** 
	 * @param kind                 kind
	 * @param ns                   ns
	 * @param name                 name
	 * @return                     Url
	 */
	public String getUrl(String kind, String ns, String name) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), 
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind), name);
	}
	
	
	/**
	 * @param kind                 kind
	 * @param ns                   ns
	 * @return                     Url
	 */
	public String listUrl(String kind, String ns) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), 
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind));
	}
	
	/**
	 * @param kind                 kind
	 * @param ns                   ns
	 * @param name                 name
	 * @return                     Url
	 */
	public String updateStatusUrl(String kind, String ns, String name) {
		return URLUtils.join(
				kubeConfig.getApiPrefix(kind), getNamespace(
				kubeConfig.isNamespaced(kind), ns), kubeConfig.getName(kind), 
				name, KubernetesConstants.HTTP_RESPONSE_STATUS);
	}
	
	/**
	 * @param kind                 kind
	 * @param ns                   ns
	 * @param name                 name
	 * @return                     Url
	 */
	public String watchOneUrl(String kind, String ns, String name) {
		return URLUtils.join(kubeConfig.getApiPrefix(kind), 
				KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind), name, 
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}
	
	/**
	 * @param kind                 kind
	 * @param ns                   ns
	 * @return                     Url
	 */
	public String watchAllUrl(String kind, String ns) {
		return URLUtils.join(kubeConfig.getApiPrefix(kind), 
				KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
				getNamespace(kubeConfig.isNamespaced(kind), ns), 
				kubeConfig.getName(kind),  
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}
	
	/**
	 * @param namespaced            bool
	 * @param namespace             ns
	 * @return                      full path
	 */
	public String getNamespace(boolean namespaced, String namespace) {
		return (namespaced && namespace != null && namespace.length() != 0) 
					? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
						: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}
	
	
	/** 
	 * @return                                json
	 */
	public JsonNode getMeta() {
		
		ObjectNode map = new ObjectMapper().createObjectNode();
		
		for (String kind : kubeConfig.kind2NameMapping.keySet()) {
			ObjectNode node = new ObjectMapper().createObjectNode();
			node.put("apiVersion", kubeConfig.kind2VersionMapping.get(kind));
			node.put("kind", kind);
			node.put("plural", kubeConfig.kind2NameMapping.get(kind));
			node.set("verbs", kubeConfig.kind2VerbsMapping.get(kind));
			
			map.set(kind, node);
		}
		
		return map;
	}
	
	/*******************************************
	 * 
	 *            singleton
	 * 
	 ********************************************/
	
	/**
	 * singleton
	 */
	protected static KubernetesAnalyzer analyzer;
	
	/**
	 * @param client               client
	 * @return                     KubernetesParser
	 */ 
	public static KubernetesAnalyzer getParser(KubernetesClient client) {
		try {
			if (analyzer == null) {
				analyzer = new KubernetesAnalyzer(client);
			}
		} catch (Exception ex) {
			
		}
		return analyzer;
	}
	
	/**
	 * @return                    config
	 */ 
	public KubernetesConfig getConfig() {
		return kubeConfig;
	}
	
}
