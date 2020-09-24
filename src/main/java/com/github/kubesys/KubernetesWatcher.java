/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesWatcher extends Thread {

	/**
	 * client
	 */
	protected CloseableHttpClient httpClient;
	
	protected HttpGet request;

	protected final KubernetesClient kubeClient;
	
	public KubernetesWatcher(KubernetesClient kubeClient) {
		super();
		this.kubeClient = kubeClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setRequest(HttpGet request) {
		this.request = request;
	}

	@Override
	public void run() {
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
						httpClient.execute(request).getEntity().getContent()));
		    String line = null;
		    while ((line = br.readLine()) != null) {
		    	JsonNode json = new ObjectMapper().readTree(line);
				String type = json.get(KubernetesConstants.KUBE_TYPE).asText();
				JsonNode obj = json.get(KubernetesConstants.KUBE_OBJECT);
				if (type.equals(KubernetesConstants.JSON_TYPE_ADDED)) {
					doAdded(obj);
				} else if (type.equals(KubernetesConstants.JSON_TYPE_MODIFIED)) {
					doModified(obj);
				} else if (type.equals(KubernetesConstants.JSON_TYPE_DELETED)) {
					doDeleted(obj);
				}
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	/**
	 * 
	 */
	public abstract void doClose();
	
}
