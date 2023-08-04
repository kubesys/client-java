/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install;

import java.util.Base64;

import io.github.kubesys.client.writers.DeploymentWriter;
import io.github.kubesys.client.writers.PVCWriter;
import io.github.kubesys.client.writers.PVWriter;
import io.github.kubesys.client.writers.SecretWriter;
import io.github.kubesys.client.writers.ServiceWriter;
import io.github.kubesys.client.writers.WorkloadWriter.Container;
import io.github.kubesys.client.writers.WorkloadWriter.Env;
import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom;
import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom.SecretKeyRef;
import io.github.kubesys.client.writers.WorkloadWriter.Port;
import io.github.kubesys.client.writers.WorkloadWriter.VolumeMount;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/02
 * @version 1.0.3
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class KubeDatabaseTest {
	
	static final String NAME = "kube-database";
	
	static final String NAMESPACE = "kube-stack";

	static final String CONFIG_PASSWORD = "password";
	
	static final String VOLUME_DATA = "data";
	
	static final String POSTGRES = "postgres";
	
	static final String POSTGRES_IMAGE = "postgres:15.3-alpine";
	
	static final String ADMINER = "adminer";
	
	static final String ADMINER_IMAGE = "adminer:4.8.1-standalone";
	
	public static final String PATH = "/var/lib/doslab/";
	
	public static void main(String[] args) throws Exception {
		SecretWriter secret = new SecretWriter(NAME, NAMESPACE);
		secret.withData(CONFIG_PASSWORD, Base64.getEncoder().encodeToString("onceas".getBytes())).stream(System.out);
		
		PVWriter pv = new PVWriter(NAME);
		
		pv.withCapacity("20").withPath(PATH + POSTGRES).withPVC(NAME, NAMESPACE).stream(System.out);
		
		PVCWriter pvc = new PVCWriter(NAME, NAMESPACE);
		pvc.withCapacity("20").stream(System.out);
		
		DeploymentWriter deploy = new DeploymentWriter(NAME, NAMESPACE);
		
		deploy.withMasterEnbale()
				.withContainer(new Container(POSTGRES, POSTGRES_IMAGE, 
								new Env[] {
										new Env("POSTGRES_PASSWORD", new ValueFrom(new SecretKeyRef(NAME, CONFIG_PASSWORD)))}, 
								new Port[] {
										new Port(5432)
								}, 
								new VolumeMount[] {
										new VolumeMount(VOLUME_DATA, "/var/lib/postgresql")
								}))
				.withContainer(new Container(ADMINER, ADMINER_IMAGE, 
								null, 
								new Port[] {
										new Port(8080)
								}, 
								null))
				.withVolume(VOLUME_DATA, NAME)
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, NAMESPACE);
		service.withType("NodePort").withSelector(NAME).withPort(5432, 30306, POSTGRES)
				.withPort(8080, 30307, ADMINER).stream(System.out);
	}
}
