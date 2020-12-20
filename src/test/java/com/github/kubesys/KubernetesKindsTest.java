/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.github.kubesys.watchers.AutoDiscoverCustomizedResourcesWacther;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesKindsTest extends AbstractKubernetesClientTest {

	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2();
		System.out.println(client.getKinds());
		System.out.println(client.getLatestApiVersion("Pod"));
		System.out.println(client.getPlural("Pod"));
		
	}

}
