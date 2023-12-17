package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityProjectResourceBinding;
import com.example.memberwho.databinding.ActivityProjectResourceUploadBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ProjectResourceUpload extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    Uri selectedFile;
    Map<String, Object> resources;
    ActivityProjectResourceUploadBinding binding;
    private static final String TAG = "silentFromProResource";
    private static final int REQ_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectResourceUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");

        binding.selectFileButton.setOnClickListener(view -> chooseFile());
        binding.fileUploadButton.setOnClickListener(view -> uploadFile());
    }
    private void chooseFile() {
        Intent intent = new Intent().setType("*/*")
                .setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE && resultCode == RESULT_OK) {
            selectedFile = data.getData(); //The uri with the location of the file
            String tmp = selectedFile.getPath();

            binding.selectFileButton.setText(tmp);
            binding.editFileName.setText(getFileNameFromUri(selectedFile));

//            // 경로 정보:  selectedfile.getPath()
//            // 전체 URI 정보: selectedfile.toString()
//            Toast.makeText(getApplicationContext(),
//                    getFileNameFromUri(selectedfile), Toast.LENGTH_LONG).show();
//            Log.d(TAG, "Selected: " + selectedfile.toString());
        }
    }
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = "";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            Log.d(TAG, "Display Name: " + fileName);
        }
        cursor.close();

        return fileName;
    }
    private void uploadFile() {
        if (selectedFile == null)
            Toast.makeText(this, "파일을 선택하세요", Toast.LENGTH_SHORT).show();
        else {
            DocumentReference docRef = db.collection("userProjects")
                    .document(pid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            resources = (HashMap<String, Object>) data.get("resources");
                            if (resources == null)
                                resources = new HashMap<>();
                            int size = resources.size();
                            String fileId = "userProjects/" + pid + "/resource_" + size;

                            StorageReference storageRef = storage.getReference();
                            StorageReference fileRef = storageRef.child(fileId);
                            UploadTask uploadTask = fileRef.putFile(selectedFile);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Calendar c = Calendar.getInstance();
                                    int year = c.get(Calendar.YEAR);
                                    int month = c.get(Calendar.MONTH) + 1;
                                    int day = c.get(Calendar.DAY_OF_MONTH);

                                    String date = year + "/" + month + "/" + day;

                                    HashMap<String, Object> fileInfo = new HashMap<>();
                                    fileInfo.put("name", binding.editFileName.getText().toString());
                                    fileInfo.put("maker", uid);
                                    fileInfo.put("madeDate", date);

                                    resources.put(fileId, fileInfo);
                                    data.replace("resources", resources);
                                    docRef.update(data);
                                    Toast.makeText(ProjectResourceUpload.this, "업로드 되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
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