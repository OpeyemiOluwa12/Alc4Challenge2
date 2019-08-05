package com.example.travelmantics.Adapters;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelmantics.Models.TravelDeal;
import com.example.travelmantics.R;
import com.example.travelmantics.Utils.FirebaseUtil;
import com.example.travelmantics.ViewDealActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TravelDealListAdapter extends RecyclerView.Adapter<TravelDealListAdapter.TravelDealHolder> {
    private ArrayList<TravelDeal> mTravelDealArrayList;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ImageView mDealListImage;

    public TravelDealListAdapter() {
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mTravelDealArrayList = FirebaseUtil.mTravelDealArrayList;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal", travelDeal.getTitle());
                travelDeal.setId(dataSnapshot.getKey());
                mTravelDealArrayList.add(travelDeal);
                notifyItemInserted(mTravelDealArrayList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public TravelDealHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_deals_item, parent, false);
        return new TravelDealHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull TravelDealHolder holder, int position) {
        TravelDeal deal = mTravelDealArrayList.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return mTravelDealArrayList.size();
    }

    public class TravelDealHolder extends RecyclerView.ViewHolder {
        TextView dealListTitle;
        TextView dealListDescription;
        TextView dealListPrice;

        public TravelDealHolder(@NonNull final View itemView) {
            super(itemView);
            dealListTitle = itemView.findViewById(R.id.deal_list_title);
            dealListDescription = itemView.findViewById(R.id.deal_list_desciption);
            dealListPrice = itemView.findViewById(R.id.deal_list_price);
            mDealListImage = itemView.findViewById(R.id.deal_list_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    TravelDeal selectedDeal = mTravelDealArrayList.get(position);
                    Intent viewDealIntent = new Intent(itemView.getContext(), ViewDealActivity.class);
                    viewDealIntent.putExtra(itemView.getContext().getString(R.string.deals), selectedDeal);
                    itemView.getContext().startActivity(viewDealIntent);
                }
            });
        }

        public void bind(TravelDeal deal) {
            dealListTitle.setText(deal.getTitle());
            dealListDescription.setText(deal.getDescription());
            dealListPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        private void showImage(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .resize(160, 160)
                        .centerCrop()
                        .into(mDealListImage);
            }
        }
    }
}
