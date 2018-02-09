package com.tool.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/16.
 */
public class NetUtil {
    public static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    /**
     * get请求获取String类型数据
     *
     * @param url 请求链接
     * @return
     */
    public static String get(String url) {
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            return executeHttpResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return "";
    }

    /**
     * post方式请求数据
     *
     * @param url  请求链接
     * @param data post 数据体
     * @return
     */
    public static String postWithForm(String url, Map<String, String> data) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> valuePairs = new ArrayList<>();
        if (null != data) {
            for (String key : data.keySet()) {
                valuePairs.add(new BasicNameValuePair(key, data.get(key)));
            }
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            return executeHttpResponse(httpResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
        }
        return "";
    }


    public static String postWithJSON(String url, String jsonString) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonString, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            return executeHttpResponse(httpResponse);
        } catch (IOException e) {
        } finally {
            httpPost.releaseConnection();
        }
        return "";
    }


    private static String executeHttpResponse(HttpResponse httpResponse) {
        StringBuffer stringBuffer = new StringBuffer();
        HttpEntity entity = httpResponse.getEntity();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(entity.getContent(), "utf-8");
            char[] charbuffer;
            while (0 < reader.read(charbuffer = new char[10])) {
                stringBuffer.append(charbuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return stringBuffer.toString();
    }
}
