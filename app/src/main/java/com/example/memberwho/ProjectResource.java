package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityProjectResourceBinding;
import com.example.memberwho.databinding.MemberInfoBinding;
import com.example.memberwho.databinding.ResourceInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProjectResource extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    Uri selectedFile;
    Map<String, Object> resources;
    boolean fromGuest;
    // Request code for creating a PDF document.
    private static final int CREATE_FILE = 1;
    private static final int REQ_CODE = 123;
    StorageReference resourceRef;
    ActivityProjectResourceBinding binding;
    private static final String TAG = "silent_P_Resource";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");
        fromGuest = getIntent().getBooleanExtra("fromGuest", false);

        binding.selectFileButton.setOnClickListener(view -> chooseFile());
        binding.goBackButton.setOnClickListener(view -> finish());
        if (fromGuest)
            binding.uploadResourceButton.setVisibility(View.GONE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }
    private void updateUI(boolean clear) {
        if (clear) {
            binding.uploadUI.setVisibility(View.GONE);
            binding.editFileName.setText("");
            binding.selectFileButton.setText("파일 선택");
            selectedFile = null;
        }
        updateUI();
    }
    private void updateUI() {
        if (mAuth != null) {
            DocumentReference docRef = db.collection("userProjects").document(pid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            boolean isFinished = (boolean) data.get("isFinished");
                            if (isFinished) {
                                binding.uploadResourceButton.setOnClickListener(view -> {
                                    Log.d(TAG, "upload resource button clicked");
                                        showToast("완료된 프로젝트는 수정할 수 없습니다.");
                                });
                            }
                            else {
                                binding.uploadResourceButton.setOnClickListener(view -> {
                                    Log.d(TAG, "upload resource clicked");
                                    if (binding.uploadUI.getVisibility() == View.VISIBLE)
                                        uploadFile();
                                    else
                                        binding.uploadUI.setVisibility(View.VISIBLE);
                                });
                            }

                            Vector<String> resourcesId = new Vector<>();
                            HashMap<String, Object> resources = (HashMap<String, Object>) data.get("resources");
                            if (resources == null)
                                resources = new HashMap<>();
                            if (resources.size() != 0) {
                                Set<String> keySet = resources.keySet();
                                resourcesId.addAll(keySet);

                                projectBind(resourcesId);
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
    private void chooseFile() {
        Log.d(TAG, "choose file button clicked");
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
            String tmp = selectedFile.getPath(); //경로 정보

            binding.selectFileButton.setText(tmp);
            binding.editFileName.setText(getFileNameFromUri(selectedFile));

//            // 경로 정보:  selectedfile.getPath()
//            // 전체 URI 정보: selectedfile.toString()
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
        if (selectedFile == null) {
            Toast.makeText(this, "파일을 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = binding.editFileName.getText().toString();
        String[] tmpName = name.split("\\.");
        if (tmpName.length == 1) {
            showToast("확장자를 입력하세요.");
        }
        else if (tmpName.length > 2) {
            showToast("잘못된 이름 형식입니다.");
        }
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

                                    fileInfo.put("name", name);
                                    fileInfo.put("maker", uid);
                                    fileInfo.put("madeDate", date);

                                    resources.put(fileId, fileInfo);
                                    data.replace("resources", resources);
                                    docRef.update(data);
                                    showToast("업로드 되었습니다.");
                                    updateUI(true);
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
    private void projectBind(List<String> resourcesId) {
        if (resourcesId == null) {
            resourcesId = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ProjectResource.this));
        binding.recyclerView.setAdapter(new ProjectResource.MyAdapter(resourcesId));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ResourceInfoBinding binding;
        private MyViewHolder(ResourceInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        private void bind(String resourceId) {
            DocumentReference docRef = db.collection("userProjects").document(pid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            HashMap<String, Object> resources = (HashMap<String, Object>) data.get("resources");
                            HashMap<String, Object> fileInfo = (HashMap<String, Object>) resources.get(resourceId);
                            if (fileInfo != null) {
                                String name = (String) fileInfo.get("name");
                                binding.resourceName.setText(name);

                                String maker = (String) fileInfo.get("maker");
                                DocumentReference docRef2 = db.collection("userInfo").document(maker);
                                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Map<String, Object> data2 = document.getData();
                                                String makerName = (String) data2.get("name");

                                                binding.resourceMaker.setText(makerName);
                                            }
                                        }
                                    }
                                });
                                String madeDate = (String) fileInfo.get("madeDate");
                                binding.resourceMadeDate.setText(madeDate);

                                binding.downloadResourceButton.setOnClickListener(view -> {
                                    Log.d(TAG, "resource download button clicked");
                                    File fileDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                                    String[] tmpName = name.split("\\.");
                                    String prefix = tmpName[0];
                                    String suffix = "." + tmpName[1];

                                    StorageReference storageRef = storage.getReference();

                                    resourceRef = storageRef.child(resourceId);

                                    try {
                                        File localFile = File.createTempFile(prefix, suffix, fileDownloads);
                                        resourceRef.getFile(localFile);
                                        showToast("다운로드 완료");
                                    } catch (IOException e) {
                                        Log.d(TAG, "fail to create localFile");
                                        throw new RuntimeException(e);
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
    private class MyAdapter extends RecyclerView.Adapter<ProjectResource.MyViewHolder> {
        private List<String> resourcesId;
        private MyAdapter(List<String> participant) {this.resourcesId = participant;}

        @NonNull
        @Override
        public ProjectResource.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            ResourceInfoBinding binding =
                    ResourceInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new ProjectResource.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectResource.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String resourceId = resourcesId.get(position);

            holder.bind(resourceId);
        }

        @Override
        public int getItemCount() {
            return resourcesId.size();
        }
    }

    public void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}