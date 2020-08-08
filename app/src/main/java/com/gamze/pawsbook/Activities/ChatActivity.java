package com.gamze.pawsbook.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamze.pawsbook.R;

public class ChatActivity extends AppCompatActivity {

    //layout views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profilePic;
    TextView nameTxt, userStatusTxt;
    EditText message_editText;
    ImageButton sendBtn;

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


    }
}