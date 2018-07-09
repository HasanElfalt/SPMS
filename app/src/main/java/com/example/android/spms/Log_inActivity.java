package com.example.android.spms;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Log_inActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    TextInputEditText email,password;
    Button login ;
    TextView signup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        email        = findViewById(R.id.emailLogIn);
        password     = findViewById(R.id.passwordLogIn);
        login        = findViewById(R.id.login);
        signup       = findViewById(R.id.sign);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Log_inActivity.this,Sign_upActivity.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            Intent decision = new Intent(Log_inActivity.this, decision.class);
            startActivity(decision);
            finish();
        }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailtext, passwordtext ;

                //createLocationRequest();

                //empty.clear();
                //new MyAsyncresources().execute("http://192.168.137.78/spms/api.php?get=android");


                emailtext     = email.getText().toString();
                passwordtext  = password.getText().toString();

                if(emailtext.isEmpty() || emailtext == " "){

                    email.setError("Fill here please");
                    return;
                }
                if(passwordtext.isEmpty() || passwordtext == " "){

                    password.setError("Fill here please");
                    return;
                }
                if(passwordtext.length() < 6){

                    password.setError("password length must not be less than 6");
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailtext, passwordtext)
                        .addOnCompleteListener(Log_inActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Intent decision = new Intent(Log_inActivity.this, decision.class);
                                    startActivity(decision);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Log_inActivity.this,"check your internet access",Toast.LENGTH_LONG).show();
                                }

                            }

                        });

            }
        });


    }


}