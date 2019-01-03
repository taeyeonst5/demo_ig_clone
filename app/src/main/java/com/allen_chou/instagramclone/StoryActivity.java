package com.allen_chou.instagramclone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen_chou.instagramclone.Model.Story;
import com.allen_chou.instagramclone.Model.User;
import com.bumptech.glide.Glide;
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
    private ImageView storyImage, storyPhoto;
    private TextView textUserName;

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

        userId = getIntent().getStringExtra("userId");
        getStories(userId);
        getUserInfo(userId);

        View reverse = findViewById(R.id.reverse);
        View skip = findViewById(R.id.skip);

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
    }

    @Override
    public void onPrev() {
        if (count - 1 < 0)
            return;
        Glide.with(getApplicationContext()).load(images.get(--count)).into(storyImage);
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
}
