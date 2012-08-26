package org.takanolab.ar.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "preference_thesaurus";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table terms (" +
			"term TEXT not null PRIMARY KEY," +
			"marker_click INTEGER DEFAULT 0," +
			"cg_zoom_in INTEGER DEFAULT 0," +
			"cg_zoom_out INTEGER DEFAULT 0," +
			"cg_rotation INTEGER DEFAULT 0," +
			"search_keyword INTEGER DEFAULT 0," +
			"photo_zoom_in INTEGER DEFAULT 0," +
			"photo_zoom_out INTEGER DEFAULT 0," + 
			"cg_session_time INTEGER DEFAULT 0," +
			"qu_session_time INTEGER DEFAULT 0" +
			");";

	public MyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		System.out.println("Database created.");
	}
	
	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(MyDatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS student_profile");
		onCreate(database);
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
}
