/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 
 * It is used for creating various HttpRequest
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
class URLUtilTest {

	@Test
	void testNamespacedWithNullValue1() {
		assertEquals("", URLUtil.namespacePath(true, null));
	}
	
	@Test
	void testNamespacedWithNullValue2() {
		assertEquals("", URLUtil.namespacePath(true, ""));
	}
	
	
	@Test
	void testNamespacedWithValue() {
		assertEquals("namespaces/test", URLUtil.namespacePath(true, "test"));
	}
	
	@Test
	void testClusterWithValue() {
		assertEquals("", URLUtil.namespacePath(false, "test"));
	}
	
	@Test
	void testClusterWithoutValue() {
		assertEquals("", URLUtil.namespacePath(false, null));
	}
}
