package com.example.fooddelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainSelectionActivity extends AppCompatActivity {
    private Button mDriver,mCustomer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_selection);

        mDriver=findViewById(R.id.button7);
        mCustomer=findViewById(R.id.button8);

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MainSelectionActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainSelectionActivity.this,LoginActivity2.class);
                startActivity(intent);
                //finish(); please remove this if there is any error certaining
                return;
            }
        });
    }
}
