/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.client.KubernetesConstants;

/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/07/24
 * @version 1.0.0
 * 
 * it is used for generating Pods and related resources
 * 
 **/
public class KubeUtil {

	private KubeUtil() {
		super();
	}

	/**
	 * for Scheduler
	 * 
	 * { "apiVersion": "v1", "kind": "Binding", "metadata": { "name": "podName" },
	 * "target": { "apiVersion": "v1", "kind": "Node", "name": "hostName" } }
	 * 
	 * @param pod  pod json
	 * @param hostName hostname
	 * @return json json from Kubernetes
	 */
	public static JsonNode bindingResource(String podName, String podNamespace, String hostName) {

		ObjectNode binding = new ObjectMapper().createObjectNode();
		binding.put(KubernetesConstants.KUBE_APIVERSION, KubernetesConstants.DEFAULT_APIVERSION);
		binding.put(KubernetesConstants.KUBE_KIND, KubernetesConstants.BINDING_KIND);

		ObjectNode metadata = new ObjectMapper().createObjectNode();
		metadata.put(KubernetesConstants.KUBE_METADATA_NAME, podName);
		metadata.put(KubernetesConstants.KUBE_METADATA_NAMESPACE, podNamespace);
		binding.set(KubernetesConstants.KUBE_METADATA, metadata);

		ObjectNode target = new ObjectMapper().createObjectNode();
		target.put(KubernetesConstants.KUBE_APIVERSION, KubernetesConstants.DEFAULT_APIVERSION);
		target.put(KubernetesConstants.KUBE_KIND, KubernetesConstants.NODE_KIND);
		target.put(KubernetesConstants.KUBE_METADATA_NAME, hostName);
		binding.set(KubernetesConstants.KUBE_TARGET, target);

		return binding;
	}
}
