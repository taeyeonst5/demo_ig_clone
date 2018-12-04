package com.allen_chou.instagramclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen_chou.instagramclone.CommentActivity;
import com.allen_chou.instagramclone.MainActivity;
import com.allen_chou.instagramclone.Model.Comment;
import com.allen_chou.instagramclone.Model.User;
import com.allen_chou.instagramclone.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private Context mContext;
    private List<Comment> mComments;

    private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mComments) {
        this.mContext = mContext;
        this.mComments = mComments;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_row, viewGroup, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder commentHolder, int i) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Comment comment = mComments.get(i);

        commentHolder.textComment.setText(comment.getComment());
        getPubilsherInfo(comment.getPublisher(), commentHolder.imageProfile, commentHolder.textNickName);

        commentHolder.textComment.setOnClickListener(createIntentOnClickListener(comment));
        commentHolder.imageProfile.setOnClickListener(createIntentOnClickListener(comment));


    }

    @NonNull
    private View.OnClickListener createIntentOnClickListener(final Comment comment) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra(CommentActivity.PUBLISHER_ID, comment.getPublisher());
                mContext.startActivity(intent);
            }
        };
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textNickName, textComment;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            textNickName = itemView.findViewById(R.id.text_nick_name);
            textComment = itemView.findViewById(R.id.text_comment);

        }
    }

    private void getPubilsherInfo(String publisherId, final ImageView profile, final TextView nickName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(profile);
                nickName.setText(user.getNickName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
