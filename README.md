<h3>使用步骤：</h3>
<p>
 1、修改resources目录下resources.properties文件的<strong> swaggerUrl </strong>为Swagger Json资源的url地址。<br/>
2、服务启动后：访问 http://host(主机):port(端口)/getWord，etc：http://localhost:8080/getWord <br/>
3、将生成的getWord文件，增加后缀名 getWord.doc 。
</p>
<p>----------------------------------</p>
<h3> 版本： SwaggerToWord 1.0 （2018-01-18）</h3>
<p>简介：一个Swagger API 文档 转 Word 文档的工具项目</P>
<p>备注：项目想法和说明可以参考：<a href='http://www.cnblogs.com/jmcui/p/8298823.html'>http://www.cnblogs.com/jmcui/p/8298823.html</a></P>
<p>----------------------------------</p>
<h3>版本：SwaggerToWord 1.1 (2018-02-11)</h3>
<p>更新说明：</P>
 <h6>已解决：</h6>
 <ul>   
   <li>替换HttpClient工具类以适配更多的Restful服务。</li>   
   <li>把 json 示例文件替换成 官方的示例文件。</li>    
   <li>更改写死的模板。让生成的 word 的内容都从Swagger api 中来。</li> 
 </ul>
 <h6>待解决：</h6>
 <ul>
   <li>Http 诸多的参数请求形式，比如 header、body、file等 还没有去处理。</li>
   <li>用户自定义的对象还没有适配。</li> 
 </ul>   
<p>----------------------------------</p>
<h3>版本：SwaggerToWord 1.2 (2018-06-21)</h3>
<p>更新说明：</P>
<p>
1、引入了Spring的RestTemplate取代 HttpClients 以支持更多的Restful请求。<br/>
2、命名规范以及增加异常处理，对于无法处理的HTTP请求返回空字符串。<br/>
3、修改之前导入data.josn的方式，变成restTemplate.getForObject("SwaggerJson的url地址",Map.class);的动态获取方式。
</p>
  
