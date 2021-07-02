/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://119.8.188.235:6443";
	
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjlveVhDVVc4STdYclJwb1p4R3pBWjVVRm4zUVh2VzIxa1dRSlFRRnVkeEEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi01bXB3eiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImI0MDIwNDliLWMyZTktNDU2MC1hMGU3LTk4MTg0YTYzZmZkNyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.eUGGSZImRaj6vj4XxfnnymJY2Wk6YoGRmKp1-gvlW5oZKnU5echJcNmGUqlYCLbrtTYZObVpOKujieHpgpI5tMbEBL7EbkQbpk9m7h0XxBHdMyCKzQXZn0JHBBOfymkzr_umAq_m3B7pa6R9A7bcnE9YS10F4grmIZP6nrpZcYGm61JJVzn93z6yoOZ2n2iOufDObZUvKWBrwMp91lbMGqhIbxDkIbeQ5gXECc8QoIIsn-PLjSMsBWz5kgJqoVTwentSc3nOI0JouQ7oUlM6r6VLLpwLZQUtOJ-yFNt7ZBhThC1foFCuNDgQ9MaBg7xu1rSe8hqf_tlbQKJGAPnnvA";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
