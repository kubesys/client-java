/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.examples;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesWatcher;


/**
 * @author wuheng09@gmail.com
 *
 */
public class RegisterResourceExample extends AbstractClient {

	static String CreateYaml = "apiVersion: v1\r\n"
			+ "kind: Pod\r\n"
			+ "metadata:\r\n"
			+ "  name: busybox\r\n"
			+ "  namespace: default\r\n"
			+ "  labels:\r\n"
			+ "    test: test\r\n"
			+ "spec:\r\n"
			+ "  containers:\r\n"
			+ "    - image: busybox\r\n"
			+ "      env:\r\n"
			+ "        - name: abc\r\n"
			+ "          value: abc\r\n"
			+ "      command:\r\n"
			+ "        - sleep\r\n"
			+ "        - '3600'\r\n"
			+ "      imagePullPolicy: IfNotPresent\r\n"
			+ "      name: busybox\r\n"
			+ "  restartPolicy: Always";
	
	static String CreateJSON = "{\r\n"
			+ "	\"apiVersion\": \"v1\",\r\n"
			+ "	\"kind\": \"Pod\",\r\n"
			+ "	\"metadata\": {\r\n"
			+ "		\"name\": \"busybox\",\r\n"
			+ "		\"namespace\": \"default\",\r\n"
			+ "		\"labels\": {\r\n"
			+ "			\"test\": \"test\"\r\n"
			+ "		}\r\n"
			+ "	},\r\n"
			+ "	\"spec\": {\r\n"
			+ "		\"containers\": [{\r\n"
			+ "			\"image\": \"busybox\",\r\n"
			+ "			\"env\": [{\r\n"
			+ "				\"name\": \"abc\",\r\n"
			+ "				\"value\": \"abc\"\r\n"
			+ "			}],\r\n"
			+ "			\"command\": [\r\n"
			+ "				\"sleep\",\r\n"
			+ "				\"3600\"\r\n"
			+ "			],\r\n"
			+ "			\"imagePullPolicy\": \"IfNotPresent\",\r\n"
			+ "			\"name\": \"busybox\"\r\n"
			+ "		}],\r\n"
			+ "		\"restartPolicy\": \"Always\"\r\n"
			+ "	}\r\n"
			+ "}";
	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);

		System.out.println("------List");
		list(client);
		System.out.println("------Create");
		create(client);
//		updateStatus(client);
		System.out.println("------Update");
		update(client);
		System.out.println("------Get");
		get(client);
		System.out.println("------Watch");
		watch(client);
		System.out.println("------Delete");
		delete(client);
	}


	static void watch(KubernetesClient client) throws Exception {
		client.watchResourcesByFullkind("Namespace", new KubernetesWatcher(client) {
			
			@Override
			public void doModified(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
			
			@Override
			public void doDeleted(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
			
			@Override
			public void doClose() {
				System.out.println("I am close");
			}
			
			@Override
			public void doAdded(JsonNode node) {
				System.out.println(node.toPrettyString());
			}
		});
	}


	protected static void updateStatus(KubernetesClient client) throws Exception {
		ObjectNode node = client.getResourceByNamespaceAndName("Pod", "default", "busybox").deepCopy();
		JsonNode status = node.get("status");
		((ObjectNode) status).put("phase", "Testing");
		System.out.println(client.updateResourceStatus(node));
		// or
//		ObjectNode node = client.getResource("apps.Deployment", "default", "busybox").deepCopy();
//		ObjectNode status = (ObjectNode) node.get("status");
//		status.put("readyReplicas", 2);
//		status.put("replicas", 2);
//		System.out.println(client.updateResourceStatus(node).toPrettyString());
	}


	protected static void list(KubernetesClient client) throws Exception {
//		System.out.println(client.listResources("Deployment").toPrettyString());
		// or
		System.out.println(client.listResources("apps.Deployment").toPrettyString());
	}


	protected static void delete(KubernetesClient client) throws Exception {
//		System.out.println(client.deleteResource("Deployment", "default", "busybox").toPrettyString());
		// or
//		System.out.println(client.deleteResource("apps.Deployment", "default", "busybox").toPrettyString());
		System.out.println(client.deleteResourceByNamespaceAndName("Pod", "default", "busybox").toPrettyString());
	}


	protected static void get(KubernetesClient client) throws Exception {
//		System.out.println(client.getResource("Deployment", "default", "busybox").toPrettyString());
		// or
//		System.out.println(client.getResource("apps.Deployment", "default", "busybox").toPrettyString());
		System.out.println(client.getResourceByNamespaceAndName("Pod", "default", "busybox"));
	}


	protected static void create(KubernetesClient client) throws Exception {
//		System.out.println(client.createResource(new ObjectMapper().readTree(CreateJSON)));
		System.out.println(client.createResourceByYaml(CreateYaml));
	}
	
	protected static void create(KubernetesClient client, JsonNode json) throws Exception {
		System.out.println(client.createResource(json));
	}

	protected static void update(KubernetesClient client) throws Exception {
		ObjectNode node = client.getResourceByNamespaceAndName("Deployment", "default", "busybox").deepCopy();
		// or
//		ObjectNode node = client.getResource("apps.Deployment", "default", "busybox").deepCopy();
//		ObjectNode spec = (ObjectNode) node.get("spec");
//		spec.put("replicas", 2);
//		System.out.println(client.updateResource(new ObjectMapper()
//				.readTree(node.toString())).toPrettyString());
	}
}
