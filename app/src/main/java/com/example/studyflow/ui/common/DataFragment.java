package com.example.studyflow.ui.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.studyflow.MainActivity;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.data.sync.StudyDataSync;
import java.util.*;

/** Lifecycle-scoped Room observation keeps all dashboards current after edits. */
public abstract class DataFragment extends Fragment {
    protected LinearLayout body;protected ScrollView scroll;
    protected StudyFlowDatabase db;
    protected List<Course> courses=new ArrayList<>();protected List<Task> tasks=new ArrayList<>();
    protected List<ClassSchedule> schedules=new ArrayList<>();protected List<StudyPlan> plans=new ArrayList<>();
    protected List<FocusSession> sessions=new ArrayList<>();
    protected int loaded;
    @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle state){
        scroll=new ScrollView(requireContext());scroll.setBackgroundColor(Ui.BG);scroll.setFillViewport(true);
        body=Ui.column(requireContext());int pad=Ui.dp(requireContext(),14);body.setPadding(pad,pad,pad,pad*2);scroll.addView(body);return scroll;
    }
    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle saved){
        super.onViewCreated(v,saved);loaded=0;db=StudyFlowDatabase.getDatabase(requireContext());
        db.courseDao().getAllCourses().observe(getViewLifecycleOwner(),l->{courses=l==null?new ArrayList<>():l;loaded|=1;refresh();});
        db.taskDao().getAllTasks().observe(getViewLifecycleOwner(),l->{tasks=l==null?new ArrayList<>():l;loaded|=2;refresh();});
        db.classScheduleDao().getAllSchedules().observe(getViewLifecycleOwner(),l->{schedules=l==null?new ArrayList<>():l;loaded|=4;refresh();});
        db.studyPlanDao().getAllStudyPlans().observe(getViewLifecycleOwner(),l->{plans=l==null?new ArrayList<>():l;loaded|=8;refresh();});
        db.focusSessionDao().getAllSessions().observe(getViewLifecycleOwner(),l->{sessions=l==null?new ArrayList<>():l;loaded|=16;refresh();});
    }
    protected void refresh(){
        if(body==null||loaded!=31)return;int y=scroll.getScrollY();body.removeAllViews();render();
        scroll.post(()->{if(scroll!=null)scroll.scrollTo(0,y);});
    }
    protected abstract void render();
    protected MainActivity host(){return (MainActivity)requireActivity();}
    protected Course course(Integer id){if(id!=null)for(Course c:courses)if(c.getCourseId()==id)return c;return null;}
    protected Task task(int id){for(Task t:tasks)if(t.getTaskId()==id)return t;return null;}
    protected void write(Runnable operation,Runnable success){
        android.content.Context appContext=requireContext().getApplicationContext();
        StudyFlowDatabase.IO.execute(()->{
            try{operation.run();StudyDataSync.upload(appContext);new Handler(Looper.getMainLooper()).post(()->{if(isAdded()&&getView()!=null){if(success!=null)success.run();}});}
            catch(Exception e){new Handler(Looper.getMainLooper()).post(()->{if(isAdded())new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Could not save").setMessage(e.getMessage()==null?"Please try again.":e.getMessage()).setPositiveButton("Close",null).show();});}
        });
    }
    protected void taskDetail(Task task){
        new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle(task.getTitle())
                .setMessage("Time: "+Ui.date(task.getDeadline())+"\nEstimated: "+task.getEstimatedMinutes()+" minutes\nProgress: "+Ui.progress(task)+"%\nStatus: "+Ui.status(task))
                .setPositiveButton("Close",null).show();
    }
    @Override public void onDestroyView(){super.onDestroyView();body=null;scroll=null;}
}
