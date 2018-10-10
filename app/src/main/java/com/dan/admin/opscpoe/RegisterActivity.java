package com.dan.admin.opscpoe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    EditText email,pass;
    Button reg_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.reg_email);
        pass = (EditText) findViewById(R.id.reg_pass);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.reg_btn).setOnClickListener(this);
        findViewById(R.id.sign_up_already).setOnClickListener(this);

    }

    private void registerUser(){
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

        mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()){
                    finish();
                    Toast.makeText(getApplicationContext(),"User registration successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{

                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),"User already registered!", Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.reg_btn:
                registerUser();
                break;

            case R.id.sign_up_already:
                startActivity(new Intent(this, LoginActivity.class));

                break;
        }

    }
}
