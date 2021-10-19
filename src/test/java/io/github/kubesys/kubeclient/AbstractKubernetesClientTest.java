/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://192.168.188.151:6443";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IllyREI3WEQ2LVNrZUpTSFJOU0wwU3ZmeEpLMHowMFJzQ2pqbDkwNXRLclUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1xN3ZqZiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjkzYjc0ZWU4LWU3ZDQtNDRjNi1hNjJkLTU5NWYyMmYxMjEwMiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.ruEd9Zfu9OkC9Vj_Zbpq8k7B6zjWKvqxeHmJn0jM5MPinOfpGrpOUXkqSMx4tTu3LQ3DvWChdn4AkTafUQ44gumSfNF_v6-yRWddY5d9ehrHK-rg_2SCOFi8JbXpwhbbaF69nlj2bURdnsW6VIygBF2gZZCjjmTgWL84eOTJe7umBo5RywX5ic26jtd_HZIGPUD9yTc-Pol8hoY4MP52frXer9iwSA9J93IdlNA_uNw2deVqgIIIB_6zlpnUTNySWv0ExQKx6IBRLTLBBBgSrLCumVqulnJwujuSIJ9q4XDszqkY84uYW8zu6wIZfr_BulcHvmGgaNYUiBUJcu4RjQ";

	public static KubernetesClient createClient(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
