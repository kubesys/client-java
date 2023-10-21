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

import io.github.kubesys.client.KubernetesWriter;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
public abstract class KindWriter {

	/**
	 * writer
	 */
	protected KubernetesWriter writer;
	
	/**
	 * json
	 */
	protected ObjectNode json; 

	/**
	 * @param name name
	 * @throws Exception exception
	 */
	public KindWriter(String name) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name});
	}
	
	/**
	 * @param name name
	 * @param namespace namespace
	 * @throws Exception exception
	 */
	public KindWriter(String name, String namespace) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), new String[] {"#NAME#", name, "#NAMESPACE#", namespace});
	}
	
	/**
	 * @param kvs key and value
	 * @throws Exception exception
	 */
	public KindWriter(String[] kvs) throws Exception {
		this.writer = new KubernetesWriter();
		this.json = toObjectNode(getTemplate(), kvs);
	}
	
	/**
	 * @param ps printStream
	 * @throws Exception exception
	 */
	public void stream(PrintStream ps) throws Exception {
		ps.println(new YAMLMapper().writeValueAsString(json));
	}
	
	/**
	 * @param path  path
	 */
	public void json(String path) {
		writer.writeAsJson(path, json);
	}
	
	/**
	 * @return options
	 */
	static DumperOptions getDumperOptionsWithPipe() {
        DumperOptions options = new DumperOptions();
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
        return options;
    }
	/**
	 * @param path path
	 */
	public void yaml(String path) {
		writer.writeAsYaml(path, json);
	}
	
	/**
	 * @param key key
	 * @return objectNode
	 */
	public ObjectNode getObjectValue(String key) {
		if (!json.has(key)) {
			ObjectNode val = new ObjectMapper().createObjectNode();
			json.set(key, val);
		}
		return (ObjectNode) json.get(key);
	}
	
	/**
	 * @param node node
	 * @param key key
	 * @return objectNode
	 */
	public ObjectNode getObjectValue(ObjectNode node, String key) {
		if (!node.has(key)) {
			ObjectNode val = new ObjectMapper().createObjectNode();
			node.set(key, val);
		}
		return (ObjectNode) node.get(key);
	}
	
	/**
	 * @param node node
	 * @param key key
	 * @return arrayNode
	 */
	public ArrayNode getArrayValue(ObjectNode node, String key) {
		if (!node.has(key)) {
			ArrayNode val = new ObjectMapper().createArrayNode();
			node.set(key, val);
		}
		return (ArrayNode) node.get(key);
	}
	
	/**
	 * @param str str
	 * @param list list
	 * @return obejctNode
	 * @throws Exception exception
	 */
	public ObjectNode toObjectNode(String str, String[] list) throws Exception {
		for (int i = 0; i < list.length; i = i + 2) {
			str = str.replaceAll(list[i], list[i + 1]);
		}
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(str);
	}
	
	/**
	 * @param key key
	 * @param json json
	 * @return objectNode
	 * @throws Exception exception
	 */
	public ObjectNode toObjectNode(String key, JsonNode json) throws Exception {
		ObjectNode val = new ObjectMapper().createObjectNode();
		val.set(key, json);
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(val.toPrettyString());
	}
	
	/**
	 * @param obj obj
	 * @return objectNode
	 * @throws Exception exception
	 */
	public ObjectNode toObjectNode(Object obj) throws Exception {
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(
				new ObjectMapper().writeValueAsString(obj));
	}
	
	/**
	 * @param json json
	 * @return objectNode
	 * @throws Exception exception
	 */
	public ObjectNode toObjectNode(String json) throws Exception {
		return  (ObjectNode) new ObjectMapper(new YAMLFactory()).readTree(json);
	}
	
	/**
	 * @return template
	 */
	public abstract String getTemplate();
	
}
