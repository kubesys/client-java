/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.io.File;

import com.github.kubesys.KubernetesClient;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesWatcherTest {

	
	public static void main(String[] args) throws Exception {

		KubernetesClient client = KubernetesClient.getKubeClient(new File("confs/admin.conf"));
		WebSocketListener listener = new WebSocketListener() {

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				// TODO Auto-generated method stub
				super.onMessage(webSocket, text);
				
				System.out.println(text);
			}

			@Override
			public void onClosing(WebSocket webSocket, int code, String reason) {
				// TODO Auto-generated method stub
				super.onClosing(webSocket, code, reason);
				System.out.println("onClosing:" + reason);
			}

			@Override
			public void onClosed(WebSocket webSocket, int code, String reason) {
				// TODO Auto-generated method stub
				super.onClosed(webSocket, code, reason);
				System.out.println("onClosed:" + code + reason);
			}

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				// TODO Auto-generated method stub
				super.onFailure(webSocket, t, response);
				System.out.println(t + ":" + response);
			}
			
			
		};
		System.out.println(client.getResource("Pod", "default", "busybox"));
		client.watchResources("Pod", "", listener );
		System.out.println("Hello");
		
	}
}
