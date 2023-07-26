/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/07/24
 * @version 1.0.0
 * 
 * it is used for generating Pods and related resources
 * 
 **/
class KubeUtilTest {

	static String EXPECTED_BINDING = "{\r\n"
			+ "  \"apiVersion\" : \"v1\",\r\n"
			+ "  \"kind\" : \"Binding\",\r\n"
			+ "  \"metadata\" : {\r\n"
			+ "    \"name\" : \"test-pod\",\r\n"
			+ "    \"namespace\" : \"test-ns\"\r\n"
			+ "  },\r\n"
			+ "  \"target\" : {\r\n"
			+ "    \"apiVersion\" : \"v1\",\r\n"
			+ "    \"kind\" : \"Node\",\r\n"
			+ "    \"name\" : \"test-node\"\r\n"
			+ "  }\r\n"
			+ "}";	
	@Test
	void testBinding() throws Exception {
		assertEquals(EXPECTED_BINDING, KubeUtil.toBinding("test-pod", "test-ns", "test-node").toPrettyString());
	}
	
}
