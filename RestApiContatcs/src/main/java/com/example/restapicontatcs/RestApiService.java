package com.example.restapicontatcs;

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
import java.util.Comparator;
import java.util.List;

public class RestApiService {
    private static RestApiService restApiService;
    private Person person;
    private int code;
    private GetDataThread getDataThread;



    public int getCode(){
        return code;
    }

    public RestApiService(Context context) {
    }

    static RestApiService getInstance(Context context) {
        if (restApiService == null) restApiService = new RestApiService(context);
        return restApiService;
    }

    public Person getPerson() {
        return person;
    }

    public void createPerson(String gson, Gson json) throws InterruptedException {
        Thread thread = new Thread(() -> {
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
                    person = json.fromJson(s, Person.class);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
    }

    public void sendList (String gson, Gson json) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), gson);

                Request request = new Request.Builder()
                        .url("http://89.66.186.198:8080/contacts/addEditList")
                        .addHeader("Content-type", "application/json")
                        .post(body)
                        .build();

                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    String resp = response.body().string();
                    Type listType = new TypeToken<ArrayList<Person>>() {
                    }.getType();
                    getDataThread.setValue(resp,listType,json);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
    }

    public void deletePerson(String json) throws InterruptedException {
        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url("http://89.66.186.198:8080/contacts/delete")
                    .addHeader("Content-type", "application/json")
                    .post(body)
                    .build();

           Call call = client.newCall(request);
            try {
                Response response = call.execute();
                code = response.code();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
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
            Response response = call.execute();
            String s = response.body().string();
            Type listType = new TypeToken<ArrayList<Person>>() {
            }.getType();
            result = json.fromJson(s, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Person> getValue() {
        result.sort(Comparator.comparing(Person::getPosition));
        return result;
    }

    public void setValue(String resp, Type listType, Gson json) {
        result = json.fromJson(resp, listType);
    }
}
