/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
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
 * limitations under the License.
 */
package io.github.kubesys.client.watchers;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.KubernetesWatcher;
import io.github.kubesys.client.cores.KubernetesRuleBase;

/**
 * @author wuheng@iscas.ac.cn
 * @since  1.0.0
 *
 */
public class KubernetesCRDWacther extends KubernetesWatcher {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesCRDWacther.class.getName());
	
	
	public KubernetesCRDWacther(KubernetesClient client) {
		super(client);
	}

	@Override
	public void doAdded(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String path =  KubernetesConstants.VALUE_APIS + "/" + apiGroup + "/" + version;
		
		try {
			client.getAnalyzer().registry.registerKinds(client, path);
		} catch (Exception e) {
			m_logger.severe(e.toString());
		}
		
	}


	@Override
	public void doDeleted(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		JsonNode names = spec.get(KubernetesConstants.KUBE_SPEC_NAMES);
		
		String shortKind = names.get(KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText();
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		String fullKind  = apiGroup + "." + shortKind;
		
		KubernetesRuleBase ruleBase = client.getAnalyzer().convertor.getRuleBase();
		ruleBase.removeFullKind(shortKind, fullKind);
		
		ruleBase.removeKindBy(fullKind);
		ruleBase.removeNameBy(fullKind);
		ruleBase.removeGroupBy(fullKind);
		ruleBase.removeVersionBy(fullKind);
		ruleBase.removeNamespacedBy(fullKind);
		ruleBase.removeApiPrefixBy(fullKind);
		ruleBase.removeVerbsBy(fullKind);
		
		m_logger.info("unregister" + shortKind);
	}

	@Override
	public void doModified(JsonNode node) {
		// ignore here
	}

	@Override
	public void doClose() {
		while (true) {
			try {
				m_logger.info("watcher apiextensions.k8s.io.CustomResourceDefinition is crash");
				m_logger.info("wait 5 seconds to restart watcher apiextensions.k8s.io.CustomResourceDefinition ");
				Thread.sleep(5000);
				client.watchResourcesByFullkindAndNamespace("apiextensions.k8s.io.CustomResourceDefinition", 
						KubernetesConstants.VALUE_ALL_NAMESPACES, 
						new KubernetesCRDWacther(client));
				break;
			} catch (Exception e) {
				m_logger.info("fail to restart watcher apiextensions.k8s.io.CustomResourceDefinition: " + e.toString());
				Thread.currentThread().interrupt();
			}
			
		}
	}

}
