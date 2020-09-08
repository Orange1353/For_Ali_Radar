package com.example.firestorerecycleradapterdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.Objects;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private TextView CreateCustomerAccount;
    private TextView TitleCustomer;
    private Button LoginCustomerButton;
    private Button RegisterCustomerButton;
    private EditText CustomerEmail;
    private EditText CustomerPassword;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);




        LoginCustomerButton = (Button) findViewById(R.id.login_driver_btn);
        RegisterCustomerButton = (Button) findViewById(R.id.register_driver_btn);
        //CustomerRegisterLink
        CreateCustomerAccount = (TextView) findViewById(R.id.create_driver_account);
        TitleCustomer = (TextView) findViewById(R.id.customer_status);
        CustomerEmail = (EditText) findViewById(R.id.driver_email);
        CustomerPassword = (EditText) findViewById(R.id.driver_password);
        loadingBar = new ProgressDialog(this);


        //новое, чтоб не выходило


        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListner =  new FirebaseAuth.AuthStateListener(){

            @Override
            public  void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
                    startActivity(intent);
                } else {
                }
            }
        };

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
            startActivity(intent);
        }



        RegisterCustomerButton.setVisibility(View.INVISIBLE);
        RegisterCustomerButton.setEnabled(false);

        CreateCustomerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateCustomerAccount.setVisibility(View.INVISIBLE);
                LoginCustomerButton.setVisibility(View.INVISIBLE);
                TitleCustomer.setText("РЕГИСТРАЦИЯ");
                RegisterCustomerButton.setVisibility(View.VISIBLE);
                RegisterCustomerButton.setEnabled(true);
            }
        });

        RegisterCustomerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                String email = CustomerEmail.getText().toString();

                String password = CustomerPassword.getText().toString();

                RegisterCustomer(email, password);

            }


        });

        LoginCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String email = CustomerEmail.getText().toString();
                //позывной

                String password = CustomerPassword.getText().toString();

                SingInCustomer( email, password);

            }
        });


    }

    private void SingInCustomer(String email, String password) {

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Вашу почту, пожалуйста.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Ваш пароль, пожалуйста....", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Подождите :");
            loadingBar.setMessage("Авторизация");
            loadingBar.show();

            mAuth = FirebaseAuth.getInstance();

            mAuth.signInWithEmailAndPassword(email.concat("@mail.ru"), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        DatabaseReference state = FirebaseDatabase.getInstance().getReference();

                            state.child("State").child(userID).setValue("ЖИВ");

                        Toast.makeText(CustomerLoginRegisterActivity.this, "Принято! ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
                        startActivity(intent);
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Попробуйте снова! ", Toast.LENGTH_SHORT).show();

                        loadingBar.dismiss();
                    }
                }
            });
        }


    }


    private void RegisterCustomer(String email, String password) {



        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Вашу почту, пожалуйста.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Ваш пароль, пожалуйста.", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Подождите :");
            loadingBar.setMessage("Авторизация ");
            loadingBar.show();



            mAuth = FirebaseAuth.getInstance();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Принято! ", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this, "Попробуйте снова! ", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

}
