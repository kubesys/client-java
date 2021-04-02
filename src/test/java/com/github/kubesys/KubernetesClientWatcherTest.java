/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClientWatcherTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2(null);
		
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
		client.watchResources("Deployment", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		// or
//		client.watchResources("apps.Deployment", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
	}


}
