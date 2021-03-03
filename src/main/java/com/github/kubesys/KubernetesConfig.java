/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author wuheng09@gmail.com
 *
 * using command kubectl api-resources <br><br>
 * 
 * Note that 'name', 'kind', 'group', 'version', 'namespaced' and 'apiPrefix' are the concepts of Kubernetes. <br>
 * 
 * NAME                              SHORTNAMES   APIGROUP                       NAMESPACED   KIND                  <br>
 * bindings                                                                      true         Binding               <br>
 * componentstatuses                 cs                                          false        ComponentStatus       <br>
 * configmaps                        cm                                          true         ConfigMap             <br>
 * endpoints                         ep                                          true         Endpoints             <br>
 * events                            ev                                          true         Event                 <br>
 * limitranges                       limits                                      true         LimitRange            <br>
 * namespaces                        ns                                          false        Namespace             <br>
 * nodes                             no                                          false        Node                  <br>
 */
public final class KubernetesConfig {

	/**
	 * name
	 */
	protected final Map<String, String> nameMapping        = new HashMap<>();
	
	/**
	 * kind
	 */
	protected final Map<String, String> kindMapping        = new HashMap<>();
	
	/**
	 * version
	 */
	protected final Map<String, String> versionMapping     = new HashMap<>();
	
	/**
	 * group
	 */
	protected final Map<String, String> groupMapping       = new HashMap<>();

	/**
	 * namespace
	 */
	protected final Map<String, Boolean> namespacedMapping = new HashMap<>();

	/**
	 * apiPrefix
	 */
	protected final Map<String, String> apiPrefixMapping   = new HashMap<>();
	
	/**
	 * verbs
	 */
	protected final Map<String, JsonNode> verbsMapping     = new HashMap<>();

	/**
	 * @param key                    key
	 * @return                       name
	 */
	public String getName(String key) {
		return nameMapping.get(key);
	}

	/**
	 * @param key                    key
	 * @param name                   name
	 */
	public void addName(String key, String name) {
		this.nameMapping.put(key, name);
	}
	
	/**
	 * @param key                   key
	 */
	public void removeNameBy(String key) {
		this.nameMapping.remove(key);
	}

	/**
	 * @param key                    key
	 * @return                       version
	 */     
	public String getVersion(String key) {
		return versionMapping.get(key);
	}

	/**
	 * @param key                    key
	 * @param version                version
	 */
	public void addVersion(String key, String version) {
		this.versionMapping.put(key, version);
	}

	/**
	 * @param key                    key
	 * @return                       version
	 */     
	public String removeVersionBy(String key) {
		return versionMapping.remove(key);
	}
	
	/**
	 * @param key                    key
	 * @return                       version
	 */
	public String getGroup(String key) {
		return groupMapping.get(key);
	}

	/**
	 * @param key                    key
	 * @param group                  group
	 */
	public void addGroup(String key, String group) {
		this.groupMapping.put(key, group);
	}
	
	/**
	 * @param key                    key
	 * @return                       version
	 */
	public String removeGroupBy(String key) {
		return groupMapping.remove(key);
	}
	
	/**
	 * @param key                    key
	 * @return                       kind
	 */
	public String getKind(String key) {
		return kindMapping.get(key);
	}

	/**
	 * @param key                    key
	 * @param kind                   kind
	 */
	public void addKind(String key, String kind) {
		this.kindMapping.put(key, kind);
	}
	
	/**
	 * @param key                    key
	 * @return                       kind
	 */
	public String removeKindBy(String key) {
		return kindMapping.remove(key);
	}
	
	/**
	 * @param key                    key
	 * @return                       namespaced
	 */
	public Boolean isNamespaced(String key) {
		return namespacedMapping.get(key);
	}

	/**
	 * @param key                    key
	 * @param namespaced             namespaced
	 */
	public void addNamespaced(String key, boolean namespaced) {
		this.namespacedMapping.put(key, namespaced);
	}

	/**
	 * @param key                    key
	 * @return                       namespaced
	 */
	public Boolean removeNamespacedBy(String key) {
		return namespacedMapping.remove(key);
	}
	
	/**
	 * @param key                     key
	 * @param verbs                   verbs
	 */
	public void addVerbs(String key, JsonNode verbs) {
		this.verbsMapping.put(key, verbs);
	}
	
	/**
	 * @param key                    key
	 * @return                       apiPrefix
	 */
	public String getApiPrefix(String key) {
		return apiPrefixMapping.get(key);
	}
	

	/**
	 * @param key                    key
	 * @param apiPrefix              apiPrefix
	 */
	public void addApiPrefix(String key, String apiPrefix) {
		this.apiPrefixMapping.put(key, apiPrefix);
	}

	/**
	 * @param key                    key
	 * @return                       apiPrefix
	 */
	public String removeApiPrefixBy(String key) {
		return apiPrefixMapping.remove(key);
	}
	
	/**
	 * @param key                    key
	 * @return                       verbs
	 */
	public JsonNode removeVerbsBy(String key) {
		return verbsMapping.remove(key);
	}
	
	/**
	 * @return                       nameMapping
	 */
	public Map<String, String> getNameMapping() {
		return nameMapping;
	}

	/**
	 * @return                       versionMapping
	 */
	public Map<String, String> getVersionMapping() {
		return versionMapping;
	}

	/**
	 * @return                       groupMapping
	 */
	public Map<String, String> getGroupMapping() {
		return groupMapping;
	}

	/**
	 * @return                       namespacedMapping
	 */
	public Map<String, Boolean> getNamespacedMapping() {
		return namespacedMapping;
	}

	/**
	 * @return                       apiPrefixMapping
	 */
	public Map<String, String> getApiPrefixMapping() {
		return apiPrefixMapping;
	}

	/**
	 * @return                       verbsMapping
	 */
	public Map<String, JsonNode> getVerbsMapping() {
		return verbsMapping;
	}

	/**
	 * @return                       kindMapping
	 */
	public Map<String, String> getKindMapping() {
		return kindMapping;
	}

}
