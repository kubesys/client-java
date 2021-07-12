/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys.utils;

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

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import sun.security.x509.X509CertImpl;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * This is a copy of io.fabric8.kubernetes.client.utils.URLUtils in project kubernetes-client
 * 
 **/
@SuppressWarnings({ "deprecation"})
public class SSLUtil {

	public static final Logger m_logger = Logger.getLogger(SSLUtil.class.getName());
	
	/**
	 * @return                                 SocketFactory
	 */
	public static org.apache.http.conn.ssl.SSLSocketFactory createSocketFactory() {
		return new org.apache.http.conn.ssl.SSLSocketFactory(
				createX509SocketFactory(), new AllowAllHostnameVerifier());
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
										X509CertImpl xc =  new X509CertImpl();
										return new X509Certificate[] {xc};
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
			
			@Override
			public String toString() {
				return super.toString();
			}

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return (hostname != null);
			}

		};
	}
}
