/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.utils;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;

/**
 * 
 * it is used for supporting ssl connections
 * 
 * @author wuheng@iscas.ac.cn
 * @since  2.0.5 
 **/
public class SSLUtil {

	public static final Logger m_logger = Logger.getLogger(SSLUtil.class.getName());
	
	/**
	 * @return                                 SocketFactory
	 */
	public static org.apache.http.conn.ssl.SSLConnectionSocketFactory createSocketFactory() {
		return new org.apache.http.conn.ssl.SSLConnectionSocketFactory(
				createX509SocketFactory(), new NoopHostnameVerifier());
	}
	
	/**
	 * @return                                  SocketFactory
	 */
	public static SSLSocketFactory createX509SocketFactory() {
		TrustManager[] managers = new TrustManager[] {
								new X509TrustManager() {

									@Override
									public void checkClientTrusted(X509Certificate[] chain, String authType)
											throws CertificateException {
										m_logger.info("check client trusted.");
									}

									@Override
									public void checkServerTrusted(X509Certificate[] chain, String authType)
											throws CertificateException {
										m_logger.info("check server trusted.");
									}

									@Override
									public X509Certificate[] getAcceptedIssuers() {
										return new X509Certificate[] {};
									}
									
								}};
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, managers, new SecureRandom());
		return sc.getSocketFactory();
		} catch (Exception ex) {
			return null;
		}
	}
	
	
	/**
	 * @return                                  HostnameVerifier
	 */
	public static HostnameVerifier createHostnameVerifier() {
		return new HostnameVerifier() {
			
			public String toString() {
				return super.toString();
			}

			public boolean verify(String hostname, SSLSession session) {
				return (hostname != null);
			}

		};
	}
}
