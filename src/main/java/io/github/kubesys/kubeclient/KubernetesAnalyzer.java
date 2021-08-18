/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.util.logging.Logger;

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
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesAnalyzer.class.getName());

	protected final KubernetesClient client;
	
	protected final KubernetesConvertor convertor;
	
	protected final KubernetesRegistry registry;
	
	/*******************************************
	 * 
	 *            Core
	 * 
	
	 ********************************************/
	
	/**
	 * @param  client              client
	 * @throws Exception           exception 
	 */
	public KubernetesAnalyzer(KubernetesClient client) throws Exception {
		this.client = client;
		
		KubernetesRuleBase ruleBase = new KubernetesRuleBase();
		this.registry = new KubernetesRegistry(ruleBase);
		this.convertor = new KubernetesConvertor(ruleBase);
	}
	
	public void start() throws Exception {
		
		KubernetesExtractor extractor = new KubernetesExtractor(client, registry);
		extractor.start();
		
		KubernetesListener listener = new KubernetesListener(client, registry);
		listener.start();
	}

	public KubernetesConvertor getConvertor() {
		return convertor;
	}

}
