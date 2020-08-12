package com.gamze.pawsbook.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Models.ModelPost;
import com.gamze.pawsbook.R;
import com.gamze.pawsbook.Activities.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //row_post.xml layoutunu dahil etme
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
     }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //get data
        final String uid = postList.get(position).getPost_uid();
        String uEmail = postList.get(position).getPost_email();
        String uName = postList.get(position).getPost_name();
        String uDp = postList.get(position).getPost_dp();
        final String pId = postList.get(position).getPost_Id();
        String pTitle = postList.get(position).getPost_title();
        String pDescription = postList.get(position).getPost_desc();
        final String pImage = postList.get(position).getPost_image();
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
            //imageview göster
            holder.pImage_Imw.setVisibility(View.VISIBLE);

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
                showMoreOptions(holder.more_Btn, uid, myUid, pId, pImage);
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
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tıklandığında tıklayan kullanıcın uid'sini kullanarak ThereProfileActivity'e gidebilmek için
                //ThereProfileActivity-> kullanıcının kendi gönderilerinin ve verilerinin gösterilmesi için
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });
    }

    private void showMoreOptions(ImageButton more_btn, String uid, String myUid, final String pId, final String pImage) {
        //postu silme işlemi için popup menu
        PopupMenu popupMenu = new PopupMenu(context, more_btn, Gravity.END);

        //delete seçeneğini sadece mevcut giriş yapmış kullanıcıya göster
        if (uid.equals(myUid)) {
            //Menuye item ekleme
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1,0, "Edit");
        }

        //menu itemlerine onClick özelliği ekleme
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    //delete tıklandı
                    beginDelete(pId, pImage);
                }
                else if(id == 1){
                    //edit tıklandı
                    //key "editPost" ve id ile tıklandığında AddPostActivity'i başlat
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        //menuyu gösterme
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //gönderi resimli veya resimsiz olabilir
        if (pImage.equals("noImage")){
            //resimsiz gönderi
            deleteWithOutImage(pId);
        }
        else {
            //resimli gönderi
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        //postu ilk önce url'ini kullanarak sil. Daha sonra gönderinin id'sini kullanarak veritabanından siler
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //resim silindi, şimdi veritabanından sil
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_Id").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();//pid'nin eşleştiği yerde değerleri firebase'den kaldır
                                }
                                //deleted
                                Toast.makeText(context,"Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                pd.dismiss();
                Toast.makeText(context,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithOutImage(String pId) {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_Id").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();//pid'nin eşleştiği yerde değerleri firebase'den kaldır
                }
                //deleted
                Toast.makeText(context,"Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        LinearLayout profileLayout;

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
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
