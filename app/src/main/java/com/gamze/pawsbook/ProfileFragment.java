package com.gamze.pawsbook;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    //Log Statment
    private static final String TAG = "ProfileFragment";

    //firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //layout views
    ImageView avatar;
    TextView nameTxt, emailTxt, phoneTxt;

    public ProfileFragment() {
        //boş public constructor gerekli
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");


        //init layout views

        avatar = view.findViewById(R.id.avatar);
        nameTxt = view.findViewById(R.id.nameTxt);
        emailTxt = view.findViewById(R.id.emailTxt);
        phoneTxt = view.findViewById(R.id.phoneTxt);

        /* giriş yapan kullanıcıların bilgilerini email yada uid kullanarak çekmek zorundayız
        Kullanıcı detaylarını email adreslerini kullanarak çekicez
        orderbyChild query kullanarak giriş yapılan email ile email key ini eşleştirerek kullanıcı detaylarına ulaşılıyor
         */
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //gerekli veriler gelene kadar kontrol et
                for (DataSnapshot ds: snapshot.getChildren()){
                    //verileri almak için
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();

                    //set data
                    nameTxt.setText(name);
                    emailTxt.setText(email);
                    phoneTxt.setText(phone);
                    Log.d(TAG, "onDataChange: \nName: "+name+" email: "+ email);


                    try {
                        // resim alınırsa ayarla
                        Picasso.get().load(image).into(avatar);
                    } catch (Exception e){
                        // resim alınırken herangi bir sıkıntı varsa varsayılan olarak ayarla
                        Picasso.get().load(R.drawable.add_photo_foreground).into(avatar);
                    }
                }
                Log.d(TAG, "onDataChange: Data is empty");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error);
            }
        });

        return view;
    }
}