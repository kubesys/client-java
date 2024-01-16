/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
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
 * limitations under the License.
 */
package io.github.kubesys.client.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import io.github.kubesys.client.KubernetesConstants;

/**
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
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
	
	public static String fromMap(Map<String, String> map) {
		//%3D
		if (map == null || map.size() == 0) {
			return null;
		}
		
		
		StringBuilder sb = new StringBuilder();
		for (String key : map.keySet()) {
			sb.append(key + "%3D" + map.get(key) + ",");
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
