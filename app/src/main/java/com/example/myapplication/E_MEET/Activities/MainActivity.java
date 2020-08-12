package com.example.myapplication.E_MEET.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.E_MEET.Model.Posts;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private ImageButton addPostButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, postRef, likeRef;
    private FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter;

    private CircleImageView navprofileImage;
    private TextView navProfileUserName;
    RecyclerView recyclerView;
    String currentUserId;

    Boolean likeChecker = false;


    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.Maintoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        navigationView = findViewById(R.id.navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        addPostButton = (ImageButton)findViewById(R.id.add_new_post_button);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navprofileImage = (CircleImageView)navView.findViewById(R.id.profileImage) ;
        navProfileUserName = (TextView)navView.findViewById(R.id.userName);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        postRef = FirebaseDatabase.getInstance().getReference().child("posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("likes");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                PickItem(menuItem);
                return false;
            }
        });

        databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("username")){

                        String username = dataSnapshot.child("username").getValue().toString();
                        navProfileUserName.setText(username);
                    }

                    if (dataSnapshot.hasChild("profileImages")){

                        String profileimage = dataSnapshot.child("profileImages").getValue().toString();
                        Picasso.with(MainActivity.this).load(profileimage).placeholder(R.drawable.ic_person_black_24dp).into(navprofileImage);
                    }else {
                        Toast.makeText(MainActivity.this, "profile  name do not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToAddPostActivity();
            }
        });


    }


    public void updateUserStatus(String state){

        String saveCurrentTime, saveCurrentDate;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMMM dd, YYYY");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime  = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a ");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map  lastseenMap = new HashMap();
        lastseenMap.put("time", saveCurrentTime);
        lastseenMap.put("date", saveCurrentDate);
        lastseenMap.put("type", state);

        databaseReference.child(currentUserId).child("userState")
                .updateChildren(lastseenMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

            }
        });
    }

    public void displayAllUserPosts() {

        Query displayPostinDecendingOrder = postRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(displayPostinDecendingOrder, Posts.class)
                        .build();



        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@Nullable PostViewHolder postViewHolder, int position, Posts posts) {

                final String postkey  = getRef(position).getKey();
                postViewHolder.setDate(posts.getDate());
                postViewHolder.setDescription(posts.getDescription());
                postViewHolder.setFullName(posts.getFullName());
                postViewHolder.setTime(posts.getTime());
                postViewHolder.setPostImage(getApplicationContext(),posts.getPostImage());
                postViewHolder.setProfileImage(getApplicationContext(), posts.getProfileImage());

                postViewHolder.setLikeButtonStatus(postkey);

                postViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent ClickIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        ClickIntent.putExtra("postKey", postkey);
                        startActivity(ClickIntent);
                    }
                });



                postViewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent commentIntent = new Intent(MainActivity.this, CommentActivity.class);
                        commentIntent.putExtra("postKey", postkey);
                        startActivity(commentIntent);
                    }
                });


                postViewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likeChecker = true;

                        likeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (likeChecker.equals(true)){

                                    if (dataSnapshot.child(postkey).hasChild(currentUserId)){

                                        likeRef.child(postkey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    }else {
                                        likeRef.child(postkey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

                Log.d("my tag" , "onCreateViewHolder : initialized!");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout, parent, false);

                return new PostViewHolder(view);

            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");

    }


    public static class  PostViewHolder extends  RecyclerView.ViewHolder{

        ImageButton likePostButton, commentPostButton;
        TextView textlikeDisplay;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            likePostButton = (ImageButton)itemView.findViewById(R.id.post_dislike);
            commentPostButton = (ImageButton)itemView.findViewById(R.id.post_comment);
            textlikeDisplay = (TextView)itemView.findViewById(R.id.post_like_text);

            likesRef = FirebaseDatabase.getInstance().getReference().child("likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey){

            likesRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)){

                        countLikes = (int)  dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.ic_like);
                        textlikeDisplay.setText((Integer.toString(countLikes ) + ("  likes")));

                    }else {

                        countLikes = (int)  dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.ic_dislike);
                        textlikeDisplay.setText((Integer.toString(countLikes ) + ("  likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        public void setFullName(String fullName) {
            TextView Username = (TextView) itemView.findViewById(R.id.post_user_name);
            Username.setText(fullName);
        }


        public void setProfileImage( Context context, String profileImage){
            CircleImageView profileimage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            Picasso.with(context).load(profileImage).into(profileimage);
        }

        public void setDate(String date){
            TextView Date = (TextView)itemView.findViewById(R.id.post_date);
            Date.setText("   " + date);
        }

        public void setTime(String time) {
            TextView Time = (TextView)itemView.findViewById(R.id.post_time);
            Time.setText("   " + time);
        }

        public void setDescription(String description) {
            TextView Description = (TextView)itemView.findViewById(R.id.post_description);
            Description.setText(description);
        }

        public void setPostImage( Context context, String postImage){
            ImageView postimage = (ImageView)itemView.findViewById(R.id.post_image);
            Picasso.with(context).load(postImage).into(postimage);
        }

    }




    private void sendUserToAddPostActivity() {

        Intent AddPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(AddPostIntent);
    }



    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }


    public void PickItem(MenuItem menuItem){
        int id = menuItem.getItemId();

        if (id == R.id.menuAddNewPost){
            sendUserToAddPostActivity();

        }else if (id == R.id.home){

        }else if (id ==R.id.findfriends){
            sendUserToFindFriendsActivity();

        }else if(id == R.id.friends){

            sendUserToFriendsActivity();

        }else if(id == R.id.message){

            sendUserToFriendsActivity();


        }else if (id == R.id.action_settings) {
            sendTosettingsActivity();

        }else if (id == R.id.logout){
            updateUserStatus("offline");
            firebaseAuth.signOut();
            sendUserToLoginActivity();
        }else if (id == R.id.menuProfile){
           sendUserToProfileActivity();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void checkRecordExistence() {
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)) {
                    sendToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override

    protected void onStart() {

        super.onStart();
        displayAllUserPosts();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        }else{
            checkRecordExistence();
        }

    }

    private void sendToSetUpActivity() {
        Intent ProfileIntent = new Intent(MainActivity.this, SetUpActivity.class);
        ProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ProfileIntent);
        finish();

    }


    public  void sendUserToLoginActivity(){
        Intent LoginItent = new Intent(MainActivity.this,Login.class);
        LoginItent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginItent);

    }

    public  void sendUserToProfileActivity(){
        Intent Intent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(Intent);

    }


    private void sendUserToFriendsActivity() {
        Intent Intent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(Intent);

    }

    private void sendTosettingsActivity() {
        Intent Intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(Intent);

    }

    public  void sendUserToFindFriendsActivity(){
        Intent Intent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(Intent);

    }
    }
