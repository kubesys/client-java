/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.jkubefrk.v3;

import io.github.kubesys.KubernetesClient;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClientTest {

	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");
	}

}
