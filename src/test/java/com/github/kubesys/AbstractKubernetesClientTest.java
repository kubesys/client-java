/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://124.70.64.232:6443/";
	
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6Imc0dVljcWdmMG1RWTVTeG1CZFotd1g3V3BhZUhrUWw1MlF4ZU8tX2hIUVUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi00Y240eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjQwZTEzNjhmLTFhNTEtNGNiMC1hMjJkLWNkOTlmMmRiYzQyNSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.EufkXbpHcznWsklmePj6Js9AiYHXtURwJmJHPAVHZb3kKpY4-9FsfQPcRc31nqE49CClbqhM6R1kjWqh2iOVA3fAMy5uu-lLmIow0e2eYntwC16CNWwG8MmA7ze-wKj_oND0YMSEFzqMAEC2GaKUcGNGZqK5myA2lM1jrRWnQAMeZjap0pXrl-4QB1iD5vQQahSb6f1Z3KLfLY7mrMJ7OYxTN9T7GbwYSPyAh1Vs3-2WdRAsZnewgLGCWRLbBbl8Hfn2Qg0o61rlHq34bGglZ9aC7RSawkNj4Bo-UfhIHp1XN96poIuuaHI2Z1ql16pMvxqM1Ja7MWdMVvM2hENLRw";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
