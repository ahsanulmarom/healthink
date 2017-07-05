package com.healthink.user.healthink;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    private EditText username, email, password, repassword;
    private Button submit;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    private static final String TAG = SignUp.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        fAuth = FirebaseAuth.getInstance();
        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User sedang login
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User sedang logout
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        username = (EditText) findViewById(R.id.signup_username);
        email = (EditText) findViewById(R.id.signup_email);
        password = (EditText) findViewById(R.id.signup_password);
        repassword = (EditText) findViewById(R.id.signup_repassword);
        submit = (Button) findViewById(R.id.signup_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(username.getText().toString().equalsIgnoreCase("")) {
                    username.setError("This Field is Required");
                } else if (username.getText().toString().length() < 5) {
                    username.setError("Username must be at least 5 characters");
                } else if(email.getText().toString().equalsIgnoreCase("")) {
                    email.setError("This Field is Required");
                } else if(password.getText().toString().equalsIgnoreCase("")) {
                    password.setError("This Field is Required");
                }else if(password.getText().toString().length() < 5) {
                    password.setError("Password must be at least 5 characters");
                }else if(repassword.getText().toString().equals(password.getText().toString())) {
                    repassword.setError("Please check your password!");
                } else {
                    signUp(email.getText().toString(), password.getText().toString());
                }

            }
        });
    }

    private void signUp(final String email, String password){
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Proses Pendaftaran Gagal",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUp.this, "Proses Pendaftaran Berhasil! Silakan Login dengan Email dan Password Anda!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(SignUp.this, Home.class));
                    }
                });
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
