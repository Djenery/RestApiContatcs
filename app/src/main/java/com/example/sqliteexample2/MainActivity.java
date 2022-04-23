package com.example.sqliteexample2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        etName = findViewById(R.id.etName);
        tvName = findViewById(R.id.tvName);
        etNumber = findViewById(R.id.etNumber);
        tvNumber = findViewById(R.id.tvNumber);
        btnAdd = findViewById(R.id.btnAdd);

        myDB = new MyDataBaseHelper(MainActivity.this);
        persons = new ArrayList<>();

        storeDataInArrays();

        recyclerView = findViewById(R.id.rvList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        myAdapter = new PersonAdapter(MainActivity.this, persons);
        recyclerView.setAdapter(myAdapter);

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        btnAdd.setOnClickListener(v -> {
            Person person = myDB.addPerson("Name1", "Number1");
            persons.add(person);
            myAdapter.notifyItemInserted(persons.size() - 1);
            recyclerView.smoothScrollToPosition(myAdapter.getItemCount());
        });

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
                    myDB.deleteOneRow(String.valueOf(id));
                    persons.remove(position);
                    myAdapter.notifyItemRemoved(position);

                    Snackbar.make(recyclerView, String.valueOf(position),
                            Snackbar.LENGTH_LONG).setAction("UNDO", v -> {
                        Person person = myDB.addPerson(name, number);
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
                        cursor.getString(2)));
            }
        }
    }

}