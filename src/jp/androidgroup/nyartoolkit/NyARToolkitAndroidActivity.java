/*
 * PROJECT: NyARToolkit for Android SDK
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * NyARToolkit for Android SDK
 *   Copyright (C)2010 NyARToolkit for Android team
 *   Copyright (C)2010 R.Iizuka(nyatla)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For further information please contact.
 *  http://sourceforge.jp/projects/nyartoolkit-and/
 *
 * This work is based on the NyARToolKit developed by
 *  R.Iizuka (nyatla)
 *    http://nyatla.jp/nyatoolkit/
 *
 * contributor(s)
 *  Atsuo Igarashi
 */

package jp.androidgroup.nyartoolkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import min3d.animation.AnimationObject3d;
import min3d.core.Scene;
import min3d.parser.IParser;
import min3d.parser.Parser;
import min3d.vos.Light;

import org.takanolab.ar.search.SearchActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.paar.ch9.MainActivity;

//public class NyARToolkitAndroidActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
public class NyARToolkitAndroidActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback, min3d.interfaces.ISceneController {

	public static final String TAG = "NyARToolkitAndroid";

	private static final int CROP_MSG = 1;
	private static final int FIRST_TIME_INIT = 2;
	private static final int RESTART_PREVIEW = 3;
	private static final int CLEAR_SCREEN_DELAY = 4;
	private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;
	public static final int SHOW_LOADING = 6;
	public static final int HIDE_LOADING = 7;

	private static final int SCREEN_DELAY = 2 * 60 * 1000;

	private android.hardware.Camera.Parameters mParameters;

	private OrientationEventListener mOrientationListener;
	private int mLastOrientation = 0;
	private SharedPreferences mPreferences;

	private static final int IDLE = 1;
	private static final int SNAPSHOT_IN_PROGRESS = 2;

	// 3DCG manipulation
	private final int MENU_SCALE = 0;
	private final int MENU_ROTATE = 1;
	private final int MENU_TRANSLATE = 2;
	private final int MENU_SCREENSHOT = 3;

	// coordinates for 3DCG
	private float lastX=0;
	private float lastY=0;

	private int mode = MENU_SCALE;

	private int mStatus = IDLE;

	private android.hardware.Camera mCameraDevice;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder = null;
	private boolean mStartPreviewFail = false;

	private GLSurfaceView mGLSurfaceView = null;
	// Renderer for metasequoia model
	private ModelRenderer mRenderer;
	// Renderer of min3d
	//    private Renderer mRenderer;

	private boolean mPreviewing;
	private boolean mPausing;
	private boolean mFirstTimeInitialized;

	private Handler mHandler = new MainHandler();

	private PreviewCallback mPreviewCallback = new PreviewCallback();

	private ARToolkitDrawer arToolkitDrawer = null;

	private MediaPlayer mMediaPlayer = null;

	/** This Handler is used to post message back onto the main thread of the application */
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RESTART_PREVIEW: {
				restartPreview();
				break;
			}

			case CLEAR_SCREEN_DELAY: {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			}

			case FIRST_TIME_INIT: {
				initializeFirstTime();
				break;
			}

			case SHOW_LOADING: {
				showDialog(DIALOG_LOADING);
				break;
			}
			case HIDE_LOADING: {
				try {
					dismissDialog(DIALOG_LOADING);
					removeDialog(DIALOG_LOADING);
				} catch (IllegalArgumentException e) {
				}
				break;
			}
			}
		}
	}

	//メニュー設定
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	//メニューセレクト
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);

		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.MENU_SCALE:
			mode = MENU_SCALE;
			return true;
		case R.id.MENU_ROTATE:
			mode = MENU_ROTATE;
			return true;
		case R.id.MENU_TRANSLATE:
			mode = MENU_TRANSLATE;
			return true;
		case R.id.MENU_CLEAR:
			arToolkitDrawer.objectClear();
			return true;
		case R.id.MENU_CHANGE_MODE:
			arToolkitDrawer.toggleARMode();
			return true;
		case R.id.MENU_QUEST_MODE:
			// change to Quest activity
			intent = new Intent(NyARToolkitAndroidActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		case R.id.MENU_SEARCH:
			// change to search activity
			intent = new Intent(NyARToolkitAndroidActivity.this, SearchActivity.class);
			startActivity(intent);
			return true;
		case R.id.MENU_SCREEN_SHOT:
			Log.e("debug","screen shot");
			shot();
			return true;
		default:
			String message = "Error";
			break;
		}
		return true;
	}

	private static final int DIALOG_LOADING = 0;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOADING: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Loading ...");
			// dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.getWindow().setFlags
			(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			return dialog;
		}
		default:
			return super.onCreateDialog(id);
		}
	}

	public static int roundOrientation(int orientationInput) {
		//		Log.d("roundOrientation", "orientationInput:" + orientationInput);
		int orientation = orientationInput;
		if (orientation == -1)
			orientation = 0;

		orientation = orientation % 360;
		int retVal;
		if (orientation < (0*90) + 45) {
			retVal = 0;
		} else if (orientation < (1*90) + 45) {
			retVal = 90;
		} else if (orientation < (2*90) + 45) {
			retVal = 180;
		} else if (orientation < (3*90) + 45) {
			retVal = 270;
		} else {
			retVal = 0;
		}

		return retVal;
	}

	// Snapshots can only be taken after this is called. It should be called
	// once only. We could have done these things in onCreate() but we want to
	// make preview screen appear as soon as possible.
	private void initializeFirstTime() {
		if (mFirstTimeInitialized) return;

		Log.d(TAG, "initializeFirstTime");

		// Create orientation listenter. This should be done first because it
		// takes some time to get first orientation.
		mOrientationListener =
				new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				// We keep the last known orientation. So if the user
				// first orient the camera then point the camera to
				// floor/sky, we still have the correct orientation.
				if (orientation != ORIENTATION_UNKNOWN) {
					orientation += 90;
				}
				orientation = roundOrientation(orientation);
				if (orientation != mLastOrientation) {
					mLastOrientation = orientation;
				}
			}
		};
		mOrientationListener.enable();

		mFirstTimeInitialized = true;

		changeGLSurfaceViewState();
	}

	// If the activity is paused and resumed, this method will be called in
	// onResume.
	private void initializeSecondTime() {
		Log.d(TAG, "initializeSecondTime");

		// Start orientation listener as soon as possible because it takes
		// some time to get first orientation.
		mOrientationListener.enable();

		changeGLSurfaceViewState();
	}

	/**
	 * Callback interface used to deliver copies of preview frames as they are displayed.
	 */
	private final class PreviewCallback
	implements android.hardware.Camera.PreviewCallback {

		@Override
		public void onPreviewFrame(byte [] data, Camera camera) {
			Log.d(TAG, "PreviewCallback.onPreviewFrame");

			if (mPausing) {
				return;
			}

			if(data != null) {
				Log.d(TAG, "data exist");

				if (arToolkitDrawer != null)
					arToolkitDrawer.draw(data);

			} else {
				try {
					// The measure against over load.
					Thread.sleep(500);
				} catch (InterruptedException e) {
					;
				}
			}
			restartPreview();
		}
	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		/*
		// SDcard内のファイルを列挙する
		String[] items = new File("sdcard/3DModelData").list();
		ArrayList<String> mqo = new ArrayList<String>();
		// 拡張子がmqoのものだけを抽出
		for(int i=0;items.length>i;i++){
			if(items[i].endsWith("mqo")){
				mqo.add(items[i]);
			}
		}
		// コレクションを配列に変換
		final String[] modelName = (String[]) mqo.toArray(new String[0]);
        */
		
		// Renderer for metasequoia model
		String[] modelName = new String[2];
		//		modelName[0] = "droid.mqo";
		//		modelName[1] = "miku01.mqo";
		modelName[0] = "Kiageha.mqo";
		modelName[1] = "kamakiri.mqo";
		float[] modelScale = new float[] {0.01f, 0.03f};
		mRenderer = new ModelRenderer(getAssets(), modelName, modelScale);
		mRenderer.setMainHandler(mHandler);

		// Test later		
		//		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		//        Display disp = wm.getDefaultDisplay();
		//        int width = disp.getWidth();
		//        int height = disp.getHeight();
		//		float[] modelScale = null;
		//	
		//		//端末のディスプレイサイズ毎に３ＤＣＧのスケールを変化
		//		
		//        if(width == 480 & height == 800) {
		//        	 modelScale = new float[] {0.01f, 0.01f};     
		//        } else if(width == 800 && height == 480) {
		//        	 modelScale = new float[] {0.01f, 0.01f};
		//		} else if(width == 1280 && height == 736) {
		//        	 modelScale = new float[] {0.01f, 0.01f};
		//		}
		//        
		//		mRenderer = new ModelRenderer(getAssets(), modelName, modelScale);
		//		mRenderer.setMainHandler(mHandler);

		///////// Min3d //////////
		// Renderer of min3d
		//		_initSceneHander = new Handler();
		//		_updateSceneHander = new Handler();

		//
		// These 4 lines are important.
		//
		//		Shared.context(this);
		//		scene = new Scene(this);
		//		scene.backgroundTransparent(true);
		//		mRenderer = new Renderer(scene);
		//		Shared.renderer(mRenderer);
		///////// Min3d //////////

		requestWindowFeature(Window.FEATURE_PROGRESS);

		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mSurfaceView.setKeepScreenOn(true);

		// don't set mSurfaceHolder here. We have it set ONLY within
		// surfaceChanged / surfaceDestroyed, other parts of the code
		// assume that when it is set, the surface is also set.
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void changeGLSurfaceViewState() {
		// If the camera resumes behind the lock screen, the orientation
		// will be portrait. That causes OOM when we try to allocation GPU
		// memory for the GLSurfaceView again when the orientation changes. So,
		// we delayed initialization of GLSurfaceView until the orientation
		// becomes landscape.
		Configuration config = getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE
				&& !mPausing && mFirstTimeInitialized) {
			if (mGLSurfaceView == null) initializeGLSurfaceView();
		} else if (mGLSurfaceView != null) {
			finalizeGLSurfaceView();
		}
	}

	private void initializeGLSurfaceView() {

		// init ARToolkit.
		if (arToolkitDrawer == null) {
			InputStream camePara = getResources().openRawResource(R.raw.camera_para);
			int[] width = new int[2];
			for (int i = 0; i < 2; i++) {
				width[i] = 80;
			}
			ArrayList<InputStream> patt = new ArrayList<InputStream>();
			patt.add(getResources().openRawResource(R.raw.patthiro));
			patt.add(getResources().openRawResource(R.raw.pattkanji));
			arToolkitDrawer = new ARToolkitDrawer(camePara, width, patt, mRenderer);


			//			mMediaPlayer = MediaPlayer.create(this, R.raw.miku_voice);
			//			mMediaPlayer.setLooping(true);
			//	        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			//	            public void onPrepared(MediaPlayer mediaplayer) {
			//	    			arToolkitDrawer.setMediaPlayer(mediaplayer);
			//	            }
			//	        });
		}

		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mGLSurfaceView.setZOrderOnTop(true);
		mGLSurfaceView.setRenderer(mRenderer);
		frame.addView(mGLSurfaceView);
	}

	private void finalizeGLSurfaceView() {
		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		frame.removeView(mGLSurfaceView);
		mGLSurfaceView = null;

		if (mMediaPlayer != null)
			mMediaPlayer.release();
		mMediaPlayer = null;

		arToolkitDrawer = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		;;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		mPausing = false;

		// Start the preview if it is not started.
		if (!mPreviewing && !mStartPreviewFail && (mSurfaceHolder != null)) {
			try {
				startPreview();
			} catch (Exception e) {
				showCameraErrorAndFinish();
				return;
			}
		}

		if (mSurfaceHolder != null) {
			// If first time initialization is not finished, put it in the
			// message queue.
			if (!mFirstTimeInitialized) {
				mHandler.sendEmptyMessage(FIRST_TIME_INIT);
			} else {
				initializeSecondTime();
			}
		}
		keepScreenOnAwhile();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		mPausing = true;
		stopPreview();
		// Close the camera now because other activities may need to use it.
		closeCamera();
		resetScreenOn();
		changeGLSurfaceViewState();

		if (mFirstTimeInitialized) {
			mOrientationListener.disable();
		}

		// Remove the messages in the event queue.
		mHandler.removeMessages(RESTART_PREVIEW);
		mHandler.removeMessages(FIRST_TIME_INIT);

		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CROP_MSG: {
			Intent intent = new Intent();
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					intent.putExtras(extras);
				}
			}
			setResult(resultCode, intent);
			finish();
			break;
		}
		}
	}

	//	@Override
	//	public boolean onTouchEvent(MotionEvent event) {
	//		switch (event.getAction()) {
	//			case MotionEvent.ACTION_DOWN:
	//				break;
	//
	//			case MotionEvent.ACTION_MOVE:
	//				break;
	//
	//			case MotionEvent.ACTION_UP:
	//				break;
	//		}
	//		return true;
	//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			float dX = lastX - event.getX();
			float dY = lastY - event.getY();
			lastX = event.getX();
			lastY = event.getY();
			if(mRenderer != null) {
				switch(mode) {
				case MENU_SCALE:
					mRenderer.setScale(dY/500.0f);
					break;
				case MENU_ROTATE:
					mRenderer.setXrot(0.2f*-dY);
					mRenderer.setYrot(0.2f*-dX);
					break;
				case MENU_TRANSLATE:
					mRenderer.setXpos(-dX/100.0f);
					mRenderer.setYpos(dY/100.0f);
					break;
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			break;
		}
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.d(TAG, "surfaceChanged");

		// Make sure we have a surface in the holder before proceeding.
		if (holder.getSurface() == null) {
			Log.d(TAG, "holder.getSurface() == null");
			return;
		}

		// We need to save the holder for later use, even when the mCameraDevice
		// is null. This could happen if onResume() is invoked after this
		// function.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder = holder;

		// The mCameraDevice will be null if it fails to connect to the camera
		// hardware. In this case we will show a dialog and then finish the
		// activity, so it's OK to ignore it.
		if (mCameraDevice == null) {

			/*
			 * To reduce startup time, we start the preview in another thread.
			 * We make sure the preview is started at the end of surfaceChanged.
			 */
			Thread startPreviewThread = new Thread(new Runnable() {
				public void run() {
					try {
						mStartPreviewFail = false;
						startPreview();
					} catch (Exception e) {
						// In eng build, we throw the exception so that test tool
						// can detect it and report it
						if ("eng".equals(Build.TYPE)) {
							throw new RuntimeException(e);
						}
						mStartPreviewFail = true;
					}
				}
			});
			startPreviewThread.start();

			// Make sure preview is started.
			try {
				startPreviewThread.join();
				if (mStartPreviewFail) {
					showCameraErrorAndFinish();
					return;
				}
			} catch (InterruptedException ex) {
				// ignore
			}
		}

		// Sometimes surfaceChanged is called after onPause.
		// Ignore it.
		if (mPausing || isFinishing()) return;

		// If first time initialization is not finished, send a message to do
		// it later. We want to finish surfaceChanged as soon as possible to let
		// user see preview first.
		if (!mFirstTimeInitialized) {
			mHandler.sendEmptyMessage(FIRST_TIME_INIT);
		} else {
			initializeSecondTime();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPreview();
		mSurfaceHolder = null;
	}

	private void closeCamera() {
		if (mCameraDevice != null) {
			CameraHolder.instance().release();
			mCameraDevice = null;
			mPreviewing = false;
		}
	}

	private void ensureCameraDevice() throws CameraHardwareException {
		if (mCameraDevice == null) {
			mCameraDevice = CameraHolder.instance().open();
		}
	}

	private void showCameraErrorAndFinish() {
		Resources ress = getResources();
		com.android.camera.Util.showFatalErrorAndFinish(NyARToolkitAndroidActivity.this,
				ress.getString(R.string.camera_error_title),
				ress.getString(R.string.cannot_connect_camera));
	}

	public void restartPreview() {
		Log.d(TAG, "restartPreview");
		try {
			startPreview();
		} catch (CameraHardwareException e) {
			showCameraErrorAndFinish();
			return;
		}
	}

	private void setPreviewDisplay(SurfaceHolder holder) {
		try {
			mCameraDevice.setPreviewDisplay(holder);
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("setPreviewDisplay failed", ex);
		}
	}

	private void startPreview() throws CameraHardwareException {
		if (mPausing || isFinishing()) return;

		ensureCameraDevice();

		// If we're previewing already, stop the preview first (this will blank
		// the screen).
		// FIXME: don't stop for avoiding blank screen.
		//        if (mPreviewing) stopPreview();

		setPreviewDisplay(mSurfaceHolder);
		if (!mPreviewing)
			setCameraParameters();

		mCameraDevice.setOneShotPreviewCallback(mPreviewCallback);

		try {
			Log.v(TAG, "startPreview");
			mCameraDevice.startPreview();
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("startPreview failed", ex);
		}
		mPreviewing = true;
		mStatus = IDLE;
	}

	private void stopPreview() {
		if (mCameraDevice != null && mPreviewing) {
			Log.v(TAG, "stopPreview");
			mCameraDevice.setOneShotPreviewCallback(null);
			mCameraDevice.stopPreview();
		}
		mPreviewing = false;
	}

	private void setCameraParameters() {
		mParameters = mCameraDevice.getParameters();

		mParameters.setPreviewSize(320, 240);

		mCameraDevice.setParameters(mParameters);
	}

	private void resetScreenOn() {
		mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void keepScreenOnAwhile() {
		mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
	}

	/**
	 * Toastを呼ぶ
	 * @param msg
	 */
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}


	// シャッターが押されたときに呼ばれるコールバック
	private Camera.ShutterCallback mShutterListener =
			new Camera.ShutterCallback() {
		public void onShutter() {
			//特に何もしない
			Log.e("debug","start Shuttercallback");
		}
	};

	// JPEGイメージ生成後に呼ばれるコールバック
	private Camera.PictureCallback mPictureListener =
			new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.e("debug","start pucturecallback");
			Bitmap screen = null,capture = null;
			// Rendererのスクリーンショットができるまで待機
			while(true){
				if(mRenderer.screenshot != null) break;
			}
			// GLのスクリーンショット取得
			screen = mRenderer.getScreen();
			Log.e("debug","screen get");

			if (data != null) {
				try {
					// 作成するBitmapのサイズを変更(4分の1？)
					BitmapFactory.Options options = new BitmapFactory.Options();  
					options.inSampleSize = 4;
					// カメラ画像のBitmap作成
					capture = BitmapFactory.decodeByteArray(data, 0, data.length,options);
					Log.e("debug","capture get");
					// 2枚のBitmapを重ね合わせる
					conposit(capture,screen);
					showToast("save");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.e("debug","startPreview");
			// カメラのプレビュー再開
			restartPreview();
		}
	};
	
	/**
	 * スクリーンショットをとる
	 * 
	 * @author s0921122
	 * 
	 */
	private void shot(){
		/*
		File file = new File("/sdcard/xxx.jpg");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		Log.e("debug","takescreen = true");
		mRenderer.takeScreen = true;
		Log.e("debug","takepucpure start");
		// 写真を撮る
		stopPreview();// エラーが出るなら止めればいいじゃない
		mCameraDevice.takePicture(mShutterListener, null, mPictureListener);
		Log.e("debug","takepicture end");
	}

	/**
	 * Bitmapを重ねる
	 * 
	 * @param image
	 * @param frame
	 * @return 
	 */
	private boolean conposit(Bitmap image, Bitmap frame) {
		Log.e("debug","overray");
		//ARGB_8888,RGB_565,ARGB_4444
		int width = image.getWidth();
		int height = image.getHeight();
		
		Log.e("debug","createbitmap");
		// 合成するための下地
		Bitmap newBitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);
		
		Log.e("debug","canvas");
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(image, 0, 0, (Paint)null);
		canvas.drawBitmap(frame, 0, 0, (Paint)null);
		Log.e("debug","recycle");
		image.recycle();
		frame.recycle();

		FileOutputStream fos;
		try {
			Log.e("debug","output");
			// ファイルに保存
			String outpath = "/sdcard/ARBrowser" + new Date().getTime() + ".jpg";
			fos = new FileOutputStream(outpath);
			newBitmap.compress(CompressFormat.JPEG, 100, fos);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	//----------------------- for min3d ------------------------
	public Scene scene;

	protected Handler _initSceneHander;
	protected Handler _updateSceneHander;

	final Runnable _initSceneRunnable = new Runnable()
	{
		public void run() {
			onInitScene();
		}
	};

	final Runnable _updateSceneRunnable = new Runnable()
	{
		public void run() {
			onUpdateScene();
		}
	};

	/**
	 * Instantiation of Object3D's, setting their properties, and adding Object3D's
	 * to the scene should be done here. Or any point thereafter.
	 *
	 * Note that this method is always called after GLCanvas is created, which occurs
	 * not only on Activity.onCreate(), but on Activity.onResume() as well.
	 * It is the user's responsibility to build the logic to restore state on-resume.
	 *
	 * @see min3d.interfaces.ISceneController#initScene()
	 */
	public void initScene()
	{
		scene.lights().add(new Light());
		scene.camera().frustum.zFar(10000.0f);
		//scene.camera().frustum.shortSideLength(0.77f);

		IParser parser;
		AnimationObject3d animationObject3d = null;

		parser = Parser.createParser(Parser.Type.MD2,
				getResources(), "jp.androidgroup.nyartoolkit:raw/droid", false);
		parser.parse();

		animationObject3d = parser.getParsedAnimationObject();
		animationObject3d.rotation().z = -90.0f;
		animationObject3d.scale().x = animationObject3d.scale().y = animationObject3d.scale().z = 1.0f;
		scene.addChild(animationObject3d);
		animationObject3d.setFps(30);

		parser = Parser.createParser(Parser.Type.MD2,
				getResources(), "jp.androidgroup.nyartoolkit:raw/droidr", false);
		parser.parse();

		animationObject3d = parser.getParsedAnimationObject();
		animationObject3d.rotation().z = -90.0f;
		animationObject3d.scale().x = animationObject3d.scale().y = animationObject3d.scale().z = 1.0f;
		scene.addChild(animationObject3d);
		animationObject3d.setFps(90);
	}

	/**
	 * All manipulation of scene and Object3D instance properties should go here.
	 * Gets called on every frame, right before drawing.
	 *
	 * @see min3d.interfaces.ISceneController#updateScene()
	 */
	public void updateScene()
	{
	}

	/**
	 * Called _after_ scene init (ie, after initScene).
	 * Unlike initScene(), is thread-safe.
	 */
	public void onInitScene()
	{
	}

	/**
	 * Called _after_ scene init (ie, after initScene).
	 * Unlike initScene(), is thread-safe.
	 */
	public void onUpdateScene()
	{
	}

	/**
	 * @see min3d.interfaces.ISceneController#getInitSceneHandler()
	 */
	public Handler getInitSceneHandler()
	{
		return _initSceneHander;
	}

	/**
	 * @see min3d.interfaces.ISceneController#getUpdateSceneHandler()
	 */
	public Handler getUpdateSceneHandler()
	{
		return _updateSceneHander;
	}

	/**
	 * @see min3d.interfaces.ISceneController#getInitSceneRunnable()
	 */
	public Runnable getInitSceneRunnable()
	{
		return _initSceneRunnable;
	}

	/**
	 * @see min3d.interfaces.ISceneController#getUpdateSceneRunnable()
	 */
	public Runnable getUpdateSceneRunnable()
	{
		return _updateSceneRunnable;
	}
	//----------------------- for min3d ------------------------
}

