package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText userStatus ,userName, userFullName, userPhoneNumber, userCountry, userGender, userDOB, userRelationshipStatus;
    private Button updateAccountBtn;
    private CircleImageView profileImageView;
    private StorageReference settingsProfileRef;
    private DatabaseReference settingsUserRef;
    private FirebaseAuth firebaseAuth;
    String currentUserId;

    private ProgressDialog progressDialog;

    final static int Gallery_pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar =(Toolbar)findViewById(R.id.settingsToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        settingsProfileRef = FirebaseStorage.getInstance().getReference().child("profileImages");


        userCountry = (EditText)findViewById(R.id.settingsUserCountry);
        userDOB = (EditText)findViewById(R.id.settingsUserDOB);
        userFullName = (EditText)findViewById(R.id.settingsUserFullName);
        userPhoneNumber = (EditText)findViewById(R.id.settingsUserPhoneNumber);
        userGender = (EditText)findViewById(R.id.settingsUserGender);
        userRelationshipStatus = (EditText)findViewById(R.id.settingsUserRelationshipStatus);
        userName = (EditText)findViewById(R.id.settingsUserName);
        userStatus = (EditText)findViewById(R.id.settingsUserStatus);
        updateAccountBtn = (Button) findViewById(R.id.settingsUpdateButton);
        profileImageView = (CircleImageView)findViewById(R.id.settingsProfileImage);


        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String myProfileImage= dataSnapshot.child("profileImages").getValue().toString();
                    String mycountry= dataSnapshot.child("country").getValue().toString();
                    String myDOB= dataSnapshot.child("dob").getValue().toString();
                    String myfullName= dataSnapshot.child("fullName").getValue().toString();
                    String myphoneNumber= dataSnapshot.child("phoneno").getValue().toString();
                    String mygender= dataSnapshot.child("gender").getValue().toString();
                    String myrelationshipStatus= dataSnapshot.child("relationshipStatus").getValue().toString();
                    String myuserName= dataSnapshot.child("username").getValue().toString();
                    String mystatus= dataSnapshot.child("status").getValue().toString();

                    Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(profileImageView);
                    userCountry.setText(mycountry);
                    userDOB.setText(myDOB);
                    userFullName.setText(myfullName);
                    userPhoneNumber.setText(myphoneNumber);
                    userGender.setText(mygender);
                    userRelationshipStatus.setText(myrelationshipStatus);
                    userName.setText(myuserName);
                    userStatus.setText(mystatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ValidateAccountInfo();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,Gallery_pick);
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

                final StorageReference filePath = settingsProfileRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getTask().isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "profile stored to storage  successfully", Toast.LENGTH_SHORT).show();

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    settingsUserRef.child("profileImages").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){
                                                        Intent intent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                                        startActivity(intent);
                                                        Toast.makeText(SettingsActivity.this, "profile image stored to d database successfully ", Toast.LENGTH_LONG).show();
                                                        progressDialog.dismiss();
                                                    }else{
                                                        String message =  task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "error occurred:" + message, Toast.LENGTH_LONG).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });

                                }
                            });
                        }
                    }
                });

            }

            else {
                Toast.makeText(this, "Error ocurred: Image can't be cropped Try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ValidateAccountInfo() {

        String UserName = userName.getText().toString();
        String FullName = userFullName.getText().toString();
        String Country = userCountry.getText().toString();
        String Dob = userDOB.getText().toString();
        String Gender = userGender.getText().toString();
        String PhoneNumber = userPhoneNumber.getText().toString();
        String RelationshipStatus = userRelationshipStatus.getText().toString();
        String Status = userStatus.getText().toString();

        if (TextUtils.isEmpty(UserName)){
            Toast.makeText(this, "please write your username", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(FullName)){
            Toast.makeText(this, "please write your full name", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(Country)){
            Toast.makeText(this, "please write your country name", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(Dob)){
            Toast.makeText(this, "please write your date of birth", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(Gender)){
            Toast.makeText(this, "please write your gender", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(PhoneNumber)){
            Toast.makeText(this, "please write your phone number", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(RelationshipStatus)){
            Toast.makeText(this, "please write your relationship status", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(Status)) {
            Toast.makeText(this, "please write your status", Toast.LENGTH_SHORT).show();

        }else {

            progressDialog.setTitle("profile image update");
            progressDialog.setMessage("wait while we update your profile image");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            updateAccountInfo(UserName,FullName,Country,Dob,Gender,PhoneNumber,RelationshipStatus,Status);
        }
    }

    private void updateAccountInfo(String userName, String fullName, String country, String dob, String gender, String phoneNumber, String relationshipStatus, String status) {

        HashMap hashMap = new HashMap();
        hashMap.put("username", userName);
        hashMap.put("fullName", fullName);
        hashMap.put("country", country);
        hashMap.put("dob", dob);
        hashMap.put("gender", gender);
        hashMap.put("phoneno", phoneNumber);
        hashMap.put("relationshipStatus", relationshipStatus);
        hashMap.put("status", status);
        settingsUserRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){

                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, " Account settings Updated successfully", Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }else {

                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error Occurred : " +message, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void sendUserToMainActivity(){
        Intent MainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }
}
