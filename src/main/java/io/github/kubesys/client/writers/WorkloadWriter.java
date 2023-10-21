/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
public abstract class WorkloadWriter extends KindWriter {

	/**
	 * template
	 */
	static final String TEMPLATE = "apiVersion: \"#APIVERSION#\"\r\n"
			+ "kind: \"#KIND#\"\r\n"
			+ "metadata: \r\n"
			+ "  name: \"#NAME#\"\r\n"
			+ "  namespace: \"#NAMESPACE#\"\r\n"
			+ "  labels: \r\n"
			+ "    name: \"#NAME#\"\r\n"
			+ "spec: \r\n"
			+ "  replicas: #NUMBER# \r\n"
			+ "  selector: \r\n"
			+ "    matchLabels:\r\n"
			+ "      name: \"#NAME#\" \r\n"
			+ "  template: \r\n"
			+ "    metadata: \r\n"
			+ "      labels: \r\n"
			+ "        name: \"#NAME#\"";

	/**
	 * @param kvs key and value
	 * @throws Exception exception
	 */
	public WorkloadWriter(String[] kvs) throws Exception {
		super(kvs);
		if (this.json.get("spec").get("replicas").asInt() == 0) {
			((ObjectNode) json.get("spec")).remove("replicas");
		}
	}
	
	/**
	 * mater
	 */
	static final String MATSER = "nodeSelector:\r\n"
			+ "  node-role.kubernetes.io/control-plane: \"\"\r\n"
			+ "tolerations:\r\n"
			+ "  - key: node-role.kubernetes.io/control-plane\r\n"
			+ "    effect: NoSchedule";
	
	/**
	 * @return this object
	 * @throws Exception exception
	 */
	public WorkloadWriter withMasterEnbale() throws Exception {
	    ObjectNode template = getObjectValue(getObjectValue("spec"), "template");
	    template.set("spec", toObjectNode(MATSER));
		return this;
	}
	
	/**
	 * @param container container
	 * @return this object
	 * @throws Exception exception
	 */
	public WorkloadWriter withContainer(Container container) throws Exception {
	    ArrayNode containers = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "containers");
	    ObjectNode c = toObjectNode(container);
	    containers.add(c);
		return this;
	}
	
	/**
	 * volume
	 */
	static final String VOLUME = "name: \"#NAME#\"\r\n"
			+ "persistentVolumeClaim:\r\n"
			+ "  claimName: #PVC#";
	
	/**
	 * @param name name
	 * @param pvc pvc
	 * @return this object
	 * @throws Exception exception
	 */
	public WorkloadWriter withPVCVolume(String name, String pvc) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(VOLUME, new String[] {"#NAME#", name, "#PVC#", pvc});
	    volumes.add(v);
		return this;
	}
	
	/**
	 * host
	 */
	static final String HOST = "name: #NAME#\r\n"
			+ "hostPath:\r\n"
			+ "  path: #PATH#";
	
	/**
	 * @param name name
	 * @param path path
	 * @return this object
	 * @throws Exception exception
	 */
	public WorkloadWriter withHostVolume(String name, String path) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(HOST, new String[] {"#NAME#", name, "#PATH#", path});
	    volumes.add(v);
		return this;
	}
	
	/**
	 * configmap
	 */
	static final String CONFIGMAP = "name: #NAME#\r\n"
			+ "configMap:\r\n"
			+ "  name: #CONFIGMAP_NAME#\r\n"
			+ "  items:\r\n"
			+ "  - key: #CONFIGMAP_KEY#\r\n"
			+ "    path: #PATH#";
	
	/**
	 * @param name name
	 * @param cm configmap
	 * @param cmKey key
	 * @param path path
	 * @return this object
	 * @throws Exception exception
	 */
	public WorkloadWriter withConfigMapVolume(String name, String cm, String cmKey, String path) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(CONFIGMAP, new String[] {"#NAME#", name, "#CONFIGMAP_NAME#", cm, "#CONFIGMAP_KEY#", cmKey, "#PATH#", path});
	    volumes.add(v);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	
	/**
	 * @author henry
	 *
	 */
	public static class Container {
		
		/**
		 * name
		 */
		String name;
		
		/**
		 * image
		 */
		String image;
		
		/**
		 * imagePullPolicy
		 */
		String imagePullPolicy = "IfNotPresent";
		
		/**
		 * env
		 */
		Env[] env;
		
		/**
		 * args
		 */
		String[] args;
		
		/**
		 * ports
		 */
		Port[] ports;
		
		/**
		 * volumeMounts
		 */
		VolumeMount[] volumeMounts;

		/**
		 * @param name name
		 * @param image image
		 * @param args args
		 * @param env env
		 * @param ports ports
		 * @param volumeMounts volumeMounts
		 */
		public Container(String name, String image, String[] args, Env[] env, Port[] ports, VolumeMount[] volumeMounts) {
			super();
			this.name = name;
			this.image = image;
			this.args = args;
			this.env = env;
			this.ports = ports;
			this.volumeMounts = volumeMounts;
		}
		
		/**
		 * @param name name
		 * @param image image
		 * @param env env
		 * @param ports ports
		 * @param volumeMounts volumeMounts
		 */
		public Container(String name, String image, Env[] env, Port[] ports, VolumeMount[] volumeMounts) {
			super();
			this.name = name;
			this.image = image;
			this.env = env;
			this.ports = ports;
			this.volumeMounts = volumeMounts;
		}

		/**
		 * @return env
		 */
		public Env[] getEnv() {
			return env;
		}

		/**
		 * @param env env
		 */
		public void setEnv(Env[] env) {
			this.env = env;
		}

		/**
		 * @return ports
		 */
		public Port[] getPorts() {
			return ports;
		}

		/**
		 * @param ports ports
		 */
		public void setPorts(Port[] ports) {
			this.ports = ports;
		}

		/**
		 * @return volumeMounts
		 */
		public VolumeMount[] getVolumeMounts() {
			return volumeMounts;
		}

		/**
		 * @param volumeMounts volumeMounts
		 */
		public void setVolumeMounts(VolumeMount[] volumeMounts) {
			this.volumeMounts = volumeMounts;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return image
		 */
		public String getImage() {
			return image;
		}

		/**
		 * @param image image
		 */
		public void setImage(String image) {
			this.image = image;
		}

		/**
		 * @return imagePullPolicy
		 */
		public String getImagePullPolicy() {
			return imagePullPolicy;
		}

		/**
		 * @param imagePullPolicy imagePullPolicy
		 */
		public void setImagePullPolicy(String imagePullPolicy) {
			this.imagePullPolicy = imagePullPolicy;
		}

		/**
		 * @return args
		 */
		public String[] getArgs() {
			return args;
		}

		/**
		 * @param args args
		 */
		public void setArgs(String[] args) {
			this.args = args;
		}
		
	}
	
	public static class Env {
		
		/**
		 * name
		 */
		String name;
		
		/**
		 * valueFrom
		 */
		ValueFrom valueFrom;
		
		/**
		 * @param name name
		 * @param valueFrom valueFrom
		 */
		public Env(String name, ValueFrom valueFrom) {
			super();
			this.name = name;
			this.valueFrom = valueFrom;
		}


		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}


		/**
		 * @param name name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return valueFrom
		 */
		public ValueFrom getValueFrom() {
			return valueFrom;
		}

		/**
		 * @param valueFrom valueFrom
		 */
		public void setValueFrom(ValueFrom valueFrom) {
			this.valueFrom = valueFrom;
		}

		/**
		 * @author henry
		 *
		 */
		public static class ValueFrom {
			
			/**
			 * secretKeyRef
			 */
			SecretKeyRef secretKeyRef;
			
			/**
			 * @param secretKeyRef secretKeyRef
			 */
			public ValueFrom(SecretKeyRef secretKeyRef) {
				super();
				this.secretKeyRef = secretKeyRef;
			}

			/**
			 * @return secretKeyRef
			 */
			public SecretKeyRef getSecretKeyRef() {
				return secretKeyRef;
			}

			/**
			 * @param secretKeyRef secretKeyRef
			 */
			public void setSecretKeyRef(SecretKeyRef secretKeyRef) {
				this.secretKeyRef = secretKeyRef;
			}

			/**
			 * @author henry
			 *
			 */
			public static class SecretKeyRef {
				
				/**
				 * name 
				 */
				String name;
				
				/**
				 * key
				 */
				String key;

				/**
				 * @param name name
				 * @param key  key
				 */
				public SecretKeyRef(String name, String key) {
					super();
					this.name = name;
					this.key = key;
				}
				

				/**
				 * @return name
				 */
				public String getName() {
					return name;
				}

				/**
				 * @param name name
				 */
				public void setName(String name) {
					this.name = name;
				}

				/**
				 * @return key
				 */
				public String getKey() {
					return key;
				}

				/**
				 * @param key key
				 */
				public void setKey(String key) {
					this.key = key;
				}

			}
		}
	}
	
	/**
	 * @author henry
	 *
	 */
	public static class Port {
		
		/**
		 * containerPort
		 */
		int containerPort;

		/**
		 * @param containerPort containerPort
		 */
		public Port(int containerPort) {
			super();
			this.containerPort = containerPort;
		}

		/**
		 * @return containerPort
		 */
		public int getContainerPort() {
			return containerPort;
		}

		/**
		 * @param containerPort containerPort
		 */
		public void setContainerPort(int containerPort) {
			this.containerPort = containerPort;
		}
		
	}
	
	/**
	 * @author henry
	 *
	 */
	public static class VolumeMount {
		/**
		 * name
		 */
		String name;
		
		/**
		 * mountPath
		 */
		String mountPath;

		/**
		 * @param name name
		 * @param mountPath mountPath
		 */
		public VolumeMount(String name, String mountPath) {
			super();
			this.name = name;
			this.mountPath = mountPath;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return mountPath
		 */
		public String getMountPath() {
			return mountPath;
		}

		/**
		 * @param mountPath mountPath
		 */
		public void setMountPath(String mountPath) {
			this.mountPath = mountPath;
		}
		
	}
}
