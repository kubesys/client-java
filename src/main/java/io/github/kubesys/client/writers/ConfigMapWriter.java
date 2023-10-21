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

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: ConfigMap\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#";
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public ConfigMapWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}

	/**
	 * @param key key
	 * @param value value
	 * @return this object
	 */
	public ConfigMapWriter withData(String key, String value) {
		ObjectNode data = getObjectValue("data");
		data.put(key, value);
		return this;
	}
	
	/**
	 * @param key key
	 * @param value value
	 * @return this object
	 * @throws Exception exception
	 */
	public ConfigMapWriter withYamlData(String key, String value) throws Exception {
		ObjectNode data = getObjectValue("data");
		data.set(key, toObjectNode(value));
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
}
