package com.be4em.auto90.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.be4em.auto90.R;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.helper.GetJSON;
import com.be4em.auto90.helper.PermissionUtils;
import com.be4em.auto90.object.RideItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by mamdouhelnakeeb on 5/4/17.
 */

public class RideNew extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemClickListener{

    private static final String TAG = RideNew.class.getSimpleName();

    Context context = this;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 99;
    private boolean mPermissionDenied = false;

    GoogleApiClient mGoogleApiClient;
    Location pickUpLocation;
    Location destinationLocation;
    Location mCurrentLocation;
    LocationRequest mLocationRequest;

    private GoogleMap rideMap;
    private LocationManager locationManager;
    private ProgressDialog pDialog;

    PlaceAutocompleteFragment pickUpAutoComplete, destinationAutoComplete;
    String placeStr;
    Place pickUpPlace, destinationPlace;
    Marker pickupMarker, destinationMarker;
    String regionPickUp, regionDestination, gender, days, timeStart, timeEnd;

    //FloatingActionButton getLocationFAB;

    int colors[] = {R.color.red,
            R.color.black,
            R.color.white,
            R.color.colorPrimary,
            R.color.green,
            R.color.grey};

    int colorsReps = 0;


    FloatingActionButton fab;
    BottomSheetBehavior behavior;
    boolean showFAB = true;

    TextView distanceTV, periodTV, feesTV;
    float pathDistance = 0;


    DateFormat formatDate = DateFormat.getDateInstance();
    DateFormat formatTime = DateFormat.getTimeInstance();
    Calendar dateCalender = Calendar.getInstance();
    Calendar timeCalender = Calendar.getInstance();

    CoordinatorLayout coordinatorLayout;

    OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_map_new_activity);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = new OkHttpClient();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        fab = (FloatingActionButton) findViewById(R.id.mapGetLocationFAB);
        // To handle FAB animation upon entrance and exit
        final Animation growAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        final Animation shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_shrink);

        fab.setVisibility(View.VISIBLE);
        fab.startAnimation(growAnimation);

        final LinearLayout bottomSheetLL = (LinearLayout) findViewById(R.id.bottomSheetLL);
        bottomSheetLL.setVisibility(View.INVISIBLE);

        final View bottomSheet = coordinatorLayout.findViewById(R.id.comment_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        distanceTV = (TextView) findViewById(R.id.distanceTV);
        periodTV = (TextView) findViewById(R.id.periodTV);
        feesTV = (TextView) findViewById(R.id.feesTV);

        periodTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(RideNew.this, d, dateCalender.get(Calendar.YEAR), dateCalender.get(Calendar.MONTH), dateCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Retrieve the PlaceAutocompleteFragment.
        pickUpAutoComplete = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.pickupAutoComplete);

        pickUpAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                rideMap.clear();
                LatLng latLng = place.getLatLng();
                pickUpPlace = place;

                regionPickUp = getRegion(latLng.latitude, latLng.longitude);

                pickUpLocation = new Location("pickUp");
                pickUpLocation.setLatitude(place.getLatLng().latitude);
                pickUpLocation.setLongitude(place.getLatLng().longitude);

                //move map camera
                rideMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                rideMap.animateCamera(CameraUpdateFactory.zoomTo(14f));

                showRoute();
            }

            @Override
            public void onError(Status status) {

            }
        });

        pickUpAutoComplete.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickUpPlace = null;
                pickUpAutoComplete.setText("");
                showRoute();
            }
        });

        destinationAutoComplete = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.destinationAutoComplete);

        destinationAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //destinationLocation.setLatitude(place.getLatLng().latitude);
                //destinationLocation.setLongitude(place.getLatLng().longitude);
                //Place current location
                LatLng latLng = place.getLatLng();
                destinationPlace = place;

                regionDestination = getRegion(latLng.latitude, latLng.longitude);

                toBounds(latLng, 150);

                destinationLocation = new Location("destination");
                destinationLocation.setLatitude(place.getLatLng().latitude);
                destinationLocation.setLongitude(place.getLatLng().longitude);

                //move map camera
                rideMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                rideMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
                //syncHospitals(place.getLatLng().latitude, place.getLatLng().longitude);

                showRoute();

            }

            @Override
            public void onError(Status status) {

            }
        });

        destinationAutoComplete.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                destinationPlace = null;
                destinationAutoComplete.setText("");
                showRoute();
            }
        });

        pickUpAutoComplete.setHint("Pick up location");
        destinationAutoComplete.setHint("Destination Location");

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("EG")
                .build();

        pickUpAutoComplete.setFilter(typeFilter);
        destinationAutoComplete.setFilter(typeFilter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    //fab.startAnimation(shrinkAnimation);
                    //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    if (ContextCompat.checkSelfPermission(RideNew.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        //Place current location marker
                        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                        //move map camera
                        rideMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        rideMap.animateCamera(CameraUpdateFactory.zoomTo(16f));

                    }
                }
                else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED){

                    //behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {

                    case BottomSheetBehavior.STATE_DRAGGING:
                        if (showFAB) {
                            fab.startAnimation(shrinkAnimation);
                        }
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        showFAB = true;
                        fab.setImageResource(R.drawable.map_location_icon);
                        fab.setVisibility(View.VISIBLE);
                        fab.startAnimation(growAnimation);
                        bottomSheetLL.setVisibility(View.INVISIBLE);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetLL.setVisibility(View.VISIBLE);
                        fab.setImageResource(R.drawable.ic_done);
                        showFAB = false;
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });

    }

    private void searchRides(){


        if (regionPickUp.isEmpty()){
            Toast.makeText(this, "Please choose your pickup location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (regionDestination.isEmpty()){
            Toast.makeText(this, "Please choose your destination location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gender.isEmpty()){
            Toast.makeText(this, "Please choose gender", Toast.LENGTH_SHORT).show();
            return;
        }
        if (days.isEmpty()){
            Toast.makeText(this, "Please choose pickup days", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeStart.isEmpty()){
            Toast.makeText(this, "Please choose pickup time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeEnd.isEmpty()){
            Toast.makeText(this, "Please choose destination time", Toast.LENGTH_SHORT).show();
            return;
        }

        String regID = FirebaseInstanceId.getInstance().getToken();

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "getRides.php?"
                + "regionPick=" + regionPickUp
                + "&"
                + "regionDist=" + regionDestination
                + "&"
                + "regID=" + regID;

        Log.d("url", url);

        pDialog.setMessage("Fetching Rides please wait ...");
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


                            final String message = jObj.getString("message");

                            final String type = jObj.getString("type");

                            if (type.equals("ride")) {

                                String rideID = jObj.getString("rideID");
                                String latStart = jObj.getString("latStart");
                                String lonStart = jObj.getString("lonStart");
                                String latEnd = jObj.getString("latEnd");
                                String lonEnd = jObj.getString("latStart");
                                String driverID = jObj.getString("driverID");
                                String riders = jObj.getString("riders");

                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(RideNew.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Launch login activity
                                Intent intent = new Intent(getBaseContext(), RideChat.class);
                                intent.putExtra("rideID", rideID);
                                startActivity(intent);

                                finish();

                            }
                            else if (type.equals("sentToDrivers")){

                            }
                            else if (type.equals("noDrivers")){

                            }


                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(RideNew.this, errorMsg, Toast.LENGTH_SHORT).show();
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

    private String getRegion(double latitude, double longitude){

        final String[] region = {""};
        GetJSON getJSON = new GetJSON();

        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                + String.valueOf(latitude)
                + ","
                + String.valueOf(longitude) + "&sensor=false";

        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    try {
                        JSONObject results = new JSONObject(responseStr);
                        JSONArray resultsJSONArray = results.getJSONArray("results");

                        for (int j=0; j < resultsJSONArray.length(); j++){
                            JSONObject resultObj1 = resultsJSONArray.getJSONObject(j);
                            JSONArray addComps = resultObj1.getJSONArray("address_components");

                            for (int i = 0; i < addComps.length(); i++){
                                JSONObject addComp = addComps.getJSONObject(i);
                                JSONArray addCompTypes = addComp.getJSONArray("types");

                                if (addCompTypes.getString(0).equals("local" +
                                        "ity")){
                                    System.out.println(addComp.get("long_name").toString());
                                    region[0] = addComp.get("long_name").toString();
                                    return;
                                }

                                if (addCompTypes.getString(0).equals("administrative_area_level_2")){
                                    System.out.println(addComp.get("long_name").toString());
                                    region[0] = addComp.get("long_name").toString();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    // Request not successful
                }
            }
        });

        return region[0];
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateCalender.set(Calendar.YEAR, year);
            dateCalender.set(Calendar.MONTH, monthOfYear);
            dateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            periodTV.setText(formatDate.format(dateCalender.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            timeCalender.set(Calendar.MINUTE, minute);
            periodTV.setText(formatTime.format(timeCalender.getTime()));
        }
    };

    private void showRoute(){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        rideMap.clear();

        if (pickUpPlace != null && destinationPlace != null){

            LatLng origin = pickUpPlace.getLatLng();
            LatLng dest = destinationPlace.getLatLng();

            float distance = pickUpLocation.distanceTo(destinationLocation);
/*
            distanceTV.setText(String.valueOf(distance / 1000).substring(0, 3) + " km");
            feesTV.setText(String.valueOf(distance * 3 / 1000).substring(0, 3) + " L.E");

            //fab.startAnimation(shrinkAnimation);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
*/

            Log.d("distance", String.valueOf(distance));

            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(origin);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ride_search_map_pin)))
                    .title(pickUpPlace.getName().toString());
            pickupMarker = rideMap.addMarker(markerOptions);
            builder.include(origin);
            builder.include(dest);

            markerOptions.position(dest);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ride_search_map_gpin)))
                    .title(destinationPlace.getName().toString());
            destinationMarker = rideMap.addMarker(markerOptions);

            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            downloadTask.execute(url);
        }
        else if (pickUpPlace != null && destinationPlace == null){
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(pickUpPlace.getLatLng());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ride_search_map_pin)))
                    .title(pickUpPlace.getName().toString());
            pickupMarker = rideMap.addMarker(markerOptions);

            builder.include(pickUpPlace.getLatLng());

            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        }
        else if (pickUpPlace == null && destinationPlace != null){
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(destinationPlace.getLatLng());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ride_search_map_gpin)))
                    .title(destinationPlace.getName().toString());
            destinationMarker = rideMap.addMarker(markerOptions);

            builder.include(destinationPlace.getLatLng());

            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (pickUpPlace != null || destinationPlace != null) {
            LatLngBounds bounds = builder.build();
            int padding = 200; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            rideMap.animateCamera(cu);
        }
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        LatLngBounds latLngBounds = new LatLngBounds(southwest, northeast);
        Log.d("latLngBnds", latLngBounds.toString());
        return latLngBounds;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        rideMap = googleMap;

        initListeners();
//        enableMyLocation();

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        }
        else {
            buildGoogleApiClient();
        }

    }

    private void enableMyLocation() {

        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            buildGoogleApiClient();
            rideMap.setMyLocationEnabled(true);
        } else if (rideMap != null) {

        }
        */
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void initListeners() {
        rideMap.setOnMapClickListener(this);
        rideMap.setOnMarkerClickListener(this);
        rideMap.setOnInfoWindowClickListener(this);
        rideMap.setTrafficEnabled(false);
        //rideMap.getUiSettings().setZoomControlsEnabled(true);
        rideMap.getUiSettings().setMyLocationButtonEnabled(false);

    }

    private void removeListeners() {
        if( rideMap != null ) {
            rideMap.setOnMarkerClickListener(null);
            rideMap.setOnMapLongClickListener(null);
            rideMap.setOnInfoWindowClickListener(null);
            rideMap.setOnMapClickListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            rideMap.setMyLocationEnabled(true);
            rideMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Create a default location if the Google API Client fails. Placing location at Googleplex
/*        mCurrentLocation = new Location( "" );
        mCurrentLocation.setLatitude( 37.422535 );
        mCurrentLocation.setLongitude(-122.084804);
//        initCamera(mCurrentLocation);
        onLocationChanged(mCurrentLocation);
        */
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
        /*
        Intent i = new Intent(this, StationInfo.class);
        i.putExtra("marker", marker.getTitle());
        startActivity(i);
*/
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        return true;
    }


    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        /*
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
*/

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        rideMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        rideMap.animateCamera(CameraUpdateFactory.zoomTo(16f));

        if (mCurrentLocation != null){
            LatLng latLng1 = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            //move map camera
            rideMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
            rideMap.animateCamera(CameraUpdateFactory.zoomTo(16f));
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        placeStr = (String) adapterView.getItemAtPosition(i);
    }

    @Override
    public void onMapClick(LatLng latLng) {

        System.out.println(String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude));

        getRegion(latLng.latitude, latLng.longitude);

        rideMap.addMarker(new MarkerOptions()
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .title("Pick up")
                .snippet("4 E. 28TH Street From $15 /per night")
                .rotation((float) -15.0)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );

        rideMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latLng.latitude, latLng.longitude), 17));
    }

    public class DirectionsJSONParser {

        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");

                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();


                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }}


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;// + "&alternatives=true";

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while do url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0; i < result.size(); i++){
                points = new ArrayList<LatLng>();

                Log.d("resultSize", String.valueOf(result.size()));

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                Log.d("pathSize", String.valueOf(path.size()));

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.geodesic(true);
                lineOptions.color(getResources().getColor(colors[0]));
            }

            // Drawing polyline in the Google Map for the i-th route
            rideMap.addPolyline(lineOptions);

            double distance = SphericalUtil.computeLength(points);
            Log.d("pathDist", String.valueOf(distance));


            distanceTV.setText(String.valueOf(distance / 1000).substring(0, 3) + " km");
            feesTV.setText(String.valueOf(distance * 3 / 1000).substring(0, 3) + " L.E");

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        }
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