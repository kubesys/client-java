/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import java.io.PrintStream;

import org.yaml.snakeyaml.DumperOptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

	protected KubernetesWriter writer;
	
	protected ObjectNode json; 

	public KindWriter(String name) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name});
	}
	
	public KindWriter(String name, String namespace) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name, "#NAMESPACE#", namespace});
	}
	
	public KindWriter(String[] kvs) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), kvs);
	}
	
	public void stream(PrintStream ps) throws Exception {
		ps.println(new YAMLMapper().writeValueAsString(json));
	}
	
	public void json(String path) {
		writer.writeAsJson(path, json);
	}
	
	static DumperOptions getDumperOptionsWithPipe() {
        DumperOptions options = new DumperOptions();
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
        return options;
    }
	public void yaml(String path) {
		writer.writeAsYaml(path, json);
	}
	
	public ObjectNode getObjectValue(String key) {
		if (!json.has(key)) {
			ObjectNode val = new ObjectMapper().createObjectNode();
			json.set(key, val);
		}
		return (ObjectNode) json.get(key);
	}
	
	public ObjectNode getObjectValue(ObjectNode node, String key) {
		if (!node.has(key)) {
			ObjectNode val = new ObjectMapper().createObjectNode();
			node.set(key, val);
		}
		return (ObjectNode) node.get(key);
	}
	
	public ArrayNode getArrayValue(ObjectNode node, String key) {
		if (!node.has(key)) {
			ArrayNode val = new ObjectMapper().createArrayNode();
			node.set(key, val);
		}
		return (ArrayNode) node.get(key);
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
	
	public ObjectNode toObjectNode(Object obj) throws Exception {
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(
				new ObjectMapper().writeValueAsString(obj));
	}
	
	public ObjectNode toObjectNode(String json) throws Exception {
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(json);
	}
	
	public abstract String getTemplate();
	
}
