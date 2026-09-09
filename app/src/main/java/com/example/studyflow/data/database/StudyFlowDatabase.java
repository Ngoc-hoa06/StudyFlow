package com.example.studyflow.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studyflow.data.dao.ClassScheduleDao;
import com.example.studyflow.data.dao.CourseDao;
import com.example.studyflow.data.dao.FocusSessionDao;
import com.example.studyflow.data.dao.StudyPlanDao;
import com.example.studyflow.data.dao.TaskDao;
import com.example.studyflow.data.entity.ClassSchedule;
import com.example.studyflow.data.entity.Course;
import com.example.studyflow.data.entity.FocusSession;
import com.example.studyflow.data.entity.StudyPlan;
import com.example.studyflow.data.entity.Task;

@Database(
        entities = {
                Course.class,
                ClassSchedule.class,
                Task.class,
                StudyPlan.class,
                FocusSession.class
        },
        version = 4,
        exportSchema = false
)
public abstract class StudyFlowDatabase extends RoomDatabase {

    public static final java.util.concurrent.ExecutorService IO = java.util.concurrent.Executors.newSingleThreadExecutor();
    public static final androidx.room.migration.Migration MIGRATION_1_2 =
            new androidx.room.migration.Migration(1, 2) {
                @Override public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("CREATE TEMP TABLE plan_backup AS SELECT * FROM study_plans");
                    db.execSQL("CREATE TEMP TABLE focus_backup AS SELECT * FROM focus_sessions");
                    db.execSQL("CREATE TEMP TABLE task_backup AS SELECT * FROM tasks");
                    db.execSQL("DROP TABLE focus_sessions");
                    db.execSQL("DROP TABLE study_plans");
                    db.execSQL("DROP TABLE tasks");
                    db.execSQL("CREATE TABLE tasks (taskId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, courseId INTEGER, title TEXT, type TEXT, deadline INTEGER NOT NULL, priority TEXT, estimatedMinutes INTEGER NOT NULL, completedMinutes INTEGER NOT NULL, status TEXT, FOREIGN KEY(courseId) REFERENCES courses(courseId) ON UPDATE NO ACTION ON DELETE SET NULL)");
                    db.execSQL("INSERT INTO tasks SELECT * FROM task_backup");
                    db.execSQL("CREATE INDEX index_tasks_courseId ON tasks(courseId)");
                    db.execSQL("CREATE TABLE study_plans (planId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, studyDate INTEGER NOT NULL, plannedMinutes INTEGER NOT NULL, completedMinutes INTEGER NOT NULL, status TEXT, FOREIGN KEY(taskId) REFERENCES tasks(taskId) ON UPDATE NO ACTION ON DELETE CASCADE)");
                    db.execSQL("INSERT INTO study_plans SELECT * FROM plan_backup");
                    db.execSQL("CREATE INDEX index_study_plans_taskId ON study_plans(taskId)");
                    db.execSQL("CREATE TABLE focus_sessions (sessionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, courseId INTEGER, taskId INTEGER, startTime INTEGER NOT NULL, durationMinutes INTEGER NOT NULL, sessionKey TEXT, FOREIGN KEY(courseId) REFERENCES courses(courseId) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(taskId) REFERENCES tasks(taskId) ON UPDATE NO ACTION ON DELETE SET NULL)");
                    db.execSQL("INSERT INTO focus_sessions(sessionId,courseId,taskId,startTime,durationMinutes) SELECT sessionId,courseId,taskId,startTime,durationMinutes FROM focus_backup");
                    db.execSQL("CREATE INDEX index_focus_sessions_courseId ON focus_sessions(courseId)");
                    db.execSQL("CREATE INDEX index_focus_sessions_taskId ON focus_sessions(taskId)");
                    db.execSQL("CREATE UNIQUE INDEX index_focus_sessions_sessionKey ON focus_sessions(sessionKey)");
                    db.execSQL("DROP TABLE task_backup");
                    db.execSQL("DROP TABLE plan_backup");
                    db.execSQL("DROP TABLE focus_backup");
                }
            };

    public static final androidx.room.migration.Migration MIGRATION_2_3 =
            new androidx.room.migration.Migration(2, 3) {
                @Override public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN progressStartedAt INTEGER NOT NULL DEFAULT 0");
                }
            };

    public static final androidx.room.migration.Migration MIGRATION_3_4 =
            new androidx.room.migration.Migration(3, 4) {
                @Override public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("ALTER TABLE class_schedules ADD COLUMN dateMillis INTEGER NOT NULL DEFAULT 0");
                }
            };

    private static volatile StudyFlowDatabase INSTANCE;

    public abstract CourseDao courseDao();

    public abstract ClassScheduleDao classScheduleDao();

    public abstract TaskDao taskDao();

    public abstract StudyPlanDao studyPlanDao();

    public abstract FocusSessionDao focusSessionDao();

    public static StudyFlowDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {

            synchronized (StudyFlowDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            StudyFlowDatabase.class,
                            "studyflow_database"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build();
                    normalizeLegacySchedules(INSTANCE);
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Older releases stored only a weekday, which made a class appear every week.
     * Give those rows one concrete upcoming date so they become one-time sessions.
     */
    private static void normalizeLegacySchedules(final StudyFlowDatabase database) {
        IO.execute(() -> {
            java.util.Calendar today = java.util.Calendar.getInstance();
            int currentDay = (today.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
            for (com.example.studyflow.data.entity.ClassSchedule schedule : database.classScheduleDao().getSnapshot()) {
                if (schedule.getDateMillis() != 0L) continue;
                java.util.Calendar target = (java.util.Calendar) today.clone();
                int delta = (schedule.getDayOfWeek() - currentDay + 7) % 7;
                target.add(java.util.Calendar.DAY_OF_YEAR, delta);
                target.set(java.util.Calendar.HOUR_OF_DAY, 0);
                target.set(java.util.Calendar.MINUTE, 0);
                target.set(java.util.Calendar.SECOND, 0);
                target.set(java.util.Calendar.MILLISECOND, 0);
                schedule.setDateMillis(target.getTimeInMillis());
                database.classScheduleDao().update(schedule);
            }
        });
    }
}
