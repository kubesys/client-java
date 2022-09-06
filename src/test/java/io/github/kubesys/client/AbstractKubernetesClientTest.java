/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import java.io.File;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://120.46.180.58:6443/";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IlNXU0pBMkczanNmdjhaOUlJZmUzUXRHUHpnUEx4bjlGREsydWxaTTFiMDQifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6Ijg5ZGQ1ZjBhLTYyM2EtNGZhMi05MjQ3LTBmOTZiZGNmMGY3MSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.DNNSlT7jMYLR7FBrh58H-E04vwtYhna_br4PgHcS1fjnUGIflqJNco4AIiGXRLk53YvM5t6C5Vg2iy8TPkG4d7eFCYaypgg7baqlkt_ZaNnS0SPY90Mzodx1VzkkZomMeti32Y2eUxk3F_jWPadoLyydYGmAmqvypdilclYbBvEblM_gwHsb6cBpGfuF1MyYsdXNTmYpsOe5husJsL_juQAc4xGF9zBMPz4qmbzPm_Myd1SddcvRjckScP_-ifQl86jJLJd8lpKGvrn0LP3KhhUdrLrUptHejpbAyUY2X4IQDwj0nnz_VVn3C30gJIGGOS75WasczVG_74oKJqFy5w";

	public static KubernetesClient createClient1(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(new File("admin.conf")) : new KubernetesClient(new File("admin.conf"), ana);
	}

}
