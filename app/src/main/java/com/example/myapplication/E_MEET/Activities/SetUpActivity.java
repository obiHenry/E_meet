package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;
    String currentUserId;

    private CircleImageView profilePics;
    private EditText UserName,FullName,CountryName,phoneNumber;
    private Button fullSignUp;
    final static int Gallery_pick = 1;
    private StorageReference  userProfileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        progressDialog = new ProgressDialog(this);
        profilePics  = findViewById(R.id.profilePicture);
        UserName = (EditText)findViewById(R.id.profileusername);
        FullName = (EditText)findViewById(R.id.profilefullname);
        CountryName = (EditText) findViewById(R.id.profilecountryname);
        fullSignUp = (Button)findViewById(R.id.fullsignUp);
        phoneNumber = (EditText)findViewById(R.id.profilephoneNummber);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("profileImages");
        fullSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccount();
            }
        });

        profilePics.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,Gallery_pick);
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public
            void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("profileImages")){
                        String image = dataSnapshot.child("profileImages").getValue().toString();
                        Picasso.with(SetUpActivity.this).load(image).placeholder(R.drawable.ic_person_black_24dp).into(profilePics);
                    }else {
                        Toast.makeText(SetUpActivity.this, "please select profile image first ", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity ()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK){
                    progressDialog.setTitle("SetUpActivity Image");
                    progressDialog.setMessage("wait while we update your profile image");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();


                    Uri resultUri = result.getUri();

                    final StorageReference filePath = userProfileRef.child(currentUserId + ".jpg");

                    filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getTask().isSuccessful()){
                                Toast.makeText(SetUpActivity.this, "profile stored to storage  successfully", Toast.LENGTH_SHORT).show();

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();
                                        reference.child("profileImages").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){
                                                            Intent intent = new Intent(SetUpActivity.this, SetUpActivity.class);
                                                            startActivity(intent);
                                                            Toast.makeText(SetUpActivity.this, "profile image stored to d database successfully ", Toast.LENGTH_LONG).show();
                                                            progressDialog.dismiss();
                                                        }else{
                                                            String message =  task.getException().getMessage();
                                                            Toast.makeText(SetUpActivity.this, "error occurred:" + message, Toast.LENGTH_LONG).show();
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }
                        }
                    });

//
//                    filePath.putFile (resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                            if (task.isSuccessful()){
//                                Toast.makeText(SetUpActivity.this, "profile stored to storage  successfully", Toast.LENGTH_LONG).show();
//                                progressDialog.dismiss();
//
//                                String downloadUri = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                                reference.child("profileImages").setValue(downloadUri)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                if (task.isSuccessful()){
//                                                    Intent intent = new Intent(SetUpActivity.this,SetUpActivity.class);
//                                                    startActivity(intent);
//                                                    Toast.makeText(SetUpActivity.this, "profile image stored to d database successfully ", Toast.LENGTH_LONG).show();
//                                                    progressDialog.dismiss();
//                                                }else{
//                                                    String message =  task.getException().getMessage();
//                                                    Toast.makeText(SetUpActivity.this, "error occurred:" + message, Toast.LENGTH_LONG).show();
//                                                    progressDialog.dismiss();
//                                                }
//                                            }
//                                        });
//                            }else{
//                                String message = task.getException().getMessage();
//                                Toast.makeText(SetUpActivity.this, "Error Occurred"+ message, Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                    });
                }

                else {
                    Toast.makeText(this, "Error ocurred: Image can't be cropped Try again", Toast.LENGTH_SHORT).show();
                }
            }
        }


    private void saveAccount() {
        String username = UserName.getText().toString();
        String fullName = FullName.getText().toString();
        String country = CountryName.getText().toString();
        String phoneNo = phoneNumber.getText().toString();

        if (username.isEmpty()||fullName.isEmpty()||country.isEmpty()){
            Toast.makeText( SetUpActivity.this,"none of the fields can be empty",Toast.LENGTH_LONG).show();
        }else{
            progressDialog.setTitle("setting up details");
            progressDialog.setMessage("wait while we validate");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username );
            userMap.put("fullName", fullName );
            userMap.put("country", country );
            userMap.put("phoneno", phoneNo );
            userMap.put("status", "am using e_app" );
            userMap.put("gender", "none" );
            userMap.put("dob", "none" );
            userMap.put("relationshipStatus", "none" );
            reference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetUpActivity.this, "Account setup successfully", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetUpActivity.this, "error ocurred: "+message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }



    private void SendUserToMainActivity() {        Intent intent = new Intent(SetUpActivity.this,MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
