package com.example.iwatched;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iwatched.databinding.RecycleRowBinding;

import java.util.ArrayList;

public class ArtAdaptor extends RecyclerView.Adapter<ArtAdaptor.artHolder> {

    ArrayList<Art> artArrayList;
    public ArtAdaptor(ArrayList<Art> artArrayList){
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public artHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecycleRowBinding recycleRowBinding = RecycleRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new artHolder(recycleRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtAdaptor.artHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recycleviewTextview.setText(artArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","Old");
                intent.putExtra("artId",artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public class artHolder extends RecyclerView.ViewHolder {
        private RecycleRowBinding binding;
        public artHolder(RecycleRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
