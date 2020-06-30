/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public class KubernetesException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3990816490885481361L;

	public KubernetesException() {
		super();
	}

	public KubernetesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public KubernetesException(String message, Throwable cause) {
		super(message, cause);
	}

	public KubernetesException(String message) {
		super(message);
	}

	public KubernetesException(Throwable cause) {
		super(cause);
	}

	
}
