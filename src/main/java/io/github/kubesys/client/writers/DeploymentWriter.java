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
public class DeploymentWriter extends WorkloadWriter {

	public DeploymentWriter(String name, String namespace) throws Exception {
		this(name, namespace, 1);
	}
	
	public DeploymentWriter(String name, String namespace, int number) throws Exception {
		super(new String[] {
						"#APIVERSION#", "apps/v1",
						"#KIND#", "Deployment",
						"#NAME#", name, 
						"#NAMESPACE#", namespace,
						"#NUMBER#", String.valueOf(number)});
	}

}
