package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityProjectImageSettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProjectImageSetting extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    boolean imageChanged = false;
    String pid;
    String tmpUri;
    String TAG = "silent_P_ImageSetting";
    ActivityProjectImageSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectImageSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");

        binding.goBackButton.setOnClickListener(view -> finish());

        binding.findImageButton.setOnClickListener(view -> {
            Log.d(TAG, "find image clicked");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });
        binding.saveImageButton.setOnClickListener(view -> {
            Log.d(TAG, "save image clicked");
            if (!imageChanged)
                showToast("이미지를 선택하세요");
            else {
                uploadImageView();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    binding.projectImage.setImageURI(uri);
                    imageChanged = true;
                }
                break;
        }
    }
    public String getUserImageCode(String pid) {
        return "userProjects/" + pid + "/mainImage.jpg";
    }
    private void uploadImageView() {
        StorageReference storageRef = storage.getReference();
        StorageReference userImagesRef = storageRef.child(getUserImageCode(pid));

        Bitmap bitmap = ((BitmapDrawable) binding.projectImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //0-100
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = userImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "이미지 업로드 실패");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                tmpUri = taskSnapshot.getMetadata().getReference().toString();

                DocumentReference docRef = db.collection("userProjects").document(pid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> data = document.getData();

                                if (!data.containsKey("imageUri"))
                                    data.put("imageUri", tmpUri);
                                docRef.update(data);

                                Log.d(TAG, "이미지 업로드 성공");
                                showToast("저장되었습니다.");

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        });
    }
    private void showToast(String message) {
        Toast.makeText(ProjectImageSetting.this, message, Toast.LENGTH_SHORT).show();
    }
}