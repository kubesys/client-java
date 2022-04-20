/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.github.kubesys.client.utils.URLUtil;

/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
public class URLUtilTest {

	@Test
	public void testNamespacedWithNullValue1() {
		assertEquals("", URLUtil.namespacePath(true, null));
	}
	
	@Test
	public void testNamespacedWithNullValue2() {
		assertEquals("", URLUtil.namespacePath(true, ""));
	}
	
	@Test
	public void testNamespacedWithValue() {
		assertEquals("namespaces/test", URLUtil.namespacePath(true, "test"));
	}
	
	@Test
	public void testClusterWithValue() {
		assertEquals("", URLUtil.namespacePath(false, "test"));
	}
	
	@Test
	public void testClusterWithoutValue() {
		assertEquals("", URLUtil.namespacePath(false, null));
	}
}
