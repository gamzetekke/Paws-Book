package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.gamze.pawsbook.Fragments.ChatListFragment;
import com.gamze.pawsbook.Fragments.HomeFragment;
import com.gamze.pawsbook.Fragments.MapFragment;
import com.gamze.pawsbook.Fragments.ProfileFragment;
import com.gamze.pawsbook.R;
import com.gamze.pawsbook.Fragments.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //FirebaseAuth
    FirebaseAuth firebaseAuth;

    //Actionbar
    ActionBar actionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar ve başlığı
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //profileTxt = findViewById(R.id.profileTxt);

        firebaseAuth = FirebaseAuth.getInstance();


        //Bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Home Fragment'ı boş default fragment olarak ayarlanması
        actionBar.setTitle("Home"); //ActionBar başlığını değiştirme
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, homeFragment, "");
        ft.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //itemlere tıklanma özelliği ekleme
                    switch (item.getItemId()){
                        case R.id.action_home:
                            //home fragment değişimi
                            actionBar.setTitle("Home"); //ActionBar başlığını değiştirme
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction ftHome = getSupportFragmentManager().beginTransaction();
                            ftHome.replace(R.id.content, homeFragment, "");
                            ftHome.commit();
                            return true;

                        case R.id.action_map:
                            //map fragment değişimi
                            actionBar.setTitle("Map"); //ActionBar başlığını değiştirme
                            MapFragment mapFragment = new MapFragment();
                            FragmentTransaction ftMap = getSupportFragmentManager().beginTransaction();
                            ftMap.replace(R.id.content, mapFragment, "");
                            ftMap.commit();
                            return true;

                        case R.id.action_users:
                            //users fragment değişimi
                            actionBar.setTitle("Users"); //ActionBar başlığını değiştirme
                            UsersFragment usersFragment = new UsersFragment();
                            FragmentTransaction ftUsers = getSupportFragmentManager().beginTransaction();
                            ftUsers.replace(R.id.content, usersFragment, "");
                            ftUsers.commit();
                            return true;

                        case R.id.action_profile:
                            //profile fragment değişimi
                            actionBar.setTitle("Profile"); //ActionBar başlığını değiştirme
                            ProfileFragment profileFragment = new ProfileFragment();
                            FragmentTransaction ftProfile = getSupportFragmentManager().beginTransaction();
                            ftProfile.replace(R.id.content, profileFragment, "");
                            ftProfile.commit();
                            return true;

                        case R.id.action_chat:
                            //profile fragment değişimi
                            actionBar.setTitle("Chats"); //ActionBar başlığını değiştirme
                            ChatListFragment chatListFragment = new ChatListFragment();
                            FragmentTransaction ftChat = getSupportFragmentManager().beginTransaction();
                            ftChat.replace(R.id.content, chatListFragment, "");
                            ftChat.commit();
                            return true;
                    }
                    return false;
                }
            };

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
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //uygulamanın başlangıcını konrtol et
        checkUserStatus();
        super.onStart();
    }



}