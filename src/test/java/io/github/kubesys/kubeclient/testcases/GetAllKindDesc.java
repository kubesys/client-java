/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.testcases;



import io.github.kubesys.kubeclient.AbstractKubernetesClientTest;
import io.github.kubesys.kubeclient.KubernetesClient;


/**
 * @author wuheng09@gmail.com
 *
 */
public class GetAllKindDesc extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient(null);
		System.out.println(client.getKinds().toPrettyString());
		System.out.println(client.getKindDesc().toPrettyString());
	}

}
