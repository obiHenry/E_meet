package com.example.myapplication.E_MEET.Adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.E_MEET.Model.Messages;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> MessageList;
    public FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    public  MessageAdapter(List<Messages> MessageList){

        this.MessageList = MessageList;

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView ReceiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {

            super(itemView);

            senderMessageText = (TextView)itemView.findViewById(R.id.sender_messsge);
            receiverMessageText = (TextView)itemView.findViewById(R.id.receivers_message);
            ReceiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_display_layout,parent,false);

        firebaseAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String SenderId =firebaseAuth.getCurrentUser().getUid();
        Messages messages = MessageList.get(position);

        String  userId = messages.getFrom();
        String type = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){


                    String image = dataSnapshot.child("profileImages").getValue().toString();
                    Picasso.with(holder.ReceiverProfileImage.getContext()).load(image)
                            .placeholder(R.drawable.round_profile_imageplaceholder).into(holder.ReceiverProfileImage);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (type.equals("text")) {

            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.ReceiverProfileImage.setVisibility(View.INVISIBLE);

            if (userId.equals(SenderId)) {

                holder.senderMessageText.setBackgroundResource(R.drawable.message_sender_background);
                holder.senderMessageText.setGravity(Gravity.LEFT);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setText(messages.getMessage());


            } else {

                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);


                holder.receiverMessageText.setBackgroundResource(R.drawable.message_receivers_background);
                holder.receiverMessageText.setGravity(Gravity.LEFT);
                holder.receiverMessageText.setTextColor(Color.WHITE);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        }


    }

    @Override
    public int getItemCount() {
        return MessageList.size();
    }


}
