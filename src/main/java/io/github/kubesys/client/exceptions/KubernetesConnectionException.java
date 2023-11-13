/*
  Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.exceptions;

import io.github.kubesys.client.annotations.Catch;

/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/10/20
 * @version 1.0.5
 *
 */
@Catch(code = 600, desc = "Kubernetes无法连接")
public class KubernetesConnectionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	public KubernetesConnectionException(String message) {
		super(message);
	}

}
