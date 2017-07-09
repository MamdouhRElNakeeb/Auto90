package com.be4em.auto90.helper;

/**
 * Created by Nakeeb on 7/19/2016.
 */
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regID", token);
        editor.apply();
    }

}