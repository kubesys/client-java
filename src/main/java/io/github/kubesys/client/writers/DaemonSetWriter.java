/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/01
 * @version 1.0.2
 *
 */
public class DaemonSetWriter extends WorkloadWriter {

	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public DaemonSetWriter(String name, String namespace) throws Exception {
		super(new String[] {
				"#APIVERSION#", "apps/v1",
				"#KIND#", "DaemonSet",
				"#NAME#", name, 
				"#NAMESPACE#", namespace,
				"#NUMBER#", String.valueOf(0)});
	}
	
}
