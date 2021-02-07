/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
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
				registerKinds(client, path);
			}
		}
	}

	/**
	 * @param client              client
	 * @param path                path
	 * @throws Exception          exception
	 */
	public void registerKinds(KubernetesClient client, String path) throws Exception {
		
		String uri = URLUtils.join(client.getMasterUrl(), path);
		
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
			
			kubeConfig.addApiPrefix(thisKind, uri);
			kubeConfig.addGroup(thisKind, getGroup(uri));
			kubeConfig.addName(thisKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			kubeConfig.addNamespaced(thisKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			kubeConfig.addVersion(thisKind, response.get(
							KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText());
			kubeConfig.addVerbs(thisKind, resource.get("verbs"));
			
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
			m_logger.warning(ex.getMessage());
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
