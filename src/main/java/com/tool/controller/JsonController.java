package com.tool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class JsonController {

    @RequestMapping("/getJson")
    public String getJson(){
        return "json";
    }


}
