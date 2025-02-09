package com.gamze.pawsbook.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gamze.pawsbook.R;

public class MainActivity extends AppCompatActivity {
    //Butonların tanımlanması
    Button register_btn, login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        register_btn = findViewById(R.id.register_btn);
        login_btn = findViewById(R.id.login_btn);

        //register butona onClick özelliği ekleme
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RegisterActivity başlatır
                // startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Login butona onClick özelliği ekleme
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //login activity başlatır
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}