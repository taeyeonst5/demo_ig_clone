package com.allen_chou.instagramclone.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.allen_chou.instagramclone.Fragment.PostDetailFragment;
import com.allen_chou.instagramclone.Model.Post;
import com.allen_chou.instagramclone.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyPostHolder> {

    private Context mContext;
    private List<Post> mPosts;

    public MyPostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public MyPostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.mypost_item_row, viewGroup, false);
        return new MyPostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostHolder myPostHolder, int i) {
        final Post post = mPosts.get(i);
        Glide.with(mContext).load(post.getPostImage()).into(myPostHolder.imageMyPost);
        myPostHolder.imageMyPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", post.getPostId());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class MyPostHolder extends RecyclerView.ViewHolder {
        ImageView imageMyPost;

        public MyPostHolder(@NonNull View itemView) {
            super(itemView);
            imageMyPost = itemView.findViewById(R.id.image_mypost);
        }
    }
}
