/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesCRDObjectWatcherTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2(null);
//		client.watchResources("Pod", new KubernetesObjectWatcher<Pod>(client) {
//
//			@Override
//			public void doObjectAdded(Pod node) {
//				System.out.println(node);
//			}
//
//			@Override
//			public void doObjectModified(Pod node) {
//				
//			}
//
//			@Override
//			public void doObjectDeleted(Pod node) {
//				
//			}
//
//			@Override
//			public void doClose() {
//				// TODO Auto-generated method stub
//				
//			}
//
//		});
	}

}
