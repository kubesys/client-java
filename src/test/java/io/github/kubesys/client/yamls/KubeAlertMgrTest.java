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
public class KubeAlertMgrTest {
	
	static final String NAME = "kube-alertmgr";
	
	static final String ALTERMGR = "alertmanager";
	
	static final String ALTERMGR_IMAGE = "prom/alertmanager:v0.25.0";
	
	static final String CONFIG_YML = "alertmanager.yml";
	
	static final String YML_PATH = "configs/alertmanager.yml";
	
	public static void main(String[] args) throws Exception {
		
		ConfigMapWriter cm = new ConfigMapWriter(NAME, StackCommon.NAMESPACE);
		cm.withYamlData(CONFIG_YML, StackCommon.read(YML_PATH)).stream(System.out);
		
		PVWriter pv = new PVWriter(NAME);
		pv.withCapacity("20").withPath(StackCommon.PATH + ALTERMGR).withPVC(NAME, StackCommon.NAMESPACE).stream(System.out);
		
		PVCWriter pvc = new PVCWriter(NAME, StackCommon.NAMESPACE);
		pvc.withCapacity("20").stream(System.out);
		
		DaemonSetWriter ds = new DaemonSetWriter(NAME, StackCommon.NAMESPACE);
		
		ds.withMasterEnbale()
				.withContainer(new Container(ALTERMGR, ALTERMGR_IMAGE, 
						new String[] {
								"--config.file=/etc/alertmanager/alertmanager.yml",
								"--storage.path=/alertmanager"
						},
						null, 
						new Port[] {
								new Port(9093)
						}, 
						new VolumeMount[] {
								new VolumeMount(StackCommon.VOLUME_CONFIG, "/etc/alertmanager"),
								new VolumeMount(StackCommon.VOLUME_DATA, "/alertmanager")
						}))
				.withConfigMapVolume(StackCommon.VOLUME_CONFIG, NAME, CONFIG_YML, CONFIG_YML)
				.withPVCVolume(StackCommon.VOLUME_DATA, NAME)
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(9093, 30303, "alertmanager")
		.stream(System.out);
	}
}
