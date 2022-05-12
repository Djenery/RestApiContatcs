package com.example.sqliteexample2;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    EditText etName, etNumber;
    TextView tvName, tvNumber;
    FloatingActionButton btnAdd;
    RecyclerView recyclerView;
    MyDataBaseHelper myDB;
    RecyclerView.Adapter myAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Person> persons;
    static MainActivity mainActivity;
    ActivityResultLauncher<Intent> activityResultLauncher;
    PersonAdapter.ViewHolder currentViewHolder;
    int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onStart() {
        super.onStart();
        etName = findViewById(R.id.etName);
        tvName = findViewById(R.id.tvName);
        etNumber = findViewById(R.id.etNumber);
        tvNumber = findViewById(R.id.tvNumber);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.rvList);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);


        myDB = new MyDataBaseHelper(MainActivity.this);
        persons = new ArrayList<>();

        storeDataInArrays();

        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        myAdapter = new PersonAdapter(MainActivity.this, persons);
        recyclerView.setAdapter(myAdapter);

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        btnAdd.setOnClickListener(v -> {

            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.sample_user_icon), 200, 200, true);
            Person person = myDB.addPerson(getString(R.string.new_contact), getString(R.string.number), Utils.getBytes(bitmap));
            persons.add(person);
            myAdapter.notifyItemInserted(persons.size() - 1);
            recyclerView.smoothScrollToPosition(myAdapter.getItemCount());
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent intent = result.getData();
                if (intent != null) {
                    try {
                        String id = persons.get(position).getId();
                        Bitmap bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), intent.getData()), 200, 200, true);
                        myDB.updateImageData(id, Utils.getBytes(bitmap));
                        persons.get(position).setImage(Utils.getBytes(bitmap));
                        myAdapter.notifyItemChanged(position);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public void imageChooser(PersonAdapter.ViewHolder viewHolder) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
        currentViewHolder = viewHolder;
        position = currentViewHolder.getAdapterPosition();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Complete Action Using"), REQUEST_CODE);
    }

    final ItemTouchHelper.SimpleCallback simpleCallback =
            new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP |
                            ItemTouchHelper.DOWN |
                            ItemTouchHelper.START |
                            ItemTouchHelper.END,
                    ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    int startPosition = viewHolder.getAdapterPosition();
                    int endPosition = target.getAdapterPosition();
                    Collections.swap(persons, startPosition, endPosition);
                    myAdapter.notifyItemMoved(startPosition, endPosition);
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    String id = persons.get(position).getId();
                    String name = persons.get(position).getName();
                    String number = persons.get(position).getNumber();
                    byte[] image = Utils.getBytes(Utils.getImage(persons.get(position).getImage()));
                    myDB.deleteOneRow(String.valueOf(id));
                    persons.remove(position);
                    myAdapter.notifyItemRemoved(position);

                    Snackbar.make(recyclerView, String.valueOf(position),
                            Snackbar.LENGTH_LONG).setAction("UNDO", v -> {
                        Person person = myDB.addPerson(name, number, image);
                        persons.add(position, person);
                        myAdapter.notifyItemInserted(position);
                    }).show();
                }
            };

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                persons.add(new Person(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getBlob(3)));
            }
        }
    }

}