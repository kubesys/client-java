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

import org.apache.hc.client5.http.classic.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.utils.ReqUtil;
import io.github.kubesys.client.utils.URLUtil;


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
	public void registerKinds(KubernetesClient caller, String path) throws Exception {
		
		String uri = URLUtil.join(caller.getKubernetesAdminConfig().getMasterUrl(), path);
		
		HttpGet request = ReqUtil.get(caller.getKubernetesAdminConfig(), uri);
		
		JsonNode response  = caller.getResponse(request);
		
		JsonNode resources = response.get(KubernetesConstants.HTTP_RESPONSE_RESOURCES);
		
		for (int i = 0; i < resources.size(); i++) {
			
			JsonNode resource = resources.get(i);
			
			String shortKind  = resource.get(KubernetesConstants.KUBE_KIND).asText();
			String apiVersion = response.get(KubernetesConstants.KUBE_RESOURCES_GROUPVERSION).asText();
			String apiGroup   = apiVersion.indexOf("/") == -1 ? null : apiVersion.substring(0, apiVersion.indexOf("/"));
			String fullKind   = apiGroup == null ? shortKind : apiGroup + "." + shortKind;
			
			// we only support a version for each resources
			if (ruleBase.getNameMapping().containsKey(fullKind) ||
					(fullKind.equals(shortKind) && fullKind.endsWith("Options"))) {
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
