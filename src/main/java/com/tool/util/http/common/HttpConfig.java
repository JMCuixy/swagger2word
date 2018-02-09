package com.tool.util.http.common;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求配置类
 *
 * @author arron
 * @version 1.0
 */
public class HttpConfig {

    /**
     * HttpClient对象
     */
    private HttpClient client;
    /**
     * 输出参数
     */
    private OutputStream out;

    /**
     * 请求路径
     */
    private String url;

    /**
     * Header头信息
     */
    private Header[] headers;

    /**
     * 是否返回response的headers
     */
    private boolean isReturnRespHeaders;

    /**
     * 请求方式
     */
    private HttpMethods method = HttpMethods.GET;

    /**
     * 请求方法名称
     */
    private String methodName;

    /**
     * 用于cookie操作
     */
    private HttpContext context;

    /**
     * 传递参数
     */
    private Map<String, Object> map;


    /**
     * 输入输出编码
     */
    private String encoding = Charset.defaultCharset().displayName();

    /**
     * 输入编码
     */
    private String inenc;

    /**
     * 输出编码
     */
    private String outenc;


    private HttpConfig() {
    }

    /**
     * 获取实例
     */
    public static HttpConfig getInstance() {
        return new HttpConfig();
    }


    public void setClient(HttpClient httpClient) {
        this.client = httpClient;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public void setHeaders(Header[] headers, boolean isReturnRespHeaders) {
        this.headers = headers;
        this.isReturnRespHeaders = isReturnRespHeaders;
    }

    public void setMethod(HttpMethods method) {
        this.method = method;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setContext(HttpContext context) {
        this.context = context;
    }

    public void setMap(Map<String, Object> map) {
        synchronized (getClass()) {
            if (this.map == null || map == null) {
                this.map = map;
            } else {
                this.map.putAll(map);
            }
        }
    }

    /**
     * @param json 以json格式字符串作为参数
     */
    public void setMap(String json) {
        map = new HashMap<>(16);
        map.put(Utils.ENTITY_STRING, json);
    }

    /**
     * 上传文件时用到
     *
     * @param filePaths                     待上传文件所在路径
     * @param inputName                     即file input 标签的name值，默认为file
     * @param forceRemoveContentTypeChraset 是否强制一处content-type中设置的编码类型
     */
    public void setMap(String[] filePaths, String inputName, boolean forceRemoveContentTypeChraset) {
        synchronized (getClass()) {
            if (this.map == null) {
                this.map = new HashMap<>(16);
            }
        }
        map.put(Utils.ENTITY_MULTIPART, filePaths);
        map.put(Utils.ENTITY_MULTIPART + ".name", inputName);
        map.put(Utils.ENTITY_MULTIPART + ".rmCharset", forceRemoveContentTypeChraset);
    }

    public void setInenc(String inenc) {
        this.inenc = inenc;
    }

    public void setOutenc(String outenc) {
        this.outenc = outenc;
    }

    public void setEncoding(String encoding) {
        setInenc(encoding);
        setOutenc(encoding);
        this.encoding = encoding;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public HttpClient getClient() {
        return client;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public boolean isReturnRespHeaders() {
        return isReturnRespHeaders;
    }

    public HttpMethods getMethod() {
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public HttpContext getContext() {
        return context;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getInenc() {
        return inenc;
    }

    public String getOutenc() {
        return outenc;
    }

    public OutputStream getOut() {
        return out;
    }

    public String getUrl() {
        return url;
    }
}