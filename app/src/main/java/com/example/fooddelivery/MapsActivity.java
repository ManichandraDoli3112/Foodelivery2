package com.example.fooddelivery;//this is for driverpart

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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

import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button mLogout;
    private Button mSettings;
    private Switch mworkingswitch;
    private String customerId = "";
    public Button mpickorderbythedriver;
    public int thepickedstatus=0;
    //EditText input_radius;
    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private TextView mCustomerName, mCustomerPhone, mCustomerDestination;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //the below lines will start the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }
        //input_radius=findViewById(R.id.editText5);

        mLogout = (Button) findViewById(R.id.logout);
        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);
        mSettings = (Button) findViewById(R.id.settings);
        mworkingswitch = (Switch) findViewById(R.id.workingswitch);
        mworkingswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    connectdriver();
                }
                else
                {
                    disconnectDriver();
                }
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this, MainSelectionActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, DriverSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });


        ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS},PackageManager.PERMISSION_GRANTED);//used to grant persmission for messaging

        getAssignedCustomer();
    }

    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverId).child("Customer Request").child("CustomerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerInfo();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();

                }
                else
                {
                    customerId="";
                    if(pickupMarker !=null)
                    {
                        pickupMarker.remove();
                    }
                    if(assignedCustomerPickupLocationRefListener != null)
                    {
                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }

                    mCustomerInfo.setVisibility(View.GONE);
                    mCustomerName.setText("");
                    mCustomerPhone.setText(" ");
                    mCustomerDestination.setText(" ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private Marker destimarker;
    private  DatabaseReference CustomerDestinationref;
    private ValueEventListener  CustomerDestinationreflistener;
    LatLng dropLatLng;
    private void getAssignedCustomerDestination(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDestinationref=FirebaseDatabase.getInstance().getReference().child("User_request").child("Destination_location").child(customerId).child("l");
        CustomerDestinationreflistener = CustomerDestinationref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map2 = (List<Object>) dataSnapshot.getValue();
                    double locationLat2 = 0;
                    double locationLng2 = 0;
                    if(map2.get(0) != null){
                        locationLat2 = Double.parseDouble(map2.get(0).toString());
                    }
                    if(map2.get(1) != null){
                        locationLng2 = Double.parseDouble(map2.get(1).toString());
                    }
                    dropLatLng = new LatLng(locationLat2,locationLng2);//delivery location
                    destimarker= mMap.addMarker(new MarkerOptions().position(dropLatLng).title("deliver location").
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {            }
        });

    }

    private Marker pickupMarker;
    private  DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation(){
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("User_request").child("Pick_up_location").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickupLatLng = new LatLng(locationLat,locationLng);//pickup location
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private String ridername;
    private String custophonenumber;
    String driveridforthisonly;
    private void getAssignedCustomerInfo()
    {
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
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
                        mCustomerName.setText( map.get("name").toString());
                    }
                    if(map.get("phone")!=null)
                    {
                        mCustomerPhone.setText(map.get("phone").toString());
                        custophonenumber= map.get("phone").toString();
                    }
                    if(map.get("Customer Type")!=null)
                    {
                        mCustomerDestination.setText(map.get("Customer Type").toString());
                    }
                    //for message sneding
                    driveridforthisonly= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference mDriverDatabaseforthisonly = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driveridforthisonly);
                    DatabaseReference mDriverDatabaseforthisonly2=mDriverDatabaseforthisonly.child("name");
                    final String[] nameforthisonly = new String[1];//=mDriverDatabaseforthisonly.child("name").getKey();
                    mDriverDatabaseforthisonly2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            nameforthisonly[0] =dataSnapshot.getKey();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    final String[] phoneforthisonly = new String[1];//=mDriverDatabaseforthisonly.child("phone").getKey();

                    DatabaseReference mDriverDatabaseforthisonly3=mDriverDatabaseforthisonly.child("phone");
                    mDriverDatabaseforthisonly3.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            phoneforthisonly[0] =dataSnapshot.getKey();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    String message= "Your order has been confirmed and will be picked up by the Driver\n Driver Details\n Name: "+ nameforthisonly[0] + "\n Phone Number: "+ phoneforthisonly[0];
                    SmsManager mysmsmanager = SmsManager.getDefault();
                    mysmsmanager.sendTextMessage(custophonenumber,null,message,null ,null);
                    //for message sending
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mpickorderbythedriver = (Button) findViewById(R.id.pickorder);
        mpickorderbythedriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thepickedstatus=1;
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
        mLastLocation = location;//this is driver location
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("driverlocation").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_deliverybike)));//was not there previouslt
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference().child("Riders_Available");
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference().child("Riders_Working");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireWorking = new GeoFire(refWorking);

        switch (customerId){
            case "":
                geoFireWorking.removeLocation(userId);
                geoFireAvailable.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                break;

            default:
                geoFireAvailable.removeLocation(userId);
                geoFireWorking.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                break;
        }

        DatabaseReference custoref10=FirebaseDatabase.getInstance().getReference().child("User_request").child("Pick_up_location").child(customerId).child("l");
        DatabaseReference custoref11=custoref10.child("0");
        DatabaseReference custoref12=custoref10.child("1");
        double lati=Double.parseDouble(custoref11.getKey());
        double longi=Double.parseDouble(custoref12.getKey());
        //pickupLatLng = Double.parseDouble(custoref1.getKey().toString());
        LatLng pickupLatlng=new LatLng(lati,longi);
        Location Picklocation = new Location("");//location for pickup
        Picklocation.setLatitude(pickupLatlng.latitude);
        Picklocation.setLongitude(pickupLatlng.longitude);


        DatabaseReference custoref20=FirebaseDatabase.getInstance().getReference().child("User_request").child("Destination_location").child(customerId).child("l");
        DatabaseReference custoref21=custoref20.child("0");
        DatabaseReference custoref22=custoref20.child("1");
        double lati2=Double.parseDouble(custoref21.getKey());
        double longi2=Double.parseDouble(custoref22.getKey());
        LatLng dropLatLng=new LatLng(lati2,longi2);
        Location Droplocation = new Location("");// location for dropping point
        Droplocation.setLatitude(dropLatLng.latitude);
        Droplocation.setLongitude(dropLatLng.longitude);
        //mlastlocation is for the present position of the driver
        float distancetopick =mLastLocation.distanceTo(Picklocation);
        if(distancetopick < 10)
        {
            SmsManager mysmsmanager = SmsManager.getDefault();
            mysmsmanager.sendTextMessage(custophonenumber,null,"Near the Pickup Location",null ,null);
        }
        float distancetodrop = mLastLocation.distanceTo(Droplocation);
        if(distancetodrop < 5)
        {
            SmsManager mysmsmanager = SmsManager.getDefault();
            mysmsmanager.sendTextMessage(custophonenumber,null,"Near the Drop Location",null ,null);
        }


    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void connectdriver()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }
    private void disconnectDriver()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Riders_Available");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut){
            disconnectDriver();
        }
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
