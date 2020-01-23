package com.example.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText ETemail;
    private EditText ETpassword;
    private Button btnlogin;
    private Button btnpassword;
    private String[] scope = new String[]{VKScope.PHOTOS};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null)
                {
                    Toast.makeText(MainActivity.this,"Вход успешно выполнен",Toast.LENGTH_SHORT).show();
                }
                else
                {

                }
            }
        };

        ETemail = (EditText) findViewById(R.id.editText);
        ETpassword = (EditText) findViewById(R.id.editText2);
        btnlogin = (Button) findViewById(R.id.button);
        btnpassword = (Button) findViewById(R.id.button2);

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        System.out.println(Arrays.asList(fingerprints));


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getApplicationContext(),"Авторизация прошла успешно", Toast.LENGTH_LONG).show();

                String token = VKSdk.getAccessToken().accessToken;
                VKParameters parameters = VKParameters.from(VKApiConst.ACCESS_TOKEN, token);

                VKRequest request = new VKRequest("account.getProfileInfo",parameters);
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        try {
                            JSONObject jsonObject = response.json.getJSONObject("response");
                            String first_name = jsonObject.getString("first_name");
                            String last_name = jsonObject.getString("last_name");
                            intent.putExtra("firstname", first_name);
                            intent.putExtra("lastname", last_name);
                            startActivity(intent);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(),"Ошибка авторизации", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button)
        {
            if(ETemail.getText().length() != 0 && ETpassword.getText().length() != 0)
            {
                signin(ETemail.getText().toString(), ETpassword.getText().toString());
            } else {
                Toast.makeText(MainActivity.this, "Вы не ввели данные", Toast.LENGTH_SHORT).show();
            }
        }
        else if (v.getId() == R.id.button2)
        {
            if(ETemail.getText().length() != 0 && ETpassword.getText().length() != 0)
            {
                registration(ETemail.getText().toString(), ETpassword.getText().toString());
            } else {
                Toast.makeText(MainActivity.this, "Вы не ввели данные", Toast.LENGTH_SHORT).show();
            }
        }
        else if (v.getId() == R.id.button3)
        {
            VKSdk.login(this, scope);
        }
    }

    public void signin(String email, String pwd)
    {
        mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    String lastname = "";
                    intent.putExtra("firstname", ETemail.getText().toString());
                    intent.putExtra("lastname", lastname);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Логин или пароль указаны неверно", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void registration(String email, String pwd)
    {
        mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,"Регистрация прошла успешно",Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        Toast.makeText(MainActivity.this,"Произошла ошибка регистрации",Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }
}
