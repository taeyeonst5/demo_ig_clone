package com.allen_chou.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.allen_chou.instagramclone.CommentActivity;
import com.allen_chou.instagramclone.FollowersActivity;
import com.allen_chou.instagramclone.Fragment.PostDetailFragment;
import com.allen_chou.instagramclone.Fragment.ProfileFragment;
import com.allen_chou.instagramclone.MainActivity;
import com.allen_chou.instagramclone.Model.Post;
import com.allen_chou.instagramclone.Model.User;
import com.allen_chou.instagramclone.R;
import com.allen_chou.instagramclone.Util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private static final String TAG = PostAdapter.class.getSimpleName();
    private Context mContext;
    private List<Post> mPosts;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item_row, viewGroup, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostHolder postHolder, int i) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPosts.get(i);
        Glide.with(mContext).load(post.getPostImage())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(postHolder.imagePost);
        if (post.getPostDescription().equals("")) {
            postHolder.textDescription.setVisibility(View.GONE);
        } else {
            postHolder.textDescription.setVisibility(View.VISIBLE);
            postHolder.textDescription.setText(post.getPostDescription());
        }

        publisherInfo(postHolder, post);
        isLiked(post.getPostId(), postHolder.imageLike);
        setLikesCount(post.getPostId(), postHolder.textLikes);
        getComments(post.getPostId(), postHolder.textComments);
        isSaved(post.getPostId(), postHolder.imageSave);

        postHolder.imageLike.setOnClickListener(createOnClickListener(postHolder, post));
        postHolder.imageSave.setOnClickListener(createSaveOnClickListener(postHolder, post));

        postHolder.imageComment.setOnClickListener(createIntentOnClickListener(post));
        postHolder.textComments.setOnClickListener(createIntentOnClickListener(post));
        postHolder.textLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostId());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        postHolder.imageProfile.setOnClickListener(createProfileFragmentOnClickListener(post));
        postHolder.textNickName.setOnClickListener(createProfileFragmentOnClickListener(post));
        postHolder.textPublisher.setOnClickListener(createProfileFragmentOnClickListener(post));
        postHolder.imagePost.setOnClickListener(createPostDetailFragmentOnClickListener(post));

        postHolder.imageMore.setOnClickListener(createPopupMenuListener(post));
    }

    @NonNull
    private View.OnClickListener createProfileFragmentOnClickListener(final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        };
    }

    @NonNull
    private View.OnClickListener createPostDetailFragmentOnClickListener(final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", post.getPostId());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
        };
    }

    @NonNull
    private View.OnClickListener createPopupMenuListener(final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                editPost(post.getPostId());
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(mContext, "post deleted!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                return true;
                            case R.id.report:
                                Toast.makeText(mContext, "report clicked", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        };
    }

    @NonNull
    private View.OnClickListener createOnClickListener(@NonNull final PostHolder postHolder, final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postHolder.imageLike.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes")
                            .child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);

                    CommonUtil.addNotifications(post.getPublisher(), "liked your post", post.getPostId(), true);
                } else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Likes")
                            .child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener createSaveOnClickListener(@NonNull final PostHolder postHolder, final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postHolder.imageSave.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).removeValue();
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener createIntentOnClickListener(final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(CommentActivity.POST_ID, post.getPostId());
                intent.putExtra(CommentActivity.PUBLISHER_ID, post.getPublisher());
                mContext.startActivity(intent);
            }
        };
    }

    private void isLiked(String postId, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikesCount(String postId, final TextView likes) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(String postId, final TextView comments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(@NonNull final PostHolder postHolder, Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(post.getPublisher());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext.getApplicationContext()).load(user.getImageUrl()).into(postHolder.imageProfile);
                postHolder.textNickName.setText(user.getNickName());
                postHolder.textPublisher.setText(user.getNickName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postId, final ImageView imageView) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editPost(final String postId) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit Post");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        getDescription(postId, editText);

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("postDescription", editText.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postId).updateChildren(hashMap);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void getDescription(String postId, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getPostDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile, imagePost, imageLike, imageComment, imageSave, imageMore;
        TextView textNickName, textLikes, textComments, textDescription, textPublisher;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            imagePost = itemView.findViewById(R.id.post_image);
            imageLike = itemView.findViewById(R.id.like_image);
            imageComment = itemView.findViewById(R.id.comment_image);
            imageSave = itemView.findViewById(R.id.save_image);
            textNickName = itemView.findViewById(R.id.nick_name_text);
            textLikes = itemView.findViewById(R.id.like_text_view);
            textComments = itemView.findViewById(R.id.comment_text_view);
            textDescription = itemView.findViewById(R.id.description_text_view);
            textPublisher = itemView.findViewById(R.id.publisher_text_view);
            imageMore = itemView.findViewById(R.id.image_more);
        }
    }
}
