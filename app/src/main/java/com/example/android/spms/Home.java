package com.example.android.spms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    FirebaseUser currentUser = mAuth.getCurrentUser();

    Button log;
    Button sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        log = findViewById(R.id.Log);
        sign= findViewById(R.id.Sign);

        if(currentUser != null){

            Intent decision = new Intent(Home.this, decision.class);
            startActivity(decision);
            finish();
        }

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Home.this,Log_inActivity.class);
                startActivity(intent);
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Home.this,Sign_upActivity.class);
                startActivity(i);
            }
        });
    }
}
