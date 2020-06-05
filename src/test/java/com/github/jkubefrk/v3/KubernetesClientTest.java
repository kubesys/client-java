/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.jkubefrk.v3;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClientTest {

	static String CreateJSON = "{\r\n" + 
			"  \"apiVersion\": \"v1\",\r\n" + 
			"  \"kind\": \"Pod\",\r\n" + 
			"  \"metadata\": {\r\n" + 
			"    \"name\": \"busybox\"\r\n" + 
			"  },\r\n" + 
			"  \"spec\": {\r\n" + 
			"    \"containers\": [\r\n" + 
			"      {\r\n" + 
			"        \"image\": \"busybox\",\r\n" + 
			"        \"command\": [\r\n" + 
			"          \"sleep\",\r\n" + 
			"          \"3600\"\r\n" + 
			"        ],\r\n" + 
			"        \"imagePullPolicy\": \"IfNotPresent\",\r\n" + 
			"        \"name\": \"busybox\"\r\n" + 
			"      }\r\n" + 
			"    ],\r\n" + 
			"    \"restartPolicy\": \"Always\"\r\n" + 
			"  }\r\n" + 
			"}";
	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");
		System.out.println(client.getConfig().getKind2ApiPrefixMapping());
//		
//		System.out.println(client.createResource(new ObjectMapper().readTree(CreateJSON)));
		ObjectNode node = client.getResource("Pod", "default", "busybox").deepCopy();
		ObjectNode meta = node.get("metadata").deepCopy();
		ObjectNode labels = new ObjectMapper().createObjectNode();
		labels.put("test", "test");
		meta.put("labels", labels);
		node.put("metadata", meta);
		System.out.println(client.updateResource(new ObjectMapper().readTree(node.toString())));
//		System.out.println(client.getResource("Pod", "default", "busybox"));
//		System.out.println(client.deleteResource("Pod", "default", "busybox"));
//		
//		System.out.println(client.listResources("Pod"));
		
	}
}
