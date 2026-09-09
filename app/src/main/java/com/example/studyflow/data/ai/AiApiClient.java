package com.example.studyflow.data.ai;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.studyflow.data.entity.StudyPlan;
import com.example.studyflow.data.entity.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/** Calls the private backend; the OpenAI key never ships in the Android app. */
public final class AiApiClient {
    private static final String URL = "http://10.0.2.2:3000/analyze";
    private AiApiClient() {}

    public interface Callback {
        void onSuccess(String recommendation, String mode);
        void onError(String message);
    }

    public static void analyze(Context context, List<Task> tasks, List<StudyPlan> plans, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            JSONArray taskArray = new JSONArray();
            for (Task task : tasks) {
                JSONObject item = new JSONObject();
                item.put("taskId", task.getTaskId());
                item.put("title", task.getTitle());
                item.put("type", task.getType());
                item.put("deadline", task.getDeadline());
                item.put("priority", task.getPriority());
                item.put("estimatedMinutes", task.getEstimatedMinutes());
                item.put("completedMinutes", task.getCompletedMinutes());
                item.put("status", task.getStatus());
                taskArray.put(item);
            }
            JSONArray planArray = new JSONArray();
            for (StudyPlan plan : plans) {
                JSONObject item = new JSONObject();
                item.put("planId", plan.getPlanId());
                item.put("taskId", plan.getTaskId());
                item.put("studyDate", plan.getStudyDate());
                item.put("plannedMinutes", plan.getPlannedMinutes());
                item.put("completedMinutes", plan.getCompletedMinutes());
                item.put("status", plan.getStatus());
                planArray.put(item);
            }
            body.put("tasks", taskArray);
            body.put("plans", planArray);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    body,
                    response -> callback.onSuccess(response.optString("recommendation", ""), response.optString("mode", "BACKEND")),
                    error -> callback.onError(error.getMessage() == null ? "AI service is unavailable." : error.getMessage())
            );
            RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
            queue.add(request);
        } catch (Exception exception) {
            callback.onError(exception.getMessage() == null ? "Could not prepare AI request." : exception.getMessage());
        }
    }
}
