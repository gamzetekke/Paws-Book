package com.gamze.pawsbook.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Models.ModelChat;
import com.gamze.pawsbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
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
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        //timeStamp'ı dd/mm/yyyy hh:mm am/pm şekline dönüştürme
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.messageTxt.setText(message);
        holder.timeTxt.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(holder.profilePic);
        }
        catch (Exception e) {

        }

        //tıklandığında delete dialog gözükmesi için
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete mesajını confirm dialog ile gösterilmesi
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure the delete this message?");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                //cancel delete button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });

                //dialog oluştur ve göster
                builder.create().show();

            }
        });

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

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //tıklanan mesajın zamanını alıp sohbetteki tüm mesajların zamanlarıyla karşılaştırır
        //her iki değerin eşleştiği yerde mesajı siler
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //kullanıcın sadece kendi yolladığı mesajı silebilmesi için sender ve myUID birbirine eşit olmalı
                    if (ds.child("sender").getValue().equals(myUID)){
                        //mesajın kaldırılması
                         //ds.getRef().removeValue(); -> bu metot da kullanılabilir
                        //kaldırılan mesajın yerine "This message was delete.." yazısının gelmesi
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context,"message deleted...",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context,"You can delete only your messages...",Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        LinearLayout messageLayout; //tıklandığında silme işleminin gösterilmesi için

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            profilePic = itemView.findViewById(R.id.profilePic);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            isSeenTxt = itemView.findViewById(R.id.isSeenTxt);
            messageLayout = itemView.findViewById(R.id.messageLayout);


        }
    }
}
