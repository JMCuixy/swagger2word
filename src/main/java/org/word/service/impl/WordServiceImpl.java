package org.word.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.word.dto.Request;
import org.word.dto.Response;
import org.word.dto.Table;
import org.word.service.WordService;
import org.word.utils.JsonUtils;

import java.util.*;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
@Slf4j
@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${swagger.url}")
    private String swaggerUrl;

    private static final String SUBSTR = "://";
    private static final String LEFT_BRACKETS = "{";
    private static final String RIGHT_BRACKETS = "}";

    @Override
    public List<Table> tableList() {
        List<Table> result = new ArrayList<>();
        try {
            String jsonStr = restTemplate.getForObject(swaggerUrl, String.class);
            // convert JSON string to Map
            Map<String, Object> map = JsonUtils.readValue(jsonStr, HashMap.class);
            //得到 host 和 basePath，拼接访问路径
            String host = StringUtils.substringBeforeLast(swaggerUrl, SUBSTR) + SUBSTR + map.get("host") + map.get("basePath");
            //解析paths
            Map<String, LinkedHashMap> paths = (LinkedHashMap) map.get("paths");
            if (paths != null) {
                Iterator<Map.Entry<String, LinkedHashMap>> it = paths.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, LinkedHashMap> path = it.next();
                    // 1.请求路径
                    String url = path.getKey();
                    // 2.请求方式，类似为 get,post,delete,put 这样
                    String requestType = "";
                    Map<String, LinkedHashMap> value = path.getValue();
                    Set<String> requestTypes = value.keySet();
                    for (String str : requestTypes) {
                        requestType += str + ",";
                    }
                    Iterator<Map.Entry<String, LinkedHashMap>> it2 = value.entrySet().iterator();
                    //不管有几种请求方式，都只解析第一种
                    Map.Entry<String, LinkedHashMap> firstRequest = it2.next();
                    Map<String, Object> content = firstRequest.getValue();
                    // 4. 大标题（类说明）
                    String title = String.valueOf(((List) content.get("tags")).get(0));
                    // 5.小标题 （方法说明）
                    String tag = String.valueOf(content.get("summary"));
                    // 6.接口描述
                    String description = String.valueOf(content.get("description"));
                    // 7.请求参数格式，类似于 multipart/form-data
                    String requestForm = "";
                    List<String> consumes = (List) content.get("consumes");
                    if (consumes != null && consumes.size() > 0) {
                        for (String consume : consumes) {
                            requestForm += consume + ",";
                        }
                    }
                    // 8.返回参数格式，类似于 application/json
                    String responseForm = "";
                    List<String> produces = (List) content.get("produces");
                    if (produces != null && produces.size() > 0) {
                        for (String produce : produces) {
                            responseForm += produce + ",";
                        }
                    }
                    // 9. 请求体
                    List<Request> requestList = new ArrayList<>();
                    List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");
                    if (parameters != null && parameters.size() > 0) {
                        for (int i = 0; i < parameters.size(); i++) {
                            Request request = new Request();
                            Map<String, Object> param = parameters.get(i);
                            request.setName(String.valueOf(param.get("name")));
                            request.setType(param.get("type") == null ? "Object" : param.get("type").toString());
                            request.setParamType(String.valueOf(param.get("in")));
                            request.setRequire((Boolean) param.get("required"));
                            request.setRemark(String.valueOf(param.get("description")));
                            requestList.add(request);
                        }
                    }
                    // 10.返回体
                    List<Response> responseList = new ArrayList<>();
                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");
                    Iterator<Map.Entry<String, Object>> it3 = responses.entrySet().iterator();
                    while (it3.hasNext()) {
                        Response response = new Response();
                        Map.Entry<String, Object> entry = it3.next();
                        // 状态码 200 201 401 403 404 这样
                        response.setName(entry.getKey());
                        LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
                        response.setDescription(String.valueOf(statusCodeInfo.get("description")));
                        response.setRemark(String.valueOf(statusCodeInfo.get("description")));
                        responseList.add(response);
                    }

                    // 模拟一次HTTP请求,封装请求体和返回体
                    // 得到请求方式
                    String restType = firstRequest.getKey();
                    Map<String, Object> paramMap = buildParamMap(requestList);
                    String buildUrl = buildUrl(host + url, requestList);

                    //封装Table
                    Table table = new Table();
                    table.setTitle(title);
                    table.setUrl(url);
                    table.setTag(tag);
                    table.setDescription(description);
                    table.setRequestForm(StringUtils.removeEnd(requestForm, ","));
                    table.setResponseForm(StringUtils.removeEnd(responseForm, ","));
                    table.setRequestType(StringUtils.removeEnd(requestType, ","));
                    table.setRequestList(requestList);
                    table.setResponseList(responseList);
                    table.setRequestParam(paramMap.toString());
                    table.setResponseParam(doRestRequest(restType, buildUrl, paramMap, url.contains(LEFT_BRACKETS)));
                    result.add(table);
                }
            }
        } catch (Exception e) {
            log.error("parse error", e);
        }
        return result;
    }

    /**
     * 重新构建url
     *
     * @param url
     * @param requestList
     * @return etc:http://localhost:8080/rest/delete?uuid={uuid}
     */
    private String buildUrl(String url, List<Request> requestList) {
        // 针对 pathParams 的参数做额外处理
        if (url.contains(LEFT_BRACKETS) && url.contains(RIGHT_BRACKETS)) {
            String before = StringUtils.substringBefore(url, LEFT_BRACKETS);
            String after = StringUtils.substringAfter(url, RIGHT_BRACKETS);
            return before + 0 + after;
        }
        StringBuffer buffer = new StringBuffer();
        if (requestList != null && requestList.size() > 0) {
            for (Request request : requestList) {
                String name = request.getName();
                buffer.append(name)
                        .append("={")
                        .append(name)
                        .append("}&");
            }
        }
        if (StringUtils.isNotEmpty(buffer.toString())) {
            url += "?" + StringUtils.removeEnd(buffer.toString(), "&");
        }
        return url;

    }

    /**
     * 发送一个 Restful 请求
     *
     * @param restType   "get", "head", "post", "put", "delete", "options", "patch"
     * @param url        资源地址
     * @param paramMap   参数
     * @param pathParams 是否是 pathParams 传参数方式
     * @return
     */
    private String doRestRequest(String restType, String url, Map<String, Object> paramMap, boolean pathParams) {
        Object object = new Object();
        try {
            if (pathParams) {
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(url);
                httpGet.setHeader("accept", "application/json");
                CloseableHttpResponse execute = httpClient.execute(httpGet);
                return EntityUtils.toString(execute.getEntity());
            }
            switch (restType) {
                case "get":
                    object = restTemplate.getForObject(url, Object.class, paramMap);
                    break;
                case "post":
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                    HttpEntity request = new HttpEntity("{}", headers);
                    object = restTemplate.postForObject(url, request, Object.class, paramMap);
                    break;
                case "put":
                    restTemplate.put(url, paramMap, paramMap);
                    break;
                case "head":
                    HttpHeaders httpHeaders = restTemplate.headForHeaders(url, paramMap);
                    return JsonUtils.writeJsonStr(httpHeaders);
                case "delete":
                    restTemplate.delete(url, paramMap);
                    break;
                case "options":
                    Set<HttpMethod> httpMethodSet = restTemplate.optionsForAllow(url, paramMap);
                    return JsonUtils.writeJsonStr(httpMethodSet);
                case "patch":
                    object = restTemplate.execute(url, HttpMethod.PATCH, null, null, paramMap);
                    break;
                case "trace":
                    object = restTemplate.execute(url, HttpMethod.TRACE, null, null, paramMap);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            // 无法使用 restTemplate 发送的请求，返回""
            // ex.printStackTrace();
            return "";
        }
        return object == null ? "" : object.toString();
    }

    /**
     * 封装post请求体
     *
     * @param list
     * @return
     */
    private Map<String, Object> buildParamMap(List<Request> list) {
        Map<String, Object> map = new HashMap<>(8);
        if (list != null && list.size() > 0) {
            for (Request request : list) {
                String name = request.getName();
                String type = request.getType();
                switch (type) {
                    case "string":
                        map.put(name, "string");
                        break;
                    case "integer":
                        map.put(name, 0);
                        break;
                    case "number":
                        map.put(name, 0.0);
                        break;
                    case "boolean":
                        map.put(name, true);
                        break;
                    case "body":
                        map.put(name, new HashMap<>(2));
                        break;
                    default:
                        map.put(name, null);
                        break;
                }
            }
        }
        return map;
    }
}
