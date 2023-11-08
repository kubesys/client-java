/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.examples;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class ListPodsWithFieldExample extends AbstractClient {

	
	/**
	 * see command `kubectl api-resources`
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		//注意：并不是所有filed都可以被查询
		Map<String, String> validMap= new HashMap<>();
		validMap.put("spec.schedulerName", "default-scheduler");
		validMap.put("spec.nodeName", "ecs-2503");
		JsonNode listResourcesWithField = client.listResourcesByField("Pod", validMap);
		System.out.println(listResourcesWithField.toPrettyString());
		
		//如何filed不能被查看，则报401错误，具体哪个filed可以被查询参照
		Map<String, String> invalidMap= new HashMap<>();
		invalidMap.put("spec.hostNetwork", "true");
		client.listResourcesByField("Pod", invalidMap);
	}

}
