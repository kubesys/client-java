package io.github.kubesys.client;
/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *limitations under the License.
 */


import java.io.File;


/**
 * Kubernetes客户端，用于建立与Kubernetes的连接，随后可以对Kubernetes的Kind资源进行生命周期管理。
 * 其中，kind的定义参见https://kubernetes.io/zh-cn/docs/concepts/overview/working-with-objects/
 * <br>
 * <br>
 * Kubernetes客户端主要支持create、update、delete、get、list和watch5种语义，其设计参见
 * https://g-ubjg5602.coding.net/p/iscas-system/km/spaces/1326202/pages/K-28
 * <br>
 * <br>
 * 
 * @author wuheng@iscas.ac.cn
 * @since 1.0.0
 * 
 */
public class KubernetesClientTest {

	private static final String YAML = "---\r\n"
			+ "apiVersion: v1\r\n"
			+ "kind: Pod\r\n"
			+ "metadata:\r\n"
			+ "  name: busybox-pod1\r\n"
			+ "  labels:\r\n"
			+ "    app: busybox\r\n"
			+ "spec:\r\n"
			+ "  containers:\r\n"
			+ "  - name: busybox-container\r\n"
			+ "    image: busybox:latest\r\n"
			+ "    command: [\"sleep\", \"3600\"]\r\n"
			+ "---\r\n"
			+ "apiVersion: v1\r\n"
			+ "kind: Pod\r\n"
			+ "metadata:\r\n"
			+ "  name: busybox-pod2\r\n"
			+ "  labels:\r\n"
			+ "    app: busybox\r\n"
			+ "spec:\r\n"
			+ "  containers:\r\n"
			+ "  - name: busybox-container\r\n"
			+ "    image: busybox:latest\r\n"
			+ "    command: [\"sleep\", \"3600\"]";

	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("https://139.9.165.93:6443", "eyJhbGciOiJSUzI1NiIsImtpZCI6IlJpRC1BZzVsU3ZZU1M2bno0Rngtbno5eF9PemRoUjZSUy14cExrSVMtLUUifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjA2Y2FhOTg4LWM2ZDQtNDhmNC04MTEyLWI0YTUxMGQ5YTU0MSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.XEbbiTqc0VuqMP5P-cwF5pjY-0EU8I8Ih0eKOhr69F3JOwVozncOXeFZ46spfFNjvzJU-0yPmH1etLWu8xdOw0XjyvYjmVlVD--rSjgRF2mxwrdaP5EirB4lxvqxqk2oo1Xayn91xA-dIp1fn5dZyFTZMpEtcYqCOpAXhRifduAI6GBuDubRdJy6nbIvxtx8q4WX43B3u-2YIfy8Qk_wvJcHznTi-dMwDYRfunrJla-l-WFZLM5lSUngrCD5xdLhxHVaybbMUR-rPScul_U_lptsOF-v1dtfwy8tp5qlFQXASPxOxyCBnB9aQUJqtpxPfwUNr-xBVmQgUBFCOu8oDA");
		client.createResourceByYaml(YAML);
	}

	// 配置文件内容正确，但连不上kubernetes
	// org.apache.hc.client5.http.HttpHostConnectException: 
	public static void connectionError() {
		new KubernetesClient(new File("configs/kube.yml"));
	}

	// 配置文件的格式错误
	// io.github.kubesys.client.exceptions.KubernetesWrongConfigException: Cannot invoke "com.fasterxml.jackson.databind.JsonNode.get(int)" because the return value of "com.fasterxml.jackson.databind.JsonNode.get(String)
	public static void wrongFile() {
		new KubernetesClient(new File("examples/busybox.yaml"));
	}

	// 配置文件不存在
	// java.io.FileNotFoundException
	public static void fileNotFound() {
		new KubernetesClient();
	}

}
