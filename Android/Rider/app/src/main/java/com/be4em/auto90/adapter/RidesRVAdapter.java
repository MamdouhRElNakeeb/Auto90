package com.be4em.auto90.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.be4em.auto90.R;
import com.be4em.auto90.activity.RideChat;
import com.be4em.auto90.helper.AppConsts;
import com.be4em.auto90.object.RideItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mamdouhelnakeeb on 5/1/17.
 */

public class RidesRVAdapter extends RecyclerView.Adapter<RidesRVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<RideItem> rideItemArrayList;
    public RidesRVAdapter (Context context, ArrayList<RideItem> rideItemArrayList){
        this.context = context;
        this.rideItemArrayList = rideItemArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RideItem rideItem = rideItemArrayList.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RideChat.class);
                intent.putExtra("rideItem", rideItem);
                context.startActivity(intent);
            }
        });

        holder.rideName.setText(rideItem.rideName);

        float timeS = 0;
        String amPM = "";

        if (rideItem.rideTime > 12){
            timeS = rideItem.rideTime - 12;
            amPM = "PM";
        }
        else {
            timeS = rideItem.rideTime;
            amPM = "AM";
        }

        String timeStr = String.valueOf(timeS);

        String hr = timeStr.substring(0, timeStr.indexOf('.'));
        String mm = timeStr.substring(timeStr.indexOf('.') + 1, timeStr.length());

        holder.rideTime.setText(rideItem.rideDays + ", " + hr + ":" + mm + " " + amPM);

        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?size=1000x500"
                + "&maptype=roadmap&"
                + "markers=icon:" + AppConsts.API_URL + "/imgs/ride_search_map_gpin.png" + "%7C"
                + String.valueOf(rideItem.latStart) + "," + String.valueOf(rideItem.lonStart)
                + "&markers=icon:" + AppConsts.API_URL +"/imgs/ride_search_map_pin.png" + "%7C"
                + String.valueOf(rideItem.latEnd) + "," + String.valueOf(rideItem.lonEnd)
                + "&zoom=14&format=jpg";

        Log.d("rideMapUrl", mapUrl);



        Picasso.with(context).load(mapUrl).placeholder(R.drawable.map_temp).into(holder.rideMap);

    }

    @Override
    public int getItemCount() {
        return rideItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView rideName, rideTime, ridePrice, rideNotifs;
        ImageView rideMap;

        public ViewHolder(View itemView) {
            super(itemView);

            rideMap = (ImageView) itemView.findViewById(R.id.ride_mapIV);
            rideName = (TextView) itemView.findViewById(R.id.ride_nameTV);
            rideTime = (TextView) itemView.findViewById(R.id.ride_timeTV);
            ridePrice = (TextView) itemView.findViewById(R.id.ride_priceTV);
            rideNotifs = (TextView) itemView.findViewById(R.id.ride_notifsTV);
        }
    }
}
