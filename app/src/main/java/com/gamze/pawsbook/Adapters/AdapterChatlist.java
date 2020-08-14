package com.gamze.pawsbook.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Activities.ChatActivity;
import com.gamze.pawsbook.Models.ModelUser;
import com.gamze.pawsbook.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUser> userList; //kullanıcı bilgilerini almal için
    private HashMap<String, String> lastMessageMap;


    //constructor
    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //row_chatlist.xml adaptere bağlanması
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String herUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(herUid);

        //set data
        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.tag_face).into(holder.profileIv);

        }
        catch (Exception e){
            Picasso.get().load(R.drawable.tag_face).into(holder.profileIv);
        }

        //chatlist'deki diğer kullanıcıların online statulerini kur
        if (userList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        //chatlist teki kullanıcılara onClick özelliği ekleme
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //üzerine tıklanan kullanıcı ile chat activity başlat
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("herUid", herUid);
                context.startActivity(intent);
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //row_chatlist.xml views
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);



        }
    }


}
