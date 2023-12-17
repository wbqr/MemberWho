package com.example.memberwho;

import java.util.HashMap;
import java.util.Vector;

public class Project{
    String TAG = "silent_from_Project";
    private static Project currentProject;

    public static Project getInstance() {
        return getInstance(false);
    }

    public static Project getInstance(boolean withNewProject) {
        if ((withNewProject) || (currentProject == null)){
            currentProject = new Project();
        }
        return currentProject;
    }
    private String pid, name, startDate, endDate, maker;
    private HashMap<String, Object> managers, participants, resources, detailSchedule;
    private boolean isFinished;
    private boolean isExported;
    private boolean isUserExported;
    private boolean isParticipantExported;
    private boolean isScheduleExported;
    private boolean isResourceExported;

    private Project() {}
    @Override
    public String toString() {
        return "Project{" +
                "pid='" + pid + '\'' +
                ", name='" + name + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", maker='" + maker + '\'' +
                ", isFinished=" + isFinished +
                ", isExported=" + isExported +
                ", isParticipantExported=" + isParticipantExported +
                ", isScheduleExported=" + isScheduleExported +
                ", isResourceExported=" + isResourceExported +
                '}';
    }

////////////////////////////////////////////////////////////////////////////////////////////
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public HashMap<String, Object> getManagers() {
        return managers;
    }

    public void setManagers(HashMap<String, Object> managers) {
        this.managers = managers;
    }

    public HashMap<String, Object> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, Object> participants) {
        this.participants = participants;
    }

    public HashMap<String, Object> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, Object> resources) {
        this.resources = resources;
    }

    public HashMap<String, Object> getDetailSchedule() {
        return detailSchedule;
    }

    public void setDetailSchedule(HashMap<String, Object> detailSchedule) {
        this.detailSchedule = detailSchedule;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean isExported() {
        return isExported;
    }

    public void setExported(boolean exported) {
        isExported = exported;
    }

    public boolean isUserExported() {
        return isUserExported;
    }

    public void setUserExported(boolean userExported) {
        isUserExported = userExported;
    }

    public boolean isParticipantExported() {
        return isParticipantExported;
    }

    public void setParticipantExported(boolean participantExported) {
        isParticipantExported = participantExported;
    }

    public boolean isScheduleExported() {
        return isScheduleExported;
    }

    public void setScheduleExported(boolean scheduleExported) {
        isScheduleExported = scheduleExported;
    }

    public boolean isResourceExported() {
        return isResourceExported;
    }

    public void setResourceExported(boolean resourceExported) {
        isResourceExported = resourceExported;
    }
}
