/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.testcases;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubesys.AbstractKubernetesClientTest;
import com.github.kubesys.KubernetesWriter;


/**
 * @author wuheng09@gmail.com
 *
 */
public class WriterTest extends AbstractKubernetesClientTest {

	static String CreateJSON = "{\r\n"
			+ "  \"apiVersion\": \"v1\",\r\n"
			+ "  \"kind\": \"Pod\",\r\n"
			+ "  \"metadata\": {\r\n"
			+ "    \"name\": \"busybox\",\r\n"
			+ "    \"namespace\": \"default\"\r\n"
			+ "  },\r\n"
			+ "  \"spec\": {\r\n"
			+ "    \"containers\": [\r\n"
			+ "      {\r\n"
			+ "        \"image\": \"busybox\",\r\n"
			+ "        \"env\": [{\r\n"
			+ "           \"name\": \"abc\",\r\n"
			+ "           \"value\": \"abc\"\r\n"
			+ "        }],\r\n"
			+ "        \"command\": [\r\n"
			+ "          \"sleep\",\r\n"
			+ "          \"3600\"\r\n"
			+ "        ],\r\n"
			+ "        \"imagePullPolicy\": \"IfNotPresent\",\r\n"
			+ "        \"name\": \"busybox\"\r\n"
			+ "      }\r\n"
			+ "    ],\r\n"
			+ "    \"restartPolicy\": \"Always\"\r\n"
			+ "  }\r\n"
			+ "}";
	
	
	public static void main(String[] args) throws Exception {
		KubernetesWriter writer = new KubernetesWriter();
		JsonNode json = new ObjectMapper().readTree(CreateJSON);
		writer.writeAsJson("test.json", json);
		writer.writeAsYaml("test.yaml", json);
	}

}
