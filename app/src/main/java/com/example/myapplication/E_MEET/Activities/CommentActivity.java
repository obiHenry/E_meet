package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.E_MEET.Model.Comments;
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
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    private EditText commentInputField;
    private ImageButton commentSendButton;
    private RecyclerView commentList;
    private String post_key , currentUserId , saveCurrentDate, saveCurrentTime, RandomKey;

    private DatabaseReference UserRef,postRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        firebaseAuth = FirebaseAuth.getInstance();
        post_key = getIntent().getExtras().get("postKey").toString();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        postRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post_key).child("comments");



        commentInputField = (EditText)findViewById(R.id.comment_input);
        commentSendButton = (ImageButton)findViewById(R.id.comment_input_button);
        commentList = (RecyclerView)findViewById(R.id.commentRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        commentList.setLayoutManager(llm);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);

        commentSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                UserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String Username = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(Username);
                            commentInputField.setText("");
                        }else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        DisplayComments();
    }

    private void DisplayComments() {

        FirebaseRecyclerOptions<Comments>options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(postRef,Comments.class)
                .build();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setUserName(model.getUsername());

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comment_layout,parent,false);
                return new CommentsViewHolder(view);
            }
        };

        recyclerAdapter.startListening();
        commentList.setAdapter(recyclerAdapter);

    }



    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

        }

        public void setComment(String comment){

            TextView commentText = (TextView)itemView.findViewById(R.id.comment_text);
            commentText.setText(comment);
        }

        public void setDate(String date) {

            TextView commentDate = (TextView)itemView.findViewById(R.id.comment_date);
            commentDate.setText(date);
        }

        public void setTime(String time) {

            TextView commentTime = (TextView)itemView.findViewById(R.id.comment_time);
            commentTime.setText(time);
        }

        public void setUserName(String username){

            TextView commentUsername = (TextView)itemView.findViewById(R.id.comment_username);
            commentUsername.setText("@"+username);
        }
    }

    private void ValidateComment(String Username) {

        String commentText = commentInputField.getText().toString();
        if (commentText.isEmpty()){

            Toast.makeText(this, "comment space is empty,  please write something", Toast.LENGTH_LONG).show();
        }else {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMMM-YYYY");
            saveCurrentDate =currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
            saveCurrentTime =currentTime.format(callForTime.getTime());

            RandomKey = currentUserId + saveCurrentDate +saveCurrentTime;

            HashMap commentHashMap = new HashMap();
            commentHashMap.put("comment",commentText);
            commentHashMap.put("uid",currentUserId);
            commentHashMap.put("time",saveCurrentTime);
            commentHashMap.put("date",saveCurrentDate);
            commentHashMap.put("username",Username);

            postRef.child(RandomKey).updateChildren(commentHashMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CommentActivity.this, "u have commented successfully", Toast.LENGTH_LONG).show();
                            }else {
                                String message = task.getException().getMessage();
                                Toast.makeText(CommentActivity.this, "Error Occurred : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }
    }


}
