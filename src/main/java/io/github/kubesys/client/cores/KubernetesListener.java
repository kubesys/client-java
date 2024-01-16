/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *limitations under the License.
 */
package io.github.kubesys.client.cores;

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

	/**
	 * client
	 */
	protected final KubernetesClient client;
	
	/**
	 * registry
	 */
	protected final KubernetesRegistry registry;
	
	/**
	 * @param client client
	 * @param registry registry
	 */
	public KubernetesListener(KubernetesClient client, KubernetesRegistry registry) {
		this.client = client;
		this.registry = registry;
	}

	/**
	 * @throws Exception Exception
	 */
	public void start() throws Exception {
		JsonNode items = client.listResources("apiextensions.k8s.io.CustomResourceDefinition").get("items");
		for (int i = 0; i < items.size(); i++) {
			register(items.get(i));
		}
	}
	
	/**
	 * @param node node
	 */
	public void register(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String path = KubernetesConstants.VALUE_APIS + "/" +  apiGroup + "/" + version;
		
		try {
			registry.registerKinds(client, path);
		} catch (Exception e) {
			m_logger.warning("fail to register: " + e);
		}
		
	}
	
}
