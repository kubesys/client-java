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
		KubernetesClient client = new KubernetesClient("http://www.cloudplus.io:8888/");

//		KubernetesClient client = new KubernetesClient("https://www.cloudplus.io:6443/",
//				"eyJhbGciOiJSUzI1NiIsImtpZCI6IjBUUGxHRE5YdG5FRkswS3NVNHhEbHctTDFwbXZOY09LQ3ZydHBUWTVfR00ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1nNWIyYyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImU5YmRlNmJhLTE0ODAtNGViMC1hMWYxLTFkMjE1YWFjNTk4ZCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.KjssR2onK6yqk4dxf6r8yFemS9HYqxrguTzMqgkwnSBZMf9x4VrH0WBmaPokcEKBigCsH12Ey501eEWvy0jBbQrwZQzc-f6AdXwI9EOX7anH0jhB7EZketEJlBlSGOgc6FVFIveOkRGMvMdjuE7ZusVj_3RsddiOqErDCaE7-n1NwWnkVb-T18w-8zLSI9QQZ5YI4VnAnuqINYOao4dds8jfV3wnl0hFzxzLDq6V8839PurSHwOa7yiVWTc3WUzu4TtRcZhLQ6irOB3BnYP4oDdD4ISeWXrinabux-zj4LNM3nxuKVB-zIRGNP_mWJCr4S4xzIimhiDXWsXLuyVxdg");
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
