package org.takanolab.ar.db;


import java.io.File;   
import java.io.FileOutputStream;
import java.io.IOException;   
import java.io.FileInputStream;
import android.os.Environment;
import android.content.Context;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.channels.FileChannel;

public class DBUtil {
	public static boolean copyDb2Sd(Context context, String dbName) throws IOException {

	    final String TAG = "copyDb2Sd";

	    // Copy database to SD
	    String pathSd = new StringBuilder()
	                        .append(Environment.getExternalStorageDirectory().getPath())
	                        .append("/")
	                        .append(context.getPackageName())
	                        .toString();
	    File filePathToSaved = new File(pathSd);
	    if (!filePathToSaved.exists() && !filePathToSaved.mkdirs()) {
	        throw new IOException("FAILED_TO_CREATE_PATH_ON_SD");
	    }

	    final String fileDb = context.getDatabasePath(dbName).getPath();
	    final String fileSd = new StringBuilder()
	                            .append(pathSd)
	                            .append("/")
	                            .append(dbName)
	                            .append(".")
	                            .append((new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()))
	                            .toString();

	    Log.i(TAG, "copy from(DB): "+fileDb);
	    Log.i(TAG, "copy to(SD)  : "+fileSd);

	    FileChannel channelSource = new FileInputStream(fileDb).getChannel();
	    FileChannel channelTarget = new FileOutputStream(fileSd).getChannel();

	    channelSource.transferTo(0, channelSource.size(), channelTarget);

	    channelSource.close();
	    channelTarget.close();

	    return true;
	}
	
	public static boolean copyDbFromSd(Context context, String dbName) throws IOException {

	    final String TAG = "copyDb2Sd";

	    // Copy database from SD
	    String pathSd = new StringBuilder()
	                        .append(Environment.getExternalStorageDirectory().getPath())
	                        .append("/")
	                        .append(context.getPackageName())
	                        .toString();
	    File filePathSd = new File(pathSd);
	    if (!filePathSd.exists()) {
	        throw new IOException("FAILED_TO_FIND_DBPATH_ON_SD");
	    }

	    final String fileDb = context.getDatabasePath(dbName).getPath();
	    final String fileSd = new StringBuilder()
	                            .append(pathSd)
	                            .append("/")
	                            .append(dbName)
	                            .toString();

	    Log.i(TAG, "copy from(DB): "+fileSd);
	    Log.i(TAG, "copy to(SD)  : "+fileDb);

	    FileChannel channelSource = new FileInputStream(fileSd).getChannel();
	    FileChannel channelTarget = new FileOutputStream(fileDb).getChannel();

	    channelSource.transferTo(0, channelSource.size(), channelTarget);

	    channelSource.close();
	    channelTarget.close();

	    return true;
	}
}
