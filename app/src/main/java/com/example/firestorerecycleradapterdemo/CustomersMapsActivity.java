package com.example.firestorerecycleradapterdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CustomersMapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{


    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button TestButton;
    private Button Logout;
    private Button StateBtn;
    private Button SettingsButton;
    private Button CallGamerButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference StateDatabaseRef;
    private LatLng CustomerPickUpLocation;

    private DatabaseReference DriverAvailableRef, DriverLocationRef;
    private DatabaseReference DriversRef;
    private DatabaseReference CustomersRef;
    private int radius = 100;
    private View mapView;

    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;
    private String customerID;
    private double LocationLat;
    Marker DriverMarker;
  //  Marker DriverMarker, PickUpMarker;
    //GeoQuery geoQuery;
    //было List<String>
private ArrayList<String> mKeys = new ArrayList<>();
//private int t;
private int state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_maps);

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

        Logout = (Button) findViewById(R.id.logout_customer_btn);
        CallGamerButton =  (Button) findViewById(R.id.call_a_gamer_button);
        StateBtn = (Button) findViewById(R.id.state_btn);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Leaders");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Gamers");
        StateDatabaseRef = FirebaseDatabase.getInstance().getReference().child("State");



        final String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        StateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (StateBtn.getText().toString()) {

                    case ("ЖИВ"): {
                        StateBtn.setText("РАНЕН");
                        StateBtn.setBackgroundResource(R.drawable.button_contur_bott_left);
                        //Внеси в бд
                        StateDatabaseRef.child(userID).setValue("РАНЕН");
                        return;          }
                    case ("РАНЕН"):
                    {
                        StateBtn.setText("МЁРТВ");
                        StateBtn.setBackgroundResource(R.drawable.button_contur_bott_left_red);
                        //Внеси в бд
                        StateDatabaseRef.child(userID).setValue("МЁРТВ");
                        return;
                    }
                    case  ("МЁРТВ"): {
                        StateBtn.setText("ЖИВ");
                        StateBtn.setBackgroundResource(R.drawable.button_contur_bott_left);
                        //Внеси в бд
                        StateDatabaseRef.child(userID).setValue("ЖИВ");
                        return;
                    }
                }


            }
        });


        TestButton = (Button) findViewById(R.id.button2);

        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomersMapsActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();

                LogOutUser();
            }
        });


        CallGamerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mMap.clear();
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                geoFire.setLocation(customerId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {

                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

                CustomerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
       //         mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("My Location"));



                //заходим в child
                DriversRef  = FirebaseDatabase.getInstance().getReference().child("Gamers");
                CustomersRef  = FirebaseDatabase.getInstance().getReference().child("Leaders");


                //слушатель, чтобы создать список ключей участников
    DriversRef.addChildEventListener(new ChildEventListener(){
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String key = dataSnapshot.getKey();
        mKeys.add(key);


     DriversRef.child(key).addValueEventListener(new ValueEventListener() {

         @Override
         public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

             String t = String.valueOf(StateDatabaseRef.child(Objects.requireNonNull(dataSnapshot.getKey())));
             t = t.replace("https://firestorerecycleradapter-2d513.firebaseio.com/State/", "");

             StateDatabaseRef.child(t).addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {

                     if (snapshot.getValue(String.class).equals("ЖИВ")){
                         Double latt = dataSnapshot.child("l").child("0").getValue(Double.class);
                         Double longt = dataSnapshot.child("l").child("1").getValue(Double.class);


                         LatLng DriverLatLng = new LatLng(latt, longt);

                         mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("mem"));

                     }

                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });



               }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {
         }

     });


    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


});

                //слушатель, чтобы создать список ключей командиров
                CustomersRef.addChildEventListener(new ChildEventListener(){
                    @Override
                    public void onChildAdded(DataSnapshot cdataSnapshot, String s) {
                        String ckey = cdataSnapshot.getKey();
                        mKeys.add(ckey);


                        CustomersRef.child(ckey).addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                String tc = String.valueOf(StateDatabaseRef.child(Objects.requireNonNull(dataSnapshot.getKey())));
                                tc = tc.replace("https://firestorerecycleradapter-2d513.firebaseio.com/State/", "");

                                StateDatabaseRef.child(tc).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (!snapshot.getValue(String.class).equals("МЁРТВ")){
                                            Double clatt = dataSnapshot.child("l").child("0").getValue(Double.class);
                                            Double clongt = dataSnapshot.child("l").child("1").getValue(Double.class);


                                            LatLng CuLatLng = new LatLng(clatt, clongt);

                                            mMap.addMarker(new MarkerOptions().position(CuLatLng).title("com").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }

                        });


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });

            }
        });

    }
    private void getClosestDriverCab()
    {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if(!driverFound)
                {
                    radius = radius + 10;
                    getClosestDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void LogOutUser() {


        Intent startPageIntent = new Intent(CustomersMapsActivity.this, WelcomeActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // now let set user location enable
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        //перенос точки моё местоположение
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 200);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //

            return;
        }
        // handle the refreshment of the location
        //без этого дает локацию только после
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


    }



    @Override
    public void onBackPressed() {

        Intent intent = new Intent(CustomersMapsActivity.this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();



    }
}
