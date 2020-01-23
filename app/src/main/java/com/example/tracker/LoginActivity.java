package com.example.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    Button btnEx;
    Button btnCont;
    TextView Name;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnCont = findViewById(R.id.btnContinue);
        btnEx = findViewById(R.id.btnExit);
        Name = findViewById(R.id.textView2);

        String firstname = getIntent().getExtras().getString("firstname");
        String lastname = getIntent().getExtras().getString("lastname");
        Name.setText(firstname + " " + lastname);

        btnEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intToMain = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intToMain);
            }
        });

        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intToMap = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intToMap);
            }
        });
    }
}
