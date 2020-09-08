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
	
	static String URL2 = "https://192.168.42.144:6443";
	
	static String TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImF2bEVKdnd3NTZQT1pzWVZsMlhfT0pXVDhZUmU4MW1JRmpfM3NXMnVLeEEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi1iZmJoMiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjA1ZGE4MGU3LTk0OWUtNGY3ZC1iZjRjLTNiZWQzMzNjZGU0MiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.EvQvafDBvJgncSCJJDspuv6HdeET5ksfZ5-LFFjukCG5u8A9QcVk1-5t70-FrZZA_5cu7G65gfOqPGlkGf8hTGErEU66AN1keqCxymrVIKfwVjOD6RwHA6gW2hu-MHmL6Q2zQexwc2AGwlXIntRnhJmG5CDqsGMPB-VtvrCStBX05bTEo2_yuHZmyLfd907yzOnL6gGR3FqeHJzXllkn9pT0oxG8qCMNP5SkVdlaYM18NRusgBw0aEK25TNuozrAuVT_w5b4yuwxY5jeroEEWNqQCZy7txWi5e7N2cSIvipKvQVX8ZBbMsGcOx58t3Feobmf1Ug6RVVoFIIVMC5FmQ";

	public static KubernetesClient createClient1() throws Exception {
		return new KubernetesClient(URL1);
	}
	
	public static KubernetesClient createClient2() throws Exception {
		return new KubernetesClient(URL2, TOKEN);
	}

}
