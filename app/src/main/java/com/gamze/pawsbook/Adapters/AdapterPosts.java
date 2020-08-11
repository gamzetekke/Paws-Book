package com.gamze.pawsbook.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Models.ModelPost;
import com.gamze.pawsbook.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //row_post.xml layoutunu dahil etme
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
     }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = postList.get(position).getPost_uid();
        String uEmail = postList.get(position).getPost_email();
        String uName = postList.get(position).getPost_name();
        String uDp = postList.get(position).getPost_dp();
        String pId = postList.get(position).getPost_Id();
        String pTitle = postList.get(position).getPost_title();
        String pDescription = postList.get(position).getPost_desc();
        String pImage = postList.get(position).getPost_image();
        String pTimeStamp = postList.get(position).getPost_time();

        //zaman göstergesini dd/mm/yyyy hh:mm am/pm şekline dönüştür
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uName_Txt.setText(uName);
        holder.pTime_Txt.setText(pTime);
        holder.pTitle_Txt.setText(pTitle);
        holder.pDesc_Txt.setText(pDescription);


        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.tag_face).into(holder.uPicture_Imw);
        }
        catch (Exception e){
        }

        //set post image
        //eğer resim yoksa imageView'i gizle
        if (pImage.equals("noImage")){
            //imageView'i gizle
            holder.pImage_Imw.setVisibility(View.GONE);
        }
        else {
            try{
                Picasso.get().load(pImage).into(holder.pImage_Imw);
            }
            catch (Exception e){
            }
        }



        //button clicks
        holder.more_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"More", Toast.LENGTH_SHORT).show();
            }
        });
        holder.like_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Like", Toast.LENGTH_SHORT).show();
            }
        });
        holder.comment_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.share_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Share", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //rows_post.xml view'leri
        ImageView uPicture_Imw, pImage_Imw;
        TextView uName_Txt, pTime_Txt, pTitle_Txt, pDesc_Txt, pLikes_Txt;
        ImageButton more_Btn, like_Btn, comment_Btn, share_Btn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPicture_Imw = itemView.findViewById(R.id.uPicture_Imw);
            pImage_Imw = itemView.findViewById(R.id.pImage_Imw);
            uName_Txt = itemView.findViewById(R.id.uName_Txt);
            pTime_Txt = itemView.findViewById(R.id.pTime_Txt);
            pTitle_Txt = itemView.findViewById(R.id.pTitle_Txt);
            pDesc_Txt = itemView.findViewById(R.id.pDesc_Txt);
            pLikes_Txt = itemView.findViewById(R.id.pLikes_Txt);
            more_Btn = itemView.findViewById(R.id.more_Btn);
            like_Btn = itemView.findViewById(R.id.like_Btn);
            comment_Btn = itemView.findViewById(R.id.comment_Btn);
            share_Btn = itemView.findViewById(R.id.share_Btn);

        }
    }
}
