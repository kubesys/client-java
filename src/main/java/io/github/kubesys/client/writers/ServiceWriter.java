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
public class ServiceWriter extends KindWriter {

	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: Service\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "  namespace: #NAMESPACE#\r\n"
			+ "  labels: \r\n"
			+ "    name: #NAME#\r\n";
	
	public ServiceWriter(String name, String namespace) throws Exception {
		super(name, namespace);
	}
	
	static final String TYPE = "type: #TYPE#";
	
	public ServiceWriter withType(String type) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("type", toObjectNode(TYPE, new String[] {"#TYPE#", type}).get("type"));
		return this;
	}
	
	static final String SELECTOR = "name: #NAME#";
	
	public ServiceWriter withSelector(String name) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("selector", toObjectNode(SELECTOR, new String[] {"#NAME#", name}));
		return this;
	}
	
	static final String PORTS = "name: #NAME#";
	
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
