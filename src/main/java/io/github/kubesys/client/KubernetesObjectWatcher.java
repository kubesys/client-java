/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;


import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesObjectWatcher<O> extends KubernetesWatcher {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesObjectWatcher.class.getName());
	
	protected final Class<?> clz;
	
	protected KubernetesObjectWatcher(KubernetesClient client) throws Exception {
		super(client);
		String gSclz = getClass().getGenericSuperclass().getTypeName();
		int idx = gSclz.indexOf("<");
		int edx = gSclz.indexOf(">");
		this.clz = Class.forName(gSclz.substring(idx + 1, edx));
	}

	/**
	 * @param node                  node
	 */
	public abstract void doObjectAdded(O node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doObjectModified(O node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doObjectDeleted(O node);
	
	/**
	 * 
	 */
	public abstract void doClose();

	@SuppressWarnings("unchecked")
	@Override
	public void doAdded(JsonNode node) {
		try {
			doObjectAdded((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
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
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doDeleted(JsonNode node) {
		try {
			doObjectDeleted((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}
	
	
}
