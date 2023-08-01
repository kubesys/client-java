/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom;
import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom.SecretKeyRef;

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
		super(name, namespace, new String[] {
						"#APIVERSION#", "apps/v1",
						"#KIND#", "Deployment",
						"#NAME#", name, 
						"#NAMESPACE#", namespace,
						"#NUMBER#", String.valueOf(number)});
	}

	public static void main(String[] args) throws Exception {
		DeploymentWriter writer = new DeploymentWriter("kube-database", "kube-system");
		writer
//		.withMasterEnbale()
		.withContainer(
				new Container("postgres", "postgres:14.5-alpine", 
							new Env[] {
									new Env("POSTGRES_PASSWORD", new ValueFrom(new SecretKeyRef("kube-database", "password")))
									}, 
							new Port[] {
									new Port(5432)
							}, 
							new VolumeMount[] {
									new VolumeMount("data", "/var/lib/postgresql")
							}))
		.withContainer(
				new Container("adminer", "adminer:4.8.1-standalone", 
							null, 
							new Port[] {
									new Port(8080)
							}, 
							null))
		.withVolume("data", "kube-database")
		.stream(System.out);
	}
}
