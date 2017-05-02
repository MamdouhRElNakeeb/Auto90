package com.be4em.auto90.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.be4em.auto90.R;
import com.be4em.auto90.object.RideItem;

import java.util.ArrayList;

/**
 * Created by mamdouhelnakeeb on 5/1/17.
 */

public class RidesRVAdapter extends RecyclerView.Adapter<RidesRVAdapter.ViewHolder> {

    private Context context;
    ArrayList<RideItem> rideItemArrayList;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
//        RideItem rideItem = rideItemArrayList.get(position);

    }

    @Override
    public int getItemCount() {
        return 6;
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
