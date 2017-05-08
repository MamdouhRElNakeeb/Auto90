package com.be4em.auto90.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.be4em.auto90.R;
import com.be4em.auto90.adapter.RidesRVAdapter;
import com.be4em.auto90.object.RideItem;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    RecyclerView ridesRV;
    ArrayList<RideItem> rideItemArrayList;
    RidesRVAdapter ridesRVAdapter;


    FloatingActionButton rideAddFAB;

    Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initDrawer();

        rideAddFAB = (FloatingActionButton) findViewById(R.id.ride_addFAB);

        ridesRV = (RecyclerView) findViewById(R.id.ridesRV);

        ridesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        rideItemArrayList = new ArrayList<RideItem>();

        ridesRVAdapter = new RidesRVAdapter(this, rideItemArrayList);

        syncRides();


        rideAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), RideNew.class));
            }
        });
    }

    void syncRides(){

        ridesRV.setAdapter(ridesRVAdapter);
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
                        String url = "http://exapply.ml";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;

                    case R.id.nav_logout:
                        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("login_activity", "0");
                        editor.apply();
                        //mFirebaseAuth.signOut();
                        //startActivity(new Intent(Home.this, Login.class));
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
}