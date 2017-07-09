package com.be4em.auto90.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by mamdouhelnakeeb on 5/2/17.
 */

public class Login extends AppCompatActivity {

    EditText emailET, passwordET;

    Button loginBtn;

    private CallbackManager callbackManager;

    LinearLayout linkToRegScr;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        GifImageView mGigImageView = (GifImageView) findViewById(R.id.logoGIF);

        GifDrawable gifDrawable = null;
        try {
            gifDrawable = new GifDrawable(getResources(), R.raw.auto90_logo);

        } catch (IOException e) {
            e.printStackTrace();
        }
        mGigImageView.setImageDrawable(gifDrawable);

        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);

        if (prefs.getBoolean("logged", false)){
            // Launch login activity
            Intent intent = new Intent(getBaseContext(), Home.class);
            startActivity(intent);
            finish();
        }

        initView();

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.fbLoginBtn);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbLogin(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    void initView (){

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        linkToRegScr = (LinearLayout) findViewById(R.id.linkToRegister);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passwordET);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        linkToRegScr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), Register.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void fbLogin(final AccessToken token){
        pDialog.setMessage("Login with Facebook");
        showDialog();

        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i("LoginActivity", response.toString());

                SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                // Get facebook data from login
                try {

                    if (object.has("id")) {
                        editor.putString("fbID", object.getString("id"));
                        Log.d("id", object.getString("id"));
                    }

                    if (object.has("name")) {
                        editor.putString("name", object.getString("name"));
                        Log.d("name", object.getString("name"));
                    }

                    if (object.has("email")) {
                        editor.putString("email", object.getString("email"));
                        Log.d("email", object.getString("email"));
                    }

                    if (object.has("gender")) {
                        editor.putString("gender", object.getString("gender"));
                        Log.d("gender", object.getString("gender"));
                    }


                    editor.putString("fbIDCon", "true");
                    editor.apply();

                    loginToHome();

                } catch (JSONException e) {
                    Log.d("LoginActivity", "Error parsing JSON");
                }
            }
        });

    }


    private void loginToHome(){
        Intent intent = new Intent(getBaseContext(), Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void fbLogout() {
        //mAuth.signOut();
        LoginManager.getInstance().logOut();

    }

    private void loginUser(){

        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        String regID = FirebaseInstanceId.getInstance().getToken();

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "login.php?"
                + "email=" + email
                + "&"
                + "password=" + password
                + "&"
                + "regID=" + regID;

        Log.d("url", url);

        pDialog.setMessage("Signing in please wait ...");
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
                                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(Login.this, errorMsg, Toast.LENGTH_SHORT).show();
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
