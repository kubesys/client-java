/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;

import org.junit.Test;

/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
public class ReqUtilTest {

	@Test(expected = MalformedURLException.class)
	public void testNullUrl() throws MalformedURLException {
		ReqUtil.post("abc", null, "abc");
	}
	
	@Test(expected = MalformedURLException.class)
	public void testInvalidNullUrl() throws MalformedURLException {
		ReqUtil.post("abc", "abc", "abc");
	}
	
	@Test
	public void testValidUrl() throws MalformedURLException {
		assertNotNull(ReqUtil.post("abc", "http://abc.com", "abc"));
	}
	
	@Test
	public void testNullToken() throws MalformedURLException {
		assertNotNull(ReqUtil.post(null, "http://abc.com", "abc"));
	}
	
	@Test
	public void testNotNullToken() throws MalformedURLException {
		assertNotNull(ReqUtil.post("abc", "http://abc.com", "abc"));
	}
	
	@Test
	public void testNullBody() throws NullPointerException, MalformedURLException {
		assertNotNull(ReqUtil.post(null, "http://abc.com", null));
	}
	
	@Test
	public void testNotNullBody() throws NullPointerException, MalformedURLException {
		assertNotNull(ReqUtil.post("abc", "http://abc.com", "abc"));
	}
	
}
