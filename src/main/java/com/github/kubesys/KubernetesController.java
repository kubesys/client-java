/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesController<O> {

	protected final KubernetesClient client;
	
	public KubernetesController(String kind, KubernetesClient client) throws Exception {
		this.client = client;
		client.watchResources(kind, new KubernetesObjectWatcher<O>(client) {

			@Override
			public void doObjectAdded(O node) {
				doCreate(createObjectsFrom(node));
			}

			@Override
			public void doObjectModified(O node) {
				updateObjectByChange(node);
			}

			@Override
			public void doObjectDeleted(O node) {
				doRemove(removeObjectsBy(node));
			}

			@Override
			public void doClose() {
				System.exit(1);
			}
		});
	}
	
	public abstract List<Object> createObjectsFrom (O node);
	
	public abstract List<Object> removeObjectsBy (O node);
	
	public abstract void updateObjectByChange(O node);
	
	public void doCreate(List<Object> objects) {
		for (Object obj : objects) {
			try {
				client.createResource(new ObjectMapper().readTree(
						new ObjectMapper().writeValueAsString(obj)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doUpdate(List<Object> objects) {
		for (Object obj : objects) {
			try {
				client.updateResource(new ObjectMapper().readTree(
						new ObjectMapper().writeValueAsString(obj)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doRemove(List<Object> objects) {
		for (Object obj : objects) {
			try {
				client.deleteResource(new ObjectMapper().readTree(
						new ObjectMapper().writeValueAsString(obj)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
