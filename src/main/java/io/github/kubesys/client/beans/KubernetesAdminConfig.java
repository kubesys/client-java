/*
  Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.beans;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


import com.fasterxml.jackson.databind.JsonNode;

/**
 * Kubernetes的客户端，根据https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/的
 * URL规则生产URL
 * 
 * 对于JSON参数，可参见https://kubernetes.io/docs/reference/kubernetes-api/
 * 
 * @author wuheng@iscas.ac.cn
 * @since 2.0.0
 * 
 */
public class KubernetesAdminConfig {

	/**
	 * m_logger
	 */
	public static final Logger m_logger = Logger.getLogger(KubernetesAdminConfig.class.getName());

	

	/**
	 * master IP
	 */
	protected String masterUrl;

	/**
	 * token
	 */
	protected String token;

	/**
	 * username
	 */
	protected String username;

	/**
	 * password
	 */
	protected String password;

	/**
	 * caCertData
	 */
	protected String caCertData;

	/**
	 * clientCertData
	 */
	protected String clientCertData;

	/**
	 * clientKeyData
	 */
	protected String clientKeyData;

	/**
	 * @param requester requester
	 * @throws Exception exception
	 */
	public KubernetesAdminConfig(KubernetesAdminConfig requester) throws Exception {
		this(requester.getMasterUrl(), requester.getToken());
	}

	/**
	 * @param masterUrl masterUrl
	 * @param token     token
	 * @throws Exception exception
	 */
	public KubernetesAdminConfig(String masterUrl, String token) throws Exception {
		super();
		this.masterUrl = masterUrl;
		this.token = token;
	}

	/**
	 * @param masterUrl masterUrl
	 * @param username  username
	 * @param password  password
	 * @throws Exception exception
	 */
	public KubernetesAdminConfig(String masterUrl, String username, String password) throws Exception {
		super();
		this.masterUrl = masterUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * @param masterUrl      masterUrl
	 * @param caCertData     caCertData
	 * @param clientCertData clientCertData
	 * @param clientKeyData  clientKeyData
	 */
	public KubernetesAdminConfig(String masterUrl, String caCertData, String clientCertData, String clientKeyData) {
		this.masterUrl = masterUrl;
		this.caCertData = caCertData;
		this.clientCertData = clientCertData;
		this.clientKeyData = clientKeyData;
	}

	/**
	 * @param json json
	 * @throws Exception Exception
	 */
	public KubernetesAdminConfig(JsonNode json) throws Exception {
		JsonNode cluster = json.get("clusters").get(0).get("cluster");
		this.masterUrl = cluster.get("server").asText();
		this.caCertData = cluster.get("certificate-authority-data").asText();
		JsonNode user = json.get("users").get(0).get("user");
		this.clientCertData = user.get("client-certificate-data").asText();
		this.clientKeyData = user.get("client-key-data").asText();
	}


	/**
	 * @return CaCertData
	 */
	public String getCaCertData() {
		return caCertData;
	}

	/**
	 * @param caCertData caCertData
	 */
	public void setCaCertData(String caCertData) {
		this.caCertData = caCertData;
	}

	/**
	 * @return ClientCertData
	 */
	public String getClientCertData() {
		return clientCertData;
	}

	/**
	 * @param clientCertData clientCertData
	 */
	public void setClientCertData(String clientCertData) {
		this.clientCertData = clientCertData;
	}

	/**
	 * @return getClientKeyData
	 */
	public String getClientKeyData() {
		return clientKeyData;
	}

	/**
	 * @param clientKeyData clientKeyData
	 */
	public void setClientKeyData(String clientKeyData) {
		this.clientKeyData = clientKeyData;
	}

	/**
	 * @param masterUrl url
	 */
	public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	/**
	 * @param token token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**********************************************************************
	 * 
	 * I do not known why, just copy from fabric8
	 * 
	 **********************************************************************/

	/**
	 * @return keyManagers
	 * @throws Exception Exception
	 */
	public KeyManager[] keyManagers() throws Exception {
		if (this.clientCertData == null || this.clientKeyData == null) {
			return null;
		}
		KeyManager[] keyManagers = null;
		char[] passphrase = "changeit".toCharArray();
		KeyStore keyStore = createKeyStore(createInputStreamFromBase64EncodedString(this.clientCertData),
				createInputStreamFromBase64EncodedString(this.clientKeyData), passphrase);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, passphrase);
		keyManagers = kmf.getKeyManagers();
		return keyManagers;
	}

	/**
	 * @param certInputStream certInputStream
	 * @param keyInputStream keyInputStream
	 * @param clientKeyPassphrase clientKeyPassphrase
	 * @return KeyStore
	 * @throws IOException IOException
	 * @throws CertificateException CertificateException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 * @throws InvalidKeySpecException InvalidKeySpecException
	 * @throws KeyStoreException KeyStoreException
	 */
	public KeyStore createKeyStore(InputStream certInputStream, InputStream keyInputStream, char[] clientKeyPassphrase)
			throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException,
			KeyStoreException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X509");
		Collection<? extends Certificate> certificates = certFactory.generateCertificates(certInputStream);
		PrivateKey privateKey = handleOtherKeys(keyInputStream, "RSA");

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		keyStore.load(null);
		String alias = certificates.stream().map(cert -> ((X509Certificate) cert).getIssuerX500Principal().getName())
				.collect(Collectors.joining("_"));
		keyStore.setKeyEntry(alias, privateKey, clientKeyPassphrase, certificates.toArray(new Certificate[0]));

		return keyStore;
	}

	/**
	 * @param keyInputStream is
	 * @return bytes
	 * @throws IOException IOException
	 */
	protected byte[] decodePem(InputStream keyInputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(keyInputStream));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("-----BEGIN ")) {
					return readBytes(reader, line.trim().replace("BEGIN", "END"));
				}
			}
			throw new IOException("PEM is invalid: no begin marker");
		} finally {
			reader.close();
		}
	}

	/**
	 * @param reader reader
	 * @param endMarker endMarker
	 * @return bytes
	 * @throws IOException IOException
	 */
	protected byte[] readBytes(BufferedReader reader, String endMarker) throws IOException {
		String line;
		StringBuffer buf = new StringBuffer();

		while ((line = reader.readLine()) != null) {
			if (line.indexOf(endMarker) != -1) {
				return Base64.getDecoder().decode(buf.toString());
			}
			buf.append(line.trim());
		}
		throw new IOException("PEM is invalid : No end marker");
	}

	/** 
	 * @param keyInputStream is 
	 * @param clientKeyAlgo clientKeyAlgo
	 * @return PrivateKey
	 * @throws IOException IOException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 * @throws InvalidKeySpecException InvalidKeySpecException
	 */
	protected PrivateKey handleOtherKeys(InputStream keyInputStream, String clientKeyAlgo)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = decodePem(keyInputStream);
		KeyFactory keyFactory = KeyFactory.getInstance(clientKeyAlgo);
		try {
			// First let's try PKCS8
			return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
		} catch (InvalidKeySpecException e) {
			// Otherwise try PKCS8
			RSAPrivateCrtKeySpec keySpec = decodePKCS1(keyBytes);
			return keyFactory.generatePrivate(keySpec);
		}
	}

	/**
	 * @param keyBytes keyBytes
	 * @return RSAPrivateCrtKeySpec
	 * @throws IOException IOException
	 */
	public RSAPrivateCrtKeySpec decodePKCS1(byte[] keyBytes) throws IOException {
		DerParser parser = new DerParser(keyBytes);
		Asn1Object sequence = parser.read();
		sequence.validateSequence();
		parser = new DerParser(sequence.getValue());
		parser.read();

		return new RSAPrivateCrtKeySpec(next(parser), next(parser), next(parser), next(parser), next(parser),
				next(parser), next(parser), next(parser));
	}

	/**
	 * @param parser parse
	 * @return BigInteger
	 * @throws IOException IOException
	 */
	protected BigInteger next(DerParser parser) throws IOException {
		return parser.read().getInteger();
	}

	/**
	 * @return trustManagers
	 * @throws CertificateException CertificateException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 * @throws KeyStoreException KeyStoreException
	 * @throws IOException IOException
	 */
	public TrustManager[] trustManagers()
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		if (this.caCertData == null) {
			return null;
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = createTrustStore(createInputStreamFromBase64EncodedString(this.caCertData));
		tmf.init(trustStore);
		return tmf.getTrustManagers();
	}

	/**
	 * @param pemInputStream is
	 * @return KeyStore
	 * @throws IOException IOException
	 * @throws CertificateException IOException
	 * @throws KeyStoreException KeyStoreException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 */
	protected KeyStore createTrustStore(InputStream pemInputStream)
			throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		loadDefaultTrustStoreFile(trustStore, "changeit".toCharArray());
		while (pemInputStream.available() > 0) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(pemInputStream);
			String alias = cert.getSubjectX500Principal().getName() + "_" + cert.getSerialNumber().toString(16);
			trustStore.setCertificateEntry(alias, cert);
		}
		return trustStore;
	}

	/**
	 * @param keyStore keyStore
	 * @param trustStorePassphrase trustStorePassphrase
	 * @throws CertificateException CertificateException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 * @throws IOException IOException
	 */
	protected void loadDefaultTrustStoreFile(KeyStore keyStore, char[] trustStorePassphrase)
			throws CertificateException, NoSuchAlgorithmException, IOException {

		File trustStoreFile = getDefaultTrustStoreFile();

		if (!loadDefaultStoreFile(keyStore, trustStoreFile, trustStorePassphrase)) {
			keyStore.load(null);
		}
	}

	/**
	 * @param keyStore keyStore
	 * @param fileToLoad fileToLoad
	 * @param passphrase passphrase
	 * @return true or false
	 * @throws CertificateException CertificateException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 * @throws IOException IOException
	 */
	protected boolean loadDefaultStoreFile(KeyStore keyStore, File fileToLoad, char[] passphrase)
			throws CertificateException, NoSuchAlgorithmException, IOException {

		if (fileToLoad.exists() && fileToLoad.isFile() && fileToLoad.length() > 0) {
			try {
				try (FileInputStream fis = new FileInputStream(fileToLoad)) {
					keyStore.load(fis, passphrase);
				}
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * @return file
	 */
	protected File getDefaultTrustStoreFile() {
		String securityDirectory = System.getProperty("java.home") + File.separator + "lib" + File.separator
				+ "security" + File.separator;

		String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
		if (trustStorePath != null) {
			return new File(trustStorePath);
		}

		File jssecacertsFile = new File(securityDirectory + "jssecacerts");
		if (jssecacertsFile.exists() && jssecacertsFile.isFile()) {
			return jssecacertsFile;
		}

		return new File(securityDirectory + "cacerts");
	}

	/**
	 * @param data data
	 * @return  stream
	 */
	protected ByteArrayInputStream createInputStreamFromBase64EncodedString(String data) {
		byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(data);
		} catch (IllegalArgumentException illegalArgumentException) {
			bytes = data.getBytes();
		}

		return new ByteArrayInputStream(bytes);
	}


	/**
	 * @return masterUrl
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * @return token
	 */
	public String getToken() {
		return token;
	}


	static class DerParser {

		/**
		 * is
		 */
		private InputStream in;

		/**
		 * @param bytes bytes
		 * @throws IOException IOException
		 */
		DerParser(byte[] bytes) throws IOException {
			this.in = new ByteArrayInputStream(bytes);
		}

		/**
		 * @return  Asn1Object
		 * @throws IOException IOException
		 */
		Asn1Object read() throws IOException {
			int tag = in.read();

			if (tag == -1) {
				throw new IOException("Invalid DER: stream too short, missing tag");
			}

			int length = getLength();
			byte[] value = new byte[length];
			if (in.read(value) < length) {
				throw new IOException("Invalid DER: stream too short, missing value");
			}

			return new Asn1Object(tag, value);
		}

		/**
		 * @return len
		 * @throws IOException IOException
		 */
		private int getLength() throws IOException {
			int i = in.read();
			if (i == -1) {
				throw new IOException("Invalid DER: length missing");
			}

			if ((i & ~0x7F) == 0) {
				return i;
			}

			int num = i & 0x7F;
			if (i >= 0xFF || num > 4) {
				throw new IOException("Invalid DER: length field too big (" + i + ")");
			}

			byte[] bytes = new byte[num];
			if (in.read(bytes) < num) {
				throw new IOException("Invalid DER: length too short");
			}

			return new BigInteger(1, bytes).intValue();
		}
	}

	/**
	 * @author henry
	 *
	 */
	static class Asn1Object {

		/**
		 * type
		 */
		private final int type;
		
		/**
		 * values
		 */
		private final byte[] value;
		
		
		/**
		 * tag
		 */
		private final int tag;

		/**
		 * @param tag tag
		 * @param value value
		 */
		public Asn1Object(int tag, byte[] value) {
			this.tag = tag;
			this.type = tag & 0x1F;
			this.value = value;
		}

		/**
		 * @return bytes
		 */
		public byte[] getValue() {
			return value;
		}

		/**
		 * @return BigInteger
		 * @throws IOException IOException
		 */
		BigInteger getInteger() throws IOException {
			if (type != 0x02) {
				throw new IOException("Invalid DER: object is not integer"); //$NON-NLS-1$
			}
			return new BigInteger(value);
		}

		/**
		 * @throws IOException IOException
		 */
		void validateSequence() throws IOException {
			if (type != 0x10) {
				throw new IOException("Invalid DER: not a sequence");
			}
			if ((tag & 0x20) != 0x20) {
				throw new IOException("Invalid DER: can't parse primitive entity");
			}
		}
	}

}
