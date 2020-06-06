/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.watchers.AutoDiscoverCustomizedResourcesWacther;

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
//		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");
		KubernetesClient client = new KubernetesClient("https://www.cloudplus.io:6443/",
				"eyJhbGciOiJSUzI1NiIsImtpZCI6IjZMbjZOUGxaZHZBamRfY2tPSUlCOGhoRXBwcWpvQjlFQ1RPU3NzZzhmeXcifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi00N2Y2ZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImYzZjRkYjRlLTUzNDYtNDc0NS1iOWM1LTdhMTJmMzk5MDI5YyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.Kt31obAmLePHJWO1Y_krp-h3KRDQFd34bunG_5u-mrDk5YP7EBJ87HbNSzNpZJe-_wZQDE_ZNMprpabfz19K3D5VrZjuq1g1pwcYTpxyaN_QjzVRBx7B2lPJmKNXeA-godT8yfbQDMtiMw9uyksLg8qDMUHP5VI-CH2KSTkRgqbaU5OoAkwy2niR3S9atsVcaPCzp1ab36XLvTLckgGSTJt5uHnFfGSmWS4Ako8aM5HVVox6Hz55OgiyRUbc7c-ED39itQHDkUOgKNUXkX9saW38l5Xn9OG_MWkpyJD7GQxbQJf2I36tgM0io1c08IGTFRLcSDB_YflDeyFqJT5aDA");
		System.out.println(client.getConfig().getKind2NameMapping().size());
		client.watchResources(AutoDiscoverCustomizedResourcesWacther.TARGET_KIND, 
								AutoDiscoverCustomizedResourcesWacther.TARGET_NAMESPACE, 
								new AutoDiscoverCustomizedResourcesWacther(client));
		
//		updateStatus(client);
//		create(client);
//		update(client);
//		get(client);
//		delete(client);
//		list(client);
		
	}


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
