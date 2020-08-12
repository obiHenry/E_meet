package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText email;
    private Button resetPasswordbtn;

    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        toolbar = (Toolbar)findViewById(R.id.forgotPasswordToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Recover Password");
        firebaseAuth = FirebaseAuth.getInstance();


        email = (EditText)findViewById(R.id.forgotPasswordemail);
        resetPasswordbtn = (Button) findViewById(R.id.recover);

        resetPasswordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                if (userEmail.isEmpty()){
                    Toast.makeText(ForgotPassword.this, "please input your username to recover account", Toast.LENGTH_SHORT).show();
                }else {

                    firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this, "please chechk your email to reset your password", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ForgotPassword.this,Login.class));
                            }else {
                                String message = task.getException().getMessage();
                                Toast.makeText(ForgotPassword.this, "Error Occurred : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
