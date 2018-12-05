package com.allen_chou.instagramclone.Util;

import android.app.ProgressDialog;
import android.content.Context;


public class ProgressDialogUtil {
    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public static void disMissProgressDialog() {
        progressDialog.dismiss();
    }
}
