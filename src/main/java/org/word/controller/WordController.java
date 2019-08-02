package org.word.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.word.dto.Table;
import org.word.service.WordService;

import java.util.List;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class WordController {

    @Autowired
    private WordService tableService;

    /**
     * @param model
     * @return
     *
     * @see #toWord(Model)
     */
    @Deprecated
    @RequestMapping("/getWord")
    public String getWord(Model model) {
        List<Table> tables = tableService.tableList();
        model.addAttribute("tables", tables);
        return "word";
    }


    @RequestMapping("/toWord")
    public String toWord(Model model) {
        List<Table> tables = tableService.tableList();
        model.addAttribute("tables", tables);
        return "word";
    }
}
