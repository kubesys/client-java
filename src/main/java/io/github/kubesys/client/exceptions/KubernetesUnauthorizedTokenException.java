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
@Catch(code = 401, exception = "未授权，需要进行身份验证或令牌无效")
public class KubernetesUnauthorizedTokenException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	public KubernetesUnauthorizedTokenException(String message) {
		super(message);
	}

}
