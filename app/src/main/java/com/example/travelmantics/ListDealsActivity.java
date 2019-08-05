package com.example.travelmantics;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;

import com.example.travelmantics.Adapters.TravelDealListAdapter;
import com.example.travelmantics.Utils.FirebaseUtil;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ListDealsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_deals);

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayTravelDealList();
    }
    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    private void displayTravelDealList() {
        FirebaseUtil.openFbRefernce(getString(R.string.travel_deals), this);
        RecyclerView listDealRecyclerAdapter = findViewById(R.id.list_deal_recyclerView);
        final TravelDealListAdapter travelDealListAdapter = new TravelDealListAdapter();
        listDealRecyclerAdapter.setAdapter(travelDealListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        listDealRecyclerAdapter.setLayoutManager(linearLayoutManager);
        FirebaseUtil.attachListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_deal, menu);
        MenuItem addNewDeal = menu.findItem(R.id.action_new_deal);
        if (FirebaseUtil.isAdmin) {
            if(!addNewDeal.isVisible()) {
                addNewDeal.setVisible(true);
            }
        } else {
            addNewDeal.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_new_deal:
                createNewDeal();
                return true;
            case R.id.action_logout:
                logout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewDeal() {

        Intent newDealIntent = new Intent(this, ViewDealActivity.class);
        startActivity(newDealIntent);
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUtil.attachListener();
                    }
                });
        FirebaseUtil.detachListener();
    }

    public void showAdminMenu() {
        invalidateOptionsMenu();
    }
}
