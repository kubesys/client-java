/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.net.InetAddress;


/**
 * @author wuheng09@gmail.com
 *
 */
public class HostnameTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		System.out.println(InetAddress.getLocalHost().getHostName());
	}


}
