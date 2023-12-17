package com.example.memberwho;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.memberwho.databinding.ActivityForSignUpBinding;

public class ForSignUp extends AppCompatActivity {
    ActivityForSignUpBinding binding;
    UserInfo currentUser = UserInfo.getInstance();
    String email;
    private static final String TAG = "silent_ForSignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");

        binding.completeButton.setOnClickListener(view -> {
            updateUserInfo();
            currentUser.uploadData();
            Log.d(TAG, "complete button clicked");

            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("USER_PROFILE", getIntent().getStringExtra("USER_PROFILE")));
        });
    }
    private void updateUserInfo() {
        currentUser.setName(binding.editTextName.getText().toString());
        currentUser.setOrganization(binding.editTextOrg.getText().toString());
        currentUser.setPart(binding.editTextPart.getText().toString());
        currentUser.setPhoneNumber(binding.editTextPhoneNumber.getText().toString());
        currentUser.setEmail(email);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}