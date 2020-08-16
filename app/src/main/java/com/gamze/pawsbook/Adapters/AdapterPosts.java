package com.gamze.pawsbook.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Models.ModelPost;
import com.gamze.pawsbook.Activities.PostDetailActivity;
import com.gamze.pawsbook.R;
import com.gamze.pawsbook.Activities.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    private DatabaseReference likesRef; //likes veritabanı için
    private DatabaseReference postsRef;//posts reference

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
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        final String uid = postList.get(position).getPost_uid();
        String uEmail = postList.get(position).getPost_email();
        String uName = postList.get(position).getPost_name();
        String uDp = postList.get(position).getPost_dp();
        final String pId = postList.get(position).getPost_Id();
        final String pTitle = postList.get(position).getPost_title();
        final String pDescription = postList.get(position).getPost_desc();
        final String pImage = postList.get(position).getPost_image();
        String pTimeStamp = postList.get(position).getPost_time();
        String pComments = postList.get(position).getPost_comments();

        //zaman göstergesini dd/mm/yyyy hh:mm am/pm şekline dönüştür
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uName_Txt.setText(uName);
        holder.pTime_Txt.setText(pTime);
        holder.pTitle_Txt.setText(pTitle);
        holder.pDesc_Txt.setText(pDescription);
        holder.pComment_Txt.setText(pComments+" Comments");



        //set user dp
        try{
            Picasso.get().load(uDp).placeholder(R.mipmap.default_pic_foreground).into(holder.uPicture_Imw);
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
        holder.comment_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PostDetailActivity başlat
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId); //bu kimliği kullanarak gönderinin ayrıntılarını alacak, tıklanan gönderinin kimliği
                context.startActivity(intent);
            }
        });
        holder.share_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fotograf içeren ya da içermeyen iki tür gönderide ele alınacak

                //imageview'den resim almak için
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImage_Imw.getDrawable();
                if (bitmapDrawable == null){
                    //resimsiz gönderi
                    shareTextOnly(pTitle,pDescription);
                }
                else {
                    //resimli gönderi

                    //resmi bitmap'e çevirme
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
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

    private void shareTextOnly(String pTitle, String pDescription) {
        //paylaşmak için başlığı ve açıklamayı birleştir
        String shareBody = pTitle +"\n" + pDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); //eposta uygulaması ile paylaşılması durumunda
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //paylaşılacak metin
        context.startActivity(Intent.createChooser(sIntent, "Share via")); //shareDialog ta gösterilecek mesaj

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //önce resmi önbelleğe kaydetmemiz gerekiyor
        //kaydedilecek resmin uri'sini almak için
        Uri uri = saveImageToShare(bitmap);

        //paylaşmak için başlığı ve açıklamayı birleştir
        String shareBody = pTitle +"\n" + pDescription;


        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); //yoksa oluştur
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.gamze.pawsbook.fileprovider", file);

        }
        catch (Exception e){
            Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return uri;
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
        popupMenu.getMenu().add(Menu.NONE, 2,0, "View Detail");


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
                else if(id == 2){
                    //PostDetailActivity başlat
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId); //bu kimliği kullanarak gönderinin ayrıntılarını alacak, tıklanan gönderinin kimliği
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
        TextView uName_Txt, pTime_Txt, pTitle_Txt, pDesc_Txt, pComment_Txt;
        ImageButton more_Btn,  comment_Btn, share_Btn;
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
            pComment_Txt = itemView.findViewById(R.id.pComments_Txt);
            more_Btn = itemView.findViewById(R.id.more_Btn);
            comment_Btn = itemView.findViewById(R.id.comment_Btn);
            share_Btn = itemView.findViewById(R.id.share_Btn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
