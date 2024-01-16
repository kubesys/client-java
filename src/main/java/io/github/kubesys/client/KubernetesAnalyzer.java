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
package io.github.kubesys.client;

import io.github.kubesys.client.cores.KubernetesConvertor;
import io.github.kubesys.client.cores.KubernetesExtractor;
import io.github.kubesys.client.cores.KubernetesRegistry;
import io.github.kubesys.client.cores.KubernetesRuleBase;

/**
 * KubernetesAnalyzer is used for learning all Kubernetes' resources.
 * 
 * @author wuheng@iscas.ac.cn
 *
 */
public final class KubernetesAnalyzer {

	/**
	 * rulebase
	 */
	protected final KubernetesRuleBase ruleBase;

	/**
	 * convertor
	 */
	public final KubernetesConvertor convertor;

	/**
	 * registry
	 */
	public final KubernetesRegistry registry;

	/*******************************************
	 * 
	 * Init
	 * 
	 ********************************************/
	public KubernetesAnalyzer() {
		this.ruleBase = new KubernetesRuleBase();
		this.registry = new KubernetesRegistry(ruleBase);
		this.convertor = new KubernetesConvertor(ruleBase);
	}

	/**
	 * @param client client
	 * @return KubernetesAnalyzer
	 * @throws Exception Exception
	 */
	public KubernetesAnalyzer initIfNeed(KubernetesClient client) throws Exception {

		if (ruleBase.empty()) {
			KubernetesExtractor extractor = new KubernetesExtractor(client, registry);
			extractor.start();
		}

		return this;
	}

	/*******************************************
	 * 
	 * Getter
	 * 
	 ********************************************/

	/**
	 * @return convertor
	 */
	public KubernetesConvertor getConvertor() {
		return convertor;
	}

	/**
	 * @return registry
	 */
	public KubernetesRegistry getRegistry() {
		return registry;
	}

}
