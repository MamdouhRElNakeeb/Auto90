package com.be4em.auto90.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.be4em.auto90.R;
import com.be4em.auto90.adapter.RideFareRVAdapter;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.helper.GetJSON;
import com.be4em.auto90.object.ChatItem;
import com.be4em.auto90.object.FareItem;
import com.be4em.auto90.object.RideItem;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mamdouhelnakeeb on 6/20/17.
 */

public class RideDetails extends AppCompatActivity {

    LinearLayout driverLL, rider1LL, ride2LL, rider3LL;

    CollapsingToolbarLayout mapCTL;

    TextView driverName, carModel, carNo, ride1Name, rider2Name, rider3Name;

    ImageView mapIV;

    RideItem rideItem;

    String driverNameStr, carModelStr, carBrandStr, carYearStr, carNoStr;

    RecyclerView fareDetailsRV;
    RideFareRVAdapter rideFareRVAdapter;
    ArrayList <FareItem> fareItemArrayList;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_details);

        Bundle bundle = getIntent().getExtras();

        if (bundle.containsKey("rideItem")){
            rideItem = (RideItem) bundle.getParcelable("rideItem");
        }
        else {
            finish();
        }

        driverLL = (LinearLayout) findViewById(R.id.driverLL);
        rider1LL = (LinearLayout) findViewById(R.id.ride1LL);
        ride2LL = (LinearLayout) findViewById(R.id.ride2LL);
        rider3LL = (LinearLayout) findViewById(R.id.ride3LL);

        mapCTL = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        mapIV = (ImageView) findViewById(R.id.ride_mapIV);
        driverName = (TextView) findViewById(R.id.driverName);
        ride1Name = (TextView) findViewById(R.id.rider1TV);
        rider2Name = (TextView) findViewById(R.id.rider2TV);
        rider3Name = (TextView) findViewById(R.id.ride3TV);

        carModel = (TextView) findViewById(R.id.carModel);
        carNo = (TextView) findViewById(R.id.carNo);


        mapCTL.setTitle(rideItem.rideName);
        mapCTL.setExpandedTitleColor(getResources().getColor(R.color.black));
        mapCTL.setCollapsedTitleTextColor(getResources().getColor(R.color.black));

        initFareDetails();

        syncDetails();

        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?size=1000x500"
                + "&maptype=roadmap&"
                + "markers=icon:" + AppConsts.API_URL + "/imgs/ride_search_map_gpin.png" + "%7C"
                + String.valueOf(rideItem.latStart) + "," + String.valueOf(rideItem.lonStart)
                + "&markers=icon:" + AppConsts.API_URL +"/imgs/ride_search_map_pin.png" + "%7C"
                + String.valueOf(rideItem.latEnd) + "," + String.valueOf(rideItem.lonEnd)
                + "&zoom=14&format=jpg";

        Log.d("rideMapUrl", mapUrl);

        Picasso.with(this).load(mapUrl).placeholder(R.drawable.map_temp).into(mapIV);

    }

    private void syncDetails(){

        //chatItemArrayList.add(new ChatItem("2", "Mamdouh El Nakeeb", "Welcome, We hope to enjoy your ride!"));
        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "getRideDetails.php?"
                + "rideID=" + String.valueOf(rideItem.rideID)
                + "&"
                + "driverID=" + String.valueOf(rideItem.driverID)
                + "&"
                + "userID=" + prefs.getString("userID", "");

        Log.d("url", url);

        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong

            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("response", responseStr);

                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {

                            JSONArray usersArr = jObj.getJSONArray("users");

                            final String users[] = {""};

                            for (int i = 0; i < usersArr.length(); i++){
                                JSONObject userObj = usersArr.getJSONObject(i);

                                int userID = Integer.parseInt(userObj.get("userID").toString());
                                String userName = userObj.get("userName").toString();
                                users[i] = userName;

                            }

                            JSONObject driverObj = (JSONObject) jObj.get("driver");

                            if (rideItem.driverID != 0){
                                driverNameStr = driverObj.get("name").toString();
                                carModelStr = driverObj.getString("carModel");
                                carBrandStr = driverObj.getString("carBrand");
                                carYearStr = driverObj.getString("carYear");
                                carNoStr = driverObj.getString("carNo");
                            }
                            else{

                                driverNameStr = "No driver yet";
                            }

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {

                                    driverName.setText(driverNameStr);
                                    carModel.setText(carModelStr + " - " + carBrandStr + " - " + call);
                                    ride1Name.setText(getSharedPreferences("UserDetails", MODE_PRIVATE).getString("name", "Me"));

                                    switch (users.length){
                                        case 1:
                                            rider2Name.setText(users[0]);
                                            break;
                                        case 2:
                                            rider2Name.setText(users[0]);
                                            rider3Name.setText(users[1]);
                                            break;
                                    }

                                }
                            });

                        } else {

                            // Error occurred in registration. Get the error

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(RideDetails.this, "An error occurred, try again later", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    // Request not successful

                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(RideDetails.this, "An error occurred, try again later", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void initFareDetails(){

        fareDetailsRV = (RecyclerView) findViewById(R.id.fareDetailsRV);

        fareDetailsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        fareItemArrayList = new ArrayList<FareItem>();

        switch (rideItem.subscription){
            case "Weekly":
                fareItemArrayList.add(new FareItem(rideItem.fare, 1453658252, "paid"));
                break;
            case "Monthly":
                break;
            case "Quarterly":
                break;
        }

        String dayStr = "";

        if (rideItem.rideDays.contains(",")){
            dayStr = rideItem.rideDays.substring(rideItem.rideDays.lastIndexOf(','), rideItem.rideDays.length());
        }
        else {
            dayStr = "Fri";
        }

        // TODO get timeInMillis and add fareItems
        fareItemArrayList.add(new FareItem(155340.5, 1453658252, "paid"));
        fareItemArrayList.add(new FareItem(155340.5, 1453658252, "pending"));
        fareItemArrayList.add(new FareItem(155340.5, 1453658252, "notYet"));

        rideFareRVAdapter = new RideFareRVAdapter(fareItemArrayList);

        fareDetailsRV.setAdapter(rideFareRVAdapter);

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
