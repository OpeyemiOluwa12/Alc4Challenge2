package com.example.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.example.travelmantics.Models.TravelDeal;
import com.example.travelmantics.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewDealActivity extends AppCompatActivity {
    public static final int PICTURE_RESULT = 42;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private EditText dealTitle;
    private EditText dealDescription;
    private EditText dealPrice;
    private ImageView dealImage;
    private TravelDeal mTravelDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deal);
        initializeField();
        processIntent();
    }

    private void processIntent() {
        TravelDeal deal = (TravelDeal) getIntent().getSerializableExtra(this.getString(R.string.deals));
        if (deal == null) {
            deal = new TravelDeal();
        }
        mTravelDeal = deal;
        dealTitle.setText(deal.getTitle());
        dealDescription.setText(deal.getDescription());
        dealPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
    }

    private void showImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(imageUrl)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(dealImage);
        }
    }

    public void selectImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(this.getString(R.string.imageType));
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent.createChooser(intent, this.getString(R.string.select_image)), PICTURE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    String url = uri.getResult().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    mTravelDeal.setImageUrl(url);
                    mTravelDeal.setImageName(pictureName);
                    Log.d("Url", url);
                    Log.d("Name", pictureName);
                    showImage(url);
                }
            });
        }

    }

    private void initializeField() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(this.getString(R.string.travel_deals));

        dealTitle = findViewById(R.id.deal_view_title);
        dealDescription = findViewById(R.id.deal_view_description);
        dealPrice = findViewById(R.id.deal_view_price);
        dealImage = findViewById(R.id.deal_view_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_deal, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_save).setVisible(true);
            findViewById(R.id.deal_upload_btn).setEnabled(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            findViewById(R.id.deal_upload_btn).setEnabled(false);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveDeal();
                Toast.makeText(this, "Travel Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.action_delete:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backToList() {
        Intent backToListIntent = new Intent(this, ListDealsActivity.class);
        startActivity(backToListIntent);
        finish();
    }

    private void clean() {
        dealTitle.setText("");
        dealDescription.setText("");
        dealPrice.setText("");
        dealTitle.requestFocus();
    }

    private void saveDeal() {

        if (dealTitle.getText().toString().isEmpty() || dealDescription.getText().toString().isEmpty() || dealPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Fill all Fields", Toast.LENGTH_LONG).show();
        } else {
            mTravelDeal.setTitle(dealTitle.getText().toString());
            mTravelDeal.setDescription(dealDescription.getText().toString());
            mTravelDeal.setPrice(dealPrice.getText().toString());

            if (mTravelDeal.getId() == null) {
                mDatabaseReference.push().setValue(mTravelDeal);
            } else {
                mDatabaseReference.child(mTravelDeal.getId()).setValue(mTravelDeal);
            }
        }
    }

    private void deleteDeal() {
        if (mTravelDeal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
        } else {
            mDatabaseReference.child(mTravelDeal.getId()).removeValue();
            if (mTravelDeal.getImageName() != null && !mTravelDeal.getImageName().isEmpty()) {
                StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(mTravelDeal.getImageName());
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete Image", "Successfully Deleted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Delete Image", e.getMessage());

                    }
                });
            }

        }
    }

    private void enableEditTexts(Boolean isEnabled) {
        dealTitle.setEnabled(isEnabled);
        dealDescription.setEnabled(isEnabled);
        dealPrice.setEnabled(isEnabled);

    }


}
