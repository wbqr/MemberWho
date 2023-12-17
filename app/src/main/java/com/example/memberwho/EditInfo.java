package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityEditInfoBinding;
import com.example.memberwho.databinding.ForMainToProjectBinding;
import com.example.memberwho.databinding.ForProjectSettingBinding;
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
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class EditInfo extends AppCompatActivity {

    ActivityEditInfoBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    UserInfo currentUser;
    boolean imageChanged = false;
    HashMap<String, Object> isCheckList = new HashMap<>();
    HashMap<String, Object> managerList = new HashMap<>();
    HashMap<String, Object> participantList = new HashMap<>();
    String uid = mAuth.getUid();
    String TAG = "silent_EditInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getUserInfoFromServer();
        updateUI();

        binding.setUserImageButton.setOnClickListener(view -> {
            Log.d(TAG, "image select clicked");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        binding.goBackButton.setOnClickListener(view -> finish());
        binding.saveButton.setOnClickListener(view -> {
            Log.d(TAG, "save button clicked");
            if (imageChanged)
                uploadUserImageView();
            updateUserInfo();
            updateProjectExportedSetting();
            currentUser.uploadData();
        });
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ForProjectSettingBinding binding;
        private MyViewHolder(ForProjectSettingBinding binding) {
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
                            binding.itemSettingPid.setText(pid);

                            String name = (String) data.get("name");
                            binding.itemSettingProjectName.setText(name);

                            String startDate = (String) data.get("start date");
                            binding.itemSettingStartDate.setText(startDate);

                            String endDate = (String) data.get("end date");
                            binding.itemSettingEndDate.setText(endDate);

                            HashMap<String, Object> manager = (HashMap<String, Object>) data.get("manager");
                            String tmp = (String) manager.get(uid);
                            binding.itemSettingBoss.setText(tmp);
                            managerList.put(pid, manager);

                            boolean isFinished = (boolean) data.get("isFinished");
                            binding.itemSettingIsFinish.setText(isFinished ? "(완료된 프로젝트입니다.)" : "(진행 중인 프로젝트입니다.)");

                            boolean isUserExported = (boolean) data.get("isUserExported");
                            binding.itemSettingUserExported.setChecked(isUserExported);
                            isCheckList.put(pid, isUserExported);

                            HashMap<String, Object> participant = (HashMap<String, Object>) data.get("participant");
                            participantList.put(pid, participant);

                            binding.itemSettingUserExported.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                    isCheckList.replace(pid, isChecked);
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
    private class MyAdapter extends RecyclerView.Adapter<EditInfo.MyViewHolder> {
        private List<String> projects;
        private MyAdapter(List<String> projects) {this.projects = projects;}

        @NonNull
        @Override
        public EditInfo.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            ForProjectSettingBinding binding =
                    ForProjectSettingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new EditInfo.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull EditInfo.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String pid = projects.get(position);;

            holder.bind(pid);
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }
    }
    private void projectBind(List<String> projects) {
        if (projects == null) {
            projects = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(EditInfo.this));
        binding.recyclerView.setAdapter(new EditInfo.MyAdapter(projects));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    public void getUserInfoFromServer() {
        currentUser = UserInfo.getInstance();
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
                            currentUser.setEmail(email);

                            String name = (String) data.get("name");
                            currentUser.setName(name);

                            String organization = (String) data.get("organization");
                            currentUser.setOrganization(organization);

                            String part = (String) data.get("part");
                            currentUser.setPart(part);

                            String phoneNumber = (String) data.get("phone number");
                            currentUser.setPhoneNumber(phoneNumber);

                            String tag1 = (String) data.get("tag1");
                            currentUser.setTag1(tag1);

                            String tag2 = (String) data.get("tag2");
                            currentUser.setTag2(tag2);

                            String tag3 = (String) data.get("tag3");
                            currentUser.setTag3(tag3);

                            String tag4 = (String) data.get("tag4");
                            currentUser.setTag4(tag4);

                            String uri = (String) data.get("imageUri");
                            currentUser.setImageUri(uri);

                            if ((uri != null) && (!uri.equals(""))){

                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.setUserImageButton.setImageBitmap(bitmap);
                                    }
                                });
                            }
                            List<String> projects = (List<String>) data.get("projects");
                            currentUser.setProjects(projects);

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
                            binding.editTextName.setText(name);

                            String organization = (String) data.get("organization");
                            binding.editTextOrg.setText(organization);

                            String part = (String) data.get("part");
                            binding.editTextPart.setText(part);

                            String phoneNumber = (String) data.get("phone number");
                            binding.editTextPhoneNumber.setText(phoneNumber);

                            String tag1 = (String) data.get("tag1");
                            binding.editTextTag1.setText(tag1);

                            String tag2 = (String) data.get("tag2");
                            binding.editTextTag2.setText(tag2);

                            String tag3 = (String) data.get("tag3");
                            binding.editTextTag3.setText(tag3);

                            String tag4 = (String) data.get("tag4");
                            binding.editTextTag4.setText(tag4);

                            String uri = (String) data.get("imageUri");
                            if ((uri != null) && (!uri.equals(""))){

                                StorageReference gsReference = storage.getReferenceFromUrl(uri);

                                final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                                gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                        binding.setUserImageButton.setImageBitmap(bitmap);
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
    private void updateProjectExportedSetting() {
        for (String pid: isCheckList.keySet()) {
            DocumentReference docRef = db.collection("userProjects")
                    .document(pid);

            HashMap<String, Object> tmp = new HashMap<>();
            tmp.put("isUserExported", isCheckList.get(pid));

            String newName = binding.editTextName.getText().toString();

            HashMap<String, Object> tmpManager = (HashMap<String, Object>) managerList.get(pid);
            tmpManager.replace(uid, newName);
            tmp.put("manager", tmpManager);

            HashMap<String, Object> tmpParticipant = (HashMap <String, Object>) participantList.get(pid);
            HashMap <String, Object> tmpUid = (HashMap <String, Object>) tmpParticipant.get(uid);
            tmpUid.replace("name", newName);
            tmpParticipant.replace(uid, tmpUid);
            tmp.put("participant", tmpParticipant);

            docRef.update(tmp);
            updateUI();
        }
    }
    private void updateUserInfo() {
        currentUser.setName(binding.editTextName.getText().toString());
        currentUser.setOrganization(binding.editTextOrg.getText().toString());
        currentUser.setPart(binding.editTextPart.getText().toString());
        currentUser.setPhoneNumber(binding.editTextPhoneNumber.getText().toString());
        currentUser.setTag1(binding.editTextTag1.getText().toString());
        currentUser.setTag2(binding.editTextTag2.getText().toString());
        currentUser.setTag3(binding.editTextTag3.getText().toString());
        currentUser.setTag4(binding.editTextTag4.getText().toString());
        currentUser.setImageUri(binding.tmpUri.getText().toString());

        showToast("저장되었습니다.");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    binding.setUserImageButton.setImageURI(uri);
                    imageChanged = true;
                }
                break;
        }
    }
    private void uploadUserImageView() {
        StorageReference storageRef = storage.getReference();
        StorageReference userImagesRef = storageRef.child(getUserImageCode(uid));

        Bitmap bitmap = ((BitmapDrawable) binding.setUserImageButton.getDrawable()).getBitmap();
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
                binding.tmpUri.setText(taskSnapshot.getMetadata().getReference().toString());
                Log.d(TAG, binding.tmpUri.getText().toString());

                Log.d(TAG, "이미지 업로드 성공");
                updateUserInfo();
                currentUser.uploadData();
            }
        });
    }
    public String getUserImageCode(String uid) {
        return uid + "/" + uid + ".jpg";
    }
    public void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}