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

	static String URL = "https://39.100.71.73:6443";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjBRXzAzZjU2Vml0d1FtTUxNd3Fwc0x2VHRSUkpxVXpzbjVKbGp1NXZMeU0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi00bTlwbCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImU1YjUyZjhjLTkxOGQtNGU4OC04MDQzLTM0N2ZkZjhmOWIxZiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.BNbA4v3D2B0qRG9uDbDjyo6A1x9LIgRLKxJgtbNFL3YUhHYQ7THvIgt_ikcKfo0pPbNt_I8AKEh1_jGNFXUr9K_k6CLhBdJ9uY6ju6-fmGxk6O8Hn_ayi235eIXyMfiIerd2rH-cnrFJna8jw8m_sfXiCCImmWmIQXubwxGRLtf7y-bwBrdnBgpaDnViYwqzDhddaXL1C82mtbBVwH8yB3KYrVOQYPJJJW0yHexSyrNlmyO_wLDDqD3MBqxc3HakOQLwVtHusgFvGyve5-Z4LpJ9tBhRQ8NQWxrF3DEcnazahy1gEZ6UauyERHA-zJGvvCi9k2bVnYi97wUfvGaYEg";

	public static KubernetesClient createClient1(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(new File("admin.conf")) : new KubernetesClient(new File("admin.conf"), ana);
	}

}
