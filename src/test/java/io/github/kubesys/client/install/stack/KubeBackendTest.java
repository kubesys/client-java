/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;


import io.github.kubesys.client.writers.DeploymentWriter;
import io.github.kubesys.client.writers.SecretWriter;
import io.github.kubesys.client.writers.ServiceWriter;
import io.github.kubesys.client.writers.WorkloadWriter.Container;
import io.github.kubesys.client.writers.WorkloadWriter.Env;
import io.github.kubesys.client.writers.WorkloadWriter.Port;
import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom;
import io.github.kubesys.client.writers.WorkloadWriter.Env.ValueFrom.SecretKeyRef;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/02
 * @version 1.0.3
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class KubeBackendTest {
	
	static final String NAME = "kube-backend";
	
	static final String BACKEND = "backend";
	
	static final String BACKEND_IMAGE = "registry.cn-beijing.aliyuncs.com/dosproj/backend:v1.2.3";
	
	public static void main(String[] args) throws Exception {
		
		DeploymentWriter deploy = new DeploymentWriter(NAME, StackCommon.NAMESPACE);
		
		deploy.withMasterEnbale()
				.withContainer(new Container(BACKEND, BACKEND_IMAGE, 
								null, 
								new Port[] {
										new Port(30308)
								}, 
								null))
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(30308, 30308, "backend")
				.stream(System.out);
	}
}
