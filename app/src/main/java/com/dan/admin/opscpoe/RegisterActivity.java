package com.dan.admin.opscpoe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = "RegisterActivity";

    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    EditText email,pass;
    Button reg_btn;
    TextView name;

    private FirebaseFirestore fsDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (TextView)findViewById(R.id.reg_name);
        email = (EditText) findViewById(R.id.reg_email);
        pass = (EditText) findViewById(R.id.reg_pass);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.reg_btn).setOnClickListener(this);
        findViewById(R.id.sign_up_already).setOnClickListener(this);

    }

    private void registerUser(final String mail, String password){
        showDialog();
//        mail = email.getText().toString().trim();
//        password = pass.getText().toString().trim();

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

        mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                fsDb = FirebaseFirestore.getInstance();

                String names = name.getText().toString().trim();
                if (task.isSuccessful()){

                    Profile user = new Profile();
                    user.setEmail(mail);
                    user.setUsername(names);
                    user.setUser_id(FirebaseAuth.getInstance().getUid());

                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setTimestampsInSnapshotsEnabled(true)
                            .build();
                    fsDb.setFirestoreSettings(settings);

                    DocumentReference newUser = fsDb
                            .collection(getString(R.string.user_collection))
                            .document(FirebaseAuth.getInstance().getUid());

                    newUser.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideDialog();

                            if(task.isSuccessful()){
                                goToLoginScreen();
                            }else{
                                Toast.makeText(getApplicationContext(),"An error occurred!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(getApplicationContext(),"An error occurred!", Toast.LENGTH_SHORT).show();
                    hideDialog();
                }

                // ...
            }
        });

//                    // Sign in success
//                    FirebaseUser user = mAuth.getCurrentUser();
//
//                    String headingName = name.getText().toString().trim();
//
//                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                            .setDisplayName(headingName).build();
//
//                    user.updateProfile(profileUpdates);
//                    finish();
//                    Toast.makeText(getApplicationContext(),"UserProfile registration successful!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                }else{
//
//                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
//                        Toast.makeText(getApplicationContext(),"UserProfile already registered!", Toast.LENGTH_SHORT).show();
//
//                    }else {
//                        Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//            }
//        });

    }

    private void goToLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.reg_btn: {
                Log.d(TAG, "onClick: attempting to register.");

                //check for null valued EditText fields
                if (!isEmpty(email.getText().toString())
                        && !isEmpty(pass.getText().toString())) {

                    //Initiate registration task
                    registerUser(email.getText().toString(), pass.getText().toString());
                } else {
                    Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                }
            }
                break;
            case R.id.sign_up_already:
                startActivity(new Intent(this, LoginActivity.class));

                break;
            }
    }

}


