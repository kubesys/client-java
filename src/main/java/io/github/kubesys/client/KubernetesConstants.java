/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

/**
 * @author wuheng09@wuheng@iscas.ac.cn
 *
 */
public final class KubernetesConstants {
	
	
	private KubernetesConstants() {
		super();
	}

	/*************************************
	 * 
	 * HTTP request
	 * 
	 *************************************/

	public static final String HTTP_RESPONSE_PATHS          = "paths";
	
	public static final String HTTP_RESPONSE_STATUS          = "status";

	public static final String HTTP_RESPONSE_RESOURCES       = "resources";
	
	public static final String HTTP_QUERY_KIND               = "?kind=";
	
	public static final String HTTP_QUERY_FIELDSELECTOR      = "&fieldSelector=";
	
	public static final String HTTP_QUERY_LABELSELECTOR      = "&labelSelector=";
	
	public static final String HTTP_QUERY_PAGELIMIT          = "&limit=";
	
	public static final String HTTP_QUERY_NEXTID             = "&continue=";
	
	public static final String HTTP_QUERY_WATCHER_ENABLE     = "?watch=true&timeoutSeconds=315360000";
	
	
	
	/*************************************
	 * 
	 * Default values
	 * 
	 *************************************/
	
	public static final String VALUE_DEFAULT_NAMESPACE       = "default";
	
	public static final String VALUE_ALL_NAMESPACES          = "";
	
	public static final String VALUE_NAMESPACED              = "Namespaced";
	
	public static final String VALUE_APIS                    = "apis";
	
	
	
	/*************************************
	 * 
	 * Kubernetes json structure
	 * 
	 *************************************/

	public static final String KUBE_CONFIG                    = "/etc/kubernetes/admin.conf";
	
	public static final String KUBE_KIND                    = "kind";
	
	public static final String KUBE_METADATA                = "metadata";
	
	public static final String KUBE_METADATA_NAMESPACE      = "namespace";
	
	public static final String KUBE_METADATA_NAME           = "name";
	
	public static final String KUBE_STATUS                  = "status";

	public static final String KUBE_APIVERSION              = "apiVersion";
	
	public static final String KUBE_SPEC                    = "spec";
	
	public static final String KUBE_SPEC_VERSIONS           = "versions";
	
	public static final String KUBE_SPEC_VERSIONS_NAME      = "name";
	
	public static final String KUBE_SPEC_NAMES              = "names";
	
	public static final String KUBE_SPEC_NAMES_KIND         = "kind";
	
	public static final String KUBE_SPEC_NAMES_PLURAL       = "plural";
	
	public static final String KUBE_SPEC_NAMES_VERBS        = "verbs";
	
	public static final String KUBE_SPEC_GROUP              = "group";
	
	public static final String KUBE_SPEC_SCOPE              = "scope";
	
	public static final String KUBE_RESOURCES_NAMESPACED    = "namespaced";
	
	public static final String KUBE_RESOURCES_GROUPVERSION  = "groupVersion";
	
	public static final String KUBE_TYPE                    = "type";
	
	public static final String KUBE_OBJECT                  = "object";
	
	
	
	/*************************************
	 * 
	 * Kubernetes url patterns
	 * 
	 *************************************/
	public static final String KUBEAPI_CORE_PATTERN          = "/api/v1";
	
	public static final String KUBEAPI_CORE_PREFIX_PATTERN   = "/api";

	public static final String KUBEAPI_WATCHER_PATTERN       = "watch";
	
	public static final String KUBEAPI_PATHSEPARTOR_PATTERN  = "/";
	
	public static final String KUBEAPI_NAMESPACES_PATTERN    = "namespaces/";
	
	
	/*************************************
	 * 
	 * JSON type
	 * 
	 *************************************/
	
	public static final String JSON_TYPE_ADDED               = "ADDED";
	
	public static final String JSON_TYPE_MODIFIED            = "MODIFIED";
	
	public static final String JSON_TYPE_DELETED             = "DELETED";
	
}
