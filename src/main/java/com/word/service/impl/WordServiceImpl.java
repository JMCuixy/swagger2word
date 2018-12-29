package com.word.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.word.dto.Request;
import com.word.dto.Response;
import com.word.dto.Table;
import com.word.service.WordService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.List;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${swaggerUrl}")
    private String swaggerUrl;

    @Override
    public List<Table> tableList() {
        String json = restTemplate.getForObject(swaggerUrl, String.class);

        Map<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        try {
            // convert JSON string to Map
            map = mapper.readValue(json, new TypeReference<HashMap<String,Object>>(){});
        } catch (Exception e) {
            LoggerFactory.getLogger(WordService.class).error("parse error", e);
        }

        List<Table> list = new LinkedList();
        //得到host，并添加上http 或 https
        String host = StringUtils.substringBefore(swaggerUrl, ":") + String.valueOf(map.get("host"));
        //解析paths
        LinkedHashMap<String, LinkedHashMap> paths = (LinkedHashMap) map.get("paths");
        if (paths != null) {
            Iterator<Map.Entry<String, LinkedHashMap>> it = paths.entrySet().iterator();
            while (it.hasNext()) {
                Table table = new Table();
                List<Request> requestList = new LinkedList<>();
                List<Response> responseList = new LinkedList<>();
                // 请求参数格式，类似于 multipart/form-data
                String requestForm = "";
                // 请求参数格式，类似于 multipart/form-data
                String responseForm = "";
                // 请求方式，类似为 get,post,delete,put 这样
                String requestType = "";
                String url; // 请求路径
                String title; // 大标题（类说明）
                String tag; // 小标题 （方法说明）
                String description; //接口描述

                Map.Entry<String, LinkedHashMap> path = it.next();
                url = path.getKey();

                LinkedHashMap<String, LinkedHashMap> value = path.getValue();
                Set<String> requestTypes = value.keySet();
                for (String str : requestTypes) {
                    requestType += str + ",";
                }

                Iterator<Map.Entry<String, LinkedHashMap>> it2 = value.entrySet().iterator();
                // 不管有几种请求方式，都只解析第一种
                Map.Entry<String, LinkedHashMap> firstRequestType = it2.next();
                LinkedHashMap content = firstRequestType.getValue();
                title = String.valueOf(((List) content.get("tags")).get(0));
                description = String.valueOf(content.get("description"));
                List<String> consumes = (List) content.get("consumes");
                if (consumes != null && consumes.size() > 0) {
                    for (String consume : consumes) {
                        requestForm += consume + ",";
                    }
                }
                List<String> produces = (List) content.get("produces");
                if (produces != null && produces.size() > 0) {
                    for (String produce : produces) {
                        responseForm += produce + ",";
                    }
                }

                tag = String.valueOf(content.get("summary"));
                //请求体
                List parameters = (ArrayList) content.get("parameters");
                if (parameters != null && parameters.size() > 0) {
                    for (int i = 0; i < parameters.size(); i++) {
                        Request request = new Request();
                        LinkedHashMap<String, Object> param = (LinkedHashMap) parameters.get(i);
                        request.setName(String.valueOf(param.get("name")));
                        request.setType(param.get("type") == null ? "Object" : param.get("type").toString());
                        request.setParamType(String.valueOf(param.get("in")));
                        request.setRequire((Boolean) param.get("required"));
                        request.setRemark(String.valueOf(param.get("description")));
                        requestList.add(request);
                    }
                }
                //返回体
                LinkedHashMap<String, Object> responses = (LinkedHashMap) content.get("responses");
                Iterator<Map.Entry<String, Object>> it3 = responses.entrySet().iterator();
                while (it3.hasNext()) {
                    Response response = new Response();
                    Map.Entry<String, Object> entry = it3.next();
                    // 状态码 200 201 401 403 404 这样
                    String statusCode = entry.getKey();
                    LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
                    String statusDescription = (String) statusCodeInfo.get("description");
                    response.setName(statusCode);
                    response.setDescription(statusDescription);
                    response.setRemark(null);
                    responseList.add(response);
                }

                // 模拟一次HTTP请求,封装请求体和返回体
                // 得到请求方式
                String restType = firstRequestType.getKey();
                Map<String, Object> paramMap = ParamMap(requestList);
                String buildUrl = buildUrl(host + url, requestList);

                //封装Table
                table.setTitle(title);
                table.setUrl(url);
                table.setTag(tag);
                table.setDescription(description);
                table.setRequestForm(StringUtils.removeEnd(requestForm, ","));
                table.setResponseForm(StringUtils.removeEnd(responseForm, ","));
                table.setRequestType(StringUtils.removeEnd(requestType, ","));
                table.setRequestList(requestList);
                table.setResponseList(responseList);
                table.setRequestParam(String.valueOf(paramMap));
                table.setResponseParam(doRestRequest(restType, buildUrl, paramMap));
                list.add(table);
            }
        }
        return list;
    }

    /**
     * 重新构建url
     *
     * @param url
     * @param requestList
     * @return etc:http://localhost:8080/rest/delete?uuid={uuid}
     */
    private String buildUrl(String url, List<Request> requestList) {
        String param = "";
        if (requestList != null && requestList.size() > 0) {
            for (Request request : requestList) {
                String name = request.getName();
                param += name + "={" + name + "}&";
            }
        }
        if (StringUtils.isNotEmpty(param)) {
            url += "?" + StringUtils.removeEnd(param, "&");
        }
        return url;

    }

    /**
     * 发送一个 Restful 请求
     *
     * @param restType "get", "head", "post", "put", "delete", "options", "patch"
     * @param url      资源地址
     * @param paramMap 参数
     * @return
     */
    private String doRestRequest(String restType, String url, Map<String, Object> paramMap) {
        Object object = null;
        try {
            switch (restType) {
                case "get":
                    object = restTemplate.getForObject(url, Object.class, paramMap);
                    break;
                case "post":
                    object = restTemplate.postForObject(url, null, Object.class, paramMap);
                    break;
                case "put":
                    restTemplate.put(url, null, paramMap);
                    break;
                case "head":
                    HttpHeaders httpHeaders = restTemplate.headForHeaders(url, paramMap);
                    return String.valueOf(httpHeaders);
                case "delete":
                    restTemplate.delete(url, paramMap);
                    break;
                case "options":
                    Set<HttpMethod> httpMethods = restTemplate.optionsForAllow(url, paramMap);
                    return String.valueOf(httpMethods);
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
        return String.valueOf(object);
    }

    /**
     * 封装post请求体
     *
     * @param list
     * @return
     */
    private Map<String, Object> ParamMap(List<Request> list) {
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
                    default:
                        map.put(name, null);
                        break;
                }
            }
        }
        return map;
    }
}
