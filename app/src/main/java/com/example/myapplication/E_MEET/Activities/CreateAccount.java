package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccount extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText
    passWord,
    confirmPassword,
    email;
    Button createAccount;
    ProgressDialog progressDialog;
    Login login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        email = (EditText) findViewById(R.id.accountEmail);
        passWord = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirmpassword);
        createAccount =(Button) findViewById(R.id.signUp);
        progressDialog = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            sendUserToMainActivity();
        }
    }


    public void CreateAccount (){
        String Email = email.getText().toString();
        String Password = passWord.getText().toString();
        String ConfirmedPassword = confirmPassword.getText().toString();

        if (Email.isEmpty()|| Password.isEmpty()|| ConfirmedPassword.isEmpty()){

            Toast.makeText(this, "fields Can't be empty", Toast.LENGTH_SHORT).show();

        }else if (!Password.equals(ConfirmedPassword)){

            Toast.makeText(this, "confirmPassword do not match password please check and try again", Toast.LENGTH_SHORT).show();

        }else {

            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("please wait while we create your new Account");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                             if (task.isSuccessful()){
                                 sendEmailVerificationMessageToUser();
                                 Toast.makeText(CreateAccount.this, "Account created successfully", Toast.LENGTH_LONG).show();
                                 progressDialog.dismiss();

                             }else {
                                 String message = task.getException().getMessage();
                                 Toast.makeText(CreateAccount.this, "error occur"+ message, Toast.LENGTH_LONG).show();
                                 progressDialog.dismiss();

                             }
                        }
                    });

        }
    }


    private void sendEmailVerificationMessageToUser(){

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        Toast.makeText(CreateAccount.this, "registration successful  please check and verify your account", Toast.LENGTH_LONG).show();

                        sendEmailVerificationMessageToUser();
                        firebaseAuth.signOut();

                    }else {

                         String message = task.getException().getMessage();
                        Toast.makeText(CreateAccount.this, "error occurred : " + message, Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut();
                    }
                }
            });
        }
    }

    public void sendUserToMainActivity(){
        Intent CreateAccountIntent = new Intent(CreateAccount.this,MainActivity.class);
        CreateAccountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(CreateAccountIntent);
        finish();
    }

    public void SendToLoginActitvity(){

        Intent intent = new Intent(CreateAccount.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
