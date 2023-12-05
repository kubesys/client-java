/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.unit;

import java.io.File;

import io.github.kubesys.client.KubernetesAnalyzer;
import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractClient {

	public static KubernetesClient createClient1(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(System.getenv("url"), System.getenv("token")) : 
						new KubernetesClient(System.getenv("url"), System.getenv("token"), ana);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(System.getenv("url"), System.getenv("username"), System.getenv("password")) : 
						new KubernetesClient(System.getenv("url"), System.getenv("username"), System.getenv("password"), ana);
	}
	
	public static KubernetesClient createClient3(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(new File("admin.conf")) : new KubernetesClient(new File("admin.conf"), ana);
	}

}
