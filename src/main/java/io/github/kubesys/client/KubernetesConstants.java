/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

/**
 * @author wuheng@iscas.ac.cn
 *
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

	public static final String KUBE_CONFIG                  = "/etc/kubernetes/admin.conf";
	
	public static final String NODE_KIND                    = "Node";
	
	public static final String KUBE_METADATA                = "metadata";
	
	public static final String KUBE_METADATA_NAMESPACE      = "namespace";
	
	public static final String KUBE_METADATA_NAME           = "name";
	
	public static final String KUBE_TARGET                  = "target";
	
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
	
	public static final String DEFAULT_APIVERSION           = "v1";
	
	public static final String KUBE_KIND                    = "kind";
	
	
	/*************************************
	 * 
	 * Kubernetes json structure
	 * 
	 *************************************/
	
	public static final String KUBD_KIND_BINDING                       = "Binding";
	
	public static final String KUBD_FULLKIND_CUSTOMRESOURCEDEFINITION  = "apiextensions.k8s.io.CustomResourceDefinition";
	
	public static final String KUBE_DEFAULT_KIND_VERBS                 = "[\"create\", \"delete\", \"deletecollection\", \"get\", \"list\", \"patch\", \"update\", \"watch\"]";
	
	public static final String KUBE_APIVERSION_SPLIT        = "/";
	
	/**
	 * 取值为default
	 */
	public static final String KUBE_DEFAULT_NAMESPACE        = "default";

	/**
	 * 取值为""
	 */
	public static final String KUBE_DEFAULT_GROUP            = "";
	
	/**
	 * 取值为.
	 */
	public static final String KUBE_VALUE_SPLIT              = ".";
	
	/**
	 * 取值为creationTimestamp
	 */
	public static final String KUBE_METADATA_CREATED         = "creationTimestamp";
	
	/**
	 * 取值为plural
	 */
	public static final String KUBE_PLURAL                   = "plural";
	
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
