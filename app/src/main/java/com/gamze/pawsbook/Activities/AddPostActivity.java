package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gamze.pawsbook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    //action bar
    ActionBar actionBar;

    //permissions constant
    private static final int CAMERA_REQUESTED_CODE = 100;
    private static final int STORAGE_REQUESTED_CODE = 200;
    //image pick constans
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    //permissions arrays
    String[] cameraPermissions;
    String[] storagePermissions;

    //firebase auth
    FirebaseAuth firebaseAuth;

    //Database reference
    DatabaseReference userDbRef;

    String name, email, uid, dp;

    //düzenlenecek postun bilgileri
    String editTitle, editDescription, editImage;

    //layout views
    EditText postTitle_Edt, postDesc_Edt;
    ImageView postImage_Imw;
    Button postUpload_Btn;

    //seçilen resim bu uri'de isimlendirilecek
    Uri image_uri = null;

    //progress bar
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        //action bar
         actionBar = getSupportActionBar();
         actionBar.setTitle("Add New Post");
         actionBar.setSubtitle(email);

        //geri tuşu aktifleştirme
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permissions arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init layout views
        postTitle_Edt = findViewById(R.id.postTitle_Edt);
        postDesc_Edt = findViewById(R.id.postDesc_Edt);
        postImage_Imw = findViewById(R.id.postImage_Imw);
        postUpload_Btn = findViewById(R.id.postUpload_Btn);

        //önceki activity'nin adapterinden intent yoluyla veri alma
        Intent intent = getIntent();
        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editPostId = ""+intent.getStringExtra("editPostId");

        if (isUpdateKey.equals("editPost")){
            //update
            actionBar.setTitle("Update Post");
            postUpload_Btn.setText("Update");
            loadPostData(editPostId);
        }
        else{
            //add
            actionBar.setTitle("Add New Post");
            postUpload_Btn.setText("Upload");


        }


        //progress dialog
        pd = new ProgressDialog(this);

        //gönderi için mevcut kullanı hakkında bazı bilgileri alır
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    dp = ""+ ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //tıklandığından kamera veya galeriden fotograf alma
        postImage_Imw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //imagePickDialog göster
                showImagePickDialog();
            }
        });

        //postUpload_Btn onClick özelliği ekleme
        postUpload_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //başlık ve açıklama verilerini ilgili editText'lerden alma
                String title = postTitle_Edt.getText().toString().trim();
                String description = postDesc_Edt.getText().toString().trim();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (isUpdateKey.equals("editPost")){
                    beginUpdate(title, description, editPostId);
                }
                else {
                    uploadData(title, description);
                }
            }
        });
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();

        if (editImage != null && !editImage.equals("noImage")){
            //resimle berabar
            updateWasWithImage(title, description, editPostId);
        }
        else if(postImage_Imw.getDrawable() != null){
            //resimle beraber
            updateWithNowImage(title, description, editPostId);
        }
        else {
            //resim yokken
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        //post bilgilerini koy
        hashMap.put("post_id", uid);
        hashMap.put("post_name", name);
        hashMap.put("post_email", email);
        hashMap.put("post_dp", dp);
        hashMap.put("post_title", title);
        hashMap.put("post_desc", description);
        hashMap.put("post_image", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated...",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" +timeStamp;

        //imageView den resmi al
        Bitmap bitmap = ((BitmapDrawable) postImage_Imw.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //görüntüyü sıkıştırma
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //resim yüklendi. yüklenen resmin url'ini al
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            //url alındı, firebase database'i güncelle
                            HashMap<String, Object> hashMap = new HashMap<>();
                            //post bilgilerini koy
                            hashMap.put("post_id", uid);
                            hashMap.put("post_name", name);
                            hashMap.put("post_email", email);
                            hashMap.put("post_dp", dp);
                            hashMap.put("post_title", title);
                            hashMap.put("post_desc", description);
                            hashMap.put("post_image", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated...",Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {
        //gönderi resim içeriyorsa ilk olarak önceki resmi sil
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //resim silindi, yeni resmi yükle
                        //post image-name, post id,publish time için
                        final String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" +timeStamp;

                        //imageView den resmi al
                        Bitmap bitmap = ((BitmapDrawable) postImage_Imw.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //görüntüyü sıkıştırma
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //resim yüklendi. yüklenen resmin url'ini al
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()){
                                            //url alındı, firebase database'i güncelle
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            //post bilgilerini koy
                                            hashMap.put("post_id", uid);
                                            hashMap.put("post_name", name);
                                            hashMap.put("post_email", email);
                                            hashMap.put("post_dp", dp);
                                            hashMap.put("post_title", title);
                                            hashMap.put("post_desc", description);
                                            hashMap.put("post_image", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated...",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //post detaylarını postun id'sini kullanarak al
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    editTitle = ""+ds.child("post_title").getValue();
                    editDescription = ""+ds.child("post_desc").getValue();
                    editImage = ""+ds.child("post_image").getValue();

                    //set data
                    postTitle_Edt.setText(editTitle);
                    postDesc_Edt.setText(editDescription);

                    //set image
                    if (!editImage.equals("noImage")){
                        try {
                            Picasso.get().load(editImage).into(postImage_Imw);
                        }
                        catch (Exception e){

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        pd.setMessage("Publishing post...");
        pd.show();

        //gönderi resmi, ismi ve yayın tarihi için
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (postImage_Imw.getDrawable() != null){

            //imageView den resmi al
            Bitmap bitmap = ((BitmapDrawable) postImage_Imw.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //görüntüyü sıkıştırma
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();



            //resimli gönderi
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //resim firebase storage' da güncellendi, şimdi resmin url'sini al
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()){
                        //url firebase database'e yüklendi
                        HashMap<String, Object> hashMap = new HashMap<>();
                        //gönderi bilgilerini koy
                        hashMap.put("post_uid",uid);
                        hashMap.put("post_name", name);
                        hashMap.put("post_email", email);
                        hashMap.put("post_dp", dp);
                        hashMap.put("post_Id", timeStamp);
                        hashMap.put("post_title", title);
                        hashMap.put("post_desc", description);
                        hashMap.put("post_image", downloadUri);
                        hashMap.put("post_time", timeStamp);

                        //gönderi verilerini depolamanın yolu
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        //verileri koyma
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //veriler veritabanına eklendi
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this,"Post published...", Toast.LENGTH_SHORT).show();

                                //reset views
                                postTitle_Edt.setText("");
                                postDesc_Edt.setText("");
                                postImage_Imw.setImageURI(null);
                                image_uri = null;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //post veridatabınıa eklenirken başarısız oldu
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //resim güncellemesi başarısız oldu
                  pd.dismiss();
                    Toast.makeText(AddPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            //resimsiz gönderi

            HashMap<String, Object> hashMap = new HashMap<>();
            //gönderi bilgilerini koy
            hashMap.put("post_uid",uid);
            hashMap.put("post_name", name);
            hashMap.put("post_email", email);
            hashMap.put("post_dp", dp);
            hashMap.put("post_Id", timeStamp);
            hashMap.put("post_title", title);
            hashMap.put("post_desc", description);
            hashMap.put("post_image", "noImage");
            hashMap.put("post_time", timeStamp);

            //gönderi verilerini depolamanın yolu
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //verileri koyma
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //veriler veritabanına eklendi
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,"Post published...", Toast.LENGTH_SHORT).show();

                    //reset views
                    postTitle_Edt.setText("");
                    postDesc_Edt.setText("");
                    postImage_Imw.setImageURI(null);
                    image_uri = null;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //post veridatabınıa eklenirken başarısız oldu
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
        //dialogta seçenekleri göster (camera, gallery)
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");

        //seçenekleri dialog için ayarla
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                if (which == 0){
                    //camera tıklandı
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //gallery tıklandı
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }

            }
        });

        //dialog oluştur ve göster
        builder.create().show();
    }

    private void pickFromGallery() {
        //galeriden resim seçmek için intent
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //cameradan resim seçmek için intent
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission(){
        //depolama izinleri alınmışsa true, alınmamışsa false döndür
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //gerekli runtime depolama izinleri
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUESTED_CODE);

    }

    private boolean checkCameraPermission(){
        //camera izinleri alınmışsa true, alınmamışsa false döndür
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //gerekli runtime camera izinleri
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUESTED_CODE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {

        //mevcut kullanıcıyı al
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //kullanıcı giriş yapmışsa burada kal
            //giriş yapan kullanıcının email ve uid'si
            email = user.getEmail();
            uid = user.getUid();

        } else {
            //kullanıcı giriş yapmamışsa main activity'e git
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//önceki activity'e git

        return super.onSupportNavigateUp();
    }

    //handle permissions results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Bu yöntem, kullanıcı izin isteği iletişim kutusundan izin ver veya reddet düğmesine bastığında çağrılır
        //burada izin durumlarını ele alınır(izin verildi ve reddedildi)

        switch (requestCode){
            case CAMERA_REQUESTED_CODE: {
                //camera seçildiğinde önce izin alınmış mı diye kontrol eder
                if (grantResults.length > 0 ){
                    boolean cameraAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //izinler etkinleştirildi
                        pickFromCamera();
                    }
                    else{
                        //izinler reddedildi
                        Toast.makeText(this,"Please enable camera && storage permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            //galeri seçildiğinde önce izin alınmış mı diye kontrol eder
            case STORAGE_REQUESTED_CODE: {
                if (grantResults.length > 0 ){
                    boolean storageAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //izinler etkinleştirildi
                        pickFromGallery();
                    }
                    else{
                        //izinler reddedildi
                        Toast.makeText(this,"Please enable storage permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //bu metot camera veya galeri den fotograf seçildikten sonra çağırılıyor
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //resim geleriden seçildi, resmin uri'sini al
                image_uri = data.getData();

                //imageView'e yollanması
                postImage_Imw.setImageURI(image_uri);


            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //resim cameradan seçildi, resmin uri'sini al

                //imageView'e yollanması
                postImage_Imw.setImageURI(image_uri);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}


















