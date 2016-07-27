package com.self.varun.mapproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;


public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final TextView register = (TextView) findViewById(R.id.txt_Register);
        final TextView email = (TextView) findViewById(R.id.txt_Email);
        final ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        final TextView passWord = (TextView) findViewById(R.id.txt_Pass);
        final Button submit = (Button) findViewById(R.id.btnSubmit);
        Firebase.setAndroidContext(this);
        final   Firebase ref = new Firebase("https://vigilanti.firebaseio.com/");



        assert submit != null;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login.this,"Logging in...",Toast.LENGTH_LONG).show();
                authorize(email.getText().toString(),passWord.getText().toString());



            }


        });
        // Send user to registration Ui
        assert register!=null;
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main = new Intent(Login.this,MainActivity.class);
                startActivity(main);
            }
        });




    }





    protected void authorize(String user, String pass){
        final TextView name = (TextView) findViewById(R.id.txt_User);
        Firebase ref = new Firebase("https://vigilanti.firebaseio.com");
        ref.authWithPassword(user, pass, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                final TextView Name = (TextView) findViewById(R.id.txt_User);
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                Intent in = new Intent(Login.this,MapsActivity.class);

                // Sending Username to the next activity
                in.putExtra("Username",Name.getText().toString());
                startActivity(in);

                Intent serviceIntent = new Intent(Login.this, Gps_Service.class);
                serviceIntent.putExtra("UserID", Name.getText().toString());



            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Intent i = new Intent(Login.this,Login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
                Toast.makeText(Login.this,"Incorrect Email or password",Toast.LENGTH_LONG).show();

                // there was an error
            }
        });
    }



}