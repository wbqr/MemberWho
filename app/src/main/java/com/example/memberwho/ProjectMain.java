package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityProjectMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ProjectMain extends AppCompatActivity {
    ActivityProjectMainBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    String TAG = "silent_ProjectMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pid = getIntent().getStringExtra("pid");

        binding.goBackButton.setOnClickListener(view -> finish());
        binding.projectDetailScheduleButton.setOnClickListener(view -> {
            Log.d(TAG, "detail schedule button clicked");
            startActivity(
                    new Intent(ProjectMain.this, ProjectScheduleUser.class)
                            .putExtra("pid", pid));
        });
        binding.projectMainToResourceButton.setOnClickListener(view -> {
            Log.d(TAG, "resource button clicked");
            startActivity(new Intent(ProjectMain.this, ProjectResource.class)
                    .putExtra("pid", pid)
                    .putExtra("fromGuest", false));
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI(pid);
    }
    public void updateUI(String pid) {
        DocumentReference docRef = db.collection("userProjects").document(pid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String uid = FirebaseAuth.getInstance().getUid();
                        String tmp;

                        Map<String, Object> data = document.getData();

                        String name = (String) data.get("name");
                        binding.projectMainName.setText(name);

                        String startDate = (String) data.get("start date");
                        String endDate = (String) data.get("end date");
                        String tmpDate = "프로젝트 기간: " + startDate + " ~ " + endDate;
                        binding.projectMainDate.setText(tmpDate);

                        String maker = (String) data.get("maker");

                        HashMap<String, Object> manager = (HashMap<String, Object>) data.get("manager");

                        HashMap<String, Object> participant = (HashMap<String, Object>) data.get("participant");

                        tmp = (String) manager.get(maker);
                        binding.projectMainParticipantButton
                            .setText("참여자 명단\n" + tmp + "\n외 " + (participant.size()-1) + " 명");

                        binding.projectMainParticipantButton.setOnClickListener(view -> {
                            Log.d(TAG, "participant button clicked");
                            startActivity(new Intent(ProjectMain.this, MemberInfoReadOnly.class)
                                    .putExtra("pid", pid));
                        });
                        binding.projectMainSettings.setOnClickListener(view -> {
                            Log.d(TAG, "project settting button clicked");
                            if (uid.equals(maker))
                            startActivity(new Intent(ProjectMain.this, ProjectMainSetting.class)
                                    .putExtra("pid", pid));
                            else
                                showToast("설정 변경은 관리자만 가능합니다.");
                        });
                        String uri = (String) data.get("imageUri");
                        if ((uri != null) && (!uri.equals(""))){
                            StorageReference gsReference = storage.getReferenceFromUrl(uri);

                            final long EIGHT_MEGABYTE = 1024 * 1024 * 8;
                            gsReference.getBytes(EIGHT_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                    binding.projectMainFrame.setImageBitmap(bitmap);
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
    private void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}