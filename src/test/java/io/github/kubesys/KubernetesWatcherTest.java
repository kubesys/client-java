/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import io.github.kubesys.KubernetesClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesWatcherTest {

	
	public static void main(String[] args) throws Exception {
//		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");

		KubernetesClient client = new KubernetesClient("https://www.cloudplus.io:6443/",
				"XXXX");
		WebSocketListener listener = new WebSocketListener() {

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				// TODO Auto-generated method stub
				super.onMessage(webSocket, text);
				
				System.out.println(text);
			}
			
		};
		System.out.println(client.getResource("VirtualMachine", "default", "magic11"));
		client.watchResources("VirtualMachine", "", listener );
		System.out.println("Hello");
		
	}
}
