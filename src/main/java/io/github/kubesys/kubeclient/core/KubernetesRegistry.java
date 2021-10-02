/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.core;

import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.github.kubesys.kubeclient.KubernetesConstants;
import io.github.kubesys.kubeclient.KubernetesClient.HttpCaller;
import io.github.kubesys.kubeclient.utils.ReqUtil;
import io.github.kubesys.kubeclient.utils.URLUtil;


/**
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 *
 * register Kubernetes kinds and their descriptions as <code>KubernetesRuleBase</code>
 */
public class KubernetesRegistry {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesRegistry.class.getName());

	
	/**
	 * ruleBase
	 */
	protected final KubernetesRuleBase ruleBase;

	
	public KubernetesRegistry(KubernetesRuleBase ruleBase) {
		super();
		this.ruleBase = ruleBase;
	}
	
	public KubernetesRuleBase getRuleBase() {
		return ruleBase;
	}

	
	/**********************************************
	 * 
	 *       core 
	 * 
	 ***********************************************/
	
	/**
	 * @param caller              caller
	 * @param path                path
	 * @throws Exception          exception
	 */
	public void registerKinds(HttpCaller caller, String path) throws Exception {
		
		String uri = URLUtil.join(caller.getMasterUrl(), path);
		
		HttpGet request = ReqUtil.get(caller.getToken(), uri);
		
		JsonNode response  = caller.getResponse(request);
		
		JsonNode resources = response.get(KubernetesConstants.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			
			String shortKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			String apiVersion = response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText();
			String apiGroup   = apiVersion.indexOf("/") == -1 ? null : apiVersion.substring(0, apiVersion.indexOf("/"));
			String fullKind   = apiGroup == null ? shortKind : apiGroup + "." + shortKind;
			
			// we only support a version for each resources
			if (ruleBase.getNameMapping().containsKey(fullKind)) {
				continue;
			}

			ruleBase.addFullKind(shortKind, fullKind);
			ruleBase.addApiPrefix(fullKind, uri);
			ruleBase.addKind(fullKind, shortKind);
			ruleBase.addGroup(fullKind, getGroupByUrl(uri));
			ruleBase.addName(fullKind, resource.get(
							KubernetesConstants.KUBE_METADATA_NAME).asText());
			ruleBase.addNamespaced(fullKind, resource.get(
							KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asBoolean());
			ruleBase.addVersion(fullKind, apiVersion);
			ruleBase.addVerbs(fullKind, (ArrayNode) resource.get("verbs"));
			
			m_logger.info("register " + fullKind + ": <" + getGroupByUrl(uri) + "," 
					+ apiVersion + ","
					+ resource.get(KubernetesConstants.KUBE_RESOURCES_NAMESPACED).asText() + ","
					+ uri + ">");
		}
	}
	
	public void unregisterKinds(JsonNode node) {
		
		JsonNode spec = node.get(KubernetesConstants.KUBE_SPEC);
		JsonNode names = spec.get(KubernetesConstants.KUBE_SPEC_NAMES);
		
		String shortKind = names.get(KubernetesConstants.KUBE_SPEC_NAMES_KIND).asText();
		String apiGroup  = spec.get(KubernetesConstants.KUBE_SPEC_GROUP).asText();
		String fullKind  = apiGroup + "." + shortKind;
		
		ruleBase.removeFullKind(shortKind, fullKind);
		
		ruleBase.removeKindBy(fullKind);
		ruleBase.removeNameBy(fullKind);
		ruleBase.removeGroupBy(fullKind);
		ruleBase.removeVersionBy(fullKind);
		ruleBase.removeNamespacedBy(fullKind);
		ruleBase.removeApiPrefixBy(fullKind);
		ruleBase.removeVerbsBy(fullKind);
		
		m_logger.info("unregister " + shortKind);
	}
	
	/**
	 * @param url                url
	 * @return                   group
	 */
	private String getGroupByUrl(String url) {
		if (url.endsWith(KubernetesConstants.KUBEAPI_CORE_PATTERN)) {
			return "";
		}
		int etx = url.lastIndexOf('/');
		int stx = url.substring(0, etx).lastIndexOf("/");
		return  url.substring(stx + 1, etx);
	}
}
