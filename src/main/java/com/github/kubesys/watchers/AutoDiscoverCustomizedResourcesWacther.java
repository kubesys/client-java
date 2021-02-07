/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.watchers;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConfig;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.utils.URLUtils;


/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.7.0
 * @since 2020.3.1
 * 
 **/
public class AutoDiscoverCustomizedResourcesWacther extends KubernetesWatcher {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(AutoDiscoverCustomizedResourcesWacther.class.getName());
	
	public AutoDiscoverCustomizedResourcesWacther(KubernetesClient client) {
		super(client);
	}

	@Override
	public void doAdded(JsonNode node) {
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		JsonNode names = spec.get(KubernetesConstants.KUBE_SPEC_NAMES);
		
		String kind = names.get(KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText();
		
		KubernetesConfig config = kubeClient.getAnalyzer().getConfig();
		if (config.getName(kind) != null) {
			return;
		}
		
		String group = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String url = URLUtils.join(KubernetesConstants
							.VALUE_APIS, group, version);
		try {
			kubeClient.getAnalyzer().registerKinds(kubeClient, url);
		} catch (Exception e) {
			m_logger.warning(e.getMessage());
		}
		
	}


	@Override
	public void doDeleted(JsonNode node) {
		String kind = node.get(KubernetesConstants.KUBE_SPEC)
						.get(KubernetesConstants.KUBE_SPEC_NAMES)
						.get(KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText();
		
		KubernetesConfig config = kubeClient.getAnalyzer().getConfig();
		config.removeNameBy(kind);
		config.removeGroupBy(kind);
		config.removeVersionBy(kind);
		config.removeNamespacedBy(kind);
		config.removeApiPrefixBy(kind);
		config.removeVerbsBy(kind);
		
		m_logger.info("unregister " + kind);
	}

	@Override
	public void doModified(JsonNode node) {
		// ignore here
	}

	@Override
	public void doClose() {
		try {
			this.kubeClient.watchResources("CustomResourceDefinition", 
					KubernetesConstants.VALUE_ALL_NAMESPACES, 
					new AutoDiscoverCustomizedResourcesWacther(kubeClient));
		} catch (Exception e) {
			try {
				Thread.sleep(5000);
			} catch (Exception e1) {
				doClose();
			}
		}
	}

}
