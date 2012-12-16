package aau.med3.assassin;


import android.graphics.Color;

public class ColorTracker {
	

	static public class Centroid {
		public int x = 0, y = 0, n = 0;
		public float hue = 0.f;
		public int threshold;
		
		public Centroid(float hue, int threshold){
			this.hue = hue;
			this.threshold = threshold;
		}
		
		public void clear(){
			x = y = n = 0;
		}
	}
	
	
	
	public Centroid[] centroids;
	
	public float[] hsv = new float[3];
	
	public ColorTracker(){
		
		
	}
	
	public void trackColors(int[] rgba, int width, int height){
		int numColors = centroids.length;
		for(int c = 0; c < numColors; c++){
			centroids[c].clear();
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int i = y*width+x;
				int col = rgba[i];
				Color.RGBToHSV(Color.red(col), Color.green(col), Color.blue(col), hsv);
				if(hsv[1] > 0.3f && hsv[2] > 0.3f){
					for(int c = 0; c < numColors; c++){
						float hue = centroids[c].hue;
						int threshold = centroids[c].threshold;
						if(hsv[0] > (hue - threshold) && hsv[0] < (hue + threshold)){
							centroids[c].x += x;
							centroids[c].y += y;
							centroids[c].n++;
						}
					}
					
				}
			}
		}
		for(int c = 0; c < numColors; c++){
			if(centroids[c].n < 1) continue;
			centroids[c].x /= centroids[c].n;
			centroids[c].y /= centroids[c].n;
		}
		
	}
	
}
