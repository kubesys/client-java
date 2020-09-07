/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.methods.HttpGet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubesys.utils.URLUtils;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesWatcher implements Runnable {

	/**
	 * client
	 */
	protected final KubernetesClient kubeClient;
	
	/**
	 * kind
	 */
	protected final String kind;
	
	protected final String namespace;
	
	protected final String name;
	
	public KubernetesWatcher(KubernetesClient kubeClient, String kind) {
		this(kubeClient, kind, KubernetesConstants.VALUE_ALL_NAMESPACES, null);
	}
	
	public KubernetesWatcher(KubernetesClient kubeClient, String kind, String namespace) {
		this(kubeClient, kind, namespace, null);
	}
	
	public KubernetesWatcher(KubernetesClient kubeClient, String kind, String namespace, String name) {
		super();
		this.kubeClient = kubeClient;
		this.kind = kind;
		this.namespace = namespace;
		this.name = name;
	}

	@Override
	public void run() {
		try {
			final String uri = URLUtils.join(kubeClient.kubeConfig.getApiPrefix(kind), 
					KubernetesConstants.KUBEAPI_WATCHER_PATTERN,  
					kubeClient.getNamespace(kubeClient.kubeConfig.isNamespaced(kind), namespace), 
					kubeClient.kubeConfig.getName(kind), name, 
					KubernetesConstants.HTTP_QUERY_WATCHER_ENABLE);
			HttpGet request = new HttpGet(uri);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(kubeClient.httpClient.execute(request).getEntity().getContent()));
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

	
}
