/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.Request;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesParser {

	/**
	 * config
	 */
	protected kubernetesConfig config;
	
	/**
	 * 
	 * @param client              client
	 * @throws Exception          exception 
	 */
	protected KubernetesParser(KubernetesClient client) throws Exception {
		//http(s)://ip:port
		// {
		//  "paths": [
		//            "/api",
		//            "/api/v1",
		//            "/apis",
		//            "/apis/",
		//            "/apis/admissionregistration.k8s.io",
		//            "/apis/admissionregistration.k8s.io/v1",
		//            "/apis/admissionregistration.k8s.io/v1beta1",
		Request request = client.createRequest(
								kubernetesConstants.HTTP_REQUEST_GET, 
								client.getUrl(), null);
		JsonNode node = client.getResponse(request);
		Iterator<JsonNode> iterator = node.get(kubernetesConstants.HTTP_RESPONSE_PATHS).iterator();
		
		// traverse all paths in key 'paths' 
		while (iterator.hasNext()) {
			String path = iterator.next().asText();
			if (!path.startsWith(kubernetesConstants.K8S_CORE_STARTWITH_API_PATTERN)) {
				continue;
			}
			
			// we just find and register Kubernetes native kinds
			// it means this kind cannot be undeployed
			if (path.split(kubernetesConstants.HTTP_PATH_SEPARTOR).length == 4 
					|| path.equals(kubernetesConstants.K8S_CORE_API_PATTERN)) {

				// register it
//				registerCoreKinds(path);
				System.out.println(path);
			}
		}
	}
	

	/**
	 * @return                    config
	 */ 
	public kubernetesConfig getConfig() {
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
	protected static KubernetesParser parser;
	
	/**
	 * @param client               client
	 * @return                     KubernetesParser
	 * @throws Exception           exception
	 */ 
	public synchronized static KubernetesParser getParser(KubernetesClient client) throws Exception {
		if (parser == null) {
			parser = new KubernetesParser(client);
		}
		return parser;
	}
	
}
