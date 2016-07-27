package com.self.varun.mapproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Firebase ref;
    boolean status = false;


    @Override
    protected void onStart() {
        super.onStart();

        final EditText uID = (EditText) findViewById(R.id.unique_id);
        final EditText name = (EditText) findViewById(R.id.txt_name);
        final EditText user = (EditText) findViewById(R.id.editText_user);
        final EditText pass = (EditText) findViewById(R.id.editText_password);
        final Button btnRegister = (Button) findViewById(R.id.btnRegister);
        final Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        final TextView text = (TextView) findViewById(R.id.textViewCondition);


        Firebase.setAndroidContext(this);
        ref = new Firebase("https://vigilanti.firebaseio.com/");

        if (btnRegister != null)
// Registering user with specific name and type, see person class below
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
Intent restart = new Intent(MainActivity.this,MainActivity.class);
                    final String s = uID.getText().toString();


                    if (s.contains(",")){
                        Toast.makeText(MainActivity.this," Username cannot contain : ,/./[/]/#/ $",Toast.LENGTH_LONG).show();
                    startActivity(restart);
                        return;
                    }
                    if (s.contains(".")){
                        Toast.makeText(MainActivity.this," Username cannot contain : ,/./[/]/#/$",Toast.LENGTH_LONG).show();
                   startActivity(restart);
                        return;
                    }



                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {


                            // if the account was already created then restart activity without registering user
                            if (snapshot.child(s).child("Latitude").exists()) {
                                Intent restart = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(restart);
                                Toast.makeText(MainActivity.this, " Username in use", Toast.LENGTH_LONG).show();
return;

                            }


                            // if registration is successful push data to firebase
                            else  {
Toast.makeText(MainActivity.this," Registering..",Toast.LENGTH_LONG).show();
                                    //Pushes information to firebase with a Unique ID
                                    Person person = new Person(name.getText().toString());

                                    Map<String, Object> map1 = new HashMap<String, Object>();
                                    Map<String, String> usersMap = new HashMap<String, String>();
                                    usersMap.put("Name", person.name);
                                    //usersMap.put("Type", person.type);
                                    Map<String, Map<String, String>> users = new HashMap<String, Map<String, String>>();
                                    users.put(s, usersMap);
                                    ref.child(s).setValue(usersMap);
                                    // if account wasnt already created then register user
                                    register(user.getText().toString(), pass.getText().toString());
                                }


                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });


                }


            });
        assert btnSignIn != null;
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(MainActivity.this, Login.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                finish();


            }


        });
    }

    // Register user
    public void register(String x, String y) {
        ref.createUser(x, y, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("Successfully created user account with uid: " + result.get("uid"));
                // if successful make status true
                Status();
                Intent login = new Intent(MainActivity.this, Login.class);
                startActivity(login);


            }

            public boolean Status() {
                // return status true
                status = true;
                return status;

            }


            @Override
            public void onError(FirebaseError firebaseError) {

                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);
                Toast.makeText(MainActivity.this, "There was an error, please check to make sure all fields are filled out correctly", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, " Email may already be registered", Toast.LENGTH_LONG).show();

            }


        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    // Data for users
    public class Person {

        public String name;


        public Person() {
        }

        public Person(String name) {
            this.name = name;

        }


    }
}

