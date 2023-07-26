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
	 * @param podName  name
	 * @param podNamespace namespace
	 * @param hostName hostname
	 * @return json json from Kubernetes
	 */
	public static JsonNode toBinding(String podName, String podNamespace, String hostName) {

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
	
	/**
	 * 不考虑apiVersion为空或者不符合Kuberneres资源定义的的情况
	 * 否则抛出异常
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/
	 * 
	 * @param apiVersion Kubernetes对应kind的apiVersion
	 * @return Kubernetes对应kind的group
	 */
	public static String toGroup(String apiVersion) {
		int idx = apiVersion.indexOf(KubernetesConstants.KUBE_APIVERSION_SPLIT);
		return idx == -1 ? KubernetesConstants.KUBE_DEFAULT_GROUP : apiVersion.substring(0, idx);
	}
	
	
	/**
	 * 不考虑json为空或者不符合Kuberneres资源定义的的情况
	 * 否则抛出异常
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/
	 * 
	 * @param json   json
	 * @return      获得kubernetes的name
	 */
	public static String getName(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}
	
	/**
	 * 不考虑json为空或者不符合Kuberneres资源定义的的情况
	 * 否则抛出异常
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/
	 * 
	 * @param json   json
	 * @return      获得kubernetes的namespace
	 */
	public static String getNamespace(JsonNode json) {
		if (json.get(KubernetesConstants.KUBE_METADATA).has(KubernetesConstants.KUBE_METADATA_NAMESPACE)) {
			return json.get(KubernetesConstants.KUBE_METADATA).get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText();
		}
		return KubernetesConstants.KUBE_DEFAULT_NAMESPACE;
	}
	
	
	/**
	 * 不考虑json为空或者不符合Kuberneres资源定义的的情况
	 * 否则抛出异常
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/
	 * 
	 * @param json   json
	 * @return      获得kubernetes的namespace
	 */
	public static String getGroup(JsonNode json) {
		return toGroup(json.get(KubernetesConstants.KUBE_APIVERSION).asText());
	}
	
	/**
	 * 不考虑fullkind为空或者不符合Kuberneres资源定义的的情况
	 * 否则抛出异常
	 * https://kubernetes.io/docs/concepts/overview/working-with-objects/
	 * 
	 * @param fullkind   fullkind = group + "." + kind
	 * @return      获得kubernetes的namespace
	 */
	public static String getGroup(String fullkind) {
		int idx = fullkind.lastIndexOf(KubernetesConstants.KUBE_VALUE_SPLIT);
		return idx == -1 ? KubernetesConstants.KUBE_DEFAULT_GROUP : fullkind.substring(0, idx);
	}
	
	/**
	 * @param value      目标KInd的元数据表述
	 * @return           是否支持被watch，像其见Kubernetes的Watch机制
	 */
	public static boolean supportWatch(JsonNode value) {
		return value.get("verbs").toPrettyString().contains("watch");
	}
}
