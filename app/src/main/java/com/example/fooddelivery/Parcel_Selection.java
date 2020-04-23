package com.example.fooddelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Parcel_Selection extends AppCompatActivity {
    RadioGroup radioGroup;
    RadioButton radioButton;
    TextView textView;
    Button proceed;
    ImageView mimage1,mimage2,mimage3;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference  parcelsizeref = FirebaseDatabase.getInstance().getReference().child("Riders_Working").child("parcel_type");
    DatabaseReference  CustomerTyperef = FirebaseDatabase.getInstance().getReference().child("Riders_Working").child("Customer Type");
    DatabaseReference  CustomerType_near_customerref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("Customer Type");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel__selection);

        radioGroup=findViewById(R.id.radioGroup);
        Button proceed =findViewById(R.id.button);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkID = radioGroup.getCheckedRadioButtonId();
                if(checkID==-1)
                {
                    Toast.makeText(Parcel_Selection.this,"No checkbox selected",Toast.LENGTH_SHORT);
                }
                else
                {
                    findRadioButton(checkID);
                }

                finish();//added these two
                return;
            }
        });

        textView = findViewById(R.id.textView);

        mimage1=findViewById(R.id.imageView);
        mimage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parcelsizeref.setValue("small");
                Toast.makeText(Parcel_Selection.this,"Small Selected",Toast.LENGTH_SHORT);

            }
        });
        mimage2=findViewById(R.id.imageView4);
        mimage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parcelsizeref.setValue("medium");
                Toast.makeText(Parcel_Selection.this,"Medium Selected",Toast.LENGTH_SHORT);
            }
        });
        mimage3=findViewById(R.id.imageView3);
        mimage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parcelsizeref.setValue("large");
                Toast.makeText(Parcel_Selection.this,"Large Selected",Toast.LENGTH_SHORT);
            }
        });

    }
    private void findRadioButton(int checkedID)
    {
        switch(checkedID){
            case R.id.radio_one:
                CustomerTyperef.setValue("R");
                CustomerType_near_customerref.setValue("R");
                Toast.makeText(Parcel_Selection.this,"U Choosed to be a Regular Customer",Toast.LENGTH_SHORT);

                break;
            case R.id.radio_two:
                CustomerTyperef.setValue("NR");
                CustomerType_near_customerref.setValue("NR");
                Toast.makeText(Parcel_Selection.this,"U are not a Regular Customer",Toast.LENGTH_SHORT);

                break;
        }
    }

//    android:onClick="checkButton"
//    public void checkButton(View v)
//    {
//        int radioId = radioGroup.getCheckedRadioButtonId();
//        radioButton = findViewById(radioId);
//        Toast.makeText(this,"Selected Radio Button"+ radioButton.getText(),Toast.LENGTH_SHORT).show();
//    }
}
