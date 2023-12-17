package com.example.memberwho;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.memberwho.databinding.ActivityProjectMainSettingsBinding;

public class ProjectMainSetting extends AppCompatActivity {
    ActivityProjectMainSettingsBinding binding;
    String TAG = "silent_P_MainSetting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectMainSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String pid = getIntent().getStringExtra("pid");

        binding.editUsersButton.setOnClickListener(view -> {
            Log.d(TAG, "member info button clicked");
            startActivity(new Intent(ProjectMainSetting.this, MemberInfo.class)
                    .putExtra("pid", pid));
        });

        binding.editScheduleButton.setOnClickListener(view -> {
            Log.d(TAG, "schedule button clicked");
            startActivity(new Intent(ProjectMainSetting.this, ProjectScheduleManager.class)
                    .putExtra("pid", pid));
        });

        binding.editProjectMainImageButton.setOnClickListener(view -> {
            Log.d(TAG, "image setting button clicked");
            startActivity(new Intent(ProjectMainSetting.this, ProjectImageSetting.class)
                    .putExtra("pid", pid));
        });

        binding.setProjectPermissionButton.setOnClickListener(view -> {
            Log.d(TAG, "setting button clicked");
            startActivity(new Intent(ProjectMainSetting.this, ProjectPermissionSettings.class)
                    .putExtra("pid", pid));
        });
    }
}