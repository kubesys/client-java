/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.unit;


import io.github.kubesys.client.KubernetesClient;


/**
 * @author wuheng09@gmail.com
 *
 */
public class ConnectionPoolExample extends AbstractClient {

	static String CRD_TEMP = "{\r\n"
			+ "  \"apiVersion\": \"apiextensions.k8s.io/v1\",\r\n"
			+ "  \"kind\": \"CustomResourceDefinition\",\r\n"
			+ "  \"metadata\": {\r\n"
			+ "    \"name\": \"#PLURAL#.doslab.io\"\r\n"
			+ "  },\r\n"
			+ "  \"spec\": {\r\n"
			+ "    \"group\": \"doslab.io\",\r\n"
			+ "    \"names\": {\r\n"
			+ "      \"kind\": \"#KIND#\",\r\n"
			+ "      \"plural\": \"#PLURAL#\",\r\n"
			+ "      \"shortNames\": [\r\n"
			+ "        \"#KIND_LOWCASE#\"\r\n"
			+ "      ],\r\n"
			+ "      \"singular\": \"#KIND_LOWCASE#\"\r\n"
			+ "    },\r\n"
			+ "    \"scope\": \"Namespaced\",\r\n"
			+ "    \"versions\": [\r\n"
			+ "      {\r\n"
			+ "        \"name\": \"v1\",\r\n"
			+ "        \"served\": true,\r\n"
			+ "        \"storage\": true,\r\n"
			+ "        \"schema\": {\r\n"
			+ "          \"openAPIV3Schema\": {\r\n"
			+ "            \"type\": \"object\",\r\n"
			+ "            \"properties\": {\r\n"
			+ "              \"spec\": {\r\n"
			+ "                \"type\": \"object\",\r\n"
			+ "                \"x-kubernetes-preserve-unknown-fields\": true\r\n"
			+ "              }\r\n"
			+ "            }\r\n"
			+ "          }\r\n"
			+ "        }\r\n"
			+ "      }\r\n"
			+ "    ]\r\n"
			+ "  }\r\n"
			+ "}";
	
	static String INS_TEMP = "{\r\n"
			+ "  \"apiVersion\": \"doslab.io/v1\",\r\n"
			+ "  \"kind\": \"#KIND#\",\r\n"
			+ "  \"metadata\": {\r\n"
			+ "    \"name\": \"#KIND_LOWCASE#\"\r\n"
			+ "  },\r\n"
			+ "  \"spec\": {\r\n"
			+ "  }\r\n"
			+ "}";
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
//		for (int i = 0; i < 200; i++) {
//			String CRD = CRD_TEMP.replaceAll("#KIND#", "TEST" + i)
//							.replaceAll("#PLURAL#", "tests" + i)
//							.replaceAll("#KIND_LOWCASE#", "test" + i);
//			System.out.println(CRD);
//			client.createResourceByJson(CRD);
//		}
//		
//		Thread.sleep(10000);
		
//		for (int i = 0; i < 200; i++) {
//			String INS = INS_TEMP.replaceAll("#KIND#", "TEST" + i)
//							.replaceAll("#KIND_LOWCASE#", "test" + i);
//			System.out.println(INS);
//			client.createResourceByJson(INS);
//		}
		
		for (int i = 0; i < 200; i++) {
			try {
				client.deleteResourceByName(
						"apiextensions.k8s.io.CustomResourceDefinition", 
						"tests" + i + ".doslab.io");
			} catch (Exception ex) {
				
			}
		}
		
//		System.out.println(client.getFullKinds());
	}

}
