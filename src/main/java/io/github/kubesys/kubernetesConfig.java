/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.util.HashMap;
import java.util.Map;

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
public class kubernetesConfig {

	/**
	 * kind2Name
	 */
	protected final Map<String, String> kind2NameMapping        = new HashMap<String, String>();
	
	/**
	 * kind2Version
	 */
	protected final Map<String, String> kind2VersionMapping     = new HashMap<String, String>();
	
	/**
	 * kind2Group
	 */
	protected final Map<String, String> kind2GroupMapping       = new HashMap<String, String>();

	/**
	 * kind2Namespace
	 */
	protected final Map<String, Boolean> kind2NamespacedMapping = new HashMap<String, Boolean>();

	/**
	 * kind2ApiPrefix
	 */
	protected final Map<String, String> kind2ApiPrefixMapping   = new HashMap<String, String>();

	/**
	 * @param kind                   kind
	 * @return                       name
	 */
	public String getName(String kind) {
		return kind2NameMapping.get(kind);
	}

	/**
	 * @param kind                   kind
	 * @param name                   name
	 */
	public void addName(String kind, String name) {
		this.kind2NameMapping.put(kind, name);
	}

	/**
	 * @param kind                   kind
	 * @return                       version
	 */     
	public String getVersion(String kind) {
		return kind2VersionMapping.get(kind);
	}

	/**
	 * @param kind                   kind
	 * @param version                version
	 */
	public void addVersion(String kind, String version) {
		this.kind2VersionMapping.put(kind, version);
	}

	/**
	 * @param kind                   kind
	 * @return                       version
	 */
	public String get2Group(String kind) {
		return kind2GroupMapping.get(kind);
	}

	/**
	 * @param kind                   kind
	 * @param group                  group
	 */
	public void addGroup(String kind, String group) {
		this.kind2GroupMapping.put(kind, group);
	}

	/**
	 * @param kind                   kind
	 * @return                       namespaced
	 */
	public Boolean isNamespaced(String kind) {
		return kind2NamespacedMapping.get(kind);
	}

	/**
	 * @param kind                   kind
	 * @param namespaced             namespaced
	 */
	public void addNamespaced(String kind, boolean namespaced) {
		this.kind2NamespacedMapping.put(kind, namespaced);
	}

	/**
	 * @param kind                   kind
	 * @return                       apiPrefix
	 */
	public String getApiPrefix(String kind) {
		return kind2ApiPrefixMapping.get(kind);
	}

	/**
	 * @param kind                   kind
	 * @param apiPrefix              apiPrefix
	 */
	public void addApiPrefix(String kind, String apiPrefix) {
		this.kind2ApiPrefixMapping.put(kind, apiPrefix);
	}

	/**
	 * @return                       kind2NameMapping
	 */
	public Map<String, String> getKind2NameMapping() {
		return kind2NameMapping;
	}

	/**
	 * @return                       kind2VersionMapping
	 */
	public Map<String, String> getKind2VersionMapping() {
		return kind2VersionMapping;
	}

	/**
	 * @return                       kind2GroupMapping
	 */
	public Map<String, String> getKind2GroupMapping() {
		return kind2GroupMapping;
	}

	/**
	 * @return                       kind2NamespacedMapping
	 */
	public Map<String, Boolean> getKind2NamespacedMapping() {
		return kind2NamespacedMapping;
	}

	/**
	 * @return                       kind2ApiPrefixMapping
	 */
	public Map<String, String> getKind2ApiPrefixMapping() {
		return kind2ApiPrefixMapping;
	}

}
