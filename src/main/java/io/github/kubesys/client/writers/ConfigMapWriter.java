/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
public class ConfigMapWriter extends KindWriter {

	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: ConfigMap\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#";
	
	public ConfigMapWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}

	public ConfigMapWriter withData(String key, String value) {
		ObjectNode data = getObjectValue("data");
		data.put(key, value);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	public static void main(String[] args) throws Exception {
		ConfigMapWriter writer = new ConfigMapWriter("kube-database", "kube-system");
		String value = "|-\r\n"
				+ "    @test \"Test Health\" {\r\n"
				+ "      url=\"http://grafana/api/health\"\r\n"
				+ "\r\n"
				+ "      code=$(wget --server-response --spider --timeout 10 --tries 1 ${url} 2>&1 | awk '/^  HTTP/{print $2}')\r\n"
				+ "      [ \"$code\" == \"200\" ]\r\n"
				+ "    }";
		writer.withData("username", "onceas").withData("run.sh", value.replaceAll("\r\n", "")).stream(System.out);
	}
}
