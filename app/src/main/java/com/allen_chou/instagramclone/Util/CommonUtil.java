package com.allen_chou.instagramclone.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class CommonUtil {

    public static String getFileExtension(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0) {
            return filePath.substring(strLength + 1).toLowerCase();
        } else {
            return null;
        }
    }

    public static void setSharedPrefs(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }


    /**
     * @param userId db
     * @param text   comment or like or following
     */
    public static void addNotifications(String userId, String text, String postId, boolean isPost) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("userId", currentUser.getUid());
        notificationMap.put("text", text);
        notificationMap.put("postId", postId);
        notificationMap.put("isPost", isPost);

        reference.push().setValue(notificationMap);
    }

    /**
     * follow don't need postId ,and isPost always false
     */
    public static void addNotificationsByFollow(String userId, String text) {
        addNotifications(userId, text, "", false);
    }
}
