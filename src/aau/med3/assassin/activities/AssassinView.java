package aau.med3.assassin.activities;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

// ----------------------------------------------------------------------

public class AssassinView extends Activity {
	private Preview mPreview;
	private DrawOnTop mDrawOnTop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hide the window title.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create our Preview view and set it as the content of our activity.
		// Create our DrawOnTop view.
		mDrawOnTop = new DrawOnTop(this);
		mPreview = new Preview(this, mDrawOnTop);
		setContentView(mPreview);
		addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	
	
}

// ----------------------------------------------------------------------



// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	DrawOnTop mDrawOnTop;
	boolean mFinished;

	Preview(Context context, DrawOnTop drawOnTop) {
		super(context);

		mDrawOnTop = drawOnTop;
		mFinished = false;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);

			// Preview callback used whenever new viewfinder frame is available
			mCamera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					if ((mDrawOnTop == null) || mFinished)
						return;

					if (mDrawOnTop.mBitmap == null) {
						// Initialize the draw-on-top companion
						Camera.Parameters params = camera.getParameters();
						mDrawOnTop.mImageWidth = params.getPreviewSize().width;
						mDrawOnTop.mImageHeight = params.getPreviewSize().height;
						mDrawOnTop.mBitmap = Bitmap.createBitmap(
								mDrawOnTop.mImageWidth,
								mDrawOnTop.mImageHeight, Bitmap.Config.ARGB_8888);
						mDrawOnTop.mRGB = new int[mDrawOnTop.mImageWidth
								* mDrawOnTop.mImageHeight];
						mDrawOnTop.mYUVData = new byte[data.length];
					}
					
					// Pass YUV data to draw-on-top companion
//					mDrawOnTop.mYUVData = data;
					System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0,
							data.length);
					mDrawOnTop.invalidate();
				}
			});
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mFinished = true;
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
//		parameters.setPreviewFrameRate(15);
//		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

}
