/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *limitations under the License.
 */
package io.github.kubesys.client.beans;

import java.util.logging.Logger;

/**
 * Kubernetes的客户端，根据https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/的
 * URL规则生产URL
 * 
 * 对于JSON参数，可参见https://kubernetes.io/docs/reference/kubernetes-api/
 * 
 * @author wuheng@iscas.ac.cn
 * @since 2.0.0
 * 
 */
public class KubernetesPermission {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesPermission.class.getName());

	protected String[] apiGroups;

	protected String[] apiVersions;

	protected String[] operations;

	protected String[] resources;

	protected String scope;

	public String[] getApiGroups() {
		return apiGroups;
	}

	public void setApiGroups(String[] apiGroups) {
		this.apiGroups = apiGroups;
	}

	public String[] getApiVersions() {
		return apiVersions;
	}

	public void setApiVersions(String[] apiVersions) {
		this.apiVersions = apiVersions;
	}

	public String[] getOperations() {
		return operations;
	}

	public void setOperations(String[] operations) {
		this.operations = operations;
	}

	public String[] getResources() {
		return resources;
	}

	public void setResources(String[] resources) {
		this.resources = resources;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
