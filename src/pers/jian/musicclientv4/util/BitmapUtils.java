package pers.jian.musicclientv4.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pers.jian.musicclientv4.appliacation.MusicApplication;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;

public class BitmapUtils {
	private static final int BUFFER = 1024 * 8;

	/**
	 * ѹ��ͼƬ�㷨, ��ȡ��Ӧ�ߴ��ͼƬ
	 * 
	 * @param is
	 *            ������
	 * @param width
	 *            Ŀ����
	 * @param height
	 *            Ŀ��߶�
	 * @return bitmap
	 * @throws IOException
	 */
	public static Bitmap loadBitmap(InputStream is, int width, int height)
			throws IOException {
		// ��is����, ������t��ȡ��byte[]��
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// ���������е����ݶ�ȡ��baos��
		byte[] buffer = new byte[BUFFER];
		int length = 0;
		while ((length = is.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
			baos.flush();
		}
		// ��byte[] ��������һ��ͼƬ��������Ϣ
		byte[] bytes = baos.toByteArray();

		// ��ȡͼƬ��ԭʼ�ߴ�
		Options opts = new Options();
		// �������ر߽�����
		// inJustDecodeBounds: �����ֵ��Ϊtrue��ô��������ʵ�ʵ�bitmap��Ҳ����������ڴ�ռ������ͱ����ڴ�����ˡ�
		// �����������ǲ�ѯͼƬ����Ϣ�����оͰ���ͼƬ��С��Ϣ
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

		// ����ԭʼ�ߴ���width��height����ѹ������
		int w = opts.outWidth / width;
		int h = opts.outHeight / height;
		Log.i("jian", "ͼƬԭʼ�ߴ�: (" + opts.outWidth + ", " + opts.outHeight + ")");
		int scale = w > h ? w : h;
		opts.inSampleSize = scale;
		Log.i("jian", "ͼƬѹ������: " + scale);

		// ִ��ѹ��
		opts.inJustDecodeBounds = false; // ���ͼƬ��Ҫ��ȡ����
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
				opts);
		return bitmap;
	}

	/**
	 * ��jpeg��ʽ�洢Bitmapͼ���ļ�
	 * 
	 * @param bitmap
	 *            Ŀ��ͼƬ
	 * @param file
	 *            �ļ��洢Ŀ¼
	 * @throws IOException
	 */
	public static void save(Bitmap bitmap, File file) throws IOException {
		if (!file.getParentFile().exists()) { // ��Ŀ¼������
			// ������Ŀ¼
			file.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
		// �ͷ���Դ
		fos.close();
		fos = null;
	}

	/**
	 * ͨ��һ������;��, �ȴӱ��س��Ի�ȡ, ��ȡ�����ٴ��������ز����浽�ļ���
	 * 
	 * @param path
	 *            ����·��
	 * @param callback
	 *            ͼƬ���غ�ص�
	 * @return bitmap
	 */
	public static void loadBitmap(final String path,
			final BitmapCallback callback) {
		if(path == null || path.equals("")){
			callback.onBitmapLoaded(null);
			return;
		}

		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {
			
			@Override
			protected Bitmap doInBackground(String... params) {

				// �ȴ��ڴ滺���ж�ȡ
				// �ٴ��ļ������ж�ȡ
				String filename = path.substring(path.lastIndexOf("/") + 1);
				final File file = new File(MusicApplication.getApplication()
						.getCacheDir(), "images/" + filename);
				Bitmap bitmap = loadBitmap(file, 0);

				if (bitmap != null) { // ���ͼƬ�ܴӻ������ҵ�,����; ���������������
					return bitmap;
				}
				// ����������
				try {
					InputStream is = HttpUtils.getInputStream(path);
					bitmap = BitmapFactory.decodeStream(is);

					// ���浽�ļ���
					save(bitmap, file);
					return bitmap;
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;

			}

			// ���߳�ִ�лص�����
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				callback.onBitmapLoaded(bitmap);
			}
		};
		task.execute();
	}

	/**
	 * �첽����ͼƬ, ͨ����Դ·��
	 * 
	 * @param file
	 *            ��Դ·��
	 * @param scale
	 *            ѹ������
	 * @param callback
	 */
	public static void loadBitmap(final String path, final int scale,
			final BitmapCallback callback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				// ���ļ������ж�ȡ
				String filename = path.substring(path.lastIndexOf("/") + 1);
				File file = new File(MusicApplication.getApplication()
						.getCacheDir(), "images/" + filename);
				Bitmap bitmap = loadBitmap(file, scale);
				if (bitmap != null) { // ͼƬ����
					return bitmap;
				}
				// ����������ԭͼ, �����浽�ļ�������
				try {
					InputStream is = HttpUtils.getInputStream(path);
					// ����ͼƬ��ѹ��
					bitmap = BitmapFactory.decodeStream(is);

					// δѹ��ԭͼ���浽�ļ���
					save(bitmap, file);

					// ���ļ��ж�ȡbitmap, ������scale����ѹ��
					bitmap = loadBitmap(file, scale);

					// �ͷ���Դ
					is.close();
					is = null;

					return bitmap;
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}
		};

		task.execute();
	}

	/**
	 * ���ļ��м���һ��Bitmap�����
	 * 
	 * @param file
	 *            �ڲ��ļ��洢Ŀ¼
	 * @param scale
	 *            ѹ������ scale == 0 ��ѹ��
	 * @return bitmap
	 */
	public static Bitmap loadBitmap(File file, int scale) {
		if (!file.exists()) { // �ļ�������
			return null;
		}
		if (scale == 0) { // ����ѹ��
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			return bitmap;
		} else { // ѹ��������Ϊ0ʱ
			Options opts = new Options();
			opts.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),
					opts);
			return bitmap;
		}

	}

	/**
	 * ��ΪcreateBlurBitmap�ȽϺ�ʱ, �첽����һ��ģ��ͼƬ
	 * 
	 * @param bitmap
	 *            ԴͼƬ
	 * @param callback
	 * @return bitmap
	 */
	public static void loadBlurBitmap(final Bitmap sourceBitmap,
			final BitmapCallback callback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				int radius = 5; // ģ���뾶
				Bitmap bitmap = createBlurBitmap(sourceBitmap, radius);
				return bitmap;
			}

			// �����߳��лص��������
			@Override
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}

		};
		task.execute();
	}

	/**
	 * ����bitmap ����ģ���뾶 ����һ����ģ����bitmap �ȽϺ�ʱ (radiusԽ��Խ��ʱ)
	 * 
	 * @param sentBitmap
	 *            ���ݵ�bitmap
	 * @param radius
	 *            ģ���뾶
	 * @return bitmap
	 */
	public static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		if (radius < 1) {
			return (null);
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);

		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}

			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);

				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;

			}
			yw += w;

		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;

				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}
}
