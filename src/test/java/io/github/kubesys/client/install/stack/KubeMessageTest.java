/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;

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
public class KubeMessageTest {
	
	static final String NAME = "kube-message";
	
	static final String RABBITMQ = "rabbitmq";
	
	static final String RABBITMQ_IMAGE = "rabbitmq:3.12.2-management";
	
	public static void main(String[] args) throws Exception {
		SecretWriter secret = new SecretWriter(NAME, StackCommon.NAMESPACE);
		secret.withData(StackCommon.CONFIG_USERNAME, StackCommon.base64("rabbitmq"))
				.withData(StackCommon.CONFIG_PASSWORD, StackCommon.base64("onceas")).stream(System.out);
		
		PVWriter pv = new PVWriter(NAME);
		pv.withCapacity("20").withPath(StackCommon.PATH + RABBITMQ).withPVC(NAME, StackCommon.NAMESPACE).stream(System.out);
		
		PVCWriter pvc = new PVCWriter(NAME, StackCommon.NAMESPACE);
		pvc.withCapacity("20").stream(System.out);
		
		DeploymentWriter deploy = new DeploymentWriter(NAME, StackCommon.NAMESPACE);
		
		deploy.withMasterEnbale()
				.withContainer(new Container(RABBITMQ, RABBITMQ_IMAGE, 
								new Env[] {
										new Env("RABBITMQ_DEFAULT_USER", new ValueFrom(
													new SecretKeyRef(NAME, StackCommon.CONFIG_USERNAME))),
										new Env("RABBITMQ_DEFAULT_PASS", new ValueFrom(
												new SecretKeyRef(NAME, StackCommon.CONFIG_PASSWORD)))}, 
								new Port[] {
										new Port(15672),
										new Port(5672)
								}, 
								new VolumeMount[] {
										new VolumeMount(StackCommon.VOLUME_DATA, "/var/lib/rabbitmq")
								}))
				.withPVCVolume(StackCommon.VOLUME_DATA, NAME)
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(15672, 30305, "management")
				.withPort(5672, 30304, "rabbitmq")
		.stream(System.out);
	}
}
