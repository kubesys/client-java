/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import java.io.PrintStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.github.kubesys.client.addons.KubernetesWriter;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
public abstract class KindWriter {

	protected final KubernetesWriter writer;
	
	protected final ObjectNode json; 

	public KindWriter(String name) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name});
	}
	
	public KindWriter(String name, String namespace) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name, "#NAMESPACE#", namespace});
	}
	
	public void stream(PrintStream ps) throws Exception {
		ps.println(new YAMLMapper().writeValueAsString(json));
	}
	
	public void json(String path) {
		writer.writeAsJson(path, json);
	}
	
	public void yaml(String path) {
		writer.writeAsYaml(path, json);
	}
	
	public ObjectNode toObjectNode(String str, String[] list) throws Exception {
		for (int i = 0; i < list.length; i = i + 2) {
			str = str.replaceAll(list[i], list[i + 1]);
		}
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(str);
	}
	
	public ObjectNode toObjectNode(String key, JsonNode json) throws Exception {
		ObjectNode val = new ObjectMapper().createObjectNode();
		val.set(key, json);
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(val.toPrettyString());
	}
	
	public abstract String getTemplate();
}
