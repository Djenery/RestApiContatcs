package com.example.sqliteexample2;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int UNIQUE_REQUEST_CODE = 1;
    EditText etName, etNumber;
    TextView tvName, tvNumber;
    Button btnCreate, btnCancel;
    FloatingActionButton btnAdd;
    RecyclerView recyclerView;
    RestApiService rest;
    MyDataBaseHelper myDB;
    RecyclerView.Adapter<PersonAdapter.ViewHolder> myAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Person> persons;
    ActivityResultLauncher<Intent> activityResultLauncher;
    PersonAdapter.ViewHolder currentViewHolder;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rest = RestApiService.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvName = findViewById(R.id.tvName);
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

        btnAdd.setOnClickListener(v -> showDialog());

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                try {
                    String id = persons.get(position).getId();
                    Bitmap bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),
                            intent.getData()), 200, 200, true);
                    myDB.updateImageData(id, Utils.getBytes(bitmap));
                    persons.get(position).setImage(Utils.getBytes(bitmap));
                    myAdapter.notifyItemChanged(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_person_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window_dialog);
        btnCreate = dialog.findViewById(R.id.btnCreate);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        etName = dialog.findViewById(R.id.etName);
        etNumber = dialog.findViewById(R.id.etNumber);

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String number = etNumber.getText().toString().trim();
            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.sample_user_icon), 200, 200, true);
            Person person = new Person(name, number, Utils.getBytes(bitmap));
            Gson json = new Gson();
            String gson = json.toJson(person);
            rest.sendData(gson, json);

            myAdapter.notifyItemInserted(persons.size() - 1);
            recyclerView.smoothScrollToPosition(myAdapter.getItemCount());
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    public void ImageChooser(PersonAdapter.ViewHolder viewHolder) {
        boolean isPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        askPermission(isPermissionGranted);
        if (!isPermissionGranted) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            String title = "Choose an application";
            activityResultLauncher.launch(Intent.createChooser(intent, title));
        }
        currentViewHolder = viewHolder;
        position = currentViewHolder.getAdapterPosition();
    }

    private void askPermission(boolean isPermissionGranted) {
        if (isPermissionGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, UNIQUE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == UNIQUE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thank you!Permission granted!", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("This permission is important to save files to the phone! Please permit it!")
                            .setTitle("Important permission required!");

                    dialog.setPositiveButton("Ok", (dialogInterface, i) -> ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, UNIQUE_REQUEST_CODE));

                    dialog.setNegativeButton("No thank!", (dialogInterface, i) ->
                            Toast.makeText(this, "Cannot be done!", Toast.LENGTH_SHORT).show());

                    dialog.show();
                } else {
                    Toast.makeText(this, "We will never show this to you again!", Toast.LENGTH_SHORT).show();
                }
            }

        }
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
        try {
            List<Person> data = rest.getData();
            persons.addAll(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}


//        Cursor cursor = myDB.readAllData();
//        if (cursor.getCount() == 0) {
//            Toast.makeText(MainActivity.this, "No data!", Toast.LENGTH_SHORT).show();
//        } else {
//            while (cursor.moveToNext()) {
//                persons.add(new Person(
//                        cursor.getString(0),
//                        cursor.getString(1),
//                        cursor.getString(2),
//                        cursor.getBlob(3)));
//            }
//        }
//    }