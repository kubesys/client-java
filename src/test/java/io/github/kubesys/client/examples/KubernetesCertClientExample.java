/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.examples;

import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023.12.05
 *
 */
public class KubernetesCertClientExample {

	/**
	 * login to master, and exec 'cat /root/.kube/config | grep server | awk -F'server:' '{print$2}''
	 * for example, https://192.168.0.1:6443
	 */
	static String url = "";
	
	/**
	 * login to master, 
	 * 1. kubectl apply -f https://raw.githubusercontent.com/kubesys/client-java/master/token.yaml
	 * 2. kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep kubernetes-client | awk '{print $1}') | grep "token:" | awk -F":" '{print$2}' | sed 's/ //g'
	 */
	static String token = "";
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient(url, token);
		System.out.println(client.getKinds());
	}

}
