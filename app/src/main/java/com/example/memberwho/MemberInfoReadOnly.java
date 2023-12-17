package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityMemberInfoReadOnlyBinding;
import com.example.memberwho.databinding.MemberInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MemberInfoReadOnly extends AppCompatActivity {
    ActivityMemberInfoReadOnlyBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    Vector<String> participant = new Vector<>();
    String TAG = "silent_from_MI_readOnly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberInfoReadOnlyBinding.inflate(getLayoutInflater());
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
                            participant = new Vector<>();

                            HashMap<String, Object> tmp = (HashMap<String, Object>) data.get("participant");
                            Set<String> keySet = tmp.keySet();
                            participant.addAll(keySet);

                            Log.d(TAG, "Uid of member in this project: " + participant.toString());

                            projectBind(participant);

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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MemberInfoReadOnly.this));
        binding.recyclerView.setAdapter(new MemberInfoReadOnly.MyAdapter(participant));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private MemberInfoBinding binding;
        private MyViewHolder(MemberInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        private void bind(String tmpUid) {
            DocumentReference docRef = db.collection("userInfo")
                    .document(tmpUid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            String name = (String) data.get("name");
                            binding.memberName.setText(name);

                            binding.memberUid.setText(tmpUid);

                            binding.memberDetailButton.setOnClickListener(view -> {
                                Log.d(TAG, "member info button clicked");
                                startActivity(new Intent(MemberInfoReadOnly.this, OtherUser.class)
                                        .putExtra("guestId", tmpUid));
                            });

                            String uri = (String) data.get("imageUri");
                            if ((uri != null) && (!uri.equals(""))){
                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.memberProfileImage.setImageBitmap(bitmap);
                                    }
                                });
                            }
                            binding.memberSeeWorkToDoButton.setOnClickListener(view -> {
                                Log.d(TAG, "work to do button clicked");
                                if (binding.memberWorkToDo.getVisibility() == View.GONE)
                                    binding.memberWorkToDo.setVisibility(View.VISIBLE);
                                else
                                    binding.memberWorkToDo.setVisibility((View.GONE));
                            });
                            DocumentReference docRef2 = db.collection("userProjects").document(pid);
                            docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Map<String, Object> data2 = document.getData();

                                            HashMap<String, Object> participant = (HashMap<String, Object>) data2.get("participant");
                                            HashMap<String, Object> tmpUser = (HashMap<String, Object>) participant.get(tmpUid);

                                            List<String> workToDo = (List<String>) tmpUser.get("work");

                                            if (workToDo != null) {
                                                binding.memberWorkToDo1.setText(workToDo.get(0));
                                                binding.memberWorkToDo2.setText(workToDo.get(1));
                                                binding.memberWorkToDo3.setText(workToDo.get(2));
                                            }
                                        }
                                    }
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
    private class MyAdapter extends RecyclerView.Adapter<MemberInfoReadOnly.MyViewHolder> {
        private List<String> participant;
        private MyAdapter(List<String> participant) {this.participant = participant;}

        @NonNull
        @Override
        public MemberInfoReadOnly.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            MemberInfoBinding binding =
                    MemberInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new MemberInfoReadOnly.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberInfoReadOnly.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String tmpUid = participant.get(position);

            holder.bind(tmpUid);
        }

        @Override
        public int getItemCount() {
            return participant.size();
        }
    }
}
