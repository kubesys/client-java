/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL1 = "http://39.106.40.190:8888/";
	
	static String URL2 = "https://192.168.42.144:6443";
	
	static String TOKEN = "";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL1);
	}
	
	public static KubernetesClient createClient2() throws Exception {
		return new KubernetesClient(URL2, TOKEN);
	}

}
