package com.gamze.pawsbook.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.Adapters.AdapterPosts;
import com.gamze.pawsbook.Models.ModelPost;
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


public class HomeFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    //Recyclerview
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();



        //recyclerview ve özellikleri
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //en son eklenen gönderiyi ilk göstermek için
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //layoutun recyclerview'e bağlanması
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();

        loadPosts();


        return view;
    }

    private void loadPosts() {
        //tüm postların yolu
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //tüm verileri bu ref isimli referanstan al
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    //adapter ve recyclerview bağlanması
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //hata varsa
                Toast.makeText(getActivity(),""+ error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchPosts(final String searchQuery){
        //tüm postların yolu
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //tüm verileri bu ref isimli referanstan al
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getPost_image().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getPost_desc().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    //adapter ve recyclerview bağlanması
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //hata varsa
                Toast.makeText(getActivity(),""+ error.getMessage(), Toast.LENGTH_SHORT).show();

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

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //kullanıcı klavyeden arama butonuna bastığından çağırılır
                //eğer seach query boş değilse ara
                if (!TextUtils.isEmpty(s.trim())){
                    //arama metni metin içeriyorıyorsa, ara
                    searchPosts(s);
                }
                else {
                    //search query boş o yüzden tüm kullanıcıları getir
                    loadPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //kullanıcı klavyeden her karakter girdiğinde çağırılır

                //eğer seach query boş değilse ara
                if (!TextUtils.isEmpty(s.trim())){
                    //arama metni metin içeriyorıyorsa, ara
                    searchPosts(s);
                }
                else {
                    //search query boş o yüzden tüm kullanıcıları getir
                    loadPosts();
                }


                return false;
            }
        });


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