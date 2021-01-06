/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://39.106.40.190:6443/";
	
	
	static String TOKEN = "";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2() throws Exception {
		return new KubernetesClient(URL, TOKEN);
	}

}
