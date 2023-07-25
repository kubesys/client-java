/*
  Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.exceptions;

import io.github.kubesys.client.annotations.Catch;

/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/07/25
 * @version 1.0.0
 *
 */
@Catch(code = 402, exception = "Kubernetes的请求地址缺少协议 http，或者是https")
public class KubernetesUnknownUrlException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	public KubernetesUnknownUrlException(String message) {
		super(message);
	}

}
