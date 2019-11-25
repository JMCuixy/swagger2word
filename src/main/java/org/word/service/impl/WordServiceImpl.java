package org.word.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.word.model.Request;
import org.word.model.Response;
import org.word.model.Table;
import org.word.service.WordService;
import org.word.utils.JsonUtils;
import org.word.utils.MenuUtils;

import java.io.IOException;
import java.util.*;

/**
 * @Author XiuYin.Cui
 * @Date 2018/1/12
 **/
@Slf4j
@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${swagger.url}")
    private String swaggerUrl;

    @Override
    public List<Table> tableList(String jsonUrl) {
        jsonUrl = Optional.ofNullable(jsonUrl).orElse(swaggerUrl);
        List<Table> result = new ArrayList<>();
        try {
            String jsonStr = restTemplate.getForObject(jsonUrl, String.class);
            // convert JSON string to Map
            Map<String, Object> map = JsonUtils.readValue(jsonStr, HashMap.class);
            // 解析paths
            Map<String, LinkedHashMap> paths = (LinkedHashMap) map.get("paths");
            if (paths != null) {
                Iterator<Map.Entry<String, LinkedHashMap>> it = paths.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, LinkedHashMap> path = it.next();
                    Iterator<Map.Entry<String, LinkedHashMap>> it2 = path.getValue().entrySet().iterator();
                    // 1.请求路径
                    String url = path.getKey();
                    Map<String, Object> values = path.getValue();
                    List<LinkedHashMap> urlParameters = (ArrayList) values.get("parameters");
                    // 解析所有方法
                    while (it2.hasNext()) {
                        Map.Entry<String, LinkedHashMap> requestItem = it2.next();
                        // 2.请求方式，类似为 get,post,delete,put
                        String requestType = requestItem.getKey();
                        if (requestItem.getKey() == "parameters") {
                            continue;
                        }
                        Map<String, Object> content = requestItem.getValue();
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
                            requestForm = StringUtils.join(consumes, ",");
                        }
                        // 8.返回参数格式，类似于 application/json
                        String responseForm = "";
                        List<String> produces = (List) content.get("produces");
                        if (produces != null && produces.size() > 0) {
                            responseForm = StringUtils.join(produces, ",");
                        }
                        // 9. 请求体
                        List<Request> requestList = new ArrayList<>();
                        List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");
                        if (!CollectionUtils.isEmpty(parameters)) {
                            if (urlParameters != null && parameters != null) {
                                parameters.addAll(urlParameters);
                            }
                            for (Map<String, Object> param : parameters) {
                                Request request = new Request();
                                request.setName(String.valueOf(param.get("name")));
                                Object in = param.get("in");
                                if (in != null && "body".equals(in)) {
                                    request.setType(String.valueOf(in));
                                    Map<String, Object> schema = (Map) param.get("schema");
                                    Object ref = schema.get("$ref");
                                    // 数组情况另外处理
                                    if (schema.get("type") != null && "array".equals(schema.get("type"))) {
                                        ref = ((Map) schema.get("items")).get("$ref");
                                    }
                                    request.setParamType(ref == null ? "{}" : ref.toString());
                                } else {
                                    request.setType(
                                            param.get("type") == null ? "Object" : param.get("type").toString());
                                    request.setParamType(String.valueOf(in));
                                }
                                if (param.get("required") != null) {
                                    request.setRequire((Boolean) param.get("required"));
                                } else {
                                    request.setRequire(false);
                                }
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

                        // 封装Table
                        Table table = new Table();
                        // 是否添加为菜单
                        if (MenuUtils.isMenu(title)) {
                            table.setTitle(title);
                        }
                        table.setUrl(url);
                        table.setTag(tag);
                        table.setDescription(description);
                        table.setRequestForm(requestForm);
                        table.setResponseForm(responseForm);
                        table.setRequestType(requestType);
                        table.setResponseList(responseList);
                        table.setRequestParam(JsonUtils.writeJsonStr(buildParamMap(requestList, map)));
                        for (Request request : requestList) {
                            request.setParamType(request.getParamType().replaceAll("#/definitions/", ""));
                        }
                        table.setRequestList(requestList);
                        // 取出来状态是200时的返回值
                        Object obj = responses.get("200");
                        if (obj == null) {
                            table.setResponseParam("");
                            result.add(table);
                            continue;
                        }
                        Object schema = ((Map) obj).get("schema");
                        if (schema == null) {
                            table.setResponseParam("");
                            result.add(table);
                            continue;
                        }
                        if (((Map) schema).get("$ref") != null) {
                            // 非数组类型返回值
                            String ref = (String) ((Map) schema).get("$ref");
                            // 解析swagger2 ref链接
                            ObjectNode objectNode = parseRef(ref, map);
                            table.setResponseParam(objectNode.toString());
                            result.add(table);
                            continue;
                        }
                        Object items = ((Map) schema).get("items");
                        if (items != null && ((Map) items).get("$ref") != null) {
                            // 数组类型返回值
                            String ref = (String) ((Map) items).get("$ref");
                            // 解析swagger2 ref链接
                            ObjectNode objectNode = parseRef(ref, map);
                            ArrayNode arrayNode = JsonUtils.createArrayNode();
                            arrayNode.add(objectNode);
                            table.setResponseParam(arrayNode.toString());
                            result.add(table);
                            continue;
                        }
                        result.add(table);
                    }
                }
            }
        } catch (Exception e) {
            log.error("parse error", e);
        }
        return result;
    }

    /**
     * 从map中解析出指定的ref
     *
     * @param ref ref链接 例如："#/definitions/PageInfoBT«Customer»"
     * @param map 是整个swagger json转成map对象
     * @return
     * @author fpzhan
     */
    private ObjectNode parseRef(String ref, Map<String, Object> map) {
        ObjectNode objectNode = JsonUtils.createObjectNode();
        if (StringUtils.isNotEmpty(ref) && ref.startsWith("#")) {
            String[] refs = ref.split("/");
            Map<String, Object> tmpMap = map;
            // 取出ref最后一个参数 start
            for (String tmp : refs) {
                if (!"#".equals(tmp)) {
                    tmpMap = (Map<String, Object>) tmpMap.get(tmp);
                }
            }
            // 取出ref最后一个参数 end
            // 取出参数
            if (tmpMap == null) {
                return objectNode;
            }
            Object properties = tmpMap.get("properties");
            if (properties == null) {
                return objectNode;
            }
            Map<String, Object> propertiesMap = (Map<String, Object>) properties;
            Set<String> keys = propertiesMap.keySet();
            // 遍历key
            for (String key : keys) {
                Map<String, Object> keyMap = (Map) propertiesMap.get(key);
                if ("array".equals(keyMap.get("type"))) {
                    // 数组的处理方式
                    String sonRef = (String) ((Map) keyMap.get("items")).get("$ref");
                    // 对象自包含，跳过解析
                    if (ref.equals(sonRef)) {
                        continue;
                    }
                    JsonNode jsonNode = parseRef(sonRef, map);
                    ArrayNode arrayNode = JsonUtils.createArrayNode();
                    arrayNode.add(jsonNode);
                    objectNode.set(key, arrayNode);
                } else if (keyMap.get("$ref") != null) {
                    // 对象的处理方式
                    String sonRef = (String) keyMap.get("$ref");
                    // 对象自包含，跳过解析
                    if (ref.equals(sonRef)) {
                        continue;
                    }
                    ObjectNode object = parseRef(sonRef, map);
                    objectNode.set(key, object);
                } else {
                    // 其他参数的处理方式，string、int
                    String str = "";
                    if (keyMap.get("description") != null) {
                        str = str + keyMap.get("description");
                    }
                    if (keyMap.get("format") != null) {
                        str = str + String.format("格式为(%s)", keyMap.get("format"));
                    }
                    objectNode.put(key, str);
                }
            }
        }
        return objectNode;
    }

    /**
     * 封装post请求体
     *
     * @param list
     * @param map
     * @return
     */
    private Map<String, Object> buildParamMap(List<Request> list, Map<String, Object> map) throws IOException {
        Map<String, Object> paramMap = new HashMap<>(8);
        if (list != null && list.size() > 0) {
            for (Request request : list) {
                String name = request.getName();
                String type = request.getType();
                switch (type) {
                case "string":
                    paramMap.put(name, "string");
                    break;
                case "integer":
                    paramMap.put(name, 0);
                    break;
                case "number":
                    paramMap.put(name, 0.0);
                    break;
                case "boolean":
                    paramMap.put(name, true);
                    break;
                case "body":
                    String paramType = request.getParamType();
                    ObjectNode objectNode = parseRef(paramType, map);
                    paramMap = JsonUtils.readValue(objectNode.toString(), Map.class);
                    break;
                default:
                    paramMap.put(name, null);
                    break;
                }
            }
        }
        return paramMap;
    }
}
