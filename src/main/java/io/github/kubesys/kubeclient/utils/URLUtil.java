/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import io.github.kubesys.kubeclient.KubernetesConstants;

/**
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
 * 
 * 
 **/
public class URLUtil {

	protected static final Logger m_logger = Logger.getLogger(URLUtil.class.getName()); 
	
	private URLUtil() {
		super();
	}

	/**
	 * @param parts path
	 * @return url
	 */
	public static String join(String... parts) {
		if (parts == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		// protocol + "://" + host : ":" + "port"
		// https://1.1.1.1:6443
		try {
			String url = new URL(parts[0]).toString().trim();
			while (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
			sb.append(url);
		} catch (MalformedURLException e) {
			m_logger.warning("invalid url: " + e);
			return null;
		}
		

		// add all paths
		// the path is /a/b/c
		int len = parts.length;
		for (int i = 1; i < len; i++) {
			parts[i] = parts[i].trim();
			while (parts[i].startsWith("/")) {
				parts[i] = parts[i].substring(1);
			}
			while (parts[i].endsWith("/")) {
				parts[i] = parts[i].substring(0, parts[i].length() - 1);
			}
			
			if (parts[i].length() == 0) {
				continue;
			} 
			
			if (parts[i].startsWith("?")) {
				sb.append(parts[i]);
			} else {
				sb.append("/").append(parts[i]);
			}
		}
		
		return sb.toString();
	}
	
	public static String namespacePath(boolean namespaced, String namespace) {
		return (namespaced && namespace != null && namespace.length() != 0)
				? KubernetesConstants.KUBEAPI_NAMESPACES_PATTERN + namespace
				: KubernetesConstants.VALUE_ALL_NAMESPACES;
	}
}
