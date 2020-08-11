package com.gamze.pawsbook.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    //Log Statment
    private static final String TAG = "ProfileFragment";

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //firabase storage
    StorageReference storageReference;
    //profil resminin ve kapak resminin nerede depolanacağının yolu
    String storagePath = "Users_Profile_Cover_Imgs/";

    //layout views
    ImageView avatar, coverPhoto;
    TextView nameTxt, emailTxt, descTxt;
    FloatingActionButton fab;

    //Progress Dialog
    ProgressDialog pd;

    //permissions
    private static final int CAMERA_REQUESTED_CODE = 100;
    private static final int STORAGE_REQUESTED_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //izinler için gerekli arrayler
    String cameraPermissions[];
    String storagePermissions[];

    //seçilen resmin uri adresi
    Uri image_uri;

    //profil fotografı yoksa kapak fotografımı kontrol et
    String profileORCoverPhoto;



    public ProfileFragment() {
        //boş public constructor gerekli
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        storageReference = FirebaseStorage.getInstance().getReference();  //Firebase Storage Reference

        //init layout views
        avatar = view.findViewById(R.id.avatar);
        coverPhoto = view.findViewById(R.id.coverPhoto);
        nameTxt = view.findViewById(R.id.nameTxt);
        emailTxt = view.findViewById(R.id.emailTxt);
        descTxt = view.findViewById(R.id.descTxt);
        fab = view.findViewById(R.id.fab);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        //izin arrayleri
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        /* giriş yapan kullanıcıların bilgilerini email yada uid kullanarak çekmek zorundayız
        Kullanıcı detaylarını email adreslerini kullanarak çekicez
        orderbyChild query kullanarak giriş yapılan email ile email key ini eşleştirerek kullanıcı detaylarına ulaşılıyor
         */
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //gerekli veriler gelene kadar kontrol et
                for (DataSnapshot ds: snapshot.getChildren()){
                    //verileri almak için
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String desc = ""+ ds.child("desc").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();



                    //set data
                    nameTxt.setText(name);
                    emailTxt.setText(email);
                    descTxt.setText(desc);
                    Log.d(TAG, "onDataChange: \nName: "+name+" email: "+ email);


                    try {
                        // resim alınırsa ayarla
                        Picasso.get().load(image).into(avatar);
                    } catch (Exception e){
                        // resim alınırken herangi bir sıkıntı varsa varsayılan olarak ayarla
                        Picasso.get().load(R.drawable.add_photo_foreground).into(avatar);
                    }

                    try {
                        // kapak resimi alınırsa ayarla
                        Picasso.get().load(cover).into(coverPhoto);
                    } catch (Exception e){
                        // kapak resmi alınırken herangi bir sıkıntı varsa varsayılan olarak ayarla
                        Picasso.get().load(R.drawable.default_cover_photo).into(coverPhoto);
                    }
                }
                Log.d(TAG, "onDataChange: Data is empty");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error);
            }
        });

        //fab button onClick özelliği ekleme
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit profile dialog göster
                showEditProfileDialog();
            }
        });

        return view;
    }


    private void requestStoragePermission(){
        //runtime depolama izinleri isteme
        requestPermissions(storagePermissions, STORAGE_REQUESTED_CODE);
    }
    private void requestCameraPermission(){
        //runtime depolama izinleri isteme
        requestPermissions(cameraPermissions, CAMERA_REQUESTED_CODE);
    }

    private boolean checkStoragePermission(){
        //depolama izinlerini kontrol et, etkinse true, değilse false döndür
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private boolean checkCameraPermission(){
        //depolama izinlerini kontrol et, etkinse true, değilse false döndür
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return  result && result1;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Bu yöntem, kullanıcı izin isteği iletişim kutusundan izin ver veya reddet düğmesine bastığında çağrılır
        //burada izin durumlarını ele alınır(izin verildi ve reddedildi)
        switch (requestCode){
            case CAMERA_REQUESTED_CODE: {
                //camera seçildiğinde önce izin alınmış mı diye kontrol eder
                if (grantResults.length > 0 ){
                    boolean cameraAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //izinler etkinleştirildi
                        pickFromCamera();
                    }
                    else{
                        //izinler reddedildi
                        Toast.makeText(getActivity(),"Please enable camera && storage permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            //galeri seçildiğinde önce izin alınmış mı diye kontrol eder
            case STORAGE_REQUESTED_CODE: {
                if (grantResults.length > 0 ){
                    boolean writeStorageAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        //izinler etkinleştirildi
                        pickFromGallery();
                    }
                    else{
                        //izinler reddedildi
                        Toast.makeText(getActivity(),"Please enable storage permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //bu metot camera veya galeri den fotograf seçildikten sonra çağırılıyor
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //resim geleriden seçildi, resmin uri'sini al
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //resim cameradan seçildi, resmin uri'sini al

                uploadProfileCoverPhoto(image_uri);

            }


        }


        super.onActivityResult(requestCode, resultCode, data);
    }



    //Editprofile dialog
    private void showEditProfileDialog() {
        // Profil resmi düzenle, kapak fotografı gösterme, isim düzenleme, açıklama düzenleme
        //Seçenekleri dialogta gösterme
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Description"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //başlık
        builder.setTitle("Choose Action");
        //dialog itemlerini ayarlama
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog itemlere onClick özelliği ekleme
                if (which == 0){
                    //profil düzenleme tıklandı
                    pd.setMessage("Updating Profile Picture");

                    //profil foto mu yoksa kapak foto mu konrol için
                    profileORCoverPhoto = "image"; // profil resmini değiştirirken aynı değerlerin atandığından emin ol

                    showImagePicDialog();

                }
                else if (which == 1){
                    //kapak düzenleme tıklandı
                    pd.setMessage("Updating Cover Picture");

                    //profil foto mu yoksa kapak foto mu konrol için
                    profileORCoverPhoto = "cover"; //kapak resmini değiştirirken aynı değerlerin atandığından emin ol

                    showImagePicDialog();
                }
                else if (which == 2){
                    //isim duzenleme tıklandı
                    pd.setMessage("Updating Name");

                    //veritabanında "name" güncellenmesi için motodun çağırılması
                    showNameDescUpdateDialog("name");

                }
                else if (which == 3){
                    //açıklama düzenleme tıklandı
                    pd.setMessage("Updating Description");

                    showNameDescUpdateDialog("desc");


                }

            }
        });
        //dialog oluşturme ve gösterme
        builder.create().show();
    }


    private void showNameDescUpdateDialog(final String key) {
        //key parametresi "name" ve "desc" değerlerini alıyor

        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key); //Update name or Update description..

        //Dialog layoutunu ayarla
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //editText ekleme
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+ key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //dialog'a update button ekle
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //kullanıcının bir şeyler girip girmediğini onayla
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //güncellendi, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Updated...",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error mesajı al ve göster, dismiss progress
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Please Enter "+key, Toast.LENGTH_SHORT).show();
                }

            }
        });

        //dialog'a cancel button ekle
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        //dialog'u oluştur ve göster
        builder.create().show();
    }


    private void showImagePicDialog() {
        //profil resmini cameradan ya da galeriden seçme seçenekleri ekleme
        String options[] = {"Camera", "Gallery"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //başlık
        builder.setTitle("Pick Image From");
        //dialog itemlerini ayarlama
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog itemlere onClick özelliği ekleme
                if (which == 0){
                    //camera tıklandı
                    // pd.setMessage("Updating Profile Picture");
                    //showImagePicDialog();
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //galeri tıklandı
                    //pd.setMessage("Updating Cover Picture");
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        //dialog oluşturme ve gösterme
        builder.create().show();

    }


    private void uploadProfileCoverPhoto(final Uri uri) {
        //Show progress dialog
        pd.show();

        //iki ayrı fonksiyon yerine profil resmi ve kapak resmi aynı fonksiyonda

        //firabase storage'da depolanan resmin yolu ve adı
        String filePathAndName = storagePath+ ""+ profileORCoverPhoto + "_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //resim depoya yüklendi, şimdi url'sini al ve kullanıcı veritabanında sakla
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //resmin yüklenip yüklenmediğini ve url'nin alındığını kontrol edin
                        if (uriTask.isSuccessful()){
                            //resim yüklendi
                            //kullanıcı veritabanına url'i ekle/güncelle
                            HashMap<String, Object> results = new HashMap<>();

                            //ilk parametre profileOrCoverPhote image veya cover değerlerine sahip
                            //ikinci parametre firebase storage'da depolanan resmin url
                            results.put(profileORCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                         //bazı errorlar var, errorları al ve error mesajı göster, dissmis dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_SHORT).show();

                 }
        });

    }

    private void pickFromCamera() {
        //cihaz kamerasından görüntü alma
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //resim uri'si
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //camera başlatılması
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }

    private void pickFromGallery() {
        //galeriden resim seçme
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }


    private void checkUserStatus() {

        //mevcut kullanıcıyı al
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //kullanıcı giriş yapmışsa burada kal
            //giriş yapan kullanıcının email i
            //profileTxt.setText(user.getEmail());
        }
        else{
            //kullanıcı giriş yapmamışsa main activity'e git
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//fragment'te options menuyu göstermek için
        super.onCreate(savedInstanceState);
    }

    //options menu dahil etme
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        //menuyu dahil etme
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //menu itemlerine onClick özelliği aktifleştirme
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //itemlerin id'lerini al
        int id = item.getItemId();

        if (id == R.id.action_logout){
            //hesaptan çıkış yap
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add){
            //AddPostActivity açmak için
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}