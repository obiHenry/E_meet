package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.E_MEET.Adapter.MessageAdapter;
import com.example.myapplication.E_MEET.Model.Messages;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton messageImageFile, sendMessagebtn;
    private EditText messageInput;
    private RecyclerView messageList;
    String messageReceiversID, messageReceiversName, messageSenderId, saveCurrentTime, saveCurrentDate ;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootRef, userRef;

    private TextView customProfileName, UserLastSeen;
    private CircleImageView customProfileImage;

    private  final List<Messages> MessageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        messageReceiversID = getIntent().getExtras().get("visitUserId").toString();
        messageReceiversName = getIntent().getExtras().get("username").toString();
        messageSenderId  = firebaseAuth.getCurrentUser().getUid();


        InitializeFields();
        DisplayReceiverInfo();


        sendMessagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        fetchMessages();
    }

    private void fetchMessages(){

        RootRef.child("message").child(messageSenderId).child(messageReceiversID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){

                    Messages messages = dataSnapshot.getValue(Messages.class);
                    MessageList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendMessage() {

        updateUserStatus("online");

        String messageText = messageInput.getText().toString();
        
        if (messageText.isEmpty())
        {
            Toast.makeText(this, "please type a message first", Toast.LENGTH_SHORT).show();

        }else{

            String message_sender_ref =  "message/"+ messageSenderId  + "/" + messageReceiversID;
            String message_receiver_ref =  "message/"+ messageReceiversID  + "/" + messageSenderId;

            DatabaseReference user_message_key = RootRef.child("messages").child(messageSenderId).child(messageReceiversID).push();

            String message_push_id = user_message_key.getKey();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMMM-YYYY");
            saveCurrentDate =currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa" );
            saveCurrentTime =currentTime.format(callForTime.getTime());

            Map MessageMap = new HashMap();
            MessageMap.put("message",messageText);
            MessageMap.put("date",saveCurrentDate);
            MessageMap.put("time",saveCurrentTime);
            MessageMap.put("type","text");
            MessageMap.put("from",messageSenderId);


            Map MessageDetails  = new HashMap();
            MessageDetails.put(message_sender_ref  + "/" + message_push_id, MessageMap);
            MessageDetails.put(message_receiver_ref  + "/" + message_push_id, MessageMap);

            RootRef.updateChildren(MessageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()){

                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_LONG).show();
                        messageInput.setText("");

                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "error occurred : " + message, Toast.LENGTH_LONG).show();
                        messageInput.setText("");


                    }

                }
            });


        }
    }

    private void DisplayReceiverInfo() {

        customProfileName.setText(messageReceiversName);


        RootRef.child("users").child(messageReceiversID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){


                    String profileImage = dataSnapshot.child("profileImages").getValue().toString();

                    final String Statetype = dataSnapshot.child("userState").child("type").getValue().toString();
                    String lastSeenTime = dataSnapshot.child("userState").child("time").getValue().toString();
                    String lastSeenDate = dataSnapshot.child("userState").child("date").getValue().toString();



                    if (Statetype.equals("online")){

                        UserLastSeen.setText("online");
                    }else {

                        UserLastSeen.setText("Last Seen : " + lastSeenDate + " " + lastSeenTime);
                    }

                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.ic_person_black_24dp).into(customProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

        userRef.child(messageSenderId).child("userState")
                .updateChildren(lastseenMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

            }
        });
    }


    private void InitializeFields() {


        toolbar =(Toolbar)findViewById(R.id.messagesToolBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        messageImageFile = (ImageButton)findViewById(R.id.message_photo);
        sendMessagebtn = (ImageButton)findViewById(R.id.message_send);
        messageInput = (EditText) findViewById(R.id.message_text);


        customProfileImage = (CircleImageView)findViewById(R.id.custom_profile_image);
        customProfileName = (TextView)findViewById(R.id.custom_profile_name);
        UserLastSeen = (TextView)findViewById(R.id.custom_user_last_seen);

        messageAdapter = new MessageAdapter(MessageList);
        messageList = (RecyclerView) findViewById(R.id.messages_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(messageAdapter);
    }


}
