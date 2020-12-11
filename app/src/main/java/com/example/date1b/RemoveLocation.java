package com.example.date1b;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class RemoveLocation extends AppCompatActivity {
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    ArrayList<String> al = new ArrayList<>();
    ArrayAdapter<String> adapter;
    Button removeLoc, submit;
    String lat, lon;
    String nam;
    AutoCompleteTextView nameBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_location);

        nameBar = findViewById(R.id.autoCompleteSearchBar);
        removeLoc = findViewById(R.id.removemylocation);
        submit = findViewById(R.id.submitRemove);

        fStore.collection("Locations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {

                }
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    String name = dc.getDocument().getData().get("name").toString();
                    al.add(name);
                }
            }
        });


        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, al);
        nameBar.setThreshold(1);
        nameBar.setAdapter(adapter);
        nameBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameBar.setText("");
            }
        });


        removeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nam = nameBar.getText().toString().trim();
                fStore.collection("Locations").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentSnapshot nameSnapshot : value.getDocuments()) {
                            if (nameSnapshot.get("name").equals(nam)) {
                                nameSnapshot.getReference().delete();
                                Toast.makeText(RemoveLocation.this, "Location was Deleted", Toast.LENGTH_SHORT).show();
                                nameBar.setText("");


                            }
                        }
                    }
                });

            }
        });
    }
}