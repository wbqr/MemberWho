package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityMainBinding;
import com.example.memberwho.databinding.ForMainToProjectBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    private static final String TAG = "silentFromMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.businessCard.setOnClickListener(view -> {
            if (binding.tagViewGroup.getVisibility() == View.GONE) {
                Log.d(TAG, "Tag On");
                binding.tagViewGroup.setVisibility(View.VISIBLE);
            }
            else {
                Log.d(TAG, "Tag Off");
                binding.tagViewGroup.setVisibility(View.GONE);
            }
        });
        binding.menuButton.setOnClickListener(view -> {
            Log.d(TAG, "menu on");
            startActivity(new Intent(MainActivity.this, Menu.class));
        });

        String uidText = "Uid: " + uid;
        binding.textUid.setText(uidText);
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ForMainToProjectBinding binding;
        private MyViewHolder(ForMainToProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        private void bind(String pid) {
            DocumentReference docRef = db.collection("userProjects")
                    .document(pid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            String pid = (String) data.get("pid");
                            binding.itemPid.setText(pid);

                            String name = (String) data.get("name");
                            binding.itemProjectName.setText(name);

                            String startDate = (String) data.get("start date");
                            binding.itemStartDate.setText(startDate);

                            String endDate = (String) data.get("end date");
                            binding.itemEndDate.setText(endDate);

                            String maker = (String) data.get("maker");
                            HashMap<String, Object> manager = (HashMap<String, Object>) data.get("manager");
                            String tmp = (String) manager.get(maker);
                            binding.itemBoss.setText(tmp);

                            boolean isFinished = (boolean) data.get("isFinished");
                            binding.itemIsFinish.setText(isFinished ? "(완료된 프로젝트입니다.)" : "(진행 중인 프로젝트입니다.)");

                            binding.itemButton.setOnClickListener(view -> {
                                Log.d(TAG, "project :" + name + " get started");
                                startActivity(new Intent(MainActivity.this, ProjectMain.class)
                                        .putExtra("pid", binding.itemPid.getText()));
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
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<String> projects;
        private MyAdapter(List<String> projects) {this.projects = projects;}

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            ForMainToProjectBinding binding =
                    ForMainToProjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String pid = projects.get(position);

            holder.bind(pid);
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }
    }
    public void updateUI() {
        if (mAuth != null) {
            DocumentReference docRef = db.collection("userInfo").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            String name = (String) data.get("name");
                            binding.viewName.setText(name);

                            String organization = (String) data.get("organization");
                            binding.viewOrg.setText(organization);

                            String part = (String) data.get("part");
                            binding.viewPart.setText(part);

                            String phoneNumber = (String) data.get("phone number");
                            binding.viewPhoneNumber.setText(phoneNumber);

                            String tag1 = (String) data.get("tag1");
                            binding.viewTag1.setText(tag1);

                            String tag2 = (String) data.get("tag2");
                            binding.viewTag2.setText(tag2);

                            String tag3 = (String) data.get("tag3");
                            binding.viewTag3.setText(tag3);

                            String tag4 = (String) data.get("tag4");
                            binding.viewTag4.setText(tag4);

                            String uri = (String) data.get("imageUri");
                            if ((uri != null) && (!uri.equals(""))){
                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.userImageView.setImageBitmap(bitmap);
                                    }
                                });
                            }

                            List<String> projects = (List<String>) data.get("projects");
                            projectBind(projects);

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
    private void projectBind(List<String> projects) {
        if (projects == null) {
            projects = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        binding.recyclerView.setAdapter(new MyAdapter(projects));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}