package com.gamze.pawsbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    //FirebaseAuth
    FirebaseAuth firebaseAuth;

    //Layout views
    TextView profileTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //ActionBar ve başlığı
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");


        profileTxt = findViewById(R.id.profileTxt);

        firebaseAuth = FirebaseAuth.getInstance();


    }

    private void checkUserStatus() {

        //mevcut kullanıcıyı al
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //kullanıcı giriş yapmışsa burada kal
            //giriş yapan kullanıcının email i
            profileTxt.setText(user.getEmail());
        }
        else{
            //kullanıcı giriş yapmamışsa main activity'e git
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        //uygulamanın başlangıcını konrtol et
        checkUserStatus();
        super.onStart();
    }


    //options menu dahil etme
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menuyu dahil etme
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
        return super.onOptionsItemSelected(item);
    }
}