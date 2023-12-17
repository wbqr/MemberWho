package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityAddNewMemberBinding;
import com.example.memberwho.databinding.MemberInfoBinding;
import com.example.memberwho.databinding.MemberInfoForInviteBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AddNewMember extends AppCompatActivity {
    ActivityAddNewMemberBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    String tmpUserName = "";
    Vector<String> currentParticipant = new Vector<>();
    private static final String TAG = "silent_AddNewMember";
    Vector<String> allUsers = new Vector<>();
    Vector<String> inviteTargets = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");
        updateUI();

        binding.goBackButton.setOnClickListener(view -> finish());

        binding.inviteButton.setOnClickListener(view -> {
            Log.d(TAG, "invite Button clicked");
            if (inviteTargets.size() == 0)
                showToast("선택된 사용자가 없습니다.");
            else
                invite(inviteTargets, 0);
        });
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

                            HashMap<String, Object> tmpPart = (HashMap<String, Object>) data.get("participant");
                            currentParticipant.addAll(tmpPart.keySet());

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            db.collection("userInfo")
                    .orderBy("name")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                allUsers = new Vector<>();
                                QuerySnapshot result = task.getResult();
                                List<DocumentSnapshot> documents = result.getDocuments();
                                for (DocumentSnapshot document: documents) {
                                    String tmp = document.getId();
                                    if (!currentParticipant.contains(tmp))
                                        allUsers.add(tmp);
                                }
                                Log.d(TAG, "all Users: " + allUsers.toString());

                                projectBind(allUsers);
                            }
                        }
                    });
        }
    }
    private void projectBind(List<String> participant) {
        if (participant == null) {
            participant = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(AddNewMember.this));
        binding.recyclerView.setAdapter(new AddNewMember.MyAdapter(participant));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private MemberInfoForInviteBinding binding;
        private MyViewHolder(MemberInfoForInviteBinding binding) {
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
                            binding.invitedMemberName.setText(name);

                            binding.invitedMemberUid.setText(tmpUid);

                            binding.inviteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean selected) {
                                    if (selected)
                                        inviteTargets.add(tmpUid);
                                    else
                                        inviteTargets.remove(tmpUid);
                                    Log.d(TAG, inviteTargets.toString());
                                }
                            });

                            String uri = (String) data.get("imageUri");
                            if ((uri != null) && (!uri.equals(""))){

                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.invitedMemberProfile.setImageBitmap(bitmap);
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
    private class MyAdapter extends RecyclerView.Adapter<AddNewMember.MyViewHolder> {
        private List<String> participant;
        private MyAdapter(List<String> participant) {this.participant = participant;}

        @NonNull
        @Override
        public AddNewMember.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            MemberInfoForInviteBinding binding =
                    MemberInfoForInviteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new AddNewMember.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull AddNewMember.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String tmpUid = participant.get(position);

            holder.bind(tmpUid);
        }

        @Override
        public int getItemCount() {
            return participant.size();
        }
    }
    private void invite(List<String> inviteTargets, int index) {
        if (inviteTargets.size() <= index) {
            showToast("초대 완료했습니다.");
            finish();
            return;
        }
        if (mAuth != null) {
            String tmpUid = inviteTargets.get(index);
            Log.d(TAG, tmpUid);
            DocumentReference docRef = db.collection("userInfo").document(tmpUid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // UserInfo update
                            Map<String, Object> data = document.getData();

                            tmpUserName = (String) data.get("name");

                            Vector<String> tmpProjects = new Vector<>();

                            List<String> projects = (List<String>) data.get("projects");
                            if (projects != null) {
                                Vector<String> tmp = new Vector<>();
                                for (String s : projects)
                                    tmp.add(s);
                                tmpProjects = tmp;
                            }
                            if (tmpProjects.contains(pid))
                                return;
                            tmpProjects.add(pid);
                            data.replace("projects", tmpProjects);

                            docRef.update(data);

                            // update currentProject
                            DocumentReference proRef = db.collection("userProjects").document(pid);
                            proRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> projectTask) {
                                    if (projectTask.isSuccessful()) {
                                        DocumentSnapshot projectDocument = projectTask.getResult();
                                        if (projectDocument.exists()) {
                                            // UserInfo update
                                            Map<String, Object> projectData = projectDocument.getData();

                                            HashMap<String, Object> tmpPart = (HashMap<String, Object>) projectData.get("participant");
                                            if (tmpPart.containsKey(tmpUid))
                                                return;
                                            HashMap<String, Object> tmpUser = new HashMap<>();
                                            tmpUser.put("name", tmpUserName);
                                            tmpPart.put(tmpUid, tmpUser);
                                            projectData.put("participant", tmpPart);

                                            proRef.update(projectData);
                                            invite(inviteTargets, index + 1);
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
    private void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}