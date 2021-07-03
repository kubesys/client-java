/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://124.70.64.232:6443";
	
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6InlxREQ2RDhwN1dkUGFuVkhMVExLRlYzWklMWkIzZXNCY0h6QkZuZzhpa2cifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1ubmZoYiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjRmZjg2YjM2LWRiZDMtNDE3MC1iMzY5LWE1ZjUyMzRjZmM0MyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.jwthYHSSqd2P7iRiqoNse0YpBNT6yKAd8zMV2ZAWbY84xVD_SvcNqxpavymQNR_C22OJaF2emXWP5SYlP1o9WBuH2QQ0eQ9f5QiHAwGd5elmZ9mDeNwie2pycDCxyAg8iWlXqo9sdKYD4-LTO8z7wMQPX1lmECrC4dC0nawwq12F8jetpIUF7IDYBPa6jidjUYbQNFenaKwPanZR5PqCbQ7rKpJ3UQt4sxGcnwCGcOQDCT8f7LQuQfyJ-AMgyQOi9C_qsYWATy60MTYjbpOmfJfsL5Mdhwxnt1RL8eVoEB5bhgjMDQZL4AC0UR8mKgE4SEZmx0lkq3WwWZRQoI3pcA";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
