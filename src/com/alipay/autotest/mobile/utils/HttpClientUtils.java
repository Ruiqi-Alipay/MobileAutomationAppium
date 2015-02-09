package com.alipay.autotest.mobile.utils;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.alipay.common.lang.util.HttpClientResponse;

/**
 * HttpClientģ�壬����Apache Common HttpClient����������Ĭ�ϳ�ʱ������ִ��Http���󣬶Ͽ�����
 * @author ming.zm
 *
 */
public class HttpClientUtils {

    private HttpClient httpClient;

    private int        maxConnPerHost           = 6;

    private int        maxTotalConn             = 10;

    /** Ĭ�ϵȴ����ӽ�����ʱ����λ:����*/
    private int        connectionTimeout        = 10000;

    /** Ĭ�ϵȴ����ݷ��س�ʱ����λ:����*/
    private int        soTimeout                = 60000;

    /** Ĭ���������ӳ����ӳ�ʱ,��λ:����*/
    private int        connectionManagerTimeout = 10000;

    public HttpClientUtils() {
        this.httpClient = createHttpClient();
    }

    /**
     * ��Get��ʽִ��http����
     * @param url
     * @return ResponseBody
     * @throws IOException
     * @throws HttpException
     */
    public String executeGet(final String url) throws IOException {

        HttpClientResponse res = execute(new GetMethod(url));
        return (String) res.getResponseBody();
    }

    /**
     * ��Post��ʽִ��http����
     * @param url
     * @return ResponseBody
     * @throws IOException
     * @throws HttpException
     */
    public String executePost(final String url) throws IOException {

        HttpClientResponse res = execute(new PostMethod(url));
        return (String) res.getResponseBody();
    }

    /**
     * ʹ��HttpClientCallback����HttpClient����Ӧ
     * @param httpMethod 
     * @param httpClientCallback
     * @return HttpClientResponse , ������Ӧ�뼰����
     * @throws IOException
     * @throws HttpException
     */
    public HttpClientResponse execute(final HttpMethod httpMethod) throws IOException,
                                                                  HttpException {

        HttpClientResponse response = new HttpClientResponse();
        try {
            //����httpclient���ô�ӡcookie injected��warn��־,CookiePolicy.BROWSER_COMPATIBILITY
            httpMethod.getParams().setParameter("http.protocol.cookie-policy",
                CookiePolicy.IGNORE_COOKIES);

            int responseCode = this.httpClient.executeMethod(httpMethod);

            response.setResponseCode(responseCode);
            response.setResponseBody(httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }
        return response;
    }

    /**
     * ��ʼ��httpClient
     */
    private HttpClient createHttpClient() {
        HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        connectionManagerParams.setConnectionTimeout(connectionTimeout);
        connectionManagerParams.setSoTimeout(soTimeout);
        connectionManagerParams.setMaxTotalConnections(maxTotalConn);
        connectionManagerParams.setDefaultMaxConnectionsPerHost(maxConnPerHost);
        httpConnectionManager.setParams(connectionManagerParams);
        HttpClient httpClient = new HttpClient(httpConnectionManager);
        httpClient.getParams().setConnectionManagerTimeout(connectionManagerTimeout);
        return httpClient;
    }
}
