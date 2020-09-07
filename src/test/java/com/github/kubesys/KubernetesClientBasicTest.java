/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClientBasicTest {

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
	
	
	static String URL = "https://192.168.42.144:6443";
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImF2bEVKdnd3NTZQT1pzWVZsMlhfT0pXVDhZUmU4MW1JRmpfM3NXMnVLeEEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1iZmJoMiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjA1ZGE4MGU3LTk0OWUtNGY3ZC1iZjRjLTNiZWQzMzNjZGU0MiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.EvQvafDBvJgncSCJJDspuv6HdeET5ksfZ5-LFFjukCG5u8A9QcVk1-5t70-FrZZA_5cu7G65gfOqPGlkGf8hTGErEU66AN1keqCxymrVIKfwVjOD6RwHA6gW2hu-MHmL6Q2zQexwc2AGwlXIntRnhJmG5CDqsGMPB-VtvrCStBX05bTEo2_yuHZmyLfd907yzOnL6gGR3FqeHJzXllkn9pT0oxG8qCMNP5SkVdlaYM18NRusgBw0aEK25TNuozrAuVT_w5b4yuwxY5jeroEEWNqQCZy7txWi5e7N2cSIvipKvQVX8ZBbMsGcOx58t3Feobmf1Ug6RVVoFIIVMC5FmQ";
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient(URL, TOKEN);
		
		create(client);
//		updateStatus(client);
//		update(client);
//		get(client);
//		delete(client);
//		list(client);
		
	}


	protected static void updateStatus(KubernetesClient client) throws Exception {
		ObjectNode node = client.getResource("Pod", "default", "busybox").deepCopy();
		ObjectNode status = node.get("status").deepCopy();
		status.put("phase", "Pending");
		node.set("status", status);
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

	protected static void update(KubernetesClient client)
			throws Exception, JsonProcessingException, JsonMappingException {
		ObjectNode node = client.getResource("Pod", "default", "busybox").deepCopy();
		ObjectNode meta = node.get("metadata").deepCopy();
		ObjectNode labels = new ObjectMapper().createObjectNode();
		labels.put("test", "test");
		meta.set("labels", labels);
		node.set("metadata", meta);
		System.out.println(client.updateResource(new ObjectMapper().readTree(node.toString())));
	}
}
