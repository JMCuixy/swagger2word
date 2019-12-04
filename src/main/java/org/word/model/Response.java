package org.word.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Data
public class Response implements Serializable{

    /**
     * 返回参数
     */
    private String description;

    /**
     * 参数名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;
}
