/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.testcases;



import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.kubesys.kubeclient.AbstractKubernetesClientTest;
import io.github.kubesys.kubeclient.KubernetesClient;


/**
 * @author wuheng09@gmail.com
 *
 */
public class GetCaBundleTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		JsonNode json = client.getResource("ConfigMap", "kube-system", "extension-apiserver-authentication");
		String cert = json.get("data").get("client-ca-file").asText();
		System.out.println(Base64.getEncoder().encodeToString(cert.getBytes()));
	}

}
