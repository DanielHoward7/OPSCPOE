package com.dan.admin.opscpoe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    EditText email, pass;
    ProgressBar progressBar;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email_address);
        pass = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);


        mAuth = FirebaseAuth.getInstance();

//        firebaseAuth();
        findViewById(R.id.sign_up).setOnClickListener(this);
        findViewById(R.id.loginBtn).setOnClickListener(this);
    }

    private void loginUser(){

        String mail = email.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (mail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            pass.setError("Password is required");
            pass.requestFocus();
            return;
        }

        if (password.length() < 6) {
            pass.setError("Minimum length of password is 6 characters");
            pass.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    finish();
                    Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private void firebaseAuth(){
////
////        mAuthListener = new FirebaseAuth.AuthStateListener() {
////            @Override
////            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
////                FirebaseUser user = firebaseAuth.getCurrentUser();
////                if (user != null) {
////                    Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
////
//////                    FirebaseFirestore db = FirebaseFirestore.getInstance();
//////                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//////                            .setTimestampsInSnapshotsEnabled(true)
//////                            .build();
//////                    db.setFirestoreSettings(settings);
//////
//////                    DocumentReference userRef = db.collection(getString(R.string.user_collection))
//////                            .document(user.getUid());
//////
//////                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//////                        @Override
//////                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//////                            if(task.isSuccessful()){
//////                                Profile profile = Objects.requireNonNull(task.getResult()).toObject(Profile.class);
//////                                ((UserProfile)(getApplicationContext())).setProfile(profile);
////                                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
////                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////                                startActivity(intent);
////                                finish();
////
////                } else {
////                    Toast.makeText(LoginActivity.this, "AUTH FAILED", Toast.LENGTH_SHORT).show();
////
////                    FirebaseAuth.getInstance().signOut();
////                    finish();
////                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
////                    startActivity(intent);
////
////                }
////            }
////        };
////    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (mAuth.getCurrentUser()!=null){
//            finish();
//            startActivity(new Intent(this, ProfileActivity.class));
//        }
//    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_up:
                finish();
                startActivity(new Intent(this, RegisterActivity.class));

                break;

            case R.id.loginBtn:
                loginUser();
                break;
        }
    }
}
