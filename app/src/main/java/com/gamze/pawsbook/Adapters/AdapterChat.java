package com.gamze.pawsbook.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Models.ModelChat;
import com.gamze.pawsbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;

    //layout views
    ImageView profilePic;
    TextView messageTxt, timeTxt, isSeenTxt;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //layout'ları dahil et: alıcı için row_chat_left.xml, gönderen için row_chat_right.xml
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        //timeStamp'ı dd/mm/yyyy hh:mm am/pm şekline dönüştürme
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.messageTxt.setText(message);
        holder.timeTxt.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(holder.profilePic);
        }
        catch (Exception e) {

        }

        //Mesajın ietildi/görüldü kısmının ayarlanması
        if (position == chatList.size() -1 ) {
            if (chatList.get(position).isSeen()){
                holder.isSeenTxt.setText("Seen");
            }
            else{
                holder.isSeenTxt.setText("Delivered");
            }
        }
        else{
            holder.isSeenTxt.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //mevcut oturum açmış kullanıcıyı al
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //layout views
        ImageView profilePic;
        TextView messageTxt, timeTxt, isSeenTxt;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            profilePic = itemView.findViewById(R.id.profilePic);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            isSeenTxt = itemView.findViewById(R.id.isSeenTxt);


        }
    }
}
