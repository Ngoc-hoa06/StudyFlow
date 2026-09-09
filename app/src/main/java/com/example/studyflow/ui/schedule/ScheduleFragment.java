package com.example.studyflow.ui.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.notification.ClassReminderScheduler;
import com.example.studyflow.ui.common.*;
import java.util.*;

public class ScheduleFragment extends DataFragment {
    private long selectedDate=Ui.startOfDay(System.currentTimeMillis());
    @Override protected void render(){
        Ui.heading(body,"Schedule",R.drawable.ic_sf_schedule);
        Ui.text(body,"Plan one-time class sessions by date",14,false);
        LinearLayout actions=Ui.row(requireContext());body.addView(actions);
        Ui.weight(Ui.button(actions,"+ Course",()->editCourse(null)));
        Ui.weight(Ui.button(actions,"+ Class session",()->editSchedule(null)));
        LinearLayout calendar=Ui.card(body);
        com.google.android.material.button.MaterialButton dateButton=Ui.button(calendar,"Date: "+formatDate(selectedDate),this::pickDate);
        dateButton.setContentDescription("Choose class date");
        List<ClassSchedule> list=new ArrayList<>();
        for(ClassSchedule s:schedules)
            if(s.getDateMillis()>0L&&Ui.startOfDay(s.getDateMillis())==selectedDate)list.add(s);
        list.sort(Comparator.comparing(ClassSchedule::getStartTime));
        if(list.isEmpty())Ui.text(calendar,"No classes scheduled for this date.",15,false);
        for(ClassSchedule s:list){
            Course c=course(s.getCourseId());if(c==null)continue;
            Ui.text(calendar,s.getStartTime()+" – "+s.getEndTime(),13,true);
            LinearLayout block=Ui.column(requireContext());int p=Ui.dp(requireContext(),12);block.setPadding(p,p,p,p);block.setBackground(Ui.background(requireContext(),Ui.color(c),12));calendar.addView(block,new LinearLayout.LayoutParams(-1,-2));
            Ui.text(block,c.getCourseName(),18,true);Ui.text(block,"Teacher: "+c.getLecturer()+"\nRoom: "+Ui.room(c,s),14,false);
            LinearLayout row=Ui.row(requireContext());block.addView(row);
            Ui.weight(Ui.button(row,"Edit session",()->editSchedule(s)));
            Ui.weight(Ui.button(row,"Delete session",()->new AlertDialog.Builder(requireContext()).setTitle("Delete class session?").setMessage(c.getCourseName()+" · "+s.getStartTime()).setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                android.content.Context ctx=requireContext().getApplicationContext();
                write(()->{db.classScheduleDao().delete(s);ClassReminderScheduler.cancel(ctx,s);},null);
            }).show()));
        }
        Ui.heading(body,"Courses",R.drawable.ic_sf_schedule);
        if(courses.isEmpty())Ui.text(body,"Create a Course first, then add its class sessions.",14,false);
        for(Course c:courses){
            LinearLayout card=Ui.card(body);card.setBackground(Ui.background(requireContext(),Ui.color(c),18));
            Ui.text(card,"Course: "+c.getCourseName(),18,true);
            Ui.text(card,"Teacher: "+c.getLecturer()+"\nDefault room: "+c.getDefaultRoom(),14,false);
            LinearLayout row=Ui.row(requireContext());card.addView(row);
            Ui.weight(Ui.button(row,"Edit Course",()->editCourse(c)));
            Ui.weight(Ui.button(row,"Delete Course",()->new AlertDialog.Builder(requireContext()).setTitle("Delete Course?")
                    .setMessage("Class sessions for this Course will be deleted. Tasks and Focus history will be kept.")
                    .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                        android.content.Context ctx=requireContext().getApplicationContext();
                        write(()->{List<ClassSchedule> old=db.classScheduleDao().getSnapshot();db.courseDao().delete(c);for(ClassSchedule s:old)if(s.getCourseId()==c.getCourseId())ClassReminderScheduler.cancel(ctx,s);},null);
                    }).show()));
        }
    }
    private void editCourse(Course old){
        LinearLayout form=Ui.column(requireContext());
        EditText name=Ui.input(form,"Course name",old==null?"":old.getCourseName(),InputType.TYPE_CLASS_TEXT);
        EditText lecturer=Ui.input(form,"Teacher",old==null?"":old.getLecturer(),InputType.TYPE_CLASS_TEXT);
        EditText room=Ui.input(form,"Default room",old==null?"":old.getDefaultRoom(),InputType.TYPE_CLASS_TEXT);
        List<String> colors=new ArrayList<>(Arrays.asList(Ui.COLORS));List<String> names=new ArrayList<>(Arrays.asList(Ui.COLOR_NAMES));
        if(old!=null && !colors.contains(old.getColor())){colors.add(old.getColor());names.add("Current color");}
        Spinner color=Ui.spinner(form,"Course color",names,old==null?0:colors.indexOf(old.getColor()));
        TextView sample=Ui.text(form,"Calendar color preview",14,true);
        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,android.view.View v,int pos,long id){Course c=new Course("","","",colors.get(pos));sample.setBackground(Ui.background(requireContext(),Ui.color(c),10));}});
        AlertDialog dialog=new AlertDialog.Builder(requireContext()).setTitle(old==null?"Create Course":"Edit Course").setView(Ui.form(form)).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            if(Ui.value(name).isEmpty()){name.setError("Enter a Course name");return;}
            Course c=new Course(Ui.value(name),Ui.value(lecturer),Ui.value(room),colors.get(color.getSelectedItemPosition()));
            if(old!=null)c.setCourseId(old.getCourseId());android.content.Context ctx=requireContext().getApplicationContext();
            dialog.getButton(-1).setEnabled(false);
            write(()->{if(old==null)db.courseDao().insert(c);else db.courseDao().update(c);
                for(ClassSchedule s:db.classScheduleDao().getSnapshot())if(s.getCourseId()==c.getCourseId())ClassReminderScheduler.scheduleReminder(ctx,c,s);
            },null);dialog.dismiss();
        }));dialog.show();
    }
    private void editSchedule(ClassSchedule old){
        if(courses.isEmpty()){new AlertDialog.Builder(requireContext()).setMessage("Create a Course first.").setPositiveButton("Create Course",(d,w)->editCourse(null)).setNegativeButton("Close",null).show();return;}
        List<Course> options=new ArrayList<>(courses);List<String> names=new ArrayList<>();int selected=0;
        for(int i=0;i<options.size();i++){names.add(options.get(i).getCourseName());if(old!=null&&options.get(i).getCourseId()==old.getCourseId())selected=i;}
        LinearLayout form=Ui.column(requireContext());Spinner coursePicker=Ui.spinner(form,"Course",names,selected);
        Calendar classDate=Calendar.getInstance();
        classDate.setTimeInMillis(old!=null&&old.getDateMillis()>0L?old.getDateMillis():old==null?selectedDate:nextDateForDay(old.getDayOfWeek()));
        EditText date=Ui.input(form,"Class date (dd/MM/yyyy)",formatDate(classDate.getTimeInMillis()),InputType.TYPE_CLASS_DATETIME);
        date.setFocusable(false);date.setOnClickListener(v->new DatePickerDialog(requireContext(),(picker,year,month,day)->{
            classDate.set(Calendar.YEAR,year);classDate.set(Calendar.MONTH,month);classDate.set(Calendar.DAY_OF_MONTH,day);
            classDate.set(Calendar.HOUR_OF_DAY,0);classDate.set(Calendar.MINUTE,0);classDate.set(Calendar.SECOND,0);classDate.set(Calendar.MILLISECOND,0);
            date.setText(formatDate(classDate.getTimeInMillis()));
        },classDate.get(Calendar.YEAR),classDate.get(Calendar.MONTH),classDate.get(Calendar.DAY_OF_MONTH)).show());
        EditText start=Ui.input(form,"Start time (HH:mm)",old==null?"08:00":old.getStartTime(),InputType.TYPE_CLASS_DATETIME);
        EditText end=Ui.input(form,"End time (HH:mm)",old==null?"10:00":old.getEndTime(),InputType.TYPE_CLASS_DATETIME);
        timePicker(start);timePicker(end);
        CheckBox override=new CheckBox(requireContext());override.setText("Use a different room for this session");override.setChecked(old!=null&&old.getRoom()!=null&&!old.getRoom().trim().isEmpty());form.addView(override);
        TextView defaultRoom=Ui.text(form,"",14,false);
        EditText room=Ui.input(form,"Override room (only if different)",old==null?"":old.getRoom(),InputType.TYPE_CLASS_TEXT);
        room.setEnabled(override.isChecked());override.setOnCheckedChangeListener((b,on)->room.setEnabled(on));
        coursePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,android.view.View v,int pos,long id){defaultRoom.setText("Default room: "+options.get(pos).getDefaultRoom());}});
        TextView error=Ui.text(form,"",13,false);error.setTextColor(0xFFB3261E);
        AlertDialog dialog=new AlertDialog.Builder(requireContext()).setTitle(old==null?"Add class session":"Edit Schedule").setView(Ui.form(form)).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{
            try{
                int from=Ui.minutes(Ui.value(start)),to=Ui.minutes(Ui.value(end));if(from>=to)throw new IllegalArgumentException("Start time must be earlier than end time.");
                if(override.isChecked()&&Ui.value(room).isEmpty())throw new IllegalArgumentException("Enter an override room or disable the option.");
                Course c=options.get(coursePicker.getSelectedItemPosition());
                classDate.set(Calendar.HOUR_OF_DAY,0);classDate.set(Calendar.MINUTE,0);classDate.set(Calendar.SECOND,0);classDate.set(Calendar.MILLISECOND,0);
                long dateMillis=classDate.getTimeInMillis();
                ClassSchedule s=new ClassSchedule(c.getCourseId(),dateMillis,Ui.time(from),Ui.time(to),override.isChecked()?Ui.value(room):"",30);
                if(old!=null)s.setScheduleId(old.getScheduleId());android.content.Context ctx=requireContext().getApplicationContext();
                dialog.getButton(-1).setEnabled(false);
                write(()->{
                    db.runInTransaction(()->{
                        for(ClassSchedule other:db.classScheduleDao().getSnapshot())
                            if(other.getScheduleId()!=s.getScheduleId()&&other.getDateMillis()>0L&&Ui.startOfDay(other.getDateMillis())==dateMillis&&from<Ui.minutes(other.getEndTime())&&to>Ui.minutes(other.getStartTime()))
                                throw new IllegalArgumentException("This overlaps with "+other.getStartTime()+" – "+other.getEndTime()+". Choose another time.");
                        if(old==null)s.setScheduleId((int)db.classScheduleDao().insert(s));else db.classScheduleDao().update(s);
                    });
                    if(old!=null)ClassReminderScheduler.cancel(ctx,old);ClassReminderScheduler.scheduleReminder(ctx,c,s);
                },null);selectedDate=dateMillis;dialog.dismiss();
            }catch(Exception ex){error.setText(ex.getMessage());}
        }));dialog.show();
    }
    private void pickDate(){
        Calendar initial=Calendar.getInstance();initial.setTimeInMillis(selectedDate);
        new DatePickerDialog(requireContext(),(picker,year,month,day)->{
            Calendar chosen=Calendar.getInstance();chosen.set(year,month,day,0,0,0);chosen.set(Calendar.MILLISECOND,0);
            selectedDate=chosen.getTimeInMillis();refresh();
        },initial.get(Calendar.YEAR),initial.get(Calendar.MONTH),initial.get(Calendar.DAY_OF_MONTH)).show();
    }

    private long nextDateForDay(int dayOfWeek){
        Calendar target=Calendar.getInstance();
        int current=Ui.day(target);
        target.add(Calendar.DAY_OF_YEAR,(dayOfWeek-current+7)%7);
        target.set(Calendar.HOUR_OF_DAY,0);target.set(Calendar.MINUTE,0);target.set(Calendar.SECOND,0);target.set(Calendar.MILLISECOND,0);
        return target.getTimeInMillis();
    }

    private String formatDate(long millis){return new java.text.SimpleDateFormat("dd/MM/yyyy",Locale.US).format(new Date(millis));}
    private void timePicker(EditText field){field.setFocusable(false);field.setOnClickListener(v->{int m;try{m=Ui.minutes(Ui.value(field));}catch(Exception e){m=480;}new TimePickerDialog(requireContext(),(p,h,min)->field.setText(Ui.time(h*60+min)),m/60,m%60,true).show();});}
}
