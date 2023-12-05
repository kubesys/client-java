/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.unit;


import io.github.kubesys.client.KubernetesClient;


/**
 * @author wuheng09@gmail.com
 *
 */
public class GetAllKindDescExample extends AbstractClient {

	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		System.out.println(client.getKinds().toPrettyString());
		System.out.println("------------------------------------");
		System.out.println(client.getKindDesc().toPrettyString());
	}

}
