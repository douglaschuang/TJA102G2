package com.babymate.forum.util;

public class MaskingUtils {

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "匿名使用者";
        }
        if (name.length() <= 1) {
            return name;
        }
        return name.substring(0, 1) + "oo";
    }
}
