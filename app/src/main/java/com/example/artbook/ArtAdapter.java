package com.example.artbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artbook.databinding.RecylerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    ArrayList<Art> artArrayList;  // PARAMETRE OLUŞTUR

    public ArtAdapter(ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;

    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecylerRowBinding recylerRowBinding = RecylerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtHolder(recylerRowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull ArtAdapter.ArtHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name); // VERİLERİN İSMİNİ SADECE AL
        holder.itemView.setOnClickListener(new View.OnClickListener() { // TIKLANAN VERİNİN HANGİSİ OLDUĞUNU GETİRİR
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artArrayList.size(); // BURADA VERİNİN KAÇ ADET ELEMAN OLDUĞUNU YAZ
    }

    public class ArtHolder extends RecyclerView.ViewHolder {

        private RecylerRowBinding binding;

        public ArtHolder(RecylerRowBinding binding) {        // BURADA BULUNAN itemView KISMINI SİL
            super(binding.getRoot() );
            this.binding = binding;
        }
    }
}
