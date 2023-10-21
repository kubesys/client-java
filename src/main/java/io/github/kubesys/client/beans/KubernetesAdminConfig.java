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
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.kubesys.client.exceptions.KubernetesBadRequestException;
import io.github.kubesys.client.exceptions.KubernetesConflictResourceException;
import io.github.kubesys.client.exceptions.KubernetesConnectionException;
import io.github.kubesys.client.exceptions.KubernetesForbiddenAccessException;
import io.github.kubesys.client.exceptions.KubernetesInternalServerErrorException;
import io.github.kubesys.client.exceptions.KubernetesResourceNotFoundException;
import io.github.kubesys.client.exceptions.KubernetesUnauthorizedTokenException;
import io.github.kubesys.client.exceptions.KubernetesUnknownException;
import io.github.kubesys.client.utils.SSLUtil;

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

	// https://www.oreilly.com/library/view/managing-kubernetes/9781492033905/ch04.html
	static Map<Integer, String> statusDesc = new HashMap<>();

	static {
		statusDesc.put(400, "Bad Request. The server could not parse or understand the request.");
		statusDesc.put(401, "Unauthorized. A request was received without a known authentication scheme.");
		statusDesc.put(403,
				"Bad Request. Forbidden. The request was received and understood, but access is forbidden.");
		statusDesc.put(409,
				"Conflict. The request was received, but it was a request to update an older version of the object.");
		statusDesc.put(422,
				"Unprocessable entity. The request was parsed correctly but failed some sort of validation.");
	}

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
	 * client
	 */
	protected final CloseableHttpClient httpClient;

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
		this.httpClient = createDefaultHttpClient();
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
		this.httpClient = createDefaultHttpClient();
	}

	/**
	 * @param masterUrl      masterUrl
	 * @param caCertData     caCertData
	 * @param clientCertData clientCertData
	 * @param clientKeyData  clientKeyData
	 */
	public KubernetesAdminConfig(String masterUrl, String caCertData, String clientCertData, String clientKeyData)
			throws Exception {
		this.masterUrl = masterUrl;
		this.caCertData = caCertData;
		this.clientCertData = clientCertData;
		this.clientKeyData = clientKeyData;
		this.httpClient = createDefaultHttpClient();
	}

	/**
	 * @param json json
	 * @throws Exception
	 */
	public KubernetesAdminConfig(JsonNode json) throws Exception {
		JsonNode cluster = json.get("clusters").get(0).get("cluster");
		this.masterUrl = cluster.get("server").asText();
		this.caCertData = cluster.get("certificate-authority-data").asText();
		JsonNode user = json.get("users").get(0).get("user");
		this.clientCertData = user.get("client-certificate-data").asText();
		this.clientKeyData = user.get("client-key-data").asText();
		this.httpClient = createDefaultHttpClient();
	}

	/**
	 * @return httpClient
	 * @throws Exception
	 */
	protected CloseableHttpClient createDefaultHttpClient()
			throws Exception {

		@SuppressWarnings("deprecation")
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Timeout.DISABLED)
				.setConnectionKeepAlive(Timeout.DISABLED).setConnectionRequestTimeout(Timeout.DISABLED)
				.setResponseTimeout(Timeout.DISABLED).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setConnectionManager(
						new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
								.register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory())
								.register(URIScheme.HTTPS.id,
										SSLUtil.createSocketFactory(keyManagers(), trustManagers()))
								.build()))
				.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy()).build();
	}

	public String getCaCertData() {
		return caCertData;
	}

	public void setCaCertData(String caCertData) {
		this.caCertData = caCertData;
	}

	public String getClientCertData() {
		return clientCertData;
	}

	public void setClientCertData(String clientCertData) {
		this.clientCertData = clientCertData;
	}

	public String getClientKeyData() {
		return clientKeyData;
	}

	public void setClientKeyData(String clientKeyData) {
		this.clientKeyData = clientKeyData;
	}

	public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public static Map<Integer, String> getStatusDesc() {
		return statusDesc;
	}

	public static void setStatusDesc(Map<Integer, String> statusDesc) {
		KubernetesAdminConfig.statusDesc = statusDesc;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**********************************************************************
	 * 
	 * I do not known why, just copy from fabric8
	 * 
	 **********************************************************************/

	public KeyManager[] keyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException,
			CertificateException, InvalidKeySpecException, IOException {
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

	public RSAPrivateCrtKeySpec decodePKCS1(byte[] keyBytes) throws IOException {
		DerParser parser = new DerParser(keyBytes);
		Asn1Object sequence = parser.read();
		sequence.validateSequence();
		parser = new DerParser(sequence.getValue());
		parser.read();

		return new RSAPrivateCrtKeySpec(next(parser), next(parser), next(parser), next(parser), next(parser),
				next(parser), next(parser), next(parser));
	}

	protected BigInteger next(DerParser parser) throws IOException {
		return parser.read().getInteger();
	}

	protected TrustManager[] trustManagers()
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		if (this.caCertData == null) {
			return null;
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = createTrustStore(createInputStreamFromBase64EncodedString(this.caCertData));
		tmf.init(trustStore);
		return tmf.getTrustManagers();
	}

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

	protected void loadDefaultTrustStoreFile(KeyStore keyStore, char[] trustStorePassphrase)
			throws CertificateException, NoSuchAlgorithmException, IOException {

		File trustStoreFile = getDefaultTrustStoreFile();

		if (!loadDefaultStoreFile(keyStore, trustStoreFile, trustStorePassphrase)) {
			keyStore.load(null);
		}
	}

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
	 * 200 OK: 请求成功，服务器成功处理了请求并返回所请求的数据。 201 Created: 请求成功，服务器创建了新资源。 204 No
	 * Content: 请求成功，服务器处理成功，但没有返回数据。 400 Bad Request: 请求无效，服务器无法理解请求。 401
	 * Unauthorized: 未授权，需要进行身份验证或令牌无效。 403 Forbidden: 请求被拒绝，客户端没有访问资源的权限。 404 Not
	 * Found: 请求的资源不存在。 409 Conflict: 请求冲突，通常用于表示资源的当前状态与请求的条件不匹配。 500 Internal
	 * Server Error: 服务器内部错误，表示服务器在处理请求时遇到了问题。
	 * 
	 * @param response response
	 * @return json json
	 */
	protected synchronized JsonNode parseResponse(CloseableHttpResponse response) {

		switch (response.getCode()) {
		case 200:
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				return objectMapper.readTree(response.getEntity().getContent());
			} catch (Exception e) {
				throw new KubernetesUnknownException(e.toString());
			}
		case 201:
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
				return objectMapper.readTree(response.getEntity().getContent());
			} catch (Exception e) {
				throw new KubernetesUnknownException(e.toString());
			}
		case 400:
			throw new KubernetesBadRequestException(response.toString());
		case 401:
			throw new KubernetesUnauthorizedTokenException(response.toString());
		case 403:
			throw new KubernetesForbiddenAccessException(response.toString());
		case 404:
			throw new KubernetesResourceNotFoundException(response.toString());
		case 409:
			throw new KubernetesConflictResourceException(response.toString());
		case 500:
			throw new KubernetesInternalServerErrorException(response.toString());
		default:
			throw new KubernetesUnknownException(response.toString());
		}

	}

	/**
	 * @param req req
	 * @return json json
	 * @throws Exception exception
	 */
	@SuppressWarnings("deprecation")
	public synchronized JsonNode getResponse(HttpUriRequestBase req) throws Exception {
		return parseResponse(httpClient.execute(req));
	}

	/**
	 * 
	 */
	protected void close() {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				m_logger.warning(e.toString());
			}
		}
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

	/**
	 * @return httpClient
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	static class DerParser {

		private InputStream in;

		DerParser(byte[] bytes) throws IOException {
			this.in = new ByteArrayInputStream(bytes);
		}

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

	static class Asn1Object {

		private final int type;
		private final byte[] value;
		private final int tag;

		public Asn1Object(int tag, byte[] value) {
			this.tag = tag;
			this.type = tag & 0x1F;
			this.value = value;
		}

		public byte[] getValue() {
			return value;
		}

		BigInteger getInteger() throws IOException {
			if (type != 0x02) {
				throw new IOException("Invalid DER: object is not integer"); //$NON-NLS-1$
			}
			return new BigInteger(value);
		}

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
