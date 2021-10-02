/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
public class URLUtil {

	/**
	 * @param parts path
	 * @return url
	 */
	public static String join(String... parts) {
		StringBuilder sb = new StringBuilder();

		String urlQueryParams = "";
		if (parts.length > 0) {
			String urlWithoutQuery = parts[0];
			try {
				URI uri = new URI(parts[0]);
				if (containsQueryParam(uri)) {
					urlQueryParams = uri.getQuery();
					urlWithoutQuery = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null,
							uri.getFragment()).toString();
				}
			} catch (URISyntaxException e) {
				// Not all first parameters are URL
			}
			sb.append(urlWithoutQuery).append("/");
		}

		StringBuilder queryParams = new StringBuilder();
		for (int i = 1; i < parts.length; i++) {
			try {
				URI partUri = new URI(parts[i]);
				if (containsQueryParam(partUri)) {
					queryParams = getQueryParams(partUri, parts, i + 1);
					// If we start detecting query params then everything will be query params part
					break;
				}

				sb.append(parts[i]);

			} catch (URISyntaxException e) {
				sb.append(parts[i]);
			}

			if (i < parts.length - 1) {
				sb.append("/");
			}

		}

		appendQueryParametersFromOriginalUrl(sb, urlQueryParams, queryParams);
		String joined = sb.toString();

		// And normalize it...
		return joined.replaceAll("/+", "/").replaceAll("/\\?", "?").replaceAll("/#", "#").replaceAll(":/", "://");

	}

	/**
	 * @param sb             stringbuffer
	 * @param urlQueryParams params
	 * @param queryParams    params
	 */
	private static void appendQueryParametersFromOriginalUrl(StringBuilder sb, String urlQueryParams,
			StringBuilder queryParams) {
		if (!urlQueryParams.isEmpty()) {
			if (queryParams.length() == 0) {
				queryParams.append("?");
			} else {
				queryParams.append("&");
			}
			queryParams.append(urlQueryParams);
		}

		sb.append(queryParams);
	}

	/**
	 * @param firstPart first part
	 * @param parts     parts
	 * @param index     index
	 * @return url
	 */
	private static StringBuilder getQueryParams(URI firstPart, String[] parts, int index) {
		StringBuilder queryParams = new StringBuilder();
		queryParams.append(firstPart.toString());

		for (int i = index; i < parts.length; i++) {
			String param = parts[i];

			if (!param.startsWith("&")) {
				queryParams.append("&");
			}
			queryParams.append((param));
		}

		return queryParams;
	}

	/**
	 * @param uri uri
	 * @return true or false
	 */
	private static boolean containsQueryParam(URI uri) {
		return uri.getQuery() != null;
	}
}
