/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubesys.watchers.AutoDiscoverCustomizedResourcesWacther;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesCRDWatcherTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2(null);
		client.watchResources("CustomResourceDefinition", new AutoDiscoverCustomizedResourcesWacther(client) {

			@Override
			public void doAdded(JsonNode node) {
				System.out.println(node.toPrettyString());
			}

			@Override
			public void doDeleted(JsonNode node) {
			}

			@Override
			public void doModified(JsonNode node) {
			}
			
			
		});
		
	}

}
