package com.example.studyflow.scheduler;

import com.example.studyflow.data.entity.*;
import java.util.*;

/** Deterministic local planner. Analysis returns a preview and never writes to Room. */
public final class SmartScheduler {
    public static final int HORIZON_DAYS=14, DAILY_CAPACITY=240;
    public static final class Analysis {
        public final List<StudyPlan> plans=new ArrayList<>();
        public final List<Task> suggestions=new ArrayList<>();
        public final List<String> warnings=new ArrayList<>();
        public final Map<Integer,String> reasons=new HashMap<>();
        public final Map<Integer,Integer> unplannedMinutes=new LinkedHashMap<>();
        public int sessionMinutes=25;
    }
    public static double calculatePriorityScore(Task task){return score(task,System.currentTimeMillis());}
    private static double score(Task task,long now){
        int priority="HIGH".equals(task.getPriority())?3:"MEDIUM".equals(task.getPriority())?2:1;
        double urgency=task.getDeadline()<=0?0:10/Math.max(.5,(task.getDeadline()-now)/86400000.0);
        return priority*10+urgency+Math.max(0,task.getEstimatedMinutes()-task.getCompletedMinutes())/60.0;
    }
    public static List<StudyPlan> generatePlan(List<Task> tasks){return analyze(tasks,Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),System.currentTimeMillis()).plans;}
    public static Analysis analyze(List<Task> tasks,List<ClassSchedule> classes,List<FocusSession> history,List<StudyPlan> existing,long now){
        Analysis out=new Analysis();
        List<Integer> durations=new ArrayList<>();for(FocusSession f:history)if(f.getDurationMinutes()>0)durations.add(f.getDurationMinutes());
        if(!durations.isEmpty()){Collections.sort(durations);out.sessionMinutes=Math.max(25,Math.min(50,durations.get(durations.size()/2)));}
        else out.warnings.add("No Focus history yet: using 25-minute sessions.");
        if(classes.isEmpty())out.warnings.add("No class schedule yet: there are no class hours to exclude from the plan.");
        Map<Integer,Integer> remaining=new HashMap<>();long today=startOfDay(now);
        for(Task t:tasks)if(!"COMPLETED".equals(t.getStatus())){
            int work=Math.max(0,t.getEstimatedMinutes()-t.getCompletedMinutes());
            if(work==0){out.warnings.add(t.getTitle()+": no estimated time remains; check its status or duration.");continue;}
            out.suggestions.add(t);remaining.put(t.getTaskId(),work);
            String reason="Priority "+("HIGH".equals(t.getPriority())?"high":"LOW".equals(t.getPriority())?"low":"medium")+"; "+work+" minutes remaining";
            reason+=t.getDeadline()<=0?"; no deadline.":t.getDeadline()<now?"; overdue, choose a new deadline before planning.":"; about "+(int)Math.ceil((t.getDeadline()-now)/86400000.0)+" days until the deadline.";
            out.reasons.put(t.getTaskId(),reason);
            if(t.getDeadline()>0&&t.getDeadline()<now)out.warnings.add(t.getTitle()+": overdue; the deadline will not be moved automatically.");
        }
        out.suggestions.sort(Comparator.comparingDouble((Task t)->score(t,now)).reversed().thenComparingInt(Task::getTaskId));
        if(out.suggestions.isEmpty()){out.warnings.add("Add an incomplete task with an estimated duration to create a plan.");return out;}
        // Existing sessions with recorded progress are kept; do not schedule their remaining work twice.
        for(StudyPlan p:existing)if(p.getStudyDate()>=today&&p.getCompletedMinutes()>0&&remaining.containsKey(p.getTaskId()))
            remaining.put(p.getTaskId(),Math.max(0,remaining.get(p.getTaskId())-Math.max(0,p.getPlannedMinutes()-p.getCompletedMinutes())));
        Calendar day=Calendar.getInstance();day.setTimeInMillis(today);
        for(int offset=0;offset<HORIZON_DAYS;offset++,day.add(Calendar.DAY_OF_YEAR,1)){
            Calendar end=(Calendar)day.clone();end.add(Calendar.DAY_OF_YEAR,1);
            List<long[]> busy=new ArrayList<>();int capacity=DAILY_CAPACITY;
            for(ClassSchedule s:classes)if(matchesDate(s,day)){
                try{busy.add(new long[]{at(day,parse(s.getStartTime())),at(day,parse(s.getEndTime()))});}catch(Exception ignored){out.warnings.add("An invalid class time was found. Check your Schedule.");}
            }
            for(StudyPlan p:existing)if(p.getStudyDate()>=day.getTimeInMillis()&&p.getStudyDate()<end.getTimeInMillis()&&p.getCompletedMinutes()>0){
                busy.add(new long[]{p.getStudyDate(),p.getStudyDate()+p.getPlannedMinutes()*60000L});capacity-=p.getPlannedMinutes();
            }
            long cursor=Math.max(at(day,8*60),((now+299999L)/300000L)*300000L),dayEnd=at(day,22*60);
            while(cursor<dayEnd&&capacity>0){
                long freeEnd=dayEnd;boolean moved=false;
                for(long[] b:busy){if(b[0]<=cursor&&b[1]>cursor){cursor=b[1];moved=true;break;}if(b[0]>cursor)freeEnd=Math.min(freeEnd,b[0]);}
                if(moved)continue;
                Task chosen=null;int minutes=0;
                for(Task t:out.suggestions){int work=remaining.get(t.getTaskId());if(work<=0)continue;
                    long cutoff=t.getDeadline()>0?Math.min(freeEnd,t.getDeadline()):freeEnd;
                    int available=(int)((cutoff-cursor)/60000L);
                    if(available<=0)continue;
                    chosen=t;minutes=Math.min(Math.min(work,out.sessionMinutes),Math.min(capacity,available));break;
                }
                if(chosen==null){cursor=freeEnd;if(freeEnd==dayEnd)break;continue;}
                out.plans.add(new StudyPlan(chosen.getTaskId(),cursor,minutes,0,"PLANNED"));
                remaining.put(chosen.getTaskId(),remaining.get(chosen.getTaskId())-minutes);capacity-=minutes;
                cursor+=minutes*60000L+5*60000L;
            }
        }
        for(Task t:out.suggestions)if(remaining.get(t.getTaskId())>0)out.unplannedMinutes.put(t.getTaskId(),remaining.get(t.getTaskId()));
        if(!out.unplannedMinutes.isEmpty())out.warnings.add("Some tasks do not fit before their deadlines or within 14 days. Reduce the workload or adjust deadlines, then analyze again.");
        return out;
    }
    private static int parse(String s){String[]p=s.split(":");int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);if(h<0||h>23||m<0||m>59)throw new IllegalArgumentException();return h*60+m;}
    private static long at(Calendar day,int minute){Calendar c=(Calendar)day.clone();c.set(Calendar.HOUR_OF_DAY,minute/60);c.set(Calendar.MINUTE,minute%60);return c.getTimeInMillis();}
    public static long startOfDay(long time){Calendar c=Calendar.getInstance();c.setTimeInMillis(time);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}
    private static boolean matchesDate(ClassSchedule schedule,Calendar day){
        if(schedule.getDateMillis()>0L)return startOfDay(schedule.getDateMillis())==startOfDay(day.getTimeInMillis());
        return schedule.getDayOfWeek()==(day.get(Calendar.DAY_OF_WEEK)+5)%7+1;
    }
    /** Full input fingerprint, stable even if query ordering changes. */
    public static String fingerprint(List<Task> tasks,List<ClassSchedule> classes,List<FocusSession> history,List<StudyPlan> plans){
        List<String> rows=new ArrayList<>();
        for(Task t:tasks)rows.add("T|"+t.getTaskId()+"|"+t.getCourseId()+"|"+t.getTitle()+"|"+t.getDeadline()+"|"+t.getPriority()+"|"+t.getEstimatedMinutes()+"|"+t.getCompletedMinutes()+"|"+t.getStatus());
        for(ClassSchedule s:classes)rows.add("C|"+s.getScheduleId()+"|"+s.getDateMillis()+"|"+s.getDayOfWeek()+"|"+s.getStartTime()+"|"+s.getEndTime());
        for(FocusSession f:history)rows.add("F|"+f.getSessionId()+"|"+f.getStartTime()+"|"+f.getDurationMinutes());
        for(StudyPlan p:plans)rows.add("P|"+p.getPlanId()+"|"+p.getTaskId()+"|"+p.getStudyDate()+"|"+p.getPlannedMinutes()+"|"+p.getCompletedMinutes()+"|"+p.getStatus());
        Collections.sort(rows);return rows.toString();
    }
}
