/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.yamls;


import io.github.kubesys.client.writers.ConfigMapWriter;
import io.github.kubesys.client.writers.DaemonSetWriter;
import io.github.kubesys.client.writers.PVCWriter;
import io.github.kubesys.client.writers.PVWriter;
import io.github.kubesys.client.writers.ServiceWriter;
import io.github.kubesys.client.writers.WorkloadWriter.Container;
import io.github.kubesys.client.writers.WorkloadWriter.Port;
import io.github.kubesys.client.writers.WorkloadWriter.VolumeMount;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/02
 * @version 1.0.3
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class KubeLoggerTest {
	
	static final String NAME = "kube-logger";
	
	static final String LOGGER = "logger";
	
	static final String LOGGER_IMAGE = "grafana/loki:2.8.3";
	
	static final String CONFIG_YML = "loki.yml";
	
	static final String YML_PATH = "configs/loki.yml";
	
	public static void main(String[] args) throws Exception {
		
		ConfigMapWriter cm = new ConfigMapWriter(NAME, StackCommon.NAMESPACE);
		cm.withYamlData(CONFIG_YML, StackCommon.read(YML_PATH)).stream(System.out);
		
		PVWriter pv = new PVWriter(NAME);
		pv.withCapacity("20").withPath(StackCommon.PATH + LOGGER).withPVC(NAME, StackCommon.NAMESPACE).stream(System.out);
		
		PVCWriter pvc = new PVCWriter(NAME, StackCommon.NAMESPACE);
		pvc.withCapacity("20").stream(System.out);
		
		DaemonSetWriter ds = new DaemonSetWriter(NAME, StackCommon.NAMESPACE);
		
		ds.withMasterEnbale()
				.withContainer(new Container(LOGGER, LOGGER_IMAGE, 
						new String[] {
								"-config.file=/etc/loki/loki.yml",
						},
						null, 
						new Port[] {
								new Port(9093)
						}, 
						new VolumeMount[] {
								new VolumeMount(StackCommon.VOLUME_CONFIG, "/etc/loki"),
								new VolumeMount(StackCommon.VOLUME_DATA, "/data")
						}))
				.withConfigMapVolume(StackCommon.VOLUME_CONFIG, NAME, CONFIG_YML, CONFIG_YML)
				.withPVCVolume(StackCommon.VOLUME_DATA, NAME)
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(3100, 30300, "logger")
		.stream(System.out);
	}
}
