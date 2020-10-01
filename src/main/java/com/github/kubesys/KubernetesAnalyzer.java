/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
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
	 * client
	 */
	protected KubernetesClient kubeClient;
	
	/**
	 * config
	 */
	protected KubernetesConfig config = new KubernetesConfig();
	
	/**
	 * 
	 * @param client              client
	 * @throws Exception          exception 
	 */
	protected KubernetesAnalyzer(KubernetesClient client) throws Exception {
		
		this.kubeClient = client;
		
		HttpGet request = createHttpGet(client.tokenInfo, client.masterUrl);
		
		JsonNode resp = client.getResponse(client.httpClient.execute(request));
		
		if (!resp.has(KubernetesConstants.HTTP_RESPONSE_PATHS)) {
			throw new Exception("Fail to init HTTP(s) client, please check url and/or token.");
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
				registerKinds(URLUtils.join(client.getMasterUrl(), path));
			}
		}
	}



	/**
	 * @param uri                 uri
	 * @throws Exception          exception
	 */
	public void registerKinds(String uri) throws Exception {
		
		
		HttpGet request = createHttpGet(kubeClient.tokenInfo, uri);
		
		JsonNode response  = kubeClient.getResponse(kubeClient.httpClient.execute(request));
		
		JsonNode resources = response.get(KubernetesConstants.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			
			String  thisKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			
			// we only support a version for each resources
			if (config.getKind2NameMapping().containsKey(thisKind)) {
				continue;
			}
			
			config.getKind2ApiPrefixMapping().put(thisKind, uri);
			config.getKind2GroupMapping().put(thisKind, getGroup(uri));
			config.getKind2NameMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			config.getKind2NamespacedMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			config.getKind2VersionMapping().put(thisKind, response.get(
							KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText());
			config.getKind2VerbsMapping().put(thisKind, resource.get("verbs"));
			
			m_logger.info("register " + thisKind + ": <" + getGroup(uri) + "," 
					+ response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText() + ","
					+ resource.get(KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asText() + ","
					+ uri + ">");
		}
	}

	/**
	 * @param tokenInfo                 taskInfo
	 * @param url                       url
	 * @return                          httpGet
	 */
	protected HttpGet createHttpGet(String tokenInfo, String url) {
		
		HttpGet request = new HttpGet(url);
		
		if (tokenInfo != null) {
			request.setHeader("Authorization", "Bearer " + tokenInfo);
		}
		
		return request;
	}
	/*******************************************
	 * 
	 *            getter
	 * 
	 ********************************************/
	
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
	
	/**
	 * @return                    config
	 */ 
	public KubernetesConfig getConfig() {
		return config;
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
	
}
