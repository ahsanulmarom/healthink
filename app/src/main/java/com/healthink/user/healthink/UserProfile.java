package com.healthink.user.healthink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by user on 12/07/2017.
 */

public class UserProfile extends AppCompatActivity {

    TextView namatampil, bio;
    ImageView pict;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    private static final String TAG = UserProfile.class.getSimpleName();
    CheckNetwork cn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cn = new CheckNetwork(this);
        if (!cn.isConnected()) {
            Toast.makeText(this, "You are not connected internet. Pease check your connection!", Toast.LENGTH_SHORT).show();
        }
            fAuth = FirebaseAuth.getInstance();
            fStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User sedang login
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        setContentView(R.layout.activity_userprofile);
                        namatampil = (TextView) findViewById(R.id.user_displayName);
                        bio = (TextView) findViewById(R.id.user_bio);
                        pict = (ImageView) findViewById(R.id.user_pict);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userData = database.getReference("userData");
                        userData.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
                                StorageReference storageReference = mStorageRef.getReference("photoProfile");
                                storageReference.child(user.getUid() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(UserProfile.this).load(uri).into(pict);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pict.setImageDrawable(getResources().getDrawable(R.drawable.ic_dp_web));
                                    }
                                });
                                UserData userdata = new UserData();
                                if (dataSnapshot.child("displayName").getValue(String.class) == null ||
                                        dataSnapshot.child("displayName").getValue(String.class) == "" ||
                                        dataSnapshot.child("displayName").getValue(String.class) == "0" ) {
                                    userdata.setDisplayName(dataSnapshot.child("username").getValue(String.class));
                                } else {
                                    userdata.setDisplayName(dataSnapshot.child("displayName").getValue(String.class));
                                }
                                if (dataSnapshot.child("role").getValue(int.class).equals(1)) {
                                    if (this != null) {
                                        namatampil.setCompoundDrawablesWithIntrinsicBounds(null,null,getResources().getDrawable(R.drawable.logo20),null);    //buat nandain di role 1
                                    }
                                }
                                userdata.setBioUser(dataSnapshot.child("bio").getValue(String.class));
                                namatampil.setText(userdata.getDisplayName() + "   ");
                                bio.setText(userdata.getBioUser());
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                    } else {
                        // User sedang logout
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                        startActivity(new Intent(UserProfile.this, Into.class));
                    }
                }
            };
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fStateListener != null) {
            fAuth.removeAuthStateListener(fStateListener);
        }
    }
}
