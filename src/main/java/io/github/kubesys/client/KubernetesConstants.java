/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

/**
 * @author wuheng@iscas.ac.cn
 *
 * 
 */
/**
 * @author henry
 *
 */
public final class KubernetesConstants {
	
	
	/**
	 * 
	 */
	private KubernetesConstants() {
		super();
	}

	/*************************************
	 * 
	 * HTTP request
	 * 
	 *************************************/

	/**
	 * HTTP_RESPONSE_PATHS
	 */
	public static final String HTTP_RESPONSE_PATHS          = "paths";
	
	/**
	 * HTTP_RESPONSE_STATUS
	 */
	public static final String HTTP_RESPONSE_STATUS          = "status";

	/**
	 * HTTP_RESPONSE_RESOURCES
	 */
	public static final String HTTP_RESPONSE_RESOURCES       = "resources";
	
	/**
	 * HTTP_QUERY_KIND
	 */
	public static final String HTTP_QUERY_KIND               = "?kind=";
	
	/**
	 * HTTP_QUERY_FIELDSELECTOR
	 */
	public static final String HTTP_QUERY_FIELDSELECTOR      = "&fieldSelector=";
	
	/**
	 * HTTP_QUERY_LABELSELECTOR
	 */
	public static final String HTTP_QUERY_LABELSELECTOR      = "&labelSelector=";
	
	/**
	 * HTTP_QUERY_PAGELIMIT
	 */
	public static final String HTTP_QUERY_PAGELIMIT          = "&limit=";
	
	/**
	 * HTTP_QUERY_NEXTID
	 */
	public static final String HTTP_QUERY_NEXTID             = "&continue=";
	
	/**
	 * HTTP_QUERY_WATCHER_ENABLE
	 */
	public static final String HTTP_QUERY_WATCHER_ENABLE     = "?watch=true&timeoutSeconds=315360000";
	
	
	
	/*************************************
	 * 
	 * Default values
	 * 
	 *************************************/
	
	/**
	 * VALUE_DEFAULT_NAMESPACE
	 */
	public static final String VALUE_DEFAULT_NAMESPACE       = "default";
	
	/**
	 * VALUE_ALL_NAMESPACES
	 */
	public static final String VALUE_ALL_NAMESPACES          = "";
	
	/**
	 * VALUE_NAMESPACED
	 */
	public static final String VALUE_NAMESPACED              = "Namespaced";
	
	/**
	 * VALUE_APIS
	 */
	public static final String VALUE_APIS                    = "apis";
	
	
	
	/*************************************
	 * 
	 * Kubernetes json structure
	 * 
	 *************************************/

	/**
	 * KUBE_CONFIG
	 */
	public static final String KUBE_CONFIG                  = "/etc/kubernetes/admin.conf";
	
	/**
	 * NODE_KIND
	 */
	public static final String NODE_KIND                    = "Node";
	
	/**
	 * KUBE_METADATA
	 */
	public static final String KUBE_METADATA                = "metadata";
	
	/**
	 * KUBE_METADATA_NAMESPACE
	 */
	public static final String KUBE_METADATA_NAMESPACE      = "namespace";
	
	/**
	 * KUBE_METADATA_NAME
	 */
	public static final String KUBE_METADATA_NAME           = "name";
	
	/**
	 * KUBE_TARGET
	 */
	public static final String KUBE_TARGET                  = "target";
	
	/**
	 * KUBE_STATUS
	 */
	public static final String KUBE_STATUS                  = "status";

	/**
	 * KUBE_APIVERSION
	 */
	public static final String KUBE_APIVERSION              = "apiVersion";
	
	/**
	 * KUBE_SPEC
	 */
	public static final String KUBE_SPEC                    = "spec";
	
	/**
	 * KUBE_SPEC_VERSIONS
	 */
	public static final String KUBE_SPEC_VERSIONS           = "versions";
	
	/**
	 * KUBE_SPEC_VERSIONS_NAME
	 */
	public static final String KUBE_SPEC_VERSIONS_NAME      = "name";
	
	/**
	 * KUBE_SPEC_NAMES
	 */
	public static final String KUBE_SPEC_NAMES              = "names";
	
	/**
	 * KUBE_SPEC_NAMES_KIND
	 */
	public static final String KUBE_SPEC_NAMES_KIND         = "kind";
	
	/**
	 * KUBE_SPEC_NAMES_PLURAL
	 */
	public static final String KUBE_SPEC_NAMES_PLURAL       = "plural";
	
	/**
	 * KUBE_SPEC_NAMES_VERBS
	 */
	public static final String KUBE_SPEC_NAMES_VERBS        = "verbs";
	
	/**
	 * KUBE_SPEC_GROUP
	 */
	public static final String KUBE_SPEC_GROUP              = "group";
	
	/**
	 * KUBE_SPEC_SCOPE
	 */
	public static final String KUBE_SPEC_SCOPE              = "scope";
	
	/**
	 * KUBE_RESOURCES_NAMESPACED
	 */
	public static final String KUBE_RESOURCES_NAMESPACED    = "namespaced";
	
	/**
	 * KUBE_RESOURCES_GROUPVERSION
	 */
	public static final String KUBE_RESOURCES_GROUPVERSION  = "groupVersion";
	
	/**
	 * KUBE_TYPE
	 */
	public static final String KUBE_TYPE                    = "type";
	
	/**
	 * KUBE_OBJECT
	 */
	public static final String KUBE_OBJECT                  = "object";
	
	/**
	 * DEFAULT_APIVERSION
	 */
	public static final String DEFAULT_APIVERSION           = "v1";
	
	/**
	 * KUBE_KIND
	 */
	public static final String KUBE_KIND                    = "kind";
	
	
	/*************************************
	 * 
	 * Kubernetes json structure
	 * 
	 *************************************/
	
	/**
	 * KUBD_KIND_BINDING
	 */
	public static final String KUBD_KIND_BINDING                       = "Binding";
	
	/**
	 * KUBD_FULLKIND_CUSTOMRESOURCEDEFINITION
	 */
	public static final String KUBD_FULLKIND_CUSTOMRESOURCEDEFINITION  = "apiextensions.k8s.io.CustomResourceDefinition";
	
	/**
	 * KUBE_DEFAULT_KIND_VERBS
	 */
	public static final String KUBE_DEFAULT_KIND_VERBS                 = "[\"create\", \"delete\", \"deletecollection\", \"get\", \"list\", \"patch\", \"update\", \"watch\"]";
	
	/**
	 * KUBE_APIVERSION_SPLIT
	 */
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
	 * 取值为.
	 */
	public static final String KUBE_VERSION_SPLIT              = "/";
	
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
	/**
	 * KUBEAPI_CORE_PATTERN
	 */
	public static final String KUBEAPI_CORE_PATTERN          = "/api/v1";
	
	/**
	 * KUBEAPI_CORE_PREFIX_PATTERN
	 */
	public static final String KUBEAPI_CORE_PREFIX_PATTERN   = "/api";

	/**
	 * KUBEAPI_WATCHER_PATTERN
	 */
	public static final String KUBEAPI_WATCHER_PATTERN       = "watch";
	
	/**
	 * KUBEAPI_PATHSEPARTOR_PATTERN
	 */
	public static final String KUBEAPI_PATHSEPARTOR_PATTERN  = "/";
	
	/**
	 * KUBEAPI_NAMESPACES_PATTERN
	 */
	public static final String KUBEAPI_NAMESPACES_PATTERN    = "namespaces/";
	
	
	/*************************************
	 * 
	 * JSON type
	 * 
	 *************************************/
	
	/**
	 * JSON_TYPE_ADDED
	 */
	public static final String JSON_TYPE_ADDED               = "ADDED";
	
	/**
	 * JSON_TYPE_MODIFIED
	 */
	public static final String JSON_TYPE_MODIFIED            = "MODIFIED";
	
	/**
	 * JSON_TYPE_DELETED
	 */
	public static final String JSON_TYPE_DELETED             = "DELETED";
	
}
