package com.tool.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tool.util.http.builder.HCB;
import com.tool.util.http.common.HttpConfig;
import com.tool.util.http.common.HttpMethods;
import com.tool.util.http.common.Utils;
import com.tool.util.http.exception.HttpProcessException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * 使用HttpClient模拟发送（http/https）请求
 *
 * @author arron
 * @version 1.0
 */
public class HttpClientUtil {

    /**
     * 默认采用的http协议的HttpClient对象
     */
    private static HttpClient client4HTTP;

    /**
     * 默认采用的https协议的HttpClient对象
     */
    private static HttpClient client4HTTPS;

    static {
        client4HTTP = HCB.getInstance().build();
        try {
            client4HTTPS = HCB.getInstance().ssl().build();
        } catch (HttpProcessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判定是否开启连接池、及url是http还是https <br>
     * 如果已开启连接池，则自动调用build方法，从连接池中获取client对象<br>
     * 否则，直接返回相应的默认client对象<br>
     *
     * @param config 请求参数配置
     * @throws HttpProcessException http处理异常
     */
    private static void create(HttpConfig config) {
        if (config.getClient() == null) {//如果为空，设为默认client对象
            if (config.getUrl().toLowerCase().startsWith("https://")) {
                config.setClient(client4HTTPS);
            } else {
                config.setClient(client4HTTP);
            }
        }
    }

    //-----------华----丽----分----割----线--------------

    /**
     * 以Get方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String get(String url, Header[] headers, HttpContext context, String encoding) {
        try {
            HttpConfig instance = HttpConfig.getInstance();
            instance.setUrl(url);
            instance.setHeaders(headers);
            instance.setContext(context);
            instance.setEncoding(encoding);
            return get(instance);
        } catch (HttpProcessException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 以Get方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回结果
     * @throws HttpProcessException http处理异常
     */
    public static String get(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.GET);
        return send(config);
    }

    /**
     * 以Post方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param parasMap 请求参数
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String post(String url, Header[] headers, Map<String, Object> parasMap, HttpContext context, String encoding) {
        try {
            HttpConfig instance = HttpConfig.getInstance();
            instance.setUrl(url);
            instance.setHeaders(headers);
            instance.setMap(parasMap);
            instance.setContext(context);
            instance.setEncoding(encoding);
            return post(instance);
        } catch (HttpProcessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以Post方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String post(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.POST);
        return send(config);
    }

    /**
     * 以Put方式，请求资源或服务
     *
     * @param url      资源地址
     * @param parasMap 请求参数
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String put(String url, Map<String, Object> parasMap, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setMap(parasMap);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return put(instance);
    }

    /**
     * 以Put方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String put(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.PUT);
        return send(config);
    }

    /**
     * 以Delete方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String delete(String url, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return delete(instance);
    }

    /**
     * 以Delete方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String delete(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.DELETE);
        return send(config);
    }

    /**
     * 以Patch方式，请求资源或服务
     *
     * @param url      资源地址
     * @param parasMap 请求参数
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String patch(String url, Map<String, Object> parasMap, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setMap(parasMap);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return patch(instance);
    }

    /**
     * 以Patch方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String patch(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.PATCH);
        return send(config);
    }

    /**
     * 以Head方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String head(String url, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return head(instance);
    }

    /**
     * 以Head方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String head(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.HEAD);
        return send(config);
    }

    /**
     * 以Options方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String options(String url, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return options(instance);
    }

    /**
     * 以Options方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String options(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.OPTIONS);
        return send(config);
    }

    /**
     * 以Trace方式，请求资源或服务
     *
     * @param url      资源地址
     * @param headers  请求头信息
     * @param context  http上下文，用于cookie操作
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String trace(String url, Header[] headers, HttpContext context, String encoding) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setEncoding(encoding);
        return trace(instance);
    }

    /**
     * 以Trace方式，请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String trace(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.TRACE);
        return send(config);
    }

    /**
     * 下载文件
     *
     * @param url     资源地址
     * @param headers 请求头信息
     * @param context http上下文，用于cookie操作
     * @param out     输出流
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static OutputStream down(String url, Header[] headers, HttpContext context, OutputStream out) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setOut(out);
        return down(instance);
    }

    /**
     * 下载文件
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static OutputStream down(HttpConfig config) throws HttpProcessException {
        config.setMethod(HttpMethods.GET);
        return fmt2Stream(execute(config), config.getOut());
    }

    /**
     * 上传文件
     *
     * @param url     资源地址
     * @param headers 请求头信息
     * @param context http上下文，用于cookie操作
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String upload(String url, Header[] headers, HttpContext context) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        return upload(instance);
    }

    /**
     * 上传文件
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String upload(HttpConfig config) throws HttpProcessException {
        if (config.getMethod() != HttpMethods.POST && config.getMethod() != HttpMethods.PUT) {
            config.setMethod(HttpMethods.POST);
        }
        return send(config);
    }

    /**
     * 查看资源链接情况，返回状态码
     *
     * @param url     资源地址
     * @param headers 请求头信息
     * @param context http上下文，用于cookie操作
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static int status(String url, Header[] headers, HttpContext context, HttpMethods method) throws HttpProcessException {
        HttpConfig instance = HttpConfig.getInstance();
        instance.setUrl(url);
        instance.setHeaders(headers);
        instance.setContext(context);
        instance.setMethod(method);
        return status(instance);
    }

    /**
     * 查看资源链接情况，返回状态码
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static int status(HttpConfig config) throws HttpProcessException {
        return fmt2Int(execute(config));
    }

    //-----------华----丽----分----割----线--------------

    /**
     * 请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    public static String send(HttpConfig config) throws HttpProcessException {
        return fmt2String(execute(config), config.getOutenc());
    }

    /**
     * 请求资源或服务
     *
     * @param config 请求参数配置
     * @return 返回HttpResponse对象
     * @throws HttpProcessException http处理异常
     */
    private static HttpResponse execute(HttpConfig config) throws HttpProcessException {
        create(config);//获取链接
        HttpResponse httpResponse;
        try {
            //创建请求对象
            HttpRequestBase request = getRequest(config.getUrl(), config.getMethod());

            //设置header信息
            request.setHeaders(config.getHeaders());

            //判断是否支持设置entity(仅HttpPost、HttpPut、HttpPatch支持)
            if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(request.getClass())) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                //检测url中是否存在参数
                config.setUrl(Utils.checkHasParas(config.getUrl(), nvps, config.getInenc()));

                //装填参数
                HttpEntity entity = Utils.map2HttpEntity(nvps, config.getMap(), config.getInenc());

                //设置参数到请求对象中
                ((HttpEntityEnclosingRequestBase) request).setEntity(entity);

            } else {
                int idx = config.getUrl().indexOf("?");
                Utils.info("请求地址：" + config.getUrl().substring(0, (idx > 0 ? idx : config.getUrl().length())));
                if (idx > 0) {
                    Utils.info("请求参数：" + config.getUrl().substring(idx + 1));
                }
            }
            //执行请求操作，并拿到结果（同步阻塞）
            httpResponse = (config.getContext() == null) ? config.getClient().execute(request) : config.getClient().execute(request, config.getContext());
            if (config.isReturnRespHeaders()) {
                //获取所有response的header信息
                config.setHeaders(httpResponse.getAllHeaders());
            }
            //获取结果实体
            return httpResponse;

        } catch (IOException e) {
            throw new HttpProcessException(e);
        }
    }

    //-----------华----丽----分----割----线--------------

    /**
     * 转化为字符串
     *
     * @param resp     响应对象
     * @param encoding 编码
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    private static String fmt2String(HttpResponse resp, String encoding) throws HttpProcessException {
        String body;
        try {
            if (resp.getEntity() != null) {
                // 按指定编码转换结果实体为String类型
                body = EntityUtils.toString(resp.getEntity(), encoding);
                Utils.info(body);
            } else {//有可能是head请求
                body = resp.getStatusLine().toString();
            }
            EntityUtils.consume(resp.getEntity());
        } catch (IOException e) {
            throw new HttpProcessException(e);
        } finally {
            close(resp);
        }
        return body;
    }

    /**
     * 转化为数字
     *
     * @param resp 响应对象
     * @return 返回处理结果
     * @throws HttpProcessException http处理异常
     */
    private static int fmt2Int(HttpResponse resp) throws HttpProcessException {
        int statusCode;
        try {
            statusCode = resp.getStatusLine().getStatusCode();
            EntityUtils.consume(resp.getEntity());
        } catch (IOException e) {
            throw new HttpProcessException(e);
        } finally {
            close(resp);
        }
        return statusCode;
    }

    /**
     * 转化为流
     *
     * @param resp 响应对象
     * @param out  输出流
     * @return 返回输出流
     * @throws HttpProcessException http处理异常
     */
    public static OutputStream fmt2Stream(HttpResponse resp, OutputStream out) throws HttpProcessException {
        try {
            resp.getEntity().writeTo(out);
            EntityUtils.consume(resp.getEntity());
        } catch (IOException e) {
            throw new HttpProcessException(e);
        } finally {
            close(resp);
        }
        return out;
    }

    /**
     * 根据请求方法名，获取request对象
     *
     * @param url    资源地址
     * @param method 请求方式
     * @return 返回Http处理request基类
     */
    private static HttpRequestBase getRequest(String url, HttpMethods method) {
        HttpRequestBase request;
        switch (method.getCode()) {
            case 0:// HttpGet
                request = new HttpGet(url);
                break;
            case 1:// HttpPost
                request = new HttpPost(url);
                break;
            case 2:// HttpHead
                request = new HttpHead(url);
                break;
            case 3:// HttpPut
                request = new HttpPut(url);
                break;
            case 4:// HttpDelete
                request = new HttpDelete(url);
                break;
            case 5:// HttpTrace
                request = new HttpTrace(url);
                break;
            case 6:// HttpPatch
                request = new HttpPatch(url);
                break;
            case 7:// HttpOptions
                request = new HttpOptions(url);
                break;
            default:
                request = new HttpPost(url);
                break;
        }
        return request;
    }

    /**
     * 尝试关闭response
     *
     * @param resp HttpResponse对象
     */
    private static void close(HttpResponse resp) {
        try {
            if (resp == null) return;
            //如果CloseableHttpResponse 是resp的父类，则支持关闭
            if (CloseableHttpResponse.class.isAssignableFrom(resp.getClass())) {
                ((CloseableHttpResponse) resp).close();
            }
        } catch (IOException e) {
            Utils.exception(e);
        }
    }
}