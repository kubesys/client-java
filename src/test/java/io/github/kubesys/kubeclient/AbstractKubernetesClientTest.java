/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://192.168.90.78:6443";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJvYXJkLXVzZXItdG9rZW4tNnE2bXgiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoia3Vib2FyZC11c2VyIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNjQ2YTliZjMtOTliYy00NzA2LWFjMDMtZGNhYzFlZTkzZmQyIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Omt1YmUtc3lzdGVtOmt1Ym9hcmQtdXNlciJ9.Byu2uMmEjqqXxiyEV_NL-DNRsVRK4SDWLZ0WmoHAPdcJfI9F7RpA78c1uQHoaQhO1kTyMFRBBXF2vbKywNzB6thgU9n1YUCZfaUOIAd99MVWbig3jgRx-QnyjuaPBSlwn55B49hgqfizR7bS-kaT_tets9PIbgNnVJxfQ0khyYpSzjbd7bP0sJsoyZARjR7Lhd5L_Qz0cdx1b3FljEiKX9yn3_o4VM9LRTtKkLrwGPPhZvk8kq7yxS1St3XPOzozvro2VOmScg_cbhXZrIE2aeQOqNLLP3BmecxyPBZnNuppnTv0kB4o-Ra93xkSmEveKl8LUce0IgeP3jCgco5P4g";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
