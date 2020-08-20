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

import com.gamze.pawsbook.Activities.AddPostActivity;
import com.gamze.pawsbook.Activities.MainActivity;
import com.gamze.pawsbook.Adapters.AdapterUsers;
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

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;

    AdapterUsers adapterUsers;

    List<ModelUser> userList;

    //firebase auth
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //RecyclerView
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //userList dahil et
        userList = new ArrayList<>();

        //tüm kullanıcıları al
        getAllUsers();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void getAllUsers() {
        //mevcut kullanıcıyı al
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //ismi "User" olan kullanıcı bilgilerini içeren veritabanının yolunu al
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //path'dan tüm verileri al
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //mevcut kayıt olan tüm kullanıcıları al
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //adapter ve recyclerview ayarlanması
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void searchUsers(final String query) {

        //mevcut kullanıcıyı al
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //ismi "User" olan kullanıcı bilgilerini içeren veritabanının yolunu al
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //path'dan tüm verileri al
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //mevcut aranan olan tüm kullanıcıları al
                    //eğer böyle bir kullanıcı varsa
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        //searchview'e girilen kullanıcı adı ve email büyük/küçük harf duyarlı olmayacak
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);

                        }

                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //adapter ve recyclerview ayarlanması
                    recyclerView.setAdapter(adapterUsers);
                }
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

        //add item gizlenmesi için
        menu.findItem(R.id.action_add).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //kullanıcı klavyeden arama butonuna bastığından çağırılır
                //eğer seach query boş değilse ara
                if (!TextUtils.isEmpty(s.trim())){
                    //arama metni metin içeriyorıyorsa, ara
                    searchUsers(s);
                }
                else {
                    //search query boş o yüzden tüm kullanıcıları getir
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //kullanıcı klavyeden her karakter girdiğinde çağırılır

                //eğer seach query boş değilse ara
                if (!TextUtils.isEmpty(s.trim())){
                    //arama metni metin içeriyorıyorsa, ara
                    searchUsers(s);
                }
                else {
                    //search query boş o yüzden tüm kullanıcıları getir
                    getAllUsers();
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
        return super.onOptionsItemSelected(item);
    }
}

