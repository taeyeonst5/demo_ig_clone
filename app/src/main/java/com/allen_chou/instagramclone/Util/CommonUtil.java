package com.allen_chou.instagramclone.Util;

public class CommonUtil {

    public static String getFileExtension(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0) {
            return filePath.substring(strLength + 1).toLowerCase();
        } else {
            return null;
        }
    }
}
