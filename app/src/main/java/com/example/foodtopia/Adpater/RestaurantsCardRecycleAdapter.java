package com.example.foodtopia.Adpater;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodtopia.R;
import com.example.foodtopia.restaurant.RestaurantProductActivity;
import com.example.foodtopia.Model.Restaurants;


import java.util.List;

public class RestaurantsCardRecycleAdapter extends RecyclerView.Adapter<RestaurantsCardRecycleAdapter.ViewHolder> {
    private Context context;
    private List<Restaurants> memberList;

    public RestaurantsCardRecycleAdapter(Context context, List<Restaurants> memberList) {
        this.context = context;
        this.memberList = memberList;
    }


    //當現有的ViewHolder不夠用時，要求Adapter產生一個新的
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant_logo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Restaurants member = memberList.get(position);
        holder.storeImage.setImageResource(member.getImage());
        holder.storeName.setText(member.getName());

//        if (position % 6 == 0) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c1));
//        } else if (position % 6 == 1) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c2));
//        } else if (position % 6 == 2) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c3));
//        } else if (position % 6 == 3) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c4));
//        } else if (position % 6 == 4) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c5));
//        } else if (position % 6 == 5) {
//            holder.linearBg.setBackgroundColor(ContextCompat.getColor(context, R.color.cv_c6));
//        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RestaurantProductActivity.class);
                intent.putExtra("image", member.getImage());
                intent.putExtra("name", member.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    //Adapter 需要一個 ViewHolder，只要實作它的 constructor 就好，保存起來的view會放在itemView裡面
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storeImage;
        LinearLayout linearBg;
        TextView  storeName;

        ViewHolder(View itemView) {
            super(itemView);
            storeImage = (ImageView) itemView.findViewById(R.id.storeImage);
            storeName = (TextView) itemView.findViewById(R.id.storeName);
            linearBg = (LinearLayout) itemView.findViewById(R.id.bg);
        }
    }
}
