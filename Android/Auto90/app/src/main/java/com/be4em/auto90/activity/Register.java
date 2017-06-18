package com.be4em.auto90.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.be4em.auto90.R;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.helper.GetJSON;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mamdouhelnakeeb on 5/4/17.
 */

public class Register extends AppCompatActivity {

    LinearLayout linkToLogin;

    EditText nameET, emailET, mobileET, passwordET;
    Button registerBtn;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        initView();
    }

    void initView (){

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        linkToLogin = (LinearLayout) findViewById(R.id.linkToLogin);

        nameET = (EditText) findViewById(R.id.nameET);
        emailET = (EditText) findViewById(R.id.emailET);
        mobileET = (EditText) findViewById(R.id.mobileET);
        passwordET = (EditText) findViewById(R.id.passwordET);

        registerBtn = (Button) findViewById(R.id.registerBtn);

        linkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser(){

        String name = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String mobile = mobileET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (name.isEmpty()){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty()){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.isEmpty()){
            Toast.makeText(this, "Please enter your mobile", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        String regID = FirebaseInstanceId.getInstance().getToken();

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "register.php?"
                + "name=" + name
                + "&"
                + "email=" + email
                + "&"
                + "mobile=" + mobile
                + "&"
                + "password=" + password
                + "&"
                + "regID=" + regID;


        Log.d("url", url);

        pDialog.setMessage("Registering please wait ...");
        showDialog();

        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong

                hideDialog();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideDialog();

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("response", responseStr);

                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            // User successfully stored in MySQL
                            // Now store the user in sqlite
                            final String message = jObj.getString("message");
                            String name = jObj.getString("name");
                            String email = jObj.getString("email");
                            String mobile = jObj.getString("mobile");
                            String userID = jObj.getString("id");

                            SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putString("mobile", mobile);
                            editor.putString("userID", userID);
                            editor.putBoolean("logged", true);

                            editor.apply();

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Launch login activity
                            Intent intent = new Intent(getBaseContext(), Home.class);
                            startActivity(intent);
                            finish();

                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(Register.this, errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    // Request not successful
                }
            }
        });
    }



    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}