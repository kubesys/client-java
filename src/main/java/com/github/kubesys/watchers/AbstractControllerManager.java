/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.watchers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubesys.KubernetesClient;
import com.github.kubesys.KubernetesObjectWatcher;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class AbstractControllerManager<O> extends KubernetesObjectWatcher<O> {

	public AbstractControllerManager(KubernetesClient client) throws Exception {
		super(client);
	}
	
	@Override
	public final void doObjectAdded(O node) {
		
	}

	@Override
	public final void doObjectDeleted(O node) {
		
	}

	@Override
	public void doAdded(JsonNode node) {
		try {
			client.createResource(node);
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doModified(JsonNode node) {
		try {
			doObjectModified((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
			client.updateResource(node);
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}

	@Override
	public void doDeleted(JsonNode node) {
		try {
			client.deleteResource(node);
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}
	
	
}
