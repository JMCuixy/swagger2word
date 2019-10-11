package org.word.service;

import org.word.model.Table;

import java.util.List;

/**
 * Created by XiuYin.Cui on 2018/1/12.
 */
public interface WordService {

    List<Table> tableList(String jsonUrl);
}
