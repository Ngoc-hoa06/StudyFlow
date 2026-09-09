package com.example.studyflow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.studyflow.ui.focus.FocusFragment;
import com.example.studyflow.ui.home.HomeFragment;
import com.example.studyflow.ui.profile.ProfileFragment;
import com.example.studyflow.ui.schedule.ScheduleFragment;
import com.example.studyflow.ui.task.TaskFragment;
import com.example.studyflow.ui.ai.AiFragment;
import com.example.studyflow.focus.*;
import com.example.studyflow.data.sync.StudyDataSync;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private boolean selecting;
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){startActivity(new Intent(this,LoginActivity.class));finish();return;}
        StudyDataSync.syncOnLogin(this);
        setContentView(R.layout.activity_main);
        android.view.View root=findViewById(R.id.mainRoot);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()|androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });
        androidx.core.view.ViewCompat.requestApplyInsets(root);
        if(Build.VERSION.SDK_INT>=33&&ActivityCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS},1001);
        bottomNavigation=findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item->{
            if(selecting)return true;
            int id=item.getItemId();
            if(id==R.id.nav_schedule)load(new ScheduleFragment());
            else if(id==R.id.nav_tasks)load(new TaskFragment());
            else if(id==R.id.nav_home)load(new HomeFragment());
            else if(id==R.id.nav_focus)load(new FocusFragment());
            else if(id==R.id.nav_profile)load(new ProfileFragment());else return false;
            return true;
        });
        bottomNavigation.setOnItemReselectedListener(item->{
            if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ProfileFragment){
                if(item.getItemId()==R.id.nav_home)navigateToHome();else {selecting=true;bottomNavigation.setSelectedItemId(R.id.nav_home);selecting=false;navigateToHome();}
            }
        });
        if(savedInstanceState==null){if(getIntent().getBooleanExtra("openFocus",false))navigateToFocus();else if(getIntent().getBooleanExtra("openSchedule",false))navigateToSchedule();else navigateToHome();}
        if(FocusController.get(this).current().active())FocusService.send(this,FocusService.RESTORE,null,"",0);
        android.content.Context appContext=getApplicationContext();
        com.example.studyflow.data.database.StudyFlowDatabase.IO.execute(()->{
            com.example.studyflow.data.database.StudyFlowDatabase db=com.example.studyflow.data.database.StudyFlowDatabase.getDatabase(appContext);
            for(com.example.studyflow.data.entity.ClassSchedule s:db.classScheduleDao().getSnapshot()){
                com.example.studyflow.data.entity.Course c=db.courseDao().getCourseById(s.getCourseId());
                if(c!=null){com.example.studyflow.notification.ClassReminderScheduler.cancel(appContext,s);com.example.studyflow.notification.ClassReminderScheduler.scheduleReminder(appContext,c,s);}
            }
        });
        getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true){
            @Override public void handleOnBackPressed(){
                Fragment f=getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof HomeFragment))navigateToHome();else{setEnabled(false);getOnBackPressedDispatcher().onBackPressed();}
            }
        });
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(bottomNavigation!=null){if(intent.getBooleanExtra("openFocus",false))navigateToFocus();else if(intent.getBooleanExtra("openSchedule",false))navigateToSchedule();}}
    private void load(Fragment fragment){getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();}
    private void select(int id,Fragment fragment){selecting=true;bottomNavigation.setSelectedItemId(id);selecting=false;load(fragment);}
    public void navigateToHome(){select(R.id.nav_home,new HomeFragment());}
    public void navigateToSchedule(){select(R.id.nav_schedule,new ScheduleFragment());}
    public void navigateToTasks(){select(R.id.nav_tasks,new TaskFragment());}
    public void navigateToFocus(){select(R.id.nav_focus,new FocusFragment());}
    // AI is opened from the Home assistant card and is intentionally not a
    // duplicate bottom-tab action; Profile remains the fifth tab.
    public void navigateToAi(){load(new AiFragment());}
    public void navigateToProfile(){load(new ProfileFragment());}
    public void navigateToSuggestedFocus(int taskId,int minutes){
        if(FocusController.get(this).current().active()){
            android.widget.Toast.makeText(this,"A Focus session is already running. The current session will be kept.",android.widget.Toast.LENGTH_LONG).show();navigateToFocus();return;
        }
        select(R.id.nav_focus,FocusFragment.suggested(taskId,minutes));
    }
}
