#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libimageprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static int rgb_clamp(int value) {
  if(value > 255) {
    return 255;
  }
  if(value < 0) {
    return 0;
  }
  return value;
}
static void brightness(AndroidBitmapInfo* info, void* pixels, float brightnessValue){
	int xx, yy, red, green, blue;
	uint32_t* line;

	for(yy = 0; yy < info->height; yy++){
			line = (uint32_t*)pixels;
			for(xx =0; xx < info->width; xx++){

			  //extract the RGB values from the pixel
				red = (int) ((line[xx] & 0x00FF0000) >> 16);
				green = (int)((line[xx] & 0x0000FF00) >> 8);
				blue = (int) (line[xx] & 0x00000FF );

        //manipulate each value
        red = rgb_clamp((int)(red * brightnessValue));
        green = rgb_clamp((int)(green * brightnessValue));
        blue = rgb_clamp((int)(blue * brightnessValue));

        // set the new pixel back in
        line[xx] =
          ((red << 16) & 0x00FF0000) |
          ((green << 8) & 0x0000FF00) |
          (blue & 0x000000FF);
			}

			pixels = (char*)pixels + info->stride;
		}
}
JNIEXPORT void JNICALL Java_com_example_ImageActivity_brightness(JNIEnv * env, jobject  obj, jobject bitmap, jfloat brightnessValue)
{

    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
            LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
            return;
        }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    brightness(&info,pixels, brightnessValue);

    AndroidBitmap_unlockPixels(env, bitmap);
}



static int getRed(int in){
	return ((in >> 16) & 0xff);
}

static int getGreen(int in){
	return ((in >> 8) & 0xff);
}

static int getBlue(int in){
	return (in & 0xff);
}

static void decodeYUV(jbyte* yuv, jint* rgb, int width, int height){
	const int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) { // if i is dividable by two
					v = (0xff & yuv[uvp++]) - 128;
					u = (0xff & yuv[uvp++]) - 128;
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

static void makeGreyscale(jint* rgb, int width, int height){
	int r, g, b;
	for(int y = 0; y++; y < height){
		for(int x = 0; x++; x < width){
			int i = y*width+x;
			rgb[i] = 0;
			continue;
			r = getRed(rgb[i]);
			g = getGreen(rgb[i]);
			b = getBlue(rgb[i]);
		}
	}
}

/*
JNIEXPORT void JNICALL Java_aau_med3_assassin_activities_DrawOnTop_decodeYUV(JNIEnv * env, jobject obj, jintArray rgb, jbyteArray yuv420sp, jint width, jint height)
{
		int* rgbData;
		int rgbDataSize = 0;
    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jbyte* yuv = yuv420sp;
    if(rgbDataSize < sz) {
        int tmp[sz];
        rgbData = &tmp[0];
        rgbDataSize = sz;
        __android_log_write(ANDROID_LOG_INFO, "JNI", "alloc");
    }

    for(j = 0; j < h; j++) {
             pixPtr = j * w;
             jDiv2 = j >> 1;
             for(i = 0; i < w; i++) {
                     Y = yuv[pixPtr];
                     if(Y < 0) Y += 255;
                     if((i & 0x1) != 1) {
                             cOff = sz + jDiv2 * w + (i >> 1) * 2;
                             Cb = yuv[cOff];
                             if(Cb < 0) Cb += 127; else Cb -= 128;
                             Cr = yuv[cOff + 1];
                             if(Cr < 0) Cr += 127; else Cr -= 128;
                     }
                     R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                     if(R < 0) R = 0; else if(R > 255) R = 255;
                     G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                     if(G < 0) G = 0; else if(G > 255) G = 255;
                     B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                     if(B < 0) B = 0; else if(B > 255) B = 255;
                     // rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
										 rgbData[pixPtr++] = 0xffffffff;
             }
    }
    (*env)->SetIntArrayRegion(env, rgb, 0, sz, ( jint * ) &rgbData[0] );
}

/*
JNIEXPORT void JNICALL Java_aau_med3_assassin_activities_DrawOnTop_decodeYUV(JNIEnv* env, jobject obj, jbyte* yuv, jint* rgb, jint width, jint height){
	LOGI("decodeYUV called from JNI!");
	decodeYUV(yuv, rgb, width, height);
	// makeGreyscale(rgb, width, height);
}
*/

