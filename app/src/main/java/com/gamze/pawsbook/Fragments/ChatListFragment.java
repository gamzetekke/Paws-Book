package com.gamze.pawsbook.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.Adapters.AdapterChatlist;
import com.gamze.pawsbook.Models.ModelChat;
import com.gamze.pawsbook.Models.ModelChatlist;
import com.gamze.pawsbook.Models.ModelUser;
import com.gamze.pawsbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    //recycler view
    RecyclerView recyclerView;

    List<ModelChatlist> chatlistsList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    public ChatListFragment() {
        //boş constructor gerekiyor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat_list, container, false);


        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //recyclerview ve özellikleri
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);


        chatlistsList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistsList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistsList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatlist chatlist: chatlistsList){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }

                    //recyclerview ve özellikleri
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    //en son eklenen gönderiyi ilk göstermek için
                    layoutManager.setStackFromEnd(true);
                    layoutManager.setReverseLayout(true);

                    //layoutun recyclerview'e bağlanması
                    recyclerView.setLayoutManager(layoutManager);

                    //adapter
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    //set adapter
                    recyclerView.setAdapter(adapterChatlist);
                    //set lastmessages
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) || chat.getReceiver().equals(userId) &&
                            chat.getSender().equals(currentUser.getUid())){

                        theLastMessage = chat.getMessage();
                    }

                }

                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}