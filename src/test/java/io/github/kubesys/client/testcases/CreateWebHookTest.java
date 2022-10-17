/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;



import java.util.HashMap;
import java.util.Map;

import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesClient.KubeRule;


/**
 * @author wuheng09@gmail.com
 *
 */
public class CreateWebHookTest extends AbstractKubernetesClientTest {

	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		Map<String, String> labels = new HashMap<>();
		labels.put("app", "webhook");
		
		KubeRule[] rules = new KubeRule[1];
		KubeRule kubeRule = new KubeRule();
		kubeRule.setApiGroups(new String[] {""});
		kubeRule.setApiVersions(new String[] {"v1"});
		kubeRule.setOperations(new String[] {"CREATE"});
		kubeRule.setResources(new String[] {"pods"});
		kubeRule.setScope("Namespaced");
		rules[0] = kubeRule;
 		
//		System.out.println(client.createWebhook("hello-webhook.leclouddev.com", "/mutate", "hello-webhook-service", "default", labels, rules).toPrettyString());
		System.out.println(client.createWebhook("hello-webhook.leclouddev.com", "/mutate", "https://133.133.134.22:90", labels, rules).toPrettyString());

	}

}
