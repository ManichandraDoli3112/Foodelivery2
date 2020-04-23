package com.example.fooddelivery;//for the customer side

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity2 extends AppCompatActivity {

    TextView btn;
    private EditText inputUsername,inputPassword,inputEmail,inputConformPassword;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        btn=findViewById(R.id.AlreadyHavingaccount);
        inputUsername=findViewById(R.id.editText);
        inputEmail=findViewById(R.id.InputEmail);
        inputPassword=findViewById(R.id.InputPassword);
        inputConformPassword=findViewById(R.id.InputConfirmPassword);
        mAuth=FirebaseAuth.getInstance();
        firebaseAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null)
                {
                    Intent intent=new Intent(RegisterActivity2.this,LoginActivity2.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mLoadingBar=new ProgressDialog(RegisterActivity2.this);

        btnRegister=findViewById(R.id.BtnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity2.this,LoginActivity2.class));
            }
        });
    }

    private void checkCredentials(){
        String username=inputUsername.getText().toString();
        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();
        String conformPassword=inputConformPassword.getText().toString();

        if(username.isEmpty() || username.length()<7)
        {
            showError(inputUsername,"Your username is not valid!");
        }
        else if (email.isEmpty() || !email.contains("@"))
        {
            showError(inputEmail,"Email is not valid!");
        }
        else if (password.isEmpty() || password.length()<7)
        {
            showError(inputPassword,"Password should be 7 charecter atleast!");
        }
        else if (conformPassword.isEmpty() || !conformPassword.equals(password))
        {
            showError(inputConformPassword,"Password is not matching!");
        }
        else
        {
            //Toast.makeText(this,"Call Registration Method",Toast.LENGTH_SHORT).show();
            mLoadingBar.setTitle("Registration");
            mLoadingBar.setMessage("Please Wait,while checking your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity2.this,"Sucessfully registered",Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                        String user_id=mAuth.getCurrentUser().getUid();

                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                        //DatabaseReference current_user_db2= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                        DatabaseReference email= current_user_db.child("Email");email.setValue(inputEmail.getText().toString());
                        DatabaseReference phonenumber = current_user_db.child("Phone Number");phonenumber.setValue(inputUsername.getText().toString());
                        //DatabaseReference number_of_order= current_user_db.child("Num_of_order");number_of_order.setValue("0");
                        //current_user_db.setValue(true);
                        DatabaseReference customertype= current_user_db.child("Customer Type");customertype.setValue("NR");

                        Intent intent=new Intent(RegisterActivity2.this,LoginActivity2.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity2.this,"Sign-up Error",Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                    }
                }
            });


        }
    }
    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();//help us to show the error

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}


