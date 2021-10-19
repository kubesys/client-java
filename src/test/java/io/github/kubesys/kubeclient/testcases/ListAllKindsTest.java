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
public class ListAllKindsTest extends AbstractKubernetesClientTest {

	
	/**
	 * see command `kubectl api-resources`
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		// Just kind
		System.out.println(client.getKinds().toPrettyString());
		// fullKind = group + kind
		System.out.println(client.getFullKinds().toPrettyString());
		// all supported operations for a kind
		System.out.println(client.getKindDesc().toPrettyString());
		
	}

}
