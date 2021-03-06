package com.example.date1b;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class Login extends AppCompatActivity {
    EditText email, password;
    Button loginBtn, gotoRegister;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
//    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Firebase call to authenticate login and database data
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        gotoRegister = findViewById(R.id.gotoRegister);
        //maybe to add btn forgot password


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField(email);
                checkField(password);
                // if it's valid go to next activity
                if (valid) {
                    fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();


                            //check if the current user is admin or regular user.
                            DocumentReference dr = fStore.collection("Users").document(authResult.getUser().getUid());
                            // Get data from document
                            dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String ad = documentSnapshot.getString("isAdmin");
                                    int admin = ad != null ? 1 : 0;
                                    checkIfAdmin(admin);
                                }

//                        });
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                  Toast.makeText(Login.this, "Failed to connect, check fields", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }


            }

            //help function to check if is char legal. need to add some things.
            public boolean checkField(EditText textField) {
                if (textField.getText().toString().isEmpty()) {
                    textField.setError("Error");
                    valid = false;
                } else {
                    valid = true;
                }

                return valid;
            }

            //if user wants to enter app after existed he enters as a user
//    @Override
//    protected void onStart(){
//        super.onStart();
//      //  if(FirebaseAuth.getInstance().getCurrentUser() != null){
//      //      startActivity(new Intent(getApplicationContext(),MainActivity.class));
//      //      finish();
//      //  }
//    }
//
            private void checkIfAdmin(int admin) {
                // Check if user is admin
                if (admin == 1) {
                    startActivity(new Intent(getApplicationContext(), Admin.class));
                    finish();
                }
                // Is a regular user
                else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }

                });
                gotoRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                    }
                });
    }
}