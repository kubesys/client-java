/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesWatcher extends WebSocketListener {

	/**
	 * client
	 */
	protected final KubernetesClient kubeClient;
	
	/**
	 * kind
	 */
	protected final String kind;
	
	public KubernetesWatcher(KubernetesClient client, String kind) {
		super();
		this.kubeClient = client;
		this.kind = kind;
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		super.onMessage(webSocket, text);
		try {
			JsonNode json = new ObjectMapper().readTree(text);
			String type = json.get(KubernetesConstants.KUBE_TYPE).asText();
			JsonNode obj = json.get(KubernetesConstants.KUBE_OBJECT);
			if (type.equals(KubernetesConstants.JSON_TYPE_ADDED)) {
				doAdded(obj);
			} else if (type.equals(KubernetesConstants.JSON_TYPE_MODIFIED)) {
				doModified(obj);
			} else if (type.equals(KubernetesConstants.JSON_TYPE_DELETED)) {
				doDeleted(obj);
			}
		} catch (Exception e) {
			throw new KubernetesException(e);
		}
		
	}
	
	/**
	 * @param node                  node
	 */
	public abstract void doAdded(JsonNode node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doModified(JsonNode node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doDeleted(JsonNode node);

	@Override
	public void onClosing(WebSocket webSocket, int code, String reason) {
	}
	
}
