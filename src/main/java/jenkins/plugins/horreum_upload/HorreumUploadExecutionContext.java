package jenkins.plugins.horreum_upload;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import hudson.AbortException;
import hudson.CloseProofOutputStream;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import jenkins.plugins.horreum_upload.auth.Authenticator;
import jenkins.plugins.horreum_upload.util.HttpClientUtil;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;
import jenkins.plugins.horreum_upload.util.RequestAction;
import jenkins.security.MasterToSlaveCallable;

/**
 * @author Janario Oliveira
 */
public class HorreumUploadExecutionContext extends MasterToSlaveCallable<ResponseContentSupplier, RuntimeException> {

	private static final long serialVersionUID = -2066857816168989599L;
	private final String url;
	private final boolean ignoreSslErrors;

	private final List<HttpRequestNameValuePair> headers;
	private final List<HttpRequestNameValuePair> params;

	private final FilePath uploadFile;

	private final int timeout;
	private final boolean consoleLogResponseBody;
	private final ResponseHandle responseHandle;

	private final Authenticator authenticator;

	private final OutputStream remoteLogger;
	private transient PrintStream localLogger;
	private boolean abortOnFailure;

	//creds from cred store
	private String client_secret;
	private String username;
	private String password;

	static HorreumUploadExecutionContext from(HorreumUploadConfig config,
											  EnvVars envVars, TaskListener taskListener, Supplier<FilePath> filePathSupplier) {
		String url = envVars != null ?  envVars.expand(HorreumUploadGlobalConfig.get().getBaseUrl()) : HorreumUploadGlobalConfig.get().getBaseUrl(); //http.resolveUrl(envVars, build, taskListener);
		List<HttpRequestNameValuePair> headers = config.resolveHeaders(envVars);
		List<HttpRequestNameValuePair> params = config.resolveParams(); //Need to define params in freestyle project
		FilePath uploadFile = filePathSupplier.get();

		return new HorreumUploadExecutionContext(
				url, config.getIgnoreSslErrors(),
				config.getAbortOnFailure(),
				headers, params, config.getTimeout(),
				uploadFile,
				config.getAuthentication(),
				config.getConsoleLogResponseBody(),
				ResponseHandle.NONE,
				taskListener.getLogger());
	}

	private HorreumUploadExecutionContext(
			String url, boolean ignoreSslErrors, boolean abortOnFailure,
			List<HttpRequestNameValuePair> headers,
			List<HttpRequestNameValuePair> params,
			Integer timeout,
			FilePath uploadFile,
			String authentication,
			Boolean consoleLogResponseBody,
			ResponseHandle responseHandle,
			PrintStream logger
	) {
		this.url = url;
		this.ignoreSslErrors = ignoreSslErrors;
		this.abortOnFailure = abortOnFailure;

		this.headers = headers;
		this.params = params;
		this.timeout = timeout != null ? timeout : -1;
		this.authenticator = HorreumUploadGlobalConfig.get().getAuthentication(authentication);;
		this.uploadFile = uploadFile;
		this.consoleLogResponseBody = Boolean.TRUE.equals(consoleLogResponseBody);
		this.responseHandle = this.consoleLogResponseBody ?
				ResponseHandle.STRING : responseHandle;

		this.localLogger = logger;
		this.remoteLogger = new RemoteOutputStream(new CloseProofOutputStream(logger));
	}

	@Override
	public ResponseContentSupplier call() throws RuntimeException {
		logger().println("URL: " + url);
		for (HttpRequestNameValuePair header : headers) {
			logger().print(header.getName() + ": ");
			logger().println(header.getMaskValue() ? "*****" : header.getValue());
		}

		try {
			return authAndRequest();
		} catch (IOException | InterruptedException |
				KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
			throw new IllegalStateException(e);
		}
	}

	private PrintStream logger() {
		if (localLogger == null) {
			try {
				localLogger = new PrintStream(remoteLogger, true, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
		return localLogger;
	}

	private ResponseContentSupplier authAndRequest()
			throws IOException, InterruptedException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		//only leave open if no error happen
		ResponseHandle responseHandle = ResponseHandle.NONE;
		CloseableHttpClient httpclient = null;
		try {
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			configureTimeoutAndSsl(clientBuilder);

			HttpClientUtil clientUtil = new HttpClientUtil();

			//TODO: tidy up this URL builder
			RequestAction requestAction = new RequestAction(
					new URL(clientUtil.getUrlWithParams(new RequestAction(new URL(url), params, headers)))
					, params, headers
			);

			HttpRequestBase httpRequestBase = clientUtil.createRequestBase(requestAction);

			if (uploadFile != null) {
				ContentType contentType = ContentType.APPLICATION_JSON;
				for (HttpRequestNameValuePair header : headers) {
					if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(header.getName())) {
						contentType = ContentType.parse(header.getValue());
						break;
					}
				}

				HttpEntity entity;
				entity = new FileEntity(new File(uploadFile.getRemote()), contentType);

				((HttpEntityEnclosingRequestBase) httpRequestBase).setEntity(entity);
				httpRequestBase.setHeader(entity.getContentType());
				httpRequestBase.setHeader(entity.getContentEncoding());
			}

			HttpContext context = new BasicHttpContext();
			httpclient = auth(clientBuilder, httpRequestBase, context);

			//TODO: can we set this via the httpClient api without having to set explicitly?
			httpRequestBase.setHeader("Authorization", "Bearer " + context.getAttribute("http.auth.access_token"));

			ResponseContentSupplier response = executeRequest(httpclient, clientUtil, httpRequestBase, context);
			processResponse(response);

			responseHandle = this.responseHandle;
			if (responseHandle == ResponseHandle.LEAVE_OPEN) {
				response.setHttpClient(httpclient);
			}
			return response;
		} finally {
			if (responseHandle != ResponseHandle.LEAVE_OPEN) {
				if (httpclient != null) {
					httpclient.close();
				}
			}
		}
	}

	private void configureTimeoutAndSsl(HttpClientBuilder clientBuilder) throws NoSuchAlgorithmException, KeyManagementException {
		//timeout
		if (timeout > 0) {
			int t = timeout * 1000;
			RequestConfig config = RequestConfig.custom()
					.setSocketTimeout(t)
					.setConnectTimeout(t)
					.setConnectionRequestTimeout(t)
					.build();
			clientBuilder.setDefaultRequestConfig(config);
		}
		//Ignore SSL errors
		if (ignoreSslErrors) {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[]{new NoopTrustManager()}, new java.security.SecureRandom());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
			clientBuilder.setSSLSocketFactory(sslsf);
		}
	}

	private CloseableHttpClient auth(
			HttpClientBuilder clientBuilder, HttpRequestBase httpRequestBase,
			HttpContext context) throws IOException, InterruptedException {

		if (authenticator == null) {
			return clientBuilder.build();
		}

		logger().println("Using authentication: " + authenticator.getKeyName());
		return authenticator.authenticate(clientBuilder, context, httpRequestBase, logger());
	}

	private ResponseContentSupplier executeRequest(
			CloseableHttpClient httpclient, HttpClientUtil clientUtil, HttpRequestBase httpRequestBase,
			HttpContext context) throws IOException, InterruptedException {
		ResponseContentSupplier responseContentSupplier;
		try {
			final HttpResponse response = clientUtil.execute(httpclient, context, httpRequestBase, logger());
			// The HttpEntity is consumed by the ResponseContentSupplier
			responseContentSupplier = new ResponseContentSupplier(responseHandle, response);
		} catch (UnknownHostException uhe) {
			logger().println("Treating UnknownHostException(" + uhe.getMessage() + ") as 404 Not Found");
			responseContentSupplier = new ResponseContentSupplier("UnknownHostException as 404 Not Found", 404);
		} catch (SocketTimeoutException | ConnectException ce) {
			logger().println("Treating " + ce.getClass() + "(" + ce.getMessage() + ") as 408 Request Timeout");
			responseContentSupplier = new ResponseContentSupplier(ce.getClass() + "(" + ce.getMessage() + ") as 408 Request Timeout", 408);
		}

		return responseContentSupplier;
	}


	private void processResponse(ResponseContentSupplier response) throws IOException, InterruptedException  {
		//logs
		if (consoleLogResponseBody) {
			logger().println("Response: \n" + response.getContent());
		}
		if ((response.getStatus()) >= 400 ) {
			logger().println("Request Failed: \n" + response.getContent());
		}

		responseCodeIsValid(response);

	}

	private void responseCodeIsValid(ResponseContentSupplier response) throws AbortException {
		//TODO: enable configuration
		if ((response.getStatus()) < 400 && abortOnFailure) {
			return;
		}
		throw new AbortException("Fail: the returned code " + response.getStatus() + " is not in the accepted range" );
	}

	public void initialiseContext(){
		authenticator.resolveCredentials();
	}

	private static class NoopTrustManager extends X509ExtendedTrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {
		}
	}
}
