package com.tool.controller;

import com.tool.dto.Table;
import com.tool.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class JsonController {

    @Autowired
    private TableService tableService;

    @RequestMapping("/getJson")
    public String getJson(Model model){
        List<Table> list = tableService.tableList();
        model.addAttribute("table",list);
        return "json";
    }


}
