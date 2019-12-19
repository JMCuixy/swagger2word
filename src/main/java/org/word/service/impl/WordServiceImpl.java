package org.word.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.word.model.ModelAttr;
import org.word.model.Request;
import org.word.model.Response;
import org.word.model.Table;
import org.word.service.WordService;
import org.word.utils.JsonUtils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @Author XiuYin.Cui
 * @Date 2018/1/12
 **/
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Map<String, Object> tableList(String swaggerUrl) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Table> result = new ArrayList<>();
        try {
            String jsonStr = restTemplate.getForObject(swaggerUrl, String.class);
            // convert JSON string to Map
            Map<String, Object> map = JsonUtils.readValue(jsonStr, HashMap.class);

            //解析model
            Map<String, ModelAttr> definitinMap = parseDefinitions(map);

            //解析paths
            Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
            if (paths != null) {
                Iterator<Map.Entry<String, Map<String, Object>>> it = paths.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Map<String, Object>> path = it.next();

                    Iterator<Map.Entry<String, Object>> it2 = path.getValue().entrySet().iterator();
                    // 1.请求路径
                    String url = path.getKey();

                    // 2.请求方式，类似为 get,post,delete,put 这样
                    String requestType = StringUtils.join(path.getValue().keySet(), ",");

                    // 3. 不管有几种请求方式，都只解析第一种
                    Map.Entry<String, Object> firstRequest = it2.next();
                    Map<String, Object> content = (Map<String, Object>) firstRequest.getValue();

                    // 4. 大标题（类说明）
                    String title = String.valueOf(((List) content.get("tags")).get(0));

                    // 5.小标题 （方法说明）
                    String tag = String.valueOf(content.get("summary"));

                    // 6.接口描述
                    String description = String.valueOf(content.get("summary"));

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
                    List<LinkedHashMap> parameters = (ArrayList) content.get("parameters");

                    // 10.返回体
                    Map<String, Object> responses = (LinkedHashMap) content.get("responses");

                    //封装Table
                    Table table = new Table();

                    table.setTitle(title);
                    table.setUrl(url);
                    table.setTag(tag);
                    table.setDescription(description);
                    table.setRequestForm(requestForm);
                    table.setResponseForm(responseForm);
                    table.setRequestType(requestType);
                    table.setRequestList(processRequestList(parameters));
                    table.setResponseList(processResponseCodeList(responses));

                    // 取出来状态是200时的返回值
                    Map<String, Object> obj = (Map<String, Object>) responses.get("200");
                    if (obj != null && obj.get("schema") != null) {
                        table.setModelAttr(processResponseModelAttrs(obj, definitinMap));
                    }

                    //示例
                    table.setRequestParam(processRequestParam(table.getRequestList(), definitinMap));
                    table.setResponseParam(processResponseParam(obj, definitinMap));

                    result.add(table);
                }
            }
            Map<String, List<Table>> tableMap = result.stream().parallel().collect(Collectors.groupingBy(Table::getTitle));
            resultMap.put("tableMap", new TreeMap<>(tableMap));
            resultMap.put("info", map.get("info"));

            log.debug(JsonUtils.writeJsonStr(resultMap));
        } catch (Exception e) {
            log.error("parse error", e);
        }
        return resultMap;
    }

    /**
     * 处理请求参数列表
     *
     * @param parameters
     * @return
     */
    private List<Request> processRequestList(List<LinkedHashMap> parameters) {
        List<Request> requestList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Map<String, Object> param : parameters) {
                Object in = param.get("in");
                Request request = new Request();
                request.setName(String.valueOf(param.get("name")));
                request.setType(param.get("type") == null ? "object" : param.get("type").toString());
                request.setParamType(String.valueOf(in));
                // 考虑对象参数类型
                if (in != null && "body".equals(in)) {
                    Map<String, Object> schema = (Map) param.get("schema");
                    Object ref = schema.get("$ref");
                    // 数组情况另外处理
                    if (schema.get("type") != null && "array".equals(schema.get("type"))) {
                        ref = ((Map) schema.get("items")).get("$ref");
                    }
                    request.setParamType(ref == null ? "{}" : ref.toString());
                }
                // 是否必填
                request.setRequire(false);
                if (param.get("required") != null) {
                    request.setRequire((Boolean) param.get("required"));
                }
                // 参数说明
                request.setRemark(String.valueOf(param.get("description")));
                request.setParamType(request.getParamType().replaceAll("#/definitions/", ""));
                requestList.add(request);
            }
        }
        return requestList;
    }


    /**
     * 处理返回码列表
     *
     * @param responses 全部状态码返回对象
     * @return
     */
    private List<Response> processResponseCodeList(Map<String, Object> responses) {
        List<Response> responseList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> resIt = responses.entrySet().iterator();
        while (resIt.hasNext()) {
            Map.Entry<String, Object> entry = resIt.next();
            Response response = new Response();
            // 状态码 200 201 401 403 404 这样
            response.setName(entry.getKey());
            LinkedHashMap<String, Object> statusCodeInfo = (LinkedHashMap) entry.getValue();
            response.setDescription(String.valueOf(statusCodeInfo.get("description")));
            Object schema = statusCodeInfo.get("schema");
            if (schema != null) {
                Object originalRef = ((LinkedHashMap) schema).get("originalRef");
                response.setRemark(originalRef == null ? "" : originalRef.toString());
            }
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * 处理返回属性列表
     *
     * @param responseObj
     * @param definitinMap
     * @return
     */
    private ModelAttr processResponseModelAttrs(Map<String, Object> responseObj, Map<String, ModelAttr> definitinMap) {
        Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
        String type = (String) schema.get("type");
        String ref = null;
        //数组
        if ("array".equals(type)) {
            Map<String, Object> items = (Map<String, Object>) schema.get("items");
            if (items != null && items.get("$ref") != null) {
                ref = (String) items.get("$ref");
            }
        }
        //对象
        if (schema.get("$ref") != null) {
            ref = (String) schema.get("$ref");
        }

        //其他类型
        ModelAttr modelAttr = new ModelAttr();
        modelAttr.setType(StringUtils.defaultIfBlank(type, StringUtils.EMPTY));

        if (StringUtils.isNotBlank(ref) && definitinMap.get(ref) != null) {
            modelAttr = definitinMap.get(ref);
        }
        return modelAttr;
    }

    /**
     * 解析Definition
     *
     * @param map
     * @return
     */
    private Map<String, ModelAttr> parseDefinitions(Map<String, Object> map) {
        Map<String, Map<String, Object>> definitions = (Map<String, Map<String, Object>>) map.get("definitions");
        Map<String, ModelAttr> definitinMap = new HashMap<>(256);
        if (definitions != null) {
            Iterator<String> modelNameIt = definitions.keySet().iterator();
            while (modelNameIt.hasNext()) {
                String modeName = modelNameIt.next();
                Map<String, Object> modeProperties = (Map<String, Object>) definitions.get(modeName).get("properties");
                if (modeProperties == null) {
                    continue;
                }
                Iterator<Entry<String, Object>> mIt = modeProperties.entrySet().iterator();

                List<ModelAttr> attrList = new ArrayList<>();

                //解析属性
                while (mIt.hasNext()) {
                    Entry<String, Object> mEntry = mIt.next();
                    Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();
                    ModelAttr modeAttr = new ModelAttr();
                    modeAttr.setName(mEntry.getKey());
                    modeAttr.setType((String) attrInfoMap.get("type"));
                    if (attrInfoMap.get("format") != null) {
                        modeAttr.setType(modeAttr.getType() + "(" + attrInfoMap.get("format") + ")");
                    }
                    modeAttr.setType(StringUtils.defaultIfBlank(modeAttr.getType(), "object"));
                    modeAttr.setDescription((String) attrInfoMap.get("description"));
                    attrList.add(modeAttr);
                }

                ModelAttr modeAttr = new ModelAttr();
                Object title = definitions.get(modeName).get("title");
                Object description = definitions.get(modeName).get("description");
                modeAttr.setClassName(title == null ? "" : title.toString());
                modeAttr.setDescription(description == null ? "" : description.toString());
                modeAttr.setProperties(attrList);
                definitinMap.put("#/definitions/" + modeName, modeAttr);
            }
        }
        return definitinMap;
    }

    /**
     * 处理返回值
     *
     * @param responseObj
     * @return
     */
    private String processResponseParam(Map<String, Object> responseObj, Map<String, ModelAttr> definitinMap) throws JsonProcessingException {
        if (responseObj != null && responseObj.get("schema") != null) {
            Map<String, Object> schema = (Map<String, Object>) responseObj.get("schema");
            String type = (String) schema.get("type");
            String ref = null;
            // 数组
            if ("array".equals(type)) {
                Map<String, Object> items = (Map<String, Object>) schema.get("items");
                if (items != null && items.get("$ref") != null) {
                    ref = (String) items.get("$ref");
                }
            }
            // 对象
            if (schema.get("$ref") != null) {
                ref = (String) schema.get("$ref");
            }
            if (StringUtils.isNotEmpty(ref)) {
                ModelAttr modelAttr = definitinMap.get(ref);
                if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                    Map<String, Object> responseMap = new HashMap<>(8);
                    for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                        responseMap.put(subModelAttr.getName(), subModelAttr.getType());
                    }
                    return JsonUtils.writeJsonStr(responseMap);
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 封装请求体
     *
     * @param list
     * @param definitinMap
     * @return
     */
    private String processRequestParam(List<Request> list, Map<String, ModelAttr> definitinMap) throws IOException {
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
                    case "object":
                        ModelAttr modelAttr = definitinMap.get("#/definitions/" + request.getParamType());
                        if (modelAttr != null && !CollectionUtils.isEmpty(modelAttr.getProperties())) {
                            for (ModelAttr subModelAttr : modelAttr.getProperties()) {
                                paramMap.put(subModelAttr.getName(), subModelAttr.getType());
                            }
                            break;
                        }
                    default:
                        paramMap.put(name, null);
                        break;
                }
            }
        }
        return JsonUtils.writeJsonStr(paramMap);
    }
}
