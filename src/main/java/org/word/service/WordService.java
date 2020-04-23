package org.word.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
public interface WordService {

    Map<String,Object> tableList(String swaggerUrl);

    Map<String, Object> tableListFromString(String jsonStr);

    Map<String, Object> tableList(MultipartFile jsonFile);
}
