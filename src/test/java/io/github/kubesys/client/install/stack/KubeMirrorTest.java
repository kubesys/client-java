/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;


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
	
	static final String MIRROR_IMAGE = "registry.cn-beijing.aliyuncs.com/dosproj/mirror:v0.2.4";
	
	public static void main(String[] args) throws Exception {
		SecretWriter secret = new SecretWriter(NAME, StackCommon.NAMESPACE);
		secret.withData(CONFIG_KUBETOKEN, StackCommon.base64("eyJhbGciOiJSUzI1NiIsImtpZCI6IjB4bEFpME9SQi1Sa2oyMWtWd1BlT0hOcXFaQlNWVFQzM0d5bE9Bb0ZlR1kifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjJiMDNkZGU2LWZmYzAtNGQwNi1hMjk2LTliNDFkYmQ3MzJkYiIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.NFJVbZZmn-vFFXAawW25TVXyL2KNbO_4S-uLKG374-i4W2LIWFK7SsrXP0LJRPwkvXnLrFlyGY7RGVqvBruobcjWr8gBH8vbE5xXHO29XTwtj4lZyccjbjoKC22fMuxPZw4cMQz0Ci6fl8Oo95TM2dY9duyEP9fi5FoUnKUJjBrKx9PYByppQ4P2hz23GxDU_Xa1P2qHX7fIsdpbLznlYZ2o4uusTUvKGW_kNM05JQyaVHO4DuuCyCc-LgfXt9uWXkaYl29rxSFtGNd-iCdwqiNabi7uehH8NUr3lbgB48wGvxwn5UWNu9ZNmjcEPO3kdJEG3i5B-ZPbfQjcJWorZg"))
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
