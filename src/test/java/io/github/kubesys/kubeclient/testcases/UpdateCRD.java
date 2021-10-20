/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.testcases;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.kubeclient.AbstractKubernetesClientTest;
import io.github.kubesys.kubeclient.KubernetesClient;
import io.github.kubesys.kubewriter.KubernetesWriter;


/**
 * @author wuheng09@gmail.com
 *
 */
public class UpdateCRD extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		JsonNode items = client.listResources("CustomResourceDefinition").get("items");
		for (int i = 0; i < items.size(); i++) {
			JsonNode json = items.get(i);
			String name = json.get("metadata").get("name").asText();
			if (name.contains("doslab.io")) {
				System.out.println(json.toPrettyString());
				((ObjectNode) json.get("metadata")).remove("annotations");
				((ObjectNode) json.get("metadata")).remove("creationTimestamp");
				((ObjectNode) json.get("metadata")).remove("generation");
				((ObjectNode) json.get("metadata")).remove("managedFields");
				((ObjectNode) json.get("metadata")).remove("resourceVersion");
				((ObjectNode) json.get("metadata")).remove("uid");
				((ObjectNode) json).remove("status");
				((ObjectNode) json).put("apiVersion", "apiextensions.k8s.io/v1");
				((ObjectNode) json).put("kind", "CustomResourceDefinition");
				new KubernetesWriter().writeAsYaml(name + "-crd.yaml", json);
			}
		}
	}

}
