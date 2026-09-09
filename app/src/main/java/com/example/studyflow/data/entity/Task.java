package com.example.studyflow.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tasks",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "courseId",
                childColumns = "courseId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("courseId")}
)
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int taskId;

    private Integer courseId;

    private String title;

    // Assignment, Project, Exam
    private String type;

    // Deadline stored as milliseconds
    private long deadline;

    // LOW, MEDIUM, HIGH
    private String priority;

    // Estimated time required to finish the task
    private int estimatedMinutes;

    // Actual completed study time
    private int completedMinutes;

    // NOT_STARTED, IN_PROGRESS, COMPLETED
    private String status;

    // Wall-clock timestamp when the current in-progress work started
    private long progressStartedAt;

    public Task(Integer courseId,
                String title,
                String type,
                long deadline,
                String priority,
                int estimatedMinutes,
                int completedMinutes,
                String status) {

        this.courseId = courseId;
        this.title = title;
        this.type = type;
        this.deadline = deadline;
        this.priority = priority;
        this.estimatedMinutes = estimatedMinutes;
        this.completedMinutes = completedMinutes;
        this.status = status;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
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

    public long getProgressStartedAt() {
        return progressStartedAt;
    }

    public void setProgressStartedAt(long progressStartedAt) {
        this.progressStartedAt = progressStartedAt;
    }
}
