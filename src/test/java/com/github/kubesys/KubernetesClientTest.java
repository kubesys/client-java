/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kubesys.KubernetesClient;


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
		KubernetesClient client = new KubernetesClient("https://www.cloudplus.io:6443/",
				"XX");
		
		updateStatus(client);
		create(client);
		update(client);
		get(client);
		delete(client);
		list(client);
		
	}


	@SuppressWarnings("deprecation")
	protected static void updateStatus(KubernetesClient client) throws Exception {
		ObjectNode node = client.getResource("Pod", "default", "busybox").deepCopy();
		ObjectNode status = node.get("status").deepCopy();
		status.put("phase", "Pending");
		node.put("status", status);
		System.out.println(client.updateResourceStatus(node));
	}


	protected static void list(KubernetesClient client) throws Exception {
		System.out.println(client.listResources("Pod"));
	}


	protected static void delete(KubernetesClient client) throws Exception {
		System.out.println(client.deleteResource("Pod", "default", "busybox"));
	}


	protected static void get(KubernetesClient client) throws Exception {
		System.out.println(client.getResource("Pod", "default", "busybox"));
	}


	protected static void create(KubernetesClient client)
			throws Exception, JsonProcessingException, JsonMappingException {
		System.out.println(client.createResource(new ObjectMapper().readTree(CreateJSON)));
	}


	@SuppressWarnings("deprecation")
	protected static void update(KubernetesClient client)
			throws Exception, JsonProcessingException, JsonMappingException {
		ObjectNode node = client.getResource("Pod", "default", "busybox").deepCopy();
		ObjectNode meta = node.get("metadata").deepCopy();
		ObjectNode labels = new ObjectMapper().createObjectNode();
		labels.put("test", "test");
		meta.put("labels", labels);
		node.put("metadata", meta);
		System.out.println(client.updateResource(new ObjectMapper().readTree(node.toString())));
	}
}
