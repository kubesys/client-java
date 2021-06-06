/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesObjectWatcher<O> extends KubernetesWatcher {

	protected final Class<?> clz;
	
	public KubernetesObjectWatcher(KubernetesClient kubeClient) throws Exception {
		super(kubeClient);
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
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doModified(JsonNode node) {
		try {
			doObjectModified((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doDeleted(JsonNode node) {
		try {
			doObjectDeleted((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
