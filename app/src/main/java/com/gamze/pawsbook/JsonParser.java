package com.gamze.pawsbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String,String> parseJsonObject(JSONObject object) {

        //Hashmap başlatılması
        HashMap<String,String> dataList = new HashMap<>();
        try {
            //objeden isim al
            String name = object.getString("name");
            //objeden enlem al
            String latitude = object.getJSONObject("geometry").getJSONObject("location").getString("lat");
            //objeden boylam al
            String longitude = object.getJSONObject("geometry").getJSONObject("location").getString("lng");

            //tüm verileri hashmap'e koy
            dataList.put("name", name);
            dataList.put("lat", latitude);
            dataList.put("lng", longitude);

        }
        catch (JSONException e){
            e.printStackTrace();
        }
        //return Hashmap
        return dataList;

    }

    private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray) {
        //HashmapList başlatılması
        List<HashMap<String,String>> dataList = new ArrayList<>();
        for (int i = 0; i<jsonArray.length(); i++){
            try {
                //hashmap başlatılması
                HashMap<String,String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                //HashMap List'e verileri ekle
                dataList.add(data);

            }
            catch (JSONException e){
                e.printStackTrace();

            }
        }
        //return HashMapList
        return dataList;
    }

    public List<HashMap<String,String>> parseResult(JSONObject object) {
        //Json array'in başlatılması
        JSONArray jsonArray = null;

        try {
            //results array'i al
            jsonArray = object.getJSONArray("results");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        //return array
        return parseJsonArray(jsonArray);

    }


}











