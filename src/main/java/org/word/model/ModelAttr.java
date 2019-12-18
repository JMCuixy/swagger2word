package org.word.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 返回属性
 *
 * @author kevin
 */
@Data
public class ModelAttr implements Serializable {

    private static final long serialVersionUID = -4074067438450613643L;

    /**
     * 类名
     */
    private String className = StringUtils.EMPTY;
    /**
     * 属性名
     */
    private String name = StringUtils.EMPTY;
    /**
     * 类型
     */
    private String type = StringUtils.EMPTY;
    /**
     * 属性描述
     */
    private String description;
    /**
     * 嵌套属性列表
     */
    private List<ModelAttr> properties = new ArrayList<>();
}
