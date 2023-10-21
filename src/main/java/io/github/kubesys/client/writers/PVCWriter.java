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
public class PVCWriter extends KindWriter {

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: PersistentVolumeClaim\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#\r\n"
			+ "  labels:\r\n"
			+ "    name: \"#NAME#\"\r\n"
			+ "spec:\r\n"
			+ "  accessModes:\r\n"
			+ "    - ReadWriteOnce";
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public PVCWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}

	/**
	 * capacity
	 */
	static final String CAPACITY = "storage: #SIZE#Gi";
	
	/**
	 * @param gb  gb
	 * @return this object
	 * @throws Exception exception
	 */
	public PVCWriter withCapacity(String gb) throws Exception {
		ObjectNode size = toObjectNode(CAPACITY, new String[] {"#SIZE#", gb});
		ObjectNode requests = toObjectNode("requests", size);
		ObjectNode spec = getObjectValue("spec");
		spec.set("resources", requests);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
}
