package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    //layout views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profilePic;
    TextView nameTxt, userStatusTxt;
    EditText message_editText;
    ImageButton sendBtn;

    //firebase auth
    FirebaseAuth firebaseAuth;

    //Firebase Database
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    String herUid;
    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init layout views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_recyclerView);
        profilePic = findViewById(R.id.profilePic);
        nameTxt = findViewById(R.id.nameTxt);
        userStatusTxt = findViewById(R.id.userStatusTxt);
        message_editText = findViewById(R.id.message_editText);
        sendBtn = findViewById(R.id.sendBtn);


        //Kullanıcı listesinden kullanıcının üzerine tıklayarak tıkladığımız kullanıcının UID'sini alıp intent ile geçişi sağladık
        // UID'sini aldığımız kullanıcının ismini ve profil resmini de buradan alıyoruz
        Intent intent = getIntent();
        herUid = intent.getStringExtra("herUid");

        //Firebase Auth başlatmak için
        firebaseAuth = FirebaseAuth.getInstance();

        //Firebase database başlatmak için
        firebaseDatabase =  firebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //Kullanıcıyı bilgilerini almak için ara
        Query userQuery =  usersDbRef.orderByChild("uid").equalTo(herUid);
        //kullanıcı profil resmini almak için
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //gerekli bilgi ulaşana kadar kontrol et
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    String image =""+ ds.child("image").getValue();

                    //set data
                    nameTxt.setText(name);
                    try {
                        //resim alındı, resmi imageView'e gönder
                        Picasso.get().load(image).placeholder(R.drawable.tag_face).into(profilePic);

                    }
                    catch (Exception e){
                        //profil resmini alırken bir sorun oluştuysa imageView'e varsayılan resmi koy
                        Picasso.get().load(R.drawable.tag_face).into(profilePic);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //sendBtn onClick özelliği ekleme
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editText'den girilen metinleri alma
                String message = message_editText.getText().toString().trim();
                //text'in boş olup olmadığını kontrol et
                if (TextUtils.isEmpty(message)){
                    //text boş
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...",Toast.LENGTH_SHORT).show();
                }
                else{
                    //text boş değilse
                    sendMessage(message);
                }
            }
        });



    }

    private void sendMessage(String message) {

        //kullanıcı mesaj attığında veritabınında "Chats" adında yeni child oluşturalacak ve "Chats"'e bazı key değerler atadık
        //sender: gönderenin UID'si
        //receiver: alıcının UID'si
        //message: gönderien mesaj

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", herUid);
        hashMap.put("message", message);
        databaseReference.child("Chats").push().setValue(hashMap);

        //mesaj gönderdikten sonra editText'in temizlenmesi
        message_editText.setText("");
    }

    private void checkUserStatus() {

        //mevcut kullanıcıyı al
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //kullanıcı giriş yapmışsa burada kal
            //giriş yapan kullanıcının email i
            //profileTxt.setText(user.getEmail());

            myUid = user.getUid(); //mevcut oturum açmış kullanıcının uid'si
        }
        else{
            //kullanıcı giriş yapmamışsa main activity'e git
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //searchView gizlenmesi için
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout){
            //hesaptan çıkış yap
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}