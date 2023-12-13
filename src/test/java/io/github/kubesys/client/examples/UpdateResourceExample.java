/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.examples;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.github.kubesys.client.KubernetesClient;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023.12.05
 *
 */
public class UpdateResourceExample {

	/**
	 * see KubernetesCertClientExample or KubernetesTokenClientExample
	 */
	static KubernetesClient client = null;
	
	public static void main(String[] args) throws Exception {
		client.createResourceByJson(Files.readString(
				Paths.get("examples/busybox.json") , StandardCharsets.UTF_8));
	
		client.createResourceByYaml(Files.readString(
				Paths.get("examples/busybox.yaml") , StandardCharsets.UTF_8));
	}

}
