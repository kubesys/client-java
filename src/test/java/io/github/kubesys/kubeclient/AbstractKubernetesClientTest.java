/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
