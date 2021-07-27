package jenkins.plugins.horreum_upload.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import com.google.common.base.Strings;

import jenkins.plugins.horreum_upload.HttpMode;

/**
 * @author Janario Oliveira
 */
public class HttpClientUtil {

    public HttpRequestBase createRequestBase(RequestAction requestAction) throws IOException {
        HttpRequestBase httpRequestBase = doCreateRequestBase(requestAction);
        for (HttpRequestNameValuePair header : requestAction.getHeaders()) {
            httpRequestBase.addHeader(header.getName(), header.getValue());
        }

        return httpRequestBase;
    }

    private HttpRequestBase doCreateRequestBase(RequestAction requestAction) throws IOException {

		//with entity
		final String uri = requestAction.getUrl().toString();
		HttpEntityEnclosingRequestBase http;
		http = new HttpPost(uri);

		http.setEntity(toUrlEncoded(requestAction.getParams()));
        return http;
    }

	public String getUrlWithParams(RequestAction requestAction) throws IOException {
		String url = requestAction.getUrl().toString();

		if (!requestAction.getParams().isEmpty()) {
			url = appendParamsToUrl(url, requestAction.getParams());
		}
		return url;
	}

	private static UrlEncodedFormEntity toUrlEncoded(List<HttpRequestNameValuePair> params) throws UnsupportedEncodingException {
		return new UrlEncodedFormEntity(params);
	}

	public static String appendParamsToUrl(String url, List<HttpRequestNameValuePair> params) throws IOException {
		url += url.contains("?") ? "&" : "?";
		url += paramsToString(params);

		return url;
	}

	public static String paramsToString(List<HttpRequestNameValuePair> params) throws IOException {
		try (InputStream is = toUrlEncoded(params).getContent()) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

    public HttpResponse execute(HttpClient client, HttpContext context, HttpRequestBase method,
								PrintStream logger) throws IOException, InterruptedException {
        logger.println("Sending request to url: " + method.getURI());

        final HttpResponse httpResponse = client.execute(method, context);
        logger.println("Response Code: " + httpResponse.getStatusLine());

        return httpResponse;
    }
}
