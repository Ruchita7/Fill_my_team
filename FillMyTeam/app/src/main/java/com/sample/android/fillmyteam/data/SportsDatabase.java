package com.sample.android.fillmyteam.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.ExecOnCreate;
import net.simonvt.schematic.annotation.OnConfigure;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by dgnc on 5/22/2016.
 */
@Database(version = SportsDatabase.VERSION,packageName = "com.sample.android.fillmyteam.provider")
public final class SportsDatabase {

    public static final int VERSION = 6;

    @Table(SportsColumns.class)
    public static final String SPORTS_INFO="sports_info";

    @Table(PlayerMatchesColumns.class)
    public static final String UPCOMING_MATCHES="upcoming_matches";

    @OnCreate
    public static void onCreate(Context context, SQLiteDatabase db) {
    }

    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion,
                                 int newVersion) {
    }

    @OnConfigure
    public static void onConfigure(SQLiteDatabase db) {
    }

    @ExecOnCreate
    public static final String EXEC_ON_CREATE = "SELECT * FROM " + SPORTS_INFO;

}

