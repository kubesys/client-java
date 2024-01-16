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

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.utils.ReqUtil;


/**
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 *
 * extract Kubernetes kinds and their descriptions using Http-based query
 * 
 */
public class KubernetesExtractor {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesExtractor.class.getName());

	
	/**
	 * httpCaller
	 */
	protected final KubernetesClient caller;
	
	/**
	 * kubeRegistry
	 */
	protected final KubernetesRegistry registry;
	
	/*******************************************
	 * 
	 *            Core
	 * 
	 ********************************************/

	/**
	 * @param client                client
	 * @param registry              registry
	 */
	public KubernetesExtractor(KubernetesClient client, 
							KubernetesRegistry registry)  {
		this.caller = client;
		this.registry = registry;
		
	}
	
	public void start() throws Exception {

		HttpGet request = ReqUtil.get(
				caller.getKubernetesAdminConfig(), 
				caller.getKubernetesAdminConfig().getMasterUrl());
		
		JsonNode resp = caller.getResponse(request);
		
		if (!resp.has(KubernetesConstants.HTTP_RESPONSE_PATHS)) {
			throw new Exception("fail to init HTTP(s) client, forbidden users or invalid token.");
		}
		
		Iterator<JsonNode> iterator = resp.get(
				KubernetesConstants.HTTP_RESPONSE_PATHS).iterator();
		
		// traverse all paths in key 'paths' 
		while (iterator.hasNext()) {
			
			String path = iterator.next().asText();
			
			// we just find and register Kubernetes native kinds
			// which cannot be undeployed
			if (path.startsWith(KubernetesConstants.KUBEAPI_CORE_PREFIX_PATTERN) && 
					(path.split(KubernetesConstants.KUBEAPI_PATHSEPARTOR_PATTERN).length == 4 
						|| path.equals(KubernetesConstants.KUBEAPI_CORE_PATTERN))) {

				// register it
				try {
					registry.registerKinds(caller, path);
				} catch (Exception ex) {
					// warning
					m_logger.warning(ex.toString());
				}
			}
		}
	}
}
