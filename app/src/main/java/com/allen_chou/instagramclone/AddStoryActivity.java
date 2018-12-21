package com.allen_chou.instagramclone;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.allen_chou.instagramclone.Util.CommonUtil;
import com.allen_chou.instagramclone.Util.ProgressDialogUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private Uri imageUri;
    String myUrl = "";
    private StorageTask storageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("story");

        CropImage.activity()
                .setAspectRatio(9, 16)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = CropImage.getActivityResult(data).getUri();

            publishStory();
        } else {
            Toast.makeText(this, "something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }
    }

    private void publishStory() {
        ProgressDialogUtil.showProgressDialog(this, "Posting");

        if (imageUri != null) {
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                    + "." + CommonUtil.getFileExtension(imageUri.toString()));
            storageTask = imageReference.putFile(imageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myId);
                        String storyId = reference.push().getKey();
                        long timeEnd = System.currentTimeMillis() + 86400000; //24 hours

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", myUrl);
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyId", storyId);
                        hashMap.put("userId", myId);

                        reference.child(storyId).setValue(hashMap);
                        ProgressDialogUtil.disMissProgressDialog();
                        finish();
                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
