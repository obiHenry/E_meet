package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonFriendActivity extends AppCompatActivity {

    private TextView userStatus ,userName, userFullName, userPhoneNumber, userCountry, userGender, userDOB, userRelationshipStatus;
    private CircleImageView userProfileImage;
    private Button sendRequestbtn, declineRequestbtn;

    private DatabaseReference findFriendsRequestRef, userRef, friendRef;
    private FirebaseAuth firebaseAuth;

    String  recieverUserId, CURRENT_STATE, senderUserId, saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_friend);


        firebaseAuth = FirebaseAuth.getInstance();
        senderUserId = firebaseAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        friendRef = FirebaseDatabase.getInstance().getReference().child("friends");
        findFriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("friendRequests");

        recieverUserId = getIntent().getExtras().get("visitUserId").toString();
        InitializeFields();

        userRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(PersonFriendActivity.this).load(myProfileImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userProfileImage);
                    userCountry.setText("Country: " + mycountry);
                    userDOB.setText( "DOB: " + myDOB);
                    userFullName.setText(myfullName);
                    userPhoneNumber.setText("Phone Number: " + myphoneNumber);
                    userGender.setText("Gender: " +mygender);
                    userRelationshipStatus.setText("Relationship status: " + myrelationshipStatus);
                    userName.setText("@"+myuserName);
                    userStatus.setText(mystatus);

                    ButtonMaintainance();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }    
        });

        declineRequestbtn.setVisibility(View.INVISIBLE);
        declineRequestbtn.setEnabled(false);

        if (!senderUserId.equals(recieverUserId)){

            sendRequestbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequestbtn.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){

                        sendFriendRequestToAFriend();

                    }else if (CURRENT_STATE.equals("request_sent")){

                        cancelFriendRequest();

                    }else if (CURRENT_STATE.equals("request_received")){

                        acceptFriendRequest();

                    }else if (CURRENT_STATE.equals("friends")){

                        unFriendThatPerson();
                    }
                }
            });

        }else {
            declineRequestbtn.setVisibility(View.INVISIBLE);
            sendRequestbtn.setVisibility(View.INVISIBLE);

        }
    }

    private void unFriendThatPerson() {

        friendRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            friendRef.child(recieverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                sendRequestbtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendRequestbtn.setText("send friend request");

                                                declineRequestbtn.setVisibility(View.INVISIBLE);
                                                declineRequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMMM-YYYY");
        saveCurrentDate =currentDate.format(callForDate.getTime());



        friendRef.child(senderUserId).child(recieverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful()){

                          friendRef.child(recieverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if (task.isSuccessful()){

                                              findFriendsRequestRef.child(senderUserId).child(recieverUserId)
                                                      .removeValue()
                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull Task<Void> task) {

                                                              if (task.isSuccessful()){

                                                                  findFriendsRequestRef.child(recieverUserId).child(senderUserId)
                                                                          .removeValue()
                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                              @Override
                                                                              public void onComplete(@NonNull Task<Void> task) {

                                                                                  if (task.isSuccessful()){

                                                                                      sendRequestbtn.setEnabled(true);
                                                                                      CURRENT_STATE = "friends";
                                                                                      sendRequestbtn.setText("unfriend this person");

                                                                                      Toast.makeText(PersonFriendActivity.this, "you are now friends with this person", Toast.LENGTH_SHORT).show();

                                                                                      declineRequestbtn.setVisibility(View.INVISIBLE);
                                                                                      declineRequestbtn.setEnabled(false);
                                                                                  }
                                                                              }
                                                                          });
                                                              }
                                                          }
                                                      });
                                          }
                                      }
                                  });
                      }
                    }
                });
    }


    private void cancelFriendRequest() {

        findFriendsRequestRef.child(senderUserId).child(recieverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    findFriendsRequestRef.child(recieverUserId).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        sendRequestbtn.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        sendRequestbtn.setText("send friend request");

                                        declineRequestbtn.setVisibility(View.INVISIBLE);
                                        declineRequestbtn.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void ButtonMaintainance() {

        findFriendsRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserId)){

                    String request_type = dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                    if (request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        sendRequestbtn.setText("cancel friend request");

                        declineRequestbtn.setVisibility(View.INVISIBLE);
                        declineRequestbtn.setEnabled(false);

                    }else if (request_type.equals("received")){
                        CURRENT_STATE = "request_received";
                        sendRequestbtn.setText("accept friend request");

                        declineRequestbtn.setVisibility(View.VISIBLE);
                        declineRequestbtn.setEnabled(true);

                        declineRequestbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        friendRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(recieverUserId)){

                            CURRENT_STATE = "friends";
                            sendRequestbtn.setText("unfriend this person");

                            declineRequestbtn.setVisibility(View.INVISIBLE);
                            declineRequestbtn.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendFriendRequestToAFriend() {

        findFriendsRequestRef.child(senderUserId).child(recieverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    findFriendsRequestRef.child(recieverUserId).child(senderUserId)
                            .child("request_type").setValue("received")

                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        sendRequestbtn.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        sendRequestbtn.setText("cancel friend request");

                                        declineRequestbtn.setVisibility(View.INVISIBLE);
                                        declineRequestbtn.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void InitializeFields() {

        userStatus = (TextView)findViewById(R.id.person_status);
        userName = (TextView)findViewById(R.id.person_username);
        userFullName = (TextView)findViewById(R.id.person_name);
        userPhoneNumber = (TextView)findViewById(R.id.person_phone_number);
        userCountry = (TextView)findViewById(R.id.person_country);
        userGender = (TextView)findViewById(R.id.person_gender);
        userDOB = (TextView)findViewById(R.id.person_dob);
        userRelationshipStatus = (TextView)findViewById(R.id.person_relationshipstatus);
        userProfileImage = (CircleImageView)findViewById(R.id.person_pic);
        sendRequestbtn = (Button) findViewById(R.id.person_sendFriendRequest);
        declineRequestbtn = (Button)findViewById(R.id.person_cancelRequest);

        CURRENT_STATE = "not_friends";
    }
}
