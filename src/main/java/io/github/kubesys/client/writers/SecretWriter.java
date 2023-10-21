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
public class SecretWriter extends KindWriter {

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: Secret\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#\r\n"
			+ "type: Opaque";
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public SecretWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}

	/**
	 * @param key key
	 * @param value value
	 * @return this object
	 */
	public SecretWriter withData(String key, String value) {
		ObjectNode data = getObjectValue("data");
		data.put(key, value);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	public static void main(String[] args) throws Exception {
		SecretWriter writer = new SecretWriter("kube-database", "kube-system");
		writer.withData("username", "onceas").withData("password", "onceas").stream(System.out);
	}
}
