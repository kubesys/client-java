/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesResourceLifecycleTest extends AbstractKubernetesClientTest {

	static String CreateJSON = "{\r\n"
			+ "  \"apiVersion\": \"v1\",\r\n"
			+ "  \"kind\": \"Pod\",\r\n"
			+ "  \"metadata\": {\r\n"
			+ "    \"name\": \"busybox\",\r\n"
			+ "    \"namespace\": \"default\"\r\n"
			+ "  },\r\n"
			+ "  \"spec\": {\r\n"
			+ "    \"containers\": [\r\n"
			+ "      {\r\n"
			+ "        \"image\": \"busybox\",\r\n"
			+ "        \"env\": [{\r\n"
			+ "           \"name\": \"abc\",\r\n"
			+ "           \"value\": \"abc\"\r\n"
			+ "        }],\r\n"
			+ "        \"command\": [\r\n"
			+ "          \"sleep\",\r\n"
			+ "          \"3600\"\r\n"
			+ "        ],\r\n"
			+ "        \"imagePullPolicy\": \"IfNotPresent\",\r\n"
			+ "        \"name\": \"busybox\"\r\n"
			+ "      }\r\n"
			+ "    ],\r\n"
			+ "    \"restartPolicy\": \"Always\"\r\n"
			+ "  }\r\n"
			+ "}";
	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2(null);

//		create(client);
//		updateStatus(client);
//		update(client);
		create(client, get(client));
//		list(client);
//		delete(client);
		
	}


	protected static void updateStatus(KubernetesClient client) throws Exception {
//		ObjectNode node = client.getResource("Deployment", "default", "busybox").deepCopy();
		// or
		ObjectNode node = client.getResource("apps.Deployment", "default", "busybox").deepCopy();
		ObjectNode status = (ObjectNode) node.get("status");
		status.put("readyReplicas", 2);
		status.put("replicas", 2);
		System.out.println(client.updateResourceStatus(node).toPrettyString());
	}


	protected static void list(KubernetesClient client) throws Exception {
//		System.out.println(client.listResources("Deployment").toPrettyString());
		// or
		System.out.println(client.listResources("apps.Deployment").toPrettyString());
	}


	protected static void delete(KubernetesClient client) throws Exception {
//		System.out.println(client.deleteResource("Deployment", "default", "busybox").toPrettyString());
		// or
		System.out.println(client.deleteResource("apps.Deployment", "default", "busybox").toPrettyString());
	}


	protected static JsonNode get(KubernetesClient client) throws Exception {
//		System.out.println(client.getResource("Deployment", "default", "busybox").toPrettyString());
		// or
//		System.out.println(client.getResource("apps.Deployment", "default", "busybox").toPrettyString());
		return client.getResource("Pod", "default", "busybox");
	}


	protected static void create(KubernetesClient client) throws Exception {
		System.out.println(client.createResource(new ObjectMapper().readTree(CreateJSON)));
	}
	
	protected static void create(KubernetesClient client, JsonNode json) throws Exception {
		System.out.println(client.createResource(json));
	}

	protected static void update(KubernetesClient client) throws Exception {
//		ObjectNode node = client.getResource("Deployment", "default", "busybox").deepCopy();
		// or
		ObjectNode node = client.getResource("apps.Deployment", "default", "busybox").deepCopy();
		ObjectNode spec = (ObjectNode) node.get("spec");
		spec.put("replicas", 2);
		System.out.println(client.updateResource(new ObjectMapper()
				.readTree(node.toString())).toPrettyString());
	}
}
