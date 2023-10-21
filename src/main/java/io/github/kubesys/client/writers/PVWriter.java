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
public class PVWriter extends KindWriter {

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: v1\r\n"
			+ "kind: PersistentVolume\r\n"
			+ "metadata:\r\n"
			+ "  name: #NAME#\r\n"
			+ "spec:\r\n"
			+ "  accessModes:\r\n"
			+ "  - ReadWriteOnce\r\n"
			+ "  persistentVolumeReclaimPolicy: Retain\r\n"
			+ "  volumeMode: Filesystem";
	
	/**
	 * @param name name
	 * @throws Exception exception
	 */
	public PVWriter(String name) throws Exception {
		super(name);
	}

	/**
	 * capacity
	 */
	static final String CAPACITY = "storage: #SIZE#Gi";
	
	/**
	 * @param gb gb
	 * @return this object
	 * @throws Exception exception
	 */
	public PVWriter withCapacity(String gb) throws Exception {
		ObjectNode spec = (ObjectNode) json.get("spec");
		spec.set("capacity", toObjectNode(CAPACITY, new String[] {"#SIZE#", gb}));
		return this;
	}
	
	/**
	 * claimref
	 */
	static final String CLAIMREF = "apiVersion: v1\r\n"
			+ "kind: PersistentVolumeClaim\r\n"
			+ "name: #PV_NAME#\r\n"
			+ "namespace: #PV_NAMESPACE#";
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @return this object
	 * @throws Exception exception
	 */
	public PVWriter withPVC(String name, String namespace) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("claimRef", toObjectNode(CLAIMREF, new String[] {"#PV_NAME#", name, "#PV_NAMESPACE#", namespace}));
		return this;
	}
	
	/**
	 * hostpath
	 */
	static final String HOSTPATH = "path: #PATH#";
	
	/**
	 * @param hostpath hostpath
	 * @return this object
	 * @throws Exception exception
	 */
	public PVWriter withPath(String hostpath) throws Exception {
		ObjectNode spec = getObjectValue("spec");
		spec.set("hostPath", toObjectNode(HOSTPATH, new String[] {"#PATH#", hostpath}));
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	public static void main(String[] args) throws Exception {
		PVWriter writer = new PVWriter("kube-database");
		writer.withCapacity("2").withPVC("kube-database", "kube-system").withPath("/var/lib/doslab/kube/postgres").stream(System.out);
	}
}
