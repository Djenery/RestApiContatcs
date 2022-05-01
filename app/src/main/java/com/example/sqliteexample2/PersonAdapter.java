package com.example.sqliteexample2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    Context context;
    ArrayList<Person> persons;
    public PersonAdapter(Context context,
                         ArrayList<Person> persons) {
        this.context = context;
        this.persons = persons;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUser;
        TextView tvName, tvNumber;
        EditText etName, etNumber;
        Button btnEditImage;
        boolean editController = false;
        private long lastTouchTime = 0;
        private long currentTouchTime = 0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUser = itemView.findViewById(R.id.ivUser);
            tvName = itemView.findViewById(R.id.tvName);
            etName = itemView.findViewById(R.id.etName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            etNumber = itemView.findViewById(R.id.etNumber);
            btnEditImage = itemView.findViewById(R.id.btnEditImage);
            etName.setVisibility(View.GONE);
            etNumber.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                String id = persons.get(position).getId();
                lastTouchTime = currentTouchTime;
                currentTouchTime = System.currentTimeMillis();
                if (currentTouchTime - lastTouchTime < 1000) {
                    Editor(id, position);
                    lastTouchTime = 0;
                    currentTouchTime = 0;
                }
            });

            btnEditImage.setOnClickListener(v -> {
                MainActivity mainActivity = new MainActivity();
                mainActivity.imageChooser();
            });
        }


        private void Editor(String id, int position) {
            if (editController) {
                editController = false;
                tvName.setVisibility(View.VISIBLE);
                tvNumber.setVisibility(View.VISIBLE);
                etName.setVisibility(View.GONE);
                etNumber.setVisibility(View.GONE);

                MyDataBaseHelper myDB = new MyDataBaseHelper(context);
                String name = etName.getText().toString().trim();
                String number = etNumber.getText().toString().trim();
                myDB.updateDate(id, name, number);
                persons.get(position).setName(name);
                persons.get(position).setNumber(number);
                notifyDataSetChanged();

            } else {
                editController = true;

                tvName.setVisibility(View.GONE);
                tvNumber.setVisibility(View.GONE);
                etName.setVisibility(View.VISIBLE);
                etNumber.setVisibility(View.VISIBLE);

            }
        }
    }

    @NonNull
    @Override
    public PersonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonAdapter.ViewHolder holder, int position) {
        holder.etName.setText(persons.get(position).getName());
        holder.tvName.setText(persons.get(position).getName());
        holder.tvNumber.setText(persons.get(position).getNumber());
        holder.etNumber.setText(persons.get(position).getNumber());

    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

}
