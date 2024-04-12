// Reference: This provider is modified based on Lab008A

package com.fy916.android_fitness_tracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataProvider extends ContentProvider {
    private final static String TAG = DataProvider.class.getSimpleName();
    private static final UriMatcher mURIMatcher;
    private static final String tableName = "LocationTable";

    static {
        mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mURIMatcher.addURI(DataProviderContract.AUTHORITY, "LocationTable", 1);
    }

    private DBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "ContentProvider - OnCreate");
        // set the current database helper
        this.mDBHelper = new DBHelper(this.getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG, String.format("Received URI: %s [Matches] %s", uri, mURIMatcher.match(uri)));
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        // this query function passes the query request to perform on the track database
        switch (mURIMatcher.match(uri)) {
            case 2:
                selection = "_ID = " + uri.getLastPathSegment();
            case 1:
                return db.query("LocationTable", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // get different type of content uri paths
        if (uri.getLastPathSegment() == null) {
            return DataProviderContract.CONTENT_TYPE_MULTIPLE;
        } else {
            return DataProviderContract.CONTENT_TYPE_SINGLE;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // this query function passes the insert request to perform on the track database

        SQLiteDatabase db = mDBHelper.getWritableDatabase();


        long id = db.insert(tableName, null, values);
        db.close();

        Uri insertURI;
        insertURI = ContentUris.withAppendedId(uri, id);

        Log.d(TAG, insertURI.toString());

        getContext().getContentResolver().notifyChange(insertURI, null);

        return insertURI;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // this query function passes the update request to perform on the track database

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = db.update(tableName, values, selection, selectionArgs);
        db.close();

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // this query function passes the delete request to perform on the track database

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = db.delete(tableName, selection, selectionArgs);
        db.close();

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}













