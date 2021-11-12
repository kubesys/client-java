/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.io.File;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://133.133.135.32:6443";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRGbWFDMDVCVEx0Rm0tQ1Y2dWFKanhPUG51Q0VQalBnSzZpb0lWSk5EVUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1sNDZqZyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjZmN2JmMTVjLTllMjYtNDVkYy1hNWNiLTU1N2ZjZmM4NzU0YiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.of0PleOkv44pshSoiSU4NJbUdR-wIuuAziLoCf-gu76yxfq7sV22tezkQU7mxT67LZx6lN_SdW7QYdHXSRqUhNJH-tJcz6CiIMJ9KTvizvH78OHYdyll2tFFlwqrzl3HCTd-LW66V9kp1_D58m8W7Qu9GKzuQBLhH37DiUO-T5hj8ZH6ff3ESJ5Hcll89WjyC20VK2kzGA6rV1SUVe8kMx2-o6y6FPSFVtnJfPgxkj9H6qVD_pv7T3Beb3nuQkGC_1bMkCIr140Oi6pS96Rli105h7ptUagDnXD0AiI7gGVE2Y1IgUjsXQKjA2gZq_DBeVkt0q9Y1L-yxwJRYuLfkQ";

	public static KubernetesClient createClient1(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(new File("admin.conf")) : new KubernetesClient(new File("admin.conf"), ana);
	}

}
