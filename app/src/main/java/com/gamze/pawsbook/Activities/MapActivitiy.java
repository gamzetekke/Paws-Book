package com.gamze.pawsbook.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.gamze.pawsbook.JsonParser;
import com.gamze.pawsbook.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MapActivitiy extends AppCompatActivity {

    //views
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //init views
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.findBtn);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        //yer çeşitlerinin arrayi
        final String[] placeTypeList = {"hospital", "vet", "atm"};
        //yer isimlerinin arrayi
        String[] placeNameList = {"Hospital", "Vet", "ATM"};

        //spinner'a adapteri ayarla
        spType.setAdapter(new ArrayAdapter<>(MapActivitiy.this, R.layout.support_simple_spinner_dropdown_item, placeNameList));

        //fused location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //izinleri kontrol et
        if (ActivityCompat.checkSelfPermission(MapActivitiy.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //izin verildiğinde, methodu çağır
            getCurrentLocation();

        }
        else {
            //izinler alınmadıysa, izin iste
            ActivityCompat.requestPermissions(MapActivitiy.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);

        }

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Spinner'de seçilen pozisyonu al
                int i = spType.getSelectedItemPosition();
                //url
                String url = "https://maps.google.com/maps/api/place/nearbysearch/json?"+//url
                "location=" + currentLat + "," + currentLong + //enlem ve boylam
                "&radius=5000"+// yakınlık yarıçapı
                "&type="+ placeTypeList[i] + //gösterilecek yer tipleri
                "&sensor=true"+ //sensor
                "&key="+ getResources().getString(R.string.google_map_key); //Google map key

                //Json verilerini indirmek için place task methodunu çalıştır
                new PlaceTask().execute(url);

            }
        });




    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //başarılıysa
                if (location != null){
                    //mevcut enlemi al
                    currentLat = location.getLatitude();
                    //mevcut boylamı al
                    currentLong = location.getLongitude();
                    //haritayı senkronize et
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //harita hazır olduğu zaman
                            map = googleMap;
                            //haritadaki mevcut konumu yakınlaştır
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat,currentLong),10));
                        }
                    });



                }
            }
        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                //izinler alındıysa, methodu cağır
                getCurrentLocation();
            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                //verilerin başlatılması
                 data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //parser task methodunu çalıştır
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //url in başlatılması
        URL url = new URL(string);
        //bağlantının başlatılması
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //bağlantının bağlanması
        connection.connect();
        //input stream başlatılması
        InputStream stream = connection.getInputStream();
        // buffer reader başlatılması
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //string builder başlatılması
        StringBuilder builder = new StringBuilder();
        //string değerlerinin başlatılması
        String line = "";

        while ((line =reader.readLine()) != null){
            //line ekle
            builder.append(line);
        }
        //append verisini al
        String data = builder.toString();
        //reader'i kapar
        reader.close();

        return  data;
    }

    private class ParserTask extends  AsyncTask<String, Integer, List<HashMap<String,String>>>  {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //json parser sınıfı oluştur
            JsonParser jsonParser = new JsonParser();
            //hashmap list başlatılması
            List<HashMap<String,String>> mapList = null;
            JSONObject object = null;
            try {
                //json objesi başlatılması
                object = new JSONObject(strings[0]);
                //parse(ayrıştırma) jsonObject
                mapList = jsonParser.parseResult(object);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            //return mapList
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //map'i temizle
            map.clear();

            for (int i = 0; i<hashMaps.size(); i++){
                //hashmap başlatılması
                HashMap<String,String> hashMapList = hashMaps.get(i);
                //Enlem al
                double lat = Double.parseDouble(hashMapList.get("lat"));
                //boylam al
                double lng = Double.parseDouble(hashMapList.get("lng"));
                //isim al
                String name = hashMapList.get("name");
                //enlem ve boylamı bağla
                LatLng latLng = new LatLng(lat,lng);
                // marker options başlatılması
                MarkerOptions options = new MarkerOptions();
                //enlem boylamın pozisyonunu ayarla
                options.position(latLng);
                //başlık oluştur
                options.title(name);
                //haritaya marker ekle
                map.addMarker(options);





            }
        }
    }
}