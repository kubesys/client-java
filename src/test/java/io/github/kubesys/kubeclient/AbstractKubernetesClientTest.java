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
	
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkZaZTYyRDBwTkVOOE15VDBnZXdqRTdGLVVKNnVTeTlZVzAtaWZiTElMNUUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi01bms4ZyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjgzM2JmNjk2LTEzOWUtNDFiNS05NzlmLTY3NGI0YWNmMDExOSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.W8-bgVwf492d2ZOP_oJaEPwrFz6yszoERYtfQJUjcxAaOER5911gRJFKM4gPXrgutapqToGalrfzlNtA9FCQgfPshhQqyefrHanmJRqx0Ic4czvx_H-31ygPlGfXcB6J9P2OF4E02TcqZ-LXK4Ao-CQyX6B_ooePtpGxowVHKGIAvY_Y4m9GViedyjdzEUZsNrKEAGz19jTTG-rUnGsVNLXb2_7Qaf1yrirwfLg4Xy_qCUYfJSeSuLfIKexsUSLzMp2B1UsA3GlCEjzQoqkJuWpJAJ_t0TGL2eiZxdXTWBZGSTmxDrokCy-LrTZWSSwB5p-3hzrya8KQdHyKRgcHRg";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL);
	}
	
	public static KubernetesClient createClient2(KubernetesAnalyzer ana) throws Exception {
		return (ana == null) ? new KubernetesClient(URL, TOKEN) : new KubernetesClient(URL, TOKEN, ana);
	}

}
