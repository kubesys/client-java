/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesMetadataTest extends AbstractKubernetesClientTest {

	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2(null);
		System.out.println(client.getFullKinds().toPrettyString());
	}

}
