/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;


/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0 
 **/
public class ReqUtilTest {

	@Test
	void testNullUrl() {
		assertThrows(MalformedURLException.class, () -> {
			ReqUtil.post("abc", null, "abc");
        });
		
	}
	
	@Test
	void testInvalidNullUrl()  {
		assertThrows(MalformedURLException.class, () -> {
			ReqUtil.post("abc", "abc", "abc");
        });
	}
	
	@Test
	void testNullToken() throws MalformedURLException {
		assertNotNull(ReqUtil.post(null, "http://abc.com", "abc"));
	}
	
	@Test
	void testNotNullToken() throws MalformedURLException {
		assertNotNull(ReqUtil.post("abc", "http://abc.com", "abc"));
	}
	
	@Test
	void testNullBody() throws NullPointerException, MalformedURLException {
		assertNotNull(ReqUtil.post(null, "http://abc.com", null));
	}
	
	@Test
	void testNotNullBody() throws NullPointerException, MalformedURLException {
		assertNotNull(ReqUtil.post("abc", "http://abc.com", "abc"));
	}
	
}
