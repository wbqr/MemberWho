package com.example.memberwho;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityEditInfoBinding;
import com.example.memberwho.databinding.ActivityProjectMainBinding;
import com.example.memberwho.databinding.ActivityProjectResourceBinding;
import com.example.memberwho.databinding.ForMainToProjectBinding;
import com.example.memberwho.databinding.ResourceInfoBinding;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Dummy {

}
//    // Byte를 Bitmap으로 변환
//    public Bitmap byteArrayToBitmap( byte[] byteArray ) {
//        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
//        return bitmap ;
//    }
//출처: https://crazykim2.tistory.com/434 [차근차근 개발일기+일상:티스토리]

//        // 디바이스 스크린 높이 알아내기
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        int height = displayMetrics.heightPixels;
//        int maxHeight = (int) (height*0.5);
//
//        // bottom sheet의 최대 높이 설정
//        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
//        bottomSheetBehavior.setMaxHeight(maxHeight);