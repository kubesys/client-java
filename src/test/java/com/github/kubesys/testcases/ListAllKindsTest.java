/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.testcases;

import com.github.kubesys.AbstractKubernetesClientTest;
import com.github.kubesys.KubernetesClient;

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
		KubernetesClient client = createClient2(null);
		// Just kind
		System.out.println(client.getKinds().toPrettyString());
		// fullKind = group + kind
		System.out.println(client.getFullKinds().toPrettyString());
		// all supported operations for a kind
		System.out.println(client.getKindDesc().toPrettyString());
		
	}

}
