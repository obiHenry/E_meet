package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.E_MEET.Model.Friends;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendsList;
    private DatabaseReference friendsRef, userRef;
    private FirebaseAuth firebaseAut;
    private  FirebaseRecyclerAdapter<Friends, FriendsViewHoldeer> adapter;
    String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        firebaseAut = FirebaseAuth.getInstance();
        currentUserId = firebaseAut.getCurrentUser().getUid();

        friendsList = (RecyclerView)findViewById(R.id.all_friends_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        friendsList.setLayoutManager(llm);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("users");


        DisplayAllFriends();
    }



       private void updateUserStatus(String state){

        String saveCurrentTime, saveCurrentDate;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMMM dd, YYYY");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime  = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a ");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map lastseenMap = new HashMap();
        lastseenMap.put("time", saveCurrentTime);
        lastseenMap.put("date", saveCurrentDate);
        lastseenMap.put("type", state);

        userRef.child(currentUserId).child("userState")
                .updateChildren(lastseenMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateUserStatus("offline");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }


    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friends>options =
                new FirebaseRecyclerOptions.Builder<Friends>()
               .setQuery(friendsRef, Friends.class)
                .build();



        FirebaseRecyclerAdapter<Friends, FriendsViewHoldeer> adapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHoldeer>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHoldeer holder, int position, @NonNull final Friends model)
            {

                holder.setDate(model.getDate());

               final String usersId  = getRef(position).getKey();

                userRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            final String username = dataSnapshot.child("fullName").getValue().toString();
                            String profileImage = dataSnapshot.child("profileImages").getValue().toString();


                            holder.setFullName(username);
                            holder.setProfileImages(getApplicationContext(),profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence option [] = new CharSequence[]{

                                            username  +"'s" + " profile",
                                            "send message to this person"
                                    };

                                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(FriendsActivity.this);
                                    alertdialog.setTitle("select option");

                                    alertdialog.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0){
                                                Intent intent = new Intent(FriendsActivity.this,PersonFriendActivity.class);
                                                intent.putExtra("visitUserId",usersId);
                                                startActivity(intent);

                                            }else if (i == 1){

                                                Intent intent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                intent.putExtra("visitUserId",usersId);
                                                intent.putExtra("username",username);
                                                startActivity(intent);
                                            }
                                        }
                                    });

                                    alertdialog.show();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public FriendsViewHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


                View  view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_user_display_layout,parent,false);
                return new FriendsViewHoldeer(view);


            }
        };

        friendsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class FriendsViewHoldeer extends RecyclerView.ViewHolder {


        public FriendsViewHoldeer(@NonNull View itemView) {
            super(itemView);
        }

        public void setProfileImages(Context context, String profileImages) {
            CircleImageView profileimage = (CircleImageView) itemView.findViewById(R.id.all_user_display_profileImage);
            Picasso.with(context).load(profileImages).into(profileimage);
        }

        public void setFullName(String fullName) {
            TextView Description = (TextView)itemView.findViewById(R.id.all_user_display_fullname);
            Description.setText(fullName);
        }

        public void setDate(String date) {
            TextView Date = (TextView)itemView.findViewById(R.id.all_user_display_status);
            Date.setText("friends since : " + date);
        }
    }
}
