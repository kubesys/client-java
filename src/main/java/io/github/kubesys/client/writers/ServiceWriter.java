/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
/**
 * @author henry
 *
 */
public class ServiceWriter extends KindWriter {

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: Service\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#\r\n"
			+ "  labels: \r\n"
			+ "    name: #NAME#\r\n";
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public ServiceWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}
	
	/**
	 * type
	 */
	static final String TYPE = "type: #TYPE#";
	
	/**
	 * @param type type
	 * @return this object
	 * @throws Exception exception
	 */
	public ServiceWriter withType(String type) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("type", toObjectNode(TYPE, new String[] {"#TYPE#", type}).get("type"));
		return this;
	}
	
	/**
	 * selector
	 */
	static final String SELECTOR = "name: #NAME#";
	
	/**
	 * @param name name
	 * @return this object
	 * @throws Exception exception
	 */
	public ServiceWriter withSelector(String name) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("selector", toObjectNode(SELECTOR, new String[] {"#NAME#", name}));
		return this;
	}
	
	/**
	 * ports
	 */
	static final String PORTS = "name: #NAME#";
	
	/**
	 * @param port port
	 * @param nodePort nodePort
	 * @param name name
	 * @return this object
	 * @throws Exception exception
	 */
	public ServiceWriter withPort(int port, int nodePort, String name) throws Exception {
		return withPort(port, port, nodePort, "TCP", name);
	}
	
	/**
	 * @param port port
	 * @param targetPort tartgetPort
	 * @param nodePort nodePort
	 * @param name name
	 * @return this object
	 * @throws Exception exception
	 */
	public ServiceWriter withPort(int port, int targetPort, int nodePort, String name) throws Exception {
		return withPort(port, targetPort, nodePort, "TCP", name);
	}
	
	/**
	 * @param port port
	 * @param targetPort targetPort
	 * @param nodePort nodePort
	 * @param protocol protocol
	 * @param name name
	 * @return this object
	 * @throws Exception exception
	 */
	public ServiceWriter withPort(int port, int targetPort, int nodePort, String protocol, String name) throws Exception {
		ArrayNode ports = getArrayValue(getObjectValue("spec"), "ports");
		
		ObjectNode portNode = new ObjectMapper().createObjectNode();
		portNode.put("port", port);
		portNode.put("targetPort", targetPort);
		portNode.put("nodePort", nodePort);
		portNode.put("protocol", protocol);
		portNode.put("name", name);
		
		ports.add(portNode);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	public static void main(String[] args) throws Exception {
		ServiceWriter writer = new ServiceWriter("kube-database", "kube-system");
		writer.withType("NodePort").withSelector("kube-database")
			.withPort(5432, 5432, 30306, "TCP", "postgre-port")
			.withPort(8080, 8080, 30307, "TCP", "adminer-port").stream(System.out);
	}
}
