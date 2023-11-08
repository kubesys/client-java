/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.yamls;


import io.github.kubesys.client.writers.DeploymentWriter;
import io.github.kubesys.client.writers.SecretWriter;
import io.github.kubesys.client.writers.WorkloadWriter.Container;
import io.github.kubesys.client.writers.WorkloadWriter.Env;
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
public class KubeMirrorTest {
	
	static final String NAME = "kube-mirror";
	
	static final String CONFIG_KUBETOKEN = "kubeToken";

	static final String CONFIG_KUBEREGION = "kubeRegion";
	
	static final String MIRROR = "mirror";
	
	static final String MIRROR_IMAGE = "registry.cn-beijing.aliyuncs.com/dosproj/mirror:v0.2.5";
	
	public static void main(String[] args) throws Exception {
		SecretWriter secret = new SecretWriter(NAME, StackCommon.NAMESPACE);
		secret.withData(CONFIG_KUBETOKEN, StackCommon.base64("eyJhbGciOiJSUzI1NiIsImtpZCI6IktUN2JxY3VBdXczb1o1UUZtUVZ3R0tpWXRtVkJZckxVazNONzhkenNDMWsifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjIxZWUyYzc2LWJiNzYtNGE2OS04ODRiLWJiYjRkZGY0OGQ1YyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.fgLwL0NGH4Op6yA5WrmlCIhCUOsAB7dyzQUR_fNGkV8aBtW0QxQDkwWFj5MG9T_XdgQ9dcHfBU--AMR29LGNnSQxL2ePiMziEs1TrVdgRYfLS8cuaqTC2Bu5pUxpHbHcxknFhhlU6aJV2h6RqhXcOQAlVB1PVSkoxqweFGtpf_t2SdLAwy9lIX73AXXxuCNq3oht6Lo3SaZtgCS8-RIsR0rOAsVMWhCESkmppGGw46YrhSFJHdDGYvPZbh9aHziEecuN2LQ6QPF_Zu92Zl4VWGCAIYCaUM11XLeASDP9tfhOmDrWd-bpY-pJ_4fLrWka84utA-Otoxb-Yr5WGkK8Ow"))
				.withData(CONFIG_KUBEREGION, StackCommon.base64("test")).stream(System.out);
		
		DeploymentWriter deploy = new DeploymentWriter(NAME, StackCommon.NAMESPACE);
		
		deploy.withMasterEnbale()
				.withContainer(new Container(MIRROR, MIRROR_IMAGE, 
								new Env[] {
										new Env("kubeToken", new ValueFrom(
													new SecretKeyRef(NAME, CONFIG_KUBETOKEN))),
										new Env("kubeRegion", new ValueFrom(
												new SecretKeyRef(NAME, CONFIG_KUBEREGION)))}, 
								null, 
								null))
		.stream(System.out);
		
	}
}
