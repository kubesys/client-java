/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.core;

import java.util.logging.Logger;


import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.kubeclient.KubernetesClient;
import io.github.kubesys.kubeclient.KubernetesConstants;
import io.github.kubesys.kubeclient.KubernetesWatcher;
import io.github.kubesys.kubeclient.utils.URLUtil;


/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources]
 * (https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
 * 
 */
public class KubernetesListener extends KubernetesWatcher {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesListener.class.getName());

	protected final KubernetesRegistry registry;
	
	public KubernetesListener(KubernetesClient client, KubernetesRegistry registry) {
		super(client);
		this.registry = registry;
	}

	public void start() throws Exception {
		client.watchResources("apiextensions.k8s.io.CustomResourceDefinition", 
				KubernetesConstants.VALUE_ALL_NAMESPACES, this);
	}
	
	@Override
	public void doAdded(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String url = URLUtil.join(KubernetesConstants
							.VALUE_APIS, apiGroup, version);
		
		try {
			registry.registerKinds(client.getHttpCaller(), url);
		} catch (Exception e) {
			m_logger.warning(e.getMessage());
		}
		
	}


	@Override
	public void doDeleted(JsonNode node) {
		registry.unregisterKinds(node);
	}

	@Override
	public void doModified(JsonNode node) {
		// ignore here
	}

	@Override
	public void doClose() {
		try {
			client.watchResources("apiextensions.k8s.io.CustomResourceDefinition", 
					KubernetesConstants.VALUE_ALL_NAMESPACES, 
					new KubernetesListener(client, registry));
		} catch (Exception e) {
			try {
				Thread.sleep(5000);
			} catch (Exception e1) {
				doClose();
			}
		}
	}
	
}
