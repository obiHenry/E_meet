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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.E_MEET.Model.FindFriends;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText searchInputText;
    private ImageButton searchButton;
    private RecyclerView searchResultList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference findFriendsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        toolbar = (Toolbar)findViewById(R.id.findfriendsToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findFriendsRef = FirebaseDatabase.getInstance().getReference().child("users");

        searchInputText = (EditText)findViewById(R.id.text_search_box);
        searchButton = (ImageButton) findViewById(R.id.search_friends_btn);
        searchResultList = (RecyclerView) findViewById(R.id.find_friends_recyclerView);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String SearchText = searchInputText.getText().toString();

                searchForFriends(SearchText);
            }
        });
    }

    private void searchForFriends(String searchText) {

        Toast.makeText(this, "Searching....", Toast.LENGTH_LONG).show();

        Query searchForFriendsQuery  = findFriendsRef.orderByChild("fullName")
                .startAt(searchText).endAt(searchText + "\uf8ff");


        FirebaseRecyclerOptions<FindFriends>options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchForFriendsQuery,FindFriends.class)
                .build();


        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {

                holder.setStatus(model.getStatus());
                holder.setFullName(model.getFullName());
                holder.setProfileImages(getApplicationContext(),model.getProfileImages());


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this,PersonFriendActivity.class);
                        intent.putExtra("visitUserId",visit_user_id);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_user_display_layout,parent,false);
                return new FindFriendsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static  class  FindFriendsViewHolder extends RecyclerView.ViewHolder {

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);


        }

        public void setProfileImages(Context context,  String profileImages) {
            CircleImageView profileimage = (CircleImageView) itemView.findViewById(R.id.all_user_display_profileImage);
            Picasso.with(context).load(profileImages).into(profileimage);
        }

        public void setFullName(String fullName) {
            TextView Description = (TextView)itemView.findViewById(R.id.all_user_display_fullname);
            Description.setText(fullName);
        }

        public void setStatus(String status) {
            TextView Status = (TextView)itemView.findViewById(R.id.all_user_display_status);
            Status.setText(status);
        }
    }
}
