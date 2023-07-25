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
@Catch(code = 403, exception = "请求被拒绝，客户端没有访问资源的权限")
public class KubernetesForbiddenAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	public KubernetesForbiddenAccessException(String message) {
		super(message);
	}

}
