package org.word.utils;

/**
 * @author : cuixiuyin
 * @date : 2019/8/31
 */
public class MenuUtils {

    public static Integer count = 0;
    public static String menuStr = "null";

    public static boolean isMenu(String tags) {
        if (menuStr.equals(tags)) {
            count++;
        } else {
            menuStr = tags;
            count = 0;
        }
        if (count == 0) {
            return true;
        } else {
            return false;
        }
    }
}
