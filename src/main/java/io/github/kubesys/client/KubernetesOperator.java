/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *limitations under the License.
 */
package io.github.kubesys.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kubesys.client.watchers.KubernetesObjectWatcher;

/**
 * @author  wuheng09@gmail.com
 * @since   2.1.1
 * 
 * it is used for generating Pods and related resources
 * 
 **/
public abstract class KubernetesOperator<O> {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesOperator.class.getName());
	
	/**
	 * client
	 */
	protected final KubernetesClient client;
	
	/**
	 * cached
	 */
	protected final Map<String, Map<String, List<O>>> cached = new HashMap<>();
	
	
	protected KubernetesOperator(KubernetesClient client, String kind) throws Exception {
		this.client = client;
		client.watchResources(kind, new KubernetesObjectWatcher<O>(client) {

			@Override
			public void doObjectAdded(O node) {
				Map<String, List<O>> values = parseFrom(node);
				doResourceCreate(values);
				cached.put(getName(node), values);
			}

			@Override
			public void doObjectModified(O node) {
				Map<String, List<O>> current = parseFrom(node);
				Map<String, List<O>> last = cached.get(getName(node));
				doResourceCreate(diffAdded(current, last));
				doResourceRemove(diffAdded(current, last));
				cached.put(getName(node), current);
			}

			@Override
			public void doObjectDeleted(O node) {
				Map<String, List<O>> values = parseFrom(node);
				doResourceRemove(values);
				cached.remove(getName(node));
			}

			@Override
			public void doClose() {
				System.exit(1);
			}
		});
	}
	
	public abstract Map<String, List<O>> parseFrom(O currentOne);
	
	public abstract Map<String, List<O>> diffAdded(
								Map<String, List<O>> currentOne, 
								Map<String, List<O>> lastOne);
	
	public abstract Map<String, List<O>> diffRemoved(
								Map<String, List<O>> currentOne, 
								Map<String, List<O>> lastOne);
	
	private void doResourceCreate(Map<String, List<O>> objects) {
		for (String key : objects.keySet()) {
			for (Object obj : objects.get(key)) {
				try {
					client.createResource(new ObjectMapper().readTree(
							new ObjectMapper().writeValueAsString(obj)));
				} catch (Exception e) {
					m_logger.severe(e.toString());
				}
			}
		}
	}
	
	private void doResourceRemove(Map<String, List<O>> objects) {
		for (String key : objects.keySet()) {
			for (Object obj : objects.get(key)) {
				try {
					client.deleteResource(new ObjectMapper().readTree(
							new ObjectMapper().writeValueAsString(obj)));
				} catch (Exception e) {
					m_logger.severe(e.toString());
				}
			}
		}
	}
	
	private String getName(O o) {
		JsonNode node = null;
		try {
			node = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(o));
		} catch (JsonMappingException e) {
			m_logger.severe(e.toString());
		} catch (JsonProcessingException e) {
			m_logger.severe(e.toString());
		}

		return node == null ? null : getName(node.get("metadata"));
	}
	
	private String getName(JsonNode meta) {
		return meta.has("namespace") 
				? meta.get("namespace").asText() + "-" + meta.get("name").asText()
						: "default-" + meta.get("name").asText();
	}
}
