package com.example.mutualfunds;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class stockNode {
    public final String shareName;
    public final int frequency;

    public stockNode(String shareName, int frequency) {
        this.shareName = shareName;
        this.frequency = frequency;
    }

}

class Database extends SQLiteOpenHelper {
    private final String[] TABLES = {"large_cap", "mid_cap", "small_cap"};

    public Database(Context context) {
        super(context, "stockData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (String tableName : TABLES)
            sqLiteDatabase.execSQL("CREATE TABLE " + tableName + " (Name VARCHAR(30),Frequency INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        for (String tableName : TABLES)
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(sqLiteDatabase);

    }

    public void refresh(String tableName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + tableName);
        sqLiteDatabase.close();

    }

    public void insert(String tableName, String stockName, int Frequency) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + tableName + " VALUES (\"" + stockName + "\", " + "\"" + Frequency + "\")");
        db.close();
    }

    public List<stockNode> getTopMost(String tableName, int num) {
        List<stockNode> stockNodeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE name != \"#MAX#\" ORDER BY Frequency desc  LIMIT " + num, null);
        if (cursor != null)
            while (cursor.moveToNext()) {
                stockNodeList.add(new stockNode(cursor.getString(0), cursor.getInt(1)));
            }
        else {
            db.close();
            Objects.requireNonNull(cursor).close();
            return null;
        }
        db.close();
        cursor.close();
        return stockNodeList;
    }

    public int getTotal(String tableName) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT frequency from " + tableName + " where Name==\"#MAX#\"", null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            int tmp = cursor.getInt(0);
            cursor.close();
            db.close();
            return tmp;
        }
        Objects.requireNonNull(cursor).close();
        db.close();
        return 0;

    }
}
