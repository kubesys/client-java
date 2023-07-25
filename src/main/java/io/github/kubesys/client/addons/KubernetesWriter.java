/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.addons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2.1.1
 * 
 */
public class KubernetesWriter {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesWriter.class.getName());
	
	/**
	 * @param path           path
	 * @param json           json
	 */
	public void writeAsJson(String path, JsonNode json) {
		write(createWriter(path), json.toPrettyString());
	}
	
	/**
	 * @param path           path
	 * @param json           json
	 */
	public void writeAsYaml(String path, JsonNode json) {
		String yaml = null;
		
		try {
			yaml = new YAMLMapper().writeValueAsString(json);
		} catch (JsonProcessingException e) {
			m_logger.warning(e.toString());
		}
		
		write(createWriter(path), yaml);
	}

	/**
	 * @param fw      fileWriter
	 * @param value   value
	 */
	private void write(FileWriter fw, String value) {
		if (fw == null || value == null || "".equals(value)) {
			return;
		}
		
		try {
			fw.write(value);
		} catch (IOException e) {
			m_logger.warning(e.toString());
		}
		closeWriter(fw);
	}
	
	/**
	 * @param path     path
	 * @return         fileWriter
	 */
	private FileWriter createWriter(String path) {
		try {
			return new FileWriter(new File(path));
		} catch (IOException e) {
			m_logger.severe("file " + path + " is not exit.");
			return null;
		}
	}
	
	/**
	 * @param fw      fileWriter
	 */
	private void closeWriter(FileWriter fw) {
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				m_logger.severe(e.toString());
			}
		}
	}
}
