package com.gamze.pawsbook.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.gamze.pawsbook.Activities.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    //constructor
     public AdapterUsers(Context context, List<ModelUser> userList) {
         this.context = context;
         this.userList = userList;
     }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         //row_user.xml layout'u dahil et
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
         //get data
        final String herUID =  userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();

        //set data
        holder.nameTxt.setText(userName);
        holder.emailTxt.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.mipmap.default_pic_foreground)
                    .into(holder.avatar);
        }
        catch (Exception e){

        }

        //itemlere onClick özelliği ekleme
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //dialog'un gösterilmesi
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            if (which == 0){
                                //profile tıklandı
                                //tıklandığında tıklayan kullanıcın uid'sini kullanarak ThereProfileActivity'e gidebilmek için
                                //ThereProfileActivity-> kullanıcının kendi gönderilerinin ve verilerinin gösterilmesi için
                                Intent intent = new Intent(context, ThereProfileActivity.class);
                                intent.putExtra("uid",herUID);
                                context.startActivity(intent);
                            }
                            else if (which == 1){
                                //chat tıklandı
                                //kullanıcı listesindeki kullanıcıya tıklandığında chat başlaması için
                                //alıcı UID'sini koyarak  activity başlatılcak
                                //UID'yi hangi kullanıcıyıla chat yapacagımızı belirlemek için kullandım
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("herUid", herUID);
                                context.startActivity(intent);
                            }
                    }
                });
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size() ;
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //Layout views
        ImageView avatar;
        TextView nameTxt, emailTxt;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            nameTxt = itemView.findViewById(R.id.nameTxt);
            emailTxt = itemView.findViewById(R.id.emailTxt);

        }
    }



}
