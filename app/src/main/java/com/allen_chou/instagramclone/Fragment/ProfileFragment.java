package com.allen_chou.instagramclone.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen_chou.instagramclone.Adapter.MyPostAdapter;
import com.allen_chou.instagramclone.EditProfileActivity;
import com.allen_chou.instagramclone.FollowersActivity;
import com.allen_chou.instagramclone.Model.Post;
import com.allen_chou.instagramclone.Model.User;
import com.allen_chou.instagramclone.OptionsActivity;
import com.allen_chou.instagramclone.R;
import com.allen_chou.instagramclone.Util.CommonUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private ImageView imageProfile, imageOptions;
    private TextView textNickName, textPosts, textFollower, textFollowing, textUserName, textBio;
    private Button buttonEditProfile;
    private ImageButton imageButtonGrid, imageButtonSave;

    private FirebaseUser firebaseUser;
    private String profileId;

    private List<Post> postList;
    private MyPostAdapter myPostAdapter;
    private RecyclerView recyclerView;

    private List<Post> postListSaves;
    private MyPostAdapter myPostAdapterSaves;
    private RecyclerView recyclerViewSaves;
    private List<String> mySaves;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileId = preferences.getString("profileId", "none");

        findViews(view);
        getData();
        return view;
    }

    private void findViews(View view) {
        imageProfile = view.findViewById(R.id.image_profile);
        imageOptions = view.findViewById(R.id.image_options);
        textNickName = view.findViewById(R.id.text_nick_name);
        textPosts = view.findViewById(R.id.text_posts);
        textFollower = view.findViewById(R.id.text_followers);
        textFollowing = view.findViewById(R.id.text_following);
        textUserName = view.findViewById(R.id.text_user_name);
        textBio = view.findViewById(R.id.text_bio);
        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        imageButtonGrid = view.findViewById(R.id.image_button_grid);
        imageButtonSave = view.findViewById(R.id.image_button_save);

        buttonEditProfile.setOnClickListener(createEditProfileOnClickListener());
        imageButtonGrid.setOnClickListener(createButtonGridOnClickListener());
        imageButtonSave.setOnClickListener(createButtonSaveOnClickListener());
        textFollower.setOnClickListener(createFollowerIntentClickListener("followers"));
        textFollowing.setOnClickListener(createFollowerIntentClickListener("following"));
        imageOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });

        //recycler
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postList = new ArrayList<>();
        myPostAdapter = new MyPostAdapter(getContext(), postList);
        recyclerView.setAdapter(myPostAdapter);

        //recycler_saves
        recyclerViewSaves = view.findViewById(R.id.recycler_save);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postListSaves = new ArrayList<>();
        myPostAdapterSaves = new MyPostAdapter(getContext(), postListSaves);
        recyclerViewSaves.setAdapter(myPostAdapterSaves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);
    }

    private void getData() {
        userInfo();
        getFollowersAndFollowing();
        getPostsCount();
        getMyPost();
        getMySave();

        //when searchFragment Profile Click intent -> MainActivity -> ProfileFragment 的畫面差異(profileId 與 User不同)
        if (profileId.equals(firebaseUser.getUid())) {
            buttonEditProfile.setText(R.string.text_edit_profile);
        } else {
            checkFollow();
            imageButtonSave.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View.OnClickListener createEditProfileOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String button = buttonEditProfile.getText().toString();

                if (button.equals(getString(R.string.text_edit_profile))) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (button.equals(getString(R.string.text_follow))) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    CommonUtil.addNotificationsByFollow(profileId, "started following you");

                } else if (button.equals(getString(R.string.text_following))) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener createButtonGridOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewSaves.setVisibility(View.GONE);
            }
        };
    }

    @NonNull
    private View.OnClickListener createButtonSaveOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        };
    }

    @NonNull
    private View.OnClickListener createFollowerIntentClickListener(final String title) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        };
    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getContext()).load(user.getImageUrl()).into(imageProfile);
                textNickName.setText(user.getNickName());
                textBio.setText(user.getBio());
                textUserName.setText(user.getNickName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    buttonEditProfile.setText(R.string.text_following);
                } else {
                    buttonEditProfile.setText(R.string.text_follow);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersAndFollowing() {
        DatabaseReference referenceFollowers = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileId).child("followers");

        referenceFollowers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textFollower.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference referenceFollowing = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileId).child("following");

        referenceFollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textFollowing.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)) {
                        i++;
                    }
                }

                textPosts.setText(i + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMyPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMySave() {
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mySaves.add(snapshot.getKey());
                    Log.d(TAG, "onDataChange: " + snapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListSaves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mySaves) {
                        if (post.getPostId().equals(id)) {
                            postListSaves.add(post);
                        }
                    }
                    myPostAdapterSaves.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
