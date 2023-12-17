package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityMemberInfoReadOnlyBinding;
import com.example.memberwho.databinding.ActivityProjectScheduleUserBinding;
import com.example.memberwho.databinding.MemberInfoBinding;
import com.example.memberwho.databinding.ScheduleBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProjectScheduleUser extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    Vector<String> workToDo;
    ActivityProjectScheduleUserBinding binding;
    String TAG = "silent_from_P_Schedule_User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectScheduleUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");

        binding.goBackButton.setOnClickListener(view -> finish());
    }
    @Override
    protected void onStart() {
        super.onStart();
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

                            workToDo = new Vector<>();
                            HashMap<String, Object> tmp = (HashMap<String, Object>) data.get("detailSchedule");
                            if (tmp == null)
                                return;
                            Set<String> keySet = tmp.keySet();
                            workToDo.addAll(keySet);

                            projectBind(workToDo);

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
    private void projectBind(List<String> participant) {
        if (participant == null) {
            participant = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ProjectScheduleUser.this));
        binding.recyclerView.setAdapter(new ProjectScheduleUser.MyAdapter(participant));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ScheduleBinding binding;
        private MyViewHolder(ScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        private void bind(String workToDo) {
            DocumentReference docRef = db.collection("userProjects")
                    .document(pid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            HashMap<String, Object> detailSchedule = (HashMap<String, Object>) data.get("detailSchedule");
                            HashMap<String, Object> tmp = (HashMap<String, Object>) detailSchedule.get(workToDo);

                            String name = (String) tmp.get("name");
                            binding.textScheduleName.setText(name);

                            String progress = (String) tmp.get("progress");
                            binding.textScheduleProgress.setText(progress);

                            String dueDate = (String) tmp.get("dueDate");
                            binding.textScheduleDueDate.setText(dueDate);
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
    private class MyAdapter extends RecyclerView.Adapter<ProjectScheduleUser.MyViewHolder> {
        private List<String> workToDo;
        private MyAdapter(List<String> workToDo) {this.workToDo = workToDo;}

        @NonNull
        @Override
        public ProjectScheduleUser.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            ScheduleBinding binding =
                    ScheduleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new ProjectScheduleUser.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectScheduleUser.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String tmpUid = workToDo.get(position);

            holder.bind(tmpUid);
        }

        @Override
        public int getItemCount() {
            return workToDo.size();
        }
    }
}