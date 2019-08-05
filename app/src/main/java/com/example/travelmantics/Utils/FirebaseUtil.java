package com.example.travelmantics.Utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.travelmantics.ListDealsActivity;
import com.example.travelmantics.Models.TravelDeal;
import com.example.travelmantics.R;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static ArrayList<TravelDeal> mTravelDealArrayList;
    private static FirebaseUtil sFirebaseUtil;
    private static ListDealsActivity caller;
    private static final int RC_SIGN_IN = 123;


    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;

    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseAuth.AuthStateListener mAuthStateListener;
    public static Boolean isAdmin;

    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;

    public FirebaseUtil() {
    }

    public static void openFbRefernce(String dbRef, ListDealsActivity activity) {
        if (sFirebaseUtil == null) {
            sFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = activity;

            fireBaseAuthCheck(activity);

            connectStorage();


        }
        mTravelDealArrayList = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(dbRef);
    }

    private static void fireBaseAuthCheck(final ListDealsActivity callerActivity) {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    FirebaseUtil.signIn();
                } else {
                    String userId = firebaseAuth.getUid();
                    validateAdmin(userId);
                }
                Toast.makeText(callerActivity.getBaseContext(), "Welcome back", Toast.LENGTH_LONG).show();
            }
        };


    }

    private static void validateAdmin(String userId) {
        FirebaseUtil.isAdmin = false;
        DatabaseReference databaseReference = mFirebaseDatabase.getReference().child(caller.getString(R.string.admin)).child(userId);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showAdminMenu();
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
        databaseReference.addChildEventListener(childEventListener);

    }

    private static void signIn() {
        FirebaseUtil.isAdmin = false;
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }

    public static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public static void connectStorage() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child(caller.getString(R.string.deals_pictures));

    }
}
