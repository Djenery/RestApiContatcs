package com.example.sqliteexample2;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RestApiService {
    private final Context context;
    private static RestApiService restApiService;


    public RestApiService(Context context) {
        this.context = context;
    }

    static RestApiService getInstance(Context context) {
        if (restApiService == null) restApiService = new RestApiService(context);
        return restApiService;
    }

    public void sendData(String gson, Gson json) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), gson);

                Request request = new Request.Builder()
                        .url("http://89.66.186.198:8080/contacts/addEdit")
                        .addHeader("Content-type", "application/json")
                        .post(body)
                        .build();

                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    String s = response.body().string();
                    Person person = json.fromJson(s, Person.class);
                    ((MainActivity) context).persons.add(person);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public List<Person> getData() throws InterruptedException {
        GetDataThread getDataThread = new GetDataThread();
        Thread thread = new Thread(getDataThread);
        thread.start();
        thread.join();
        return getDataThread.getValue();
    }
}

class GetDataThread implements Runnable {
    private volatile List<Person> result;
    Gson json = new Gson();

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://89.66.186.198:8080/contacts/list")
                    .addHeader("Content-type", "application/json")
                    .get()
                    .build();
            Call call = client.newCall(request);
            Response response;
            response = call.execute();
            String s = response.body().string();
            Type listType = new TypeToken<ArrayList<Person>>() {}.getType();
            result = json.fromJson(s, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Person> getValue() {
        return result;
    }
}
