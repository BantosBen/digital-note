package com.sanj.digote.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.sanj.EncryptionAlgorithm;
import com.sanj.digote.models.NoteModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sanj.digote.data.NoteDBSchema.columnColor;
import static com.sanj.digote.data.NoteDBSchema.columnDate;
import static com.sanj.digote.data.NoteDBSchema.columnId;
import static com.sanj.digote.data.NoteDBSchema.columnNote;
import static com.sanj.digote.data.NoteDBSchema.columnSecurity;
import static com.sanj.digote.data.NoteDBSchema.columnTimestamp;
import static com.sanj.digote.data.NoteDBSchema.columnTitle;
import static com.sanj.digote.data.NoteDBSchema.dbName;
import static com.sanj.digote.data.NoteDBSchema.tableName;
import static com.sanj.digote.data.NoteDBSchema.tableNameVault;
import static com.sanj.digote.data.NoteDBSchema.version;

public class NoteDB extends SQLiteOpenHelper {
    @SuppressLint("StaticFieldLeak")
    private static NoteDB noteDB;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private SharedPreferences sharedPreferences;

    private NoteDB(@Nullable Context context) {
        super(context, dbName, null, version);
    }

    public static NoteDB getInstance(Context context) {
        if (noteDB == null) {
            noteDB = new NoteDB(context);
            mContext = context;
        }
        return noteDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE `" + tableName + "` " +
                "(`" + columnId + "` VARCHAR PRIMARY KEY, " +
                "`" + columnTitle + "` VARCHAR, " +
                "`" + columnNote + "` TEXT, " +
                "`" + columnDate + "` VARCHAR, " +
                "`" + columnColor + "` VARCHAR," +
                "`" + columnSecurity + "` VARCHAR," +
                "`" + columnTimestamp + "` VARCHAR )";
        db.execSQL(sql);
        sql = "CREATE TABLE `" + tableNameVault + "`" +
                "(`" + columnId + "` VARCHAR," +
                "`" + columnSecurity + "` VARCHAR )";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addNewNote(NoteModel noteModel) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            String encryptedNote=new EncryptionAlgorithm().encrypt(noteModel.getNote());
            contentValues.put(columnId, noteModel.getId());
            contentValues.put(columnTitle, noteModel.getTitle());
            contentValues.put(columnNote, encryptedNote);
            contentValues.put(columnDate, noteModel.getDate());
            contentValues.put(columnColor, noteModel.getColor());
            contentValues.put(columnSecurity, "0");
            contentValues.put(columnTimestamp, String.valueOf(new Date().getTime()));

            return db.insert(tableName, null, contentValues) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<NoteModel> getAllUnSecuredNotes() {
        sharedPreferences = mContext.getSharedPreferences("note", Context.MODE_PRIVATE);
        SQLiteDatabase db = this.getWritableDatabase();
        List<NoteModel> noteModelList = new ArrayList<>();
        String sql;
        if (sharedPreferences.getString("sort", "Date").equals("Date")) {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnSecurity + "`='0'ORDER BY `" + columnTimestamp + "` DESC";
        } else {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnSecurity + "`='0'ORDER BY `" + columnTitle + "` ASC";
        }
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        try {
            while (result.moveToNext()) {
                String id, title, note, date, color;
                id = result.getString(0);
                title = result.getString(1);
                note = new EncryptionAlgorithm().decrypt(result.getString(2));
                date = result.getString(3);
                color = result.getString(4);
                noteModelList.add(new NoteModel(id, title, note, date, color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noteModelList;
    }


    public NoteModel getSingleNote(String mId) {
        SQLiteDatabase db = this.getWritableDatabase();
        NoteModel noteModel = null;
        String sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnId + "` = '" + mId + "'";
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        try {
            while (result.moveToNext()) {
                String id, title, note, date, color;
                id = result.getString(0);
                title = result.getString(1);
                note = new EncryptionAlgorithm().decrypt(result.getString(2));
                date = result.getString(3);
                color = result.getString(4);
                noteModel = new NoteModel(id, title, note, date, color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noteModel;
    }

    public List<NoteModel> getSearchUnSecuredNotes(String mTitle) {
        sharedPreferences = mContext.getSharedPreferences("note", Context.MODE_PRIVATE);
        SQLiteDatabase db = this.getWritableDatabase();
        List<NoteModel> noteModelList = new ArrayList<>();
        String sql;
        if (sharedPreferences.getString("sort", "Date").equals("Date")) {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnTitle + "` LIKE '%" + mTitle + "%' AND `" + columnSecurity + "`='0' ORDER BY `" + columnTimestamp + "` DESC";
        } else {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnTitle + "` LIKE '%" + mTitle + "%' AND `" + columnSecurity + "`='0'ORDER BY `" + columnTitle + "` ASC";
        }
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        try {
            while (result.moveToNext()) {
                String id, title, note, date, color;
                id = result.getString(0);
                title = result.getString(1);
                note = new EncryptionAlgorithm().decrypt(result.getString(2));
                date = result.getString(3);
                color = result.getString(4);
                noteModelList.add(new NoteModel(id, title, note, date, color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noteModelList;
    }

    public List<NoteModel> getSearchSecuredNotes(String mTitle) {
        sharedPreferences = mContext.getSharedPreferences("note", Context.MODE_PRIVATE);
        SQLiteDatabase db = this.getWritableDatabase();
        List<NoteModel> noteModelList = new ArrayList<>();
        String sql;
        if (sharedPreferences.getString("sort", "Date").equals("Date")) {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnTitle + "` LIKE '%" + mTitle + "%' AND `" + columnSecurity + "`='1' ORDER BY `" + columnTimestamp + "` DESC";
        } else {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnTitle + "` LIKE '%" + mTitle + "%' AND `" + columnSecurity + "`='1' ORDER BY `" + columnTitle + "` ASC";
        }
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        try {
            while (result.moveToNext()) {
                String id, title, note, date, color;
                id = result.getString(0);
                title = result.getString(1);
                note = new EncryptionAlgorithm().decrypt(result.getString(2));
                date = result.getString(3);
                color = result.getString(4);
                noteModelList.add(new NoteModel(id, title, note, date, color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noteModelList;
    }

    public boolean updateNote(NoteModel noteModel) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] args = new String[]{String.valueOf(noteModel.getId())};
            String encryptedNote=new EncryptionAlgorithm().encrypt(noteModel.getNote());
            ContentValues contentValues = new ContentValues();
            contentValues.put(columnTitle, noteModel.getTitle());
            contentValues.put(columnNote, encryptedNote);
            contentValues.put(columnDate, noteModel.getDate());
            contentValues.put(columnColor, noteModel.getColor());
            contentValues.put(columnTimestamp, String.valueOf(new Date().getTime()));
            return db.update(tableName, contentValues, columnId + "=?", args) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateNoteSecurity(String mId, String mSecurity) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String[] args = new String[]{String.valueOf(mId)};
            ContentValues contentValues = new ContentValues();
            contentValues.put(columnSecurity, mSecurity);
            return db.update(tableName, contentValues, columnId + "=?", args) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getNoteSecurity(String mId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT `" + columnSecurity + "` FROM `" + tableName + "` WHERE `" + columnId + "` = '" + mId + "'";
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        boolean secure = false;
        if (result.moveToNext()) {
            secure = result.getString(0).equals("1");
        }
        return secure;
    }

    public boolean deleteNote(String mId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{String.valueOf(mId)};
        return db.delete(tableName, columnId + "=?", args) > 0;
    }

    public boolean addNewVaultCredentials(String mId, String mPin) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM `" + tableNameVault + "`";
            db.execSQL(sql);
            String encryptedSecurity = new EncryptionAlgorithm().encrypt(mPin);
            ContentValues contentValues = new ContentValues();
            contentValues.put(columnId, mId);
            contentValues.put(columnSecurity, encryptedSecurity);

            return db.insert(tableNameVault, null, contentValues) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean vaultVerification(String mPin) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "SELECT * FROM `" + tableNameVault + "`";
            @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
            String encryptedSecurity = "";
            while (result.moveToNext()) {
                encryptedSecurity = result.getString(1);
            }
            return new EncryptionAlgorithm().decrypt(encryptedSecurity).equals(mPin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkVaultExistence() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM `" + tableNameVault + "`";
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        return result.moveToNext();
    }

    public List<NoteModel> getAllSecuredNotes() {
        sharedPreferences = mContext.getSharedPreferences("note", Context.MODE_PRIVATE);
        SQLiteDatabase db = this.getWritableDatabase();
        List<NoteModel> noteModelList = new ArrayList<>();
        String sql;

        if (sharedPreferences.getString("sort", "Date").equals("Date")) {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnSecurity + "`='1' ORDER BY `" + columnTimestamp + "` DESC";
        } else {
            sql = "SELECT * FROM `" + tableName + "` WHERE `" + columnSecurity + "`='1' ORDER BY `" + columnTitle + "` ASC";
        }
        @SuppressLint("Recycle") Cursor result = db.rawQuery(sql, null);
        try {
            while (result.moveToNext()) {
                String id, title, note, date, color;
                id = result.getString(0);
                title = result.getString(1);
                note = new EncryptionAlgorithm().decrypt(result.getString(2));
                date = result.getString(3);
                color = result.getString(4);
                noteModelList.add(new NoteModel(id, title, note, date, color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noteModelList;
    }
}
