package org.word.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xiuyin.cui
 * @Description
 * @date 2019/3/22 10:52
 */
@Controller
public class IndexController {


    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        return "index";
    }
}
