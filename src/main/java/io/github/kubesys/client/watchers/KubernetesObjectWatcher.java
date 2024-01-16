/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
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
 * limitations under the License.
 */
package io.github.kubesys.client.watchers;


import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kubesys.client.KubernetesClient;
import io.github.kubesys.client.KubernetesWatcher;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class KubernetesObjectWatcher<O> extends KubernetesWatcher {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesObjectWatcher.class.getName());
	
	/**
	 * clz
	 */
	protected final Class<?> clz;
	
	/**
	 * @param client client
	 * @throws Exception  Exception
	 */
	protected KubernetesObjectWatcher(KubernetesClient client) throws Exception {
		super(client);
		String gSclz = getClass().getGenericSuperclass().getTypeName();
		int idx = gSclz.indexOf("<");
		int edx = gSclz.indexOf(">");
		this.clz = Class.forName(gSclz.substring(idx + 1, edx));
	}

	/**
	 * @param node                  node
	 */
	public abstract void doObjectAdded(O node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doObjectModified(O node);
	
	/**
	 * @param node                  node
	 */
	public abstract void doObjectDeleted(O node);
	
	/**
	 * 
	 */
	public abstract void doClose();

	@SuppressWarnings("unchecked")
	@Override
	public void doAdded(JsonNode node) {
		try {
			doObjectAdded((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doModified(JsonNode node) {
		try {
			doObjectModified((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doDeleted(JsonNode node) {
		try {
			doObjectDeleted((O) new ObjectMapper().readValue(
								node.toPrettyString(), clz));
		} catch (Exception e) {
			m_logger.severe(e.getMessage());
		}
	}
	
	
}
