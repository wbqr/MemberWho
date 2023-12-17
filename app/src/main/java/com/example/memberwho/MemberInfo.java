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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityMemberInfoBinding;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class MemberInfo extends AppCompatActivity {
    ActivityMemberInfoBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    Vector<String> participant = new Vector<>();
    private static final String TAG = "silent_from_MemberInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberInfoBinding.inflate(getLayoutInflater());
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

                            boolean isFinished = (boolean) data.get("isFinished");
                            if (isFinished) {
                                binding.addNewMemberButton.setOnClickListener(view -> {
                                    Log.d(TAG, "new member button clicked");
                                    showToast("완료된 프로젝트에 초대할 수 없습니다.");
                                });
                            }
                            else {
                                binding.addNewMemberButton.setOnClickListener(view -> {
                                    Log.d(TAG, "new member button clicked");
                                    startActivity(new Intent(MemberInfo.this, AddNewMember.class)
                                            .putExtra("pid", pid)
                                            .putExtra("participant", participant));
                                });
                            }

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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MemberInfo.this));
        binding.recyclerView.setAdapter(new MyAdapter(participant));
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
                                    startActivity(new Intent(MemberInfo.this, OtherUser.class)
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
                                if (binding.memberSetToDo.getVisibility() == View.GONE)
                                    binding.memberSetToDo.setVisibility(View.VISIBLE);
                                else
                                    binding.memberSetToDo.setVisibility((View.GONE));
                            });
                            binding.memberSaveWorkButton.setOnClickListener(view -> {
                                Log.d(TAG, "save info button clicked");
                                DocumentReference docRef3 = db.collection("userProjects").document(pid);
                                docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Map<String, Object> data3 = document.getData();

                                                Vector<String> willBeWork = new Vector<>();

                                                willBeWork.add(binding.memberSetToDo1.getText().toString());
                                                willBeWork.add(binding.memberSetToDo2.getText().toString());
                                                willBeWork.add(binding.memberSetToDo3.getText().toString());

                                                HashMap<String, Object> participant = (HashMap<String, Object>) data3.get("participant");
                                                HashMap<String, Object> tmpUser = (HashMap<String, Object>) participant.get(tmpUid);
                                                List<String> workToDo = (List<String>) tmpUser.get("work");

                                                if (workToDo != null) {
                                                    tmpUser.replace("work", willBeWork);
                                                }
                                                else {
                                                    tmpUser.put("work", willBeWork);
                                                }
                                                participant.replace("participant", tmpUser);
                                                docRef3.update(data3);
                                                showToast("저장되었습니다.");
                                            }
                                        }
                                    }
                                });
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
                                                binding.memberSetToDo1.setText(workToDo.get(0));
                                                binding.memberSetToDo2.setText(workToDo.get(1));
                                                binding.memberSetToDo3.setText(workToDo.get(2));
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
    private class MyAdapter extends RecyclerView.Adapter<MemberInfo.MyViewHolder> {
        private List<String> participant;
        private MyAdapter(List<String> participant) {this.participant = participant;}

        @NonNull
        @Override
        public MemberInfo.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            MemberInfoBinding binding =
                    MemberInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new MemberInfo.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberInfo.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String tmpUid = participant.get(position);

            holder.bind(tmpUid);
        }

        @Override
        public int getItemCount() {
            return participant.size();
        }
    }
    private void saveWorkToServer() {
        if (mAuth != null) {

        }
    }
    private void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}