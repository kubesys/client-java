/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import io.github.kubesys.client.beans.KubernetesAdminConfig;


/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0 
 **/
class ReqUtilTest {

	@Test
	void testNullUrl() throws Exception {
		KubernetesAdminConfig config = new KubernetesAdminConfig("127.0.0.1", "abc");
		assertThrows(MalformedURLException.class, () -> {
			ReqUtil.post(config, null, "abc");
        });
		
	}
	
	@Test
	void testInvalidNullUrl() throws Exception  {
		KubernetesAdminConfig config = new KubernetesAdminConfig("127.0.0.1", "abc");
		assertThrows(MalformedURLException.class, () -> {
			ReqUtil.post(config, "abc", "abc");
        });
	}
	
	
	@Test
	void testNotNullToken() throws Exception {
		KubernetesAdminConfig config = new KubernetesAdminConfig("127.0.0.1", "abc");
		assertNotNull(ReqUtil.post(config, "http://abc.com", "abc"));
	}
	
	
	@Test
	void testNotNullBody() throws Exception {
		KubernetesAdminConfig config = new KubernetesAdminConfig("127.0.0.1", "abc");
		assertNotNull(ReqUtil.post(config, "http://abc.com", "abc"));
	}
	
}
