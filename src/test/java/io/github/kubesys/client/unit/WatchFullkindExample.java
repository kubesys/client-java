/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.unit;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.KubernetesWatcher;


/**
 * @author wuheng09@gmail.com
 *
 */
public class WatchFullkindExample extends AbstractClient {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		System.setProperty("org.apache.http.debug", "true");
		
		KubernetesWatcher watcher = new KubernetesWatcher(client) {
			
			@Override
			public void doModified(JsonNode node) {
				System.out.println(node);
			}
			
			@Override
			public void doDeleted(JsonNode node) {
				System.out.println(node);
			}
			
			@Override
			public void doAdded(JsonNode node) {
				System.out.println(node);
			}

			@Override
			public void doClose() {
				System.out.println("close");
			}
			
			

		};
		client.watchResourcesByFullkindAndNamespace("Pod", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
//		client.watchResourcesByFullkindAndNamespace("apiextensions.k8s.io.CustomResourceDefinition", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
		
	}
}
