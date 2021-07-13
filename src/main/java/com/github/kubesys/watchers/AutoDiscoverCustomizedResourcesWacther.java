/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.watchers;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.KubernetesAnalyzer;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesConstants;
import com.github.kubesys.KubernetesWatcher;
import com.github.kubesys.utils.URLUtil;


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
		
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		
		String version = spec.get(KubernetesConstants.KUBE_SPEC_VERSIONS)
							.iterator().next().get(KubernetesConstants
									.KUBE_SPEC_VERSIONS_NAME).asText();
		String url = URLUtil.join(KubernetesConstants
							.VALUE_APIS, apiGroup, version);
		
		try {
			client.getAnalyzer().registerKinds(client.getHttpCaller(), url);
		} catch (Exception e) {
			m_logger.warning(e.getMessage());
		}
		
	}


	@Override
	public void doDeleted(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		JsonNode names = spec.get(KubernetesConstants.KUBE_SPEC_NAMES);
		
		String shortKind = names.get(KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText();
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		String fullKind  = apiGroup + "." + shortKind;
		
		KubernetesAnalyzer analyzer = client.getAnalyzer();
		analyzer.removeFullKind(shortKind, fullKind);
		
		analyzer.removeKindBy(fullKind);
		analyzer.removeNameBy(fullKind);
		analyzer.removeGroupBy(fullKind);
		analyzer.removeVersionBy(fullKind);
		analyzer.removeNamespacedBy(fullKind);
		analyzer.removeApiPrefixBy(fullKind);
		analyzer.removeVerbsBy(fullKind);
		
		m_logger.info("unregister " + shortKind);
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
					new AutoDiscoverCustomizedResourcesWacther(client));
		} catch (Exception e) {
			try {
				Thread.sleep(5000);
			} catch (Exception e1) {
				doClose();
			}
		}
	}

}
