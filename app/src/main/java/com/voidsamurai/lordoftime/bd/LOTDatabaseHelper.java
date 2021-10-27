package com.voidsamurai.lordoftime.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class LOTDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LOT";

    private static final int DB_VERSION = 10;
    private static SQLiteDatabase db;

    public LOTDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.v("create","db");
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
            db.execSQL("DROP TABLE IF EXISTS OLDSTATS;");
            db.execSQL("DROP TABLE IF EXISTS COLOR;");
            //    db.execSQL("DROP TABLE IF EXISTS AVATARS;");

        }
        Log.v("oldVersiun",""+oldVersion);
        //if(oldVersion<90) {
        //   db.execSQL("CREATE TABLE IF NOT EXISTS AVATARS (user_id TEXT PRIMARY KEY , file BLOB);");
        db.execSQL("CREATE TABLE IF NOT EXISTS TASKTABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT,name TEXT, datetime INTEGER, working_time INTEGER,priority INTEGER, current_work_time INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS COLOR (category_id TEXT PRIMARY KEY , color TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS OLDSTATS (date_id INTEGER PRIMARY KEY , working_time INTEGER, category TEXT);");
          /*  Cursor c=db.rawQuery("SHOW TABLES;",new String[]{});
            if(c.moveToFirst())
                do{
                    Log.v("TABLES",c.toString());
                }while (c.moveToNext());
            c.close();*/
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2021, 11, 1, 12, 6,0);
        cal.set(Calendar.MILLISECOND,0);
        fillTestData("Książki", "Ludzie bezdomni", cal.getTime().getTime(),(int)(2.3*3600) ,3,(int)(1.2*3600));
        cal.set(2021, 11, 1, 15, 6,0);
        cal.set(Calendar.MILLISECOND,0);
        fillTestData("Sport", "Sztanga", cal.getTime().getTime(), (int)(6.6*3600),1,(int)(2.6*3600));
        cal.set(2021, 11, 2, 1, 6,0);
        cal.set(Calendar.MILLISECOND,0);
        fillTestData("Sport", "Bieganie", cal.getTime().getTime(), (int)(1.8*3600),1,3600);
        cal.set(2022, 4, 11, 14, 16,0);
        cal.set(Calendar.MILLISECOND,0);
        fillTestData("Praca w ogrodzie","Sadzenie cebuli",cal.getTime().getTime(),(int)(2.4*3600) ,2,(int)(2.0*3600));
        cal.set(2022, 6, 30, 10, 2,0);
        cal.set(Calendar.MILLISECOND,0);
        fillTestData("Praca w ogrodzie","Podlewanie kwiatów",cal.getTime().getTime(),(int)(0.4*3600),2 ,0);

        addColorRow( "Praca w ogrodzie", "#FFAA56");
        addColorRow( "Książki", "#AAFF96");
        addColorRow( "Sport", "#2266BB");

        //  }



    }
    private void fillTestData(String category, String name, Long startdatetime, int hours, int priority, int workingTime){
        addTaskRow(category,name,startdatetime,hours,priority,workingTime);
        addOldstatRow(startdatetime,hours,category);
    }

    /**
     *
     * @param date set only Y,M,D rest need to be 0, also milliseconds
     * @param duration float value as String
     */
    public void addOldstatRow(Long date,int duration,String category){
        ContentValues cv = createOldstatValues(date,duration,category);
        db.insert("OLDSTATS", null, cv);
        cv.clear();
    }
    public void editOldstatRow(Long date,Long newDate,int duration,String category){
        ContentValues cv = createOldstatValues(newDate,duration,category);
        db.update("OLDSTATS", cv,"date_id = ?",new String[]{String.valueOf(date)});
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
    /*public long addAvatarRow(String user_id, byte[] img){
        ContentValues cv =crateAvatarValues(user_id,img);
        return db.insert("AVATARS",null,cv);
    }
    public long editAvatarRow(String user_id,byte[] img){
        ContentValues cv =crateAvatarValues(user_id,img);
        return db.update("AVATARS",cv,"user_id=?",new String[]{user_id});
    }
    public Boolean isAvatarRowExist(String user_id){
        Cursor c= db.rawQuery("SELECT * FROM AVATARS WHERE user_id=?",new String[]{user_id});
        Boolean t=c.moveToFirst();
        c.close();
        return t;

    }
    public byte[] getAvatarRow(String user_id) {
        Cursor c = db.rawQuery("SELECT * FROM AVATARS WHERE user_id=?", new String[]{user_id});
        byte[] path=null;
        if (c.moveToFirst())
            path= c.getBlob(1);
        c.close();
        return path;



    }*/

    public long addTaskRow(String category, String name, Long startdatetime, int hours, int priority,int workingTime){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createTasktableValues(category,name,startdatetime,hours,priority,workingTime);
        return db.insert("TASKTABLE", null, cv);

    }
    public long addTaskRow(DataRowWithColor data){                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createTasktableValues(data.getCategory(),data.getName(),data.getDate().getTime().getTime(),(int)data.getWorkingTime(),data.getPriority(),(int)data.getCurrentWorkingTime());
        return  db.insert("TASKTABLE", null, cv);

    }
    public int editTaskRow(int oldId, String category, String name, Long startdatetime, int hours, int priority,int workingTime){        // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createEditTasktableValues(category,name,startdatetime,hours,priority,workingTime);
        return db.update("TASKTABLE"
                ,cv
                ,"_id = ?"
                ,new String[]{String.valueOf(oldId)});
    }
    public DataRowWithColor getTaskRow(int id){
        Cursor c=db.rawQuery("SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime, TASKTABLE.working_time, TASKTABLE.priority, TASKTABLE.current_work_time, COLOR.color FROM TASKTABLE JOIN COLOR ON TASKTABLE.category=COLOR.category_id WHERE TASKTABLE._id=?",new String[]{String.valueOf(id)});
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
                        c.getString(7),
                        cal.getTime().getTime() < now.getTime().getTime());
            }finally {
                c.close();
            }
        }else
            return null;
    }
    public ArrayList<DataRowWithColor> getTodayTasks(Long data){
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


        Cursor c=db.rawQuery("SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime, TASKTABLE.working_time, TASKTABLE.priority, TASKTABLE.current_work_time, COLOR.color " +
                "FROM TASKTABLE JOIN COLOR ON TASKTABLE.category=COLOR.category_id WHERE TASKTABLE.datetime BETWEEN "+startTime+" AND "+endTime,new String[]{});
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
                            cal.getTime().getTime() < now.getTime().getTime()));

            }while(c.moveToNext());
            c.close();
            return array;
        }else
            return null;
    }

    public int deleteTaskRow(int id){
        return db.delete("TASKTABLE","_id = ?", new String[]{String.valueOf(id)});
    }

    @NotNull
    private  ContentValues createTasktableValues(String category, String name, Long starttime, int hours, int priority,int currentWorkingTime){
        ContentValues cv = new ContentValues();
        cv.put("category",category);
        cv.put("name",name);
        cv.put("datetime",starttime);
        cv.put("working_time",hours);
        cv.put("priority",priority);
        cv.put("current_work_time",currentWorkingTime);
        return cv;
    }
    @NotNull
    private  ContentValues crateAvatarValues(String user_id,byte[] img){
        ContentValues cv = new ContentValues();
        cv.put("user_id",user_id);
        cv.put("file",img);
        return cv;
    }
    @NotNull
    private  ContentValues createEditTasktableValues(String category, String name, Long starttime, int hours, int priority,int currentWorkingTime){
        ContentValues cv = new ContentValues();
        if(category!=null)cv.put("category",category);
        if(name!=null)cv.put("name",name);
        if(starttime!=null)cv.put("datetime",starttime);
        if(hours!=0)cv.put("working_time",hours);
        if(priority!=0)cv.put("priority",priority);
        if(currentWorkingTime!=0)cv.put("current_work_time",currentWorkingTime);
        return cv;
    }
    private  ContentValues createOldstatValues( Long starttime, int hours,String category){
        ContentValues cv = new ContentValues();
        if(starttime!=null)cv.put("date_id",starttime);
        if(hours!=0)cv.put("working_time",hours);
        if(category!=null)cv.put("category",category);
        return cv;
    }

    public  void addColorRow(String category, String color) {                                                                       // dodaj sprawdzanie czy wpisy już istnieją
        db.insert("COLOR", null, createColorCValues(category,color));
    }

    public  void editColorRow(String oldCategory,String newCategory, String newColor) {                                               // dodaj sprawdzanie czy wpisy już istnieją
        db.update("COLOR",createColorCValues(newCategory,newColor),"category_id = ?", new String[]{oldCategory});
    }
    public  void deleteColorRow(String oldCategory) {
        db.delete("COLOR","category_id = ? ", new String[]{oldCategory});
    }

    @NotNull
    private  ContentValues createColorCValues(String category, String color){
        ContentValues cv = new ContentValues();
        if(category!=null)cv.put("category_id", category);
        if(color!=null)cv.put("color", color);
        return cv;
    }

}
