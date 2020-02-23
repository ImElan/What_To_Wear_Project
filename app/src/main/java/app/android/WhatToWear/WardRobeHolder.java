package app.android.WhatToWear;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class WardRobeHolder extends RecyclerView.ViewHolder
{
    private View mView;

    public WardRobeHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }

    void setName(String name)
    {
        TextView nameText = mView.findViewById(R.id.individual_dress_Name);
        nameText.setText(name);
    }

    void setType(String type)
    {
        TextView typeText = mView.findViewById(R.id.individual_dress_Type);
        typeText.setText(type);
    }

    void setCategory(String category)
    {
        TextView categoryText = mView.findViewById(R.id.individual_dress_Category);
        categoryText.setText(category);
    }

    void setClimate(String climate)
    {
        TextView climateText = mView.findViewById(R.id.individual_dress_climate);
        climateText.setText(climate);
    }

    void setImage(final String url, final Context context)
    {
        final CircleImageView imageView = mView.findViewById(R.id.individual_image);
        Picasso.with(context)
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.hoodie)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context)
                                .load(url)
                                .placeholder(R.drawable.hoodie)
                                .into(imageView);
                    }
                });
    }
}
