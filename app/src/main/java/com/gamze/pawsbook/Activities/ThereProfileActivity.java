package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamze.pawsbook.Adapters.AdapterPosts;
import com.gamze.pawsbook.Models.ModelPost;
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
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    //Firebase Auth
    FirebaseAuth firebaseAuth;

    //layout views
    ImageView avatar, coverPhoto;
    TextView nameTxt, emailTxt, descTxt;

    //RecyclerView
    RecyclerView recyclerview_posts;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init layout views
        avatar = findViewById(R.id.avatar);
        coverPhoto = findViewById(R.id.coverPhoto);
        nameTxt = findViewById(R.id.nameTxt);
        emailTxt = findViewById(R.id.emailTxt);
        descTxt = findViewById(R.id.descTxt);
        //recyclerview
        recyclerview_posts = findViewById(R.id.recyclerview_posts);

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //tıklanan kullanıcın gönderilerini almak için uid'sini al
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
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
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadHerPosts();
    }

    private void loadHerPosts() {
        //recyclerView için linearLayout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //en yeni gönderiyi ilk göstermek için
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //layout'u recyclerView'e bağlama
        recyclerview_posts.setLayoutManager(linearLayoutManager);

        //init postList
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //gönderiyi yüklemek için query
        Query query = ref.orderByChild("uid").equalTo(uid);
        //bu ref referansından tüm verileri al
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //listeye ekleme
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //adapteri recyclerView'e bağlama
                    recyclerview_posts.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //errorla karşılaşılırsa
                Toast.makeText(ThereProfileActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchHerPosts(final String searchQuery){
        //recyclerView için linearLayout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //en yeni gönderiyi ilk göstermek için
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        //layout'u recyclerView'e bağlama
        recyclerview_posts.setLayoutManager(linearLayoutManager);

        //init postList
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //gönderiyi yüklemek için query
        Query query = ref.orderByChild("uid").equalTo(uid);
        //bu ref referansından tüm verileri al
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getPost_title().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getPost_desc().toLowerCase().contains(searchQuery.toLowerCase())){

                        //listeye ekleme
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //adapteri recyclerView'e bağlama
                    recyclerview_posts.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //errorla karşılaşılırsa
                Toast.makeText(ThereProfileActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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

        //add post itemin gizlenmesi
        menu.findItem(R.id.action_add).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);

        //Kullanıcın postlarını aramak için SearchView

       SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
       // SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //kullanıcı arama butonuna tıkladığında çağırılır
                if (!TextUtils.isEmpty(query)){
                    //search
                    searchHerPosts(query);
                }
                else{
                    loadHerPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //kullanıcı her karakter girdiğinde çağırılır
                if (!TextUtils.isEmpty(newText)){
                    //search
                    searchHerPosts(newText);
                }
                else{
                    loadHerPosts();
                }
                return false;
            }
        });

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