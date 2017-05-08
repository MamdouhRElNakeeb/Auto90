package com.be4em.auto90.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.be4em.auto90.R;
import com.be4em.auto90.object.ChatItem;

import java.util.ArrayList;

/**
 * Created by mamdouhelnakeeb on 5/8/17.
 */

public class ChatRVAdapter extends RecyclerView.Adapter<ChatRVAdapter.ViewHolder> {

    Context context;
    ArrayList<ChatItem> chatItemArrayList;

    public ChatRVAdapter(Context context, ArrayList<ChatItem> chatItemArrayList){

        this.context = context;
        this.chatItemArrayList = chatItemArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ChatItem chatItem = chatItemArrayList.get(position);

        if (chatItem.userID.equals("1")){
            holder.msgItemLL.setScaleX(-1);
            holder.userPPCV.setVisibility(View.GONE);
            holder.userNameTV.setVisibility(View.GONE);
            holder.msgItemCV.setScaleX(-1);
            holder.msgItemCV.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.msgTV.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.msgTV.setText(chatItem.msg);
            holder.chatTri1.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.chatTri2.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        }
        else {
            holder.userNameTV.setText(chatItem.userName);
            holder.msgTV.setText(chatItem.msg);
        }

    }

    @Override
    public int getItemCount() {
        return chatItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView userPPCV, msgItemCV;
        TextView userNameTV, msgTV;
        LinearLayout msgItemLL;
        ImageView chatTri1, chatTri2;

        public ViewHolder(View itemView) {
            super(itemView);

            userPPCV = (CardView) itemView.findViewById(R.id.chat_message_userImgCV);
            msgItemCV = (CardView) itemView.findViewById(R.id.msgItemCV);
            userNameTV = (TextView) itemView.findViewById(R.id.chat_message_userNameTV);
            msgTV = (TextView) itemView.findViewById(R.id.chat_message_textTV);
            msgItemLL = (LinearLayout) itemView.findViewById(R.id.chat_message_itemLL);

            chatTri1 = (ImageView) itemView.findViewById(R.id.chatTri1);
            chatTri2 = (ImageView) itemView.findViewById(R.id.chatTri2);
        }
    }
}
