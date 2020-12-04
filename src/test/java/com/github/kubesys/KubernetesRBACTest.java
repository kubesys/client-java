/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesRBACTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		
		KubernetesClient all = new KubernetesClient("https://39.106.40.190:6443", 
				"");
		
		KubernetesClient limited = new KubernetesClient("https://39.106.40.190:6443", 
				"", 
				all.getAnalyzer());
		
		KubernetesWatcher watcher = new KubernetesWatcher(limited) {
			
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
		limited.watchResources("Namespace", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
	
	}


}
