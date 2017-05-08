package com.be4em.auto90.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.be4em.auto90.R;

/**
 * Created by mamdouhelnakeeb on 5/4/17.
 */

public class Register extends AppCompatActivity {

    LinearLayout linkToLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        initView();
    }

    void initView (){
        linkToLogin = (LinearLayout) findViewById(R.id.linkToLogin);

        linkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
