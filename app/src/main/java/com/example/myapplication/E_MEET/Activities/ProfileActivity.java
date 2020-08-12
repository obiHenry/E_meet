package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userStatus ,userName, userFullName, userPhoneNumber, userCountry, userGender, userDOB, userRelationshipStatus;
    private CircleImageView userProfileImage;

    private DatabaseReference profileref, friendsRef, postsRef;
    private FirebaseAuth firebaseAuth;

    private Button myPosts, myFriends;

    String currentUserId;
    private int friendsCount,  postsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        profileref = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");

        userStatus = (TextView)findViewById(R.id.profile_status);
        userName = (TextView)findViewById(R.id.profile_username);
        userFullName = (TextView)findViewById(R.id.profile_name);
        userPhoneNumber = (TextView)findViewById(R.id.profile_phone_number);
        userCountry = (TextView)findViewById(R.id.profile_country);
        userGender = (TextView)findViewById(R.id.profile_gender);
        userDOB = (TextView)findViewById(R.id.profile_dob);
        userRelationshipStatus = (TextView)findViewById(R.id.profile_relationshipstatus);
        userProfileImage = (CircleImageView)findViewById(R.id.profile_pic);

        myPosts = (Button) findViewById(R.id.profile_all_post_button);
        myFriends = (Button) findViewById(R.id.profile_friends_button);

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToFriendsActivity();
            }
        });


        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToMyPostsActivity();
            }
        });


        postsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                           postsCount = (int)dataSnapshot.getChildrenCount();
                           myPosts.setText(Integer.toString(postsCount) + " posts");

                        }else {

                            myPosts.setText(" 0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    friendsCount = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(Integer.toString(friendsCount) + "  Friends");
                }else {

                    myFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        profileref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage= dataSnapshot.child("profileImages").getValue().toString();
                    String mycountry= dataSnapshot.child("country").getValue().toString();
                    String myDOB= dataSnapshot.child("dob").getValue().toString();
                    String myfullName= dataSnapshot.child("fullName").getValue().toString();
                    String myphoneNumber= dataSnapshot.child("phoneno").getValue().toString();
                    String mygender= dataSnapshot.child("gender").getValue().toString();
                    String myrelationshipStatus= dataSnapshot.child("relationshipStatus").getValue().toString();
                    String myuserName= dataSnapshot.child("username").getValue().toString();
                    String mystatus= dataSnapshot.child("status").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userProfileImage);
                    userCountry.setText("Country: " + mycountry);
                    userDOB.setText( "DOB: " + myDOB);
                    userFullName.setText(myfullName);
                    userPhoneNumber.setText("Phone Number: " + myphoneNumber);
                    userGender.setText("Gender: " +mygender);
                    userRelationshipStatus.setText("Relationship status: " + myrelationshipStatus);
                    userName.setText("@"+myuserName);
                    userStatus.setText(mystatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToFriendsActivity() {
        Intent Intent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(Intent);

    }

    private void sendUserToMyPostsActivity() {
        Intent Intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(Intent);

    }
}
