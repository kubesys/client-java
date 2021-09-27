/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

/**
 * @author wuheng09@gmail.com
 *
 */
public abstract class AbstractKubernetesClientTest {

	static String URL = "https://39.100.71.73:6443";
	
	// kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep kuboard-user | awk '{print $1}') -o go-template='{{.data.token}}' | base64 -d
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjNEd1dGSVRaVVdvRUJCdkRlcHlLckw3WnNCcGhsdVVTek43cGRMLXhxSFUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi04bmdzYyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjJhNGEwN2E0LTRiODktNDAxYy04NjlhLTE3ZGQ2OGNlYzc0OSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.M0z9lDSZytKRKPyS3wVZS5AeVu4j8366HTSUJJSm-kVyas7hsYT0BNQraRRy5RCHukHDjb6nIV-9aAjnItO0pdQB6hOvW6JufTA3ukZM5aEYidD-hlt-_iPanYMXaRcUTeBOWpPFTjpWbU4jF9Q5rfTeAgLzBl09kgNTPnTAkFov9jnZx0HZaFCijJZLZkmU_D0djqEUi1jgU7O__W33gPN3mCPYwwAuXzJkEoiVijXVpJT8i3SyYBW9TrK7uoKmMFi5v1e0hm0VfKdQCE4ywFt17Hnc0Zbqga5laeuirT6BHzDe7Tfp4r3j2eCT8QT3PIhohwEDPn4gC3qAwUU6Hg";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
