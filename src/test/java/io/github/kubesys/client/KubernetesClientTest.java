/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.kubesys.client.exceptions.KubernetesConnectionException;

/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesClientTest {

	
	static final String TOKEN1 = "";
	
	static final String TOKEN2 = "";
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = new KubernetesClient("https://139.9.165.93:6443", TOKEN2);
		client.listResources("doslab.io.VirtualMachine2");
	}
	
//	@Test
//	void test400() throws Exception {
//		assertThrows(KubernetesBadRequestException.class, () -> {
//			new KubernetesClient("http://139.9.165.93:6443","");
//        });
//	}
//	
//	@Test
//	void test401() throws Exception {
//		assertThrows(KubernetesForbiddenAccessException.class, () -> {
//			new KubernetesClient("https://139.9.165.93:6443","");
//        });
//	}
	
	@Test
	void test402() throws Exception {
		assertThrows(KubernetesConnectionException.class, () -> {
			new KubernetesClient("139.9.165.93:6443","");
        });
	}
	
//	@Test
//	void test403() throws Exception {
//		assertThrows(KubernetesForbiddenAccessException.class, () -> {
//			KubernetesClient client = new KubernetesClient("https://139.9.165.93:6443", TOKEN1);
//			client.listResources("doslab.io.VirtualMachine");
//        });
//	}
//	
//	@Test
//	void test404() throws Exception {
//		assertThrows(KubernetesResourceNotFoundException.class, () -> {
//			KubernetesClient client = new KubernetesClient("https://139.9.165.93:6443", TOKEN2);
//			client.listResources("doslab.io.VirtualMachine2");
//        });
//	}
//	
//	@Test
//	void test409() throws Exception {
//		assertThrows(KubernetesConflictResourceException.class, () -> {
//			KubernetesClient client = new KubernetesClient("https://139.9.165.93:6443", TOKEN2);
//			client.createResourceUsingYaml("apiVersion: v1\r\n"
//					+ "kind: Pod\r\n"
//					+ "metadata:\r\n"
//					+ "  name: busybox-pod\r\n"
//					+ "  labels:\r\n"
//					+ "    app: busybox\r\n"
//					+ "spec:\r\n"
//					+ "  containers:\r\n"
//					+ "  - name: busybox-container\r\n"
//					+ "    image: busybox\r\n"
//					+ "    command: [\"sleep\", \"3600\"]\r\n"
//					+ "    resources:\r\n"
//					+ "      limits:\r\n"
//					+ "        memory: \"256Mi\"\r\n"
//					+ "        cpu: \"500m\"");
//        });
//	}
}
