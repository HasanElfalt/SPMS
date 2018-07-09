package com.example.android.spms;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.TextInputEditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Sign_upActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextInputEditText email,password,userName,confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        userName        = findViewById(R.id.name);
        email           = findViewById(R.id.Email);
        password        = findViewById(R.id.Password);
        confirmPassword = findViewById(R.id.confirm);

    }

    public void Registration(View view){

        String emailText, passwordText, confirmPasswordText, userNameText ;

        userNameText        = userName.getText().toString();
        emailText           = email.getText().toString();
        passwordText        = password.getText().toString();
        confirmPasswordText = confirmPassword.getText().toString();

        if(userNameText.isEmpty() || userNameText == " "){

            userName.setError("Fill here please");
            return;
        }
        if(emailText.isEmpty() || emailText == " "){

            email.setError("Fill here please");
            return;
        }
        if(passwordText.isEmpty() || passwordText == " "){

            password.setError("Fill here please");
            return;
        }
        if(confirmPasswordText.isEmpty() || confirmPasswordText == " "){

            confirmPassword.setError("Fill here please");
            return;
        }
        if(!passwordText.equals(confirmPasswordText)){

            confirmPassword.setError("Password doesn't match");
            return;
        }
        if(passwordText.length() < 6){

            password.setError("password length must not be less than 6");
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(Sign_upActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent decision = new Intent(Sign_upActivity.this, decision.class);
                            startActivity(decision);
                            finish();

                        } else {

                            Log.e("try", "createUserWithEmail:failure", task.getException());

                            Toast.makeText(Sign_upActivity.this,"SomethingWrong",Toast.LENGTH_LONG).show();

                        }

                    }
                });


    }

}