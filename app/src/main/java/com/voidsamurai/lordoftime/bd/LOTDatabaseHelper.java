package com.voidsamurai.lordoftime.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import kotlin.Triple;


public class LOTDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LOT";
    private static final int DB_VERSION = 18;
    private static SQLiteDatabase db;
    private static List<String> guide;

    public LOTDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    public static void SetGuide(List<String> guide) {
        LOTDatabaseHelper.guide =guide;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db,0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db,oldVersion);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        LOTDatabaseHelper.db =db;
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion){
        LOTDatabaseHelper.db =db;
        if (oldVersion > 0) {

            db.execSQL("DROP TABLE IF EXISTS TASKTABLE;");
            db.execSQL("DROP TABLE IF EXISTS RUTINES;");
            db.execSQL("DROP TABLE IF EXISTS OLDSTATS;");
            db.execSQL("DROP TABLE IF EXISTS COLOR;");
            //    db.execSQL("DROP TABLE IF EXISTS AVATARS;");

        }
        db.execSQL("CREATE TABLE IF NOT EXISTS TASKTABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT,name TEXT, datetime INTEGER, working_time INTEGER,priority INTEGER, current_work_time INTEGER, is_finished INTEGER,user_id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS AVATARS (user_id TEXT PRIMARY KEY, avatar BLOB);");
        db.execSQL("CREATE TABLE IF NOT EXISTS RUTINES (_id INTEGER PRIMARY KEY AUTOINCREMENT,task_id INTEGER, days TEXT,hours TEXT,user_id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS COLOR (category_id TEXT PRIMARY KEY , color TEXT,user_id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS OLDSTATS (date_id INTEGER PRIMARY KEY , working_time INTEGER, category TEXT,user_id TEXT);");

      //  if(DB_VERSION==1) {

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if(guide!=null){
            int i=guide.size();
            cal.add(Calendar.HOUR,-(i+1));
            for (String x: guide){
                cal.add(Calendar.HOUR,1);
                fillTestData("Tutorial", x, cal.getTime().getTime(), 0, i--, (int) (0.5 * 3600));
            }
            addColorRow("Tutorial", "#2266BB", "");
        }
          /*  cal.set(2021, 11, 1, 12, 6, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fillTestData("Książki", "Ludzie bezdomni", cal.getTime().getTime(), (int) (2.3 * 3600), 3, (int) (1.2 * 3600));
            cal.set(2021, 11, 1, 15, 6, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fillTestData("Sport", "Sztanga", cal.getTime().getTime(), (int) (6.6 * 3600), 1, (int) (2.6 * 3600));
            cal.set(2021, 11, 2, 1, 6, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fillTestData("Sport", "Bieganie", cal.getTime().getTime(), (int) (1.8 * 3600), 1, 3600);
            cal.set(2022, 4, 11, 14, 16, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fillTestData("Praca w ogrodzie", "Sadzenie cebuli", cal.getTime().getTime(), (int) (2.4 * 3600), 2, (int) (2.0 * 3600));
            cal.set(2022, 6, 30, 10, 2, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fillTestData("Praca w ogrodzie", "Podlewanie kwiatów", cal.getTime().getTime(), (int) (0.4 * 3600), 2, 0);
*/
            //addColorRow("Praca w ogrodzie", "#FFAA56", "");
            //addColorRow("Książki", "#AAFF96", "");
            addColorRow("Sport", "#2266BB", "");

       // }

    }

    public void addAvatar(String user_id,byte [] avatar){
        Cursor c=db.rawQuery("SELECT * FROM AVATARS  WHERE AVATARS.user_id=?",new String[]{String.valueOf(user_id)});
        ContentValues cv= new ContentValues();
        cv.put("user_id",user_id);
        cv.put("avatar",avatar);
        if (c.moveToFirst()){
            db.update("AVATARS"
                    ,cv
                    ,"user_id = ?"
                    ,new String[]{String.valueOf(user_id)});

        }else{
            db.insert("AVATARS",null,cv);

        }
        c.close();
        cv.clear();
    }
    public Optional<Bitmap> getAvatar(String user_id){
        Optional<Bitmap> bit=Optional.empty();

        Cursor c=db.rawQuery("SELECT * FROM AVATARS  WHERE AVATARS.user_id=?",new String[]{user_id});
        if (c.moveToFirst()){
            byte[] b=c.getBlob(1);
            if(b!=null&&b.length!=0) {
                bit = Optional.of(BitmapFactory.decodeByteArray(b, 0, b.length));
            }
        }
        c.close();
        return bit;
    }

    private void fillTestData(String category, String name, Long startdatetime, int currentTimeInS, int priority, int workingTime){
        addTaskRow(category,name,startdatetime,currentTimeInS,priority,workingTime,"");
        addOldstatRow(startdatetime,currentTimeInS,category,"");
    }

    /**
     *
     * @param date set only Y,M,D rest need to be 0, also milliseconds
     * @param duration float value as String
     */
    public void addOldstatRow(Long date,int duration,String category,String userId){
        ContentValues cv = createOldstatValues(date,duration,category,userId);
        db.insert("OLDSTATS", null, cv);
        cv.clear();
    }
    /**
     * to not change specified values set null or 0, depends on type
     */
    public void editOldstatRow(Long date,Long newDate,int duration,String category,String userId){
        ContentValues cv = createOldstatValues(newDate,duration,category,"");
        db.update("OLDSTATS", cv,"date_id=? AND (user_id=? OR TRIM(user_id) IS NULL)",new String[]{String.valueOf(date),userId});
        cv.clear();
    }


    /**
     *
     * @param date set only Y,M,D rest need to be 0, also milliseconds
     */
    public int deleteOldstatRow(Long date){
        return db.delete("OLDSTATS"
                ,"date_id = ?"
                ,new String[]{String.valueOf(date)});
    }


    /**
     * @param days-string with names of week MON,THU,WED,THU,FRI,SAT,SUN separated by , without spaces
     *
     * */

    public long addRutinesRow(int task_id, String days, String hours, String userId){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createRutinesValues(task_id,days,hours,userId);
        return db.insert("RUTINES", null, cv);

    }
    /**
     * @param days-string with names of week MON,THU,WED,THU,FRI,SAT,SUN separated by , without spaces
     *
     * */
    public long editRutinesRow(int id,int task_id, String days, String hours, String user_id){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createRutinesValues(task_id,days,hours,"");
        return db.update("RUTINES"
                ,cv
                ,"_id = ? AND (user_id=? OR TRIM(user_id) IS NULL)"
                ,new String[]{String.valueOf(id),user_id});
    }

    /**
     * @param task_id - id of task [not rutine]
     * */
    public ArrayList<RutinesRow> getRutinesArray(int task_id){
        Cursor c=db.rawQuery("SELECT * FROM RUTINES  WHERE RUTINES.task_id=?",new String[]{String.valueOf(task_id)});
        ArrayList<RutinesRow> array= new ArrayList<>();
        if(c.moveToFirst())
            do{
                array.add(new RutinesRow(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3)));
            }while(c.moveToNext());
        c.close();
        return  array;
    }
    public Map<Integer,RutinesRow> getUserRutinesArray(String user_id){
        Cursor c=db.rawQuery("SELECT * FROM RUTINES WHERE RUTINES.user_id=? OR TRIM(RUTINES.user_id) IS NULL ",new String[]{user_id});
        Map<Integer,RutinesRow> array= new HashMap<>();
        if(c.moveToFirst())
            do{
                array.put(c.getInt(0),new RutinesRow(c.getInt(0),c.getInt(1),c.getString(2),c.getString(3)));
            }while(c.moveToNext());
        c.close();
        return  array;
    }

    public int deleteRutinesRow(int id){
        return db.delete("RUTINES","_id = ?", new String[]{String.valueOf(id)});
    }
    /**
     * Return List<Integer> of removed id's
     * */
    public List<Integer> deleteRutinesRowAssignedToTask(int task_id){
        List<Integer> list= new ArrayList<>();
        Cursor c=db.rawQuery("SELECT RUTINES._id FROM RUTINES WHERE RUTINES.task_id=?",new String[]{String.valueOf(task_id)});
        if(c.moveToFirst())
            do{
                list.add(c.getInt(0));
            }while(c.moveToNext());
        int licz=db.delete("RUTINES","task_id = ?", new String[]{String.valueOf(task_id)});
        Log.v("DELETED",""+licz+" rows   "+list);
        c.close();
        return list;
    }

    public long addTaskRow(String category, String name, Long startdatetime, int hours, int priority,int workingTime,String userId){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createTasktableValues(category,name,startdatetime,hours,priority,workingTime,userId);
        return db.insert("TASKTABLE", null, cv);

    }
    public long addTaskRow(DataRowWithColor data,String userId){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createTasktableValues(data.getCategory(),data.getName(),data.getDate().getTime().getTime(),(int)data.getWorkingTime(),data.getPriority(),(int)data.getCurrentWorkingTime(),userId);
        return  db.insert("TASKTABLE", null, cv);

    }
    /**
     * to not change specified values set null or 0, depends on type and -1 to is finishted
     * @param workingTime [-1 set time to 0]
     */
    public int editTaskRow(int oldId, String category, String name, Long startdatetime, int hours, int priority,int workingTime, int isFinished ){        // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createEditTasktableValues(category,name,startdatetime,hours,priority,workingTime,isFinished,"");
        return db.update("TASKTABLE"
                ,cv
                ,"_id = ?"
                ,new String[]{String.valueOf(oldId)});
    }
    public ArrayList<Triple<Integer,String,Long>> getServiceTaskInfo(String userId){
        ArrayList<Triple<Integer,String,Long>> array = new ArrayList<>();
        Calendar todayMorning=Calendar.getInstance();
        Calendar todayNight=Calendar.getInstance();
        todayMorning.set(Calendar.HOUR_OF_DAY,0);
        todayMorning.set(Calendar.MINUTE,0);
        todayMorning.set(Calendar.SECOND,0);
        todayMorning.set(Calendar.MILLISECOND,0);
        todayNight.set(Calendar.HOUR_OF_DAY,24);
        todayNight.set(Calendar.MINUTE,0);
        todayNight.set(Calendar.SECOND,0);
        todayNight.set(Calendar.MILLISECOND,0);


        Cursor c=db.rawQuery("SELECT TASKTABLE._id, TASKTABLE.name, TASKTABLE.datetime  FROM TASKTABLE  WHERE (TASKTABLE.datetime BETWEEN ? AND ? )AND (TASKTABLE.user_id=? OR TRIM(TASKTABLE.user_id) IS NULL)",new String[]{String.valueOf(todayMorning.getTimeInMillis()),String.valueOf(todayNight.getTimeInMillis()),userId});

        if(c.moveToFirst())
            do{
                array.add(new Triple<Integer,String,Long>(c.getInt(0),c.getString(1),c.getLong(2)));
            }while (c.moveToNext());

        c.close();
        return array;
    }

    public DataRowWithColor getTaskRow(int id,String userId){
        Cursor c=db.rawQuery("SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime, TASKTABLE.working_time, TASKTABLE.priority, TASKTABLE.current_work_time,TASKTABLE.is_finished, COLOR.color  FROM TASKTABLE JOIN COLOR ON TASKTABLE.category=COLOR.category_id WHERE TASKTABLE._id=? AND (TASKTABLE.user_id=? OR TRIM(TASKTABLE.user_id) IS NULL)",new String[]{String.valueOf(id),userId});
        if(c.moveToFirst()){
            Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar now=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date(c.getLong(3)));
            try {
                return new DataRowWithColor(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        cal,
                        ((float) c.getInt(4)) / 3600,
                        c.getInt(5),
                        ((float) c.getInt(6)) / 3600,
                        c.getString(8),
                        cal.getTime().getTime() < now.getTime().getTime(),
                        c.getInt(7));
            }finally {
                c.close();
            }
        }else
            return null;
    }
    public ArrayList<DataRowWithColor> getTodayTasks(Long data,String userId){
        Calendar start=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar end=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.setTime(new Date(data));
        end.setTime(new Date(data));
        start.set(Calendar.HOUR_OF_DAY,0);
        start.set(Calendar.MINUTE,0);
        start.set(Calendar.SECOND,0);
        start.set(Calendar.MILLISECOND,0);
        end.set(Calendar.HOUR_OF_DAY,24);
        end.set(Calendar.MINUTE,0);
        end.set(Calendar.SECOND,0);
        end.set(Calendar.MILLISECOND,0);
        long startTime=start.getTime().getTime();
        long endTime=end.getTime().getTime();


        Cursor c=db.rawQuery("SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime, TASKTABLE.working_time, TASKTABLE.priority, TASKTABLE.current_work_time, COLOR.color , TASKTABLE.is_finished " +
                "FROM TASKTABLE JOIN COLOR ON TASKTABLE.category=COLOR.category_id WHERE TASKTABLE.datetime BETWEEN "+startTime+" AND "+endTime+" AND (TASKTABLE.user_id=? OR TRIM(TASKTABLE.user_id) IS NULL) ",new String[]{userId});
        if(c.moveToFirst()){

            ArrayList<DataRowWithColor> array = new ArrayList<>();
            do {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTime(new Date(c.getLong(3)));
                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                array.add( new DataRowWithColor(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        cal,
                        ((float) c.getInt(4)) / 3600,
                        c.getInt(5),
                        ((float) c.getInt(6)) / 3600,
                        c.getString(7),
                        cal.getTime().getTime() < now.getTime().getTime(),
                        c.getInt(8)));

            }while(c.moveToNext());
            c.close();
            return array;
        }else
            return null;
    }

    public int deleteTaskRow(int id){
        return db.delete("TASKTABLE","_id = ?", new String[]{String.valueOf(id)});
    }
    /**
     * @param user_id empty - nothing
     * */
    @NotNull
    private  ContentValues createTasktableValues(String category, String name, Long starttime, int hours, int priority,int currentWorkingTime,String user_id){
        ContentValues cv = new ContentValues();
        cv.put("category",category);
        cv.put("name",name);
        cv.put("datetime",starttime);
        cv.put("working_time",hours);
        cv.put("priority",priority);
        cv.put("current_work_time",currentWorkingTime);
        if(!user_id.equals(""))cv.put("user_id",user_id);
        return cv;
    }
    /**
     * 0 or null mean no changes
     * @param isFinished 0 if false, 1 if true, -1 if not change
     * @param currentWorkingTime [-1 sets time to 0]
     * @param user_id empty - nothing
     *
     * */
    @NotNull
    private  ContentValues createEditTasktableValues(String category, String name, Long starttime, int hours, int priority,int currentWorkingTime,int isFinished,String user_id){
        ContentValues cv = new ContentValues();
        if(category!=null)cv.put("category",category);
        if(name!=null)cv.put("name",name);
        if(starttime!=null)cv.put("datetime",starttime);
        if(hours!=0)cv.put("working_time",hours);
        if(!user_id.equals(""))cv.put("user_id",user_id);
        if(priority!=0)cv.put("priority",priority);
        if(currentWorkingTime!=0){
            if(currentWorkingTime==-1)
                cv.put("current_work_time",0);
            else
                cv.put("current_work_time",currentWorkingTime);
        }
        if(isFinished==0||isFinished==1)cv.put("is_finished",isFinished);
        return cv;
    }
    /**
     * @param user_id -1 nothing
     * */
    private  ContentValues createOldstatValues( Long starttime, int hours,String category,String user_id){
        ContentValues cv = new ContentValues();
        if(starttime!=null)cv.put("date_id",starttime);
        if(hours!=0)cv.put("working_time",hours);
        if(!user_id.equals(""))cv.put("user_id",user_id);
        if(category!=null)cv.put("category",category);
        return cv;
    }
    /**
     * to not change value set -1 for numbers or null for strings
     * */
    private  ContentValues createRutinesValues(int task_id,String  days,String hour, String user_id){
        ContentValues cv = new ContentValues();
        if(task_id!=-1)cv.put("task_id",task_id);
        if(days!=null)cv.put("days",days);
        if(hour!=null)cv.put("hours",hour);
        if(!user_id.equals(""))cv.put("user_id",user_id);
        return cv;
    }

    public  void addColorRow(String category, String color,String user_id) {
        db.insert("COLOR", null, createColorCValues(category,color,user_id));
    }

    public  void editColorRow(String oldCategory,String newCategory, String newColor,String user_id) {
        db.update("COLOR",createColorCValues(newCategory,newColor,""),"category_id = ? AND (user_id=? OR TRIM(user_id) IS NULL)", new String[]{oldCategory,user_id});
    }
    /**
     * @return -1 if category is used
     *  0 if no row deleted
     *  else return number of deleted rows
     */

    public  int deleteColorRow(String oldCategory,String user_id) {

        Cursor c=db.rawQuery("SELECT  TASKTABLE.category FROM TASKTABLE WHERE  (TASKTABLE.user_id=? OR TRIM(TASKTABLE.user_id) IS NULL) ",new String[]{user_id});
        Boolean found=false;
        if(c.moveToFirst())
            do{
                if(c.getString(0).equals(oldCategory)){
                    found=true;
                    break;
                }
            }while(c.moveToNext());
        if(!found)
            return db.delete("COLOR","category_id = ? AND (user_id=? OR TRIM(user_id) IS NULL)", new String[]{oldCategory,user_id});
        return -1;
    }

    @NotNull
    private  ContentValues createColorCValues(String category, String color,String user_id){
        ContentValues cv = new ContentValues();
        if(category!=null)cv.put("category_id", category);
        if(color!=null)cv.put("color", color);
        if(!user_id.equals(""))cv.put("user_id", user_id);
        return cv;
    }
}
