### 使用步骤（Google Chrome）

1. 修改 application.yml 文件的<strong> swagger.url </strong>为Swagger Json资源的url地址。 
2. 服务启动后：访问 http://host(主机):port(端口)/toWord，etc：http://127.0.0.1:8080/toWord  
3. 可以看到网页上生成的类似 word 文档的页面，右键另存为 xxx.doc 文件即可。

#### 版本： SwaggerToWord 1.0 （2018-01-18）
1. 一个Swagger API 文档转 Word 文档的工具项目 
2. 项目想法和说明可以参考：[http://www.cnblogs.com/jmcui/p/8298823.html](http://www.cnblogs.com/jmcui/p/8298823.html)

#### 版本：SwaggerToWord 1.1 (2018-02-11)
1. 替换 HttpClient 工具类以适配更多的Restful服务。
2. 把 json 示例文件替换成官方的示例文件。
3. 更改写死的模板。让生成的 word 的内容都从 Swagger api 中来。

#### 版本：SwaggerToWord 1.2 (2018-06-21)
1. 引入了 Spring 的 RestTemplate 取代 HttpClients 以支持更多的 Restful 请求。
2. 命名规范以及增加异常处理，对于无法处理的HTTP请求返回空字符串。
3. 修改之前导入data.josn的方式，变成 restTemplate.getForObject("SwaggerJson的url地址",Map.class) 的动态获取方式。

#### 版本：SwaggerToWord 1.3 (2019-06-12)
1. Spring 框架向 SpringBoot 升级
2. thymeleaf 取代 jsp模板 

#### 版本：SwaggerToWord 1.4 (2019-08-02)
1. 取消 HttpClient 的请求方式去获得返回值，改由从 Swagger Json 文件中直接读取  
2. 针对 application/json 请求方式的入参做渲染     
3. 对于文字过多导致 HTML table 变形做适配   
4. 真诚感谢 [fpzhan](https://github.com/fpzhan)  的代码贡献  
