package com.example.fooddelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
// in billing the rider should have the option of got paid and when this button is clicked the driver
// should be avilable and readt ro take the orders in that area again
// and customer should not have that he should have only the datails of the bill and he his order
// should be moved out of the requests in the database
public class Billing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
    }
}
