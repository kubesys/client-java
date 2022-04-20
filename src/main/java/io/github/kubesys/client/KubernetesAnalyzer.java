/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import io.github.kubesys.client.core.KubernetesConvertor;
import io.github.kubesys.client.core.KubernetesExtractor;
import io.github.kubesys.client.core.KubernetesRegistry;
import io.github.kubesys.client.core.KubernetesRuleBase;

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
	protected final KubernetesConvertor convertor;

	/**
	 * registry
	 */
	protected final KubernetesRegistry registry;

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
