/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author  wuheng@iscas.ac.cn
 * 
 * 
 **/
public abstract class KubernetesWatcher implements Runnable {

	public static final Logger m_logger = Logger.getLogger(KubernetesWatcher.class.getName());
	/**
	 * client
	 */
	protected KubernetesClient client;
	
	protected HttpGet request;
	
	protected KubernetesWatcher(KubernetesClient client) {
		super();
		this.client = client;
	}

	public void setRequest(HttpGet request) {
		this.request = request;
	}

	@Override
	public void run() {
		
		BufferedReader br = null;
		try {
			CloseableHttpClient httpClient = client.copy().getHttpClient();
			CloseableHttpResponse execute = httpClient.execute(request);
			
			br = new BufferedReader(new InputStreamReader(
						execute.getEntity().getContent(), Charset.forName("utf-8")));
		    String line = null;
		    
		    while ((line = br.readLine()) != null) {
		    	JsonNode json = new ObjectMapper().readTree(line);
		    	if (!json.has(KubernetesConstants.KUBE_TYPE)) {
		    		continue;
		    	}
				String type = json.get(KubernetesConstants.KUBE_TYPE).asText();
				JsonNode obj = json.get(KubernetesConstants.KUBE_OBJECT);
				if (type.equals(KubernetesConstants.JSON_TYPE_ADDED)) {
					doAdded(obj);
				} else if (type.equals(KubernetesConstants.JSON_TYPE_MODIFIED)) {
					doModified(obj);
				} else if (type.equals(KubernetesConstants.JSON_TYPE_DELETED)) {
					doDeleted(obj);
				}
		    }
		} catch (Exception ex) {
			m_logger.severe(ex.toString());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					m_logger.warning(e.toString());
				}
			}
		}
		
	}
	

	/**
	 * @param node                  node
	 */
	public abstract void doAdded(JsonNode node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doModified(JsonNode node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doDeleted(JsonNode node);
	
	/**
	 * 
	 */
	public abstract void doClose();
	
}
