package com.word.controller;

import com.word.dto.Table;
import com.word.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class WordController {

    @Autowired
    private WordService tableService;

    @RequestMapping("/getWord")
    public String getJson(Model model){
        List<Table> list = tableService.tableList();
        model.addAttribute("table",list);
        return "word";
    }
}
