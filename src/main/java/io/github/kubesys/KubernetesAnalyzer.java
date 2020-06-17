/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.util.Iterator;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.utils.URLUtils;
import okhttp3.Request;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesAnalyzer {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesAnalyzer.class.getName());

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
		
		Request request = client.createRequest(
								KubernetesConstants.HTTP_REQUEST_GET, 
								client.getUrl(), null);
		JsonNode node = client.getResponse(request);
		
		if (!node.has(KubernetesConstants.HTTP_RESPONSE_PATHS)) {
			throw new KubernetesException("Fail to init HTTP(s) client, please check url and/or token.");
		}
		
		Iterator<JsonNode> iterator = node.get(KubernetesConstants.HTTP_RESPONSE_PATHS).iterator();
		
		// traverse all paths in key 'paths' 
		while (iterator.hasNext()) {
			String path = iterator.next().asText();
			if (!path.startsWith(KubernetesConstants.KUBEAPI_CORE_PREFIX_PATTERN)) {
				continue;
			}
			
			// we just find and register Kubernetes native kinds
			// it means this kind cannot be undeployed
			if (path.split(KubernetesConstants.KUBEAPI_PATHSEPARTOR_PATTERN).length == 4 
					|| path.equals(KubernetesConstants.KUBEAPI_CORE_PATTERN)) {

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
	protected void registerKinds(KubernetesClient client, String path) throws Exception {
		
		String   fullUrl   = URLUtils.join(client.getUrl(), path);
		Request  request   = client.createRequest(KubernetesConstants
								.HTTP_REQUEST_GET, fullUrl, null);
		JsonNode response  = client.getResponse(request);
		JsonNode resources = response.get(KubernetesConstants
								.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			String  thisKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			
			if (config.getKind2NameMapping().containsKey(thisKind)) {
				continue;
			}
			
			config.getKind2ApiPrefixMapping().put(thisKind, fullUrl);
			config.getKind2GroupMapping().put(thisKind, getGroupFrom(path));
			config.getKind2NameMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			config.getKind2NamespacedMapping().put(thisKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			config.getKind2VersionMapping().put(thisKind, response.get(
							KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText());
			
			m_logger.info("register " + thisKind + ": <" + getGroupFrom(path) + "," 
					+ response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText() + ","
					+ resource.get(KubernetesConstants.KUBE_RESOURCES_NAMESPACED) + ","
					+ fullUrl + ">");
		}
	}
	
	/*******************************************
	 * 
	 *            getter
	 * 
	 ********************************************/
	
	/**
	 * @param path               path
	 * @return                   group
	 */
	public String getGroupFrom(String path) {
		if (path.equals(KubernetesConstants.KUBEAPI_CORE_PATTERN)) {
			return "";
		}
		int stx = path.indexOf('/', 1);
		int etx = path.lastIndexOf('/');
		return  path.substring(stx + 1, etx);
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
	 * @throws Exception           exception
	 */ 
	public static KubernetesAnalyzer getParser(KubernetesClient client) throws Exception {
		if (analyzer == null) {
			analyzer = new KubernetesAnalyzer(client);
		}
		return analyzer;
	}
	
}
