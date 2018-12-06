package com.allen_chou.instagramclone.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen_chou.instagramclone.Fragment.PostDetailFragment;
import com.allen_chou.instagramclone.Fragment.ProfileFragment;
import com.allen_chou.instagramclone.Model.Notification;
import com.allen_chou.instagramclone.Model.Post;
import com.allen_chou.instagramclone.Model.User;
import com.allen_chou.instagramclone.R;
import com.allen_chou.instagramclone.Util.CommonUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {
    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_row, viewGroup, false);
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder notificationHolder, int i) {
        final Notification notification = mNotification.get(i);

        notificationHolder.textComment.setText(notification.getText());
        getUserInfo(notificationHolder.imageProfile, notificationHolder.textNickName, notification.getUserId());

        if (notification.getIsPost()) {
            notificationHolder.imagePost.setVisibility(View.VISIBLE);
            getPostImage(notificationHolder.imagePost, notification.getPostId());
        } else {
            notificationHolder.imagePost.setVisibility(View.GONE);
        }

        notificationHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.getIsPost()) {
                    CommonUtil.setSharedPrefs(mContext, "postId", notification.getPostId());

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PostDetailFragment()).commit();
                } else {
                    CommonUtil.setSharedPrefs(mContext, "profileId", notification.getUserId());

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class NotificationHolder extends RecyclerView.ViewHolder {

        CircleImageView imageProfile;
        ImageView imagePost;
        TextView textNickName, textComment;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            imagePost = itemView.findViewById(R.id.image_post);
            textNickName = itemView.findViewById(R.id.text_nick_name);
            textComment = itemView.findViewById(R.id.text_comment);
        }
    }

    private void getUserInfo(final ImageView profile, final TextView nickName, String publisherId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nickName.setText(user.getNickName());
                Glide.with(mContext.getApplicationContext()).load(user.getImageUrl()).into(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostImage(final ImageView imageView, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Glide.with(mContext.getApplicationContext()).load(post.getPostImage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
