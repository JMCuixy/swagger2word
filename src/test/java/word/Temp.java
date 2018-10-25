package word;

import com.word.dto.Request;
import com.word.dto.Response;
import com.word.dto.Table;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by XiuYin.Cui on 2018/6/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class Temp {

    @Autowired
    private RestTemplate restTemplate;

    public List<Table> tableListNew() {
        List<Table> list = new LinkedList();
        //1、将 Swagger json文档反序列化成Swagger对象
        Swagger swagger = restTemplate.getForObject("http://192.168.1.199:8680/v2/api-docs", Swagger.class);
        //2、解析数据
        String host = swagger.getHost();
        Map<String, Path> paths = swagger.getPaths();
        if (paths != null) {
            Table table = new Table();
            List<Request> requestList = new LinkedList<>();
            List<Response> responseList = new LinkedList<>();

            String requestForm = ""; //请求参数格式，类似于 multipart/form-data
            String responseForm = ""; //响应参数格式
            String requestType = ""; //请求方式，类似为 get,post,delete,put 这样
            String url; //请求路径
            String title = ""; //大标题（类说明）
            String tag = ""; //小标题 （方法说明）
            String description = ""; //接口描述
            String requestParam = ""; //请求参数
            String responseParam = ""; //返回参数

            Iterator<Map.Entry<String, Path>> it = paths.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Path> pathEntry = it.next();
                //得到请求路径
                url = pathEntry.getKey();
                Path path = pathEntry.getValue();
                //得到请求方式
                requestType = handlePath(path);
                //只对请求路径的任意一种方式做参数解析
                Operation operation = getOperation(path);
                if (operation != null) {
                    title = operation.getTags().get(0);
                    tag = operation.getSummary();
                    description = operation.getDescription();
                    requestForm = StringUtils.join(operation.getConsumes(), ",");
                    responseForm = StringUtils.join(operation.getProduces(), ",");
                    //封装请求参数
                    List<Parameter> parameters = operation.getParameters();
                    if (parameters != null && parameters.size() > 0) {
                        for (Parameter parameter : parameters) {
                            Request request = new Request();
                            request.setName(parameter.getName());
                            request.setType(parameter.getPattern());
                            request.setParamType(parameter.getIn());
                            request.setRequire(parameter.getRequired());
                            request.setRemark(parameter.getDescription());
                            requestList.add(request);
                        }
                    }
                    //封装返回参数
                    Map<String, io.swagger.models.Response> responses = operation.getResponses();
                    if (responses != null) {
                        Set<Map.Entry<String, io.swagger.models.Response>> entrySet = responses.entrySet();
                        Iterator<Map.Entry<String, io.swagger.models.Response>> iterator = entrySet.iterator();
                        while (iterator.hasNext()) {
                            Response response = new Response();
                            Map.Entry<String, io.swagger.models.Response> responseEntry = iterator.next();
                            response.setName(responseEntry.getKey());
                            io.swagger.models.Response responseEntryValue = responseEntry.getValue();
                            response.setDescription(responseEntryValue.getDescription());
                            response.setRemark(responseEntryValue.getExamples().toString());
                            responseList.add(response);
                        }
                    }

                }
                //封装Table
                table.setTitle(title);
                table.setUrl(url);
                table.setTag(tag);
                table.setDescription(description);
                table.setRequestForm(requestForm);
                table.setResponseForm(responseForm);
                table.setRequestType(requestType);
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
     * 返回请求路径的任意一种方式作为参数解析
     *
     * @param path
     * @return
     */
    public Operation getOperation(Path path) {
        Operation operation;
        if ((operation = path.getGet()) != null) {
            return operation;
        }
        if ((operation = path.getPost()) != null) {
            return operation;
        }
        if ((operation = path.getPut()) != null) {
            return operation;
        }
        if ((operation = path.getHead()) != null) {
            return operation;
        }
        if ((operation = path.getDelete()) != null) {
            return operation;
        }
        if ((operation = path.getPatch()) != null) {
            return operation;
        }
        if ((operation = path.getOptions()) != null) {
            return operation;
        }
        return operation;

    }

    /**
     * 处理请求路径，返回支持的请求方式，以 ',' 分隔开
     *
     * @param path
     * @return 类似为 get/post/delete/put
     */
    public String handlePath(Path path) {
        String requestType = "";
        if (path.getGet() != null) {
            requestType += ",get";
        }
        if (path.getPost() != null) {
            requestType += ",post";
        }
        if (path.getPut() != null) {
            requestType += ",put";
        }
        if (path.getHead() != null) {
            requestType += ",head";
        }
        if (path.getDelete() != null) {
            requestType += ",delete";
        }
        if (path.getPatch() != null) {
            requestType += ",patch";
        }
        if (path.getOptions() != null) {
            requestType += ",options";
        }
        return StringUtils.removeStart(requestType, ",");
    }
}
