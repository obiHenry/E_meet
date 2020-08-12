package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView clickPostImage;
    private TextView clickPostDescription;
    private Button clickPostEdit, clickPostDelete;
    private DatabaseReference clickPostref;
    private FirebaseAuth firebaseAuth;
    String currentUserId, databaseUserId, description, postImage ;

     String Postkey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);



        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        Postkey = getIntent().getExtras().get("postKey").toString();
        clickPostref = FirebaseDatabase.getInstance().getReference().child("posts").child(Postkey);

        clickPostImage = (ImageView) findViewById(R.id.click_post_image);
        clickPostDescription = (TextView) findViewById(R.id.click_post_description);
        clickPostEdit = (Button) findViewById(R.id.click_post_edit);
        clickPostDelete = (Button) findViewById(R.id.click_post_delete);

        clickPostDelete.setVisibility(View.INVISIBLE);
        clickPostEdit.setVisibility(View.INVISIBLE);

        clickPostref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshot dataSnapshot) {

             if (dataSnapshot.exists()){
                 description = dataSnapshot.child("description").getValue().toString();
                 postImage = dataSnapshot.child("postImage").getValue().toString();
                 databaseUserId = dataSnapshot.child("uid").getValue().toString();



                 clickPostDescription.setText(description);
                 Picasso.with(ClickPostActivity.this).load(postImage).into(clickPostImage);

                 if (currentUserId.equals(databaseUserId)){
                     clickPostDelete.setVisibility(View.VISIBLE);
                     clickPostEdit.setVisibility(View.VISIBLE);

                 }

                 clickPostEdit.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         editCurrentPost(description);
                     }
                 });
             }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });




        clickPostDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentUserPost();
            }
        });
    }

    private void editCurrentPost(String description) {

        final AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
        setTitle("EditPost");

        final EditText inputField = new EditText(this);
        inputField.setText(description);
        alertdialog.setView(inputField);

        alertdialog.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickPostref.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "post updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        alertdialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = alertdialog.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_bright);
    }

    private void deleteCurrentUserPost() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.delete_alert_layout,null);
        builder.setView(view);
        TextView textView = view.findViewById(R.id.confirmation);
        TextView textView1 = view.findViewById(R.id.question);
        Button btnOk = view.findViewById(R.id.ok);
        Button btnCancel = view.findViewById(R.id.cancel);
        final AlertDialog alertDialog = builder.create();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPostref.removeValue();
                sendUserTOMainActivity();
                alertDialog.dismiss();
                Toast.makeText(ClickPostActivity.this, "post deleted successfully", Toast.LENGTH_SHORT).show();


            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        alertDialog.show();

    }



    public void sendUserTOMainActivity(){
        Intent MainIntent = new Intent(ClickPostActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }
}
