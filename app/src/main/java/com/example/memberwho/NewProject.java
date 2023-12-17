package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memberwho.databinding.ActivityNewProjectBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class NewProject extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    UserInfo currentUser = UserInfo.getInstance();
    String TAG = "silent_NewProject";
    ActivityNewProjectBinding binding;
    private Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    int[] todayDate = {year, month+1, day};
    LocalDate today, startDate, endDate;
    int[] startYearMonthDate, endYearMonthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getUserInfoFromServer();

        binding.goBackButton.setOnClickListener(view -> finish());

        binding.createNewProjectButton.setOnClickListener(view -> {
            Log.d(TAG, "create new button clicked");
            if (binding.newProjectTitle.getText() == null) showToast("제목을 입력하세요.");
            else {
                String tmp = binding.newProjectTitle.getText().toString()
                        .replace(" ", "")
                        .replace("\t", "")
                        .replace("\n", "");
                if (tmp.equals(""))
                    showToast("제목을 입력하세요.");
                else if ((startDate == null) && (startYearMonthDate == null))
                    showToast("시작 날짜를 입력하세요.");
                else if ((endDate == null) && (endYearMonthDate == null))
                    showToast("끝낼 날짜를 입력하세요.");
                else if (endDateIsEarlyThanToday())
                    showToast("끝날 날짜가 오늘보다 빠릅니다.");
                else if (endDateIsEarlyThanStartDate())
                    showToast("시작 날짜가 끝날 날짜보다 빠릅니다.");
                else {
                    createNewProject(
                            binding.newProjectTitle.getText().toString(),
                            binding.startDateView.getText().toString(),
                            binding.endDateView.getText().toString()
                            );
                }
            }
        });

        binding.startDateView.setOnClickListener(view -> {
            Log.d(TAG, "start date button clicked");
            DatePickerDialog dateDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            String tmp = year+"/"+(monthOfYear+1)+"/"+dayOfMonth;
                            binding.startDateView.setText(tmp);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                today = LocalDate.now();
                                startDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                            }
                            else {
                                startYearMonthDate = new int[3];
                                startYearMonthDate[0] = year;
                                startYearMonthDate[1] = monthOfYear+1;
                                startYearMonthDate[2] = dayOfMonth;
                            }
                        }
                    }, year, month, day);
            dateDialog.show();
        });
        binding.endDateView.setOnClickListener(view -> {
            Log.d(TAG, "end date button clicked");
            DatePickerDialog dateDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            String tmp = year+"/"+(monthOfYear+1)+"/"+dayOfMonth;
                            binding.endDateView.setText(tmp);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                endDate = LocalDate.of(year, monthOfYear+1, dayOfMonth);
                            else {
                                endYearMonthDate = new int[3];
                                endYearMonthDate[0] = year;
                                endYearMonthDate[1] = monthOfYear+1;
                                endYearMonthDate[2] = dayOfMonth;
                            }
                        }
                    }, year, month, day);
            dateDialog.show();
        });
    }
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

                            String name = (String) data.get("name");
                            currentUser.setName(name);

                            List<String> projects = (List<String>) data.get("projects");
                            if (projects != null) {
                                Vector<String> tmp = new Vector<>();
                                for (String s: projects)
                                    tmp.add(s);
                                currentUser.setProjects(tmp);
                            }
                            else
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

    public void createNewProject(String name, String startDate, String endDate) {
        getUserInfoFromServer();

        if (currentUser.getProjects() == null)
            currentUser.setProjects(new Vector<>());
        Vector<String> tmpProjects = (Vector<String>) currentUser.getProjects();

        String pid = getPid(currentUser.getUid(), tmpProjects.size());

        CollectionReference userProjects = db.collection("userProjects");

        Map<String, Object> data = new HashMap<>();
        data.put("pid", pid);
        data.put("name", name);
        data.put("start date", startDate);
        data.put("end date", endDate);
        data.put("maker", uid);

        Map <String, Object> manager = new HashMap<>();
        manager.put(uid, currentUser.getName());
        data.put("manager", manager);

        Map <String, Object> participant = new HashMap<>();
        Map <String, Object> id = new HashMap<>();
        id.put("name", currentUser.getName());
        participant.put(uid, id);
        data.put("participant", participant);

        data.put("resources", null);
        data.put("detailSchedule", null);
        data.put("isFinished", false);
        data.put("isExported", false);
        data.put("isUserExported", false);
        data.put("isParticipantExported", false);
        data.put("isScheduleExported", false);
        data.put("isResourceExported", false);

        userProjects.document(pid)
                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "프로젝트 생성");
                        showToast("프로젝트 " + binding.newProjectTitle.getText().toString() + "이(가) 생성되었습니다.");
                        finish();
                    }
                });

        uid = FirebaseAuth.getInstance().getUid();
        DocumentReference docRef = db.collection("userInfo").document(uid);

        tmpProjects.add(pid);
        Map<String, Object> dataForUserInfo = new HashMap<>();
        dataForUserInfo.put("projects", tmpProjects);

        docRef.update(dataForUserInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }
    private boolean isBefore(int[] startYearMonthDate, int[] endYearMonthDate) {
        //start...가 end...보다 더 나중이면 true 반환
        if (startYearMonthDate[0] < endYearMonthDate[0]) return true;
        else if (startYearMonthDate[1] < endYearMonthDate[1]) return true;
        else return startYearMonthDate[2] < endYearMonthDate[2];
    }
    private boolean startDateIsEarlyThanToday() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return startDate.isBefore(today);
        }
        return isBefore(startYearMonthDate, todayDate);
    }
    private boolean endDateIsEarlyThanToday() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return endDate.isBefore(today);
        }
        return isBefore(endYearMonthDate, todayDate);
    }
    private boolean endDateIsEarlyThanStartDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return endDate.isBefore(startDate);
        return isBefore(endYearMonthDate, startYearMonthDate);
    }

    private String getPid(String uid, int nthProject) {
        return uid + "_project_" + nthProject;
    }
    public void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}