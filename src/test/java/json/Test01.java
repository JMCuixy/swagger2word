package json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
public class Test01 {
    public static void main(String[] args) {
        try {
            ClassLoader classLoader = Test01.class.getClassLoader();
            URL resource = classLoader.getResource("data.json");
            Map map = new ObjectMapper().readValue(resource, Map.class);
            Object swagger = map.get("swagger");
            System.out.println(swagger.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
