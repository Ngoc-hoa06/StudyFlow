package com.example.studyflow.data.sync;

import android.content.Context;

import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.ClassSchedule;
import com.example.studyflow.data.entity.Course;
import com.example.studyflow.data.entity.FocusSession;
import com.example.studyflow.data.entity.StudyPlan;
import com.example.studyflow.data.entity.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/** Synchronizes local Room data with the signed-in user's Firestore space. */
public final class StudyDataSync {
    private StudyDataSync() {}

    public static void syncOnLogin(Context context) {
        String uid = uid();
        if (uid == null) return;
        FirebaseFirestore store = FirebaseFirestore.getInstance();
        store.collection("users").document(uid).collection("courses").get()
                .addOnSuccessListener(courses -> mergeCourses(context, store, uid, courses));
    }

    public static void upload(Context context) {
        String uid = uid();
        if (uid == null) return;
        FirebaseFirestore store = FirebaseFirestore.getInstance();
        StudyFlowDatabase.IO.execute(() -> uploadAll(context.getApplicationContext(), store, uid));
    }

    private static void mergeCourses(Context context, FirebaseFirestore store, String uid, QuerySnapshot result) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        StudyFlowDatabase.IO.execute(() -> {
            for (DocumentSnapshot d : result.getDocuments()) {
                Course c = new Course(text(d, "courseName"), text(d, "lecturer"), text(d, "defaultRoom"), text(d, "color"));
                c.setCourseId(number(d, "courseId"));
                if (db.courseDao().getById(c.getCourseId()) == null) db.courseDao().insert(c); else db.courseDao().update(c);
            }
            store.collection("users").document(uid).collection("schedules").get()
                    .addOnSuccessListener(schedules -> mergeSchedules(context, store, uid, schedules));
        });
    }

    private static void mergeSchedules(Context context, FirebaseFirestore store, String uid, QuerySnapshot result) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        StudyFlowDatabase.IO.execute(() -> {
            for (DocumentSnapshot d : result.getDocuments()) {
                ClassSchedule s = new ClassSchedule(number(d, "courseId"), number(d, "dayOfWeek"), text(d, "startTime"), text(d, "endTime"), text(d, "room"), number(d, "reminderMinutes"));
                s.setScheduleId(number(d, "scheduleId"));
                if (db.classScheduleDao().getById(s.getScheduleId()) == null) db.classScheduleDao().insert(s); else db.classScheduleDao().update(s);
            }
            store.collection("users").document(uid).collection("tasks").get()
                    .addOnSuccessListener(tasks -> mergeTasks(context, store, uid, tasks));
        });
    }

    private static void mergeTasks(Context context, FirebaseFirestore store, String uid, QuerySnapshot result) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        StudyFlowDatabase.IO.execute(() -> {
            for (DocumentSnapshot d : result.getDocuments()) {
                Integer courseId = d.getLong("courseId") == null ? null : number(d, "courseId");
                Task t = new Task(courseId, text(d, "title"), text(d, "type"), longNumber(d, "deadline"), text(d, "priority"), number(d, "estimatedMinutes"), number(d, "completedMinutes"), text(d, "status"));
                t.setTaskId(number(d, "taskId"));
                if (db.taskDao().getById(t.getTaskId()) == null) db.taskDao().insert(t); else db.taskDao().update(t);
            }
            store.collection("users").document(uid).collection("plans").get()
                    .addOnSuccessListener(plans -> mergePlans(context, store, uid, plans));
        });
    }

    private static void mergePlans(Context context, FirebaseFirestore store, String uid, QuerySnapshot result) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        StudyFlowDatabase.IO.execute(() -> {
            for (DocumentSnapshot d : result.getDocuments()) {
                StudyPlan p = new StudyPlan(number(d, "taskId"), longNumber(d, "studyDate"), number(d, "plannedMinutes"), number(d, "completedMinutes"), text(d, "status"));
                p.setPlanId(number(d, "planId"));
                if (db.studyPlanDao().getById(p.getPlanId()) == null) db.studyPlanDao().insert(p); else db.studyPlanDao().update(p);
            }
            store.collection("users").document(uid).collection("focus_sessions").get()
                    .addOnSuccessListener(sessions -> mergeSessions(context, store, uid, sessions));
        });
    }

    private static void mergeSessions(Context context, FirebaseFirestore store, String uid, QuerySnapshot result) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        StudyFlowDatabase.IO.execute(() -> {
            for (DocumentSnapshot d : result.getDocuments()) {
                Integer courseId = d.getLong("courseId") == null ? null : number(d, "courseId");
                Integer taskId = d.getLong("taskId") == null ? null : number(d, "taskId");
                FocusSession s = new FocusSession(courseId, taskId, longNumber(d, "startTime"), number(d, "durationMinutes"));
                s.setSessionId(number(d, "sessionId"));
                s.setSessionKey(text(d, "sessionKey"));
                if (db.focusSessionDao().getById(s.getSessionId()) == null) db.focusSessionDao().insert(s);
            }
            uploadAll(context.getApplicationContext(), store, uid);
        });
    }

    private static void uploadAll(Context context, FirebaseFirestore store, String uid) {
        StudyFlowDatabase db = StudyFlowDatabase.getDatabase(context);
        String root = "users/" + uid;
        for (Course c : db.courseDao().getSnapshot()) upload(store, root, "courses", String.valueOf(c.getCourseId()), courseMap(c));
        for (ClassSchedule s : db.classScheduleDao().getSnapshot()) upload(store, root, "schedules", String.valueOf(s.getScheduleId()), scheduleMap(s));
        for (Task t : db.taskDao().getSnapshot()) upload(store, root, "tasks", String.valueOf(t.getTaskId()), taskMap(t));
        for (StudyPlan p : db.studyPlanDao().getSnapshot()) upload(store, root, "plans", String.valueOf(p.getPlanId()), planMap(p));
        for (FocusSession s : db.focusSessionDao().getSnapshot()) upload(store, root, "focus_sessions", String.valueOf(s.getSessionId()), sessionMap(s));
    }

    private static void upload(FirebaseFirestore store, String root, String collection, String id, Map<String, Object> data) {
        store.collection(root + "/" + collection).document(id).set(data);
    }

    private static Map<String, Object> courseMap(Course c) { Map<String,Object> m=new HashMap<>();m.put("courseId",c.getCourseId());m.put("courseName",c.getCourseName());m.put("lecturer",c.getLecturer());m.put("defaultRoom",c.getDefaultRoom());m.put("color",c.getColor());return m; }
    private static Map<String, Object> scheduleMap(ClassSchedule s) { Map<String,Object> m=new HashMap<>();m.put("scheduleId",s.getScheduleId());m.put("courseId",s.getCourseId());m.put("dayOfWeek",s.getDayOfWeek());m.put("startTime",s.getStartTime());m.put("endTime",s.getEndTime());m.put("room",s.getRoom());m.put("reminderMinutes",s.getReminderMinutes());return m; }
    private static Map<String, Object> taskMap(Task t) { Map<String,Object> m=new HashMap<>();m.put("taskId",t.getTaskId());m.put("courseId",t.getCourseId());m.put("title",t.getTitle());m.put("type",t.getType());m.put("deadline",t.getDeadline());m.put("priority",t.getPriority());m.put("estimatedMinutes",t.getEstimatedMinutes());m.put("completedMinutes",t.getCompletedMinutes());m.put("status",t.getStatus());return m; }
    private static Map<String, Object> planMap(StudyPlan p) { Map<String,Object> m=new HashMap<>();m.put("planId",p.getPlanId());m.put("taskId",p.getTaskId());m.put("studyDate",p.getStudyDate());m.put("plannedMinutes",p.getPlannedMinutes());m.put("completedMinutes",p.getCompletedMinutes());m.put("status",p.getStatus());return m; }
    private static Map<String, Object> sessionMap(FocusSession s) { Map<String,Object> m=new HashMap<>();m.put("sessionId",s.getSessionId());m.put("sessionKey",s.getSessionKey());m.put("courseId",s.getCourseId());m.put("taskId",s.getTaskId());m.put("startTime",s.getStartTime());m.put("durationMinutes",s.getDurationMinutes());return m; }
    private static String uid() { return FirebaseAuth.getInstance().getCurrentUser() == null ? null : FirebaseAuth.getInstance().getCurrentUser().getUid(); }
    private static String text(DocumentSnapshot d, String key) { String value=d.getString(key);return value==null?"":value; }
    private static int number(DocumentSnapshot d, String key) { Long value=d.getLong(key);return value==null?0:value.intValue(); }
    private static long longNumber(DocumentSnapshot d, String key) { Long value=d.getLong(key);return value==null?0L:value; }
}
