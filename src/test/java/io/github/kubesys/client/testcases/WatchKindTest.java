/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesConstants;
import io.github.kubesys.client.KubernetesWatcher;


/**
 * @author wuheng09@gmail.com
 *
 */
public class WatchKindTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		
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
				try {
					client.registerResource(node);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void doClose() {
				System.out.println("close");
			}

		};
//		client.watchResources("Pod", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
		client.watchResources("apiextensions.k8s.io.CustomResourceDefinition", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
//		try {
//			client.listResources("doslab.io.VirtualMachine");
//		} catch (Exception ex) {
//			System.out.println("暂时不支持doslab.io.VirtualMachine");
//		}
////		
//		Thread.sleep(30000);
////		
//		System.out.println(client.listResources("doslab.io.VirtualMachine"));
		// or
//		client.watchResources("apps.Deployment", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
		
	}
}
