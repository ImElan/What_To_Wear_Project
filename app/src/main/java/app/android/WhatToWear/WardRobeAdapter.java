package app.android.WhatToWear;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WardRobeAdapter extends RecyclerView.Adapter<WardRobeAdapter.ViewHolder>
{
    Context mContext;
    List<WardRobeClass> WardRobeList;

    public WardRobeAdapter(Context mContext, List<WardRobeClass> wardRobeList)
    {
        this.mContext = mContext;
        WardRobeList = wardRobeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_page_dress_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String name = WardRobeList.get(position).getName();
        String type = WardRobeList.get(position).getType();
        final String url = WardRobeList.get(position).getImage_url();

        holder.NameText.setText(name);
        holder.TypeText.setText(type);

        Picasso.with(mContext)
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.hoodie)
                .into(holder.ClothesImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext)
                                .load(url)
                                .placeholder(R.drawable.hoodie)
                                .into(holder.ClothesImage);
                    }
                });
        int id;
        switch (type)
        {
            case "Shirts" :
                id = R.drawable.shirt;
                break;
            case "Trousers" :
                id = R.drawable.trousers;
                break;
            case "Coats" :
                id = R.drawable.coat;
                break;
            case "Jackets" :
                id = R.drawable.jacket;
                break;
            case "Sweat Shirts" :
                id = R.drawable.sweat_shirt;
                break;
            case "Hoodies" :
                id = R.drawable.hoodie;
                break;
            case "Shoes" :
                id = R.drawable.sneakers;
                break;
            case "Accessories" :
                id = R.drawable.watch;
                break;
            default:
                id = R.drawable.loading;
                break;
        }
        holder.TypeImage.setImageResource(id);
    }

    @Override
    public int getItemCount() {
        return WardRobeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView NameText;
        TextView TypeText;
        CircleImageView ClothesImage;
        ImageView TypeImage;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            NameText = itemView.findViewById(R.id.individual_dress_Name);
            TypeText = itemView.findViewById(R.id.individual_dress_Type);
            ClothesImage = itemView.findViewById(R.id.individual_image);
            TypeImage = itemView.findViewById(R.id.type_image);
        }
    }
}
