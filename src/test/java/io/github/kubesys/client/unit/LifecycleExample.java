/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.unit;


import io.github.kubesys.client.KubernetesClient;


/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/12/04
 *
 */
public class LifecycleExample extends AbstractClient {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient1(null);
		System.out.println(client.registerResource("doslab.io", "Test", "tests"));
	}


}
