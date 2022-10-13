/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;

import java.util.HashMap;
import java.util.Map;

import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class ListNodesTest extends AbstractKubernetesClientTest {

	
	/**
	 * see command `kubectl api-resources`
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		Map<String, String> map= new HashMap<>();
		map.put("host", "vm.node133");
		// Just kind
		System.out.println(client.listResources("Node").get("items").size());
		System.out.println(client.listResourcesWithLabel("Node", map).get("items").size());
		
	}

}
