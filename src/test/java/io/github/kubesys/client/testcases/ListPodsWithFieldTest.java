/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class ListPodsWithFieldTest extends AbstractKubernetesClientTest {

	
	/**
	 * see command `kubectl api-resources`
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		Map<String, String> map= new HashMap<>();
		map.put("spec.schedulerName", "default-scheduler");
		// Just kind
		JsonNode listResourcesWithField = client.listResourcesWithField("Pod", map);
		System.out.println(listResourcesWithField.toPrettyString());
	}

}
