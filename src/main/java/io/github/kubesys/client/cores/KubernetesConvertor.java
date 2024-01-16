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


import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.exceptions.KubernetesResourceNotFoundException;
import io.github.kubesys.client.utils.URLUtil;


/**
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class KubernetesConvertor {

	
	/**
	 * ruleBase
	 */
	protected final KubernetesRuleBase ruleBase;
	
	
	/**
	 * @param ruleBase
	 */
	public KubernetesConvertor(KubernetesRuleBase ruleBase) {
		super();
		this.ruleBase = ruleBase;
	}

	/*******************************************
	 * 
	 *            Core
	 * 
	 ********************************************/
	
	
	/**
	 * @param fullKind        fullKind
	 * @param namespace       namespace
	 * @param isWatch         isWatch
	 * @return                url
	 */
	protected String baseUrl(String fullKind, String namespace, boolean isWatch) {

		try {
			return URLUtil.join(ruleBase.getApiPrefix(fullKind),
							isWatch ? KubernetesConstants.KUBEAPI_WATCHER_PATTERN : "",
							URLUtil.namespacePath(ruleBase.isNamespaced(fullKind), namespace),
							ruleBase.getName(fullKind));
		} catch (Exception ex) {
			throw new KubernetesResourceNotFoundException(fullKind + " is not registered" );
		}
	}
	
	/**
	 * @param json json
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String createUrl(JsonNode json) throws NullPointerException {
		String fullKind = fullkind(json);
		return URLUtil.join(baseUrl(fullKind, namespace(json), false));
	}


	/**
	 * @param json json
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String bindingUrl(JsonNode json) throws NullPointerException {
		String fullKind = fullkind(json);
		return URLUtil.join(baseUrl(fullKind, namespace(json), false),
							"pods", name(json), "binding");
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String deleteUrl(String kind, String ns, String name) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, false), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String updateUrl(String kind, String ns, String name) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, false), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String getUrl(String kind, String ns, String name) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, false), name);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String listUrl(String kind, String ns) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, false));
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String updateStatusUrl(String kind, String ns, String name) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, false), 
				name, KubernetesConstants.HTTP_RESPONSE_STATUS);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @param name name
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String watchOneUrl(String kind, String ns, String name) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, true), name,
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}

	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String watchAllUrl(String kind, String ns) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, true),
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
	}
	
	/**
	 * @param kind kind
	 * @param ns   ns
	 * @return Url
	 * @throws NullPointerException exception
	 */
	public String watchAllUrlWithFieldSelector(String kind, String ns, String fieldSelector) throws NullPointerException {
		String fullKind = fullkind(kind);
		return URLUtil.join(baseUrl(fullKind, ns, true),
				KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE)
					+ "&fieldSelector=" + fieldSelector;
	}
	
	
	/*******************************************
	 * 
	 *            Util
	 * 
	 ********************************************/
	/**
	 * @param json json
	 * @return apiVersion
	 */
	protected String apiVersion(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_APIVERSION).asText();

	}

	/**
	 * @param json       json
	 * @return kind
	 */
	public String kind(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_KIND).asText();
	}
	
	/**
	 * @param kind        kind
	 * @return fullkind
	 */
	protected String fullkind(String kind) {
		return kind.indexOf(".") == -1 ? ruleBase.getFullKind(kind) : kind;
	}
	
	/**
	 * @param json         json
	 * @return fullkind
	 */
	public String fullkind(JsonNode json) {
		String apiVersion = apiVersion(json);
		String kind = kind(json);
		return apiVersion.indexOf("/") == -1 ? kind : 
			apiVersion.substring(0, apiVersion.indexOf("/")) + "." + kind;
	}
	
	/**
	 * @param json              json
 	 * @return metadata.name
	 */
	public String name(JsonNode json) {
		return json.get(KubernetesConstants.KUBE_METADATA)
				.get(KubernetesConstants.KUBE_METADATA_NAME).asText();
	}

	/**
	 * @param json            json
	 * @return                namespace
	 */
	public String namespace(JsonNode json) {
		JsonNode meta = json.get(KubernetesConstants.KUBE_METADATA);
		return meta.has(KubernetesConstants.KUBE_METADATA_NAMESPACE)
				? meta.get(KubernetesConstants.KUBE_METADATA_NAMESPACE).asText()
				: KubernetesConstants.VALUE_DEFAULT_NAMESPACE;

	}
	

	/**
	 * @return  rulebase
	 */
	public KubernetesRuleBase getRuleBase() {
		return ruleBase;
	}
	
}
