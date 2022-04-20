/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;



import java.util.HashMap;
import java.util.Map;

import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesClient.Rule;


/**
 * @author wuheng09@gmail.com
 *
 */
public class CreateWebHookTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		Map<String, String> labels = new HashMap<>();
		labels.put("app", "webhook");
		
		Rule[] rules = new Rule[1];
		Rule rule = new Rule();
		rule.setApiGroups(new String[] {""});
		rule.setApiVersions(new String[] {"v1"});
		rule.setOperations(new String[] {"CREATE"});
		rule.setResources(new String[] {"pods"});
		rule.setScope("Namespaced");
		rules[0] = rule;
 		
//		System.out.println(client.createWebhook("hello-webhook.leclouddev.com", "/mutate", "hello-webhook-service", "default", labels, rules).toPrettyString());
		System.out.println(client.createWebhook("hello-webhook.leclouddev.com", "/mutate", "https://133.133.134.22:90", labels, rules).toPrettyString());

	}

}
