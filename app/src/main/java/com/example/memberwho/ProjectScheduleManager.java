package com.example.memberwho;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.memberwho.databinding.ActivityProjectScheduleManagerBinding;
import com.example.memberwho.databinding.EditScheduleBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProjectScheduleManager extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String uid = mAuth.getUid();
    String pid;
    HashMap<String, Object> detailSchedule;
    private Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    int[] todayDate = {year, month+1, day};
    int[] endYearMonthDate = new int[3];
    ActivityProjectScheduleManagerBinding binding;
    String TAG = "silent_from_P_Schedule_Manager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectScheduleManagerBinding.inflate(getLayoutInflater());
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

                            Vector<String> workToDoList = new Vector<>();
                            detailSchedule = (HashMap<String, Object>) data.get("detailSchedule");
                            if (detailSchedule == null) {
                                detailSchedule = new HashMap<>();
                            }
                            Set<String> keySet = detailSchedule.keySet();
                            workToDoList.addAll(keySet);

                            boolean isFinished = (boolean) data.get("isFinished");

                            binding.addNewScheduleButton.setOnClickListener(view -> {
                                Log.d(TAG, "add schedule button clicked");
                                if (isFinished) {
                                    showToast("완료된 프로젝트를 편집할 수 없습니다.");
                                    return;
                                }
                                int size = workToDoList.size();
                                String Sid = "Schedule_" + size;

                                HashMap<String, Object> workToDo = new HashMap<>();
                                workToDo.put("name", "");
                                workToDo.put("progress", "");
                                workToDo.put("dueDate", "");

                                detailSchedule.put(Sid, workToDo);
                                data.replace("detailSchedule", detailSchedule);
                                docRef.update(data);
                                updateUI();
                            });
                            projectBind(workToDoList);
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
    private void projectBind(List<String> participant) {
        if (participant == null) {
            participant = new Vector<>();
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ProjectScheduleManager.this));
        binding.recyclerView.setAdapter(new ProjectScheduleManager.MyAdapter(participant));
        //binding.recyclerView.addItemDecoration(new MyItemDecoration());
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private EditScheduleBinding binding;
        private MyViewHolder(EditScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        private void bind(String workToDo) {
            DocumentReference docRef = db.collection("userProjects")
                    .document(pid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            HashMap<String, Object> detailSchedule = (HashMap<String, Object>) data.get("detailSchedule");
                            HashMap<String, Object> tmp = (HashMap<String, Object>) detailSchedule.get(workToDo) ;

                            String name = (String) tmp.get("name");
                            binding.textScheduleName.setText(name);
                            binding.editScheduleName.setText(name);
                            binding.editScheduleName.setVisibility(View.GONE);

                            String progress = (String) tmp.get("progress");
                            binding.textScheduleProgress.setText(progress);
                            binding.editScheduleProgress.setText(progress);
                            binding.editScheduleProgress.setVisibility(View.GONE);

                            String dueDate = (String) tmp.get("dueDate");
                            if ((dueDate != null) && (!dueDate.equals(""))) {
                                String[] tmpEndYearMonthDate = dueDate.split("/");
                                Log.d(TAG, tmpEndYearMonthDate[0]);
                                Log.d(TAG, tmpEndYearMonthDate[1]);
                                Log.d(TAG, tmpEndYearMonthDate[2]);
                                for (int i = 0; i < 3; i++) {
                                    Log.d(TAG, tmpEndYearMonthDate[i]);
                                    endYearMonthDate[i] = Integer.parseInt(tmpEndYearMonthDate[i]);
                                }
                            }
                            binding.textDueDate.setText(dueDate);
                            binding.editDueDate.setText(dueDate);
                            binding.editDueDate.setVisibility(View.GONE);
                            binding.editDueDate.setOnClickListener(view -> {
                                Log.d(TAG, "end date button clicked");
                                DatePickerDialog dateDialog = new DatePickerDialog(ProjectScheduleManager.this,
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                                String tmp = year+"/"+(monthOfYear+1)+"/"+dayOfMonth;
                                                binding.editDueDate.setText(tmp);

                                                endYearMonthDate = new int[3];
                                                endYearMonthDate[0] = year;
                                                endYearMonthDate[1] = monthOfYear+1;
                                                endYearMonthDate[2] = dayOfMonth;
                                            }
                                        }, year, month, day);
                                dateDialog.show();
                            });

                            binding.textCondition.setOnClickListener(view -> {
                                String condition = binding.textCondition.getText().toString();
                                if (condition.equals("편집")) {
                                    Log.d(TAG, "edit button clicked");
                                    binding.textCondition.setText("완료");
                                    binding.textScheduleName.setVisibility(View.GONE);
                                    binding.textScheduleProgress.setVisibility(View.GONE);
                                    binding.textDueDate.setVisibility(View.GONE);

                                    binding.editScheduleName.setVisibility(View.VISIBLE);
                                    binding.editScheduleProgress.setVisibility(View.VISIBLE);
                                    binding.editDueDate.setVisibility(View.VISIBLE);
                                }
                                else {
                                    Log.d(TAG, "save button clicked");
                                    String tmpDate = binding.editDueDate.getText().toString();
                                    if (tmpDate.equals("")) {
                                        showToast("마감일을 입력하세요.");
                                    }
                                    else if (endDateIsEarlyThanToday())
                                        showToast("끝날 날짜가 오늘보다 빠릅니다.");
                                    else {
                                        binding.textCondition.setText("편집");
                                        binding.textScheduleName.setVisibility(View.VISIBLE);
                                        binding.textScheduleProgress.setVisibility(View.VISIBLE);
                                        binding.textDueDate.setVisibility(View.VISIBLE);

                                        binding.editScheduleName.setVisibility(View.GONE);
                                        binding.editScheduleProgress.setVisibility(View.GONE);
                                        binding.editDueDate.setVisibility(View.GONE);

                                        String tmpName = binding.editScheduleName.getText().toString();
                                        String tmpProgress = binding.editScheduleProgress.getText().toString();
                                        String tmpDueDate = binding.editDueDate.getText().toString();

//                                        binding.textScheduleName.setText(tmpName);
//                                        binding.textScheduleProgress.setText(tmpProgress);
//                                        binding.textDueDate.setText(tmpDueDate);

                                        tmp.replace("name", tmpName);
                                        tmp.replace("progress", tmpProgress);
                                        tmp.replace("dueDate", tmpDueDate);
                                        detailSchedule.replace(workToDo, tmp);

                                        data.replace("detailSchedule", detailSchedule);
                                        docRef.update(data);
                                        showToast("저장되었습니다.");
                                        updateUI();
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
    private class MyAdapter extends RecyclerView.Adapter<ProjectScheduleManager.MyViewHolder> {
        private List<String> workToDo;
        private MyAdapter(List<String> workToDo) {this.workToDo = workToDo;}

        @NonNull
        @Override
        public ProjectScheduleManager.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent /* recycler view */, int viewType) {
            EditScheduleBinding binding =
                    EditScheduleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            // parameter for inflation should be passed.
            // attachToParent: if true, attach as soon as it generated else attach it when it is needed.
            return new ProjectScheduleManager.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectScheduleManager.MyViewHolder holder, int position) { // 돌려막기 담당하는 부분(값 갱신)
            // position: index of view that should be attach
            String tmpUid = workToDo.get(position);

            holder.bind(tmpUid);
        }

        @Override
        public int getItemCount() {
            return workToDo.size();
        }
    }
    private void showToast(String message) {
        Toast.makeText(ProjectScheduleManager.this, message, Toast.LENGTH_SHORT).show();
    }
    private boolean endDateIsEarlyThanToday() {
        return isBefore(endYearMonthDate, todayDate);
    }
    private boolean isBefore(int[] startYearMonthDate, int[] endYearMonthDate) {
        //start...가 end...보다 더 나중이면 true 반환
        if (startYearMonthDate[0] < endYearMonthDate[0]) return true;
        else if (startYearMonthDate[1] < endYearMonthDate[1]) return true;
        else return startYearMonthDate[2] < endYearMonthDate[2];
    }
}