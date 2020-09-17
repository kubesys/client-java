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
		KubernetesClient client = createClient1();
		
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

		};
		client.watchResources("Pod", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
		int i = 3;
		while (i-- > 0) {
			System.out.println(client.listResources("Namespace"));
			Thread.sleep(30*60*1000);
		}
	}


}
