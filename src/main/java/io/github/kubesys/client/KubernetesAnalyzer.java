/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
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
