/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kubesys.KubernetesClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesWatcherTest {

	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");
		WebSocketListener listener = new WebSocketListener() {

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				// TODO Auto-generated method stub
				super.onMessage(webSocket, text);
				
				System.out.println(text);
			}
			
		};
		client.watchResources("Pod", "default", listener );
		
	}
}
