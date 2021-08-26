package com.voidsamurai.lordoftime.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;


public class LOTDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LOT";
    private static final int DB_VERSION = 3;
    private static SQLiteDatabase db;

    public LOTDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db,0,DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db,oldVersion,newVersion);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        LOTDatabaseHelper.db =db;
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion){
        LOTDatabaseHelper.db =db;
        if(oldVersion<1) {
         //   db.execSQL("DROP TABLE TASKTABLE");
            db.execSQL("CREATE TABLE TASKTABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT,name TEXT, datetime INTEGER, working_time TEXT,priority INTEGER);");
            db.execSQL("CREATE TABLE COLOR (category_id TEXT PRIMARY KEY , color TEXT);");
            db.execSQL("CREATE TABLE OLDSTATS (date_id INTEGER PRIMARY KEY , working_time TEXT);");

            Calendar cal = Calendar.getInstance();
            cal.set(2021, 11, 1, 12, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Książki", "Ludzie bezdomni", cal.getTime().getTime(), "2.4",3);
            cal.set(2021, 11, 1, 15, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Sport", "Sztanga", cal.getTime().getTime(), "6.6",1);
            cal.set(2021, 11, 2, 1, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Sport", "Bieganie", cal.getTime().getTime(), "1.8",1);
            cal.set(2022, 4, 11, 14, 16,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Praca w ogrodzie","Sadzenie cebuli",cal.getTime().getTime(),"2.4" ,2);
            cal.set(2022, 6, 30, 10, 2,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Praca w ogrodzie","Podlewanie kwiatów",cal.getTime().getTime(),"0.4",2 );

            addColorRow( "Praca w ogrodzie", "#FFAA56");
            addColorRow( "Książki", "#AAFF96");
            addColorRow( "Sport", "#2266BB");
        }
        if(oldVersion<3){
            Calendar cal = Calendar.getInstance();
            cal.set(2021, 11, 1, 12, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Książki", "Ludzie bezdomni", cal.getTime().getTime(), "2.4",3);
            cal.set(2021, 11, 1, 15, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Sport", "Sztanga", cal.getTime().getTime(), "6.6",1);
            cal.set(2021, 11, 2, 1, 6,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Sport", "Bieganie", cal.getTime().getTime(), "1.8",1);
            cal.set(2022, 4, 11, 14, 16,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Praca w ogrodzie","Sadzenie cebuli",cal.getTime().getTime(),"2.4" ,2);
            cal.set(2022, 6, 30, 10, 2,0);
            cal.set(Calendar.MILLISECOND,0);
            fillData("Praca w ogrodzie","Podlewanie kwiatów",cal.getTime().getTime(),"0.4",2 );
        }
        if(oldVersion<5){

           // db.execSQL("CREATE TABLE OLDSTATS (date_id INTEGER PRIMARY KEY , working_time TEXT);");
/*
            Calendar cal = Calendar.getInstance();
            cal.set(2021, 6, 1, 0, 0,0);
            cal.set(Calendar.MILLISECOND,0);
            addOldstatRow(cal.getTime().getTime(),"2.0");
            cal.set(2021, 8, 1, 0, 0,0);
            cal.set(Calendar.MILLISECOND,0);
            addOldstatRow(cal.getTime().getTime(),"4.0");
            cal.set(2021, 6, 30, 0, 0,0);
            cal.set(Calendar.MILLISECOND,0);
            addOldstatRow(cal.getTime().getTime(),"8.0");
*/
        }

    }
    private void fillData(String category, String name, Long startdatetime, String hours, int priority){
        addTaskRow(category,name,startdatetime,hours,priority);
        addOldstatRow(startdatetime,hours);
    }

    /**
     *
     * @param date set only Y,M,D rest need to be 0, also milliseconds
     * @param duration float value as String
     */
    public void addOldstatRow(Long date,String duration){
        ContentValues cv = createOldstatValues(date,duration);
        db.insert("OLDSTATS", null, cv);
        cv.clear();
    }
    public void editOldstatRow(Long date,Long newDate,String duration){
        ContentValues cv = createOldstatValues(newDate,duration);
        db.update("OLDSTATS", cv,"date_id = ?",new String[]{String.valueOf(date)});
        cv.clear();
    }

    /**
     *
     * @param date set only Y,M,D rest need to be 0, also milliseconds
     */
    public void deleteOldstatRow(Long date){
        db.delete("OLDSTATS"
                ,"date_id = ?"
                ,new String[]{String.valueOf(date)});
    }
    public void addTaskRow(String category, String name, Long startdatetime, String hours, int priority) {                   // dodaj sprawdzanie czy wpisy już istnieją
        ContentValues cv = createTasktableValues(category,name,startdatetime,hours,priority);
        db.insert("TASKTABLE", null, cv);
        cv.clear();
    }
    public void editTaskRow(int oldId, String category, String name, Long startdatetime, String hours, int priority){        // dodaj sprawdzanie czy wpisy już istnieją
            ContentValues cv = createTasktableValues(category,name,startdatetime,hours,priority);
            db.update("TASKTABLE"
                    ,cv
                    ,"_id = ?"
                    ,new String[]{String.valueOf(oldId)});
    }

    public void deleteTaskRow(int id){
            db.delete("TASKTABLE","_id = ?", new String[]{String.valueOf(id)});
    }

    @NotNull
    private  ContentValues createTasktableValues(String category, String name, Long starttime, String hours, int priority){
        ContentValues cv = new ContentValues();
        if(category!=null)cv.put("category",category);
        if(name!=null)cv.put("name",name);
        if(starttime!=null)cv.put("datetime",starttime);
        if(hours!=null)cv.put("working_time",hours);
        if(priority!=0)cv.put("priority",priority);
        return cv;
    }
    private  ContentValues createOldstatValues( Long starttime, String hours){
        ContentValues cv = new ContentValues();
        if(starttime!=null)cv.put("date_id",starttime);
        if(hours!=null)cv.put("working_time",hours);
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
