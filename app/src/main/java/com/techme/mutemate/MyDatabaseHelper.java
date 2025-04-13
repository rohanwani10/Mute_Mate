package com.techme.mutemate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mutemate.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    public static final String TABLE_LEARNING_VIDEOS = "learning_videos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PATH = "path";

    // SQL statement to create the table
    private static final String CREATE_TABLE_LEARNING_VIDEOS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_LEARNING_VIDEOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_PATH + " TEXT NOT NULL);";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LEARNING_VIDEOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEARNING_VIDEOS);
        onCreate(db);
    }

    public long insertVideo(String title, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PATH, path);

        long result = db.insert(TABLE_LEARNING_VIDEOS, null, values);
        db.close();
        return result;
    }

    public int updateVideoTitle(int id, String newTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, newTitle);

        int rowsAffected = db.update(TABLE_LEARNING_VIDEOS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public String getVideoPath(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LEARNING_VIDEOS, new String[]{COLUMN_PATH}, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATH));
            cursor.close();
            db.close();
            return path;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }
}
