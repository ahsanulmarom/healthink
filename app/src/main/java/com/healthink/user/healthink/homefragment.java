package com.healthink.user.healthink;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
 * A simple {@link Fragment} subclass.
 */
public class homefragment extends Fragment {

    ImageButton addFriend,addGroup,contacts;
    TextView displayName, bio;
    ImageView pict;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    private FirebaseStorage mStorageRef;
    private static final String TAG = Home.class.getSimpleName();

    public static homefragment newInstance() {
        // Required empty public constructor
        return new homefragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_homefragment, container, false);
        displayName = (TextView) view.findViewById(R.id.home_displayName);
        addFriend = (ImageButton) view.findViewById(R.id.addFr);
        addGroup = (ImageButton) view.findViewById(R.id.addGr);
        contacts = (ImageButton) view.findViewById(R.id.contcs);
        bio = (TextView) view.findViewById(R.id.home_bio);
        pict = (ImageView) view.findViewById(R.id.home_userPict);

        //Pindah window
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Add_friend.class));
            }
        });
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Add_group.class));
            }
        });
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Kontak.class));
            }
        });

        fAuth = FirebaseAuth.getInstance();
        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User sedang login
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    getDisplayName();
                    FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
                    StorageReference storageReference = mStorageRef.getReference("photoProfile");
                    storageReference.child(user.getUid() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(view.getContext()).load(uri).into(pict);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pict.setImageDrawable(getResources().getDrawable(R.drawable.ic_dp_web));
                        }
                    });
                } else {
                    // User sedang logout
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivity(new Intent(getActivity(), Into.class));
                }
            }
        };
        return view;
    }

    public void getDisplayName() {
        final FirebaseUser user = fAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userData = database.getReference("userData");
        userData.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData userdata = new UserData();
                if(dataSnapshot.child("displayName").getValue(String.class) == null ||
                        dataSnapshot.child("displayName").getValue(String.class) == "" ||
                        dataSnapshot.child("displayName").getValue(String.class) == "0") {
                    userdata.setDisplayName(dataSnapshot.child("username").getValue(String.class));
                } else {
                    userdata.setDisplayName(dataSnapshot.child("displayName").getValue(String.class));
                }
                if (dataSnapshot.child("role").getValue(int.class).equals(1)) {
                    if (getActivity() != null) {
                        displayName.setCompoundDrawablesWithIntrinsicBounds(null,null,getResources().getDrawable(R.drawable.logo20),null);    //buat nandain di role 1
                    }
                }

                userdata.setBioUser(dataSnapshot.child("bio").getValue(String.class));
                displayName.setText(userdata.getDisplayName() + "   ");
                bio.setText(userdata.getBioUser());
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        displayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), UserProfile.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fStateListener != null) {
            fAuth.removeAuthStateListener(fStateListener);
        }
    }
}
