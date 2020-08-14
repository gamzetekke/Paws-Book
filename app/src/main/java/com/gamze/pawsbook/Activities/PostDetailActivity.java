package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.Adapters.AdapterComments;
import com.gamze.pawsbook.Adapters.AdapterPosts;
import com.gamze.pawsbook.Models.ModelComment;
import com.gamze.pawsbook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    //kullanıcının ve post'un detaylarını almak için
    String myUid, myEmail, myName, myDp, postId, herDp, herName, herUid, pImage;

    //comment process
    boolean mProcessComment = false;

    //ProgressBar
    ProgressDialog pd;

    //views
    ImageView uPicture_Imw, pImage_Imw;
    TextView uName_Txt, pTime_Txt, pTitle_Txt, pDesc_Txt, pComments_Txt;
    ImageButton more_Btn, share_Btn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //comment views
    EditText comment_Edt;
    ImageButton send_Btn;
    ImageView cAvatarImw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //action bar ve özellikleri
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //intent kullanarak gönderenin kimliğini al
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init views
        uPicture_Imw = findViewById(R.id.uPicture_Imw);
        pImage_Imw = findViewById(R.id.pImage_Imw);
        uName_Txt = findViewById(R.id.uName_Txt);
        pTime_Txt = findViewById(R.id.pTime_Txt);
        pTitle_Txt = findViewById(R.id.pTitle_Txt);
        pDesc_Txt = findViewById(R.id.pDesc_Txt);
        pComments_Txt = findViewById(R.id.pComments_Txt);
        more_Btn = findViewById(R.id.more_Btn);
        share_Btn = findViewById(R.id.share_Btn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        //comment views
        comment_Edt = findViewById(R.id.comment_Edt);
        send_Btn = findViewById(R.id.send_Btn);
        cAvatarImw = findViewById(R.id.cAvatarImw);


        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        //action bar'a alt başlık ekle
        actionBar.setSubtitle("SignedIn as: "+myEmail);
        
        loadComments();

        //comment butonuna onClick özelliği ekleme
        send_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //more butonuna onClick özelliği ekleme
        more_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        //share butonuna onClick özelliği ekleme
        share_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitle_Txt.getText().toString().trim();
                String pDescription = pDesc_Txt.getText().toString().trim();

                //fotograf içeren ya da içermeyen iki tür gönderide ele alınacak

                //imageview'den resim almak için
                BitmapDrawable bitmapDrawable = (BitmapDrawable) pImage_Imw.getDrawable();
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

    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //paylaşmak için başlığı ve açıklamayı birleştir
        String shareBody = pTitle +"\n" + pDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); //eposta uygulaması ile paylaşılması durumunda
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //paylaşılacak metin
        startActivity(Intent.createChooser(sIntent, "Share via")); //shareDialog ta gösterilecek mesaj

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
       startActivity(Intent.createChooser(sIntent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); //yoksa oluştur
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.gamze.pawsbook.fileprovider", file);

        }
        catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        //recyclerview için linearLayout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //layout'u recyclerview e ayarla
        recyclerView.setLayoutManager(layoutManager);

        //comments list
        commentList = new ArrayList<>();

        //yorumu alıcanak post'un yolu
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //myUid ve postId'yi AdapterComment constructor'un parametreleri olarak ayarladım



                    //adapterin kurulması
                    adapterComments = new AdapterComments(getApplicationContext(),commentList, myUid, postId);
                    //adapteri bağla
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions() {

        //postu silme işlemi için popup menu
        PopupMenu popupMenu = new PopupMenu(this, more_Btn, Gravity.END);

        //delete seçeneğini sadece mevcut giriş yapmış kullanıcıya göster
        if (herUid .equals(myUid)) {
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
                    beginDelete();
                }
                else if(id == 1){
                    //edit tıklandı
                    //key "editPost" ve id ile tıklandığında AddPostActivity'i başlat
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }

                return false;
            }
        });
        //menuyu gösterme
        popupMenu.show();

    }

    private void beginDelete() {
        //gönderi resimli veya resimsiz olabilir
        if (pImage.equals("noImage")){
            //resimsiz gönderi
            deleteWithOutImage();
        }
        else {
            //resimli gönderi
            deleteWithImage();
        }

    }

    private void deleteWithImage() {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        //postu ilk önce url'ini kullanarak sil. Daha sonra gönderinin id'sini kullanarak veritabanından siler
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //resim silindi, şimdi veritabanından sil
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_Id").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();//pid'nin eşleştiği yerde değerleri firebase'den kaldır
                                }
                                //deleted
                                Toast.makeText(PostDetailActivity.this,"Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithOutImage() {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_Id").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();//pid'nin eşleştiği yerde değerleri firebase'den kaldır
                }
                //deleted
                Toast.makeText(PostDetailActivity.this,"Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");

        //yorum editText'den verilerin alınması
        final String comment = comment_Edt.getText().toString().trim();

        //onayla
        if (TextUtils.isEmpty(comment)){
            //değer girilmedi
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //her gönderi sahip oldupu yorumları göstermek için "Comments" adında bir child'e sahiptir
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //bilgileri yerleştir
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        //bu verileri veritabanına yerleştir
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //eklendi
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this,"Comment Added...", Toast.LENGTH_SHORT).show();
                        comment_Edt.setText("");
                        updateCommentCount();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //başarısız, eklenmedi
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void updateCommentCount() {
        //kullanıcı yorum eklediği zaman comment count'u arttırır
        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment)
                {
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommnetVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommnetVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        //mevcut kullanıcının bilgilerini al
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    //set data
                    try {
                        //görüntü alınırsa ayarlar
                        Picasso.get().load(myDp).placeholder(R.drawable.tag_face).into(cAvatarImw);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.tag_face).into(cAvatarImw);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //postId'sini kullanarak post'u al
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //gerekli gönderiyi alana kadar gönderileri kontrol etmeye devam edin
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDesc = ""+ds.child("pDesc").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    herDp = ""+ds.child("uDp").getValue();
                    herUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    herName = ""+ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    //zaman göstergesini dd/mm/yyyy hh:mm am/pm şekline dönüştür
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    pTitle_Txt.setText(pTitle);
                    pDesc_Txt.setText(pDesc);
                    pTime_Txt.setText(pTime);
                    uName_Txt.setText(herName);
                    pComments_Txt.setText(commentCount+" Comments");

                    //postu gönderen kullanıcın resmini almak için
                    //Eğer post resimsiz ise pImage.equals("noImage") ve imageView'i gizle
                    if (pImage.equals("noImage")){
                        //imageView'i gizle
                        pImage_Imw.setVisibility(View.GONE);
                    }
                    else {
                        //imageView'i göster
                        pImage_Imw.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImage_Imw);
                        }
                        catch (Exception e){

                        }
                    }

                    //yorum kısmındaki kullancıı resmini ayarlama
                    try {
                        Picasso.get().load(herDp).placeholder(R.drawable.tag_face).into(uPicture_Imw);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.tag_face).into(uPicture_Imw);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user!=null){
            myEmail =user.getEmail();
            myUid = user.getUid();
        }
        else{
            //kullanıcı giriş yapmamışsa, maina activity'e git
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //bazı menu itemlerinin gizlenmesi
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //itemlerin id'lerini al
        int id = item.getItemId();

        if (id == R.id.action_logout){
            //hesaptan çıkış yap
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}