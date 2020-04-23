package com.example.fooddelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity {
    private EditText mNameField, mPhoneField;
    private Button mBack, mConfirm;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase,mtypecustomerdatabase;
    private String userID;
    private String mName;
    private String mPhone;
    private RadioButton radioButton,radioButton2;

   // private String memail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        //memail = (EditText) findViewById(R.id.);
        radioButton=findViewById(R.id.radiobutton);
        radioButton2=findViewById(R.id.radiobutton2);


        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo()
    {
        mCustomerDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mtypecustomerdatabase=mCustomerDatabase.child("Customer Type");
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null)
                    {
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null)
                    {
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                }
                if(mtypecustomerdatabase.getKey() == "NR")
                {
                    radioButton.setVisibility(View.VISIBLE);
                    radioButton2.setVisibility(View.INVISIBLE);
                }
                if(mtypecustomerdatabase.getKey() == "R")
                {
                    radioButton2.setVisibility(View.VISIBLE);
                    radioButton.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        mCustomerDatabase.updateChildren(userInfo);
        finish();
    }

    private void checkdata(View v)
    {

        if(mtypecustomerdatabase.getKey() == "NR")
        {
            Toast.makeText(CustomerSettingsActivity.this,"You are a Regular Customer now..",Toast.LENGTH_SHORT);
            mtypecustomerdatabase.setValue("R");
        }
        if(mtypecustomerdatabase.getKey()=="R")
        {
            Toast.makeText(CustomerSettingsActivity.this,"You are now a Non-Regular Customer..",Toast.LENGTH_SHORT);
            mtypecustomerdatabase.setValue("NR");
        }
    }
}
