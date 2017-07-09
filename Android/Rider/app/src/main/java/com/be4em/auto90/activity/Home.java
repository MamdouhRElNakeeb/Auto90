package com.be4em.auto90.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.be4em.auto90.R;
import com.be4em.auto90.adapter.RidesRVAdapter;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.helper.GetJSON;
import com.be4em.auto90.object.RideItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Home extends AppCompatActivity {

    RecyclerView ridesRV;
    ArrayList<RideItem> rideItemArrayList;
    RidesRVAdapter ridesRVAdapter;


    FloatingActionButton rideAddFAB;

    Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initDrawer();

        rideAddFAB = (FloatingActionButton) findViewById(R.id.ride_addFAB);

        ridesRV = (RecyclerView) findViewById(R.id.ridesRV);

        ridesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        rideItemArrayList = new ArrayList<RideItem>();
        ridesRVAdapter = new RidesRVAdapter(this, rideItemArrayList);

        //ridesRV.setAdapter(ridesRVAdapter);
        syncRides();


        rideAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), RideNew.class));
            }
        });
    }

    void syncRides(){

        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);

        rideItemArrayList = new ArrayList<>();

        rideItemArrayList.clear();

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "getUserRides.php?"
                + "userID=" + prefs.getString("userID", "");


        Log.d("url", url);

        //GetRidesTask getRidesTask = new GetRidesTask();


        pDialog.setMessage("Fetching rides, please wait ...");
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

                            if (jObj.has("rides")) {
                                JSONArray ridesJArray = jObj.getJSONArray("rides");

                                Log.d("ridesNo", String.valueOf(ridesJArray.length()));


                                for (int i = 0; i < ridesJArray.length(); i++) {

                                    JSONObject rideObj = (JSONObject) ridesJArray.get(i);

                                    int rideID = Integer.parseInt(rideObj.get("rideID").toString());
                                    int driverID = Integer.parseInt(rideObj.get("driverID").toString());
                                    double latStart = Double.parseDouble(rideObj.get("latStart").toString());
                                    double lonStart = Double.parseDouble(rideObj.get("lonStart").toString());
                                    double latEnd = Double.parseDouble(rideObj.get("latEnd").toString());
                                    double lonEnd = Double.parseDouble(rideObj.get("lonEnd").toString());
                                    String name = rideObj.get("name").toString();
                                    float rideTime = Float.parseFloat(rideObj.getString("timeStart"));
                                    String rideDays = rideObj.getString("days");
                                    String subscription = rideObj.getString("subscription");
                                    float fare = Float.parseFloat(rideObj.get("fare").toString());

                                    rideItemArrayList.add(new RideItem(rideID, driverID, name, latStart, lonStart, latEnd, lonEnd, rideTime, rideDays, subscription, fare));
                                }

                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        ridesRVAdapter = new RidesRVAdapter(Home.this, rideItemArrayList);
                                        ridesRV.setAdapter(ridesRVAdapter);

                                    }
                                });

                            }
                            else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(Home.this, "You have no rides yet", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(Home.this, errorMsg, Toast.LENGTH_SHORT).show();
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

    void initDrawer(){

        mDrawerLayout = (DrawerLayout) findViewById(R.id.homeDL);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,  R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mDrawerToggle.syncState();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerToggle.syncState();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);

        final ImageButton userPP = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.user_profile_photo);
        userPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });


        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.userNameTV)).setText(getSharedPreferences("UserDetails", MODE_PRIVATE).getString("name", ""));

        navigationView.getMenu().getItem(0).setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        navigationView.getMenu().getItem(0).setChecked(true);
                        return true;

                    case R.id.nav_profile:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        //startActivity(new Intent(Home.this, Profile.class));
                        navigationView.getMenu().getItem(1).setChecked(true);
                        return true;

                    case R.id.nav_add_event:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(getBaseContext(), RideNew.class));
                        navigationView.getMenu().getItem(2).setChecked(true);
                        return true;

                    case R.id.nav_settings:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        //startActivity(new Intent(Home.this, Profile.class));
                        navigationView.getMenu().getItem(3).setChecked(true);
                        return true;

                    case R.id.nav_about:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        navigationView.getMenu().getItem(4).setChecked(true);
                        //startActivity(new Intent(getBaseContext(), About.class));
                        return true;

                    case R.id.nav_contact:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        navigationView.getMenu().getItem(5).setChecked(true);
                        String url = "http://be4em.info/auto90";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;

                    case R.id.nav_logout:
                        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("logged", false);
                        editor.apply();
                        startActivity(new Intent(getBaseContext(), Login.class));
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //syncRides();
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