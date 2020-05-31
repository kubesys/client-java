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

	public final static String HTTP_RESPONSE_PATHS          = "paths";

	public final static String HTTP_RESPONSE_RESOURCES      = "resources";

	public final static String HTTP_PUT = "PUT";

	public final static String HTTP_DELETE = "DELETE";

	public final static String HTTP_ORIGIN = "Origin";

	public final static String HTTP_DEFAULTL_PATH = "";

	public final static String HTTP_PATH_SEPARTOR = "/";

	public final static String HTTP_DOT_SEPARTOR = ".";

	public final static String HTTP_HEADER_KEY = "Sec-WebSocket-Protocol";

	public final static String HTTP_HEADER_VALUE = "v4.channel.k8s.io";

	public final static MediaType HTTP_MEDIA_TYPE = MediaType.parse("application/json");

	public final static String QUERY_FIELD_SELECTOR = "&fieldSelector=";

	public final static String QUERY_LABEL_SELECTOR = "&labelSelector=";

	public final static String KUBE_ALL_NAMESPACES = "";

	public final static String KUBE_DEAFULT_NAMESPACE = "default";

	public final static String KUBE_NAMESPACE_PREFIX = "namespaces/";

	public final static String KUBE_WATCHER_PATH = "watch";

	public final static String KUBE_WATCHER_ENABLE = "?watch=true";

	/*************************************
	 * 
	 * Kubernetes specification
	 * 
	 *************************************/
	public final static String K8S_CORE_API_PATTERN = "/api/v1";

	public final static String K8S_CORE_STARTWITH_API_PATTERN = "/api";

	public final static String K8S_CORE_STARTWITH_APIS_PATTERN = "/apis";

	public final static String K8S_EXTENDED_URL_PATTERN = "k8s.io";
}
