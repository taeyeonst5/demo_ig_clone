package com.allen_chou.instagramclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.allen_chou.instagramclone.Model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class CommentActivity extends AppCompatActivity {

    public static final String POST_ID = "postId";
    public static final String PUBLISHER_ID = "publisherId";
    private ImageView imageProfile;
    private EditText editTextComment;
    private TextView textViewPost;

    private String postId;
    private String publisherId;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);
        Intent intent = getIntent();
        postId = intent.getStringExtra(POST_ID);
        publisherId = intent.getStringExtra(PUBLISHER_ID);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        findViews();
    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void findViews() {
        imageProfile = findViewById(R.id.image_profile);
        editTextComment = findViewById(R.id.add_comment_edit);
        textViewPost = findViewById(R.id.post_comment_text_view);
        textViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextComment.getText().toString().equals("")) {
                    Toast.makeText(CommentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });
        getImageProfile();
    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", editTextComment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());

        reference.push().setValue(hashMap);
        editTextComment.setText("");
        //todo 訊息完應該導頁嗎?
    }

    private void getImageProfile() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
