/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author wuheng@iscas.ac.cn
 *
 * Support create, update, delete, get and list [Kubernetes resources]
 * (https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
 * using [Kubernetes native API](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/)
 * 
 */
public class KubernetesRuleBase {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesRuleBase.class.getName());

	
	/*******************************************************
	 * 
	 *  Core
	 * 
	 ********************************************************/
	
	/**
	 * mapper
	 */
	public Map<String, List<String>>   kindToFullKindMapper        = new HashMap<>();
	
	/**
	 * kind
	 */
	public final Map<String, String>   fullKindToKindMapper        = new HashMap<>();
	
	/**
	 * name
	 */
	public final Map<String, String>   fullKindToNameMapper        = new HashMap<>();
	
	/**
	 * namespace
	 */
	public final Map<String, Boolean>  fullKindToNamespacedMapper  = new HashMap<>();
	
	/**
	 * version
	 */
	public final Map<String, String>   fullKindToVersionMapper     = new HashMap<>();
	
	/**
	 * group
	 */
	public final Map<String, String>   fullKindToGroupMapper       = new HashMap<>();

	/**
	 * verbs
	 */
	public final Map<String, JsonNode> fullKindToVerbsMapper       = new HashMap<>();


	/**
	 * apiPrefix
	 */
	public final Map<String, String>   fullKindToApiPrefixMapper   = new HashMap<>();
	
	
	/**
	 * @return                    fullKinds
	 */
	public Map<String, List<String>> getFullKinds() {
		return kindToFullKindMapper;
	}
	
	/**
	 * @param kind                kind
	 * @return                    fullKinds
	 */
	public List<String> getFullKinds(String kind) {
		return kindToFullKindMapper.get(kind);
	}

	/**
	 * @param kind                kind
	 * @param fullKind            fullKind
	 */
	public void addFullKind(String kind, String fullKind) {
		List<String> values = kindToFullKindMapper.containsKey(kind) ? 
				kindToFullKindMapper.get(kind) : new ArrayList<>();
		values.add(fullKind);
		kindToFullKindMapper.put(kind, values);
	}

	/**
	 * @param kind                kind
	 * @return                    fullKinds
	 * @throws Exception          exception
	 */
	public String getFullKind(String kind) throws Exception {
		List<String> values = kindToFullKindMapper.get(kind);
		if (values != null && values.size() == 1) {
			return values.get(0);
		}
		throw new Exception("Please use fullKind, " + getFullKinds(kind));
	}
	
	/**
	 * @param kind                kind
	 * @param fullKind            fullKind
	 */
	public void removeFullKind(String kind, String fullKind) {
		List<String> values = kindToFullKindMapper.get(kind);
		if (values == null) {
			return;
		}
		
		values.remove(fullKind);
		
		if (values.size() == 0) {
			kindToFullKindMapper.remove(kind);
		} else {
			kindToFullKindMapper.put(kind, values);
		}
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       name
	 */
	public String getName(String fullKind) {
		return fullKindToNameMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param name                   name
	 */
	public void addName(String fullKind, String name) {
		this.fullKindToNameMapper.put(fullKind, name);
	}
	
	/**
	 * @param fullKind             fullKind
	 */
	public void removeNameBy(String fullKind) {
		this.fullKindToNameMapper.remove(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */     
	public String getVersion(String fullKind) {
		return fullKindToVersionMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param version                version
	 */
	public void addVersion(String fullKind, String version) {
		this.fullKindToVersionMapper.put(fullKind, version);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */     
	public String removeVersionBy(String fullKind) {
		return fullKindToVersionMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */
	public String getGroup(String fullKind) {
		return fullKindToGroupMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param group                  group
	 */
	public void addGroup(String fullKind, String group) {
		this.fullKindToGroupMapper.put(fullKind, group);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       version
	 */
	public String removeGroupBy(String fullKind) {
		return fullKindToGroupMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       kind
	 */
	public String getKind(String fullKind) {
		return fullKindToKindMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param kind                   kind
	 */
	public void addKind(String fullKind, String kind) {
		this.fullKindToKindMapper.put(fullKind, kind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       kind
	 */
	public String removeKindBy(String fullKind) {
		return fullKindToKindMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       namespaced
	 */
	public Boolean isNamespaced(String fullKind) {
		return fullKindToNamespacedMapper.get(fullKind);
	}

	/**
	 * @param fullKind              fullKind
	 * @param namespaced             namespaced
	 */
	public void addNamespaced(String fullKind, boolean namespaced) {
		this.fullKindToNamespacedMapper.put(fullKind, namespaced);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       namespaced
	 */
	public Boolean removeNamespacedBy(String fullKind) {
		return fullKindToNamespacedMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind               fullKind
	 * @param verbs                   verbs
	 */
	public void addVerbs(String fullKind, JsonNode verbs) {
		this.fullKindToVerbsMapper.put(fullKind, verbs);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       apiPrefix
	 */
	public String getApiPrefix(String fullKind) {
		return fullKindToApiPrefixMapper.get(fullKind);
	}
	

	/**
	 * @param fullKind              fullKind
	 * @param apiPrefix              apiPrefix
	 */
	public void addApiPrefix(String fullKind, String apiPrefix) {
		this.fullKindToApiPrefixMapper.put(fullKind, apiPrefix);
	}

	/**
	 * @param fullKind              fullKind
	 * @return                       apiPrefix
	 */
	public String removeApiPrefixBy(String fullKind) {
		return fullKindToApiPrefixMapper.remove(fullKind);
	}
	
	/**
	 * @param fullKind              fullKind
	 * @return                       verbs
	 */
	public JsonNode removeVerbsBy(String fullKind) {
		return fullKindToVerbsMapper.remove(fullKind);
	}
	
	/**
	 * @return                       nameMapping
	 */
	public Map<String, String> getNameMapping() {
		return fullKindToNameMapper;
	}

	/**
	 * @return                       versionMapping
	 */
	public Map<String, String> getVersionMapping() {
		return fullKindToVersionMapper;
	}

	/**
	 * @return                       groupMapping
	 */
	public Map<String, String> getGroupMapping() {
		return fullKindToGroupMapper;
	}

	/**
	 * @return                       namespacedMapping
	 */
	public Map<String, Boolean> getNamespacedMapping() {
		return fullKindToNamespacedMapper;
	}

	/**
	 * @return                       apiPrefixMapping
	 */
	public Map<String, String> getApiPrefixMapping() {
		return fullKindToApiPrefixMapper;
	}

	/**
	 * @return                       verbsMapping
	 */
	public Map<String, JsonNode> getVerbsMapping() {
		return fullKindToVerbsMapper;
	}

	/**
	 * @return                       kindMapping
	 */
	public Map<String, String> getKindMapping() {
		return fullKindToKindMapper;
	}
	
}
