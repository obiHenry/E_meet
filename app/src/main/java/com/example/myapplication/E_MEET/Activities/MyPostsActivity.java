package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.E_MEET.Model.Posts;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.acl.LastOwnerException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView myPostList;
    private DatabaseReference PostsRef, likeRef, userRef;
    private FirebaseAuth firebaseAuth;
    String currentUserId;

    Boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        toolbar =(Toolbar)findViewById(R.id.myPostToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myPostList = (RecyclerView)findViewById(R.id.my_all_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        myPostList.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        likeRef = FirebaseDatabase.getInstance().getReference().child("likes");



    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllMyPost();
    }

    private void DisplayAllMyPost() {

        Query PostQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff");

        FirebaseRecyclerOptions<Posts>options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(PostQuery, Posts.class)
                .build();


        FirebaseRecyclerAdapter<Posts,MyPostviewHolder> adapter = new FirebaseRecyclerAdapter<Posts, MyPostviewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostviewHolder holder, int position, @NonNull Posts model) {


                final String postkey  = getRef(position).getKey();

                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setFullName(model.getFullName());
                holder.setTime(model.getTime());
                holder.setPostImage(getApplicationContext(),model.getPostImage());
                holder.setProfileImage(getApplicationContext(), model.getProfileImage());


                holder.setLikeButtonStatus(postkey);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent ClickIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        ClickIntent.putExtra("postKey", postkey);
                        startActivity(ClickIntent);
                    }
                });



                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent commentIntent = new Intent(MyPostsActivity.this, CommentActivity.class);
                        commentIntent.putExtra("postKey", postkey);
                        startActivity(commentIntent);
                    }
                });


                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
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
            public MyPostviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_post_layout,parent,false);
                return new MyPostviewHolder(view);
            }
        };

        myPostList.setAdapter(adapter);
        adapter.startListening();
    }


    public static  class MyPostviewHolder extends RecyclerView.ViewHolder {

        ImageButton likePostButton, commentPostButton;
        TextView textlikeDisplay;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;
        public MyPostviewHolder(@NonNull View itemView) {

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


        public void setProfileImage(Context context, String profileImage){
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
}
