package com.example.studyflow.ui.task;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.Course;
import com.example.studyflow.data.entity.Task;
import com.example.studyflow.ui.common.DataFragment;
import com.example.studyflow.ui.common.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.*;

public class TaskFragment extends DataFragment {
    private int filter, sort;
    private String query = "";
    private boolean searchExpanded;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressTicker = new Runnable() {
        @Override public void run() {
            if (!isAdded() || getView() == null) return;
            boolean active = false;
            for (Task task : tasks) if ("IN_PROGRESS".equals(task.getStatus())) { active = true; break; }
            if (active) refresh();
            progressHandler.postDelayed(this, 1000);
        }
    };

    @Override public void onStart() {
        super.onStart();
        progressHandler.removeCallbacks(progressTicker);
        progressHandler.post(progressTicker);
    }
    @Override protected void render() {
        LinearLayout header=Ui.row(requireContext()); body.addView(header);
        LinearLayout title=Ui.column(requireContext()); header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Ui.text(title,"StudyFlow",15,true).setTextColor(Ui.BLUE);
        Ui.text(title,"Tasks",30,true);
        Ui.text(title,"Manage study and personal tasks",14,false);
        MaterialButton search=new MaterialButton(requireContext()); search.setText("⌕"); search.setTextSize(28); search.setTextColor(Ui.INK);
        search.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF)); search.setContentDescription("Search tasks");
        search.setOnClickListener(v->{searchExpanded=!searchExpanded;refresh();});
        header.addView(search,new LinearLayout.LayoutParams(Ui.dp(requireContext(),56),Ui.dp(requireContext(),56)));
        LinearLayout quote=Ui.card(body); Ui.text(quote,"“Small steps make big progress.”",14,false);
        if(searchExpanded){ EditText input=Ui.input(body,"Search",query,InputType.TYPE_CLASS_TEXT); input.setHint("Search tasks...");
            input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){query=s.toString().trim().toLowerCase(Locale.ROOT);} public void afterTextChanged(Editable e){}}); }
        LinearLayout chips=Ui.row(requireContext()); body.addView(chips);
        chip(chips,"All ("+tasks.size()+")",filter==0,()->setFilter(0));
        chip(chips,"Study ("+countType("Assignment","Project","Exam")+")",filter==4,()->setFilter(4));
        chip(chips,"Personal ("+countType("Personal","Work")+")",filter==5,()->setFilter(5));
        LinearLayout sortRow=Ui.row(requireContext());
        sortRow.setPadding(0,Ui.dp(requireContext(),8),0,Ui.dp(requireContext(),2));
        body.addView(sortRow);
        TextView sortText=Ui.text(sortRow,"Sort by",14,true);
        sortText.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        MaterialButton sortButton=new MaterialButton(requireContext());
        sortButton.setText(sort==0?"Due date":"Priority");
        sortButton.setAllCaps(false);
        sortButton.setTextColor(Ui.INK);
        sortButton.setTextSize(13);
        sortButton.setMinHeight(Ui.dp(requireContext(),42));
        sortButton.setMinWidth(Ui.dp(requireContext(),120));
        sortButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.LINE));
        sortButton.setOnClickListener(v->{sort=sort==0?1:0;refresh();});
        sortButton.setContentDescription("Change task sorting");
        sortRow.addView(sortButton,new LinearLayout.LayoutParams(Ui.dp(requireContext(),120),Ui.dp(requireContext(),46)));
        List<Task> ordered=filteredTasks(); renderGroup("To Do ("+countIncomplete(ordered)+")",ordered,false); renderGroup("Completed ("+countCompleted(ordered)+")",ordered,true);
        Ui.button(body,"+  Add Task",()->edit(null));
    }
    private void chip(LinearLayout p,String label,boolean selected,Runnable action){MaterialButton b=Ui.button(p,label,action);b.setTextSize(13);b.setMinHeight(Ui.dp(requireContext(),46));b.setPadding(Ui.dp(requireContext(),10),0,Ui.dp(requireContext(),10),0);if(selected){b.setTextColor(0xFFFFFFFF);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.BLUE));}Ui.weight(b);}
    private void setFilter(int value){filter=value;refresh();}
    private int countType(String...types){int n=0;for(Task t:tasks)if(Arrays.asList(types).contains(t.getType()))n++;return n;}
    private List<Task> filteredTasks(){List<Task> r=new ArrayList<>();for(Task t:tasks){if(!query.isEmpty()&&!t.getTitle().toLowerCase(Locale.ROOT).contains(query))continue;if(filter==1&&!"NOT_STARTED".equals(t.getStatus()))continue;if(filter==2&&!"IN_PROGRESS".equals(t.getStatus()))continue;if(filter==3&&!"COMPLETED".equals(t.getStatus()))continue;if(filter==4&&!Arrays.asList("Assignment","Project","Exam").contains(t.getType()))continue;if(filter==5&&!Arrays.asList("Personal","Work").contains(t.getType()))continue;r.add(t);}Comparator<Task> due=Comparator.comparingLong(t->t.getDeadline()<=0?Long.MAX_VALUE:t.getDeadline());if(sort==0)r.sort(due);else r.sort(Comparator.comparingInt((Task t)->"HIGH".equals(t.getPriority())?0:"MEDIUM".equals(t.getPriority())?1:2).thenComparing(due));return r;}
    private int countIncomplete(List<Task> l){int n=0;for(Task t:l)if(!"COMPLETED".equals(t.getStatus()))n++;return n;}
    private int countCompleted(List<Task> l){int n=0;for(Task t:l)if("COMPLETED".equals(t.getStatus()))n++;return n;}
    private void renderGroup(String heading,List<Task> list,boolean completed){LinearLayout section=Ui.card(body);Ui.text(section,heading,20,true);Ui.text(section,completed?"Great job! Keep going!":"Tasks you need to complete",13,false);int n=0;for(Task t:list)if("COMPLETED".equals(t.getStatus())==completed){renderTaskCard(section,t);n++;}if(n==0)Ui.text(section,completed?"No completed tasks yet.":"No matching tasks.",14,false);}
    private void renderTaskCard(LinearLayout parent,Task task){MaterialCardView card=new MaterialCardView(requireContext());card.setRadius(Ui.dp(requireContext(),16));card.setCardElevation(0);card.setCardBackgroundColor(0xFFFFFFFF);card.setStrokeColor(Ui.LINE);card.setStrokeWidth(Ui.dp(requireContext(),1));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=Ui.dp(requireContext(),10);parent.addView(card,cp);
        LinearLayout row=Ui.row(requireContext());row.setPadding(Ui.dp(requireContext(),8),Ui.dp(requireContext(),8),Ui.dp(requireContext(),4),Ui.dp(requireContext(),8));card.addView(row);
        CheckBox check=new CheckBox(requireContext());check.setChecked("COMPLETED".equals(task.getStatus()));check.setContentDescription("Mark task complete: "+task.getTitle());check.setOnClickListener(v->updateStatus(task,check.isChecked()?"COMPLETED":"IN_PROGRESS"));row.addView(check);
        TextView icon=new TextView(requireContext());icon.setText(iconFor(task));icon.setTextSize(21);icon.setGravity(Gravity.CENTER);icon.setBackground(Ui.background(requireContext(),colorFor(task),18));row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(requireContext(),48),Ui.dp(requireContext(),48)));
        LinearLayout info=Ui.column(requireContext());row.addView(info,new LinearLayout.LayoutParams(0,-2,1));Ui.text(info,task.getTitle(),16,true);Course c=course(task.getCourseId());Ui.text(info,Ui.date(task.getDeadline())+" · "+(c==null?task.getType():c.getCourseName()),12,false);
        LinearProgressIndicator bar=new LinearProgressIndicator(requireContext());bar.setMax(1000);bar.setTrackColor(0xFFE7ECF0);bar.setIndicatorColor(Ui.BLUE);bar.setTrackThickness(Ui.dp(requireContext(),7));bar.setTrackCornerRadius(Ui.dp(requireContext(),4));bar.setProgress(Ui.progressPermille(task));info.addView(bar,new LinearLayout.LayoutParams(-1,Ui.dp(requireContext(),9)));Ui.text(info,Ui.progress(task)+"% · "+Ui.status(task),12,false);
        MaterialButton more=new MaterialButton(requireContext());more.setText("⋮");more.setTextSize(23);more.setTextColor(Ui.MUTED);more.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));more.setContentDescription("Task options: "+task.getTitle());more.setOnClickListener(v->showTaskMenu(task));row.addView(more,new LinearLayout.LayoutParams(Ui.dp(requireContext(),50),Ui.dp(requireContext(),55)));card.setOnClickListener(v->taskDetail(task));}
    private String iconFor(Task t){if("Personal".equals(t.getType()))return "🛒";if("Work".equals(t.getType()))return "▣";if("Project".equals(t.getType()))return "▱";return "▤";}
    private int colorFor(Task t){if("Personal".equals(t.getType()))return 0xFFE9F8EE;if("Work".equals(t.getType()))return 0xFFFFEBDD;if("Project".equals(t.getType()))return 0xFFE4F0FF;if("Exam".equals(t.getType()))return 0xFFFFE5F0;return 0xFFE9DFFF;}
    private void showTaskMenu(Task task){String[] actions={"Not Started","In Progress","Completed","Edit Task","Delete Task"};new AlertDialog.Builder(requireContext()).setTitle(task.getTitle()).setItems(actions,(d,which)->{if(which<3)updateStatus(task,new String[]{"NOT_STARTED","IN_PROGRESS","COMPLETED"}[which]);else if(which==3)edit(task);else new AlertDialog.Builder(requireContext()).setTitle("Delete task?").setMessage("Focus history will be kept.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(x,w)->write(()->db.taskDao().delete(task),null)).show();}).show();}
    private void updateStatus(Task task,String status){write(()->db.runInTransaction(()->{Task current=db.taskDao().getTaskById(task.getTaskId());if(current!=null){String previous=current.getStatus();current.setStatus(status);if("IN_PROGRESS".equals(status)&&!"IN_PROGRESS".equals(previous)&&current.getProgressStartedAt()<=0)current.setProgressStartedAt(System.currentTimeMillis());if(!"IN_PROGRESS".equals(status))current.setProgressStartedAt(0);db.taskDao().update(current);}}),null);}
    private void edit(Task old){LinearLayout form=Ui.column(requireContext());EditText title=Ui.input(form,"Task name",old==null?"":old.getTitle(),InputType.TYPE_CLASS_TEXT);List<Course> choices=new ArrayList<>(courses);List<String> names=new ArrayList<>();names.add("No Course");int chosen=0;for(int i=0;i<choices.size();i++){names.add(choices.get(i).getCourseName());if(old!=null&&old.getCourseId()!=null&&old.getCourseId()==choices.get(i).getCourseId())chosen=i+1;}Spinner course=Ui.spinner(form,"Course (optional)",names,chosen);List<String> types=Arrays.asList("Personal","Work","Assignment","Project","Exam");Spinner type=Ui.spinner(form,"Task type",types,old==null?0:Math.max(0,types.indexOf(old.getType())));List<String> priorities=Arrays.asList("LOW","MEDIUM","HIGH");Spinner priority=Ui.spinner(form,"Priority",priorities,old==null?1:Math.max(0,priorities.indexOf(old.getPriority())));EditText duration=Ui.input(form,"Estimated duration (minutes)",old==null?"30":String.valueOf(old.getEstimatedMinutes()),InputType.TYPE_CLASS_NUMBER);CheckBox hasDeadline=new CheckBox(requireContext());hasDeadline.setText("Has deadline");hasDeadline.setChecked(old!=null&&old.getDeadline()>0);form.addView(hasDeadline);Calendar deadline=Calendar.getInstance();deadline.add(Calendar.DAY_OF_YEAR,1);if(old!=null&&old.getDeadline()>0)deadline.setTimeInMillis(old.getDeadline());MaterialButton date=Ui.button(form,Ui.date(deadline.getTimeInMillis()),()->new DatePickerDialog(requireContext(),(p,y,m,d)->{deadline.set(y,m,d);new TimePickerDialog(requireContext(),(tp,h,min)->{deadline.set(Calendar.HOUR_OF_DAY,h);deadline.set(Calendar.MINUTE,min);((TextView)form.findViewWithTag("deadline")).setText(Ui.date(deadline.getTimeInMillis()));},deadline.get(Calendar.HOUR_OF_DAY),deadline.get(Calendar.MINUTE),true).show();},deadline.get(Calendar.YEAR),deadline.get(Calendar.MONTH),deadline.get(Calendar.DAY_OF_MONTH)).show());date.setTag("deadline");date.setEnabled(hasDeadline.isChecked());hasDeadline.setOnCheckedChangeListener((button,checked)->date.setEnabled(checked));TextView error=Ui.text(form,"",13,false);error.setTextColor(0xFFB3261E);AlertDialog dialog=new AlertDialog.Builder(requireContext()).setTitle(old==null?"Add Task":"Edit Task").setView(Ui.form(form)).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{try{if(Ui.value(title).isEmpty())throw new IllegalArgumentException("Enter a task name.");int minutes=Integer.parseInt(Ui.value(duration));if(minutes<1)throw new IllegalArgumentException("Duration must be greater than 0.");int index=course.getSelectedItemPosition();Integer courseId=index==0?null:choices.get(index-1).getCourseId();Task edited=new Task(courseId,Ui.value(title),types.get(type.getSelectedItemPosition()),hasDeadline.isChecked()?deadline.getTimeInMillis():0,priorities.get(priority.getSelectedItemPosition()),minutes,old==null?0:old.getCompletedMinutes(),old==null?"NOT_STARTED":old.getStatus());if(old!=null){edited.setTaskId(old.getTaskId());edited.setProgressStartedAt(old.getProgressStartedAt());}write(()->db.runInTransaction(()->{if(old==null)db.taskDao().insert(edited);else{Task latest=db.taskDao().getTaskById(old.getTaskId());if(latest==null)throw new IllegalArgumentException("The task no longer exists.");edited.setCompletedMinutes(latest.getCompletedMinutes());edited.setStatus(latest.getStatus());edited.setProgressStartedAt(latest.getProgressStartedAt());db.taskDao().update(edited);}}),null);dialog.dismiss();}catch(Exception ex){error.setText(ex.getMessage());}}));dialog.show();}
    @Override public void onDestroyView(){progressHandler.removeCallbacks(progressTicker);super.onDestroyView();}
}
