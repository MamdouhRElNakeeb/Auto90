package com.be4em.auto90.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.be4em.auto90.R;
import com.be4em.auto90.adapter.ChatRVAdapter;
import com.be4em.auto90.adapter.RidesRVAdapter;
import com.be4em.auto90.object.ChatItem;
import com.be4em.auto90.object.RideItem;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_chat_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                    chatItemArrayList.add(new ChatItem("1", "MamdouhRElNakeeb", chatTextET.getText().toString().trim()));
                    rideChatRVAdapter.notifyItemInserted(chatItemArrayList.size());
                    rideChatRV.smoothScrollToPosition(chatItemArrayList.size());
                    chatTextET.setText("");
                }
            }
        });
    }

    private void syncChat(){

        chatItemArrayList.add(new ChatItem("2", "Mamdouh El Nakeeb", "Welcome, We hope to enjoy your ride!"));

        rideChatRV.setAdapter(rideChatRVAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
