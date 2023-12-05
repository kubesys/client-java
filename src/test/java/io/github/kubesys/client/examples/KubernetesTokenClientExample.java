/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.examples;

import java.io.File;

import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023.12.05
 *
 */
public class KubernetesTokenClientExample {

	/**
	 * login to master, copy /root/.kube/config to local path config/config.yaml
	 */
	static String file = "config/config.yaml";
	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient(new File(file));
		System.out.println(client.getKinds());
	}

}
