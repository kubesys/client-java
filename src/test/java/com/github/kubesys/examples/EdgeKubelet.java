/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.examples;

import java.net.InetAddress;

import com.github.kubesys.AbstractKubernetesClientTest;
import com.github.kubesys.KubernetesClient;

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
			client.createResource(NODE.replace("#NAME", hostname));
		}
	}

}
