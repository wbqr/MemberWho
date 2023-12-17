package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityOtherProjectBinding;
import com.example.memberwho.databinding.ActivityOtherUserBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class OtherProject extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String guestPid;
    String TAG = "silent_OtherProject";
    ActivityOtherProjectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        guestPid = getIntent().getStringExtra("guestPid");

        binding.goBackButton.setOnClickListener(view -> finish());
    }
    @Override
    protected void onStart() {
        super.onStart();
        updateUI(guestPid);
    }
    public void updateUI(String pid) {
        DocumentReference docRef = db.collection("userProjects").document(pid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
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
                        boolean isParticipantExported = (boolean) data.get("isParticipantExported");

                        String tmp;
                        tmp = (String) manager.get(maker);
                        binding.projectMainParticipantButton
                                .setText(isParticipantExported ?
                                        "참여자 명단\n" + tmp + "\n외 " + (participant.size()-1) + " 명"
                                        : "참여자 명단이\n비공개로\n설정되었습니다."
                                        );

                        binding.projectMainParticipantButton.setOnClickListener(view -> {
                            Log.d(TAG, "main participant button clicked");
                            if (isParticipantExported) {
                                    startActivity(new Intent(OtherProject.this, MemberInfoReadOnly.class)
                                            .putExtra("pid", pid)); }
                            else showToast("비공개된 정보입니다.");
                        });

                        boolean isScheduleExported = (boolean) data.get("isScheduleExported");
                        binding.otherProjectDetailScheduleButton.setOnClickListener(view -> {
                            Log.d(TAG, "detail schedule button clicked");
                            if (isScheduleExported) {
                                startActivity(new Intent(OtherProject.this, ProjectScheduleUser.class)
                                        .putExtra("pid", pid)); }
                            else showToast("비공개된 정보입니다.");
                        });

                        boolean isResourceExported = (boolean) data.get("isResourceExported");
                        binding.otherProjectToResourceButton.setOnClickListener(view -> {
                            Log.d(TAG, "resource button clicked");
                            if (isResourceExported) {
                                startActivity(new Intent(OtherProject.this, ProjectResource.class)
                                        .putExtra("pid", pid)
                                        .putExtra("fromGuest", true)); }
                            else showToast("비공개된 정보입니다.");
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
    private void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}