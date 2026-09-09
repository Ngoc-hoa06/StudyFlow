package com.example.studyflow.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "study_plans",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "taskId",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("taskId")}
)
public class StudyPlan {

    @PrimaryKey(autoGenerate = true)
    private int planId;

    private int taskId;

    // Ngày học được lưu dưới dạng milliseconds
    private long studyDate;

    // Thời gian được hệ thống lên kế hoạch
    private int plannedMinutes;

    // Thời gian người dùng thực tế đã học
    private int completedMinutes;

    // PLANNED, IN_PROGRESS, COMPLETED, MISSED
    private String status;

    public StudyPlan(int taskId,
                     long studyDate,
                     int plannedMinutes,
                     int completedMinutes,
                     String status) {

        this.taskId = taskId;
        this.studyDate = studyDate;
        this.plannedMinutes = plannedMinutes;
        this.completedMinutes = completedMinutes;
        this.status = status;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(long studyDate) {
        this.studyDate = studyDate;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public void setPlannedMinutes(int plannedMinutes) {
        this.plannedMinutes = plannedMinutes;
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void setCompletedMinutes(int completedMinutes) {
        this.completedMinutes = completedMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}