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
@Catch(code = 409, desc = "请求冲突，通常用于表示资源的当前状态与请求的条件不匹配")
public class KubernetesConflictResourceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	public KubernetesConflictResourceException(String message) {
		super(message);
	}

}
