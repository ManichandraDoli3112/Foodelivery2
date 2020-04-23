package com.example.fooddelivery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button mLogout;
    private Button mrequest;
    private Button mSettings;
    private Button mCancel;
    private Boolean requestBol = false;//
    private Marker mn;
    private String destination;
    Address address2;
    LatLng latLng3;
    private LinearLayout mDriverInfo;
    private TextView mDriverName, mDriverPhone;
    String SearchLocation;

    //SupportMapFragment mapFragment;
    private SearchView searchView;//for showing the destination location to the driver.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //the below lines will start the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mCancel=(Button) findViewById(R.id.cancel);


        searchView= findViewById(R.id.sv_location);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchLocation =searchView.getQuery().toString();
                Geocoder geocoder = new Geocoder(MapsActivity2.this);
                List<Address> list = new ArrayList<>();
                if(SearchLocation != null || !SearchLocation.equals("")){
                    try{
                        list=geocoder.getFromLocationName(SearchLocation,1);

                    } catch (IOException e) {
                        e.printStackTrace();}
                    if(mn!=null){
                        mn.remove();
                    }
                    if( list.size()>0)
                    {
                        Address address = list.get(0);
                        Log.e("TAG","geolocate: found a location"+address.toString());
                        address2=address;
                        LatLng latLng2 = new LatLng(address.getLatitude(), address.getLongitude());
                        latLng3=latLng2;
                        mn=mMap.addMarker(new MarkerOptions().position(latLng2).title(SearchLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mrequest=(Button) findViewById(R.id.request);
        mrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                LatLng latLng2 = new LatLng(address2.getLatitude(), address2.getLongitude());
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User_request").child("Pick_up_location");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(latLng2.latitude,latLng2.longitude));
                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("User_request").child("Destination_location");
                GeoFire geoFire1=new GeoFire(ref1);
                geoFire1.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                mrequest.setText("Getting your Driver....");
                getClosestDriver();
            }
        });

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity2.this, MainSelectionActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mSettings = (Button) findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity2.this, CustomerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });



    }
    private int radius=1;//need to create a text field for radius from the rider side and store it in the and it is in Km
    private Boolean driverFound = false;
    private String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestDriver()
    {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("Riders_Available");
        GeoFire geoFire = new GeoFire(driverLocation);
        //below is picked up from above
        //Address address2 = addressList2.get(0);
        //latLng3 and address2 belongs to the pickup location
        final LatLng latLng3 = new LatLng(address2.getLatitude(), address2.getLongitude());
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng3.latitude, latLng3.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound){
                    driverFound = true;
                    driverFoundID = key;
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("Customer Request");
                    DatabaseReference driverRef2 = driverRef.child("Customer Destination");
                    DatabaseReference customerdestination = FirebaseDatabase.getInstance().getReference("User_request").child("Destination_location");
                    GeoFire geoFiresetdestionrider=new GeoFire(customerdestination);
                    geoFiresetdestionrider.setLocation(customerId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    HashMap map = new HashMap();
                    map.put("CustomerRideId", customerId);
                    //map.put("destination", SearchLocation);
                    driverRef.updateChildren(map);
                    getDriverLocation();
                    getDriverInfo();
                    mrequest.setText("Looking for Driver Location....");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Riders_Working").child(driverFoundID).child("l");
       // DatabaseReference settinguserdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("your Driver Id").child(driverFoundID);
        driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override//@NonNull before DataSnapShot
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    //mrequest.setText("Driver Found");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    //code for finding the distance between the driver and customer pickup also will
                    // notify when the driver comes near the parcel location
                    //from here to
                    Location loc1 = new Location("");
                    loc1.setLatitude(latLng3.latitude);
                    loc1.setLongitude(latLng3.longitude);//this is the parcel location before picking up

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);//this is the drivers live position
                    // will become the parcel location after the driver picks it up

                    Location  loc3=mLastLocation;
                    float distance = loc1.distanceTo(loc2);//distance in meters
                    if (distance<100){
                        mrequest.setText("Driver's Near your Parcel");

                        mrequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MapsActivity2.this, Parcel_Selection.class);
                                startActivity(intent);
                                return;

                            }
                        });
                    }else{
                        mrequest.setText("Driver Found: " + String.valueOf(distance));
                    }
                    float distance2=loc3.distanceTo((loc2));//distance in meters

                    if(distance2 <50)
                    {
                        mrequest.setText("Driver is here");
                        mrequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent =new Intent(MapsActivity2.this,Billing.class);
                                startActivity(intent);
                                return;
                            }
                        });
                    }
                    else
                    {
                        mrequest.setText("Driver is on the way : "+ String.valueOf(distance2));
                    }
                    // here
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_deliverybike)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });}

        private void getDriverInfo()
        {
            mDriverInfo.setVisibility(View.VISIBLE);
            DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID);
            mCustomerDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                    {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if(map.get("name")!=null)
                        {
                            mDriverName.setText( map.get("name").toString());
                        }
                        if(map.get("phone")!=null)
                        {
                            mDriverPhone.setText(map.get("phone").toString());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            mCancel.setVisibility(View.VISIBLE);
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    //used to remove the assigned customerid under the driverid
                    if (driverFoundID != null){
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("Customer Request");
                        driverRef.removeValue();
                        driverFoundID = null;
                    }
                    //remove the marker of the driver
                    if(mn != null){                        mn.remove();                    }
                    driverFound = false;                  radius = 1;
                    //removing the request from the customer request
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User_request").child("Pick_up_location");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("User_request").child("Destination_location");
                    GeoFire geoFire1=new GeoFire(ref1);
                    geoFire1.removeLocation(userId);
                    mrequest.setText("Request Rider");
                    mDriverInfo.setVisibility(View.GONE);
                    mDriverName.setText("");
                    mDriverPhone.setText("");
                    //removing the cancel button ie making it gone
                    mCancel.setVisibility(View.GONE);
                    return;
                }
            });


        }





    //onmapready=waiting for the map to load(use the marker)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.addMarker(new MarkerOptions().position(latLng).title("my location"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Riders_Available");
        //GeoFire geoFire = new GeoFire(ref);
        //geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"PLease provide the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
