/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/02
 * @version 1.0.3
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class StackCommon {
	
	static final String PATH = "/var/lib/doslab/";
	
	static final String NAMESPACE = "kube-stack";
	
	static final String CONFIG_USERNAME = "username";
	
	static final String CONFIG_PASSWORD = "password";
	
	static final String VOLUME_CONFIG = "config";
	
	static final String VOLUME_DATA = "data";
	
	static final String TOKEN_NAME = "kubernetes-client-token";
	
	static final String TOKEN_NAMESPACE = "kube-system";
	
	static String base64(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes());
	}
	
	static String read(String path) throws Exception {
		return Files.readString(Paths.get(path));
	}
}
