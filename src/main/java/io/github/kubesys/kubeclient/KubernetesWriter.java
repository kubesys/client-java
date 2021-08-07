/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.io.File;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author wuheng@iscas.ac.cn
 *
 * 
 */
public class KubernetesWriter {

	public void writeAsJson(String path, JsonNode json) throws Exception {
		FileWriter fw = new FileWriter(new File(path));
		fw.write(json.toPrettyString());
		fw.close();
	}
	
	public void writeAsYaml(String path, JsonNode json) throws Exception {
		FileWriter fw = new FileWriter(new File(path));
		fw.write(new YAMLMapper().writeValueAsString(json));
		fw.close();
	}
	
}
