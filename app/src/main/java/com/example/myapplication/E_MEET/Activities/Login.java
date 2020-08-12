package com.example.myapplication.E_MEET.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myapplication.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    private EditText userEmail;
    private EditText password;
    private TextView forgotPassword;
    private Button createAccount;
    private Button signIn;
    private ImageView googgleSignButton;

    public static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mSignInClient;
    public static final String TAG = "loginActivity";

    private ProgressDialog progressDialog;

    DatabaseReference databaseReference;

    private Boolean EmailAdressChecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userEmail = (EditText)findViewById(R.id.Loginname);
        password = (EditText)findViewById(R.id.password);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        createAccount = (Button) findViewById(R.id.account);
        signIn = (Button)findViewById(R.id.signIn);
        googgleSignButton = (ImageView) findViewById(R.id.goggle_sign) ;

        progressDialog = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,CreateAccount.class));
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendToforgotPasswordActivity();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUsersDetailsToLogin();

            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mSignInClient = GoogleSignIn.getClient(this, gso);


        googgleSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser User = firebaseAuth.getCurrentUser();
        if (User != null){
            sendUserTOMainActivity();
            checkUsersExistence();
        }
    }

    private void checkUsersExistence() {
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)) {

                    sendToProfileActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkUsersDetailsToLogin (){
        String Useremail = userEmail.getText().toString();
        String Password = password.getText().toString();

        if (Useremail.isEmpty()|| Password.isEmpty()){
            Toast.makeText(this, "none of the fields should be empty.", Toast.LENGTH_LONG).show();

        }else {
            progressDialog.setTitle("Logging in");
            progressDialog.setMessage("wait while we check ur credentials ");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            firebaseAuth.signInWithEmailAndPassword(Useremail,Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                verifyEmailAddress();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(Login.this, "error occurred:"+message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
    }



    private void signIn() {
        // Launches the sign in flow, the result is returned in onActivityResult
        Intent intent = mSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {


            progressDialog.setTitle("google sign in");
            progressDialog.setMessage("wait while we allow u login with your google account ");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()){

                progressDialog.dismiss();
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
                Toast.makeText(this, "please wait while we get ur authentication result", Toast.LENGTH_SHORT).show();
            }
            else {
                progressDialog.dismiss();
                Toast.makeText(this, "we can't get ur authentication result", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserTOMainActivity();

                        } else {

                            progressDialog.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            Toast.makeText(Login.this, "not authenticated try again : " + message, Toast.LENGTH_SHORT).show();
                            sendUserToLoginActivity();

                        }

                    }
                });
    }


    private void verifyEmailAddress()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        EmailAdressChecker = user.isEmailVerified();

        if (EmailAdressChecker){
            sendUserTOMainActivity();

        }else {

            Toast.makeText(this, "please go to your email account verify your email first...", Toast.LENGTH_LONG).show();
            firebaseAuth.signOut();
        }
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(Login.this,Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void sendToProfileActivity(){
        Intent intent = new Intent(Login.this, SetUpActivity.class);
        startActivity(intent);

    }

    private void sendToforgotPasswordActivity(){
        Intent intent = new Intent(Login.this, ForgotPassword.class);
        startActivity(intent);

    }


    private void sendUserTOMainActivity(){
        Intent MainIntent = new Intent(Login.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }


}
