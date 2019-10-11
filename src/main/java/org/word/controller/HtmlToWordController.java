package org.word.controller;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 描述:
 * 直接生成word下载
 *
 * @author huangzhe
 * @create 2019-10-11 3:20 下午
 */
@Controller
public class HtmlToWordController {

    @Autowired
    private RestTemplate template;

    @Value("${server.port}")
    private Integer port;

    @RequestMapping("/word")
    public void word(@RequestParam String url, HttpServletResponse response){
        ResponseEntity<String> forEntity = template.getForEntity("http://localhost:" + port + "/toWord?url=" + url, String.class);
        System.out.println(forEntity.getBody());
        byte[] body = forEntity.getBody().getBytes();
        response.setHeader("Content-Disposition", "attachment;fileName=interface.doc"); // 设置文件头
        try {
            convert(body,response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void convert(byte[] b, OutputStream outputStream) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        POIFSFileSystem poifs = new POIFSFileSystem();
        DirectoryEntry directory = poifs.getRoot();
        DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
        poifs.writeFilesystem(outputStream);
        bais.close();
    }





}
