package com.example.studyflow.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "focus_sessions",
        foreignKeys = {
                @ForeignKey(
                        entity = Course.class,
                        parentColumns = "courseId",
                        childColumns = "courseId",
                        onDelete = ForeignKey.SET_NULL
                ),
                @ForeignKey(
                        entity = Task.class,
                        parentColumns = "taskId",
                        childColumns = "taskId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("courseId"),
                @Index("taskId"),
                @Index(value = "sessionKey", unique = true)
        }
)
public class FocusSession {

    @PrimaryKey(autoGenerate = true)
    private int sessionId;

    private String sessionKey;

    // Có thể null đối với Focus tự do.
    private Integer courseId;
    private Integer taskId;

    private long startTime;
    private int durationMinutes;

    public FocusSession(
            Integer courseId,
            Integer taskId,
            long startTime,
            int durationMinutes
    ) {
        this.courseId = courseId;
        this.taskId = taskId;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}