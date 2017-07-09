package com.be4em.auto90.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.be4em.auto90.R;
import com.be4em.auto90.object.FareItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mamdouhelnakeeb on 6/29/17.
 */

public class RideFareRVAdapter extends RecyclerView.Adapter<RideFareRVAdapter.ViewHolder> {

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dayFormatter = new SimpleDateFormat("MMM, dd, yyyy", Locale.US);


    ArrayList<FareItem> fareItemArrayList;
    Context context;

    public RideFareRVAdapter(ArrayList<FareItem> fareItemArrayList){
        this.fareItemArrayList = fareItemArrayList;
        dayFormatter.setTimeZone(TimeZone.getDefault());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_fare_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FareItem fareItem = fareItemArrayList.get(position);

        switch (fareItem.status){
            case "paid":
                holder.fareStatus.setImageResource(R.drawable.dot_green);
                break;
            case "pending":
                holder.fareStatus.setImageResource(R.drawable.dot_yellow);
                break;
            case "notYet":
                holder.fareStatus.setImageResource(R.drawable.dot_red);
                break;
        }

        holder.fareValue.setText(String.valueOf(fareItem.value) + " L.E");

        calendar.setTimeInMillis(fareItem.timeInMillis);
        String date = dayFormatter.format(calendar.getTime());

        holder.fareDate.setText(date);

    }

    @Override
    public int getItemCount() {
        return fareItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView fareStatus;
        TextView fareValue, fareDate;

        public ViewHolder(View itemView) {
            super(itemView);

            fareStatus = (ImageView) itemView.findViewById(R.id.fare_status);
            fareValue = (TextView) itemView.findViewById(R.id.fare_value);
            fareDate = (TextView) itemView.findViewById(R.id.fare_date);
        }
    }
}
