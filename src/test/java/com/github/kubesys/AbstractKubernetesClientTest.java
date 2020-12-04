/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL1 = "http://39.106.40.190:8888/";
	
	static String URL2 = "https://39.106.40.190:6443";
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ilg0dzJkS01TR1dybkVmZFhiS3dFTWxOdWo3X1k3TnkySi1UckFiWXBZRjgifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi05Z2x6ZiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjY1NDk0ZmJiLTI4OTgtNDRlZi1hNmZlLWZlNTNmOGNhN2I5NiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.VSe8o5MNgvCbSbvQPdrYoM4zHXAtpWmak5Env4tzBp1-ioKxn165V8nd2_FT6BrH5YlwHDbM-GOn3EWU8g4Lpcwoc_CsnE_uBMq577bWXe8Yo1zVLI_cpCLa1PYg74pFshzXcAvMahtQDOgc6Hix6MJC2s4Ncfivm52nmUJzsSNqLehrGOJEKOvyhZRMxP2Qz-iMl7oF_6LemaUifCh6NaCgIBTVgnAvq64GuRFJNZQcHqZdtesY8_ltuEhUz74xKPVJuBOOReA0kKb-rbigEtfWSBr9aWfT_ncjcz_UrcSsefbTc6gVQPV7v8EB_drB4t8lkwa_qCxCnagz6ymI7Q";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL1);
	}
	
	public static KubernetesClient createClient2() throws Exception {
		return new KubernetesClient(URL2, TOKEN);
	}

}
