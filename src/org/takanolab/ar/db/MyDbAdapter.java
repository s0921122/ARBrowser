package org.takanolab.ar.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MyDbAdapter {

	// Database fields
	public static final String KEY_ID = "id";
	public static final String KEY_TERM = "term";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_MARKER_CLICK = "marker_click";
	public static final String KEY_CG_ZOOM_IN = "cg_zoom_in";
	public static final String KEY_CG_ZOOM_OUT = "cg_zoom_out";
	public static final String KEY_CG_ROTATION = "cg_rotation";
	public static final String KEY_SEARCH_KEYWORD = "search_keyword";	
	public static final String KEY_PHOTO_ZOOM_IN = "photo_zoom_in";
	public static final String KEY_PHOTO_ZOOM_OUT = "photo_zoom_out";
	public static final String KEY_CG_SESSION_TIME = "cg_session_time";
	public static final String KEY_QU_SESSION_TIME = "qu_session_time";
	
	private static final String DATABASE_TABLE = "terms";
	private Context context;
	private SQLiteDatabase database;
	private MyDatabaseHelper dbHelper;

	public MyDbAdapter(Context context) {
		this.context = context;
	}

	public MyDbAdapter open() throws SQLException {
		dbHelper = new MyDatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	
	/* Insert */
	public long insert (String term, String typeName, Integer val) {
		ContentValues initialValues = createContentValues(term, typeName, val);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}
/** * Update */

	public boolean update (String term, String typeName, Integer val) {
		ContentValues updateValues = createContentValues(term, typeName, val);

		return database.update(DATABASE_TABLE, updateValues, KEY_TERM + "="
				+ term, null) > 0;
	}

	
/** * Delete */

	public boolean delete (String term) {
		return database.delete(DATABASE_TABLE, KEY_TERM + "=" + term, null) > 0;
	}

	
/** * Return a Cursor over the list of all in the database * * @return Cursor over all notes */
	public Cursor fetchAllData() {
		return database.query(DATABASE_TABLE, new String[] { KEY_TERM, KEY_MARKER_CLICK}, null, null, null,
				null, null);
	}

	public Cursor select(String term) {
		return database.query(DATABASE_TABLE, new String[] { KEY_TERM, KEY_MARKER_CLICK }, "term = ?",new String[]{term}, null,
				null, null);
	}
		
	private ContentValues createContentValues(String term, String typeName, Integer val) {
		ContentValues values = new ContentValues();
		values.put(KEY_TERM, term);
		values.put(typeName, val);
		return values;
	}
	
}