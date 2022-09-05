/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.utils;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;

/**
 * 
 * it is used for supporting ssl connections
 * 
 * @author wuheng@iscas.ac.cn
 * @since 2.0.5
 **/
public class SSLUtil {

	public static final Logger m_logger = Logger.getLogger(SSLUtil.class.getName());

	private SSLUtil() {
		super();
	}

	/**
	 * @return SocketFactory
	 */
	public static org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory 
				createSocketFactory(KeyManager[] km, TrustManager[] tm) {
		return new org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory(
				createX509SocketFactory(km, tm), new NoopHostnameVerifier());
	}

	/**
	 * @return SocketFactory
	 */
	public static SSLSocketFactory createX509SocketFactory(KeyManager[] km, TrustManager[] tm) {
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(km, tm == null ? createDefaultTrustManager(): tm, new SecureRandom());
			return sc.getSocketFactory();
		} catch (Exception ex) {
			m_logger.severe("unable to create X509 SocketFactory, " + ex);
			return null;
		}
	}

	/**
	 * @return TrustManager[]
	 */
	public static TrustManager[] createDefaultTrustManager() {
		return new TrustManager[] { new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				m_logger.info("client is trusted.");
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				m_logger.info("server is trusted.");
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}

		} };
	}

	/**
	 * @return HostnameVerifier
	 */
	public static HostnameVerifier createDefaultHostnameVerifier() {
		return new HostnameVerifier() {

			public String toString() {
				return super.toString();
			}

			public boolean verify(String hostname, SSLSession session) {
				m_logger.info("hostname is trusted.");
				return (hostname != null);
			}

		};
	}
}
