package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityProjectPermissionSettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProjectPermissionSettings extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    ActivityProjectPermissionSettingsBinding binding;
    private static final String TAG = "silent_P_PermSetting";
    boolean[] switchList = new boolean[5];
        // [프로젝트 완료, 명함에 출력, 참여자 명단, 일정, 리소스 모음]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectPermissionSettingsBinding.inflate(getLayoutInflater());
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
                            switchList[0] = isFinished;
                            binding.projectIsFinishedSwitch.setChecked(isFinished);
                            binding.projectIsFinishedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    Log.d(TAG, "project finish: " + b);
                                    switchList[0] = b;
                                }
                            });

                            boolean isExported = (boolean) data.get("isExported");
                            switchList[1] = isExported;
                            binding.projectDisplayInBusinessCardSwitch.setChecked(isExported);
                            binding.projectDisplayInBusinessCardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    Log.d(TAG, "project exported: " + b);
                                    switchList[1] = b;
                                }
                            });

                            boolean isParticipantExported = (boolean) data.get("isParticipantExported");
                            switchList[2] = isParticipantExported;
                            binding.displayMemberListSwitch.setChecked(isParticipantExported);
                            binding.displayMemberListSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    Log.d(TAG, "project member exported: " + b);
                                    switchList[2] = b;
                                }
                            });

                            boolean isScheduleExported = (boolean) data.get("isScheduleExported");
                            switchList[3] = isScheduleExported;
                            binding.displayCalendarSwitch.setChecked(isScheduleExported);
                            binding.displayCalendarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    Log.d(TAG, "project schedule exported: " + b);
                                    switchList[3] = b;
                                }
                            });

                            boolean isResourceExported = (boolean) data.get("isResourceExported");
                            switchList[4] = isResourceExported;
                            binding.displayResourcesSwitch.setChecked(isResourceExported);
                            binding.displayResourcesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    Log.d(TAG, "project resource exported: " + b);
                                    switchList[4] = b;
                                }
                            });

                            binding.saveButton.setOnClickListener(view -> {
                                Log.d(TAG, "save button clicked");
                                data.replace("isFinished", switchList[0]);
                                data.replace("isExported", switchList[1]);
                                data.replace("isParticipantExported", switchList[2]);
                                data.replace("isScheduleExported", switchList[3]);
                                data.replace("isResourceExported", switchList[4]);

                                docRef.update(data);
                                showToast("저장되었습니다.");
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
    public void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}