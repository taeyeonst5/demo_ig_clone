package com.allen_chou.instagramclone.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.allen_chou.instagramclone.Fragment.APIService;
import com.allen_chou.instagramclone.Notification.Client;
import com.allen_chou.instagramclone.Notification.Data;
import com.allen_chou.instagramclone.Notification.MyResponse;
import com.allen_chou.instagramclone.Notification.Sender;
import com.allen_chou.instagramclone.Notification.Token;
import com.allen_chou.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;


public class CommonUtil {

    private static APIService apiService;

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
     * @param userId publisherId
     * @param text   comment or like or following
     */
    public static void addNotifications(final String userId, final String text, String postId, boolean isPost) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("userId", currentUser.getUid());
        notificationMap.put("text", text);
        notificationMap.put("postId", postId);
        notificationMap.put("isPost", isPost);

        reference.push().setValue(notificationMap);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
        tokenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUser.getUid(), "New Message!", text, R.drawable.ic_notify, userId);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Log.d(TAG, "apiService Failed...");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * follow don't need postId ,and isPost always false
     */
    public static void addNotificationsByFollow(String userId, String text) {
        addNotifications(userId, text, "", false);
    }

    public static void alertDialog(Context context, String title, String negativeButtonText
            , String positiveButtonText, DialogInterface.OnClickListener negativeListener
            , DialogInterface.OnClickListener positiveListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText, negativeListener);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText, positiveListener);
        alertDialog.show();
    }
}
