/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.logging.Logger;

import com.github.kubesys.core.KubernetesConvertor;
import com.github.kubesys.core.KubernetesExtractor;
import com.github.kubesys.core.KubernetesRegistry;
import com.github.kubesys.core.KubernetesRuleBase;

/**
 * @author wuheng09@gmail.com
 *
 */
public final class KubernetesAnalyzer {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesAnalyzer.class.getName());

	protected final KubernetesRuleBase ruleBase;
	
	protected final KubernetesConvertor convertor;
	
	protected final KubernetesClient client;
	
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
		this.ruleBase = new KubernetesRuleBase();
		this.convertor = new KubernetesConvertor(
				client.getHttpCaller().getMasterUrl(), ruleBase);
	}
	
	public void start() throws Exception {
		
		KubernetesRegistry registry = new KubernetesRegistry(ruleBase);
		
		KubernetesExtractor extractor = new KubernetesExtractor(client.getHttpCaller(), registry);
		extractor.start();
		
//		KubernetesListener listener = new KubernetesListener(client, registry);
//		listener.start();
	}

	
	public KubernetesRuleBase getRuleBase() {
		return ruleBase;
	}


	public KubernetesConvertor getConvertor() {
		return convertor;
	}
	
}
