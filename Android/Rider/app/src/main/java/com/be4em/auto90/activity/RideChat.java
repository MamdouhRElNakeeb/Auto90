package com.be4em.auto90.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.be4em.auto90.R;
import com.be4em.auto90.adapter.ChatRVAdapter;
import com.be4em.auto90.adapter.RidesRVAdapter;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.helper.GetJSON;
import com.be4em.auto90.object.ChatItem;
import com.be4em.auto90.object.RideItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mamdouhelnakeeb on 5/8/17.
 */

public class RideChat extends AppCompatActivity {

    RecyclerView rideChatRV;
    ArrayList<ChatItem> chatItemArrayList;
    ChatRVAdapter rideChatRVAdapter;
    ImageView sendBtnIV;
    EditText chatTextET;

    Toolbar toolbar;

    RideItem rideItem;

    private ProgressDialog pDialog;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_chat_activity);

        Bundle bundle = getIntent().getExtras();

        rideItem = bundle.getParcelable("rideItem");

        prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = prefs.edit();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rideItem.rideName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rideChatRV = (RecyclerView) findViewById(R.id.ride_chatRV);
        chatTextET = (EditText) findViewById(R.id.ride_chatET);
        sendBtnIV = (ImageView) findViewById(R.id.chatSendIV);

        rideChatRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        chatItemArrayList = new ArrayList<ChatItem>();

        rideChatRVAdapter = new ChatRVAdapter(this, chatItemArrayList);

        syncChat();

        sendBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chatTextET.getText().toString().isEmpty() && !chatTextET.getText().toString().equals(" ")) {
                    sendMsg();
                }
            }
        });
    }

    private void syncChat(){

        //chatItemArrayList.add(new ChatItem("2", "Mamdouh El Nakeeb", "Welcome, We hope to enjoy your ride!"));

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "getRideChat.php?"
                + "rideID=" + String.valueOf(rideItem.rideID);

        Log.d("url", url);

        pDialog.setMessage("Ride chat is loading....");
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

                            if (jObj.has("chat")) {
                                JSONArray ridesJArray = jObj.getJSONArray("chat");

                                Log.d("ridesNo", String.valueOf(ridesJArray.length()));

                                for (int i = 0; i < ridesJArray.length(); i++) {

                                    JSONObject rideObj = (JSONObject) ridesJArray.get(i);

                                    int msgID = Integer.parseInt(rideObj.get("msgID").toString());
                                    int userID = Integer.parseInt(rideObj.get("userID").toString());

                                    String msg = rideObj.get("msg").toString();
                                    String userName = rideObj.getString("userName");

                                    chatItemArrayList.add(new ChatItem(msgID, userID, userName, msg));
                                }

                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        rideChatRV.setAdapter(rideChatRVAdapter);
                                    }
                                });
                            }
                            else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(RideChat.this, "No messages yet for this ride", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {

                            // Error occurred in registration. Get the error

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(RideChat.this, "Messages loading failed", Toast.LENGTH_SHORT).show();
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

    private void sendMsg(){

        //chatItemArrayList.add(new ChatItem("2", "Mamdouh El Nakeeb", "Welcome, We hope to enjoy your ride!"));

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "sendMsg.php?"
                + "rideID=" + String.valueOf(rideItem.rideID)
                + "&"
                + "userID=" + prefs.getString("userID", "")
                + "&"
                + "msg=" + chatTextET.getText().toString().trim();

        Log.d("url", url);


        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("response", responseStr);

                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {

                                    Toast.makeText(RideChat.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                    chatItemArrayList.add(
                                            new ChatItem(0, Integer.parseInt(prefs.getString("userID", "0")),
                                                    prefs.getString("name", ""),
                                                    chatTextET.getText().toString().trim()));

                                    rideChatRVAdapter.notifyItemInserted(chatItemArrayList.size());
                                    rideChatRV.smoothScrollToPosition(chatItemArrayList.size());
                                    chatTextET.setText("");
                                }
                            });

                        } else {

                            // Error occurred in registration. Get the error

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(RideChat.this, "Message sending failed", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RideChat.this, "Message sending failed", Toast.LENGTH_SHORT).show();
                        }
                    });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.rideInfo){
            Intent intent = new Intent(getBaseContext(), RideDetails.class);
            intent.putExtra("rideItem", rideItem);
            startActivity(intent);
        }

        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;

    }

}
