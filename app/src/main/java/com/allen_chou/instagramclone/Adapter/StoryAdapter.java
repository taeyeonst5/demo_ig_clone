package com.allen_chou.instagramclone.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen_chou.instagramclone.AddStoryActivity;
import com.allen_chou.instagramclone.Model.Story;
import com.allen_chou.instagramclone.Model.User;
import com.allen_chou.instagramclone.R;
import com.allen_chou.instagramclone.StoryActivity;
import com.allen_chou.instagramclone.Util.CommonUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private static final String TAG = StoryAdapter.class.getSimpleName();
    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    /**
     * position 0 = AddStoryItem
     * position 1 = StoryItem
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == 0) {
            return new StoryViewHolder(LayoutInflater.from(mContext).inflate(R.layout.add_story_item_row, viewGroup, false));
        } else {
            return new StoryViewHolder(LayoutInflater.from(mContext).inflate(R.layout.story_item_row, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final StoryViewHolder storyViewHolder, int i) {
        final Story story = mStory.get(i);

        userInfo(story.getUserId(), storyViewHolder, i);

        if (storyViewHolder.getAdapterPosition() != 0) {
            seenStory(story.getUserId(), storyViewHolder);
        }

        if (storyViewHolder.getAdapterPosition() == 0) {
            myStory(storyViewHolder.storyAddText, storyViewHolder.storyAdd, false);
        }

        storyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storyViewHolder.getAdapterPosition() == 0) {
                    myStory(storyViewHolder.storyAddText, storyViewHolder.storyAdd, true);
                } else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userId", story.getUserId());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    private void userInfo(final String userId, final StoryViewHolder viewHolder, final int position) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(viewHolder.storyPhoto);

                if (position != 0) {
                    Glide.with(mContext).load(user.getImageUrl()).into(viewHolder.storyPhotoSeen);
                    viewHolder.storyUserNameText.setText(user.getNickName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final Boolean click) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long currentTime = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (isStoryInTime(currentTime, story)) {
                        count++;
                    }
                }
                if (click) {
                    if (count > 0) {
                        CommonUtil.alertDialog(mContext, "View story", "Add Story"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(mContext, StoryActivity.class);
                                        intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        mContext.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mContext.startActivity(new Intent(mContext, AddStoryActivity.class));
                                        dialogInterface.dismiss();
                                    }
                                });
                    } else {
                        mContext.startActivity(new Intent(mContext, AddStoryActivity.class));
                    }
                } else {
                    if (count > 0) {
                        textView.setText("My story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(String userId, final StoryViewHolder viewHolder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeEnd()
                            ) {
                        i++;
                    }
                }
                viewHolder.storyPhoto.setVisibility(i > 0 ? View.VISIBLE : View.GONE);
                viewHolder.storyPhotoSeen.setVisibility(i > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isStoryInTime(long currentTime, Story story) {
        return currentTime > story.getTimeStart() && currentTime < story.getTimeEnd();
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView storyPhoto, storyAdd, storyPhotoSeen;
        public TextView storyUserNameText, storyAddText;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyPhoto = itemView.findViewById(R.id.story_photo);
            storyAdd = itemView.findViewById(R.id.story_add);
            storyPhotoSeen = itemView.findViewById(R.id.story_photo_seen);
            storyUserNameText = itemView.findViewById(R.id.story_user_name);
            storyAddText = itemView.findViewById(R.id.text_add_story);
        }
    }
}
