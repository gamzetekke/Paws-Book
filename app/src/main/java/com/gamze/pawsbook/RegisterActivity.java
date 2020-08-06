package com.gamze.pawsbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    //layouttaki viewlerin tanımlanması
    EditText emailEt, passwordEt;
    Button button_register;
    TextView have_accounttxt;

    //ProgressBar
    ProgressDialog progressDialog;

    //FirebaseAuth bildirme
    private FirebaseAuth mAuth;

    //Firebase Realtime Database
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ActionBar ve başlığı
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        have_accounttxt = findViewById(R.id.have_accounttxt);
        button_register = findViewById(R.id.button_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //realtime database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");


        //button_register'a onClick özelliği eklenir
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //email ve şifre girme
                final String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();

                //kontrol etme
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //error kur ve email edittext'e odaklan
                    emailEt.setError("Invalid Email");
                    emailEt.setFocusable(true);
                }
                else if(password.length()<6){
                    passwordEt.setError("Password lenght should be at least 6 characters");
                    passwordEt.setFocusable(true);
                }
                else{
                   // registerUser(email, password);
                    // kullanıcıyı kaydet
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, dismiss dialog and start register activity
                                progressDialog.dismiss();

                                FirebaseUser user = mAuth.getCurrentUser();
                                //Firebase auth'dan kullanıcı email ve uid bilgilerini al
                                String email = user.getEmail();
                                String uid = user.getUid();
                                //Kullanıcı kaydoldugunda verileri depolamak için HashMap kullanılır
                                //HashMap kullanımı
                                HashMap<Object,String> hashMap = new HashMap<>();
                                //bilgileri HashMap'e koyma
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name",""); //profil ayarları sayfasında eklenecek
                                hashMap.put("phone", "");//profil ayarları sayfasında eklenecek
                                hashMap.put("image","" );//profil ayarları sayfasında eklenecek

                                //firebase database
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //"Users" isimli kullanıcının verilerini depolama
                                DatabaseReference reference = database.getReference("Users");
                                //HashMap ile verilerin database e eklenmesi
                                reference.child(uid).setValue(hashMap);


                                Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();

                                //HomeActivity başlat
                                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                                finish();

                            } else {
                                // If sign in fails, display a message to the user.
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        //have_accounttxt onClick özelliği aktifleştirme, tıklandığında login activity'e git
        have_accounttxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*private void registerUser(String email, String password) {
        //email ve şifre geçerliyse kullanıcıyı kaydet ve progressDialog göster
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();

                            //HomeActivity başlat
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progres dialog ve error mesajın gösterilmesi
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }*/


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //önceki activity git
        return super.onSupportNavigateUp();
    }
}