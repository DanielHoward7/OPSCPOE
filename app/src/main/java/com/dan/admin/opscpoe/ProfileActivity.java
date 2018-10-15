package com.dan.admin.opscpoe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    TextView unitTv,headingTv;
    FirebaseAuth mAuth;
    DatabaseReference databaseProfile;
    Spinner spinnerModes;
    Spinner spinnerUnits;
    private FirebaseFirestore fsDb;
    private UserLocation userLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fsDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        databaseProfile = FirebaseDatabase.getInstance().getReference("Users");

        unitTv = (TextView) findViewById(R.id.unitTV);
        headingTv = (TextView) findViewById(R.id.headingTV);

        spinnerModes = (Spinner) findViewById(R.id.spinnerMode);
        spinnerUnits = (Spinner) findViewById(R.id.spinnerUnit);
//        loadUserInfo();
//        getProfileDetails();

        findViewById(R.id.map_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

//    private void loadUserInfo() {
//
//        FirebaseUser user = mAuth.getCurrentUser();
//
//
//        if (user != null) {
//
//                String displayName = user.getDisplayName();
//                headingTv.setText("Welcome " + displayName);
//                Log.d(TAG, "got here");
//                Toast.makeText(getApplicationContext(),"WHATTHEFUCK", Toast.LENGTH_SHORT).show();
//        }else{
//            Log.d(TAG, "wtf");
//        }
//    }


//    private void getProfileDetails(){
//
//        if (userLocation == null){
//            userLocation = new UserLocation();
//
//            DocumentReference reference = fsDb.collection(getString(R.string.user_collection))
//                    .document(FirebaseAuth.getInstance().getUid());
//
//            reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()){
//                        Log.d(TAG, "onComplete: successfully set the user profile.");
//                        Profile profile = task.getResult().toObject(Profile.class);
//                        userLocation.setProfile(profile);
//                        ((UserProfile)getApplicationContext()).setProfile(profile);
//                        Log.d(TAG, "getProfileDetails: worked" );
//                    }
//                }
//            });
//        }else{
//            Log.d(TAG, "getProfileDetails: didnt work" );
//        }
//    }



//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user == null) {
//            Toast.makeText(getApplicationContext(),"Null user", Toast.LENGTH_SHORT).show();
//
////            finish();
////            startActivity(new Intent(this, LoginActivity.class));
//        } else {
//            loadUserInfo();
//
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuMap:
                startActivity(new Intent(this, MapActivity.class));

            case R.id.menuLogout:

                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
            }
        return true;

    }

    private void saveUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){

            String ids = user.getUid();

            Toast.makeText(getApplicationContext(),ids, Toast.LENGTH_SHORT).show();

            String mode = spinnerModes.getSelectedItem().toString();

            String email = user.getEmail();
            String username = user.getDisplayName();

//          String id = databaseProfile.push().getKey();

            Profile profile = new Profile(ids,email,username, mode);
            profile.setMode(mode);


            databaseProfile.child(ids).setValue(profile);

            Toast.makeText(getApplicationContext(),"Profile Updated", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Profile did not update, maybe no user", Toast.LENGTH_SHORT).show();

        }

    }
}

