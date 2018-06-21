package json;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class Test01 {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void invoke() {
        Map<String, Object> swagger = restTemplate.getForObject("http://192.168.1.199:8680/v2/api-docs", Map.class);
        System.out.println(swagger);
        CloseableHttpClient build = HttpClients.custom().setSSLSocketFactory(null).build();
    }
}
