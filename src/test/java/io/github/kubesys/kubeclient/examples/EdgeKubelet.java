/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.examples;

import java.net.InetAddress;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kubesys.kubeclient.AbstractKubernetesClientTest;
import io.github.kubesys.kubeclient.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class EdgeKubelet extends AbstractKubernetesClientTest {

	
	static final String NODE = "{\r\n"
			+ "	\"apiVersion\": \"v1\",\r\n"
			+ "	\"kind\": \"Node\",\r\n"
			+ "	\"metadata\": {\r\n"
			+ "		\"labels\": {\r\n"
			+ "			\"node-role.kubernetes.io/agent\": \"\",\r\n"
			+ "			\"node-role.kubernetes.io/edge\": \"\"\r\n"
			+ "		},\r\n"
			+ "		\"name\": \"#NAME\"\r\n"
			+ "	}\r\n"
			+ "}";
	
	/**
	 * see command `kubectl api-resources`
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		KubernetesClient client = createClient2(null);

		String hostname = InetAddress.getLocalHost()
					.getHostName().toLowerCase();
		
		if (!client.hasResource("Node", null, hostname)) {
			client.createResource(new ObjectMapper().readTree(NODE.replace("#NAME", hostname)));
		}
	}

}
