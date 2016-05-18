package com.kiwihealthcare.bpppgcollector.view;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.SurfaceView;

public class WaveDraw {
	static String TAG = "WaveDraw";
	private ArrayList<Float> inBuf = new ArrayList<Float>();
	public boolean isDrawing = false;
	int num = 100;

	public void start(SurfaceView sfv, Paint paint, float xLen, float yLen) {
		isDrawing = true;
		new DrawThread(sfv, paint, num, xLen, yLen).start();
	}

	public void restart() {
		inBuf.clear();
	}

	public void stop() {
		isDrawing = false;
		inBuf.clear();
	}

	public void clear(SurfaceView sfv) {
		Canvas c = sfv.getHolder().lockCanvas();
		c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		sfv.getHolder().unlockCanvasAndPost(c);
	}

	public void add(float point) {
		synchronized (inBuf) {
			inBuf.add(point);
			// Log.i(TAG,"add buf size:"+inBuf.size());
		}
	}

	class DrawThread extends Thread {
		SurfaceView sfv;
		Paint mPaint;
		float xLen;
		float yLen;
		int num;
		float[] y_buf; // 长度同num，
		int y_buf_start = 0; // 从buf中的哪一点开始画

		public DrawThread(SurfaceView sfv, Paint paint, int num, float xLen,
				float yLen) {
			this.sfv = sfv;
			this.mPaint = paint;
			this.num = num;
			this.xLen = xLen;
			this.yLen = yLen;
			y_buf = new float[num];
		}

		public void run() {
			while (isDrawing) {
				try {
					sleep(100);
					synchronized (inBuf) {
						if (inBuf.size() == 0)
							continue;
						for (int i = 0; i < inBuf.size(); i++) {
							y_buf[y_buf_start] = inBuf.get(i);
							y_buf_start = (y_buf_start + 1) % num;
						}
						inBuf.clear();
					}
					simpleDraw(y_buf);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			clear();
		}

		public void clear() {
			if (sfv != null) {
				Canvas c = sfv.getHolder().lockCanvas();
				try {
					c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				} catch (Exception e) {
					Log.i("draw_Data", e.getMessage());
				} finally {
					sfv.getHolder().unlockCanvasAndPost(c);
				}
			}

		}

		public void simpleDraw(float[] data) {
			Canvas c = sfv.getHolder().lockCanvas();
			try {
				c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				float min = getMin(data);
				float max = getMax(data);
				float xgap = xLen / num;
				if (min == max) {
					c.drawLine(0, yLen / 2, xgap * num, yLen / 2, mPaint);
					return;
				}
				final float yrate = yLen * 4 / (5 * (max - min)); // 最大0.8*yLen
				float xval = xLen;

				for (int i = num - 1; i > 0; i--) {
					float y1 = (y_buf[(y_buf_start + i) % num] - min) * yrate
							+ yLen / 10;
					float y2 = (y_buf[(y_buf_start + i - 1) % num] - min)
							* yrate + yLen / 10;
					c.drawLine(xval, y1, xval - xgap, y2, mPaint);
					// this.chartPath.lineTo(xval - xgap, y2);
					xval = xval - xgap;
				}
				// c.drawPath(this.chartPath, mPaint); //通过path画图
			} catch (Exception e) {
				Log.i("draw_Data", e.getMessage());
			} finally {
				sfv.getHolder().unlockCanvasAndPost(c);
			}
		}

		public float getMin(float data[]) {
			float min = 1000;
			int len = data.length;
			for (int i = 0; i < len; i++) {
				if (data[i] < min)
					min = data[i];
			}
			return min;
		}

		public float getMax(float data[]) {
			float max = -1000;
			int len = data.length;
			for (int i = 0; i < len; i++) {
				if (data[i] > max)
					max = data[i];
			}
			return max;
		}
	}
}
