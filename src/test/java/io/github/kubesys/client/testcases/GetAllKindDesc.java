/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.testcases;



import io.github.kubesys.client.AbstractKubernetesClientTest;
import io.github.kubesys.client.KubernetesClient;


/**
 * @author wuheng09@gmail.com
 *
 */
public class GetAllKindDesc extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		System.out.println(client.getKinds().toPrettyString());
		System.out.println(client.getKindDesc().toPrettyString());
	}

}
