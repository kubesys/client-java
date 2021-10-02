/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import io.github.kubesys.kubeclient.core.KubernetesConvertor;
import io.github.kubesys.kubeclient.core.KubernetesExtractor;
import io.github.kubesys.kubeclient.core.KubernetesListener;
import io.github.kubesys.kubeclient.core.KubernetesRegistry;
import io.github.kubesys.kubeclient.core.KubernetesRuleBase;

/**
 * @author wuheng09@gmail.com
 *
 */
public final class KubernetesAnalyzer {

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
	 *            Core
	 * 
	 ********************************************/
	
	/**
	 * init KubernetesRegistry and KubernetesConvertor
	 */
	public KubernetesAnalyzer() {
		KubernetesRuleBase ruleBase = new KubernetesRuleBase();
		this.registry = new KubernetesRegistry(ruleBase);
		this.convertor = new KubernetesConvertor(ruleBase);
	}
	
	public void analyseServerBy(KubernetesClient client) throws Exception {
		
		KubernetesExtractor extractor = new KubernetesExtractor(client, registry);
		extractor.start();
		
//		KubernetesListener listener = new KubernetesListener(client, registry);
//		listener.start();
	}

	/*******************************************
	 * 
	 *            Getter
	 * 
	 ********************************************/
	
	/**
	 * @return                   convertor
	 */
	public KubernetesConvertor getConvertor() {
		return convertor;
	}

	/**
	 * @return                   registry
	 */
	public KubernetesRegistry getRegistry() {
		return registry;
	}


}
