package com.gamze.pawsbook.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class HomeFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        return view;
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
            //AddPostActivity açılması için
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}