package com.allen_chou.instagramclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allen_chou.instagramclone.Model.Story;
import com.allen_chou.instagramclone.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private int count = 0;
    private long pressTime;
    private long limit = 500L;

    private StoriesProgressView storiesProgressView;
    private ImageView storyImage, storyPhoto, storyDelete;
    private TextView textUserName, textSeenCounter;
    private LinearLayout storySeenLayout;

    private String userId;
    private List<String> images;
    private List<String> storyIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        storiesProgressView = findViewById(R.id.stories);
        storyImage = findViewById(R.id.story_image);
        storyPhoto = findViewById(R.id.story_photo);
        textUserName = findViewById(R.id.story_username);
        View reverse = findViewById(R.id.reverse);
        View skip = findViewById(R.id.skip);
        storySeenLayout = findViewById(R.id.story_seen_linear_layout);
        textSeenCounter = findViewById(R.id.text_seen_counter);
        storyDelete = findViewById(R.id.story_delete);
        storySeenLayout.setVisibility(View.GONE);
        storyDelete.setVisibility(View.GONE);

        userId = getIntent().getStringExtra("userId");

        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            storySeenLayout.setVisibility(View.VISIBLE);
            storyDelete.setVisibility(View.VISIBLE);
        }
        getStories(userId);
        getUserInfo(userId);

        reverse.setOnTouchListener(onTouchListener);
        skip.setOnTouchListener(onTouchListener);

        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        storySeenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("title", "views");
                intent.putExtra("storyId", storyIds.get(count));
                startActivity(intent);
            }
        });
        storyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                        .child(userId).child(storyIds.get(count));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StoryActivity.this, "story deleted!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;

            }
            return false;
        }
    };

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++count)).into(storyImage);
        addView(storyIds.get(count));
        seenCounter(storyIds.get(count));
    }

    @Override
    public void onPrev() {
        if (count - 1 < 0)
            return;
        Glide.with(getApplicationContext()).load(images.get(--count)).into(storyImage);
        seenCounter(storyIds.get(count));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String userId) {
        images = new ArrayList<>();
        storyIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long currentTime = System.currentTimeMillis();
                    if (story.getTimeStart() < currentTime && story.getTimeEnd() > currentTime) {
                        images.add(story.getImageUrl());
                        storyIds.add(story.getStoryId());
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(count);

                Glide.with(getApplicationContext()).load(images.get(count)).into(storyImage);
                addView(storyIds.get(count));
                seenCounter(storyIds.get(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(storyPhoto);
                textUserName.setText(user.getNickName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addView(String storyId) {
        FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId)
                .child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenCounter(String storyId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId)
                .child(storyId).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textSeenCounter.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
