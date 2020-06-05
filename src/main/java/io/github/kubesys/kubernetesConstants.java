/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import okhttp3.MediaType;

/**
 * @author wuheng09@gmail.com
 *
 */
public interface kubernetesConstants {

	/*************************************
	 * 
	 * HTTP request
	 * 
	 *************************************/

	public final static String HTTP_REQUEST_GET             = "GET";

	public final static String HTTP_REQUEST_POST            = "POST";
	
	public final static String HTTP_REQUEST_PUT             = "PUT";
	
	public final static String HTTP_REQUEST_DELETE          = "DELETE";

	public final static String HTTP_RESPONSE_PATHS          = "paths";
	
	public final static String HTTP_RESPONSE_STATUS         = "status";

	public final static String HTTP_RESPONSE_RESOURCES      = "resources";
	
	public final static String HTTP_QUERY_KIND              = "?kind=";
	
	public final static String HTTP_QUERY_FIELDSELECTOR     = "&fieldSelector=";
	
	public final static String HTTP_QUERY_LABELSELECTOR      = "&labelSelector=";
	
	public final static String HTTP_QUERY_PAGELIMIT          = "&limit=";
	
	public final static String HTTP_QUERY_NEXTID             = "&continue=";
	
	
	public final static MediaType HTTP_MEDIA_TYPE           = MediaType.parse("application/json");
	

	
	/*************************************
	 * 
	 * Default values
	 * 
	 *************************************/
	
	public final static String DEFAULT_NAMESPACE            = "default";
	
	/*************************************
	 * 
	 * Kubernetes resource structure
	 * 
	 *************************************/
	
	public final static String KUBE_KIND                    = "kind";
	
	
	
	public final static String KUBE_METADATA                = "metadata";
	
	public final static String KUBE_METADATA_NAMESPACE      = "namespace";
	
	public final static String KUBE_METADATA_NAME           = "name";
	
	
	public final static String KUBE_RESOURCES_NAMESPACED    = "namespaced";
	
	public final static String KUBE_RESOURCES_GROUPVERSION  = "groupVersion";
	
	
	
	/*************************************
	 * 
	 * Kubernetes url patterns
	 * 
	 *************************************/
	public final static String KUBEAPI_CORE_PATTERN          = "/api/v1";
	
	public final static String KUBEAPI_CORE_PREFIX_PATTERN   = "/api";
	
	public final static String KUBE_NAMESPACES_PREFIX        = "namespaces/";

	public final static String KUBE_ALL_NAMESPACES           = "";
	
	
	

	public final static String HTTP_ORIGIN = "Origin";

	public final static String HTTP_DEFAULTL_PATH = "";

	public final static String HTTP_PATH_SEPARTOR = "/";

	public final static String HTTP_DOT_SEPARTOR = ".";

	public final static String HTTP_HEADER_KEY = "Sec-WebSocket-Protocol";

	public final static String HTTP_HEADER_VALUE = "v4.channel.k8s.io";


	public final static String KUBE_NAMESPACE_PREFIX = "namespaces/";

	public final static String KUBE_WATCHER_PATH = "watch";

	public final static String KUBE_WATCHER_ENABLE = "?watch=true";
	
	
	
	
}
