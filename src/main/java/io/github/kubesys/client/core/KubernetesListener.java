/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.core;

import java.util.logging.Logger;


import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;


/**
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 * 
 * listen for Kubernetes kinds and their descriptions during runtime
 *
 */
public class KubernetesListener {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesListener.class.getName());

	protected final KubernetesClient client;
	
	protected final KubernetesRegistry registry;
	
	public KubernetesListener(KubernetesClient client, KubernetesRegistry registry) {
		this.client = client;
		this.registry = registry;
	}

	public void start() throws Exception {
		JsonNode items = client.listResources("apiextensions.k8s.io.CustomResourceDefinition").get("items");
		for (int i = 0; i < items.size(); i++) {
			register(items.get(i));
		}
	}
	
	public void register(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String path = KubernetesConstants.VALUE_APIS + "/" +  apiGroup + "/" + version;
		
		try {
			registry.registerKinds(client.getRequester(), path);
		} catch (Exception e) {
			m_logger.warning("fail to register: " + e);
		}
		
	}
	
}
