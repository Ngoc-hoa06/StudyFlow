package com.example.studyflow.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(
        tableName = "class_schedules",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "courseId",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("courseId")}
)
public class ClassSchedule {

    @PrimaryKey(autoGenerate = true)
    private int scheduleId;

    private int courseId;
    /**
     * The calendar day for this one-time class session, stored at local midnight.
     * A value of zero is kept only for schedules created by older app versions.
     */
    @ColumnInfo(defaultValue = "0")
    private long dateMillis;
    private int dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private int reminderMinutes;

    public ClassSchedule(int courseId,
                         int dayOfWeek,
                         String startTime,
                         String endTime,
                         String room,
                         int reminderMinutes) {

        this.courseId = courseId;
        this.dateMillis = 0L;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.reminderMinutes = reminderMinutes;
    }

    /** Creates a date-specific class session. */
    @Ignore
    public ClassSchedule(int courseId,
                         long dateMillis,
                         String startTime,
                         String endTime,
                         String room,
                         int reminderMinutes) {
        this(courseId, dayOfWeek(dateMillis), startTime, endTime, room, reminderMinutes);
        this.dateMillis = dateMillis;
    }

    private static int dayOfWeek(long millis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }
}
