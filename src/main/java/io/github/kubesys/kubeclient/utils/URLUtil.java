/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
 * 
 * 
 **/
public class URLUtil {

	protected static final Logger m_logger = Logger.getLogger(URLUtil.class.getName()); 
	
	/**
	 * @param parts path
	 * @return url
	 */
	public static String join(String... parts) {
		if (parts == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(parts[0]);
			sb.append(url.getProtocol()).append("://")
					.append(url.getHost()).append(":")
					.append(url.getPort() == -1 
						? url.getDefaultPort() 
								: url.getPort());
		} catch (MalformedURLException e) {
			m_logger.warning("invalid url: " + e);
			return null;
		}
		
		
		int len = parts.length;
		
		for (int i = 1; i < len - 1; i++) {
			while (parts[i].startsWith("/")) {
				parts[i] = parts[i].substring(1);
			}
			while (parts[i].endsWith("/")) {
				parts[i] = parts[i].substring(0, parts[i].length() - 1);
			}
			sb.append("/").append(parts[i]);
		}
		
		if (len != 1) {
			sb.append("/").append(parts[len - 1]);
		}
		return sb.toString();
	}
}
