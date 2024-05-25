package com.example.nir;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {
    private final String TAG = DatabaseAdapter.class.getSimpleName();
    private final Activity  context;
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;
    private String sql;

    public DatabaseAdapter(Activity context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(this.context);
    }

    public DatabaseAdapter createDataBase() throws SQLException {
        try {
            databaseHelper.createDataBase();
        } catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DatabaseAdapter openDataBase() throws SQLException {
        try {
            databaseHelper.openDataBase();
            databaseHelper.close();
            database = databaseHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        databaseHelper.close();
    }

    public Cursor getData(String sql) {
        this.sql = sql;
        try {
            Cursor mCur = database.rawQuery(sql, null);
            return mCur;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public void executeSql(String sql) {
        this.sql = sql;
        database.execSQL(sql);
    }

    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups";
        Cursor cursor = getData(sql);
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            groups.add(name);
        }
        cursor.close();
        return groups;
    }

    public ArrayList<ItemEpt> getAllEptById(int id) {
        ArrayList<ItemEpt> ept = new ArrayList<>();
        String sql = "SELECT * FROM sub_groups WHERE group_id=" + (id + 1) + ";";
        Cursor cursor = getData(sql);
        while (cursor.moveToNext()) {
            int subgroup_id = cursor.getInt(0);
            int group_id = cursor.getInt(1);
            String name = cursor.getString(2);
            String sql1 = "SELECT * FROM vals WHERE sub_group_id="+subgroup_id+";";
            Cursor cursor1 = getData(sql1);
            while (cursor1.moveToNext()) {
                int value_id = cursor1.getInt(0);
                int sub_group_id = cursor1.getInt(1);
                int value = cursor1.getInt(2);
                String type = cursor1.getString(3);
                ItemEpt itemEpt = new ItemEpt(name, value, type);
                ept.add(itemEpt);
            }
            cursor1.close();
        }
        cursor.close();
        return ept;
    }

    public ArrayList<ItemEpt> getAllEpt() {
        ArrayList<ItemEpt> ept = new ArrayList<>();
        String sql = "SELECT * FROM sub_groups;";
        Cursor cursor = getData(sql);
        while (cursor.moveToNext()) {
            int subgroup_id = cursor.getInt(0);
            String name = cursor.getString(2);
            String sql1 = "SELECT * FROM vals WHERE sub_group_id="+subgroup_id+";";
            Cursor cursor1 = getData(sql1);
            while (cursor1.moveToNext()) {
                int value = cursor1.getInt(2);
                String type = cursor1.getString(3);
                ItemEpt itemEpt = new ItemEpt(name, value, type);
                ept.add(itemEpt);
            }
            cursor1.close();
        }
        cursor.close();
        return ept;
    }

    public ItemEpt getEptByValue(int code) {
        ItemEpt ept = null;
        String sqlEPT = "SELECT * FROM vals WHERE value=" + code + ";";
        Cursor cursorEPT = getData(sqlEPT);
        while (cursorEPT.moveToNext()) {
            int value_id = cursorEPT.getInt(0);
            int sub_group_id = cursorEPT.getInt(1);
            int value = cursorEPT.getInt(2);
            String type = cursorEPT.getString(3);
            String sqlName = "SELECT * FROM sub_groups WHERE id=" + sub_group_id + ";";
            Cursor cursorName = getData(sqlName);
            while (cursorName.moveToNext()) {
                int sub_id = cursorName.getInt(0);
                int group_id = cursorName.getInt(1);
                String name = cursorName.getString(2);

                ept = new ItemEpt(name, value, type);
            }
        }
        return ept;
    }
}
