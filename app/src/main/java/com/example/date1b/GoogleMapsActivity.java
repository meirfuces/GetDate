package com.example.date1b;
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    protected double Tlatitude, Tlongitude;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    ArrayList fav = getFavoriteMarkers();
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        catchGoogleMapsException(this);


        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id)
                {
                    case R.id.nav_account:
                        Toast.makeText(GoogleMapsActivity.this, "My Account",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(GoogleMapsActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_search:
                        Toast.makeText(GoogleMapsActivity.this, "Search",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_map:
                        Toast.makeText(GoogleMapsActivity.this, "Map",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), GoogleMapsActivity.class));
                        finish();
                        break;
                    case R.id.nav_logout:
                        Toast.makeText(GoogleMapsActivity.this, "Logout",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                        break;
                    default:
                        return true;

                }
                return true;
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            };
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng myLoc;
        if (location != null) {
            Tlongitude = location.getLongitude();
            Tlatitude = location.getLatitude();
            //In emulator is set to tel aviv
            myLoc = new LatLng(Tlatitude, Tlongitude);
        } else {
            //  Ariel Location
            myLoc = new LatLng(32.1046, 35.1745);
        }
        // Add a marker in persons location and move the camera
        mMap.addMarker(new MarkerOptions().position(myLoc).title("Marker in myLoc"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));


        addMarkers();


        mMap.setOnMarkerClickListener(this);

        // Setting a custom info window adapter for the google map
        MarkerInfoWindowAdapter markerInfoWindowAdapter = new MarkerInfoWindowAdapter(getApplicationContext());
        mMap.setInfoWindowAdapter(markerInfoWindowAdapter);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addMark(latLng);
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent i = new Intent(getApplicationContext(), PlaceInfo.class);
                i.putExtra("name", marker.getTitle());
                i.putExtra("desc", marker.getSnippet());
                i.putExtra("pos", marker.getPosition());
                i.putExtra("lat", marker.getPosition().latitude);
                i.putExtra("lan", marker.getPosition().longitude);
                startActivity(i);
            }

        });


    }

    private ArrayList getFavoriteMarkers() {
        DocumentReference dr = fStore.collection("Users").document(fAuth.getCurrentUser().getUid());
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> a = document.getData();
                        if(a.get("favorites")!=null) {
                            fav =  (ArrayList)a.get("favorites");
                        }
                    } else {
                        System.out.println("No such document");
                    }
                } else {
                    task.getException();
                }
            }


        });
        return fav;
    }

    private void addMark(final LatLng latLng) {
// Init the dialog object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter details:");


// Set up the input
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);

        LinearLayout lp = new LinearLayout(getBaseContext());

        lp.setOrientation(LinearLayout.VERTICAL);

        lp.addView(input);
        lp.addView(input2);

        input.setHint("Name");
        input2.setHint("Description");

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input2.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(lp);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name;
                String description;

                name = input.getText().toString();
                description = input2.getText().toString();

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .snippet(description));

                // Add a new document with a generated id.
                Map<String, Object> data = new HashMap<>();
                data.put("latitude", Double.toString(latLng.latitude));
                data.put("longitude", Double.toString(latLng.longitude));
                data.put("name", name);
                data.put("snippet", description);

                DocumentReference newMarker = fStore.collection("Locations").document();

                newMarker.set(data);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


    @Override
    public void onLocationChanged(Location location) {
        Tlongitude = location.getLongitude();
        Tlatitude = location.getLatitude();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    private void addMarkers() {


        fStore.collection("Locations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    e.printStackTrace();
                }
                String description = "";
                String name = "";
                double latitude = 0;
                double longitude = 0;
                LatLng location = null;
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                    Object lat = dc.getDocument().getString("latitude");
                    Object lon = dc.getDocument().getString("longitude");
                    Object nam = dc.getDocument().getData().get("name");
                    Object snip = dc.getDocument().getData().get("snippet");

                    if (lat != null) {
                        latitude = Double.parseDouble((String) lat);
                    }
                    if (lon != null) {
                        longitude = Double.parseDouble((String) lon);
                    }
                    if (nam != null) {
                        name = nam.toString();
                    }
                    if (snip != null) {
                        description = snip.toString();
                    }
                    if (latitude != 0 && longitude != 0) {
                        location = new LatLng(latitude, longitude);
                    }
                    if (location != null) {
                        if(fav != null && fav.contains(name)) {
                            mMap.addMarker(new MarkerOptions().position(location).title(name).snippet(description).draggable(true)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        }else{
                            mMap.addMarker(new MarkerOptions().position(location).title(name).snippet(description).draggable(true));
                        }
                    }

                    description = "";
                    nam = "";
                    latitude = 0;
                    longitude = 0;
                    location = null;
                }
            }
        });
    }


    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);


    }

    public void catchGoogleMapsException(final Context context) {
        try {
            SharedPreferences hasFixedGoogleBug154855417 = getSharedPreferences("google_bug_154855417", Context.MODE_PRIVATE);
            if (!hasFixedGoogleBug154855417.contains("fixed")) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                File corruptedSavedClientParameters = new File(getFilesDir(), "SavedClientParameters.data.cs");
                File corruptedClientParametersData =
                        new File(
                                getFilesDir(),
                                "DATA_ServerControlledParametersManager.data."
                                        + getBaseContext().getPackageName());
                File corruptedClientParametersDataV1 =
                        new File(
                                getFilesDir(),
                                "DATA_ServerControlledParametersManager.data.v1."
                                        + getBaseContext().getPackageName());
                corruptedZoomTables.delete();
                corruptedSavedClientParameters.delete();
                corruptedClientParametersData.delete();
                corruptedClientParametersDataV1.delete();
                hasFixedGoogleBug154855417.edit().putBoolean("fixed", true).apply();
            }
        } catch (Exception e) {

        }

    }
}





