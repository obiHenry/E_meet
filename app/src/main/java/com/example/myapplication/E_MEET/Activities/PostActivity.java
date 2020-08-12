package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    public static final int galleryPick = 1;
    private Uri imageUri;
    private Toolbar postToolbar;
    private ImageButton AddPhoto;
    private EditText AddPost;
    private Button UpdateButton;
    private String description;
    private StorageReference postImageReference;
    private String saveCurrentTime, saveCurrentDate, postRandomName, downloadUrl, currentUserId;
    private DatabaseReference userRef, postRef;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private long postCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        postToolbar = (Toolbar)findViewById(R.id.updatePostPageToolBar);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update post");


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        postImageReference = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        postRef = FirebaseDatabase.getInstance().getReference().child("posts");
        progressDialog = new ProgressDialog(this);


        AddPhoto = findViewById(R.id.inputPhoto);
        AddPost = findViewById(R.id.updateText);
        UpdateButton = findViewById(R.id.updatePost);

        AddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });
    }

    private void validatePostInfo() {

        description = AddPost.getText().toString();
        if (imageUri == null){
            Toast.makeText(this, "please select an image.. ", Toast.LENGTH_LONG).show();
        }else if (description.isEmpty()){
            Toast.makeText(this, "please something about your image.. ", Toast.LENGTH_LONG).show();
        }else {


            progressDialog.setTitle("adding new post");
            progressDialog.setMessage("please wait, while we update your post");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }
    }

    private void storeImageToFirebaseStorage() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMMM-YYYY");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime =currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate +saveCurrentTime;

        final StorageReference filePath = postImageReference.child("post Image").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getTask().isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            saveImagetoDatabase();
                        }
                    });
                }
            }
        });


    }

    private void saveImagetoDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    postCount = dataSnapshot.getChildrenCount();
                }else {
                    postCount = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullName").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileImages").getValue().toString();


                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserId);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("description", description);
                    postMap.put("postImage", downloadUrl);
                    postMap.put("profileImages", userProfileImage );
                    postMap.put("fullName", userFullName );
                    postMap.put("counter",postCount);

                    postRef.child(currentUserId + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){
                                        progressDialog.dismiss();
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "new Post is updated Successfully ", Toast.LENGTH_SHORT).show();
                                    }else {
                                        String message = task.getException().getMessage();
                                        progressDialog.dismiss();
                                        Toast.makeText(PostActivity.this, "Error occurred while updating ur post:  " + message, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void openGallery() {

        Intent GalleryIntent = new Intent();
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        GalleryIntent.setType("image/*");
        startActivityForResult(GalleryIntent,galleryPick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){

            imageUri = data.getData();
            AddPhoto.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id ==  android.R.id.home){

            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendUserToMainActivity(){
        Intent MainIntent = new Intent(PostActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }
}
