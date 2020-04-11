package org.word.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Data
public class Request implements Serializable{

    /**
     * 参数名
     */
    private String name;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 是否必填
     */
    private Boolean require;

    /**
     * 说明
     */
    private String remark;

    /**
     * 复杂对象引用
     */
    private ModelAttr modelAttr;
}
