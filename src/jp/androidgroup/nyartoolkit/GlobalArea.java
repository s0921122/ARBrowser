/**
 * どのクラスでも使えるように値を保持するクラス
 * 引数の繋がりを理解できたらいらなくなる？
 * 
 * @author s0921122
 */

package jp.androidgroup.nyartoolkit;

import android.os.Environment;


public class GlobalArea {

	public static GlobalArea instance = new GlobalArea();
	
	public GlobalArea(){}
	
	public static GlobalArea getInstace(){
		return instance;
	}
	
	private static String Modelname = "kamakiri.mqo";
	private static String Texturename = "";
	
	// ModelDataPath (mmt/sdcard/3DModelDataを指す)
	public static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "3DModelData";
	
	public static String getModelname() {
		return Modelname;
	}

	public static void setModelname(String modelname) {
		Modelname = modelname;
	}

	public static String getTexturename() {
		return Texturename;
	}

	public static void setTexturename(String texturename) {
		Texturename = texturename;
	}

}
