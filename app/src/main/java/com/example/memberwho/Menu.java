package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityMenuBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class Menu extends AppCompatActivity {
    ActivityMenuBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    private static final String TAG = "silent_menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateUI();

        binding.editInfoButton.setOnClickListener(view -> {
            Log.d(TAG, "edit info button clicked");
            startActivity(new Intent(Menu.this, EditInfo.class));
        });
        binding.newProjectButton.setOnClickListener(view -> {
            Log.d(TAG, "new project button clicked");
            startActivity(new Intent(Menu.this, NewProject.class));
        });
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "sign out button clicked");
                signOut();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }
    private void signOut() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        // Check if there is no current user.
        if (firebaseAuth.getCurrentUser() == null)
            Log.d(TAG, "signOut:success");
        else
            Log.d(TAG, "signOut:failure");
        startActivity(new Intent(Menu.this, EmailPasswordActivity.class));
    }
    private void updateUI() {
        if (mAuth != null) {
            DocumentReference docRef = db.collection("userInfo").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            String email = (String) data.get("email");
                            binding.email.setText(email);

                            String uri = (String) data.get("imageUri");
                            if ((uri != null) && (!uri.equals(""))){
                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.menuUserImageView.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }
}