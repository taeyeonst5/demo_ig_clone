package com.allen_chou.instagramclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    public static final String FIREBASE_STORAGE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/fir-instagram-9da23.appspot.com/o/defaultProfile.png?alt=media&token=33919152-b866-4dfc-a2c1-cf12e145a3c1";
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutNickName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextNickName;
    private Button buttonRegister;
    private TextView textViewLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViews();
        mAuth = FirebaseAuth.getInstance();
    }

    private void findViews() {
        textInputLayoutEmail = findViewById(R.id.text_input_layout_email);
        textInputLayoutPassword = findViewById(R.id.text_input_layout_password);
        textInputLayoutNickName = findViewById(R.id.text_input_layout_nick_name);
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        editTextNickName = findViewById(R.id.edit_nick_name);
        buttonRegister = findViewById(R.id.button_register);
        textViewLogin = findViewById(R.id.text_login);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialogUtil.showProgressDialog(RegisterActivity.this, getString(R.string.message_please_wait));

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String nickName = editTextNickName.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nickName)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    textInputLayoutEmail.setError(TextUtils.isEmpty(email) ? "must required" : null);
                    textInputLayoutPassword.setError(TextUtils.isEmpty(password) ? "must required" : null);
                    textInputLayoutNickName.setError(TextUtils.isEmpty(nickName) ? "must required" : null);
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must have 6 characters", Toast.LENGTH_SHORT).show();
                    textInputLayoutPassword.setError("Password must have 6-12 characters");
                } else {
                    register(email,password,nickName);
                }
            }
        });

    }

    private void register(String email, String password, final String nickName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("userId", userId);
                            hashMap.put("nickName", nickName);
                            hashMap.put("bio", "");
                            hashMap.put("imageUrl", FIREBASE_STORAGE_IMAGE_URL);

                            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        ProgressDialogUtil.disMissProgressDialog();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });
                        } else {
                            ProgressDialogUtil.disMissProgressDialog();
                            Toast.makeText(RegisterActivity.this, "You can't register with this email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
