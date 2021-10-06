package com.voidsamurai.lordoftime.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;


public class LOTDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LOT";

    private static final int DB_VERSION = 7;
    private static SQLiteDatabase db;

    public LOTDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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
        if (oldVersion > 1) {

            db.execSQL("DROP TABLE TASKTABLE;");
            db.execSQL("DROP TABLE OLDSTATS;");
            db.execSQL("DROP TABLE COLOR;");
            db.execSQL("DROP TABLE AVATARS;");

        }
        if(oldVersion<90) {
            db.execSQL("CREATE TABLE TASKTABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT,name TEXT, datetime INTEGER, working_time INTEGER,priority INTEGER, current_work_time INTEGER);");
            db.execSQL("CREATE TABLE COLOR (category_id TEXT PRIMARY KEY , color TEXT);");
            db.execSQL("CREATE TABLE OLDSTATS (date_id INTEGER PRIMARY KEY , working_time INTEGER, category TEXT);");
            db.execSQL("CREATE TABLE AVATARS (user_id TEXT PRIMARY KEY , file BLOB);");

            Calendar cal = Calendar.getInstance();
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

        }



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
    public long addAvatarRow(String user_id, byte[] img){
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



    }

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
