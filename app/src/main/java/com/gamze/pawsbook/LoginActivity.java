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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //layouttaki viewlerin tanımlanması
    EditText emailEt, passwordEt;
    Button button_login;
    TextView nothave_accounttxt;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //Progress Dialog
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar ve başlığı
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        nothave_accounttxt = findViewById(R.id.nothave_accounttxt);
        button_login = findViewById(R.id.button_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Login button onClick özelliği ekleme -> tıklandığında profile activity git
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //email ve şifre girme
                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //error kur ve email edittext'e odaklan
                    emailEt.setError("Invalid Email");
                    emailEt.setFocusable(true);
                }
                else{
                    //geçerli email girilmişse
                    loginUser(email, password);
                }

            }
        });

        //nothave_accounttxt textview onClick özelliği ekleme -> tıklandığında register activity git
        nothave_accounttxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //ProgressDialog
        pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");

    }

    private void loginUser(String email, String password) {
        //ProgressDialog göster
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //kullanıcı girişi başarılıysa profil activity e git
                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            //dismiss progress dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
                pd.dismiss();
                  //error ver ve error mesaj göster
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //önceki activity git
        return super.onSupportNavigateUp();
    }
}