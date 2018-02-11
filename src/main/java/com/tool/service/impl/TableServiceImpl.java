package com.tool.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.dto.Request;
import com.tool.dto.Response;
import com.tool.dto.Table;
import com.tool.service.TableService;
import com.tool.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
@Service
public class TableServiceImpl implements TableService {

    private static Map<String, Object> MAP = new HashMap<>(256);

    static {
        try {
            //解析json
            ClassLoader classLoader = TableService.class.getClassLoader();
            URL resource = classLoader.getResource("data.json");
            MAP = new ObjectMapper().readValue(resource, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Table> tableList() {
        List<Table> list = new LinkedList();
        //得到host，用于模拟http请求
        String host = String.valueOf(MAP.get("host"));
        //解析paths
        LinkedHashMap<String, LinkedHashMap> paths = (LinkedHashMap) MAP.get("paths");
        if (paths != null) {
            Iterator<Map.Entry<String, LinkedHashMap>> iterator = paths.entrySet().iterator();
            while (iterator.hasNext()) {
                Table table = new Table();
                List<Request> requestList = new LinkedList<>();
                List<Response> responseList = new LinkedList<>();

                String requestForm = ""; //请求参数格式，类似于 multipart/form-data
                String requestType = ""; //请求方式，类似为 get/post/delete/put 这样
                String url; //请求路径
                String title; //大标题（类说明）
                String tag; //小标题 （方法说明）
                String requestParam = ""; //请求参数
                String responseParam = ""; //返回参数

                Map.Entry<String, LinkedHashMap> next = iterator.next();
                url = next.getKey();

                LinkedHashMap<String, LinkedHashMap> value = next.getValue();
                Set<String> requestTypes = value.keySet();
                for (String str : requestTypes) {
                    requestType += str + "/";
                }

                Iterator<Map.Entry<String, LinkedHashMap>> iterator2 = value.entrySet().iterator();
                //不管有几种请求方式，都只解析第一种
                Map.Entry<String, LinkedHashMap> get = iterator2.next();
                LinkedHashMap getValue = get.getValue();
                title = (String) ((List) getValue.get("tags")).get(0);
                List<String> consumes = (List) getValue.get("consumes");
                if (consumes != null && consumes.size() > 0) {
                    for (String consume : consumes) {
                        requestForm += consume + "、";
                    }
                }
                tag = String.valueOf(getValue.get("summary"));
                //请求体
                List parameters = (ArrayList) getValue.get("parameters");
                if (parameters != null && parameters.size() > 0) {
                    for (int i = 0; i < parameters.size(); i++) {
                        Request request = new Request();
                        LinkedHashMap<String, Object> param = (LinkedHashMap) parameters.get(i);
                        request.setName(String.valueOf(param.get("name")));
                        request.setType(String.valueOf(param.get("type")));
                        request.setParamType(String.valueOf(param.get("in")));
                        request.setRequire((Boolean) param.get("required"));
                        request.setRemark(String.valueOf(param.get("description")));
                        requestList.add(request);
                    }
                }
                //返回体
                LinkedHashMap<String, Object> responses = (LinkedHashMap) getValue.get("responses");
                Iterator<Map.Entry<String, Object>> iterator3 = responses.entrySet().iterator();
                while (iterator3.hasNext()) {
                    Response response = new Response();
                    Map.Entry<String, Object> entry = iterator3.next();
                    String status = entry.getKey(); //状态码 200 201 401 403 404 这样
                    LinkedHashMap<String, Object> statusInfo = (LinkedHashMap) entry.getValue();
                    String statusDescription = (String) statusInfo.get("description");
                    response.setName(status);
                    response.setDescription(statusDescription);
                    response.setRemark(null);
                    responseList.add(response);
                }

                //模拟一次HTTP请求,封装请求体和返回体，如果是Restful的文档可以再补充
                String request;
                request = StringUtils.remove(url, "{");
                request = StringUtils.remove(request, "}");//去掉路径中的{}参数请求
                if (requestType.contains("post")) {
                    Map<String, Object> strMap = otherRequestParam(requestList);
                    requestParam = strMap.toString();
                    responseParam = HttpClientUtil.post(host + request, null, strMap, null, "utf-8");
                } else if (requestType.contains("get")) {
                    String s = getRequestParam(requestList);
                    requestParam = s;
                    responseParam = HttpClientUtil.get(host + request + s, null, null, "utf-8");
                }

                //封装Table
                table.setTitle(title);
                table.setUrl(url);
                table.setTag(tag);
                table.setRequestForm(requestForm);
                table.setResponseForm("application/json");
                table.setRequestType(StringUtils.removeEnd(requestType, "/"));
                table.setRequestList(requestList);
                table.setResponseList(responseList);
                table.setRequestParam(requestParam);
                table.setResponseParam(responseParam);
                list.add(table);
            }
        }
        return list;
    }

    /**
     * 封装post请求体
     *
     * @param list
     * @return
     */
    private Map<String, Object> otherRequestParam(List<Request> list) {
        Map<String, Object> map = new HashMap<>(16);
        if (list != null && list.size() > 0) {
            for (Request request : list) {
                String name = request.getName();
                String type = request.getType();
                switch (type) {
                    case "string":
                        map.put(name, "string");
                        break;
                    case "integer":
                        map.put(name, "0");
                        break;
                    case "double":
                        map.put(name, "0.0");
                        break;
                    case "boolean":
                        map.put(name, "true");
                    default:
                        map.put(name, "null");
                        break;
                }
            }
        }
        return map;
    }

    /**
     * 封装get参数
     *
     * @param list
     * @return
     */
    private String getRequestParam(List<Request> list) {
        StringBuffer stringBuffer = new StringBuffer();
        if (list != null && list.size() > 0) {
            for (Request request : list) {
                String name = request.getName();
                String type = request.getType();
                switch (type) {
                    case "string":
                        stringBuffer.append("&" + name + "=string");
                        break;
                    case "integer":
                        stringBuffer.append("&" + name + "=0");
                        break;
                    case "double":
                        stringBuffer.append("&" + name + "=0.0");
                        break;
                    case "boolean":
                        stringBuffer.append("&" + name + "=true");
                    default:
                        stringBuffer.append("&" + name + "=null");
                        break;
                }
            }
        }
        String s = stringBuffer.toString();
        if ("".equalsIgnoreCase(s)) {
            return "";
        }
        return "?" + StringUtils.removeStart(s, "&");
    }

}
