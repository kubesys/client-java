/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.core;

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.kubeclient.KubernetesConstants;
import io.github.kubesys.kubeclient.KubernetesClient.HttpCaller;
import io.github.kubesys.kubeclient.utils.HttpUtil;


/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources]
 * (https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
 * 
 */
public class KubernetesExtractor {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesExtractor.class.getName());

	
	/**
	 * httpCaller
	 */
	protected final HttpCaller caller;
	
	/**
	 * kubeRegistry
	 */
	protected final KubernetesRegistry registry;
	
	/*******************************************
	 * 
	 *            Core
	 * 
	
	 ********************************************/
	

	/**
	 * @param caller                caller
	 * @param registry              registry
	 * @throws Exception            exception
	 */
	public KubernetesExtractor(HttpCaller caller, 
			KubernetesRegistry registry) throws Exception {
		
		this.caller = caller;
		this.registry = registry;
		
	}

	
	public void start() throws Exception {

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
					registry.registerKinds(caller, path);
				} catch (Exception ex) {
					// warning
				}
			}
		}
	}
}
