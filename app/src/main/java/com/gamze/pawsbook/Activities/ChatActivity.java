package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamze.pawsbook.Adapters.AdapterChat;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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


    //Kullanıcının mesajı görüp görmediğini konrtol etmek için
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String herUid;
    String myUid;
    String herImage;


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



        //Recyclerview için layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerview özellikleri
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);



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
                    herImage =""+ ds.child("image").getValue();
                    String typingStatus =""+ ds.child("typing").getValue();

                    //typing status kontrol et
                    if (typingStatus.equals(myUid)){
                        userStatusTxt.setText("typing...");
                    }
                    else{
                        //onlineStatus değerini al
                        String onlineStatus = ""+ ds.child("onlineStatus").getValue();

                        if (onlineStatus.equals("online")) {
                            userStatusTxt.setText(onlineStatus);
                        }
                        else {
                            //zamanı uygun zaman dilimine dönüştür
                            //timeStamp'ı dd/mm/yyyy hh:mm am/pm şekline dönüştürme
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();

                            userStatusTxt.setText("Last seen at: "+ dateTime);
                        }
                    }

                    //set data
                    nameTxt.setText(name);

                    try {
                        //resim alındı, resmi imageView'e gönder
                        Picasso.get().load(herImage).placeholder(R.drawable.tag_face).into(profilePic);

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

        //edit text change listener kontrol et
        message_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(herUid); //alıcı uid'si
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();

    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(herUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages() {

        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) &&  chat.getSender().equals(herUid) ||
                             chat.getReceiver().equals(herUid) && chat.getSender().equals(myUid)) {

                        chatList.add(chat);

                    }

                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, herImage);
                    adapterChat.notifyDataSetChanged();
                    //recyclerview'in adaptere bağlanması
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String message) {

        //kullanıcı mesaj attığında veritabınında "Chats" adında yeni child oluşturalacak ve "Chats"'e bazı key değerler atadık
        //sender: gönderenin UID'si
        //receiver: alıcının UID'si
        //message: gönderien mesaj

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", herUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
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

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //mevcut kullanıcının onlineStatus değerini günceller
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        //mevcut kullanıcının onlineStatus değerini günceller
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //son görülme saati ile birlikte ofline kısmını ayarla
        checkOnlineStatus(timestamp);

        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {

        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //searchView gizlenmesi için
        menu.findItem(R.id.action_search).setVisible(false);
        //addpost item gizlenmesi için
        menu.findItem(R.id.action_add).setVisible(false);

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