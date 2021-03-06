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
 *  noritsuna
 */

package jp.androidgroup.nyartoolkit;

import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.nyatla.kGLModel.KGLException;
import jp.nyatla.kGLModel.KGLModelData;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Rendering 3DModels.
 *
 * @author noritsuna
 *
 */
public class ModelRenderer implements GLSurfaceView.Renderer
{

	private static final int PATT_MAX = 2;
	private static final int MARKER_MAX = 8;

	private int found_markers;
	private int [] ar_code_index = new int[MARKER_MAX];
	private float [][] resultf = new float[MARKER_MAX][16];
	private float [] cameraRHf = new float[16];
	private boolean useRHfp = false;

	private boolean drawp = false;

	// Rotation
	public float xrot = 0;
    public float yrot = 0;
    public float zrot = 0;
    public float xpos = 0;
    public float ypos = 0;
    public float zpos = 0;
    
    // Initialize 3DCG scale
    public float scale = 2f;
    
    public static final int STATE_DYNAMIC = 0;
    public static final int STATE_FINALIZED = 1;
    public int STATE = STATE_DYNAMIC;
    
	// metaseq
	private KGLModelData[] model = new KGLModelData[PATT_MAX];
	private AssetManager am;
	private String[] modelName = new String[PATT_MAX];
	private float[] modelScale = new float[PATT_MAX];
	
	public int mWidth;
	public int mHeight;

	public boolean takeScreen;
	Bitmap screenshot;

	public ModelRenderer(AssetManager am, String[] modelName, float[] modelScale) {
        this.am = am;
		cameraReset();
		for (int i = 0; i < PATT_MAX; i++) {
	        this.modelName[i] = modelName[i];
			this.modelScale[i] = modelScale[i];
			Log.e("ERROR","Loop on "+i);
		}
    }

	//---
	public void setScale(float f) {
		this.scale += f;
		if(this.scale < 0.0001f)
			this.scale = 0.0001f;
	}

	public void setXrot(float dY) {
		this.xrot += dY;
	}

	public void setYrot(float dX) {
		this.yrot += dX;
	}

	public void setXpos(float f) {
		this.xpos += f;
	}

	public void setYpos(float f) {
		this.ypos += f;
	}
	//---

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
         gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
				  GL10.GL_FASTEST);

        gl.glClearColor(0,0,0,0);

		initModel(gl);

		cameraChangep = true;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;

		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done
		 * each time we draw, but usually a new projection needs to
		 * be set when the viewport is resized.
		 */
		ratio = (float) width / height;

		cameraChangep = true;
    }

    public void onDrawFrame(GL10 gl) {
		/*
		 * Usually, the first thing one might want to do is to clear
		 * the screen. The most efficient way of doing this is to use
		 * glClear().
		 */

		//gl.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (drawp) {
			// camera
			if (useRHfp) {
				gl.glMatrixMode(GL10.GL_PROJECTION);
				gl.glLoadMatrixf(cameraRHf, 0);
			} else if (cameraChangep) {
				cameraSetup(gl);
			}

			// FIXME: Draw detected marker only.
 			for (int i = 0; i < MARKER_MAX; i++) {
 				gl.glMatrixMode(GL10.GL_MODELVIEW);
 				if (useRHfp) {
 					gl.glLoadMatrixf(resultf[i], 0);
 					// 位置調整
// 					gl.glTranslatef(0.0f, 0.0f, 0.0f);
 					gl.glTranslatef(this.xpos, this.ypos, this.zpos);
 					// OpenGL座標系→ARToolkit座標系
// 					gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
 					gl.glRotatef(this.xrot, 1.0f,0.0f,0.0f);
 					gl.glRotatef(this.yrot, 0.0f,1.0f,0.0f);
 					gl.glRotatef(this.zrot, 0.0f,0.0f,1.0f);
 					
 					gl.glScalef(this.scale, this.scale, this.scale);
 				} else {
 					gl.glLoadIdentity();
 				}
 				Log.d("ModelRenderer", "onDrawFrame: " + i + ",model: "+ ar_code_index[i]);
 				if (lightp)
 					lightSetup(gl);
 				model[ar_code_index[i]].enables(gl, 1.0f);
 				model[ar_code_index[i]].draw(gl);
 				model[ar_code_index[i]].disables(gl);
 				if (lightp)
 					lightCleanup(gl);
 			}
		} else {
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadMatrixf(cameraRHf, 0);
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}
		// スクリーンショット
		// よくわかってないけど取れる
		if(takeScreen){
			takeScreen = false;
			Log.e("debug","takescreen false");
			int takeWidth = mWidth;
			int takeHeight = mHeight;
			int[] tmp = new int[takeHeight*takeWidth];
			int[] screenshot = new int[takeHeight*takeWidth];
			Buffer screenshotBuffer = IntBuffer.wrap(tmp);
			screenshotBuffer.position(0);
			gl.glReadPixels(0,0,takeWidth,takeHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, screenshotBuffer); 
			for(int i=0; i<takeHeight; i++) 
			{
				//remember, that OpenGL bitmap is incompatible with Android bitmap 
				//and so, some correction need.      
				for(int j=0; j<takeWidth; j++) 
				{ 
					int pix=tmp[i*takeWidth+j]; 
					int pb=(pix>>16)&0xff; 
					int pr=(pix<<16)&0x00ff0000; 
					int pix1=(pix&0xff00ff00) | pr | pb; 
					screenshot[(takeHeight-i-1)*takeWidth+j]=pix1; 
				} 
			} 
			// アルファありのBitmapを作成する
            this.screenshot = Bitmap.createBitmap(screenshot, takeWidth, takeHeight, Config.ARGB_8888);
		}
		makeFramerate();
    }

    public Bitmap getScreen(){
    	return screenshot;
    }
    
	private Handler mainHandler;

	public void setMainHandler(Handler handler) {
		mainHandler = handler;
	}

	public void initModel(GL10 gl) {
		if (mainHandler != null) {
			mainHandler.sendMessage
				(mainHandler.obtainMessage
						(NyARToolkitAndroidActivity.SHOW_LOADING));
		}
		Log.d("ModelRenderer", "initModel");
		for (int i = 0; i < PATT_MAX; i++) {
			if (model[i] != null) {
				model[i].Clear(gl);
				model[i] = null;
			}
			if (modelName[i] != null) {
				try {
					model[i] = KGLModelData.createGLModel
						(gl, null, am, modelName[i], modelScale[i]);

				} catch (KGLException e) {
					Log.e("ModelRenderer", "KGLModelData error", e);
				}
			}
		}
		if (mainHandler != null) {
			mainHandler.sendMessage
				(mainHandler.obtainMessage
						(NyARToolkitAndroidActivity.HIDE_LOADING));
		}
	}

	public void objectClear() {
		drawp = false;
	}


	public float [] zoomV = new float[4];
	public float [] upOriV = { 0.0f, 1.0f, 0.0f, 0.0f };
	public float [] lookV = new float[4];
	public float [] camRmtx = new float[16];

	public float [] camV = new float[4];
	public float [] upV = new float[4];
	public float ratio;

	// Temporary
	private float [] mtx = new float[16];

	private boolean cameraChangep;

	private void cameraSetup(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// gl.glFrustumf(-ratio, ratio, -1, 1, 1, 1000);
		GLU.gluPerspective(gl, 45, ratio, 1.0f, 10000.0f);
		GLU.gluLookAt(gl,
					  camV[0], camV[1], camV[2],
					  lookV[0], lookV[1], lookV[2],
					  upV[0], upV[1], upV[2]);
		cameraChangep = false;
	}

	private void cameraMake() {
		Matrix.setIdentityM(mtx, 0);
		Matrix.translateM(mtx, 0, lookV[0], lookV[1], lookV[2]);
		Matrix.multiplyMM(mtx, 0, camRmtx, 0, mtx, 0);
		Matrix.multiplyMV(camV, 0, mtx, 0, zoomV, 0);
		Matrix.multiplyMV(upV, 0, camRmtx, 0, upOriV, 0);
		cameraChangep = true;
	}

	public void cameraReset() {
		zoomV[0] = zoomV[1] = camV[0] = camV[1] = 0.0f;
		zoomV[2] = camV[2] = -500.0f;
		lookV[0] = lookV[1] = lookV[2] = 0.0f;
		upV[0] = upV[2] = 0.0f;
		upV[1] = 1.0f;
		Matrix.setIdentityM(camRmtx, 0);
		cameraChangep = true;
	}

	public void cameraRotate(float rot, float x, float y, float z,
							 float [] sMtx) {
		float [] vec = { x, y, z, 0 };
		Matrix.setIdentityM(mtx, 0);
		Matrix.rotateM(mtx, 0, rot, vec[0], vec[1], vec[2]);
		Matrix.multiplyMM(camRmtx, 0, sMtx, 0, mtx, 0);
		cameraMake();
	}

	public void cameraZoom(float z) {
		zoomV[2] += z;
		cameraMake();
	}

	public void cameraMove(float x, float y, float z) {
		float [] vec = { x, y, z, 0 };
		Matrix.multiplyMV(vec, 0, camRmtx, 0, vec, 0);
		for (int i = 0; i < 3; i++) {
			lookV[i] += vec[i];
		}
		cameraMake();
	}


    public void objectPointChanged(int found_markers, int [] ar_code_index, float[][] resultf,
			   float[] cameraRHf) {
		synchronized (this) {
			this.found_markers = found_markers;
			for (int i = 0; i < MARKER_MAX; i++) {
				this.ar_code_index[i] = ar_code_index[i];
				System.arraycopy(resultf[i], 0, this.resultf[i], 0, 16);
			}
			System.arraycopy(cameraRHf, 0, this.cameraRHf, 0, 16);
		}
		useRHfp = true;
		drawp = true;
    }

	// Light
	public boolean lightCamp = false;
	public boolean lightp = true;
	public boolean speLightp = false;

	float[] lightPos0 = { 1000, 1000, 1000, 0 };
	float[] lightPos1 = { 1000, 1000, 1000, 0 };
	float[] lightPos2 = { 1000, 1000, 1000, 0 };
	float[] lightDif = { 0.6f, 0.6f, 0.6f, 1 };
	float[] lightSpe = { 1.0f, 1.0f, 1.0f, 1 };
	float[] lightAmb = { 0.01f, 0.01f, 0.01f, 1 };

	private void lightSetup(GL10 gl) {
		if (lightCamp) {
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, camV, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDif, 0);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, camV, 0);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, lightAmb, 0);
			if (speLightp) {
				gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, camV, 0);
				gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_SPECULAR, lightSpe, 0);
			}
		} else {
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos0, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDif, 0);
			gl.glLightfv(GL10.GL_LIGHT1,GL10.GL_POSITION, lightPos2, 0);
			gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, lightAmb, 0);
			if (speLightp) {
				gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, lightPos1, 0);
				gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_SPECULAR, lightSpe, 0);
			}
		}
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHT1);
		if (speLightp)
			gl.glEnable(GL10.GL_LIGHT2);
	}

	private void lightCleanup(GL10 gl) {
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_LIGHT0);
		gl.glDisable(GL10.GL_LIGHT1);
		gl.glDisable(GL10.GL_LIGHT2);
	}

    private int mFrames = 0;
    private float mFramerate;
    private long mStartTime;

	public float getFramerate() {
		return mFramerate;
	}
	public float getStartTime() {
		return mStartTime;
	}
    private void makeFramerate() {
        long time = SystemClock.uptimeMillis();

		synchronized (this) {
			mFrames++;
			if (mStartTime == 0) {
				mStartTime = time;
			}
			if (time - mStartTime >= 1) {
				mFramerate = (float)(1000 * mFrames)
					/ (float)(time - mStartTime);
				Log.d("ModelRenderer", "Framerate: " + mFramerate + " (" + (time - mStartTime) + "ms)");
				mFrames = 0;
				mStartTime = time;
			}
		}
    }
    
    public boolean isObjectClear () {
    	return drawp;
    }
}
