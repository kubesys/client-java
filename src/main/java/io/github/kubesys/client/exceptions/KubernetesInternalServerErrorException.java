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
@Catch(code = 500, desc = "服务器内部错误，表示服务器在处理请求时遇到了问题")
public class KubernetesInternalServerErrorException extends RuntimeException {

	/**
	 * uid
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	/**
	 * @param message message
	 */
	public KubernetesInternalServerErrorException(String message) {
		super(message);
	}

}
