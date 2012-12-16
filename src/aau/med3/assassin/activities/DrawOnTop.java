package aau.med3.assassin.activities;

import aau.med3.assassin.ColorTracker;
import aau.med3.assassin.ColorTracker.Centroid;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

class DrawOnTop extends View {
	Bitmap mBitmap;
	Bitmap mTestBitmap;
	int mTestDim = 100;
	byte[] mYUVData;
	int[] mRGB;
	int[] mTestPixels;
	int mImageWidth, mImageHeight;

	Paint mPaint;
	Rect mRect;
	
	ColorTracker tracker;
	ColorTracker.Centroid[] centroids = new ColorTracker.Centroid[4];
	
	static {
		System.loadLibrary("imageprocessing");
	}
	
	public native void brightness(Bitmap bmp, float brightness);
	public native void decodeYUV(byte[] yuv, int[] rgb, int width, int height);
	
	public DrawOnTop(Context context) {
		super(context);

		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		
		mRect = new Rect(0, 0, 1, 1);
		
		tracker = new ColorTracker();
		centroids[0] = new ColorTracker.Centroid(125, 10); // green;
		centroids[1] = new ColorTracker.Centroid(230, 10); // blue;
		centroids[2] = new ColorTracker.Centroid(0, 10); // red;
		centroids[3] = new ColorTracker.Centroid(60, 10); // yellow;
		
		
		
		tracker.centroids = centroids;
		/*
		mTestBitmap = Bitmap.createBitmap(mTestDim, mTestDim, Bitmap.Config.ARGB_8888);
		
		mTestPixels = new int[mTestDim*mTestDim];
		for(int i = 0; i < mTestDim*mTestDim; i++){
			mTestPixels[i] = 0xff000000;
		}*/
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			int canvasWidth = width;
			int canvasHeight = height;
			int newImageWidth = mTestDim;
			int newImageHeight = mTestDim;
			int marginWidth = (canvasWidth - newImageWidth) / 2;
			
			
			if(mRGB == null){
				return;
			}
			// Convert from YUV to RGB
			decodeYUV420SP(mRGB, mYUVData, mImageWidth, mImageHeight);

			tracker.trackColors(mRGB, width, height);
			ColorTracker.Centroid green = tracker.centroids[0];
			for(int c = 0; c < centroids.length; c++){
				canvas.drawCircle(centroids[c].x, centroids[c].y, 5, mPaint);
			}
			
			/*
			for(int y = 0; y < mTestDim; y++){
				for(int x = 0; x < mTestDim; x++){
					int i = (y*mTestDim) + x;
					int j = (y*mImageWidth) + x;
					int a = getAlpha(mRGB[j]);
					int r = getRed(mRGB[j]);
					int g = getGreen(mRGB[j]);
					int b = getBlue(mRGB[j]);
//					mTestPixels[i] = mRGB[j];
//					mTestPixels[i] = setARGB(a, r, g, b);
					
//					Log.d("DRAW", String.format("r: %d, g: %d, b: %d", r, g, b));
//					if(r > g + 30 && r > b + 30){
//						mRect.set(x, y, x+1, y+1);
//						Log.d("DRAW", "Blue pixel found!");
//						canvas.drawCircle(x, y, 2, mPaint);
//						canvas.drawRect(mRect, mPaint);
//					}
//				}
//			}
			*/
			
			
//			canvas.drawCircle(30, 30, 50, mPaint);
			
			//Draw bitmap
//			mBitmap.setPixels(mBlackPixels, 0, newImageWidth, 0, 0, newImageWidth, newImageHeight);
//			Rect src = new Rect(0, 0, mImageWidth, mImageHeight);
//			Rect dst = new Rect(marginWidth, 0, canvasWidth-marginWidth, canvasHeight);
//			canvas.drawBitmap(mBitmap, null, dst, mPaint);
			
//			mTestBitmap.setPixels(mTestPixels, 0, mTestDim, 0, 0, mTestDim, mTestDim);
//			canvas.drawBitmap(mTestBitmap, 0, 0, mPaint);

//			Draw black borders
//			canvas.drawRect(0, 0, marginWidth, canvasHeight, mPaint);
//			canvas.drawRect(canvasWidth - marginWidth, 0,
//			canvasWidth, canvasHeight, mPaint);

		} // end if statement

		super.onDraw(canvas);

	} // end onDraw method
	
	static public int getRed(int in){
		return ((in >> 16) & 0xff);
	}
	
	static public int getGreen(int in){
		return ((in >> 8) & 0xff);
	}
	
	static public int getBlue(int in){
		return (in & 0xff);
	}
	
	static public int getAlpha(int in){
		return ((in >> 24) & 0xff);
	}
	
	static public int setARGB(int a, int r, int g, int b){
		int ret;
//		ret = 0xff000000 | ((r << 16) & 0x00ff0000)
//				| ((g << 8) & 0x0000ff00)
//				| ((b) & 0x000000ff);
		ret = (a << 24) | (r << 16) | (g << 8) | b;
				
		return ret;
	}
	
	static public int setRGB(int r, int g, int b){
		return setARGB(0xff, r, g, b);
	}
	
	public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
	    final int frameSize = width * height;

	    final int ii = 0;
	    final int ij = 0;
	    final int di = +1;
	    final int dj = +1;

	    int a = 0;
	    for (int i = 0, ci = ii; i < height; ++i, ci += di) {
	        for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
	            int y = (0xff & ((int) yuv[ci * width + cj]));
	            int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
	            int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
	            y = y < 16 ? 16 : y;

	            int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
	            int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	            int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

	            r = r < 0 ? 0 : (r > 255 ? 255 : r);
	            g = g < 0 ? 0 : (g > 255 ? 255 : g);
	            b = b < 0 ? 0 : (b > 255 ? 255 : b);

	            argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
	        }
	    }
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
//				rgb[yp] = setRGB(r, g, b);
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