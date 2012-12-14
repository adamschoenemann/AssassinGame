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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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

class DrawOnTop extends View {
	Bitmap mBitmap;

	byte[] mYUVData;
	int[] mRGB;
	int mImageWidth, mImageHeight;

	Paint mPaint;
	Rect mRect;
	
	public DrawOnTop(Context context) {
		super(context);

		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		
		mRect = new Rect(0, 0, 1, 1);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			

			// Convert from YUV to RGB
			decodeYUV420SP(mRGB, mYUVData, mImageWidth, mImageHeight);
			
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					int i = (y*width) + x;
					int r = getRed(mRGB[i]);
					int g = getBlue(mRGB[i]);
					int b = getGreen(mRGB[i]);
//					Log.d("DRAW", String.format("r: %d, g: %d, b: %d", r, g, b));
					if(r > g + 30 && r > b + 30){
//						mRect.set(x, y, x+1, y+1);
//						Log.d("DRAW", "Blue pixel found!");
						canvas.drawCircle(x, y, 2, mPaint);
//						canvas.drawRect(mRect, mPaint);
					}
				}
			}
			
//			canvas.drawCircle(30, 30, 50, mPaint);

		} // end if statement

		super.onDraw(canvas);

	} // end onDraw method
	
	public int getRed(int in){
		return ((in >> 16) & 0xff);
	}
	
	public int getGreen(int in){
		return ((in >> 8) & 0xff);
	}
	
	public int getBlue(int in){
		return (in & 0xff);
	}
	
	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) { // if i is dividable by two
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	static public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp,
			int width, int height) {
		final int frameSize = width * height;

		for (int pix = 0; pix < frameSize; pix++) {
			int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
			if (pixVal < 0)
				pixVal = 0;
			if (pixVal > 255)
				pixVal = 255;
			rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
		} // pix
	}

	static public void calculateIntensityHistogram(int[] rgb, int[] histogram,
			int width, int height, int component) {
		for (int bin = 0; bin < 256; bin++) {
			histogram[bin] = 0;
		} // bin
		if (component == 0) // red
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = (rgb[pix] >> 16) & 0xff;
				histogram[pixVal]++;
			} // pix
		} else if (component == 1) // green
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = (rgb[pix] >> 8) & 0xff;
				histogram[pixVal]++;
			} // pix
		} else // blue
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = rgb[pix] & 0xff;
				histogram[pixVal]++;
			} // pix
		}
	}
}

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
//						mDrawOnTop.mYUVData = new byte[data.length];
					}
					
					// Pass YUV data to draw-on-top companion
					mDrawOnTop.mYUVData = data;
//					System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0,
//							data.length);
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
//		parameters.setPreviewSize(320, 240);
//		parameters.setPreviewFrameRate(15);
//		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
//		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

}
