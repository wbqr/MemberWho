package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class UserInfo {
    private UserInfo() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = mAuth.getUid();
    }
    private static UserInfo currentUser;
    private static UserInfo guest;
    public String TAG = "silent_from_UserInfo";

    public static UserInfo getInstance() {
        return new UserInfo();
    }
    public static UserInfo getGuest(String Uid) {
        if (guest == null) {
            guest = new UserInfo();
        }
        return guest;
    }

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;
    private String email, name, organization, part, phoneNumber;
    private String tag1, tag2, tag3, tag4;
    String imageUri; //for image

    int imageSwitch;
    private List<String> projects;


//    public void downloadImageTo(Context context, ImageView imageView) {
//        StorageReference storageRef = storage.getReference();
//        StorageReference userImagesRef = storageRef.child(getUserImageCode(uid));
//
//        // Download directly from StorageReference using Glide
//        // (See MyAppGlideModule for Loader registration)
//        Glide.with(context)
//                .load(userImagesRef)
//                .into(imageView);
//    }
//
//    public String getUserImageCode(String uid) {
//        return uid + "/" + uid + ".jpg";
//    }
    public void getUserInfoFromServer() {
        if (mAuth != null) {
            DocumentReference docRef = db.collection("userInfo").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            //Log.d(TAG, "DocumentSnapshot data: " + data);

                            String email = (String) data.get("email");
                            UserInfo.currentUser.setEmail(email);

                            String name = (String) data.get("name");
                            UserInfo.currentUser.setName(name);

                            String organization = (String) data.get("organization");
                            UserInfo.currentUser.setOrganization(organization);

                            String part = (String) data.get("part");
                            UserInfo.currentUser.setPart(part);

                            String phoneNumber = (String) data.get("phone number");
                            UserInfo.currentUser.setPhoneNumber(phoneNumber);

                            String tag1 = (String) data.get("tag1");
                            UserInfo.currentUser.setTag1(tag1);

                            String tag2 = (String) data.get("tag2");
                            UserInfo.currentUser.setTag2(tag2);

                            String tag3 = (String) data.get("tag3");
                            UserInfo.currentUser.setTag3(tag3);

                            String tag4 = (String) data.get("tag4");
                            UserInfo.currentUser.setTag4(tag4);

                            List<String> projects = (List<String>) data.get("projects");
                            UserInfo.currentUser.setProjects(projects);

                            //Log.d(TAG, UserInfo.currentUser.toString());

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
    public void uploadData() {
        CollectionReference userInfo = db.collection("userInfo");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("email", getEmail());
        data1.put("name", getName());
        data1.put("organization", getOrganization());
        data1.put("part", getPart());
        data1.put("phone number", getPhoneNumber());
        data1.put("tag1", getTag1());
        data1.put("tag2", getTag2());
        data1.put("tag3", getTag3());
        data1.put("tag4", getTag4());
        data1.put("imageUri", getImageUri());
        data1.put("imageSwitch", getImageSwitch());
        data1.put("projects", getProjects());
        data1.put("timestamp", FieldValue.serverTimestamp());
        Log.d("silent", "DataSet");

        userInfo.document(uid).set(data1);
        Log.d("silent", "userInfoUploaded");
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "UserInfo{" +
                "email='" + email + '\'' +
                "name='" + name + '\'' +
                ", organization='" + organization + '\'' +
                ", part='" + part + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", tag1='" + tag1 + '\'' +
                ", tag2='" + tag2 + '\'' +
                ", tag3='" + tag3 + '\'' +
                ", tag4='" + tag4 + '\'' +
                '}';
    }
    public String getUid() {
        return uid;
    }
    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPart() {
        return part;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTag1() {
        return tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public String getTag4() {
        return tag4;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }

    public void setTag4(String tag4) {
        this.tag4 = tag4;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public int getImageSwitch() {
        return imageSwitch;
    }

    public void setImageSwitch(int imageSwitch) {
        this.imageSwitch = imageSwitch;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
