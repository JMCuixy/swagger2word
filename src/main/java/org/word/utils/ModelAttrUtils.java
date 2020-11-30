package org.word.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.word.model.ModelAttr;

/**
 * @author ivenhan
 * @Date: 2020/10/15
 */

public class ModelAttrUtils {

	// 封装schema - properties下某个具体property对象
    public static ModelAttr propertyModelAttr(Map<String, Map<String, Object>> property) {
    	ModelAttr modeAttr = new ModelAttr();

        Map<String, Object> modeProperties = (Map<String, Object>) property.get("properties");
        ArrayList modeRequired = (ArrayList) property.get("required");
        List<ModelAttr> attrList = new ArrayList<>();

    	if (modeProperties != null) {
        	Iterator<Entry<String, Object>> mIt = modeProperties.entrySet().iterator();

            //解析属性
            while (mIt.hasNext()) {
                Entry<String, Object> mEntry = mIt.next();
                Map<String, Object> attrInfoMap = (Map<String, Object>) mEntry.getValue();
                ModelAttr child = new ModelAttr();
                child.setName(mEntry.getKey());
                child.setType((String) attrInfoMap.get("type"));
                if (attrInfoMap.get("format") != null) {
                    child.setType(child.getType() + "(" + attrInfoMap.get("format") + ")");
                }
                child.setType(StringUtils.defaultIfBlank(child.getType(), "object"));

                Object ref = attrInfoMap.get("$ref");
                Object items = attrInfoMap.get("items");

                if (items != null && ((Map) items).get("$ref") == null) {
            		ModelAttr refModel = propertyModelAttr((Map<String, Map<String, Object>>)items);
					if (refModel != null) {
					    child.setProperties(refModel.getProperties());
					}
					child.setType(child.getType());
                }

                child.setDescription((String) attrInfoMap.get("description"));

                child.setRequire(false);
                if (modeRequired != null && modeRequired.contains(mEntry.getKey())) {
                    child.setRequire(true);
                }

                attrList.add(child);
            }
        }

        Object title = property.get("title");
        Object description = property.get("description");
        modeAttr.setClassName(title == null ? "" : title.toString());
        modeAttr.setDescription(description == null ? "" : description.toString());
        modeAttr.setProperties(attrList);
        return modeAttr;
    }
}