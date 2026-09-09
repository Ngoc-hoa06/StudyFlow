package com.example.studyflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studyflow.data.entity.FocusSession;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    long insert(FocusSession focusSession);

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    LiveData<List<FocusSession>> getAllSessions();

    @Query("SELECT * FROM focus_sessions WHERE courseId = :courseId ORDER BY startTime DESC")
    LiveData<List<FocusSession>> getSessionsByCourse(int courseId);

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions")
    LiveData<Integer> getTotalFocusMinutes();

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE courseId = :courseId")
    LiveData<Integer> getTotalFocusMinutesForCourse(int courseId);
    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    LiveData<List<FocusSession>> getSessionsByTask(int taskId);
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    List<FocusSession> getSnapshot();
    @Query("SELECT * FROM focus_sessions WHERE sessionId = :id LIMIT 1")
    FocusSession getById(int id);
}
