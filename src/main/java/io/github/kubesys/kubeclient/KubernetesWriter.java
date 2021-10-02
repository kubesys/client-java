/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author wuheng@iscas.ac.cn
 *
 * 
 */
public class KubernetesWriter {

	public final static Logger m_logger = Logger.getLogger(KubernetesWriter.class.getName());
	
	public void writeAsJson(String path, JsonNode json) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(path));
			fw.write(json.toPrettyString());
		} catch (Exception ex) {
			m_logger.severe(ex.toString());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					m_logger.severe(e.toString());
				}
			}
		}
	}
	
	public void writeAsYaml(String path, JsonNode json) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(path));
			fw.write(new YAMLMapper().writeValueAsString(json));
		} catch (Exception ex) {
			m_logger.severe(ex.toString());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					m_logger.severe(e.toString());
				}
			}
		}
	}
	
}
